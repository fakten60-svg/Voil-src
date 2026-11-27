package lol.lwes.voil.hook;

import lol.lwes.voil.VoilClient;
import lol.lwes.voil.event.KeyPressEvent;
import lol.lwes.voil.setting.KeybindSetting;
import net.minecraft.client.input.KeyInput;

public class KeyboardHook {
    public static void onKey(int i, KeyInput keyinput) {
        if (!VoilClient.instance.disabled) {
            if (i == 1) {
                KeybindSetting.addPressedKey(keyinput.getKeycode());
            }

            KeyPressEvent keypressevent = KeyPressEvent.create(keyinput.getKeycode(), keyinput.scancode(), i);
            VoilClient.eventBus.postObject(keypressevent);
        }
    }
}
