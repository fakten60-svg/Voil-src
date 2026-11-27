package lol.lwes.voil.mixin;

import lol.lwes.voil.hook.EndCrystalEntityHooks;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EndCrystalEntity.class)
public abstract class EntityEnderCrystalMixin {
    @Inject(
        method = "damage",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/decoration/EndCrystalEntity;isAlwaysInvulnerableTo(Lnet/minecraft/entity/damage/DamageSource;)Z"
        )
    )
    private void onDamage(ServerWorld serverworld, DamageSource damagesource, float f, CallbackInfoReturnable callbackinforeturnable) {
        EndCrystalEntityHooks.onDamage(this, serverworld, damagesource, f, callbackinforeturnable);
    }
}
