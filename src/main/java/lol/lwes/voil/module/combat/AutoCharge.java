package lol.lwes.voil.module.combat;

import lol.lwes.voil.VoilClient;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.ModeSetting;
import lol.lwes.voil.setting.NumberSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.inventory.InventoryUtil;
import lol.lwes.voil.util.rotation.Rotation;
import lol.lwes.voil.util.rotation.RotationManager;
import lol.lwes.voil.util.rotation.RotationPriority;
import lol.lwes.voil.util.target.TargetManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class AutoCharge extends Module {
    private final ModeSetting mode = new ModeSetting("Mode", "Looking", "Targeting mode", new String[]{"Looking", "Silent"});
    private final NumberSetting minSpeed = new NumberSetting("Min Speed", 0.0, 25.0, 8.0, 0.1, "Minimum horizontal speed in blocks/sec");
    private final NumberSetting range = new NumberSetting("Range", 1.0, 32.0, 6.0, 1.0, "Max distance to target");
    private final BooleanSetting autoSwap = new BooleanSetting("Auto Swap", true, "Swap to spear slot automatically");
    private final BooleanSetting swapBack = new BooleanSetting("Swap Back", true, "Swap back to the previous slot when target is lost");
    private int previousSlot = -1;
    private boolean charging = false;
    private Vec3d lastPosition = null;
    private double speed = 0.0;

    public AutoCharge() {
        super("AutoCharge", "Charges spear when moving fast with a player in reach", Category.Combat);
        Setting[] asetting = new Setting[]{this.mode, this.minSpeed, this.range, this.autoSwap, this.swapBack};
        this.addSettings(asetting);
    }

    private void stopCharging() {
        VoilClient.getRotationManager().removeRequest(this);
        if (this.charging) {
            mc.options.useKey.setPressed(false);
            if (this.swapBack.isEnabled() && this.previousSlot != -1 && mc.player != null) {
                mc.player.getInventory().setSelectedSlot(this.previousSlot);
            }

            this.previousSlot = -1;
            this.charging = false;
        }
    }

    @Override
    public void onTick() {
        if (mc.player != null && mc.world != null) {
            this.updateSpeed();
            if (mc.player.isTouchingWater()) {
                this.stopCharging();
            } else if (this.speed < this.minSpeed.getValue()) {
                this.stopCharging();
            } else {
                int i = InventoryUtil.findHotbarSlotByTag(ItemTags.SPEARS);
                if (i == -1) {
                    this.stopCharging();
                } else {
                    PlayerEntity playerentity1;
                    if (this.mode.is("Silent")) {
                        double d0 = this.range.getValue();
                        playerentity1 = this.findNearbyPlayer(d0);
                    } else {
                        double d1 = this.range.getValue();
                        playerentity1 = this.findLookingTarget(d1);
                    }

                    PlayerEntity playerentity = playerentity1;
                    if (playerentity == null) {
                        this.stopCharging();
                    } else {
                        int j = mc.player.getInventory().getSelectedSlot();
                        if (j != i) {
                            if (!this.autoSwap.isEnabled()) {
                                this.stopCharging();
                                return;
                            }

                            if (!this.charging) {
                                this.previousSlot = j;
                            }

                            InventoryUtil.selectSlot(i);
                        }

                        if (this.mode.is("Silent")) {
                            Vec3d vec3d1 = mc.player.getEyePos();
                            Vec3d vec3d = playerentity.getEyePos();
                            Rotation rotation = Rotation.fromPositions(vec3d1, vec3d);
                            RotationManager rotationmanager = VoilClient.getRotationManager();
                            float f2 = rotation.getYawFloat();
                            float f = rotation.getPitchFloat();
                            float f1 = f2;
                            AutoCharge autocharge1 = this;
                            rotationmanager.requestAnglesPriority(autocharge1, f1, f, RotationPriority.HIGH);
                        }

                        if (!this.charging) {
                            mc.options.useKey.setPressed(true);
                            this.charging = true;
                        }
                    }
                }
            }
        } else {
            this.stopCharging();
            this.lastPosition = null;
        }
    }

    @Override
    public void onDisable() {
        this.stopCharging();
        this.lastPosition = null;
        this.speed = 0.0;
    }

    @Override
    public void onEnable() {
        this.lastPosition = null;
        this.speed = 0.0;
    }

    private void updateSpeed() {
        Vec3d vec3d = mc.player.getEntityPos();
        if (this.lastPosition != null) {
            double d0 = vec3d.x - this.lastPosition.x;
            double d1 = vec3d.z - this.lastPosition.z;
            this.speed = Math.sqrt(d0 * d0 + d1 * d1) * 20.0;
        } else {
            this.speed = 0.0;
        }

        this.lastPosition = vec3d;
    }

    private PlayerEntity findNearbyPlayer(double d0) {
        if (mc.player == null) {
            return null;
        }

        double d1 = d0 * d0;

        for (Entity entity : TargetManager.getTargetListDefault()) {
            if (entity instanceof PlayerEntity playerentity && !(mc.player.squaredDistanceTo(playerentity) > d1)) {
                return playerentity;
            }
        }

        return null;
    }

    private PlayerEntity findLookingTarget(double d0) {
        if (mc.player == null) {
            return null;
        } else {
            Vec3d vec3d = mc.player.getEyePos();
            Vec3d vec3d1 = mc.player.getRotationVec(1.0F);
            Vec3d vec3d2 = vec3d.add(vec3d1.multiply(d0));
            Box box = mc.player.getBoundingBox().stretch(vec3d1.multiply(d0)).expand(1.0);
            EntityHitResult entityhitresult = ProjectileUtil.raycast(
                mc.player, vec3d, vec3d2, box, entity -> entity instanceof PlayerEntity && TargetManager.isTarget(entity, false), d0 * d0
            );
            if (entityhitresult == null) {
                return null;
            } else {
                return entityhitresult.getEntity() instanceof PlayerEntity playerentity ? playerentity : null;
            }
        }
    }
}
