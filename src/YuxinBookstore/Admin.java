package YuxinBookstore;

import java.awt.*;
import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by Orthocenter on 5/14/15.
 */
public class Admin {

    public static ArrayList<String> mainMenuDescs() {
        ArrayList<String> descs = new ArrayList<String>();
        descs.add("For administrator");
        return descs;
    }

    public static void mainMenu() {
        ArrayList<MenuItem> menuItems = new ArrayList<MenuItem>();

        menuItems.add(new MenuItem(){
            public ArrayList<String> getDescs() {
                return Book.addBookDescs();
            }
            public void run() {
                Book.addBook();
            }
        });

        menuItems.add(new MenuItem() {
            @Override
            public ArrayList<String> getDescs() {
                return Author.addDescs();
            }

            @Override
            public void run() {
                Author.add();
            }
        });

        menuItems.add(new MenuItem() {
            @Override
            public ArrayList<String> getDescs() {
                return Publisher.addDescs();
            }

            @Override
            public void run() {
                Publisher.add();
            }
        });

        menuItems.add(new MenuItem() {
            public ArrayList<String> getDescs() {
                return Book.replenishDescs();
            }
            public void run() {
                Book.replenish();
            }
        });
        menuItems.add(new MenuItem() {
            @Override
            public ArrayList<String> getDescs() {
                return Author.showDegreesOfSeperationDescs();
            }

            @Override
            public void run() {
                Author.showDegreesOfSeperation();
            }
        });

        menuItems.add(new MenuItem() {
            @Override
            public ArrayList<String> getDescs() {
                return Book.showPopularBooksDescs();
            }

            @Override
            public void run() {
                Book.showPopularBooks();
            }
        });

        menuItems.add(new MenuItem() {
            @Override
            public ArrayList<String> getDescs() {
                return Publisher.showPopularPublishersDescs();
            }

            @Override
            public void run() {
                Publisher.showPopularPublishers();
            }
        });
        menuItems.add(new MenuItem() {
            @Override
            public ArrayList<String> getDescs() {
                return Author.showPopularAuthorsDescs();
            }

            @Override
            public void run() {
                Author.showPopularAuthors();
            }
        });
        menuItems.add(new MenuItem() {
            @Override
            public ArrayList<String> getDescs() {
                return Customer.trustedUsersDescs();
            }

            @Override
            public void run() {
                Customer.trustedUsers();
            }
        });
        menuItems.add(new MenuItem() {
            @Override
            public ArrayList<String> getDescs() {
                return Customer.usefulUsersDescs();
            }

            @Override
            public void run() {
                Customer.usefulUsers();
            }
        });

        int[] maxSizes = {60};
        MenuDisplay.chooseAndRun(menuItems, null, maxSizes, null, true);
    }

}
