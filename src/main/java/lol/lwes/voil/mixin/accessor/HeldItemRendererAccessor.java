package lol.lwes.voil.mixin.accessor;

import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(HeldItemRenderer.class)
public interface HeldItemRendererAccessor {
    @Accessor("equipProgressMainHand")
    float getEquippedProgressMainHand();

    @Accessor("equipProgressMainHand")
    void setEquippedProgressMainHand(float f);

    @Accessor("equipProgressOffHand")
    void setEquippedProgressOffHand(float f);

    @Accessor("equipProgressOffHand")
    float getEquippedProgressOffHand();

    @Accessor("mainHand")
    void setItemStackMainHand(ItemStack itemstack);

    @Accessor("offHand")
    void setItemStackOffHand(ItemStack itemstack);
}
