package lol.lwes.voil.util;

public final class Stopwatch {
    private long lastReset;

    public Stopwatch() {
        this.reset();
    }

    public boolean hasElapsed(float f) {
        return (float)(this.getScaledTime() - this.lastReset) >= f;
    }

    public boolean hasElapsed(long i) {
        return this.getTimeMillis() - this.lastReset >= i;
    }

    public boolean hasElapsed(double d0) {
        return this.getTimeMillis() - this.lastReset >= d0;
    }

    public long getScaledTime() {
        return System.nanoTime() / 1_000_000L;
    }

    public void reset() {
        this.lastReset = this.getTimeMillis();
    }

    public long getTimeMillis() {
        return System.nanoTime() / 1000000L;
    }
}
