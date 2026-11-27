package lol.lwes.voil.module.combat;

import dev.prestigeclient.renderlib.impl.api.RenderAPI;
import java.awt.Color;
import java.util.Optional;
import java.util.function.Supplier;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.event.EntitySpawnEvent;
import lol.lwes.voil.event.EventHandler;
import lol.lwes.voil.event.PacketSendPostEvent;
import lol.lwes.voil.gui.NotificationManager;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.module.ModuleManager;
import lol.lwes.voil.module.client.Notifications;
import lol.lwes.voil.module.player.AutoBowBoost;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.KeybindSetting;
import lol.lwes.voil.setting.ModeSetting;
import lol.lwes.voil.setting.NumberSetting;
import lol.lwes.voil.setting.RangeSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.AimMathUtil;
import lol.lwes.voil.util.CombatUtil;
import lol.lwes.voil.util.RaycastUtil;
import lol.lwes.voil.util.SystemUtil;
import lol.lwes.voil.util.inventory.FoundSlot;
import lol.lwes.voil.util.inventory.InventoryUtil;
import lol.lwes.voil.util.packet.PacketUtil;
import lol.lwes.voil.util.render.EasingFunction;
import lol.lwes.voil.util.render.Render3DUtil;
import lol.lwes.voil.util.render.UiEasings;
import lol.lwes.voil.util.rotation.Rotation;
import lol.lwes.voil.util.rotation.RotationAxisMode;
import lol.lwes.voil.util.rotation.SmoothRotationController;
import lol.lwes.voil.util.rotation.SmoothRotationState;
import lol.lwes.voil.util.world.BlockUtil;
import lol.lwes.voil.util.world.EntityUtil;
import net.minecraft.block.RailBlock;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.network.SequencedPacketCreator;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import net.minecraft.world.RaycastContext;
import org.joml.Matrix4f;

public class CartMacro extends Module {
    private final NumberSetting railDelay = new NumberSetting("Rail delay", 0.0, 10.0, 0.0, 1.0, "Delay to place rail");
    private final NumberSetting cartDelay = new NumberSetting("Cart delay", 0.0, 10.0, 0.0, 1.0, "Delay to switch cart");
    private final NumberSetting bowSwitchBackDelay = new NumberSetting(
        "Bow Switch Back Delay", 0.0, 10.0, 0.0, 1.0, "Delay to switch back to the bow after placing TNT cart"
    );
    private final BooleanSetting switchBackToBow = new BooleanSetting("Switch Back to Bow", true, "Switch back to the bow after placing TNT cart");
    private final BooleanSetting safeBlock = new BooleanSetting("Safe Block", true, "Places a block between you and the tnt cart");
    private final ModeSetting cartMode = new ModeSetting("Cart mode", "Bow", "Cart modes", new String[]{"Crossbow", "Bow", "Both"});
    private final KeybindSetting keybindForCrossbow = new KeybindSetting("Keybind for crossbow", 0, false, "Keybind to use crossbow cart");
    private final ModeSetting crossbowMode = new ModeSetting(
        "Crossbow Mode", "Fast", "Fast = minimal holds, Normal = extra safety tick per step", new String[]{"Fast", "Normal"}
    );
    private final ModeSetting rotations = new ModeSetting("Rotations", "Looking", "Rotations for the cart macro", new String[]{"Silent", "Looking"});
    private final RangeSetting rotationSmooth = new RangeSetting("Rotation Smooth", 1.0, 20.0, 2.0, 4.0, 1.0, "Ticks to smooth each rotation over");
    private final BooleanSetting miss = new BooleanSetting("Miss", false, "Miss the cart");
    private final NumberSetting missChance = new NumberSetting("Miss Chance", 0.0, 100.0, 0.0, 1.0, "Chance of the cart not being placed");
    private final BooleanSetting autoCartFill = new BooleanSetting("Auto Cart Fill", false, "Automatically fills the cart slot again");
    private final NumberSetting slot = new NumberSetting("Slot", 1.0, 9.0, 0.0, 1.0, "Slot of the cart to fill");
    private final BooleanSetting cycleSettingWithBind = new BooleanSetting("Cycle Setting with bind", false, "Cycles the setting when you press the keybind");
    private final KeybindSetting cycleSetting = new KeybindSetting("Cycle Setting", 0, false, "Cycles the setting when you press the keybind");
    private final BooleanSetting prioritizeOffhand = new BooleanSetting("Prioritize Offhand", false, "Prioritizes offhand for cart placement");
    private final BooleanSetting visualisePlacement = new BooleanSetting("Visualise Placement", false, "Renders where the rail and fire be placed at");
    private final BooleanSetting bowOnKey = new BooleanSetting(
        "Bow On Key", false, "Auto charges and shoots the bow on keypress, then places the cart at the predicted landing spot"
    );
    private final KeybindSetting bowMacroKey = new KeybindSetting("Bow Macro Key", 0, false, "Keybind for the bow macro");
    private final NumberSetting bowCharge = new NumberSetting("Bow Charge", 3.0, 20.0, 5.0, 1.0, "Ticks to charge the bow before releasing (20 = full)");
    private int previousSlot = -1;
    private BlockPos railPos;
    private int placeDelayTicks = 0;
    private int railSlot = -1;
    private int breakDelayTicks = 0;
    private boolean placingRail = false;
    private int stepIndex = 0;
    boolean shootingBow = false;
    private BlockPos targetPos = null;
    private boolean igniting = false;
    private int counterA = 0;
    private int counterB = 0;
    private int counterC = 0;
    private int counterD = 0;
    private BlockPos railBase = null;
    private BlockPos targetBlock = null;
    private float yaw = Float.NaN;
    private float pitch = Float.NaN;
    private int modeCycleCooldown = 0;
    private final int MAX_DELAY = 10;
    private boolean railPlaced = false;
    private boolean trackingArrow = false;
    private int arrowTicks = 0;
    private boolean usingItem = false;
    private boolean holdingUse = false;
    private boolean chargingBow = false;
    private boolean active = false;
    private int useTicks = 0;
    private int cooldown = 0;
    private BlockPos lastPlacedPos = null;
    private SmoothRotationState rotationHandle;
    private boolean rotating = false;

