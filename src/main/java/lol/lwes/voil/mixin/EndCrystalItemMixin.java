package lol.lwes.voil.mixin;

import lol.lwes.voil.hook.EndCrystalItemHooks;
import net.minecraft.item.EndCrystalItem;
import net.minecraft.item.ItemUsageContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EndCrystalItem.class)
public abstract class EndCrystalItemMixin {
    @Inject(method = "useOnBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemUsageContext;getWorld()Lnet/minecraft/world/World;"))
    private void onUse(ItemUsageContext itemusagecontext, CallbackInfoReturnable callbackinforeturnable) {
        EndCrystalItemHooks.onUseOnBlock(this, itemusagecontext, callbackinforeturnable);
    }
}
