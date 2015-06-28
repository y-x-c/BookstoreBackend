/**
 * Created by Orthocenter on 5/11/15.
 */

package YuxinBookstore;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.awt.*;
import java.io.*;
import java.sql.*;
import java.util.ArrayList;

public class Customer {
    private static JsonObjectBuilder JSONCustomer(int authcid, int isAdmin, ResultSet rs, JsonObjectBuilder customer) throws Exception {
        Connector con = null;

        try {
            con = new Connector();
            String sql;
            ResultSet rs2;
            int cid = rs.getInt("cid");
            Boolean trusted = false, beTrusted = false;

            sql = "SELECT trust FROM TrustRecords WHERE cid1 = " + cid + " AND cid2 = " + authcid;
            rs2 = con.stmt.executeQuery(sql);
            if (rs2.next()) {
                beTrusted = rs2.getBoolean("trust");
            }

            customer.add("id", cid);
            customer.add("username", rs.getString("username"));

            if (authcid == cid || isAdmin == 1 || beTrusted) {
                customer.add("name", rs.getString("name"));
                String email = rs.getString("email");
                customer.add("email", email == null ? "" : email);
                String phone = rs.getString("phone");
                customer.add("phone", phone == null ? "" : phone);

                sql = "SELECT orderid FROM Orders WHERE cid = " + cid + " ORDER BY time DESC";
                rs2 = con.stmt.executeQuery(sql);

                JsonArrayBuilder orders = Json.createArrayBuilder();
                while (rs2.next()) {
                    orders.add(rs2.getInt("orderid"));
                }

                customer.add("orders", orders);
            }

            sql = "SELECT trust FROM TrustRecords WHERE cid1 = " + authcid + " AND cid2 = " + cid;
            rs2 = con.stmt.executeQuery(sql);
            if (rs2.next()) {
                trusted = rs2.getBoolean("trust");
                customer.add("trusted", trusted);
            }

            customer.add("admin", rs.getBoolean("admin"));

            if (con != null) con.closeConnection();
            return customer;
        } catch (Exception e) {
            if(con!=null) con.closeConnection();
            throw new Exception();
        }
    }

    private static JsonObjectBuilder JSONCustomer(int authcid, int isAdmin, int cid, JsonObjectBuilder customer) throws Exception {
        Connector con = null;
        try {
            con = new Connector();

            String sql = "SELECT * from Customer where cid = '" + cid + "'";
            ResultSet rs = con.stmt.executeQuery(sql);
            rs.next();
            if (con != null) con.closeConnection();
            return JSONCustomer(authcid, isAdmin, rs, customer);
        } catch(Exception e) {
            if(con!=null) con.closeConnection();
            throw new Exception();
        }
    }


    public static String details(int authcid, int isAdmin, final int cid) {
        JsonObjectBuilder result = Json.createObjectBuilder();
        JsonObjectBuilder customer = Json.createObjectBuilder();

        try {
            customer = JSONCustomer(authcid, isAdmin, cid, customer);
        } catch (Exception e) {
            System.out.println("Failed to query customers details");
            System.err.println(e.getMessage());
            return null;
        }

        result.add("customer", customer);
        return result.build().toString();
    }


    public static String trusted(int authcid, int isAdmin, int limit, int offset) {
        JsonObjectBuilder result = Json.createObjectBuilder();
        JsonArrayBuilder customers = Json.createArrayBuilder();
        JsonObjectBuilder scores = Json.createObjectBuilder();
        Connector con = null;
        try {
            String sql = "SELECT *, " +
                    "(SELECT COUNT(*) FROM TrustRecords T1 WHERE T1.cid2 = C.cid AND T1.trust = 1) - (SELECT COUNT(*) FROM TrustRecords T2 WHERE T2.cid2 = C.cid AND T2.trust = 0) AS score " +
                    "FROM Customer C ORDER BY " + "score DESC" +
                    " LIMIT " + limit + " OFFSET " + offset;
            //System.err.println(sql);
            con = new Connector();
            ResultSet rs = con.stmt.executeQuery(sql);

            while (rs.next()) {
                final int cid = rs.getInt("C.cid"), score = rs.getInt("score");

                JsonObjectBuilder customer = Json.createObjectBuilder();
                customer = JSONCustomer(authcid, isAdmin, cid, customer);
                customers.add(customer);

                String _cid = Integer.toString(cid);
                scores.add(_cid, score);
            }

            sql = "SELECT COUNT(C.cid) AS total FROM Customer C";
            rs = con.stmt.executeQuery(sql);
            rs.next();

            JsonObjectBuilder meta = Json.createObjectBuilder();
            meta.add("total", rs.getInt("total"));

            result.add("meta", meta);
            result.add("customers", customers).add("scores", scores);
            if(con!=null) con.closeConnection(); return result.build().toString();
        } catch (Exception e) {
            System.out.println("Failed to query most trusted customers");
            System.out.println(e.getMessage());
            if(con!=null) con.closeConnection(); return null;
        }
    }

