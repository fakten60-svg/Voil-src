package lol.lwes.voil.module.player;

import java.util.function.Supplier;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.KeybindSetting;
import lol.lwes.voil.setting.ModeSetting;
import lol.lwes.voil.setting.NumberSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.inventory.FoundSlot;
import lol.lwes.voil.util.inventory.InventoryUtil;
import lol.lwes.voil.util.prediction.DefaultMotionPredictor;
import lol.lwes.voil.util.prediction.MotionPredictor;
import lol.lwes.voil.util.prediction.MotionRequest;
import lol.lwes.voil.util.prediction.MotionResult;
import lol.lwes.voil.util.render.EasingFunction;
import lol.lwes.voil.util.render.UiEasings;
import lol.lwes.voil.util.rotation.Rotation;
import lol.lwes.voil.util.rotation.RotationAxisMode;
import lol.lwes.voil.util.rotation.SmoothRotationController;
import lol.lwes.voil.util.rotation.SmoothRotationState;
import lol.lwes.voil.util.world.BlockUtil;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class Clutch extends Module {
    private final ModeSetting rotations = new ModeSetting("Rotations", "Silent", "How to rotate", new String[]{"Silent", "Legit", "Looking"});
    private final NumberSetting minFall = new NumberSetting("Min Fall", 3.0, 30.0, 4.0, 1.0, "Minimum predicted fall height in blocks before clutching");
    private final NumberSetting activateTicks = new NumberSetting("Activate Ticks", 1.0, 20.0, 5.0, 1.0, "Ticks before predicted impact to place");
    private final BooleanSetting pickupWater = new BooleanSetting("Pickup Water", true, "Picks the water back up after landing");
    private final BooleanSetting switchBack = new BooleanSetting("Switch Back", true, "Switches back to the previous slot after clutching");
    private final BooleanSetting rotateBack = new BooleanSetting("Rotate Back", true, "Rotates back after clutching (legit mode)");
    private final BooleanSetting onKey = new BooleanSetting("On Key", false, "Only clutch when holding the keybind");
    private final KeybindSetting keybind = new KeybindSetting("Keybind", 0, false, "Keybind to clutch on");
    private final MotionPredictor fallPredictor = DefaultMotionPredictor.create();
    private final Object lock = new Object();
    private boolean clutching = false;
    private boolean rotated = false;
    private boolean waterPlaced = false;
    private int previousSlot = -1;
    private float previousYaw;
    private float previousPitch;
    private int tickCounter = 0;
    private Vec3d predictedImpact = null;
    private double predictedFall = 0.0;
    private SmoothRotationState rotationHandle = SmoothRotationState.createEnded();

    public Clutch() {
        super("Clutch", "Automatically water clutches falls using movement prediction", Category.Player);
        Setting[] asetting = new Setting[]{
            this.rotations, this.onKey, this.keybind, this.minFall, this.activateTicks, this.pickupWater, this.switchBack, this.rotateBack
        };
        this.addSettings(asetting);
        Setting.dependSetting(this.keybind, this.onKey);
    }

    private BlockPos getImpactPos() {
        return BlockPos.ofFloored(this.predictedImpact.x, this.predictedImpact.y - 0.01, this.predictedImpact.z);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) {
            this.finish(false);
        } else if (this.clutching) {
            int k = this.tickCounter;
            Clutch clutch3 = this;
            clutch3.tickCounter = k + 1;
            if (k > 60) {
                this.finish(true);
            } else {
                boolean flag2 = mc.player.isOnGround() || mc.player.isTouchingWater() || mc.player.hasVehicle();
                if (flag2) {
                    if (this.pickupWater.isEnabled()) {
                        FoundSlot foundslot1 = InventoryUtil.find(new Item[]{Items.BUCKET});
                        int i = -1;
                        if (foundslot1.isFound()) {
                            i = foundslot1.getSlot();
                        }

                        BlockPos blockpos2 = BlockPos.ofFloored(mc.player.getEntityPos());
                        if (i != -1 && BlockUtil.isBlockAt(Blocks.WATER, blockpos2)) {
                            if (!this.waterPlaced) {
                                this.waterPlaced = true;
                                VoilClient.getRotationController().release(this);
                                if (this.rotations.is("Looking")) {
                                    return;
                                }

                                SmoothRotationController smoothrotationcontroller = VoilClient.getRotationController();
                                Supplier supplier6 = SmoothRotationController.smoothForTicks(8.0);
                                Supplier supplier7 = () -> {
                                    Vec3d vec3d = Vec3d.ofCenter(blockpos2);
                                    return Rotation.toPoint(vec3d);
                                };
                                Supplier supplier8 = () -> false;
                                boolean flag = this.rotations.is("Silent");
                                Supplier supplier = supplier8;
                                Supplier supplier1 = supplier7;
                                Supplier supplier2 = supplier6;
                                Clutch clutch1 = this;
                                this.rotationHandle = smoothrotationcontroller.start(clutch1, supplier2, supplier1, supplier, flag);
                                return;
                            }

                            if (!this.isAimedAt(blockpos2)) {
                                return;
                            }

                            InventoryUtil.selectSlot(i);
                            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                            mc.player.swingHand(Hand.MAIN_HAND);
                        }
                    }

                    this.finish(true);
                }
            }
        } else if (!mc.player.isOnGround()
            && !mc.player.isTouchingWater()
            && !mc.player.hasVehicle()
            && !mc.player.getAbilities().flying
            && !mc.player.isGliding()
            && !(mc.player.getVelocity().y > -0.1)) {
            if (this.onKey.isEnabled() && !this.keybind.isPressed()) {
                if (this.rotated) {
                    this.finish(true);
                }
            } else {
                MotionPredictor motionpredictor = this.fallPredictor;
                MotionRequest motionrequest = new MotionRequest(mc.player, 100);
                MotionResult motionresult = motionpredictor.predict(motionrequest);
                if (motionresult.isCollided()) {
                    double d0 = mc.player.fallDistance + Math.max(0.0, mc.player.getY() - motionresult.getVelocity().y);
                    if (d0 < this.minFall.getValue()) {
                        if (this.rotated) {
                            this.finish(true);
                        }
                    } else {
                        this.predictedImpact = motionresult.getVelocity();
                        this.predictedFall = motionresult.getHitCount();
                        if (!BlockUtil.isBlockAt(Blocks.WATER, this.getImpactPos())) {
                            Block block1 = Blocks.WATER;
                            BlockPos blockpos = BlockPos.ofFloored(this.predictedImpact);
                            Block block = block1;
                            if (!BlockUtil.isBlockAt(block, blockpos)) {
                                Item[] aitem = new Item[]{Items.WATER_BUCKET};
                                FoundSlot foundslot = InventoryUtil.find(aitem);
                                int j = -1;
                                if (foundslot.isFound()) {
                                    j = foundslot.getSlot();
                                }

                                if (j == -1) {
                                    return;
                                }

                                if (!this.rotated || this.rotationHandle.hasEnded()) {
                                    this.previousYaw = mc.player.getYaw();
                                    this.previousPitch = mc.player.getPitch();
                                    this.rotated = true;
                                    if (this.rotations.is("Looking")) {
                                        return;
                                    }

                                    SmoothRotationController smoothrotationcontroller1 = VoilClient.getRotationController();
                                    Supplier supplier9 = () -> Math.max(0.1, this.predictedFall - this.activateTicks.getValue());
                                    Supplier supplier10 = () -> {
                                        Rotation rotation;
                                        if (this.predictedImpact == null) {
                                            rotation = null;
                                        } else {
                                            Vec3d vec3d = this.predictedImpact.add(0.0, -0.3, 0.0);
                                            rotation = Rotation.toPoint(vec3d);
                                        }

                                        return rotation;
                                    };
                                    Supplier supplier11 = () -> false;
                                    boolean flag3 = this.rotations.is("Silent");
                                    RotationAxisMode rotationaxismode1 = RotationAxisMode.YawAndPitch;
                                    EasingFunction easingfunction = UiEasings::easeOutCubic;
                                    RotationAxisMode rotationaxismode = rotationaxismode1;
                                    Object object = null;
                                    boolean flag1 = flag3;
                                    Supplier supplier3 = supplier11;
                                    Supplier supplier4 = supplier10;
                                    Supplier supplier5 = supplier9;
                                    Clutch clutch2 = this;
                                    this.rotationHandle = smoothrotationcontroller1.startWith(
                                        clutch2, supplier5, supplier4, supplier3, flag1, false, (SmoothRotationState)object, rotationaxismode, easingfunction
                                    );
                                }

                                if (this.predictedFall > this.activateTicks.getInt()) {
                                    return;
                                }

                                if (mc.player.getEyePos().distanceTo(this.predictedImpact) > 5.0) {
                                    return;
                                }

                                BlockPos blockpos1 = this.getImpactPos();
                                if (!this.isAimedAt(blockpos1)) {
                                    return;
                                }

                                if (this.previousSlot == -1) {
                                    this.previousSlot = mc.player.getInventory().getSelectedSlot();
                                }

                                InventoryUtil.selectSlot(j);
                                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                                mc.player.swingHand(Hand.MAIN_HAND);
                                this.clutching = true;
                                this.tickCounter = 0;
                                return;
                            }
                        }

                        if (this.rotated) {
                            this.finish(true);
                        }
                    }
                }
            }
        } else {
            if (this.rotated) {
                this.finish(true);
            }
        }
    }

    @Override
    public void onDisable() {
        this.finish(true);
        super.onDisable();
    }

    private boolean isAimedAt(BlockPos blockpos) {
        if (this.rotations.is("Looking")) {
            return mc.crosshairTarget instanceof BlockHitResult blockhitresult && mc.crosshairTarget.getType() == Type.BLOCK
                ? blockhitresult.getBlockPos().isWithinDistance(blockpos, 2.5)
                : false;
        } else {
            return this.rotationHandle.isOnTarget();
        }
    }

    private void finish(boolean flag) {
        this.rotationHandle.markDone();
        VoilClient.getRotationController().release(this);
        if (flag && mc.player != null) {
            if (this.switchBack.isEnabled() && this.previousSlot != -1) {
                int i = this.previousSlot;
                InventoryUtil.selectSlot(i);
            }

            if (this.rotateBack.isEnabled() && this.rotated && this.rotations.is("Legit")) {
                float f = this.previousYaw;
                float f1 = this.previousPitch;
                SmoothRotationController smoothrotationcontroller = VoilClient.getRotationController();
                Object object1 = this.lock;
                Supplier supplier2 = SmoothRotationController.smoothForTicks(5.0);
                Supplier supplier = () -> new Rotation(f, f1);
                Supplier supplier1 = supplier2;
                Object object = object1;
                smoothrotationcontroller.start(object, supplier1, supplier, null, false);
            }
        }

        this.clutching = false;
        this.rotated = false;
        this.waterPlaced = false;
        this.previousSlot = -1;
        this.tickCounter = 0;
        this.predictedImpact = null;
        this.predictedFall = 0.0;
    }
}
