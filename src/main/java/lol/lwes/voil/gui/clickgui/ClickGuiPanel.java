package lol.lwes.voil.gui.clickgui;

public abstract class ClickGuiPanel extends ClickGuiComponent {
    protected final ClickGuiScreen screen;

    public ClickGuiPanel(ClickGuiScreen clickguiscreen) {
        this.screen = clickguiscreen;
    }

    public void onOpen() {
    }

    public static int opaqueConstant() {
        return 0;
    }

    public ClickGuiScreen getScreen() {
        return this.screen;
    }

    public void onClose() {
    }

    public static int opaqueZero() {
        return 2;
    }
}
