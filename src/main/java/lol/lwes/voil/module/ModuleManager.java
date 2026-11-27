package lol.lwes.voil.module;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lol.lwes.voil.api.ApiClient;
import lol.lwes.voil.config.ClientSettings;
import lol.lwes.voil.event.EventHandler;
import lol.lwes.voil.event.ShutdownEvent;
import lol.lwes.voil.module.client.ArrayList;
import lol.lwes.voil.module.client.HUD;
import lol.lwes.voil.module.client.Interface;
import lol.lwes.voil.module.client.MoveFix;
import lol.lwes.voil.module.client.Notifications;
import lol.lwes.voil.module.client.OBSBypass;
import lol.lwes.voil.module.client.Sounds;
import lol.lwes.voil.module.client.Spoofer;
import lol.lwes.voil.module.client.Targets;
import lol.lwes.voil.module.client.Vlippy;
import lol.lwes.voil.module.combat.AimAssist;
import lol.lwes.voil.module.combat.AttributeSwap;
import lol.lwes.voil.module.combat.AutoCharge;
import lol.lwes.voil.module.combat.AutoClicker;
import lol.lwes.voil.module.combat.AutoCombo;
import lol.lwes.voil.module.combat.AutoCrossbow;
import lol.lwes.voil.module.combat.AutoDrain;
import lol.lwes.voil.module.combat.AutoJumpReset;
import lol.lwes.voil.module.combat.AutoPot;
import lol.lwes.voil.module.combat.AutoRefill;
import lol.lwes.voil.module.combat.AutoStun;
import lol.lwes.voil.module.combat.AutoWater;
import lol.lwes.voil.module.combat.AutoWeb;
import lol.lwes.voil.module.combat.Backtrack;
import lol.lwes.voil.module.combat.BedMacro;
import lol.lwes.voil.module.combat.BowAssist;
import lol.lwes.voil.module.combat.BowTrigger;
import lol.lwes.voil.module.combat.CartMacro;
import lol.lwes.voil.module.combat.FastBow;
import lol.lwes.voil.module.combat.FastEat;
import lol.lwes.voil.module.combat.Hitboxes;
import lol.lwes.voil.module.combat.KnockbackDisplacement;
import lol.lwes.voil.module.combat.MaceSwap;
import lol.lwes.voil.module.combat.Piercing;
import lol.lwes.voil.module.combat.Reach;
import lol.lwes.voil.module.combat.Triggerbot;
import lol.lwes.voil.module.crystalpvp.AutoAnchor;
import lol.lwes.voil.module.crystalpvp.AutoCrystal2;
import lol.lwes.voil.module.crystalpvp.AutoDoubleHand;
import lol.lwes.voil.module.crystalpvp.AutoHitCrystal;
import lol.lwes.voil.module.crystalpvp.AutoTotem;
import lol.lwes.voil.module.crystalpvp.CrystalOptimizer;
import lol.lwes.voil.module.crystalpvp.KeyPearl;
import lol.lwes.voil.module.crystalpvp.TotemHit;
import lol.lwes.voil.module.miscellaneous.AntiBot;
import lol.lwes.voil.module.miscellaneous.AutoTool;
import lol.lwes.voil.module.miscellaneous.ChestStealer;
import lol.lwes.voil.module.miscellaneous.FakePlayer;
import lol.lwes.voil.module.miscellaneous.FastUse;
import lol.lwes.voil.module.miscellaneous.FlagDetector;
import lol.lwes.voil.module.miscellaneous.Friends;
import lol.lwes.voil.module.miscellaneous.KillInsults;
import lol.lwes.voil.module.miscellaneous.NameProtect;
import lol.lwes.voil.module.miscellaneous.Plugins;
import lol.lwes.voil.module.miscellaneous.Prevent;
import lol.lwes.voil.module.miscellaneous.Proxy;
import lol.lwes.voil.module.miscellaneous.SelfDestruct;
import lol.lwes.voil.module.miscellaneous.Teams;
import lol.lwes.voil.module.movement.AutoHeadHitter;
import lol.lwes.voil.module.movement.Blink;
import lol.lwes.voil.module.movement.BridgeAssist;
import lol.lwes.voil.module.movement.FakeLag;
import lol.lwes.voil.module.movement.FastFall;
import lol.lwes.voil.module.movement.Fly;
import lol.lwes.voil.module.movement.InventoryMove;
import lol.lwes.voil.module.movement.NoJumpCooldown;
import lol.lwes.voil.module.movement.Scaffold;
import lol.lwes.voil.module.movement.SnapTap;
import lol.lwes.voil.module.movement.Speed;
import lol.lwes.voil.module.movement.Sprint;
import lol.lwes.voil.module.player.AirPotion;
import lol.lwes.voil.module.player.AntiCombo;
import lol.lwes.voil.module.player.AutoArmor;
import lol.lwes.voil.module.player.AutoBowBoost;
import lol.lwes.voil.module.player.AutoLedge;
import lol.lwes.voil.module.player.Clutch;
import lol.lwes.voil.module.player.ElytraSwap;
import lol.lwes.voil.module.player.FastMine;
import lol.lwes.voil.module.player.HotbarManager;
import lol.lwes.voil.module.player.LungeSwap;
import lol.lwes.voil.module.player.NoAttackCooldown;
import lol.lwes.voil.module.player.NoFall;
import lol.lwes.voil.module.player.NoRotate;
import lol.lwes.voil.module.player.NoSlow;
import lol.lwes.voil.module.player.PearlCatch;
import lol.lwes.voil.module.player.PearlChase;
import lol.lwes.voil.module.player.TickBase;
import lol.lwes.voil.module.player.Velocity;
import lol.lwes.voil.module.player.WindChargeMacro;
import lol.lwes.voil.module.player.WindchargeDeflect;
import lol.lwes.voil.module.render.AimDot;
import lol.lwes.voil.module.render.ArmorInfo;
import lol.lwes.voil.module.render.Arrows;
import lol.lwes.voil.module.render.BlockESP;
import lol.lwes.voil.module.render.BlurOverride;
import lol.lwes.voil.module.render.Breadcrumbs;
import lol.lwes.voil.module.render.CustomSky;
import lol.lwes.voil.module.render.ESP;
import lol.lwes.voil.module.render.Freecam;
import lol.lwes.voil.module.render.HitCircle;
import lol.lwes.voil.module.render.HotkeyHUD;
import lol.lwes.voil.module.render.ItemESP;
import lol.lwes.voil.module.render.NameTags;
import lol.lwes.voil.module.render.NoRender;
import lol.lwes.voil.module.render.PotionHUD;
import lol.lwes.voil.module.render.RadarHUD;
import lol.lwes.voil.module.render.SpeedHUD;
import lol.lwes.voil.module.render.SpotifyHUD;
import lol.lwes.voil.module.render.SwingAnimation;
import lol.lwes.voil.module.render.TargetHUD;
import lol.lwes.voil.module.render.Trajectories;
import lol.lwes.voil.util.CrashHandler;

