<%--
  Created by IntelliJ IDEA.
  User: Orthocenter
  Date: 15/6/10
  Time: 21:58
  To change this template use File | Settings | File Templates.
--%>

<%@ page isErrorPage="true" contentType="text/json; charset=UTF-8" language="java" %>
<%@ page language="java" import="YuxinBookstore.*" %>
<%@ page import="java.io.InputStream" %>
<%@ page import="java.io.ByteArrayInputStream" %>
<%@ page import="javax.json.*" %>
<%@ page import="java.nio.charset.StandardCharsets" %>

<%
    String verb = request.getMethod().toUpperCase();
    String URI = request.getRequestURI();
    String baseURI = "/api/";
    String[] dirs = URI.substring(baseURI.length(), URI.length()).split("/");
    System.err.println(verb + " " + URI);

    int sessionCid = session.getAttribute("cid") == null ? -1 : (Integer)session.getAttribute("cid");
    int authcid = sessionCid;
    int isAdmin = session.getAttribute("isAdmin") == null ? 0 : (Integer)session.getAttribute("isAdmin");

    System.err.println(session.getId() + " " + session.isNew() + " " + session.getAttribute("cid") + " " + session.getAttribute("isAdmin"));
    authcid = 2; isAdmin = 1;

        // GET /whoAmI
    if (dirs.length == 1 && dirs[0].equals("whoAmI")) {
        System.out.println("Forwarding to Customer.whoAmI()");
        String result = Customer.whoAmI(authcid, request.getRemoteAddr());

        if (result == null) {
            response.sendError(response.SC_NOT_FOUND, (String)request.getRemoteAddr());
        } else {
            out.println(result);
        }

        // login
        // POST /customers/login
    } else if (dirs.length == 2 && dirs[0].equals("customers") && dirs[1].equals("login") && verb.equals("POST")) {
        System.err.println("Forwarding to Customer.login()");
        InputStream body = request.getInputStream();
        JsonReader jsonReader = Json.createReader(body);
        JsonObject payload = jsonReader.readObject();
        JsonObjectBuilder result = Json.createObjectBuilder();

        String[] states = Customer.login(payload, result).split("/");
        isAdmin = Integer.parseInt(states[0]);
        int cid = Integer.parseInt(states[1]);

        if(cid > 0) {
            out.println(result.build().toString());
            session.setAttribute("cid", cid);
            session.setAttribute("isAdmin", isAdmin);
            System.err.println(session.getId() + " " + session.isNew() + " " + session.getAttribute("cid") + " " + session.getAttribute("isAdmin"));
        } else {
            response.sendError(response.SC_NOT_FOUND);
            session.invalidate();
        }

        // logout
        // POST /customers/logout
    } else if (dirs.length == 2 && dirs[0].equals("customers") && dirs[1].equals("logout") && verb.equals("POST")) {
        System.err.println("cid: " + sessionCid + " logged out");
        session.invalidate();

        // signup
        // POST /customers/signup
    } else if (dirs.length == 2 && dirs[0].equals("customers") && dirs[1].equals("signup") && verb.equals("POST")) {
        System.err.println("Forwarding to Customer.signup()");
        InputStream body = request.getInputStream();
        JsonReader jsonReader = Json.createReader(body);
        JsonObject payload = jsonReader.readObject();
        JsonObjectBuilder result = Json.createObjectBuilder();

        int cid = Customer.signup(payload, result);

        if(cid > 0) {
            out.println(result.build().toString());
            session.setAttribute("cid", cid);
        } else {
            response.sendError(response.SC_NOT_FOUND);
            session.invalidate();
        }

        // (ADMIN) get sales data from 'start' to 'end'
        // 'span' has not supported yet
        // GET /customers/visits?start=&end=
    } else if (dirs.length == 2 && dirs[0].equals("customers") && dirs[1].equals("visits") && verb.equals("GET")) {
        System.err.println("Forwarding to Customer.visits()");
        if (isAdmin == 0) {
            response.sendError(response.SC_NOT_FOUND);
        }

        String start = request.getParameter("start");
        String end = request.getParameter("end");
        String span = request.getParameter("span");

        String result = Customer.visits(start, end, span);
        if (result == null) {
            response.sendError(response.SC_NOT_FOUND);
        } else {
            out.println(result);
        }

        // (ADMIN) get lastest orders
        // GET /orders/latest
    } else if (dirs.length == 2 && dirs[0].equals("orders") && dirs[1].equals("latest") && verb.equals("GET")) {
        if(authcid < 0 || isAdmin == 0) {
            response.sendError(response.SC_NOT_FOUND);
        }

        System.err.println("Forwarding to Orders.latest()");

        String _limit = request.getParameter("limit");
        String _offset = request.getParameter("offset");
        int limit, offset;
        if(_limit == null) limit = 5; else limit = Integer.parseInt(_limit);
        if(_offset == null) offset = 0; else offset = Integer.parseInt(_offset);

        String result = Order.latest(limit, offset);
        if (result == null) {
            response.sendError(response.SC_NOT_FOUND);
        } else {
            out.println(result);
        }
        // get popular books
        // GET /books/popular?start=&end=&limit=&offset=
    } else if (dirs.length == 2 && dirs[0].equals("books") && dirs[1].equals("popular") && verb.equals("GET")) {
        System.err.println("Forwarding to Book.popular()");
        String start = request.getParameter("start");
        String end = request.getParameter("end");
        String _limit = request.getParameter("limit");
        String _offset = request.getParameter("offset");
        int limit, offset;
        if (_limit == null) limit = 5; else limit = Integer.parseInt(_limit);
        if(_offset == null) offset = 0; else offset = Integer.parseInt(_offset);

        String result = Book.popular(limit, offset, start, end);
        if (result == null) {
            response.sendError(response.SC_NOT_FOUND);
        } else {
            out.println(result);
        }

        // get details
        // GET /books/:ISBN
    } else if (dirs.length == 2 && dirs[0].equals("books") && verb.equals("GET")) {
        System.err.println("Forwarding to Book.details() ");
        String isbn = dirs[1];

        String result = Book.details(isbn);
        if (result == null) {
            response.sendError(response.SC_NOT_FOUND, "Book not found");
        } else {
            out.println(result);
        }

        // get popular authors
        // GET /authors/popular?start=&end=&limit=&offset=
    } else if (dirs.length == 2 && dirs[0].equals("authors") && dirs[1].equals("popular") && verb.equals("GET")) {
        System.err.println("Forwarding to Author.popular()");
        String start = request.getParameter("start");
        String end = request.getParameter("end");
        String _limit = request.getParameter("limit");
        String _offset = request.getParameter("offset");
        int limit, offset;
        if (_limit == null) limit = 5; else limit = Integer.parseInt(_limit);
        if(_offset == null) offset = 0; else offset = Integer.parseInt(_offset);

        String result = Author.popular(limit, offset, start, end);
        if (result == null) {
            response.sendError(response.SC_NOT_FOUND);
        } else {
            out.println(result);
        }

        // get details of a author
        // GET /authors/:authid
    } else if (dirs.length == 2 && dirs[0].equals("authors") && !dirs[1].equals("degree") && verb.equals("GET")) {
        System.err.println("Forwarding to Author.details()");
        int authid = Integer.parseInt(dirs[1]);

        String result = Author.details(authid);
        if (result == null) {
            response.sendError(response.SC_NOT_FOUND, "Author not found");
        } else {
            out.println(result);
        }

        // get popular publishers
        // GET /publishers/popular?start=&end=&limit=&offset=
    } else if (dirs.length == 2 && dirs[0].equals("publishers") && dirs[1].equals("popular") && verb.equals("GET")) {
        System.err.println("Forwarding to Publisher.popular()");
        String start = request.getParameter("start");
        String end = request.getParameter("end");
        String _limit = request.getParameter("limit");
        String _offset = request.getParameter("offset");
        int limit, offset;
        if (_limit == null) limit = 5; else limit = Integer.parseInt(_limit);
        if(_offset == null) offset = 0; else offset = Integer.parseInt(_offset);

        String result = Publisher.popular(limit, offset, start, end);
        if (result == null) {
            response.sendError(response.SC_NOT_FOUND);
        } else {
            out.println(result);
        }

        // get details of a publisher
        // GET /publishers/:pid
    } else if (dirs.length == 2 && dirs[0].equals("publishers") && verb.equals("GET")) {
        System.err.println("Forwarding to Publisher.details()");
        int pid = Integer.parseInt(dirs[1]);

        String result = Publisher.details(pid);
        if (result == null) {
            response.sendError(response.SC_NOT_FOUND, "Publisher not found");
        } else {
            out.println(result);
        }

        // get feedbacks of a certain book
        // GET /feedbacks?isbn=&limit=&offset=&orderBy
    } else if (dirs.length == 1 && dirs[0].equals("feedbacks") && verb.equals("GET")) {
        System.err.println("Forwarding to Feedback.feedbacks()");
        String _limit = request.getParameter("limit");
        String _offset = request.getParameter("offset");
        int limit, offset;
        if (_limit == null) limit = 5; else limit = Integer.parseInt(_limit);
        if(_offset == null) offset = 0; else offset = Integer.parseInt(_offset);
        String isbn = request.getParameter("isbn");
        String _orderBy = request.getParameter("orderBy");

        String result = Feedback.feedbacks(isbn, _orderBy, authcid, limit, offset);
        if (result == null) {
            response.sendError(response.SC_NOT_FOUND);
        } else {
            out.println(result);
        }

        // get details of a feedback
        // GET /feedbacks/:fid
    } else if (dirs.length == 2 && dirs[0].equals("feedbacks") && verb.equals("GET")) {
        System.err.println("Forwarding to Feedback.details()");
        int fid = Integer.parseInt(dirs[1]);
        ///////////////TBD
        int cid = 2;

        String result = Feedback.details(cid, fid);
        if (result == null) {
            response.sendError(response.SC_NOT_FOUND, "Feedback not found");
        } else {
            out.println(result);
        }

        // (LOGINED) assess a feedback
        // PUT /feedbacks/:fid
    } else if (dirs.length == 2 && dirs[0].equals("feedbacks") && verb.equals("PUT")) {
        System.err.println("Forwarding to Feedback.assess()");
        if(authcid < 0) {
            response.sendError(response.SC_NOT_FOUND);
        }

        int fid = Integer.parseInt(dirs[1]);

        InputStream body = request.getInputStream();
        JsonReader jsonReader = Json.createReader(body);
        JsonObject payload = jsonReader.readObject();

        String result = Feedback.assess(authcid, fid, payload);
        if (result == null) {
            response.sendError(response.SC_NOT_FOUND);
        } else {
            out.println(result);
        }

        // (LOGINED) new feedback
        // POST /feedbacks
    } else if (dirs.length == 1 && dirs[0].equals("feedbacks") && verb.equals("POST")) {
        System.err.println("Forwarding to Feedback.add()");
        if(authcid < 0) {
            response.sendError(response.SC_NOT_FOUND);
        }

        InputStream body = request.getInputStream();
        JsonReader jsonReader = Json.createReader(body);
        JsonObject payload = jsonReader.readObject();

        String result = Feedback.add(payload);
        if(result == null) {
            response.sendError(response.SC_NOT_FOUND);
        } else {
            out.println(result);
        }

        // (ADMIN) get most useful customers
        // GET /customers/useful?limit=&offset=
    } else if (dirs.length == 2 && dirs[0].equals("customers") && dirs[1].equals("useful") && verb.equals("GET")) {
        System.err.println("Forwarding to Customer.useful()");
        if(isAdmin == 0) {
            response.sendError(response.SC_NOT_FOUND);
        }

        String _limit = request.getParameter("limit");
        String _offset = request.getParameter("offset");
        int limit, offset;
        if(_limit == null) limit = 5; else limit = Integer.parseInt(_limit);
        if(_offset == null) offset = 0; else offset = Integer.parseInt(_offset);

        String result = Customer.useful(authcid, limit, offset);
        if (result == null) {
            response.sendError(response.SC_NOT_FOUND);
        } else {
            out.println(result);
        }

        // (ADMIN) get most trusted customers
        // GET /customers/trusted?limit=&offset=
    } else if (dirs.length == 2 && dirs[0].equals("customers") && dirs[1].equals("trusted") && verb.equals("GET")) {
        System.err.println("Forwarding to Customer.useful()");
        if(isAdmin == 0) {
            response.sendError(response.SC_NOT_FOUND);
        }

        String _limit = request.getParameter("limit");
        String _offset = request.getParameter("offset");
        int limit, offset;
        if(_limit == null) limit = 5; else limit = Integer.parseInt(_limit);
        if(_offset == null) offset = 0; else offset = Integer.parseInt(_offset);

        String result = Customer.trusted(authcid, limit, offset);
        if (result == null) {
            response.sendError(response.SC_NOT_FOUND);
        } else {
            out.println(result);
        }

        // username of cid
        // (AFTER LOGINED) extra details if the customer identified by authcid is trusted by cid
        // GET /customers/:cid
    } else if (dirs.length == 2 && dirs[0].equals("customers") && verb.equals("GET")) {
        System.err.println("Forwarding to Customers.details()");

        int cid = Integer.parseInt(dirs[1]);

        String result = Customer.details(authcid, cid);
        if (result == null) {
            response.sendError(response.SC_NOT_FOUND, "Customer not found");
        } else {
            out.println(result);
        }

        // (LOGINED) authcid trust or distrust cid
        // PUT /customers/:cid
    } else if (dirs.length == 2 && dirs[0].equals("customers") && verb.equals("PUT")) {
        System.err.println("Forwarding to Customers.trust()");
        if(authcid < 0) {
            response.sendError(response.SC_NOT_FOUND);
        }

        InputStream body = request.getInputStream();
        JsonReader jsonReader = Json.createReader(body);
        JsonObject payload = jsonReader.readObject();
        int cid = Integer.parseInt(dirs[1]);

        String result = Customer.trust(authcid, cid, payload);
        if (result == null) {
            response.sendError(response.SC_NOT_FOUND);
        } else {
            out.println(result);
        }

        // (LOGINED) authcid confirm a order
        // POST /orders
    } else if (dirs.length == 1 && dirs[0].equals("orders") && verb.equals("POST")) {
        System.err.println("Forwarding to Orders.add()");
        if(authcid < 0) {
            response.sendError(response.SC_NOT_FOUND);
        }

        InputStream body = request.getInputStream();
        JsonReader jsonReader = Json.createReader(body);
        JsonObject payload = jsonReader.readObject();

        String result = Order.add(authcid, payload);
        if (result == null) {
            response.sendError(response.SC_NOT_FOUND);
        } else {
            out.println(result);
        }

        // (ADMIN) get sales data from 'start' to 'end'
        // 'span' has not supported yet
        // GET /orders/orders?start=&end=
    } else if (dirs.length == 2 && dirs[0].equals("orders") && dirs[1].equals("orders") && verb.equals("GET")) {
        System.err.println("Forwarding to Order.orders()");
        if (isAdmin == 0) {
            response.sendError(response.SC_NOT_FOUND);
        }

        String start = request.getParameter("start");
        String end = request.getParameter("end");
        String span = request.getParameter("span");

        String result = Order.orders(start, end, span);
        if (result == null) {
            response.sendError(response.SC_NOT_FOUND);
        } else {
            out.println(result);
        }


        // (LOGINED) get order details
        // GET /orders/:orderid
    } else if (dirs.length == 2 && dirs[0].equals("orders") && verb.equals("GET")) {
        ////////////////////////////////////TBD
        if(authcid < 0) {
            response.sendError(response.SC_NOT_FOUND);
        }

        System.err.println("Forwarding to Orders.details()");
        int orderid = Integer.parseInt(dirs[1]);

        String result = Order.details(authcid, isAdmin, orderid);
        if (result == null) {
            response.sendError(response.SC_NOT_FOUND, "Order not found");
        } else {
            out.println(result);
        }

        // (LOGINED) get all items in the cart
        // GET /carts
    } else if (dirs.length == 1 && dirs[0].equals("carts") && verb.equals("GET")) {
        System.err.println("Forwarding to Order.cart()");

        if(authcid < 0) {
            response.sendError(response.SC_NOT_FOUND);
        }

        String result = Order.cart(authcid);
        if (result == null) {
            response.sendError(response.SC_NOT_FOUND);
        } else {
            out.println(result);
        }

        // (LOGINED) get items of the cart
        // GET /carts/:cart_id
    } else if (dirs.length == 2 && dirs[0].equals("carts") && verb.equals("GET")) {
        System.err.println("Forwarding to Order.cartDetails()");

        if(authcid < 0) {
            response.sendError(response.SC_NOT_FOUND);
        }

        String[] customerBook = dirs[1].split("-");
        int cid = Integer.parseInt(customerBook[0]);
        String isbn = customerBook[1];

        if(isAdmin == 0 && authcid != cid) {
            response.sendError(response.SC_NOT_FOUND);
        }

        String result = Order.cartDetails(cid, isbn);
        if (result == null) {
            response.sendError(response.SC_NOT_FOUND);
        } else {
            out.println(result);
        }

        // (LOGINED) add a new item
        // POST /carts
    } else if (dirs.length == 1 && dirs[0].equals("carts") && verb.equals("POST")) {
        System.err.println("Forwarding to Order.add2cart()");

        if(authcid < 0) {
            response.sendError(response.SC_NOT_FOUND);
        }

        InputStream body = request.getInputStream();
        JsonReader jsonReader = Json.createReader(body);
        JsonObject payload = jsonReader.readObject();

        String result = Order.add2Cart(authcid, payload);
        if (result == null) {
            response.sendError(response.SC_BAD_REQUEST);
        } else {
            out.println(result);
        }

        // (ADMIN) add a new publisher
        // POST /publishers
    } else if (dirs.length == 1 && dirs[0].equals("publishers") && verb.equals("POST")) {
        System.err.println("Forwarding to Publisher.add()");

        if(authcid < 0 || isAdmin == 0) {
            response.sendError(response.SC_NOT_FOUND);
        }

        InputStream body = request.getInputStream();
        JsonReader jsonReader = Json.createReader(body);
        JsonObject payload = jsonReader.readObject();

        String result = Publisher.add(payload);
        if (result == null) {
            response.sendError(response.SC_BAD_REQUEST);
        } else {
            out.println(result);
        }

        // (ADMIN) add a new author
        // POST /authors
    } else if (dirs.length == 1 && dirs[0].equals("authors") && verb.equals("POST")) {
        System.err.println("Forwarding to Author.add()");

        if(authcid < 0 || isAdmin == 0) {
            response.sendError(response.SC_NOT_FOUND);
        }

        InputStream body = request.getInputStream();
        JsonReader jsonReader = Json.createReader(body);
        JsonObject payload = jsonReader.readObject();

        String result = Author.add(payload);
        if (result == null) {
            response.sendError(response.SC_BAD_REQUEST);
        } else {
            out.println(result);
        }

        // (ADMIN) add a new book
        // POST /books
    } else if (dirs.length == 1 && dirs[0].equals("books") && verb.equals("POST")) {
        System.err.println("Forwarding to Book.add()");

        if(authcid < 0 || isAdmin == 0) {
            response.sendError(response.SC_NOT_FOUND);
        }

        InputStream body = request.getInputStream();
        JsonReader jsonReader = Json.createReader(body);
        JsonObject payload = jsonReader.readObject();

        String result = Book.add(payload);
        if (result == null) {
            response.sendError(response.SC_BAD_REQUEST);
        } else {
            out.println(result);
        }

        // predict publisher
        // GET /publishers?name=&limit=
    } else if (dirs.length == 1 && dirs[0].equals("publishers") && verb.equals("GET")) {
        System.err.println("Forwarding to Publisher.find()");
        String name = request.getParameter("name");
        String _limit = request.getParameter("limit");
        int limit = 5;

        String result = Publisher.find(limit, name);
        if (result == null) {
            response.sendError(response.SC_NOT_FOUND);
        } else {
            out.println(result);
        }

        // predict author
        // GET /publisher?name=&limit=
    } else if (dirs.length == 1 && dirs[0].equals("authors") && verb.equals("GET")) {
        System.err.println("Forwarding to Author.find()");
        String name = request.getParameter("name");
        String _limit = request.getParameter("limit");
        int limit = 5;
        if (_limit != null) limit = Integer.parseInt(_limit);

        String result = Author.find(limit, name);
        if (result == null) {
            response.sendError(response.SC_NOT_FOUND);
        } else {
            out.println(result);
        }

        // simple/advanced book search
        // GET /books?all=  /books?advanced=
    } else if (dirs.length == 1 && dirs[0].equals("books") && verb.equals("GET")) {

        String orderBy = request.getParameter("orderBy");
        String all = request.getParameter("all");
        String advanced = request.getParameter("advanced");
        String popular = request.getParameter("popular");
        String _limit = request.getParameter("limit");
        String _offset = request.getParameter("offset");
        String start = request.getParameter("start");
        String end = request.getParameter("end");
        int limit, offset;
        if(_limit == null) limit = 5; else limit = Integer.parseInt(_limit);
        if(_offset == null) offset = 0; else offset = Integer.parseInt(_offset);

        String result = null;
        if(all != null && all.length() > 0) {
            System.err.println("Forwarding to Books.simpleSearch()");
            result = Book.simpleSearch(sessionCid, limit, offset, all, orderBy);
        } else if(advanced != null && advanced.length() > 0) {
            System.err.println("Forwarding to Books.advancedSearch()");
            InputStream stream = new ByteArrayInputStream(advanced.getBytes(StandardCharsets.UTF_8));
            JsonReader jsonReader = Json.createReader(stream);
            JsonArray conditions = jsonReader.readArray();
            result = Book.advancedSearch(sessionCid, limit, offset, conditions, orderBy);
        } else {
            System.err.println("Forwarding to Books.popular()");
            result = Book.popular(limit, offset, start, end);
        }

        if (result == null) {
            response.sendError(response.SC_NOT_FOUND);
        } else {
            out.println(result);
        }

        // (ADMIN) update inventory of a book
        // PUT /books/:isbn
    } else if (dirs.length == 2 && dirs[0].equals("books") && verb.equals("PUT")) {
        System.err.println("Forwarding to Book.add()");

        if(authcid < 0 || isAdmin == 0) {
            response.sendError(response.SC_NOT_FOUND);
        }

        InputStream body = request.getInputStream();
        JsonReader jsonReader = Json.createReader(body);
        JsonObject payload = jsonReader.readObject();

        String result = Book.add(payload);
        if (result == null) {
            response.sendError(response.SC_BAD_REQUEST);
        } else {
            out.println(result);
        }

        // update amount of a certain cart item
        // PUT /carts/:cart_id
    } else if (dirs.length == 2 && dirs[0].equals("carts") && verb.equals("PUT")) {
        System.err.println("Forwarding to Order.add2Cart()");

        if(authcid < 0) {
            response.sendError(response.SC_NOT_FOUND);
        }

        InputStream body = request.getInputStream();
        JsonReader jsonReader = Json.createReader(body);
        JsonObject payload = jsonReader.readObject();

        String result = Order.add2Cart(authcid, payload);
        if (result == null) {
            response.sendError(response.SC_BAD_REQUEST);
        } else {
            out.println(result);
        }

        // (ADMIN)get degree of two authors
        // GET /authors/degree
    } else if (dirs.length == 2 && dirs[0].equals("authors") && dirs[1].equals("degree") && verb.equals("GET")) {
        System.err.println("Forwarding to Author.degree()");

        if(authcid < 0 || isAdmin == 0) {
            response.sendError(response.SC_NOT_FOUND);
        }

        String author1 = request.getParameter("author1");
        String author2 = request.getParameter("author2");

        String result = Author.degree(author1, author2);
        if (result == null) {
            response.sendError(response.SC_NOT_FOUND);
        } else {
            out.println(result);
        }


    } else {
        System.err.println("Unknown method");
        response.sendError(response.SC_METHOD_NOT_ALLOWED, "Method you used is not allowed. Request URI: " + URI);
    }
%>