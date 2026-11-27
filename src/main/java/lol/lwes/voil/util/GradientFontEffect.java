package lol.lwes.voil.util;

import dev.prestigeclient.renderlib.api.font.RLFontEffect;
import lol.lwes.voil.setting.GradientSetting;
import lol.lwes.voil.util.render.GradientUtil;
import lol.lwes.voil.util.render.IndexedColorSource;

public class GradientFontEffect implements RLFontEffect, IndexedColorSource {
    private static final float[] EMPTY_FLOATS;
    private GradientSetting gradient;
    private float x;
    private float y;
    private float width;
    private float height;
    private float phase;
    private float[] glyphX;
    private float[] glyphY;
    private float[] glyphAlpha;
    private float[][] glyphOffsets;

    public GradientFontEffect() {
        this.glyphX = EMPTY_FLOATS;
        this.glyphY = EMPTY_FLOATS;
        this.glyphAlpha = EMPTY_FLOATS;
        this.glyphOffsets = new float[0][];
    }

    static {
        float[] afloat = new float[0];
        EMPTY_FLOATS = afloat;
    }

    public void begin(GradientSetting gradientsetting, float f, float f1, float f2, float f3, float f4, int i) {
        this.gradient = gradientsetting;
        this.x = f;
        this.y = f1;
        this.width = f2;
        this.height = f3;
        this.phase = f4;
        if (this.glyphX.length < i) {
            int k = this.glyphX.length;
            int j = Math.max(i, k + k);
            this.glyphX = new float[j];
            this.glyphY = new float[j];
            this.glyphAlpha = new float[j];
            this.glyphOffsets = new float[j][];
        }
    }

    public void setGlyph(int i, float f, float f1, float[] afloat, float f2) {
        this.glyphX[i] = f;
        this.glyphY[i] = f1;
        this.glyphOffsets[i] = afloat;
        this.glyphAlpha[i] = Math.max(0.0F, Math.min(1.0F, f2));
    }

    public int getGlyphColor(int i, int j) {
        if (this.gradient != null && j >= 0 && j < this.glyphX.length) {
            float[] afloat = this.glyphOffsets[j];
            float f = 0.0F;
            if (afloat != null && i >= 0 && i < afloat.length) {
                f = afloat[i];
            }

            float f1 = this.x;
            int k = GradientUtil.getGradientColor(this.gradient, f1, this.y, this.width, this.height, this.glyphX[j] + f, this.glyphY[j], this.phase);
            int l = k >>> 24;
            int j1 = Math.round((l & 0xFF) * this.glyphAlpha[j]) << 24;
            int i1 = k & 16777215;
            l = j1;
            return l | i1;
        } else {
            return -1;
        }
    }

    @Override
    public int getColorFor(int i, double d0) {
        return this.getGlyphColor2(i, 0);
    }

    @Override
    public int getGlyphColor2(int i, int j) {
        return this.getGlyphColor(i, j);
    }
}
