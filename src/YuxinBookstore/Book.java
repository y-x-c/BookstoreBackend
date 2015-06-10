package YuxinBookstore;

/**
 * Created by Orthocenter on 5/12/15.
 */

import java.awt.*;
import java.io.*;
import java.lang.reflect.Array;
import java.sql.ResultSet;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;


public class Book {

    public static ResultSet search(int cid, String conditions) {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        Connector con = Bookstore.con;
        try {
            con.newStatement();
        } catch(Exception e) {
            return null;
        }

        int orderBy = 0;
        try {
            System.out.println("Sort by year(ASC 0, DESC 1), \n" +
                    "   or by the average numerical score of the feedbacks(ASC 2, DESC 3), \n" +
                    "   or by the average numerical score of the trusted user feedbacks(ASC 4, DESC 5) :");
            orderBy = Integer.parseInt(in.readLine());
        } catch(Exception e) {
            System.out.println("Failed to get ordering manner");
            System.err.println(e.getMessage());
            return null;
        }

        String sql = "SELECT * FROM Book B NATURAL JOIN Publisher P NATURAL JOIN WrittenBy W NATURAL JOIN Author A WHERE ";
        sql += conditions;
        sql += " GROUP BY B.isbn ";
        if(orderBy == 0) {
            sql += " ORDER BY pubdate ASC";
        } else if(orderBy == 1) {
            sql += " ORDER BY pubdate DESC";
        } else if(orderBy == 2) {
            sql += " ORDER BY (SELECT AVG(score) FROM Feedback F WHERE F.isbn = B.isbn)ASC";
        } else if(orderBy == 3) {
            sql += " ORDER BY (SELECT AVG(score) FROM Feedback F WHERE F.isbn = B.isbn)DESC";
        } else if(orderBy == 4) {
            sql += " ORDER BY (SELECT AVG(score) FROM Feedback F WHERE F.isbn = B.isbn AND " +
                    "(F.cid = " + cid + " OR F.cid IN ( " +
                    "SELECT T.cid2 FROM TrustRecords T WHERE T.trust = TRUE AND T.cid1 = " + cid + ")))ASC";
        } else if(orderBy == 5) {
            sql += " ORDER BY (SELECT AVG(score) FROM Feedback F WHERE F.isbn = B.isbn AND " +
                    "(F.cid = " + cid + " OR F.cid IN ( " +
                    "SELECT T.cid2 FROM TrustRecords T WHERE T.trust = TRUE AND T.cid1 = " + cid + ")))DESC";
        }
        //System.out.println(sql);

        try {
            ResultSet rs = con.stmt.executeQuery(sql);
            return rs;
        } catch(Exception e) {
            System.out.println("Query failed");
            System.err.println(e.getMessage());
            return null;
        }
    }

    public static void showSearchResults(final int cid, ResultSet rs) {
        try {
            ArrayList<MenuItem> menuItems = new ArrayList<MenuItem>();

            while (rs.next()) {
                final String row = rs.getString("title") + " " + rs.getString("price") + " "
                        + rs.getString("A.authname") + " " + rs.getString("isbn");
                final String isbn = rs.getString("isbn"), title = rs.getString("title"),
                        price = rs.getString("price"), authname = rs.getString("authname");

                menuItems.add(new MenuItem() {
                    public ArrayList<String> getDescs() {
                        ArrayList<String> descs = new ArrayList<String>();
                        descs.add(title);
                        descs.add(price);
                        descs.add(authname);
                        descs.add(isbn);
                        return descs;
                    }

                    public void run() {
                        showDetails(cid, isbn);
                    }
                });
            }

            String[] headers = {"title", "price", "one of authors", "ISBN"};
            int[] maxSizes = {30, 15, 30, 30};

            MenuDisplay.chooseAndRun(menuItems, headers, maxSizes, null, true);
        } catch (Exception e) {
            System.out.println("Failed to print results");
            System.err.println(e.getMessage());
            return;
        }
    }

    public static ArrayList<String> simpleSearchMenuDescs() {
        ArrayList<String> descs = new ArrayList<String>();
        descs.add("Simple book search");
        return descs;
    }

