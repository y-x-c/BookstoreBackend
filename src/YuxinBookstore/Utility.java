package YuxinBookstore;

/**
 * Created by Orthocenter on 5/14/15.
 */

import java.io.*;
import java.sql.ResultSet;

public class Utility {
    public static int getChoice(int bound) {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        String choice;
        int c;
        do {
            try {
                do {
                    System.out.print("Please enter your choice (-1 for return) : ");
                }
                while ((choice = in.readLine()) == null || choice.length() == 0);

                c = Integer.parseInt(choice);
                if (c < -1 || c > bound) throw new Exception();
            } catch (Exception e) {
                System.out.println("Your choice is invalid, please try again.");
                c = -2;
            }
        } while (c == -2);

        return c;
    }

    public static String getShortString(String str, int bound) {
        if(str == null) return "null";
        String ret = "";
        if(str.length() > bound) {
            ret = str.substring(bound) + "...";
        } else {
            ret = str.substring(str.length());
        }
        return ret;
    }

    public static String readPassword() throws Exception {
        Console console = System.console();
        if (console == null) {
            throw new Exception("Couldn't get Console instance");
        }

        char passwordArray[] = console.readPassword();
        return new String(passwordArray);
    }

    public static String getFullAddress(ResultSet rs) {
        String ret = "";

        try {
            ret += "(Room)" + rs.getString("room");
            ret += "(Street)" + rs.getString("street");
            ret += "(district)" + rs.getString("district");
            ret += "(city)" + rs.getString("city");
            ret += "(state)" + rs.getString("state");
            ret += "(country)" + rs.getString("country");
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return null;
        }

        return ret;
    }

    public static String sanitize(String str) {
        return str.replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_")
                .replace("\'", "\\\'")
                .replace("\"", "\\\"");
    }

    public static String genStringAttr(String str, String separator) {
        if(str == null || str.length() == 0) return "null" + separator;
        return "'" + str + "'" + separator;
    }
}
