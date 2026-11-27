package lol.lwes.voil.module.combat;

import dev.prestigeclient.renderlib.impl.api.RenderAPI;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.event.AttackEntityEvent;
import lol.lwes.voil.event.EventHandler;
import lol.lwes.voil.mixin.accessor.MouseAccessor;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.ItemListSetting;
import lol.lwes.voil.setting.KeybindSetting;
import lol.lwes.voil.setting.ModeSetting;
import lol.lwes.voil.setting.MultiSelectSetting;
import lol.lwes.voil.setting.NumberSetting;
import lol.lwes.voil.setting.RangeSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.AimMathUtil;
import lol.lwes.voil.util.SystemUtil;
import lol.lwes.voil.util.prediction.DefaultMotionPredictor;
import lol.lwes.voil.util.prediction.MotionPredictor;
import lol.lwes.voil.util.prediction.MotionRequest;
import lol.lwes.voil.util.prediction.MotionResult;
import lol.lwes.voil.util.render.EasingFunction;
import lol.lwes.voil.util.render.Render3DUtil;
import lol.lwes.voil.util.render.UiEasings;
import lol.lwes.voil.util.rotation.Rotation;
import lol.lwes.voil.util.rotation.RotationAxisMode;
import lol.lwes.voil.util.rotation.SmoothRotationController;
import lol.lwes.voil.util.rotation.SmoothRotationState;
import lol.lwes.voil.util.target.TargetManager;
import lol.lwes.voil.util.target.TargetSupplier;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class AimAssist extends Module {
    public final ModeSetting style;
    public final ModeSetting mode;
    public final NumberSetting assistStrength;
    public final NumberSetting assistMaxStep;
    public final BooleanSetting flick;
    public final KeybindSetting holdKey;
    public final NumberSetting range;
    public final NumberSetting fov;
    public final BooleanSetting stickyTarget;
    public final NumberSetting stickyBlocks;
    public final NumberSetting switchDelay;
    public final RangeSetting smoothing;
    public final ModeSetting easing;
    public final ModeSetting axes;
    public final NumberSetting deadzone;
    public final NumberSetting multipointHorizontal;
    public final NumberSetting multipointVertical;
    public final BooleanSetting renderPoint;
    public final BooleanSetting predict;
    public final BooleanSetting selfPredict;
    public final NumberSetting predictTicks;
    public final RangeSetting reaction;
    public final BooleanSetting stopOnAttack;
    public final RangeSetting attackPause;
    public final BooleanSetting mouseOverride;
    public final NumberSetting mouseThreshold;
    public final NumberSetting overrideDelay;
    public final MultiSelectSetting require;
    public final ItemListSetting weapons;
    private LivingEntity target;
    private SmoothRotationState rotationHandle;
    private double acquireTime;
    private double reactionTicks;
    private double smoothingTicks;
    private double lastAssistTime;
    private double lastAttackTime;
    private double attackPauseTicks;
    private double lastManualMoveTime;
    private double lastFrameTime;
    private double lastMouseX;
    private double lastMouseY;
    private boolean mouseInitialized;
    private final MotionPredictor simulator;
    private double lastPredictTime;
    private Vec3d targetOffset;
    private Vec3d selfOffset;
    private double flickStart;
    private double flickEnd;
    private double flickProgress;
    private boolean flicking;
    private Vec3d aimPoint;
    private Vec3d renderedAimPoint;
    private final double renderLerp;

    public AimAssist() {
        super("AimAssist", "Smoothly assists your aim towards targets", Category.Combat);
        this.style = new ModeSetting(
            "Style", "Auto", "Auto fully rotates onto the target, Assist only moves your mouse toward it", new String[]{"Auto", "Assist"}
        );
        this.mode = new ModeSetting("Mode", "Legit", "Auto only: Legit moves your real view, Silent only rotates server-side", new String[]{"Legit", "Silent"});
        this.assistStrength = new NumberSetting("Assist Strength", 0.01, 1.0, 0.25, 0.01, "Assist only: fraction of the remaining angle closed per tick");
        this.assistMaxStep = new NumberSetting("Assist Max Step", 0.5, 45.0, 8.0, 0.5, "Assist only: max degrees moved per tick");
        this.flick = new BooleanSetting("Flick", true, "Assist only: close onto a new target with the Smoothing/Easing curve, then track with Assist Strength");
        this.holdKey = new KeybindSetting("Hold Key", 0, false, "Only assist while this key is held");
        this.range = new NumberSetting("Range", 1.0, 8.0, 4.5, 0.1, "Max distance to the target's hitbox");
        this.fov = new NumberSetting("FOV", 5.0, 360.0, 90.0, 1.0, "Only pick up targets inside this fov");
        this.stickyTarget = new BooleanSetting("Sticky Target", true, "Keep the current target until it dies or leaves range");
        this.stickyBlocks = new NumberSetting("Sticky Blocks", 0.0, 3.0, 1.0, 0.1, "Extra blocks a sticky target may leave range by before it is dropped");
        this.switchDelay = new NumberSetting("Switch Delay", 0.0, 40.0, 5.0, 1.0, "Min ticks between target switches");
        this.smoothing = new RangeSetting("Smoothing", 1.0, 40.0, 4.0, 8.0, 1.0, "Ticks to reach the target, randomized per acquisition");
        this.easing = new ModeSetting(
            "Easing",
            "Ease Out Quad",
            "Curve of the aim movement",
            new String[]{
                "Linear",
                "Ease Out Quad",
                "Ease Out Cubic",
                "Ease Out Sine",
                "Ease In Out Sine",
                "Ease In Out Quad",
                "Ease In Out Cubic",
                "Ease Out Expo",
                "Ease Out Circ",
                "Ease Out Smooth",
                "Ease In Out Bezier",
                "Overshoot"
            }
        );
        this.axes = new ModeSetting("Axes", "Both", "Which axes get assisted", new String[]{"Both", "Yaw", "Pitch"});
        this.deadzone = new NumberSetting("Deadzone", 0.0, 15.0, 1.5, 0.1, "Don't start aiming when the target is already within this many degrees");
        this.multipointHorizontal = new NumberSetting(
            "Multipoint Horizontal", 0.0, 100.0, 100.0, 1.0, "Horizontal aim point: 0% = hitbox center, 100% = point closest to you"
        );
        this.multipointVertical = new NumberSetting(
            "Multipoint Vertical", 0.0, 100.0, 50.0, 1.0, "Vertical aim point: 0% = hitbox center, 100% = point closest to you"
        );
        this.renderPoint = new BooleanSetting("Render Point", false, "Draw the current aim point on the target");
        this.predict = new BooleanSetting("Predict", true, "Lead the aim point by the target's simulated movement");
        this.selfPredict = new BooleanSetting("Self Predict", true, "Lead the aim by your own simulated movement");
        this.predictTicks = new NumberSetting("Predict Ticks", 0.0, 10.0, 2.0, 1.0, "How many ticks ahead to simulate");
        this.reaction = new RangeSetting(
            "Reaction", 0.0, 30.0, 2.0, 6.0, 1.0, "Ticks to wait before assisting a newly acquired target, randomized per acquisition"
        );
        this.stopOnAttack = new BooleanSetting("Stop On Attack", false, "Pause assisting for a moment after you hit the target");
        this.attackPause = new RangeSetting("Attack Pause", 0.0, 40.0, 3.0, 8.0, 1.0, "Ticks to pause after a hit");
        this.mouseOverride = new BooleanSetting("Mouse Override", true, "Pause assisting when you move your mouse on your own");
        this.mouseThreshold = new NumberSetting("Mouse Threshold", 1.0, 100.0, 15.0, 1.0, "Cursor pixels moved in one frame that count as manual");
        this.overrideDelay = new NumberSetting("Override Delay", 0.0, 60.0, 10.0, 1.0, "Ticks to stay paused after a mouse move");
        List list = List.of("No GUI");
        this.require = MultiSelectSetting.create(
            "Require", "Conditions that must hold for assisting", list, new String[]{"Weapon", "No GUI", "Not Using", "Not Sneaking"}
        );
        Item[] aitem = new Item[]{Items.NETHERITE_SWORD, Items.NETHERITE_AXE};
        this.weapons = new ItemListSetting("Weapons", "Items that count as a weapon for the Weapon requirement", aitem).allowCategoryMatch();
        this.rotationHandle = SmoothRotationState.createEnded();
        this.lastAssistTime = -1.0E9;
        this.lastAttackTime = -1.0E9;
        this.lastManualMoveTime = -1.0E9;
        this.lastFrameTime = -1.0E9;
        this.simulator = DefaultMotionPredictor.create();
        this.lastPredictTime = -1.0;
        this.targetOffset = Vec3d.ZERO;
        this.selfOffset = Vec3d.ZERO;
        this.flickStart = -1.0E9;
        this.flickEnd = -1.0E9;
        this.renderedAimPoint = new Vec3d(0.0, 0.0, 0.0);
        this.renderLerp = 0.1;
        this.holdKey.setHold(true);
        Setting[] asetting = new Setting[]{
            this.style,
            this.mode,
            this.assistStrength,
            this.assistMaxStep,
            this.flick,
            this.holdKey,
            this.range,
            this.fov,
            this.stickyTarget,
            this.stickyBlocks,
            this.switchDelay,
            this.smoothing,
            this.easing,
            this.axes,
            this.deadzone,
            this.multipointHorizontal,
            this.multipointVertical,
            this.renderPoint,
            this.predict,
            this.selfPredict,
            this.predictTicks,
            this.reaction,
            this.stopOnAttack,
            this.attackPause,
            this.mouseOverride,
            this.mouseThreshold,
            this.overrideDelay,
            this.require,
            this.weapons
        };
        this.addSettings(asetting);
        ModeSetting modesetting = this.style;
        Setting.dependSetting(this.mode, "Auto", modesetting);
        ModeSetting modesetting1 = this.style;
        Setting.dependSetting(this.assistStrength, "Assist", modesetting1);
        ModeSetting modesetting2 = this.style;
        Setting.dependSetting(this.assistMaxStep, "Assist", modesetting2);
        ModeSetting modesetting3 = this.style;
        Setting.dependSetting(this.flick, "Assist", modesetting3);
        Setting.dependSetting(this.smoothing, () -> this.style.is("Auto") || this.flick.isEnabled());
        Setting.dependSetting(this.easing, () -> this.style.is("Auto") || this.flick.isEnabled());
        Setting.dependSetting(this.stickyBlocks, this.stickyTarget);
        BooleanSetting booleansetting = this.stickyTarget;
        Setting.dependSettingFalse(this.switchDelay, booleansetting);
        Setting.dependSetting(this.predictTicks, () -> this.predict.isEnabled() || this.selfPredict.isEnabled());
        Setting.dependSetting(this.attackPause, this.stopOnAttack);
        Setting.dependSetting(this.mouseThreshold, this.mouseOverride);
        Setting.dependSetting(this.overrideDelay, this.mouseOverride);
        Setting.dependSetting(this.weapons, "Weapon", this.require);
    }

    private void applyAssist(double d0, double d1) {
        Rotation rotation = this.computeAimRotation(d0);
        if (rotation != null) {
            ClientPlayerEntity clientplayerentity = mc.player;
            Rotation rotation1 = Rotation.fromEntity(clientplayerentity);
            float f = MathHelper.wrapDegrees(rotation.getYawFloat() - rotation1.getYawFloat());
            float f1 = rotation.getPitchFloat() - rotation1.getPitchFloat();
            if (!(Math.hypot(f, f1) < this.deadzone.getValue())) {
                if (this.flick.isEnabled() && !this.flicking) {
                    this.flicking = true;
                    this.flickStart = d0;
                    this.flickEnd = d0 + this.smoothingTicks;
                    this.flickProgress = 0.0;
                }

                boolean flag = this.flick.isEnabled() && d0 < this.flickEnd;
                double d2;
                if (flag) {
                    double d3 = this.getEasing().apply((float)MathHelper.clamp((d0 - this.flickStart) / this.smoothingTicks, 0.0, 1.0));
                    d2 = Math.abs(1.0 - this.flickProgress) < 1.0E-6 ? 0.0 : (d3 - this.flickProgress) / (1.0 - this.flickProgress);
                    this.flickProgress = d3;
                } else {
                    d2 = 1.0 - Math.pow(1.0 - this.assistStrength.getValue(), d1);
                }

                double d7 = flag ? 360.0 : this.assistMaxStep.getValue() * d1;
                double d4 = MathHelper.clamp(f * d2, -d7, d7);
                double d5 = MathHelper.clamp(f1 * d2, -d7, d7);
                RotationAxisMode rotationaxismode = this.getAxes();
                if (rotationaxismode == RotationAxisMode.Pitch) {
                    d4 = 0.0;
                }

                if (rotationaxismode == RotationAxisMode.Yaw) {
                    d5 = 0.0;
                }

                if (mc.options.getInvertMouseY().getValue()) {
                    d5 = -d5;
                }

                double d6 = getMouseSensitivityFactor();
                MouseAccessor mouseaccessor = (MouseAccessor)mc.mouse;
                mouseaccessor.setDeltaX(mouseaccessor.getCursorDeltaX() + d4 / d6);
                mouseaccessor.setDeltaY(mouseaccessor.getCursorDeltaY() + d5 / d6);
            }
        }
    }

    private boolean isValidTarget(LivingEntity livingentity, Vec3d vec3d, Rotation rotation, boolean flag) {
        if (livingentity.isAlive() && !livingentity.isRemoved()) {
            double d0 = this.range.getValue() + (flag && this.stickyTarget.isEnabled() ? this.stickyBlocks.getValue() : 0.0);
            if (vec3d.distanceTo(AimMathUtil.closestPointOnEntity(livingentity)) > d0) {
                return false;
            }

            double d1 = this.fov.getValue() / 2.0 + (flag ? 20.0 : 0.0);
            Vec3d vec3d1 = livingentity.getBoundingBox().getCenter();
            Rotation rotation1 = Rotation.fromPositions(vec3d, vec3d1);
            return Rotation.angularDistance(rotation, rotation1) <= d1;
        } else {
            return false;
        }
    }

    private void setTarget(LivingEntity livingentity, double d0) {
        this.onDisable();
        this.target = livingentity;
        this.acquireTime = d0;
        this.lastAssistTime = d0;
        this.reactionTicks = this.reaction.getRandomValue();
        this.smoothingTicks = this.smoothing.getRandomValue();
        this.flicking = false;
    }

    private boolean isSilent() {
        return this.mode.is("Silent");
    }

    private EasingFunction getEasing() {
        return UiEasings.getEasing(this.easing.getMode());
    }

    private RotationAxisMode getAxes() {
        return switch (this.axes.getMode()) {
            case "Yaw" -> RotationAxisMode.Yaw;
            case "Pitch" -> RotationAxisMode.Pitch;
            default -> RotationAxisMode.YawAndPitch;
        };
    }

    public void reset() {
        this.onDisable();
        this.lastAssistTime = -1.0E9;
        this.lastAttackTime = -1.0E9;
        this.lastManualMoveTime = -1.0E9;
        this.mouseInitialized = false;
    }

    private static double getPartialTime() {
        return mc.player.age + SystemUtil.getTickDelta();
    }

    private void releaseRotation() {
        VoilClient.getRotationController().release(this);
        this.rotationHandle = SmoothRotationState.createEnded();
    }

    private Rotation computeAimRotation(double d0) {
        if (this.target != null && this.target.isAlive()) {
            this.updatePrediction(d0);
            Vec3d vec3d = getCameraPos();
            Vec3d vec3d1 = vec3d.add(this.selfOffset.x, 0.0, this.selfOffset.z);
            double d1 = vec3d.y + (this.selfPredict.isEnabled() && this.predictTicks.getInt() > 0 ? mc.player.getVelocity().y : 0.0);
            Vec3d vec3d2 = new Vec3d(vec3d1.x, d1, vec3d1.z);
            Box box = this.target.getBoundingBox().offset(this.targetOffset);
            Vec3d vec3d3 = box.getCenter();
            Vec3d vec3d4 = new Vec3d(
                MathHelper.clamp(vec3d2.x, box.minX, box.maxX), MathHelper.clamp(vec3d2.y, box.minY, box.maxY), MathHelper.clamp(vec3d2.z, box.minZ, box.maxZ)
            );
            double d2 = this.multipointHorizontal.getValue() / 100.0;
            double d3 = this.multipointVertical.getValue() / 100.0;
            Vec3d vec3d5 = new Vec3d(MathHelper.lerp(d2, vec3d3.x, vec3d4.x), MathHelper.lerp(d3, vec3d3.y, vec3d4.y), MathHelper.lerp(d2, vec3d3.z, vec3d4.z));
            float f = Rotation.fromPositions(vec3d1, vec3d5).getYawFloat();
            double d4 = Math.hypot(vec3d5.x - vec3d2.x, vec3d5.z - vec3d2.z);
            float f1 = (float)(-Math.toDegrees(Math.atan2(vec3d5.y - vec3d2.y, d4)));
            this.aimPoint = vec3d5;
            return new Rotation(f, f1);
        } else {
            return null;
        }
    }

    private Rotation computeAimRotation() {
        double d0 = getPartialTime();
        return this.computeAimRotation(d0);
    }

    private void renderAimPoint() {
        if (this.renderPoint.isEnabled() && this.target != null && this.aimPoint != null) {
            this.renderedAimPoint = this.renderedAimPoint.lerp(this.aimPoint, 0.1);
            Box box = new Box(
                this.renderedAimPoint.x - 0.05,
                this.renderedAimPoint.y - 0.05,
                this.renderedAimPoint.z - 0.05,
                this.renderedAimPoint.x + 0.05,
                this.renderedAimPoint.y + 0.05,
                this.renderedAimPoint.z + 0.05
            );
            Render3DUtil.drawBoxFilled(new Matrix4f(), box, new Color(255, 59, 59, 200));
        }
    }

    private Vec3d predictPosition(LivingEntity livingentity, Vec3d vec3d, int i) {
        MotionPredictor motionpredictor = this.simulator;
        MotionRequest motionrequest = new MotionRequest(livingentity, i, vec3d);
        MotionResult motionresult = motionpredictor.predict(motionrequest);
        return motionresult.getVelocity().subtract(motionresult.getPosition());
    }

    private static Vec3d getCameraPos() {
        return mc.player.getCameraPosVec(SystemUtil.getTickDelta());
    }

    private LivingEntity chooseTarget(double d0) {
        Vec3d vec3d = getCameraPos();
        ClientPlayerEntity clientplayerentity = mc.player;
        Rotation rotation = Rotation.fromEntity(clientplayerentity);
        if (this.stickyTarget.isEnabled() && this.target != null) {
            LivingEntity livingentity1 = this.target;
            if (this.isValidTarget(livingentity1, vec3d, rotation, true)) {
                return this.target;
            }
        }

        TargetSupplier<LivingEntity> targetsupplier = entity -> entity instanceof LivingEntity livingentity2 && this.isValidTarget(livingentity2, vec3d, rotation, false)
            ? Optional.of(livingentity2)
            : Optional.empty();
        ArrayList<LivingEntity> arraylist = TargetManager.getMappedTargets(false, targetsupplier);
        if (arraylist.isEmpty()) {
            return null;
        }

        LivingEntity livingentity = (LivingEntity)arraylist.get(0);
        return this.target != null
                && livingentity != this.target
                && this.isValidTarget(this.target, vec3d, rotation, true)
                && d0 - this.lastAssistTime < this.switchDelay.getValue()
            ? this.target
            : livingentity;
    }

    private Rotation getCurrentRotation() {
        if (this.isSilent() && VoilClient.getRotationManager().isActive()) {
            return VoilClient.getRotationManager().getTargetRotation();
        }

        ClientPlayerEntity clientplayerentity = mc.player;
        return Rotation.fromEntity(clientplayerentity);
    }

    private boolean canAssist() {
        if (this.holdKey.getKey() != 0 && !this.holdKey.isPressed(true)) {
            return false;
        }

        if (this.require.isSelected("No GUI") && mc.currentScreen != null) {
            return false;
        }

        if (this.require.isSelected("Weapon")) {
            ItemListSetting itemlistsetting = this.weapons;
            Item item1 = mc.player.getMainHandStack().getItem();
            if (!itemlistsetting.matches(item1)) {
                return false;
            }
        }

        return this.require.isSelected("Not Using") && mc.player.isUsingItem() ? false : !this.require.isSelected("Not Sneaking") || !mc.player.isSneaking();
    }

    private void updatePrediction(double d0) {
        if (this.lastPredictTime != d0) {
            this.lastPredictTime = d0;
            int i = this.predictTicks.getInt();
            Vec3d vec3d;
            if (this.predict.isEnabled() && this.target != null && i > 0) {
                LivingEntity livingentity = this.target;
                vec3d = this.predictPosition(livingentity, null, i);
            } else {
                vec3d = Vec3d.ZERO;
            }

            this.targetOffset = vec3d;
            if (this.selfPredict.isEnabled() && i > 0) {
                ClientPlayerEntity clientplayerentity = mc.player;
                vec3d = this.predictPosition(clientplayerentity, mc.player.getVelocity(), i);
            } else {
                vec3d = Vec3d.ZERO;
            }

            this.selfOffset = vec3d;
        }
    }

    private void updateMouseOverride(double d0) {
        double d1 = mc.mouse.getX();
        double d2 = mc.mouse.getY();
        if (!this.mouseInitialized) {
            this.mouseInitialized = true;
            this.lastMouseX = d1;
            this.lastMouseY = d2;
        } else {
            double d3 = Math.abs(d1 - this.lastMouseX) + Math.abs(d2 - this.lastMouseY);
            this.lastMouseX = d1;
            this.lastMouseY = d2;
            if (this.mouseOverride.isEnabled() && mc.currentScreen == null) {
                if (d3 > this.mouseThreshold.getValue()) {
                    this.lastManualMoveTime = d0 + this.overrideDelay.getValue();
                    this.releaseRotation();
                }
            }
        }
    }

    @Override
    public void onEnable() {
        this.reset();
    }

    @Override
    public void onDisable() {
        this.releaseRotation();
        this.target = null;
        this.aimPoint = null;
    }

    @EventHandler
    public void onAttack(AttackEntityEvent attackentityevent) {
        if (this.target != null && attackentityevent.target == this.target) {
            this.lastAttackTime = getPartialTime();
            this.attackPauseTicks = this.attackPause.getRandomValue();
            if (this.stopOnAttack.isEnabled()) {
                this.releaseRotation();
            }
        }
    }

    private static double getMouseSensitivityFactor() {
        double d0 = mc.options.getMouseSensitivity().getValue() * 0.6 + 0.2;
        return d0 * d0 * d0 * 8.0 * 0.15;
    }

    @Override
    public void onWorldRender(RenderAPI.RenderContext rendercontext) {
        if (Module.isNotInGame()) {
            this.onDisable();
        } else {
            double d0 = getPartialTime();
            double d1 = Math.min(1.0, Math.max(1.0E-4, d0 - this.lastFrameTime));
            this.lastFrameTime = d0;
            this.updateMouseOverride(d0);
            this.renderAimPoint();
            if (!this.canAssist()) {
                this.onDisable();
            } else {
                LivingEntity livingentity = this.chooseTarget(d0);
                if (livingentity == null) {
                    this.onDisable();
                } else {
                    if (livingentity != this.target) {
                        this.setTarget(livingentity, d0);
                    }

                    if (!(d0 - this.acquireTime < this.reactionTicks)) {
                        if (!(d0 < this.lastManualMoveTime)) {
                            if (!this.stopOnAttack.isEnabled() || !(d0 - this.lastAttackTime < this.attackPauseTicks)) {
                                if (this.style.is("Assist")) {
                                    if (!this.rotationHandle.hasEnded()) {
                                        this.releaseRotation();
                                    }

                                    this.applyAssist(d0, d1);
                                } else if (this.rotationHandle.hasEnded()) {
                                    Rotation rotation = this.computeAimRotation(d0);
                                    if (rotation != null && !(Rotation.angularDistance(this.getCurrentRotation(), rotation) < this.deadzone.getValue())) {
                                        double d2 = d0 + this.smoothingTicks;
                                        SmoothRotationController smoothrotationcontroller = VoilClient.getRotationController();
                                        Supplier supplier3 = () -> d2 - getPartialTime();
                                        Supplier supplier4 = this::computeAimRotation;
                                        Supplier supplier5 = () -> false;
                                        boolean flag1 = this.isSilent();
                                        EasingFunction easingfunction = this.getEasing();
                                        RotationAxisMode rotationaxismode = this.getAxes();
                                        Object object = null;
                                        boolean flag = flag1;
                                        Supplier supplier = supplier5;
                                        Supplier supplier1 = supplier4;
                                        Supplier supplier2 = supplier3;
                                        AimAssist aimassist1 = this;
                                        this.rotationHandle = smoothrotationcontroller.startWith(
                                            aimassist1,
                                            supplier2,
                                            supplier1,
                                            supplier,
                                            flag,
                                            false,
                                            (SmoothRotationState)object,
                                            rotationaxismode,
                                            easingfunction
                                        );
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
