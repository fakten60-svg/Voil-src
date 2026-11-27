package lol.lwes.voil.module.crystalpvp;

import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket.Handler;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;

public class AutoCrystalAttackHandler implements Handler {
    private final AutoCrystal module;

    public AutoCrystalAttackHandler(AutoCrystal module) {
        this.module = module;
    }

    @Override
    public void interact(Hand hand) {
    }

    @Override
    public void interactAt(Hand hand, Vec3d pos) {
    }

    @Override
    public void attack() {
        MinecraftClient mc = AutoCrystal.getClient();
        if (mc.player == null || !this.module.isEnabled()) {
            return;
        }

        HitResult hit = mc.crosshairTarget;
        if (hit == null || hit.getType() != HitResult.Type.ENTITY) {
            return;
        }

        Entity entity = ((EntityHitResult) hit).getEntity();
        if (!(entity instanceof EndCrystalEntity)) {
            return;
        }

        StatusEffectInstance weakness = mc.player.getStatusEffect(StatusEffects.WEAKNESS);
        StatusEffectInstance strength = mc.player.getStatusEffect(StatusEffects.STRENGTH);
        boolean weakened = weakness != null && (strength == null || strength.getAmplifier() <= weakness.getAmplifier());
        if (weakened && !isTool(mc.player.getMainHandStack())) {
            return;
        }

        entity.discard();
        entity.setRemoved(RemovalReason.KILLED);
        entity.onRemoved();
    }

    private static boolean isTool(ItemStack stack) {
        return stack.contains(DataComponentTypes.TOOL) && !(stack.getItem() instanceof HoeItem);
    }
}
