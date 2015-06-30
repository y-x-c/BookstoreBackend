package YuxinBookstore;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.io.*;
import java.sql.ResultSet;
import java.util.ArrayList;

/**
 * Created by Orthocenter on 5/14/15.
 */
public class Author {

    private static JsonObjectBuilder JSONAuthor(ResultSet rs, JsonObjectBuilder author) throws Exception {
        final String authid = rs.getString("authid");
        final String authname = rs.getString("authname");
        final String intro = rs.getString("intro");

        author.add("id", authid);
        author.add("name", authname);
        author.add("intro", intro == null ? "" : intro);
        Connector con = null;
        try {
            String sql = "SELECT * FROM WrittenBy W WHERE" +
                    " W.authid = " + authid;

            con = new Connector();
            ResultSet rs2 = con.stmt.executeQuery(sql);
            JsonArrayBuilder books = Json.createArrayBuilder();
            while (rs2.next()) {
                final String isbn = rs2.getString("W.isbn");
                books.add(isbn);
            }
            author.add("books", books);
            con.closeConnection();
        } catch (Exception e) {
            if(con!=null) con.closeConnection();
            throw new Exception();
        }

        if(con!=null) con.closeConnection();
        return author;
    }

    private static JsonObjectBuilder JSONAuthor(String authid, JsonObjectBuilder author) throws Exception {
        Connector con = null;
        try {
            con = new Connector();
            String sql = "SELECT * from Author where authid = '" + authid + "'";
            ResultSet rs = con.stmt.executeQuery(sql);
            rs.next();
            author = JSONAuthor(rs, author);
            if (con != null) con.closeConnection();
            return author;
        } catch(Exception e) {
            if(con!=null) con.closeConnection();
            throw new Exception();
        }
    }

    public static String add(JsonObject payload) {
        JsonObject publisher = payload.getJsonObject("author");
        String authname = publisher.getString("name");
        String intro = publisher.isNull("intro") ? null : publisher.getString("intro");
        JsonObjectBuilder result = Json.createObjectBuilder();
        JsonObjectBuilder newAuthor = Json.createObjectBuilder();
        Connector con = null;
        try {
            String sql = "INSERT INTO Author (authname, intro) VALUES (";
            sql += Utility.genStringAttr(authname, ",");
            sql += Utility.genStringAttr(intro, "");
            sql += ")";

            con = new Connector();
            con.newStatement();
            con.stmt.execute(sql);

            sql = "SELECT LAST_INSERT_ID() AS id";
            ResultSet rs = con.stmt.executeQuery(sql);
            rs.next();

            newAuthor.add("id", rs.getString("id"));
            con.closeConnection();
        } catch (Exception e) {
            System.out.println("Failed to add author");
            System.err.println(e.getMessage());

            if(con!=null) con.closeConnection(); return null;
        }


        result.add("author", newAuthor);
        if(con!=null) con.closeConnection();
        return result.build().toString();
    }

    public static String details(final int authid) {
        JsonObjectBuilder result = Json.createObjectBuilder();
        JsonObjectBuilder author = Json.createObjectBuilder();
        Connector con = null;

        try {
            con = new Connector();
            System.err.println("Connected to the database.");
        } catch (Exception e) {
            System.err.println("Cannot connect to the database.");
            System.err.println(e.getMessage());

            if(con!=null) con.closeConnection(); return null;
        }

        String sql = "SELECT * FROM Author WHERE authid = " + authid;
        try {
            ResultSet rs = con.stmt.executeQuery(sql);
            rs.next();

            author = JSONAuthor(rs, author);
            con.closeConnection();
        } catch (Exception e) {
            System.err.println(e.getMessage());

            if(con!=null) con.closeConnection(); return null;
        }


        result.add("author", author);
        if(con!=null) con.closeConnection(); return result.build().toString();
    }

