package lol.lwes.voil.event;

public class DoAttackEvent extends AttackEvent {
    private static final DoAttackEvent INSTANCE = new DoAttackEvent();

    public static DoAttackEvent getInstance() {
        return INSTANCE;
    }
}
