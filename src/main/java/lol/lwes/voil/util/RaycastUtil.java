package lol.lwes.voil.util;

import java.util.Optional;
import java.util.function.Predicate;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.event.RaycastEvent;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

public class RaycastUtil {
    public static HitResult raycast(double d0, Predicate<Entity> predicate, boolean flag, FluidHandling fluidhandling, float f, boolean flag1) {
        if (VoilClient.mc.player != null && VoilClient.mc.world != null) {
            RaycastEvent raycastevent = createRotationState(VoilClient.mc.player);
            boolean flag2 = flag1 || raycastevent.hasFilter();
            float f1 = SystemUtil.getTickDelta();
            Vec3d vec3d = VoilClient.mc.player.getCameraPosVec(f1);
            Vec3d vec3d1 = VoilClient.mc.player.getRotationVec(f1).normalize();
            Vec3d vec3d2 = vec3d.add(vec3d1.multiply(d0));
            double d1 = d0 * d0;
            Box box = VoilClient.mc.player.getBoundingBox().stretch(vec3d1.multiply(d0)).expand(0.15);
            double d2 = MathHelper.cos((float)Math.toRadians(Math.max(f, 0.5F)));
            Predicate<Entity> predicate1 = entity -> predicate.test(entity)
                && raycastevent.test(entity)
                && entity.isAlive()
                && !entity.isSpectator()
                && entity.canHit()
                && (!flag || entity != VoilClient.mc.player);
            if (flag2) {
                EntityHitResult entityhitresult1 = ProjectileUtil.raycast(VoilClient.mc.player, vec3d, vec3d2, box, predicate1, d1);
                if (entityhitresult1 != null && entityhitresult1.getPos() != null) {
                    if (vec3d.squaredDistanceTo(entityhitresult1.getPos()) > d1) {
                        return null;
                    }

                    Vec3d vec3d3 = entityhitresult1.getPos().subtract(vec3d).normalize();
                    return vec3d3.dotProduct(vec3d1) < d2 ? null : entityhitresult1;
                } else {
                    return null;
                }
            } else {
                BlockHitResult blockhitresult = VoilClient.mc
                    .world
                    .raycast(new RaycastContext(vec3d, vec3d2, ShapeType.OUTLINE, fluidhandling, VoilClient.mc.player));
                if (blockhitresult != null && blockhitresult.getType() != Type.MISS) {
                    d1 = Math.min(d1, vec3d.squaredDistanceTo(blockhitresult.getPos()));
                }

                EntityHitResult entityhitresult = ProjectileUtil.raycast(VoilClient.mc.player, vec3d, vec3d2, box, predicate1, d1);
                if (entityhitresult != null && entityhitresult.getPos() != null) {
                    Vec3d vec3d4 = entityhitresult.getPos().subtract(vec3d).normalize();
                    if (vec3d4.dotProduct(vec3d1) < d2) {
                        entityhitresult = null;
                    }
                }

                if (blockhitresult == null) {
                    return entityhitresult;
                }

                if (entityhitresult == null) {
                    return blockhitresult;
                }

                double d3 = vec3d.squaredDistanceTo(blockhitresult.getPos());
                return vec3d.squaredDistanceTo(entityhitresult.getPos()) <= d3 ? entityhitresult : blockhitresult;
            }
        } else {
            return null;
        }
    }

