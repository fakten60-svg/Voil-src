package lol.lwes.voil.event;

import java.util.function.Consumer;
import net.minecraft.network.packet.Packet;

public class PacketBlinkEvent {
    public Packet packet;
    public boolean flag1;
    public boolean held;
    public boolean flag2;
    public Long delayMs;
    public Consumer<Packet> callback;

    private PacketBlinkEvent() {
    }

    public static PacketBlinkEvent create(Packet packet, boolean flag, Long olong, boolean flag1, boolean flag2, Consumer<Packet> consumer) {
        PacketBlinkEvent packetblinkevent = new PacketBlinkEvent();
        packetblinkevent.packet = packet;
        packetblinkevent.flag1 = flag;
        packetblinkevent.held = flag1;
        packetblinkevent.flag2 = flag2;
        packetblinkevent.delayMs = olong;
        packetblinkevent.callback = consumer;
        return packetblinkevent;
    }
}
