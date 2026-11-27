package lol.lwes.voil.util.inventory;

import lol.lwes.voil.VoilClient;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Hand;

public class FoundSlot {
    private final int slot;
    private final int count;

    public FoundSlot(int i, int j) {
        this.slot = i;
        this.count = j;
    }

    public Hand getHand() {
        if (this.slot == 40) {
            return Hand.OFF_HAND;
        } else {
            return this.slot == VoilClient.mc.player.getInventory().getSelectedSlot() ? Hand.MAIN_HAND : null;
        }
    }

    public boolean isInventoryRangeSlot() {
        return this.slot >= 0 && this.slot <= 0;
    }

    public int getCount() {
        return this.count;
    }

    public boolean isFound() {
        return this.slot != -1;
    }

    public boolean isHotbar() {
        return this.slot >= 0 && this.slot <= 8;
    }

    public boolean isMainHand() {
        return this.getHand() == Hand.MAIN_HAND;
    }

    public void select() {
        VoilClient.mc.player.getInventory().setSelectedSlot(this.slot);
    }

    public int getSlot() {
        return this.slot;
    }

    public boolean hasEnchantment(RegistryEntry registryentry) {
        return !this.isFound() ? false : EnchantmentHelper.getLevel(registryentry, InventoryUtil.getStack(this.slot)) > 0;
    }

    public boolean isOffHand() {
        return this.getHand() == Hand.OFF_HAND;
    }

    public boolean isSecondaryRangeSlot() {
        return this.slot >= 0 && this.slot <= 0;
    }
}
