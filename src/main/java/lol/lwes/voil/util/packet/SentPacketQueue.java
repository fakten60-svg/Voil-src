package lol.lwes.voil.util.packet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.event.EventHandler;
import lol.lwes.voil.event.PacketBlinkEvent;
import lol.lwes.voil.event.PacketSendEvent;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.query.QueryPingC2SPacket;
import org.apache.commons.lang3.tuple.Triple;

public class SentPacketQueue {
    private final List<Triple<Packet<?>, Long, Consumer<Packet>>> queue = Collections.synchronizedList(new ArrayList<>());
    public boolean holding;
    public boolean flushing;
    public long lastPacketTime;
    private static final int MAX_SENDS_PER_FLUSH = 10000;

    public void flushQueue(boolean flag) {
        if (VoilClient.mc.getNetworkHandler() != null) {
            synchronized (this.queue) {
                for (int i = 0;
                    !this.queue.isEmpty() && (!flag || this.queue.get(0).getMiddle() < System.currentTimeMillis()) && i < MAX_SENDS_PER_FLUSH;
                    i++
                ) {
                    Triple triple = (Triple)this.queue.get(0);

                    try {
                        if (triple != null) {
                            Packet packet = (Packet)triple.getLeft();
                            Consumer consumer = (Consumer)triple.getRight();
                            VoilClient.sendSilentPacket(packet);
                            if (consumer != null) {
                                consumer.accept(packet);
                            }
                        }
                    } catch (Exception exception1) {
                    }

                    try {
                        if (!this.queue.isEmpty()) {
                            this.queue.remove(0);
                        }
                    } catch (Exception exception) {
                    }
                }

                if (!flag) {
                    this.queue.clear();
                }
            }
        }
    }

    @EventHandler(priority = -200)
    private void onSendPacket(PacketSendEvent packetsendevent) {
        if (VoilClient.mc.player != null && VoilClient.mc.world != null && VoilClient.mc.getNetworkHandler() != null) {
            Packet packet = packetsendevent.getPacket();
            Long olong = this.lastPacketTime;
            PacketBlinkEvent packetblinkevent = PacketBlinkEvent.create(packet, false, olong, false, false, null);
            VoilClient.eventBus.postObject(packetblinkevent);
            if (!packetblinkevent.flag2
                && packetsendevent.getPacket() != null
                && !packetsendevent.isCancelled()
                && VoilClient.mc.player != null
                && VoilClient.mc.world != null
                && VoilClient.mc.interactionManager != null
                && VoilClient.mc.getNetworkHandler() != null
                && !(packetsendevent.getPacket() instanceof QueryPingC2SPacket)) {
                if (!this.holding && packetblinkevent.flag1) {
                    this.holding = true;
                    this.flushQueue(false);
                }

                if (packetblinkevent.delayMs != this.lastPacketTime) {
                    this.lastPacketTime = packetblinkevent.delayMs;
                    this.flushQueue(false);
                }

                if (!this.flushing && packetblinkevent.held) {
                    this.flushing = true;
                    this.flushQueue(false);
                }

                if (packetblinkevent.flag1) {
                    synchronized (this.queue) {
                        this.queue.add(Triple.of(packetsendevent.getPacket(), System.currentTimeMillis() + this.lastPacketTime, packetblinkevent.callback));
                        this.flushQueue(true);
                        packetsendevent.cancel();
                    }
                } else if (packetblinkevent.held) {
                    synchronized (this.queue) {
                        this.queue.add(Triple.of(packetsendevent.getPacket(), 0L, packetblinkevent.callback));
                        packetsendevent.cancel();
                    }
                } else {
                    this.flushing = false;
                    this.holding = false;
                    this.flushQueue(false);
                }
            }
        }
    }
}
