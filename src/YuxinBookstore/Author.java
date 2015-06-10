package YuxinBookstore;

import java.io.*;
import java.sql.ResultSet;
import java.util.ArrayList;

/**
 * Created by Orthocenter on 5/14/15.
 */
public class Author {
    public static void showDetails(final int cid, final int authid) {
        String sql = "SELECT * FROM Author WHERE authid = " + authid;
        Connector con = Bookstore.con;
        try {
            con.newStatement();
        } catch(Exception e) {
            System.err.println(e.getMessage());
            return;
        }

        try {
            ResultSet rs = con.stmt.executeQuery(sql);
            rs.next();
            final String authname = rs.getString("authname");
            final String intro = rs.getString("intro");

            ArrayList<MenuItem> menuItems = new ArrayList<MenuItem>();
            menuItems.add(new MenuItem() {
                @Override
                public ArrayList<String> getDescs() {
                    ArrayList<String> descs = new ArrayList<String>();
                    descs.add("Author's name");
                    descs.add(authname);
                    return descs;
                }

                @Override
                public void run() {

                }
            });

            menuItems.add(new MenuItem() {
                @Override
                public ArrayList<String> getDescs() {
                    ArrayList<String> descs = new ArrayList<String>();
                    descs.add("Introduction");
                    descs.add(intro == null ? "" : intro.replace("\n", " "));
                    return descs;
                }

                @Override
                public void run() {

                }
            });

            int[] maxSizes = {30, 90};

            MenuDisplay.show(menuItems, null, maxSizes, null, true);
        } catch(Exception e) {
            System.err.println(e.getMessage());
            return;
        }

        try {
            sql = "SELECT * FROM Book B, WrittenBy W, Author A WHERE B.isbn = W.isbn AND " +
                    " W.authid = A.authid AND A.authid = " + authid;

            ArrayList<MenuItem> menuItems = new ArrayList<MenuItem>();

            ResultSet rs = con.stmt.executeQuery(sql);
            while(rs.next()) {
                final String title = rs.getString("B.title"), isbn = rs.getString("B.isbn"),
                        authname = rs.getString("A.authname");

                menuItems.add(new MenuItem() {
                    @Override
                    public ArrayList<String> getDescs() {
                        ArrayList<String> descs = new ArrayList<String>();
                        descs.add(title);
                        descs.add(isbn);
                        descs.add(authname);
                        return descs;
                    }

                    @Override
                    public void run() {
                        Book.showDetails(cid, isbn);
                    }
                });
            }

            String[] headers = {"Title", "ISBN", "Author's name"};
            int[] maxSizes = {30, 30, 30};
            MenuDisplay.chooseAndRun(menuItems, headers, maxSizes, null, false);
        } catch(Exception e) {
            System.out.println(e.getMessage());
            return;
        }
    }

    public static int choose() {
        ArrayList<MenuItem> menuItems = new ArrayList<MenuItem>();
        menuItems.add(new MenuItem() {
            @Override
            public ArrayList<String> getDescs() {
                ArrayList<String> descs = new ArrayList<String>();
                descs.add("Search an existing author");
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
                descs.add("Add a new author");
                return descs;
            }

            @Override
            public void run() {
                return;
            }
        });

        int[] maxSizes = {30};
        int c = MenuDisplay.getChoice(menuItems, null, maxSizes, null, true);

        if (c == 0)
            return search();
        else if (c == 1)
            return add();

        return -1;
    }

    public static void writtenBy(String isbn) {
        int i = 0;
        while(true) {
            int authid = choose();

            if(i > 0 && authid == -1) return;

            String sql = "INSERT INTO WrittenBy (isbn, authid) VALUES (";
            sql += "'" + isbn + "'," + authid + ")";
            //System.err.println(sql);

            try {
                Connector con = Bookstore.con;
                try {
                    con.newStatement();
                } catch(Exception e) {
                    return ;
                }
                con.stmt.executeUpdate(sql);

                i++;
            } catch(Exception e) {
                System.out.println("Cannot add to relation WrittenBy");
                System.err.println(e.getMessage());
            }
        }
    }

    public static int search() {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String name = null;

        try {
            System.out.print("Please enter author's name or author's id : ");
            name = in.readLine();
        } catch(Exception e) {
            System.out.println("Failed to read author's name or author's id");
            System.err.println(e.getMessage());
            return -1;
        }

        name = Utility.sanitize(name);
        String sql = "SELECT A.authname, A.authid, A.intro FROM Author A " +
                "WHERE A.authname LIKE ";
        sql += "'%" + name + "%'";
        sql += " OR A.authid LIKE '%" + name + "%' ";
        sql += " GROUP BY A.authid";
        //System.err.println(sql);

        final ArrayList<Integer> authids = new ArrayList<Integer>();
        ArrayList<MenuItem> menuItems = new ArrayList<MenuItem>();

        int i = 0;
        try {
            Connector con = Bookstore.con;
            try {
                con.newStatement();
            } catch(Exception e) {
                return -1;
            }
            ResultSet rs = con.stmt.executeQuery(sql);

            while(rs.next()) {
                final String authname = rs.getString("A.authname"), intro = rs.getString("A.intro");
                final String authid = rs.getString("A.authid");

                menuItems.add(new MenuItem() {
                    @Override
                    public ArrayList<String> getDescs() {
                        ArrayList<String> descs = new ArrayList<String>();
                        descs.add(authname);
                        descs.add(authid);
                        descs.add(intro == null ? "" : intro.replace("\n", " "));
                        return descs;
                    }

                    @Override
                    public void run() {

                    }
                });

//                System.out.format("%3d : ", i++);
//                System.out.format("%s %s\n", rs.getString("A.authname"), Utility.getShortString(rs.getString("A.intro"), 50));
                authids.add(rs.getInt("A.authid"));
            }

        } catch (Exception e) {
            System.out.println("Failed to search author");
            System.err.println(e.getMessage());
        }

        String[] headers = {"Author's name", "Author's id", "Introduction"};
        int[] maxSizes = {30, 15, 80};

        int choice = MenuDisplay.getChoice(menuItems, headers, maxSizes, null, true);
        if(choice == -1) return -1;

        return authids.get(choice).intValue();
    }