    public static String useful(int authcid, int isAdmin, int limit, int offset) {
        JsonObjectBuilder result = Json.createObjectBuilder();
        JsonArrayBuilder customers = Json.createArrayBuilder();
        JsonObjectBuilder ratings = Json.createObjectBuilder();
        Connector con = null;
        try {
            String sql = "SELECT *, (SELECT AVG(rating) FROM Usefulness U, Feedback F WHERE U.fid = F.fid AND F.cid = C.cid) AS avgRating" +
                    " FROM Customer C ORDER BY avgRating DESC " +
                    " LIMIT " + limit + " OFFSET " + offset;

            //System.err.println(sql);
            con = new Connector();
            ResultSet rs = con.stmt.executeQuery(sql);

            while (rs.next()) {
                final int cid = rs.getInt("cid");
                final double rating = rs.getDouble("avgRating");

                JsonObjectBuilder customer = Json.createObjectBuilder();
                customer = JSONCustomer(authcid, isAdmin, cid, customer);
                customers.add(customer);

                String _cid = Integer.toString(cid);
                ratings.add(_cid, rating);
            }

            sql = "SELECT COUNT(C.cid) AS total FROM Customer C";
            rs = con.stmt.executeQuery(sql);
            rs.next();

            JsonObjectBuilder meta = Json.createObjectBuilder();
            meta.add("total", rs.getInt("total"));

            result.add("meta", meta);
            result.add("customers", customers).add("ratings", ratings);
            if(con!=null) con.closeConnection(); return result.build().toString();
        } catch (Exception e) {
            System.out.println("Failed to query most useful customers");
            System.out.println(e.getMessage());
            if(con!=null) con.closeConnection(); return null;
        }
    }

    public static String trust(int authcid, int isAdmin, int cid, JsonObject payload) {
        JsonObject customer = payload.getJsonObject("customer");
        Boolean trusted = customer.getBoolean("trusted");
        Connector con = null;
        try {
            String sql = "INSERT INTO TrustRecords (cid1, cid2, trust) VALUES (";
            sql += authcid + "," + cid + "," + trusted + ")";
            sql += " ON DUPLICATE KEY UPDATE trust=VALUES(trust)";

            con = new Connector();

            con.stmt.execute(sql);

            JsonObjectBuilder newCustomer = Json.createObjectBuilder();
            newCustomer = JSONCustomer(authcid, isAdmin, cid, newCustomer);
            JsonObjectBuilder result = Json.createObjectBuilder();
            result.add("customer", newCustomer);
            if(con!=null) con.closeConnection(); return result.build().toString();

        } catch (Exception e) {
            System.out.println("Failed to insert");
            System.err.println(e.getMessage());
            if(con!=null) con.closeConnection(); return null;
        }
    }

    public static String whoAmI(int sessionCid, int isAdmin, String ip) {
        Connector con = null;
        try {
            String sql = "INSERT INTO History(time, ip, cid) VALUES( NOW(), '" + ip + "', " + sessionCid + ")";
            con = new Connector();
            con.stmt.execute(sql);
        } catch (Exception e) {
            if(con!=null) con.closeConnection(); return null;
        }

        if (sessionCid < 0) {
            if(con!=null) con.closeConnection(); return null;
        } else {
            try {
                JsonObjectBuilder customer = Json.createObjectBuilder();
                customer = JSONCustomer(sessionCid, isAdmin, sessionCid, customer);
                JsonObjectBuilder result = Json.createObjectBuilder();
                result.add("customer", customer);
                if(con!=null) con.closeConnection(); return result.build().toString();
            } catch (Exception e) {
                if(con!=null) con.closeConnection(); return null;
            }
        }
    }

