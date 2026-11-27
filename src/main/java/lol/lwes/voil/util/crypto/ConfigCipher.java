package lol.lwes.voil.util.crypto;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public final class ConfigCipher {
    public static final int PLAIN = 0;
    public static final int AES_GCM = 1;
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12;
    private static final int TAG_BITS = 128;
    private static final byte[] SALT = "voil-config-cipher".getBytes(StandardCharsets.UTF_8);
    private static final SecureRandom RANDOM = new SecureRandom();

    private ConfigCipher() {
    }

    public static String encryptString(int mode, boolean urlSafe, byte[] key, String plaintext) {
        if (plaintext == null || mode == PLAIN) {
            return plaintext;
        }

        try {
            byte[] iv = new byte[IV_LENGTH];
            RANDOM.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, deriveKey(key), new GCMParameterSpec(TAG_BITS, iv));
            byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            byte[] payload = ByteBuffer.allocate(iv.length + encrypted.length).put(iv).put(encrypted).array();
            return encoder(urlSafe).encodeToString(payload);
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Failed to encrypt data", exception);
        }
    }

    public static String decryptString(int mode, boolean urlSafe, byte[] key, String data) {
        if (data == null || mode == PLAIN) {
            return data;
        }

        String trimmed = data.trim();
        if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
            return trimmed;
        }

        try {
            byte[] payload = decoder(urlSafe).decode(trimmed);
            if (payload.length <= IV_LENGTH) {
                throw new IllegalArgumentException("Encrypted payload is too short");
            }

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, deriveKey(key), new GCMParameterSpec(TAG_BITS, payload, 0, IV_LENGTH));
            byte[] decrypted = cipher.doFinal(payload, IV_LENGTH, payload.length - IV_LENGTH);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Failed to decrypt data", exception);
        }
    }

    private static SecretKeySpec deriveKey(byte[] key) throws GeneralSecurityException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(SALT);
        if (key != null) {
            digest.update(key);
        }
        return new SecretKeySpec(digest.digest(), "AES");
    }

    private static Base64.Encoder encoder(boolean urlSafe) {
        return urlSafe ? Base64.getUrlEncoder().withoutPadding() : Base64.getEncoder();
    }

    private static Base64.Decoder decoder(boolean urlSafe) {
        return urlSafe ? Base64.getUrlDecoder() : Base64.getDecoder();
    }
}
