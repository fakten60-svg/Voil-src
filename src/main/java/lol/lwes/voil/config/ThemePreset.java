package lol.lwes.voil.config;

import java.awt.Color;

public class ThemePreset {
    private final String name;
    private final String description;
    private final String category;
    private final String author;
    private final Color startColor;
    private final Color endColor;
    private final Color accentColor;
    private final Color backgroundColor;

    public ThemePreset(String s, String s1, String s2, String s3, Color color, Color color1, Color color2, Color color3) {
        this.name = s;
        this.description = s1;
        this.category = s2;
        this.author = s3;
        this.startColor = color;
        this.endColor = color1;
        this.accentColor = color2;
        this.backgroundColor = color3;
    }

    public Color getStartColor() {
        return this.startColor;
    }

    public String getAuthor() {
        return this.author;
    }

    public String getCategory() {
        return this.category;
    }

    public Color getBackgroundColor() {
        return this.backgroundColor;
    }

    public Color getEndColor() {
        return this.endColor;
    }

    public String getDescription() {
        return this.description;
    }

    public String getName() {
        return this.name;
    }

    public Color getAccentColor() {
        return this.accentColor;
    }
}
