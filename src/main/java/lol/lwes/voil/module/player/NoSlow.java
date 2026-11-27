package lol.lwes.voil.module.player;

import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.Setting;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class NoSlow extends Module {
    public BooleanSetting items = new BooleanSetting("Items", true, "Makes you not slow down while using items");
    public final BooleanSetting sneaking = new BooleanSetting("Sneaking", false, "Makes you not slow down while sneaking");
    public BooleanSetting webs = new BooleanSetting("Webs", false, "Makes you not slow down while in webs");

    public NoSlow() {
        super("NoSlow", "Makes you not slow down", Category.Player);
        Setting[] asetting = new Setting[]{this.items, this.sneaking, this.webs};
        this.addSettings(asetting);
    }

    @Override
    public void onTick() {
        if (this.webs.isEnabled() && isBlockInBox(mc.player.getBoundingBox(), Blocks.COBWEB)) {
            mc.player.slowMovement(mc.world.getBlockState(mc.player.getBlockPos()), new Vec3d(2.35, 1.75, 2.35));
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    public static boolean isBlockInBox(Box box, Block block) {
        for (int i = (int)Math.floor(box.minX); i < Math.ceil(box.maxX); i++) {
            for (int j = (int)Math.floor(box.minY); j < Math.ceil(box.maxY); j++) {
                for (int k = (int)Math.floor(box.minZ); k < Math.ceil(box.maxZ); k++) {
                    if (mc.world.getBlockState(new BlockPos(i, j, k)).getBlock() == block) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
