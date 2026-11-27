package lol.lwes.voil.module.render;

import dev.prestigeclient.renderlib.impl.api.RenderAPI;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.ColorSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.SystemUtil;
import lol.lwes.voil.util.render.ColorUtil;
import lol.lwes.voil.util.render.Render3DUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.EggItem;
import net.minecraft.item.EnderPearlItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SnowballItem;
import net.minecraft.item.WindChargeItem;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import net.minecraft.world.RaycastContext;
import org.joml.Matrix4f;

public class Trajectories extends Module {
    BooleanSetting simulateForEveryone;
    ColorSetting color;
    BooleanSetting drawEntityBox;

    public Trajectories() {
        super("Trajectories", "Draws where projectiles will go", Category.Render);
        this.simulateForEveryone = new BooleanSetting("Simulate For Everyone", false, "Simulate trajectories for all players in the world");
        Color color1 = new Color(255, 255, 255);
        String s2 = "Color of the trajectory lines";
        Color colorx = color1;
        this.color = new ColorSetting("Color", colorx, s2);
        this.drawEntityBox = new BooleanSetting("Draw Entity Box", true, "Draw box at the entity hit position");
        Setting[] asetting = new Setting[]{this.color, this.simulateForEveryone, this.drawEntityBox};
        this.addSettings(asetting);
    }

    public static boolean isThrowable(Item item) {
        return item instanceof BowItem
            || item instanceof CrossbowItem
            || item instanceof SnowballItem
            || item instanceof EggItem
            || item instanceof EnderPearlItem
            || item instanceof WindChargeItem;
    }

    public static List calcTrajectoryPoints(
        LivingEntity livingentity, ItemStack itemstack, float f, float f1, boolean[] aboolean, LivingEntity[] alivingentity, Direction[] adirection
    ) {
        ArrayList arraylist = new ArrayList();
        double d0 = SystemUtil.getTickDelta();
        double d1 = MathHelper.lerp(d0, livingentity.lastX, livingentity.getX());
        double d2 = MathHelper.lerp(d0, livingentity.lastY, livingentity.getY()) + livingentity.getEyeHeight(livingentity.getPose()) - 0.1;
        double d3 = MathHelper.lerp(d0, livingentity.lastZ, livingentity.getZ());
        double d4 = -MathHelper.sin(f * (float) (Math.PI / 180.0)) * MathHelper.cos(f1 * (float) (Math.PI / 180.0));
        double d5 = -MathHelper.sin(f1 * (float) (Math.PI / 180.0));
        double d6 = MathHelper.cos(f * (float) (Math.PI / 180.0)) * MathHelper.cos(f1 * (float) (Math.PI / 180.0));
        float f2;
        float f3;
        float f4;
        if (itemstack.getItem() instanceof BowItem) {
            float f5;
            if (livingentity == mc.player && mc.player.isUsingItem() && mc.player.getActiveItem().getItem() instanceof BowItem) {
                f5 = mc.player.getItemUseTime() / 20.0F;
            } else {
                f5 = 1.0F;
            }

            f5 = Math.min((f5 * f5 + 2.0F * f5) / 3.0F, 1.0F);
            f2 = f5 * 3.0F;
            f3 = 0.05F;
            f4 = 0.99F;
        } else if (itemstack.getItem() instanceof CrossbowItem) {
            f2 = 3.15F;
            f3 = 0.05F;
            f4 = 0.99F;
        } else if (!(itemstack.getItem() instanceof SnowballItem)
            && !(itemstack.getItem() instanceof EggItem)
            && !(itemstack.getItem() instanceof EnderPearlItem)) {
            if (!(itemstack.getItem() instanceof WindChargeItem)) {
                return null;
            }

            f2 = 2.0F;
            f3 = 0.0F;
            f4 = 0.99F;
        } else {
            f2 = 1.5F;
            f3 = 0.03F;
            f4 = 0.99F;
        }

        float f6 = MathHelper.sqrt((float)(d4 * d4 + d5 * d5 + d6 * d6));
        d4 = d4 / f6 * f2;
        d5 = d5 / f6 * f2;
        d6 = d6 / f6 * f2;
        arraylist.add(new Vec3d(d1, d2, d3));

        for (int i = 0; i < 300; i++) {
            Vec3d vec3d = new Vec3d(d1, d2, d3);
            d1 += d4;
            d2 += d5;
            d3 += d6;
            d4 *= f4;
            d5 = d5 * f4 - f3;
            d6 *= f4;
            Vec3d vec3d1 = new Vec3d(d1, d2, d3);
            arraylist.add(vec3d1);
            Box box = new Box(vec3d, vec3d1).expand(0.01);
            EntityHitResult entityhitresult = ProjectileUtil.raycast(
                livingentity,
                vec3d,
                vec3d1,
                box,
                entity -> !entity.isSpectator() && entity instanceof LivingEntity && entity.isAttackable() && entity != livingentity,
                vec3d.squaredDistanceTo(vec3d1)
            );
            if (entityhitresult != null) {
                aboolean[0] = true;
                if (entityhitresult.getEntity() instanceof LivingEntity) {
                    alivingentity[0] = (LivingEntity)entityhitresult.getEntity();
                }

                int j = arraylist.size();
                arraylist.set(j - 1, entityhitresult.getPos());
                break;
            }

            BlockHitResult blockhitresult = mc.world.raycast(new RaycastContext(vec3d, vec3d1, ShapeType.OUTLINE, FluidHandling.NONE, livingentity));
            if (blockhitresult != null && blockhitresult.getType() == Type.BLOCK) {
                adirection[0] = blockhitresult.getSide();
                arraylist.add(blockhitresult.getPos());
                break;
            }

            if (d2 <= -65.0) {
                break;
            }
        }

        return arraylist;
    }

