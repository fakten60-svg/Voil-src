package lol.lwes.voil.module.movement;

import lol.lwes.voil.event.EventHandler;
import lol.lwes.voil.event.PlayerMotionEvent;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.ModeSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.RaycastUtil;
import lol.lwes.voil.util.world.BlockUtil;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.BlockItem;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;

public class BridgeAssist extends Module {
    BooleanSetting autoPlace = new BooleanSetting("Auto Place", false, "Auto place blocks");
    ModeSetting safeWalkMode = new ModeSetting("SafeWalk Mode", "Sneak", "Mode for SafeWalk", new String[]{"Motion", "Sneak"});
    public boolean shouldSneak;
    private static final String MOTION_MODE = "Motion";

    public BridgeAssist() {
        super("BridgeAssist", "Automatically bridges", Category.Movement);
        Setting[] asetting = new Setting[]{this.autoPlace, this.safeWalkMode};
        this.addSettings(asetting);
    }

    @Override
    public void onTick() {
        if (mc.player.getMainHandStack().getItem() instanceof BlockItem || mc.player.getOffHandStack().getItem() instanceof BlockItem) {
            if (mc.player.getPitch() < 70.0F) {
                if (this.shouldSneak) {
                    mc.options.sneakKey.setPressed(false);
                    this.shouldSneak = false;
                }

                return;
            }

            BlockPos blockpos = BlockPos.ofFloored(mc.player.getEntityPos()).down();
            ClientWorld clientworld = mc.world;
            if (clientworld.getBlockState(blockpos).isReplaceable()
                && clientworld.getBlockState(blockpos.down()).isReplaceable()
                && clientworld.getBlockState(blockpos.down().down()).isReplaceable()) {
                mc.options.sneakKey.setPressed(true);
                this.shouldSneak = true;
                if (this.autoPlace.isEnabled() && mc.player.getMainHandStack().getItem() instanceof BlockItem) {
                    ClientPlayerEntity clientplayerentity1 = mc.player;
                    float f = mc.player.getPitch();
                    float f1 = mc.player.getYaw();
                    ClientPlayerEntity clientplayerentity = clientplayerentity1;
                    BlockHitResult blockhitresult = RaycastUtil.raycastBlockAngles(clientplayerentity, f1, f, 3.0, true);
                    BlockPos blockpos1 = blockhitresult.getBlockPos();
                    Block block = mc.world.getBlockState(blockpos1).getBlock();
                    if (block == Blocks.AIR) {
                        return;
                    }

                    if (blockhitresult != null) {
                        BlockUtil.interactBlockMainHand(blockhitresult, true);
                    }
                }
            } else if (this.shouldSneak) {
                mc.options.sneakKey.setPressed(false);
                this.shouldSneak = false;
            }
        }
    }

    @EventHandler
    public void onMotion(PlayerMotionEvent playermotionevent) {
        if (!this.safeWalkMode.is(MOTION_MODE) || mc.player == null || mc.world == null) {
            ;
        }
    }
}
