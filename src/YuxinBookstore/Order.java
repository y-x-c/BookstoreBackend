package YuxinBookstore;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.xml.transform.Result;
import java.awt.*;
import java.io.*;
import java.lang.reflect.Array;
import java.sql.ResultSet;
import java.util.ArrayList;


/**
 * Created by Orthocenter on 5/13/15.
 */


public class Order {

    private static JsonObjectBuilder JSONOrder(ResultSet rs, JsonObjectBuilder order) throws Exception {
        Connector con = null;
        try {
            int orderid = rs.getInt("orderid");
            order.add("id", orderid);
            order.add("time", rs.getString("time"));
            order.add("customer", rs.getInt("cid"));
            order.add("address", rs.getString("addr"));

            String sql = "SELECT SUM(I.price * I.amount) AS totalPrice FROM ItemInOrder I WHERE orderid = " + orderid;
            con = new Connector();
            ResultSet rs2 = con.stmt.executeQuery(sql);
            rs2.next();
            order.add("totalPrice", rs2.getDouble("totalPrice"));

            if (con != null) con.closeConnection();
            return order;
        } catch(Exception e) {
            if(con!=null) con.closeConnection();
            throw new Exception();
        }
    }

    private static JsonObjectBuilder JSONOrder(int orderid, JsonObjectBuilder order) throws Exception {
        Connector con = null;
        try {
            String sql = "SELECT * FROM Orders WHERE orderid = " + orderid;
            con = new Connector();
            ResultSet rs = con.stmt.executeQuery(sql);
            rs.next();
            order = JSONOrder(rs, order);
            if (con != null) con.closeConnection();
            return order;
        } catch (Exception e) {
            if(con!=null) con.closeConnection();
            throw new Exception();
        }
    }

    private static void JSONOrderItems(int orderid, JsonArrayBuilder items, JsonObjectBuilder order) throws Exception {
        Connector con = null;
        try {
            con = new Connector();
            String sql = "SELECT * FROM ItemInOrder WHERE orderid = " + orderid;
            JsonArrayBuilder itemids = Json.createArrayBuilder();
            ResultSet rs = con.stmt.executeQuery(sql);

            while (rs.next()) {
                itemids.add(rs.getInt("id"));

                JsonObjectBuilder item = Json.createObjectBuilder();
                item.add("id", rs.getInt("id"));
                item.add("order", rs.getInt("orderid"));
                item.add("book", rs.getString("isbn"));
                item.add("amount", rs.getInt("amount"));
                item.add("price", rs.getDouble("price"));
                items.add(item);
            }

            order.add("orderItems", itemids);
            con.closeConnection();
        } catch(Exception e) {
            if(con!=null) con.closeConnection();
            throw new Exception();
        }
    }

    public static String details(int authcid, int isAdmin, final int orderid) {
        JsonObjectBuilder result = Json.createObjectBuilder();
        Connector con = null;

        try {
            con = new Connector();
            System.err.println("Connected to the database.");
        } catch (Exception e) {
            System.err.println("Cannot connect to the database.");
            System.err.println(e.getMessage());

            if(con!=null) con.closeConnection();
            return null;
        }

        JsonObjectBuilder order = Json.createObjectBuilder();

        // get details
        String sql = "SELECT * FROM Orders O"
                + " WHERE orderid = " + orderid;

        ResultSet rs = null;
        try {
            rs = con.stmt.executeQuery(sql);
            rs.next();

            if (isAdmin == 0 && rs.getInt("cid") != authcid) {
                if(con!=null) con.closeConnection();
                return null;
            }

            order = JSONOrder(rs, order);
        } catch (Exception e) {
            System.out.println("Failed to added details");
            System.err.println(e.getMessage());

            if(con!=null) con.closeConnection();
            return null;
        }

        JsonArrayBuilder items = Json.createArrayBuilder();
        // get order-items
        try {
            JSONOrderItems(orderid, items, order);
        } catch (Exception e) {
            System.out.println("Failed to added order items");
            System.err.println(e.getMessage());

            if(con!=null) con.closeConnection();
            return null;
        }

        result.add("order", order);
        result.add("orderItems", items);
        if(con!=null) con.closeConnection();
        return result.build().toString();
    }

