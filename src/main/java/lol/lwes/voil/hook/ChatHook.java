package lol.lwes.voil.hook;

import lol.lwes.voil.module.ModuleManager;
import lol.lwes.voil.module.client.Interface;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class ChatHook {
    private static final String CHAT_OPTION = "Chat";

    public static void onAddMessage(Text text, MessageIndicator messageindicator) {
        Interface iface = (Interface)ModuleManager.INSTANCE.getModule(Interface.class);
        if (iface != null) {
            iface.addChatMessage(text, messageindicator);
        }
    }

    public static void onChatInput(int i, CallbackInfo callbackinfo) {
        Interface iface = (Interface)ModuleManager.INSTANCE.getModule(Interface.class);
        if (iface != null && iface.isEnabled()) {
            String s = CHAT_OPTION;
            if (iface.changes.isSelected(s)) {
                callbackinfo.cancel();
                iface.scrollChat(i);
            }
        }
    }

    public static void onChatClosed() {
        Interface iface = (Interface)ModuleManager.INSTANCE.getModule(Interface.class);
        if (iface != null) {
            iface.clearChat();
        }
    }
}
