package com.logicerror.ColorApproximator;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.List;

public class ColorApproximation extends Thread{
    File file;
    Picture img;
    Color[] palette;

    Picture outputImg;

    Interface callingInterface;

    double percentComplete;

    int[] imgColors;
    Integer[] uniqueColors;
    HashMap<Integer, Color> colorRef;

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


    public void setUpColors(){
        // Get all colors in image
        imgColors = img.getAll();
        // Remove duplicate colors
        uniqueColors = removeDuplicates(imgColors);

        // Generate color approximates and put into hash map
        percentComplete = 0;
        colorRef = new HashMap<>();
        for (int color : uniqueColors) {
            colorRef.put(color, ColorCalc.getClosestColor(palette, new Color(color)));
            updatePercentageComplete(1);
        }
    }

    public HashMap<Integer, Color> getColorRef(){
        return colorRef;
    }

    public int[] getImgColors(){
        return imgColors;
    }

    public void run() {
        setUpColors();

        // Set colors of image

        int width = img.width();

        for (int i = 0; i < imgColors.length; i++) {
            int x = i % width;
            int y = i / width;
            outputImg.set(x, y, colorRef.get(imgColors[i]));
            updatePercentageComplete(1);
        }

        if (callingInterface!= null) {
            callingInterface.runCompleted();
        }
    }

    public void updatePercentageComplete(int i){
        percentComplete += (double)i/(uniqueColors.length+imgColors.length);
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


