package lol.lwes.voil.module.player;

public enum ElytraSwapState {
    IDLE,
    SWAPPING,
    WAIT_AIRBORNE,
    TRIGGER_GLIDE,
    BOOSTING;

    private static ElytraSwapState[] $values() {
        return new ElytraSwapState[]{IDLE, SWAPPING, WAIT_AIRBORNE, TRIGGER_GLIDE, BOOSTING};
    }
}
