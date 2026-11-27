package lol.lwes.voil.module.movement;

import lol.lwes.voil.event.EventHandler;
import lol.lwes.voil.event.InputEvent;
import lol.lwes.voil.mixin.accessor.LivingEntityAccessor;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.NumberSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.Stopwatch;
import lol.lwes.voil.util.world.BlockUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public class AutoHeadHitter extends Module {
    private final NumberSetting jumpDelay = new NumberSetting("Jump delay", 0.0, 20.0, 0.0, 1.0, "Delay to jump");
    Stopwatch timer = new Stopwatch();

    public AutoHeadHitter() {
        super("AutoHeadHitter", "Automatically jumps when block above", Category.Movement);
        Setting[] asetting = new Setting[]{this.jumpDelay};
        this.addSettings(asetting);
    }

    @Override
    public void onTick() {
        if (mc.player != null && this.shouldJump()) {
            ((LivingEntityAccessor)mc.player).setJumpingCooldown(0);
        }
    }

    private boolean shouldJump() {
        BlockPos blockpos = mc.player.getBlockPos().up(2);
        if (!mc.player.isSprinting()) {
            return false;
        }

        Box box = BlockUtil.getOutlineBox(blockpos);
        return box == null ? false : box.offset(blockpos.multiply(-1)).minY == 0.0;
    }

    @EventHandler
    private void onInput(InputEvent inputevent) {
        if (inputevent.localPlayer && this.shouldJump()) {
            inputevent.input.playerInput.jump = true;
        }
    }
}
