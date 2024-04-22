package com.logicerror;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.List;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ColorApproximation extends Thread{
    File file;
    Picture img;
    Color[] palette;

    Picture outputImg;

    Interface callingInterface;

    double percentComplete;

    int[] imgColors;
    Integer[] uniqueColors;
    ConcurrentHashMap<Integer, Color> colorRef;

    public ColorApproximation() {
        this.file = null;
        this.palette = null;
//        createImage();
    }

    public ColorApproximation(File file, Color[] palette) {
        this.file = file;
        this.palette = palette;
        createImage();
    }

    public ColorApproximation(BufferedImage buffImg, Color[] palette) {
        this.img = new Picture(buffImg, "img");
        outputImg = new Picture(buffImg, "img");
    }

//    public ColorApproximation(File file, File palette_file) {
//        this.file = file;
//        this.palette = readColorFile(palette_file);
//        createImage();
//    }

    public void setCallingInterface(Interface callingInterface) {
        this.callingInterface = callingInterface;
    }

    public ColorApproximation(String path, Color[] palette) {
        this.file = new File(path);
        this.palette = palette;
        createImage();
    }

    public ColorApproximation(ColorApproximation self) {
        file = self.file;
        img = self.img;
        palette = self.palette;
        outputImg = self.outputImg;
        callingInterface = self.callingInterface;
        percentComplete = self.percentComplete;
    }


    public ConcurrentHashMap<Integer, Color> getColorRef(){
        return colorRef;
    }

    public int[] getImgColors(){
        return imgColors;
    }

    private static final int BATCH_SIZE = 5000; // Adjust batch size as needed
    private static final int THREAD_COUNT = 4;

    public void run() {
        // Get all colors in image
        imgColors = img.getAllColors();
        // Remove duplicate colors
        uniqueColors = removeDuplicates(imgColors);

        // Generate color approximates and put into hash map
        colorRef = new ConcurrentHashMap<>(); // ConcurrentHashMap for thread safety

        int totalUniqueColors = uniqueColors.length;
        int totalPixelBatches = (int) Math.ceil((double) imgColors.length / BATCH_SIZE);
        int totalTasks = totalUniqueColors + totalPixelBatches;
        final int[] tasksCompleted = {0}; // Initialize the completed tasks counter
        updatePercentageComplete(tasksCompleted[0], totalTasks);

        try (ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT)) { // Adjust thread pool size as needed

            // Calculate closest matches for unique colors in parallel
            for (int i = 0; i < totalUniqueColors; i += BATCH_SIZE) {
                final int startIndex = i;
                final int endIndex = Math.min(i + BATCH_SIZE, totalUniqueColors);
                executor.execute(() -> {
                    batchProcessColors(startIndex, endIndex);
                    synchronized (this) {
                        tasksCompleted[0]++; // Increment completed tasks counter
                        updatePercentageComplete(tasksCompleted[0], totalTasks); // Update progress
                    }
                });
            }

            executor.shutdown();
            while (!executor.isTerminated()) {
                // Wait for all tasks to finish
            }
        }
        try (ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT)) {

            // Set colors for the entire image
            for (int i = 0; i < imgColors.length; i += BATCH_SIZE) {
                final int startIndex = i;
                final int endIndex = Math.min(i + BATCH_SIZE, imgColors.length);
                executor.execute(() -> {
                    batchProcessImageSet(startIndex, endIndex, img.width());
                    synchronized (this) {
                        tasksCompleted[0]++; // Increment completed tasks counter
                        updatePercentageComplete(tasksCompleted[0], totalTasks); // Update progress
                    }
                });
            }

            executor.shutdown();
            while (!executor.isTerminated()) {
                // Wait for all tasks to finish
            }
        }
        // Ensure the progress reaches 100% when all tasks are completed
        updatePercentageComplete(totalTasks, totalTasks);

        if (callingInterface != null) {
            callingInterface.runCompleted();
        }
    }


    public void batchProcessColors(int startIndex, int endIndex) {
        for (int j = startIndex; j < endIndex; j++) {
            int color = uniqueColors[j];
            colorRef.put(color, ColorDistance.getClosestColor(palette, new Color(color)));
        }
    }

    public void batchProcessImageSet(int startIndex, int endIndex, int width) {
        for (int i = startIndex; i < endIndex; i++) {
            int x = i % width;
            int y = i / width;
            outputImg.set(x, y, colorRef.get(imgColors[i]));
        }
    }

    public void updatePercentageComplete(int tasksCompleted, int totalTasks) {
        percentComplete = (double) tasksCompleted / totalTasks * 100.0;
    }


    public double getPercentComplete(){
        return percentComplete;
    }

    public void resetPercentComplete(){
        percentComplete = 0;
    }


    public void setImage(File file) {
        this.file = file;
        createImage();
    }

    public void setPalette(Color[] palette) {
        this.palette = palette;
    }

    public void setPalette(File file) {
        try {
            this.palette = readColorFile(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void createImage() {
        img = new Picture(file);
        outputImg = new Picture(file);
    }

    public File getFile() {
        return file;
    }

    public Picture getOriginalImg() {
        return img;
    }

    public Picture getOutputImg() {
        return outputImg;
    }

    public void showOriginal(){
        img.show();
    }

    public void showOutput(){
        outputImg.show();
    }

    public static Integer[] removeDuplicates(int[] a) {
        LinkedHashSet<Integer> set = new LinkedHashSet<>();

        // adding elements to LinkedHashSet
        for (int j : a) set.add(j);

        Integer[] LHSArray = new Integer[set.size()];
        LHSArray = set.toArray(LHSArray);
        return LHSArray;
    }

    public static Color[] readColorFile(File file) throws FileNotFoundException {
        Scanner file_reader = new Scanner(file);
        List<Color> list = new ArrayList<>();
        while (file_reader.hasNextLine()) {
            String[] data = file_reader.nextLine().split(", ");
//            System.out.println(data);
            int[] color = new int[3];
            for (int i = 0; i < data.length; i++) {
                color[i] = Integer.parseInt(data[i]);
            }
            list.add(new Color(color[0], color[1], color[2]));
        }
        file_reader.close();
        Color[] colors = new Color[list.size()];
        return list.toArray(colors);
    }

    public static Color[] combineColorPalettes(Color[] palette1, Color[] palette2) {
        Color[] palette = new Color[palette1.length + palette2.length];
        System.arraycopy(palette1, 0, palette, 0, palette1.length);
        System.arraycopy(palette2, 0, palette, palette1.length, palette2.length);
        return palette;
    }
}


