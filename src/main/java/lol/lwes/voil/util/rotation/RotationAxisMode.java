package lol.lwes.voil.util.rotation;

public enum RotationAxisMode {
    YawAndPitch,
    Yaw,
    Pitch;

    private static RotationAxisMode[] $values() {
        return new RotationAxisMode[]{YawAndPitch, Yaw, Pitch};
    }
}
