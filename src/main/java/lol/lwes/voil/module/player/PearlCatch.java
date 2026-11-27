package lol.lwes.voil.module.player;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BooleanSupplier;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.event.EventHandler;
import lol.lwes.voil.event.ItemUseEvent;
import lol.lwes.voil.event.PreRenderEvent;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.KeybindSetting;
import lol.lwes.voil.setting.ModeSetting;
import lol.lwes.voil.setting.NumberSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.inventory.InventoryUtil;
import lol.lwes.voil.util.rotation.RotationManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import net.minecraft.world.RaycastContext;

public class PearlCatch extends Module {
    private final ModeSetting activation = new ModeSetting("Activation", "Auto", "How to activate pearl catch", new String[]{"On Key", "Auto"});
    private final KeybindSetting keybind = new KeybindSetting("Keybind", 0, false, "Keybind for pearl catch");
    private final ModeSetting onActivation = new ModeSetting("On Activation", "Looking", "How to rotate", new String[]{"Pitch", "Looking", "Look Adjust"});
    private final NumberSetting pitch;
    private final ModeSetting rotation;
    private final BooleanSetting predict;
    private final NumberSetting windChargeDelay;
    private final BooleanSetting switchBack;
    private final NumberSetting switchBackDelay;
    private int rotationTicks;
    private int switchBackTicks;
    private int previousSlot;
    private float targetPitch;
    private float pitchStep;
    private float currentPitch;
    private boolean pitchActive;
    private boolean switchPending;
    private int predictOffset;
    private Vec3d pearlStartPos;
    private Vec3d pearlVelocity;
    private boolean simulated;
    private final Set processedTicks;
    private boolean inRender;

    public PearlCatch() {
        super("PearlCatch", "Catches pearl for mace combos", Category.Player);
        NumberSetting numbersetting = null;
        this.pitch = new NumberSetting("Pitch", -90.0, 90.0, 80.0, 1.0, "Pitch to throw pearl at");
        this.rotation = new ModeSetting("Rotation", "Silent", "How to rotate", new String[]{"Normal", "Silent"});
        this.predict = new BooleanSetting("Predict", true, "Simulates the pearl and picks the throw tick");
        this.windChargeDelay = new NumberSetting("Wind Charge Delay", 0.0, 20.0, 2.0, 1.0, "Delay in ticks before throwing wind charge");
        this.switchBack = new BooleanSetting("Switch Back", true, "Switch back to original slot after throwing");
        this.switchBackDelay = new NumberSetting("Switch Back Delay", 0.0, 20.0, 1.0, 1.0, "Delay in ticks before switching back");
        this.rotationTicks = -1;
        this.switchBackTicks = -1;
        this.previousSlot = -1;
        this.processedTicks = new HashSet();
        Setting[] asetting = new Setting[]{
            this.activation,
            this.onActivation,
            this.pitch,
            this.keybind,
            this.rotation,
            this.predict,
            this.windChargeDelay,
            this.switchBack,
            this.switchBackDelay
        };
        this.addSettings(asetting);
        numbersetting = this.windChargeDelay;
        BooleanSupplier booleansupplier = () -> this.activation.is("On Key") || this.predict.isEnabled();
        numbersetting.setVisibleWhen(booleansupplier);
        Setting.dependAny(this.keybind, this.activation, new String[]{"On Key"});
        Setting.dependAny(this.pitch, this.onActivation, new String[]{"Pitch"});
        Setting.dependAny(this.switchBack, this.activation, new String[]{"On Key"});
        Setting.dependSetting(this.switchBackDelay, this.switchBack);
    }

    private int simulateTrajectory(Vec3d[] avec3d) {
        Vec3d vec3d = this.pearlStartPos;
        Vec3d vec3d1 = this.pearlVelocity;
        avec3d[0] = vec3d;

        for (int i = 1; i < avec3d.length; i++) {
            Vec3d vec3d2 = vec3d.add(vec3d1);
            if (this.raycastHits(vec3d, vec3d2)) {
                return i;
            }

            avec3d[i] = vec3d2;
            vec3d = vec3d2;
            vec3d1 = new Vec3d(vec3d1.x * 0.99, vec3d1.y * 0.99 - 0.03, vec3d1.z * 0.99);
        }

        return avec3d.length;
    }

