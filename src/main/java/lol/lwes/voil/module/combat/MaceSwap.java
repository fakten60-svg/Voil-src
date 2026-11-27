package lol.lwes.voil.module.combat;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.event.AttackKeyEvent;
import lol.lwes.voil.event.EventHandler;
import lol.lwes.voil.event.HotbarKeyEvent;
import lol.lwes.voil.event.TickEvent;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.module.combat.MaceSwapState;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.ListSetting;
import lol.lwes.voil.setting.ModeSetting;
import lol.lwes.voil.setting.NumberSetting;
import lol.lwes.voil.setting.RangeSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.AimMathUtil;
import lol.lwes.voil.util.CombatUtil;
import lol.lwes.voil.util.SystemUtil;
import lol.lwes.voil.util.inventory.FoundSlot;
import lol.lwes.voil.util.inventory.InventoryUtil;
import lol.lwes.voil.util.inventory.SwapBuilder;
import lol.lwes.voil.util.inventory.SwapMode;
import lol.lwes.voil.util.inventory.SwapScheduler;
import lol.lwes.voil.util.prediction.DefaultMotionPredictor;
import lol.lwes.voil.util.prediction.MotionPredictor;
import lol.lwes.voil.util.prediction.MotionRequest;
import lol.lwes.voil.util.prediction.MotionResult;
import lol.lwes.voil.util.rotation.Rotation;
import lol.lwes.voil.util.rotation.SmoothRotationController;
import lol.lwes.voil.util.rotation.SmoothRotationState;
import lol.lwes.voil.util.target.TargetManager;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;

public class MaceSwap extends Module {
    private final NumberSetting fallDistance = new NumberSetting("Fall Distance", 3.0, 20.0, 5.0, 0.1, "Min blocks fallen before swap triggers");
    private final ModeSetting attackMode = new ModeSetting(
        "Attack Mode",
        "Normal",
        "Normal = switch, wait, hit. Attribute Swap = same-tick swap+hit keeping sword cooldown",
        new String[]{"Normal", "Attribute Swap"}
    );
    private final ModeSetting rotation = new ModeSetting(
        "Rotation", "Silent", "Method of rotating to hit the nearest target", new String[]{"Silent", "Player", "None"}
    );
    private final NumberSetting range = new NumberSetting("Range", 3.0, 15.0, 8.0, 0.1, "Max target distance");
    private final NumberSetting fov = new NumberSetting("FOV", 30.0, 360.0, 180.0, 5.0, "Max FOV angle for Silent target");
    private final RangeSetting attackDelay = new RangeSetting("Attack Delay", 0.0, 20.0, 0.0, 0.0, 1.0, "Ticks between mace attack");
    private final RangeSetting initialHitDelay = new RangeSetting("Initial Hit Delay", 0.0, 20.0, 0.0, 0.0, 1.0, "Ticks to wait after activating before the first hit");
    private final BooleanSetting stun = new BooleanSetting("Stun", false, "Breaks or drain target shield");
    private final ModeSetting stunMode = new ModeSetting(
        "Stun Mode", "Break Shield", "Break shield breaks the shield, drain attacks to shield with mace", new String[]{"Break Shield", "Drain"}
    );
    private final RangeSetting drainAttackCount = new RangeSetting("Drain Attack Count", 1.0, 20.0, 8.0, 15.0, 1.0, "Amount of attacks to do for drain");
    private final NumberSetting stunChance = new NumberSetting("Stun Chance", 0.0, 100.0, 100.0, 1.0, "% chance to trigger stun when target is shielding");
    private final NumberSetting stunMinAirtime = new NumberSetting(
        "Stun Min Airtime", 3.0, 20.0, 3.0, 1.0, "Min predicted ticks until landing to the axe, checked at swing time"
    );
    private final BooleanSetting switchBack = new BooleanSetting("Switch Back", true, "Restore old slot after the attack");
    private final ListSetting enchantPriority = new ListSetting(
            "Enchant Priority", "Mace enchant preference, top = picked first", new String[]{"Density", "Breach", "Windburst", "Plain"}
        )
        .addDefaultEnabled("Density", 7.0);
    private final MotionPredictor simulator = DefaultMotionPredictor.create();
    private int returnSlot = -1;
    private int maceSlot = -1;
    private int axeSlot = -1;
    private int swapsRemaining = 0;
    private int swapsDone = 0;
    private boolean maceSwapped = false;
    private boolean axeSwapped = false;
    private boolean breakingShield = false;
    private boolean cancelled = false;
    private boolean shieldBlocking = false;
    private boolean attackPending = false;
    private LivingEntity target = null;
    public MaceSwapState currentState;
    private boolean stateAdvanced = false;
    private boolean pendingStun = false;
    private SmoothRotationState rotationHandle = SmoothRotationState.createEnded();
    private int tickDelay = -1;
    private int attackWindow = 0;
    private int lastTick = -1;
    private int preSwapSlot = -1;
    private int candidateSlot = -1;
    private int candidateScore = Integer.MAX_VALUE;
    private int airTicks = 0;
    private int maxSwaps = 0;
    private boolean swapLimitReached = false;
    private final MaceSwapState idleState;
    private final MaceSwapState state1;
    private final MaceSwapState state2;
    private final MaceSwapState state3;
    private final MaceSwapState state4;
    private final MaceSwapState state5;
    private final MaceSwapState state6;
    private MaceSwapState state7;
    private final MaceSwapState state8;
    private final MaceSwapState state9;

