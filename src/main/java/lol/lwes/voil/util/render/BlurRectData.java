package lol.lwes.voil.util.render;

import java.util.Objects;
import org.joml.Vector4f;

public record BlurRectData(
    Vector4f bounds, Vector4f cornerRadii, Vector4f tint, Vector4f params, Vector4f colorStart, Vector4f colorEnd, Vector4f[] stopsStart, Vector4f[] stopsEnd
) {

    @Override
    public final boolean equals(Object object) {
        return this == object
            || object instanceof BlurRectData blurrectdata1
                && Objects.equals(this.stopsEnd, blurrectdata1.stopsEnd)
                && Objects.equals(this.stopsStart, blurrectdata1.stopsStart)
                && Objects.equals(this.colorEnd, blurrectdata1.colorEnd)
                && Objects.equals(this.colorStart, blurrectdata1.colorStart)
                && Objects.equals(this.params, blurrectdata1.params)
                && Objects.equals(this.tint, blurrectdata1.tint)
                && Objects.equals(this.cornerRadii, blurrectdata1.cornerRadii)
                && Objects.equals(this.bounds, blurrectdata1.bounds);
    }

    @Override
    public final String toString() {
        return "bx["
            + "ca="
            + this.bounds
            + ", cb="
            + this.cornerRadii
            + ", cc="
            + this.tint
            + ", cd="
            + this.params
            + ", ce="
            + this.colorStart
            + ", cf="
            + this.colorEnd
            + ", cg="
            + this.stopsStart
            + ", ch="
            + this.stopsEnd
            + "]";
    }

    @Override
    public final int hashCode() {
        int i = 0 + Objects.hashCode(this.bounds);
        i = i * 31 + Objects.hashCode(this.cornerRadii);
        i = i * 31 + Objects.hashCode(this.tint);
        i = i * 31 + Objects.hashCode(this.params);
        i = i * 31 + Objects.hashCode(this.colorStart);
        i = i * 31 + Objects.hashCode(this.colorEnd);
        i = i * 31 + Objects.hashCode(this.stopsStart);
        return i * 31 + Objects.hashCode(this.stopsEnd);
    }
}
