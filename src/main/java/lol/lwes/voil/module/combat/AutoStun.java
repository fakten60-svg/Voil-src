package lol.lwes.voil.module.combat;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.event.AttackKeyEvent;
import lol.lwes.voil.event.EventHandler;
import lol.lwes.voil.event.HotbarKeyEvent;
import lol.lwes.voil.event.PacketSendPostEvent;
import lol.lwes.voil.event.UseKeyEvent;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.module.combat.StunAimResetState;
import lol.lwes.voil.module.combat.StunAimState;
import lol.lwes.voil.module.combat.StunPlaceState;
import lol.lwes.voil.module.combat.StunState;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.KeybindSetting;
import lol.lwes.voil.setting.ModeSetting;
import lol.lwes.voil.setting.NumberSetting;
import lol.lwes.voil.setting.RangeSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.AimMathUtil;
import lol.lwes.voil.util.CombatUtil;
import lol.lwes.voil.util.PlayerTargetUtil;
import lol.lwes.voil.util.RaycastUtil;
import lol.lwes.voil.util.inventory.FoundSlot;
import lol.lwes.voil.util.inventory.InventoryUtil;
import lol.lwes.voil.util.inventory.SwapBuilder;
import lol.lwes.voil.util.inventory.SwapMode;
import lol.lwes.voil.util.inventory.SwapScheduler;
import lol.lwes.voil.util.rotation.Rotation;
import lol.lwes.voil.util.rotation.SmoothRotationController;
import lol.lwes.voil.util.rotation.SmoothRotationState;
import lol.lwes.voil.util.target.TargetManager;
import lol.lwes.voil.util.world.BlockUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class AutoStun extends Module {
    private final ModeSetting useMode;
    private final ModeSetting attackMode;
    private final NumberSetting attributeSwapChance;
    private final NumberSetting attributeSwapDelay;
    private final NumberSetting hitDelay;
    private final NumberSetting switchDelay;
    private final BooleanSetting switchBackToPreviousSlot;
    private final BooleanSetting cancelWhileBlocking;
    private final BooleanSetting stun;
    public final BooleanSetting silentStunWeb;
    public final BooleanSetting stunWebOnlyOnKey;
    public final KeybindSetting stunWebKey;
    private final ModeSetting item;
    private final RangeSetting distance;
    private final BooleanSetting useDistanceCheck;
    private final NumberSetting activateAfterTicks;
    private final ModeSetting rotation;
    private final BooleanSetting silentHit;
    private final NumberSetting range;
    private final NumberSetting rotationTicks;
    private final BooleanSetting holdingKeyOnly;
    private final KeybindSetting keybind;
    private final BooleanSetting onlyStunWhenFalling;
    private int pendingSlot;
    private int delayTicks;
    private LivingEntity target;
    private int waitTicks;
    private int cooldownTicks;
    public int placeTicks;
    public StunState currentState;
    private int savedSlot;
    private int targetSlot;
    private int originalSlot;
    public BlockPos webPos;
    private float lastYaw;
    private float lastPitch;
    public int stunTicks;
    public SmoothRotationState rotationHandle;
    private final StunState resetState;
    private final StunState waitState;
    public final StunState cooldownState;
    public final StunState finishState;
    private final StunState webRotateState;
    private final StunState webAimState;
    private final StunState webPlaceState;
    public final StunState webSwapState;
    private final StunState hitRotateState;
    private final StunState hitAimState;
    private final StunState hitState;
    private final StunState shieldCheckState;
    private final StunState swapBackState;
    private final StunState axeSwapState;
    private StunState idleState;

    public AutoStun() {
        super("AutoStun", "Automatically breaks shield", Category.Combat);
        this.useMode = new ModeSetting("Use Mode", "Always", "How to stun", new String[]{"On Hit", "Always", "Hold Axe"});
        this.attackMode = new ModeSetting(
            "Attack Mode",
            "Normal",
            "Normal = switch, wait, hit. Attribute Swap = same-tick swap+hit keeping weapon cooldown",
            new String[]{"Normal", "Attribute Swap"}
        );
        this.attributeSwapChance = new NumberSetting(
            "Attribute Swap Chance", 0.0, 100.0, 100.0, 1.0, "% chance to attribute swap, otherwise falls back to normal hit"
        );
        this.attributeSwapDelay = new NumberSetting("Attribute Swap Delay", 0.0, 40.0, 10.0, 1.0, "Ticks before attribute swap can trigger again");
        this.hitDelay = new NumberSetting("Hit Delay", 0.0, 20.0, 0.0, 1.0, "Delay (in ticks) before breaking shield");
        this.switchDelay = new NumberSetting("Switch Delay", 0.0, 20.0, 0.0, 1.0, "Delay (in ticks) before switching back");
        this.switchBackToPreviousSlot = new BooleanSetting("Switch Back to Previous Slot", true, "Switches back to old slot after breaking shield");
        this.cancelWhileBlocking = new BooleanSetting("Cancel While Blocking", true, "Cancel while blocking,eating");
        this.stun = new BooleanSetting("Stun", true, "Breaks shield and hits again");
        this.silentStunWeb = new BooleanSetting("Silent Stun Web", false, "Places item when shield stun happens");
        this.stunWebOnlyOnKey = new BooleanSetting("Stun Web Only On Key", false, "Only place stun web when holding key");
        this.stunWebKey = new KeybindSetting("Stun Web Key", 0, false, "Key to hold for placing stun web");
        this.item = new ModeSetting("Item", "Web", "Item to place on stun", new String[]{"Web", "Lava"});
        this.distance = new RangeSetting("Distance", 0.1, 3.0, 2.5, 3.0, 0.1, "Distance to stun web");
        this.useDistanceCheck = new BooleanSetting("Use Distance Check", false, "Use distance check to place web");
        this.activateAfterTicks = new NumberSetting("Activate After Ticks", 0.0, 20.0, 3.0, 1.0, "Ticks target must be shielding before triggering");
        this.rotation = new ModeSetting("Rotation", "Legit", "How to rotate for hits / placements", new String[]{"Silent", "Legit"});
        this.silentHit = new BooleanSetting("Silent Hit", false, "Acquire target in range and hit via rotation manager, no crosshair needed");
        this.range = new NumberSetting("Range", 1.0, 6.0, 3.0, 0.1, "Max distance to target for silent hit");
        this.rotationTicks = new NumberSetting("Rotation Ticks", 1.0, 10.0, 3.0, 1.0, "Ticks for smooth rotation to finish");
        this.holdingKeyOnly = new BooleanSetting("Holding Key Only", false, "Only attack when holding the key");
        this.keybind = new KeybindSetting("Keybind", 0, false, "Key to hold for triggerbot");
        this.onlyStunWhenFalling = new BooleanSetting(
            "Only Stun When Falling", false, "When mid air, the module will only stun if you are falling, not ascending"
        );
        this.pendingSlot = -1;
        this.delayTicks = 0;
        this.target = null;
        this.waitTicks = 0;
        this.cooldownTicks = 0;
        this.placeTicks = 0;
        this.savedSlot = -1;
        this.targetSlot = -1;
        this.originalSlot = -1;
        this.webPos = null;
        this.lastYaw = Float.NaN;
        this.lastPitch = Float.NaN;
        this.stunTicks = 0;
        this.rotationHandle = SmoothRotationState.createEnded();
        this.resetState = new StunState(attackkeyevent -> {
            this.reset();
            return this.idleState;
        }, null);
        this.waitState = new StunState(null, hotbarkeyevent -> {
            if (this.pendingSlot != -1 && this.switchBackToPreviousSlot.isEnabled()) {
                if (this.pendingSlot == hotbarkeyevent.getSlot()) {
                    hotbarkeyevent.addTimesPressed(1);
                    this.pendingSlot = -1;
                    return this.resetState;
                } else {
                    return null;
                }
            } else {
                return this.resetState;
            }
        });
        this.cooldownState = new StunState(null, hotbarkeyevent -> {
            if (hotbarkeyevent.getSlot() != 0) {
                return null;
            } else if (this.cooldownTicks < this.switchDelay.getInt()) {
                int k = this.cooldownTicks++;
                return null;
            } else {
                return this.waitState;
            }
        });
        this.finishState = new StunState(null, hotbarkeyevent -> {
            this.releaseRotation();
            return this.cooldownState;
        });
        this.webRotateState = new StunPlaceState(this, null, null);
        this.webAimState = new StunState(null, hotbarkeyevent -> {
            Object object1 = null;
            Item[] aitem = new Item[]{Items.COBWEB};
            FoundSlot foundslot = InventoryUtil.findHotbar(aitem);
            if (foundslot.isFound() && foundslot.isHotbar()) {
                if (foundslot.getSlot() == hotbarkeyevent.getSlot()) {
                    this.webPos = this.findWebPos();
                    if (this.webPos == null) {
                        return this.cooldownState;
                    }

                    hotbarkeyevent.addTimesPressed(1);
                    this.stunTicks = 0;
                    this.aimAt(() -> this.webPos == null ? null : Vec3d.ofCenter(this.webPos));
                    return this.webRotateState;
                } else {
                    return null;
                }
            } else {
                return this.cooldownState;
            }
        });
        this.webPlaceState = new StunAimState(this, null, null);
        this.webSwapState = new StunState(null, hotbarkeyevent -> {
            if (hotbarkeyevent.getSlot() != 0) {
                return null;
            } else if (this.placeTicks < 1) {
                int k = this.placeTicks++;
                return null;
            } else {
                return this.webPlaceState;
            }
        });
        this.hitRotateState = new StunAimResetState(this, null, null);
        this.hitAimState = new StunState(null, hotbarkeyevent -> {
            Object object1 = null;
            Item[] aitem = new Item[]{Items.LAVA_BUCKET};
            FoundSlot foundslot = InventoryUtil.findHotbar(aitem);
            if (!foundslot.isFound()) {
                return this.cooldownState;
            } else if (foundslot.isHotbar() && foundslot.getSlot() == hotbarkeyevent.getSlot()) {
                hotbarkeyevent.addTimesPressed(1);
                this.aimAt(() -> this.target == null ? null : this.target.getEntityPos().add(0.0, 0.3, 0.0));
                return this.hitRotateState;
            } else {
                return null;
            }
        });
        this.hitState = new StunState(null, hotbarkeyevent -> {
            if (this.silentStunWeb.isEnabled() && (!this.stunWebOnlyOnKey.isEnabled() || this.stunWebKey.isPressed())) {
                if (this.item.is("Web") && BlockUtil.isInCobweb(this.target.getBlockPos())) {
                    return this.cooldownState;
                } else if (this.useDistanceCheck.isEnabled() && this.distanceTo(this.target) > this.distance.getRandomValueFloat()) {
                    return this.cooldownState;
                } else {
                    return this.item.is("Lava") ? this.hitAimState : this.webAimState;
                }
            } else {
                return this.cooldownState;
            }
        });
        this.shieldCheckState = new StunState(attackkeyevent -> {
            if (this.waitTicks < this.hitDelay.getInt()) {
                this.waitTicks++;
                return null;
            }

            if (this.onlyStunWhenFalling.isEnabled() && !mc.player.isOnGround() && mc.player.getVelocity().y >= -mc.player.getFinalGravity() * 2.0) {
                return null;
            }

            if (!this.shouldRun()) {
                int k = this.stunTicks;
                int l = k + 1;
                AutoStun autostun2 = this;
                autostun2.stunTicks = l;
                if (l > 20) {
                    this.stunTicks = 0;
                    return this.cooldownState;
                } else {
                    return null;
                }
            } else {
                this.stunTicks = 0;
                int i1 = this.stun.isEnabled() ? 2 : 1;
                this.attack(attackkeyevent, i1);
                return this.hitState;
            }
        }, null);
        this.swapBackState = new StunState(null, hotbarkeyevent -> {
            if (InventoryUtil.getStack(hotbarkeyevent.getSlot()).getItem() instanceof AxeItem) {
                hotbarkeyevent.addTimesPressed(1);
                this.pendingSlot = InventoryUtil.getSelectedSlot();
                return this.shieldCheckState;
            } else {
                return null;
            }
        });
        this.axeSwapState = new StunState(attackkeyevent -> {
            if (this.target != null && this.target.isAlive()) {
                if (this.waitTicks < this.hitDelay.getInt()) {
                    int i1 = this.waitTicks++;
                    return null;
                }

                if (this.onlyStunWhenFalling.isEnabled() && !mc.player.isOnGround() && mc.player.getVelocity().y >= -mc.player.getFinalGravity() * 2.0) {
                    return null;
                }

                if (!this.shouldRun()) {
                    int k = this.stunTicks;
                    int l = k + 1;
                    AutoStun autostun2 = this;
                    autostun2.stunTicks = l;
                    return l > 20 ? this.resetState : null;
                }

                this.stunTicks = 0;
                if (mc.player.getAttackCooldownProgress(0.0F) < 0.9F) {
                    return null;
                }

                Predicate<ItemStack> predicate = itemstack -> itemstack.getItem() instanceof AxeItem;
                FoundSlot foundslot = InventoryUtil.findHotbarStack(predicate);
                if (!foundslot.isFound()) {
                    return this.resetState;
                }

                this.delayTicks = this.attributeSwapDelay.getInt();
                LivingEntity livingentity = this.target;
                boolean flag = this.silentStunWeb.isEnabled() && (!this.stunWebOnlyOnKey.isEnabled() || this.stunWebKey.isPressed());
                SwapBuilder swapbuilder1 = SwapScheduler.newSwap().slot(foundslot.getSlot());
                SwapMode swapmode = SwapMode.SWAP_THEN_ACTION;
                SwapBuilder swapbuilder = swapbuilder1.mode(swapmode).attack(livingentity);
                if (this.stun.isEnabled()) {
                    Runnable runnable = () -> {
                        if (livingentity.isAlive() && this.raycastTarget(livingentity)) {
                            mc.interactionManager.attackEntity(mc.player, livingentity);
                            mc.player.swingHand(Hand.MAIN_HAND);
                        }
                    };
                    swapbuilder.onSwapBack(runnable);
                }

                if (flag) {
                    this.pendingSlot = InventoryUtil.getSelectedSlot();
                    swapbuilder.noSwapBack();
                    swapbuilder.execute();
                    return this.hitState;
                } else {
                    swapbuilder.swapBackAfter(1);
                    swapbuilder.execute();
                    return this.resetState;
                }
            } else {
                return this.resetState;
            }
        }, null);
        this.idleState = new StunState(attackkeyevent -> {
            if (!this.useMode.is("On Hit")) {
                return null;
            } else if (attackkeyevent.getTimesPressed() > 0 && mc.player.getMainHandStack().getItem() instanceof AxeItem) {
                attackkeyevent.addTimesPressed(1);
                return this.hitState;
            } else {
                return null;
            }
        }, hotbarkeyevent -> {
            if (this.useMode.is("Always")) {
                if (this.attackMode.is("Attribute Swap")) {
                    if (this.delayTicks > 0) {
                        return null;
                    }

                    double d0 = this.attributeSwapChance.getValue();
                    if (AimMathUtil.rollChance(d0)) {
                        return this.axeSwapState;
                    }
                }

                return this.swapBackState;
            } else {
                if (!this.useMode.is("Hold Axe")) {
                    return null;
                }

                int i = hotbarkeyevent.getSlot();
                if (hotbarkeyevent.getTimesPressed() > 0) {
                    this.savedSlot = i;
                    if (InventoryUtil.getStack(i).getItem() instanceof AxeItem) {
                        this.targetSlot = i;
                        this.originalSlot = InventoryUtil.getSelectedSlot();
                    }
                }

                if (hotbarkeyevent.getSlot() != 8) {
                    return null;
                }

                boolean flag = this.savedSlot == this.targetSlot && this.savedSlot != -1;
                this.savedSlot = -1;
                this.targetSlot = -1;
                if (flag) {
                    this.pendingSlot = this.originalSlot;
                    return this.shieldCheckState;
                }

                if (mc.player.getMainHandStack().getItem() instanceof AxeItem) {
                    this.pendingSlot = InventoryUtil.getSelectedSlot();
                    return this.shieldCheckState;
                } else {
                    return null;
                }
            }
        });
        Setting[] asetting = new Setting[]{
            this.useMode,
            this.attackMode,
            this.attributeSwapChance,
            this.attributeSwapDelay,
            this.hitDelay,
            this.switchDelay,
            this.activateAfterTicks,
            this.switchBackToPreviousSlot,
            this.cancelWhileBlocking,
            this.stun,
            this.silentStunWeb,
            this.stunWebOnlyOnKey,
            this.stunWebKey,
            this.item,
            this.holdingKeyOnly,
            this.keybind,
            this.useDistanceCheck,
            this.distance,
            this.rotation,
            this.silentHit,
            this.range,
            this.rotationTicks,
            this.onlyStunWhenFalling
        };
        this.addSettings(asetting);
        Setting.dependSetting(this.silentStunWeb, this.stun);
        Setting.dependSetting(this.item, this.silentStunWeb);
        Setting.dependSetting(this.distance, this.silentStunWeb);
        Setting.dependSetting(this.distance, this.useDistanceCheck);
        Setting.dependSetting(this.keybind, this.holdingKeyOnly);
        Setting.dependSetting(this.stunWebOnlyOnKey, this.silentStunWeb);
        Setting.dependSetting(this.stunWebKey, this.stunWebOnlyOnKey);
        Setting.dependAny(this.silentHit, this.useMode, new String[]{"Always", "Hold Axe"});
        Setting.dependSetting(this.range, this.silentHit);
        Setting.dependSetting(this.rotation, () -> this.silentStunWeb.isEnabled() || this.silentHit.isEnabled());
        Setting.dependSetting(this.rotationTicks, () -> this.silentStunWeb.isEnabled() || this.silentHit.isEnabled());
        Setting.dependAny(this.attackMode, this.useMode, new String[]{"Always"});
        Setting.dependSetting(this.attributeSwapChance, () -> this.useMode.is("Always") && this.attackMode.is("Attribute Swap"));
        Setting.dependSetting(this.attributeSwapDelay, () -> this.useMode.is("Always") && this.attackMode.is("Attribute Swap"));
        Setting.dependAny(this.hitDelay, this.useMode, new String[]{"Always", "Hold Axe"});
        Setting.dependAny(this.activateAfterTicks, this.useMode, new String[]{"Always", "Hold Axe"});
        Setting.dependSetting(this.switchDelay, () -> !this.useMode.is("On Hit") && this.attackMode.is("Normal"));
        Setting.dependSetting(
            this.switchBackToPreviousSlot, () -> !this.useMode.is("On Hit") && (this.attackMode.is("Normal") || this.silentStunWeb.isEnabled())
        );
    }

    private boolean canPlaceAt(BlockPos blockpos) {
        return mc.world.getBlockState(blockpos).isReplaceable() && BlockUtil.getBestFace(blockpos) != null;
    }

    private void aimAtTarget() {
        this.aimAt(() -> this.target == null ? null : AimMathUtil.closestPointOnBox(mc.player.getEyePos(), this.target.getBoundingBox().contract(0.1)));
    }

    public void releaseRotation() {
        VoilClient.getRotationController().release(this);
        this.rotationHandle = SmoothRotationState.createEnded();
    }

    private boolean shouldRun() {
        if (!this.isSilentHit()) {
            return true;
        }

        if (this.target != null && this.target.isAlive()) {
            if (this.distanceTo(this.target) > this.range.getValue()) {
                return false;
            } else {
                return this.rotation.is("Legit") && !this.rotationHandle.isOnTarget() ? false : this.raycastTarget(this.target);
            }
        } else {
            return false;
        }
    }

    public static MinecraftClient getMc() {
        return mc;
    }

    private void reset() {
        this.pendingSlot = -1;
        this.target = null;
        this.waitTicks = 0;
        this.cooldownTicks = 0;
        this.placeTicks = 0;
        this.currentState = this.idleState;
        this.savedSlot = -1;
        this.targetSlot = -1;
        this.originalSlot = -1;
        this.webPos = null;
        this.stunTicks = 0;
        this.releaseRotation();
    }

    @Override
    public void onDisable() {
        this.releaseRotation();
        super.onDisable();
    }

    @Override
    public void onEnable() {
        this.reset();
        this.delayTicks = 0;
        this.lastYaw = Float.NaN;
        this.lastPitch = Float.NaN;
    }

    private boolean initialCheck() {
        boolean flag = List.of(this.shieldCheckState, this.axeSwapState, this.swapBackState, this.idleState).contains(this.currentState);
        Predicate<ItemStack> predicate = itemstack -> itemstack.getItem() instanceof AxeItem;
        boolean flag2 = !InventoryUtil.findInventoryStack(predicate).isHotbar()
            || mc.player.isBlocking() && this.cancelWhileBlocking.isEnabled()
            || mc.player.isUsingItem() && this.cancelWhileBlocking.isEnabled()
            || mc.currentScreen instanceof HandledScreen
            || this.holdingKeyOnly.isEnabled() && !this.keybind.isPressed();
        if (flag2) {
            this.releaseRotation();
            return false;
        }

        if (this.target != null && !flag) {
            return true;
        }

        LivingEntity livingentity = this.isSilentHit() ? this.acquireTarget() : this.findTarget();
        if (livingentity == null) {
            this.target = null;
            this.releaseRotation();
            return false;
        }

        boolean flag1 = livingentity != this.target;
        this.target = livingentity;
        if (CombatUtil.isShieldBlockingFrom(mc.player, this.target, true) && this.target.getItemUseTime() >= this.activateAfterTicks.getInt()) {
            if (this.isSilentHit() && (flag1 || this.rotationHandle.hasEnded())) {
                this.aimAtTarget();
            }

            return true;
        } else {
            this.releaseRotation();
            return false;
        }
    }

    private BlockPos findWebPos() {
        BlockPos blockpos = this.target.getBlockPos();
        if (this.canPlaceAt(blockpos)) {
            return blockpos;
        } else {
            return this.canPlaceAt(blockpos.up()) ? blockpos.up() : null;
        }
    }

    public static MinecraftClient getMc2() {
        return mc;
    }

    public static MinecraftClient getMc22() {
        return mc;
    }

    public boolean isAimedAtPos(BlockPos blockpos) {
        if (Float.isNaN(this.lastYaw)) {
            return false;
        }

        Vec3d vec3d = mc.player.getEyePos();
        Vec3d vec3d1 = vec3d.add(this.getLookVector().multiply(mc.player.getBlockInteractionRange()));
        return new Box(blockpos).raycast(vec3d, vec3d1).isPresent();
    }

    private boolean isAimedAtEntity(LivingEntity livingentity) {
        if (Float.isNaN(this.lastYaw)) {
            return false;
        }

        Vec3d vec3d = mc.player.getEyePos();
        Vec3d vec3d1 = vec3d.add(this.getLookVector().multiply(mc.player.getEntityInteractionRange()));
        return livingentity.getBoundingBox().expand(livingentity.getTargetingMargin()).raycast(vec3d, vec3d1).isPresent();
    }

    private double distanceTo(LivingEntity livingentity) {
        Vec3d vec3d = mc.player.getEyePos();
        return vec3d.distanceTo(AimMathUtil.closestPointOnBox(vec3d, livingentity.getBoundingBox()));
    }

    private LivingEntity findTarget() {
        ClientPlayerEntity clientplayerentity = mc.player;
        return RaycastUtil.getHitResultRanged(
                    clientplayerentity,
                    entity -> entity instanceof LivingEntity && entity != mc.player,
                    mc.player.getYaw(),
                    mc.player.getPitch(),
                    mc.player.getEntityInteractionRange(),
                    false
                ) instanceof EntityHitResult entityhitresult
                && this.isValidTarget(entityhitresult.getEntity())
            ? (LivingEntity)entityhitresult.getEntity()
            : null;
    }

    public static MinecraftClient getMc222() {
        return mc;
    }

    private void aimAt(Supplier<Vec3d> supplier) {
        VoilClient.getRotationController().release(this);
        SmoothRotationController smoothrotationcontroller = VoilClient.getRotationController();
        Supplier supplier4 = SmoothRotationController.smoothForTicks(this.rotationTicks.getInt());
        Supplier supplier5 = () -> {
            Vec3d vec3d = (Vec3d)supplier.get();
            return vec3d == null ? null : Rotation.fromPositions(mc.player.getEyePos(), vec3d);
        };
        Supplier supplier6 = () -> false;
        boolean flag = this.rotation.is("Silent");
        Supplier supplier1 = supplier6;
        Supplier supplier2 = supplier5;
        Supplier supplier3 = supplier4;
        AutoStun autostun1 = this;
        this.rotationHandle = smoothrotationcontroller.start(autostun1, supplier3, supplier2, supplier1, flag);
    }

    private void attack(AttackKeyEvent attackkeyevent, int i) {
        if (this.isSilentHit() && this.rotation.is("Silent")) {
            for (int j = 0; j < i; j++) {
                mc.interactionManager.attackEntity(mc.player, this.target);
            }

            mc.player.swingHand(Hand.MAIN_HAND);
        } else {
            attackkeyevent.addTimesPressed(i);
        }
    }

    private boolean raycastTarget(LivingEntity livingentity) {
        if (this.isSilentHit() && this.rotation.is("Silent")) {
            return this.isAimedAtEntity(livingentity);
        }

        ClientPlayerEntity clientplayerentity = mc.player;
        HitResult hitresult = RaycastUtil.getHitResultRanged(
            clientplayerentity, entity -> entity == livingentity, mc.player.getYaw(), mc.player.getPitch(), mc.player.getEntityInteractionRange(), false
        );
        return hitresult instanceof EntityHitResult && ((EntityHitResult)hitresult).getEntity() == livingentity;
    }

    private boolean isValidTarget(Entity entity) {
        if (!(entity instanceof LivingEntity livingentity && livingentity != mc.player && livingentity.isAlive())) {
            return false;
        } else {
            return !PlayerTargetUtil.isFriend(livingentity.getUuid()) && !PlayerTargetUtil.isIgnored(livingentity.getUuid())
                ? TargetManager.isTarget(livingentity, false)
                : false;
        }
    }

    private LivingEntity acquireTarget() {
        if (this.target != null && this.isValidTarget(this.target) && this.distanceTo(this.target) <= this.range.getValue()) {
            return this.target;
        }

        LivingEntity livingentity = null;
        double d0 = Double.MAX_VALUE;

        for (Entity entity : mc.world.getEntities()) {
            if (this.isValidTarget(entity)) {
                LivingEntity livingentity1 = (LivingEntity)entity;
                double d1 = this.distanceTo(livingentity1);
                if (!(d1 > this.range.getValue()) && !(d1 >= d0)) {
                    livingentity = livingentity1;
                    d0 = d1;
                }
            }
        }

        return livingentity;
    }

    private Vec3d getLookVector() {
        double d0 = Math.toRadians(this.lastYaw);
        double d1 = Math.toRadians(this.lastPitch);
        double d2 = Math.cos(d1);
        return new Vec3d(-Math.sin(d0) * d2, -Math.sin(d1), Math.cos(d0) * d2);
    }

    private boolean isSilentHit() {
        return this.silentHit.isEnabled() && !this.useMode.is("On Hit");
    }

    public static MinecraftClient getMc2222() {
        return mc;
    }

    @EventHandler
    public void onUseKey(UseKeyEvent usekeyevent) {
        if (!Module.isNotInGame()) {
            if (!this.initialCheck()) {
                this.currentState = this.waitState;
            }

            this.currentState = (StunState)this.currentState.applyUse(usekeyevent);
        }
    }

    @EventHandler
    public void onSwitchSlot(HotbarKeyEvent hotbarkeyevent) {
        if (!Module.isNotInGame()) {
            if (!this.initialCheck()) {
                this.currentState = this.waitState;
            }

            this.currentState = (StunState)this.currentState.applyHotbar(hotbarkeyevent);
        }
    }

    @EventHandler
    public void onPacketSendPost(PacketSendPostEvent packetsendpostevent) {
        if (packetsendpostevent.getPacket() instanceof PlayerMoveC2SPacket playermovec2spacket && playermovec2spacket.changesLook()) {
            this.lastYaw = playermovec2spacket.getYaw(this.lastYaw);
            this.lastPitch = playermovec2spacket.getPitch(this.lastPitch);
        }
    }

    @EventHandler
    public void onAttackKey(AttackKeyEvent attackkeyevent) {
        if (!Module.isNotInGame()) {
            if (this.delayTicks > 0) {
                int i = this.delayTicks--;
            }

            if (!this.initialCheck()) {
                this.currentState = this.waitState;
            }

            this.currentState = (StunState)this.currentState.applyAttack(attackkeyevent);
        }
    }
}
