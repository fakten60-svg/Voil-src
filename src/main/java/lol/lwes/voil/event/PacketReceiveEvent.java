package lol.lwes.voil.event;

import net.minecraft.network.packet.Packet;

public class PacketReceiveEvent extends Event {
    private static final PacketReceiveEvent INSTANCE = new PacketReceiveEvent();
    public Packet packet;
    public Packet originalPacket;

    public Packet getPacket() {
        return this.packet;
    }

    public Packet getOriginalPacket() {
        return this.originalPacket;
    }

    public static PacketReceiveEvent create(Packet packet, Packet packet1) {
        INSTANCE.setCancelled2(false);
        INSTANCE.packet = packet;
        INSTANCE.originalPacket = packet1;
        return INSTANCE;
    }
}