    public static void simpleSearchMenu(final int cid) {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String conditions = "";

        try {
            System.out.print("Please enter keywords in one line split by space (title, author, summary, etc.) : ");
            String _keyWords = in.readLine();
            String[] keyWords = _keyWords.split(" ");

            conditions += "true" ;

            for(String _keyWord : keyWords) {
                String keyWord = Utility.sanitize(_keyWord);
                conditions += " AND (" + "title LIKE '%" + keyWord + "%' OR authname like '%" + keyWord +
                        "%' OR summary LIKE '%" + keyWord + "%' OR pubname LIKE '%" + keyWord +
                        "%' OR keyword LIKE '%" + keyWord + "%' OR subject LIKE '%" + keyWord + "%'" +  ") ";
            }

            ResultSet rs = search(cid, conditions);

            showSearchResults(cid, rs);

            // TBD: turn the page

        } catch (Exception e) {
            System.out.println("Failed to print search result");
            System.err.println(e.getMessage());
        }
    }

    public static ArrayList<String> advancedSearchDescs() {
        ArrayList<String> descs = new ArrayList<String>();
        descs.add("Advanced Search");
        return descs;
    }

    public static void advancedSearch(final int cid) {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String conditions = "";

        try {

            while (true) {
                int choice;

                if(conditions.length() != 0) {
                    System.out.println("(0) search now");
                    System.out.println("(1) AND; (2) OR");

                    choice = Integer.parseInt(in.readLine());

                    if (choice == 0)
                        break;
                    else if (choice == 1)
                        conditions += " AND ";
                    else if (choice == 2)
                        conditions += " OR ";
                }


                System.out.println("(0) title includes; (1) author includes;");
                System.out.println("(2) publisher includes; (3) subject includes;");

                choice = Integer.parseInt(in.readLine());

                System.out.println("Includes what? ");

                String included = in.readLine();
                included = Utility.sanitize(included);

                if(choice == 0)
                    conditions += " title LIKE '%" + included + "%'";
                else if(choice == 1)
                    conditions += " authname LIKE '%" + included + "%'";
                else if(choice == 2)
                    conditions += " pubname LIKE '%" + included + "%'";
                else if(choice == 3)
                    conditions += " subject LIKE '%" + included + "%'";
            }
        } catch(Exception e) {
            System.out.println("Failed to build conditions");
            System.err.println(e.getMessage());
            return;
        }


        try {
            ResultSet rs = search(cid, conditions);

            showSearchResults(cid, rs);
        } catch (Exception e) {
            System.out.println("Failed to print search result");
            System.err.println(e.getMessage());
            return;
        }
    }

//    public static ArrayList<String> showDetailsDesc(final String row) {
//        System.out.println(row);
//    }

    public static void showDetails(final int cid, final String isbn) {
        Connector con = Bookstore.con;
        try {
            con.newStatement();
        } catch(Exception e) {
            return ;
        }

        String sql = "SELECT * FROM Book B NATURAL JOIN Publisher P NATURAL JOIN Author A"
                        + " WHERE isbn = " + isbn;

        ResultSet rs = null;
        try {
            rs = con.stmt.executeQuery(sql);
        } catch(Exception e) {
            System.out.println("Failed to get details");
            System.err.println(e.getMessage());
            return ;
        }

        ArrayList<MenuItem> menuItems = new ArrayList<MenuItem>();

        try {
            rs.next();

            final String title = rs.getString("title"),
                    publisher = rs.getString("P.pubname"), pubdate = rs.getString("pubdate"),
                    format = rs.getString("format"), price = rs.getString("price"),
                    copies = rs.getString("copies"), summary = rs.getString("summary");

            menuItems.add(new MenuItem() {
                @Override
                public ArrayList<String> getDescs() {
                    ArrayList<String> descs = new ArrayList<String>();
                    descs.add("Title");
                    descs.add(title);
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
                    descs.add("Publisher");
                    descs.add(publisher);
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
                    descs.add("Publish date");
                    descs.add(pubdate);
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
                    descs.add("Format");
                    descs.add(format);
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
                    descs.add("Price");
                    descs.add(price);
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
                    descs.add("copies");
                    descs.add(copies);
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
                    descs.add("summary");
                    descs.add(summary == null ? "" : summary.replace("\n", " "));
                    return descs;
                }

                @Override
                public void run() {

                }
            });

            int[] maxSizes = {30, 100};
            MenuDisplay.show(menuItems, null, maxSizes, null, true);

        } catch (Exception e) {
            System.out.println("Failed to print details");
            System.err.println(e.getMessage());
        }

        sql = "SELECT * FROM WrittenBy W NATURAL JOIN Author WHERE W.isbn = '" + isbn + "'";
        //System.err.println(sql);
        try {
            rs = con.stmt.executeQuery(sql);
        } catch(Exception e) {
            System.out.println("Failed to get author(s)");
            System.err.println(e.getMessage());
        }
        try {
            menuItems = new ArrayList<MenuItem>();

            while(rs.next()) {
                final String authname = rs.getString("authname");

                menuItems.add(new MenuItem() {
                    @Override
                    public ArrayList<String> getDescs() {
                        ArrayList<String> descs = new ArrayList<String>();
                        descs.add(authname);
                        return descs;
                    }

                    @Override
                    public void run() {

                    }
                });

            }

            String[] headers = {"Author"};
            int[] maxSizes = {30};
            MenuDisplay.show(menuItems, headers, maxSizes, null, false);

        } catch (Exception e) {
            System.out.println("Failed to print author(s)");
            System.err.println(e.getMessage());
        }

        if(cid == -1) return;

        try {
            menuItems = new ArrayList<MenuItem>();

            menuItems.add(new MenuItem() {
                public ArrayList<String> getDescs() {
                    return Order.add2CartDescs();
                }
                public void run() {
                    Order.add2Cart(cid, isbn);
                }
            });
            menuItems.add(new MenuItem() {
                @Override
                public ArrayList<String> getDescs() {
                    return Book.showSuggestionsDescs();
                }

                @Override
                public void run() {
                    Book.showSuggestions(isbn);
                }
            });
            menuItems.add(new MenuItem() {
                @Override
                public ArrayList<String> getDescs() {
                    return Feedback.recordDescs();
                }

                @Override
                public void run() {
                    Feedback.record(cid, isbn);
                }
            });
            menuItems.add(new MenuItem() {
                @Override
                public ArrayList<String> getDescs() {
                    return Feedback.showFeedbacksDescs();
                }

                @Override
                public void run() {
                    Feedback.showFeedbacks(isbn, cid, 100);
                }
            });

            int[] maxSizes = {50};
            MenuDisplay.chooseAndRun(menuItems, null, maxSizes, null, false);
        } catch(Exception e) {
            System.out.println("Failed to show more options");
            System.err.println(e.getMessage());
        }
    }

