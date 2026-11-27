package lol.lwes.voil.util.prediction;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class DefaultMotionPredictor implements MotionPredictor {
    private double gravity = 0.08;
    private double verticalDrag = 0.98;
    private double horizontalDrag = 0.91;
    private double groundFriction = 0.55;
    private double airAcceleration = 0.02;
    private int maxTicks = 200;

    private static boolean hasCollision(World world, LivingEntity livingentity, Box box) {
        return world.getBlockCollisions(livingentity, box).iterator().hasNext();
    }

    public static MotionPredictor create() {
        return new DefaultMotionPredictor();
    }

    private Box getCollisionBox(World world, LivingEntity livingentity, Box box, double d0) {
        for (int i = 0; i < 8 && d0 < -1.0E-4; i++) {
            d0 /= 2.0;
            Box box1 = box.offset(0.0, d0, 0.0);
            if (!hasCollision(world, livingentity, box1)) {
                box = box1;
            }
        }

        return box;
    }

    private static double horizontalLength(double d0, double d1) {
        return Math.sqrt(d0 * d0 + d1 * d1);
    }

    @Override
    public MotionResult predict(MotionRequest motionrequest) {
        LivingEntity livingentity = motionrequest.getEntity();
        World world = livingentity.getEntityWorld();
        int i = Math.min(motionrequest.getTicks(), this.maxTicks);
        Vec3d vec3d = livingentity.getEntityPos();
        Vec3d vec3d1 = motionrequest.getVelocity();
        double d0;
        double d1;
        double d2;
        if (vec3d1 != null) {
            d0 = vec3d1.x;
            d1 = vec3d1.y;
            d2 = vec3d1.z;
        } else {
            d0 = livingentity.getX() - livingentity.lastRenderX;
            d1 = livingentity.getY() - livingentity.lastRenderY;
            d2 = livingentity.getZ() - livingentity.lastRenderZ;
        }

        Box box = livingentity.getBoundingBox().contract(0.001, 0.0, 0.001).offset(0.0, 0.001, 0.0);
        double d3 = vec3d.y;
        Vec3d vec3d2 = vec3d;
        int j = 0;
        int k = -1;
        boolean flag = false;
        boolean flag1 = false;
        boolean flag2 = false;

        for (int l = 1; l <= i; l++) {
            if (!flag && d1 <= 0.0) {
                j = l - 1;
                flag = true;
            }

            Box box1 = box.offset(d0, 0.0, 0.0);
            if (hasCollision(world, livingentity, box1)) {
                flag1 = true;
                d0 = 0.0;
            } else {
                box = box1;
            }

            Box box2 = box.offset(0.0, 0.0, d2);
            if (hasCollision(world, livingentity, box2)) {
                flag1 = true;
                d2 = 0.0;
            } else {
                box = box2;
            }

            boolean flag3 = false;
            Box box3 = box.offset(0.0, d1, 0.0);
            if (hasCollision(world, livingentity, box3)) {
                if (d1 < 0.0) {
                    flag3 = true;
                    box = this.getCollisionBox(world, livingentity, box, d1);
                }

                d1 = 0.0;
            } else {
                box = box3;
            }

            double d4 = (box.minX + box.maxX) / 2.0;
            double d5 = (box.minZ + box.maxZ) / 2.0;
            if (box.minY > d3) {
                d3 = box.minY;
                vec3d2 = new Vec3d(d4, box.minY, d5);
            }

            boolean flag4 = !flag3 && !hasCollision(world, livingentity, box.offset(0.0, -0.001, 0.0));
            double d6 = flag4 ? this.horizontalDrag : this.groundFriction;
            d0 *= d6;
            d2 *= d6;
            d1 -= this.gravity;
            d1 *= this.verticalDrag;
            if (flag3 && k < 0) {
                k = l;
                flag2 = true;
            }

            if (!flag4 && horizontalLength(d0, d2) < this.airAcceleration) {
                if (k < 0) {
                    k = l;
                    flag2 = true;
                }
                break;
            }
        }

        if (!flag) {
            j = 0;
        }

        double d7 = (box.minX + box.maxX) / 2.0;
        double d8 = (box.minZ + box.maxZ) / 2.0;
        Vec3d vec3d3 = new Vec3d(d7, box.minY, d8);
        int i1 = k >= 0 ? k - j : -1;
        return new MotionResult(vec3d, vec3d3, vec3d2, d3 - vec3d.y, j, k, i1, flag1, flag2);
    }
}
