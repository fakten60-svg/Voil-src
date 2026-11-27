package lol.lwes.voil.module.render;

import java.awt.Color;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.ColorSetting;
import lol.lwes.voil.setting.NumberSetting;
import lol.lwes.voil.setting.Setting;

public class BlurOverride extends Module {
    public NumberSetting strength;
    public NumberSetting saturation;
    public BooleanSetting shadow;
    public BooleanSetting outline;
    public NumberSetting outlineThickness;
    public ColorSetting outlineColor;

    public BlurOverride() {
        super("BlurOverride", "Overrides blur rectangle to your liking", Category.Render);
        this.strength = new NumberSetting("Strength", 1.0, 50.0, 16.0, 1.0, "Sets the strength of the blur effect");
        this.saturation = new NumberSetting("Saturation", 0.0, 2.0, 1.0, 0.1, "Sets the saturation of the blur effect");
        this.shadow = new BooleanSetting("Shadow", false, "Adds a shadow effect to the blur");
        this.outline = new BooleanSetting("Outline", false, "Adds an outline effect to the blur");
        this.outlineThickness = new NumberSetting("Outline Thickness", 0.1, 3.0, 1.0, 0.1, "Sets the thickness of the outline effect");
        String s2 = "Sets the color of the outline effect";
        Color color = Color.WHITE;
        this.outlineColor = new ColorSetting("Outline Color", color, s2);
        Setting[] asetting = new Setting[]{this.strength, this.saturation, this.shadow, this.outline, this.outlineThickness, this.outlineColor};
        this.addSettings(asetting);
        Setting.dependSetting(this.outlineThickness, this.outline);
        Setting.dependSetting(this.outlineColor, this.outline);
    }
}
