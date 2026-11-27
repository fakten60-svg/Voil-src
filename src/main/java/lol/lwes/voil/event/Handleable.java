package lol.lwes.voil.event;

public interface Handleable {
    default void markHandled() {
        this.setHandled(true);
    }

    boolean isHandled();

    void setHandled(boolean flag);
}
