package lol.lwes.voil.util.world;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.mixin.accessor.AbstractClientPlayerEntityAccessor;
import lol.lwes.voil.util.AimMathUtil;
import lol.lwes.voil.util.PlayerTargetUtil;
import lol.lwes.voil.util.SystemUtil;
import lol.lwes.voil.util.render.GuiScale;
import lol.lwes.voil.util.rotation.Rotation;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.chunk.WorldChunk;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class EntityUtil {
    public static final Matrix4f projectionMatrix = new Matrix4f();
    public static final Matrix4f viewMatrix = new Matrix4f();
    public static final Matrix4f modelMatrix = new Matrix4f();
    public static int[] viewport = new int[4];

    public static List getEntitiesOfType(Class oclass) {
        ArrayList arraylist = new ArrayList();

        for (Entity entity : VoilClient.mc.world.getEntities()) {
            if (!entity.equals(VoilClient.mc.player) && oclass.isAssignableFrom(entity.getClass())) {
                arraylist.add(entity);
            }
        }

        return arraylist;
    }

    public static BlockPos getLookBlockPos(float f) {
        double d0 = AimMathUtil.interpolate(VoilClient.mc.player.lastX, VoilClient.mc.player.getX(), SystemUtil.getTickDelta());
        double d1 = AimMathUtil.interpolate(VoilClient.mc.player.lastY, VoilClient.mc.player.getY(), SystemUtil.getTickDelta());
        double d2 = AimMathUtil.interpolate(VoilClient.mc.player.lastZ, VoilClient.mc.player.getZ(), SystemUtil.getTickDelta());
        d1 = d1 + VoilClient.mc.player.getEyeHeight(VoilClient.mc.player.getPose()) - 0.1000000014901161;
        double d3 = -MathHelper.sin(f / 180.0F * (float) Math.PI) * MathHelper.cos(VoilClient.mc.player.getPitch() / 180.0F * (float) Math.PI);
        double d4 = -MathHelper.sin(VoilClient.mc.player.getPitch() / 180.0F * 3.141593F);
        double d5 = MathHelper.cos(f / 180.0F * (float) Math.PI) * MathHelper.cos(VoilClient.mc.player.getPitch() / 180.0F * (float) Math.PI);
        float f1 = VoilClient.mc.player.getItemUseTime() / 20.0F;
        f1 = (f1 * f1 + f1 * 2.0F) / 3.0F;
        if (f1 > 1.0F) {
            f1 = 1.0F;
        }

        float f2 = MathHelper.sqrt((float)(d3 * d3 + d4 * d4 + d5 * d5));
        d3 /= f2;
        d4 /= f2;
        d5 /= f2;
        float f3 = f1 * 3.0F;
        d3 *= f3;
        d4 *= f3;
        d5 *= f3;
        if (!VoilClient.mc.player.isOnGround()) {
            d4 += VoilClient.mc.player.getVelocity().getY();
        }

        for (int i = 0; i < 300; i++) {
            Vec3d vec3d = new Vec3d(d0, d1, d2);
            d0 += d3;
            d1 += d4;
            d2 += d5;
            d3 *= 0.99;
            d4 *= 0.99;
            d5 *= 0.99;
            d4 -= 0.05F;
            Vec3d vec3d1 = new Vec3d(d0, d1, d2);

            for (Entity entity : VoilClient.mc.world.getEntities()) {
                if (!(entity instanceof ArrowEntity)
                    && !entity.equals(VoilClient.mc.player)
                    && entity.getBoundingBox().intersects(new Box(d0 - 0.3, d1 - 0.3, d2 - 0.3, d0 + 0.3, d1 + 0.3, d2 + 0.3))) {
                    return null;
                }
            }

            BlockHitResult blockhitresult = VoilClient.mc
                .world
                .raycast(new RaycastContext(vec3d, vec3d1, ShapeType.OUTLINE, FluidHandling.NONE, VoilClient.mc.player));
            if (blockhitresult != null && blockhitresult.getType() == Type.BLOCK) {
                return blockhitresult.getBlockPos();
            }

            if (d1 <= -65.0) {
                break;
            }
        }

        return null;
    }

    public static PlayerEntity getClosestPlayer(PlayerEntity playerentity, float f, boolean flag) {
        float f1 = Float.MAX_VALUE;
        PlayerEntity playerentity1 = null;

        for (PlayerEntity playerentity2 : VoilClient.mc.world.getPlayers()) {
            if (!PlayerTargetUtil.isFriend(playerentity2.getUuid())) {
                float f2 = playerentity2.distanceTo(playerentity);
                if (!playerentity2.getName().equals(playerentity.getName())
                    && playerentity2 != playerentity
                    && f2 <= f
                    && (!flag || playerentity.canSee(playerentity2))
                    && f2 < f1) {
                    f1 = f2;
                    playerentity1 = playerentity2;
                }
            }
        }

        return playerentity1;
    }

    public static Stream<WorldChunk> getLoadedChunks() {
        int k = Math.max(2, VoilClient.mc.options.getClampedViewDistance());
        int i = k + 3;
        k = i + i;
        int j = k + 1;
        ChunkPos chunkpos = VoilClient.mc.player.getChunkPos();
        k = chunkpos.x;
        ChunkPos chunkpos1 = new ChunkPos(k - i, chunkpos.z - i);
        k = chunkpos.x;
        ChunkPos chunkpos2 = new ChunkPos(k + i, chunkpos.z + i);
        return Stream.<ChunkPos>iterate(chunkpos1, chunkpos5 -> {
                int l = chunkpos5.x;
                int i1 = chunkpos5.z;
                if (++l > chunkpos2.x) {
                    l = chunkpos1.x;
                    i1++;
                }

                if (i1 > chunkpos2.z) {
                    throw new IllegalStateException("Stream limit didn't work.");
                } else {
                    return new ChunkPos(l, i1);
                }
            })
            .limit(j * j)
            .filter(chunkpos3 -> VoilClient.mc.world.isChunkLoaded(chunkpos3.x, chunkpos3.z))
            .map(chunkpos3 -> VoilClient.mc.world.getChunk(chunkpos3.x, chunkpos3.z))
            .filter(Objects::nonNull);
    }

    public static boolean isWithinDistance(Entity entity, double d0) {
        if (entity != null && VoilClient.mc.player != null) {
            Vec3d vec3d = VoilClient.mc.player.getEntityPos().add(0.0, VoilClient.mc.player.getEyeHeight(VoilClient.mc.player.getPose()), 0.0);
            Rotation rotation = VoilClient.getRotationManager().isActive()
                ? VoilClient.getRotationManager().getTargetRotation()
                : new Rotation(VoilClient.mc.player.getYaw(), VoilClient.mc.player.getPitch());
            double d1 = Math.toRadians(-rotation.getYawFloat());
            double d2 = Math.toRadians(-rotation.getPitchFloat());
            Vec3d vec3d1 = new Vec3d(Math.sin(d1) * Math.cos(d2), Math.sin(d2), Math.cos(d1) * Math.cos(d2));
            double d3 = entity.getBoundingBox().getCenter().subtract(vec3d).dotProduct(vec3d1);
            if (d3 < 0.0) {
                return false;
            }

            Vec3d vec3d2 = vec3d.add(vec3d1.multiply(d3));
            double d4 = AimMathUtil.closestPointOnBox(vec3d2, entity.getBoundingBox()).distanceTo(vec3d2);
            return d4 > 0.0 && d4 <= d0;
        } else {
            return false;
        }
    }

    public static boolean isLookingAtMe(PlayerEntity playerentity) {
        if (VoilClient.mc.player != null && playerentity != null) {
            Vec3d vec3d = VoilClient.mc.player.getEntityPos().subtract(playerentity.getEntityPos()).normalize();
            float f = playerentity.getYaw();
            float f1 = playerentity.getPitch();
            return new Vec3d(
                        -Math.sin(Math.toRadians(f)) * Math.cos(Math.toRadians(f1)),
                        -Math.sin(Math.toRadians(f1)),
                        Math.cos(Math.toRadians(f)) * Math.cos(Math.toRadians(f1))
                    )
                    .normalize()
                    .dotProduct(vec3d)
                < 0.0;
        } else {
            return false;
        }
    }

    public static PlayerEntity findPlayerByUuid(UUID uuid) {
        return MinecraftClient.getInstance()
            .world
            .getPlayers()
            .stream()
            .filter(abstractclientplayerentity -> abstractclientplayerentity.getUuid().equals(uuid))
            .findFirst()
            .orElse(null);
    }

    public static boolean canPlaceCrystalServer(BlockPos blockpos) {
        if (VoilClient.mc.world == null) {
            return false;
        }

        BlockState blockstate = VoilClient.mc.world.getBlockState(blockpos);
        return blockstate.getBlock() != Blocks.OBSIDIAN && blockstate.getBlock() != Blocks.BEDROCK ? false : canPlaceCrystalClient(blockpos);
    }

    public static boolean isDeadBodyNearby() {
        return VoilClient.mc
            .world
            .getPlayers()
            .parallelStream()
            .filter(abstractclientplayerentity -> abstractclientplayerentity != VoilClient.mc.player)
            .filter(abstractclientplayerentity -> abstractclientplayerentity.squaredDistanceTo(VoilClient.mc.player) <= 36.0)
            .anyMatch(LivingEntity::isDead);
    }

    public static Vec3d[] getBoxCorners(Entity entity) {
        double d0 = entity.lastX + (entity.getX() - entity.lastX) * SystemUtil.getTickDelta();
        double d1 = entity.lastY + (entity.getY() - entity.lastY) * SystemUtil.getTickDelta();
        double d2 = entity.lastZ + (entity.getZ() - entity.lastZ) * SystemUtil.getTickDelta();
        Box box = entity.getBoundingBox().offset(-entity.getX(), -entity.getY(), -entity.getZ()).offset(d0, d1, d2).expand(0.05);
        return new Vec3d[]{
            new Vec3d(box.minX, box.minY, box.minZ),
            new Vec3d(box.minX, box.maxY, box.minZ),
            new Vec3d(box.maxX, box.minY, box.minZ),
            new Vec3d(box.maxX, box.maxY, box.minZ),
            new Vec3d(box.minX, box.minY + 0.1, box.maxZ),
            new Vec3d(box.minX, box.maxY + 0.1, box.maxZ),
            new Vec3d(box.maxX, box.minY + 0.1, box.maxZ),
            new Vec3d(box.maxX, box.maxY + 0.1, box.maxZ)
        };
    }

    public static Entity getClosestTarget(PlayerEntity playerentity, float f, boolean flag) {
        float f1 = Float.MAX_VALUE;
        Entity entity = null;

        for (Entity entity1 : VoilClient.mc.world.getPlayers()) {
            float f2 = entity1.distanceTo(playerentity);
            if (!entity1.getName().equals(playerentity.getName()) && entity1 != playerentity && f2 <= f && (!flag || playerentity.canSee(entity1)) && f2 < f1) {
                f1 = f2;
                entity = entity1;
            }
        }

        return entity;
    }

    public static Vec3d worldToScreen(Vec3d vec3d) {
        if (vec3d == null) {
            throw new NullPointerException("pos is marked non-null but is null");
        }

        if (VoilClient.mc.getEntityRenderDispatcher().camera == null) {
            return new Vec3d(0.0, 0.0, 0.0);
        }

        Camera camera = VoilClient.mc.getEntityRenderDispatcher().camera;
        int i = VoilClient.mc.getWindow().getFramebufferHeight();
        Vector3f vector3f = new Vector3f();
        double d0 = vec3d.x - camera.getCameraPos().x;
        double d1 = vec3d.y - camera.getCameraPos().y;
        double d2 = vec3d.z - camera.getCameraPos().z;
        Vector4f vector4f = new Vector4f((float)d0, (float)d1, (float)d2, 1.0F).mul(modelMatrix);
        Matrix4f matrix4f = new Matrix4f(projectionMatrix);
        Matrix4f matrix4f1 = new Matrix4f(viewMatrix);
        matrix4f.mul(matrix4f1).project(vector4f.x(), vector4f.y(), vector4f.z(), viewport, vector3f);
        float f = GuiScale.getEffectiveScale();
        return new Vec3d(vector3f.x / f, (i - vector3f.y) / f, vector3f.z);
    }

    public static boolean canPlaceCrystalClient(BlockPos blockpos) {
        BlockPos blockpos1 = blockpos.up();
        if (!VoilClient.mc.world.isAir(blockpos1)) {
            return false;
        }

        Box box = new Box(blockpos1).offset(0.5, 0.0, 0.5).stretch(0.0, 2.0, 0.0);
        Iterator iterator = VoilClient.mc.world.getEntitiesByClass(Entity.class, box, entity -> !(entity instanceof ClientPlayerEntity)).iterator();

        while (iterator.hasNext()) {
            if ((Entity)iterator.next() instanceof EndCrystalEntity) {
                return false;
            }
        }

        return true;
    }

    public static boolean isCrystalBase(BlockPos blockpos) {
        if (VoilClient.mc.world == null) {
            return false;
        }

        BlockState blockstate = VoilClient.mc.world.getBlockState(blockpos);
        if (blockstate.getBlock() != Blocks.OBSIDIAN && blockstate.getBlock() != Blocks.BEDROCK) {
            return false;
        }

        BlockPos blockpos1 = blockpos.up();
        BlockState blockstate1 = VoilClient.mc.world.getBlockState(blockpos1);
        if (VoilClient.mc.world.isAir(blockpos1) && blockstate1.getBlock() != Blocks.OBSIDIAN && blockstate1.getBlock() != Blocks.BEDROCK) {
            Box box = new Box(blockpos1).offset(0.5, 0.0, 0.5).stretch(0.0, 2.0, 0.0);
            Iterator iterator = VoilClient.mc.world.getEntitiesByClass(Entity.class, box, entity -> !(entity instanceof ClientPlayerEntity)).iterator();

            while (iterator.hasNext()) {
                if ((Entity)iterator.next() instanceof EndCrystalEntity) {
                    return false;
                }
            }

            return true;
        } else {
            return false;
        }
    }

    public static boolean isNearPlayer(LivingEntity livingentity) {
        if (VoilClient.mc.player != null && livingentity != null) {
            Vec3d vec3d = VoilClient.mc.player.getEntityPos();
            Vec3d vec3d1 = livingentity.getEntityPos();
            Vec3d vec3d2 = vec3d.subtract(vec3d1).normalize();
            float f = livingentity.getYaw();
            float f1 = livingentity.getPitch();
            return new Vec3d(
                        -Math.sin(Math.toRadians(f)) * Math.cos(Math.toRadians(f1)),
                        -Math.sin(Math.toRadians(f1)),
                        Math.cos(Math.toRadians(f)) * Math.cos(Math.toRadians(f1))
                    )
                    .normalize()
                    .dotProduct(vec3d2)
                < 0.0;
        } else {
            return false;
        }
    }

    public static int getPing(PlayerEntity playerentity) {
        if (VoilClient.mc.getNetworkHandler() == null) {
            return 0;
        }

        PlayerListEntry playerlistentry = ((AbstractClientPlayerEntityAccessor)playerentity).invokeGetPlayerListEntry();
        return playerlistentry == null ? 0 : playerlistentry.getLatency();
    }
}
