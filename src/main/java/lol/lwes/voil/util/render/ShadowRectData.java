package lol.lwes.voil.util.render;

import java.util.Objects;
import org.joml.Vector2f;
import org.joml.Vector4f;

public record ShadowRectData(
    float radius,
    float softness,
    float thickness,
    Vector2f position,
    Vector2f size,
    Vector4f colorTopLeft,
    Vector4f colorTopRight,
    Vector4f colorBottomRight,
    Vector4f colorBottomLeft
) {

    @Override
    public final boolean equals(Object object) {
        return this == object
            || object instanceof ShadowRectData shadowrectdata1
                && Objects.equals(this.colorBottomLeft, shadowrectdata1.colorBottomLeft)
                && Objects.equals(this.colorBottomRight, shadowrectdata1.colorBottomRight)
                && Objects.equals(this.colorTopRight, shadowrectdata1.colorTopRight)
                && Objects.equals(this.colorTopLeft, shadowrectdata1.colorTopLeft)
                && Objects.equals(this.size, shadowrectdata1.size)
                && Objects.equals(this.position, shadowrectdata1.position)
                && Float.compare(this.thickness, shadowrectdata1.thickness) == 0
                && Float.compare(this.softness, shadowrectdata1.softness) == 0
                && Float.compare(this.radius, shadowrectdata1.radius) == 0;
    }

    @Override
    public final String toString() {
        return "bw["
            + "b2="
            + this.radius
            + ", b3="
            + this.softness
            + ", b4="
            + this.thickness
            + ", b5="
            + this.position
            + ", b6="
            + this.size
            + ", b7="
            + this.colorTopLeft
            + ", b8="
            + this.colorTopRight
            + ", b9="
            + this.colorBottomRight
            + ", b_="
            + this.colorBottomLeft
            + "]";
    }

    @Override
    public final int hashCode() {
        int i = 0 + Float.hashCode(this.radius);
        i = i * 31 + Float.hashCode(this.softness);
        i = i * 31 + Float.hashCode(this.thickness);
        i = i * 31 + Objects.hashCode(this.position);
        i = i * 31 + Objects.hashCode(this.size);
        i = i * 31 + Objects.hashCode(this.colorTopLeft);
        i = i * 31 + Objects.hashCode(this.colorTopRight);
        i = i * 31 + Objects.hashCode(this.colorBottomRight);
        return i * 31 + Objects.hashCode(this.colorBottomLeft);
    }
}
