package lol.lwes.voil.event;

public class RenderTickEvent extends Event {
    private static final RenderTickEvent INSTANCE = new RenderTickEvent();

    public static RenderTickEvent getInstance() {
        INSTANCE.setCancelled2(false);
        return INSTANCE;
    }
}
