package lol.lwes.voil.setting;

public final class CurvePoint implements Comparable<CurvePoint> {
    public final float x;
    public final float y;

    public CurvePoint(float x, float y) {
        this.x = Math.max(0.0F, Math.min(1.0F, x));
        this.y = y;
    }

    public CurvePoint withY(float y) {
        return new CurvePoint(this.x, y);
    }

    @Override
    public int compareTo(CurvePoint other) {
        return Float.compare(this.x, other.x);
    }

    @Override
    public String toString() {
        return "(" + this.x + ", " + this.y + ")";
    }
}