    public MaceSwap() {
        super("MaceSwap", "Auto swaps to mace and attacks when falling N blocks", Category.Combat);
        Function<AttackKeyEvent, MaceSwapState> function = attackkeyevent -> {
            this.resetSwaps();
            return null;
        };
        this.idleState = new MaceSwapState(function);
        Function<AttackKeyEvent, MaceSwapState> function1 = attackkeyevent -> {
            this.restoreSlot();
            return this.idleState;
        };
        this.state1 = new MaceSwapState(function1);
        Function<AttackKeyEvent, MaceSwapState> function2 = attackkeyevent -> null;
        this.state2 = new MaceSwapState(function2);
        Function<AttackKeyEvent, MaceSwapState> function3 = attackkeyevent -> {
            if (this.maceSwapped) {
                this.maceSwapped = false;
                return this.state1;
            }

            if (this.target == null || !this.target.isAlive()) {
                return this.state1;
            }

            if (!this.isAimedAtTarget() || !this.canRotate()) {
                return null;
            }

            if (!this.isDone()) {
                return null;
            }

            MaceSwapState statenodeunary = this.getNextState();
            if (statenodeunary != null) {
                return statenodeunary;
            }

            int j = 1;
            if (this.breakingShield && this.stunMode.is("Drain")) {
                j = Math.min(this.drainAttackCount.getRandomInt(), Math.max(1, this.getFallBlocks() - 1));
            }

            attackkeyevent.addTimesPressed(j);
            this.swapLimitReached = true;
            this.maceSwapped = true;
            return null;
        };
        this.state3 = new MaceSwapState(function3);
        Function<AttackKeyEvent, MaceSwapState> function4 = attackkeyevent -> {
            int i = this.maceSlot;
            InventoryUtil.selectSlot(i);
            this.stateAdvanced = true;
            return this.state3;
        };
        this.state4 = new MaceSwapState(function4);
        Function<AttackKeyEvent, MaceSwapState> function5 = attackkeyevent -> {
            if (this.axeSwapped) {
                this.axeSwapped = false;
                return this.state4;
            }

            if (this.target != null && this.target.isAlive()) {
                if (this.getFallBlocks() < 3) {
                    this.breakingShield = false;
                    return this.state7;
                }

                if (!this.isAimedAtTarget() || !this.canRotate()) {
                    return null;
                }

                if (!this.isDone()) {
                    return null;
                }

                attackkeyevent.addTimesPressed(1);
                this.swapLimitReached = true;
                this.axeSwapped = true;
                return null;
            } else {
                return this.state1;
            }
        };
        this.state5 = new MaceSwapState(function5);
        Function<AttackKeyEvent, MaceSwapState> function6 = attackkeyevent -> {
            int i = this.axeSlot;
            InventoryUtil.selectSlot(i);
            this.stateAdvanced = true;
            return this.state5;
        };
        this.state6 = new MaceSwapState(function6);
        Function<AttackKeyEvent, MaceSwapState> function7 = attackkeyevent -> {
            int i = this.maceSlot;
            InventoryUtil.selectSlot(i);
            return this.state3;
        };
        this.state7 = new MaceSwapState(function7);
        Function<AttackKeyEvent, MaceSwapState> function8 = attackkeyevent -> {
            if (this.target == null || !this.target.isAlive()) {
                return this.idleState;
            }

            if (!this.isAimedAtTarget() || !this.canRotate()) {
                return null;
            }

            if (!this.isDone()) {
                return null;
            }

            this.swapLimitReached = true;
            boolean flag = this.stun.isEnabled()
                && this.stunMode.is("Break Shield")
                && this.shieldBlocking
                && this.getFallBlocks() >= 2
                && CombatUtil.isShieldBlockingFrom(mc.player, this.target, true);
            if (flag) {
                Predicate<ItemStack> predicate = itemstack -> itemstack.getItem() instanceof AxeItem;
                FoundSlot foundslot = InventoryUtil.findHotbarStack(predicate);
                if (foundslot.isFound()) {
                    this.axeSlot = foundslot.getSlot();
                } else {
                    flag = false;
                }
            }

            if (flag) {
                this.preSwapSlot = InventoryUtil.getSelectedSlot();
                SwapBuilder swapbuilder = SwapScheduler.newSwap().slot(this.axeSlot);
                SwapMode swapmode = SwapMode.SWAP_THEN_ACTION;
                swapbuilder = swapbuilder.mode(swapmode);
                LivingEntity livingentity = this.target;
                swapbuilder.attack(livingentity).noSwapBack().execute();
                this.tickDelay = 0;
                this.attackWindow = 10;
                this.lastTick = mc.player.age;
                this.stateAdvanced = true;
                return this.state2;
            } else {
                SwapBuilder swapbuilder1 = SwapScheduler.newSwap().slot(this.maceSlot);
                SwapMode swapmode1 = SwapMode.SWAP_THEN_ACTION;
                swapbuilder1 = swapbuilder1.mode(swapmode1);
                LivingEntity livingentity1 = this.target;
                swapbuilder1.attack(livingentity1).swapBackAfter(this.switchBack.isEnabled() ? 1 : -1).execute();
                return this.idleState;
            }
        };
        this.state8 = new MaceSwapState(function8);
        Function<AttackKeyEvent, MaceSwapState> function9 = attackkeyevent -> {
            if (this.target == null || !this.target.isAlive()) {
                return null;
            }

            if (this.rotation.is("None") && !this.isAimedAtTarget()) {
                return null;
            }

            this.stun();
            this.attackPending = true;
            this.swapsDone = 0;
            this.maxSwaps = this.initialHitDelay.getRandomInt();
            this.swapLimitReached = false;
            if (this.attackMode.is("Attribute Swap")) {
                this.returnSlot = -1;
                return this.state8;
            }

            this.returnSlot = InventoryUtil.getSelectedSlot();
            if (this.breakingShield && this.stunMode.is("Break Shield") && this.axeSlot != -1) {
                if (this.getFallBlocks() >= Math.max(3, (int)this.stunMinAirtime.getValue())) {
                    return this.state6;
                }

                this.breakingShield = false;
            }

            return InventoryUtil.getSelectedSlot() == this.maceSlot ? this.state3 : this.state7;
        };
        this.state9 = new MaceSwapState(function9);
        this.currentState = this.state9;
        Setting[] asetting = new Setting[]{
            this.fallDistance,
            this.attackMode,
            this.rotation,
            this.range,
            this.fov,
            this.attackDelay,
            this.initialHitDelay,
            this.stun,
            this.stunMode,
            this.drainAttackCount,
            this.stunChance,
            this.stunMinAirtime,
            this.switchBack,
            this.enchantPriority
        };
        this.addSettings(asetting);
        Setting.dependAny(this.range, this.rotation, new String[]{"Silent", "Player"});
        Setting.dependAny(this.fov, this.rotation, new String[]{"Silent", "Player"});
        Setting.dependSetting(this.stunMode, this.stun);
        Setting.dependSetting(this.stunChance, this.stun);
        Setting.dependSetting(this.stunMinAirtime, () -> this.stun.isEnabled() && this.attackMode.is("Normal"));
        Setting.dependSetting(this.drainAttackCount, () -> this.stun.isEnabled() && this.stunMode.is("Drain") && this.attackMode.is("Normal"));
    }

