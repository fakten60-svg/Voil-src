package lol.lwes.voil.util.render;

import dev.prestigeclient.renderlib.api.font.RLFontEffect;
import java.awt.Color;

public class GradientFontEffect implements RLFontEffect, IndexedColorSource {
    private final Color startColor;
    private final Color endColor;
    private final float period;
    private final float[] positions;

    public GradientFontEffect(Color color, Color color1, float f, float[] afloat) {
        this.startColor = color;
        this.endColor = color1;
        this.period = Math.max(0.01F, f);
        this.positions = (float[])afloat.clone();
    }

    public int lerpColor(Color color, Color color1, float f) {
        float f1 = Math.max(0.0F, Math.min(1.0F, f));
        float f2 = color.getRed();
        int l1 = color1.getRed();
        int i1 = color.getRed();
        int l = l1;
        int i = (int)(f2 + (l - i1) * f1);
        f2 = color.getGreen();
        l1 = color1.getGreen();
        i1 = color.getGreen();
        l = l1;
        int j = (int)(f2 + (l - i1) * f1);
        f2 = color.getBlue();
        l1 = color1.getBlue();
        i1 = color.getBlue();
        l = l1;
        int k = (int)(f2 + (l - i1) * f1);
        f2 = color.getAlpha();
        l1 = color1.getAlpha();
        i1 = color.getAlpha();
        l = l1;
        int j1 = (int)(f2 + (l - i1) * f1) << 24;
        i1 = i << 16;
        l = j1;
        int k1 = l | i1;
        i1 = j << 8;
        l = k1;
        l |= i1;
        return l | k;
    }

    public int getColor(int i) {
        float f = (float)i / this.positions.length;
        double d0 = System.currentTimeMillis();
        double d1 = this.period * 1000.0;
        float f1 = (float)(Math.sin((d0 / d1 - Math.floor(d0 / d1) + f) * Math.PI * 2.0) * 0.5 + 0.5);
        Color color = this.endColor;
        Color color1 = this.startColor;
        return this.lerpColor(color1, color, f1);
    }

    @Override
    public int getColorFor(int i, double d0) {
        return this.getGlyphColor2(i, 0);
    }

    @Override
    public int getGlyphColor2(int j, int i) {
        return this.getColor(i);
    }
}
