package lol.lwes.media;

public final class Native {
    private Native() {
    }

    public static native void initSpectrum(int bands);

    public static native void nTick();

    public static native boolean nIsPlaying();

    public static native String nGetTitle();

    public static native String nGetArtist();

    public static native String nGetSourceAppId();

    public static native String nGetArtUrl();

    public static native byte[] nGetThumbnail();

    public static native long nGetDurationMs();

    public static native long nGetPositionMs();

    public static native boolean nPlayPause();

    public static native boolean nNext();

    public static native boolean nPrevious();

    public static native boolean nSeek(long positionMs);

    public static native void getSpectrum(float[] out);
}
