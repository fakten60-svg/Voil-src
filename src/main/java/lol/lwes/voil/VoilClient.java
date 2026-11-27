package lol.lwes.voil;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import lol.lwes.voil.api.ApiClient;
import lol.lwes.voil.config.HudLayout;
import lol.lwes.voil.event.DefaultEventBus;
import lol.lwes.voil.event.EventBus;
import lol.lwes.voil.gui.NotificationManager;
import lol.lwes.voil.input.KeybindHandler;
import lol.lwes.voil.module.ModuleManager;
import lol.lwes.voil.script.ScriptManager;
import lol.lwes.voil.util.CrashReporter;
import lol.lwes.voil.util.PlayerTargetUtil;
import lol.lwes.voil.util.ReleaseNotes;
import lol.lwes.voil.util.SoundPlayer;
import lol.lwes.voil.util.TickFastForward;
import lol.lwes.voil.util.combat.SpinAttackController;
import lol.lwes.voil.util.font.FontManager;
import lol.lwes.voil.util.input.KeySimulator;
import lol.lwes.voil.util.input.MouseSimulator;
import lol.lwes.voil.util.inventory.SwapScheduler;
import lol.lwes.voil.util.media.MediaController;
import lol.lwes.voil.util.packet.ReceivedPacketQueue;
import lol.lwes.voil.util.packet.SentPacketQueue;
import lol.lwes.voil.util.render.ShaderRegistry;
import lol.lwes.voil.util.rotation.RotationManager;
import lol.lwes.voil.util.rotation.SmoothRotationController;
import lol.lwes.voil.util.target.TargetManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.Packet;
import net.minecraft.resource.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VoilClient implements ModInitializer {
    public static final String NAME = "Voil";
    public static final String COMMAND_PREFIX = ".";
    public static final Logger LOGGER = LoggerFactory.getLogger(NAME);
    public static final List<Packet<?>> silentPackets = new ArrayList<>();

    public static VoilClient instance;
    public static MinecraftClient mc;
    public static EventBus eventBus;
    public static FontManager fonts;

    public HudLayout hudLayout;
    public RotationManager rotationManager;
    public SmoothRotationController rotationController;
    public NotificationManager notifications;
    public SoundPlayer soundPlayer;
    public PlayerTargetUtil playerTargets;
    public ScriptManager scriptManager;
    public KeybindHandler keybindHandler;
    public ShaderRegistry shaderRegistry;
    public MediaController mediaController;
    public ReleaseNotes releaseNotes;
    public SpinAttackController spinAttackController;
    public boolean disabled;

    private MouseSimulator mouseSimulator;
    private KeySimulator keySimulator;

    @Override
    public void onInitialize() {
        instance = this;
        mc = MinecraftClient.getInstance();
        eventBus = new DefaultEventBus();
        eventBus.registerLookupProvider("lol.lwes", (lookupIn, owner) -> (MethodHandles.Lookup) lookupIn.invoke(null, owner, MethodHandles.lookup()));

        fonts = new FontManager();
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(fonts);
        ShaderRegistry.registerReloadListener();

        hudLayout = new HudLayout();
        rotationManager = new RotationManager();
        rotationController = new SmoothRotationController();
        notifications = new NotificationManager();
        soundPlayer = new SoundPlayer();
        playerTargets = new PlayerTargetUtil();
        scriptManager = new ScriptManager();
        keybindHandler = new KeybindHandler();
        shaderRegistry = new ShaderRegistry();
        mediaController = new MediaController();
        releaseNotes = new ReleaseNotes();
        spinAttackController = new SpinAttackController();
        mouseSimulator = new MouseSimulator();
        keySimulator = new KeySimulator();

        eventBus.register(keybindHandler);
        eventBus.register(notifications);
        eventBus.register(soundPlayer);
        eventBus.register(rotationManager);
        eventBus.register(rotationController);
        eventBus.register(spinAttackController);
        eventBus.register(mouseSimulator);
        eventBus.register(keySimulator);
        eventBus.register(new SwapScheduler());
        eventBus.register(new ReceivedPacketQueue());
        eventBus.register(new SentPacketQueue());
        eventBus.registerStatic(TickFastForward.class);
        eventBus.registerStatic(TargetManager.class);
        eventBus.registerStatic(CrashReporter.class);

        ModuleManager.INSTANCE.registerModules();
        eventBus.register(ModuleManager.INSTANCE);
        ApiClient.INSTANCE.start();
        ApiClient.INSTANCE.loadSettings();
        LOGGER.info("{} loaded {} modules", NAME, ModuleManager.INSTANCE.getModules().size());
    }

    public static void sendSilentPacket(Packet<?> packet) {
        if (mc.getNetworkHandler() == null) {
            return;
        }
        silentPackets.add(packet);
        mc.getNetworkHandler().sendPacket(packet);
    }

    public static String getCommandPrefix() {
        return COMMAND_PREFIX;
    }

    public static VoilClient getInstance() {
        return instance;
    }

    public static HudLayout getHudLayout() {
        return instance.hudLayout;
    }

    public static RotationManager getRotationManager() {
        return instance.rotationManager;
    }

    public static SmoothRotationController getRotationController() {
        return instance.rotationController;
    }

    public static NotificationManager getNotifications() {
        return instance.notifications;
    }

    public static SoundPlayer getSoundPlayer() {
        return instance.soundPlayer;
    }

    public static PlayerTargetUtil getPlayerTargets() {
        return instance.playerTargets;
    }

    public static ScriptManager getScriptManager() {
        return instance.scriptManager;
    }

    public static MediaController getMediaController() {
        return instance.mediaController;
    }

    public static ReleaseNotes getReleaseNotes() {
        return instance.releaseNotes;
    }

    public static SpinAttackController getSpinAttackController() {
        return instance.spinAttackController;
    }

    public static MouseSimulator getMouseSimulator() {
        return instance.mouseSimulator;
    }

    public static KeySimulator getKeySimulator() {
        return instance.keySimulator;
    }
}
