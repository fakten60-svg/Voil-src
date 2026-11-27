package lol.lwes.voil.duck;

import net.minecraft.item.ItemStack;

public interface ItemUseTicksAccessor {
    int voil$getRemainingTicks(ItemStack itemstack);

    int voil$getTotalTicks(ItemStack itemstack);
}
