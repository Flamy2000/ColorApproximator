package com.logicerror.ColorApproximator;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;

public class ConsoleRun {
    public static void main(String[] args) throws FileNotFoundException {
        Color[] palette = ColorApproximation.readColorFile(new File(System.getProperty("user.dir") + "\\Colors\\Crayola100SuperTipMarkers.txt"));
        ColorApproximation ca = new ColorApproximation(System.getProperty("user.dir") + "\\SampleImages\\windows.jpg", palette);
        ca.run(); // run without threading
        ca.showOutput();

    }
}