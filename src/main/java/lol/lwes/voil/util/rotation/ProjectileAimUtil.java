package lol.lwes.voil.util.rotation;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;

public final class ProjectileAimUtil {
    private static final int MAX_FLIGHT_TICKS = 300;
    private static final double MIN_PULL_PROGRESS = 0.1;

    private ProjectileAimUtil() {
    }

    public static Rotation aimThrowable(PlayerEntity shooter, Vec3d target, ItemStack stack) {
        if (shooter == null || target == null) {
            return null;
        }

        Ballistics ballistics = ballisticsFor(shooter, stack);
        if (ballistics == null) {
            return null;
        }

        return solve(shooter, target, ballistics);
    }

    public static Rotation aimBow(PlayerEntity shooter, PlayerEntity target, ItemStack stack, Vec3d velocity, int extraTicks) {
        if (shooter == null || target == null) {
            return null;
        }

        Ballistics ballistics = ballisticsFor(shooter, stack);
        if (ballistics == null) {
            return null;
        }

        Vec3d targetVelocity = velocity != null ? velocity : target.getVelocity();
        int prediction = Math.max(0, extraTicks);
        Vec3d aimPoint = bodyCenter(target);
        Rotation rotation = null;

        for (int i = 0; i < 4; i++) {
            Shot shot = simulateTowards(shooter, aimPoint, ballistics);
            if (shot == null) {
                return null;
            }

            int ticks = shot.ticks() + prediction;
            Vec3d predicted = bodyCenter(target).add(targetVelocity.x * ticks, 0.0, targetVelocity.z * ticks);
            if (!target.isOnGround()) {
                predicted = predicted.add(0.0, targetVelocity.y * ticks, 0.0);
            }

            rotation = solve(shooter, predicted, ballistics);
            if (rotation == null) {
                return null;
            }

            if (predicted.squaredDistanceTo(aimPoint) < 0.01) {
                break;
            }
            aimPoint = predicted;
        }

        return rotation;
    }

    private static Rotation solve(PlayerEntity shooter, Vec3d target, Ballistics ballistics) {
        Vec3d origin = launchOrigin(shooter);
        double dx = target.x - origin.x;
        double dz = target.z - origin.z;
        double distance = Math.sqrt(dx * dx + dz * dz);
        double yaw = Math.toDegrees(Math.atan2(dz, dx)) - 90.0;
        if (distance < 1.0E-3) {
            return new Rotation(shooter.getYaw(), target.y > origin.y ? -90.0 : 90.0);
        }

        double height = target.y - origin.y;
        double carry = shooterCarry(shooter, dx / distance, dz / distance);
        Double previousError = null;
        double previousPitch = 90.0;

        for (double pitch = 90.0; pitch >= -90.0; pitch -= 1.0) {
            Shot shot = simulate(pitch, distance, height, carry, ballistics);
            double error = shot == null ? Double.NEGATIVE_INFINITY : shot.y() - height;
            if (previousError != null && previousError < 0.0 && error >= 0.0) {
                double low = previousPitch;
                double high = pitch;
                for (int i = 0; i < 24; i++) {
                    double mid = (low + high) * 0.5;
                    Shot midShot = simulate(mid, distance, height, carry, ballistics);
                    double midError = midShot == null ? Double.NEGATIVE_INFINITY : midShot.y() - height;
                    if (midError >= 0.0) {
                        high = mid;
                    } else {
                        low = mid;
                    }
                }
                return new Rotation(yaw, high);
            }

            previousError = error;
            previousPitch = pitch;
        }

        return null;
    }

    private static Shot simulateTowards(PlayerEntity shooter, Vec3d target, Ballistics ballistics) {
        Rotation rotation = solve(shooter, target, ballistics);
        if (rotation == null) {
            return null;
        }

        Vec3d origin = launchOrigin(shooter);
        double dx = target.x - origin.x;
        double dz = target.z - origin.z;
        double distance = Math.sqrt(dx * dx + dz * dz);
        double carry = distance < 1.0E-3 ? 0.0 : shooterCarry(shooter, dx / distance, dz / distance);
        return simulate(rotation.getPitch(), distance, target.y - origin.y, carry, ballistics);
    }

    private static Shot simulate(double pitch, double distance, double height, double carry, Ballistics ballistics) {
        double radians = Math.toRadians(pitch);
        double horizontalVelocity = Math.cos(radians) * ballistics.speed() + carry;
        double verticalVelocity = -Math.sin(radians) * ballistics.speed();
        if (horizontalVelocity <= 1.0E-4) {
            return null;
        }

        double travelled = 0.0;
        double y = 0.0;

        for (int tick = 1; tick <= MAX_FLIGHT_TICKS; tick++) {
            double nextTravelled = travelled + horizontalVelocity;
            double nextY = y + verticalVelocity;
            if (nextTravelled >= distance) {
                double fraction = (distance - travelled) / (nextTravelled - travelled);
                return new Shot(y + (nextY - y) * fraction, tick);
            }

            travelled = nextTravelled;
            y = nextY;
            horizontalVelocity *= ballistics.drag();
            verticalVelocity = verticalVelocity * ballistics.drag() - ballistics.gravity();
            if (y < height - 64.0 || horizontalVelocity <= 1.0E-4) {
                return null;
            }
        }

        return null;
    }

    private static Ballistics ballisticsFor(PlayerEntity shooter, ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }

        if (stack.getItem() instanceof BowItem) {
            float pull = shooter.isUsingItem() ? BowItem.getPullProgress(shooter.getItemUseTime()) : 1.0F;
            if (pull < MIN_PULL_PROGRESS) {
                return null;
            }
            return new Ballistics(pull * 3.0, 0.05, 0.99);
        }

        if (stack.getItem() instanceof CrossbowItem) {
            return new Ballistics(3.15, 0.05, 0.99);
        }

        if (stack.isOf(Items.TRIDENT)) {
            return new Ballistics(2.5, 0.05, 0.99);
        }

        if (stack.isOf(Items.EXPERIENCE_BOTTLE)) {
            return new Ballistics(0.7, 0.07, 0.99);
        }

        if (stack.isOf(Items.SPLASH_POTION) || stack.isOf(Items.LINGERING_POTION)) {
            return new Ballistics(0.5, 0.05, 0.99);
        }

        return new Ballistics(1.5, 0.03, 0.99);
    }

    private static double shooterCarry(PlayerEntity shooter, double dirX, double dirZ) {
        Vec3d velocity = shooter.getVelocity();
        return velocity.x * dirX + velocity.z * dirZ;
    }

    private static Vec3d launchOrigin(PlayerEntity shooter) {
        return shooter.getEyePos().subtract(0.0, 0.1, 0.0);
    }

    private static Vec3d bodyCenter(Entity entity) {
        return new Vec3d(entity.getX(), entity.getY() + entity.getHeight() * 0.5, entity.getZ());
    }

    private record Ballistics(double speed, double gravity, double drag) {
    }

    private record Shot(double y, int ticks) {
    }
}
