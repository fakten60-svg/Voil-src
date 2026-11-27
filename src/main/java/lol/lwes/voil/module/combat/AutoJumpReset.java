package lol.lwes.voil.module.combat;

import lol.lwes.voil.event.EventHandler;
import lol.lwes.voil.event.InputEvent;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.NumberSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.AimMathUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerEntity;

public class AutoJumpReset extends Module {
    private final NumberSetting jumpResetChance = new NumberSetting("Jump Reset Chance", 1.0, 100.0, 100.0, 1.0, "Chance to jump on hit");
    private final NumberSetting hurtTime = new NumberSetting("Hurt Time", 1.0, 10.0, 9.0, 1.0, "Hurt time to jump at");

    public AutoJumpReset() {
        super("AutoJumpReset", "Automatically jumps on hit", Category.Combat);
        Setting[] asetting = new Setting[]{this.jumpResetChance, this.hurtTime};
        this.addSettings(asetting);
    }

    @Override
    public String getSuffix() {
        return String.valueOf(this.jumpResetChance.getInt());
    }

    private boolean hasNearbyPlayer() {
        assert mc.world != null;

        for (PlayerEntity playerentity : mc.world.getPlayers()) {
            if (!playerentity.getAbilities().creativeMode && playerentity != mc.player && playerentity.isAlive() && playerentity.distanceTo(mc.player) <= 6.0F) {
                return true;
            }
        }

        return false;
    }

    @EventHandler
    private void onInput(InputEvent inputevent) {
        if (inputevent.localPlayer) {
            if (mc.player != null) {
                if (!(mc.currentScreen instanceof HandledScreen)) {
                    if (!mc.player.isOnFire()) {
                        if (this.hasNearbyPlayer()) {
                            if (!mc.player.isInsideWall()) {
                                if (!mc.player.isTouchingWater()) {
                                    if (!mc.world
                                        .getStatesInBox(mc.player.getBoundingBox().expand(0.001))
                                        .anyMatch(blockstate -> blockstate.isOf(Blocks.COBWEB))) {
                                        if (mc.player.isSprinting()) {
                                            if (mc.player.hurtTime == this.hurtTime.getInt() && AimMathUtil.randomInt(0, 100) <= this.jumpResetChance.getInt()) {
                                                inputevent.input.playerInput.jump = true;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
