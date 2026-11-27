package lol.lwes.voil.util.render;

import java.awt.Color;
import lol.lwes.voil.setting.GradientSetting;

public class GradientUtil {
    private GradientUtil() {
    }

    private static float triangleWave(float f, float f1) {
        float f2 = f * 0.5F + f1;
        f2 -= (float)Math.floor(f2);
        return 1.0F - Math.abs(2.0F * f2 - 1.0F);
    }

    public static Color getColorAt(GradientSetting gradientsetting, float f, float f1, float f2, float f3, float f4, float f5, float f6) {
        return new Color(getGradientColor(gradientsetting, f, f1, f2, f3, f4, f5, f6), true);
    }

    public static Color[] getCornerColors(GradientSetting gradientsetting, float f, float f1, float f2, float f3, float f4) {
        return new Color[]{
            getColorAt(gradientsetting, f, f1, f2, f3, f, f1 + f3, f4),
            getColorAt(gradientsetting, f, f1, f2, f3, f + f2, f1 + f3, f4),
            getColorAt(gradientsetting, f, f1, f2, f3, f, f1, f4),
            getColorAt(gradientsetting, f, f1, f2, f3, f + f2, f1, f4)
        };
    }

    private static float clamp01(float f) {
        return Math.max(0.0F, Math.min(1.0F, f));
    }

    private static int sampleStops(float[] afloat, Color[] acolor, float f) {
        if (afloat != null && acolor != null) {
            int i = Math.min(afloat.length, acolor.length);
            if (i == 0) {
                return -1;
            }

            if (i != 1 && !(f <= afloat[0])) {
                if (f >= afloat[i - 1]) {
                    return acolor[i - 1].getRGB();
                }

                for (int j = 1; j < i; j++) {
                    if (f <= afloat[j]) {
                        float f1 = afloat[j] - afloat[j - 1];
                        float f2 = f1 <= 0.0F ? 1.0F : (f - afloat[j - 1]) / f1;
                        Color color = acolor[j - 1];
                        Color color1 = acolor[j];
                        Color color2 = color;
                        return lerpArgb(color2, color1, f2);
                    }
                }

                return acolor[i - 1].getRGB();
            } else {
                return acolor[0].getRGB();
            }
        } else {
            return -1;
        }
    }

    private static float getGradientPosition(GradientSetting gradientsetting, float f, float f1) {
        if (gradientsetting.getMode() == GradientType.RADIAL) {
            float f5 = (float)Math.sqrt(f * f + f1 * f1);
            return clamp01(f5 / 0.70710677F);
        } else {
            double d0 = Math.toRadians(gradientsetting.getAngle());
            float f2 = (float)Math.cos(d0);
            float f3 = (float)Math.sin(d0);
            float f4 = Math.abs(f2) + Math.abs(f3);
            return clamp01(0.5F + (f * f2 + f1 * f3) / f4);
        }
    }

    public static int getGradientColor(GradientSetting gradientsetting, float f, float f1, float f2, float f3, float f4, float f5, float f6) {
        float f7 = f2 > 0.0F ? (f4 - f) / f2 : 0.0F;
        float f8 = f3 > 0.0F ? (f5 - f1) / f3 : 0.0F;
        float f9 = getGradientPosition(gradientsetting, f7 - 0.5F, f8 - 0.5F);
        Color[] acolor = gradientsetting.getColors();
        return sampleStops(gradientsetting.getPositions(), acolor, triangleWave(f9, f6));
    }

    private static int lerpArgb(Color color, Color color1, float f) {
        int i = Math.round(color.getRed() + (color1.getRed() - color.getRed()) * f);
        float f1 = color.getGreen();
        int l1 = color1.getGreen();
        int i1 = color.getGreen();
        int l = l1;
        int j = Math.round(f1 + (l - i1) * f);
        f1 = color.getBlue();
        l1 = color1.getBlue();
        i1 = color.getBlue();
        l = l1;
        int k = Math.round(f1 + (l - i1) * f);
        f1 = color.getAlpha();
        l1 = color1.getAlpha();
        i1 = color.getAlpha();
        l = l1;
        int j1 = Math.round(f1 + (l - i1) * f) << 24;
        i1 = i << 16;
        l = j1;
        int k1 = l | i1;
        i1 = j << 8;
        l = k1;
        l |= i1;
        return l | k;
    }
}