    public static HitResult getHitResult(PlayerEntity playerentity, boolean flag, float f, float f1) {
        HitResult hitresult = null;
        if (playerentity != null && VoilClient.mc.world != null) {
            RaycastEvent raycastevent = createRotationState(playerentity);
            double d0 = VoilClient.mc.player.getEntityInteractionRange();
            Vec3d vec3d = playerentity.getCameraPosVec(SystemUtil.getTickDelta());
            Vec3d vec3d1 = getLookVector(f, f1);
            Vec3d vec3d2 = vec3d.add(vec3d1.multiply(d0));
            hitresult = VoilClient.mc.world.raycast(new RaycastContext(vec3d, vec3d2, ShapeType.OUTLINE, FluidHandling.NONE, playerentity));
            double d1 = d0 * d0;
            if (hitresult != null && !raycastevent.hasFilter()) {
                d1 = hitresult.getPos().squaredDistanceTo(vec3d);
            }

            Vec3d vec3d3 = vec3d.add(vec3d1.multiply(d0));
            Box box = playerentity.getBoundingBox().stretch(vec3d1.multiply(d0)).expand(1.0, 1.0, 1.0);
            EntityHitResult entityhitresult = ProjectileUtil.raycast(
                playerentity,
                vec3d,
                vec3d3,
                box,
                entity -> !entity.isSpectator() && entity.canHit() && (!entity.isInvisible() || flag) && raycastevent.test(entity),
                d1
            );
            if (entityhitresult != null) {
                Vec3d vec3d4 = entityhitresult.getPos();
                double d2 = vec3d.squaredDistanceTo(vec3d4);
                if (d2 <= d0 * d0 && (raycastevent.hasFilter() || d2 < d1 || hitresult == null)) {
                    hitresult = entityhitresult;
                }
            }
        }

        return hitresult;
    }

    public static BlockHitResult raycastBlock(double d0, FluidHandling fluidhandling) {
        if (VoilClient.mc.player != null && VoilClient.mc.world != null) {
            Vec3d vec3d = VoilClient.mc.player.getCameraPosVec(SystemUtil.getTickDelta());
            Vec3d vec3d1 = VoilClient.mc.player.getRotationVec(SystemUtil.getTickDelta()).normalize();
            Vec3d vec3d2 = vec3d.add(vec3d1.multiply(d0));
            return VoilClient.mc.world.raycast(new RaycastContext(vec3d, vec3d2, ShapeType.OUTLINE, fluidhandling, VoilClient.mc.player));
        } else {
            return null;
        }
    }

    public static HitResult getHitResultWithRange(PlayerEntity playerentity, boolean flag, float f, float f1, double d0) {
        if (playerentity != null && VoilClient.mc.world != null) {
            RaycastEvent raycastevent = createRotationState(playerentity);
            Vec3d vec3d = playerentity.getCameraPosVec(SystemUtil.getTickDelta());
            Vec3d vec3d1 = getLookVector(f, f1);
            Vec3d vec3d2 = vec3d.add(vec3d1.x * d0, vec3d1.y * d0, vec3d1.z * d0);
            HitResult hitresult = VoilClient.mc.world.raycast(new RaycastContext(vec3d, vec3d2, ShapeType.OUTLINE, FluidHandling.NONE, playerentity));
            double d1 = d0 * d0;
            if (hitresult != null && !raycastevent.hasFilter()) {
                d1 = hitresult.getPos().squaredDistanceTo(vec3d);
            }

            Vec3d vec3d3 = vec3d.add(vec3d1.x * d0, vec3d1.y * d0, vec3d1.z * d0);
            Box box = playerentity.getBoundingBox().stretch(vec3d1.multiply(d0)).expand(1.0, 1.0, 1.0);
            EntityHitResult entityhitresult = ProjectileUtil.raycast(
                playerentity,
                vec3d,
                vec3d3,
                box,
                entity -> !entity.isSpectator() && entity.canHit() && (!entity.isInvisible() || !flag) && raycastevent.test(entity),
                d1
            );
            if (entityhitresult != null) {
                Vec3d vec3d4 = entityhitresult.getPos();
                double d2 = vec3d.squaredDistanceTo(vec3d4);
                if (raycastevent.hasFilter() || d0 > d0 && d2 > Math.pow(d0, 2.0) || d2 < d1 || hitresult == null) {
                    hitresult = d2 > Math.pow(d0, 2.0)
                        ? BlockHitResult.createMissed(vec3d4, Direction.getFacing(vec3d1.x, vec3d1.y, vec3d1.z), BlockPos.ofFloored(vec3d4))
                        : entityhitresult;
                }
            }

            return hitresult;
        } else {
            return null;
        }
    }

