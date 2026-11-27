package lol.lwes.voil.module.combat;

import java.util.ArrayList;
import lol.lwes.voil.mixin.accessor.HandledScreenAccessor;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.ModeSetting;
import lol.lwes.voil.setting.MultiSelectSetting;
import lol.lwes.voil.setting.NumberSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.inventory.InventoryUtil;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

public class AutoRefill extends Module {
    private final NumberSetting swapDelay = new NumberSetting("Swap Delay", 0.0, 10.0, 2.0, 1.0, "Delay for putting potions");
    private static final ModeSetting mode = new ModeSetting("Mode", "Legit", "Mode to use when refilling", new String[]{"Packet", "Legit"});
    private final MultiSelectSetting potionsToRefill;
    private final NumberSetting strengthSlot;
    private final NumberSetting regenSlot;
    private final NumberSetting speedSlot;
    private final NumberSetting fireResSlot;
    private final NumberSetting harmingSlot;
    private final NumberSetting slownessSlot;
    private final NumberSetting poisonSlot;
    private final NumberSetting jumpSlot;
    private final NumberSetting weaknessSlot;
    private int refilledCount;

    public AutoRefill() {
        super("AutoRefill", "Automatically refills your inventory", Category.Combat);
        String[] astring = new String[]{
            "Health", "Strength", "Regeneration", "Swiftness", "Fire Resistance", "Harming", "Slowness", "Poison", "JumpBoost", "Weakness"
        };
        this.potionsToRefill = new MultiSelectSetting("Potions to Refill", "Refill Potions", astring);
        this.strengthSlot = new NumberSetting("Strength Slot", 1.0, 9.0, 1.0, 1.0, "Slot for Strength Potion");
        this.regenSlot = new NumberSetting("Regen Slot", 1.0, 9.0, 2.0, 1.0, "Slot for Regeneration Potion");
        this.speedSlot = new NumberSetting("Speed Slot", 1.0, 9.0, 3.0, 1.0, "Slot for Swiftness Potion");
        this.fireResSlot = new NumberSetting("FireRes Slot", 1.0, 9.0, 4.0, 1.0, "Slot for Fire Resistance Potion");
        this.harmingSlot = new NumberSetting("Harming Slot", 1.0, 9.0, 5.0, 1.0, "Slot for Harming Potion");
        this.slownessSlot = new NumberSetting("Slowness Slot", 1.0, 9.0, 6.0, 1.0, "Slot for Slowness Potion");
        this.poisonSlot = new NumberSetting("Poison Slot", 1.0, 9.0, 7.0, 1.0, "Slot for Poison Potion");
        this.jumpSlot = new NumberSetting("Jump Slot", 1.0, 9.0, 8.0, 1.0, "Slot for Jump Boost Potion");
        this.weaknessSlot = new NumberSetting("Weakness Slot", 1.0, 9.0, 9.0, 1.0, "Slot for Weakness Potion");
        Setting[] asetting = new Setting[]{
            mode,
            this.swapDelay,
            this.strengthSlot,
            this.regenSlot,
            this.speedSlot,
            this.fireResSlot,
            this.harmingSlot,
            this.slownessSlot,
            this.poisonSlot,
            this.jumpSlot,
            this.weaknessSlot,
            this.potionsToRefill
        };
        this.addSettings(asetting);
        Setting.dependSetting(this.strengthSlot, "Strength", this.potionsToRefill);
        Setting.dependSetting(this.regenSlot, "Regeneration", this.potionsToRefill);
        Setting.dependSetting(this.speedSlot, "Swiftness", this.potionsToRefill);
        Setting.dependSetting(this.fireResSlot, "Fire Resistance", this.potionsToRefill);
        Setting.dependSetting(this.harmingSlot, "Harming", this.potionsToRefill);
        Setting.dependSetting(this.slownessSlot, "Slowness", this.potionsToRefill);
        Setting.dependSetting(this.poisonSlot, "Poison", this.potionsToRefill);
        Setting.dependSetting(this.jumpSlot, "JumpBoost", this.potionsToRefill);
        Setting.dependSetting(this.weaknessSlot, "Weakness", this.potionsToRefill);
    }

    private int findEmptyHotbarSlot(PlayerInventory playerinventory) {
        for (int i = 0; i <= 8; i++) {
            if (playerinventory.getStack(i).isEmpty()) {
                return i;
            }
        }

        return -1;
    }

