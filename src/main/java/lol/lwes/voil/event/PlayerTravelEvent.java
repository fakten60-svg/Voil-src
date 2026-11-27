package lol.lwes.voil.event;

import net.minecraft.util.math.Vec3d;

public class PlayerTravelEvent extends Event {
    private Vec3d velocity;

    public PlayerTravelEvent(Vec3d vec3d) {
        this.velocity = vec3d;
    }

    public Vec3d getVelocity() {
        return this.velocity;
    }
}