    public static HitResult getHitResultFiltered(PlayerEntity playerentity, Predicate<Entity> predicate, float f, float f1) {
        return getHitResultRanged(playerentity, predicate, f, f1, VoilClient.mc.player.getEntityInteractionRange(), false);
    }

    public static BlockHitResult getTargetedBlock(boolean flag) {
        Vec3d vec3d = VoilClient.mc.player.getCameraPosVec(1.0F);
        Vec3d vec3d1 = VoilClient.mc.player.getRotationVec(1.0F);
        double d0 = VoilClient.mc.player.getAttributeValue(EntityAttributes.BLOCK_INTERACTION_RANGE);
        Vec3d vec3d2 = vec3d.add(vec3d1.multiply(d0));
        BlockHitResult blockhitresult = VoilClient.mc
            .world
            .raycast(new RaycastContext(vec3d, vec3d2, ShapeType.OUTLINE, FluidHandling.NONE, VoilClient.mc.player));
        if (blockhitresult.getType() != Type.BLOCK) {
            return null;
        }

        if (flag) {
            double d1 = vec3d.squaredDistanceTo(blockhitresult.getPos());
            if (ProjectileUtil.raycast(
                    VoilClient.mc.player,
                    vec3d,
                    vec3d2,
                    VoilClient.mc.player.getBoundingBox().stretch(vec3d1.multiply(d0)).expand(1.0),
                    entity -> entity instanceof PlayerEntity && entity != VoilClient.mc.player,
                    d1
                )
                != null) {
                return null;
            }
        }

        return blockhitresult;
    }

    public static boolean isLookingAtEntity(Entity entity, double d0, float f) {
        return raycastEntityRotated(d0, entity1 -> entity1 == entity, true, f) != null;
    }

    public static EntityHitResult raycastEntity(double d0, Predicate predicate, boolean flag) {
        if (VoilClient.mc.player != null && VoilClient.mc.world != null) {
            Vec3d vec3d = VoilClient.mc.player.getCameraPosVec(SystemUtil.getTickDelta());
            Vec3d vec3d1 = VoilClient.mc.player.getRotationVec(SystemUtil.getTickDelta()).normalize();
            Vec3d vec3d2 = vec3d.add(vec3d1.multiply(d0));
            Box box = new Box(vec3d, vec3d2).expand(1.0);
            double d1 = d0 * d0;
            EntityHitResult entityhitresult = null;

            for (Entity entity : VoilClient.mc.world.getEntities()) {
                if (predicate.test(entity) && (!flag || entity != VoilClient.mc.player) && entity.getBoundingBox().intersects(box)) {
                    Vec3d vec3d3 = entity.getBoundingBox().expand(0.3).raycast(vec3d, vec3d2).orElse(null);
                    if (vec3d3 != null) {
                        double d2 = vec3d.squaredDistanceTo(vec3d3);
                        if (d2 < d1) {
                            entityhitresult = new EntityHitResult(entity, vec3d3);
                            d1 = d2;
                        }
                    }
                }
            }

            return entityhitresult;
        } else {
            return null;
        }
    }

    public static HitResult raycastFiltered(double d0, Predicate<Entity> predicate, boolean flag, FluidHandling fluidhandling, float f) {
        EntityHitResult entityhitresult = raycastEntityRotated(d0, predicate, flag, f);
        if (entityhitresult != null) {
            ClientPlayerEntity clientplayerentity = VoilClient.mc.player;
            if (createRotationState(clientplayerentity).hasFilter()) {
                return entityhitresult;
            }
        }

        BlockHitResult blockhitresult = raycastBlock(d0, fluidhandling);
        if (blockhitresult == null && entityhitresult == null) {
            return null;
        }

        double d1 = blockhitresult == null ? Double.MAX_VALUE : VoilClient.mc.player.getEyePos().squaredDistanceTo(blockhitresult.getPos());
        double d2 = entityhitresult == null ? Double.MAX_VALUE : VoilClient.mc.player.getEyePos().squaredDistanceTo(entityhitresult.getPos());
        return d1 <= d2 ? blockhitresult : entityhitresult;
    }

