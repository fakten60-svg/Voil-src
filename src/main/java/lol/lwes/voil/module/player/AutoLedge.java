package lol.lwes.voil.module.player;

import lol.lwes.voil.event.AttackEntityPostEvent;
import lol.lwes.voil.event.EventHandler;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.util.world.BlockUtil;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class AutoLedge extends Module {
    private boolean placing = false;
    private PlayerEntity target;

    public AutoLedge() {
        super("AutoLedge", "Automatically puts people on ledges", Category.Player);
    }

    @Override
    public void onTick() {
        if (mc.player != null) {
            if (this.placing) {
                Item item = Items.OBSIDIAN;
                BlockPos blockpos = this.target.getBlockPos();
                BlockUtil.canPlaceItemAt(item, blockpos);
                if (BlockUtil.isBlockAt(Blocks.OBSIDIAN, this.target.getBlockPos())) {
                    this.placing = false;
                }
            }
        }
    }

    private boolean hasSolidBlockBehind(PlayerEntity playerentity) {
        if (playerentity != null && mc.world != null) {
            Direction direction = playerentity.getHorizontalFacing().getOpposite();
            BlockPos blockpos = playerentity.getBlockPos().offset(direction);
            return mc.world.getBlockState(blockpos).isSolidBlock(mc.world, blockpos);
        } else {
            return false;
        }
    }

    @EventHandler
    public void onAttack(AttackEntityPostEvent attackentitypostevent) {
        if (attackentitypostevent.getTarget() instanceof PlayerEntity) {
            this.target = (PlayerEntity)attackentitypostevent.getTarget();
            PlayerEntity playerentity = this.target;
            if (this.hasSolidBlockBehind(playerentity)) {
                this.placing = true;
            }
        }
    }
}