    public static String login(JsonObject payload, JsonObjectBuilder result) {
        JsonObject info = payload.getJsonObject("customer");
        String username = info.getString("username");
        String password = info.getString("password");
        JsonObjectBuilder customer = Json.createObjectBuilder();
//        JsonObjectBuilder result = Json.createObjectBuilder();
        Connector con = null;
        try {
            con = new Connector();

            String sql = "SELECT * FROM Customer WHERE";
            sql += " username = \"" + username + "\"";
            sql += " AND password = SHA1(\"" + password + "\")";

            ResultSet rs = con.stmt.executeQuery(sql);

            if (rs.next()) {
                int cid = rs.getInt("cid");
                int isAdmin = rs.getInt("admin");
                JSONCustomer(cid, isAdmin, cid, customer);
                result.add("customer", customer);
                System.out.println("" + rs.getInt("admin") + "/" + cid);
                if(con!=null) con.closeConnection(); return "" + rs.getInt("admin") + "/" + cid;
            } else {
                if(con!=null) con.closeConnection(); return "0/-1";
            }

        } catch (Exception e) {
            System.out.println("Failed to validate");
            System.err.println(e.getMessage());

            if(con!=null) con.closeConnection(); return "0/-1";
        }
    }

    public static int signup(JsonObject payload, JsonObjectBuilder result) {
        JsonObject info = payload.getJsonObject("customer");
        String username = info.getString("username");
        String password = info.getString("password");
        String name = info.getString("name");
        String email = info.getString("email");
        String phone = info.getString("phone");
        JsonObjectBuilder customer = Json.createObjectBuilder();
        Connector con = null;
        try {
            con = new Connector();

            String sql = "INSERT INTO Customer (username, password, name, email, phone) VALUES (";
            sql += "\"" + username + "\",";
            sql += "SHA1(\"" + password + "\")" + ",";
            sql += "\"" + name + "\",";
            if (email.length() == 0) sql += "NULL,";
            else sql += "\"" + email + "\",";
            if (phone.length() == 0) sql += "NULL";
            else sql += "\"" + phone + "\"";
            sql += ")";

            con.stmt.executeUpdate(sql);

            sql = "SELECT cid FROM Customer WHERE username = \"" + username + "\"";

            ResultSet rs = con.stmt.executeQuery(sql);

            rs.next();
            int cid = rs.getInt(1);

            customer = JSONCustomer(cid, 0, cid, customer);
            result.add("customer", customer);

            if(con!=null) con.closeConnection(); return cid;
        } catch (Exception e) {
            System.out.println("Failed to signup");
            System.err.println(e.getMessage());
            if(con!=null) con.closeConnection(); return -1;
        }
    }


    public static String visits(String start, String end, String span) {
        JsonObjectBuilder result = Json.createObjectBuilder();
        JsonObjectBuilder visits = Json.createObjectBuilder();
        String st = start.split("T")[0];
        String ed = end.split("T")[0];
        Connector con = null;
        try {
            String sql = "SELECT COUNT(*) AS visits, DATE_FORMAT(H.time, '%Y-%m-%d') AS day FROM History H " +
                    " WHERE " + "H.time >= '" + st + "' AND H.time <= '" + ed + "'" +
                    " GROUP BY day ORDER BY day ASC";

//            System.out.println(sql);
            con = new Connector();
            ResultSet rs = con.stmt.executeQuery(sql);

            while (rs.next()) {
                visits.add(rs.getString("day"), rs.getInt("visits"));
            }

        } catch (Exception e) {
            System.out.println("Failed to get amount of visits");
            System.err.println(e.getMessage());
            if(con!=null) con.closeConnection(); return null;
        }

        if(con!=null) con.closeConnection(); return result.add("visits", visits).build().toString();
    }
};