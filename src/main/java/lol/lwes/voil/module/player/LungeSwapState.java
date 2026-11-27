package lol.lwes.voil.module.player;

public enum LungeSwapState {
    IDLE,
    WEAR_CHEST,
    LUNGE,
    WEAR_ELYTRA,
    GLIDE;

    private static LungeSwapState[] $values() {
        return new LungeSwapState[]{IDLE, WEAR_CHEST, LUNGE, WEAR_ELYTRA, GLIDE};
    }
}
