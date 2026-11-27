package lol.lwes.voil.module.player;

import lol.lwes.voil.event.EventHandler;
import lol.lwes.voil.event.PacketBlinkEvent;
import lol.lwes.voil.event.TickEvent;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.ModeSetting;
import lol.lwes.voil.setting.NumberSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.packet.PacketUtil;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.Full;

public class NoFall extends Module {
    ModeSetting mode = new ModeSetting("Mode", "Vulcan", "Mode of the NoFall", new String[]{"Vanilla", "Blink", "Vulcan"});
    NumberSetting distance = new NumberSetting("distance", 3.0, 20.0, 4.0, 0.1, "Distance to activate the NoFall");
    private boolean blinking = false;
    private long blinkStartTime = 0L;
    private final long blinkTimeout = 500L;

    public NoFall() {
        super("NoFall", "Removes fall damage", Category.Player);
        Setting[] asetting = new Setting[]{this.mode, this.distance};
        this.addSettings(asetting);
    }

    @Override
    public void onTick() {
        if (mc.player != null) {
            if (mc.player.fallDistance >= this.distance.getFloat() && this.mode.is("Vulcan")) {
                Full full = new Full(
                    mc.player.getX(),
                    mc.player.getY() + 1.0E-6,
                    mc.player.getZ(),
                    mc.player.getYaw(),
                    mc.player.getPitch(),
                    mc.player.isOnGround(),
                    mc.player.horizontalCollision
                );
                PacketUtil.sendPacket(full);
                mc.player.onLanding();
            }
        }
    }

    @EventHandler
    private void onUpdate(TickEvent tickevent) {
        if (mc.player != null && mc.world != null) {
            if (this.mode.is("Blink")) {
                if (mc.player.fallDistance > 2.5) {
                    this.blinking = true;
                }

                if (this.blinking) {
                    long i = System.currentTimeMillis();
                    if (i - this.blinkStartTime >= 500L) {
                        this.blinking = false;
                        this.blinkStartTime = i;
                    }
                }
            }
        }
    }

    @EventHandler
    private void onSendPacket(PacketBlinkEvent packetblinkevent) {
        if (mc.player != null && this.mode.is("Blink")) {
            if (this.blinking) {
                packetblinkevent.held = true;
            }
        }
    }
}
