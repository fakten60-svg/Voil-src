package lol.lwes.voil.setting;

import java.awt.Color;

public final class GradientStop {
    public int position;
    public Color color;

    public GradientStop(int i, Color color) {
        this.position = i;
        this.color = color;
    }

    public Color getColor() {
        return this.color;
    }

    public int getPosition() {
        return this.position;
    }
}
