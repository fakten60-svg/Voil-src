package lol.lwes.voil.module.combat;

import dev.prestigeclient.renderlib.impl.api.RenderAPI;
import java.awt.Color;
import java.util.HashSet;
import java.util.Set;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.event.BlockInteractEvent;
import lol.lwes.voil.event.EventHandler;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.KeybindSetting;
import lol.lwes.voil.setting.ModeSetting;
import lol.lwes.voil.setting.MultiSelectSetting;
import lol.lwes.voil.setting.NumberSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.AimMathUtil;
import lol.lwes.voil.util.CombatUtil;
import lol.lwes.voil.util.RaycastUtil;
import lol.lwes.voil.util.inventory.InventoryUtil;
import lol.lwes.voil.util.render.Render3DUtil;
import lol.lwes.voil.util.rotation.RotationManager;
import lol.lwes.voil.util.rotation.RotationPriority;
import lol.lwes.voil.util.world.BlockUtil;
import lol.lwes.voil.util.world.InteractionUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext.FluidHandling;
import org.joml.Matrix4f;

public class AutoDrain extends Module {
    private final NumberSetting delay = new NumberSetting("Delay", 1.0, 10.0, 1.0, 1.0, "Delay for the drain");
    private final NumberSetting range = new NumberSetting("Range", 1.0, 3.0, 3.0, 0.1, "Range to detect liquid");
    private final BooleanSetting lava = new BooleanSetting("Lava", true, "Drains lava too");
    private final BooleanSetting ignoreOwnLiquid = new BooleanSetting("Ignore own liquid", true, "Ignore your own liquid sources");
    private final BooleanSetting mousesimulation = new BooleanSetting("Mousesimulation", true, "Simulate clicks while using");
    private final BooleanSetting onKey = new BooleanSetting("On key", true, "Drain on key press");
    private final ModeSetting rotations = new ModeSetting(
        "Rotations", "Manual", "Manual mode only picks up when you look at the block silent auto picks up", new String[]{"Silent", "Manual"}
    );
    private final KeybindSetting key = new KeybindSetting("Key", 0, false, "Key to activate");
    private final BooleanSetting showTargetedBlock = new BooleanSetting("Show Targeted Block", true, "Shows the block you are targeting to drain");
    private final MultiSelectSetting drainItem;
    private int placedCount;
    private final Set placedLiquids;
    private BlockPos targetPos;

    public AutoDrain() {
        super("AutoDrain", "Drains enemies liquid", Category.Combat);
        Set set = Set.of("Bucket");
        this.drainItem = MultiSelectSetting.create("Drain Item", "", set, new String[]{"Bucket", "Cobweb", "Block"});
        this.placedCount = 0;
        this.placedLiquids = new HashSet();
        this.targetPos = null;
        Setting[] asetting = new Setting[]{
            this.delay,
            this.range,
            this.lava,
            this.ignoreOwnLiquid,
            this.showTargetedBlock,
            this.mousesimulation,
            this.onKey,
            this.rotations,
            this.key,
            this.drainItem
        };
        this.addSettings(asetting);
        Setting.dependSetting(this.key, this.onKey);
        ModeSetting modesetting = this.rotations;
        Setting.dependSetting(this.range, "Silent", modesetting);
    }

    private Item getDrainItem() {
        if (this.drainItem.isSelected("Bucket")) {
            int i = InventoryUtil.findHotbarSlot(Items.BUCKET);
            if (i != -1) {
                return Items.BUCKET;
            }
        }

        if (this.drainItem.isSelected("Cobweb")) {
            int j = InventoryUtil.findHotbarSlot(Items.COBWEB);
            if (j != -1) {
                return Items.COBWEB;
            }
        }

        if (this.drainItem.isSelected("Block")) {
            for (ItemStack itemstack : InventoryUtil.getHotbarStacks()) {
                if (!itemstack.isEmpty() && InventoryUtil.isBlockItem(itemstack.getItem()) && itemstack.getItem() != Items.COBWEB) {
                    return itemstack.getItem();
                }
            }
        }

        return null;
    }

