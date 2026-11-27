package lol.lwes.voil.hook;

import lol.lwes.voil.VoilClient;
import lol.lwes.voil.event.EntitySpawnEvent;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class ClientWorldHooks {
    public static void addEntityHook(Entity entity, CallbackInfo callbackinfo) {
        EntitySpawnEvent entityspawnevent = new EntitySpawnEvent(entity);
        VoilClient.eventBus.post(entityspawnevent);
        if (entityspawnevent.isCancelled()) {
            callbackinfo.cancel();
        }
    }
}
