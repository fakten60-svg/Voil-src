package lol.lwes.voil.gui.clickgui.theme;

import java.awt.Color;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class ColorBinding {
    public final String label;
    public final Supplier<Color> getter;
    public final Consumer<Color> setter;

    public ColorBinding(String label, Supplier<Color> getter, Consumer<Color> setter) {
        this.label = label;
        this.getter = getter;
        this.setter = setter;
    }

    public Color get() {
        return this.getter.get();
    }

    public void set(Color color) {
        this.setter.accept(color);
    }
}
