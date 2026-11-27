package lol.lwes.voil.event;

public class Event implements Cancellable {
    private boolean cancelled = false;

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    public void setCancelled(boolean flag) {
        this.cancelled = flag;
    }

    @Override
    public void setCancelled2(boolean flag) {
        this.setCancelled(flag);
    }
}
