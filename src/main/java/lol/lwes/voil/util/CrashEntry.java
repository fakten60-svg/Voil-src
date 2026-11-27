package lol.lwes.voil.util;

import java.util.Objects;

public record CrashEntry(String module, String stackTrace, long timestamp) {
    @Override
    public final boolean equals(Object object) {
        return this == object
            || object instanceof CrashEntry crashentry1
                && this.timestamp == crashentry1.timestamp
                && Objects.equals(this.stackTrace, crashentry1.stackTrace)
                && Objects.equals(this.module, crashentry1.module);
    }

    @Override
    public final String toString() {
        return "in[" + "a1=" + this.module + ", a2=" + this.stackTrace + ", a3=" + this.timestamp + "]";
    }

    @Override
    public final int hashCode() {
        int i = 0 + Objects.hashCode(this.module);
        i = i * 31 + Objects.hashCode(this.stackTrace);
        return i * 31 + Long.hashCode(this.timestamp);
    }
}
