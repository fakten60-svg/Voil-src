package lol.lwes.voil.event;

import net.minecraft.block.Block;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

public class BlockInteractEvent extends Event {
    public Block block;
    public Hand hand;
    public BlockPos pos;
    public double hitX;
    public double hitY;
    public double hitZ;

    public BlockInteractEvent(Block block, Hand hand, BlockPos blockpos, double d0, double d1, double d2) {
        this.block = block;
        this.hand = hand;
        this.pos = blockpos;
        this.hitX = d0;
        this.hitY = d1;
        this.hitZ = d2;
    }

    public Block getBlock() {
        return this.block;
    }

    public double getZ() {
        return this.hitZ;
    }

    public double getX() {
        return this.hitX;
    }

    public double getY() {
        return this.hitY;
    }
}
