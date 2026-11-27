package lol.lwes.voil.module.combat;

import java.util.ArrayList;
import java.util.List;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.NumberSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.Stopwatch;
import lol.lwes.voil.util.inventory.InventoryUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import net.minecraft.world.RaycastContext;

public class AutoCrossbow extends Module {
    BooleanSetting autoSwitch = new BooleanSetting("Auto Switch", false, "Automatically switches to crossbow when not in crossbow");
    BooleanSetting switchBack = new BooleanSetting("Switch Back", false, "Switches back to old slot after shooting");
    NumberSetting switchDelay = new NumberSetting("Switch Delay", 0.0, 800.0, 300.0, 1.0, "Delay to switch to crossbow");
    NumberSetting raycastDistance = new NumberSetting("Raycast Distance", 1.0, 50.0, 20.0, 1.0, "Maximum distance for raycast");
    Stopwatch timer = new Stopwatch();
    public int previousSlot = -1;

    public AutoCrossbow() {
        super("AutoCrossbow", "Automatically shoots crossbow", Category.Combat);
        Setting[] asetting = new Setting[]{this.autoSwitch, this.switchBack, this.switchDelay, this.raycastDistance};
        this.addSettings(asetting);
    }

    private void aimAt(Entity entity) {
        Vec3d vec3d = mc.player.getEyePos();
        Vec3d vec3d1 = entity.getEyePos();
        double d0 = vec3d1.x - vec3d.x;
        double d1 = vec3d1.y - vec3d.y;
        double d2 = vec3d1.z - vec3d.z;
        double d3 = Math.sqrt(d0 * d0 + d2 * d2);
        float f = (float)(Math.toDegrees(Math.atan2(d2, d0)) - 90.0);
        float f1 = (float)(-Math.toDegrees(Math.atan2(d1, d3)));
        mc.player.setYaw(f);
        mc.player.setPitch(f1);
    }

    private void useCrossbow() {
        if (mc.player.getMainHandStack().getItem() instanceof CrossbowItem) {
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        } else if (mc.player.getOffHandStack().getItem() instanceof CrossbowItem) {
            mc.interactionManager.interactItem(mc.player, Hand.OFF_HAND);
        }
    }

    @Override
    public void onTick() {
        if (mc.player != null && mc.world != null) {
            if (!(mc.player.getMainHandStack().getItem() instanceof CrossbowItem)
                && !(mc.player.getOffHandStack().getItem() instanceof CrossbowItem)
                && this.autoSwitch.isEnabled()) {
                this.selectCrossbow();
            } else {
                double d0 = this.raycastDistance.getValue();
                PlayerEntity playerentity = this.findTargetPlayer(180, d0);
                if (playerentity != null) {
                    this.aimAt(playerentity);
                    if (this.timer.hasElapsed(this.switchDelay.getValue())) {
                        this.useCrossbow();
                        if (this.switchBack.isEnabled() && this.previousSlot != -1) {
                            InventoryUtil.hasItemInHotbar(mc.player.getInventory().getStack(this.previousSlot).getItem());
                            this.previousSlot = -1;
                        }

                        this.timer.reset();
                    }
                }
            }
        }
    }

    private PlayerEntity findTargetPlayer(int i, double d0) {
        for (HitResult hitresult : castRays(i, d0)) {
            if (hitresult.getType() == Type.ENTITY) {
                Entity entity = ((EntityHitResult)hitresult).getEntity();
                if (entity instanceof PlayerEntity && entity != mc.player) {
                    return (PlayerEntity)entity;
                }
            }
        }

        return null;
    }

    private void selectCrossbow() {
        for (int i = 0; i < 9; i++) {
            ItemStack itemstack = mc.player.getInventory().getStack(i);
            if (itemstack.getItem() instanceof CrossbowItem) {
                this.previousSlot = mc.player.getInventory().getSelectedSlot();
                mc.player.getInventory().setSelectedSlot(i);
                break;
            }
        }
    }

    public static List<HitResult> castRays(int i, double d0) {
        ArrayList<HitResult> arraylist = new ArrayList<>();
        if (mc.player != null && mc.world != null) {
            Vec3d vec3d = mc.player.getCameraPosVec(1.0F);
            double d1 = 360.0 / i;

            for (double d2 = 0.0; d2 < i; d2 += d1) {
                double d3 = Math.toRadians(d2 + mc.player.getYaw());
                Vec3d vec3d1 = new Vec3d(Math.cos(d3), 0.0, Math.sin(d3)).normalize();
                Vec3d vec3d2 = vec3d.add(vec3d1.multiply(d0));
                BlockHitResult blockhitresult = mc.world.raycast(new RaycastContext(vec3d, vec3d2, ShapeType.OUTLINE, FluidHandling.NONE, mc.player));
                arraylist.add(blockhitresult);
            }

            return arraylist;
        } else {
            return arraylist;
        }
    }
}
