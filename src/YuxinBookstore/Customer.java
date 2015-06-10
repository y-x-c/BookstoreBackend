/**
 * Created by Orthocenter on 5/11/15.
 */

package YuxinBookstore;

import java.awt.*;
import java.io.*;
import java.sql.*;
import java.util.ArrayList;

public class Customer {

    public static ArrayList<String> mainMenuDescs() {
        ArrayList<String> descs = new ArrayList<String>();
        descs.add("For customer");
        return descs;
    }

    public static void mainMenu() {
        ArrayList<MenuItem> menuItems = new ArrayList<MenuItem>();
        menuItems.add(new MenuItem() {
            public ArrayList<String> getDescs() { return loginMenuDescs(); }
            public void run() { loginMenu(); }
        });
        menuItems.add(new MenuItem() {
            public ArrayList<String> getDescs() { return signupMenuDescs(); }
            public void run() { signupMenu(); }
        });

        int[] maxSize = {30};
        MenuDisplay menuDisplay = new MenuDisplay();
        menuDisplay.chooseAndRun(menuItems, null, maxSize, null, true);
    }

    public static ArrayList<String> loginMenuDescs() {
        ArrayList<String> descs = new ArrayList<String>();
        descs.add("Login");
        return descs;
    }

    public static void loginMenu() {
        //System.out.println("loginMenu");
        Connector con = Bookstore.con;
        try {
            con.newStatement();
        } catch(Exception e) {
            return ;
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        try {
            String username, password;

            do { System.out.print("Please enter your username : "); }
            while ((username = in.readLine()) == null || username.length() == 0) ;
            do { System.out.print("Please enter your password : "); }
            while ((password = Utility.readPassword()) == null || password.length() == 0) ;

            String sql = "SELECT cid FROM Customer WHERE";
            sql += " username = \"" + username + "\"";
            sql += " AND password = SHA1(\"" + password + "\")";
            //System.out.println(sql);

            ResultSet rs = con.stmt.executeQuery(sql);
            ResultSetMetaData rsmd = rs.getMetaData();

            if(rs.next()) {
                int cid;
                cid = rs.getInt(1);

                System.out.println("Logged in!");
                userhomeMenu(cid);
            } else
                System.out.println("Failed");

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<String> signupMenuDescs() {
        ArrayList<String> descs = new ArrayList<String>();
        descs.add("Sign Up");
        return descs;
    }

    public static void signupMenu() {
        Connector con = Bookstore.con;
        try {
            con.newStatement();
        } catch(Exception e) {
            return ;
        }
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        try {
            String username, password, name, email, phone, confirmPassword;

            do { System.out.print("Please enter your username : "); }
            while ((username = in.readLine()) == null || username.length() == 0) ;
            do { System.out.print("Please enter your password : "); }
            while ((password = Utility.readPassword()) == null || password.length() == 0) ;
            do { System.out.print("Please confirm your password : "); }
            while ((confirmPassword = Utility.readPassword()) == null || confirmPassword.length() == 0) ;
            if(!password.equals(confirmPassword)) {
                System.out.println("Two passwords are not equal");
                return;
            }

            do { System.out.print("Please enter your name : "); }
            while ((name = in.readLine()) == null || name.length() == 0) ;
            System.out.print("Please enter your email : ");
            email = in.readLine();
            System.out.print("Please enter your phone : ");
            phone = in.readLine();

            String sql = "INSERT INTO Customer (username, password, name, email, phone) VALUES (";
            sql += "\"" + username + "\",";
            sql += "SHA1(\"" + password + "\")" + ",";
            sql += "\"" + name + "\",";
            if(email.length() == 0) sql += "NULL,"; else sql += "\"" + email + "\",";
            if(phone.length() == 0) sql += "NULL"; else sql += "\"" + phone + "\"";
            sql += ")";
            //System.out.println(sql);

            try {
                int status = con.stmt.executeUpdate(sql);
                System.out.println("Registered");

                sql = "SELECT cid FROM Customer WHERE username = \"" + username + "\"";
                //System.err.println(sql);

                ResultSet rs = con.stmt.executeQuery(sql);
                ResultSetMetaData rsmd = rs.getMetaData();

                rs.next();
                int cid = rs.getInt(1);

                userhomeMenu(cid);
            } catch (Exception e) {
                System.out.println("Failed to find the user who registered just now");
                System.err.println(e.getMessage());
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<String> declareUserDescs() {
        ArrayList<String> descs = new ArrayList<String>();
        descs.add("Declare other users as 'trusted' or 'not-trusted'");
        return descs;
    }

    public static void declareUser(int cid) {
        int dcid = -1;
        boolean trust;

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        try {
            System.out.println("Please enter cid : ");
            dcid = Integer.parseInt(in.readLine());
            System.out.println("Please enter trusted(true) or not-trusted(false) : ");
            trust = Boolean.parseBoolean(in.readLine());
        } catch (Exception e) {
            System.out.println("Failed to read");
            System.err.println(e.getMessage());
            return;
        }

        if (dcid == cid) {
            System.out.println("Youself are always trusted!");
            return;
        }

        try {
            String sql = "INSERT INTO TrustRecords (cid1, cid2, trust) VALUES (";
            sql += cid + "," + dcid + "," + trust + ")";

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

    public static ArrayList<String> userhomeMenuDescs() {
        return null;
    }

    public static void userhomeMenu(final int cid) {
        ArrayList<MenuItem> menuItems = new ArrayList<MenuItem>();

        menuItems.add(new MenuItem() {
            public ArrayList<String> getDescs() { return Book.simpleSearchMenuDescs(); }
            public void run() { Book.simpleSearchMenu(cid); }
        });
        menuItems.add(new MenuItem() {
            @Override
            public ArrayList<String> getDescs() { return Book.advancedSearchDescs();
            }

            @Override
            public void run() {
                Book.advancedSearch(cid);
            }
        });
        menuItems.add(new MenuItem() {
            @Override
            public ArrayList<String> getDescs() { return Feedback.recordMenuDescs();
            }

            @Override
            public void run() {
                Feedback.recordMenu(cid);
            }
        });
        menuItems.add(new MenuItem() {
            @Override
            public ArrayList<String> getDescs() { return Feedback.assessFeedbackDescs();
            }

            @Override
            public void run() {
                Feedback.assessFeedback(cid);
            }
        });
        menuItems.add(new MenuItem() {
            @Override
            public ArrayList<String> getDescs() { return Customer.declareUserDescs();
            }

            @Override
            public void run() {
                Customer.declareUser(cid);
            }
        });
        menuItems.add(new MenuItem() {
            @Override
            public ArrayList<String> getDescs() { return Feedback.showFeedbacksMenuDescs();
            }

            @Override
            public void run() {
                Feedback.showFeedbacksMenu(cid);
            }
        });
        menuItems.add(new MenuItem() {
            @Override
            public ArrayList<String> getDescs() { return Order.showAllOrdersDescs();
            }

            @Override
            public void run() {
                Order.showAllOrder(cid);
            }
        });
        menuItems.add(new MenuItem() {
            @Override
            public ArrayList<String> getDescs() { return Order.showCartDescs();
            }

            @Override
            public void run() {
                Order.showCart(cid);
            }
        });
        menuItems.add(new MenuItem() {
            @Override
            public ArrayList<String> getDescs() { return Order.confirmOrderDescs();
            }

            @Override
            public void run() {
                Order.confirmOrder(cid);
            }

        });

        int[] maxSizes = {50};
        MenuDisplay display = new MenuDisplay();
        display.chooseAndRun(menuItems, null, maxSizes, null, true);
    }

    public static ArrayList<String> trustedUsersDescs() {
        ArrayList<String> descs = new ArrayList<String>();
        descs.add("Print the top m most 'trusted' users");
        return descs;
    }

    public static void trustedUsers() {
        int m;

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

            System.out.println("Please enter the amount of the most popular authors you want to see");
            m = Integer.parseInt(in.readLine());
        } catch(Exception e) {
            System.out.println("Failed to read");
            System.err.println(e.getMessage());
            return;
        }

        try {
            String sql = "SELECT *, " +
                    "(SELECT COUNT(*) FROM TrustRecords T1 WHERE T1.cid2 = C.cid AND T1.trust = 1) - (SELECT COUNT(*) FROM TrustRecords T2 WHERE T2.cid2 = C.cid AND T2.trust = 0) AS score " +
                    "FROM Customer C ORDER BY " +
                    "score DESC";
            //System.err.println(sql);
            Connector con = Bookstore.con;
            try {
                con.newStatement();
            } catch(Exception e) {
                return ;
            }
            ResultSet rs = con.stmt.executeQuery(sql);

            ArrayList<MenuItem> menuItems = new ArrayList<MenuItem>();

            while(rs.next() && m-- > 0) {
                final int cid = rs.getInt("C.cid"), score = rs.getInt("score");
                menuItems.add(new MenuItem() {
                    @Override
                    public ArrayList<String> getDescs() {
                        ArrayList<String> descs = new ArrayList<String>();
                        descs.add("" + cid);
                        descs.add("" + score);
                        return descs;
                    }

                    @Override
                    public void run() {
                        System.err.println("TBD: SHOW CUSTOMER'S DETAILS");
                    }
                });
            }

            String[] headers = {"Customer id", "Trust score"};
            int[] maxSizes = {30, 30};

            MenuDisplay.chooseAndRun(menuItems, headers, maxSizes, null, true);

        } catch(Exception e) {
            System.out.println("Failed to query");
            System.out.println(e.getMessage());
            return;
        }
    }

    public static ArrayList<String> usefulUsersDescs() {
        ArrayList<String> descs = new ArrayList<String>();
        descs.add("Print the top m most 'useful' users");
        return descs;
    }

    public static void usefulUsers() {
        int m;

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

            System.out.println("Please enter the amount of the most popular authors you want to see");
            m = Integer.parseInt(in.readLine());
        } catch(Exception e) {
            System.out.println("Failed to read");
            System.err.println(e.getMessage());
            return;
        }

        try {
            String sql = "SELECT cid, AVG(rating) AS avgRating FROM Usefulness U GROUP BY U.cid ORDER BY avgRating DESC";
            //System.err.println(sql);
            Connector con = Bookstore.con;
            try {
                con.newStatement();
            } catch(Exception e) {
                return ;
            }
            ResultSet rs = con.stmt.executeQuery(sql);

            ArrayList<MenuItem> menuItems = new ArrayList<MenuItem>();

            while(rs.next() && m-- > 0) {
                final int cid = rs.getInt("cid");
                final String avg = rs.getString("avgRating");

                menuItems.add(new MenuItem() {
                    @Override
                    public ArrayList<String> getDescs() {
                        ArrayList<String> descs = new ArrayList<String>();
                        descs.add("" + cid);
                        descs.add("" + avg);
                        return descs;
                    }

                    @Override
                    public void run() {
                        System.err.println("TBD: SHOW CUSTOMER DETAILS");
                    }
                });

                System.out.format("Customer id: %d  Average usefulness: %f\n", rs.getInt("cid"), rs.getFloat("avgRating"));
            }

            String[] headers = {"Customer's id", "Average usefulnes"};
            int[] maxSizes = {30, 30};
            MenuDisplay.chooseAndRun(menuItems, headers, maxSizes, null, true);

        } catch(Exception e) {
            System.out.println("Failed to query");
            System.out.println(e.getMessage());
            return;
        }
    }
}
