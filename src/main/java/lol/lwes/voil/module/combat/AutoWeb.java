package lol.lwes.voil.module.combat;

import dev.prestigeclient.renderlib.impl.api.RenderAPI;
import java.awt.Color;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.event.EventHandler;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.ColorSetting;
import lol.lwes.voil.setting.KeybindSetting;
import lol.lwes.voil.setting.ModeSetting;
import lol.lwes.voil.setting.NumberSetting;
import lol.lwes.voil.setting.RangeSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.PlayerTargetUtil;
import lol.lwes.voil.util.RaycastUtil;
import lol.lwes.voil.util.combat.SpinAttackController;
import lol.lwes.voil.util.inventory.FoundSlot;
import lol.lwes.voil.util.inventory.InventoryUtil;
import lol.lwes.voil.util.prediction.DefaultKnockbackPredictor;
import lol.lwes.voil.util.prediction.DefaultMotionPredictor;
import lol.lwes.voil.util.prediction.KnockbackPredictor;
import lol.lwes.voil.util.prediction.KnockbackRequest;
import lol.lwes.voil.util.prediction.KnockbackResult;
import lol.lwes.voil.util.prediction.MotionRequest;
import lol.lwes.voil.util.render.EasingFunction;
import lol.lwes.voil.util.render.Render3DUtil;
import lol.lwes.voil.util.render.UiEasings;
import lol.lwes.voil.util.rotation.Rotation;
import lol.lwes.voil.util.rotation.RotationAxisMode;
import lol.lwes.voil.util.rotation.SmoothRotationController;
import lol.lwes.voil.util.rotation.SmoothRotationState;
import lol.lwes.voil.util.target.TargetManager;
import lol.lwes.voil.util.world.BlockUtil;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class AutoWeb extends Module {
    private final ModeSetting activation;
    private final BooleanSetting ignorePlayers;
    private final KeybindSetting keybind;
    private final ModeSetting placeAt;
    private final NumberSetting range;
    private final BooleanSetting headweb;
    private final ModeSetting headwebFallback;
    private final BooleanSetting kbDisplace;
    private final RangeSetting displaceScan;
    private final NumberSetting displaceStep;
    private final BooleanSetting attackCooldown;
    private final NumberSetting skipIfCovered;
    private final RangeSetting placeDelay;
    private final RangeSetting switchBackDelay;
    private final ModeSetting rotations;
    private final BooleanSetting render;
    private final ColorSetting renderColor;
    private final DefaultMotionPredictor placeHelper;
    private final KnockbackPredictor slotHelper;
    private SmoothRotationState rotationHandle;
    private boolean firstTick;
    private BlockPos placePos;
    private BlockPos hitBlockPos;
    private Direction hitSide;
    private Vec3d hitVec;
    private BlockPos renderPos;
    private int delayTicks;
    private int previousSlot;
    private boolean canPlace;
    private int placeTicks;
    private boolean rotating;
    private long lastPlaceTime;
    private static long fadeDurationMs = 400L;

    public AutoWeb() {
        super("AutoWeb", "Places web on the target", Category.Combat);
        this.activation = new ModeSetting("Activation", "Auto", "When to place", new String[]{"Auto", "On Key", "Macro"});
        this.ignorePlayers = new BooleanSetting("Ignore Players", false, "Ignore players when placing");
        this.keybind = new KeybindSetting("Keybind", 0, false, "Key for On Key mode");
        this.placeAt = new ModeSetting("Place At", "Current", "Where to web", new String[]{"Current", "Predict"});
        this.range = new NumberSetting("Range", 3.0, 6.0, 4.5, 0.1, "Max target distance");
        this.headweb = new BooleanSetting("Headweb", false, "Place at head height instead of feet");
        this.headwebFallback = new ModeSetting("Headweb Fallback", "Feet", "If the head cell has no support", new String[]{"Feet", "Skip"});
        this.kbDisplace = new BooleanSetting(
            "KB Displace", false, "does nothing until the target can be knocked into a wall auto attacks and headweb them there"
        );
        this.displaceScan = new RangeSetting("Displace Scan", 20.0, 180.0, 60.0, 120.0, 5.0, "Yaw offset range scanned for a wall");
        this.displaceStep = new NumberSetting("Displace Step", 2.0, 30.0, 8.0, 1.0, "Scan step in degrees");
        this.attackCooldown = new BooleanSetting("Attack Cooldown", true, "Only displace when the attack is charged");
        this.skipIfCovered = new NumberSetting("Skip If Covered %", 0.0, 100.0, 60.0, 1.0, "Skip if the target is already N amount covered by web");
        this.placeDelay = new RangeSetting("Place Delay", 1.0, 20.0, 3.0, 5.0, 1.0, "Ticks between placements");
        this.switchBackDelay = new RangeSetting("Switch Back Delay", 0.0, 20.0, 1.0, 2.0, 1.0, "Ticks before restoring the previous slot");
        this.rotations = new ModeSetting("Rotations", "Silent", "Placement rotation", new String[]{"Silent", "Legit", "Looking"});
        this.render = new BooleanSetting("Render", true, "Draw the target web cell");
        Color color = new Color(120, 200, 255, 140);
        this.renderColor = new ColorSetting("Render Color", color, "Cell color", true);
        this.placeHelper = new DefaultMotionPredictor();
        this.slotHelper = DefaultKnockbackPredictor.create();
        this.rotationHandle = SmoothRotationState.createEnded();
        this.previousSlot = -1;
        this.lastPlaceTime = 0L;
        Setting[] asetting = new Setting[]{
            this.activation,
            this.ignorePlayers,
            this.keybind,
            this.placeAt,
            this.range,
            this.headweb,
            this.headwebFallback,
            this.kbDisplace,
            this.displaceScan,
            this.displaceStep,
            this.attackCooldown,
            this.skipIfCovered,
            this.placeDelay,
            this.switchBackDelay,
            this.rotations,
            this.render,
            this.renderColor
        };
        this.addSettings(asetting);
        ModeSetting modesetting = this.activation;
        Setting.dependSetting(this.keybind, "On Key", modesetting);
        Setting.dependSetting(this.headwebFallback, this.headweb);
        Setting.dependSetting(this.kbDisplace, this.headweb);
        Setting.dependSetting(this.displaceScan, this.kbDisplace);
        Setting.dependSetting(this.displaceStep, this.kbDisplace);
        Setting.dependSetting(this.attackCooldown, this.kbDisplace);
        Setting.dependSetting(this.renderColor, this.render);
        ModeSetting modesetting1 = this.activation;
        Setting.dependSetting(this.keybind, "On Key", modesetting1);
        ModeSetting modesetting2 = this.activation;
        Setting.dependSetting(this.keybind, "Macro", modesetting2);
        ModeSetting modesetting3 = this.activation;
        Setting.dependSetting(this.ignorePlayers, "Macro", modesetting3);
    }

    private void beginPlace(BlockPos blockpos, boolean flag) {
        if (this.canPlaceWeb(blockpos)) {
            this.previousSlot = mc.player.getInventory().getSelectedSlot();
            Predicate<ItemStack> predicate = itemstack -> itemstack.isOf(Items.COBWEB);
            FoundSlot foundslot = InventoryUtil.findHotbarStack(predicate);
            if (foundslot.isFound()) {
                int i = foundslot.getSlot();
                InventoryUtil.selectSlot(i);
            }

            if (!this.rotations.is("Looking") || flag) {
                this.firstTick = true;
                SmoothRotationController smoothrotationcontroller = VoilClient.getRotationController();
                Supplier supplier3 = SmoothRotationController.smoothForTicks(3.0);
                Supplier supplier4 = () -> {
                    Vec3d vec3d1 = mc.player.getEyePos();
                    Vec3d vec3d = this.hitVec;
                    return Rotation.fromPositions(vec3d1, vec3d);
                };
                Supplier supplier5 = () -> true;
                boolean flag2 = flag || this.rotations.is("Silent");
                RotationAxisMode rotationaxismode1 = RotationAxisMode.YawAndPitch;
                EasingFunction easingfunction = UiEasings::linear;
                RotationAxisMode rotationaxismode = rotationaxismode1;
                Object object = null;
                boolean flag1 = flag2;
                Supplier supplier = supplier5;
                Supplier supplier1 = supplier4;
                Supplier supplier2 = supplier3;
                AutoWeb autoweb1 = this;
                this.rotationHandle = smoothrotationcontroller.startWith(
                    autoweb1, supplier2, supplier1, supplier, flag1, true, (SmoothRotationState)object, rotationaxismode, easingfunction
                );
            } else if (mc.world.getBlockState(this.placePos).isReplaceable()) {
                mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, new BlockHitResult(this.hitVec, this.hitSide, this.hitBlockPos, false));
                mc.player.swingHand(Hand.MAIN_HAND);
                this.lastPlaceTime = System.currentTimeMillis();
                this.delayTicks = this.placeDelay.getRandomInt();
                this.canPlace = true;
                this.placeTicks = this.switchBackDelay.getRandomInt();
            }
        }
    }

    private double getCoverage(LivingEntity livingentity, boolean flag) {
        Box box = livingentity.getBoundingBox();
        int i = MathHelper.floor(flag ? box.maxY - 0.1 : box.minY + 0.1);
        int j = 0;
        int k = 0;

        for (int l = MathHelper.floor(box.minX); l <= MathHelper.floor(box.maxX); l++) {
            for (int i1 = MathHelper.floor(box.minZ); i1 <= MathHelper.floor(box.maxZ); i1++) {
                j++;
                if (mc.world.getBlockState(new BlockPos(l, i, i1)).isOf(Blocks.COBWEB)) {
                    k++;
                }
            }
        }

        return j == 0 ? 0.0 : (double)k / j;
    }

    private boolean isInWeb(LivingEntity livingentity) {
        Box box = livingentity.getBoundingBox();

        for (BlockPos blockpos : BlockPos.iterate(BlockPos.ofFloored(box.minX, box.minY, box.minZ), BlockPos.ofFloored(box.maxX, box.maxY, box.maxZ))) {
            if (mc.world.getBlockState(blockpos).isOf(Blocks.COBWEB)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void onTick() {
        if (!Module.isNotInGame() && mc.currentScreen == null) {
            if (this.delayTicks > 0) {
                this.delayTicks--;
            }

            if (this.canPlace) {
                int i = this.placeTicks;
                AutoWeb autoweb1 = this;
                autoweb1.placeTicks = i - 1;
                if (i <= 0) {
                    if (this.previousSlot != -1) {
                        int j = this.previousSlot;
                        InventoryUtil.selectSlot(j);
                    }

                    this.canPlace = false;
                }
            }

            if (!VoilClient.getSpinAttackController().isActive()) {
                this.rotating = false;
                if (this.activation.is("Macro")) {
                    if (this.keybind.isPressed() && this.delayTicks <= 0) {
                        this.macroPlace();
                    }
                } else if (this.activation.is("Auto") || this.activation.is("On Key") && this.keybind.isPressed()) {
                    if (!this.firstTick) {
                        if (this.delayTicks <= 0) {
                            LivingEntity livingentity = TargetManager.getPrimaryTargetDefault();
                            if (livingentity != null && !(mc.player.squaredDistanceTo(livingentity) > this.range.getFloat() * this.range.getFloat())) {
                                Predicate<ItemStack> predicate = itemstack -> itemstack.isOf(Items.COBWEB);
                                if (InventoryUtil.findHotbarStack(predicate).isFound()) {
                                    boolean flag = this.headweb.isEnabled();
                                    double d0 = this.skipIfCovered.getFloat() / 100.0;
                                    if (flag && this.kbDisplace.isEnabled()) {
                                        this.displace(livingentity, d0);
                                    } else if (!(d0 > 0.0) || !(this.getCoverage(livingentity, flag) >= d0)) {
                                        Vec3d vec3d1;
                                        if (this.placeAt.is("Predict")) {
                                            DefaultMotionPredictor defaultmotionpredictor = this.placeHelper;
                                            MotionRequest motionrequest = new MotionRequest(livingentity);
                                            vec3d1 = defaultmotionpredictor.predict(motionrequest).getVelocity();
                                        } else {
                                            vec3d1 = livingentity.getEntityPos();
                                        }

                                        Vec3d vec3d = vec3d1;
                                        BlockPos blockpos = this.getPlacePos(vec3d, flag, livingentity);
                                        if (flag && !BlockUtil.isExposed(blockpos)) {
                                            if (this.headwebFallback.is("Skip")) {
                                                return;
                                            }

                                            blockpos = this.getPlacePos(vec3d, false, livingentity);
                                        }

                                        this.renderPos = blockpos;
                                        this.lastPlaceTime = -1L;
                                        if (!this.isPlayerInside(blockpos)) {
                                            this.beginPlace(blockpos, false);
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        if (this.rotationHandle.isOnTarget() || this.rotationHandle.hasEnded()) {
                            this.firstTick = false;
                            if (!mc.world.getBlockState(this.placePos).isReplaceable()) {
                                return;
                            }

                            mc.interactionManager
                                .interactBlock(mc.player, Hand.MAIN_HAND, new BlockHitResult(this.hitVec, this.hitSide, this.hitBlockPos, false));
                            mc.player.swingHand(Hand.MAIN_HAND);
                            this.lastPlaceTime = System.currentTimeMillis();
                            this.delayTicks = this.placeDelay.getRandomInt();
                            this.canPlace = true;
                            this.placeTicks = this.switchBackDelay.getRandomInt();
                        }
                    }
                } else {
                    this.firstTick = false;
                }
            }
        }
    }

    @Override
    public void onDisable() {
        if (this.rotating) {
            VoilClient.getSpinAttackController().cancel();
        }

        this.firstTick = false;
        this.renderPos = null;
        VoilClient.getRotationController().release(this);
        if (this.previousSlot != -1) {
            int i = this.previousSlot;
            InventoryUtil.selectSlot(i);
        }
    }

    @Override
    public void onEnable() {
        this.delayTicks = 0;
        this.previousSlot = -1;
        this.firstTick = false;
        this.renderPos = null;
        this.canPlace = false;
        this.placeTicks = 0;
        this.rotating = false;
        this.rotationHandle = SmoothRotationState.createEnded();
    }

    private void displace(LivingEntity livingentity, double d0) {
        if (!this.isInWeb(livingentity)) {
            if (!(d0 > 0.0) || !(this.getCoverage(livingentity, false) >= d0) && !(this.getCoverage(livingentity, true) >= d0)) {
                if (!this.attackCooldown.isEnabled() || !(mc.player.getAttackCooldownProgress(0.5F) < 1.0F)) {
                    ItemStack itemstack = mc.player.getMainHandStack();
                    RegistryKey registrykey1 = Enchantments.KNOCKBACK;
                    Registry registry = mc.player.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
                    RegistryKey registrykey = registrykey1;
                    ItemStack itemstack1 = itemstack;
                    int i = InventoryUtil.getEnchantmentLevel(itemstack1, registrykey, registry);
                    Vec3d vec3d = mc.player.getEyePos();
                    double d1 = this.range.getFloat() * this.range.getFloat();
                    float f = this.displaceStep.getFloat();
                    float f1 = 0.0F;
                    KnockbackResult knockbackresult = null;
                    double d2 = Double.MAX_VALUE;

                    for (float f2 = this.displaceScan.getMinFloat(); f2 <= this.displaceScan.getValueMaxFloat(); f2 += f) {
                        for (byte b0 = -1; b0 <= 1; b0 += 2) {
                            float f3 = f2 * b0;
                            KnockbackPredictor knockbackpredictor = this.slotHelper;
                            KnockbackRequest knockbackrequest = new KnockbackRequest(
                                mc.player, livingentity, mc.player.getYaw() + f3, mc.player.isSprinting(), i
                            );
                            KnockbackResult knockbackresult1 = knockbackpredictor.simulate(knockbackrequest);
                            if (knockbackresult1.collided()) {
                                double d3 = vec3d.squaredDistanceTo(knockbackresult1.position());
                                if (!(d3 > d1) && !(d3 >= d2)) {
                                    d2 = d3;
                                    f1 = f3;
                                    knockbackresult = knockbackresult1;
                                }
                            }
                        }
                    }

                    if (knockbackresult != null) {
                        Vec3d vec3d1 = knockbackresult.position();
                        SpinAttackController spinattackcontroller = VoilClient.getSpinAttackController();
                        Runnable runnable = () -> {
                            BlockPos blockpos = this.getPlacePos(vec3d1, true, livingentity);
                            if (!BlockUtil.isExposed(blockpos)) {
                                blockpos = this.getPlacePos(vec3d1, false, livingentity);
                            }

                            this.renderPos = blockpos;
                            this.lastPlaceTime = -1L;
                            this.beginPlace(blockpos, true);
                        };
                        Object object = null;
                        float f4 = f1;
                        LivingEntity livingentity1 = livingentity;
                        boolean flag = spinattackcontroller.start(livingentity1, f4, (Consumer)object, runnable);
                        if (flag) {
                            this.rotating = true;
                            this.delayTicks = this.placeDelay.getRandomInt();
                        }
                    }
                }
            }
        }
    }

    private void macroPlace() {
        BlockHitResult blockhitresult = RaycastUtil.getTargetedBlock(this.ignorePlayers.isEnabled());
        if (blockhitresult != null) {
            BlockPos blockpos = blockhitresult.getBlockPos().offset(blockhitresult.getSide());
            if (mc.world.getBlockState(blockpos).isReplaceable()) {
                Predicate<ItemStack> predicate = itemstack -> itemstack.isOf(Items.COBWEB);
                FoundSlot foundslot = InventoryUtil.findHotbarStack(predicate);
                if (foundslot.isFound()) {
                    this.previousSlot = mc.player.getInventory().getSelectedSlot();
                    int i = foundslot.getSlot();
                    InventoryUtil.selectSlot(i);
                    mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, blockhitresult);
                    mc.player.swingHand(Hand.MAIN_HAND);
                    this.renderPos = blockpos;
                    this.lastPlaceTime = System.currentTimeMillis();
                    this.delayTicks = this.placeDelay.getRandomInt();
                    this.canPlace = true;
                    this.placeTicks = this.switchBackDelay.getRandomInt();
                }
            }
        }
    }

    private BlockPos getPlacePos(Vec3d vec3d, boolean flag, LivingEntity livingentity) {
        double d0 = flag ? vec3d.y + Math.max(0.1, livingentity.getHeight() - 0.2) : vec3d.y + 0.1;
        return BlockPos.ofFloored(vec3d.x, d0, vec3d.z);
    }

    private boolean isPlayerInside(BlockPos blockpos) {
        Object object = null;
        Box box = new Box(blockpos);
        if (mc.player.getBoundingBox().intersects(box)) {
            return true;
        }

        for (PlayerEntity playerentity : mc.world.getPlayers()) {
            if (playerentity != mc.player
                && (PlayerTargetUtil.isIgnored(playerentity.getUuid()) || PlayerTargetUtil.isFriend(playerentity.getUuid()))
                && playerentity.getBoundingBox().intersects(box)) {
                return true;
            }
        }

        return false;
    }

    private boolean canPlaceWeb(BlockPos blockpos) {
        if (!mc.world.getBlockState(blockpos).isReplaceable()) {
            return false;
        }

        Vec3d vec3d = mc.player.getEyePos();
        double d0 = mc.player.getAttributeValue(EntityAttributes.BLOCK_INTERACTION_RANGE);
        double d1 = -Double.MAX_VALUE;
        boolean flag = false;

        for (Direction direction : Direction.values()) {
            BlockPos blockpos1 = blockpos.offset(direction);
            if (!mc.world.getBlockState(blockpos1).isReplaceable()) {
                Direction direction1 = direction.getOpposite();
                Vec3d vec3d1 = new Vec3d(direction1.getOffsetX(), direction1.getOffsetY(), direction1.getOffsetZ());
                Vec3d vec3d2 = Vec3d.ofCenter(blockpos1).add(vec3d1.multiply(0.5));
                double d2 = vec3d.squaredDistanceTo(vec3d2);
                if (!(d2 > d0 * d0)) {
                    double d3 = vec3d.subtract(vec3d2).normalize().dotProduct(vec3d1);
                    if (!(d3 <= 0.0)) {
                        double d4 = d3 - d2 * 0.01;
                        if (d4 > d1) {
                            d1 = d4;
                            this.placePos = blockpos;
                            this.hitBlockPos = blockpos1;
                            this.hitSide = direction1;
                            this.hitVec = vec3d2;
                            flag = true;
                        }
                    }
                }
            }
        }

        return flag;
    }

    @EventHandler
    @Override
    public void onWorldRender(RenderAPI.RenderContext rendercontext) {
        if (this.render.isEnabled() && this.renderPos != null) {
            Color color = this.renderColor.getColor();
            float f = 1.0F;
            if (this.lastPlaceTime != -1L) {
                float f1 = (float)(System.currentTimeMillis() - this.lastPlaceTime) / (float)fadeDurationMs;
                if (f1 >= 1.0F) {
                    this.renderPos = null;
                    this.lastPlaceTime = -1L;
                    return;
                }

                f = 1.0F - UiEasings.easeOutQuad(f1);
            }

            Color color1 = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(color.getAlpha() * f));
            Render3DUtil.drawBoxFilled(new Matrix4f(), new Box(this.renderPos), color1);
        }
    }
}
