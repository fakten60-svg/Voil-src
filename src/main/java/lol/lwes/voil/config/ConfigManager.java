package lol.lwes.voil.config;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.PrintStream;
import java.util.Map.Entry;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.module.ModuleManager;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.PlayerTargetUtil;

public class ConfigManager {
    private static final Gson gson = new Gson();
    private static final JsonObject root = new JsonObject();

    public static void setValue(String s, Object object) {
        switch (object) {
            case null -> root.remove(s);
            case JsonElement jsonelement -> root.add(s, jsonelement.deepCopy());
            case String string -> root.addProperty(s, string);
            case Number number -> root.addProperty(s, number);
            case Boolean bool -> root.addProperty(s, bool);
            case Character character -> root.addProperty(s, character);
            default -> root.add(s, gson.toJsonTree(object));
        }
    }

    public static int getInt(String s, int i) {
        JsonElement jsonelement = root.get(s);
        if (jsonelement != null && jsonelement.isJsonPrimitive()) {
            try {
                return jsonelement.getAsInt();
            } catch (Exception exception) {
                return i;
            }
        } else {
            return i;
        }
    }

    private static void applyModuleSettings(String s, JsonObject jsonobject) {
        ModuleManager modulemanager = ModuleManager.INSTANCE;
        Module module = modulemanager.getModules().stream().filter(module1 -> module1.getName().equals(s)).findFirst().orElse(null);
        if (module != null) {
            boolean flag = jsonobject.has("enabled") && jsonobject.get("enabled").getAsBoolean();
            module.setEnabled(flag);
            if (jsonobject.has("key")) {
                module.setKeyCode(jsonobject.get("key").getAsInt());
            }

            for (Setting setting : module.getSettings()) {
                if (setting.isPersistent()) {
                    String s1 = setting.getName();
                    if (jsonobject.has(s1)) {
                        JsonElement jsonelement = jsonobject.get(s1);
                        if (jsonelement != null) {
                            try {
                                setting.fromJson2(jsonelement);
                            } catch (Exception exception) {
                                PrintStream printstream = System.err;
                                String s6 = setting.getName();
                                String s2 = exception.getMessage();
                                String s3 = s;
                                String s4 = s6;
                                printstream.println("Failed to load setting '" + s4 + "' on module '" + s3 + "': " + s2);
                            }
                        }
                    }
                }
            }
        }
    }

    public static JsonObject serializeModules() {
        JsonObject jsonobject = new JsonObject();
        ModuleManager modulemanager = ModuleManager.INSTANCE;

        for (Module module : modulemanager.getModules()) {
            JsonObject jsonobject1 = new JsonObject();
            jsonobject1.addProperty("enabled", module.isEnabled());
            jsonobject1.addProperty("key", module.getKeyCode());

            for (Setting setting : module.getSettings()) {
                if (setting.isPersistent()) {
                    JsonElement jsonelement = setting.toJson();
                    if (jsonelement != null) {
                        jsonobject1.add(setting.getName(), jsonelement);
                    }
                }
            }

            jsonobject.add(module.getName(), jsonobject1);
        }

        return jsonobject;
    }

    public static boolean getBoolean(String s, boolean flag) {
        JsonElement jsonelement = root.get(s);
        if (jsonelement != null && jsonelement.isJsonPrimitive()) {
            try {
                return jsonelement.getAsBoolean();
            } catch (Exception exception) {
                return flag;
            }
        } else {
            return flag;
        }
    }

    public static JsonElement getElement(String s) {
        return root.get(s);
    }

    public static JsonObject getRoot() {
        JsonObject jsonobject = new JsonObject();
        jsonobject.add("settings", serializeModules());
        root.entrySet().stream().filter(entry -> !entry.getKey().equals("settings")).forEach(entry -> jsonobject.add(entry.getKey(), entry.getValue()));
        return jsonobject;
    }

    public static void loadSettings(String s) {
        try {
            JsonObject jsonobject = gson.fromJson(s, JsonObject.class);
            if (jsonobject == null) {
                return;
            }

            boolean flag = jsonobject.has("settings");
            JsonObject jsonobject1 = flag ? jsonobject.getAsJsonObject("settings") : jsonobject;
            if (jsonobject1 == null) {
                return;
            }

            jsonobject.entrySet().stream().filter(entry -> !entry.getKey().equals("settings")).filter(entry -> {
                if (!flag) {
                    Object object = entry.getKey();
                    String sx = (String)object;
                    if (!isFriendsKey(sx)) {
                        return false;
                    }
                }

                return true;
            }).forEach(entry -> root.add(entry.getKey(), entry.getValue()));
            jsonobject1.entrySet().forEach(entry -> {
                String sx = entry.getKey();

                try {
                    JsonObject jsonobjectx = entry.getValue().getAsJsonObject();
                    applyModuleSettings(sx, jsonobjectx);
                } catch (Exception exceptionx) {
                }
            });
            PlayerTargetUtil.clearFriendCache();
        } catch (Exception exception) {
            exception.printStackTrace(System.err);
        }
    }

    private static boolean isFriendsKey(String s) {
        return s.equals("friends") || s.equals("lastSeenRelease");
    }

    public static String getString(String s, String s1) {
        JsonElement jsonelement = root.get(s);
        return jsonelement != null && jsonelement.isJsonPrimitive() ? jsonelement.getAsString() : s1;
    }

    public static boolean has(String s) {
        return root.has(s);
    }
}
