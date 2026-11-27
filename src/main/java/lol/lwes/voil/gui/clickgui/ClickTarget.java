package lol.lwes.voil.gui.clickgui;

public final class ClickTarget {
    private final float x;
    private final float y;
    private final float width;
    private final float height;
    private final Runnable action;

    public ClickTarget(float x, float y, float width, float height, Runnable action) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.action = action;
    }

    public boolean contains(double mouseX, double mouseY) {
        return mouseX >= this.x && mouseX <= this.x + this.width && mouseY >= this.y && mouseY <= this.y + this.height;
    }

    public void activate() {
        if (this.action != null) {
            this.action.run();
        }
    }
}
