package lol.lwes.voil.module.player;

import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.KeybindSetting;
import lol.lwes.voil.setting.ModeSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.input.KeyUtil;
import lol.lwes.voil.util.inventory.InventoryUtil;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.entity.EquipmentSlot.Type;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;

public class AutoArmor extends Module {
    ModeSetting mode = new ModeSetting("Mode", "onKey", "Automatically equips armor onkey or when inv open", new String[]{"onKey", "onInventory"});
    KeybindSetting activate = new KeybindSetting("Activate", 0, false, "Keybind to use");
    BooleanSetting throwJunk = new BooleanSetting("Throw Junk", true, "To throw junk armor");

    public AutoArmor() {
        super("AutoArmor", "Automatically equips armor", Category.Player);
        Setting[] asetting = new Setting[]{this.mode, this.activate};
        this.addSettings(asetting);
    }

    public boolean isArmorComplete() {
        for (EquipmentSlot equipmentslot : EquipmentSlot.values()) {
            if (equipmentslot.getType() == Type.HUMANOID_ARMOR) {
                mc.player.getEquippedStack(equipmentslot);
            }
        }

        return true;
    }

    @Override
    public void onTick() {
        if (mc.player != null) {
            if (this.isTriggered()) {
                PlayerInventory playerinventory = mc.player.getInventory();
                ItemStack[] aitemstack = new ItemStack[]{
                    mc.player.getEquippedStack(EquipmentSlot.HEAD),
                    mc.player.getEquippedStack(EquipmentSlot.CHEST),
                    mc.player.getEquippedStack(EquipmentSlot.LEGS),
                    mc.player.getEquippedStack(EquipmentSlot.FEET)
                };
                int[] aint = new int[]{36, 37, 38, 39};

                for (int i = 0; i < playerinventory.size(); i++) {
                    ItemStack itemstack = playerinventory.getStack(i);
                    if (itemstack.contains(DataComponentTypes.EQUIPPABLE)) {
                        byte b0 = -1;
                        EquippableComponent equippablecomponent = itemstack.get(DataComponentTypes.EQUIPPABLE);
                        if (equippablecomponent != null) {
                            EquipmentSlot equipmentslot = equippablecomponent.slot();
                            switch (equipmentslot) {
                                case HEAD -> b0 = 3;
                                case CHEST -> b0 = 2;
                                case LEGS -> b0 = 1;
                                case FEET -> b0 = 0;
                                default -> {
                                    continue;
                                }
                            }
                        }

                        int l = 3 - b0;
                        ItemStack itemstack1 = aitemstack[l];
                        if (itemstack1.contains(DataComponentTypes.EQUIPPABLE)) {
                            if (InventoryUtil.isBetterArmor(itemstack1, itemstack, mc.player.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT))) {
                                aitemstack[l] = itemstack;
                                aint[l] = i;
                            }
                        } else {
                            aitemstack[l] = itemstack;
                            aint[l] = i;
                        }
                    }
                }

                for (int k = 0; k < 4; k++) {
                    if (!aitemstack[k].equals(playerinventory.getStack(41 + k))) {
                        InventoryUtil.pickupActionFlagged().decodedSlot(aint[k]).executeWithHotbarTarget(Math.abs(k + -4 + 1));
                    }
                }
            }
        }
    }

    public boolean isTriggered() {
        if (this.mode.getMode().equals("onKey")) {
            int i = this.activate.getKey();
            return KeyUtil.isKeyDown(i);
        } else {
            return this.mode.getMode().equals("onInventory") ? mc.currentScreen instanceof InventoryScreen : false;
        }
    }
}