    public CartMacro() {
        super("CartMacro", "Helps you with cart pvp", Category.Combat);
        Setting[] asetting = new Setting[]{
            this.cartMode,
            this.railDelay,
            this.cartDelay,
            this.bowSwitchBackDelay,
            this.switchBackToBow,
            this.autoCartFill,
            this.prioritizeOffhand,
            this.slot,
            this.miss,
            this.missChance,
            this.cycleSettingWithBind,
            this.cycleSetting,
            this.safeBlock,
            this.keybindForCrossbow,
            this.crossbowMode,
            this.rotations,
            this.rotationSmooth,
            this.visualisePlacement,
            this.bowOnKey,
            this.bowMacroKey,
            this.bowCharge
        };
        this.addSettings(asetting);
        Setting.dependAny(this.railDelay, this.cartMode, new String[]{"Bow", "Both"});
        Setting.dependAny(this.cartDelay, this.cartMode, new String[]{"Bow", "Both"});
        Setting.dependAny(this.bowSwitchBackDelay, this.cartMode, new String[]{"Bow", "Both"});
        Setting.dependAny(this.switchBackToBow, this.cartMode, new String[]{"Bow", "Both"});
        Setting.dependAny(this.safeBlock, this.cartMode, new String[]{"Bow", "Both"});
        Setting.dependAny(this.rotations, this.cartMode, new String[]{"Bow", "Both"});
        Setting.dependAny(this.prioritizeOffhand, this.cartMode, new String[]{"Bow", "Both"});
        Setting.dependAny(this.miss, this.cartMode, new String[]{"Bow", "Both"});
        Setting.dependAny(this.bowOnKey, this.cartMode, new String[]{"Bow", "Both"});
        Setting.dependAny(this.keybindForCrossbow, this.cartMode, new String[]{"Crossbow", "Both"});
        Setting.dependAny(this.crossbowMode, this.cartMode, new String[]{"Crossbow", "Both"});
        Setting.dependAny(this.visualisePlacement, this.cartMode, new String[]{"Crossbow", "Both"});
        Setting.dependSetting(this.missChance, this.miss);
        Setting.dependSetting(this.cycleSetting, this.cycleSettingWithBind);
        Setting.dependSetting(this.slot, this.autoCartFill);
        Setting.dependSetting(this.bowMacroKey, this.bowOnKey);
        Setting.dependSetting(this.bowCharge, this.bowOnKey);
    }

    private boolean usesCrossbow() {
        return this.cartMode.is("Crossbow") || this.cartMode.is("Both");
    }

    private boolean isRotating() {
        return this.rotationHandle != null && this.rotationHandle.isOnTarget();
    }

    private boolean isRotationDone() {
        return this.rotationHandle == null || this.rotationHandle.hasEnded();
    }

    private Box getRailBox() {
        double d0 = this.railBase.getX() + 0.5;
        double d1 = this.railBase.getZ() + 0.5;
        double d2 = this.railBase.getY() + 1.0;
        return new Box(d0 - 0.49, d2, d1 - 0.49, d0 + 0.49, d2 + 0.7, d1 + 0.49);
    }

    private BlockPos predictArrowLanding(int i) {
        float f = i / 20.0F;
        f = (f * f + f * 2.0F) / 3.0F;
        if (f > 1.0F) {
            f = 1.0F;
        }

        float f1 = f * 3.0F;
        double d0 = Math.toRadians(mc.player.getYaw());
        double d1 = Math.toRadians(mc.player.getPitch());
        Vec3d vec3d = new Vec3d(-Math.sin(d0) * Math.cos(d1), -Math.sin(d1), Math.cos(d0) * Math.cos(d1)).multiply(f1);
        Vec3d vec3d1 = mc.player.getVelocity();
        vec3d = vec3d.add(vec3d1.x, mc.player.isOnGround() ? 0.0 : vec3d1.y, vec3d1.z);
        Vec3d vec3d2 = mc.player.getEyePos().subtract(0.0, 0.1, 0.0);

        for (int j = 0; j < 200; j++) {
            Vec3d vec3d3 = vec3d2.add(vec3d);
            BlockHitResult blockhitresult = mc.world.raycast(new RaycastContext(vec3d2, vec3d3, ShapeType.COLLIDER, FluidHandling.NONE, mc.player));
            if (blockhitresult.getType() == Type.BLOCK) {
                return blockhitresult.getBlockPos();
            }

            vec3d2 = vec3d3;
            vec3d = vec3d.multiply(0.99).add(0.0, -0.05, 0.0);
        }

        return null;
    }

