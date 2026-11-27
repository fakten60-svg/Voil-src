package lol.lwes.media;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class NativePlatform {
    private static final Logger LOGGER = LoggerFactory.getLogger("Voil/Media");
    private static boolean attempted;
    private static boolean loaded;

    private NativePlatform() {
    }

    static String getPlatformId() {
        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        String arch = System.getProperty("os.arch").toLowerCase(Locale.ROOT);
        String platform = os.contains("win") ? "windows" : (os.contains("mac") ? "macos" : "linux");
        String bits = !arch.equals("aarch64") && !arch.equals("arm64") ? "x64" : "arm64";
        return platform + "-" + bits;
    }

    private static String libraryFile(String platformId) {
        if (platformId.startsWith("windows")) {
            return "voil_media.dll";
        }
        return platformId.startsWith("macos") ? "libvoil_media.dylib" : "libvoil_media.so";
    }

    public static synchronized boolean load() {
        if (attempted) {
            return loaded;
        }
        attempted = true;
        String platformId = getPlatformId();
        String file = libraryFile(platformId);
        try (InputStream in = NativePlatform.class.getResourceAsStream("/natives/" + platformId + "/" + file)) {
            if (in == null) {
                LOGGER.info("No media library bundled for {}, now playing is disabled", platformId);
                return false;
            }
            Path dir = Files.createTempDirectory("voil-media");
            Path target = dir.resolve(file);
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            target.toFile().deleteOnExit();
            dir.toFile().deleteOnExit();
            System.load(target.toAbsolutePath().toString());
            loaded = true;
        } catch (Throwable t) {
            LOGGER.warn("Could not load media library", t);
        }
        return loaded;
    }

    public static boolean isLoaded() {
        return loaded;
    }
}
