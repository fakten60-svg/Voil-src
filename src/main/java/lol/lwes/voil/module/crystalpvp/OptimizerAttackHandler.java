package lol.lwes.voil.module.crystalpvp;

import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket.Handler;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;

public class OptimizerAttackHandler implements Handler {
    final CrystalOptimizer module;

    public OptimizerAttackHandler(CrystalOptimizer crystaloptimizer) {
        this.module = crystaloptimizer;
    }

    @Override
    public void interact(Hand hand) {
    }

    @Override
    public void interactAt(Hand hand, Vec3d vec3d) {
    }

    @Override
    public void attack() {
        HitResult hitresult = CrystalOptimizer.getClient().crosshairTarget;
        if (hitresult != null) {
            Entity entity;
            if (hitresult.getType() == Type.ENTITY && (entity = ((EntityHitResult)hitresult).getEntity()) instanceof EndCrystalEntity) {
                StatusEffectInstance statuseffectinstance = CrystalOptimizer.getClient4().player.getStatusEffect(StatusEffects.WEAKNESS);
                StatusEffectInstance statuseffectinstance1 = CrystalOptimizer.getClient3().player.getStatusEffect(StatusEffects.STRENGTH);
                if (statuseffectinstance != null
                    && (statuseffectinstance1 == null || statuseffectinstance1.getAmplifier() <= statuseffectinstance.getAmplifier())) {
                    CrystalOptimizer crystaloptimizer = this.module;
                    ItemStack itemstack = CrystalOptimizer.getClient2().player.getMainHandStack();
                    if (!crystaloptimizer.isTool(itemstack)) {
                        return;
                    }
                }

                entity.discard();
                entity.setRemoved(RemovalReason.KILLED);
                entity.onRemoved();
            }
        }
    }
}
