package lol.lwes.voil.module.player;

import lol.lwes.voil.event.EventHandler;
import lol.lwes.voil.event.TickEvent;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.inventory.InventoryUtil;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public class AirPotion extends Module {
    private final BooleanSetting switchBack = new BooleanSetting("Switch Back", true, "Restore the slot you had before throwing");
    private int originalSlot = -1;
    private boolean throwing = false;

    public AirPotion() {
        super("AirPotion", "Throws potions mid air and hits with windcharge", Category.Player);
        Setting[] asetting = new Setting[]{this.switchBack};
        this.addSettings(asetting);
    }

    private int findPotionSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack itemstack = mc.player.getInventory().getStack(i);
            if (this.isUsefulPotion(itemstack)) {
                return i;
            }
        }

        return -1;
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            if (this.switchBack.isEnabled() && this.originalSlot != -1 && this.originalSlot != mc.player.getInventory().getSelectedSlot()) {
                mc.player.getInventory().setSelectedSlot(this.originalSlot);
            }

            this.throwing = false;
        }
    }

    @Override
    public void onEnable() {
        this.originalSlot = mc.player != null ? mc.player.getInventory().getSelectedSlot() : -1;
        this.throwing = false;
    }

    private void throwItem(int i) {
        InventoryUtil.selectSlot(i);
        float f = mc.player.getPitch();
        mc.player.setPitch(90.0F);
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        mc.player.swingHand(Hand.MAIN_HAND);
        mc.player.setPitch(f);
    }

    @EventHandler
    public void onTick(TickEvent tickevent) {
        if (Module.isNotInGame() || mc.player.isOnGround()) {
            this.toggle();
        } else if (this.throwing) {
            this.throwing = false;
            int j = InventoryUtil.findInventorySlot(Items.WIND_CHARGE);
            if (j != -1) {
                this.throwItem(j);
            }
        } else {
            int i = this.findPotionSlot();
            if (i == -1) {
                this.toggle();
            } else {
                this.throwItem(i);
                this.throwing = true;
            }
        }
    }

    private boolean isUsefulPotion(ItemStack itemstack) {
        if (!itemstack.isOf(Items.SPLASH_POTION) && !itemstack.isOf(Items.LINGERING_POTION)) {
            return false;
        }

        PotionContentsComponent potioncontentscomponent = itemstack.get(DataComponentTypes.POTION_CONTENTS);
        if (potioncontentscomponent == null) {
            return false;
        }

        boolean flag = false;

        for (StatusEffectInstance statuseffectinstance : potioncontentscomponent.getEffects()) {
            StatusEffectCategory statuseffectcategory = statuseffectinstance.getEffectType().value().getCategory();
            if (statuseffectcategory == StatusEffectCategory.HARMFUL) {
                return false;
            }

            if (statuseffectcategory == StatusEffectCategory.BENEFICIAL) {
                if (mc.player.hasStatusEffect(statuseffectinstance.getEffectType())) {
                    return false;
                }

                flag = true;
            }
        }

        return flag;
    }
}