    public static ArrayList<String> addBookDescs() {
        ArrayList<String> descs = new ArrayList<String>();
        descs.add("Add a book");
        return descs;
    }

    public static void addBook() {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String isbn = null, title = null, format = null, price_s = null, pubdate, copies_s = null, summary;
        int authid, pid;
        float price, copies;

        try {
            do {
                System.out.print("Please enter ISBN : ");
            }
            while ((isbn = in.readLine()) == null || isbn.length() == 0);

            do {
                System.out.print("Please enter title : ");
            }
            while ((title = in.readLine()) == null || title.length() == 0);

            pid = Publisher.choose();

            do {
                System.out.print("Please enter copies : ");
            }
            while ((copies_s = in.readLine()) == null || copies_s.length() == 0);
            copies = Integer.parseInt(copies_s);

            do {
                System.out.print("Please enter price : ");
            }
            while ((price_s = in.readLine()) == null || price_s.length() == 0);
            price = Float.parseFloat(price_s);

            do {
                System.out.print("Please enter publish date : ");
            }
            while ((pubdate = in.readLine()) == null || pubdate.length() == 0);

            do {
                System.out.print("Please enter format(optional) : ");
            }
            while ((format = in.readLine()) == null);

            do {
                System.out.print("Please enter summary(optional) : ");
            }
            while ((summary = in.readLine()) == null);
        } catch(Exception e) {
            System.out.println("Failed to read details");
            System.err.println(e.getMessage());
            return;
        }

        try {
            String sql = "INSERT INTO Book (isbn, title, pid, copies, price, pubdate, format, summary) VALUES ";
            sql += "('" + isbn + "','" + title + "'," + pid + "," + copies + "," + price + ",'" + pubdate + "',";
            if(format != null && format.length() > 0) sql += "'" + format + "'"; else sql += "null";
            sql += summary == null ? ", null" : ",'" + summary + "'";
            sql += ")";
            //System.err.println(sql);

            Connector con = Bookstore.con;
            try {
                con.newStatement();
            } catch(Exception e) {
                return ;
            }
            con.stmt.executeUpdate(sql);
        } catch (Exception e) {
            System.out.println("Failed to add the book into database");
            System.err.println(e.getMessage());
            return;
        }

        try {
            Author.writtenBy(isbn);
        } catch (Exception e) {
            System.out.println("Failed to add authors");
            System.err.println(e.getMessage());
        }
    }

    public static ArrayList<String> replenishDescs() {
        ArrayList<String> descs = new ArrayList<String>();
        descs.add("Arrival of more copies");
        return descs;
    }

