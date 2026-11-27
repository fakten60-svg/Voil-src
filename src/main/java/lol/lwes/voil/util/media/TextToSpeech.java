package lol.lwes.voil.util.media;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TextToSpeech {
    private static final int MAX_LENGTH = 400;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "Voil TTS");
        thread.setDaemon(true);
        return thread;
    });
    private final Platform platform = Platform.detect();
    private volatile Process current;

    public boolean isAvailable() {
        return this.platform != Platform.UNSUPPORTED;
    }

    public void speak(String text) {
        if (text == null || text.isBlank()) {
            return;
        }

        if (!this.isAvailable()) {
            throw new IllegalStateException("Text to speech is not supported on this platform");
        }

        String sanitized = sanitize(text);
        this.executor.execute(() -> this.run(sanitized));
    }

    public void stop() {
        Process process = this.current;
        if (process != null) {
            process.destroy();
        }
    }

    private void run(String text) {
        this.stop();
        try {
            Process process = new ProcessBuilder(this.platform.command(text)).redirectErrorStream(true).start();
            this.current = process;
            process.getInputStream().transferTo(OutputStream.nullOutputStream());
            if (!process.waitFor(30L, TimeUnit.SECONDS)) {
                process.destroyForcibly();
            }
        } catch (IOException ignored) {
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        } finally {
            this.current = null;
        }
    }

    private static String sanitize(String text) {
        String cleaned = text.replaceAll("§.", "").replaceAll("[^\\p{L}\\p{N}\\p{Zs}.,!?'\\-]", " ").trim();
        return cleaned.length() > MAX_LENGTH ? cleaned.substring(0, MAX_LENGTH) : cleaned;
    }

    private enum Platform {
        WINDOWS,
        MAC,
        LINUX,
        UNSUPPORTED;

        static Platform detect() {
            String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
            if (os.contains("win")) {
                return WINDOWS;
            }
            if (os.contains("mac")) {
                return MAC;
            }
            if (os.contains("nux") || os.contains("nix")) {
                return LINUX;
            }
            return UNSUPPORTED;
        }

        List<String> command(String text) {
            return switch (this) {
                case WINDOWS -> List.of(
                    "powershell", "-NoProfile", "-NonInteractive", "-Command",
                    "Add-Type -AssemblyName System.Speech; (New-Object System.Speech.Synthesis.SpeechSynthesizer).Speak('" + text.replace("'", "''") + "')"
                );
                case MAC -> List.of("say", text);
                case LINUX -> List.of("espeak", text);
                case UNSUPPORTED -> throw new IllegalStateException("Text to speech is not supported on this platform");
            };
        }
    }
}
