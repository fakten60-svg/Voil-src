package lol.lwes.voil.module.movement;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class ScaffoldTarget {
    public final BlockPos pos;
    public final Direction side;

    public ScaffoldTarget(BlockPos blockpos, Direction direction) {
        this.pos = blockpos;
        this.side = direction;
    }
}
