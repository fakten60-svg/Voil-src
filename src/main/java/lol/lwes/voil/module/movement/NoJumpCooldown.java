package lol.lwes.voil.module.movement;

import lol.lwes.voil.mixin.accessor.LivingEntityAccessor;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;

public class NoJumpCooldown extends Module {
    public int jumpCooldown = 0;

    public NoJumpCooldown() {
        super("NoJumpCooldown", "Removes jump cooldown", Category.Movement);
    }

    @Override
    public void onTick() {
        if (mc.player != null) {
            ((LivingEntityAccessor)mc.player).setJumpingCooldown(this.jumpCooldown);
        }
    }
}
