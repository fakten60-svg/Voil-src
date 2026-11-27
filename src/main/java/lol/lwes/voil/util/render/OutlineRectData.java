package lol.lwes.voil.util.render;

import java.util.Objects;
import org.joml.Vector2f;
import org.joml.Vector4f;

public record OutlineRectData(
    Vector4f bounds,
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
            || object instanceof OutlineRectData outlinerectdata1
                && Objects.equals(this.colorBottomLeft, outlinerectdata1.colorBottomLeft)
                && Objects.equals(this.colorBottomRight, outlinerectdata1.colorBottomRight)
                && Objects.equals(this.colorTopRight, outlinerectdata1.colorTopRight)
                && Objects.equals(this.colorTopLeft, outlinerectdata1.colorTopLeft)
                && Objects.equals(this.size, outlinerectdata1.size)
                && Objects.equals(this.position, outlinerectdata1.position)
                && Float.compare(this.thickness, outlinerectdata1.thickness) == 0
                && Objects.equals(this.bounds, outlinerectdata1.bounds);
    }

    @Override
    public final String toString() {
        return "by["
            + "ci="
            + this.bounds
            + ", cj="
            + this.thickness
            + ", ck="
            + this.position
            + ", cl="
            + this.size
            + ", cm="
            + this.colorTopLeft
            + ", cn="
            + this.colorTopRight
            + ", co="
            + this.colorBottomRight
            + ", cp="
            + this.colorBottomLeft
            + "]";
    }

    @Override
    public final int hashCode() {
        int i = 0 + Objects.hashCode(this.bounds);
        i = i * 31 + Float.hashCode(this.thickness);
        i = i * 31 + Objects.hashCode(this.position);
        i = i * 31 + Objects.hashCode(this.size);
        i = i * 31 + Objects.hashCode(this.colorTopLeft);
        i = i * 31 + Objects.hashCode(this.colorTopRight);
        i = i * 31 + Objects.hashCode(this.colorBottomRight);
        return i * 31 + Objects.hashCode(this.colorBottomLeft);
    }
}
