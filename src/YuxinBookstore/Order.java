package YuxinBookstore;

import javax.xml.transform.Result;
import java.awt.*;
import java.io.*;
import java.lang.reflect.Array;
import java.sql.ResultSet;
import java.util.ArrayList;


/**
 * Created by Orthocenter on 5/13/15.
 */


public class Order {

    public static ArrayList<String> add2CartDescs() {
        ArrayList<String> descs = new ArrayList<String>();
        descs.add("Add it into the cart");
        return descs;
    }

    public static void add2Cart(final int cid, final String isbn) {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String amount = null;

        try {
            do{ System.out.println("Please enter the amount : "); }
            while ((amount = in.readLine()) == null || amount.length() == 0) ;
        } catch(Exception e) {
            System.out.println("Failed to read");
            System.err.println(e.getMessage());
            return ;
        }

        try {
            String sql = "INSERT INTO Cart (cid, isbn, amount) VALUES (" + cid + ",'" + isbn + "'," + amount + ") " +
                    "ON DUPLICATE KEY UPDATE amount = amount + " + amount;
            System.err.println(sql);

            Connector con = Bookstore.con;
            try {
                con.newStatement();
            } catch(Exception e) {
                return ;
            }
            con.stmt.execute(sql);

            System.out.println("Successfully");
        } catch(Exception e) {
            System.out.println("Failed to add into shopping cart");
            System.err.println(e.getMessage());
            return ;
        }

        try {
            System.out.println("Added to the shopping cart successfully");

            ArrayList<MenuItem> menuItems = new ArrayList<MenuItem>();

            menuItems.add(new MenuItem() {
                              public ArrayList<String> getDescs() {
                                  return showCartDescs();
                              }
                              public void run() {
                                  showCart(cid);
                              }
                          }
            );


            int[] maxSizes = {30};
            MenuDisplay menuDisplay = new MenuDisplay();
            menuDisplay.chooseAndRun(menuItems, null, maxSizes, null, false);

        } catch(Exception e) {
            System.err.println(e.getMessage());
            return;
        }
    }

    public static ArrayList<String> showCartDescs() {
        ArrayList<String> descs = new ArrayList<String>();
        descs.add("Show all items in your cart");
        return descs;
    }

    public static void showCart(final int cid) {
        try {
            String sql = "SELECT * FROM Cart C NATURAL JOIN Book B WHERE C.cid = " + cid;
            //System.err.println(sql);
            Connector con = Bookstore.con;
            try {
                con.newStatement();
            } catch(Exception e) {
                return ;
            }
            ResultSet rs = con.stmt.executeQuery(sql);

            ArrayList<MenuItem> menuItems = new ArrayList<MenuItem>();

            while(rs.next()) {
                final String title = rs.getString("B.title"), price = rs.getString("B.price"),
                        amount = rs.getString("C.amount"), isbn = rs.getString("isbn");

                menuItems.add(new MenuItem() {
                    @Override
                    public ArrayList<String> getDescs() {
                        ArrayList<String> descs = new ArrayList<String>();
                        descs.add(title);
                        descs.add(price);
                        descs.add(amount);
                        descs.add(isbn);
                        return descs;
                    }

                    @Override
                    public void run() {

                    }
                });
            }

            String[] headers = {"Title", "Price", "Amount", "ISBN"};
            int[] maxSizes = {30, 15, 15, 30};
            MenuDisplay.chooseAndRun(menuItems, headers, maxSizes, null, true);
        } catch (Exception e) {
            System.out.println("Failed to query");
            System.err.println(e.getMessage());
            return;
        }
    }

