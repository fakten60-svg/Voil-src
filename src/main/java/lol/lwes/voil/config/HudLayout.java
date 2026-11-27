package lol.lwes.voil.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import dev.prestigeclient.renderlib.impl.api.RenderAPI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.module.ModuleManager;
import lol.lwes.voil.module.client.HUD;
import lol.lwes.voil.module.render.HotkeyHUD;
import lol.lwes.voil.module.render.PotionHUD;
import lol.lwes.voil.module.render.RadarHUD;
import lol.lwes.voil.module.render.SpeedHUD;
import lol.lwes.voil.module.render.SpotifyHUD;
import lol.lwes.voil.module.render.TargetHUD;
import lol.lwes.voil.util.CrashHandler;
import lol.lwes.voil.util.render.GuiScale;
import lol.lwes.voil.util.render.RenderManager;
import lol.lwes.voil.util.render.shader.FramebufferBlitRenderer;

public class HudLayout {
    private final List<Module> modules = new ArrayList<>();
    private final Map positions = new HashMap();
    private final Map sizes = new HashMap();
    private final Map<String, Float> scales = new HashMap<>();
    private final Map modulesByName = new HashMap();

    public HudLayout() {
        this.loadDefaults();
    }

    public void setY(Module module, int i) {
        int[] aint = (int[])this.positions.get(getKey(module));
        if (aint != null) {
            aint[1] = i;
        }
    }

    public void setScale(Module module, float f) {
        this.scales.put(getKey(module), f);
    }

    private void loadDefaults() {
        TargetHUD targethud = new TargetHUD();
        this.setPosition(targethud, 300, 300);
        HUD hud = new HUD();
        this.setPosition(hud, 2, 30);
        HotkeyHUD hotkeyhud = new HotkeyHUD();
        this.setPosition(hotkeyhud, 2, 20);
        RadarHUD radarhud = new RadarHUD();
        this.setPosition(radarhud, 2, 30);
        lol.lwes.voil.module.client.ArrayList arraylist = new lol.lwes.voil.module.client.ArrayList();
        this.setPosition(arraylist, 3, 5);
        SpotifyHUD spotifyhud = new SpotifyHUD();
        this.setPosition(spotifyhud, 50, 70);
        SpeedHUD speedhud = new SpeedHUD();
        this.setPosition(speedhud, 50, 100);
        PotionHUD potionhud = new PotionHUD();
        this.setPosition(potionhud, 80, 100);
    }

    public String serialize() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject jsonobject = new JsonObject();
        Iterator iterator = this.modules.iterator();

        while (iterator.hasNext()) {
            String s = getKey((Module)iterator.next());
            int[] aint = (int[])this.positions.get(s);
            Float f = (Float)this.scales.get(s);
            if (f == null) {
                f = 1.0F;
            }

            if (aint != null) {
                JsonObject jsonobject1 = new JsonObject();
                jsonobject1.addProperty("x", aint[0]);
                jsonobject1.addProperty("y", aint[1]);
                jsonobject1.addProperty("s", f);
                jsonobject.add(s, jsonobject1);
            }
        }

        return gson.toJson(jsonobject);
    }

    public void setPosition(Module module, int i, int j) {
        this.modules.add(module);
        String s = getKey(module);
        this.positions.putIfAbsent(s, new int[]{i, j});
        this.sizes.putIfAbsent(s, new int[]{100, 20});
        this.scales.putIfAbsent(s, 1.0F);
        this.modulesByName.putIfAbsent(s, new FramebufferBlitRenderer());
    }

    public float[] getScaleRange() {
        return new float[]{0.5F, 2.0F};
    }

    public Collection getAll() {
        return this.modulesByName.values();
    }

    public int getX(Module module) {
        int[] aint = (int[])this.positions.get(getKey(module));
        return aint != null ? aint[0] : 0;
    }

    public int getWidth(Module module) {
        int[] aint = (int[])this.sizes.get(getKey(module));
        return aint != null ? aint[0] : 100;
    }

    public void setX(Module module, int i) {
        int[] aint = (int[])this.positions.get(getKey(module));
        if (aint != null) {
            aint[0] = i;
        }
    }

    private static String getKey(Module module) {
        return module.getName();
    }

    public int getHeight(Module module) {
        int[] aint = (int[])this.sizes.get(getKey(module));
        return aint != null ? aint[1] : 20;
    }

    public void addModule(Module module) {
        String s = getKey(module);

        try {
            FramebufferBlitRenderer framebufferblitrenderer = (FramebufferBlitRenderer)this.modulesByName.get(s);
            if (framebufferblitrenderer != null && this.getScale(module) == 1.0F) {
                framebufferblitrenderer = null;
            }

            if (framebufferblitrenderer != null) {
                framebufferblitrenderer.drawn = true;
            }

            module.onRender2D();
            if (framebufferblitrenderer != null) {
                int[] aint = (int[])this.positions.get(s);
                float f = this.getScale(module);
                int[] aint1 = (int[])this.positions.get(s);
                VoilClient.mc.getWindow();
                float f1 = GuiScale.getScaledWidth() * f;
                float f2 = GuiScale.getScaledHeight() * f;
                framebufferblitrenderer.scale[0] = f1;
                framebufferblitrenderer.scale[1] = f2 * -1.0F;
                float f3 = aint[0] * f;
                float f4 = aint[1] * f;
                framebufferblitrenderer.offset[0] = aint[0] - f3;
                framebufferblitrenderer.offset[1] = f2 + aint[1] - f4;
                framebufferblitrenderer.drawn = false;
                framebufferblitrenderer.useViewProj = true;
                RenderAPI.RenderContext renderapi$rendercontext = RenderManager.renderContext;
                framebufferblitrenderer.drawBuffer(renderapi$rendercontext);
                framebufferblitrenderer.useViewProj = false;
                this.positions.put(s, aint1);
            }
        } catch (Throwable throwable) {
            CrashHandler.handleModuleCrash(module, throwable);
        }
    }

    public List<Module> getModules() {
        return this.modules;
    }

    public void setSize(Module module, int i, int j) {
        this.sizes.put(getKey(module), new int[]{i, j});
    }

    public void deserialize(String s) {
        if (s != null && !s.isEmpty()) {
            JsonObject jsonobject;
            try {
                jsonobject = new Gson().fromJson(s, JsonObject.class);
            } catch (Exception exception) {
                return;
            }

            if (jsonobject != null) {
                for (String s1 : jsonobject.keySet()) {
                    if (jsonobject.get(s1).isJsonObject()) {
                        JsonObject jsonobject1 = jsonobject.getAsJsonObject(s1);
                        if (jsonobject1.has("x") && jsonobject1.has("y")) {
                            float f = !jsonobject1.has("s") ? 1.0F : jsonobject1.get("s").getAsFloat();
                            int i = jsonobject1.get("x").getAsInt();
                            int j = jsonobject1.get("y").getAsInt();
                            this.positions.put(s1, new int[]{i, j});
                            this.scales.put(s1, f);
                        }
                    }
                }
            }
        }
    }

    public int getY(Module module) {
        int[] aint = (int[])this.positions.get(getKey(module));
        return aint != null ? aint[1] : 0;
    }

    public float getScale(Module module) {
        return this.scales.getOrDefault(getKey(module), 1.0F);
    }

    public boolean isRegistered(Module module) {
        return this.modules.contains(module);
    }

    public boolean isVisible(Module module) {
        ModuleManager modulemanager = ModuleManager.INSTANCE;
        Class oclass = module.getClass();
        return modulemanager.getModule(oclass).isEnabled();
    }
}
