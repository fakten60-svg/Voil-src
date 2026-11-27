package lol.lwes.voil.module.crystalpvp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.event.EventHandler;
import lol.lwes.voil.event.HotbarKeyEvent;
import lol.lwes.voil.event.PreRenderEvent;
import lol.lwes.voil.event.TickEvent;
import lol.lwes.voil.event.UseKeyEvent;
import lol.lwes.voil.mixin.accessor.KeyBindingAccessor;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.module.crystalpvp.AutoAnchorState;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.ItemListSetting;
import lol.lwes.voil.setting.KeybindSetting;
import lol.lwes.voil.setting.ListSetting;
import lol.lwes.voil.setting.ModeSetting;
import lol.lwes.voil.setting.MultiSelectSetting;
import lol.lwes.voil.setting.NumberSetting;
import lol.lwes.voil.setting.RangeSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.AimMathUtil;
import lol.lwes.voil.util.SystemUtil;
import lol.lwes.voil.util.TickFastForward;
import lol.lwes.voil.util.TickRequest;
import lol.lwes.voil.util.inventory.InventoryUtil;
import lol.lwes.voil.util.render.EasingFunction;
import lol.lwes.voil.util.render.UiEasings;
import lol.lwes.voil.util.rotation.Rotation;
import lol.lwes.voil.util.rotation.RotationAxisMode;
import lol.lwes.voil.util.rotation.RotationManager;
import lol.lwes.voil.util.rotation.SmoothRotationController;
import lol.lwes.voil.util.rotation.SmoothRotationState;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.InputUtil.Key;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import net.minecraft.world.RaycastContext;
import org.lwjgl.glfw.GLFW;

public class AutoAnchor extends Module {
    private final ModeSetting activation;
    private final NumberSetting explodeHoldTicks;
    private final RangeSetting chargeDelay;
    private final RangeSetting swapDelay;
    private final RangeSetting explodeDelay;
    public final BooleanSetting ignoreDoubleHand;
    private final BooleanSetting retainAnchor;
    private final BooleanSetting anchorKey;
    private final KeybindSetting anchorKeybind;
    private final BooleanSetting onlyWorkWithItem;
    private final ItemListSetting onlyWorkWithItemList;
    private final BooleanSetting autoBlowUp;
    private final NumberSetting finishSlot;
    private final BooleanSetting finishSlotOnFail;
    private final BooleanSetting damageTick;
    private final NumberSetting damageTickChance;
    private final MultiSelectSetting damageTickAffect;
    private final BooleanSetting airAnchor;
    private final NumberSetting airAnchorChance;
    private final NumberSetting airAnchorLimit;
    private final KeybindSetting forceAirAnchorKey;
    private final BooleanSetting forceAirAnchorOnProximity;
    private final NumberSetting airAnchorProximityRange;
    private final BooleanSetting airAnchorOnKey;
    private final KeybindSetting airAnchorKey;
    private final RangeSetting airAnchorDelay;
    private final BooleanSetting safePlace;
    private final ModeSetting safePlaceRot;
    private final BooleanSetting onlySafePlaceOnKey;
    private final KeybindSetting safePlaceKey;
    private final RangeSetting safeSettleDelay;
    private final BooleanSetting shiftPlace;
    private final ModeSetting shiftMode;
    private final ListSetting safePlaceOrder;
    private final ListSetting safeBlock;
    private final KeybindSetting forceObsidian;
    private final KeybindSetting forceGlowstone;
    private final BooleanSetting tickbaseTest;
    private final NumberSetting tickCount;
    private final ModeSetting tickbaseRotateMode;
    private final AutoAnchorState idleState;
    private final AutoAnchorState explodeWaitState;
    private AutoAnchorState placeAnchorState;
    private final AutoAnchorState markerStateA;
    private final AutoAnchorState markerStateB;
    private final AutoAnchorState markerStateC;
    private final AutoAnchorState markerStateD;
    private final AutoAnchorState markerStateE;
    private final AutoAnchorState selectAnchorState;
    private AutoAnchorState safePlaceState;
    public final AutoAnchorState chargeState;
    private AutoAnchorState selectGlowstoneState;
    private final AutoAnchorState swapItemState;
    private AutoAnchorState explodeState;
    private final AutoAnchorState selectExplodeAnchorState;
    private final AutoAnchorState initialState;
    private final AutoAnchorState holdActivationState;
    private BlockPos anchorPos;
    private boolean previewKeyHandled;
    public AutoAnchorState currentState;
    private int chargeDelayTicks;
    private int swapDelayTicks;
    private int finishSwapDelayTicks;
    private int explodeDelayTicks;
    private int airAnchorStreak;
    private boolean airAnchoring;
    private boolean airKeyHeld;
    private boolean forcedAirAnchor;
    private boolean anchorCharged;
    private boolean safeBlockPlaced;
    private boolean rechargePending;
    private boolean explodeReady;
    private int airAnchorDelayTicks;
    private boolean sneakingByModule;
    private BlockPos safePlacePos;
    private BlockPos safeSupportPos;
    private Direction safeFace;
    private Vec3d safeHitVec;
    private int safeSettleTicks;
    private Item safeBlockItem;
    private Item pendingItem;
    private AutoAnchorState afterSwapState;
    private AutoAnchorState returnState;
    private int swapTicks;
    private boolean previewTriggered;
    private boolean holdHandled;
    private int holdTicks;
    private static final long ROTATION_KEY = 100L;

