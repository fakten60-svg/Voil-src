package lol.lwes.voil.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import dev.prestigeclient.renderlib.impl.render.resource.GlTexture;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lol.lwes.voil.gui.clickgui.ConfigEntryInfo;
import org.lwjgl.glfw.GLFW;

public final class ClientSettings {
    public static final ThemeProperty userId = new ThemeProperty("userId", "0");
    public static final ThemeProperty username = new ThemeProperty("username", "Player");
    public static final ThemeProperty userRank = new ThemeProperty("userRank", "User");
    public static final ThemeProperty menuKeybind = new ThemeProperty("menuKeybind", GLFW.GLFW_KEY_RIGHT_SHIFT);
    public static final Map<String, CloudConfig> cloudConfigs = new LinkedHashMap<>();
    public static final Map<String, ConfigEntryInfo> cloudScripts = new LinkedHashMap<>();
    public static final Map<String, ThemePreset> themePresets = new LinkedHashMap<>();
    public static final ThemeProperty searchBarEnabled = new ThemeProperty("searchBar", true);
    public static final ThemeProperty customMainMenu = new ThemeProperty("customMainMenu", true);
    public static final ThemeProperty glowEnabled = new ThemeProperty("glow", true);
    public static final ThemeProperty translucentSearchBar = new ThemeProperty("translucentSearchBar", false);
    public static final ThemeProperty syncVisuals = new ThemeProperty("syncVisuals", false);
    public static final ThemeProperty searchSettings = new ThemeProperty("searchSettings", true);
    public static final ThemeProperty fontName = new ThemeProperty("font", "Inter");
    public static final ThemeProperty activeTheme = new ThemeProperty("theme", "Default");
    public static final ThemeProperty openSpeed = new ThemeProperty("openSpeed", 250.0F);
    public static final ThemeProperty closeSpeed = new ThemeProperty("closeSpeed", 200.0F);
    public static final ThemeProperty easingType = new ThemeProperty("easing", "Ease Out Smooth");
    public static final ThemeProperty mainMenuBackground = new ThemeProperty("mainMenuBackground", "Gradient");
    public static final ThemeProperty menuKeyMode = new ThemeProperty("menuKeyMode", "Menu");
    public static final ThemeProperty moduleSortMode = new ThemeProperty("moduleSort", "Alphabetical");
    public static final ThemeProperty customBackground = new ThemeProperty("customBackground", false);
    public static final ThemeProperty backgroundType = new ThemeProperty("backgroundType", "Blur");
    public static final ThemeProperty guiScale = new ThemeProperty("guiScale", 100.0F);
    public static final ThemeProperty hudScale = new ThemeProperty("hudScale", 100.0F);
    public static final ThemeProperty followMcScale = new ThemeProperty("followMcScale", true);
    public static Object G;
    public static String userAvatarUrl;
    public static String activeAvatarUrl;
    public static GlTexture userAvatarTexture;
    public static GlTexture activeAvatarTexture;

    private static final List<ThemeProperty> PROPERTIES = List.of(
        menuKeybind, searchBarEnabled, customMainMenu, glowEnabled, translucentSearchBar, syncVisuals, searchSettings, fontName, activeTheme,
        openSpeed, closeSpeed, easingType, mainMenuBackground, menuKeyMode, moduleSortMode, customBackground,
        backgroundType, guiScale, hudScale, followMcScale
    );

    private ClientSettings() {
    }

    public static void loadFromJson(JsonObject json) {
        for (ThemeProperty property : PROPERTIES) {
            JsonElement element = json.get(property.getName());
            if (element == null || !element.isJsonPrimitive()) {
                continue;
            }
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            Object current = property.getValue();
            if (current instanceof Boolean && primitive.isBoolean()) {
                property.setValue(primitive.getAsBoolean());
            } else if (current instanceof Integer && primitive.isNumber()) {
                property.setValue(primitive.getAsInt());
            } else if (current instanceof Float && primitive.isNumber()) {
                property.setValue(primitive.getAsFloat());
            } else if (current instanceof String && primitive.isString()) {
                property.setValue(primitive.getAsString());
            }
        }
    }

    public static JsonElement toJson() {
        JsonObject json = new JsonObject();
        for (ThemeProperty property : PROPERTIES) {
            Object value = property.getValue();
            if (value instanceof Boolean b) {
                json.addProperty(property.getName(), b);
            } else if (value instanceof Number n) {
                json.addProperty(property.getName(), n);
            } else if (value != null) {
                json.addProperty(property.getName(), value.toString());
            }
        }
        return json;
    }
}
