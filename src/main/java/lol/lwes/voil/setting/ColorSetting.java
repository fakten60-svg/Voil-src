package lol.lwes.voil.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.awt.Color;

public class ColorSetting extends Setting {
    private Color color;
    private final String label;
    private String tooltip;
    private final boolean alpha;

    public ColorSetting(String s, Color color, String s1) {
        this(s, color, s1, false);
    }

    public ColorSetting(String s, Color color, String s1, boolean flag) {
        super(s, s1);
        this.label = s;
        this.color = color != null ? color : Color.WHITE;
        this.tooltip = s1;
        this.alpha = flag;
    }

    @Override
    public String getName() {
        return this.label.replaceAll("[&@#$%!\\^\\*\\(\\)\\-_+=]", "");
    }

    public void setColor(Color color) {
        if (color != null) {
            this.color = color;
        }
    }

    @Override
    public JsonElement toJson() {
        Color colorx = this.getColor();
        if (this.alpha) {
            return new JsonPrimitive(String.format("#%08X", colorx.getRGB()));
        }

        return new JsonPrimitive(String.format("#%06X", colorx.getRGB() & 0xFFFFFF));
    }

    public boolean hasAlpha() {
        return this.alpha;
    }

    public Color getColor() {
        return this.color == null ? Color.WHITE : this.color;
    }

    public void fromJson(JsonElement jsonelement) {
        String s = jsonelement.getAsString();
        if (s.startsWith("#") && s.length() == 9) {
            Color color = new Color((int)Long.parseLong(s.substring(1), 16), true);
            this.setColor(color);
        } else {
            Color color1 = Color.decode(s);
            this.setColor(color1);
        }
    }

    @Override
    public void fromJson2(JsonElement jsonelement) {
        this.fromJson(jsonelement);
    }
}
