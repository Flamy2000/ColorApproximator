import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Main {
    // Function to remove duplicates from an ArrayList
    // Function to remove duplicate from array
    public static Integer[] removeDuplicates(int[] a) {
        LinkedHashSet<Integer> set = new LinkedHashSet<>();

        // adding elements to LinkedHashSet
        for (int j : a) set.add(j);

        Integer[] LHSArray = new Integer[set.size()];
        LHSArray = set.toArray(LHSArray);
        return LHSArray;
    }



    public static void main(String[] args) throws FileNotFoundException {
        Color[] palette = ColorApproximation.readColorFile(new File(System.getProperty("user.dir") + "\\Colors\\Crayola100SuperTipMarkers.txt"));
        ColorApproximation ca = new ColorApproximation(System.getProperty("user.dir") + "\\SampleImages\\windows.jpg", palette);
//        ca.run();
        ca.showOutput();

//        Picture img = new Picture(System.getProperty("user.dir") + "\\SampleImages\\windows.jpg");
//        out.println(Arrays.toString(readColorFile(new File(System.getProperty("user.dir") + "\\Colors\\MSPaintBasic.txt"))));
//        Color[] palette = new Color[]{
//                new Color(255, 169, 204, 255),
//                new Color(238, 32, 78, 255),
//                new Color(255, 117, 55, 255),
//                new Color(252, 232, 133, 255),
//                new Color(28, 172, 119, 255),
//                new Color(180, 103, 77, 255),
//                new Color(0, 0, 0, 255),
//                new Color(149, 146, 141, 255),
//                new Color(146, 110, 174, 255),
//                new Color(31, 117, 254, 255),
//        };

//        img.show();

//        // Get all colors in image
//        int[] imgColors = img.getAll();
//        // Remove duplicate colors
//        Integer[] uniqueColors = removeDuplicates(imgColors);
//
//        // Generate color approximates and put into hash map
//        HashMap<Integer, Color> colorRef = new HashMap<>();
//        for (int color : uniqueColors) {
//            colorRef.put(color, ColorCalc.getClosestColor(palette, new Color(color)));
//        }
//
//        // Set colors of image
//        int width = img.width();
//        for (int i = 0; i < imgColors.length; i++) {
//            int x = i % width;
//            int y = i / width;
//            img.set(x, y, colorRef.get(imgColors[i]));
//        }
//        // Show image
//        img.show();
//
//        out.println("Done.");
    }


}