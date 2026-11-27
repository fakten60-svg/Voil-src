package lol.lwes.media;

import java.util.Collections;
import java.util.Locale;

/**
 * Offline lyrics source.
 *
 * <p>Voil does not send the currently playing artist and title to any online service. Lyrics stay
 * empty; the public API is unchanged so the HUD keeps rendering normally.</p>
 */
public class LyricsFetcher {
    private static final Lyrics EMPTY = new Lyrics(Collections.emptyList());
    private volatile Lyrics current;
    private volatile String currentKey = "";

    public Lyrics getCurrent() {
        return this.current;
    }

    public boolean isLoading() {
        return false;
    }

    public void clearCache() {
    }

    public void update(String artist, String title) {
        if (title == null || title.isEmpty()) {
            this.current = null;
            this.currentKey = "";
            return;
        }

        String key = (artist + "|" + title).toLowerCase(Locale.ROOT);
        if (key.equals(this.currentKey)) {
            return;
        }

        this.currentKey = key;
        this.current = EMPTY;
    }
}
