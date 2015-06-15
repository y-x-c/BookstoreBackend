<%--
  Created by IntelliJ IDEA.
  User: Orthocenter
  Date: 15/6/10
  Time: 21:58
  To change this template use File | Settings | File Templates.
--%>

<%@ page contentType="text/json; charset=UTF-8" language="java" %>
<%@ page language="java" import="YuxinBookstore.*" %>

<%
    String verb = request.getMethod().toUpperCase();
    String URI = request.getRequestURI();
    String baseURI = "/api/";
    String[] dirs = URI.substring(baseURI.length(), URI.length()).split("/");


    if(dirs.length == 2 && dirs[0].equals("books") && verb.equals("GET")) {
        System.err.println("Forward to Book.details() ");
        String isbn = dirs[1];
        Book book = new Book();
        String result = book.details(-1, isbn);

        if(result == null)
            response.sendError(response.SC_NOT_FOUND, "Book not found");
        else
            out.println(result);
    } else {
        response.sendError(response.SC_METHOD_NOT_ALLOWED, "Method you used is not allowed. Request URI: " + URI);
    }
%>
