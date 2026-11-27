package lol.lwes.voil.mixin;

import lol.lwes.voil.hook.ResourcePackHooks;
import net.minecraft.network.listener.ClientCommonPacketListener;
import net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ResourcePackSendS2CPacket.class)
public abstract class ResourcePackSendS2CPacketMixin {
    @Inject(method = "apply(Lnet/minecraft/network/listener/ClientCommonPacketListener;)V", at = @At("HEAD"), cancellable = true)
    private void apply(ClientCommonPacketListener clientcommonpacketlistener, CallbackInfo callbackinfo) {
        ResourcePackHooks.onApply(clientcommonpacketlistener, callbackinfo);
    }
}
