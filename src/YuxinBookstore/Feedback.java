package YuxinBookstore;

/**
 * Created by Orthocenter on 5/17/15.
 */

import javax.json.*;
import javax.json.JsonObjectBuilder;
import javax.xml.transform.Result;
import java.io.*;
import java.sql.ResultSet;
import java.util.ArrayList;

public class Feedback {
    private static JsonObjectBuilder JSONFeedback(ResultSet rs, JsonObjectBuilder feedback) throws Exception {
        feedback.add("id", rs.getString("F.fid"));
        feedback.add("isbn", rs.getString("F.isbn"));
        feedback.add("customer", rs.getInt("F.cid"));
        feedback.add("score", rs.getInt("F.score"));
        String comment = rs.getString("F.comment");
        feedback.add("comment", comment == null ? "" : comment);
        feedback.add("time", rs.getString("F.time"));
        feedback.add("usefulness", rs.getDouble("usefulness"));
        String opinion = rs.getString("opinion");
        if (opinion == null) {
            feedback.add("opinion", JsonValue.NULL);
        } else {
            feedback.add("opinion", Integer.parseInt(opinion));
        }

        return feedback;
    }

    private static JsonObjectBuilder JSONFeedback(String isbn, String cid, JsonObjectBuilder feedback) throws Exception {
        Connector con = null;

        try {
            con = new Connector();

            String sql = "SELECT F.fid, F.isbn, F.cid, F.score, F.comment, F.time, " +
                    "(SELECT AVG(U.rating) FROM Usefulness U WHERE U.fid = F.fid) AS usefulness, " +
                    "(SELECT U.rating FROM Usefulness U WHERE U.fid = F.fid AND U.cid = " + cid + ") AS opinion " +
                    "FROM Feedback F WHERE isbn = '" + isbn + "' AND cid = " + cid;

            ResultSet rs = null;

            rs = con.stmt.executeQuery(sql);
            rs.next();

            if(con!=null) con.closeConnection(); return JSONFeedback(rs, feedback);
        } catch (Exception e) {
            if(con!=null) con.closeConnection();
            throw new Exception();
        }
    }

    private static JsonObjectBuilder JSONFeedback(int cid, int fid, JsonObjectBuilder feedback) throws Exception {
        Connector con = null;

        try {
            String sql = "SELECT F.fid, F.isbn, F.cid, F.score, F.comment, F.time, " +
                    "(SELECT AVG(U.rating) FROM Usefulness U WHERE U.fid = F.fid) AS usefulness, " +
                    "(SELECT U.rating FROM Usefulness U WHERE U.fid = F.fid AND U.cid = " + cid + ") AS opinion " +
                    "FROM Feedback F WHERE fid = " + fid;

            ResultSet rs = null;
            con = new Connector();
            rs = con.stmt.executeQuery(sql);
            rs.next();
            if (con != null) con.closeConnection();
            return JSONFeedback(rs, feedback);
        } catch(Exception e) {
            if(con!=null) con.closeConnection();
            throw new Exception();
        }
    }

    public static String statistic(String isbn) {
        JsonObjectBuilder result = Json.createObjectBuilder();
        JsonObjectBuilder statistic = Json.createObjectBuilder();
        Connector con = null;
        try {
            String sql = "SELECT COUNT(*) AS amount, F.score FROM Feedback F" +
                    " WHERE F.isbn = '" + isbn + "'" +
                    " GROUP BY score ORDER BY score DESC";

//            System.out.println(sql);
            con = new Connector();
            ResultSet rs = con.stmt.executeQuery(sql);

            while (rs.next()) {
                statistic.add(rs.getString("F.score"), rs.getInt("amount"));
            }

        } catch (Exception e) {
            System.out.println("Failed to get amount of orders");
            System.err.println(e.getMessage());
            if(con!=null) con.closeConnection(); return null;
        }

        if(con!=null) con.closeConnection(); return result.add("statistic", statistic).build().toString();
    }

    public static String details(final int cid, final int fid) {
        JsonObjectBuilder result = Json.createObjectBuilder();

        try {
            System.err.println("Connected to the database.");
        } catch (Exception e) {
            System.err.println("Cannot connect to the database.");
            System.err.println(e.getMessage());

            return null;
        }

        // get details
        try {
            JsonObjectBuilder feedback = Json.createObjectBuilder();
            feedback = JSONFeedback(cid, fid, feedback);
            result.add("feedback", feedback);
        } catch (Exception e) {
            System.out.println("Failed to added details into result");
            System.err.println(e.getMessage());

            return null;
        }

        return result.build().toString();
    }


