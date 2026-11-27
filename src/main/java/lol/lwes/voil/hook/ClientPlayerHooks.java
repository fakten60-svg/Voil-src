package lol.lwes.voil.hook;

import lol.lwes.voil.VoilClient;
import lol.lwes.voil.event.HandSwingEvent;
import lol.lwes.voil.event.PlayerMoveEvent;
import lol.lwes.voil.module.ModuleManager;
import lol.lwes.voil.module.player.NoSlow;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public abstract class ClientPlayerHooks {
    public static void onIsSneaking(CallbackInfoReturnable callbackinforeturnable) {
        NoSlow noslow = (NoSlow)ModuleManager.INSTANCE.getModule(NoSlow.class);
        if (noslow != null && noslow.isEnabled() && noslow.sneaking.isEnabled()) {
            callbackinforeturnable.setReturnValue(false);
        }
    }

    public static void swingHand(Hand hand, CallbackInfo callbackinfo) {
        if (!VoilClient.getInstance().disabled) {
            HandSwingEvent handswingevent = new HandSwingEvent(hand);
            VoilClient.eventBus.post(handswingevent);
            if (handswingevent.isCancelled()) {
                callbackinfo.cancel();
            }
        }
    }

    public static void onShouldSlowDown(CallbackInfoReturnable callbackinforeturnable) {
        NoSlow noslow = (NoSlow)ModuleManager.INSTANCE.getModule(NoSlow.class);
        if (noslow != null && noslow.isEnabled() && noslow.sneaking.isEnabled()) {
            callbackinforeturnable.setReturnValue(false);
        }
    }

    public static boolean applyMovementSpeedFactors(ClientPlayerEntity clientplayerentity) {
        NoSlow noslow = (NoSlow)ModuleManager.INSTANCE.getModule(NoSlow.class);
        return noslow != null && noslow.isEnabled() && noslow.items.isEnabled() ? false : clientplayerentity.isUsingItem();
    }

    public static void onMoveHook(Object object, MovementType movementtype, Vec3d vec3d, CallbackInfo callbackinfo) {
        ClientPlayerEntity clientplayerentity = (ClientPlayerEntity)object;
        if (!VoilClient.getInstance().disabled) {
            PlayerMoveEvent playermoveevent = new PlayerMoveEvent(vec3d.x, vec3d.y, vec3d.z);
            VoilClient.eventBus.post(playermoveevent);
            if (playermoveevent.isCancelled()) {
                clientplayerentity.move(movementtype, new Vec3d(playermoveevent.getX(), playermoveevent.getY(), playermoveevent.getZ()));
                callbackinfo.cancel();
            }
        }
    }
}
