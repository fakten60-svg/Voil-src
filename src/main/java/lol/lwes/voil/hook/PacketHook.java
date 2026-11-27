package lol.lwes.voil.hook;

import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import java.io.Serializable;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.event.DisconnectEvent;
import lol.lwes.voil.event.EventBus;
import lol.lwes.voil.event.PacketReceiveEvent;
import lol.lwes.voil.event.PacketSendEvent;
import lol.lwes.voil.event.PacketSendPostEvent;
import lol.lwes.voil.util.proxy.ProxyManager;
import net.minecraft.network.DisconnectionInfo;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class PacketHook implements Serializable {
    private static final String PROXY_HANDLER_NAME = "voil_proxy";

    public static void onSendPacket(Packet packet, CallbackInfo callbackinfo) {
        if (!VoilClient.getInstance().disabled) {
            if (VoilClient.silentPackets.contains(packet)) {
                VoilClient.silentPackets.remove(packet);
            } else {
                EventBus eventbus = VoilClient.eventBus;
                PacketSendEvent packetsendevent = PacketSendEvent.create(packet);
                if (((PacketSendEvent)eventbus.post(packetsendevent)).isCancelled()) {
                    callbackinfo.cancel();
                }
            }
        }
    }

    public static void onReceivePacket(Packet packet, CallbackInfo callbackinfo) {
        if (!VoilClient.getInstance().disabled) {
            EventBus eventbus = VoilClient.eventBus;
            PacketReceiveEvent packetreceiveevent = PacketReceiveEvent.create(packet, packet);
            if (((PacketReceiveEvent)eventbus.post(packetreceiveevent)).isCancelled()) {
                callbackinfo.cancel();
            }
        }
    }

    public static void onPipelineSetup(ChannelPipeline channelpipeline, NetworkSide networkside, boolean flag) {
        if (!flag && networkside == NetworkSide.CLIENTBOUND) {
            ChannelOutboundHandlerAdapter channeloutboundhandleradapter = ProxyManager.createCurrentHandler();
            if (channeloutboundhandleradapter != null) {
                channelpipeline.addFirst(PROXY_HANDLER_NAME, channeloutboundhandleradapter);
            }
        }
    }

    public static void onPacketSent(Packet packet) {
        if (!VoilClient.getInstance().disabled) {
            VoilClient.eventBus.postObject(PacketSendPostEvent.create(packet));
        }
    }

    public static void onDisconnect(DisconnectionInfo disconnectioninfo) {
        VoilClient.eventBus.postObject(DisconnectEvent.create(disconnectioninfo));
    }
}
