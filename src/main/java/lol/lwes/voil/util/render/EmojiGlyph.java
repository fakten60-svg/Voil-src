package lol.lwes.voil.util.render;

import java.util.Objects;

public record EmojiGlyph(
    String emoji, String name, float atlasX, float atlasY, float width, float height, float uMin, float vMin, float uMax, float vMax, float advance
) {
    public float getVMin() {
        return this.vMin;
    }

    @Override
    public final boolean equals(Object object) {
        return this == object
            || object instanceof EmojiGlyph emojiglyph1
                && Float.compare(this.advance, emojiglyph1.advance) == 0
                && Float.compare(this.vMax, emojiglyph1.vMax) == 0
                && Float.compare(this.uMax, emojiglyph1.uMax) == 0
                && Float.compare(this.vMin, emojiglyph1.vMin) == 0
                && Float.compare(this.uMin, emojiglyph1.uMin) == 0
                && Float.compare(this.height, emojiglyph1.height) == 0
                && Float.compare(this.width, emojiglyph1.width) == 0
                && Float.compare(this.atlasY, emojiglyph1.atlasY) == 0
                && Float.compare(this.atlasX, emojiglyph1.atlasX) == 0
                && Objects.equals(this.name, emojiglyph1.name)
                && Objects.equals(this.emoji, emojiglyph1.emoji);
    }

    @Override
    public final String toString() {
        return "bn["
            + "a7="
            + this.emoji
            + ", a8="
            + this.name
            + ", a9="
            + this.atlasX
            + ", a_="
            + this.atlasY
            + ", ba="
            + this.width
            + ", bb="
            + this.height
            + ", bc="
            + this.uMin
            + ", bd="
            + this.vMin
            + ", be="
            + this.uMax
            + ", bf="
            + this.vMax
            + ", bg="
            + this.advance
            + "]";
    }

    @Override
    public final int hashCode() {
        int i = 0 + Objects.hashCode(this.emoji);
        i = i * 31 + Objects.hashCode(this.name);
        i = i * 31 + Float.hashCode(this.atlasX);
        i = i * 31 + Float.hashCode(this.atlasY);
        i = i * 31 + Float.hashCode(this.width);
        i = i * 31 + Float.hashCode(this.height);
        i = i * 31 + Float.hashCode(this.uMin);
        i = i * 31 + Float.hashCode(this.vMin);
        i = i * 31 + Float.hashCode(this.uMax);
        i = i * 31 + Float.hashCode(this.vMax);
        return i * 31 + Float.hashCode(this.advance);
    }

    public float getWidth() {
        return this.width;
    }

    public float getHeight() {
        return this.height;
    }

    public float getUMax() {
        return this.uMax;
    }

    public float getUMin() {
        return this.uMin;
    }

    public String getEmoji() {
        return this.emoji;
    }

    public float getAtlasX() {
        return this.atlasX;
    }

    public float getAtlasY() {
        return this.atlasY;
    }

    public String getName() {
        return this.name;
    }

    public float getVMax() {
        return this.vMax;
    }

    public float getAdvance() {
        return this.advance;
    }
}
