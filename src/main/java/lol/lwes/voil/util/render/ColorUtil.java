package lol.lwes.voil.util.render;

import java.awt.Color;

public class ColorUtil {
    private static float rainbowHue = 0.0F;

    public static Color scaleBrightness(Color color, float f) {
        if (color == null) {
            return Color.WHITE;
        }

        int l = (int)(color.getRed() * f);
        int i = clampChannel(l);
        int i1 = (int)(color.getGreen() * f);
        int j = clampChannel(i1);
        int j1 = (int)(color.getBlue() * f);
        int k = clampChannel(j1);
        return new Color(i, j, k);
    }

    public static double lerp(double d0, double d1, double d2) {
        return d0 + (d1 - d0) * d2;
    }

    public static Color withAlpha(Color color, float f) {
        int i = Math.max(0, Math.min(255, (int)(f * 255.0F)));
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), i);
    }

    public static int toArgb(Color color) {
        int i = color.getAlpha();
        int k = (i & 0xFF) << 24;
        int j = (color.getBlue() & 0xFF) << 16;
        i = k;
        k = i | j;
        i = color.getGreen();
        k |= (i & 0xFF) << 8;
        i = color.getRed();
        return k | i & 0xFF;
    }

    public static float clamp01(float f) {
        return Math.max(0.0F, Math.min(1.0F, f));
    }

    public static int lerpInt(int i, int j, double d0) {
        return (int)lerp(i, j, (float)d0);
    }

    public static Color timeGradient(Color color, Color color1, double d0, double d1) {
        int i = (int)((System.currentTimeMillis() / d0 + d1) % 360.0);
        int j = i >= 180 ? 360 - i : i;
        i = j + j;
        return blendColors(color, color1, i / 360.0F);
    }

    public static Color rainbow(int i) {
        rainbowHue += i * 1000.0F;
        if (rainbowHue > 1.0F) {
            rainbowHue--;
        }

        return Color.getHSBColor(rainbowHue, 1.0F, 1.0F);
    }

    private static int clampChannel(int i) {
        return Math.min(255, Math.max(0, i));
    }

    public static Color withAlpha(Color color, int i) {
        int j = Math.max(0, Math.min(255, i));
        if (color == null) {
            return new Color(255, 255, 255, j);
        }

        return new Color(color.getRed(), color.getGreen(), color.getBlue(), j);
    }

    public static Color lerpColor(Color color, Color color1, float f) {
        float f5 = color.getRed();
        int k = color1.getRed();
        int j = color.getRed();
        int i = k;
        float f1 = f5 + f * (i - j);
        f5 = color.getGreen();
        k = color1.getGreen();
        j = color.getGreen();
        i = k;
        float f2 = f5 + f * (i - j);
        f5 = color.getBlue();
        k = color1.getBlue();
        j = color.getBlue();
        i = k;
        float f3 = f5 + f * (i - j);
        f5 = color.getAlpha();
        k = color1.getAlpha();
        j = color.getAlpha();
        i = k;
        float f4 = f5 + f * (i - j);
        return new Color((int)f1, (int)f2, (int)f3, (int)f4);
    }

    private static int clamp(int i, int j, int k) {
        return Math.max(j, Math.min(k, i));
    }

    public static Color blendColors(Color color, Color color1, float f) {
        float f1 = f;
        Color color2 = color1;
        float f2 = Math.min(1.0F, Math.max(0.0F, f1));
        return new Color(
            lerpInt(color.getRed(), color2.getRed(), f2),
            lerpInt(color.getGreen(), color2.getGreen(), f2),
            lerpInt(color.getBlue(), color2.getBlue(), f2),
            lerpInt(color.getAlpha(), color2.getAlpha(), f2)
        );
    }

    public static Color lerpColorClamped(Color color, Color color1, float f) {
        int i = clamp((int)(color.getRed() + f * (color1.getRed() - color.getRed())), 0, 0);
        float f1 = color.getGreen();
        int j1 = color1.getGreen();
        int i1 = color.getGreen();
        int l = j1;
        int j = clamp((int)(f1 + f * (l - i1)), 0, 255);
        int k = clamp((int)(color.getBlue() + f * (color1.getBlue() - color.getBlue())), 0, 255);
        return new Color(i, j, k);
    }
}
