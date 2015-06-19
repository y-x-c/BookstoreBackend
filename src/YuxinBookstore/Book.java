package YuxinBookstore;

/**
 * Created by Orthocenter on 5/12/15.
 */

import com.sun.media.sound.UlawCodec;
import org.omg.IOP.TAG_MULTIPLE_COMPONENTS;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.util.ArrayList;
import javax.json.*;
import javax.rmi.CORBA.Util;

public class Book {
    public static String details(final String isbn) {
        JsonObjectBuilder result = Json.createObjectBuilder();
        Connector con = null;

        try {
            con = new Connector();
            System.err.println("Connected to the database.");
        } catch (Exception e) {
            System.err.println("Cannot connect to the database.");
            System.err.println(e.getMessage());

            return null;
        }

        // get details
        String sql = "SELECT * FROM Book B"
                + " WHERE isbn = " + isbn;

        ResultSet rs = null;
        try {
            rs = con.stmt.executeQuery(sql);
        } catch(Exception e) {
            System.out.println("Failed to get details");
            System.err.println(e.getMessage());
            return null;
        }

        JsonObjectBuilder book = Json.createObjectBuilder();

        try {
            rs.next();

            book.add("ISBN", isbn);
            String title = rs.getString("title");
            book.add("title", title);
            String subtitle = rs.getString("subtitle");
            book.add("subtitle", subtitle == null ? "" : subtitle);
            double price = rs.getDouble("price");
            book.add("price", price);
            int amount = rs.getInt("copies");
            book.add("amount", amount);
            String pubdate = rs.getString("pubdate");
            book.add("pubdate", pubdate == null ? "" : pubdate);
            String format = rs.getString("format");
            book.add("format", format == null ? "" : format);
            String keyword = rs.getString("keyword");
            book.add("keyword", keyword == null ? "" : format);
            String subject = rs.getString("subject");
            book.add("subject", subject == null ? "" : subject);
            String summary = rs.getString("summary");
            book.add("summary", summary == null ? "" : summary);
            int pid = rs.getInt("pid");
            book.add("publisher", pid);
        } catch (Exception e) {
            System.err.println("Failed to add details of this book into result");
            System.err.println(e);

            return null;
        }

        // get authors info
        sql = "SELECT * FROM WrittenBy W WHERE W.isbn = '" + isbn + "'";

        try {
            rs = con.stmt.executeQuery(sql);
        } catch(Exception e) {
            System.out.println("Failed to get author(s)");
            System.err.println(e.getMessage());

            return null;
        }

        try {
            JsonArrayBuilder authors = Json.createArrayBuilder();
            while(rs.next()) {
                int authid = rs.getInt("authid");
                authors.add(authid);
            }
            book.add("authors", authors);
        } catch (Exception e) {
            System.out.println("Failed to add author(s) into result");
            System.err.println(e.getMessage());

            return null;
        }

        try {
            sql = "SELECT fid FROM Feedback WHERE isbn = '" + isbn + "'";

            JsonArrayBuilder feedbacks = Json.createArrayBuilder();
            rs = con.stmt.executeQuery(sql);
            while(rs.next()) {
                feedbacks.add(rs.getInt("fid"));
            }

            book.add("feedbacks", feedbacks);
        } catch (Exception e) {
            System.out.println("Failed to add feedback(s) into result");
            System.err.println(e.getMessage());

            return null;
        }

        result.add("book", book);

        return result.build().toString();
    }

    public static String add(JsonObject payload) {
        JsonObject book = payload.getJsonObject("book");
        JsonObjectBuilder newBook = Json.createObjectBuilder();
        JsonObjectBuilder result = Json.createObjectBuilder();

        try {
            String isbn = book.getString("ISBN");
            String title = book.getString("title");
            int pid = Integer.parseInt(book.getString("publisher"));
            int copies = book.getInt("amount");
            double price = book.getJsonNumber("price").doubleValue();

            String pubdate = book.getString("pubdate").split("T")[0];
            String format = book.isNull("format") ? null : book.getString("format");
            String summary = book.isNull("summary") ? null : book.getString("summary");
            String subject = book.isNull("subject") ? null : book.getString("subject");
            String keyword = book.isNull("keyword") ? null : book.getString("keyword");;
            String subtitle = book.isNull("subtitle") ? null : book.getString("subtitle");;

            String sql = "INSERT INTO Book (isbn, title, pid, copies, price, pubdate, " +
                    "format, summary, subject, keyword, subtitle) VALUES ";
            sql += "('" + isbn + "','" + title + "'," + pid + "," + copies + "," + price + ",'" + pubdate + "',";
            sql += Utility.genStringAttr(format, ",");
            sql += Utility.genStringAttr(summary, ",");
            sql += Utility.genStringAttr(subject, ",");
            sql += Utility.genStringAttr(keyword, ",");
            sql += Utility.genStringAttr(subtitle, "");
            sql += ") ON DUPLICATE KEY UPDATE copies=VALUES(copies)";

//            System.err.println(sql);
            Connector con = new Connector();
            con.stmt.executeUpdate(sql);

            newBook.add("ISBN", isbn);

            sql = "INSERT INTO WrittenBy (isbn, authid) VALUES ";
            boolean first = true;
            for(JsonValue authid : book.getJsonArray("authors")) {
                if(!first) sql += ','; else first = false;
                String _authid = authid.toString();
                _authid = _authid.substring(1, _authid.length() - 1);
                sql += "('" + isbn + "'," + _authid + ")";
            }
            sql += " ON DUPLICATE KEY UPDATE isbn=isbn";

            con.stmt.executeUpdate(sql);

        } catch (Exception e) {
            System.out.println("Failed to add the book into database");
            System.err.println(e.getMessage());

            return null;
        }

        result.add("book", newBook);
        return result.build().toString();
    }

