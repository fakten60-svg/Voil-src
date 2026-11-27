package lol.lwes.voil.util;

public final class TickRequest {
    public final int releaseTicks;
    public final int chargeTicks;
    public final boolean holdPackets;

    public TickRequest(int releaseTicks, int chargeTicks, boolean holdPackets) {
        this.releaseTicks = Math.max(0, releaseTicks);
        this.chargeTicks = Math.max(0, chargeTicks);
        this.holdPackets = holdPackets;
    }

    @Override
    public String toString() {
        return "TickRequest[release=" + this.releaseTicks + ", charge=" + this.chargeTicks + ", holdPackets=" + this.holdPackets + "]";
    }
}
