package lol.lwes.voil.mixin;

import io.netty.channel.ChannelPipeline;
import lol.lwes.voil.hook.PacketHook;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.DisconnectionInfo;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.handler.PacketSizeLogger;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public abstract class ClientConnectionMixin {
    @Inject(method = "handlePacket", at = @At("HEAD"), cancellable = true)
    private static void onPacketReceive(Packet packet, PacketListener packetlistener, CallbackInfo callbackinfo) {
        if (packetlistener.getSide() == NetworkSide.CLIENTBOUND) {
            PacketHook.onReceivePacket(packet, callbackinfo);
        }
    }

    @Inject(method = "send(Lnet/minecraft/network/packet/Packet;)V", at = @At("HEAD"), cancellable = true)
    void onPacketSend(Packet packet, CallbackInfo callbackinfo) {
        if (this.isClientSide()) {
            PacketHook.onSendPacket(packet, callbackinfo);
        }
    }

    @Inject(
        method = "disconnect(Lnet/minecraft/network/DisconnectionInfo;)V",
        at = @At(value = "FIELD", target = "Lnet/minecraft/network/ClientConnection;channel:Lio/netty/channel/Channel;", ordinal = 0)
    )
    private void onDisconnect(DisconnectionInfo disconnectioninfo, CallbackInfo callbackinfo) {
        if (this.isClientSide()) {
            PacketHook.onDisconnect(disconnectioninfo);
        }
    }

    @Inject(method = "send(Lnet/minecraft/network/packet/Packet;)V", at = @At("RETURN"), cancellable = true)
    private void onSendPacketPost(Packet packet, CallbackInfo callbackinfo) {
        if (this.isClientSide()) {
            PacketHook.onPacketSent(packet);
        }
    }

    @Inject(method = "addHandlers", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkSide;getOpposite()Lnet/minecraft/network/NetworkSide;"))
    private static void addProxy(
        ChannelPipeline channelpipeline, NetworkSide networkside, boolean flag, PacketSizeLogger packetsizelogger, CallbackInfo callbackinfo
    ) {
        PacketHook.onPipelineSetup(channelpipeline, networkside, flag);
    }

    @Unique
    private boolean isClientSide() {
        return ((ClientConnection)(Object)this).getSide() == NetworkSide.CLIENTBOUND;
    }
}
