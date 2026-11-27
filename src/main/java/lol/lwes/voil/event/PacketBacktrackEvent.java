package lol.lwes.voil.event;

import net.minecraft.network.packet.Packet;

public class PacketBacktrackEvent {
    public Packet packet;
    public boolean flag1;
    public boolean flag2;
    public boolean flag3;
    public boolean flag4;
    public Long delayMs;

    private PacketBacktrackEvent() {
    }

    public static PacketBacktrackEvent create(Packet packet, boolean flag, boolean flag1, Long olong, boolean flag2, boolean flag3) {
        PacketBacktrackEvent packetbacktrackevent = new PacketBacktrackEvent();
        packetbacktrackevent.packet = packet;
        packetbacktrackevent.flag1 = flag;
        packetbacktrackevent.flag2 = flag2;
        packetbacktrackevent.flag3 = flag1;
        packetbacktrackevent.flag4 = flag3;
        packetbacktrackevent.delayMs = olong;
        return packetbacktrackevent;
    }
}
