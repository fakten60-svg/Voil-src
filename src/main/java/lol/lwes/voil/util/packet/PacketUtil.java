package lol.lwes.voil.util.packet;

import lol.lwes.voil.VoilClient;
import lol.lwes.voil.mixin.accessor.ClientWorldAccessor;
import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.client.network.SequencedPacketCreator;
import net.minecraft.network.packet.Packet;

public class PacketUtil {
    public static void sendSequenced(SequencedPacketCreator sequencedpacketcreator) {
        if (VoilClient.mc.getNetworkHandler() != null && VoilClient.mc.world != null) {
            try (PendingUpdateManager pendingupdatemanager = ((ClientWorldAccessor)VoilClient.mc.world).getPendingUpdateManager().incrementSequence()) {
                int i = pendingupdatemanager.getSequence();
                VoilClient.mc.getNetworkHandler().sendPacket(sequencedpacketcreator.predict(i));
            }
        }
    }

    public static void sendImmediate(Packet packet) {
        VoilClient.mc.getNetworkHandler().getConnection().send(packet, null);
    }

    public static void sendPacket(Packet packet) {
        if (VoilClient.mc.player != null) {
            VoilClient.mc.getNetworkHandler().sendPacket(packet);
        }
    }
}
