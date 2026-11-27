package lol.lwes.voil.gui;

public record MenuButton(String label, Runnable action) {
    public void press() {
        if (this.action != null) {
            this.action.run();
        }
    }
}
