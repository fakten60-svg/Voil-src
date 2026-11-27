package lol.lwes.voil.util.crypto;

public class HashUtil {
    private static long hashString(String s) {
        long i = -3750763034362895579L;
        if (s != null) {
            for (int j = 0; j < s.length(); j++) {
                i ^= s.charAt(j);
                i *= 1099511628211L;
            }
        }

        return i;
    }

    private static long mixBits(long i) {
        long j = (i ^ i >>> 30) * 0xBF58476D1CE4E5B9L;
        j = (j ^ j >>> 27) * 0x94D049BB133111EBL;
        return j ^ j >>> 31;
    }

    public static long combine(long i, String s) {
        long j = mixBits(0L ^ i);
        return mixBits(j ^ hashString(s));
    }
}
