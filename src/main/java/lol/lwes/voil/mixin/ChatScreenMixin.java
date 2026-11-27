package lol.lwes.voil.mixin;

import lol.lwes.voil.hook.ChatScreenHooks;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin {
    @Shadow
    private TextFieldWidget chatField;

    @Inject(method = "init", at = @At("RETURN"))
    private void onInit(CallbackInfo callbackinfo) {
        ChatScreenHooks.onInit(callbackinfo, this.chatField);
    }

    @Inject(
        method = "removed",
        at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screen/ChatScreen;client:Lnet/minecraft/client/MinecraftClient;", ordinal = 0)
    )
    private void onRemoved(CallbackInfo callbackinfo) {
        ChatScreenHooks.onRemoved(callbackinfo);
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void onMouseScrolled(double d0, double d1, double d2, double d3, CallbackInfoReturnable callbackinforeturnable) {
        ChatScreenHooks.onMouseScrolled(d0, d1, d2, d3, callbackinforeturnable);
    }
}
