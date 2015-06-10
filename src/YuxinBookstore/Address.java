package YuxinBookstore;

import org.omg.IOP.TAG_MULTIPLE_COMPONENTS;

import javax.rmi.CORBA.Util;
import java.lang.reflect.Array;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.io.*;

/**
 * Created by Orthocenter on 5/18/15.
 */
public class Address {
    public static int choose(int cid) {
        int i = 0;

        ArrayList<MenuItem> menuItems = new ArrayList<MenuItem>();

        menuItems.add(new MenuItem() {
            public ArrayList<String> getDescs() {
                ArrayList<String> descs = new ArrayList<String>();
                descs.add("Select an existing address");
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
                descs.add("Add a new address");
                return descs;
            }

            @Override
            public void run() {
                return;
            }
        });

        int[] maxSizes = {30};
        String[] manner = {"c"};
        int addrid = -1;

        while (addrid == -1) {
            int c = MenuDisplay.getChoice(menuItems, null, maxSizes, manner, true);

            if (c == 0)
                addrid = search(cid);
            else if (c == 1)
                addrid = add(cid);
            else if (c == -1)
                return -1;
        }

        return addrid;
    }

    public static int search(int cid) {
        Connector con = Bookstore.con;
        try {
            con.newStatement();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return -1;
        }

        try {
            String sql = "SELECT * FROM Address A WHERE A.cid = " + cid;
            ResultSet rs = con.stmt.executeQuery(sql);

            ArrayList<Integer> addrids = new ArrayList<Integer>();
            ArrayList<MenuItem> menuItems = new ArrayList<MenuItem>();

            while(rs.next()) {
                addrids.add(rs.getInt("addrid"));
                final String rname = rs.getString("rname"), rphone = rs.getString("rphone"),
                        country = rs.getString("country"), state = rs.getString("state"),
                        city = rs.getString("city"), district = rs.getString("district"),
                        street = rs.getString("street"), room = rs.getString("room");
                final int zip = rs.getInt("zip");

                menuItems.add(new MenuItem() {
                    @Override
                    public ArrayList<String> getDescs() {
                        ArrayList<String> descs = new ArrayList<String>();
                        descs.add(rname);
                        descs.add(rphone);
                        descs.add(country);
                        descs.add(state);
                        descs.add(city);
                        descs.add(district);
                        descs.add(street);
                        descs.add(room);
                        descs.add("" + zip);
                        return descs;
                    }

                    @Override
                    public void run() {
                        return;
                    }
                });
            }

            String[] headers = {"Receiver's name", "Receiver's phone", "country", "state", "city", "district",
                                "street", "room", "zip"};
            int[] maxSizes = {30, 30, 10, 10, 10, 10, 10, 10, 10};

            int choice = MenuDisplay.getChoice(menuItems, headers, maxSizes, null, true);
            if(choice == - 1) {
                return -1;
            }

            return addrids.get(choice);

        } catch(Exception e) {
            System.out.println("Failed to query");
            System.err.println(e.getMessage());
            return -1;
        }
    }

    public static int add(int cid) {
        String rname, rphone, country, state, city, district, street, room;
        int zip;
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        try {
            System.out.println("Enter receiver's name : ");
            rname = in.readLine();
            System.out.println("Enter receiver's phone number : ");
            rphone = in.readLine();
            System.out.println("Enter country : ");
            country = in.readLine();
            System.out.println("Enter state : ");
            state = in.readLine();
            System.out.println("Enter city : ");
            city = in.readLine();
            System.out.println("Enter district : ");
            district = in.readLine();
            System.out.println("Enter street : ");
            street = in.readLine();
            System.out.println("Enter room : ");
            room = in.readLine();
            System.out.println("Enter zip : ");
            zip = Integer.parseInt(in.readLine());
        } catch(Exception e) {
            System.out.println("Failed to read address information");
            System.err.println(e.getMessage());
            return -1;
        }

        try {
            String sql = "INSERT INTO Address (cid, rname, rphone, country, state, city, district, street, room, zip) VALUES (";
            sql += cid + ",";
            sql += Utility.genStringAttr(rname, ",");
            sql += Utility.genStringAttr(rphone, ",");
            sql += Utility.genStringAttr(country, ",");
            sql += Utility.genStringAttr(state, ",");
            sql += Utility.genStringAttr(city, ",");
            sql += Utility.genStringAttr(district, ",");
            sql += Utility.genStringAttr(street, ",");
            sql += Utility.genStringAttr(room, ",");
            sql += zip + ")";

            Connector con = Bookstore.con;
            con.newStatement();
            con.stmt.execute(sql);

            sql = "SELECT LAST_INSERT_ID()";
            ResultSet rs = con.stmt.executeQuery(sql);
            if(rs.next()) return rs.getInt(1);

        } catch(Exception e) {
            System.out.println("Failed to add address");
            System.err.println(e.getMessage());
            return -1;
        }

        return -1;
    }

}
