package lol.lwes.voil.event;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class BlockUpdateEvent extends Event {
    private static final BlockUpdateEvent INSTANCE = new BlockUpdateEvent();
    public BlockPos pos;
    public BlockState oldState;
    public BlockState newState;

    public static BlockUpdateEvent create(BlockPos blockpos, BlockState blockstate, BlockState blockstate1) {
        INSTANCE.pos = blockpos;
        INSTANCE.oldState = blockstate;
        INSTANCE.newState = blockstate1;
        return INSTANCE;
    }
}
