package lol.lwes.voil.module.player;

import java.util.function.Supplier;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.ModeSetting;
import lol.lwes.voil.setting.NumberSetting;
import lol.lwes.voil.setting.RangeSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.CombatUtil;
import lol.lwes.voil.util.rotation.Rotation;
import lol.lwes.voil.util.rotation.SmoothRotationController;
import lol.lwes.voil.util.rotation.SmoothRotationState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.AbstractWindChargeEntity;
import net.minecraft.entity.projectile.BreezeWindChargeEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class WindchargeDeflect extends Module {
    private final ModeSetting rotation = new ModeSetting("Rotation", "Silent", "Rotation type", new String[]{"Normal", "Silent"});
    private final RangeSetting ticksToRotate = new RangeSetting("Ticks to Rotate", 0.0, 10.0, 1.0, 3.0, 1.0, "Over how many ticks the rotation take to rotate");
    private final NumberSetting fov = new NumberSetting("FOV", 5.0, 360.0, 160.0, 1.0, "Search FOV for windcharge");
    private final NumberSetting radius = new NumberSetting("Radius", 1.0, 3.0, 3.0, 0.1, "The radius in which to search for windcharges");
    private final NumberSetting predictTicks = new NumberSetting("Predict Ticks", 3.0, 40.0, 15.0, 1.0, "How many ticks to predict the windcharge's position");
    private final NumberSetting hitDelay = new NumberSetting(
        "Hit Delay", 0.0, 10.0, 1.0, 1.0, "Delay in ticks before hitting the windcharge after rotating to it"
    );
    private final BooleanSetting breezeCharges = new BooleanSetting("Breeze Charges", true, "Whether to deflect breeze windcharges as well");
    private final BooleanSetting wallCheck = new BooleanSetting("Wall Check", true, "Whether to check if the windcharge is behind a wall before deflecting it");
    private final BooleanSetting pauseInScreens = new BooleanSetting(
        "Pause In Screens", true, "Whether to pause the module when in a screen (inventory, chat, etc.)"
    );
    private AbstractWindChargeEntity target;
    private boolean rotating;
    private int hitDelayTicks;
    private SmoothRotationState rotationHandle = SmoothRotationState.createEnded();
    private static final String SILENT_MODE = "Silent";

    public WindchargeDeflect() {
        super("WindchargeDeflect", "Automatically deflects windcharge projectiles", Category.Player);
        Setting[] asetting = new Setting[]{
            this.rotation, this.ticksToRotate, this.fov, this.radius, this.predictTicks, this.hitDelay, this.breezeCharges, this.wallCheck, this.pauseInScreens
        };
        this.addSettings(asetting);
    }

    private boolean willHitPlayer(AbstractWindChargeEntity abstractwindchargeentity) {
        return !abstractwindchargeentity.isAlive() ? false : this.ticksUntilImpact(abstractwindchargeentity) >= 0;
    }

    @Override
    public void onTick() {
        if (mc.player != null && mc.world != null && mc.interactionManager != null) {
            if (this.hitDelayTicks > 0) {
                int i = this.hitDelayTicks--;
            } else if (!this.pauseInScreens.isEnabled() || mc.currentScreen == null) {
                if (this.target != null) {
                    AbstractWindChargeEntity abstractwindchargeentity1 = this.target;
                    if (!this.willHitPlayer(abstractwindchargeentity1)) {
                        this.target = null;
                        if (!this.rotating) {
                            VoilClient.getRotationController().release(this);
                        }
                    }
                }

                if (this.target == null) {
                    this.target = this.findTarget();
                    if (this.target == null) {
                        return;
                    }

                    this.rotating = false;
                }

                AbstractWindChargeEntity abstractwindchargeentity = this.target;
                if (this.rotationHandle.hasEnded() && !this.rotating) {
                    SmoothRotationController smoothrotationcontroller = VoilClient.getRotationController();
                    Supplier supplier3 = SmoothRotationController.smoothForTicks(this.ticksToRotate.getRandomInt());
                    Supplier supplier4 = () -> {
                        Vec3d vec3d1 = mc.player.getEyePos();
                        Vec3d vec3d = abstractwindchargeentity.getBoundingBox().getCenter();
                        return Rotation.fromPositions(vec3d1, vec3d);
                    };
                    Supplier supplier5 = () -> this.rotating;
                    boolean flag = this.rotation.is(SILENT_MODE);
                    Supplier supplier = supplier5;
                    Supplier supplier1 = supplier4;
                    Supplier supplier2 = supplier3;
                    WindchargeDeflect windchargedeflect1 = this;
                    this.rotationHandle = smoothrotationcontroller.start(windchargedeflect1, supplier2, supplier1, supplier, flag);
                }

                if (this.rotationHandle.isOnTarget() && !this.rotationHandle.hasEnded() && this.isInRadius(abstractwindchargeentity)) {
                    mc.interactionManager.attackEntity(mc.player, abstractwindchargeentity);
                    mc.player.swingHand(Hand.MAIN_HAND);
                    this.rotating = true;
                    this.target = null;
                    this.hitDelayTicks = this.hitDelay.getInt();
                }
            }
        }
    }

    @Override
    public void onDisable() {
        VoilClient.getRotationController().release(this);
    }

    @Override
    public void onEnable() {
        this.target = null;
        this.rotating = false;
        this.hitDelayTicks = 0;
        this.rotationHandle = SmoothRotationState.createEnded();
        VoilClient.getRotationController().release(this);
    }

    private AbstractWindChargeEntity findTarget() {
        AbstractWindChargeEntity abstractwindchargeentity = null;
        int i = Integer.MAX_VALUE;

        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof AbstractWindChargeEntity abstractwindchargeentity1
                && abstractwindchargeentity1.isAlive()
                && abstractwindchargeentity1.getOwner() != mc.player
                && (this.breezeCharges.isEnabled() || !(abstractwindchargeentity1 instanceof BreezeWindChargeEntity))
                && CombatUtil.isInFov(this.fov.getFloat(), abstractwindchargeentity1)
                && (!this.wallCheck.isEnabled() || mc.player.canSee(abstractwindchargeentity1))) {
                int j = this.ticksUntilImpact(abstractwindchargeentity1);
                if (j >= 0 && j < i) {
                    abstractwindchargeentity = abstractwindchargeentity1;
                    i = j;
                }
            }
        }

        return abstractwindchargeentity;
    }

    private int ticksUntilImpact(AbstractWindChargeEntity abstractwindchargeentity) {
        Vec3d vec3d = abstractwindchargeentity.getVelocity();
        if (vec3d.lengthSquared() < 1.0E-4) {
            return -1;
        }

        Box box = mc.player.getBoundingBox().expand(this.radius.getValue());
        Vec3d vec3d1 = abstractwindchargeentity.getEntityPos();
        int i = this.predictTicks.getInt();

        for (int j = 0; j <= i; j++) {
            Vec3d vec3d2 = vec3d1.add(vec3d.multiply(j));
            if (box.contains(vec3d2)) {
                return j;
            }
        }

        return -1;
    }

    private boolean isInRadius(AbstractWindChargeEntity abstractwindchargeentity) {
        double d0 = this.radius.getValue() * this.radius.getValue();
        return mc.player.getEyePos().squaredDistanceTo(abstractwindchargeentity.getBoundingBox().getCenter()) <= d0;
    }
}
