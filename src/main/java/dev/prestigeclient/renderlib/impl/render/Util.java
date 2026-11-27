package dev.prestigeclient.renderlib.impl.render;

public final class Util {
    private Util() {
    }

    public static int getAlpha(int argb) {
        return argb >>> 24 & 0xFF;
    }

    public static int getRed(int argb) {
        return argb >>> 16 & 0xFF;
    }

    public static int getGreen(int argb) {
        return argb >>> 8 & 0xFF;
    }

    public static int getBlue(int argb) {
        return argb & 0xFF;
    }

    public static float getAlphaFloat(int argb) {
        return getAlpha(argb) / 255.0F;
    }

    public static float getRedFloat(int argb) {
        return getRed(argb) / 255.0F;
    }

    public static float getGreenFloat(int argb) {
        return getGreen(argb) / 255.0F;
    }

    public static float getBlueFloat(int argb) {
        return getBlue(argb) / 255.0F;
    }

    public static int pack(int red, int green, int blue, int alpha) {
        return (alpha & 0xFF) << 24 | (red & 0xFF) << 16 | (green & 0xFF) << 8 | blue & 0xFF;
    }

    public static int toVertexColor(int argb) {
        return argb & 0xFF00FF00 | (argb >>> 16 & 0xFF) | (argb & 0xFF) << 16;
    }
}
