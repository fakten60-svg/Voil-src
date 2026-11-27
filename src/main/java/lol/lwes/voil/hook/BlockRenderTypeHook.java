package lol.lwes.voil.hook;

import lol.lwes.voil.module.ModuleManager;
import lol.lwes.voil.module.render.NoRender;
import lol.lwes.voil.setting.ItemListSetting;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class BlockRenderTypeHook {
    public static void onGetRenderType(BlockState blockstate, CallbackInfoReturnable callbackinforeturnable) {
        if (ModuleManager.INSTANCE.isEnabled(NoRender.class) && NoRender.hideBlocksEnabled.isEnabled()) {
            if (blockstate.getBlock().asItem() != Items.AIR) {
                ItemListSetting itemlistsetting = NoRender.hiddenBlocks;
                Block block = blockstate.getBlock();
                if (itemlistsetting.containsBlock(block)) {
                    callbackinforeturnable.setReturnValue(BlockRenderType.INVISIBLE);
                }
            }
        }
    }
}
