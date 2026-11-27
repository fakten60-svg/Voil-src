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

public class CrystalAttackHandler implements Handler {
    final AutoCrystal2 module;

    public CrystalAttackHandler(AutoCrystal2 autocrystal2) {
        this.module = autocrystal2;
    }

    @Override
    public void interact(Hand hand) {
    }

    @Override
    public void interactAt(Hand hand, Vec3d vec3d) {
    }

    @Override
    public void attack() {
        HitResult hitresult = AutoCrystal2.getClient4().crosshairTarget;
        if (hitresult != null) {
            Entity entity;
            if (hitresult.getType() == Type.ENTITY && (entity = ((EntityHitResult)hitresult).getEntity()) instanceof EndCrystalEntity) {
                StatusEffectInstance statuseffectinstance = AutoCrystal2.getClient().player.getStatusEffect(StatusEffects.WEAKNESS);
                StatusEffectInstance statuseffectinstance1 = AutoCrystal2.getClient2().player.getStatusEffect(StatusEffects.STRENGTH);
                if (statuseffectinstance != null
                    && (statuseffectinstance1 == null || statuseffectinstance1.getAmplifier() <= statuseffectinstance.getAmplifier())) {
                    AutoCrystal2 autocrystal2 = this.module;
                    ItemStack itemstack = AutoCrystal2.getClient3().player.getMainHandStack();
                    if (!autocrystal2.isTool(itemstack)) {
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
