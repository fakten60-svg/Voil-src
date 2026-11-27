package lol.lwes.voil.event;

public class PlayerMoveEvent extends Event {
    public double x;
    public double y;
    public double z;

    public PlayerMoveEvent(double d0, double d1, double d2) {
        this.x = d0;
        this.y = d1;
        this.z = d2;
    }

    public void setZ(double d0) {
        this.z = d0;
    }

    public double getZ() {
        return this.z;
    }

    public double getY() {
        return this.y;
    }

    public void setX(double d0) {
        this.x = d0;
    }

    public void setY(double d0) {
        this.y = d0;
    }

    public double getX() {
        return this.x;
    }
}
