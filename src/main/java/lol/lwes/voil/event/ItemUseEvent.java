package lol.lwes.voil.event;

public class ItemUseEvent extends UseEvent {
    private static final ItemUseEvent INSTANCE = new ItemUseEvent();

    public static ItemUseEvent getInstance() {
        return INSTANCE;
    }
}
