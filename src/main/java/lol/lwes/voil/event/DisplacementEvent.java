package lol.lwes.voil.event;

import java.util.function.Consumer;
import net.minecraft.entity.Entity;

public class DisplacementEvent {
    public Entity entity;
    public boolean handled;
    public float yawOffset;
    public Consumer<Float> callback;
    public Runnable onFinish;

    private DisplacementEvent() {
    }

    public static DisplacementEvent create(Entity entity) {
        DisplacementEvent displacementevent = new DisplacementEvent();
        displacementevent.entity = entity;
        return displacementevent;
    }
}
