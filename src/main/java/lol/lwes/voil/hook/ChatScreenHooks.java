package lol.lwes.voil.hook;

import lol.lwes.voil.module.ModuleManager;
import lol.lwes.voil.module.client.Interface;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class ChatScreenHooks {
    private static final String CHAT_OPTION = "Chat";
    private static TextFieldWidget chatField;

    public static void onInit(CallbackInfo callbackInfo, TextFieldWidget field) {
        if (field != null) {
            chatField = field;
        }

        Interface iface = getCustomChat();
        if (iface != null) {
            iface.setChatOpen(true);
        }
    }

    public static void onRemoved(CallbackInfo callbackInfo) {
        chatField = null;
        Interface iface = getCustomChat();
        if (iface != null) {
            iface.setChatOpen(false);
        }
    }

    public static void onMouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount, CallbackInfoReturnable<Boolean> callbackInfo) {
        Interface iface = getCustomChat();
        if (iface == null) {
            return;
        }

        int lines = (int) Math.signum(verticalAmount);
        if (lines != 0) {
            iface.scrollChat(lines);
        }

        callbackInfo.setReturnValue(true);
    }

    public static TextFieldWidget getChatField() {
        return chatField;
    }

    private static Interface getCustomChat() {
        Interface iface = (Interface) ModuleManager.INSTANCE.getModule(Interface.class);
        if (iface == null || !iface.isEnabled() || !iface.changes.isSelected(CHAT_OPTION)) {
            return null;
        }

        return iface;
    }
}
