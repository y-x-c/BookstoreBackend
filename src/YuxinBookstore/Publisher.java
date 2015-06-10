package YuxinBookstore;

import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.sql.ResultSet;
import java.util.ArrayList;

/**
 * Created by Orthocenter on 5/14/15.
 */
public class Publisher {
    public static int choose() {
        int i = 0;

        ArrayList<MenuItem> menuItems = new ArrayList<MenuItem>();

        menuItems.add(new MenuItem() {
            @Override
            public ArrayList<String> getDescs() {
                ArrayList<String> descs = new ArrayList<String>();
                descs.add("Select an existing publisher");
                return descs;
            }

            @Override
            public void run() {
                return;
            }
        });

        menuItems.add(new MenuItem() {
            @Override
            public ArrayList<String> getDescs() {
                ArrayList<String> descs = new ArrayList<String>();
                descs.add("Add a new publisher");
                return descs;
            }

            @Override
            public void run() {
                return;
            }
        });

        int pid = -1;

        int[] maxSizes = {30};

        while (pid == -1) {
            int c = MenuDisplay.getChoice(menuItems, null, maxSizes, null, true);

            if (c == 0)
                pid = search();
            else if (c == 1)
                pid = add();
        }

        return pid;
    }

    public static int search() {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String name = null;

        try {
            System.out.print("Please enter publisher's name or publisher's id: ");
            name = in.readLine();
        } catch(Exception e) {
            System.out.println("Failed to read publisher's name or publisher's id");
            System.err.println(e.getMessage());
            return -1;
        }

        String sql = "SELECT P.pubname, B.title, P.pid FROM Publisher P NATURAL JOIN Book B WHERE P.pubname LIKE";
        name = Utility.sanitize(name);
        sql += "'%" + name + "%'";
        sql += " OR P.pid LIKE '%" + name + "%' ";
        sql += " GROUP BY P.pid";
        System.err.println(sql);

        ArrayList<Integer> pids = new ArrayList<Integer>();
        ArrayList<MenuItem> menuItems = new ArrayList<MenuItem>();

        int i = 0;
        try {
            Connector con = Bookstore.con;
            con.newStatement();

            ResultSet rs = con.stmt.executeQuery(sql);

            while(rs.next()) {
                final String pubname = rs.getString("P.pubname");
                final String title = rs.getString("B.title");

                menuItems.add(new MenuItem() {
                    @Override
                    public ArrayList<String> getDescs() {
                        ArrayList<String> descs = new ArrayList<String>();
                        descs.add(pubname);
                        descs.add(title);
                        return descs;
                    }

                    @Override
                    public void run() {
                        return;
                    }
                });

                pids.add(new Integer(rs.getInt("P.pid")));
            }

        } catch (Exception e) {
            System.out.println("Failed to search author");
            System.err.println(e.getMessage());
        }

        String[] headers = {"publisher's name", "one published book"};
        int[] maxSizes = {30, 30};
        int choice = MenuDisplay.getChoice(menuItems, headers, maxSizes, null, true);
        if(choice == - 1) return -1;

        return pids.get(choice).intValue();
    }

    public static ArrayList<String> addDescs() {
        ArrayList<String> descs = new ArrayList<String>();
        descs.add("Add a new publisher");
        return descs;
    }

    public static int add() {
        String pubname;
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        try {
            System.out.println("Enter publisher's name : ");
            pubname = in.readLine();
        } catch(Exception e) {
            System.out.println("Failed to read publisher's information");
            System.err.println(e.getMessage());
            return -1;
        }

        try {
            String sql = "INSERT INTO Publisher (pubname) VALUES (";
            sql += Utility.genStringAttr(pubname, "");
            sql += ")";

            Connector con = Bookstore.con;
            con.newStatement();
            con.stmt.execute(sql);

            sql = "SELECT LAST_INSERT_ID()";
            ResultSet rs = con.stmt.executeQuery(sql);
            if(rs.next()) return rs.getInt(1);

        } catch(Exception e) {
            System.out.println("Failed to add publisher");
            System.err.println(e.getMessage());
            return -1;
        }

        return -1;
    }

    public static ArrayList<String> showPopularPublishersDescs() {
        ArrayList<String> descs = new ArrayList<String>();
        descs.add("Show most popular publishers in a certain period");
        return descs;
    }

    public static void showPopularPublishers() {
        int m;
        String st, ed;

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

            System.out.println("Please enter the amount of the most popular publishers you want to see");
            m = Integer.parseInt(in.readLine());

            System.out.println("Please enter the start time: ");
            st = in.readLine();
            System.out.println("Please enter the end time: ");
            ed = in.readLine();
        } catch(Exception e) {
            System.out.println("Failed to read");
            System.err.println(e.getMessage());
            return;
        }

        ArrayList<MenuItem> menuItems = new ArrayList<MenuItem>();

        try {
            String sql = "SELECT B.pid, SUM(I.amount) as sales FROM ItemInOrder I, Book B, Orders O " +
                    "WHERE I.isbn = B.isbn AND O.orderid = I.orderid AND O.time >= '" + st + "' AND O.time <= '" + ed +
                    "' GROUP BY B.pid ORDER BY SUM(I.amount) DESC";

            Connector con = Bookstore.con;
            con.newStatement();
            ResultSet rs = con.stmt.executeQuery(sql);

            while(rs.next() && m-- > 0) {
                final String pid = rs.getString("B.pid"), sales = rs.getString("sales");

                menuItems.add(new MenuItem() {
                    @Override
                    public ArrayList<String> getDescs() {
                        ArrayList<String> descs = new ArrayList<String>();
                        descs.add(pid);
                        descs.add(sales);
                        return descs;
                    }

                    @Override
                    public void run() {
                        System.err.println("TBD: SHOW PUBLISHER'S DETAILS");
                    }
                });
            }
        } catch (Exception e) {
            System.out.println("Failed to query");
            System.err.println(e.getMessage());
            return;
        }

        String[] headers = {"Publisher's id", "Sales"};
        int[] maxSizes = {30, 30};
        MenuDisplay.chooseAndRun(menuItems, headers, maxSizes, null, true);
    }
}
