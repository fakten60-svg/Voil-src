package lol.lwes.voil.event;

public class BlockBreakStartEvent extends BlockBreakEvent {
    private static final BlockBreakStartEvent INSTANCE = new BlockBreakStartEvent();

    public static BlockBreakStartEvent getInstance() {
        return INSTANCE;
    }
}
