package lol.lwes.voil.module.combat;

import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.NumberSetting;
import lol.lwes.voil.setting.Setting;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;

public class FastEat extends Module {
    private final NumberSetting health = new NumberSetting("Health", 1.0, 20.0, 10.0, 1.0, "Minimum health to fast eat");
    private final BooleanSetting workOnHealth = new BooleanSetting("Work on Health", true, "Only fast eat when health is in a certain range");
    private final NumberSetting speed = new NumberSetting("Speed", 0.0, 3.0, 1.0, 1.0, "Speed to eat");

    public FastEat() {
        super("FastEat", "Lets you eat faster", Category.Combat);
        Setting[] asetting = new Setting[]{this.health, this.workOnHealth, this.speed};
        this.addSettings(asetting);
    }

    @Override
    public void onTick() {
        if (this.isEnabled() && mc.player != null) {
            ClientPlayerEntity clientplayerentity = mc.player;
            if (clientplayerentity.isUsingItem()) {
                clientplayerentity.getActiveItem();
                boolean flag = true;
                if (this.workOnHealth.isEnabled()) {
                    flag = clientplayerentity.getHealth() <= this.health.getValue();
                }

                if (flag) {
                    int i = this.speed.getInt();

                    for (int j = 0; j < i; j++) {
                        mc.getNetworkHandler().sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0, mc.player.getYaw(), mc.player.getPitch()));
                    }

                    mc.gameRenderer.firstPersonRenderer.resetEquipProgress(Hand.MAIN_HAND);
                }
            }
        }
    }
}
