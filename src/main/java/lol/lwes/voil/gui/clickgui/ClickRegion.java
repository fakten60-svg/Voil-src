package lol.lwes.voil.gui.clickgui;

import java.awt.Rectangle;
import lol.lwes.voil.config.ThemeProperty;

public class ClickRegion {
    public Rectangle bounds;
    public Runnable action;
    public ThemeProperty type;
    public float minValue;
    public float maxValue;
    public float step;

    public ClickRegion(Rectangle rectangle, Runnable runnable) {
        this.bounds = rectangle;
        this.action = runnable;
    }

    public ClickRegion(Rectangle rectangle, ThemeProperty themeproperty, float f, float f1, float f2) {
        this.bounds = rectangle;
        this.type = themeproperty;
        this.minValue = f;
        this.maxValue = f1;
        this.step = f2;
        this.action = ClickRegion::lambda$new$0;
    }

    private static void lambda$new$0() {
    }
}
