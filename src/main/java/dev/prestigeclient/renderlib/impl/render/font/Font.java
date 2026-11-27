package dev.prestigeclient.renderlib.impl.render.font;

import dev.prestigeclient.renderlib.api.font.MSDFFontAtlas;
import java.awt.Color;
import java.util.Objects;

public class Font {
    private final MSDFFontAtlas atlas;
    private final float baseSize;
    private float size;
    protected float lastScale = -1.0F;

    public Font(MSDFFontAtlas atlas, float size) {
        if (size <= 0.0F) {
            throw new IllegalArgumentException("font size must be positive, got " + size);
        }

        this.atlas = Objects.requireNonNull(atlas, "atlas");
        this.baseSize = size;
        this.size = size;
    }

    public void setScale(float scale) {
        this.lastScale = scale;
        if (scale <= 0.0F) {
            this.size = this.baseSize;
            return;
        }

        float pixels = Math.max(1.0F, Math.round(this.baseSize * scale));
        this.size = pixels / scale;
    }

    public float getScale() {
        return this.lastScale;
    }

    public float getBaseSize() {
        return this.baseSize;
    }

    public float getSize() {
        return this.size;
    }

    public MSDFFontAtlas getAtlas() {
        return this.atlas;
    }

    public void render(String text, float x, float y, int color) {
        this.atlas.render(text, x, y, this.size, color);
    }

    public void render(String text, float x, float y, Color color) {
        this.render(text, x, y, color.getRGB());
    }

    public void renderWithShadow(String text, float x, float y, int color) {
        this.atlas.renderWithShadow(text, x, y, this.size, color);
    }

    public void renderCentered(String text, float centerX, float y, int color) {
        this.render(text, centerX - this.getWidth(text) * 0.5F, y, color);
    }

    public void renderRight(String text, float rightX, float y, int color) {
        this.render(text, rightX - this.getWidth(text), y, color);
    }

    public float getWidth(String text) {
        return this.atlas.getWidth(text, this.size);
    }

    public float getLineHeight() {
        return this.atlas.getLineHeight(this.size);
    }

    public String truncate(String text, float maxWidth) {
        return this.atlas.truncate(text, maxWidth, this.size);
    }
}