    public AutoAnchor() {
        super("AutoAnchor", "Auto respawn anchor usage, Modernized", Category.CrystalPvP);
        this.activation = new ModeSetting(
            "Activation",
            "Tap",
            "Tap: one click runs the whole sequence. Hold: runs only while right click is held, release to stop",
            new String[]{"Tap", "Hold"}
        );
        this.explodeHoldTicks = new NumberSetting("Explode Hold Ticks", 0.0, 20.0, 5.0, 1.0, "Ticks right click must stay held before swapping to explode");
        this.chargeDelay = new RangeSetting("Charge Delay", 0.0, 10.0, 1.0, 2.0, 1.0, "Delay before charging an anchor");
        this.swapDelay = new RangeSetting("Swap Delay", 0.0, 10.0, 1.0, 1.0, 1.0, "Delay before swapping to glowstone");
        this.explodeDelay = new RangeSetting("Explode Delay", 0.0, 10.0, 1.0, 2.0, 1.0, "Delay before blowing up an anchor");
        this.ignoreDoubleHand = new BooleanSetting("Ignore double hand", true, "Ignores double handing when anchoring");
        this.retainAnchor = new BooleanSetting("Retain Anchor", false, "Remembers the anchor you placed when you look away and explodes it when you look back");
        this.anchorKey = new BooleanSetting("Anchor Key", false, "Places and blows up an anchor at where you look when pressed");
        this.anchorKeybind = new KeybindSetting("Anchor Keybind", 2, false, "Keybind for Anchor Key");
        this.onlyWorkWithItem = new BooleanSetting("Only Work With Item", true, "Only works when holding an specific item");
        Item[] aitem = new Item[]{Items.NETHERITE_SWORD};
        this.onlyWorkWithItemList = new ItemListSetting("Only Work With Item List", "The items to only work with", aitem);
        this.autoBlowUp = new BooleanSetting("Auto Blow Up", true, "Auto blows up an anchor after anchor charged");
        this.finishSlot = new NumberSetting("Finish Slot", 1.0, 9.0, 5.0, 1.0, "Slot to swap before exploding an anchor");
        this.finishSlotOnFail = new BooleanSetting("Finish slot on Fail", true, "Switches to finish slot if glowstone fails to charge anchor");
        this.damageTick = new BooleanSetting("Damage tick", false, "Predicts hurt time to double tap the target");
        this.damageTickChance = new NumberSetting("Damage tick chance", 1.0, 100.0, 85.0, 1.0, "Chance to predict damage tick");
        String[] astring = new String[]{"Air Anchor", "Anchor"};
        this.damageTickAffect = new MultiSelectSetting("Damage tick affect", "Who to predict damage tick for", astring);
        this.airAnchor = new BooleanSetting("Air Anchor", false, "Enable Air Anchoring features in AutoAnchor");
        this.airAnchorChance = new NumberSetting("Air Anchor chance", 0.0, 100.0, 30.0, 1.0, "Chance to do air anchor normally");
        this.airAnchorLimit = new NumberSetting("Air Anchor Limit", 1.0, 10.0, 1.0, 1.0, "The hard limit of air anchors in a row");
        this.forceAirAnchorKey = new KeybindSetting("Force Air Anchor Key", 0, false, "Keybind to force an air anchor");
        this.forceAirAnchorOnProximity = new BooleanSetting("Force Air Anchor on proximity", false, "Enable proximity check to force air anchor");
        this.airAnchorProximityRange = new NumberSetting("Air Anchor Proximity range", 1.0, 10.0, 3.0, 0.1, "Max distance for proximity check");
        this.airAnchorOnKey = new BooleanSetting("Air Anchor on Key", false, "Only air anchors when you press the key");
        this.airAnchorKey = new KeybindSetting("Air Anchor Key", 0, false, "Key to air anchor");
        this.airAnchorDelay = new RangeSetting("Air Anchor Delay", 0.0, 10.0, 1.0, 2.0, 1.0, "Delay before air anchoring");
        this.safePlace = new BooleanSetting("Safe Place", false, "Silent-places a glowstone between you and the anchor before exploding");
        this.safePlaceRot = new ModeSetting("Safe Place Rot", "Silent", "The mode for safe place rotations", new String[]{"Silent", "Legit", "Manual"});
        this.onlySafePlaceOnKey = new BooleanSetting("Only Safe Place On Key", false, "Require a keybind to use safe place");
        this.safePlaceKey = new KeybindSetting("Safe Place Key", 0, false, "Keybind to use safe place");
        this.safeSettleDelay = new RangeSetting("Safe Settle Delay", 1.0, 10.0, 1.0, 1.0, 1.0, "Ticks to wait after each silent rotation settles");
        this.shiftPlace = new BooleanSetting("Shift Place", false, "Lets you place glowstone on the anchor's face while sneaking (sneak bypasses the interact)");
        this.shiftMode = new ModeSetting(
            "Shift Mode", "Auto Shift", "Whether the client sneaks for you or you hold it yourself", new String[]{"Auto Shift", "Manual Shift"}
        );
        this.safePlaceOrder = new ListSetting(
            "Safe Place Order", "Order of charging the anchor and placing the safe glowstone", new String[]{"Charge", "Safe Place"}
        );
        this.safeBlock = new ListSetting("Safe Block", "Fallback order first present in the hotbar is used", new String[]{"Obsidian", "Glowstone"});
        this.forceObsidian = new KeybindSetting("Force Obsidian", 0, false, "Hold to force obsidian as the safe block");
        this.forceGlowstone = new KeybindSetting("Force Glowstone", 0, false, "Hold to force glowstone as the safe block");
        this.tickbaseTest = new BooleanSetting("tickbase test", false, "");
        this.tickCount = new NumberSetting("tick count", 0.0, 10.0, 2.0, 1.0, "");
        this.tickbaseRotateMode = new ModeSetting("tickbase rotate mode", "None", "", new String[]{"None", "Silent", "Legit"});
        this.idleState = new AutoAnchorState(null, null);
        this.explodeWaitState = new AutoAnchorState(
            usekeyevent -> {
                if (mc.crosshairTarget instanceof BlockHitResult blockhitresult
                    && blockhitresult.getType() == Type.BLOCK
                    && blockhitresult.getBlockPos().equals(this.anchorPos)) {
                    if (this.explodeDelayTicks > 0) {
                        int k = this.explodeDelayTicks--;
                        return null;
                    } else if (this.airAnchorStreak > 0 && this.airAnchorDelayTicks > 0) {
                        int j = this.airAnchorDelayTicks--;
                        return null;
                    } else if (this.shouldDamageTick(this.airAnchoring)) {
                        return null;
                    } else {
                        usekeyevent.addTimesPressed(1);
                        if (this.airAnchoring && mc.player.getMainHandStack().isOf(Items.RESPAWN_ANCHOR)) {
                            int i = this.airAnchorStreak++;
                            this.resetDelays();
                            return this.explodeState;
                        } else {
                            return this.idleState;
                        }
                    }
                } else {
                    if (this.retainAnchor.isEnabled() && this.anchorPos != null) {
                        BlockState blockstate = mc.world.getBlockState(this.anchorPos);
                        if (blockstate.getBlock() == Blocks.RESPAWN_ANCHOR && blockstate.get(Properties.CHARGES) > 0) {
                            return this.placeAnchorState;
                        }
                    }

                    return this.idleState;
                }
            },
            null
        );
        this.placeAnchorState = new AutoAnchorState(usekeyevent -> {
            if (this.anchorPos == null || mc.world.getBlockState(this.anchorPos).getBlock() != Blocks.RESPAWN_ANCHOR) {
                return this.idleState;
            }

            if (!(mc.crosshairTarget instanceof BlockHitResult blockhitresult) || blockhitresult.getType() != Type.BLOCK) {
                return null;
            }

            ItemStack itemstack = mc.player.getMainHandStack();
            if (usekeyevent.getTimesPressed() >= 1 && itemstack.isOf(Items.RESPAWN_ANCHOR)) {
                ItemPlacementContext itemplacementcontext = new ItemPlacementContext(mc.player, Hand.MAIN_HAND, itemstack, blockhitresult);
                if (!itemplacementcontext.canPlace()) {
                    return null;
                }

                BlockState blockstate = mc.world.getBlockState(blockhitresult.getBlockPos());
                if (blockstate.getBlock() == Blocks.RESPAWN_ANCHOR && blockstate.get(Properties.CHARGES) > 0) {
                    this.anchorPos = blockhitresult.getBlockPos();
                } else {
                    this.anchorPos = itemplacementcontext.getBlockPos();
                }

                this.safeBlockPlaced = false;
                this.anchorCharged = false;
                this.safePlacePos = null;
                this.rechargePending = false;
                this.explodeReady = false;
                this.resetDelays();
                return this.selectGlowstoneState;
            }

            if (!blockhitresult.getBlockPos().equals(this.anchorPos) || this.explodeDelayTicks > 0 || this.shouldDamageTick(false)) {
                return null;
            }

            usekeyevent.addTimesPressed(1);
            return this.idleState;
        }, null);
        this.markerStateA = new AutoAnchorState(null, null);
        this.markerStateB = new AutoAnchorState(null, null);
        this.markerStateC = new AutoAnchorState(null, null);
        this.markerStateD = new AutoAnchorState(null, null);
        this.markerStateE = new AutoAnchorState(null, null);
        Function<HotbarKeyEvent, AutoAnchorState> function = hotbarkeyevent -> {
            int i = InventoryUtil.findHotbarSlot(Items.RESPAWN_ANCHOR);
            if (i == -1) {
                this.airAnchoring = false;
            }

            if (!this.airAnchoring || i != hotbarkeyevent.getSlot()) {
                if (this.airAnchoring) {
                    return null;
                }

                int j = this.finishSlot.getInt();
                if (j - 1 != hotbarkeyevent.getSlot()) {
                    return null;
                }
            }

            if (this.finishSwapDelayTicks > 0) {
                int k = this.finishSwapDelayTicks--;
                return null;
            } else {
                hotbarkeyevent.addTimesPressed(1);
                if (!this.autoBlowUp.isEnabled()) {
                    return this.idleState;
                } else if (this.anchorCharged) {
                    return this.markerStateD;
                } else {
                    return this.safeBlockPlaced ? this.placeAnchorState : this.explodeWaitState;
                }
            }
        };
        this.selectAnchorState = new AutoAnchorState(null, function);
        this.safePlaceState = new AutoAnchorState(
            usekeyevent -> {
                if (!this.isExplosionBlockable()) {
                    this.releaseSneak();
                    return this.nextSafePlaceState();
                } else if (!(mc.crosshairTarget instanceof BlockHitResult blockhitresult && blockhitresult.getType() == Type.BLOCK)) {
                    this.releaseSneak();
                    return null;
                } else if (!blockhitresult.getBlockPos().equals(this.anchorPos)) {
                    this.releaseSneak();
                    if (mc.player.getMainHandStack().isOf(this.getSafeBlock())) {
                        ItemPlacementContext itemplacementcontext = new ItemPlacementContext(
                            mc.player, Hand.MAIN_HAND, mc.player.getMainHandStack(), blockhitresult
                        );
                        if (!itemplacementcontext.canPlace() || itemplacementcontext.getBlockPos().equals(this.anchorPos)) {
                            return null;
                        } else if (this.safeSettleTicks > 0) {
                            this.safeSettleTicks--;
                            return null;
                        } else {
                            usekeyevent.addTimesPressed(1);
                            this.safeBlockPlaced = true;
                            this.safePlacePos = itemplacementcontext.getBlockPos();
                            return this.nextSafePlaceState();
                        }
                    } else {
                        if (InventoryUtil.findHotbarSlot(this.getSafeBlock()) == -1) {
                            return this.nextSafePlaceState();
                        }

                        Item item1 = this.getSafeBlock();
                        return this.selectItem(item1, this.safePlaceState);
                    }
                } else if (!this.shiftPlace.isEnabled()) {
                    if (this.rechargePending) {
                        BlockState blockstate = mc.world.getBlockState(this.anchorPos);
                        if (blockstate.getBlock() != Blocks.RESPAWN_ANCHOR) {
                            return null;
                        }

                        if (!mc.player.getMainHandStack().isOf(Items.GLOWSTONE)) {
                            if (InventoryUtil.findHotbarSlot(Items.GLOWSTONE) == -1) {
                                this.rechargePending = false;
                                return null;
                            }

                            return this.selectItem(Items.GLOWSTONE, this.safePlaceState);
                        }

                        if (this.chargeDelayTicks > 0) {
                            int j = this.chargeDelayTicks--;
                            return null;
                        }

                        if (blockstate.get(Properties.CHARGES) != 4) {
                            usekeyevent.addTimesPressed(1);
                        }

                        this.rechargePending = false;
                    }

                    return null;
                } else if (mc.player.getMainHandStack().isOf(this.getSafeBlock())) {
                    ItemPlacementContext itemplacementcontext1 = new ItemPlacementContext(
                        mc.player, Hand.MAIN_HAND, mc.player.getMainHandStack(), blockhitresult
                    );
                    if (!itemplacementcontext1.canPlace() || itemplacementcontext1.getBlockPos().equals(this.anchorPos)) {
                        this.releaseSneak();
                        return null;
                    }

                    if (!mc.player.isSneaking()) {
                        if (this.shiftMode.is("Auto Shift")) {
                            mc.options.sneakKey.setPressed(true);
                            this.sneakingByModule = true;
                        }

                        return null;
                    } else if (this.safeSettleTicks > 0) {
                        int i = this.safeSettleTicks--;
                        return null;
                    } else {
                        usekeyevent.addTimesPressed(1);
                        this.safeBlockPlaced = true;
                        this.safePlacePos = itemplacementcontext1.getBlockPos();
                        return this.nextSafePlaceState();
                    }
                } else if (InventoryUtil.findHotbarSlot(this.getSafeBlock()) == -1) {
                    this.releaseSneak();
                    return this.nextSafePlaceState();
                } else {
                    Item item = this.getSafeBlock();
                    return this.selectItem(item, this.safePlaceState);
                }
            },
            null
        );
        this.chargeState = new AutoAnchorState(usekeyevent -> {
            if (!(mc.crosshairTarget instanceof BlockHitResult blockhitresult && blockhitresult.getType() == Type.BLOCK)) {
                return null;
            } else {
                BlockState blockstate;
                if ((blockstate = mc.world.getBlockState(blockhitresult.getBlockPos())).getBlock() == Blocks.RESPAWN_ANCHOR) {
                    if (this.chargeDelayTicks > 0) {
                        int i = this.chargeDelayTicks--;
                        return null;
                    }

                    if (blockstate.get(Properties.CHARGES) != 4) {
                        usekeyevent.addTimesPressed(1);
                    }

                    if (!this.explodeReady) {
                        this.airAnchoring = this.shouldAirAnchor();
                        if (!this.airAnchoring && this.shouldSafePlace()) {
                            return this.beginSafePlace(this.selectAnchorState);
                        }
                    }

                    return this.selectAnchorState;
                } else {
                    if (this.rechargePending) {
                        return null;
                    }

                    usekeyevent.addTimesPressed(1);
                    return this.finishSlotOnFail.isEnabled() ? this.selectAnchorState : this.idleState;
                }
            }
        }, null);
        Function<HotbarKeyEvent, AutoAnchorState> function1 = hotbarkeyevent -> {
            ItemStack itemstack = mc.player.getInventory().getStack(hotbarkeyevent.getSlot());
            if (itemstack.isOf(Items.GLOWSTONE)) {
                if (this.swapDelayTicks > 0) {
                    int i = this.swapDelayTicks--;
                    return null;
                }

                hotbarkeyevent.addTimesPressed(1);
                if ("Safe Place".equals(this.safePlaceOrder.getFirst()) && this.shouldSafePlace()) {
                    this.explodeReady = true;
                    this.airAnchoring = this.shouldAirAnchor();
                    if (!this.airAnchoring) {
                        this.rechargePending = true;
                        return this.beginSafePlace(this.chargeState);
                    }
                }

                return this.chargeState;
            } else {
                return null;
            }
        };
        this.selectGlowstoneState = new AutoAnchorState(null, function1);
        Function<HotbarKeyEvent, AutoAnchorState> function2 = hotbarkeyevent -> {
            Item item = this.pendingItem;
            if (item == null) {
                return this.afterSwapState != null ? this.afterSwapState : this.idleState;
            }

            if (!mc.player.getInventory().getStack(hotbarkeyevent.getSlot()).isOf(item)) {
                return null;
            }

            if (this.swapTicks > 0) {
                this.swapTicks--;
                return null;
            }

            hotbarkeyevent.addTimesPressed(1);
            return this.afterSwapState != null ? this.afterSwapState : this.idleState;
        };
        this.swapItemState = new AutoAnchorState(null, function2);
        this.explodeState = new AutoAnchorState(usekeyevent -> {
            if (!(mc.crosshairTarget instanceof BlockHitResult blockhitresult) || blockhitresult.getType() != Type.BLOCK) {
                return this.idleState;
            }

            ItemPlacementContext itemplacementcontext = new ItemPlacementContext(mc.player, Hand.MAIN_HAND, mc.player.getMainHandStack(), blockhitresult);
            if (!itemplacementcontext.canPlace()) {
                return null;
            }

            BlockState blockstate = mc.world.getBlockState(blockhitresult.getBlockPos());
            if (usekeyevent.getTimesPressed() >= 1
                && mc.player.getMainHandStack().isOf(Items.RESPAWN_ANCHOR)
                && blockstate.getBlock() == Blocks.RESPAWN_ANCHOR
                && blockstate.get(Properties.CHARGES) > 0) {
                this.anchorPos = blockhitresult.getBlockPos();
            } else {
                this.anchorPos = itemplacementcontext.getBlockPos();
            }

            usekeyevent.addTimesPressed(1);
            return this.selectGlowstoneState;
        }, null);
        Function<HotbarKeyEvent, AutoAnchorState> function3 = hotbarkeyevent -> {
            ItemStack itemstack = mc.player.getInventory().getStack(hotbarkeyevent.getSlot());
            if (!itemstack.isOf(Items.RESPAWN_ANCHOR)) {
                return null;
            }

            hotbarkeyevent.addTimesPressed(1);
            return this.explodeState;
        };
        this.selectExplodeAnchorState = new AutoAnchorState(null, function3);
        this.initialState = new AutoAnchorState(
            usekeyevent -> {
                ItemStack itemstack;
                if (usekeyevent.getTimesPressed() >= 1
                    && (itemstack = mc.player.getMainHandStack()).isOf(Items.RESPAWN_ANCHOR)
                    && mc.crosshairTarget instanceof BlockHitResult blockhitresult
                    && blockhitresult.getType() == Type.BLOCK) {
                    ItemPlacementContext itemplacementcontext = new ItemPlacementContext(mc.player, Hand.MAIN_HAND, itemstack, blockhitresult);
                    if (itemplacementcontext.canPlace()) {
                        this.anchorPos = itemplacementcontext.getBlockPos();
                        int i = this.getTickbaseCount();
                        if (this.tickbaseTest.isEnabled()) {
                            TickFastForward.requestTicks(new TickRequest(i, i, true));
                        }

                        return this.selectGlowstoneState;
                    }
                }

                return null;
            },
            null
        );
        this.holdActivationState = new AutoAnchorState(
            usekeyevent -> {
                if (mc.crosshairTarget instanceof BlockHitResult blockhitresult && blockhitresult.getType() == Type.BLOCK) {
                    BlockState blockstate = mc.world.getBlockState(blockhitresult.getBlockPos());
                    if (blockstate.getBlock() == Blocks.RESPAWN_ANCHOR) {
                        this.anchorPos = blockhitresult.getBlockPos().toImmutable();
                        if (blockstate.get(Properties.CHARGES) > 0) {
                            this.explodeReady = true;
                            this.airAnchoring = this.shouldAirAnchor();
                            return this.autoBlowUp.isEnabled() ? this.selectAnchorState : this.idleState;
                        } else {
                            return this.selectGlowstoneState;
                        }
                    } else {
                        return new ItemPlacementContext(mc.player, Hand.MAIN_HAND, new ItemStack(Items.RESPAWN_ANCHOR), blockhitresult).canPlace()
                            ? this.selectExplodeAnchorState
                            : null;
                    }
                } else {
                    return null;
                }
            },
            null
        );
        Setting[] asetting = new Setting[]{
            this.activation,
            this.explodeHoldTicks,
            this.chargeDelay,
            this.swapDelay,
            this.explodeDelay,
            this.ignoreDoubleHand,
            this.retainAnchor,
            this.anchorKey,
            this.anchorKeybind,
            this.onlyWorkWithItem,
            this.onlyWorkWithItemList,
            this.autoBlowUp,
            this.finishSlot,
            this.finishSlotOnFail,
            this.damageTick,
            this.damageTickChance,
            this.damageTickAffect,
            this.airAnchor,
            this.airAnchorChance,
            this.airAnchorLimit,
            this.forceAirAnchorKey,
            this.forceAirAnchorOnProximity,
            this.airAnchorProximityRange,
            this.airAnchorOnKey,
            this.airAnchorKey,
            this.airAnchorDelay,
            this.safePlace,
            this.safePlaceOrder,
            this.safeBlock,
            this.forceObsidian,
            this.forceGlowstone,
            this.shiftPlace,
            this.shiftMode,
            this.safePlaceRot,
            this.onlySafePlaceOnKey,
            this.safePlaceKey,
            this.safeSettleDelay
        };
        this.addSettings(asetting);
        BooleanSetting booleansetting1 = this.damageTick;
        BooleanSupplier booleansupplier4 = this.autoBlowUp::isEnabled;
        booleansetting1.setVisibleWhen(booleansupplier4);
        NumberSetting numbersetting1 = this.damageTickChance;
        BooleanSupplier booleansupplier5 = () -> this.autoBlowUp.isEnabled() && this.damageTick.isEnabled();
        numbersetting1.setVisibleWhen(booleansupplier5);
        MultiSelectSetting multiselectsetting = this.damageTickAffect;
        BooleanSupplier booleansupplier6 = () -> this.autoBlowUp.isEnabled() && this.damageTick.isEnabled();
        multiselectsetting.setVisibleWhen(booleansupplier6);
        KeybindSetting keybindsetting1 = this.anchorKeybind;
        BooleanSupplier booleansupplier7 = this.anchorKey::isEnabled;
        keybindsetting1.setVisibleWhen(booleansupplier7);
        booleansetting1 = this.onlyWorkWithItem;
        BooleanSupplier booleansupplier8 = this.anchorKey::isEnabled;
        booleansetting1.setVisibleWhen(booleansupplier8);
        ItemListSetting itemlistsetting = this.onlyWorkWithItemList;
        BooleanSupplier booleansupplier9 = this.onlyWorkWithItem::isEnabled;
        itemlistsetting.setVisibleWhen(booleansupplier9);
        booleansetting1 = this.airAnchor;
        BooleanSupplier booleansupplier10 = this.autoBlowUp::isEnabled;
        booleansetting1.setVisibleWhen(booleansupplier10);
        Predicate predicate = v -> this.autoBlowUp.isEnabled() && this.airAnchor.isEnabled();
        BooleanSupplier booleansupplier = () -> predicate.test(null);
        this.airAnchorChance.setVisibleWhen(booleansupplier);
        this.airAnchorLimit.setVisibleWhen(booleansupplier);
        this.forceAirAnchorKey.setVisibleWhen(booleansupplier);
        this.forceAirAnchorOnProximity.setVisibleWhen(booleansupplier);
        BooleanSupplier booleansupplier1 = () -> predicate.and(v -> this.forceAirAnchorOnProximity.isEnabled()).test(null);
        this.airAnchorProximityRange.setVisibleWhen(booleansupplier1);
        this.airAnchorOnKey.setVisibleWhen(booleansupplier);
        this.airAnchorKey.setVisibleWhen(booleansupplier);
        this.airAnchorDelay.setVisibleWhen(booleansupplier);
        BooleanSetting booleansetting = this.safePlace;
        BooleanSupplier booleansupplier11 = this.autoBlowUp::isEnabled;
        booleansetting.setVisibleWhen(booleansupplier11);
        BooleanSupplier booleansupplier2 = () -> this.autoBlowUp.isEnabled() && this.safePlace.isEnabled();
        this.onlySafePlaceOnKey.setVisibleWhen(booleansupplier2);
        KeybindSetting keybindsetting = this.safePlaceKey;
        BooleanSupplier booleansupplier12 = () -> this.onlySafePlaceOnKey.isEnabled() && booleansupplier2.getAsBoolean();
        keybindsetting.setVisibleWhen(booleansupplier12);
        this.safeSettleDelay.setVisibleWhen(booleansupplier2);
        this.safePlaceRot.setVisibleWhen(booleansupplier2);
        this.safePlaceOrder.setVisibleWhen(booleansupplier2);
        this.safeBlock.setVisibleWhen(booleansupplier2);
        this.forceObsidian.setVisibleWhen(booleansupplier2);
        this.forceGlowstone.setVisibleWhen(booleansupplier2);
        this.forceObsidian.setHold(true);
        this.forceGlowstone.setHold(true);
        BooleanSupplier booleansupplier3 = () -> booleansupplier2.getAsBoolean() && this.safePlaceRot.is("Manual");
        this.shiftPlace.setVisibleWhen(booleansupplier3);
        ModeSetting modesetting = this.shiftMode;
        BooleanSupplier booleansupplier13 = () -> booleansupplier3.getAsBoolean() && this.shiftPlace.isEnabled();
        modesetting.setVisibleWhen(booleansupplier13);
        NumberSetting numbersetting = this.explodeHoldTicks;
        BooleanSupplier booleansupplier14 = () -> this.activation.is("Hold") && this.autoBlowUp.isEnabled();
        numbersetting.setVisibleWhen(booleansupplier14);
    }

