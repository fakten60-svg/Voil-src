package lol.lwes.voil.event;

import net.minecraft.network.DisconnectionInfo;

public class DisconnectEvent {
    private final DisconnectionInfo info;

    public DisconnectEvent(DisconnectionInfo disconnectioninfo) {
        this.info = disconnectioninfo;
    }

    public static DisconnectEvent create(DisconnectionInfo disconnectioninfo) {
        return new DisconnectEvent(disconnectioninfo);
    }

    public DisconnectionInfo getInfo() {
        return this.info;
    }
}
