package com.logicerror;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.FileOutputStream;


/**
 * Represents an image with various operations such as reading from file, manipulation, and display.
 */
public class Picture {
    String imgName;
    String imgPath;
    File imgFile;
    private BufferedImage bufImg;

    public Picture(Picture picture) {
        imgName = picture.imgName;
        imgPath = picture.imgPath;
        imgFile = picture.imgFile;

        ColorModel cm = picture.bufImg.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = picture.bufImg.copyData(null);
        bufImg = new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }

    /**
     * Constructs a Picture object from the specified image file path.
     *
     * @param path The path to the image file.
     */
    public Picture(String path) {
        this(new File(path));
    }

    /**
     * Constructs a Picture object from the specified image file.
     *
     * @param file The image file.
     */
    public Picture(File file) {
        imgPath = file.getPath();
        imgFile = file;
        imgName = file.getName();

        verify_image_type();
        try {
            bufImg = ImageIO.read(file);
        } catch (IOException ex) {
            bufImg = null;
            Logger.getLogger(Picture.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Constructs a Picture object from the provided BufferedImage.
     *
     * @param bufImg  The BufferedImage to be used.
     * @param imgName The name to set the image.
     */
    public Picture(BufferedImage bufImg, String imgName) {
        this.bufImg = bufImg;
        this.imgName = imgName;
    }

    /**
     * Constructs a Picture object with the specified width and height.
     *
     * @param w The width of the picture.
     * @param h The height of the picture.
     */
    public Picture(int w, int h) {
        bufImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    }

    /**
     * Verifies if the image file has a valid type.
     */
    private void verify_image_type() {
        String[] valid_types = {"png", "jpg", "jpeg", "gif", "bmp", "webmp"};
        boolean valid = false;
        for (String type : valid_types) {
            if (imgFile.getName().endsWith("." + type) || imgFile.getName().endsWith("." + type.toUpperCase())) {
                valid = true;
            }
        }
        if (!valid) {
            throw new InvalidImageTypeException("Invalid Image Type: " + imgFile.getName());
        }
    }

    /**
     * Returns the width of the picture.
     *
     * @return The width of the picture.
     */
    public int width() {
        return bufImg.getWidth();
    }

    /**
     * Returns the height of the picture.
     *
     * @return The height of the picture.
     */
    public int height() {
        return bufImg.getHeight();
    }

    /**
     * Gets the color of the pixel at the specified position.
     *
     * @param col The column index of the pixel.
     * @param row The row index of the pixel.
     * @return The color of the pixel.
     */
    public Color getPixelColor(int col, int row) {
        Color color = new Color(bufImg.getRGB(col, row));
        return color;
    }

    /**
     * Gets the colors of all pixels in the image.
     *
     * @return An int array containing the colors of all pixels.
     */
    public int[] getAllColors() {
        return bufImg.getRGB(0, 0, width(), height(), null, 0, width());
    }

    /**
     * Sets the color of a pixel at the specified position.
     *
     * @param col   The column index of the pixel.
     * @param row   The row index of the pixel.
     * @param color The color to set.
     */
    public void set(int col, int row, Color color) {
        bufImg.setRGB(col, row, color.getRGB());
    }

    /**
     * Sets the color of the pixel at the specified position.
     *
     * @param col       The column index of the pixel.
     * @param row       The row index of the pixel.
     * @param int_color The integer representation of the color.
     */
    public void set(int col, int row, int int_color) {
        bufImg.setRGB(col, row, int_color);
    }

    /**
     * Displays the image in the default system application for viewing images.
     */
    public void show() {
        try {
            File saveAs = new File(System.getProperty("java.io.tmpdir") + new Random().nextInt() + ".png");
            try (FileOutputStream fos = new FileOutputStream(saveAs)) {
                ImageIO.write(bufImg, "png", fos);
            }
            Desktop.getDesktop().open(saveAs);
        } catch (IOException ex) {
            Logger.getLogger(Picture.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    /**
     * Checks if the BufferedImage object (bufImg) is null and logs a message if it is.
     *
     * @param msg The message to be logged if bufImg is null.
     * @return true if bufImg is null, false otherwise.
     */
    private boolean isImgNull(String msg) {
        if (bufImg == null) {
            Logger.getLogger(Picture.class.getName()).log(Level.WARNING, msg, new RuntimeException());
            return true;
        }
        return false;
    }

    /**
     * Scales the image to the specified dimensions.
     *
     * @param newWidth  The new width of the image.
     * @param newHeight The new height of the image.
     */
    public void scaleValue(int newWidth, int newHeight) {
        if (isImgNull("Unable to scale null image")) {
            return;
        }

        BufferedImage scaledImg = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = scaledImg.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.drawImage(bufImg, 0, 0, newWidth, newHeight, null);
        g2d.dispose();
        bufImg = scaledImg;
    }

    /**
     * Scales the image by the specified percentage.
     *
     * @param widthMult  The multiplier for the width.
     * @param heightMult The multiplier for the height.
     */
    public void scalePercent(float widthMult, float heightMult) {

        if (isImgNull("Unable to scale null image")) {
            return;
        }
        scaleValue(Math.round(width() * widthMult), Math.round(height() * heightMult));
    }

    /**
     * Degrades image by scaling down and then back up.
     * Intended for use before color approximation to round edges.
     * @param widthFactor  The modifier for the width.
     * @param heightFactor  The modifier for the height.
     */
    public void degrade(float widthFactor, float heightFactor){
        scalePercent(1/widthFactor, 1/heightFactor);
        scalePercent(widthFactor, heightFactor);
    }

    /**
     * Degrades image by scaling down and then back up.
     * Intended for use before color approximation to round edges.
     * @param factor  The modifier for smoothing.
     */
    public void degrade(float factor){
        scalePercent(1/factor, 1/factor);
        scalePercent(factor, factor);
    }


    public BufferedImage getBufferedImage() {
        return bufImg;
    }


    /**
     * Represents an exception thrown when the image type is invalid.
     */
    public static class InvalidImageTypeException extends RuntimeException {
        public InvalidImageTypeException(String errorMessage, Throwable err) {
            super(errorMessage, err);
        }

        public InvalidImageTypeException(String errorMessage) {
            super(errorMessage);
        }
    }
}


