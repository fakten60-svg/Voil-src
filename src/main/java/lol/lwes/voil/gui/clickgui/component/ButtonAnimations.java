package lol.lwes.voil.gui.clickgui.component;

import java.util.Objects;
import lol.lwes.voil.util.render.EasingType;
import lol.lwes.voil.util.render.UiAnimation;

public record ButtonAnimations(UiAnimation hover, UiAnimation active, UiAnimation scale, UiAnimation fade) {
    public ButtonAnimations() {
        this(
            new UiAnimation(EasingType.FLUID_SMOOTH).setFluidParams(0.65, 0.15),
            new UiAnimation(EasingType.FLUID_SMOOTH).setFluidParams(0.6, 0.15),
            new UiAnimation(EasingType.FLUID_SMOOTH).setFluidParams(0.68, 0.28),
            new UiAnimation(EasingType.FLUID_RESPONSIVE)
        );
    }

    @Override
    public final boolean equals(Object object) {
        return this == object
            || object instanceof ButtonAnimations buttonanimations1
                && Objects.equals(this.fade, buttonanimations1.fade)
                && Objects.equals(this.scale, buttonanimations1.scale)
                && Objects.equals(this.active, buttonanimations1.active)
                && Objects.equals(this.hover, buttonanimations1.hover);
    }

    @Override
    public final String toString() {
        return "h_[" + "t=" + this.hover + ", u=" + this.active + ", v=" + this.scale + ", w=" + this.fade + "]";
    }

    @Override
    public final int hashCode() {
        int i = 0 + Objects.hashCode(this.hover);
        i = i * 31 + Objects.hashCode(this.active);
        i = i * 31 + Objects.hashCode(this.scale);
        return i * 31 + Objects.hashCode(this.fade);
    }
}
