package lol.lwes.voil.module.movement;

import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.ModeSetting;
import lol.lwes.voil.setting.Setting;

public class FastFall extends Module {
    ModeSetting mode = new ModeSetting("Mode", "Vanilla", "Mode of the FastFall", new String[]{"Matrix", "Vanilla"});

    public FastFall() {
        super("FastFall", "Makes you fall faster", Category.Movement);
        Setting[] asetting = new Setting[]{this.mode};
        this.addSettings(asetting);
    }

    @Override
    public void onTick() {
        if (mc.player.fallDistance > 0.4 && this.mode.is("Vanilla")) {
            mc.player.setVelocity(mc.player.getVelocity().x, -1.6, mc.player.getVelocity().z);
        }

        if (mc.player.fallDistance > 0.8F && this.mode.is("Matrix") && mc.player.fallDistance > 0.8) {
            mc.player.setVelocity(0.0, -0.54, 0.0);
        }
    }
}
