package lol.lwes.voil.util;

import lol.lwes.voil.VoilClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import net.minecraft.world.RaycastContext;

public class CombatUtil {
    private static final String CLOCK_TOTEM_SERVER_KEYWORD = "hugosmp";

    public static boolean isStandingStill(PlayerEntity playerentity) {
        return playerentity == null
            ? false
            : !playerentity.hasStatusEffect(StatusEffects.BLINDNESS)
                && !playerentity.isClimbing()
                && !playerentity.isTouchingWater()
                && !playerentity.hasVehicle()
                && playerentity.getMovement().lengthSquared() < 1.0E-7;
    }

    public static boolean isShieldBlockingFrom(LivingEntity livingentity, LivingEntity livingentity1, boolean flag) {
        if (livingentity == null || livingentity1 == null) {
            return false;
        } else if (PlayerTargetUtil.isFriend(livingentity1.getUuid())) {
            return false;
        } else if (livingentity1.isBlocking() || flag && livingentity1.getActiveItem().isOf(Items.SHIELD)) {
            Vec3d vec3d = livingentity1.getRotationVec(1.0F);
            Vec3d vec3d1 = livingentity.getEntityPos().relativize(livingentity1.getEntityPos());
            vec3d1 = new Vec3d(vec3d1.x, 0.0, vec3d1.z).normalize();
            return vec3d1.dotProduct(vec3d) < 0.0;
        } else {
            return false;
        }
    }

    public static boolean canSee(Vec3d vec3d, Vec3d vec3d1) {
        return VoilClient.mc.world.raycast(new RaycastContext(vec3d, vec3d1, ShapeType.OUTLINE, FluidHandling.NONE, VoilClient.mc.player)).getType()
            == Type.MISS;
    }

    public static boolean isInFov(float f, Entity entity) {
        if (VoilClient.mc.player != null && entity != null && entity != VoilClient.mc.player) {
            float f1 = MathHelper.wrapDegrees(VoilClient.mc.player.getYaw());
            double d0 = entity.getX() - VoilClient.mc.player.getX();
            return Math.abs(
                    MathHelper.wrapDegrees(
                        (float)MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(entity.getZ() - VoilClient.mc.player.getZ(), d0)) - 90.0) - f1
                    )
                )
                <= f / 2.0F;
        } else {
            return false;
        }
    }

    public static double getBlockReach() {
        return VoilClient.mc.player == null ? 0.0 : VoilClient.mc.player.getBlockInteractionRange();
    }

    public static ItemStack getShieldStack(PlayerEntity playerentity) {
        ItemStack itemstack = null;

        for (Hand hand : Hand.values()) {
            ItemStack itemstack1 = playerentity.getStackInHand(hand);
            if (itemstack1.isOf(Items.SHIELD)) {
                itemstack = itemstack1;
            }
        }

        return itemstack;
    }

    public static boolean canCritical(PlayerEntity playerentity, boolean flag) {
        if (playerentity == null) {
            return false;
        }

        if (flag) {
            BlockHitResult blockhitresult = RaycastUtil.raycastBlock(10.0, FluidHandling.NONE);
            if (playerentity.getY() - blockhitresult.getPos().getY() <= 0.15) {
                return false;
            }
        }

        return !playerentity.hasStatusEffect(StatusEffects.BLINDNESS)
            && !playerentity.isClimbing()
            && !playerentity.isTouchingWater()
            && !playerentity.hasVehicle()
            && !playerentity.isOnGround()
            && playerentity.fallDistance > 0.065F
            && playerentity.getAttackCooldownProgress(0.5F) >= 0.7
            && playerentity.getMovement().lengthSquared() < 1.0E-7;
    }

    public static float getDistanceSqToEyes(Vec3d vec3d) {
        if (VoilClient.mc.player == null) {
            return 0.0F;
        }

        double d0 = vec3d.x - VoilClient.mc.player.getX();
        double d1 = vec3d.y - (VoilClient.mc.player.getY() + VoilClient.mc.player.getEyeHeight(VoilClient.mc.player.getPose()));
        double d2 = vec3d.z - VoilClient.mc.player.getZ();
        return (float)(d0 * d0 + d1 * d1 + d2 * d2);
    }

    public static Item getTotemItem() {
        ServerInfo serverinfo = VoilClient.mc.getCurrentServerEntry();
        return (serverinfo != null ? serverinfo.address : "").toLowerCase().contains(CLOCK_TOTEM_SERVER_KEYWORD) ? Items.CLOCK : Items.TOTEM_OF_UNDYING;
    }

    public static double getEntityReach() {
        return VoilClient.mc.player == null ? 0.0 : VoilClient.mc.player.getEntityInteractionRange();
    }
}
