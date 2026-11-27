package lol.lwes.voil.util;

import java.io.File;
import java.io.FileInputStream;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.SecureRandom;
import lol.lwes.voil.VoilClient;

public class SystemUtil {
    private static final int HASH_BUFFER_SIZE = 8192;

    public static boolean isLinux() {
        return System.getProperty("os.name").toLowerCase().contains("linux");
    }

    public static Path getAppDataDir() {
        String s = System.getenv("LOCALAPPDATA");
        return s != null && !s.isBlank() ? Path.of(s, "Programs", "Common") : Path.of(System.getProperty("user.home"), ".lolcow");
    }

    public static float getTickDelta() {
        return TickFastForward.tickDelta != null ? TickFastForward.tickDelta : VoilClient.mc.getRenderTickCounter().getTickProgress(false);
    }

    public static String randomString(int i) {
        SecureRandom securerandom = new SecureRandom();
        StringBuilder stringbuilder = new StringBuilder(i);

        for (int j = 0; j < i; j++) {
            stringbuilder.append(
                "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
                    .charAt(securerandom.nextInt("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".length()))
            );
        }

        return stringbuilder.toString();
    }

    public static String getJarHash() {
        try {
            File file1 = getJarFile();
            MessageDigest messagedigest = MessageDigest.getInstance("SHA-256");

            try (FileInputStream fileinputstream = new FileInputStream(file1)) {
                byte[] abyte = new byte[HASH_BUFFER_SIZE];

                int i;
                while ((i = fileinputstream.read(abyte)) > 0) {
                    messagedigest.update(abyte, 0, i);
                }
            }

            byte[] abyte1 = messagedigest.digest();
            StringBuilder stringbuilder = new StringBuilder();

            for (byte b0 : abyte1) {
                stringbuilder.append(String.format("%02x", b0));
            }

            return stringbuilder.toString();
        } catch (Exception exception) {
            exception.printStackTrace(System.err);
            return "";
        }
    }

    public static File getJarFile() throws URISyntaxException {
        return VoilClient.mc == null ? null : new File(SystemUtil.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
    }
}