    public void resetDelays() {
        this.chargeDelayTicks = this.chargeDelay.getRandomInt();
        this.swapDelayTicks = this.swapDelay.getRandomInt();
        this.finishSwapDelayTicks = this.swapDelay.getRandomInt();
        this.explodeDelayTicks = this.explodeDelay.getRandomInt();
        this.airAnchorDelayTicks = this.airAnchorDelay.getRandomInt();
        this.swapTicks = this.swapDelay.getRandomInt();
    }

    private void releaseSneak() {
        if (this.sneakingByModule) {
            this.sneakingByModule = false;
            mc.options.sneakKey.setPressed(false);
        }
    }

    private boolean findSafePlacement() {
        if (this.anchorPos != null && mc.player != null && mc.world != null) {
            Vec3d vec3d = mc.player.getEntityPos();
            double d0 = vec3d.x - (this.anchorPos.getX() + 0.5);
            double d1 = vec3d.z - (this.anchorPos.getZ() + 0.5);
            Direction direction;
            if (Math.abs(d0) > Math.abs(d1)) {
                direction = d0 > 0.0 ? Direction.EAST : Direction.WEST;
            } else {
                direction = d1 > 0.0 ? Direction.SOUTH : Direction.NORTH;
            }

            BlockPos blockpos = this.anchorPos.offset(direction);
            if (!mc.world.getBlockState(blockpos).isReplaceable()) {
                return false;
            }

            for (Direction direction1 : new Direction[]{
                direction, Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.UP
            }) {
                BlockPos blockpos1 = blockpos.offset(direction1);
                if (!blockpos1.equals(this.anchorPos)) {
                    BlockState blockstate = mc.world.getBlockState(blockpos1);
                    if (!blockstate.isAir() && !blockstate.isReplaceable() && blockstate.isSolidBlock(mc.world, blockpos1)) {
                        this.safeSupportPos = blockpos1;
                        this.safeFace = direction1.getOpposite();
                        this.safeHitVec = Vec3d.ofCenter(blockpos1)
                            .add(this.safeFace.getOffsetX() * 0.5, this.safeFace.getOffsetY() * 0.5, this.safeFace.getOffsetZ() * 0.5);
                        this.safePlacePos = blockpos;
                        return true;
                    }
                }
            }

            return false;
        } else {
            return false;
        }
    }

