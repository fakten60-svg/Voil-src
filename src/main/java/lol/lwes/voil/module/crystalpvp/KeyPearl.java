package lol.lwes.voil.module.crystalpvp;

import java.util.Comparator;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.KeybindSetting;
import lol.lwes.voil.setting.ModeSetting;
import lol.lwes.voil.setting.NumberSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.inventory.InventoryUtil;
import lol.lwes.voil.util.render.EasingFunction;
import lol.lwes.voil.util.render.UiEasings;
import lol.lwes.voil.util.rotation.Rotation;
import lol.lwes.voil.util.rotation.RotationAxisMode;
import lol.lwes.voil.util.rotation.SmoothRotationController;
import lol.lwes.voil.util.rotation.SmoothRotationState;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class KeyPearl extends Module {
    private final ModeSetting mode = new ModeSetting("Mode", "Looking", "How to rotate", new String[]{"Looking", "Normal", "Silent"});
    private final ModeSetting aim = new ModeSetting("Aim", "Pitch", "How to aim the pearl throw", new String[]{"Pitch", "Target"});
    private final NumberSetting pitch = new NumberSetting("Pitch", -90.0, 90.0, 45.0, 1.0, "Pitch of the throw");
    private final NumberSetting range = new NumberSetting("Range", 5.0, 80.0, 40.0, 1.0, "Max target distance");
    private final BooleanSetting predict = new BooleanSetting("Predict", true, "Leads the target by their movement over the pearl's flight time");
    private final NumberSetting smoothTicks = new NumberSetting("Smooth ticks", 1.0, 20.0, 3.0, 1.0, "Ticks to reach the aim before throwing");
    private final KeybindSetting key = new KeybindSetting("Key", 0, false, "Key to throw");
    private final BooleanSetting onHurt = new BooleanSetting("On Hurt", false, "Throws a pearl when you take damage");
    private final BooleanSetting swing = new BooleanSetting("Swing", false, "Swing the arm");
    private final BooleanSetting switchBack = new BooleanSetting("Switch Back", false, "Switches back to the old slot after throwing");
    private final NumberSetting delay = new NumberSetting("Delay", 0.0, 10.0, 0.0, 1.0, "Ticks before switching back");
    private SmoothRotationState rotationRequest;
    private boolean rotating;
    private boolean switchedToPearl;
    private int previousSlot = -1;
    private int ticksSinceThrow;
    private boolean keyHeld;
    private int lastHurtTime;
    private PlayerEntity target;
    private PlayerEntity candidateTarget;
    private Vec3d lastTargetPos;
    private Vec3d targetVelocity = Vec3d.ZERO;

    public KeyPearl() {
        super("KeyPearl", "Throws an Ender Pearl with the key", Category.CrystalPvP);
        Setting[] asetting = new Setting[]{
            this.mode, this.aim, this.pitch, this.range, this.predict, this.smoothTicks, this.key, this.onHurt, this.swing, this.switchBack, this.delay
        };
        this.addSettings(asetting);
        Setting.dependSetting(this.delay, this.switchBack);
        ModeSetting modesetting = this.aim;
        BooleanSupplier booleansupplier = () -> !this.mode.is("Looking");
        modesetting.setVisibleWhen(booleansupplier);
        NumberSetting numbersetting = this.smoothTicks;
        BooleanSupplier booleansupplier1 = () -> !this.mode.is("Looking");
        numbersetting.setVisibleWhen(booleansupplier1);
        NumberSetting numbersetting1 = this.pitch;
        BooleanSupplier booleansupplier2 = () -> !this.mode.is("Looking") && this.aim.is("Pitch");
        numbersetting1.setVisibleWhen(booleansupplier2);
        NumberSetting numbersetting2 = this.range;
        BooleanSupplier booleansupplier3 = () -> !this.mode.is("Looking") && this.aim.is("Target");
        numbersetting2.setVisibleWhen(booleansupplier3);
        BooleanSetting booleansetting = this.predict;
        BooleanSupplier booleansupplier4 = () -> !this.mode.is("Looking") && this.aim.is("Target");
        booleansetting.setVisibleWhen(booleansupplier4);
    }

    private boolean isTargetAim() {
        return !this.mode.is("Looking") && this.aim.is("Target");
    }

    private Rotation getAimRotation() {
        if (this.target == null) {
            return new Rotation(mc.player.getYaw(), this.pitch.getFloat());
        }

        if (!this.target.isAlive() || this.target.isRemoved()) {
            return null;
        }

        Vec3d vec3d = mc.player.getEyePos().subtract(0.0, 0.1, 0.0);
        Vec3d vec3d1 = getFeetPos(this.target);
        float[] afloat = calcThrowAngles(vec3d, vec3d1);
        if (afloat == null) {
            return null;
        }

        if (this.predict.isEnabled() && this.targetVelocity.lengthSquared() - 1.0E-6 > 0.0) {
            Vec3d vec3d2 = new Vec3d(this.targetVelocity.x, 0.0, this.targetVelocity.z).multiply(afloat[2]);
            Vec3d vec3d3 = vec3d1.add(vec3d2);
            float[] afloat1 = calcThrowAngles(vec3d, vec3d3);
            if (afloat1 != null) {
                afloat = afloat1;
            }
        }

        return new Rotation(afloat[0], afloat[1]);
    }

    private static float[] calcThrowAngles(Vec3d vec3d, Vec3d vec3d1) {
        double d0 = vec3d1.x - vec3d.x;
        double d1 = vec3d1.z - vec3d.z;
        double d2 = vec3d1.y - vec3d.y;
        double d3 = Math.sqrt(d0 * d0 + d1 * d1);
        float f = (float)Math.toDegrees(Math.atan2(d1, d0)) - 90.0F;
        if (d3 < 0.5) {
            return new float[]{f, 90.0F, 1.0F};
        }

        float f1 = Float.NaN;
        int i = 0;
        double d4 = Double.MAX_VALUE;

        for (float f2 = 89.0F; f2 >= -89.0F; f2 -= 0.25F) {
            double d5 = Math.toRadians(f2);
            double d6 = 1.5 * Math.cos(d5);
            double d7 = -1.5 * Math.sin(d5);
            double d8 = 0.0;
            double d9 = 0.0;

            for (int j = 1; j <= 300; j++) {
                double d10 = d8 + d6;
                double d11 = d9 + d7;
                if (d10 >= d3) {
                    double d12 = Math.abs(d9 + (d11 - d9) * ((d3 - d8) / d6) - d2);
                    if (d12 < 0.25) {
                        return new float[]{f, f2, j};
                    }

                    if (d12 < d4) {
                        d4 = d12;
                        f1 = f2;
                        i = j;
                    }
                    break;
                }

                d8 = d10;
                d9 = d11;
                d6 *= 0.99;
                d7 = d7 * 0.99 - 0.03;
                if (d9 < d2 - 64.0) {
                    break;
                }
            }
        }

        return d4 < 1.5 ? new float[]{f, f1, i} : null;
    }

    private void updateTargetMotion(PlayerEntity playerentity) {
        if (playerentity != this.candidateTarget) {
            this.candidateTarget = playerentity;
            this.lastTargetPos = null;
            this.targetVelocity = Vec3d.ZERO;
        }

        if (playerentity != null) {
            Vec3d vec3d = getFeetPos(playerentity);
            if (this.lastTargetPos != null) {
                this.targetVelocity = vec3d.subtract(this.lastTargetPos);
            }

            this.lastTargetPos = vec3d;
        }
    }

    @Override
    public void onTick() {
        if (mc.player != null && mc.world != null) {
            boolean flag = this.key.isPressed() && mc.currentScreen == null;
            boolean flag1 = flag && !this.keyHeld;
            this.keyHeld = flag;
            boolean flag2 = this.onHurt.isEnabled() && mc.player.hurtTime > this.lastHurtTime;
            this.lastHurtTime = mc.player.hurtTime;
            if (this.isTargetAim()) {
                PlayerEntity playerentity = this.rotating ? this.target : this.findTarget();
                this.updateTargetMotion(playerentity);
            }

            if ((flag1 || flag2) && !this.isBusy()) {
                this.startThrow();
            }

            if (this.rotating && this.rotationRequest != null && (this.rotationRequest.isOnTarget() || this.rotationRequest.hasEnded())) {
                if (this.mode.is("Normal")) {
                    Rotation rotation = this.getAimRotation();
                    if (rotation != null) {
                        if (this.target != null) {
                            mc.player.setYaw(rotation.getYawFloat());
                        }

                        mc.player.setPitch(rotation.getPitchFloat());
                    }
                }

                this.throwPearl();
                this.rotating = false;
            }

            if (this.rotationRequest != null && !this.rotating && this.ticksSinceThrow >= 1) {
                VoilClient.getRotationController().release(this);
                this.rotationRequest = null;
                this.target = null;
            }

            if (this.switchedToPearl && !this.rotating && this.ticksSinceThrow >= this.delay.getValue()) {
                if (this.switchBack.isEnabled() && this.previousSlot != -1) {
                    int j = this.previousSlot;
                    InventoryUtil.selectSlot(j);
                }

                this.switchedToPearl = false;
                this.previousSlot = -1;
            }

            int i = this.ticksSinceThrow++;
        }
    }

    @Override
    public void onDisable() {
        VoilClient.getRotationController().release(this);
        this.rotationRequest = null;
        this.rotating = false;
        this.switchedToPearl = false;
        this.previousSlot = -1;
        this.target = null;
        this.candidateTarget = null;
        this.lastTargetPos = null;
        this.targetVelocity = Vec3d.ZERO;
    }

    private boolean isBusy() {
        return this.rotating || this.rotationRequest != null || this.switchedToPearl;
    }

    private static Vec3d getFeetPos(PlayerEntity playerentity) {
        Box box = playerentity.getBoundingBox();
        return new Vec3d((box.minX + box.maxX) / 2.0, box.minY, (box.minZ + box.maxZ) / 2.0);
    }

    private void throwPearl() {
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        if (this.swing.isEnabled()) {
            mc.player.swingHand(Hand.MAIN_HAND);
        }

        this.ticksSinceThrow = 0;
    }

    private PlayerEntity findTarget() {
        double d0 = this.range.getValue();
        return mc.world
            .getPlayers()
            .stream()
            .filter(
                abstractclientplayerentity -> abstractclientplayerentity != mc.player
                    && abstractclientplayerentity.isAlive()
                    && !abstractclientplayerentity.isSpectator()
                    && mc.player.distanceTo(abstractclientplayerentity) <= d0
            )
            .min(Comparator.comparingDouble(abstractclientplayerentity -> mc.player.squaredDistanceTo(abstractclientplayerentity)))
            .orElse(null);
    }

    private void startThrow() {
        int i = InventoryUtil.findInventorySlot(Items.ENDER_PEARL);
        if (i != -1) {
            if (!mc.player.getItemCooldownManager().isCoolingDown(mc.player.getInventory().getStack(i))) {
                if (this.isTargetAim()) {
                    this.target = this.candidateTarget;
                    if (this.target == null || this.getAimRotation() == null) {
                        this.target = null;
                        return;
                    }
                }

                this.previousSlot = mc.player.getInventory().getSelectedSlot();
                InventoryUtil.selectSlot(i);
                this.switchedToPearl = true;
                this.ticksSinceThrow = 0;
                if (this.mode.is("Looking")) {
                    this.throwPearl();
                } else {
                    RotationAxisMode rotationaxismode = this.mode.is("Normal") && this.target == null ? RotationAxisMode.Pitch : RotationAxisMode.YawAndPitch;
                    SmoothRotationController smoothrotationcontroller = VoilClient.getRotationController();
                    Supplier supplier2 = SmoothRotationController.smoothForTicks(this.smoothTicks.getValue());
                    Supplier<Rotation> supplier3 = this::getAimRotation;
                    boolean flag1 = this.mode.is("Silent");
                    EasingFunction easingfunction = UiEasings::linear;
                    RotationAxisMode rotationaxismode1 = rotationaxismode;
                    Object object = null;
                    boolean flag = flag1;
                    Object object1 = null;
                    Supplier supplier = supplier3;
                    Supplier supplier1 = supplier2;
                    KeyPearl keypearl1 = this;
                    this.rotationRequest = smoothrotationcontroller.startWith(
                        keypearl1, supplier1, supplier, (Supplier<Boolean>)object1, flag, true, (SmoothRotationState)object, rotationaxismode1, easingfunction
                    );
                    this.rotating = true;
                }
            }
        }
    }
}
