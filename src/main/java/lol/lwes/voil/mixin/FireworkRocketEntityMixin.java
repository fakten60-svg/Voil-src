package lol.lwes.voil.mixin;

import lol.lwes.voil.hook.FireworkRocketEntityHooks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FireworkRocketEntity.class)
public abstract class FireworkRocketEntityMixin {
    @Redirect(
        method = "tick",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getRotationVector()Lnet/minecraft/util/math/Vec3d;")
    )
    private Vec3d fireworkMoveFix(LivingEntity livingentity) {
        return FireworkRocketEntityHooks.fireworkMoveFix(livingentity);
    }
}
