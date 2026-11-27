package lol.lwes.voil.event;

public interface Cancellable {
    default void cancel() {
        this.setCancelled2(true);
    }

    void setCancelled2(boolean flag);

    boolean isCancelled();
}
