package lol.lwes.media;

public record LyricLine(long timeMs, String text) implements Comparable<LyricLine> {
    @Override
    public int compareTo(LyricLine other) {
        return Long.compare(this.timeMs, other.timeMs);
    }
}
