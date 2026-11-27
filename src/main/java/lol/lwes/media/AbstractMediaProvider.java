package lol.lwes.media;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import javax.imageio.ImageIO;

public abstract class AbstractMediaProvider extends MediaProvider {
    private static final long NO_POSITION = -1L;
    private String title = "";
    private String artist = "";
    private String sourceAppId = "";
    private boolean playing;
    private long durationMs;
    private byte[] thumbnail = new byte[0];
    private String trackKey = "";
    private boolean coverChanged;
    private long basePositionMs;
    private long baseTimeMs;
    private long lastRawPositionMs = NO_POSITION;
    private float[] spectrum;
    protected final boolean available;

    protected AbstractMediaProvider() {
        this.available = NativePlatform.load();
    }

    @Override
    public long getDurationMs() {
        return this.durationMs;
    }

    @Override
    public String getSourceAppId() {
        return this.sourceAppId;
    }

    @Override
    public boolean isCoverChanged() {
        return this.coverChanged;
    }

    @Override
    public String getTitle() {
        return this.title;
    }

    @Override
    public String getArtist() {
        return this.artist;
    }

    @Override
    public boolean isPlaying() {
        return this.playing;
    }

    @Override
    public boolean next() {
        return this.available && Native.nNext();
    }

    @Override
    public boolean previous() {
        return this.available && Native.nPrevious();
    }

    @Override
    public boolean togglePlayPause() {
        return this.available && Native.nPlayPause();
    }

    @Override
    public long getPositionMs() {
        if (!this.playing) {
            return this.basePositionMs;
        }
        long position = this.basePositionMs + (System.currentTimeMillis() - this.baseTimeMs);
        if (this.durationMs > 0L) {
            position = Math.min(position, this.durationMs);
        }
        return Math.max(position, 0L);
    }

    @Override
    public long getRawPositionMs() {
        return this.lastRawPositionMs;
    }

    @Override
    public float[] getSpectrum(int bands) {
        if (this.spectrum == null || this.spectrum.length != bands) {
            this.spectrum = new float[bands];
        }
        if (this.available) {
            Native.getSpectrum(this.spectrum);
        }
        return this.spectrum;
    }

    @Override
    public void update() {
        if (!this.available) {
            return;
        }
        Native.nTick();
        this.title = nonNull(Native.nGetTitle());
        this.artist = nonNull(Native.nGetArtist());
        this.playing = Native.nIsPlaying();
        this.durationMs = Native.nGetDurationMs();
        this.sourceAppId = nonNull(Native.nGetSourceAppId());

        String key = this.artist + "\u0000" + this.title;
        if (!key.equals(this.trackKey)) {
            this.trackKey = key;
            this.setThumbnail(this.loadThumbnail());
            this.coverChanged = true;
        } else {
            this.coverChanged = false;
        }

        long raw = Native.nGetPositionMs();
        long now = System.currentTimeMillis();
        if (this.coverChanged || raw != this.lastRawPositionMs) {
            this.lastRawPositionMs = raw;
            this.basePositionMs = raw;
            this.baseTimeMs = now;
        } else if (!this.playing) {
            this.basePositionMs = raw;
            this.baseTimeMs = now;
        }
    }

    @Override
    public boolean seek(long positionMs) {
        long target = Math.max(positionMs, 0L);
        if (this.durationMs > 0L) {
            target = Math.min(target, this.durationMs);
        }
        if (!this.available || !Native.nSeek(target)) {
            return false;
        }
        this.basePositionMs = target;
        this.baseTimeMs = System.currentTimeMillis();
        return true;
    }

    @Override
    public BufferedImage getCoverImage() {
        if (this.thumbnail == null || this.thumbnail.length == 0) {
            return null;
        }
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(this.thumbnail));
            if (image == null) {
                return null;
            }
            int width = image.getWidth();
            if (image.getHeight() > width) {
                return image.getSubimage(0, 0, width, width);
            }
            return this.processCover(image);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public byte[] getThumbnail() {
        return this.thumbnail;
    }

    protected void setThumbnail(byte[] data) {
        this.thumbnail = data == null ? new byte[0] : data;
    }

    protected BufferedImage processCover(BufferedImage image) {
        return image;
    }

    protected byte[] loadThumbnail() {
        return Native.nGetThumbnail();
    }

    private static String nonNull(String s) {
        return s == null ? "" : s;
    }
}
