package YuxinBookstore;

/**
 * Created by Orthocenter on 5/14/15.
 */

import java.io.*;
import java.sql.ResultSet;

public class Utility {

    public static String sanitize(String str) {
        return str.replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_")
                .replace("\'", "\\\'")
                .replace("\"", "\\\"");
    }

    public static String genStringAttr(String str, String separator) {
        if(str == null || str.length() == 0) return "null" + separator;
        return "'" + Utility.sanitize(str) + "'" + separator;
    }
}