    public static String orders(String start, String end, String span) {
        JsonObjectBuilder result = Json.createObjectBuilder();
        JsonObjectBuilder orders = Json.createObjectBuilder();
        String st = start.split("T")[0];
        String ed = end.split("T")[0];
        Connector con = null;

        try {
            String sql = "SELECT COUNT(*) AS orders, DATE_FORMAT(O.time, '%Y-%m-%d') AS day FROM Orders O " +
                    " WHERE " + "O.time >= '" + Utility.sanitize(st) + "' AND O.time <= '" + Utility.sanitize(ed) + "'" +
                    " GROUP BY day ORDER BY day ASC";

//            System.out.println(sql);
            con = new Connector();
            ResultSet rs = con.stmt.executeQuery(sql);

            while (rs.next()) {
                orders.add(rs.getString("day"), rs.getInt("orders"));
            }

        } catch (Exception e) {
            System.out.println("Failed to get amount of orders");
            System.err.println(e.getMessage());
            if(con!=null) con.closeConnection();
            return null;
        }

        if(con!=null) con.closeConnection();
        return result.add("orders", orders).build().toString();
    }

    public static String latest(int limit, int offset) {
        JsonObjectBuilder result = Json.createObjectBuilder();
        JsonArrayBuilder orders = Json.createArrayBuilder();
        Connector con = null;
        try {
            String sql = "SELECT * FROM Orders O " +
                    " ORDER BY time DESC" +
                    " LIMIT " + limit + " OFFSET " + offset;

//            System.out.println(sql);
            con = new Connector();
            ResultSet rs = con.stmt.executeQuery(sql);

            while (rs.next()) {
                JsonObjectBuilder order = Json.createObjectBuilder();
                orders.add(JSONOrder(rs, order));
            }

            sql = "SELECT COUNT(*) AS total FROM Orders O";
            rs = con.stmt.executeQuery(sql);
            rs.next();
            JsonObjectBuilder meta = Json.createObjectBuilder();
            meta.add("total", rs.getInt("total"));
            result.add("meta", meta);

            if(con!=null) con.closeConnection();
            return result.add("orders", orders).build().toString();
        } catch (Exception e) {
            System.out.println("Failed to get amount of orders");
            System.err.println(e.getMessage());
            if(con!=null) con.closeConnection();
            return null;
        }
    }

    public static String add(final int sessionCid, JsonObject payload) {

        JsonObjectBuilder result = Json.createObjectBuilder();
        JsonObjectBuilder newOrder = Json.createObjectBuilder();
        JsonArrayBuilder newItems = Json.createArrayBuilder();
        JsonObject order = payload.getJsonObject("order");
        String addr = order.getString("address");

        final int cid = sessionCid;

        Connector con = null;
        try {
            con = new Connector();
        } catch (Exception e) {
            if(con!=null) con.closeConnection();
            return null;
        }

        try {
            // check available amount
            String sql = "SELECT B.isbn, B.title FROM Book B, Cart C WHERE C.cid = " + cid +
                    " AND B.isbn = C.isbn AND (B.copies < C.amount OR C.amount < 0)";

            ResultSet rs = con.stmt.executeQuery(sql);

            if (rs.next()) {
                System.out.print("No enough books ");
                System.out.println(rs.getString("title"));
                if(con!=null) con.closeConnection();
                return null;
            }

            // modify amount and record order
            // without batch

            sql = "UPDATE Book SET copies = copies - " +
                    "(SELECT C.amount FROM Cart C WHERE Book.isbn = C.isbn AND C.cid = " + cid + ") " +
                    "WHERE Book.isbn IN (SELECT C.isbn FROM Cart C WHERE C.cid = " + cid + ")";
            System.err.println(sql);
            con.stmt.execute(sql);

            sql = "INSERT INTO Orders (time, cid, addr) VALUES (NOW(), " + cid + ", '" + Utility.sanitize(addr) + "')";
            System.err.println(sql);
            con.stmt.execute(sql);

            sql = "SELECT LAST_INSERT_ID() AS orderid";
            rs = con.stmt.executeQuery(sql);
            rs.next();
            int orderid = rs.getInt("orderid");

            sql = "INSERT INTO ItemInOrder (orderid, isbn, price, amount) " +
                    "SELECT LAST_INSERT_ID(), C.isbn, B.price, C.amount FROM Cart C, Book B WHERE " +
                    "C.isbn = B.isbn AND C.cid = " + cid;
            System.err.println(sql);
            con.stmt.execute(sql);

            sql = "DELETE FROM Cart WHERE cid = " + cid;
            System.err.println(sql);
            con.stmt.execute(sql);

            newOrder = JSONOrder(orderid, newOrder);
            JSONOrderItems(orderid, newItems, newOrder);

            result.add("order", newOrder);
            result.add("orderItems", newItems);

            if(con!=null) con.closeConnection();
            return result.build().toString();
        } catch (Exception e) {
            System.out.println("Failed to update");
            System.err.println(e.getMessage());

            if(con!=null) con.closeConnection();
            return null;
        }

    }