    public static Vec3d getPlayerLookVector(PlayerEntity playerentity) {
        return getLookVector(playerentity.getYaw(), playerentity.getPitch());
    }

    public static EntityHitResult raycastEntityRotated(double d0, Predicate<Entity> predicate, boolean flag, float f) {
        if (VoilClient.mc.player != null && VoilClient.mc.world != null) {
            RaycastEvent raycastevent = createRotationState(VoilClient.mc.player);
            Vec3d vec3d = VoilClient.mc.player.getCameraPosVec(SystemUtil.getTickDelta());
            Vec3d vec3d1 = VoilClient.mc.player.getRotationVec(SystemUtil.getTickDelta()).normalize();
            Vec3d vec3d2 = vec3d.add(vec3d1.multiply(d0));
            double d1 = d0 * d0;
            if (!raycastevent.hasFilter()) {
                BlockHitResult blockhitresult = VoilClient.mc
                    .world
                    .raycast(new RaycastContext(vec3d, vec3d2, ShapeType.COLLIDER, FluidHandling.NONE, VoilClient.mc.player));
                if (blockhitresult != null) {
                    d1 = vec3d.squaredDistanceTo(blockhitresult.getPos());
                }
            }

            EntityHitResult entityhitresult = null;
            double d2 = d1;

            for (Entity entity : VoilClient.mc.world.getEntities()) {
                if (predicate.test(entity) && raycastevent.test(entity) && (!flag || entity != VoilClient.mc.player)) {
                    Box box = entity.getBoundingBox();
                    float f1 = 0.0F;

                    while (f1 < d0) {
                        Vec3d vec3d3 = vec3d.add(vec3d1.multiply(f1));
                        double d3 = vec3d.squaredDistanceTo(vec3d3);
                        if (d3 > d1) {
                            break;
                        }

                        if (box.contains(vec3d3) && d3 < d2) {
                            entityhitresult = new EntityHitResult(entity, vec3d3);
                            d2 = d3;
                        }

                        f1 += f;
                    }
                }
            }

            return entityhitresult;
        } else {
            return null;
        }
    }

    private static RaycastEvent createRotationState(Entity entity) {
        RaycastEvent raycastevent = RaycastEvent.create(entity);
        VoilClient.eventBus.postObject(raycastevent);
        return raycastevent;
    }

    public static boolean isAimingAtEntity(Entity entity, double d0, float f) {
        EntityHitResult entityhitresult = raycastEntityRotated(d0, entity1 -> entity1 == entity, true, f);
        return entityhitresult != null && entityhitresult.getEntity() == entity;
    }

    public static boolean canHitEntity(PlayerEntity playerentity, Entity entity, double d0, float f) {
        if (playerentity != null && entity != null && VoilClient.mc.world != null) {
            EntityHitResult entityhitresult = raycastEntityFromPlayer(playerentity, d0, entity1 -> entity1 == entity, true, f);
            return entityhitresult != null && entityhitresult.getEntity() == entity;
        } else {
            return false;
        }
    }

    private static EntityHitResult raycastEntityFromPlayer(PlayerEntity playerentity, double d0, Predicate<Entity> predicate, boolean flag, float f) {
        Vec3d vec3d = playerentity.getCameraPosVec(SystemUtil.getTickDelta());
        Vec3d vec3d1 = getPlayerLookVector(playerentity).normalize();
        double d1 = d0 * d0;
        EntityHitResult entityhitresult = null;
        double d2 = d1;

        for (Entity entity : VoilClient.mc.world.getEntities()) {
            if (predicate.test(entity) && (!flag || entity != playerentity)) {
                Box box = entity.getBoundingBox().expand(0.1);
                float f1 = 0.0F;

                while (f1 < d0) {
                    Vec3d vec3d2 = vec3d.add(vec3d1.multiply(f1));
                    double d3 = vec3d.squaredDistanceTo(vec3d2);
                    if (d3 > d1) {
                        break;
                    }

                    if (box.contains(vec3d2) && d3 < d2) {
                        entityhitresult = new EntityHitResult(entity, vec3d2);
                        d2 = d3;
                    }

                    f1 += f;
                }
            }
        }

        return entityhitresult;
    }

