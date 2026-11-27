package lol.lwes.voil.event;

import net.minecraft.util.math.Vec3d;

public class PlayerTravelPostEvent extends PlayerTravelEvent {
    public PlayerTravelPostEvent(Vec3d vec3d) {
        super(vec3d);
    }
}