    private void scheduleSwitchBack() {
        this.switchPending = false;
        if (this.switchBack.isEnabled() && this.previousSlot != -1) {
            this.switchBackTicks = this.switchBackDelay.getInt();
        }
    }

    private Vec3d getPlayerMotion() {
        double d0 = mc.player.getX() - mc.player.lastX;
        double d1 = mc.player.getY() - mc.player.lastY;
        double d2 = mc.player.getZ() - mc.player.lastZ;
        return new Vec3d(d0, mc.player.isOnGround() ? 0.0 : d1, d2);
    }

    @Override
    public void onTick() {
        this.inRender = false;
        if (mc.player != null && mc.world != null) {
            if (this.switchBackTicks == 0) {
                if (this.previousSlot != -1) {
                    mc.player.getInventory().setSelectedSlot(this.previousSlot);
                    this.previousSlot = -1;
                }

                this.switchBackTicks = -1;
            } else if (this.switchBackTicks > 0) {
                this.switchBackTicks--;
            } else if (this.switchPending) {
                this.predictOffset++;
                if (!this.simulated) {
                    for (Entity entity : mc.world.getEntities()) {
                        if (entity instanceof EnderPearlEntity
                            && !this.processedTicks.contains(entity.getId())
                            && ((EnderPearlEntity)entity).getOwner() == mc.player) {
                            this.processedTicks.add(entity.getId());
                            Vec3d vec3d3 = new Vec3d(entity.getX(), entity.getY(), entity.getZ());
                            Vec3d vec3d4 = entity.getVelocity();
                            Vec3d vec3d = null;
                            double d0 = Double.MAX_VALUE;
                            int l1 = Math.max(0, this.predictOffset);
                            int i = Math.min(60, l1 + 10);

                            for (int j = 0; j <= i; j++) {
                                Vec3d vec3d1 = vec3d4;

                                for (int k = 0; k < j; k++) {
                                    vec3d1 = new Vec3d(vec3d1.x / 0.99, (vec3d1.y + 0.03) / 0.99, vec3d1.z / 0.99);
                                }

                                Vec3d vec3d5 = this.pearlStartPos;
                                Vec3d vec3d2 = vec3d1;

                                for (int l = 0; l < j; l++) {
                                    vec3d5 = vec3d5.add(vec3d2);
                                    vec3d2 = new Vec3d(vec3d2.x * 0.99, vec3d2.y * 0.99 - 0.03, vec3d2.z * 0.99);
                                }

                                double d1 = vec3d5.squaredDistanceTo(vec3d3);
                                if (d1 < d0) {
                                    d0 = d1;
                                    vec3d = vec3d1;
                                }
                            }

                            if (vec3d != null && d0 < 4.0) {
                                this.pearlVelocity = vec3d;
                                this.simulated = true;
                            }

                            return;
                        }
                    }

                    int j1 = Math.max(1, this.windChargeDelay.getInt());
                    if (this.predictOffset >= j1) {
                        int i2 = this.predictOffset;
                        Vec3d[] avec3d = new Vec3d[i2 + 61];
                        int k1 = this.simulateTrajectory(avec3d);
                        if (k1 <= this.predictOffset) {
                            this.scheduleSwitchBack();
                        } else {
                            float[] afloat = this.solveRotation(avec3d, k1);
                            if (afloat != null) {
                                InventoryUtil.hasItemInHotbar(Items.WIND_CHARGE);
                                boolean flag5 = this.rotation.is("Silent");
                                if (flag5) {
                                    RotationManager rotationmanager1 = VoilClient.getRotationManager();
                                    float f1 = afloat[0];
                                    rotationmanager1.requestFromCallerAngles(f1, afloat[1]);
                                } else {
                                    mc.player.setYaw(afloat[0]);
                                    mc.player.setPitch(clampPitch(afloat[1]));
                                }

                                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                                if (flag5) {
                                    VoilClient.getRotationManager().reset();
                                }

                                this.scheduleSwitchBack();
                            } else {
                                if (this.predictOffset > j1 + 20) {
                                    this.scheduleSwitchBack();
                                }
                            }
                        }
                    }
                }
            } else if (this.rotationTicks == 0) {
                InventoryUtil.hasItemInHotbar(Items.WIND_CHARGE);
                boolean flag3 = this.rotation.is("Silent");
                boolean flag4 = this.activation.is("On Key") && this.onActivation.is("Pitch");
                if (flag3) {
                    float f6 = flag4 ? this.pitch.getFloat() : mc.player.getPitch();
                    if (flag4 || this.targetPitch != 0.0F) {
                        RotationManager rotationmanager = VoilClient.getRotationManager();
                        float f2 = mc.player.getYaw();
                        rotationmanager.requestFromCallerAngles(f2, clampPitch(f6 + this.targetPitch));
                    }
                } else if (flag4) {
                    mc.player.setPitch(this.pitch.getFloat());
                } else if (this.targetPitch != 0.0F) {
                    mc.player.setPitch(clampPitch(mc.player.getPitch() + (this.targetPitch - this.currentPitch)));
                }

                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                if (flag3) {
                    VoilClient.getRotationManager().reset();
                }

                this.rotationTicks = -1;
                this.currentPitch = 0.0F;
                float f3 = 0.0F;
                PearlCatch pearlcatch1 = this;
                pearlcatch1.pitchStep = f3;
                this.targetPitch = f3;
                if (this.switchBack.isEnabled() && this.previousSlot != -1) {
                    this.switchBackTicks = this.switchBackDelay.getInt();
                }
            } else if (this.rotationTicks > 0) {
                if (this.rotation.is("Normal") && this.pitchStep != 0.0F) {
                    mc.player.setPitch(clampPitch(mc.player.getPitch() + this.pitchStep));
                    this.currentPitch = this.currentPitch + this.pitchStep;
                }

                int i1 = this.rotationTicks--;
            } else {
                boolean flag = this.keybind.isPressed();
                boolean flag1 = flag && !this.pitchActive;
                this.pitchActive = flag;
                if (this.activation.is("On Key")
                    && flag1
                    && InventoryUtil.containsItemInHotbar(Items.WIND_CHARGE)
                    && InventoryUtil.containsItemInHotbar(Items.ENDER_PEARL)) {
                    if (this.switchBack.isEnabled()) {
                        this.previousSlot = mc.player.getInventory().getSelectedSlot();
                    }

                    InventoryUtil.hasItemInHotbar(Items.ENDER_PEARL);
                    boolean flag2 = this.onActivation.is("Pitch");
                    if (flag2 && this.rotation.is("Silent")) {
                        RotationManager rotationmanager2 = VoilClient.getRotationManager();
                        float f4 = mc.player.getYaw();
                        rotationmanager2.requestFromCallerAngles(f4, this.pitch.getFloat());
                    } else if (flag2) {
                        mc.player.setPitch(this.pitch.getFloat());
                    }

                    float f = flag2 ? this.pitch.getFloat() : mc.player.getPitch();
                    mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                    if (this.predict.isEnabled()) {
                        float f5 = mc.player.getYaw();
                        this.startPrediction(f5, f, 0);
                    } else {
                        this.startPitchRotation(this.windChargeDelay.getInt(), this.onActivation.is("Look Adjust") ? getAdjustedPitch(f) : 0.0F);
                    }
                }
            }
        }
    }