    public static ArrayList<String> showOrderDetailsDescs(final int orderid) {
        String sql = "SELECT * FROM Orders WHERE orderid = " + orderid;

        try {
            Connector con = Bookstore.con;
            con.newStatement();

            ResultSet rs = con.stmt.executeQuery(sql);
            rs.next();
            final String _orderid = rs.getString("orderid"), time =  rs.getString("time"),
                    cid = rs.getString("cid");

            ArrayList<String> descs = new ArrayList<String>();
            descs.add(_orderid);
            descs.add(time);
            descs.add(cid);
            return descs;

        } catch(Exception e) {
            System.out.println("Failed to get order description");
            System.err.println(e.getMessage());
            return null;
        }
    }

    public static void showOrderDetails(final int orderid) {
        try {
            String sql = "SELECT * FROM Orders O WHERE orderid = " + orderid;
            Connector con = Bookstore.con;
            con.newStatement();

            ResultSet rs = con.stmt.executeQuery(sql);
            rs.next();
            final String time = rs.getString("time");
            //orderid

            final int cid = rs.getInt("cid");
            final int addrid = rs.getInt("addrid");

            sql = "SELECT * FROM Customer C WHERE cid = " + cid;
            rs = con.stmt.executeQuery(sql);
            rs.next();

            final String username = rs.getString("username");

            sql = "SELECT * FROM Address A WHERE addrid = " + addrid;
            rs = con.stmt.executeQuery(sql);
            rs.next();

            final String address = Utility.getFullAddress(rs);
            final String rname = rs.getString("rname");
            final String rphone = rs.getString("rphone");

            sql = "SELECT * FROM ItemInOrder I, Book B WHERE orderid = " + orderid +
                " AND I.isbn = B.isbn";
            rs = con.stmt.executeQuery(sql);

            ArrayList<MenuItem> menuItems = new ArrayList<MenuItem>();
            ArrayList<MenuItem> menuItems2 = new ArrayList<MenuItem>();

            menuItems.add(new MenuItem() {
                @Override
                public ArrayList<String> getDescs() {
                    ArrayList<String> descs = new ArrayList<String>();
                    descs.add("Orderid");
                    descs.add("" + orderid);
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
                    descs.add("Address");
                    descs.add(address);
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
                    descs.add("Time");
                    descs.add(time);
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
                    descs.add("Username");
                    descs.add(username);
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
                    descs.add("Receiver's name");
                    descs.add(rname);
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
                    descs.add("Receiver's phone");
                    descs.add(rphone);
                    return descs;
                }

                @Override
                public void run() {

                }
            });

            int i = 0;
            float totalPrices = 0;

            while(rs.next()) {
                final String title = rs.getString("B.title"), price = rs.getString("I.price"),
                        isbn = rs.getString("B.isbn"), amount = rs.getString("I.amount");

                menuItems2.add(new MenuItem() {
                    @Override
                    public ArrayList<String> getDescs() {
                        ArrayList<String> descs = new ArrayList<String>();
                        descs.add(title);
                        descs.add(price);
                        descs.add(isbn);
                        descs.add(amount);
                        return descs;
                    }

                    @Override
                    public void run() {
                        Book.showDetails(cid, isbn);
                    }
                });

                totalPrices += rs.getFloat("I.price") * rs.getInt("I.amount");
            }


            final String _totalPrices = "" + totalPrices;
            menuItems.add(new MenuItem() {
                @Override
                public ArrayList<String> getDescs() {
                    ArrayList<String> descs = new ArrayList<String>();
                    descs.add("Total price");
                    descs.add(_totalPrices);
                    return descs;
                }

                @Override
                public void run() {

                }
            });


            System.out.print("\u001b[2J");
            System.out.flush();

            int[] maxSizes = {20, 80};
            int[] maxSizes2 = {30, 15, 30, 15};

            MenuDisplay.show(menuItems, null, maxSizes, null, true);
            MenuDisplay.chooseAndRun(menuItems2, null, maxSizes2, null, false);

        } catch(Exception e) {
            System.out.println("Failed to print order details");
            System.err.println(e.getMessage());
            return;
        }
    }

