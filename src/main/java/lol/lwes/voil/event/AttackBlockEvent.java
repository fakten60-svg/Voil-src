package lol.lwes.voil.event;

public class AttackBlockEvent extends AttackEvent {
    private static final AttackBlockEvent INSTANCE = new AttackBlockEvent();

    public static AttackBlockEvent getInstance() {
        return INSTANCE;
    }
}
