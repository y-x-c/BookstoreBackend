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
    private static JsonObjectBuilder JSONCustomer(int authcid, ResultSet rs, JsonObjectBuilder customer) throws Exception{
        Connector con = new Connector();
        String sql;
        ResultSet rs2;
        int cid = rs.getInt("cid");
        Boolean trusted = false, beTrusted = false;

        sql = "SELECT trust FROM TrustRecords WHERE cid1 = " + cid + " AND cid2 = " + authcid;
        rs2 = con.stmt.executeQuery(sql);
        if(rs2.next()) {
            beTrusted = rs2.getBoolean("trust");
        }

        customer.add("id", cid);
        customer.add("username", rs.getString("username"));

        if(authcid < 0 || authcid == cid || beTrusted) {
            customer.add("name", rs.getString("name"));
            String email = rs.getString("email");
            customer.add("email", email == null ? "" : email);
            String phone = rs.getString("phone");
            customer.add("phone", phone == null ? "" : phone);

            sql = "SELECT orderid FROM Orders WHERE cid = " + cid;
            rs2 = con.stmt.executeQuery(sql);

            JsonArrayBuilder orders = Json.createArrayBuilder();
            while (rs2.next()) {
                orders.add(rs2.getInt("orderid"));
            }

            customer.add("orders", orders);
        }

        sql = "SELECT trust FROM TrustRecords WHERE cid1 = " + authcid + " AND cid2 = " + cid;
        rs2 = con.stmt.executeQuery(sql);
        if(rs2.next()){
            trusted = rs2.getBoolean("trust");
            customer.add("trusted", trusted);
        }

        return customer;
    }

    private static JsonObjectBuilder JSONCustomer(int authcid, int cid, JsonObjectBuilder customer) throws Exception{
        Connector con = new Connector();
        String sql = "SELECT * from Customer where cid = '" + cid + "'";
        ResultSet rs = con.stmt.executeQuery(sql);
        rs.next();
        return JSONCustomer(authcid, rs, customer);
    }


    public static String details(int authcid, final int cid) {
        JsonObjectBuilder result = Json.createObjectBuilder();
        JsonObjectBuilder customer = Json.createObjectBuilder();


        try {
            customer = JSONCustomer(authcid, cid, customer);
        } catch (Exception e) {
            System.out.println("Failed to query customers details");
            System.err.println(e.getMessage());
            return null;
        }

        result.add("customer", customer);
        return result.build().toString();
    }


    public static String trusted(int authcid, int limit, int offset) {
        JsonObjectBuilder result = Json.createObjectBuilder();
        JsonArrayBuilder customers = Json.createArrayBuilder();
        JsonObjectBuilder scores = Json.createObjectBuilder();

        try {
            String sql = "SELECT *, " +
                    "(SELECT COUNT(*) FROM TrustRecords T1 WHERE T1.cid2 = C.cid AND T1.trust = 1) - (SELECT COUNT(*) FROM TrustRecords T2 WHERE T2.cid2 = C.cid AND T2.trust = 0) AS score " +
                    "FROM Customer C ORDER BY " + "score DESC" +
                    " LIMIT " + limit + " OFFSET " + offset;
            //System.err.println(sql);
            Connector con = new Connector();
            ResultSet rs = con.stmt.executeQuery(sql);

            while(rs.next()) {
                final int cid = rs.getInt("C.cid"), score = rs.getInt("score");

                JsonObjectBuilder customer = Json.createObjectBuilder();
                customer = JSONCustomer(authcid, cid, customer);
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
            return result.build().toString();
        } catch(Exception e) {
            System.out.println("Failed to query most trusted customers");
            System.out.println(e.getMessage());
            return null;
        }
    }

    public static String useful(int authcid, int limit, int offset) {
        JsonObjectBuilder result = Json.createObjectBuilder();
        JsonArrayBuilder customers = Json.createArrayBuilder();
        JsonObjectBuilder ratings = Json.createObjectBuilder();

        try {
            String sql = "SELECT *, (SELECT AVG(rating) FROM Usefulness U, Feedback F WHERE U.fid = F.fid AND F.cid = C.cid) AS avgRating" +
                    " FROM Customer C ORDER BY avgRating DESC " +
                    " LIMIT " + limit + " OFFSET " + offset;

            //System.err.println(sql);
            Connector con = new Connector();
            ResultSet rs = con.stmt.executeQuery(sql);

            while(rs.next()) {
                final int cid = rs.getInt("cid");
                final double rating = rs.getDouble("avgRating");

                JsonObjectBuilder customer = Json.createObjectBuilder();
                customer = JSONCustomer(authcid, cid, customer);
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
            return result.build().toString();
        } catch(Exception e) {
            System.out.println("Failed to query most useful customers");
            System.out.println(e.getMessage());
            return null;
        }
    }

    public static String trust(int authcid, int cid, JsonObject payload) {
        JsonObject customer = payload.getJsonObject("customer");
        Boolean trusted = customer.getBoolean("trusted");

        try {
            String sql = "INSERT INTO TrustRecords (cid1, cid2, trust) VALUES (";
            sql += authcid + "," + cid + "," + trusted + ")";
            sql += " ON DUPLICATE KEY UPDATE trust=VALUES(trust)";

            Connector con = new Connector();

            con.stmt.execute(sql);

            JsonObjectBuilder newCustomer = Json.createObjectBuilder();
            newCustomer = JSONCustomer(authcid, cid, newCustomer);
            JsonObjectBuilder result = Json.createObjectBuilder();
            result.add("customer", newCustomer);
            return result.build().toString();

        } catch (Exception e) {
            System.out.println("Failed to insert");
            System.err.println(e.getMessage());
            return null;
        }
    }

    public static int login(JsonObject payload, JsonObjectBuilder result) {
        JsonObject info = payload.getJsonObject("customer");
        String username = info.getString("username");
        String password = info.getString("password");
        JsonObjectBuilder customer = Json.createObjectBuilder();
//        JsonObjectBuilder result = Json.createObjectBuilder();

        try {
            Connector con = new Connector();

            String sql = "SELECT cid FROM Customer WHERE";
            sql += " username = \"" + username + "\"";
            sql += " AND password = SHA1(\"" + password + "\")";

            ResultSet rs = con.stmt.executeQuery(sql);

            if(rs.next()) {
                int cid = rs.getInt(1);
                JSONCustomer(cid, cid, customer);
                result.add("customer", customer);
                return cid;
            } else {
                return -1;
            }

        } catch(Exception e) {
            System.out.println("Failed to validate");
            System.err.println(e.getMessage());

            return -1;
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

        try {
            Connector con = new Connector();

            String sql = "INSERT INTO Customer (username, password, name, email, phone) VALUES (";
            sql += "\"" + username + "\",";
            sql += "SHA1(\"" + password + "\")" + ",";
            sql += "\"" + name + "\",";
            if(email.length() == 0) sql += "NULL,"; else sql += "\"" + email + "\",";
            if(phone.length() == 0) sql += "NULL"; else sql += "\"" + phone + "\"";
            sql += ")";

            con.stmt.executeUpdate(sql);

            sql = "SELECT cid FROM Customer WHERE username = \"" + username + "\"";

            ResultSet rs = con.stmt.executeQuery(sql);

            rs.next();
            int cid = rs.getInt(1);

            customer = JSONCustomer(cid, cid, customer);
            result.add("customer", customer);

            return cid;
        } catch(Exception e) {
            System.out.println("Failed to signup");
            System.err.println(e.getMessage());
            return -1;
        }
    }

    ////////////////////////////////////////////
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
