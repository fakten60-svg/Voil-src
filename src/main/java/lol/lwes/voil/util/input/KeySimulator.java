package lol.lwes.voil.util.input;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.event.EventHandler;
import lol.lwes.voil.event.TickEvent;
import lol.lwes.voil.mixin.accessor.KeyboardAccessor;
import net.minecraft.client.input.KeyInput;
import org.lwjgl.glfw.GLFW;

public class KeySimulator {
    private final HashMap heldKeys = new HashMap();
    private static final int TAP_TICKS = 10;

    public void releaseKey(int i) {
        if (this.isPressed(i)) {
            KeyInput keyinput = new KeyInput(i, GLFW.glfwGetKeyScancode(i), 0);
            this.getKeyboardAccessor().invokeOnKey(VoilClient.mc.getWindow().getHandle(), 0, keyinput);
            this.heldKeys.remove(i);
        }
    }

    public KeyboardAccessor getKeyboardAccessor() {
        return (KeyboardAccessor)VoilClient.mc.keyboard;
    }

    public void pressKey(int i, int j) {
        if (!this.isPressed(i)) {
            this.heldKeys.put(i, j);
            KeyInput keyinput = new KeyInput(i, GLFW.glfwGetKeyScancode(i), 0);
            this.getKeyboardAccessor().invokeOnKey(VoilClient.mc.getWindow().getHandle(), 1, keyinput);
        }
    }

    private void tickKeys() {
        Iterator iterator = this.heldKeys.entrySet().iterator();

        while (iterator.hasNext()) {
            Entry entry = (Entry)iterator.next();
            int i = (Integer)entry.getKey();
            int j = (Integer)entry.getValue();
            if (j > 0) {
                entry.setValue(j - 1);
            } else {
                this.releaseKey(i);
                iterator.remove();
            }
        }
    }

    public void tapKey(int i) {
        this.pressKey(i, TAP_TICKS);
    }

    public boolean isPressed(int i) {
        return this.heldKeys.containsKey(i);
    }

    @EventHandler(priority = 200)
    private void onWorldRender(TickEvent tickevent) {
        this.tickKeys();
    }
}