    @Override
    public void onTick() {
        if (!Module.isNotInGame()) {
            if (!this.onKey.isEnabled() || this.key.isPressed()) {
                Item item = this.getDrainItem();
                if (item != null) {
                    if (this.rotations.is("Silent")) {
                        BlockPos blockpos = null;
                        double d0 = Double.MAX_VALUE;
                        int i = (int)this.range.getValue();
                        BlockPos blockpos1 = mc.player.getBlockPos();

                        for (int j = -i; j <= i; j++) {
                            for (int k = -i; k <= i; k++) {
                                for (int l = -i; l <= i; l++) {
                                    BlockPos blockpos2 = blockpos1.add(j, k + 1, l);
                                    double d1 = mc.player.squaredDistanceTo(blockpos2.getX() + 0.5, blockpos2.getY() + 0.5, blockpos2.getZ() + 0.5);
                                    if (!(d1 > this.range.getValue() * this.range.getValue())) {
                                        BlockState blockstate1 = mc.world.getBlockState(blockpos2);
                                        if (this.isDrainable(blockstate1)
                                            && (!this.ignoreOwnLiquid.isEnabled() || !this.placedLiquids.contains(blockpos2))
                                            && d1 < d0) {
                                            d0 = d1;
                                            blockpos = blockpos2;
                                        }
                                    }
                                }
                            }
                        }

                        if (blockpos != null) {
                            if (mc.world.getBlockState(blockpos.down()).getBlock() == Blocks.WATER && BlockUtil.isExposed(blockpos)) {
                                this.targetPos = BlockUtil.findAdjacentSolid(blockpos);
                            } else {
                                this.targetPos = blockpos.down();
                            }

                            int i1 = this.placedCount++;
                            if (this.placedCount < this.delay.getValue()) {
                                return;
                            }

                            this.placedCount = 0;
                            int l1 = InventoryUtil.findHotbarSlot(item);
                            if (l1 != -1 && mc.player.getMainHandStack().getItem() != item) {
                                InventoryUtil.selectSlot(l1);
                            } else if (mc.player.getMainHandStack().getItem() != item) {
                                return;
                            }

                            RotationManager rotationmanager = VoilClient.getRotationManager();
                            Vec3d vec3d1 = this.targetPos.toCenterPos();
                            Vec3d vec3d2 = AimMathUtil.addRandomJitter(vec3d1);
                            RotationPriority rotationpriority = RotationPriority.MEDIUM;
                            Vec3d vec3d = vec3d2;
                            rotationmanager.lookAtPosition(vec3d, rotationpriority);
                            if (item == Items.BUCKET) {
                                InteractionUtil.useItem(Hand.MAIN_HAND);
                            } else {
                                InteractionUtil.clickBlock(this.targetPos, Hand.MAIN_HAND, true);
                            }

                            this.placedLiquids.remove(blockpos);
                            this.targetPos = null;
                        } else {
                            this.placedCount = 0;
                            this.targetPos = null;
                        }
                    } else {
                        double d2 = CombatUtil.getBlockReach();
                        BlockHitResult blockhitresult = RaycastUtil.raycastBlock(d2, FluidHandling.SOURCE_ONLY);
                        if (blockhitresult == null || blockhitresult.getType() == Type.MISS) {
                            this.placedCount = 0;
                            return;
                        }

                        BlockPos blockpos5 = blockhitresult.getBlockPos();
                        BlockState blockstate = mc.world.getBlockState(blockpos5);
                        if (this.isDrainable(blockstate) && (!this.ignoreOwnLiquid.isEnabled() || !this.placedLiquids.contains(blockpos5))) {
                            this.placedCount++;
                            if (this.placedCount < this.delay.getValue()) {
                                return;
                            }

                            this.placedCount = 0;
                            if (mc.player.getMainHandStack().getItem() != item) {
                                int k1 = InventoryUtil.findHotbarSlot(item);
                                if (k1 == -1) {
                                    return;
                                }

                                InventoryUtil.selectSlot(k1);
                            }

                            if (mc.world.getBlockState(blockpos5.down()).getBlock() == Blocks.WATER && BlockUtil.isExposed(blockpos5)) {
                                this.targetPos = BlockUtil.findAdjacentSolid(blockpos5);
                            } else {
                                this.targetPos = blockpos5.down();
                            }

                            if (item == Items.BUCKET) {
                                InteractionUtil.useItem(Hand.MAIN_HAND);
                            } else {
                                InteractionUtil.clickBlock(this.targetPos, Hand.MAIN_HAND, true);
                            }

                            this.placedLiquids.remove(this.targetPos);
                        } else {
                            this.placedCount = 0;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onDisable() {
        this.placedCount = 0;
        this.targetPos = null;
    }

    @Override
    public void onEnable() {
        this.placedCount = 0;
    }

    private boolean isDrainable(BlockState blockstate) {
        if (blockstate.getBlock() == Blocks.WATER) {
            return blockstate.getFluidState().isStill();
        } else {
            return this.lava.isEnabled() && blockstate.getBlock() == Blocks.LAVA ? blockstate.getFluidState().isStill() : false;
        }
    }

    @EventHandler
    public void onItemUse(BlockInteractEvent blockinteractevent) {
        if (blockinteractevent.hand == Hand.MAIN_HAND
            && (mc.player.getMainHandStack().getItem() == Items.WATER_BUCKET || mc.player.getMainHandStack().getItem() == Items.LAVA_BUCKET)) {
            this.placedLiquids.add(blockinteractevent.pos.add(0, 1, 0));
        }
    }

    @Override
    public void onWorldRender(RenderAPI.RenderContext rendercontext) {
        if (this.targetPos != null && this.showTargetedBlock.isEnabled()) {
            Box box = new Box(this.targetPos);
            Render3DUtil.drawBoxFilled(new Matrix4f(), box, new Color(255, 0, 0, 100));
        }
    }
}
