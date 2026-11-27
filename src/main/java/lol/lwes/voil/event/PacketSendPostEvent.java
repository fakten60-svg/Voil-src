package lol.lwes.voil.event;

import net.minecraft.network.packet.Packet;

public class PacketSendPostEvent {
    public Packet packet;

    public static PacketSendPostEvent create(Packet packet) {
        PacketSendPostEvent packetsendpostevent = new PacketSendPostEvent();
        packetsendpostevent.packet = packet;
        return packetsendpostevent;
    }

    public Packet getPacket() {
        return this.packet;
    }
}
