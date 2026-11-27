package lol.lwes.voil.event;

import lol.lwes.voil.mixin.accessor.KeyBindingAccessor;
import net.minecraft.client.option.KeyBinding;

public abstract class KeyBindingEvent implements Cancellable, Handleable {
    private boolean cancelled = false;
    private boolean handled = false;
    private int timesPressed;

    public KeyBindingEvent(KeyBinding keybinding) {
        this.timesPressed = ((KeyBindingAccessor)keybinding).getTimesPressed();
    }

    public void addTimesPressed(int i) {
        this.setTimesPressed(this.timesPressed + i);
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    public void setTimesPressed(int i) {
        this.timesPressed = i;
        this.setHandled(true);
    }

    @Override
    public boolean isHandled() {
        return this.handled;
    }

    @Override
    public void setHandled(boolean flag) {
        this.handled = flag;
    }

    public void setCancelled(boolean flag) {
        this.cancelled = flag;
    }

    public int getTimesPressed() {
        return this.timesPressed;
    }

    @Override
    public void setCancelled2(boolean flag) {
        this.setCancelled(flag);
    }
}
