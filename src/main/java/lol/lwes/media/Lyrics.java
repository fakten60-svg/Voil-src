package lol.lwes.media;

import java.util.List;

public class Lyrics {
    private static final long DEFAULT_LINE_DURATION_MS = 5000L;
    private final List<LyricLine> lines;

    public Lyrics(List<LyricLine> lines) {
        this.lines = List.copyOf(lines);
    }

    public LyricLine getLineAt(long positionMs) {
        int index = this.findLineIndex(positionMs);
        return index >= 0 ? this.lines.get(index) : null;
    }

    public LyricLine getNextLine(long positionMs) {
        int index = this.findLineIndex(positionMs) + 1;
        return index < this.lines.size() ? this.lines.get(index) : null;
    }

    public boolean isEmpty() {
        return this.lines.isEmpty();
    }

    public int getLineCount() {
        return this.lines.size();
    }

    public List<LyricLine> getLines() {
        return this.lines;
    }

    public int findLineIndex(long positionMs) {
        int low = 0;
        int high = this.lines.size() - 1;
        int found = -1;
        while (low <= high) {
            int mid = low + high >>> 1;
            if (this.lines.get(mid).timeMs() <= positionMs) {
                found = mid;
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        return found;
    }

    public float getLineProgress(long positionMs) {
        int index = this.findLineIndex(positionMs);
        if (index < 0) {
            return 0.0F;
        }
        long start = this.lines.get(index).timeMs();
        long end = index + 1 < this.lines.size() ? this.lines.get(index + 1).timeMs() : start + DEFAULT_LINE_DURATION_MS;
        long length = Math.max(1L, end - start);
        return Math.min(1.0F, Math.max(0.0F, (float)(positionMs - start) / length));
    }
}
