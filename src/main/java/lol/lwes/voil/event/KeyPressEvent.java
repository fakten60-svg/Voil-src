package lol.lwes.voil.event;

public class KeyPressEvent {
    private static final KeyPressEvent INSTANCE = new KeyPressEvent();
    public int key;
    public int scancode;
    public int action;
    public long window;

    public static KeyPressEvent create(int i, int j, int k) {
        INSTANCE.key = i;
        INSTANCE.scancode = j;
        INSTANCE.action = k;
        INSTANCE.window = 0L;
        return INSTANCE;
    }
}
