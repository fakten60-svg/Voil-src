package lol.lwes.voil.module.combat;

import java.util.ArrayList;
import java.util.Optional;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.event.DoAttackEvent;
import lol.lwes.voil.event.EventHandler;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.ModeSetting;
import lol.lwes.voil.setting.NumberSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.AimMathUtil;
import lol.lwes.voil.util.RaycastUtil;
import lol.lwes.voil.util.rotation.Rotation;
import lol.lwes.voil.util.rotation.RotationPriority;
import lol.lwes.voil.util.target.TargetManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class Hitboxes extends Module {
    public static ModeSetting expandMode = new ModeSetting("Expand Mode", "Normal", "How to expand hitbox", new String[]{"Rotations", "Static", "Normal"});
    public NumberSetting size = new NumberSetting("Size", 0.1, 3.0, 0.3, 0.1, "Size of hitboxes");
    public static BooleanSetting renderExpanded = new BooleanSetting("Render Expanded", true, "RenderS expanded hitboxe size");
    private PlayerEntity target = null;

    public Hitboxes() {
        super("Hitboxes", "Expands enemy hitboxes", Category.Combat);
        Setting[] asetting = new Setting[]{expandMode, this.size, renderExpanded};
        this.addSettings(asetting);
        Setting.dependSetting(this.size, "Normal", expandMode);
        Setting.dependAny(renderExpanded, expandMode, new String[]{"Normal", "Static"});
    }

    @Override
    public void onTick() {
        if (this.target != null && mc.player != null && mc.interactionManager != null) {
            Rotation rotation = VoilClient.getRotationManager().getTargetRotation();
            HitResult hitresult = RaycastUtil.getHitResultCustomRange(
                mc.player, entity -> entity == this.target, rotation.getYawFloat(), rotation.getPitchFloat(), mc.player.getEntityInteractionRange(), false, 0.1
            );
            if (hitresult instanceof EntityHitResult && ((EntityHitResult)hitresult).getEntity() == this.target) {
                mc.interactionManager.attackEntity(mc.player, this.target);
                mc.player.swingHand(Hand.MAIN_HAND);
            }

            VoilClient.getRotationManager().reset();
            this.target = null;
        }
    }

    @EventHandler
    public void onAttack(DoAttackEvent doattackevent) {
        if (mc.world != null && mc.player != null) {
            if (expandMode.is("Rotations")) {
                ArrayList<PlayerEntity> arraylist = TargetManager.getMappedTargets(false, entity -> {
                    Box box = entity.getBoundingBox();
                    if (AimMathUtil.distanceToBoxFromCamera(box) > 3.0) {
                        return Optional.empty();
                    } else {
                        return entity instanceof PlayerEntity playerentity1 ? Optional.of(playerentity1) : Optional.empty();
                    }
                });
                PlayerEntity playerentity;
                if (arraylist.isEmpty()) {
                    playerentity = null;
                } else {
                    playerentity = (PlayerEntity)arraylist.get(0);
                }

                if (playerentity != null) {
                    doattackevent.cancel();
                    Vec3d vec3d = AimMathUtil.closestPointOnBoxFromCamera(playerentity.getBoundingBox());
                    Rotation rotation = Rotation.lookAtPoint(vec3d);
                    HitResult hitresult = RaycastUtil.getHitResultCustomRange(
                        mc.player,
                        entity -> entity == playerentity,
                        rotation.getYawFloat(),
                        rotation.getPitchFloat(),
                        mc.player.getEntityInteractionRange(),
                        false,
                        0.1
                    );
                    if (hitresult instanceof EntityHitResult && ((EntityHitResult)hitresult).getEntity() == playerentity) {
                        VoilClient.getRotationManager().requestFromCaller(rotation, RotationPriority.MEDIUM);
                        this.target = playerentity;
                    }
                }
            }
        }
    }
}
