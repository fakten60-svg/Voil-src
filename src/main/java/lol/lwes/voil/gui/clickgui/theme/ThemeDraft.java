package lol.lwes.voil.gui.clickgui.theme;

import java.awt.Color;

public class ThemeDraft {
    public String name = "";
    public String description = "";
    public String id;
    public boolean isPublic;
    public boolean glow;
    public Color windowColor = new Color(24, 24, 30);
    public Color sidebarColor = new Color(30, 30, 36);
    public Color settingsBackground = new Color(24, 24, 30);
    public Color settingsHeader = new Color(167, 139, 250);
    public Color moduleDisabledBackground = new Color(30, 30, 36);
    public Color moduleBackground = new Color(30, 30, 36);
    public Color moduleText = Color.WHITE;
    public Color secondaryText = new Color(122, 124, 134);
    public Color normalText = Color.WHITE;
    public Color modeBackground = new Color(30, 30, 36);
    public Color modeFill = new Color(167, 139, 250);
    public Color sliderBackground = new Color(24, 24, 30);
    public Color outlineColor = new Color(167, 139, 250);
    public Color accentColor1 = new Color(167, 139, 250);
    public Color accentColor2 = new Color(196, 181, 253);
    public Color enabledColor = new Color(196, 181, 253);
    public float[] radii = new float[]{6.0F, 6.0F, 4.0F, 6.0F};
    public float borderThickness = 1.5F;

    public boolean isNew() {
        return this.id == null;
    }

    public ThemeDraft copy() {
        ThemeDraft copy = new ThemeDraft();
        copy.name = this.name;
        copy.description = this.description;
        copy.id = this.id;
        copy.isPublic = this.isPublic;
        copy.glow = this.glow;
        copy.windowColor = this.windowColor;
        copy.sidebarColor = this.sidebarColor;
        copy.settingsBackground = this.settingsBackground;
        copy.settingsHeader = this.settingsHeader;
        copy.moduleDisabledBackground = this.moduleDisabledBackground;
        copy.moduleBackground = this.moduleBackground;
        copy.moduleText = this.moduleText;
        copy.secondaryText = this.secondaryText;
        copy.normalText = this.normalText;
        copy.modeBackground = this.modeBackground;
        copy.modeFill = this.modeFill;
        copy.sliderBackground = this.sliderBackground;
        copy.outlineColor = this.outlineColor;
        copy.accentColor1 = this.accentColor1;
        copy.accentColor2 = this.accentColor2;
        copy.enabledColor = this.enabledColor;
        copy.radii = this.radii.clone();
        copy.borderThickness = this.borderThickness;
        return copy;
    }
}
