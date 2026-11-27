package lol.lwes.voil.util;

import lol.lwes.voil.VoilClient;

/**
 * Local-only crash reporting.
 *
 * <p>The client no longer reads {@code logs/latest.log}, {@code options.txt}, the config folder or
 * hashes of installed mods, and it never uploads a report. Minecraft already writes its own crash
 * report into {@code crash-reports/}.</p>
 */
public final class CrashReporter {
    private CrashReporter() {
    }

    public static void collectAndSendReport(String type) {
        VoilClient.LOGGER.warn("Crash report of type {} was generated locally; nothing is uploaded.", type);
    }
}