    public static ArrayList<String> showAllOrdersDescs() {
        ArrayList<String> descs = new ArrayList<String>();
        descs.add("Show all orders for a certain customer");
        return descs;
    }

    public static void showAllOrder(int cid) {
        try {
            String sql = "SELECT orderid FROM Orders O WHERE O.cid = " + cid;
            System.err.println(sql);
            Connector con = Bookstore.con;
            try {
                con.newStatement();
            } catch(Exception e) {
                return ;
            }
            ResultSet rs = con.stmt.executeQuery(sql);

            ArrayList<MenuItem> menuItems = new ArrayList<MenuItem>();
            while(rs.next()) {
                final int orderid = rs.getInt("orderid");
                menuItems.add(new MenuItem() {
                    @Override
                    public ArrayList<String> getDescs() {
                        //orderid, time, cid
                        return showOrderDetailsDescs(orderid);
                    }

                    @Override
                    public void run() {
                        showOrderDetails(orderid);
                    }
                });
            }

            String[] headers = {"Order id", "Time", "Customer id"};
            int[] maxSizes = {30, 30, 30};
            MenuDisplay.chooseAndRun(menuItems, headers, maxSizes, null, true);
        } catch (Exception e) {
            System.out.println("Failed to query");
            System.err.println(e.getMessage());
            return;
        }
    }

    public static ArrayList<String> confirmOrderDescs() {
        ArrayList<String> descs = new ArrayList<String>();
        descs.add("Confirm your order");
        return descs;
    }

    public static void confirmOrder(final int cid) {
        Connector con = Bookstore.con;
        try {
            con.newStatement();
        } catch(Exception e) {
            System.out.println("Failed to create new statement");
            System.err.println(e.getMessage());
            return;
        }

        // check available amount
        try {
            String sql = "SELECT B.isbn, B.title FROM Book B, Cart C WHERE C.cid = " + cid +
                    " AND B.isbn = C.isbn AND B.copies < C.amount" ;
            System.err.println(sql);
            ResultSet rs = con.stmt.executeQuery(sql);

            if(rs.next()) {
                System.out.print("No enough books ");
                System.out.println(rs.getString("title"));
                return;
            }
        } catch(Exception e) {
            System.out.println("Failed to check amount limitation");
            System.err.println(e.getMessage());
            return;
        }

        //TBD
        int addrid = Address.choose(cid);
        if(addrid == -1) return;

        // modify amount and record order
        try {
            con.con.setAutoCommit(false);

            String sql = "UPDATE Book SET copies = copies - " +
                    "(SELECT C.amount FROM Cart C WHERE Book.isbn = C.isbn AND C.cid = " + cid + ") " +
                    "WHERE Book.isbn IN (SELECT C.isbn FROM Cart C WHERE C.cid = " + cid + ")";
            //System.err.println(sql);
            con.stmt.addBatch(sql);

            sql = "INSERT INTO Orders (time, cid, addrid) VALUES (NOW(), " + cid + "," + addrid + ")";
            //System.err.println(sql);
            con.stmt.addBatch(sql);

            sql = "INSERT INTO ItemInOrder (orderid, isbn, price, amount) " +
                    "SELECT LAST_INSERT_ID(), C.isbn, B.price, C.amount FROM Cart C, Book B WHERE " +
                    "C.isbn = B.isbn AND C.cid = " + cid;
            //System.err.println(sql);
            con.stmt.addBatch(sql);

            sql = "DELETE FROM Cart WHERE cid = " + cid;
            //System.err.println(sql);
            con.stmt.addBatch(sql);

            con.stmt.executeBatch();
        } catch(Exception e) {
            System.out.println("Failed to update");
            System.err.println(e.getMessage());

            try {
                con.con.rollback();
            } catch(Exception e2) {
                System.out.println("Failed to roll back");
                System.err.println(e2);
            }
        } finally {
            try {
                con.con.setAutoCommit(true);
            } catch(Exception e3) {
                System.err.println(e3);
            }
        }

    }
}