    private boolean isDone() {
        return this.swapLimitReached || this.swapsDone >= this.maxSwaps || this.getFallBlocks() <= 1;
    }

    private MaceSwapState getNextState() {
        if (this.cancelled) {
            return null;
        } else if (!this.stun.isEnabled()) {
            return null;
        } else {
            this.stun();
            if (!this.breakingShield) {
                return null;
            } else if (this.stunMode.is("Drain")) {
                return null;
            } else if (this.axeSlot == -1) {
                return null;
            } else if (this.getFallBlocks() < Math.max(3, (int)this.stunMinAirtime.getValue())) {
                this.breakingShield = false;
                return null;
            } else {
                return this.state6;
            }
        }
    }

    private void tickSwap() {
        if (mc.player.age != this.lastTick) {
            if (this.tickDelay > 0) {
                int i = this.tickDelay--;
            } else if (mc.player.isOnGround()) {
                this.swapBack(true);
            } else {
                boolean flag = this.target != null && this.target.isAlive();
                if (flag && this.isAimedAtTarget()) {
                    SwapBuilder swapbuilder = SwapScheduler.newSwap().slot(this.maceSlot);
                    SwapMode swapmode = SwapMode.SWAP_THEN_ACTION;
                    swapbuilder = swapbuilder.mode(swapmode);
                    LivingEntity livingentity = this.target;
                    swapbuilder.attack(livingentity).returnSlot(this.preSwapSlot).swapBackAfter(this.switchBack.isEnabled() ? 1 : -1).execute();
                    this.swapBack(false);
                } else {
                    this.attackWindow--;
                    if (!flag || this.attackWindow <= 0 || this.getFallBlocks() < 1) {
                        this.swapBack(true);
                    }
                }
            }
        }
    }

