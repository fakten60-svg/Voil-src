package lol.lwes.voil.util.render;

public enum AnimationState {
    IDLE,
    ANIMATING,
    FINISHED;

    private static AnimationState[] $values() {
        return new AnimationState[]{IDLE, ANIMATING, FINISHED};
    }
}