    private boolean shouldSafePlace() {
        return this.safePlace.isEnabled() && this.autoBlowUp.isEnabled() && (!this.onlySafePlaceOnKey.isEnabled() || this.safePlaceKey.isPressed());
    }

    private AutoAnchorState nextSafePlaceState() {
        return this.rechargePending ? this.selectItem(Items.GLOWSTONE, this.chargeState) : this.selectAnchorState;
    }

    private Item chooseSafeBlock() {
        Item item = null;
        if (this.forceObsidian.getKey() != 0 && this.forceObsidian.isPressed()) {
            item = Items.OBSIDIAN;
        } else if (this.forceGlowstone.getKey() != 0 && this.forceGlowstone.isPressed()) {
            item = Items.GLOWSTONE;
        }

        if (item != null && InventoryUtil.findHotbarSlot(item) != -1) {
            return item;
        }

        for (String s : this.safeBlock.getItems()) {
            Item item1 = getItemByName(s);
            if (item1 != null && InventoryUtil.findHotbarSlot(item1) != -1) {
                return item1;
            }
        }

        Item item2 = Items.GLOWSTONE;
        if (InventoryUtil.findHotbarSlot(item2) != -1) {
            return Items.GLOWSTONE;
        }

        String s1 = this.safeBlock.getFirst();
        Item item3 = getItemByName(s1);
        return item3 != null ? item3 : Items.GLOWSTONE;
    }

