package lol.lwes.media;

public class LyricsController {
    private static final long INTERPOLATION_TIMEOUT_MS = 2000L;
    private final LyricsFetcher fetcher = new LyricsFetcher();
    private long basePositionMs;
    private long baseTimeMs;
    private long lastMediaPositionMs;
    private boolean interpolating;

    public void update(String artist, String title) {
        this.fetcher.update(artist, title);
        long position = MediaManager.getProvider().getPositionMs();
        if (position != this.lastMediaPositionMs) {
            this.lastMediaPositionMs = position;
            this.basePositionMs = position;
            this.baseTimeMs = System.currentTimeMillis();
            this.interpolating = true;
        } else if (System.currentTimeMillis() - this.baseTimeMs > INTERPOLATION_TIMEOUT_MS) {
            this.interpolating = false;
        }
    }

    public long getPositionMs() {
        if (!this.interpolating) {
            return this.basePositionMs;
        }
        return this.basePositionMs + (System.currentTimeMillis() - this.baseTimeMs);
    }

    public LyricLine getCurrentLine() {
        Lyrics lyrics = this.fetcher.getCurrent();
        return lyrics == null ? null : lyrics.getLineAt(this.getPositionMs());
    }

    public LyricLine getNextLine() {
        Lyrics lyrics = this.fetcher.getCurrent();
        return lyrics == null ? null : lyrics.getNextLine(this.getPositionMs());
    }

    public float getLineProgress() {
        Lyrics lyrics = this.fetcher.getCurrent();
        return lyrics == null ? 0.0F : lyrics.getLineProgress(this.getPositionMs());
    }

    public boolean hasLyrics() {
        Lyrics lyrics = this.fetcher.getCurrent();
        return lyrics != null && !lyrics.isEmpty();
    }

    public boolean isLoading() {
        return this.fetcher.isLoading();
    }

    public Lyrics getLyrics() {
        return this.fetcher.getCurrent();
    }

    public void clearCache() {
        this.fetcher.clearCache();
    }
}
