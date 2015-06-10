 /**
 * Created by Orthocenter on 5/11/15.
 */

// format: ChenYuxin13307130248
// ddl 19th May, 10pm

package YuxinBookstore;

 import java.awt.*;
 import java.util.ArrayList;

 public class Bookstore {

    public static Connector con = null;

    public static void displayMenu() {
        ArrayList<MenuItem> menuItems = new ArrayList<MenuItem>();
        int[] maxSizes = {35};
        String[] manners = {"c"};

        menuItems.add(new MenuItem() {
            @Override
            public ArrayList<String> getDescs() {
                return Customer.mainMenuDescs();
            }

            @Override
            public void run() {
                Customer.mainMenu();
            }
        });

        menuItems.add(new MenuItem() {
            @Override
            public ArrayList<String> getDescs() {
                return Admin.mainMenuDescs();
            }

            @Override
            public void run() {
                Admin.mainMenu();
            }
        });

       System.out.println("   ___             _        _                 \n" +
               "  / __\\ ___   ___ | | _____| |_ ___  _ __ ___ \n" +
               " /__\\/// _ \\ / _ \\| |/ / __| __/ _ \\| '__/ _ \\\n" +
               "/ \\/  \\ (_) | (_) |   <\\__ \\ || (_) | | |  __/\n" +
               "\\_____/\\___/ \\___/|_|\\_\\___/\\__\\___/|_|  \\___|\n" +
               "                                              ");

        System.out.println("     Yuxin Chen  13307103248  May 19, 2015");

        MenuDisplay menuDisplay = new MenuDisplay();
        menuDisplay.chooseAndRun(menuItems, null, maxSizes, manners, false);
    }

    public static void main(String[] args) {
        try {
            con = new Connector();
            System.err.println("test ok");
        } catch (Exception e) {
            System.out.println("Cannot connect to the database");
            System.err.println(e.getMessage());
            System.exit(-1);
        }

        displayMenu();
    }
}