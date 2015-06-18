<%--
  Created by IntelliJ IDEA.
  User: Orthocenter
  Date: 15/6/10
  Time: 21:58
  To change this template use File | Settings | File Templates.
--%>

<%@ page contentType="text/json; charset=UTF-8" language="java" %>
<%@ page language="java" import="YuxinBookstore.*" %>
<%@ page import="java.io.InputStream" %>
<%@ page import="javax.json.JsonReader" %>
<%@ page import="javax.json.Json" %>
<%@ page import="javax.json.JsonObject" %>

<%
    response.setHeader("Access-Control-Allow-Origin", "*");

    String verb = request.getMethod().toUpperCase();
    String URI = request.getRequestURI();
    String baseURI = "/api/";
    String[] dirs = URI.substring(baseURI.length(), URI.length()).split("/");
    System.err.println(verb + " " + URI);

        // /books/:ISBN
    if (dirs.length == 2 && dirs[0].equals("books") && verb.equals("GET")) {
        System.err.println("Forwarding to Book.details() ");
        String isbn = dirs[1];

        String result = Book.details(isbn);
        if (result == null) {
            response.sendError(response.SC_NOT_FOUND, "Book not found");
        } else {
            out.println(result);
        }

        // /authors/:aid
    } else if (dirs.length == 2 && dirs[0].equals("authors") && verb.equals("GET")) {
        System.err.println("Forwarding to Author.details()");
        int authid = Integer.parseInt(dirs[1]);

        String result = Author.details(authid);
        if (result == null) {
            response.sendError(response.SC_NOT_FOUND, "Author not found");
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

        // /feedbacks/:fid
    } else if (dirs.length == 2 && dirs[0].equals("feedbacks") && verb.equals("GET")) {
        System.err.println("Forwarding to Feedback.details()");
        int fid = Integer.parseInt(dirs[1]);

        String result = Feedback.details(fid);
        if (result == null) {
            response.sendError(response.SC_NOT_FOUND, "Feedback not found");
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

        // /orders/:orderid
    } else if (dirs.length == 2 && dirs[0].equals("orders") && verb.equals("GET")) {
        System.err.println("Forwarding to Orders.details()");
        int cid = Integer.parseInt(dirs[1]);

        String result = Order.details(cid);
        if (result == null) {
            response.sendError(response.SC_NOT_FOUND, "Order not found");
        } else {
            out.println(result);
        }

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

        String result = Publisher.find(name);
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