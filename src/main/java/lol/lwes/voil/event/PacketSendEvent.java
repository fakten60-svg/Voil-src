package lol.lwes.voil.event;

import net.minecraft.network.packet.Packet;

public class PacketSendEvent extends Event {
    public Packet packet;

    public Packet getPacket() {
        return this.packet;
    }

    public static PacketSendEvent create(Packet packet) {
        PacketSendEvent packetsendevent = new PacketSendEvent();
        packetsendevent.setCancelled2(false);
        packetsendevent.packet = packet;
        return packetsendevent;
    }
}
