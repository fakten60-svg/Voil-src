package lol.lwes.voil.event;

public class ExplosionEvent extends Event {
    private final int id;

    public ExplosionEvent(int i) {
        this.id = i;
    }

    public int getId() {
        return this.id;
    }
}
