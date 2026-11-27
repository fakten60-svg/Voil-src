package lol.lwes.voil.event;

public class ShutdownEvent extends Event {
    private static final ShutdownEvent INSTANCE = new ShutdownEvent();

    private ShutdownEvent() {
    }

    public static ShutdownEvent getInstance() {
        return INSTANCE;
    }
}