public class ModuleManager {
    public static final ModuleManager INSTANCE = new ModuleManager();

    private final List<Module> modules = new java.util.ArrayList<>();
    private final Map<Class<? extends Module>, Module> modulesByClass = new ConcurrentHashMap<>();
    private final Map<String, Module> modulesByName = new ConcurrentHashMap<>();
    private final Map<String, List<Module>> categoryCache = new ConcurrentHashMap<>();
    private final Set<Module> enabledModules = ConcurrentHashMap.newKeySet();

    private ModuleManager() {
    }

    public void registerModules() {
        if (!modules.isEmpty()) {
            return;
        }
        register(new ArrayList(), new HUD(), new Interface(), new MoveFix(), new Notifications(), new OBSBypass(),
            new Sounds(), new Spoofer(), new Targets(), new Vlippy());
        register(new AimAssist(), new AttributeSwap(), new AutoCharge(), new AutoClicker(), new AutoCombo(),
            new AutoCrossbow(), new AutoDrain(), new AutoJumpReset(), new AutoPot(), new AutoRefill(), new AutoStun(),
            new AutoWater(), new AutoWeb(), new Backtrack(), new BedMacro(), new BowAssist(), new BowTrigger(),
            new CartMacro(), new FastBow(), new FastEat(), new Hitboxes(), new KnockbackDisplacement(), new MaceSwap(),
            new Piercing(), new Reach(), new Triggerbot());
        register(new AutoAnchor(), new AutoCrystal2(), new AutoDoubleHand(), new AutoHitCrystal(),
            new AutoTotem(), new CrystalOptimizer(), new KeyPearl(), new TotemHit());
        register(new AntiBot(), new AutoTool(), new ChestStealer(), new FakePlayer(), new FastUse(), new FlagDetector(),
            new Friends(), new KillInsults(), new NameProtect(), new Plugins(), new Prevent(), new Proxy(),
            new SelfDestruct(), new Teams());
        register(new AutoHeadHitter(), new Blink(), new BridgeAssist(), new FakeLag(), new FastFall(), new Fly(),
            new InventoryMove(), new NoJumpCooldown(), new Scaffold(), new SnapTap(), new Speed(), new Sprint());
        register(new AirPotion(), new AntiCombo(), new AutoArmor(), new AutoBowBoost(), new AutoLedge(), new Clutch(),
            new ElytraSwap(), new FastMine(), new HotbarManager(), new LungeSwap(), new NoAttackCooldown(), new NoFall(),
            new NoRotate(), new NoSlow(), new PearlCatch(), new PearlChase(), new TickBase(), new Velocity(),
            new WindChargeMacro(), new WindchargeDeflect());
        register(new AimDot(), new ArmorInfo(), new Arrows(), new BlockESP(), new BlurOverride(), new Breadcrumbs(),
            new CustomSky(), new ESP(), new Freecam(), new HitCircle(), new HotkeyHUD(), new ItemESP(), new NameTags(),
            new NoRender(), new PotionHUD(), new RadarHUD(), new SpeedHUD(), new SpotifyHUD(), new SwingAnimation(),
            new TargetHUD(), new Trajectories());
    }