    private boolean isAimedAtTarget() {
        if (this.target == null) {
            return false;
        }

        HitResult hitresult = mc.crosshairTarget;
        return hitresult instanceof EntityHitResult && ((EntityHitResult)hitresult).getEntity() == this.target;
    }

    private double distanceTo(LivingEntity livingentity) {
        return mc.player.getCameraPosVec(SystemUtil.getTickDelta()).distanceTo(AimMathUtil.closestPointOnEntity(livingentity));
    }

    private boolean isFalling() {
        return !mc.player.isOnGround() && !mc.player.isClimbing() && !mc.player.isTouchingWater();
    }

    private void enterState(MaceSwapState statenodeunary, AttackKeyEvent attackkeyevent) {
        this.stateAdvanced = false;
        this.currentState = statenodeunary;

        MaceSwapState statenodeunary1;
        for (statenodeunary1 = (MaceSwapState)this.currentState.applyAttack(attackkeyevent);
            statenodeunary1 != this.currentState && !this.stateAdvanced;
            statenodeunary1 = (MaceSwapState)this.currentState.applyAttack(attackkeyevent)
        ) {
            this.currentState = statenodeunary1;
        }

        this.currentState = statenodeunary1;
    }

    @Override
    public void onEnable() {
        this.onDisable();
    }