    public static Vec3d getLookVector(float f, float f1) {
        float f2 = f1 * (float) (Math.PI / 180.0);
        float f3 = -f * (float) (Math.PI / 180.0);
        float f4 = MathHelper.cos(f3);
        float f5 = MathHelper.sin(f3);
        float f6 = MathHelper.cos(f2);
        float f7 = MathHelper.sin(f2);
        return new Vec3d(f5 * f6, -f7, f4 * f6);
    }

    public static HitResult getHitResultAnyEntity(PlayerEntity playerentity, float f, float f1) {
        return getHitResultFiltered(playerentity, entity -> true, f, f1);
    }

    public static EntityHitResult raycastEntityAngles(double d0, Predicate predicate, boolean flag, float f, float f1) {
        if (VoilClient.mc.player != null && VoilClient.mc.world != null) {
            Vec3d vec3d = VoilClient.mc.player.getCameraPosVec(SystemUtil.getTickDelta());
            Vec3d vec3d1 = VoilClient.mc.player.getRotationVec(SystemUtil.getTickDelta()).normalize();
            double d1 = d0 * d0;
            EntityHitResult entityhitresult = null;
            double d2 = d1;

            for (Entity entity : VoilClient.mc.world.getEntities()) {
                if (predicate.test(entity) && (!flag || entity != VoilClient.mc.player)) {
                    Vec3d vec3d2 = entity.getEntityPos().add(entity.getVelocity().multiply(f));
                    Box box = new Box(
                            vec3d2.subtract(entity.getWidth() / 2.0F, 0.0, entity.getWidth() / 2.0F),
                            vec3d2.add(entity.getWidth() / 2.0F, entity.getHeight(), entity.getWidth() / 2.0F)
                        )
                        .expand(0.1);
                    float f2 = 0.0F;

                    while (f2 < d0) {
                        Vec3d vec3d3 = vec3d.add(vec3d1.multiply(f2));
                        double d3 = vec3d.squaredDistanceTo(vec3d3);
                        if (d3 > d1) {
                            break;
                        }

                        if (box.contains(vec3d3) && d3 < d2) {
                            entityhitresult = new EntityHitResult(entity, vec3d3);
                            d2 = d3;
                        }

                        f2 += f1;
                    }
                }
            }

            return entityhitresult;
        } else {
            return null;
        }
    }

    public static HitResult getHitResultFromOrigin(
        PlayerEntity playerentity, Predicate<Entity> predicate, float f, float f1, double d0, boolean flag, double d1, Vec3d vec3d
    ) {
        HitResult hitresult = null;
        if (playerentity != null && VoilClient.mc.world != null) {
            RaycastEvent raycastevent = createRotationState(playerentity);
            boolean flag1 = flag || raycastevent.hasFilter();
            double d2 = Math.max(4.5, d0);
            Vec3d vec3d1 = getLookVector(f, f1);
            Vec3d vec3d2 = vec3d.add(vec3d1.x * d2, vec3d1.y * d2, vec3d1.z * d2);
            hitresult = VoilClient.mc.world.raycast(new RaycastContext(vec3d, vec3d2, ShapeType.OUTLINE, FluidHandling.NONE, playerentity));
            boolean flag2 = false;
            if (VoilClient.mc.interactionManager.getCurrentGameMode() == GameMode.CREATIVE) {
                d2 = 6.0;
            } else if (d2 > d0) {
                flag2 = true;
            }

            double d3 = d2 * d2;
            if (hitresult != null) {
                d3 = hitresult.getPos().squaredDistanceTo(vec3d);
            }

            Vec3d vec3d3 = vec3d.add(vec3d1.x * d2, vec3d1.y * d2, vec3d1.z * d2);
            Box box = playerentity.getBoundingBox().stretch(vec3d1.multiply(d2)).expand(1.0, 1.0, 1.0);
            EntityHitResult entityhitresult = raycastEntityBox(
                playerentity,
                vec3d,
                vec3d3,
                box,
                entity -> !entity.isSpectator() && entity.canHit() && predicate.test(entity) && raycastevent.test(entity),
                d3,
                d1,
                flag1
            );
            if (entityhitresult != null) {
                Vec3d vec3d4 = entityhitresult.getPos();
                double d4 = vec3d.squaredDistanceTo(vec3d4);
                if (flag2 && d4 > d0 * d0) {
                    hitresult = BlockHitResult.createMissed(vec3d4, Direction.getFacing(vec3d1.x, vec3d1.y, vec3d1.z), BlockPos.ofFloored(vec3d4));
                } else if (d4 < d3 || hitresult == null || flag1) {
                    hitresult = entityhitresult;
                }
            }
        }

        return hitresult;
    }

