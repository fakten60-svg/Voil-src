package lol.lwes.voil.util.render;

public enum EasingType {
    LINEAR,
    EASE_OUT_CUBIC,
    EASE_IN_OUT_CUBIC,
    EASE_OUT_EXPO,
    EASE_OUT_BACK,
    EASE_OUT_ELASTIC,
    EASE_OUT_BOUNCE,
    FLUID_SMOOTH,
    FLUID_SPRING,
    FLUID_DAMPED,
    FLUID_RESPONSIVE;

    public static int opaqueZero() {
        return 0;
    }

    public static int opaqueConstant() {
        return 41;
    }

    private static EasingType[] $values() {
        return new EasingType[]{
            LINEAR,
            EASE_OUT_CUBIC,
            EASE_IN_OUT_CUBIC,
            EASE_OUT_EXPO,
            EASE_OUT_BACK,
            EASE_OUT_ELASTIC,
            EASE_OUT_BOUNCE,
            FLUID_SMOOTH,
            FLUID_SPRING,
            FLUID_DAMPED,
            FLUID_RESPONSIVE
        };
    }
}
