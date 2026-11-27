package lol.lwes.voil.gui.clickgui.page;

public enum ColorPickerDragMode {
    NONE,
    BORDER,
    SAT_BRI,
    HUE;

    private static ColorPickerDragMode[] $values() {
        return new ColorPickerDragMode[]{NONE, BORDER, SAT_BRI, HUE};
    }
}
