/**
 * Created by Orthocenter on 5/11/15.
 */

// format: ChenYuxin13307130248
// ddl 19th May, 10pm

package YuxinBookstore;

import java.awt.*;
import java.util.ArrayList;

public class Bookstore {

    public static Connector con = null, con2 = null;

    public static void main() {
        try {
            con = new Connector();

            System.err.println("Connected to the database.");
        } catch (Exception e) {
            System.out.println("Cannot connect to the database.");
            System.err.println(e.getMessage());
        }
    }

}