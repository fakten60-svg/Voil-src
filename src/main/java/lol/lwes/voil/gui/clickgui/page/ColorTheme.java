package lol.lwes.voil.gui.clickgui.page;

import java.awt.Color;
import java.util.Objects;

public record ColorTheme(
    String name,
    String author,
    String description,
    boolean featured,
    String id,
    Color primary,
    Color secondary,
    Color background,
    Color accent,
    boolean dark,
    int version
) {

    @Override
    public final boolean equals(Object object) {
        return this == object
            || object instanceof ColorTheme colortheme1
                && this.version == colortheme1.version
                && this.dark == colortheme1.dark
                && Objects.equals(this.accent, colortheme1.accent)
                && Objects.equals(this.background, colortheme1.background)
                && Objects.equals(this.secondary, colortheme1.secondary)
                && Objects.equals(this.primary, colortheme1.primary)
                && Objects.equals(this.id, colortheme1.id)
                && this.featured == colortheme1.featured
                && Objects.equals(this.description, colortheme1.description)
                && Objects.equals(this.author, colortheme1.author)
                && Objects.equals(this.name, colortheme1.name);
    }

    @Override
    public final String toString() {
        return "ie["
            + "al="
            + this.name
            + ", am="
            + this.author
            + ", an="
            + this.description
            + ", ao="
            + this.featured
            + ", ap="
            + this.id
            + ", aq="
            + this.primary
            + ", ar="
            + this.secondary
            + ", as="
            + this.background
            + ", at="
            + this.accent
            + ", au="
            + this.dark
            + ", av="
            + this.version
            + "]";
    }

    @Override
    public final int hashCode() {
        int i = 0 + Objects.hashCode(this.name);
        i = i * 31 + Objects.hashCode(this.author);
        i = i * 31 + Objects.hashCode(this.description);
        i = i * 31 + Boolean.hashCode(this.featured);
        i = i * 31 + Objects.hashCode(this.id);
        i = i * 31 + Objects.hashCode(this.primary);
        i = i * 31 + Objects.hashCode(this.secondary);
        i = i * 31 + Objects.hashCode(this.background);
        i = i * 31 + Objects.hashCode(this.accent);
        i = i * 31 + Boolean.hashCode(this.dark);
        return i * 31 + Integer.hashCode(this.version);
    }
}
