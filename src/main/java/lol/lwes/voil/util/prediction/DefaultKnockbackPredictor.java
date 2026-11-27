package lol.lwes.voil.util.prediction;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class DefaultKnockbackPredictor implements KnockbackPredictor {
    private double knockbackStrength = 0.25;
    private double verticalStrength = 0.4;
    private double resistanceFactor = 0.5;
    private double maxVerticalBoost = 0.4;
    private double gravity = 0.08;
    private double verticalDrag = 0.98;
    private double groundFriction = 0.55;
    private double airFriction = 0.85;
    private double airAcceleration = 0.02;
    private int maxTicks;
    private static final int DEFAULT_MAX_TICKS = 60;

    public DefaultKnockbackPredictor() {
        this.maxTicks = DEFAULT_MAX_TICKS;
    }

    private Vec3d simulateEndPosition(LivingEntity livingentity, Vec3d vec3d) {
        double d0 = livingentity.getY();
        double d1 = livingentity.getX();
        double d2 = d0;
        double d3 = livingentity.getZ();
        double d4 = vec3d.x;
        double d5 = vec3d.y;
        double d6 = vec3d.z;

        for (int i = 0; i < this.maxTicks; i++) {
            d1 += d4;
            d2 += d5;
            d3 += d6;
            double d7 = d2 > d0 + 0.001 ? this.airFriction : this.groundFriction;
            d4 *= d7;
            d6 *= d7;
            d5 -= this.gravity;
            d5 *= this.verticalDrag;
            if (d2 <= d0 && d5 < 0.0) {
                d2 = d0;
                d5 = 0.0;
                if (horizontalLength(d4, d6) < this.airAcceleration) {
                    break;
                }
            }
        }

        return new Vec3d(d1, d2, d3);
    }

    @Override
    public KnockbackResult simulate(KnockbackRequest knockbackrequest) {
        Vec3d vec3d = this.computeKnockback(knockbackrequest);
        double d0 = horizontalLength(vec3d.x, vec3d.z);
        return this.simulateWithCollision(knockbackrequest.target(), vec3d, d0);
    }

    public static KnockbackPredictor create() {
        return new DefaultKnockbackPredictor();
    }

    private static boolean hasCollision(World world, LivingEntity livingentity, Box box) {
        return world.getBlockCollisions(livingentity, box).iterator().hasNext();
    }

    private KnockbackResult simulateWithCollision(LivingEntity livingentity, Vec3d vec3d, double d0) {
        World world = livingentity.getEntityWorld();
        Box box = livingentity.getBoundingBox().contract(0.001, 0.0, 0.001).offset(0.0, 0.001, 0.0);
        double d1 = vec3d.x;
        double d2 = vec3d.y;
        double d3 = vec3d.z;
        boolean flag = false;

        for (int i = 0; i < this.maxTicks; i++) {
            Box box1 = box.offset(d1, 0.0, 0.0);
            if (hasCollision(world, livingentity, box1)) {
                flag = true;
                d1 = 0.0;
            } else {
                box = box1;
            }

            Box box2 = box.offset(0.0, 0.0, d3);
            if (hasCollision(world, livingentity, box2)) {
                flag = true;
                d3 = 0.0;
            } else {
                box = box2;
            }

            boolean flag1 = false;
            Box box3 = box.offset(0.0, d2, 0.0);
            if (hasCollision(world, livingentity, box3)) {
                if (d2 < 0.0) {
                    flag1 = true;
                }

                d2 = 0.0;
            } else {
                box = box3;
            }

            boolean flag2 = !flag1 && !hasCollision(world, livingentity, box.offset(0.0, -0.001, 0.0));
            double d4 = flag2 ? this.airFriction : this.groundFriction;
            d1 *= d4;
            d3 *= d4;
            d2 -= this.gravity;
            d2 *= this.verticalDrag;
            if (!flag2 && horizontalLength(d1, d3) < this.airAcceleration) {
                break;
            }
        }

        double d5 = (box.minX + box.maxX) / 2.0;
        double d6 = (box.minZ + box.maxZ) / 2.0;
        return new KnockbackResult(new Vec3d(d5, box.minY, d6), d0, flag);
    }

    private Vec3d computeKnockback(KnockbackRequest knockbackrequest) {
        LivingEntity livingentity = knockbackrequest.attacker();
        LivingEntity livingentity1 = knockbackrequest.target();
        double d0 = livingentity1.getAttributeValue(EntityAttributes.KNOCKBACK_RESISTANCE);
        Vec3d vec3d = new Vec3d(
            livingentity1.getX() - livingentity1.lastRenderX,
            livingentity1.getY() - livingentity1.lastRenderY,
            livingentity1.getZ() - livingentity1.lastRenderZ
        );
        boolean flag = livingentity1.isOnGround();
        double d1 = livingentity1.getX() - livingentity.getX();
        double d2 = livingentity1.getZ() - livingentity.getZ();
        double d3 = Math.sqrt(d1 * d1 + d2 * d2);
        if (d3 < 1.0E-4) {
            double d4 = Math.toRadians(knockbackrequest.yaw());
            d1 = -Math.sin(d4);
            d2 = Math.cos(d4);
            d3 = 1.0;
        }

        double d11 = d1 / d3;
        double d5 = d2 / d3;
        double d6 = this.knockbackStrength * (1.0 - d0);
        double d7 = this.verticalStrength * (1.0 - d0);
        double d8 = flag ? Math.min(this.maxVerticalBoost, vec3d.y / 2.0 + d7) : vec3d.y;
        Vec3d vec3d1 = new Vec3d(vec3d.x / 2.0 + d11 * d6, d8, vec3d.z / 2.0 + d5 * d6);
        int k = knockbackrequest.sprinting() ? 1 : 0;
        int j = knockbackrequest.ticks();
        int i = j + k;
        if (i > 0) {
            double d9 = i * this.resistanceFactor * (1.0 - d0);
            double d10 = Math.toRadians(knockbackrequest.yaw());
            vec3d1 = vec3d1.add(-Math.sin(d10) * d9, 0.0, Math.cos(d10) * d9);
        }

        return vec3d1;
    }

    @Override
    public KnockbackResult simulateFast(KnockbackRequest knockbackrequest) {
        Vec3d vec3d = this.computeKnockback(knockbackrequest);
        double d0 = horizontalLength(vec3d.x, vec3d.z);
        LivingEntity livingentity = knockbackrequest.target();
        Vec3d vec3d1 = this.simulateEndPosition(livingentity, vec3d);
        return new KnockbackResult(vec3d1, d0, false);
    }

    private static double horizontalLength(double d0, double d1) {
        return Math.sqrt(d0 * d0 + d1 * d1);
    }
}