    public static String simpleSearch(int cid, String all, String _orderBy) {
        int orderBy = _orderBy == null ? 1 : Integer.parseInt(_orderBy);
        JsonObjectBuilder result = Json.createObjectBuilder();
        JsonArrayBuilder books = Json.createArrayBuilder();

        try {
            String[] keyWords = all.split(" ");

            String conditions = "true";

            for (String _keyWord : keyWords) {
                System.err.println(_keyWord);
                String keyWord = Utility.sanitize(_keyWord);
                conditions += " AND (" + "B.title LIKE '%" + keyWord + "%' OR B.subtitle like '%" + keyWord +
                        "%' OR A.authname like '%" + keyWord + "%' OR B.isbn like '%" + keyWord +
                        "%' OR B.summary LIKE '%" + keyWord + "%' OR P.pubname LIKE '%" + keyWord +
                        "%' OR B.keyword LIKE '%" + keyWord + "%' OR B.subject LIKE '%" + keyWord + "%'" + ") ";
            }

            Connector con = new Connector();

            String sql = "SELECT * FROM Book B, Publisher P, WrittenBy W, Author A WHERE ";
            sql += " B.pid = P.pid AND W.isbn = B.isbn AND A.authid = W.authid AND ";
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

            ResultSet rs = con.stmt.executeQuery(sql);
            Connector con2 = new Connector();
            while (rs.next()) {
                JsonObjectBuilder book = Json.createObjectBuilder();

                String isbn = rs.getString("isbn");
                book.add("ISBN", isbn);
                String title = rs.getString("title");
                book.add("title", title);
                String subtitle = rs.getString("subtitle");
                book.add("subtitle", subtitle == null ? "" : subtitle);
                double price = rs.getDouble("price");
                book.add("price", price);
                int amount = rs.getInt("copies");
                book.add("amount", amount);
                String pubdate = rs.getString("pubdate");
                book.add("pubdate", pubdate == null ? "" : pubdate);
                String format = rs.getString("format");
                book.add("format", format == null ? "" : format);
                String keyword = rs.getString("keyword");
                book.add("keyword", keyword == null ? "" : format);
                String subject = rs.getString("subject");
                book.add("subject", subject == null ? "" : subject);
                String summary = rs.getString("summary");
                book.add("summary", summary == null ? "" : summary);
                int pid = rs.getInt("pid");
                book.add("publisher", pid);

                sql = "SELECT * FROM WrittenBy W WHERE W.isbn = '" + isbn + "'";
                con2.newStatement();
                ResultSet rs2 = con2.stmt.executeQuery(sql);

                JsonArrayBuilder authors = Json.createArrayBuilder();
                while(rs2.next()) {
                    int authid = rs2.getInt("authid");
                    authors.add(authid);
                }
                book.add("authors", authors);

                sql = "SELECT fid FROM Feedback WHERE isbn = '" + isbn + "'";

                JsonArrayBuilder feedbacks = Json.createArrayBuilder();
                con2.newStatement();
                System.err.println(sql);
                rs2 = con2.stmt.executeQuery(sql);
                while(rs2.next()) {
                    feedbacks.add(rs2.getInt("fid"));
                }

                book.add("feedbacks", feedbacks);


                books.add(book);
            }

        } catch(Exception e) {
            System.out.println("Simple search failed");
            System.err.println(e.getMessage());
            return null;
        }

        result.add("books", books);
        return result.build().toString();
    }

    ////////////////////////////////////
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

    public static void showDetails(final int cid, final String isbn) {

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
