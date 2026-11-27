package lol.lwes.voil.util;

public enum MovementMode {
    NONE,
    FOLLOW,
    FLEE,
    FALL;

    private static MovementMode[] allValues() {
        return new MovementMode[]{NONE, FOLLOW, FLEE, FALL};
    }
}