    private boolean canPlaceAnchorAt(BlockPos blockpos) {
        double d0 = mc.player.getBlockInteractionRange();
        Vec3d vec3d = mc.player.getCameraPosVec(1.0F);

        for (Direction direction : Direction.values()) {
            BlockPos blockpos1 = blockpos.offset(direction);
            boolean flag = blockpos1.equals(this.anchorPos);
            if (!flag || this.shiftPlace.isEnabled()) {
                BlockState blockstate = mc.world.getBlockState(blockpos1);
                if (flag || blockstate.isSolidBlock(mc.world, blockpos1)) {
                    Direction direction1 = direction.getOpposite();
                    Vec3d vec3d1 = Vec3d.ofCenter(blockpos1).add(direction1.getOffsetX() * 0.5, direction1.getOffsetY() * 0.5, direction1.getOffsetZ() * 0.5);
                    if (!(vec3d.squaredDistanceTo(vec3d1) > d0 * d0)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private boolean canPlaceGlowstoneAt(BlockPos blockpos) {
        double d0 = mc.player.getBlockInteractionRange();
        Vec3d vec3d = mc.player.getCameraPosVec(1.0F);

        for (Direction direction : Direction.values()) {
            BlockPos blockpos1 = this.anchorPos.offset(direction);
            if (!blockpos1.equals(blockpos)) {
                BlockState blockstate = mc.world.getBlockState(blockpos1);
                if (blockstate.isAir() || blockstate.isReplaceable()) {
                    Vec3d vec3d1 = Vec3d.ofCenter(this.anchorPos).add(direction.getOffsetX() * 0.5, direction.getOffsetY() * 0.5, direction.getOffsetZ() * 0.5);
                    if (!(vec3d.squaredDistanceTo(vec3d1) > d0 * d0)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public void onEnable() {
        this.reset();
    }

    @Override
    public void onDisable() {
        VoilClient.getRotationController().release(this);
    }

    private AutoAnchorState beginSafePlace(AutoAnchorState conditionnodebinary) {
        this.safeSettleTicks = this.safeSettleDelay.getRandomInt();
        this.safePlacePos = null;
        this.returnState = conditionnodebinary;
        this.safeBlockItem = this.chooseSafeBlock();
        if (this.safePlaceRot.is("Manual")) {
            return this.isExplosionBlockable() ? this.safePlaceState : conditionnodebinary;
        }

        Item item = this.safeBlockItem;
        return this.selectItem(item, this.markerStateA);
    }

    private void requestRotation(Supplier<Rotation> supplier, int i, Supplier<Boolean> supplier1) {
        SmoothRotationController smoothrotationcontroller = VoilClient.getRotationController();
        Supplier supplier5 = SmoothRotationController.smoothForTicks(i);
        boolean flag1 = this.safePlaceRot.is("Silent");
        RotationAxisMode rotationaxismode1 = RotationAxisMode.YawAndPitch;
        EasingFunction easingfunction = UiEasings::linear;
        RotationAxisMode rotationaxismode = rotationaxismode1;
        Object object = null;
        boolean flag = flag1;
        Supplier supplier2 = supplier1;
        Supplier supplier3 = supplier;
        Supplier supplier4 = supplier5;
        AutoAnchor autoanchor1 = this;
        smoothrotationcontroller.startWith(
            autoanchor1, supplier4, supplier3, supplier2, flag, false, (SmoothRotationState)object, rotationaxismode, easingfunction
        );
    }

    public void reset() {
        this.previewKeyHandled = false;
        this.anchorPos = null;
        this.currentState = this.initialState;
        this.airAnchorStreak = 0;
        this.forcedAirAnchor = false;
        this.airAnchoring = false;
        this.safePlacePos = null;
        this.safeSupportPos = null;
        this.safeFace = null;
        this.safeHitVec = null;
        this.safeSettleTicks = 0;
        this.anchorCharged = false;
        this.previewTriggered = false;
        this.safeBlockPlaced = false;
        this.rechargePending = false;
        this.explodeReady = false;
        this.holdHandled = false;
        this.safeBlockItem = null;
        this.pendingItem = null;
        this.afterSwapState = null;
        this.returnState = null;
        VoilClient.getRotationController().release(this);
        this.resetDelays();
        if (this.sneakingByModule && this.currentState != this.safePlaceState) {
            this.releaseSneak();
        }
    }

    private AutoAnchorState selectItem(Item item, AutoAnchorState conditionnodebinary) {
        if (mc.player.getMainHandStack().isOf(item)) {
            return conditionnodebinary;
        }

        if (InventoryUtil.findHotbarSlot(item) == -1) {
            return conditionnodebinary;
        }

        this.pendingItem = item;
        this.afterSwapState = conditionnodebinary;
        this.swapTicks = this.swapDelay.getRandomInt();
        return this.swapItemState;
    }

    private Item getSafeBlock() {
        return this.safeBlockItem != null ? this.safeBlockItem : Items.GLOWSTONE;
    }

    public BlockPos getAnchorPos() {
        return this.anchorPos;
    }

    private Set<BlockPos> replaceable(Vec3d vec3d, Vec3d vec3d1) {
        HashSet<BlockPos> hashset = new HashSet<>();
        Box box = mc.player.getBoundingBox();
        BlockView.raycast(vec3d, vec3d1, null, (v, blockpos) -> {
            if (!blockpos.equals(this.anchorPos) && mc.world.getBlockState(blockpos).isReplaceable() && !box.intersects(new Box(blockpos))) {
                hashset.add(blockpos.toImmutable());
            }

            return null;
        }, v -> null);
        return hashset;
    }

    private boolean isAllowed() {
        return true;
    }

    private void onAnchorKey(KeybindSetting keybindsetting) {
        if (((KeyBindingAccessor)mc.options.useKey).getBoundKey().getCode() == keybindsetting.getKey()) {
            mc.options.useKey.setPressed(false);
            mc.options.useKey.wasPressed();
        }

        this.reset();
        this.currentState = this.selectExplodeAnchorState;
        if (keybindsetting == this.anchorKeybind) {
            if (this.tickbaseTest.isEnabled()) {
                int i = this.getTickbaseCount();
                TickFastForward.requestTicks(new TickRequest(i, i, true));
            }
        }
    }

    private boolean shouldDamageTick(boolean flag) {
        if (!this.damageTick.isEnabled()) {
            return false;
        }

        String s = flag ? "Air Anchor" : "Anchor";
        if (!this.damageTickAffect.isSelected(s)) {
            return false;
        }

        if (AutoCrystal2.damageTick(false)) {
            double d0 = (int)this.damageTickChance.getValue();
            if (AimMathUtil.rollChance(d0)) {
                return true;
            }
        }

        return false;
    }

    private static Item getItemByName(String s) {
        if (s == null) {
            return null;
        }

        return switch (s) {
            case "Obsidian" -> Items.OBSIDIAN;
            case "Glowstone" -> Items.GLOWSTONE;
            default -> null;
        };
    }

    private boolean canActivate() {
        if (Module.isNotInGame()) {
            return false;
        } else if (InventoryUtil.findHotbarSlot(Items.RESPAWN_ANCHOR) == -1) {
            return false;
        } else {
            return InventoryUtil.findHotbarSlot(Items.GLOWSTONE) == -1 ? false : !(mc.currentScreen instanceof HandledScreen);
        }
    }

    private boolean shouldAirAnchor() {
        if (!this.airAnchor.isEnabled() || !this.autoBlowUp.isEnabled() || this.airAnchorStreak >= this.airAnchorLimit.getInt()) {
            return false;
        }

        if (this.forcedAirAnchor) {
            return true;
        }

        if (this.airAnchorOnKey.isEnabled()) {
            return false;
        }

        if (!this.forceAirAnchorKey.isPressed() && (!this.forceAirAnchorOnProximity.isEnabled() || !this.proximityCheck())) {
            double d0 = (int)this.airAnchorChance.getValue();
            if (!AimMathUtil.rollChance(d0)) {
                return false;
            }
        }

        return true;
    }

    private boolean isExplosionBlockable() {
        if (this.anchorPos == null) {
            return false;
        }

        Vec3d vec3d = this.anchorPos.toCenterPos();
        Box box = mc.player.getBoundingBox();
        ArrayList<Vec3d> arraylist = new ArrayList<>();

        for (Vec3d vec3d1 : this.getBoxSamplePoints(box)) {
            if (!this.isPathObstructed(vec3d1, vec3d)) {
                arraylist.add(vec3d1);
            }
        }

        if (arraylist.isEmpty()) {
            return false;
        }

        Set<BlockPos> set1 = null;

        for (Vec3d vec3d2 : arraylist) {
            Set<BlockPos> set = this.replaceable(vec3d2, vec3d);
            if (set.isEmpty()) {
                return false;
            }

            if (set1 == null) {
                set1 = set;
            } else {
                set1.retainAll(set);
            }

            if (set1.isEmpty()) {
                return false;
            }
        }

        for (BlockPos blockpos : set1) {
            if (this.canPlaceAnchorAt(blockpos) && this.canPlaceGlowstoneAt(blockpos)) {
                return true;
            }
        }

        return false;
    }

    private void triggerTickbase() {
        this.reset();
        this.holdHandled = true;
        this.holdTicks = 0;
        mc.options.useKey.setPressed(false);
        if (this.tickbaseTest.isEnabled()) {
            int i = this.getTickbaseCount();
            TickFastForward.requestTicks(new TickRequest(i, i, true));
        }
    }

    private int getTickbaseCount() {
        return this.tickCount.getInt();
    }

    private AutoAnchorState startSequence(UseKeyEvent usekeyevent) {
        if (mc.crosshairTarget instanceof BlockHitResult blockhitresult && blockhitresult.getType() == Type.BLOCK) {
            BlockPos blockpos = blockhitresult.getBlockPos();
            BlockState blockstate = mc.world.getBlockState(blockpos);
            if (blockstate.getBlock() == Blocks.RESPAWN_ANCHOR) {
                usekeyevent.setTimesPressed(0);
                this.triggerTickbase();
                this.anchorPos = blockpos.toImmutable();
                if (blockstate.get(Properties.CHARGES) > 0) {
                    this.explodeReady = true;
                    this.airAnchoring = this.shouldAirAnchor();
                    this.holdTicks = this.explodeHoldTicks.getInt();
                    return this.autoBlowUp.isEnabled() ? this.selectAnchorState : this.idleState;
                } else {
                    return this.selectGlowstoneState;
                }
            } else {
                ItemStack itemstack = mc.player.getMainHandStack();
                if (!itemstack.isOf(Items.RESPAWN_ANCHOR)) {
                    return null;
                }

                ItemPlacementContext itemplacementcontext = new ItemPlacementContext(mc.player, Hand.MAIN_HAND, itemstack, blockhitresult);
                if (!itemplacementcontext.canPlace()) {
                    return null;
                }

                boolean flag = usekeyevent.getTimesPressed() >= 1;
                this.triggerTickbase();
                this.anchorPos = itemplacementcontext.getBlockPos();
                if (!flag) {
                    usekeyevent.addTimesPressed(1);
                }

                return this.selectGlowstoneState;
            }
        } else {
            return null;
        }
    }

    private boolean isUseKeyHeld() {
        Key key = ((KeyBindingAccessor)mc.options.useKey).getBoundKey();
        long i = mc.getWindow().getHandle();
        if (key.getCategory() == net.minecraft.client.util.InputUtil.Type.MOUSE) {
            return GLFW.glfwGetMouseButton(i, key.getCode()) == 1;
        } else {
            return key.getCategory() == net.minecraft.client.util.InputUtil.Type.KEYSYM ? GLFW.glfwGetKey(i, key.getCode()) == 1 : false;
        }
    }

    private boolean isHoldingAllowedItem() {
        if (!this.onlyWorkWithItem.isEnabled()) {
            return true;
        }

        if (mc.player != null) {
            ItemListSetting itemlistsetting = this.onlyWorkWithItemList;
            Item item1 = mc.player.getMainHandStack().getItem();
            if (itemlistsetting.matches(item1)) {
                return true;
            }
        }

        return false;
    }

    private boolean interactAnchor() {
        Direction direction = Direction.UP;

        for (Direction direction1 : Direction.values()) {
            BlockState blockstate = mc.world.getBlockState(this.anchorPos.offset(direction1));
            if (blockstate.isAir() || blockstate.isReplaceable()) {
                direction = direction1;
                break;
            }
        }

        Vec3d vec3d = Vec3d.ofCenter(this.anchorPos).add(direction.getOffsetX() * 0.5, direction.getOffsetY() * 0.5, direction.getOffsetZ() * 0.5);
        BlockHitResult blockhitresult = new BlockHitResult(vec3d, direction, this.anchorPos, false);
        ActionResult actionresult = mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, blockhitresult);
        if (actionresult.isAccepted()) {
            mc.player.swingHand(Hand.MAIN_HAND);
            return true;
        } else {
            return false;
        }
    }

    @EventHandler
    public void onTick(TickEvent tickevent) {
        if (mc.player != null && mc.world != null) {
            if (this.updateHoldActivation()) {
                if (this.holdHandled) {
                    int i = this.holdTicks++;
                }

                if (this.currentState == this.idleState) {
                    this.onDisable();
                } else {
                    if (this.currentState == this.markerStateA) {
                        if (this.safePlacePos == null && !this.findSafePlacement()) {
                            this.onDisable();
                            this.currentState = this.selectAnchorState;
                            return;
                        }

                        Supplier supplier = () -> {
                            Vec3d vec3d3 = mc.player.getCameraPosVec(SystemUtil.getTickDelta());
                            Vec3d vec3d2 = this.safeHitVec;
                            return Rotation.fromPositions(vec3d3, vec3d2);
                        };
                        int i2 = this.safeSettleTicks;
                        Supplier supplier1 = () -> this.currentState != this.markerStateA && this.currentState != this.markerStateB;
                        this.requestRotation(supplier, i2, supplier1);
                        Rotation rotation3 = (Rotation)supplier.get();
                        if (this.isAllowed()) {
                            if (this.safeSettleTicks > 0) {
                                int l = this.safeSettleTicks--;
                                return;
                            }

                            this.currentState = this.markerStateB;
                        }
                    } else if (this.currentState == this.markerStateB) {
                        ItemStack itemstack = mc.player.getMainHandStack();
                        if (!itemstack.isOf(this.getSafeBlock()) || this.safeHitVec == null || this.safeSupportPos == null || this.safeFace == null) {
                            this.currentState = this.idleState;
                            this.safeSettleTicks = this.safeSettleDelay.getRandomInt();
                            return;
                        }

                        BlockHitResult blockhitresult = new BlockHitResult(this.safeHitVec, this.safeFace, this.safeSupportPos, false);
                        ActionResult actionresult = mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, blockhitresult);
                        if (actionresult.isAccepted()) {
                            mc.player.swingHand(Hand.MAIN_HAND);
                        }

                        this.explodeDelayTicks = Math.max(1, this.explodeDelay.getRandomInt());
                        this.currentState = this.markerStateC;
                    } else if (this.currentState == this.markerStateC) {
                        if (this.anchorPos == null) {
                            this.onDisable();
                            this.currentState = this.idleState;
                            return;
                        }

                        Supplier supplier3 = () -> {
                            Vec3d vec3d3 = mc.player.getCameraPosVec(SystemUtil.getTickDelta());
                            Vec3d vec3d2 = this.anchorPos.toCenterPos().add(0.0, 0.5, 0.0);
                            return Rotation.fromPositions(vec3d3, vec3d2);
                        };
                        int l1 = this.explodeDelayTicks;
                        Supplier supplier2 = () -> this.currentState != this.markerStateC
                            && this.currentState != this.selectAnchorState
                            && this.currentState != this.markerStateD
                            && this.currentState != this.markerStateE;
                        this.requestRotation(supplier3, l1, supplier2);
                        Rotation rotation2 = (Rotation)supplier3.get();
                        if (this.isAllowed()) {
                            if (this.explodeDelayTicks > 0) {
                                int i1 = this.explodeDelayTicks--;
                                return;
                            }

                            this.anchorCharged = true;
                            this.currentState = this.rechargePending ? this.selectItem(Items.GLOWSTONE, this.markerStateE) : this.selectAnchorState;
                        }
                    } else if (this.currentState == this.markerStateE) {
                        if (this.anchorPos == null
                            || mc.world.getBlockState(this.anchorPos).getBlock() != Blocks.RESPAWN_ANCHOR
                            || !mc.player.getMainHandStack().isOf(Items.GLOWSTONE)) {
                            this.onDisable();
                            this.currentState = this.idleState;
                            return;
                        }

                        if (this.chargeDelayTicks > 0) {
                            int j1 = this.chargeDelayTicks--;
                            return;
                        }

                        if (mc.world.getBlockState(this.anchorPos).get(Properties.CHARGES) < 4) {
                            this.interactAnchor();
                        }

                        this.currentState = this.selectAnchorState;
                    } else if (this.currentState == this.markerStateD) {
                        if (this.anchorPos == null || mc.world == null) {
                            this.onDisable();
                            this.currentState = this.idleState;
                            return;
                        }

                        BlockState blockstate = mc.world.getBlockState(this.anchorPos);
                        if (blockstate.getBlock() != Blocks.RESPAWN_ANCHOR) {
                            this.onDisable();
                            this.currentState = this.idleState;
                            return;
                        }

                        if (this.shouldDamageTick(false)) {
                            return;
                        }

                        this.interactAnchor();
                        this.onDisable();
                        this.currentState = this.idleState;
                    } else if (this.currentState == this.placeAnchorState) {
                        if (this.explodeDelayTicks > 0) {
                            int k1 = this.explodeDelayTicks--;
                        }
                    } else {
                        if (!this.tickbaseTest.isEnabled() || this.anchorPos == null) {
                            return;
                        }

                        Box box = new Box(Vec3d.of(this.anchorPos), Vec3d.of(this.anchorPos.add(1, 1, 1))).shrink(0.2, 0.2, 0.2);
                        Vec3d vec3d1 = mc.player.getCameraPosVec(SystemUtil.getTickDelta());
                        Vec3d vec3d = AimMathUtil.closestPointOnBoxFromCamera(box);
                        Rotation rotation1 = Rotation.fromPositions(vec3d1, vec3d);
                        switch (this.tickbaseRotateMode.getMode()) {
                            case "Silent" -> VoilClient.getRotationManager().requestFromCallerDefault(rotation1);
                            case "Legit" -> {
                                ClientPlayerEntity clientplayerentity = mc.player;
                                rotation1.applyTo(clientplayerentity);
                            }
                            default -> {
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean proximityCheck() {
        return mc.world
            .getPlayers()
            .parallelStream()
            .filter(abstractclientplayerentity -> abstractclientplayerentity != mc.player)
            .anyMatch(
                abstractclientplayerentity -> abstractclientplayerentity.squaredDistanceTo(this.anchorPos.toCenterPos())
                    < this.airAnchorProximityRange.getValue() * this.airAnchorProximityRange.getValue()
            );
    }

    private List<Vec3d> getBoxSamplePoints(Box box) {
        ArrayList<Vec3d> arraylist = new ArrayList<>();
        double d0 = 1.0 / ((box.maxX - box.minX) * 2.0 + 1.0);
        double d1 = 1.0 / ((box.maxY - box.minY) * 2.0 + 1.0);
        double d2 = 1.0 / ((box.maxZ - box.minZ) * 2.0 + 1.0);
        double d3 = (1.0 - Math.floor(1.0 / d0) * d0) / 2.0;
        double d4 = (1.0 - Math.floor(1.0 / d2) * d2) / 2.0;

        for (double d5 = 0.0; d5 <= 1.0; d5 += d0) {
            for (double d6 = 0.0; d6 <= 1.0; d6 += d1) {
                for (double d7 = 0.0; d7 <= 1.0; d7 += d2) {
                    arraylist.add(
                        new Vec3d(
                            MathHelper.lerp(d5, box.minX, box.maxX) + d3, MathHelper.lerp(d6, box.minY, box.maxY), MathHelper.lerp(d7, box.minZ, box.maxZ) + d4
                        )
                    );
                }
            }
        }

        return arraylist;
    }

    private boolean isPathObstructed(Vec3d vec3d, Vec3d vec3d1) {
        BlockHitResult blockhitresult = mc.world.raycast(new RaycastContext(vec3d, vec3d1, ShapeType.COLLIDER, FluidHandling.NONE, mc.player));
        return blockhitresult.getType() != Type.BLOCK ? false : !blockhitresult.getBlockPos().equals(this.anchorPos);
    }

    @EventHandler
    public void onSwitchSlot(HotbarKeyEvent hotbarkeyevent) {
        if (this.canActivate()) {
            if (this.updateHoldActivation()) {
                if (!this.holdHandled || this.currentState != this.selectAnchorState || this.holdTicks >= this.explodeHoldTicks.getInt()) {
                    this.currentState = (AutoAnchorState)this.currentState.applyHotbar(hotbarkeyevent);
                }
            }
        }
    }

    private boolean updateHoldActivation() {
        if (!this.activation.is("Hold") || !this.holdHandled) {
            return true;
        } else if (!this.isUseKeyHeld()) {
            this.reset();
            return false;
        } else if (this.currentState == this.idleState) {
            this.reset();
            this.holdHandled = true;
            this.holdTicks = this.explodeHoldTicks.getInt();
            this.currentState = this.holdActivationState;
            mc.options.useKey.setPressed(false);
            return false;
        } else {
            mc.options.useKey.setPressed(false);
            return true;
        }
    }

    @EventHandler
    public void onPreRender(PreRenderEvent prerenderevent) {
        if (this.airAnchor.isEnabled() && this.autoBlowUp.isEnabled() && this.airAnchorKey.isPressed()) {
            if (!this.airKeyHeld) {
                this.onAnchorKey(this.airAnchorKey);
                this.airKeyHeld = true;
                this.forcedAirAnchor = true;
            }
        } else {
            this.airKeyHeld = false;
            if (!this.anchorKey.isEnabled()) {
                if (this.currentState == this.idleState && !this.holdHandled) {
                    this.reset();
                }
            } else {
                if (this.currentState == this.idleState && !this.previewTriggered) {
                    this.previewTriggered = true;
                }

                if (this.anchorKeybind.isPressed()) {
                    if (!this.previewKeyHandled) {
                        this.previewKeyHandled = true;
                        if (this.isHoldingAllowedItem()) {
                            this.onAnchorKey(this.anchorKeybind);
                        }
                    }
                } else {
                    this.previewKeyHandled = false;
                }
            }
        }
    }

    @EventHandler
    public void onRightClick(UseKeyEvent usekeyevent) {
        if (this.canActivate()) {
            if (this.updateHoldActivation()) {
                if (this.activation.is("Hold") && !this.holdHandled && (this.currentState == this.initialState || this.currentState == this.idleState)) {
                    if (this.currentState == this.idleState) {
                        this.reset();
                    }

                    if (mc.currentScreen == null && this.isUseKeyHeld()) {
                        AutoAnchorState conditionnodebinary = this.startSequence(usekeyevent);
                        if (conditionnodebinary != null) {
                            this.currentState = conditionnodebinary;
                        }
                    }
                } else {
                    this.currentState = (AutoAnchorState)this.currentState.applyUse(usekeyevent);
                }
            }
        }
    }
}