    public static void replenish() {
        int num = 0;
        String isbn;

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

            System.out.println("Please enter isbn : ");
            isbn = in.readLine();

            System.out.println("Please enter the amount of new copies : ");
            num = Integer.parseInt(in.readLine());
        } catch (Exception e) {
            System.out.println("Failed to read amount");
            System.err.println(e.getMessage());
            return;
        }

        try {
            String sql = "UPDATE Book SET copies = copies + " + num + " WHERE isbn = '" + isbn + "'";
            Connector con = Bookstore.con;
            try {
                con.newStatement();
            } catch(Exception e) {
                return ;
            }
            con.stmt.execute(sql);
        } catch(Exception e) {
            System.out.println("Failed to update the amount");
            System.err.println(e.getMessage());
            return ;
        }
    }

    public static ArrayList<String> showPopularBooksDescs() {
        ArrayList<String> descs = new ArrayList<String>();
        descs.add("Show most popular books in a certain period");
        return descs;
    }

    public static void showPopularBooks() {
        int m;
        String st, ed;

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

            System.out.println("Please enter the amount of the most popular books you want to see");
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
            String sql = "SELECT isbn, SUM(amount) as sales FROM ItemInOrder I, Orders O " +
                    "WHERE I.orderid = O.orderid AND O.time >= '" + st + "' AND O.time <= '" + ed +
                    "' GROUP BY isbn ORDER BY SUM(amount) DESC";
            System.err.println(sql);

            Connector con = Bookstore.con;
            ResultSet rs = con.stmt.executeQuery(sql);

            while(rs.next() && m-- > 0) {
                final String isbn = rs.getString("isbn");
                final String sales = rs.getString("sales");

                menuItems.add(new MenuItem() {
                    @Override
                    public ArrayList<String> getDescs() {
                        ArrayList<String> descs = new ArrayList<String>();
                        descs.add(isbn);
                        descs.add(sales);
                        return descs;
                    }

                    @Override
                    public void run() {
                        Book.showDetails(-1, isbn);
                    }
                });
            }
        } catch (Exception e) {
            System.out.println("Failed to query");
            System.err.println(e.getMessage());
            return;
        }

        String[] headers = {"ISBN", "sales"};
        int[] maxSizes = {30, 30};

        MenuDisplay.chooseAndRun(menuItems, headers, maxSizes, null, true);
    }

    public static ArrayList<String> suggest(final String isbn) {
        try {
            String sql = "SELECT I2.isbn, SUM(I2.amount) FROM ItemInOrder I1, ItemInOrder I2, Orders O1, Orders O2 WHERE " +
                    "O1.cid = O2.cid AND O1.orderid = I1.orderid AND O2.orderid = I2.orderid AND " +
                    "I1.isbn='" + isbn + "'" + " AND I2.isbn != '" + isbn + "'" +
                    " GROUP BY I2.isbn";

            //System.err.println(sql);
            Connector con = Bookstore.con;

            ResultSet rs = con.stmt.executeQuery(sql);

            ArrayList<String> suggestions = new ArrayList<String>();

            while(rs.next()) {
                suggestions.add(rs.getString("I2.isbn"));
            }

            return suggestions;
        } catch(Exception e) {
            System.out.println("Failed to query");
            System.err.println(e.getMessage());
            return null;
        }
    }

    public static ArrayList<String> showSuggestionsDescs() {
        ArrayList<String> descs = new ArrayList<String>();
        descs.add("Give you some suggested books");
        return descs;
    }

    public static void showSuggestions(final String isbn) {
        ArrayList<String> suggestions = suggest(isbn);

        try {
            Connector con = Bookstore.con;

            ArrayList<MenuItem> menuItems = new ArrayList<MenuItem>();

            for (String suggestion : suggestions) {
                String sql = "SELECT * FROM Book WHERE isbn = '" + suggestion + "'";
                System.err.println(sql);
                ResultSet rs = con.stmt.executeQuery(sql);
                rs.next();

                final String title = rs.getString("title");
                final String isbn2 = rs.getString("isbn");

                menuItems.add(new MenuItem() {
                    @Override
                    public ArrayList<String> getDescs() {
                        ArrayList<String> descs = new ArrayList<String>();
                        descs.add(title);
                        descs.add(isbn2);
                        return descs;
                    }

                    @Override
                    public void run() {
                        Book.showDetails(-1, isbn2);
                    }
                });
            }

            String[] headers = {"Title", "ISBN"};
            int[] maxSizes = {30, 30};
            MenuDisplay.chooseAndRun(menuItems, headers, maxSizes, null, true);

        } catch(Exception e) {
            System.out.print("Failed to print suggestions");
            System.err.println(e.getMessage());
            return;
        }
    }
}
