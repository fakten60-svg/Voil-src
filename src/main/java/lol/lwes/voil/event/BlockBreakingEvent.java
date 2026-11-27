package lol.lwes.voil.event;

public class BlockBreakingEvent extends BlockBreakEvent {
    private static final BlockBreakingEvent INSTANCE = new BlockBreakingEvent();

    public static BlockBreakingEvent getInstance() {
        return INSTANCE;
    }
}
