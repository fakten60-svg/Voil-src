package lol.lwes.voil.util.crypto;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class StringCipher {
    private final long key;

    public StringCipher(long i) {
        this.key = i;
    }

    public String decrypt(String s) {
        byte[] abyte = Base64.getUrlDecoder().decode(s);
        byte[] abyte1 = this.crypt(abyte);
        return new String(abyte1, StandardCharsets.UTF_8);
    }

    private byte[] crypt(byte[] abyte) {
        byte[] abyte1 = new byte[abyte.length];
        long i = this.key ^ -7046029254386353131L ^ 1099511628211L * abyte.length;

        for (int j = 0; j < abyte.length; j++) {
            i = mixBits(i ^ j * -7046029254386353131L);
            byte b2 = abyte[j];
            byte b1 = (byte)i;
            byte b0 = b2;
            abyte1[j] = (byte)(b0 ^ b1);
        }

        return abyte1;
    }

    private static long mixBits(long i) {
        long j = (i ^ i >>> 30) * 0xBF58476D1CE4E5B9L;
        j = (j ^ j >>> 27) * 0x94D049BB133111EBL;
        return j ^ j >>> 31;
    }

    public String encrypt(String s) {
        byte[] abyte1 = s.getBytes(StandardCharsets.UTF_8);
        byte[] abyte = this.crypt(abyte1);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(abyte);
    }
}
