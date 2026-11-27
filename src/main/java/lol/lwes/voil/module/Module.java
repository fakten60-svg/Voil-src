package lol.lwes.voil.module;

import dev.prestigeclient.renderlib.impl.api.RenderAPI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.event.ModuleDisableEvent;
import lol.lwes.voil.event.ModuleEnableEvent;
import lol.lwes.voil.setting.KeybindSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.CrashHandler;
import lol.lwes.voil.util.MemoryWiper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Module {
    protected static MinecraftClient mc = MinecraftClient.getInstance();
    private final List<Setting> settings = new ArrayList<>();
    private final List<ModuleTag> tags;
    private String name;
    private String description;
    private String suffix;
    private KeybindSetting keybindSetting;
    private boolean enabled;
    private Category category;
    private boolean expanded;
    private boolean hidden;
    private boolean binding;
    public static final Logger LOGGER = LoggerFactory.getLogger("Voil/CrashHandler");

    protected Module() {
        this.tags = new ArrayList<>();
    }

    public Module(String s, String s1) {
        this.tags = new ArrayList<>();
        this.name = s;
        this.description = s1;
    }

    public Module(String s, String s1, Category category) {
        this.tags = new ArrayList<>();
        this.name = s;
        this.description = s1;
        this.category = category;
        this.keybindSetting = new KeybindSetting("K&e@y#b$i%n^d*", 0, true, "K&e@y#b$i%n^d* T@o# a!c^t*i)v_a(t*e)-_ m!o^d*u(l)e+");
    }

    protected void sendMessage(String s) {
        mc.player.sendMessage(Text.of(s.replace("&", "§")), false);
    }

    public boolean isBinding() {
        return this.binding;
    }

    public void setBinding(boolean flag) {
        this.binding = flag;
    }

    public void forceDisable() {
        this.enabled = false;
        VoilClient.eventBus.unregister(this);
        VoilClient.eventBus.post(new ModuleDisableEvent(this));
    }

    public List<ModuleTag> getTags() {
        return this.tags;
    }

    public static boolean isNotInGame() {
        return Objects.isNull(mc.world);
    }

    public Category getCategory() {
        return this.category;
    }

    public void setDescription(String s) {
        this.description = s;
    }

    public static boolean canInteract() {
        return !isNotInGame() ? mc.player.isUsingItem() : false;
    }

    public void setName(String s) {
        this.name = s;
    }

    public String getName() {
        return this.name.replaceAll("[&@#$%!\\^\\*\\(\\)\\-_+=]", "");
    }

    public List<Setting> getSettings() {
        return this.settings;
    }

    public void reload() {
        Module module1 = this;

        try {
            module1.onDisable();
        } catch (Throwable throwable1) {
            CrashHandler.handleModuleCrash(this, throwable1);
        }

        try {
            this.onEnable();
        } catch (Throwable throwable) {
            CrashHandler.handleModuleCrash(this, throwable);
        }
    }

    public void addSettings(Setting[] asetting) {
        this.settings.addAll(Arrays.asList(asetting));
    }

    public void onTick() {
    }

    public void addTags(ModuleTag[] amoduletag) {
        this.tags.addAll(Arrays.asList(amoduletag));
    }

    public void toggle() {
        this.setEnabled(!this.isEnabled());
    }

    protected void setup(String s, String s1, Category category) {
        this.name = s;
        this.description = s1;
        this.category = category;
        this.keybindSetting = new KeybindSetting("K&e@y#b$i%n^d*", 0, true, "K&e@y#b$i%n^d* T@o# a!c^t*i)v_a(t*e)-_ m!o^d*u(l)e+");
    }

    public void onRender2D() {
    }

    public String getSuffix() {
        return String.valueOf(this.suffix);
    }

    public String getDescription() {
        return this.description.replaceAll("[&@#$%!\\^\\*\\(\\)\\-_+=]", "");
    }

    public void onDisable() {
    }

    public int getKeyCode() {
        return this.keybindSetting.getKey();
    }

    public void wipeStrings() {
        String s = this.name;
        MemoryWiper.wipeString(s);
        String s1 = this.description;
        MemoryWiper.wipeString(s1);
        String s2 = this.suffix;
        MemoryWiper.wipeString(s2);
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void onEnable() {
    }

    public boolean isHidden() {
        return this.hidden;
    }

    public boolean isExpanded() {
        return this.expanded;
    }

    public void setEnabled(boolean flag) {
        if (flag == this.enabled) {
            return;
        }

        if (flag) {
            try {
                this.onEnable();
            } catch (Throwable throwable1) {
                CrashHandler.handleModuleCrash(this, throwable1);
                return;
            }

            this.enabled = true;
            VoilClient.eventBus.register(this);
            VoilClient.eventBus.post(new ModuleEnableEvent(this));
            VoilClient.getNotifications().addNotification("Notification", "Toggled " + this.getName() + " on");
            return;
        }

        this.enabled = false;

        try {
            this.onDisable();
        } catch (Throwable throwable) {
            CrashHandler.handleModuleCrash(this, throwable);
        }

        VoilClient.eventBus.unregister(this);
        VoilClient.eventBus.post(new ModuleDisableEvent(this));
        VoilClient.getNotifications().addNotification("Notification", "Toggled " + this.getName() + " off");
    }

    public void setKeyCode(int i) {
        this.keybindSetting.setKey(i);
    }

    public void setHidden(boolean flag) {
        this.hidden = flag;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public void setExpanded(boolean flag) {
        this.expanded = flag;
    }

    public void onWorldRender(RenderAPI.RenderContext rendercontext) {
    }
}
