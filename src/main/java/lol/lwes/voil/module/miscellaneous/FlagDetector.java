package lol.lwes.voil.module.miscellaneous;

import lol.lwes.voil.VoilClient;
import lol.lwes.voil.event.EventHandler;
import lol.lwes.voil.event.PacketReceiveEvent;
import lol.lwes.voil.event.WorldJoinEvent;
import lol.lwes.voil.gui.NotificationManager;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.ButtonSetting;
import lol.lwes.voil.setting.ModeSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.ChatUtil;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;

public class FlagDetector extends Module {
    ModeSetting mode = new ModeSetting("Mode", "Chat", "How to send alert", new String[]{"Notification", "Chat"});
    ButtonSetting resetFlags = new ButtonSetting("Reset Flags", () -> flagCount = 0, "Clears flags list");
    public static int flagCount = 0;

    public FlagDetector() {
        super("FlagDetector", "Sends a notification when you flag anticheat", Category.Miscellaneous);
        Setting[] asetting = new Setting[]{this.mode, this.resetFlags};
        this.addSettings(asetting);
    }

    @EventHandler
    public void onPacketReceive(PacketReceiveEvent packetreceiveevent) {
        if (mc.player != null) {
            if (packetreceiveevent.getPacket() instanceof PlayerPositionLookS2CPacket && mc.player.age > 40) {
                int i = flagCount++;
                if (this.mode.is("Chat")) {
                    int j = flagCount;
                    ChatUtil.info("§cFlag Detected! §eFlags: §f" + j);
                } else {
                    NotificationManager notificationmanager = VoilClient.getNotifications();
                    int k = flagCount;
                    String s2 = "§cFlag Detected! §eFlags: §f" + k;
                    String s3 = "FlagDetected!";
                    notificationmanager.addNotification(s3, s2);
                }
            }
        }
    }

    @EventHandler
    public void onJoinWorld(WorldJoinEvent worldjoinevent) {
        flagCount = 0;
    }
}
