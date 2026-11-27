package lol.lwes.voil.mixin;

import lol.lwes.voil.hook.ClientPlayerEntityHooks;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin {
    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;tick()V"), cancellable = true)
    public void sendTickMovementPacketsPre(CallbackInfo callbackinfo) {
        ClientPlayerEntityHooks.sendTickMovementPacketsPre(this);
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void sendTickMovementPacketsPost(CallbackInfo callbackinfo) {
        ClientPlayerEntityHooks.sendTickMovementPacketsPost(this);
    }
}
