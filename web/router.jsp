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
    Boolean isAdmin = session.getAttribute("isAdmin") == null ? false : (Boolean)session.getAttribute("isAdmin");

    System.err.println(session.getId() + " " + session.isNew() + " " + session.getAttribute("cid"));
    // /customers/login
    if (dirs.length == 2 && dirs[0].equals("customers") && dirs[1].equals("login") && verb.equals("POST")) {
        System.err.println("Forwarding to Customer.login()");
        InputStream body = request.getInputStream();
        JsonReader jsonReader = Json.createReader(body);
        JsonObject payload = jsonReader.readObject();
        JsonObjectBuilder result = Json.createObjectBuilder();

        int cid = Customer.login(payload, result);

        if(cid > 0) {
            out.println(result.build().toString());
            session.setAttribute("cid", cid);
            System.err.println(session.getId() + " " + session.isNew() + " " + session.getAttribute("cid"));
        } else {
            response.sendError(response.SC_NOT_FOUND);
            session.invalidate();
        }

        // /customers/logout
    } else if (dirs.length == 2 && dirs[0].equals("customers") && dirs[1].equals("logout") && verb.equals("POST")) {
        System.err.println("cid: " + sessionCid + " logged out");
        session.invalidate();

        // /customers/signup
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

        // /books/popular
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

        // /books/:ISBN
    } else if (dirs.length == 2 && dirs[0].equals("books") && verb.equals("GET")) {
        System.err.println("Forwarding to Book.details() ");
        String isbn = dirs[1];

        String result = Book.details(isbn);
        if (result == null) {
            response.sendError(response.SC_NOT_FOUND, "Book not found");
        } else {
            out.println(result);
        }

        // /authors/popular
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

        // /authors/:authid
    } else if (dirs.length == 2 && dirs[0].equals("authors") && !dirs[1].equals("degree") && verb.equals("GET")) {
        System.err.println("Forwarding to Author.details()");
        int authid = Integer.parseInt(dirs[1]);

        String result = Author.details(authid);
        if (result == null) {
            response.sendError(response.SC_NOT_FOUND, "Author not found");
        } else {
            out.println(result);
        }

        // /publishers/popular
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

        // /publishers/:pid
    } else if (dirs.length == 2 && dirs[0].equals("publishers") && verb.equals("GET")) {
        System.err.println("Forwarding to Publisher.details()");
        int pid = Integer.parseInt(dirs[1]);

        String result = Publisher.details(pid);
        if (result == null) {
            response.sendError(response.SC_NOT_FOUND, "Publisher not found");
        } else {
            out.println(result);
        }

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

    } else if (dirs.length == 2 && dirs[0].equals("feedbacks") && verb.equals("PUT")) {
        System.err.println("Forwarding to Feedback.assess()");
        int fid = Integer.parseInt(dirs[1]);
        ///////////////TBD
        int cid = 2;

        InputStream body = request.getInputStream();
        JsonReader jsonReader = Json.createReader(body);
        JsonObject payload = jsonReader.readObject();

        String result = Feedback.assess(cid, fid, payload);
        if (result == null) {
            response.sendError(response.SC_NOT_FOUND);
        } else {
            out.println(result);
        }

        // /feedbacks
    } else if (dirs.length == 1 && dirs[0].equals("feedbacks") && verb.equals("POST")) {
        System.err.println("Forwarding to Feedback.add()");
        InputStream body = request.getInputStream();
        JsonReader jsonReader = Json.createReader(body);
        JsonObject payload = jsonReader.readObject();

        String result = Feedback.add(payload);
        if(result == null) {
            response.sendError(response.SC_NOT_FOUND);
        } else {
            out.println(result);
        }

        // /customers/useful
    } else if (dirs.length == 2 && dirs[0].equals("customers") && dirs[1].equals("useful") && verb.equals("GET")) {
        System.err.println("Forwarding to Customer.useful()");
        String _limit = request.getParameter("limit");
        String _offset = request.getParameter("offset");
        int limit, offset;
        if(_limit == null) limit = 5; else limit = Integer.parseInt(_limit);
        if(_offset == null) offset = 0; else offset = Integer.parseInt(_offset);

        String result = Customer.useful(limit, offset);
        if (result == null) {
            response.sendError(response.SC_NOT_FOUND);
        } else {
            out.println(result);
        }
        // /customers/trusted
    } else if (dirs.length == 2 && dirs[0].equals("customers") && dirs[1].equals("trusted") && verb.equals("GET")) {
        System.err.println("Forwarding to Customer.useful()");
        String _limit = request.getParameter("limit");
        String _offset = request.getParameter("offset");
        int limit, offset;
        if(_limit == null) limit = 5; else limit = Integer.parseInt(_limit);
        if(_offset == null) offset = 0; else offset = Integer.parseInt(_offset);

        String result = Customer.trusted(limit, offset);
        if (result == null) {
            response.sendError(response.SC_NOT_FOUND);
        } else {
            out.println(result);
        }

        // /customers/:cid
        // authentication required
    } else if (dirs.length == 2 && dirs[0].equals("customers") && verb.equals("GET")) {
        System.err.println("Forwarding to Customers.details()");
        int cid = Integer.parseInt(dirs[1]);

        String result = Customer.details(cid);
        if (result == null) {
            response.sendError(response.SC_NOT_FOUND, "Customer not found");
        } else {
            out.println(result);
        }

    } else if (dirs.length == 1 && dirs[0].equals("orders") && verb.equals("POST")) {
        System.err.println("Forwarding to Orders.add()");
        InputStream body = request.getInputStream();
        JsonReader jsonReader = Json.createReader(body);
        JsonObject payload = jsonReader.readObject();

        // sessionCid
        ///////////////////////////// TBD
        String result = Order.add(sessionCid, payload);
        if (result == null) {
            response.sendError(response.SC_NOT_FOUND);
        } else {
            out.println(result);
        }

        // /orders/orders
    } else if (dirs.length == 2 && dirs[0].equals("orders") && dirs[1].equals("orders") && verb.equals("GET")) {
        System.err.println("Forwarding to Order.orders()");
        String start = request.getParameter("start");
        String end = request.getParameter("end");
        String span = request.getParameter("span");

        String result = Order.orders(start, end, span);
        if (result == null) {
            response.sendError(response.SC_NOT_FOUND);
        } else {
            out.println(result);
        }

        // /orders/:orderid
    } else if (dirs.length == 2 && dirs[0].equals("orders") && verb.equals("GET")) {
        System.err.println("Forwarding to Orders.details()");
        int orderid = Integer.parseInt(dirs[1]);

        String result = Order.details(orderid);
        if (result == null) {
            response.sendError(response.SC_NOT_FOUND, "Order not found");
        } else {
            out.println(result);
        }

    } else if (dirs.length == 1 && dirs[0].equals("carts") && verb.equals("GET")) {
        System.err.println("Forwarding to Order.cart()");
//        String _limit = request.getParameter("limit");
//        String _offset = request.getParameter("offset");
//        int limit, offset;
//        if(_limit == null) limit = 5; else limit = Integer.parseInt(_limit);
//        if(_offset == null) offset = 0; else offset = Integer.parseInt(_offset);
        String _cid = request.getParameter("customer");
        int cid = Integer.parseInt(_cid);
        //////////////////// TBD
        // use SESSION ID

        String result = Order.cart(cid);
        if (result == null) {
            response.sendError(response.SC_NOT_FOUND);
        } else {
            out.println(result);
        }

    } else if (dirs.length == 2 && dirs[0].equals("carts") && verb.equals("GET")) {
        System.err.println("Forwarding to Order.cartDetails()");
//        String _limit = request.getParameter("limit");
//        String _offset = request.getParameter("offset");
//        int limit, offset;
//        if(_limit == null) limit = 5; else limit = Integer.parseInt(_limit);
//        if(_offset == null) offset = 0; else offset = Integer.parseInt(_offset);
        String[] customerBook = dirs[1].split("-");
        int cid = Integer.parseInt(customerBook[0]);
        String isbn = customerBook[1];

        //////////////////// TBD
        // use SESSION ID

        String result = Order.cartDetails(cid, isbn);
        if (result == null) {
            response.sendError(response.SC_NOT_FOUND);
        } else {
            out.println(result);
        }

        // POST /carts
    } else if (dirs.length == 1 && dirs[0].equals("carts") && verb.equals("POST")) {
        System.err.println("Forwarding to Order.add2cart()");
        InputStream body = request.getInputStream();
        JsonReader jsonReader = Json.createReader(body);
        JsonObject payload = jsonReader.readObject();

        String result = Order.add2Cart(sessionCid, payload);
        if (result == null) {
            response.sendError(response.SC_BAD_REQUEST);
        } else {
            out.println(result);
        }

        // POST /publishers
    } else if (dirs.length == 1 && dirs[0].equals("publishers") && verb.equals("POST")) {
        System.err.println("Forwarding to Publisher.add()");
        InputStream body = request.getInputStream();
        JsonReader jsonReader = Json.createReader(body);
        JsonObject payload = jsonReader.readObject();

        String result = Publisher.add(payload);
        if (result == null) {
            response.sendError(response.SC_BAD_REQUEST);
        } else {
            out.println(result);
        }
    } else if (dirs.length == 1 && dirs[0].equals("authors") && verb.equals("POST")) {
        System.err.println("Forwarding to Author.add()");
        InputStream body = request.getInputStream();
        JsonReader jsonReader = Json.createReader(body);
        JsonObject payload = jsonReader.readObject();

        String result = Author.add(payload);
        if (result == null) {
            response.sendError(response.SC_BAD_REQUEST);
        } else {
            out.println(result);
        }
    } else if (dirs.length == 1 && dirs[0].equals("books") && verb.equals("POST")) {
        System.err.println("Forwarding to Book.add()");
        InputStream body = request.getInputStream();
        JsonReader jsonReader = Json.createReader(body);
        JsonObject payload = jsonReader.readObject();

        String result = Book.add(payload);
        if (result == null) {
            response.sendError(response.SC_BAD_REQUEST);
        } else {
            out.println(result);
        }
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

        // GET /books?all=  /books?advanced=
    } else if (dirs.length == 1 && dirs[0].equals("books") && verb.equals("GET")) {
        System.err.println("Forwarding to Books.simpleSearch()");
        String orderBy = request.getParameter("orderBy");
        String all = request.getParameter("all");
        String advanced = request.getParameter("advanced");
        String _limit = request.getParameter("limit");
        String _offset = request.getParameter("offset");
        int limit, offset;
        if(_limit == null) limit = 5; else limit = Integer.parseInt(_limit);
        if(_offset == null) offset = 0; else offset = Integer.parseInt(_offset);

        String result = null;
        if(all != null && all.length() > 0) {
            result = Book.simpleSearch(sessionCid, limit, offset, all, orderBy);
        } else {
            InputStream stream = new ByteArrayInputStream(advanced.getBytes(StandardCharsets.UTF_8));
            JsonReader jsonReader = Json.createReader(stream);
            JsonArray conditions = jsonReader.readArray();
            result = Book.advancedSearch(sessionCid, limit, offset, conditions, orderBy);
        }

        if (result == null) {
            response.sendError(response.SC_NOT_FOUND);
        } else {
            out.println(result);
        }

        // PUT /books/:isbn
    } else if (dirs.length == 2 && dirs[0].equals("books") && verb.equals("PUT")) {
        System.err.println("Forwarding to Book.add()");
        InputStream body = request.getInputStream();
        JsonReader jsonReader = Json.createReader(body);
        JsonObject payload = jsonReader.readObject();

        String result = Book.add(payload);
        if (result == null) {
            response.sendError(response.SC_BAD_REQUEST);
        } else {
            out.println(result);
        }

        // PUT /carts/:cart_id
    } else if (dirs.length == 2 && dirs[0].equals("carts") && verb.equals("PUT")) {
        System.err.println("Forwarding to Order.add2Cart()");
        InputStream body = request.getInputStream();
        JsonReader jsonReader = Json.createReader(body);
        JsonObject payload = jsonReader.readObject();

        String result = Order.add2Cart(sessionCid, payload);
        if (result == null) {
            response.sendError(response.SC_BAD_REQUEST);
        } else {
            out.println(result);
        }
    } else if (dirs.length == 2 && dirs[0].equals("authors") && dirs[1].equals("degree") && verb.equals("GET")) {
        System.err.println("Forwarding to Author.degree()");
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