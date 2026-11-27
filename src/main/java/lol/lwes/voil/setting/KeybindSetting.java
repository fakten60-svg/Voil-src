package lol.lwes.voil.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.module.ModuleManager;
import lol.lwes.voil.util.input.KeyUtil;
import org.lwjgl.glfw.GLFW;

public class KeybindSetting extends Setting {
    private static final Set<KeybindSetting> INSTANCES = Collections.newSetFromMap(new WeakHashMap<>());
    private static final Set<Integer> PRESSED_KEYS = new HashSet<>();
    private static final Set<Integer> PENDING_KEYS = new HashSet<>();
    private int key;
    private final int defaultKey;
    private boolean listening = false;
    private boolean toggleable;
    private boolean hold = false;
    private boolean wasPressed = false;
    private boolean released = false;
    private static final int MAX_MOUSE_BUTTON = 7;

    public KeybindSetting(String s, int i, boolean flag, String s1) {
        super(s, s1);
        this.key = i;
        this.defaultKey = i;
        this.toggleable = flag;
        INSTANCES.add(this);
    }

    public boolean wasJustPressed() {
        boolean flag1 = this.isPressed();
        boolean flag2 = this.hold ? flag1 : flag1 && !this.wasPressed;
        this.wasPressed = flag1;
        return flag2;
    }

    public void setListening(boolean flag) {
        if (flag) {
            for (KeybindSetting keybindsetting1 : INSTANCES) {
                if (keybindsetting1 != this) {
                    keybindsetting1.listening = false;
                }
            }
        }

        this.listening = flag;
    }

    public boolean isToggleable() {
        return this.toggleable;
    }

    private boolean isKeyDown(boolean flag) {
        if (!flag && VoilClient.mc.currentScreen != null) {
            return false;
        }

        long i = VoilClient.mc.getWindow().getHandle();
        if (i != 0L && VoilClient.mc.isWindowFocused()) {
            int j = this.key;
            if (j == 0) {
                return false;
            }

            int k;
            if (j < 0) {
                k = (j + 1) * -1;
            } else {
                if (!KeyUtil.isMouseButtonCode(j)) {
                    return KeyUtil.isKeyDown(j);
                }

                k = KeyUtil.getMouseButtonIndex(j);
            }

            return k >= 0 && k <= MAX_MOUSE_BUTTON ? GLFW.glfwGetMouseButton(i, k) == 1 : false;
        } else {
            return false;
        }
    }

    public boolean isPressed() {
        return this.isPressed(false);
    }

    public boolean isPressed(boolean flag) {
        if (this.hold) {
            return this.isKeyDown(flag);
        } else if (this.key == 0) {
            return false;
        } else {
            return !flag && VoilClient.mc.currentScreen != null ? false : PRESSED_KEYS.contains(this.key);
        }
    }

    public int getDefaultKey() {
        return this.defaultKey;
    }

    @Override
    public JsonElement toJson() {
        JsonObject jsonobject = new JsonObject();
        jsonobject.addProperty("name", this.getName());
        jsonobject.addProperty("key", this.key);
        jsonobject.addProperty("toggleable", this.isToggleable());
        jsonobject.addProperty("hold", this.hold);
        return jsonobject;
    }

    public int getKey() {
        return this.key;
    }

    public boolean isHold() {
        return this.hold;
    }

    public void setKey(int i) {
        this.key = i;
    }

    public void fromJson(JsonElement jsonelement) {
        JsonObject jsonobject = jsonelement.getAsJsonObject();
        if (jsonobject.has("key")) {
            this.setKey(jsonobject.get("key").getAsInt());
        }

        if (jsonobject.has("hold")) {
            this.hold = jsonobject.get("hold").getAsBoolean();
        }
    }

    public boolean isListening() {
        return this.listening;
    }

    public void setToggleable(boolean flag) {
        this.toggleable = flag;
    }

    public void setHold(boolean flag) {
        this.hold = flag;
        this.wasPressed = false;
    }

    public static KeybindSetting getListening() {
        for (KeybindSetting keybindsetting : INSTANCES) {
            if (keybindsetting.listening) {
                return keybindsetting;
            }
        }

        return null;
    }

    public static void stopListening() {
        Iterator iterator = INSTANCES.iterator();

        while (iterator.hasNext()) {
            ((KeybindSetting)iterator.next()).listening = false;
        }
    }

    public static List findConflicts(Module module) {
        return collectConflicts(module.getKeyCode(), module, null);
    }

    public static String formatConflicts(List list) {
        String s3 = String.join(", ", list);
        String s1 = list.size() == 1 ? " also has the same bind" : " also have the same bind";
        String s = s3;
        return s + s1;
    }

    public static void addPressedKey(int i) {
        PENDING_KEYS.add(i);
    }

    public static List findConflicts(Module module, KeybindSetting keybindsetting) {
        return collectConflicts(keybindsetting.getKey(), keybindsetting.isToggleable() ? module : null, keybindsetting);
    }

    private static List collectConflicts(int i, Module module, KeybindSetting keybindsetting) {
        if (i == 0) {
            return List.of();
        }

        ArrayList arraylist = new ArrayList<>();

        for (Module module1 : ModuleManager.INSTANCE.getModules()) {
            boolean flag = module1.getKeyCode() == i;
            if (flag && module1 != module) {
                String s = module1.getName();
                arraylist.add(s + " Module");
            }

            for (Setting setting : module1.getSettings()) {
                if (setting instanceof KeybindSetting keybindsetting1
                    && keybindsetting1 != keybindsetting
                    && keybindsetting1.getKey() == i
                    && (!keybindsetting1.isToggleable() || !flag)) {
                    String s5 = module1.getName();
                    String s4 = keybindsetting1.getName();
                    String s2 = s5;
                    arraylist.add(s2 + "'s " + s4);
                }
            }
        }

        return arraylist;
    }

    public static boolean isAnyListening() {
        for (KeybindSetting keybindsetting : INSTANCES) {
            if (keybindsetting.listening) {
                return true;
            }
        }

        return false;
    }

    public static void flushPressedKeys() {
        PRESSED_KEYS.clear();
        PRESSED_KEYS.addAll(PENDING_KEYS);
        PENDING_KEYS.clear();
    }

    @Override
    public void fromJson2(JsonElement jsonelement) {
        this.fromJson(jsonelement);
    }
}
