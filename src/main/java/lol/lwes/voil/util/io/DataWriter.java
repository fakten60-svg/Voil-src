package lol.lwes.voil.util.io;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class DataWriter {
    private final DataOutputStream out;

    public DataWriter(OutputStream outputstream) {
        this.out = outputstream instanceof DataOutputStream ? (DataOutputStream)outputstream : new DataOutputStream(outputstream);
    }

    public void writeLong(long i) throws IOException {
        this.out.writeLong(i);
    }

    public void writeVarInt() throws IOException {
        byte b0;
        for (b0 = 0; (b0 & -128) != 0; b0 >>>= 7) {
            this.out.writeByte(b0 & 127 | 128);
        }

        this.out.writeByte(b0);
    }

    public void writeByte(int i) throws IOException {
        this.out.writeByte(i);
    }

    public void writeShort(int i) throws IOException {
        this.out.writeShort(i);
    }

    public void writeBytes(byte[] abyte) throws IOException {
        this.out.write(abyte);
    }

    public void writeFloat(float f) throws IOException {
        this.out.writeFloat(f);
    }

    public void flush() throws IOException {
        this.out.flush();
    }

    public void writeInt(int i) throws IOException {
        this.out.writeInt(i);
    }

    public void writeBoolean(boolean flag) throws IOException {
        this.out.writeBoolean(flag);
    }

    public void writeByteArray(byte[] abyte) throws IOException {
        if (abyte.length > 33554432) {
            int i = abyte.length;
            throw new IOException("byte array too long: " + i);
        }

        int j = abyte.length;
        this.writeVarInt();
        this.out.write(abyte);
    }

    public void writeDouble(double d0) throws IOException {
        this.out.writeDouble(d0);
    }

    public void writeString(String s) throws IOException {
        if (s == null) {
            s = "";
        }

        byte[] abyte = s.getBytes(StandardCharsets.UTF_8);
        if (abyte.length > 3145728) {
            int i = abyte.length;
            throw new IOException("string too long: " + i);
        }

        int j = abyte.length;
        this.writeVarInt();
        this.out.write(abyte);
    }
}
