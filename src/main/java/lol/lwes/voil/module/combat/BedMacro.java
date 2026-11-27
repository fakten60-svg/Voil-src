package lol.lwes.voil.module.combat;

import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.inventory.InventoryUtil;
import net.minecraft.block.BedBlock;
import net.minecraft.item.BedItem;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;

public class BedMacro extends Module {
    BooleanSetting swapTotem = new BooleanSetting("Swap Totem", true, "Swap to totem before exploding the bed");
    BooleanSetting autoReplenish = new BooleanSetting("Auto Replenish", true, "Auto gets bed from inventory");

    public BedMacro() {
        super("BedMacro", "Macro for bed", Category.Combat);
        Setting[] asetting = new Setting[]{this.swapTotem, this.autoReplenish};
        this.addSettings(asetting);
    }

    @Override
    public void onTick() {
        if (mc.crosshairTarget != null && mc.crosshairTarget.getType() == Type.BLOCK && mc.crosshairTarget instanceof BlockHitResult blockhitresult) {
            BlockPos blockpos = blockhitresult.getBlockPos();
            if (mc.world.getBlockState(blockpos).getBlock() instanceof BedBlock) {
                if (this.swapTotem.isEnabled()) {
                    InventoryUtil.hasItemInHotbar(Items.TOTEM_OF_UNDYING);
                    mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, blockhitresult);
                    mc.player.swingHand(Hand.MAIN_HAND);
                } else {
                    mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, blockhitresult);
                    mc.player.swingHand(Hand.MAIN_HAND);
                }

                if (this.autoReplenish.isEnabled() && !(mc.player.getMainHandStack().getItem() instanceof BedItem)) {
                    int i = this.findBedSlot();
                    if (i != -1) {
                        int j = mc.player.getInventory().getSelectedSlot();
                        if (i != j) {
                            mc.player.getInventory().setSelectedSlot(i);
                        }
                    }
                }
            }
        }
    }

    private int findBedSlot() {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() instanceof BedItem) {
                return i;
            }
        }

        return -1;
    }
}
