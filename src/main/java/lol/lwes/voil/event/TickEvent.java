package lol.lwes.voil.event;

public class TickEvent extends Event {
    private static final TickEvent INSTANCE = new TickEvent();

    public static TickEvent getInstance() {
        INSTANCE.setCancelled2(false);
        return INSTANCE;
    }
}
