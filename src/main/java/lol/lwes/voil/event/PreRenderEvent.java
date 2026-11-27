package lol.lwes.voil.event;

public class PreRenderEvent {
    private static final PreRenderEvent INSTANCE = new PreRenderEvent();

    public static PreRenderEvent getInstance() {
        return INSTANCE;
    }
}