    @Override
    public void onTick() {
        if (mc.currentScreen instanceof InventoryScreen inventoryscreen) {
            PlayerInventory playerinventory = mc.player.getInventory();
            if (mode.is("Packet")) {
                StatusEffect[] astatuseffect = new StatusEffect[]{
                    StatusEffects.STRENGTH.value(),
                    StatusEffects.REGENERATION.value(),
                    StatusEffects.SPEED.value(),
                    StatusEffects.FIRE_RESISTANCE.value(),
                    StatusEffects.INSTANT_DAMAGE.value(),
                    StatusEffects.SLOWNESS.value(),
                    StatusEffects.POISON.value(),
                    StatusEffects.JUMP_BOOST.value(),
                    StatusEffects.WEAKNESS.value()
                };
                ArrayList arraylist = new ArrayList();

                for (StatusEffect statuseffect : astatuseffect) {
                    int j = this.getTargetAmount(statuseffect);
                    if (j != -1) {
                        arraylist.add(j);
                    }
                }

                for (int i = 0; i < playerinventory.size(); i++) {
                    if (!InventoryUtil.isArmorSlot(i) && !InventoryUtil.isHotbarSlot(i)) {
                        ItemStack itemstack1 = playerinventory.getStack(i);
                        if (!itemstack1.isEmpty()) {
                            Object object = StatusEffects.INSTANT_HEALTH.value();
                            StatusEffect statuseffect2 = (StatusEffect)object;
                            if (InventoryUtil.isSplashPotionWith(statuseffect2, itemstack1)) {
                                for (int j1 = 0; j1 < 9; j1++) {
                                    if (!arraylist.contains(j1) && playerinventory.getStack(j1).isEmpty()) {
                                        mc.interactionManager.clickSlot(inventoryscreen.getScreenHandler().syncId, i, j1, SlotActionType.SWAP, mc.player);
                                        break;
                                    }
                                }
                            } else {
                                for (StatusEffect statuseffect1 : astatuseffect) {
                                    if (InventoryUtil.isSplashPotionWith(statuseffect1, itemstack1)) {
                                        int k = this.getTargetAmount(statuseffect1);
                                        if (k != -1 && playerinventory.getStack(k).isEmpty()) {
                                            mc.interactionManager.clickSlot(inventoryscreen.getScreenHandler().syncId, i, k, SlotActionType.SWAP, mc.player);
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (mode.is("Legit")) {
                Slot slot = ((HandledScreenAccessor)inventoryscreen).getFocusedSlot();
                if (slot == null) {
                    return;
                }

                int l = slot.getIndex();
                if (InventoryUtil.isArmorSlot(l)) {
                    return;
                }

                int i1 = slot.getIndex();
                if (InventoryUtil.isHotbarSlot(i1)) {
                    return;
                }

                ItemStack itemstack = slot.getStack();
                StatusEffect[] astatuseffect1 = new StatusEffect[]{
                    StatusEffects.STRENGTH.value(),
                    StatusEffects.REGENERATION.value(),
                    StatusEffects.SPEED.value(),
                    StatusEffects.FIRE_RESISTANCE.value(),
                    StatusEffects.INSTANT_DAMAGE.value(),
                    StatusEffects.SLOWNESS.value(),
                    StatusEffects.POISON.value(),
                    StatusEffects.JUMP_BOOST.value(),
                    StatusEffects.WEAKNESS.value()
                };
                Object object1 = StatusEffects.INSTANT_HEALTH.value();
                StatusEffect statuseffect3 = (StatusEffect)object1;
                if (InventoryUtil.isSplashPotionWith(statuseffect3, itemstack)) {
                    ArrayList arraylist1 = new ArrayList();

                    for (StatusEffect statuseffect5 : astatuseffect1) {
                        int i2 = this.getTargetAmount(statuseffect5);
                        if (i2 != -1) {
                            arraylist1.add(i2);
                        }
                    }

                    for (int k1 = 0; k1 < 9; k1++) {
                        if (!arraylist1.contains(k1) && mc.player.getInventory().getStack(k1).isEmpty()) {
                            if (this.refilledCount < this.swapDelay.getInt()) {
                                this.refilledCount++;
                                return;
                            }

                            mc.interactionManager.clickSlot(inventoryscreen.getScreenHandler().syncId, slot.getIndex(), k1, SlotActionType.SWAP, mc.player);
                            this.refilledCount = 0;
                        }
                    }
                }

                for (StatusEffect statuseffect4 : astatuseffect1) {
                    if (InventoryUtil.isSplashPotionWith(statuseffect4, itemstack)) {
                        int l1 = this.getTargetAmount(statuseffect4);
                        if (l1 != -1) {
                            if (this.refilledCount < this.swapDelay.getInt()) {
                                this.refilledCount++;
                                return;
                            }

                            mc.interactionManager.clickSlot(inventoryscreen.getScreenHandler().syncId, slot.getIndex(), l1, SlotActionType.SWAP, mc.player);
                            this.refilledCount = 0;
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onEnable() {
        this.refilledCount = 0;
    }

    private int getTargetAmount(StatusEffect statuseffect) {
        if (statuseffect == StatusEffects.STRENGTH && this.potionsToRefill.isSelected("Strength")) {
            int j1 = this.strengthSlot.getInt();
            return j1 - 1;
        } else if (statuseffect == StatusEffects.REGENERATION && this.potionsToRefill.isSelected("Regeneration")) {
            int i1 = this.regenSlot.getInt();
            return i1 - 1;
        } else if (statuseffect == StatusEffects.SPEED && this.potionsToRefill.isSelected("Swiftness")) {
            int l = this.speedSlot.getInt();
            return l - 1;
        } else if (statuseffect == StatusEffects.FIRE_RESISTANCE && this.potionsToRefill.isSelected("Fire Resistance")) {
            return this.fireResSlot.getInt() - 1;
        } else if (statuseffect == StatusEffects.INSTANT_DAMAGE && this.potionsToRefill.isSelected("Harming")) {
            int k = this.harmingSlot.getInt();
            return k - 1;
        } else if (statuseffect == StatusEffects.SLOWNESS && this.potionsToRefill.isSelected("Slowness")) {
            int j = this.slownessSlot.getInt();
            return j - 1;
        } else if (statuseffect == StatusEffects.POISON && this.potionsToRefill.isSelected("Poison")) {
            return this.poisonSlot.getInt() - 1;
        } else if (statuseffect == StatusEffects.JUMP_BOOST && this.potionsToRefill.isSelected("JumpBoost")) {
            return this.jumpSlot.getInt() - 1;
        } else if (statuseffect == StatusEffects.WEAKNESS && this.potionsToRefill.isSelected("Weakness")) {
            int i = this.weaknessSlot.getInt();
            return i - 1;
        } else {
            return -1;
        }
    }
}
