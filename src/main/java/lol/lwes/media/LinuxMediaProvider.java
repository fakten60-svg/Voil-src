package lol.lwes.media;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

public class LinuxMediaProvider extends AbstractMediaProvider {
    private static final int MAX_ART_BYTES = 8 * 1024 * 1024;
    private String lastArtUrl = "";

    public LinuxMediaProvider() {
        if (this.available) {
            Native.initSpectrum(64);
        }
    }

    @Override
    protected byte[] loadThumbnail() {
        if (!this.available) {
            return new byte[0];
        }
        String url = Native.nGetArtUrl();
        if (url == null || url.isEmpty()) {
            this.lastArtUrl = "";
            return new byte[0];
        }
        if (url.equals(this.lastArtUrl)) {
            return this.getThumbnail();
        }
        this.lastArtUrl = url;
        return readArtBytes(url);
    }

    private static byte[] readArtBytes(String url) {
        try {
            URI uri = URI.create(url);
            String scheme = uri.getScheme();
            if ("file".equals(scheme)) {
                Path path = Path.of(uri);
                return Files.size(path) > MAX_ART_BYTES ? new byte[0] : Files.readAllBytes(path);
            }
        } catch (Exception ignored) {
        }
        return new byte[0];
    }
}
