package lol.lwes.voil.module.player;

import lol.lwes.voil.event.EventHandler;
import lol.lwes.voil.event.PacketReceiveEvent;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.ModeSetting;
import lol.lwes.voil.setting.Setting;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;

public class NoRotate extends Module {
    ModeSetting cancelMode = new ModeSetting("Cancel mode", "Packet", "Mode how to cancel", new String[]{"Manual", "Packet"});
    private float yaw;
    private float pitch;
    private boolean pending;

    public NoRotate() {
        super("NoRotate", "Prevents server from rotating your head", Category.Player);
        Setting[] asetting = new Setting[]{this.cancelMode};
        this.addSettings(asetting);
    }

    @Override
    public void onTick() {
        if (this.pending) {
            mc.player.setYaw(this.yaw);
            mc.player.setPitch(this.pitch);
            this.pending = false;
        }
    }

    @EventHandler
    public void onPacket(PacketReceiveEvent packetreceiveevent) {
        if (packetreceiveevent.getPacket() instanceof PlayerPositionLookS2CPacket playerpositionlooks2cpacket) {
            if (mc.player == null) {
                return;
            }

            if (this.cancelMode.is("Packet")) {
                packetreceiveevent.cancel();
            } else if (this.cancelMode.is("Manual")) {
                this.yaw = mc.player.getYaw();
                this.pitch = mc.player.getPitch();
                mc.player.setYaw(playerpositionlooks2cpacket.change().yaw());
                mc.player.setPitch(playerpositionlooks2cpacket.change().pitch());
                this.pending = true;
            }
        }
    }
}