    public static String add2Cart(final int sessionCid, JsonObject payload) {
        Connector con = null;
        try {
            JsonObject cart = payload.getJsonObject("cart");
            String isbn = cart.getString("book");
            String cid = cart.getString("customer");
            ////////////////// TBD
            // assert sessionCid == cid
            int amount = cart.getInt("amount");
            JsonObjectBuilder result = Json.createObjectBuilder();

            String sql = "INSERT INTO Cart (cid, isbn, amount) VALUES (" + cid + ",'" + Utility.sanitize(isbn) + "'," + amount + ") " +
                    "ON DUPLICATE KEY UPDATE amount = VALUES(amount)";
//            System.err.println(sql);

            con = new Connector();
            con.stmt.execute(sql);

            sql = "SELECT * FROM Cart WHERE isbn = '" + Utility.sanitize(isbn) + "' AND cid = " + cid;
            ResultSet rs = con.stmt.executeQuery(sql);
            rs.next();

            int newAmount = rs.getInt("amount");

            JsonObjectBuilder newCart = Json.createObjectBuilder();
            newCart.add("amount", newAmount);
            newCart.add("book", isbn);
            newCart.add("id", cid + "-" + isbn);
            newCart.add("customer", cid);
            result.add("cart", newCart);
            if(con!=null) con.closeConnection();
            return result.build().toString();
        } catch (Exception e) {
            System.out.println("Failed to add into shopping cart");
            System.err.println(e.getMessage());
            if(con!=null) con.closeConnection();
            return null;
        }
    }

    public static String cart(final int cid) {
        Connector con = null;
        try {
            String sql = "SELECT * FROM Cart C WHERE C.cid = " + cid;
            //System.err.println(sql);
            con = new Connector();

            ResultSet rs = con.stmt.executeQuery(sql);

            JsonObjectBuilder result = Json.createObjectBuilder();
            JsonArrayBuilder carts = Json.createArrayBuilder();

            while (rs.next()) {
                final int amount = rs.getInt("C.amount");
                String isbn = rs.getString("isbn");

                JsonObjectBuilder cart = Json.createObjectBuilder();
                cart.add("book", isbn);
                cart.add("amount", amount);
                cart.add("customer", cid);
                cart.add("id", cid + "-" + isbn);

                carts.add(cart);
            }

            if(con!=null) con.closeConnection();
            return result.add("carts", carts).build().toString();

        } catch (Exception e) {
            System.out.println("Failed to query");
            System.err.println(e.getMessage());
            if(con!=null) con.closeConnection();
            return null;
        }
    }

    public static String cartDetails(final int cid, String isbn) {
        Connector con = null;
        try {
            String sql = "SELECT * FROM Cart C WHERE C.cid = " + cid + " AND C.isbn = '" + Utility.sanitize(isbn) + "'";
            //System.err.println(sql);
            con = new Connector();

            ResultSet rs = con.stmt.executeQuery(sql);

            JsonObjectBuilder result = Json.createObjectBuilder();
            JsonArrayBuilder carts = Json.createArrayBuilder();

            JsonObjectBuilder cart = Json.createObjectBuilder();

            cart.add("customer", cid);
            cart.add("book", isbn);
            cart.add("id", cid + "-" + isbn);
            if (rs.next()) {
                final int amount = rs.getInt("C.amount");
                cart.add("amount", amount);
            } else {
                cart.add("amount", 0);
            }

            if(con!=null) con.closeConnection();
            return result.add("cart", cart).build().toString();
        } catch (Exception e) {
            System.out.println("Failed to query");
            System.err.println(e.getMessage());
            if(con!=null) con.closeConnection();
            return null;
        }
    }

};