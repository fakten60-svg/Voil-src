package lol.lwes.voil.gui.clickgui.component;

public enum ThresholdKind {
    FALL,
    HEALTH;

    public boolean isFall() {
        return this == FALL;
    }
}
