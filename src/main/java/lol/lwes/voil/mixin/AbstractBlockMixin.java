package lol.lwes.voil.mixin;

import lol.lwes.voil.hook.BlockRenderTypeHook;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.class)
public abstract class AbstractBlockMixin {
    @Inject(method = "getRenderType", at = @At("HEAD"), cancellable = true)
    private void hideBlock(BlockState blockstate, CallbackInfoReturnable callbackinforeturnable) {
        BlockRenderTypeHook.onGetRenderType(blockstate, callbackinforeturnable);
    }
}