    @Override
    public void onWorldRender(RenderAPI.RenderContext rendercontext) {
        if (mc.player != null && mc.world != null) {
            float f = SystemUtil.getTickDelta();
            ArrayList<LivingEntity> arraylist = new ArrayList<>();
            if (this.simulateForEveryone.isEnabled()) {
                mc.world
                    .getEntitiesByClass(LivingEntity.class, mc.player.getBoundingBox().expand(128.0), livingentity2 -> livingentity2 instanceof PlayerEntity)
                    .forEach(arraylist::add);
            } else {
                arraylist.add(mc.player);
            }

            for (LivingEntity livingentity : arraylist) {
                ItemStack itemstack = livingentity.getMainHandStack();
                Item item = itemstack.getItem();
                if (isThrowable(item)) {
                    float f1 = MathHelper.lerpAngleDegrees(f, livingentity.lastYaw, livingentity.getYaw());
                    float f2 = MathHelper.lerp(f, livingentity.lastPitch, livingentity.getPitch());
                    Vec3d vec3d = new Vec3d(
                        MathHelper.lerp(f, livingentity.lastX, livingentity.getX()),
                        MathHelper.lerp(f, livingentity.lastY, livingentity.getY()) + livingentity.getEyeHeight(livingentity.getPose()) - 0.1,
                        MathHelper.lerp(f, livingentity.lastZ, livingentity.getZ())
                    );
                    boolean[] aboolean = new boolean[1];
                    LivingEntity[] alivingentity = new LivingEntity[1];
                    Direction[] adirection = new Direction[1];
                    List list = calcTrajectoryPoints(livingentity, itemstack, f1, f2, aboolean, alivingentity, adirection);
                    if (list != null && !list.isEmpty()) {
                        Color colorx = aboolean[0] ? Color.GREEN : this.color.getColor();
                        if (livingentity == mc.player) {
                            int j = list.size();
                            Vec3d vec3d2 = (Vec3d)list.get(j - 1);
                            Render3DUtil.drawLine(vec3d, vec3d2, colorx);
                        } else {
                            for (int i = 0; i < list.size() - 1; i++) {
                                Render3DUtil.drawLine((Vec3d)list.get(i), (Vec3d)list.get(i + 1), colorx);
                            }
                        }

                        Vec3d vec3d3 = (Vec3d)list.get(list.size() - 1);
                        if (this.drawEntityBox.isEnabled()) {
                            if (aboolean[0] && alivingentity[0] != null) {
                                LivingEntity livingentity1 = alivingentity[0];
                                double d2 = MathHelper.lerp(f, livingentity1.lastX, livingentity1.getX());
                                double d0 = MathHelper.lerp(f, livingentity1.lastY, livingentity1.getY());
                                double d1 = MathHelper.lerp(f, livingentity1.lastZ, livingentity1.getZ());
                                Box box = livingentity1.getBoundingBox()
                                    .offset(d2 - livingentity1.getX(), d0 - livingentity1.getY(), d1 - livingentity1.getZ());
                                Render3DUtil.drawBoxFilled(new Matrix4f(), box, ColorUtil.withAlpha(colorx, 125));
                            } else {
                                Direction direction = adirection[0] != null ? adirection[0] : Direction.UP;
                                Vec3d vec3d1 = Vec3d.of(direction.getVector());
                                BlockPos blockpos = BlockPos.ofFloored(vec3d3.subtract(vec3d1.multiply(0.5)));
                                Render3DUtil.drawBlockFace(new Matrix4f(), blockpos, direction, ColorUtil.withAlpha(colorx, 125), colorx);
                            }
                        }
                    }
                }
            }
        }
    }
}
