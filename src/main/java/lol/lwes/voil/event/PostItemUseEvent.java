package lol.lwes.voil.event;

public class PostItemUseEvent extends Event {
    private static final PostItemUseEvent INSTANCE = new PostItemUseEvent();

    public static PostItemUseEvent getInstance() {
        INSTANCE.setCancelled(false);
        return INSTANCE;
    }

    public boolean wasCancelled() {
        return this.isCancelled();
    }
}