    public static String find(int limit, String name) {
        JsonObjectBuilder result = Json.createObjectBuilder();
        JsonArrayBuilder authors = Json.createArrayBuilder();
        Connector con = null;
        String sql = "SELECT * FROM Author A WHERE A.authname LIKE";
        name = Utility.sanitize(name);
        sql += "'%" + name + "%'";
        sql += " LIMIT " + limit;

        try {
            con = new Connector();
            con.newStatement();

            ResultSet rs = con.stmt.executeQuery(sql);

            while (rs.next()) {
                JsonObjectBuilder author = Json.createObjectBuilder();
                author.add("id", rs.getInt("authid"));
                author.add("name", rs.getString("authname"));
                String intro = rs.getString("intro");
                author.add("intro", intro == null ? "" : intro);
                authors.add(author);
            }
            con.closeConnection();
        } catch (Exception e) {
            System.out.println("Failed to search author");
            System.err.println(e.getMessage());
            if(con!=null) con.closeConnection(); return null;
        }

        result.add("authors", authors);
        if(con!=null) con.closeConnection(); return result.build().toString();
    }

    public static String degree(String authid1, String authid2) {
        JsonObjectBuilder _result = Json.createObjectBuilder(); //wrapper
        JsonObjectBuilder result = Json.createObjectBuilder();
        Connector con = null;
        try {
            if (authid1.equals(authid2)) {
                result.add("degree", 0);
                if(con!=null) con.closeConnection(); return _result.add("result", result).build().toString();
            }

            String sql = "SELECT W1.isbn FROM WrittenBy W1, WrittenBy W2 WHERE W1.isbn = W2.isbn " +
                    "AND W1.authid = " + authid1 + " AND W2.authid = " + authid2;

            con = new Connector();
            ResultSet rs = con.stmt.executeQuery(sql);

            if (rs.next()) {
                result.add("degree", 1);
                if(con!=null) con.closeConnection(); return _result.add("result", result).build().toString();
            }

            sql = "SELECT W1.isbn, W4.isbn FROM WrittenBy W1, WrittenBy W2, WrittenBy W3, WrittenBy W4 WHERE " +
                    "W1.isbn = W2.isbn AND W3.isbn = W4.isbn AND W1.authid = " + authid1 +
                    " AND W2.authid = W3.authid AND W4.authid = " + authid2;
            rs = con.stmt.executeQuery(sql);

            if (rs.next()) {
                result.add("degree", 2);
                if(con!=null) con.closeConnection(); return _result.add("result", result).build().toString();
            }

            result.add("degree", 3);

            if(con!=null) con.closeConnection(); return _result.add("result", result).build().toString();
        } catch (Exception e) {
            System.out.println("Failed to query degree of two author");
            System.err.println(e.getMessage());
            if(con!=null) con.closeConnection(); return null;
        }
    }

    public static String popular(int limit, int offset, String start, String end) {
        JsonObjectBuilder result = Json.createObjectBuilder();
        JsonArrayBuilder authors = Json.createArrayBuilder();
        JsonObjectBuilder sales = Json.createObjectBuilder();
        String st = start.split("T")[0];
        String ed = end.split("T")[0];
        Connector con = null;
        try {
            String sql = "SELECT W.authid, SUM(I.amount) as sales FROM ItemInOrder I, WrittenBy W, Orders O " +
                    "WHERE I.isbn = W.isbn AND O.orderid = I.orderid AND O.time >= '" + st + "' AND O.time <= '" + ed +
                    "' GROUP BY W.authid ORDER BY SUM(I.amount) DESC";

            sql += " LIMIT " + limit + " OFFSET " + offset;

             con = new Connector();
            ResultSet rs = con.stmt.executeQuery(sql);

            while (rs.next()) {
                final String authid = rs.getString("W.authid");

                final int _sales = rs.getInt("sales");

                JsonObjectBuilder author = Json.createObjectBuilder();
                author = JSONAuthor(authid, author);
                authors.add(author);

                sales.add(authid, _sales);
            }

            sql = "SELECT COUNT(DISTINCT W.authid) AS total FROM ItemInOrder I, WrittenBy W, Orders O " +
                    "WHERE I.isbn = W.isbn AND O.orderid = I.orderid AND O.time >= '" + st + "' AND O.time <= '" + ed + "'";
            rs = con.stmt.executeQuery(sql);
            rs.next();
            JsonObjectBuilder meta = Json.createObjectBuilder();
            meta.add("total", rs.getInt("total"));

            result.add("meta", meta);
            if(con!=null) con.closeConnection(); return result.add("authors", authors).add("sales", sales).build().toString();
        } catch (Exception e) {
            System.out.println("Failed to query popular authors");
            System.err.println(e.getMessage());
            if(con!=null) con.closeConnection(); return null;
        }
    }

};