    @Override
    public void onDisable() {
        this.rotationTicks = -1;
        this.switchBackTicks = -1;
        this.previousSlot = -1;
        this.currentPitch = 0.0F;
        float f = 0.0F;
        PearlCatch pearlcatch1 = this;
        pearlcatch1.pitchStep = f;
        this.targetPitch = f;
        this.pitchActive = false;
        this.switchPending = false;
        this.processedTicks.clear();
    }

    private void startPrediction(float f, float f1, int i) {
        Vec3d vec3d = this.getPlayerMotion();
        this.pearlStartPos = new Vec3d(mc.player.getX(), mc.player.getEyeY() - 0.1, mc.player.getZ());
        this.pearlVelocity = getRotationVector(f, f1).multiply(1.5).add(vec3d);
        this.predictOffset = i;
        this.simulated = false;
        this.switchPending = true;
        this.processedTicks.clear();

        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof EnderPearlEntity && ((EnderPearlEntity)entity).getOwner() == mc.player) {
                this.processedTicks.add(entity.getId());
            }
        }
    }

    private boolean raycastHits(Vec3d vec3d, Vec3d vec3d1) {
        return mc.world.raycast(new RaycastContext(vec3d, vec3d1, ShapeType.COLLIDER, FluidHandling.NONE, mc.player)).getType() != Type.MISS;
    }

    private void startPitchRotation(int i, float f) {
        this.rotationTicks = i;
        this.targetPitch = f;
        this.currentPitch = 0.0F;
        this.pitchStep = f / (i + 1);
    }

    private static float getAdjustedPitch(float f) {
        if (f < -89.0F) {
            return 0.0F;
        } else if (f < -66.0F) {
            return 2.0F;
        } else if (f < -56.0F) {
            return 4.0F;
        } else if (f < -50.0F) {
            return 5.0F;
        } else if (f < -30.0F) {
            return 8.0F;
        } else {
            return f < -25.0F ? 11.0F : 0.0F;
        }
    }

    private static double predictionError(Vec3d vec3d, Vec3d vec3d1, double d0) {
        return vec3d.subtract(vec3d1.multiply(d0)).length() - 1.5 * d0;
    }

    private static float clampPitch(float f) {
        return MathHelper.clamp(f, -90.0F, 90.0F);
    }

    private static Vec3d getRotationVector(float f, float f1) {
        float f2 = -f * (float) (Math.PI / 180.0) - (float) Math.PI;
        float f3 = -f1 * (float) (Math.PI / 180.0);
        float f4 = -MathHelper.cos(f3);
        return new Vec3d(MathHelper.sin(f2) * f4, MathHelper.sin(f3), MathHelper.cos(f2) * f4);
    }

    private float[] solveRotation(Vec3d[] avec3d, int i) {
        Vec3d vec3d = new Vec3d(mc.player.getX(), mc.player.getEyeY(), mc.player.getZ());
        Vec3d vec3d1 = this.getPlayerMotion();
        int j = this.predictOffset;

        for (int k = 1; k <= 60; k++) {
            int l = j + k;
            if (l >= i) {
                break;
            }

            Vec3d vec3d2 = avec3d[l].add(0.0, 0.125, 0.0);
            Vec3d vec3d3 = vec3d2.subtract(vec3d);
            double d0 = k - 1;
            double d1 = k;
            if (!(predictionError(vec3d3, vec3d1, d0) < 0.0) && !(predictionError(vec3d3, vec3d1, d1) > 0.0)) {
                for (int i1 = 0; i1 < 24; i1++) {
                    double d2 = (d0 + d1) * 0.5;
                    if (predictionError(vec3d3, vec3d1, d2) > 0.0) {
                        d0 = d2;
                    } else {
                        d1 = d2;
                    }
                }

                double d4 = (d0 + d1) * 0.5;
                if (!(d4 < 0.001) && !this.raycastHits(vec3d, vec3d2)) {
                    Vec3d vec3d4 = vec3d3.subtract(vec3d1.multiply(d4));
                    double d3 = Math.sqrt(vec3d4.x * vec3d4.x + vec3d4.z * vec3d4.z);
                    float f = (float)Math.toDegrees(Math.atan2(vec3d4.z, vec3d4.x)) - 90.0F;
                    float f1 = (float)(-Math.toDegrees(Math.atan2(vec3d4.y, d3)));
                    float f2 = mc.player.getYaw();
                    return new float[]{f2 + MathHelper.wrapDegrees(f - f2), clampPitch(f1)};
                }
            }
        }

        return null;
    }

    @EventHandler
    public void onUseItem(ItemUseEvent itemuseevent) {
        if (mc.player != null && this.activation.is("Auto") && this.rotationTicks < 0 && !this.switchPending) {
            float f2 = mc.player.getPitch();
            if (mc.player.getStackInHand(Hand.MAIN_HAND).getItem() == Items.ENDER_PEARL && f2 < -25.0F && InventoryUtil.containsItemInHotbar(Items.WIND_CHARGE)
                )
             {
                if (this.predict.isEnabled()) {
                    float f3 = mc.player.getYaw();
                    int i = this.inRender ? -1 : 0;
                    float f = f2;
                    float f1 = f3;
                    this.startPrediction(f1, f, i);
                } else {
                    this.startPitchRotation(2, getAdjustedPitch(f2));
                }
            }
        }
    }

    @EventHandler
    public void onPreRender(PreRenderEvent prerenderevent) {
        this.inRender = true;
    }
}
