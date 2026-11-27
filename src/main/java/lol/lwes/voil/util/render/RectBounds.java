package lol.lwes.voil.util.render;

public class RectBounds {
    public float posX;
    public float posY;
    public float width;
    public float height;

    public RectBounds(float f, float f1, float f2, float f3) {
        this.posX = Math.round(f);
        this.posY = Math.round(f1);
        this.width = Math.round(f2);
        this.height = Math.round(f3);
    }

    public boolean contains(double d0, double d1) {
        return d0 >= this.posX && d0 <= this.posX + this.width && d1 >= this.posY && d1 <= this.posY + this.height;
    }
}