    @Override
    public void onDisable() {
        this.resetSlots();
        this.maceSlot = -1;
        this.swapsRemaining = 0;
        this.swapsDone = 0;
        this.attackPending = false;
        this.tickDelay = -1;
        this.attackWindow = 0;
        this.lastTick = -1;
        this.preSwapSlot = -1;
        this.candidateSlot = -1;
        this.candidateScore = Integer.MAX_VALUE;
        this.currentState = this.state9;
        this.rotationHandle = SmoothRotationState.createEnded();
    }

    private void resetSlots() {
        this.returnSlot = -1;
        this.axeSlot = -1;
        this.maceSwapped = false;
        this.axeSwapped = false;
        this.breakingShield = false;
        this.cancelled = false;
        this.shieldBlocking = false;
        this.target = null;
        this.maxSwaps = 0;
        this.swapLimitReached = false;
    }

    private void finish() {
        this.restoreSlot();
        this.currentState = this.idleState;
        this.resetSwaps();
    }

    private void resetSwaps() {
        this.attackPending = false;
        this.swapsDone = 0;
        this.swapsRemaining = this.attackDelay.getRandomInt();
        this.pendingStun = true;
        this.resetSlots();
    }

    private void restoreSlot() {
        if (this.switchBack.isEnabled()) {
            if (this.returnSlot != -1) {
                if (this.returnSlot != InventoryUtil.getSelectedSlot()) {
                    int i = this.returnSlot;
                    InventoryUtil.selectSlot(i);
                }
            }
        }
    }

    private int findMaceSlot() {
        if (mc.player == null) {
            return -1;
        }

        Registry registry = mc.player.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
        double fallDistance = mc.player.fallDistance;
        int i = -1;

        for (String s : this.enchantPriority.getActiveItems(fallDistance)) {
            if (s.equals("Plain")) {
                for (int k1 = 0; k1 < 9; k1++) {
                    if (mc.player.getInventory().getStack(k1).isOf(Items.MACE)) {
                        return k1;
                    }
                }
            } else {
                RegistryKey registrykey = switch (s) {
                    case "Breach" -> Enchantments.BREACH;
                    case "Windburst" -> Enchantments.WIND_BURST;
                    default -> Enchantments.DENSITY;
                };
                int j = -1;
                int k = 0;

                for (int l = 0; l < 9; l++) {
                    ItemStack itemstack = mc.player.getInventory().getStack(l);
                    if (itemstack.isOf(Items.MACE)) {
                        if (i == -1) {
                            i = l;
                        }

                        int i1 = InventoryUtil.getEnchantmentLevel(itemstack, registrykey, registry);
                        if (i1 > k) {
                            k = i1;
                            j = l;
                        }
                    }
                }

                if (j != -1) {
                    return j;
                }
            }
        }

        return i;
    }

    private boolean isInRange(LivingEntity livingentity) {
        double d0 = CombatUtil.getEntityReach();
        double d1 = this.distanceTo(livingentity);
        if (d1 <= d0) {
            return true;
        } else if (!this.isFalling()) {
            return false;
        } else {
            double d2 = -mc.player.getVelocity().y;
            if (!(d2 <= 0.0) && !(livingentity.getY() > mc.player.getY())) {
                int i = Math.min(this.getFallBlocks(), 10);
                return d1 - d2 * i <= d0;
            } else {
                return false;
            }
        }
    }

