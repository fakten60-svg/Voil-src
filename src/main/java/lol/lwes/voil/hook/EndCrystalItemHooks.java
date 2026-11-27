package lol.lwes.voil.hook;

import lol.lwes.voil.VoilClient;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class EndCrystalItemHooks {
    private static BlockPos lastPlacementPos;
    private static long lastPlacementTime;

    public static void onUseOnBlock(Object item, ItemUsageContext context, CallbackInfoReturnable<?> callbackInfo) {
        if (VoilClient.mc == null || context.getPlayer() == null) {
            return;
        }

        if (!context.getWorld().isClient() || context.getPlayer() != VoilClient.mc.player) {
            return;
        }

        lastPlacementPos = context.getBlockPos().toImmutable();
        lastPlacementTime = System.currentTimeMillis();
    }

    public static BlockPos getLastPlacementPos() {
        return lastPlacementPos;
    }

    public static long getLastPlacementTime() {
        return lastPlacementTime;
    }

    public static long getTimeSinceLastPlacement() {
        return lastPlacementTime == 0L ? Long.MAX_VALUE : System.currentTimeMillis() - lastPlacementTime;
    }
}