    private Rotation rotationTo(Vec3d vec3d) {
        Vec3d vec3d1 = mc.player.getEyePos();
        double d0 = vec3d.x - vec3d1.x;
        double d1 = vec3d.y - vec3d1.y;
        double d2 = vec3d.z - vec3d1.z;
        double d3 = Math.sqrt(d0 * d0 + d2 * d2);
        float f = (float)Math.toDegrees(Math.atan2(d2, d0)) - 90.0F;
        float f1 = (float)(-Math.toDegrees(Math.atan2(d1, d3)));
        return new Rotation(f, MathHelper.clamp(f1, -90.0F, 90.0F));
    }

    private int findBowSlot() {
        PlayerInventory playerinventory = mc.player.getInventory();

        for (int i = 0; i < 9; i++) {
            ItemStack itemstack = playerinventory.getStack(i);
            Item item = itemstack.getItem();
            if (this.isRail(item)) {
                return i;
            }
        }

        return -1;
    }

    private int findBlockSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack itemstack = mc.player.getInventory().getStack(i);
            if (itemstack.getItem() instanceof BlockItem) {
                Item item = itemstack.getItem();
                if (!this.isRail(item)) {
                    return i;
                }
            }
        }

        return 0;
    }

    @Override
    public void onTick() {
        if (mc.player != null && mc.world != null) {
            Class<? extends Module> oclass = AutoBowBoost.class;
            ModuleManager modulemanager = ModuleManager.INSTANCE;
            if (!modulemanager.isEnabled(oclass)) {
                this.handleInventory();
                if (this.cooldown > 0) {
                    int j = this.cooldown--;
                }

                if (!this.bowOnKey.isEnabled() || !this.usesBow()) {
                    this.releaseUse();
                } else if (this.chargingBow) {
                    if (!mc.player.isUsingItem()) {
                        this.chargingBow = false;
                    }
                } else if (!this.holdingUse) {
                    if (this.cooldown == 0 && this.bowMacroKey.isPressed()) {
                        this.usingItem = true;
                    }

                    label345:
                    if (this.usingItem) {
                        if (this.hasRails()) {
                            Item item = Items.TNT_MINECART;
                            if (InventoryUtil.containsItemInHotbar(item)) {
                                if (mc.player.getMainHandStack().getItem() != Items.BOW) {
                                    int i = InventoryUtil.findHotbarSlot(Items.BOW);
                                    if (i == -1) {
                                        this.usingItem = false;
                                    } else {
                                        mc.player.getInventory().setSelectedSlot(i);
                                    }
                                } else {
                                    this.usingItem = false;
                                    mc.options.useKey.setPressed(true);
                                    this.holdingUse = true;
                                    this.useTicks = 0;
                                    this.cooldown = 10;
                                }
                                break label345;
                            }
                        }

                        this.usingItem = false;
                    }
                } else if (mc.player.getMainHandStack().getItem() != Items.BOW) {
                    this.releaseUse();
                } else if (!mc.player.isUsingItem()) {
                    int i4 = this.useTicks;
                    int k = i4 + 1;
                    CartMacro cartmacro1 = this;
                    cartmacro1.useTicks = k;
                    if (k > 10) {
                        this.releaseUse();
                    }
                } else {
                    this.useTicks = 0;
                    if (mc.player.getItemUseTime() >= this.bowCharge.getInt()) {
                        BlockPos blockpos2 = this.predictArrowLanding(mc.player.getItemUseTime());
                        double d0 = blockpos2 == null ? -1.0 : CombatUtil.getDistanceSqToEyes(blockpos2.toCenterPos());
                        if (blockpos2 != null && !(d0 > this.square(6.0F)) && !(d0 <= 3.0)) {
                            this.lastPlacedPos = blockpos2;
                            mc.options.useKey.setPressed(false);
                            this.holdingUse = false;
                            this.chargingBow = true;
                        } else {
                            this.releaseUse();
                            oclass = Notifications.class;
                            modulemanager = ModuleManager.INSTANCE;
                            if (modulemanager.isEnabled(oclass)) {
                                NotificationManager notificationmanager1 = VoilClient.getNotifications();
                                String s1 = blockpos2 == null ? "No landing spot" : "Landing out of reach lower Bow Charge";
                                String s2 = "Cart Macro";
                                notificationmanager1.addNotification(s2, s1);
                            }
                        }
                    }
                }

                if (this.trackingArrow) {
                    int j4 = this.arrowTicks--;
                    if (this.arrowTicks <= 0) {
                        this.trackingArrow = false;
                        this.railPos = null;
                    }
                }

                if (this.modeCycleCooldown > 0) {
                    int k4 = this.modeCycleCooldown--;
                } else if (this.cycleSettingWithBind.isEnabled() && this.cycleSetting.isPressed()) {
                    this.cartMode.cycle();
                    this.modeCycleCooldown = 10;
                    oclass = Notifications.class;
                    modulemanager = ModuleManager.INSTANCE;
                    if (modulemanager.isEnabled(oclass)) {
                        NotificationManager notificationmanager = VoilClient.getNotifications();
                        String s5 = this.cartMode.getMode();
                        String s3 = "Mode set to " + s5;
                        String s4 = "Cart Macro";
                        notificationmanager.addNotification(s4, s3);
                    }
                }

                if (this.usesBow() && (this.placingRail || this.railPos != null && this.railPlaced || this.railSlot >= 0 || this.shootingBow || this.igniting)) {
                    if (this.placingRail) {
                        if (this.railPos == null || this.isRotationDone()) {
                            this.placingRail = false;
                            this.startRotation();
                        } else if (!this.isRotating()) {
                            int j5 = this.stepIndex;
                            int l = j5 + 1;
                            CartMacro cartmacro2 = this;
                            cartmacro2.stepIndex = l;
                            if (l > 40) {
                                this.placingRail = false;
                                this.startRotation();
                            }
                        } else {
                            this.stepIndex = 0;
                            int i2 = this.findBowSlot();
                            if (i2 == -1) {
                                this.placingRail = false;
                                this.startRotation();
                            } else {
                                InventoryUtil.selectSlot(i2);
                                BlockPos blockpos4 = this.railPos;
                                PacketUtil.sendSequenced(
                                    i7 -> new PlayerInteractBlockC2SPacket(
                                        Hand.MAIN_HAND,
                                        new BlockHitResult(
                                            new Vec3d(blockpos4.getX() + 0.5, blockpos4.up().getY(), blockpos4.getZ() + 0.5), Direction.UP, blockpos4, false
                                        ),
                                        i7
                                    )
                                );
                                this.placingRail = false;
                                this.breakDelayTicks = this.railDelay.getInt();
                                this.placeDelayTicks = this.cartDelay.getInt();
                            }
                        }
                    } else if (this.railPos != null && this.railPlaced) {
                        if (this.breakDelayTicks > 0) {
                            int l4 = this.breakDelayTicks--;
                        }

                        if (this.placeDelayTicks > 0) {
                            int i5 = this.placeDelayTicks--;
                        }

                        label296:
                        if (this.placeDelayTicks == 0 && this.previousSlot != -1) {
                            if (this.miss.isEnabled()) {
                                double d1 = this.missChance.getValue();
                                if (AimMathUtil.rollChance(d1)) {
                                    if (this.switchBackToBow.isEnabled()) {
                                        this.railSlot = this.bowSwitchBackDelay.getInt();
                                    }

                                    this.previousSlot = -1;
                                    this.railPlaced = false;
                                    this.railPos = null;
                                    this.startRotation();
                                    break label296;
                                }
                            }

                            if (this.isRotating() || this.isRotationDone()) {
                                BlockPos blockpos3 = this.railPos;
                                Hand hand;
                                if (this.prioritizeOffhand.isEnabled() && mc.player.getOffHandStack().getItem() == Items.TNT_MINECART) {
                                    hand = Hand.OFF_HAND;
                                } else {
                                    hand = Hand.MAIN_HAND;
                                    InventoryUtil.hasItemInHotbar(Items.TNT_MINECART);
                                }

                                SequencedPacketCreator sequencedpacketcreator = i7 -> new PlayerInteractBlockC2SPacket(
                                    hand,
                                    new BlockHitResult(
                                        new Vec3d(blockpos3.getX() + 0.5, blockpos3.up().getY() + 0.125, blockpos3.getZ() + 0.5),
                                        Direction.UP,
                                        blockpos3.up(),
                                        false
                                    ),
                                    i7
                                );
                                PacketUtil.sendSequenced(sequencedpacketcreator);
                                this.startRotation();
                                this.placeDelayTicks = -1;
                                this.previousSlot = -1;
                                this.railPlaced = false;
                                this.railPos = null;
                                if (this.safeBlock.isEnabled()) {
                                    this.targetPos = this.getFrontPos();
                                    if (this.targetPos != null) {
                                        this.igniting = true;
                                        this.counterA = 0;
                                        Vec3d vec3d1 = this.toTopCenter(this.targetPos);
                                        boolean flag = this.rotations.is("Silent");
                                        this.rotateTo(vec3d1, flag);
                                    }
                                }

                                if (this.switchBackToBow.isEnabled()) {
                                    this.railSlot = this.bowSwitchBackDelay.getInt();
                                }
                            }
                        }
                    }

                    if (this.igniting) {
                        if (this.targetPos == null || this.isRotationDone()) {
                            this.igniting = false;
                            this.targetPos = null;
                            this.startRotation();
                        } else if (this.isRotating()) {
                            int j2 = this.findBlockSlot();
                            InventoryUtil.selectSlot(j2);
                            BlockUtil.placeBlock(this.targetPos, Hand.MAIN_HAND, j2, true, true);
                            this.igniting = false;
                            this.targetPos = null;
                            this.startRotation();
                        } else {
                            int k5 = this.counterA;
                            int i1 = k5 + 1;
                            CartMacro cartmacro3 = this;
                            cartmacro3.counterA = i1;
                            if (i1 > 40) {
                                this.igniting = false;
                                this.targetPos = null;
                                this.startRotation();
                            }
                        }
                    }

                    if (this.railSlot > 0) {
                        int l5 = this.railSlot--;
                    } else if (this.railSlot == 0) {
                        if (this.switchBackToBow.isEnabled() && !this.igniting) {
                            int k2 = InventoryUtil.findHotbarSlot(Items.BOW);
                            if (k2 != -1) {
                                mc.player.getInventory().setSelectedSlot(k2);
                            }

                            this.railSlot = -1;
                        } else if (!this.switchBackToBow.isEnabled()) {
                            this.railSlot = -1;
                        }
                    }
                }

                if (!this.usesCrossbow()) {
                    if (this.counterB != 0) {
                        this.resetCounters();
                    }
                } else {
                    if (this.counterB == 0) {
                        if (!this.keybindForCrossbow.isPressed()) {
                            return;
                        }

                        BlockHitResult blockhitresult = RaycastUtil.raycastBlock(4.5, FluidHandling.NONE);
                        if (blockhitresult == null || blockhitresult.getType() != Type.BLOCK) {
                            return;
                        }

                        BlockPos blockpos5 = blockhitresult.getBlockPos();
                        if (!this.isAboveBlock(blockpos5) || !mc.world.getBlockState(blockpos5.up()).isReplaceable()) {
                            return;
                        }

                        this.railBase = blockpos5;
                        this.targetBlock = this.findPlaceablePos(blockpos5);
                        this.counterB = 1;
                        this.counterC = 0;
                        this.counterD = 0;
                    }

                    if (this.counterC > 0) {
                        int l6 = this.counterC--;
                    } else {
                        int l2 = this.crossbowMode.is("Fast") ? 1 : 2;
                        switch (this.counterB) {
                            case 1:
                                int k3 = this.findBowSlot();
                                if (k3 == -1) {
                                    this.resetCounters();
                                    return;
                                }

                                InventoryUtil.selectSlot(k3);
                                this.rotateTo(this.toTopCenter(this.railBase), true);
                                this.counterB = 2;
                                this.counterD = 0;
                                break;
                            case 2:
                                if (this.isRotationDone()) {
                                    this.resetCounters();
                                    return;
                                }

                                if (this.isRotating()) {
                                    BlockPos blockpos = this.railBase;
                                    if (this.isAimingAt(blockpos)) {
                                        this.counterD = 0;
                                        BlockPos blockpos6 = this.railBase;
                                        PacketUtil.sendSequenced(
                                            i7 -> new PlayerInteractBlockC2SPacket(
                                                Hand.MAIN_HAND,
                                                new BlockHitResult(
                                                    new Vec3d(blockpos6.getX() + 0.5, blockpos6.getY() + 1.0, blockpos6.getZ() + 0.5),
                                                    Direction.UP,
                                                    blockpos6,
                                                    false
                                                ),
                                                i7
                                            )
                                        );
                                        int l3 = InventoryUtil.findHotbarSlot(Items.TNT_MINECART);
                                        if (l3 == -1) {
                                            this.resetCounters();
                                            return;
                                        }

                                        InventoryUtil.selectSlot(l3);
                                        PacketUtil.sendSequenced(
                                            i7 -> new PlayerInteractBlockC2SPacket(
                                                Hand.MAIN_HAND,
                                                new BlockHitResult(
                                                    new Vec3d(blockpos6.getX() + 0.5, blockpos6.getY() + 1.125, blockpos6.getZ() + 0.5),
                                                    Direction.UP,
                                                    blockpos6.up(),
                                                    false
                                                ),
                                                i7
                                            )
                                        );
                                        this.counterB = 3;
                                        this.counterC = l2;
                                        break;
                                    }
                                }

                                int k6 = this.counterD;
                                int j1 = k6 + 1;
                                CartMacro cartmacro4 = this;
                                cartmacro4.counterD = j1;
                                if (j1 > 40) {
                                    this.resetCounters();
                                }

                                return;
                            case 3:
                                if (this.targetBlock == null) {
                                    this.startRotation();
                                    this.counterB = 6;
                                    this.counterC = l2;
                                    return;
                                }

                                this.rotateTo(this.toTopCenter(this.targetBlock), true);
                                this.counterB = 4;
                                this.counterD = 0;
                                break;
                            case 4:
                                if (this.isRotationDone()) {
                                    this.resetCounters();
                                    return;
                                }

                                if (this.isRotating()) {
                                    BlockPos blockpos1 = this.targetBlock;
                                    if (this.isAimingAt(blockpos1)) {
                                        this.counterD = 0;
                                        int j3 = InventoryUtil.findHotbarSlot(Items.FLINT_AND_STEEL);
                                        if (j3 == -1) {
                                            this.resetCounters();
                                            return;
                                        }

                                        InventoryUtil.selectSlot(j3);
                                        BlockPos blockpos7 = this.targetBlock;
                                        PacketUtil.sendSequenced(
                                            i7 -> new PlayerInteractBlockC2SPacket(
                                                Hand.MAIN_HAND,
                                                new BlockHitResult(
                                                    new Vec3d(blockpos7.getX() + 0.5, blockpos7.getY() + 1.0, blockpos7.getZ() + 0.5),
                                                    Direction.UP,
                                                    blockpos7,
                                                    false
                                                ),
                                                i7
                                            )
                                        );
                                        this.counterB = 5;
                                        this.counterC = l2;
                                        break;
                                    }
                                }

                                int j6 = this.counterD;
                                int k1 = j6 + 1;
                                CartMacro cartmacro5 = this;
                                cartmacro5.counterD = k1;
                                if (k1 > 40) {
                                    this.resetCounters();
                                }

                                return;
                            case 5:
                                this.startRotation();
                                this.counterB = 6;
                                this.counterC = l2;
                                break;
                            case 6:
                                int i3 = InventoryUtil.findHotbarSlot(Items.CROSSBOW);
                                if (i3 == -1) {
                                    this.resetCounters();
                                    return;
                                }

                                InventoryUtil.selectSlot(i3);
                                this.counterB = 7;
                                this.counterC = 1;
                                break;
                            case 7:
                                Vec3d vec3d = this.getAimPoint();
                                if (vec3d == null) {
                                    this.resetCounters();
                                    return;
                                }

                                this.rotateTo(vec3d, true);
                                this.counterB = 8;
                                this.counterD = 0;
                                break;
                            case 8:
                                if (this.isRotationDone()) {
                                    this.resetCounters();
                                    return;
                                }

                                if (this.isRotating() && this.isAimedAtTarget()) {
                                    this.counterD = 0;
                                    float f1 = this.yaw;
                                    float f = this.pitch;
                                    PacketUtil.sendSequenced(i7 -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, i7, f1, f));
                                    mc.player.swingHand(Hand.MAIN_HAND);
                                    this.counterB = 9;
                                    this.counterC = 2;
                                    break;
                                }

                                int i6 = this.counterD;
                                int l1 = i6 + 1;
                                CartMacro cartmacro6 = this;
                                cartmacro6.counterD = l1;
                                if (l1 > 40) {
                                    this.resetCounters();
                                }

                                return;
                            case 9:
                                this.resetCounters();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onDisable() {
        VoilClient.getRotationController().release(this);
        this.rotationHandle = SmoothRotationState.createEnded();
    }

    @Override
    public void onEnable() {
        VoilClient.getRotationController().release(this);
        this.rotationHandle = SmoothRotationState.createEnded();
        this.rotating = false;
        this.resetCounters();
        this.releaseUse();
        this.resetState();
        this.yaw = Float.NaN;
        this.pitch = Float.NaN;
    }

    private boolean rayIntersects(Vec3d vec3d, Vec3d vec3d1, Box box, Box box1) {
        Vec3d vec3d2 = vec3d1.subtract(vec3d).normalize();
        Vec3d vec3d3 = vec3d.add(vec3d2.multiply(12.0));
        Optional optional = box.raycast(vec3d, vec3d3);
        Optional optional1 = box1.raycast(vec3d, vec3d3);
        return !optional.isEmpty() && !optional1.isEmpty()
            ? ((Vec3d)optional.get()).squaredDistanceTo(vec3d) < ((Vec3d)optional1.get()).squaredDistanceTo(vec3d)
            : false;
    }

    private boolean isRail(Item item) {
        return item == Items.RAIL || item == Items.POWERED_RAIL || item == Items.DETECTOR_RAIL || item == Items.ACTIVATOR_RAIL;
    }

    private boolean usesBow() {
        return this.cartMode.is("Bow") || this.cartMode.is("Both");
    }

    private void releaseUse() {
        if (this.holdingUse || this.chargingBow) {
            mc.options.useKey.setPressed(false);
        }

        this.usingItem = false;
        this.holdingUse = false;
        this.chargingBow = false;
        this.active = false;
        this.lastPlacedPos = null;
        this.useTicks = 0;
    }

    private boolean canPlaceRail(BlockPos blockpos) {
        return !mc.world.getBlockState(blockpos).isAir()
            && mc.world.getBlockState(blockpos.up()).isReplaceable()
            && this.isAboveBlock(blockpos)
            && CombatUtil.getDistanceSqToEyes(blockpos.toCenterPos()) <= this.square(4.5F);
    }

    private void resetState() {
        this.placingRail = false;
        this.stepIndex = 0;
        this.igniting = false;
        this.targetPos = null;
        this.counterA = 0;
        this.shootingBow = false;
        this.railPlaced = false;
        this.trackingArrow = false;
        this.previousSlot = -1;
        this.railPos = null;
    }

    private boolean hasRails() {
        for (int i = 0; i < 9; i++) {
            ItemStack itemstack = mc.player.getInventory().getStack(i);
            Item item = itemstack.getItem();
            if (this.isRail(item)) {
                return true;
            }
        }

        return false;
    }

    private boolean isAimingAt(BlockPos blockpos) {
        if (Float.isNaN(this.yaw)) {
            return false;
        }

        Vec3d vec3d = mc.player.getEyePos();
        Vec3d vec3d1 = this.rotationToVector(this.yaw, this.pitch);
        Vec3d vec3d2 = vec3d.add(vec3d1.multiply(4.5));
        return new Box(blockpos).raycast(vec3d, vec3d2).isPresent();
    }

    private Vec3d rotationToVector(float f, float f1) {
        double d0 = Math.toRadians(f);
        double d1 = Math.toRadians(f1);
        double d2 = Math.cos(d1);
        return new Vec3d(-Math.sin(d0) * d2, -Math.sin(d1), Math.cos(d0) * d2);
    }

    private boolean isAboveBlock(BlockPos blockpos) {
        return mc.player.getEyePos().y > blockpos.getY() + 1.0 + 0.05;
    }

    private boolean isAimedAtTarget() {
        if (!Float.isNaN(this.yaw) && this.targetBlock != null && this.railBase != null) {
            Vec3d vec3d = mc.player.getEyePos();
            Vec3d vec3d1 = this.rotationToVector(this.yaw, this.pitch);
            Vec3d vec3d2 = vec3d.add(vec3d1.multiply(12.0));
            Box box = this.getTargetBox();
            Box box1 = this.getRailBox();
            Optional optional = box.raycast(vec3d, vec3d2);
            Optional optional1 = box1.raycast(vec3d, vec3d2);
            return !optional.isEmpty() && !optional1.isEmpty()
                ? ((Vec3d)optional.get()).squaredDistanceTo(vec3d) < ((Vec3d)optional1.get()).squaredDistanceTo(vec3d)
                : false;
        } else {
            return false;
        }
    }

    private void startRotation() {
        this.rotating = true;
        VoilClient.getRotationController().release(this);
        this.rotationHandle = SmoothRotationState.createEnded();
    }

    private Vec3d getAimPoint() {
        if (this.targetBlock != null && this.railBase != null) {
            Vec3d vec3d = mc.player.getEyePos();
            Box box = this.getTargetBox();
            Box box1 = this.getRailBox();
            Vec3d vec3d1 = box1.getCenter();
            if (this.rayIntersects(vec3d, vec3d1, box, box1)) {
                return vec3d1;
            }

            for (double d0 = 0.15; d0 <= 0.75; d0 += 0.15) {
                for (double d1 = 0.2; d1 <= 0.8; d1 += 0.15) {
                    for (double d2 = 0.2; d2 <= 0.8; d2 += 0.15) {
                        Vec3d vec3d2 = new Vec3d(this.targetBlock.getX() + d1, this.targetBlock.up().getY() + d0, this.targetBlock.getZ() + d2);
                        if (this.rayIntersects(vec3d, vec3d2, box, box1)) {
                            return vec3d2;
                        }
                    }
                }
            }

            return null;
        } else {
            return null;
        }
    }

    private Box getTargetBox() {
        BlockPos blockpos = this.targetBlock.up();
        return new Box(blockpos).contract(0.05);
    }

    private void resetCounters() {
        this.counterB = 0;
        this.counterC = 0;
        this.counterD = 0;
        this.railBase = null;
        this.targetBlock = null;
        this.startRotation();
    }

    private BlockPos getFrontPos() {
        Vec3d vec3d = mc.player.getEntityPos();
        Object object = null;
        double d0 = Math.toRadians(mc.player.getYaw());
        double d1 = -Math.sin(d0);
        double d2 = Math.cos(d0);
        Vec3d vec3d1 = new Vec3d(d1, 0.0, d2);
        BlockPos blockpos = new BlockPos((int)Math.floor(vec3d.x + vec3d1.x), (int)Math.floor(vec3d.y), (int)Math.floor(vec3d.z + vec3d1.z));
        return CombatUtil.getDistanceSqToEyes(blockpos.toCenterPos()) <= this.square(6.0F)
                && !(mc.world.getBlockState(blockpos).getBlock() instanceof RailBlock)
            ? blockpos
            : null;
    }

    private BlockPos findPlaceablePos(BlockPos blockpos) {
        BlockPos blockpos1 = blockpos.offset(mc.player.getHorizontalFacing().getOpposite());
        if (this.canPlaceRail(blockpos1)) {
            return blockpos1;
        }

        for (Direction direction : net.minecraft.util.math.Direction.Type.HORIZONTAL) {
            BlockPos blockpos2 = blockpos.offset(direction);
            if (this.canPlaceRail(blockpos2)) {
                return blockpos2;
            }
        }

        return null;
    }

    private Vec3d toTopCenter(BlockPos blockpos) {
        return new Vec3d(blockpos.getX() + 0.5, blockpos.getY() + 1.0, blockpos.getZ() + 0.5);
    }

    private void rotateTo(Vec3d vec3d, boolean flag) {
        this.rotating = false;
        SmoothRotationController smoothrotationcontroller = VoilClient.getRotationController();
        Supplier supplier3 = SmoothRotationController.smoothForTicks(this.rotationSmooth.getRandomValue());
        Supplier supplier4 = () -> this.rotationTo(vec3d);
        Supplier supplier5 = () -> this.rotating;
        RotationAxisMode rotationaxismode1 = RotationAxisMode.YawAndPitch;
        EasingFunction easingfunction = UiEasings::linear;
        RotationAxisMode rotationaxismode = rotationaxismode1;
        Object object = null;
        boolean flag1 = flag;
        Supplier supplier = supplier5;
        Supplier supplier1 = supplier4;
        Supplier supplier2 = supplier3;
        CartMacro cartmacro1 = this;
        this.rotationHandle = smoothrotationcontroller.startWith(
            cartmacro1, supplier2, supplier1, supplier, flag1, true, (SmoothRotationState)object, rotationaxismode, easingfunction
        );
    }

    private float square(Number number) {
        return (Float)number * (Float)number;
    }

    private void handleInventory() {
        if (this.autoCartFill.isEnabled()) {
            if (mc.currentScreen instanceof InventoryScreen) {
                int j = (int)this.slot.getValue();
                int i = j - 1;
                if (i < 0 || i > 8) {
                    return;
                }

                ItemStack itemstack = mc.player.getInventory().getStack(i);
                if (!itemstack.isEmpty() && itemstack.getItem() == Items.TNT_MINECART) {
                    return;
                }

                Item[] aitem = new Item[]{Items.TNT_MINECART};
                FoundSlot foundslot = InventoryUtil.findHotbar(aitem);
                if (foundslot.getSlot() != -1) {
                    mc.interactionManager
                        .clickSlot(((InventoryScreen)mc.currentScreen).getScreenHandler().syncId, foundslot.getSlot(), i, SlotActionType.SWAP, mc.player);
                }
            }
        }
    }

    @EventHandler
    public void onPacketSendPost(PacketSendPostEvent packetsendpostevent) {
        if (packetsendpostevent.getPacket() instanceof PlayerMoveC2SPacket playermovec2spacket && playermovec2spacket.changesLook()) {
            this.yaw = playermovec2spacket.getYaw(this.yaw);
            this.pitch = playermovec2spacket.getPitch(this.pitch);
        }

        if (!ModuleManager.INSTANCE.isEnabled(AutoBowBoost.class)) {
            if (packetsendpostevent.getPacket() instanceof PlayerActionC2SPacket playeractionc2spacket
                && playeractionc2spacket.getAction() == Action.RELEASE_USE_ITEM
                && mc.player.getMainHandStack().getItem() == Items.BOW
                && this.usesBow()) {
                if (this.lastPlacedPos != null) {
                    this.railPos = this.lastPlacedPos;
                    this.lastPlacedPos = null;
                    this.active = true;
                } else if (this.rotations.is("Looking")) {
                    HitResult hitresult = mc.player.raycast(6.0, SystemUtil.getTickDelta(), false);
                    if (hitresult.getType() == Type.BLOCK) {
                        BlockHitResult blockhitresult = (BlockHitResult)hitresult;
                        this.railPos = blockhitresult.getBlockPos();
                    } else {
                        this.railPos = null;
                    }
                } else {
                    this.railPos = EntityUtil.getLookBlockPos(mc.player.getYaw());
                }

                if (this.railPos != null) {
                    double d0 = CombatUtil.getDistanceSqToEyes(this.railPos.toCenterPos());
                    if ((this.active || d0 <= this.square(6.0F) && d0 > 3.0) && this.hasRails() && InventoryUtil.containsItemInHotbar(Items.TNT_MINECART)) {
                        this.trackingArrow = true;
                        this.arrowTicks = 20;
                        this.railPlaced = false;
                    }

                    this.active = false;
                }
            }
        }
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent entityspawnevent) {
        if (this.trackingArrow) {
            if (entityspawnevent.getEntity() instanceof PersistentProjectileEntity persistentprojectileentity) {
                if (persistentprojectileentity.getOwner() == mc.player) {
                    if (this.railPos != null) {
                        this.trackingArrow = false;
                        this.railPlaced = true;
                        this.previousSlot = mc.player.getInventory().getSelectedSlot();
                        this.placingRail = true;
                        this.stepIndex = 0;
                        Vec3d vec3d = this.toTopCenter(this.railPos);
                        boolean flag = this.rotations.is("Silent");
                        this.rotateTo(vec3d, flag);
                    }
                }
            }
        }
    }

    @Override
    public void onWorldRender(RenderAPI.RenderContext rendercontext) {
        if (this.visualisePlacement.isEnabled()) {
            if (this.usesCrossbow()) {
                if (mc.player != null && mc.world != null) {
                    if (mc.player.getMainHandStack().isOf(Items.CROSSBOW)) {
                        BlockHitResult blockhitresult = RaycastUtil.raycastBlock(4.5, FluidHandling.NONE);
                        if (blockhitresult != null && blockhitresult.getType() == Type.BLOCK) {
                            BlockPos blockpos1 = blockhitresult.getBlockPos();
                            BlockPos blockpos2 = this.findPlaceablePos(blockpos1);
                            boolean flag = this.isAboveBlock(blockpos1) && mc.world.getBlockState(blockpos1.up()).isReplaceable();
                            boolean flag1 = blockpos2 != null;
                            Color color2 = new Color(flag ? 0 : 255, flag ? 255 : 0, 0, 80);
                            Color color3 = new Color(flag ? 0 : 255, flag ? 255 : 0, 0, 220);
                            Matrix4f matrix4f = new Matrix4f();
                            Render3DUtil.drawBlockTop(matrix4f, blockpos1, color2, color3);
                            if (flag1) {
                                Matrix4f matrix4f2 = new Matrix4f();
                                Color color7 = new Color(0, 255, 0, 80);
                                Color color = new Color(0, 255, 0, 220);
                                Color color1 = color7;
                                BlockPos blockpos = blockpos2;
                                matrix4f = matrix4f2;
                                Render3DUtil.drawBlockTop(matrix4f, blockpos, color1, color);
                            } else {
                                BlockPos blockpos3 = blockpos1.offset(mc.player.getHorizontalFacing().getOpposite());
                                Matrix4f matrix4f1 = new Matrix4f();
                                Color color6 = new Color(255, 0, 0, 80);
                                Color color4 = new Color(255, 0, 0, 220);
                                Color color5 = color6;
                                BlockPos blockpos4 = blockpos3;
                                matrix4f = matrix4f1;
                                Render3DUtil.drawBlockTop(matrix4f, blockpos4, color5, color4);
                            }
                        }
                    }
                }
            }
        }
    }
}
