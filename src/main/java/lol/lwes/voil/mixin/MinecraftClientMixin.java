package lol.lwes.voil.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import lol.lwes.voil.hook.MinecraftClientHook;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Inject(method = "handleBlockBreaking", at = @At("HEAD"), cancellable = true)
    private void onPreBlockBreak(boolean flag, CallbackInfo callbackinfo) {
        MinecraftClientHook.a(callbackinfo);
    }

    @Inject(method = "handleBlockBreaking", at = @At("RETURN"), cancellable = true)
    private void onPostBlockBreak(boolean flag, CallbackInfo callbackinfo) {
        MinecraftClientHook.b(callbackinfo);
    }

    @Inject(method = "doAttack", at = @At("RETURN"), cancellable = true)
    private void onPostAttack(CallbackInfoReturnable callbackinforeturnable) {
        MinecraftClientHook.onAttackBlock(callbackinforeturnable);
    }

    @Inject(method = "doItemUse", at = @At("HEAD"), cancellable = true)
    private void onPreItemUse(CallbackInfo callbackinfo) {
        MinecraftClientHook.onItemUse(callbackinfo);
    }

    @Inject(method = "doItemUse", at = @At("RETURN"), cancellable = true)
    private void onPostItemUse(CallbackInfo callbackinfo) {
        MinecraftClientHook.onPostItemUse(callbackinfo);
    }

    @Inject(
        method = "handleInputEvents",
        at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;options:Lnet/minecraft/client/option/GameOptions;", ordinal = 0)
    )
    private void onHandleHead(CallbackInfo callbackinfo) {
        MinecraftClientHook.saveCrosshairTarget();
    }

    @Inject(method = "handleInputEvents", at = @At("RETURN"))
    private void onHandleReturn(CallbackInfo callbackinfo) {
        MinecraftClientHook.restoreCrosshairTarget();
    }

    @Inject(method = "handleInputEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/KeyBinding;wasPressed()Z", ordinal = 4), cancellable = true)
    private void onInventoryOpen(CallbackInfo callbackinfo) {
        MinecraftClientHook.onDropKey(callbackinfo);
    }

    @Inject(method = "handleInputEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/KeyBinding;wasPressed()Z", ordinal = 14), cancellable = true)
    private void onAttack(CallbackInfo callbackinfo) {
        MinecraftClientHook.onAttackKey(callbackinfo);
    }

    @Inject(method = "render", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;window:Lnet/minecraft/client/util/Window;", ordinal = 0))
    private void onPreRender(CallbackInfo callbackinfo) {
        MinecraftClientHook.onPreRender();
    }

    @Inject(
        method = "close",
        at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;currentGlTimerQuery:Lnet/minecraft/client/gl/GlTimer$Query;", ordinal = 0)
    )
    private void onClose(CallbackInfo callbackinfo) {
        MinecraftClientHook.onShutdown();
    }

    @Inject(method = "doAttack", at = @At("HEAD"), cancellable = true)
    private void onPreAttack(CallbackInfoReturnable callbackinforeturnable) {
        MinecraftClientHook.onDoAttack(callbackinforeturnable);
    }

    @Inject(method = "handleInputEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/KeyBinding;wasPressed()Z", ordinal = 15), cancellable = true)
    private void onUseItem(CallbackInfo callbackinfo) {
        MinecraftClientHook.onUseKey(callbackinfo);
    }

    @Inject(method = "handleInputEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/KeyBinding;wasPressed()Z", ordinal = 16), cancellable = true)
    private void onPickItem(CallbackInfo callbackinfo) {
        MinecraftClientHook.onPickItemKey(callbackinfo);
    }

    @Inject(
        method = "tick",
        at = @At("HEAD"),
        cancellable = true
    )
    public void onTick(CallbackInfo callbackinfo) {
        MinecraftClientHook.onTick(callbackinfo);
    }

    @Inject(method = "handleInputEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/KeyBinding;wasPressed()Z", ordinal = 2), cancellable = true)
    private void onHotbarKey(CallbackInfo callbackinfo, @Local(ordinal = 0) int i) {
        MinecraftClientHook.onHotbarKey(callbackinfo, i);
    }
}
