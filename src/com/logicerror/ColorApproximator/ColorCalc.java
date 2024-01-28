package com.logicerror.ColorApproximator;

import java.awt.*;

public class ColorCalc {
    // Returns the distance between two RGB colors,
            // sometimes referred to as the "Redmean" algorithm
    public static double getColorDistance(float[] rgb1, float[] rgb2) {
        // Algorithm from:
        // Riemersma, Thiadmer. “Colour Metric.” Compu Phase, www.compuphase.com/cmetric.htm.

        double r_mean = 0.5 * (rgb1[0] + rgb2[0]);
        double r = rgb1[0] - rgb2[0];
        double g = rgb1[1] - rgb2[1];
        double b = rgb1[2] - rgb2[2];
        return Math.sqrt((2 + r_mean) * (r * r) + 4 * g * g + (2 + (1 - r_mean)) * (b * b));
    }

    public static Color getClosestColor(Color[] palette, Color color){
        Color closest_color = null;
        double closest_distance = Double.MAX_VALUE;
        for (Color palette_color : palette){
            double distance = getColorDistance(palette_color.getRGBColorComponents(null), color.getRGBColorComponents(null));
            if (distance < closest_distance){
                closest_color = palette_color;
                closest_distance = distance;
            }
        }

        return closest_color;
    }
}
