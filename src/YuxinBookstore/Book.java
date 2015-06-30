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
    private static JsonObjectBuilder JSONBook(ResultSet rs, JsonObjectBuilder book) throws Exception {
        String sql;

        Connector con2 = null;

        try {
            con2 = new Connector();

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
            String img = rs.getString("img");
            book.add("img", img == null ? "" : img);
            int pid = rs.getInt("pid");
            book.add("publisher", pid);

            sql = "SELECT * FROM WrittenBy W WHERE W.isbn = '" + isbn + "'";
            con2.newStatement();
            ResultSet rs2 = con2.stmt.executeQuery(sql);

            JsonArrayBuilder authors = Json.createArrayBuilder();
            while (rs2.next()) {
                int authid = rs2.getInt("authid");
                authors.add(authid);
            }
            book.add("authors", authors);

            sql = "SELECT fid FROM Feedback WHERE isbn = '" + isbn + "'";

            JsonArrayBuilder feedbacks = Json.createArrayBuilder();
            con2.newStatement();
            rs2 = con2.stmt.executeQuery(sql);
            while (rs2.next()) {
                feedbacks.add(rs2.getInt("fid"));
            }

            book.add("feedbacks", feedbacks);


            sql = "SELECT I2.isbn, SUM(I2.amount) as sales FROM ItemInOrder I1, ItemInOrder I2, Orders O1, Orders O2 WHERE " +
                    "O1.cid = O2.cid AND O1.orderid = I1.orderid AND O2.orderid = I2.orderid AND " +
                    "I1.isbn='" + isbn + "'" + " AND I2.isbn != '" + isbn + "'" +
                    " GROUP BY I2.isbn" +
                    " ORDER BY sales DESC " +
                    " LIMIT 5 ";


            //System.err.println(sql);

            con2.newStatement();
            rs2 = con2.stmt.executeQuery(sql);

            JsonArrayBuilder suggestions = Json.createArrayBuilder();
            while (rs2.next()) {
                suggestions.add(rs2.getString("I2.isbn"));
            }

            book.add("suggestions", suggestions);

            if (con2 != null) con2.closeConnection();
            return book;
        } catch(Exception e) {
            if(con2!=null) con2.closeConnection();
            throw new Exception();
        }
    }

    private static JsonObjectBuilder JSONBook(String ISBN, JsonObjectBuilder book) throws Exception {
        Connector con = null;
        try {
            con = new Connector();
            String sql = "SELECT * from Book where isbn = '" + ISBN + "'";
            ResultSet rs = con.stmt.executeQuery(sql);
            rs.next();
            book = JSONBook(rs, book);
            if (con != null) con.closeConnection();
            return book;
        } catch(Exception e) {
            if(con!=null) con.closeConnection();
            throw new Exception();
        }
    }

    public static String details(final String isbn) {
        JsonObjectBuilder result = Json.createObjectBuilder();
        Connector con = null;

        try {
            con = new Connector();
            System.err.println("Connected to the database.");
        } catch (Exception e) {
            System.err.println("Cannot connect to the database.");
            System.err.println(e.getMessage());

            if(con!=null) con.closeConnection(); return null;
        }

        // get details
        String sql = "SELECT * FROM Book B"
                + " WHERE isbn = " + isbn;

        ResultSet rs = null;
        try {
            rs = con.stmt.executeQuery(sql);
        } catch (Exception e) {
            System.out.println("Failed to get details");
            System.err.println(e.getMessage());
            if(con!=null) con.closeConnection(); return null;
        }

        JsonObjectBuilder book = Json.createObjectBuilder();

        try {
            rs.next();

            book = JSONBook(rs, book);

        } catch (Exception e) {
            System.err.println("Failed to add details of this book into result");
            System.err.println(e);

            if(con!=null) con.closeConnection(); return null;
        }

        result.add("book", book);

        if(con!=null) con.closeConnection();
        return result.build().toString();
    }

    public static String add(JsonObject payload) {
        JsonObject book = payload.getJsonObject("book");
        JsonObjectBuilder newBook = Json.createObjectBuilder();
        JsonObjectBuilder result = Json.createObjectBuilder();
        Connector con = null;
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
            String keyword = book.isNull("keyword") ? null : book.getString("keyword");
            String subtitle = book.isNull("subtitle") ? null : book.getString("subtitle");
            String img = book.isNull("img") ? null : book.getString("img");

            String sql = "INSERT INTO Book (isbn, title, pid, copies, price, pubdate, " +
                    "format, summary, subject, keyword, subtitle, img) VALUES ";
            sql += "('" + isbn + "','" + title + "'," + pid + "," + copies + "," + price + ",'" + pubdate + "',";
            sql += Utility.genStringAttr(format, ",");
            sql += Utility.genStringAttr(summary, ",");
            sql += Utility.genStringAttr(subject, ",");
            sql += Utility.genStringAttr(keyword, ",");
            sql += Utility.genStringAttr(subtitle, ",");
            sql += Utility.genStringAttr(img, "");
            sql += ") ON DUPLICATE KEY UPDATE copies=VALUES(copies)";

//            System.err.println(sql);
            con = new Connector();
            con.stmt.executeUpdate(sql);

            newBook.add("ISBN", isbn);

            sql = "INSERT INTO WrittenBy (isbn, authid) VALUES ";
            boolean first = true;
            for (JsonValue authid : book.getJsonArray("authors")) {
                if (!first) sql += ',';
                else first = false;
                String _authid = authid.toString();
                _authid = _authid.substring(1, _authid.length() - 1);
                sql += "('" + isbn + "'," + _authid + ")";
            }
            sql += " ON DUPLICATE KEY UPDATE isbn=isbn";

            con.stmt.executeUpdate(sql);

        } catch (Exception e) {
            System.out.println("Failed to add the book into database");
            System.err.println(e.getMessage());

            if(con!=null) con.closeConnection(); return null;
        }

        result.add("book", newBook);
        if(con!=null) con.closeConnection();
        return result.build().toString();
    }

    public static String simpleSearch(int cid, int limit, int offset, String all, String _orderBy) {
        int orderBy = _orderBy == null ? 1 : Integer.parseInt(_orderBy);
        JsonObjectBuilder result = Json.createObjectBuilder();
        JsonArrayBuilder books = Json.createArrayBuilder();
        Connector con = null;
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

            con = new Connector();

            String sql = "SELECT * FROM Book B, Publisher P, WrittenBy W, Author A WHERE ";
            sql += " B.pid = P.pid AND W.isbn = B.isbn AND A.authid = W.authid AND ";
            sql += conditions;
            sql += " GROUP BY B.isbn ";
            if (orderBy == 0) {
                sql += " ORDER BY pubdate ASC";
            } else if (orderBy == 1) {
                sql += " ORDER BY pubdate DESC";
            } else if (orderBy == 2) {
                sql += " ORDER BY (SELECT AVG(score) FROM Feedback F WHERE F.isbn = B.isbn)ASC";
            } else if (orderBy == 3) {
                sql += " ORDER BY (SELECT AVG(score) FROM Feedback F WHERE F.isbn = B.isbn)DESC";
            } else if (orderBy == 4) {
                sql += " ORDER BY (SELECT AVG(score) FROM Feedback F WHERE F.isbn = B.isbn AND " +
                        "(F.cid = " + cid + " OR F.cid IN ( " +
                        "SELECT T.cid2 FROM TrustRecords T WHERE T.trust = TRUE AND T.cid1 = " + cid + ")))ASC";
            } else if (orderBy == 5) {
                sql += " ORDER BY (SELECT AVG(score) FROM Feedback F WHERE F.isbn = B.isbn AND " +
                        "(F.cid = " + cid + " OR F.cid IN ( " +
                        "SELECT T.cid2 FROM TrustRecords T WHERE T.trust = TRUE AND T.cid1 = " + cid + ")))DESC";
            }

            sql += " LIMIT " + limit + " OFFSET " + offset;
//            System.out.println(sql);

            ResultSet rs = con.stmt.executeQuery(sql);
            while (rs.next()) {
                JsonObjectBuilder book = Json.createObjectBuilder();

                book = JSONBook(rs, book);

                books.add(book);
            }


            sql = "SELECT COUNT(DISTINCT B.isbn) as total FROM Book B, Publisher P, WrittenBy W, Author A WHERE ";
            sql += " B.pid = P.pid AND W.isbn = B.isbn AND A.authid = W.authid AND ";
            sql += conditions;

//            System.out.println(sql);
            rs = con.stmt.executeQuery(sql);
            rs.next();
            int total = rs.getInt("total");

            JsonObjectBuilder meta = Json.createObjectBuilder();
            meta.add("total", total);
            result.add("meta", meta);

            result.add("books", books);
            if(con!=null) con.closeConnection();
            return result.build().toString();
        } catch (Exception e) {
            System.out.println("Simple search failed");
            System.err.println(e.getMessage());
            if(con!=null) con.closeConnection(); return null;
        }
    }


    public static String advancedSearch(int cid, int limit, int offset, JsonArray advanced, String _orderBy) {
        Connector con = null;
        try {
            int orderBy = _orderBy == null ? 1 : Integer.parseInt(_orderBy);
            JsonObjectBuilder result = Json.createObjectBuilder();
            JsonArrayBuilder books = Json.createArrayBuilder();
            String conditions = "";

            for (int i = 0; i < advanced.size(); i++) {
                JsonObject condition = advanced.getJsonObject(i);
                String term = condition.getString("term");
                String included = Utility.sanitize(condition.getString("cond"));
                String conj = condition.getString("conj");

                if (term.equals("Title"))
                    conditions += " title LIKE '%" + included + "%'";
                else if (term.equals("Author"))
                    conditions += " authname LIKE '%" + included + "%'";
                else if (term.equals("Publisher"))
                    conditions += " pubname LIKE '%" + included + "%'";
                else if (term.equals("Subject"))
                    conditions += " subject LIKE '%" + included + "%'";
                else {if(con!=null) con.closeConnection(); return null;}

                if (i != advanced.size() - 1) {
                    if (conj.equals("OR"))
                        conditions += " OR ";
                    else
                        conditions += " AND ";
                }
            }

            String sql = "SELECT * FROM Book B, Publisher P, WrittenBy W, Author A WHERE ";
            sql += " B.pid = P.pid AND W.isbn = B.isbn AND A.authid = W.authid AND ";
            sql += conditions;
            sql += " GROUP BY B.isbn ";
            if (orderBy == 0) {
                sql += " ORDER BY pubdate ASC";
            } else if (orderBy == 1) {
                sql += " ORDER BY pubdate DESC";
            } else if (orderBy == 2) {
                sql += " ORDER BY (SELECT AVG(score) FROM Feedback F WHERE F.isbn = B.isbn)ASC";
            } else if (orderBy == 3) {
                sql += " ORDER BY (SELECT AVG(score) FROM Feedback F WHERE F.isbn = B.isbn)DESC";
            } else if (orderBy == 4) {
                sql += " ORDER BY (SELECT AVG(score) FROM Feedback F WHERE F.isbn = B.isbn AND " +
                        "(F.cid = " + cid + " OR F.cid IN ( " +
                        "SELECT T.cid2 FROM TrustRecords T WHERE T.trust = TRUE AND T.cid1 = " + cid + ")))ASC";
            } else if (orderBy == 5) {
                sql += " ORDER BY (SELECT AVG(score) FROM Feedback F WHERE F.isbn = B.isbn AND " +
                        "(F.cid = " + cid + " OR F.cid IN ( " +
                        "SELECT T.cid2 FROM TrustRecords T WHERE T.trust = TRUE AND T.cid1 = " + cid + ")))DESC";
            }

            sql += " LIMIT " + limit + " OFFSET " + offset;

            con = new Connector();
            ResultSet rs = con.stmt.executeQuery(sql);
            while (rs.next()) {
                JsonObjectBuilder book = Json.createObjectBuilder();

                book = JSONBook(rs, book);

                books.add(book);
            }

            sql = "SELECT COUNT(DISTINCT B.isbn) as total FROM Book B, Publisher P, WrittenBy W, Author A WHERE ";
            sql += " B.pid = P.pid AND W.isbn = B.isbn AND A.authid = W.authid AND ";
            sql += conditions;

            rs = con.stmt.executeQuery(sql);
            rs.next();
            int total = rs.getInt("total");

            JsonObjectBuilder meta = Json.createObjectBuilder();
            meta.add("total", total);
            result.add("meta", meta);

            result.add("books", books);
            if(con!=null) con.closeConnection();
            return result.build().toString();
        } catch (Exception e) {
            System.out.println("Failed to build conditions");
            System.err.println(e.getMessage());
            if(con!=null) con.closeConnection(); return null;
        }
    }

    public static String popular(int limit, int offset, String start, String end) {
        JsonObjectBuilder result = Json.createObjectBuilder();
        JsonArrayBuilder books = Json.createArrayBuilder();
        JsonObjectBuilder sales = Json.createObjectBuilder();
        String st = start.split("T")[0];
        String ed = end.split("T")[0];
        Connector con = null;
        try {
            String sql = "SELECT isbn, SUM(amount) as sales FROM ItemInOrder I, Orders O " +
                    "WHERE I.orderid = O.orderid AND O.time >= '" + st + "' AND O.time <= '" + ed +
                    "' GROUP BY isbn ORDER BY SUM(amount) DESC";
            sql += " LIMIT " + limit + " OFFSET " + offset;
//            System.err.println(sql);

            con = new Connector();
            ResultSet rs = con.stmt.executeQuery(sql);

            while (rs.next()) {
                final String isbn = rs.getString("isbn");
                final int _sales = rs.getInt("sales");

                JsonObjectBuilder book = Json.createObjectBuilder();
                book = JSONBook(isbn, book);
                books.add(book);
                sales.add(isbn, _sales);
            }

            sql = "SELECT COUNT(DISTINCT isbn) AS total FROM ItemInOrder I, Orders O " +
                    "WHERE I.orderid = O.orderid AND O.time >= '" + st + "' AND O.time <= '" + ed + "'";
            rs = con.stmt.executeQuery(sql);
            rs.next();
            JsonObjectBuilder meta = Json.createObjectBuilder();
            meta.add("total", rs.getInt("total"));
            result.add("meta", meta);

            result.add("books", books);
            result.add("sales", sales);
            if(con!=null) con.closeConnection();
            return result.build().toString();
        } catch (Exception e) {
            System.out.println("Failed to query popular books");
            System.err.println(e.getMessage());
            if(con!=null) con.closeConnection(); return null;
        }
    }

};