package lol.lwes.voil.util.render;

import dev.prestigeclient.renderlib.api.uniform.RLUniforms;
import java.awt.Color;
import java.util.Objects;
import java.util.function.Consumer;

public record QuadOutlineUniforms(
    float x,
    float y,
    float width,
    float height,
    float p0x,
    float p0y,
    float p1x,
    float p1y,
    float p2x,
    float p2y,
    float p3x,
    float p3y,
    float thickness,
    float smoothness,
    Color color1,
    Color color2,
    float scale
) implements Consumer {
    @Override
    public final boolean equals(Object object) {
        return this == object
            || object instanceof QuadOutlineUniforms quadoutlineuniforms1
                && Float.compare(this.scale, quadoutlineuniforms1.scale) == 0
                && Objects.equals(this.color2, quadoutlineuniforms1.color2)
                && Objects.equals(this.color1, quadoutlineuniforms1.color1)
                && Float.compare(this.smoothness, quadoutlineuniforms1.smoothness) == 0
                && Float.compare(this.thickness, quadoutlineuniforms1.thickness) == 0
                && Float.compare(this.p3y, quadoutlineuniforms1.p3y) == 0
                && Float.compare(this.p3x, quadoutlineuniforms1.p3x) == 0
                && Float.compare(this.p2y, quadoutlineuniforms1.p2y) == 0
                && Float.compare(this.p2x, quadoutlineuniforms1.p2x) == 0
                && Float.compare(this.p1y, quadoutlineuniforms1.p1y) == 0
                && Float.compare(this.p1x, quadoutlineuniforms1.p1x) == 0
                && Float.compare(this.p0y, quadoutlineuniforms1.p0y) == 0
                && Float.compare(this.p0x, quadoutlineuniforms1.p0x) == 0
                && Float.compare(this.height, quadoutlineuniforms1.height) == 0
                && Float.compare(this.width, quadoutlineuniforms1.width) == 0
                && Float.compare(this.y, quadoutlineuniforms1.y) == 0
                && Float.compare(this.x, quadoutlineuniforms1.x) == 0;
    }

    @Override
    public final String toString() {
        return "bu["
            + "bH="
            + this.x
            + ", bI="
            + this.y
            + ", bJ="
            + this.width
            + ", bK="
            + this.height
            + ", bL="
            + this.p0x
            + ", bM="
            + this.p0y
            + ", bN="
            + this.p1x
            + ", bO="
            + this.p1y
            + ", bP="
            + this.p2x
            + ", bQ="
            + this.p2y
            + ", bR="
            + this.p3x
            + ", bS="
            + this.p3y
            + ", bT="
            + this.thickness
            + ", bU="
            + this.smoothness
            + ", bV="
            + this.color1
            + ", bW="
            + this.color2
            + ", bX="
            + this.scale
            + "]";
    }

    @Override
    public final int hashCode() {
        int i = 0 + Float.hashCode(this.x);
        i = i * 31 + Float.hashCode(this.y);
        i = i * 31 + Float.hashCode(this.width);
        i = i * 31 + Float.hashCode(this.height);
        i = i * 31 + Float.hashCode(this.p0x);
        i = i * 31 + Float.hashCode(this.p0y);
        i = i * 31 + Float.hashCode(this.p1x);
        i = i * 31 + Float.hashCode(this.p1y);
        i = i * 31 + Float.hashCode(this.p2x);
        i = i * 31 + Float.hashCode(this.p2y);
        i = i * 31 + Float.hashCode(this.p3x);
        i = i * 31 + Float.hashCode(this.p3y);
        i = i * 31 + Float.hashCode(this.thickness);
        i = i * 31 + Float.hashCode(this.smoothness);
        i = i * 31 + Objects.hashCode(this.color1);
        i = i * 31 + Objects.hashCode(this.color2);
        return i * 31 + Float.hashCode(this.scale);
    }

    public void applyUniforms(RLUniforms rluniforms) {
        float f = GuiScale.getScaledHeight();
        float f1 = this.x * this.scale;
        float f2 = (f - this.y - this.height) * this.scale;
        rluniforms.set2f("uLocation", f1, f2);
        rluniforms.set2f("uSize", this.width * this.scale, this.height * this.scale);
        rluniforms.set2f("p0", (this.p0x - this.x) * this.scale, (this.y + this.height - this.p0y) * this.scale);
        rluniforms.set2f("p1", (this.p1x - this.x) * this.scale, (this.y + this.height - this.p1y) * this.scale);
        rluniforms.set2f("p2", (this.p2x - this.x) * this.scale, (this.y + this.height - this.p2y) * this.scale);
        rluniforms.set2f("p3", (this.p3x - this.x) * this.scale, (this.y + this.height - this.p3y) * this.scale);
        rluniforms.set1f("thickness", this.thickness * this.scale);
        rluniforms.set1f("smoothness", this.smoothness * this.scale);
        rluniforms.set4f(
            "color1", this.color1.getRed() / 255.0F, this.color1.getGreen() / 255.0F, this.color1.getBlue() / 255.0F, this.color1.getAlpha() / 255.0F
        );
        rluniforms.set4f(
            "color2", this.color2.getRed() / 255.0F, this.color2.getGreen() / 255.0F, this.color2.getBlue() / 255.0F, this.color2.getAlpha() / 255.0F
        );
    }

    @Override
    public void accept(Object object) {
        this.applyUniforms((RLUniforms)object);
    }
}
