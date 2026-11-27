package lol.lwes.voil.module.combat;

import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.NumberSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.AimMathUtil;
import lol.lwes.voil.util.SystemUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class BowTrigger extends Module {
    private final NumberSetting minPower = new NumberSetting("Min Power", 0.2F, 1.0, 0.5, 0.1F, "Delay between bow shots in ticks");

    public BowTrigger() {
        super("BowTrigger", "Helps you shoot bow easier", Category.Combat);
        Setting[] asetting = new Setting[]{this.minPower};
        this.addSettings(asetting);
    }

    @Override
    public void onTick() {
        if (mc.player != null && mc.world != null) {
            if (mc.player.isUsingItem()) {
                if (mc.player.getActiveItem().getItem() instanceof BowItem) {
                    if (mc.player.getItemUseTime() / 20.0F >= this.minPower.getFloat() && this.willHit(mc.player.getYaw())) {
                        mc.interactionManager.stopUsingItem(mc.player);
                    }
                }
            }
        }
    }

    private boolean willHit(float f) {
        double d0 = AimMathUtil.interpolate(mc.player.lastX, mc.player.getX(), SystemUtil.getTickDelta());
        double d1 = AimMathUtil.interpolate(mc.player.lastY, mc.player.getY(), SystemUtil.getTickDelta());
        double d2 = AimMathUtil.interpolate(mc.player.lastZ, mc.player.getZ(), SystemUtil.getTickDelta());
        d1 += mc.player.getEyeHeight(mc.player.getPose()) - 0.1;
        double d3 = -MathHelper.sin(f * 0.0174533F) * MathHelper.cos(mc.player.getPitch() * 0.0174533F);
        double d4 = -MathHelper.sin(mc.player.getPitch() * 0.0174533F);
        double d5 = MathHelper.cos(f * 0.0174533F) * MathHelper.cos(mc.player.getPitch() * 0.0174533F);
        float f1 = mc.player.getItemUseTime() / 20.0F;
        f1 = (f1 * f1 + f1 * 2.0F) / 3.0F;
        if (f1 > 1.0F) {
            f1 = 1.0F;
        }

        float f2 = f1 * 3.0F;
        double d6 = Math.sqrt(d3 * d3 + d4 * d4 + d5 * d5);
        d3 = d3 / d6 * f2;
        d4 = d4 / d6 * f2;
        d5 = d5 / d6 * f2;
        if (!mc.player.isOnGround()) {
            d4 += mc.player.getVelocity().getY();
        }

        new Vec3d(d0, d1, d2);

        for (int i = 0; i < 300; i++) {
            d0 += d3;
            d1 += d4;
            d2 += d5;
            d3 *= 0.99;
            d4 *= 0.99;
            d5 *= 0.99;
            d4 -= 0.05;
            Vec3d vec3d = new Vec3d(d0, d1, d2);

            for (Entity entity : mc.world.getEntities()) {
                if (entity instanceof PlayerEntity && entity != mc.player && entity.getBoundingBox().expand(0.3).contains(vec3d)) {
                    PlayerEntity playerentity = (PlayerEntity)entity;
                    if (playerentity.isUsingItem() && playerentity.getActiveItem().getItem() == Items.SHIELD) {
                        return false;
                    }

                    return true;
                }
            }

            if (d1 <= -64.0) {
                break;
            }
        }

        return false;
    }
}
