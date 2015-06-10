package YuxinBookstore;

/**
 * Created by Orthocenter on 5/17/15.
 */

import java.io.*;
import java.sql.ResultSet;
import java.util.ArrayList;

public class Feedback {
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
