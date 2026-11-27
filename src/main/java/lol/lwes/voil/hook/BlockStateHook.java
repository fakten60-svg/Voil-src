package lol.lwes.voil.hook;

import lol.lwes.voil.VoilClient;
import lol.lwes.voil.event.BlockUpdateEvent;
import lol.lwes.voil.event.EventBus;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public abstract class BlockStateHook {
    public static void onSetBlockState(World world, BlockPos blockpos, BlockState blockstate, CallbackInfoReturnable callbackinforeturnable) {
        if (world.isClient()) {
            EventBus eventbus = VoilClient.eventBus;
            BlockUpdateEvent blockupdateevent = BlockUpdateEvent.create(blockpos, (BlockState)callbackinforeturnable.getReturnValue(), blockstate);
            eventbus.post(blockupdateevent);
        }
    }
}