    public static ArrayList<String> addDescs() {
        ArrayList<String> descs = new ArrayList<String>();
        descs.add("Add a new author");
        return descs;
    }

    public static int add() {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String name = null, intro;

        try {
            do {
                System.out.print("Please enter author's name : ");
            }
            while ((name = in.readLine()) == null || name.length() == 0);

            System.out.print("Please enter introduction : ");
            intro = in.readLine();
        } catch(Exception e) {
            System.out.println("Failed to read author's name");
            System.err.println(e.getMessage());
            return -1;
        }

        String sql = "INSERT INTO Author (authname, intro) VALUES";
        sql += "('" + name + "','" + intro +  "')";
        //System.err.println(sql);

        try {
            Connector con = Bookstore.con;
            try {
                con.newStatement();
            } catch(Exception e) {
                return -1;
            }
            con.stmt.execute(sql);

            sql = "SELECT LAST_INSERT_ID()";
            ResultSet rs = con.stmt.executeQuery(sql);
            rs.next();
            return rs.getInt(1);
        } catch (Exception e) {
            System.out.println("Failed to add author");
            System.err.println(e.getMessage());
            return -1;
        }
    }

    public static ArrayList<String> showDegreesOfSeperationDescs() {
        ArrayList<String> descs = new ArrayList<String>();
        descs.add("Determine the degree of separation of two given authors");
        return descs;
    }

    public static void showDegreesOfSeperation() {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        int authid1, authid2;

        try {
            System.out.print("The first author, ");
//            authid1 = Integer.parseInt(in.readLine());
            authid1 = search();
            System.out.print("The second author, ");
//            authid2 = Integer.parseInt(in.readLine());
            authid2 = search();
        } catch(Exception e) {
            System.out.println("Failed to read");
            System.err.println(e.getMessage());
            return;
        }

        try {
            String sql = "SELECT W1.isbn FROM WrittenBy W1, WrittenBy W2 WHERE W1.isbn = W2.isbn " +
                    "AND W1.authid = " + authid1 + " AND W2.authid = " + authid2;
            Connector con = Bookstore.con;
            try {
                con.newStatement();
            } catch(Exception e) {
                return ;
            }
            ResultSet rs = con.stmt.executeQuery(sql);

            if(rs.next()) {
                System.out.format("%d and %d is 1-degree away\n", authid1, authid2);
                return;
            }
        } catch(Exception e) {
            System.out.println("Failed to query");
            System.err.println(e.getMessage());
            return;
        }

        try {
            String sql = "SELECT W1.isbn, W4.isbn FROM WrittenBy W1, WrittenBy W2, WrittenBy W3, WrittenBy W4 WHERE " +
                    "W1.isbn = W2.isbn AND W3.isbn = W4.isbn AND W1.authid = " + authid1 +
                    " AND W2.authid = W3.authid AND W4.authid = " + authid2;
            Connector con = Bookstore.con;
            try {
                con.newStatement();
            } catch(Exception e) {
                return ;
            }
            ResultSet rs = con.stmt.executeQuery(sql);

            if(rs.next()) {
                System.out.format("%d and %d is 2-degrees away\n", authid1, authid2);
                return;
            }
        } catch(Exception e) {
            System.out.println("Failed to query");
            System.err.println(e.getMessage());
            return;
        }

        System.out.format("The degree between %d and %d is too far to be determined. > <\n", authid1, authid2);
    }

    public static ArrayList<String> showPopularAuthorsDescs() {
        ArrayList<String> descs = new ArrayList<String>();
        descs.add("Show most popular authors in a certain period");
        return descs;
    }

    public static void showPopularAuthors() {
        int m;
        String st, ed;

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

            System.out.println("Please enter the amount of the most popular authors you want to see");
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
            String sql = "SELECT W.authid, SUM(I.amount) as sales FROM ItemInOrder I, WrittenBy W, Orders O " +
                    "WHERE I.isbn = W.isbn AND O.orderid = I.orderid AND O.time >= '" + st + "' AND O.time <= '" + ed +
                    "' GROUP BY W.authid ORDER BY SUM(I.amount) DESC";

            Connector con = Bookstore.con;
            try {
                con.newStatement();
            } catch(Exception e) {
                return ;
            }
            ResultSet rs = con.stmt.executeQuery(sql);

            while(rs.next() && m-- > 0) {
                final String authid = rs.getString("W.authid"), sales = rs.getString("sales");

                menuItems.add(new MenuItem() {
                    @Override
                    public ArrayList<String> getDescs() {
                        ArrayList<String> descs = new ArrayList<String>();
                        descs.add(authid);
                        descs.add(sales);
                        return descs;
                    }

                    @Override
                    public void run() {
                        Author.showDetails(-1, Integer.parseInt(authid));
                    }
                });
            }
        } catch (Exception e) {
            System.out.println("Failed to query");
            System.err.println(e.getMessage());
            return;
        }

        String[] headers = {"Author's id", "Sales"};
        int[] maxSizes = {30, 30};

        MenuDisplay.chooseAndRun(menuItems, headers, maxSizes, null, true);
    }
}
