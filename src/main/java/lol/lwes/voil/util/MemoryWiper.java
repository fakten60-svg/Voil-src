package lol.lwes.voil.util;

import java.lang.reflect.Field;
import sun.misc.Unsafe;

public class MemoryWiper {
    private static Unsafe unsafe;
    private static long valueOffset = -1L;

    static {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            unsafe = (Unsafe)field.get(null);

            try {
                Field field1 = String.class.getDeclaredField("value");
                Field field3 = String.class.getDeclaredField("coder");
                valueOffset = unsafe.objectFieldOffset(field1);
                unsafe.objectFieldOffset(field3);
            } catch (NoSuchFieldException nosuchfieldexception) {
                Field field2 = String.class.getDeclaredField("value");
                valueOffset = unsafe.objectFieldOffset(field2);
            }
        } catch (Exception exception) {
            exception.printStackTrace(System.err);
        }
    }

    public static void overwriteStringBytes(String s, byte[] abyte) {
        if (s != null && unsafe != null && valueOffset != 0L) {
            try {
                byte[] abyte1 = (byte[])unsafe.getObject(s, valueOffset);
                if (abyte1 != null) {
                    for (int i = 0; i < abyte1.length && i < abyte.length; i++) {
                        unsafe.putByte(abyte1, Unsafe.ARRAY_BYTE_BASE_OFFSET + (long)i * Unsafe.ARRAY_BYTE_INDEX_SCALE, abyte[i]);
                    }
                }
            } catch (Throwable throwable) {
                System.err.println("Memory put failed for: " + s);
            }
        }
    }

    public static void overwriteString(String s, String s1) {
        if (s != null && unsafe != null && valueOffset != 0L) {
            try {
                byte[] abyte = (byte[])unsafe.getObject(s, valueOffset);
                byte[] abyte1 = (byte[])unsafe.getObject(s1, valueOffset);
                if (abyte != null) {
                    for (int i = 0; i < abyte.length && i < s1.length(); i++) {
                        unsafe.putByte(abyte, Unsafe.ARRAY_BYTE_BASE_OFFSET + (long)i * Unsafe.ARRAY_BYTE_INDEX_SCALE, abyte1[i]);
                    }
                }
            } catch (Throwable throwable) {
                System.err.println("Memory put failed for: " + s);
            }
        }
    }

    public static Unsafe getUnsafe() {
        return unsafe;
    }

    public static void wipeString(String s) {
        if (s != null && unsafe != null && valueOffset != -1L) {
            try {
                byte[] abyte = (byte[])unsafe.getObject(s, valueOffset);
                if (abyte != null) {
                    for (int i = 0; i < abyte.length; i++) {
                        unsafe.putByte(abyte, Unsafe.ARRAY_BYTE_BASE_OFFSET + (long)i * Unsafe.ARRAY_BYTE_INDEX_SCALE, (byte)0);
                    }
                }
            } catch (Throwable throwable) {
                System.err.println("Memory wipe failed for: " + s);
            }
        }
    }
}
