package YuxinBookstore;

import javax.json.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.util.ArrayList;

/**
 * Created by Orthocenter on 5/14/15.
 */
public class Publisher {

    private static JsonObjectBuilder JSONPublisher(ResultSet rs, JsonObjectBuilder publisher) throws Exception {
        final String pid = rs.getString("pid");
        final String pubname = rs.getString("pubname");
        final String intro = rs.getString("intro");

        publisher.add("id", pid);
        publisher.add("name", pubname);
        publisher.add("intro", intro == null ? "" : intro);

        return publisher;
    }

    private static JsonObjectBuilder JSONPublisher(String pid, JsonObjectBuilder publisher) throws Exception {
        Connector con = null;
        try {
            con = new Connector();
            String sql = "SELECT * from Publisher where pid = '" + pid + "'";
            ResultSet rs = con.stmt.executeQuery(sql);
            rs.next();
            if (con != null) con.closeConnection();
            return JSONPublisher(rs, publisher);
        } catch(Exception e) {
            if(con!=null) con.closeConnection();
            throw new Exception();
        }
    }


    public static String details(final int pid) {
        JsonObjectBuilder result = Json.createObjectBuilder();
        Connector con = null;

        try {
            con = new Connector();
            System.err.println("Connected to the database.");
        } catch (Exception e) {
            System.err.println("Cannot connect to the database.");
            System.err.println(e.getMessage());

            if(con!=null) con.closeConnection(); return null;
        }

        // get details
        String sql = "SELECT * FROM Publisher P"
                + " WHERE pid = " + pid;

        ResultSet rs = null;
        try {
            rs = con.stmt.executeQuery(sql);
        } catch (Exception e) {
            System.out.println("Failed to get details");
            System.err.println(e.getMessage());

            if(con!=null) con.closeConnection(); return null;
        }

        JsonObjectBuilder publisher = Json.createObjectBuilder();
        try {
            rs.next();

            publisher = JSONPublisher(rs, publisher);
        } catch (Exception e) {
            System.out.println("Failed to add details into result");
            System.err.println(e.getMessage());

            if(con!=null) con.closeConnection(); return null;
        }

        //get books
        sql = "SELECT * FROM Book B"
                + " WHERE pid = " + pid;

        try {
            rs = con.stmt.executeQuery(sql);
        } catch (Exception e) {
            System.out.println("Failed to get details");
            System.err.println(e.getMessage());

            if(con!=null) con.closeConnection(); return null;
        }

        try {
            JsonArrayBuilder books = Json.createArrayBuilder();
            while (rs.next()) {
                books.add(rs.getString("isbn"));
            }
            publisher.add("books", books);

        } catch (Exception e) {
            System.out.println("Failed to add books into result");
            System.err.println(e.getMessage());

            if(con!=null) con.closeConnection(); return null;
        }

        result.add("publisher", publisher);
        if(con!=null) con.closeConnection(); return result.build().toString();
    }

    public static String add(JsonObject payload) {
        JsonObject publisher = payload.getJsonObject("publisher");
        String pubname = publisher.getString("name");
        String intro = publisher.isNull("intro") ? null : publisher.getString("intro");
        JsonObjectBuilder result = Json.createObjectBuilder();
        JsonObjectBuilder newPublisher = Json.createObjectBuilder();
        Connector con = null;
        try {
            String sql = "INSERT INTO Publisher (pubname, intro) VALUES (";
            sql += Utility.genStringAttr(pubname, ",");
            sql += Utility.genStringAttr(intro, "");
            sql += ")";

            con = new Connector();
            con.newStatement();
            con.stmt.execute(sql);

            sql = "SELECT LAST_INSERT_ID() AS id";
            ResultSet rs = con.stmt.executeQuery(sql);
            rs.next();

            newPublisher.add("id", rs.getString("id"));
        } catch (Exception e) {
            System.out.println("Failed to add publisher");
            System.err.println(e.getMessage());

            if(con!=null) con.closeConnection(); return null;
        }

        result.add("publisher", newPublisher);
        if(con!=null) con.closeConnection(); return result.build().toString();
    }

    public static String find(int limit, String name) {
        JsonObjectBuilder result = Json.createObjectBuilder();
        JsonArrayBuilder publishers = Json.createArrayBuilder();

        String sql = "SELECT * FROM Publisher P WHERE P.pubname LIKE";
        name = Utility.sanitize(name);
        sql += "'%" + name + "%'";
        sql += " LIMIT " + limit;
        Connector con = null;
        try {
            con = new Connector();
            con.newStatement();

            ResultSet rs = con.stmt.executeQuery(sql);

            while (rs.next()) {
                JsonObjectBuilder publisher = Json.createObjectBuilder();
                publisher.add("id", rs.getInt("pid"));
                publisher.add("name", rs.getString("pubname"));
                String intro = rs.getString("intro");
                publisher.add("intro", intro == null ? "" : intro);
                publishers.add(publisher);
            }

        } catch (Exception e) {
            System.out.println("Failed to search author");
            System.err.println(e.getMessage());
            if(con!=null) con.closeConnection(); return null;
        }

        result.add("publishers", publishers);
        if(con!=null) con.closeConnection(); return result.build().toString();
    }

    public static String popular(int limit, int offset, String start, String end) {
        JsonObjectBuilder result = Json.createObjectBuilder();
        JsonArrayBuilder publishers = Json.createArrayBuilder();
        JsonObjectBuilder sales = Json.createObjectBuilder();
        String st = start.split("T")[0];
        String ed = end.split("T")[0];
        Connector con = null;
        try {
            String sql = "SELECT B.pid, SUM(I.amount) as sales FROM ItemInOrder I, Book B, Orders O " +
                    "WHERE I.isbn = B.isbn AND O.orderid = I.orderid AND O.time >= '" + st + "' AND O.time <= '" + ed +
                    "' GROUP BY B.pid ORDER BY SUM(I.amount) DESC";

            sql += " LIMIT " + limit + " OFFSET " + offset;

            con = new Connector();
            ResultSet rs = con.stmt.executeQuery(sql);

            while (rs.next()) {
                final String pid = rs.getString("B.pid");

                final int _sales = rs.getInt("sales");

                JsonObjectBuilder publisher = Json.createObjectBuilder();
                publisher = JSONPublisher(pid, publisher);
                publishers.add(publisher);

                sales.add(pid, _sales);
            }

            sql = "SELECT COUNT(DISTINCT B.isbn) AS total FROM ItemInOrder I, Book B, Orders O " +
                    "WHERE I.isbn = B.isbn AND O.orderid = I.orderid AND O.time >= '" + st + "' AND O.time <= '" + ed + "'";
            rs = con.stmt.executeQuery(sql);
            rs.next();

            JsonObjectBuilder meta = Json.createObjectBuilder();
            meta.add("total", rs.getInt("total"));

            result.add("meta", meta);
            if(con!=null) con.closeConnection(); return result.add("publishers", publishers).add("sales", sales).build().toString();
        } catch (Exception e) {
            System.out.println("Failed to query popular publishers");
            System.err.println(e.getMessage());
            if(con!=null) con.closeConnection(); return null;
        }
    }
};