    public static BlockHitResult raycastBlockAngles(PlayerEntity playerentity, float f, float f1, double d0, boolean flag) {
        if (playerentity != null && VoilClient.mc.world != null) {
            Vec3d vec3d = playerentity.getCameraPosVec(SystemUtil.getTickDelta());
            Vec3d vec3d1 = getLookVector(f, f1);
            Vec3d vec3d2 = vec3d.add(vec3d1.multiply(d0));
            FluidHandling fluidhandling = flag ? FluidHandling.NONE : FluidHandling.ANY;
            return VoilClient.mc.world.raycast(new RaycastContext(vec3d, vec3d2, ShapeType.OUTLINE, fluidhandling, playerentity));
        } else {
            return null;
        }
    }

    public static HitResult getHitResultCustomRange(
        PlayerEntity playerentity, Predicate<Entity> predicate, float f, float f1, double d0, boolean flag, double d1
    ) {
        return getHitResultFromOrigin(playerentity, predicate, f, f1, d0, flag, d1, playerentity.getCameraPosVec(1.0F));
    }

    public static EntityHitResult raycastEntityBox(
        Entity entity, Vec3d vec3d, Vec3d vec3d1, Box box, Predicate<Entity> predicate, double d0, double d1, boolean flag
    ) {
        World world = entity.getEntityWorld();
        Entity entity1 = null;
        Vec3d vec3d2 = null;

        for (Entity entity2 : world.getOtherEntities(entity, box, predicate)) {
            Box box1 = entity2.getBoundingBox().expand(entity2.getTargetingMargin()).expand(d1);
            Optional<Vec3d> optional = box1.raycast(vec3d, vec3d1);
            if (box1.contains(vec3d)) {
                if (!(d0 < 0.0)) {
                    entity1 = entity2;
                    vec3d2 = optional.orElse(vec3d);
                    d0 = 0.0;
                }
            } else if (!optional.isEmpty()) {
                Vec3d vec3d3 = optional.get();
                double d2 = vec3d.squaredDistanceTo(vec3d3);
                if (flag || !(d2 >= d0) || d0 == 0.0) {
                    if (entity2.getRootVehicle() == entity.getRootVehicle()) {
                        if (d0 == 0.0) {
                            entity1 = entity2;
                            vec3d2 = vec3d3;
                        }
                    } else {
                        entity1 = entity2;
                        vec3d2 = vec3d3;
                        d0 = d2;
                    }
                }
            }
        }

        return entity1 == null ? null : new EntityHitResult(entity1, vec3d2);
    }

    public static HitResult getHitResultFromPosition(PlayerEntity playerentity, float f, float f1, Vec3d vec3d) {
        Predicate predicate = entity -> true;
        return getHitResultFromOrigin(playerentity, predicate, f, f1, VoilClient.mc.player.getEntityInteractionRange(), false, 0.0, vec3d);
    }

    public static HitResult getHitResultRanged(PlayerEntity playerentity, Predicate<Entity> predicate, float f, float f1, double d0, boolean flag) {
        return getHitResultCustomRange(playerentity, predicate, f, f1, d0, flag, 0.0);
    }
}
