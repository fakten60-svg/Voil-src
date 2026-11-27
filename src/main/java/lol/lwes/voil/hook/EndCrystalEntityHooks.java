package lol.lwes.voil.hook;

import java.util.UUID;
import lol.lwes.voil.VoilClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class EndCrystalEntityHooks {
    private static int lastBrokenCrystalId = -1;
    private static long lastBreakTime;

    public static void onDamage(Object crystal, ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<?> callbackInfo) {
        if (!(crystal instanceof Entity entity) || VoilClient.mc == null || VoilClient.mc.player == null) {
            return;
        }

        Entity attacker = source.getAttacker();
        if (attacker == null) {
            return;
        }

        UUID playerUuid = VoilClient.mc.player.getUuid();
        if (playerUuid.equals(attacker.getUuid())) {
            lastBrokenCrystalId = entity.getId();
            lastBreakTime = System.currentTimeMillis();
        }
    }

    public static int getLastBrokenCrystalId() {
        return lastBrokenCrystalId;
    }

    public static long getLastBreakTime() {
        return lastBreakTime;
    }
}
