package lol.lwes.voil.util.render;

public enum GradientType {
    LINEAR,
    RADIAL;

    private static GradientType[] allValues() {
        return new GradientType[]{LINEAR, RADIAL};
    }
}