    private void stun() {
        boolean flag;
        label36: {
            this.cancelled = true;
            this.breakingShield = false;
            this.axeSlot = -1;
            if (this.stun.isEnabled()) {
                double d0 = this.stunChance.getValue();
                if (AimMathUtil.rollChance(d0)) {
                    flag = true;
                    break label36;
                }
            }

            flag = false;
        }

        this.shieldBlocking = flag;
        if (this.shieldBlocking) {
            if (!this.attackMode.is("Attribute Swap")) {
                if (CombatUtil.isShieldBlockingFrom(mc.player, this.target, true)) {
                    if (this.getFallBlocks() >= 2) {
                        if (this.stunMode.is("Drain")) {
                            this.breakingShield = true;
                        } else {
                            Predicate<ItemStack> predicate = itemstack -> itemstack.getItem() instanceof AxeItem;
                            FoundSlot foundslot = InventoryUtil.findHotbarStack(predicate);
                            if (foundslot.isFound()) {
                                this.axeSlot = foundslot.getSlot();
                                this.breakingShield = true;
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean canRotate() {
        return this.rotation.is("None") || this.rotationHandle.isOnTarget();
    }

    private void preAim() {
        if (!this.rotation.is("None")) {
            SmoothRotationController smoothrotationcontroller = VoilClient.getRotationController();
            Supplier supplier3 = () -> this.target == null
                ? 0.0
                : mc.player.getCameraPosVec(SystemUtil.getTickDelta()).distanceTo(AimMathUtil.closestPointOnEntity(this.target)) - CombatUtil.getEntityReach();
            Supplier supplier4 = () -> {
                if (this.target == null) {
                    return null;
                }

                Vec3d vec3d1 = mc.player.getCameraPosVec(SystemUtil.getTickDelta());
                Vec3d vec3d = this.target.getCameraPosVec(SystemUtil.getTickDelta());
                return Rotation.fromPositions(vec3d1, vec3d);
            };
            Supplier supplier5 = () -> !this.attackPending && this.tickDelay < 0;
            boolean flag = this.rotation.is("Silent");
            Supplier supplier = supplier5;
            Supplier supplier1 = supplier4;
            Supplier supplier2 = supplier3;
            MaceSwap maceswap1 = this;
            this.rotationHandle = smoothrotationcontroller.start(maceswap1, supplier2, supplier1, supplier, flag);
        }
    }

    private boolean shouldSwap() {
        if (mc.player == null) {
            return false;
        }

        if (this.attackPending) {
            if (this.maceSlot < 0 || !mc.player.getInventory().getStack(this.maceSlot).isOf(Items.MACE)) {
                return false;
            } else if (this.target == null || !this.target.isAlive()) {
                return false;
            } else if (!this.isFalling()) {
                return false;
            } else {
                return mc.player.getVelocity().y > 0.1 ? false : this.airTicks <= 3;
            }
        } else {
            int i = this.findMaceSlot();
            if (i == -1) {
                return false;
            }

            this.maceSlot = i;
            if (this.swapsRemaining > 0) {
                return false;
            }

            if (!this.isFalling()) {
                return false;
            }

            if (mc.player.fallDistance < this.fallDistance.getValue()) {
                return false;
            }

            if (mc.player.getVelocity().y >= 0.0) {
                return false;
            }

            this.target = this.findTarget();
            return this.target != null;
        }
    }

    private LivingEntity findTarget() {
        double d0 = this.range.getValue() * this.range.getValue();
        Predicate<Entity> predicate = entity1 -> entity1 instanceof LivingEntity livingentity2
            && mc.player.squaredDistanceTo(entity1) < d0
            && this.isInRange(livingentity2);
        switch (this.rotation.getMode()) {
            case "Silent", "Player" -> predicate = predicate.and(entity1 -> CombatUtil.isInFov(this.fov.getFloat(), entity1));
            case "None" -> {
                HitResult hitresult = mc.crosshairTarget;
                if (hitresult instanceof EntityHitResult
                    && ((EntityHitResult)hitresult).getEntity() instanceof LivingEntity livingentity1
                    && mc.player.squaredDistanceTo(livingentity1) < d0) {
                    return livingentity1;
                }

                return null;
            }
            default -> {
            }
        }

        LivingEntity livingentity = null;
        double d2 = Double.MAX_VALUE;

        for (Entity entity : TargetManager.getTargetListDefault()) {
            if (predicate.test(entity)) {
                double d1 = mc.player.distanceTo(entity);
                if (!(d2 < d1)) {
                    livingentity = (LivingEntity)entity;
                    d2 = d1;
                }
            }
        }

        return livingentity;
    }

    private void swapBack(boolean flag) {
        if (flag && this.switchBack.isEnabled() && this.preSwapSlot != -1) {
            int i = this.preSwapSlot;
            InventoryUtil.selectSlot(i);
        }

        this.tickDelay = -1;
        this.attackWindow = 0;
        this.preSwapSlot = -1;
        this.currentState = this.idleState;
        this.resetSwaps();
    }

    @EventHandler
    public void onTick(TickEvent tickevent) {
        if (this.swapsRemaining > 0) {
            int i = this.swapsRemaining--;
        }

        if (!Module.isNotInGame()) {
            if (this.attackPending && this.target != null && !this.isInRange(this.target)) {
                int j = this.airTicks++;
            } else {
                this.airTicks = 0;
            }

            if (this.tickDelay >= 0) {
                this.preAim();
                this.tickSwap();
            } else {
                if (this.pendingStun) {
                    this.pendingStun = false;
                    this.rotationHandle.markDone();
                }

                if (this.attackPending) {
                    this.swapsDone++;
                    if (this.swapsDone > 60) {
                        this.finish();
                        return;
                    }
                }

                if (!this.shouldSwap()) {
                    if (this.attackPending) {
                        this.finish();
                    } else {
                        this.target = null;
                        this.rotationHandle.markDone();
                    }
                } else if (this.attackPending) {
                    this.preAim();
                } else {
                    this.rotationHandle.markDone();
                }
            }
        }
    }

    private int getFallBlocks() {
        if (mc.player == null) {
            return 0;
        }

        int i = mc.player.age;
        if (this.candidateSlot == i) {
            return this.candidateScore;
        }

        this.candidateSlot = i;
        MotionPredictor motionpredictor = this.simulator;
        MotionRequest motionrequest = new MotionRequest(mc.player, 40, mc.player.getVelocity());
        MotionResult motionresult = motionpredictor.predict(motionrequest);
        this.candidateScore = motionresult.isCollided() ? motionresult.getHitCount() : Integer.MAX_VALUE;
        return this.candidateScore;
    }

    @EventHandler
    public void onSwitchSlot(HotbarKeyEvent hotbarkeyevent) {
        if (!Module.isNotInGame()) {
            if (this.tickDelay < 0) {
                if (this.currentState == this.idleState) {
                    this.currentState = this.state9;
                }

                if (this.shouldSwap()) {
                    this.currentState = (MaceSwapState)this.currentState.applyHotbar(hotbarkeyevent);
                }
            }
        }
    }

    @EventHandler
    public void onAttackKey(AttackKeyEvent attackkeyevent) {
        if (!Module.isNotInGame()) {
            if (this.tickDelay < 0) {
                if (this.currentState == this.idleState) {
                    this.currentState = this.state9;
                }

                if (!this.shouldSwap()) {
                    if (this.attackPending) {
                        MaceSwapState statenodeunary = this.state1;
                        this.enterState(statenodeunary, attackkeyevent);
                    }
                } else {
                    MaceSwapState statenodeunary1 = this.currentState;
                    this.enterState(statenodeunary1, attackkeyevent);
                }
            }
        }
    }
}
