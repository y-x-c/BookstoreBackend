/**
 * Created by Orthocenter on 5/11/15.
 */

package YuxinBookstore;

import java.io.*;
import java.text.Normalizer;
import java.util.ArrayList;

interface MenuItem {
    public ArrayList<String> getDescs();
    //public void showDesc();
    public void run();
}

public class MenuDisplay {

    public static void show(ArrayList<MenuItem> menuItems, String[] headers,
                            int[] maxSizes, String[] manners, boolean flush) {
        if(flush) {
            System.out.print("\u001b[2J");
            System.out.flush();
        } else {
//            System.out.println();
        }

        int width = 0;
        for(int i = 0; i < maxSizes.length; i++) width += maxSizes[i];
        width += maxSizes.length * 3 + 5;

        System.out.print("  ");
        for(int i = 0; i < width; i++) System.out.print("-");
        System.out.println();

        if(headers != null) {
            System.out.print(" | ");
            System.out.print(Formatter.format("", 3, "c"));
            System.out.print(" | ");

            int j = 0;
            for (; j < headers.length; j++) {
                if(manners == null)
                    System.out.print(Formatter.format(headers[j], maxSizes[j], "c"));
                else
                    System.out.print(Formatter.format(headers[j], maxSizes[j], manners[j]));
                System.out.print(" | ");
            }
            System.out.println();

            System.out.print(" |");
            for(int i = 0; i < width; i++) System.out.print("=");
            System.out.println("|");
        }

        int id = 0;
        for(MenuItem item : menuItems) {
            System.out.print(" | ");
            System.out.print(Formatter.format("" + id++, 3, "c"));
            System.out.print(" | ");

            int i = 0;
            ArrayList<String> descs = item.getDescs();
            for(; i < descs.size(); i++) {
                if(manners == null)
                    System.out.print(Formatter.format(descs.get(i), maxSizes[i], "c"));
                else
                    System.out.print(Formatter.format(descs.get(i), maxSizes[i], manners[i]));
                System.out.print(" | ");
            }
            System.out.println();
        }

        System.out.print("  ");
        for(int i = 0; i < width; i++) System.out.print("-");
        System.out.println();
    }

    public static int getChoice(ArrayList<MenuItem> menuItems, String[] headers,
                                int[] maxSizes, String[] manners, boolean flush) {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String choice;
        int c;

        try {
            while (true) {
                show(menuItems, headers, maxSizes, manners, flush);

                c = Utility.getChoice(menuItems.size() - 1);

                return c;
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    public static void chooseAndRun(ArrayList<MenuItem> menuItems, String[] headers,
                                    int[] maxSizes, String[] manners, boolean flush) {
        int c;
        do {
            c = getChoice(menuItems, headers, maxSizes, manners, flush);
            flush = false;
            if(c == -1) return;
            menuItems.get(c).run();
        } while (true);
    }
}
