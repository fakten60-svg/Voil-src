package lol.lwes.voil.gui.clickgui;

import java.awt.Color;
import java.util.Objects;

public record ChangelogEntry(String title, Color startColor, Color endColor, String description, String module) {
    @Override
    public final boolean equals(Object object) {
        return this == object
            || object instanceof ChangelogEntry changelogentry1
                && Objects.equals(this.module, changelogentry1.module)
                && Objects.equals(this.description, changelogentry1.description)
                && Objects.equals(this.endColor, changelogentry1.endColor)
                && Objects.equals(this.startColor, changelogentry1.startColor)
                && Objects.equals(this.title, changelogentry1.title);
    }

    @Override
    public final String toString() {
        return "h8[" + "j=" + this.title + ", k=" + this.startColor + ", l=" + this.endColor + ", m=" + this.description + ", n=" + this.module + "]";
    }

    @Override
    public final int hashCode() {
        int i = 0 + Objects.hashCode(this.title);
        i = i * 31 + Objects.hashCode(this.startColor);
        i = i * 31 + Objects.hashCode(this.endColor);
        i = i * 31 + Objects.hashCode(this.description);
        return i * 31 + Objects.hashCode(this.module);
    }
}
