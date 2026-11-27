package lol.lwes.voil.event;

public class MousePressEvent extends Event {
    private final int button;
    private final int action;
    private final int modifiers;
    private final double mouseX;
    private final double mouseY;

    public MousePressEvent(int i, int j, int k, double d0, double d1) {
        this.button = i;
        this.action = j;
        this.modifiers = k;
        this.mouseX = d0;
        this.mouseY = d1;
    }

    public int getButton() {
        return this.button;
    }

    public double getX() {
        return this.mouseY;
    }

    public double getY() {
        return this.mouseX;
    }

    public int getAction() {
        return this.action;
    }

    public boolean isRelease() {
        return this.action == 0;
    }

    public int getModifiers() {
        return this.modifiers;
    }

    public boolean isPress() {
        return this.action == 1;
    }
}
