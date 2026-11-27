package lol.lwes.voil.gui.clickgui.page;

import java.awt.Rectangle;
import java.util.Objects;

public record ClickArea(Rectangle bounds, Runnable action) {

    @Override
    public final boolean equals(Object object) {
        return this == object
            || object instanceof ClickArea clickarea1 && Objects.equals(this.action, clickarea1.action) && Objects.equals(this.bounds, clickarea1.bounds);
    }

    @Override
    public final String toString() {
        return "id[" + "aj=" + this.bounds + ", ak=" + this.action + "]";
    }

    @Override
    public final int hashCode() {
        int i = 0 + Objects.hashCode(this.bounds);
        return i * 31 + Objects.hashCode(this.action);
    }
}
