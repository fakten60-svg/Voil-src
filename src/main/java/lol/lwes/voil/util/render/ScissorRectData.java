package lol.lwes.voil.util.render;

import dev.prestigeclient.renderlib.impl.render.state.StateApplier;
import java.util.Objects;

public record ScissorRectData(float[] cornerRadii, float x, float y, float width, float height, float radius, StateApplier.ScissorState scissor) {
    @Override
    public final boolean equals(Object object) {
        return this == object
            || object instanceof ScissorRectData scissorrectdata1
                && Objects.equals(this.scissor, scissorrectdata1.scissor)
                && Float.compare(this.radius, scissorrectdata1.radius) == 0
                && Float.compare(this.height, scissorrectdata1.height) == 0
                && Float.compare(this.width, scissorrectdata1.width) == 0
                && Float.compare(this.y, scissorrectdata1.y) == 0
                && Float.compare(this.x, scissorrectdata1.x) == 0
                && Objects.equals(this.cornerRadii, scissorrectdata1.cornerRadii);
    }

    @Override
    public final String toString() {
        return "bA["
            + "cC="
            + this.cornerRadii
            + ", cD="
            + this.x
            + ", cE="
            + this.y
            + ", cF="
            + this.width
            + ", cG="
            + this.height
            + ", cH="
            + this.radius
            + ", cI="
            + this.scissor
            + "]";
    }

    @Override
    public final int hashCode() {
        int i = 0 + Objects.hashCode(this.cornerRadii);
        i = i * 31 + Float.hashCode(this.x);
        i = i * 31 + Float.hashCode(this.y);
        i = i * 31 + Float.hashCode(this.width);
        i = i * 31 + Float.hashCode(this.height);
        i = i * 31 + Float.hashCode(this.radius);
        return i * 31 + Objects.hashCode(this.scissor);
    }
}
