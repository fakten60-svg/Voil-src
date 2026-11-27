package lol.lwes.voil.util.render;

import java.awt.Color;

public class AnimatedColor {
    private final UiAnimation red = new UiAnimation(EasingType.FLUID_SMOOTH).setFluidParams(0.72, 0.15);
    private final UiAnimation green = new UiAnimation(EasingType.FLUID_SMOOTH).setFluidParams(0.72, 0.15);
    private final UiAnimation blue = new UiAnimation(EasingType.FLUID_SMOOTH).setFluidParams(0.72, 0.15);
    private final UiAnimation alpha = new UiAnimation(EasingType.FLUID_SMOOTH).setFluidParams(0.72, 0.15);
    private boolean initialized;
    private static final int MAX_CHANNEL_VALUE = 255;

    public void snapTo(Color color) {
        if (color != null) {
            this.red.snapTo(color.getRed());
            this.green.snapTo(color.getGreen());
            this.blue.snapTo(color.getBlue());
            this.alpha.snapTo(color.getAlpha());
            this.initialized = true;
        }
    }

    private static int clampChannel(int i) {
        return Math.max(0, Math.min(MAX_CHANNEL_VALUE, i));
    }

    public Color update(Color color) {
        if (color == null) {
            return null;
        } else if (!this.initialized) {
            this.red.snapTo(color.getRed());
            this.green.snapTo(color.getGreen());
            this.blue.snapTo(color.getBlue());
            this.alpha.snapTo(color.getAlpha());
            this.initialized = true;
            return color;
        } else {
            UiAnimation uianimation = this.red;
            double d0 = color.getRed();
            uianimation.setTarget(d0);
            uianimation = this.green;
            double d1 = color.getGreen();
            uianimation.setTarget(d1);
            uianimation = this.blue;
            double d2 = color.getBlue();
            uianimation.setTarget(d2);
            uianimation = this.alpha;
            double d3 = color.getAlpha();
            uianimation.setTarget(d3);
            this.red.update();
            this.green.update();
            this.blue.update();
            this.alpha.update();
            int i = this.red.getRounded();
            int i1 = clampChannel(i);
            int j = this.green.getRounded();
            int j1 = clampChannel(j);
            int k = this.blue.getRounded();
            int k1 = clampChannel(k);
            int l = this.alpha.getRounded();
            return new Color(i1, j1, k1, clampChannel(l));
        }
    }
}
