package lol.lwes.voil.event;

import net.minecraft.client.particle.Particle;

public class ParticleEvent extends Event {
    public Particle particle;

    public ParticleEvent(Particle particle) {
        this.particle = particle;
    }
}
