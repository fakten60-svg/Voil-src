package lol.lwes.voil.mixin;

import lol.lwes.voil.hook.ChatHook;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatHud.class)
public abstract class ChatHudMixin {
    @Inject(method = "clear", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/hud/ChatHud;client:Lnet/minecraft/client/MinecraftClient;", ordinal = 0))
    private void onClear(boolean flag, CallbackInfo callbackinfo) {
        ChatHook.onChatClosed();
    }

    @Inject(method = "scroll", at = @At("HEAD"), cancellable = true)
    private void onScroll(int i, CallbackInfo callbackinfo) {
        ChatHook.onChatInput(i, callbackinfo);
    }

    @Inject(
        method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V",
        at = @At("HEAD")
    )
    private void onAddMessage(Text text, MessageSignatureData messagesignaturedata, MessageIndicator messageindicator, CallbackInfo callbackinfo) {
        ChatHook.onAddMessage(text, messageindicator);
    }
}
