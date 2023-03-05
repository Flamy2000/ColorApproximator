import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.System.out;

public class Picture {
    String imgName;
    String imgPath;
    File imgFile;
    BufferedImage bufImg;
    int width;
    int height;

    public Picture(String name) {
        this.imgPath = name;
        imgFile = new File(imgPath);
        imgName = imgFile.getName();

        verify_image();
        try {
            bufImg = ImageIO.read(imgFile);
        } catch (IOException ex) {
            Logger.getLogger(Picture.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Picture(File file) {
        imgPath = file.getPath();
        imgFile = file;
        imgName = file.getName();

        verify_image();
        try {
            bufImg = ImageIO.read(file);
        } catch (IOException ex) {
            Logger.getLogger(Picture.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Picture(int w, int h) {
        this.width = w;
        this.height = h;
        bufImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    }

    private void verify_image() {
        String[] valid_types = {"png", "jpg", "jpeg", "gif", "bmp", "webmp"};
        boolean valid = false;
        for (String type : valid_types){
            if (imgFile.getName().endsWith("." + type) || imgFile.getName().endsWith("." + type.toUpperCase())){
                valid = true;
            }
        }
        if (!valid){
            throw new InvalidImageTypeException("Invalid Image Type: " + imgFile.getName());
        }
    }
    public int width() {
        width = bufImg.getWidth();
        return width;
    }

    public int height() {
        height = bufImg.getHeight();
        return height;
    }

    public Color get(int col, int row) {
        Color color = new Color(bufImg.getRGB(col, row));
        return color;
    }

    public int[] getAll(){
        int[] intColors = bufImg.getRGB(0, 0, width(), height(), null, 0, width());
        return intColors;
    }

    public void set(int col, int row, Color color) {
        bufImg.setRGB(col, row, color.getRGB());
    }

    public void set(int col, int row, int int_color) {
        bufImg.setRGB(col, row, int_color);
    }

    public void show() {
        try {
            File saveAs = new File(System.getProperty("java.io.tmpdir") + new Random().nextInt() + ".png");
            ImageIO.write(bufImg, "png", saveAs);

            Desktop.getDesktop().open(saveAs);
        } catch (IOException ex) {
            Logger.getLogger(Picture.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static class InvalidImageTypeException extends RuntimeException {
        public InvalidImageTypeException(String errorMessage, Throwable err) {
            super(errorMessage, err);
        }

        public InvalidImageTypeException(String errorMessage) {
            super(errorMessage);
        }
    }

}

