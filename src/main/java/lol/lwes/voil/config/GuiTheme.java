package lol.lwes.voil.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lol.lwes.voil.util.GsonHolder;
import lol.lwes.voil.util.render.AnimatedColor;
import lol.lwes.voil.util.render.ColorUtil;

public class GuiTheme {
    public static Color windowColor;
    public static Color leftPanel;
    public static Color settingsBg;
    public static Color settingsHeader;
    public static Color moduleDisabledBg;
    public static Color moduleColor;
    public static Color moduleText;
    public static Color unfocusedTextColor;
    public static Color normalTextColor;
    public static Color modeSettingBg;
    public static Color modeSettingFill;
    public static Color sliderSettingBg;
    public static Color outlineColor;
    public static Color accentColor1;
    public static Color accentColor2;
    public static boolean glowEnabled;
    public static float radiusTopLeft = 16.0F;
    public static float radiusTopRight = 6.0F;
    public static float radiusBottomRight = 4.0F;
    public static float radiusBottomLeft = 12.0F;
    public static float borderThickness = 0.5F;
    private static final Map booleanOverrides = new HashMap();
    private static final Map<String, AnimatedColor> smoothColors = new HashMap<>();
    private static final Map themeColors = new HashMap();
    private static final Set rainbowKeys = new HashSet();

    public static void setBooleanOverride(String s, boolean flag) {
        booleanOverrides.put(s, flag);
    }

    private static float[] readFloatArray(JsonObject jsonobject, String s) {
        if (jsonobject.has(s) && jsonobject.get(s).isJsonArray()) {
            try {
                return GsonHolder.GSON.fromJson(jsonobject.get(s), float[].class);
            } catch (Exception exception) {
            }
        }

        return null;
    }

    public static void reloadColors() {
        windowColor = readColor("windowColor");
        leftPanel = readColor("leftPanel");
        settingsBg = readColor("settingsBg");
        settingsHeader = readColor("settingsHeader");
        moduleDisabledBg = readColor("moduleDisabledBg");
        moduleColor = readColor("moduleColor");
        moduleText = readColor("moduleText");
        unfocusedTextColor = readColor("unfocusedTextColor");
        normalTextColor = readColor("normalTextColor");
        modeSettingBg = readColor("modeSettingBg");
        modeSettingFill = readColor("modeSettingFill");
        sliderSettingBg = readColor("sliderSettingBg");
        outlineColor = readColor("outlineColor");
        accentColor1 = readColor("accentColor1");
        accentColor2 = readColor("accentColor2");
    }

    private static Color readColor(String s) {
        Color color = rainbowKeys.contains(s) ? ColorUtil.rainbow(1) : (Color)themeColors.get(s);
        if (color == null) {
            color = Color.BLACK;
        }

        return smoothColors.computeIfAbsent(s, s1 -> new AnimatedColor()).update(color);
    }

    private static int readInt(JsonObject jsonobject, String s) {
        return jsonobject.has(s) ? jsonobject.get(s).getAsInt() : 0;
    }

    public static boolean isBlurMode() {
        return (Boolean)ClientSettings.customBackground.getValue() && "Blur".equalsIgnoreCase(String.valueOf(ClientSettings.backgroundType.getValue()));
    }

    private static void applyThemeJson(JsonObject jsonobject, String s) {
        rainbowKeys.remove(s);
        if (jsonobject.has(s)) {
            try {
                if (jsonobject.get(s).isJsonPrimitive() && jsonobject.get(s).getAsJsonPrimitive().isString()) {
                    String s1 = jsonobject.get(s).getAsString();
                    if ("rainbow".equalsIgnoreCase(s1)) {
                        rainbowKeys.add(s);
                        themeColors.put(s, ColorUtil.rainbow(1));
                        return;
                    }
                } else if (jsonobject.get(s).isJsonArray()) {
                    int[] aint = GsonHolder.GSON.fromJson(jsonobject.get(s), int[].class);
                    if (aint.length >= 3) {
                        themeColors.put(
                            s, new Color(Math.max(0, Math.min(255, aint[0])), Math.max(0, Math.min(255, aint[1])), Math.max(0, Math.min(255, aint[2])))
                        );
                        return;
                    }
                }
            } catch (JsonSyntaxException | NumberFormatException jsonsyntaxexception) {
                jsonsyntaxexception.printStackTrace();
            }
        }

        themeColors.put(s, Color.BLACK);
    }

    public static void loadTheme(String s) {
        try {
            JsonObject jsonobject = GsonHolder.GSON.fromJson(s, JsonObject.class);
            if (jsonobject == null) {
                return;
            }

            applyThemeJson(jsonobject, "windowColor");
            applyThemeJson(jsonobject, "leftPanel");
            applyThemeJson(jsonobject, "settingsBg");
            applyThemeJson(jsonobject, "settingsHeader");
            applyThemeJson(jsonobject, "moduleDisabledBg");
            applyThemeJson(jsonobject, "moduleColor");
            applyThemeJson(jsonobject, "moduleText");
            applyThemeJson(jsonobject, "unfocusedTextColor");
            applyThemeJson(jsonobject, "normalTextColor");
            applyThemeJson(jsonobject, "modeSettingBg");
            applyThemeJson(jsonobject, "modeSettingFill");
            applyThemeJson(jsonobject, "sliderSettingBg");
            applyThemeJson(jsonobject, "outlineColor");
            applyThemeJson(jsonobject, "accentColor1");
            applyThemeJson(jsonobject, "accentColor2");
            glowEnabled = readBoolean(jsonobject, "glowEnabled");
            radiusTopLeft = 16.0F;
            radiusTopRight = 6.0F;
            radiusBottomRight = 4.0F;
            radiusBottomLeft = 12.0F;
            float[] afloat = readFloatArray(jsonobject, "radius");
            if (afloat != null) {
                if (afloat.length > 0) {
                    radiusTopLeft = afloat[0];
                }

                if (afloat.length > 1) {
                    radiusTopRight = afloat[1];
                }

                if (afloat.length > 2) {
                    radiusBottomRight = afloat[2];
                }

                if (afloat.length > 3) {
                    radiusBottomLeft = afloat[3];
                }
            }

            borderThickness = readFloat(jsonobject, "borderThickness", 0.3F);
            reloadColors();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private static boolean readBoolean(JsonObject jsonobject, String s) {
        return jsonobject.has(s) && jsonobject.get(s).getAsBoolean();
    }

    public static boolean isGradientMode() {
        return (Boolean)ClientSettings.customBackground.getValue() && "Gradient".equalsIgnoreCase(String.valueOf(ClientSettings.backgroundType.getValue()));
    }

    private static float readFloat(JsonObject jsonobject, String s, float f) {
        return jsonobject.has(s) ? jsonobject.get(s).getAsFloat() : f;
    }
}