    private void register(Module... toAdd) {
        for (Module module : toAdd) {
            modules.add(module);
            modulesByClass.put(module.getClass(), module);
            modulesByName.put(module.getName().toLowerCase(Locale.ROOT), module);
        }
        categoryCache.clear();
    }

    public List<Module> getModules() {
        return modules;
    }

    @SuppressWarnings("unchecked")
    public <T extends Module> T getModule(Class<T> type) {
        return (T) modulesByClass.get(type);
    }

    public Module getModuleUnchecked(Class<?> type) {
        return modulesByClass.get(type);
    }

    public Module getModuleByName(String name) {
        return name == null ? null : modulesByName.get(name.toLowerCase(Locale.ROOT));
    }

    public List<Module> getModulesByCategory(Category category) {
        String sortMode = String.valueOf(ClientSettings.moduleSortMode.getValue());
        return categoryCache.computeIfAbsent(category.name() + "|" + sortMode, key -> sortForCategory(category, sortMode));
    }

    private List<Module> sortForCategory(Category category, String sortMode) {
        List<Module> result = new java.util.ArrayList<>();
        for (Module module : modules) {
            if (module.getCategory() == category) {
                result.add(module);
            }
        }
        Comparator<Module> byName = Comparator.comparing(module -> module.getName().toLowerCase(Locale.ROOT));
        if ("Enabled".equals(sortMode)) {
            result.sort(Comparator.comparingInt((Module module) -> module.isEnabled() ? 0 : 1).thenComparing(byName));
        } else {
            result.sort(byName);
        }
        return result;
    }

    public void init() {
        clearCategoryCache();
    }

    public void clearCategoryCache() {
        categoryCache.clear();
    }

    public List<Module> getEnabledModules() {
        List<Module> enabled = new java.util.ArrayList<>();
        for (Module module : modules) {
            if (module.isEnabled()) {
                enabled.add(module);
            }
        }
        return enabled;
    }

    public Set<Module> getEnabledModuleSet() {
        return enabledModules;
    }

    public boolean isEnabled(Module module) {
        return module != null && module.isEnabled();
    }

    public boolean isEnabled(Class<? extends Module> type) {
        return isEnabled(modulesByClass.get(type));
    }

    public void setEnabled(Module module, boolean enabled) {
        if (module == null || module.isEnabled() == enabled) {
            return;
        }
        try {
            module.setEnabled(enabled);
        } catch (Throwable t) {
            CrashHandler.handleModuleCrash(module, t);
            return;
        }
        if (enabled) {
            enabledModules.add(module);
        } else {
            enabledModules.remove(module);
        }
        categoryCache.clear();
    }

    public void enable(Module module) {
        setEnabled(module, true);
    }

    public void disable(Module module) {
        setEnabled(module, false);
    }

    public void toggle(Module module) {
        setEnabled(module, !module.isEnabled());
    }

    @EventHandler
    private void onShutdown(ShutdownEvent event) {
        ApiClient.INSTANCE.shutdown();
    }
}
