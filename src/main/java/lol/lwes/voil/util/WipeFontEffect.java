package lol.lwes.voil.util;

import dev.prestigeclient.renderlib.api.font.RLFontEffect;
import java.awt.Color;
import lol.lwes.voil.util.render.ColorUtil;
import lol.lwes.voil.util.render.UiEasings;

public class WipeFontEffect implements RLFontEffect {
    private Color startColor;
    private Color endColor;
    private Color midColor;
    private float progress;
    private int length;
    private int offset;
    private float softness;

    public WipeFontEffect(Color color, Color color1) {
        this(color, color1, ColorUtil.scaleBrightness(color, 1.4F));
    }

    public WipeFontEffect(Color color, Color color1, Color color2) {
        this.startColor = color;
        this.endColor = color1;
        this.midColor = color2;
        this.progress = 0.0F;
        this.length = 1;
        this.offset = 0;
        this.softness = 2.5F;
    }

    public WipeFontEffect setOffset(int i) {
        this.offset = i;
        return this;
    }

    public WipeFontEffect setLength(int i) {
        this.length = Math.max(1, i);
        return this;
    }

    public WipeFontEffect setSoftness(float f) {
        this.softness = f;
        return this;
    }

    public WipeFontEffect setColors(Color color, Color color1) {
        this.startColor = color;
        this.endColor = color1;
        this.midColor = ColorUtil.scaleBrightness(color, 1.4F);
        return this;
    }

    public WipeFontEffect setProgress(float f) {
        this.progress = Math.max(0.0F, Math.min(1.0F, f));
        return this;
    }

    @Override
    public int getColorFor(int i, double d0) {
        float f = -this.softness + this.progress * (this.length + this.softness * 2.0F);
        int k = this.offset;
        int j = i;
        float f1 = j + k - f;
        if (f1 < -this.softness) {
            return this.startColor.getRGB();
        } else if (f1 > this.softness) {
            return this.endColor.getRGB();
        } else {
            float f2 = (f1 + this.softness) / (this.softness * 2.0F);
            if (f2 < 0.5F) {
                float f4 = f2 * 2.0F;
                f4 = UiEasings.easeOutCirc(f4);
                return ColorUtil.lerpColor(this.startColor, this.midColor, f4).getRGB();
            } else {
                float f3 = (f2 - 0.5F) * 2.0F;
                f3 = UiEasings.easeInSine(f3);
                return ColorUtil.lerpColor(this.midColor, this.endColor, f3).getRGB();
            }
        }
    }
}
