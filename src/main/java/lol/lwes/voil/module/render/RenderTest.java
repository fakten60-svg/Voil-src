package lol.lwes.voil.module.render;

import dev.prestigeclient.renderlib.impl.api.RenderAPI;
import java.awt.Color;
import java.util.function.Supplier;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.event.AttackEntityEvent;
import lol.lwes.voil.event.EventHandler;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.module.ModuleTag;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.ItemSlotsSetting;
import lol.lwes.voil.setting.KeybindSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.AimMathUtil;
import lol.lwes.voil.util.CombatUtil;
import lol.lwes.voil.util.SystemUtil;
import lol.lwes.voil.util.prediction.DefaultMotionPredictor;
import lol.lwes.voil.util.prediction.MotionPredictor;
import lol.lwes.voil.util.prediction.MotionRequest;
import lol.lwes.voil.util.prediction.MotionResult;
import lol.lwes.voil.util.render.Render2DUtil;
import lol.lwes.voil.util.render.Render3DUtil;
import lol.lwes.voil.util.render.RenderManager;
import lol.lwes.voil.util.rotation.Rotation;
import lol.lwes.voil.util.rotation.SmoothRotationController;
import lol.lwes.voil.util.rotation.SmoothRotationState;
import lol.lwes.voil.util.target.TargetManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class RenderTest extends Module {
    private static final RenderTest INSTANCE = new RenderTest();
    private static float animationDurationMs = 300.0F;
    private long enabledAt;
    private BooleanSetting drawBlur;
    private ItemSlotsSetting hotbarItem;
    private KeybindSetting activateKey;
    private BooleanSetting silentRotations;
    private BooleanSetting fallPredict;
    private boolean aimedAtTarget;
    private boolean attackedTarget;
    private SmoothRotationState rotationTask;
    private final MotionPredictor fallPredictor;
    private Vec3d predictedLanding;
    private boolean landingImminent;
    private static final long UNSET_TIME = -1L;

    public RenderTest() {
        super("RenderTest", "Render test", Category.Render);
        this.enabledAt = UNSET_TIME;
        this.drawBlur = new BooleanSetting("draw blur", false, "bb");
        this.hotbarItem = new ItemSlotsSetting("Hotbar item", "hi");
        this.activateKey = new KeybindSetting(
            "activate key", 0, false, "targets closest target until you either attack them, or acquire and lose them as a hit result"
        );
        this.silentRotations = new BooleanSetting("silent rots", false, "ssd");
        this.fallPredict = new BooleanSetting("Fall Predict", false, "ultra halal prediction test 3000");
        this.fallPredictor = DefaultMotionPredictor.create();
        ModuleTag[] amoduletag = new ModuleTag[]{ModuleTag.DEV};
        this.addTags(amoduletag);
        Setting[] asetting = new Setting[]{this.drawBlur, this.hotbarItem, this.activateKey, this.silentRotations, this.fallPredict};
        this.addSettings(asetting);
    }

    @Override
    public void onTick() {
        if (!Module.isNotInGame()) {
            LivingEntity livingentity = TargetManager.getPrimaryTargetDefault();
            if (livingentity != null) {
                if (this.fallPredict.isEnabled() && !livingentity.isOnGround()) {
                    MotionPredictor motionpredictor = this.fallPredictor;
                    MotionRequest motionrequest = new MotionRequest(livingentity);
                    MotionResult motionresult = motionpredictor.predict(motionrequest);
                    if (motionresult.isCollided()) {
                        this.predictedLanding = motionresult.getVelocity();
                        this.landingImminent = motionresult.getHitCount() >= 0 && motionresult.getHitCount() <= 10;
                    } else {
                        this.predictedLanding = null;
                        this.landingImminent = false;
                    }
                } else {
                    this.predictedLanding = null;
                    this.landingImminent = false;
                }

                if (!this.rotationTask.hasEnded() && this.rotationTask.isOnTarget()) {
                    if (mc.targetedEntity != null && mc.targetedEntity == TargetManager.getPrimaryTargetDefault()) {
                        this.aimedAtTarget = true;
                    } else if (this.aimedAtTarget) {
                        this.rotationTask.markDone();
                    }
                }

                if (this.activateKey.isPressed()) {
                    SmoothRotationController smoothrotationcontroller = VoilClient.getRotationController();
                    Supplier supplier3 = () -> {
                        LivingEntity livingentity1 = TargetManager.getPrimaryTargetDefault();
                        return livingentity1 == null
                            ? 0.0
                            : mc.player.getCameraPosVec(SystemUtil.getTickDelta()).distanceTo(AimMathUtil.closestPointOnEntity(livingentity1))
                                - CombatUtil.getEntityReach();
                    };
                    Supplier supplier4 = () -> {
                        Vec3d vec3d1 = mc.player.getCameraPosVec(SystemUtil.getTickDelta());
                        Vec3d vec3d = AimMathUtil.closestPointOnEntity(TargetManager.getPrimaryTargetDefault());
                        return Rotation.fromPositions(vec3d1, vec3d);
                    };
                    Supplier supplier5 = () -> this.attackedTarget;
                    boolean flag = this.silentRotations.isEnabled();
                    Supplier supplier = supplier5;
                    Supplier supplier1 = supplier4;
                    Supplier supplier2 = supplier3;
                    RenderTest rendertest1 = this;
                    this.rotationTask = smoothrotationcontroller.start(rendertest1, supplier2, supplier1, supplier, flag);
                }
            }
        }
    }

    @Override
    public void onRender2D() {
        if (Render2DUtil.blurRenderer != null && RenderManager.getDrawContext() != null) {
            if (this.enabledAt >= 0L) {
                if (this.drawBlur.isEnabled()) {
                    float f = Math.min(1.0F, (float)(System.currentTimeMillis() - this.enabledAt) / animationDurationMs);
                    float f1 = f * f * (3.0F - 2.0F * f);
                    Render2DUtil.drawBlurRounded(100.0F, 100.0F, 200.0F, 100.0F, 8.0F, 25.0F, f1);
                }
            }
        }
    }

    @Override
    public void onEnable() {
        this.rotationTask = SmoothRotationState.createEnded();
        this.aimedAtTarget = false;
        this.attackedTarget = false;
        this.enabledAt = System.currentTimeMillis();
        VoilClient.getRotationController().release(this);
    }

    public static void preload() {
    }

    @EventHandler
    public void onAttack(AttackEntityEvent attackentityevent) {
        if (attackentityevent.target == TargetManager.getPrimaryTargetDefault()) {
            this.attackedTarget = true;
        }
    }

    @EventHandler
    @Override
    public void onWorldRender(RenderAPI.RenderContext rendercontext) {
        if (this.fallPredict.isEnabled() && this.predictedLanding != null) {
            Color color = this.landingImminent ? Color.GREEN : new Color(255, 180, 0, 200);
            Render3DUtil.drawPillar(new Matrix4f(), this.predictedLanding, color, 0.3F, 1.0F);
        }
    }
}
