package lol.lwes.media;

import java.awt.image.BufferedImage;

public abstract class MediaProvider {
    public abstract long getDurationMs();

    public abstract String getSourceAppId();

    public abstract boolean isCoverChanged();

    public abstract String getTitle();

    public abstract boolean next();

    public abstract boolean isPlaying();

    public abstract long getPositionMs();

    public abstract float[] getSpectrum(int bands);

    public abstract String getArtist();

    public abstract void update();

    public abstract boolean togglePlayPause();

    public abstract boolean previous();

    public abstract boolean seek(long positionMs);

    public abstract BufferedImage getCoverImage();

    public abstract long getRawPositionMs();

    public abstract byte[] getThumbnail();
}
