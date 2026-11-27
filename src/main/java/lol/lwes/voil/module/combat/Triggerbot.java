package lol.lwes.voil.module.combat;

import dev.prestigeclient.renderlib.impl.api.RenderAPI;
import java.awt.Color;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.event.AttackKeyEvent;
import lol.lwes.voil.event.EventHandler;
import lol.lwes.voil.event.HotbarKeyEvent;
import lol.lwes.voil.event.TickEvent;
import lol.lwes.voil.mixin.accessor.MinecraftClientAccessor;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.module.combat.TriggerbotState;
import lol.lwes.voil.module.movement.Sprint;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.ItemListSetting;
import lol.lwes.voil.setting.KeybindSetting;
import lol.lwes.voil.setting.ListSetting;
import lol.lwes.voil.setting.ModeSetting;
import lol.lwes.voil.setting.NumberSetting;
import lol.lwes.voil.setting.RangeSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.AimMathUtil;
import lol.lwes.voil.util.CombatUtil;
import lol.lwes.voil.util.ItemKind;
import lol.lwes.voil.util.RaycastUtil;
import lol.lwes.voil.util.SystemUtil;
import lol.lwes.voil.util.input.MouseSimulator;
import lol.lwes.voil.util.inventory.InventoryUtil;
import lol.lwes.voil.util.render.Render3DUtil;
import lol.lwes.voil.util.rotation.Rotation;
import lol.lwes.voil.util.target.TargetManager;
import lol.lwes.voil.util.world.BlockUtil;
import lol.lwes.voil.util.world.EntityUtil;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class Triggerbot extends Module {
    private final ItemListSetting weapons;
    private final ModeSetting attackTime;
    private final NumberSetting initialHitDelay;
    private final NumberSetting missChance;
    private final NumberSetting groundMultiplier;
    private final NumberSetting critMultiplier;
    private final NumberSetting fakeSwingPercent;
    private final RangeSetting distance;
    private final BooleanSetting shrinkHitbox;
    private final NumberSetting hitboxSize;
    private final BooleanSetting renderHitbox;
    private final BooleanSetting autoCrit;
    private final BooleanSetting miss;
    private final BooleanSetting fakeSwing;
    private final BooleanSetting cancelWhileBlocking;
    private final BooleanSetting simulateClicks;
    private final BooleanSetting ignoreShield;
    private final BooleanSetting throughWalls;
    private final BooleanSetting prioritizeCrits;
    private final BooleanSetting distanceCheck;
    private final BooleanSetting holdingKeyOnly;
    private final BooleanSetting critsOnly;
    private final BooleanSetting pCrit;
    private final KeybindSetting keybind;
    private final BooleanSetting autoWeapon;
    private final ListSetting weaponPriority;
    private int sprintResetTicks;
    private int ticksSinceHurt;
    private boolean pCritPending;
    private float randomCooldownThreshold;
    private PlayerEntity target;
    private boolean attackPending;
    private int targetWeaponSlot;
    private int switchCooldown;
    private boolean weaponListDirty;
    private PlayerEntity lastHoveredPlayer;
    private int targetTicks;
    private double maxAttackDistance;
    public TriggerbotState currentState;
    private final TriggerbotState resetState;
    private final TriggerbotState fakeSwingState;
    private final TriggerbotState sprintResetState;
    private TriggerbotState attackState;
    private final TriggerbotState weaponSwitchState;
    private TriggerbotState idleState;
    private static final String TIMED_MODE = "Timed";

    public Triggerbot() {
        super("Triggerbot", "Automatically attacks enemies", Category.Combat);
        Item[] aitem1 = new Item[]{Items.NETHERITE_SWORD};
        ItemListSetting itemlistsetting1 = new ItemListSetting("Weapons", "Weapons to use triggerbot with", aitem1);
        Item[] aitem = new Item[]{Items.NETHERITE_SWORD, Items.NETHERITE_AXE, Items.MACE, Items.NETHERITE_PICKAXE, Items.NETHERITE_SHOVEL};
        this.weapons = itemlistsetting1.setRecommendedItems(aitem).allowCategoryMatch();
        this.attackTime = new ModeSetting("Attack Time", "Timed", "Time to attack", new String[]{"Random", "Timed"});
        this.initialHitDelay = new NumberSetting(
            "Initial Hit Delay", 0.0, 20.0, 0.0, 1.0, "Ticks to wait after acquiring a target before the first hit (0 = instant)"
        );
        this.missChance = new NumberSetting("Miss chance", 0.0, 100.0, 0.0, 1.0, "Chance to miss (%)");
        this.groundMultiplier = new NumberSetting("Ground Multiplier", 0.7, 1.0, 0.9, 0.01, "Damage multiplier threshold for ground attacks");
        this.critMultiplier = new NumberSetting("Crit Multiplier", 0.7, 1.0, 0.97, 0.01, "Damage multiplier threshold for crit attacks");
        this.fakeSwingPercent = new NumberSetting("Fake Swing Percent", 0.0, 100.0, 50.0, 1.0, "Chance to fake swing (%)");
        this.distance = new RangeSetting("Distance", 0.1, 3.0, 2.5, 3.0, 0.1, "Distance to attack at");
        this.shrinkHitbox = new BooleanSetting("Shrink Hitbox", false, "Only attack inside a reduced hitbox");
        this.hitboxSize = new NumberSetting("Hitbox Size", 10.0, 100.0, 100.0, 1.0, "Hittable hitbox size (% of real)");
        this.renderHitbox = new BooleanSetting("Render Hitbox", false, "Render the hittable hitbox");
        this.autoCrit = new BooleanSetting("Auto crit", true, "Auto crits when attacking");
        this.miss = new BooleanSetting("Miss", true, "Automatically misses when attacking");
        this.fakeSwing = new BooleanSetting("Fake Swing", false, "Only show swing animation without actually attacking");
        this.cancelWhileBlocking = new BooleanSetting("Cancel While Blocking", true, "Cancel while blocking, eating");
        this.simulateClicks = new BooleanSetting("Simulate Clicks", true, "Simulate clicks");
        this.ignoreShield = new BooleanSetting("Ignore shield", true, "Ignores people using a shield");
        this.throughWalls = new BooleanSetting("Through Walls", true, "Attack through walls");
        this.prioritizeCrits = new BooleanSetting("Prioritize Crits", true, "Prioritize critical hits");
        this.distanceCheck = new BooleanSetting("Distance Check", true, "Check if player is close enough to attack");
        this.holdingKeyOnly = new BooleanSetting("Holding Key Only", false, "Only attack when holding the key");
        this.critsOnly = new BooleanSetting("Crits Only", false, "Only attack when not on ground");
        this.pCrit = new BooleanSetting("P-Crit", false, "Crit using knockback from enemy hits");
        this.keybind = new KeybindSetting("Keybind", 0, false, "Key to hold for triggerbot");
        this.autoWeapon = new BooleanSetting("Auto Weapon", false, "Switches to the best weapon before attacking");
        this.weaponPriority = new ListSetting("Weapon Priority", "Top = preferred, per-weapon conditions", new String[0]);
        this.sprintResetTicks = 0;
        this.ticksSinceHurt = 100;
        this.pCritPending = false;
        this.randomCooldownThreshold = -1.0F;
        this.target = null;
        this.attackPending = false;
        this.targetWeaponSlot = -1;
        this.switchCooldown = 0;
        this.weaponListDirty = true;
        this.lastHoveredPlayer = null;
        this.targetTicks = 0;
        this.resetState = new TriggerbotState(attackkeyevent -> {
            this.randomCooldownThreshold = -1.0F;
            return this.idleState;
        }, null);
        this.fakeSwingState = new TriggerbotState(
            attackkeyevent -> {
                this.attackPending = true;
                Rotation rotation = VoilClient.getRotationManager().isActive()
                    ? VoilClient.getRotationManager().getTargetRotation()
                    : new Rotation(mc.player.getYaw(), mc.player.getPitch());
                ClientPlayerEntity clientplayerentity = mc.player;
                HitResult hitresult = RaycastUtil.getHitResultFiltered(
                    clientplayerentity,
                    entity -> entity instanceof PlayerEntity && TargetManager.isTarget(entity, false),
                    rotation.getYawFloat(),
                    rotation.getPitchFloat()
                );
                if (hitresult instanceof EntityHitResult
                    && ((EntityHitResult)hitresult).getEntity() instanceof PlayerEntity playerentity
                    && this.isLookingAtHitbox(playerentity, rotation)) {
                    return this.attackState;
                }

                ((MinecraftClientAccessor)mc).invokeDoAttack();
                if (this.simulateClicks.isEnabled()) {
                    MouseSimulator mousesimulator = VoilClient.getMouseSimulator();
                    int i = ThreadLocalRandom.current().nextInt(40);
                    mousesimulator.pressButton(0, 30 + i);
                }

                return this.resetState;
            },
            null
        );
        this.sprintResetState = new TriggerbotState(attackkeyevent -> {
            this.attackPending = true;
            mc.player.setSprinting(false);
            Sprint.setSprintBlocked(true);
            this.sprintResetTicks = 3;
            return this.resetState;
        }, null);
        this.attackState = new TriggerbotState(attackkeyevent -> {
            this.attackPending = true;
            if (this.miss.isEnabled() && this.missChance.getFloat() > 0.0F && ThreadLocalRandom.current().nextFloat() * 100.0F < this.missChance.getFloat()) {
                return this.resetState;
            }

            attackkeyevent.addTimesPressed(1);
            this.sprintResetTicks = 1;
            return this.resetState;
        }, null);
        this.weaponSwitchState = new TriggerbotState(null, hotbarkeyevent -> {
            if (this.targetWeaponSlot >= 0 && this.targetWeaponSlot != mc.player.getInventory().getSelectedSlot()) {
                if (hotbarkeyevent.getSlot() != this.targetWeaponSlot) {
                    return null;
                }

                hotbarkeyevent.addTimesPressed(1);
                this.targetWeaponSlot = -1;
                this.switchCooldown = 4;
                return this.idleState;
            } else {
                this.targetWeaponSlot = -1;
                return this.idleState;
            }
        });
        this.idleState = new TriggerbotState(attackkeyevent -> {
            if (this.attackPending) {
                return null;
            }

            if (this.target == null) {
                if (this.fakeSwing.isEnabled() && this.miss.isEnabled()) {
                    PlayerEntity playerentity = this.findNearestPlayer();
                    if (playerentity != null && this.canFakeSwing(playerentity)) {
                        return this.fakeSwingState;
                    }
                }

                return null;
            } else {
                PlayerEntity playerentity1 = this.target;
                return this.evaluateAttack(playerentity1);
            }
        }, null);
        Setting.dependSetting(this.critMultiplier, this.autoCrit);
        Setting.dependSetting(this.pCrit, this.autoCrit);
        Setting.dependSetting(this.keybind, this.holdingKeyOnly);
        Setting.dependSetting(this.missChance, this.miss);
        Setting.dependSetting(this.distance, this.distanceCheck);
        BooleanSetting booleansetting = this.critsOnly;
        Setting.dependSettingFalse(this.prioritizeCrits, booleansetting);
        BooleanSetting booleansetting1 = this.critsOnly;
        Setting.dependSettingFalse(this.autoCrit, booleansetting1);
        Setting.dependSetting(this.fakeSwing, this.miss);
        Setting.dependSetting(this.fakeSwingPercent, this.fakeSwing);
        Setting.dependSetting(this.weaponPriority, this.autoWeapon);
        ItemListSetting itemlistsetting = this.weapons;
        Runnable runnable = () -> this.weaponListDirty = true;
        itemlistsetting.setOnChange(runnable);
        Setting.dependSetting(this.hitboxSize, this.shrinkHitbox);
        Setting.dependSetting(this.renderHitbox, this.shrinkHitbox);
        Setting[] asetting = new Setting[]{
            this.groundMultiplier,
            this.critMultiplier,
            this.shrinkHitbox,
            this.hitboxSize,
            this.renderHitbox,
            this.autoWeapon,
            this.weaponPriority,
            this.autoCrit,
            this.cancelWhileBlocking,
            this.simulateClicks,
            this.ignoreShield,
            this.prioritizeCrits,
            this.throughWalls,
            this.miss,
            this.missChance,
            this.fakeSwing,
            this.fakeSwingPercent,
            this.holdingKeyOnly,
            this.distanceCheck,
            this.distance,
            this.weapons,
            this.attackTime,
            this.initialHitDelay,
            this.critsOnly,
            this.pCrit,
            this.keybind
        };
        this.addSettings(asetting);
        this.currentState = this.idleState;
    }

    private boolean initialCheck() {
        if (mc.player == null || mc.world == null || mc.currentScreen != null) {
            return false;
        }

        if (this.holdingKeyOnly.isEnabled() && !this.keybind.isPressed()) {
            return false;
        }

        if ((mc.player.isBlocking() || mc.player.isUsingItem()) && this.cancelWhileBlocking.isEnabled()) {
            return false;
        }

        if (this.autoWeapon.isEnabled()) {
            if (!this.isHoldingWeapon() && !this.hasWeaponInHotbar()) {
                return false;
            }
        } else if (!this.isHoldingWeapon()) {
            return false;
        }

        Rotation rotation = VoilClient.getRotationManager().isActive()
            ? VoilClient.getRotationManager().getTargetRotation()
            : new Rotation(mc.player.getYaw(), mc.player.getPitch());
        ClientPlayerEntity clientplayerentity = mc.player;
        HitResult hitresult = RaycastUtil.getHitResultFiltered(
            clientplayerentity,
            entity -> entity instanceof PlayerEntity && TargetManager.isTarget(entity, false),
            rotation.getYawFloat(),
            rotation.getPitchFloat()
        );
        if (hitresult instanceof EntityHitResult && ((EntityHitResult)hitresult).getEntity() instanceof PlayerEntity playerentity) {
            if (playerentity != this.lastHoveredPlayer) {
                this.lastHoveredPlayer = playerentity;
                this.targetTicks = 0;
                this.randomCooldownThreshold = -1.0F;
                this.maxAttackDistance = this.distance.getRandomValue();
            }

            this.target = this.isLookingAtHitbox(playerentity, rotation) ? playerentity : null;
        } else {
            this.target = null;
            this.lastHoveredPlayer = null;
        }

        return true;
    }

    private boolean canFakeSwing(PlayerEntity playerentity) {
        if (!EntityUtil.isWithinDistance(playerentity, 0.02 + ThreadLocalRandom.current().nextDouble() * 0.2)) {
            return false;
        } else {
            return ThreadLocalRandom.current().nextFloat() * 100.0F >= this.fakeSwingPercent.getFloat()
                ? false
                : !(mc.player.getAttackCooldownProgress(0.5F) < 0.8F + ThreadLocalRandom.current().nextFloat() * 0.15F);
        }
    }

    public static boolean isCritReady(PlayerEntity playerentity) {
        return playerentity.getAttackCooldownProgress(0.5F) > 0.9F
            && playerentity.fallDistance > 0.0
            && !playerentity.isOnGround()
            && !playerentity.isClimbing()
            && !playerentity.isTouchingWater()
            && !playerentity.hasStatusEffect(StatusEffects.BLINDNESS)
            && !playerentity.hasVehicle()
            && !playerentity.isSprinting();
    }

    @Override
    public void onEnable() {
        this.reset();
    }

    @Override
    public void onDisable() {
        this.reset();
        Sprint.setSprintBlocked(false);
    }

    private TriggerbotState evaluateAttack(PlayerEntity playerentity) {
        if (!playerentity.isAlive()) {
            return null;
        }

        if (this.targetTicks < this.initialHitDelay.getInt()) {
            return null;
        }

        if (CombatUtil.isShieldBlockingFrom(mc.player, playerentity, false) && this.ignoreShield.isEnabled()) {
            return null;
        }

        if (playerentity.hurtTime > 0) {
            return null;
        }

        if (BlockUtil.isInCobweb(playerentity.getBlockPos()) && !this.throughWalls.isEnabled()) {
            return null;
        }

        if (this.distanceCheck.isEnabled()) {
            Vec3d vec3d = mc.player.getEyePos();
            double d0 = vec3d.distanceTo(AimMathUtil.closestPointOnBox(vec3d, this.getHitbox(playerentity)));
            if (d0 > this.maxAttackDistance) {
                return null;
            }
        }

        if (this.autoWeapon.isEnabled() && this.switchCooldown <= 0) {
            int i = this.findBestWeaponSlot(playerentity);
            if (i != -1 && i != mc.player.getInventory().getSelectedSlot()) {
                this.targetWeaponSlot = i;
                return this.weaponSwitchState;
            }

            if (i == -1 && !this.isHoldingWeapon()) {
                return null;
            }
        }

        float f1 = mc.player.getAttackCooldownProgress(0.5F);
        float f2 = 0.2F + f1 * f1 * 0.8F;
        if (this.attackTime.is(TIMED_MODE)) {
            float f = mc.player.isOnGround() ? this.groundMultiplier.getFloat() : this.critMultiplier.getFloat();
            if (f2 < f) {
                return null;
            }
        } else {
            if (this.randomCooldownThreshold < 0.0F) {
                float f3 = 0.985F + ThreadLocalRandom.current().nextFloat() * 0.012F;
                this.randomCooldownThreshold = mc.player.isOnGround()
                    ? AimMathUtil.randomizedLerp(0.78F, 0.97F, 0.55F)
                    : AimMathUtil.randomizedLerp(0.9F, f3, 0.75F);
            }

            if (f2 < this.randomCooldownThreshold) {
                return null;
            }
        }

        if (this.prioritizeCrits.isEnabled() && !mc.player.isOnGround()) {
            boolean flag = this.autoCrit.isEnabled() ? isCritReadyWhileSprinting(mc.player) : isCritReady(mc.player);
            if (mc.player.getVelocity().y > 0.0 || mc.player.fallDistance <= 0.0 && !flag) {
                return null;
            }
        }

        if (this.pCrit.isEnabled()) {
            if (mc.player.hurtTime > 0 && !mc.player.isOnGround() && mc.player.getVelocity().y > 0.0) {
                this.pCritPending = true;
            }

            if (this.pCritPending) {
                boolean flag1 = !mc.player.isOnGround()
                    && mc.player.getVelocity().y < -0.08
                    && mc.player.fallDistance > 0.0
                    && !mc.player.isClimbing()
                    && !mc.player.isTouchingWater()
                    && !mc.player.isInLava();
                if (!flag1 && !mc.player.isOnGround()) {
                    return null;
                }

                this.pCritPending = false;
            }
        }

        if (this.autoCrit.isEnabled()
            && mc.player.isSprinting()
            && !mc.player.isOnGround()
            && mc.player.getVelocity().y < 0.0
            && isCritReadyWhileSprinting(mc.player)
            && this.ticksSinceHurt >= 2) {
            float f4 = mc.player.getAttackCooldownProgress(1.5F);
            if (0.2F + f4 * f4 * 0.8F >= this.critMultiplier.getFloat()) {
                return this.sprintResetState;
            }
        }

        return !this.critsOnly.isEnabled()
                || !mc.player.isOnGround()
                    && !(mc.player.getVelocity().y >= -0.08)
                    && !mc.player.isClimbing()
                    && !mc.player.hasVehicle()
                    && !mc.player.isTouchingWater()
                    && !mc.player.isInLava()
                    && !mc.player.getAbilities().flying
            ? this.attackState
            : null;
    }

    private boolean isHoldingWeapon() {
        ItemStack itemstack = mc.player.getMainHandStack();
        if (itemstack != null && !itemstack.isEmpty()) {
            ItemListSetting itemlistsetting = this.weapons;
            Item item1 = itemstack.getItem();
            return itemlistsetting.matches(item1);
        } else {
            return false;
        }
    }

    private void refreshWeaponPriority() {
        ArrayList arraylist = new ArrayList();

        for (Item item : this.weapons.getItems()) {
            arraylist.add(Registries.ITEM.getId(item).toString());
        }

        this.weaponPriority.setItems(arraylist);

        for (Item item1 : this.weapons.getItems()) {
            String s = Registries.ITEM.getId(item1).toString();
            if (item1 == Items.MACE) {
                this.weaponPriority.enable(s, 1.5);
            }

            if (ItemListSetting.getCategory(item1) == ItemKind.AXE) {
                this.weaponPriority.enableMax(s, 6.0);
            }
        }
    }

    private int findBestWeaponSlot(PlayerEntity playerentity) {
        double d0 = mc.player.fallDistance;
        double d1 = playerentity.getHealth() + playerentity.getAbsorptionAmount();

        for (String s : this.weaponPriority.getItems()) {
            if (this.weaponPriority.matches(s, d0, d1)) {
                Identifier identifier = Identifier.tryParse(s);
                if (identifier != null) {
                    Item item = Registries.ITEM.get(identifier);
                    if (item != Items.AIR) {
                        ItemListSetting itemlistsetting = this.weapons;
                        Item item1 = item;
                        int i = InventoryUtil.findItemSlot(item1, itemlistsetting);
                        if (i != -1) {
                            return i;
                        }
                    }
                }
            }
        }

        return -1;
    }

    private boolean isLookingAtHitbox(PlayerEntity playerentity, Rotation rotation) {
        if (!this.shrinkHitbox.isEnabled()) {
            return true;
        }

        Vec3d vec3d = mc.player.getEyePos();
        Vec3d vec3d1 = Vec3d.fromPolar(rotation.getPitchFloat(), rotation.getYawFloat());
        Vec3d vec3d2 = vec3d.add(vec3d1.multiply(6.0));
        return this.getHitbox(playerentity).raycast(vec3d, vec3d2).isPresent();
    }

    private PlayerEntity findNearestPlayer() {
        PlayerEntity playerentity = null;
        double d0 = 4.0;

        for (PlayerEntity playerentity1 : mc.world.getPlayers()) {
            if (playerentity1 != mc.player && TargetManager.isTarget(playerentity1, false)) {
                double d1 = mc.player.distanceTo(playerentity1);
                if (d1 < d0) {
                    d0 = d1;
                    playerentity = playerentity1;
                }
            }
        }

        return playerentity;
    }

    private Box getHitbox(PlayerEntity playerentity) {
        Box box = playerentity.getBoundingBox();
        if (!this.shrinkHitbox.isEnabled()) {
            return box;
        }

        double d0 = this.hitboxSize.getValue() / 100.0;
        double d1 = box.getLengthX() * (1.0 - d0) / 2.0;
        double d2 = box.getLengthY() * (1.0 - d0) / 2.0;
        double d3 = box.getLengthZ() * (1.0 - d0) / 2.0;
        return box.contract(d1, d2, d3);
    }

    private boolean hasWeaponInHotbar() {
        for (int i = 0; i < 9; i++) {
            ItemListSetting itemlistsetting = this.weapons;
            Item item1 = mc.player.getInventory().getStack(i).getItem();
            if (itemlistsetting.matches(item1)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isCritReadyWhileSprinting(PlayerEntity playerentity) {
        return playerentity.getAttackCooldownProgress(0.5F) > 0.9F
            && playerentity.fallDistance > 0.0
            && !playerentity.isOnGround()
            && !playerentity.isClimbing()
            && !playerentity.isTouchingWater()
            && !playerentity.hasStatusEffect(StatusEffects.BLINDNESS)
            && !playerentity.hasVehicle();
    }

    @EventHandler
    public void onTick(TickEvent tickevent) {
        if (mc.player != null) {
            if (this.weaponListDirty && mc.world != null) {
                this.weaponListDirty = false;
                this.refreshWeaponPriority();
            }

            if (this.lastHoveredPlayer != null) {
                int i = this.targetTicks++;
            }

            this.ticksSinceHurt = mc.player.hurtTime > 0 ? 0 : this.ticksSinceHurt + 1;
            if (this.switchCooldown > 0) {
                int l = this.switchCooldown;
                int j = l - 1;
                Triggerbot triggerbot1 = this;
                triggerbot1.switchCooldown = j;
            }

            if (this.sprintResetTicks > 0) {
                int i1 = this.sprintResetTicks;
                int k = i1 - 1;
                Triggerbot triggerbot2 = this;
                triggerbot2.sprintResetTicks = k;
                if (k == 0) {
                    Sprint.setSprintBlocked(false);
                }
            }
        }
    }

    public void reset() {
        this.randomCooldownThreshold = -1.0F;
        this.pCritPending = false;
        this.sprintResetTicks = 0;
        this.ticksSinceHurt = 100;
        this.target = null;
        this.attackPending = false;
        this.lastHoveredPlayer = null;
        this.targetTicks = 0;
        this.currentState = this.idleState;
    }

    @EventHandler
    public void onSwitchSlot(HotbarKeyEvent hotbarkeyevent) {
        if (!Module.isNotInGame()) {
            if (!this.initialCheck()) {
                this.currentState = this.idleState;
            } else {
                this.currentState = (TriggerbotState)this.currentState.applyHotbar(hotbarkeyevent);
            }
        }
    }

    @EventHandler
    public void onAttackKey(AttackKeyEvent attackkeyevent) {
        if (!Module.isNotInGame()) {
            if (!this.initialCheck()) {
                this.currentState = this.idleState;
            } else {
                this.attackPending = false;
                this.currentState = (TriggerbotState)this.currentState.applyAttack(attackkeyevent);
            }
        }
    }

    @Override
    public void onWorldRender(RenderAPI.RenderContext rendercontext) {
        if (mc.player != null && mc.world != null && this.shrinkHitbox.isEnabled() && this.renderHitbox.isEnabled()) {
            for (PlayerEntity playerentity : mc.world.getPlayers()) {
                if (playerentity != mc.player && TargetManager.isTarget(playerentity, false) && mc.player.canSee(playerentity)) {
                    Box box1 = this.getHitbox(playerentity).offset(playerentity.getLerpedPos(SystemUtil.getTickDelta()).subtract(playerentity.getEntityPos()));
                    Matrix4f matrix4f1 = new Matrix4f();
                    Color color = Color.red;
                    Box box = box1;
                    Matrix4f matrix4f = matrix4f1;
                    Render3DUtil.drawBoxOutline(matrix4f, box, color);
                }
            }
        }
    }
}
