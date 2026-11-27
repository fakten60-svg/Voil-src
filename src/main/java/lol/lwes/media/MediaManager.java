package lol.lwes.media;

import java.util.Locale;

public final class MediaManager {
    private static MediaProvider provider;

    private MediaManager() {
    }

    public static MediaProvider getProvider() {
        if (provider == null) {
            initProvider();
        }
        return provider;
    }

    public static synchronized void initProvider() {
        if (provider != null) {
            return;
        }
        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        if (os.contains("win")) {
            provider = new WindowsMediaProvider();
        } else {
            provider = new LinuxMediaProvider();
        }
    }
}
