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
    private static JsonObjectBuilder JSONFeedback(ResultSet rs, JsonObjectBuilder feedback) throws Exception{
        feedback.add("id", rs.getString("F.fid"));
        feedback.add("isbn", rs.getString("F.isbn"));
        feedback.add("customer", rs.getInt("F.cid"));
        feedback.add("score", rs.getInt("F.score"));
        String comment = rs.getString("F.comment");
        feedback.add("comment", comment == null ? "" : comment);
        feedback.add("time", rs.getString("F.time"));
        feedback.add("usefulness", rs.getDouble("usefulness"));
        String opinion = rs.getString("opinion");
        if(opinion == null) {
            feedback.add("opinion", JsonValue.NULL);
        } else {
            feedback.add("opinion", Integer.parseInt(opinion));
        }

        return feedback;
    }

    private static JsonObjectBuilder JSONFeedback(String isbn, String cid, JsonObjectBuilder feedback) throws Exception {
        String sql = "SELECT F.fid, F.isbn, F.cid, F.score, F.comment, F.time, " +
                "(SELECT AVG(U.rating) FROM Usefulness U WHERE U.fid = F.fid) AS usefulness, " +
                "(SELECT U.rating FROM Usefulness U WHERE U.fid = F.fid AND U.cid = " + cid + ") AS opinion " +
                "FROM Feedback F WHERE isbn = '" + isbn + "' AND cid = " + cid;

        ResultSet rs = null;
        Connector con = new Connector();
        rs = con.stmt.executeQuery(sql);
        rs.next();
        return JSONFeedback(rs, feedback);
    }

    private static JsonObjectBuilder JSONFeedback(int cid, int fid, JsonObjectBuilder feedback) throws Exception {
        String sql = "SELECT F.fid, F.isbn, F.cid, F.score, F.comment, F.time, " +
                "(SELECT AVG(U.rating) FROM Usefulness U WHERE U.fid = F.fid) AS usefulness, " +
                "(SELECT U.rating FROM Usefulness U WHERE U.fid = F.fid AND U.cid = " + cid + ") AS opinion " +
                "FROM Feedback F WHERE fid = " + fid;

        ResultSet rs = null;
        Connector con = new Connector();
        rs = con.stmt.executeQuery(sql);
        rs.next();
        return JSONFeedback(rs, feedback);
    }

    public static String statistic(String isbn) {
        JsonObjectBuilder result = Json.createObjectBuilder();
        JsonObjectBuilder statistic = Json.createObjectBuilder();

        try {
            String sql = "SELECT COUNT(*) AS amount, F.score FROM Feedback F" +
                    " WHERE F.isbn = '" + isbn + "'" +
                    " GROUP BY score ORDER BY score DESC";

//            System.out.println(sql);
            Connector con = new Connector();
            ResultSet rs = con.stmt.executeQuery(sql);

            while(rs.next()) {
                statistic.add(rs.getString("F.score"), rs.getInt("amount"));
            }

        } catch (Exception e) {
            System.out.println("Failed to get amount of orders");
            System.err.println(e.getMessage());
            return null;
        }

        return result.add("statistic", statistic).build().toString();
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
        } catch(Exception e) {
            System.out.println("Failed to added details into result");
            System.err.println(e.getMessage());

            return null;
        }

        return result.build().toString();
    }


    public static String feedbacks(String isbn, String _orderBy, int authcid, int limit, int offset) {
        try {
            JsonObjectBuilder result = Json.createObjectBuilder();
            int orderBy = _orderBy == null ? 0 : Integer.parseInt(_orderBy);

            String sql = "SELECT *, (SELECT AVG(U.rating) FROM Usefulness U WHERE U.fid = F.fid) AS usefulness, " +
                    "(SELECT U.rating FROM Usefulness U WHERE U.fid = F.fid AND U.cid = " + authcid + ") AS opinion " +
                    " FROM Feedback F WHERE isbn = '" + isbn + "'";

            if(orderBy == 0) sql += " ORDER BY usefulness DESC";
            if(orderBy == 1) sql += " ORDER BY F.time DESC";
            sql += " LIMIT " + limit + " OFFSET " + offset;
            System.out.println(sql);

            Connector con = new Connector();
            ResultSet rs = con.stmt.executeQuery(sql);

            JsonArrayBuilder feedbacks = Json.createArrayBuilder();
            while(rs.next()) {
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
            return result.build().toString();
        } catch(Exception e) {
            System.out.println("Failed to get feedbacks");
            System.err.println(e.getMessage());

            return null;
        }
    }

    public static String add(JsonObject payload) {
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

            Connector con = new Connector();
            con.stmt.execute(sql);

            JsonObjectBuilder newFeedback = Json.createObjectBuilder();
            newFeedback = JSONFeedback(isbn, cid, newFeedback);
            result.add("feedback", newFeedback);

            return result.build().toString();
        } catch(Exception e) {
            System.out.println("Failed to record the feedback");
            System.err.println(e.getMessage());
            return null;
        }
    }

    public static String assess(int cid, final int fid, JsonObject payload) {
        JsonObject feedback = payload.getJsonObject("feedback");
        JsonObjectBuilder result = Json.createObjectBuilder();
        int rating = feedback.getInt("opinion");

        try {
            String sql = "INSERT INTO Usefulness (fid, cid, rating) VALUES (";
            sql += fid + "," + cid + "," + rating + ")";
            Connector con = new Connector();

            con.stmt.execute(sql);

            JsonObjectBuilder newFeedback = Json.createObjectBuilder();
            newFeedback = JSONFeedback(cid, fid, newFeedback);
            result.add("feedback", newFeedback);

            return result.build().toString();
        } catch (Exception e) {
            System.out.println("Failed to insert");
            System.err.println(e.getMessage());
            return null;
        }
    }


    ////////////////////////////////////////////
    public static ArrayList<String> recordDescs() {
        ArrayList<String> descs = new ArrayList<String>();
        descs.add("Record your feedback for a book");
        return descs;
    }

    public static void record(final int cid, final String isbn) {
        int score = 0;
        String comment;
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        try {
            System.out.println("Please enter score(1 - 10) : ");
            score = Integer.parseInt(in.readLine());
            System.out.println("Please enter your comment(optional) : ");
            comment = in.readLine();
        } catch(Exception e) {
            System.out.println("Failed to read feedback information");
            System.err.println(e.getMessage());
            return;
        }

        try {
            String sql = "INSERT INTO Feedback (isbn, cid, score, comment, time) VALUES (";
            sql += "'" + isbn + "'," + cid + "," + score + ",";
            if(comment.length() > 0) sql += "'" + comment + "',"; else sql += null + ",";
            sql += "NOW()" + ")";
            //System.err.println(sql);

            Connector con = Bookstore.con;
            try {
                con.newStatement();
            } catch(Exception e) {
                return ;
            }
            con.stmt.execute(sql);
            System.out.println("Successfully");
        } catch(Exception e) {
            System.out.println("Failed to record the feedback");
            System.err.println(e.getMessage());
            return;
        }
    }

    public static ArrayList<String> recordMenuDescs() {
        ArrayList<String> descs = new ArrayList<String>();
        descs.add("Record your feedback for a given book");
        return descs;
    }

    public static void recordMenu(final int cid) {
        String isbn;

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        try {
            System.out.println("Please enter isbn : ");
            isbn = in.readLine();
        } catch(Exception e) {
            System.out.println("Failed to read feedback information");
            System.err.println(e.getMessage());
            return;
        }

        record(cid, isbn);
    }

    public static ArrayList<String> assessFeedbackDescs() {
        ArrayList<String> descs = new ArrayList<String>();
        descs.add("Assess a feed back record");
        return descs;
    }

    public static void assessFeedback(int cid, final int fid) {
        int rating = 0;
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        try {
            System.out.println("Please enter rating(0, 1 or 2)");
            rating = Integer.parseInt(in.readLine());
        } catch(Exception e) {
            System.out.println("Failed to read");
            System.err.println(e.getMessage());
            return;
        }

        try {
            String sql = "SELECT cid FROM Feedback WHERE fid = " + fid;
            Connector con = Bookstore.con;
            try {
                con.newStatement();
            } catch(Exception e) {
                return ;
            }
            ResultSet rs = con.stmt.executeQuery(sql);
            rs.next();

            if(rs.getInt("cid") == cid) {
                System.out.println("Sorry, you are not allow to assess your own feedback");
                return;
            }
        } catch(Exception e) {
            System.out.println("Failed to check");
            System.err.println(e.getMessage());
            return;
        }

        try {
            String sql = "INSERT INTO Usefulness (fid, cid, rating) VALUES (";
            sql += fid + "," + cid + "," + rating + ")";
            Connector con = Bookstore.con;
            try {
                con.newStatement();
            } catch(Exception e) {
                return ;
            }
            con.stmt.execute(sql);

            System.out.println("Successfully");
        } catch (Exception e) {
            System.out.println("Failed to insert");
            System.err.println(e.getMessage());
            return;
        }
    }

    public static void assessFeedback(int cid) {
        int fid = -1, rating = 0;
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        try {
            System.out.println("Please enter fid of the feedback which you want to assess");
            fid = Integer.parseInt(in.readLine());
        } catch(Exception e) {
            System.out.println("Failed to read");
            System.err.println(e.getMessage());
            return;
        }

        assessFeedback(cid, fid);
    }

    public static ArrayList<String> showFeedbacksDescs() {
        ArrayList<String> descs = new ArrayList<String>();
        descs.add("Show feedbacks related to this book");
        return descs;
    }

    public static void showFeedbacks(final String isbn, final int cid, int m) {
        try {
            String sql = "SELECT F.fid, C.username, F.score, F.comment, (SELECT AVG(U.rating) FROM Usefulness U WHERE U.fid = F.fid) AS usefulness FROM Feedback F NATURAL JOIN Customer C WHERE isbn = '" + isbn + "'";
            sql += " ORDER BY (SELECT SUM(U.rating) FROM Usefulness U WHERE U.fid = F.fid) DESC";

            Connector con = Bookstore.con;
            try {
                con.newStatement();
            } catch(Exception e) {
                return ;
            }
            ResultSet rs = con.stmt.executeQuery(sql);

            ArrayList<MenuItem> menuItems = new ArrayList<MenuItem>();

            while(rs.next() && m-- > 0) {
                final String username = rs.getString("C.username"), score = rs.getString("F.score"),
                        comment = rs.getString("F.comment"), usefulness = rs.getString("usefulness");
                final int fid = rs.getInt("F.fid");
                menuItems.add(new MenuItem() {
                    @Override
                    public ArrayList<String> getDescs() {
                        ArrayList<String> descs = new ArrayList<String>();
                        descs.add(username);
                        descs.add(score);
                        descs.add(comment);
                        descs.add(usefulness);
                        return descs;
                    }

                    @Override
                    public void run() {
                        assessFeedback(cid, fid);
                    }
                });
            }

            String[] headers = {"Username", "Score", "Comment", "Usefulness"};
            int[] maxSizes = {30, 10, 70, 10};
            MenuDisplay.chooseAndRun(menuItems, headers, maxSizes, null, true);

        } catch(Exception e) {
            System.out.println("Failed to query");
            System.err.println(e.getMessage());
            return;
        }
    }

    public static ArrayList<String> showFeedbacksMenuDescs() {
        ArrayList<String> descs = new ArrayList<String>();
        descs.add("Show feedbacks for a given book");
        return descs;
    }

    public static void showFeedbacksMenu(final int cid) {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        int m;
        String isbn;

        try {
            System.out.println("Please enter the isbn : ");
            isbn = in.readLine();
            System.out.println("The amount of the most useful feedbacks you want to see : ");
            m = Integer.parseInt(in.readLine());
        } catch (Exception e) {
            System.out.println("Failed to read");
            System.err.println(e.getMessage());
            return;
        }

        showFeedbacks(isbn, cid, m);
    }
}
