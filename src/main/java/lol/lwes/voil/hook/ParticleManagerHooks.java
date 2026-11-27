package lol.lwes.voil.hook;

import lol.lwes.voil.VoilClient;
import lol.lwes.voil.event.ParticleEvent;
import net.minecraft.client.particle.Particle;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class ParticleManagerHooks {
    public static void onAddParticle(Particle particle, CallbackInfo callbackinfo) {
        ParticleEvent particleevent = new ParticleEvent(particle);
        VoilClient.eventBus.post(particleevent);
        if (particleevent.isCancelled()) {
            callbackinfo.cancel();
        }
    }
}
