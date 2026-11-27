package lol.lwes.voil.util;

import java.util.List;

public record ReleaseEntry(String id, String version, List<String> added, List<String> modified, List<String> removed) {
    public ReleaseEntry {
        added = added == null ? List.of() : List.copyOf(added);
        modified = modified == null ? List.of() : List.copyOf(modified);
        removed = removed == null ? List.of() : List.copyOf(removed);
    }

    public boolean isEmpty() {
        return this.added.isEmpty() && this.modified.isEmpty() && this.removed.isEmpty();
    }

    public int changeCount() {
        return this.added.size() + this.modified.size() + this.removed.size();
    }
}