    public static String feedbacks(String isbn, String _orderBy, int authcid, int limit, int offset) {
        Connector con = null;
        try {
            JsonObjectBuilder result = Json.createObjectBuilder();
            int orderBy = _orderBy == null ? 0 : Integer.parseInt(_orderBy);

            String sql = "SELECT *, (SELECT AVG(U.rating) FROM Usefulness U WHERE U.fid = F.fid) AS usefulness, " +
                    "(SELECT U.rating FROM Usefulness U WHERE U.fid = F.fid AND U.cid = " + authcid + ") AS opinion " +
                    " FROM Feedback F WHERE isbn = '" + isbn + "'";

            if (orderBy == 0) sql += " ORDER BY usefulness DESC";
            if (orderBy == 1) sql += " ORDER BY F.time DESC";
            sql += " LIMIT " + limit + " OFFSET " + offset;
            System.out.println(sql);

            con = new Connector();
            ResultSet rs = con.stmt.executeQuery(sql);

            JsonArrayBuilder feedbacks = Json.createArrayBuilder();
            while (rs.next()) {
                JsonObjectBuilder feedback = Json.createObjectBuilder();
                feedbacks.add(JSONFeedback(rs, feedback));
            }

            sql = "SELECT COUNT(*) AS total FROM Feedback F WHERE isbn = '" + isbn + "'";
            rs = con.stmt.executeQuery(sql);
            rs.next();
            JsonObjectBuilder meta = Json.createObjectBuilder();
            meta.add("total", rs.getInt("total"));
            result.add("meta", meta);

            result.add("feedbacks", feedbacks);
            if(con!=null) con.closeConnection(); return result.build().toString();
        } catch (Exception e) {
            System.out.println("Failed to get feedbacks");
            System.err.println(e.getMessage());

            if(con!=null) con.closeConnection(); return null;
        }
    }

    public static String add(JsonObject payload) {
        Connector con = null;
        try {
            JsonObjectBuilder result = Json.createObjectBuilder();
            JsonObject feedback = payload.getJsonObject("feedback");
            Integer _score = feedback.getInt("score");
            String score = _score.toString();
            String cid = feedback.getString("customer");
            String comment = feedback.getString("comment");
            String isbn = feedback.getString("book");

            String sql = "INSERT INTO Feedback (isbn, cid, score, comment, time) VALUES (";
            sql += Utility.genStringAttr(isbn, ",");
            sql += Utility.genStringAttr(cid, ",");
            sql += Utility.genStringAttr(score, ",");
            sql += Utility.genStringAttr(comment, ",");
            sql += "NOW()" + ")";

            System.out.println(sql);

            con = new Connector();
            con.stmt.execute(sql);

            JsonObjectBuilder newFeedback = Json.createObjectBuilder();
            newFeedback = JSONFeedback(isbn, cid, newFeedback);
            result.add("feedback", newFeedback);

            if(con!=null) con.closeConnection(); return result.build().toString();
        } catch (Exception e) {
            System.out.println("Failed to record the feedback");
            System.err.println(e.getMessage());
            if(con!=null) con.closeConnection(); return null;
        }
    }

    public static String assess(int cid, final int fid, JsonObject payload) {
        JsonObject feedback = payload.getJsonObject("feedback");
        JsonObjectBuilder result = Json.createObjectBuilder();
        int rating = feedback.getInt("opinion");
        Connector con = null;
        try {
            String sql = "INSERT INTO Usefulness (fid, cid, rating) VALUES (";
            sql += fid + "," + cid + "," + rating + ")";
            con = new Connector();

            con.stmt.execute(sql);

            JsonObjectBuilder newFeedback = Json.createObjectBuilder();
            newFeedback = JSONFeedback(cid, fid, newFeedback);
            result.add("feedback", newFeedback);

            if(con!=null) con.closeConnection(); return result.build().toString();
        } catch (Exception e) {
            System.out.println("Failed to insert");
            System.err.println(e.getMessage());
            if(con!=null) con.closeConnection(); return null;
        }
    }

};


