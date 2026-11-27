package lol.lwes.voil.util.render;

import java.util.Objects;
import org.joml.Vector4f;

public record RectInstanceData(Vector4f cornerRadii, Vector4f bounds, Vector4f colorStart, Vector4f colorEnd) {

    @Override
    public final boolean equals(Object object) {
        return this == object
            || object instanceof RectInstanceData rectinstancedata1
                && Objects.equals(this.colorEnd, rectinstancedata1.colorEnd)
                && Objects.equals(this.colorStart, rectinstancedata1.colorStart)
                && Objects.equals(this.bounds, rectinstancedata1.bounds)
                && Objects.equals(this.cornerRadii, rectinstancedata1.cornerRadii);
    }

    @Override
    public final String toString() {
        return "bv[" + "bY=" + this.cornerRadii + ", bZ=" + this.bounds + ", b0=" + this.colorStart + ", b1=" + this.colorEnd + "]";
    }

    @Override
    public final int hashCode() {
        int i = 0 + Objects.hashCode(this.cornerRadii);
        i = i * 31 + Objects.hashCode(this.bounds);
        i = i * 31 + Objects.hashCode(this.colorStart);
        return i * 31 + Objects.hashCode(this.colorEnd);
    }
}
