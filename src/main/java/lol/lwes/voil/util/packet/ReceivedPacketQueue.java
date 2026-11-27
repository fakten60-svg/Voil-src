package lol.lwes.voil.util.packet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.event.EventHandler;
import lol.lwes.voil.event.PacketBacktrackEvent;
import lol.lwes.voil.event.PacketReceiveEvent;
import net.minecraft.client.gui.screen.world.LevelLoadingScreen;
import net.minecraft.network.NetworkPhase;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.query.QueryPingC2SPacket;
import net.minecraft.util.Pair;

public class ReceivedPacketQueue {
    private final List<Pair<Packet<?>, Long>> queue = Collections.synchronizedList(new ArrayList<>());
    public boolean holding;
    public boolean flushing;
    public boolean delaying;
    public long lastPacketTime;
    private static final int MAX_RELEASES_PER_FLUSH = 10000;

    public void flushQueue(boolean flag) {
        if (VoilClient.mc.getNetworkHandler() != null) {
            synchronized (this.queue) {
                for (int i = 0;
                    !this.queue.isEmpty()
                        && (!flag || this.queue.get(0).getRight() + this.lastPacketTime < System.currentTimeMillis())
                        && i < MAX_RELEASES_PER_FLUSH;
                    i++
                ) {
                    Pair pair = (Pair)this.queue.get(0);

                    try {
                        ((Packet)pair.getLeft()).apply(VoilClient.mc.getNetworkHandler());
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

    public void clearQueue() {
        this.flushQueue(false);
    }

    @EventHandler(priority = -200)
    public void onReceivePacket(PacketReceiveEvent packetreceiveevent) {
        if (VoilClient.mc.player != null
            && VoilClient.mc.world != null
            && VoilClient.mc.interactionManager != null
            && VoilClient.mc.getNetworkHandler() != null) {
            if (!(VoilClient.mc.currentScreen instanceof LevelLoadingScreen)) {
                PacketListener packetlistener = VoilClient.mc.getNetworkHandler().getConnection().getPacketListener();
                if (packetlistener != null
                    && packetlistener.getPhase() != NetworkPhase.HANDSHAKING
                    && packetlistener.getPhase() != NetworkPhase.STATUS
                    && packetlistener.getPhase() != NetworkPhase.CONFIGURATION) {
                    Packet packet = packetreceiveevent.getPacket();
                    Long olong = this.lastPacketTime;
                    PacketBacktrackEvent packetbacktrackevent = PacketBacktrackEvent.create(packet, false, false, olong, false, false);
                    VoilClient.eventBus.postObject(packetbacktrackevent);
                    if (!packetbacktrackevent.flag4
                        && packetreceiveevent.getPacket() != null
                        && !packetreceiveevent.isCancelled()
                        && !(packetreceiveevent.getPacket() instanceof QueryPingC2SPacket)) {
                        if (!this.holding && packetbacktrackevent.flag1) {
                            this.holding = true;
                            this.clearQueue();
                        }

                        if (!this.delaying && packetbacktrackevent.flag3) {
                            this.delaying = true;
                            this.clearQueue();
                        }

                        if (packetbacktrackevent.delayMs != this.lastPacketTime) {
                            this.lastPacketTime = packetbacktrackevent.delayMs;
                            if (!this.delaying) {
                                this.clearQueue();
                            }
                        }

                        if (!this.flushing && packetbacktrackevent.flag2) {
                            this.flushing = true;
                            this.clearQueue();
                        }

                        if (packetbacktrackevent.flag1) {
                            synchronized (this.queue) {
                                this.queue.add(new Pair<>(packetreceiveevent.getOriginalPacket(), System.currentTimeMillis()));
                            }

                            this.flushQueue(true);
                            packetreceiveevent.cancel();
                        } else if (packetbacktrackevent.flag3) {
                            synchronized (this.queue) {
                                this.queue.add(new Pair<>(packetreceiveevent.getOriginalPacket(), System.currentTimeMillis()));
                            }

                            this.flushQueue(true);
                            packetreceiveevent.cancel();
                        } else if (packetbacktrackevent.flag2) {
                            synchronized (this.queue) {
                                this.queue.add(new Pair<>(packetreceiveevent.getOriginalPacket(), 0L));
                            }

                            packetreceiveevent.cancel();
                        } else {
                            this.delaying = false;
                            this.flushing = false;
                            this.holding = false;
                            this.clearQueue();
                        }
                    }
                } else {
                    synchronized (this.queue) {
                        this.queue.clear();
                    }
                }
            }
        }
    }
}
