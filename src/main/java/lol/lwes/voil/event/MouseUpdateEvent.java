package lol.lwes.voil.event;

public class MouseUpdateEvent extends Event {
    private final double x;
    private final double y;

    public MouseUpdateEvent(double d0, double d1) {
        this.x = d0;
        this.y = d1;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }
}
