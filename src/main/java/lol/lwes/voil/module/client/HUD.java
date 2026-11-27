package lol.lwes.voil.module.client;

import dev.prestigeclient.renderlib.api.font.MSDFFontAtlas;
import dev.prestigeclient.renderlib.impl.render.resource.GlTexture;
import java.awt.Color;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.api.ApiClient;
import lol.lwes.voil.config.ClientSettings;
import lol.lwes.voil.config.CloudConfig;
import lol.lwes.voil.config.GuiTheme;
import lol.lwes.voil.config.HudLayout;
import lol.lwes.voil.event.AttackEntityPostEvent;
import lol.lwes.voil.event.EventHandler;
import lol.lwes.voil.event.ModuleDisableEvent;
import lol.lwes.voil.event.ModuleEnableEvent;
import lol.lwes.voil.event.MousePressEvent;
import lol.lwes.voil.event.PacketReceiveEvent;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.module.ModuleManager;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.ListSetting;
import lol.lwes.voil.setting.ModeSetting;
import lol.lwes.voil.setting.MultiSelectSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.SystemUtil;
import lol.lwes.voil.util.font.IconGlyphs;
import lol.lwes.voil.util.media.MediaController;
import lol.lwes.voil.util.render.EasingType;
import lol.lwes.voil.util.render.GradientFontEffect;
import lol.lwes.voil.util.render.IndexedFontEffect;
import lol.lwes.voil.util.render.Render2DUtil;
import lol.lwes.voil.util.render.TextureRectangleRenderer;
import lol.lwes.voil.util.render.UiAnimation;
import lol.lwes.voil.util.world.EntityUtil;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.util.Window;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.query.QueryPingC2SPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.OverlayMessageS2CPacket;
import net.minecraft.network.packet.s2c.query.PingResultS2CPacket;
import org.lwjgl.glfw.GLFW;

public class HUD extends Module {
    public BooleanSetting glow = new BooleanSetting("Glow", true, "Enable glow effect");
    public ModeSetting hudStyle = new ModeSetting(
        "Hud Style", "Voil", "Style of the hud", new String[]{"Voil", "Voil 2", "Gamesense", "Dynamic Island", "Glass"}
    );
    public MultiSelectSetting islandStates;
    public BooleanSetting showAlbumArt;
    public BooleanSetting hoverExpand;
    public MultiSelectSetting expandWidgets;
    public ListSetting expandOrder;
    public ModeSetting toggleSource;
    public ModeSetting expandScale;
    private static final List<String> WIDGET_NAMES = List.of("Media", "Performance", "Session", "Server", "Toggles", "Configs");
    private static final float[] EMPTY_LAYOUT = new float[5];
    private static final Pattern QUEUE_PATTERN = Pattern.compile(
        "Queued for\\s+(.+?)\\s*(?:\\(\\s*([^)]*?)(?:\\s+[\\u2012-\\u2015\\-]\\s+([^)]*?))?\\s*\\))?\\s*$", 2
    );
    private static final Pattern FORMAT_CODE_PATTERN = Pattern.compile("§.");
    private static final Pattern TRAILING_PUNCT_PATTERN = Pattern.compile("[.\\s]+$");
    private static final DateTimeFormatter CLOCK_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final Color ACCENT_LIGHT = new Color(96, 165, 250);
    private static final Color ACCENT = new Color(59, 130, 246);
    private static final Color ACCENT_DARK = new Color(37, 99, 235);
    private static final Color ISLAND_BACKGROUND = new Color(24, 23, 29);
    private static final Color MUTED_TEXT = new Color(118, 103, 133);
    private static final Color PANEL_COLOR = new Color(45, 45, 45);
    private static final Color PANEL_COLOR_DARK = PANEL_COLOR.darker();
    private static final Color PANEL_BORDER = new Color(60, 60, 60);
    private static final Color SPOTIFY_GREEN = new Color(30, 185, 84);
    private volatile long pingMs;
    private volatile long lastPingSentAt;
    private int islandState;
    private String lastToggledName;
    private boolean lastToggledState;
    private long stateChangeTime;
    private AbstractClientPlayerEntity targetPlayer;
    private long lastAttackTime;
    private boolean targetTracked;
    private volatile String queueName;
    private volatile String queueDetail;
    private volatile long queueStartTime;
    private volatile long lastQueueUpdate;
    private final UiAnimation[] stateAnimations;
    private final UiAnimation[] expandAnimations;
    private final UiAnimation islandWidthAnim;
    private final UiAnimation islandHeightAnim;
    private final UiAnimation islandRadiusAnim;
    private final UiAnimation hoverAnim;
    private final UiAnimation expandAnim;
    private final UiAnimation trackAnim;
    private final UiAnimation mediaAnim;
    private final UiAnimation toggleSpringAnim;
    private final UiAnimation toggleStateAnim;
    private final Map<String, UiAnimation> chipAnimations;
    private final Set usedChipKeys;
    private float mouseX;
    private float mouseY;
    private boolean screenOpen;
    private boolean mouseHeld;
    private float[] clickBounds;
    private final List clickRegions;
    private boolean seekBarValid;
    private float seekBarX;
    private float seekBarWidth;
    private float seekBarY;
    private float seekBarHeight;
    private long trackDurationMs;
    private boolean seeking;
    private float seekProgress;
    private long seekHoldUntil;
    private boolean mediaPlaying;
    private long playingDebounceUntil;
    private int islandX;
    private int islandY;
    private int islandWidth;
    private int islandHeight;
    private boolean hovered;
    private boolean expanded;
    private long hoverStartTime;
    private int lastIslandWidth;
    private int lastIslandHeight;
    private float uiScale;
    private final String[] segmentNames;
    private final float[] segmentValues;
    private int segmentCount;
    private float expandedHeight;
    private final List<Module> recentModules;
    private List<Module> toggleModules;
    private List<String> toggleLabels;
    private List<CloudConfig> configs;
    private List<String> configNames;
    private float[] toggleLayout;
    private float[] configLayout;
    private List<String> cachedToggleLabels;
    private List<String> cachedConfigNames;
    private float cachedLayoutWidth;
    private float cachedLayoutScale;
    private String pendingConfig;
    private long pendingConfigTime;
    private String loadedConfig;
    private long loadedConfigTime;
    private long lastConfigRefreshTime;
    private Object networkHandler;
    private long sessionStartTime;
    private int kills;
    private int deaths;
    private boolean wasDead;
    private static final long HOVER_GRACE_MS = 300L;
    private static final long STATE_TIMEOUT_MS = 5000L;
    private static final long FIGHT_TIMEOUT_MS = 3000L;
    private static final long CONFIRM_WINDOW_MS = 3000L;
    private static final long LOADED_HINT_MS = 2000L;

    public HUD() {
        super("HUD", "Hud elements", Category.Client);
        List list = List.of("Music", "Notifications", "Queue");
        this.islandStates = MultiSelectSetting.create(
            "Island States", "Which states to show on Dynamic Island", list, new String[]{"Music", "Fighting", "Notifications", "Queue"}
        );
        this.showAlbumArt = new BooleanSetting("Show Album Art", true, "Show album art on Dynamic Island music display");
        this.hoverExpand = new BooleanSetting("Hover Expand", true, "Expand the island into a widget panel when hovered in chat or inventory");
        List list1 = List.of("Media", "Performance", "Session", "Toggles", "Configs");
        this.expandWidgets = MultiSelectSetting.create(
            "Expand Widgets", "Which widgets the expanded island shows", list1, new String[]{"Media", "Performance", "Session", "Server", "Toggles", "Configs"}
        );
        this.expandOrder = new ListSetting(
            "Expand Order",
            "Drag to arrange the widgets on the expanded island",
            new String[]{"Media", "Performance", "Session", "Server", "Toggles", "Configs"}
        );
        BooleanSetting booleansetting = null;
        this.toggleSource = new ModeSetting(
            "Toggle Source",
            "Recent",
            "Which modules the Toggles widget lists",
            new String[]{"Recent", "Combat", "Movement", "Render", "Player", "Miscellaneous", "CrystalPvP"}
        );
        this.expandScale = new ModeSetting("Expand Scale", "85%", "Size of the expanded island", new String[]{"40%", "50%", "60%", "75%", "85%", "100%"});
        this.pingMs = -1L;
        this.islandState = 0;
        this.lastToggledName = "";
        this.stateChangeTime = -1L;
        this.queueName = "";
        this.queueStartTime = -1L;
        this.lastQueueUpdate = -1L;
        this.stateAnimations = new UiAnimation[]{
            new UiAnimation(EasingType.FLUID_RESPONSIVE),
            new UiAnimation(EasingType.FLUID_RESPONSIVE),
            new UiAnimation(EasingType.FLUID_RESPONSIVE),
            new UiAnimation(EasingType.FLUID_RESPONSIVE),
            new UiAnimation(EasingType.FLUID_RESPONSIVE)
        };
        this.expandAnimations = new UiAnimation[]{
            new UiAnimation(EasingType.FLUID_RESPONSIVE), new UiAnimation(EasingType.FLUID_RESPONSIVE), new UiAnimation(EasingType.FLUID_RESPONSIVE)
        };
        this.islandWidthAnim = new UiAnimation(EasingType.FLUID_SMOOTH);
        this.islandHeightAnim = new UiAnimation(EasingType.FLUID_SMOOTH);
        this.islandRadiusAnim = new UiAnimation(EasingType.FLUID_SMOOTH);
        this.hoverAnim = new UiAnimation(EasingType.FLUID_SMOOTH);
        this.expandAnim = new UiAnimation(EasingType.FLUID_SMOOTH);
        this.trackAnim = new UiAnimation(EasingType.FLUID_SMOOTH);
        this.mediaAnim = new UiAnimation(EasingType.FLUID_SMOOTH);
        this.toggleSpringAnim = new UiAnimation(EasingType.FLUID_SPRING).setFluidParams(0.6, 0.25);
        this.toggleStateAnim = new UiAnimation(EasingType.FLUID_SMOOTH).setFluidParams(0.7, 0.15);
        this.chipAnimations = new HashMap<>();
        this.usedChipKeys = new HashSet();
        this.clickBounds = new float[64];
        this.clickRegions = new java.util.ArrayList();
        this.seekHoldUntil = -1L;
        this.playingDebounceUntil = -1L;
        this.hoverStartTime = -1L;
        this.uiScale = 0.85F;
        this.segmentNames = new String[WIDGET_NAMES.size()];
        int i = WIDGET_NAMES.size();
        this.segmentValues = new float[i * 4];
        this.recentModules = new java.util.ArrayList<>();
        this.toggleModules = List.of();
        this.toggleLabels = List.of();
        this.configs = List.of();
        this.configNames = List.of();
        this.toggleLayout = EMPTY_LAYOUT;
        this.configLayout = EMPTY_LAYOUT;
        this.cachedLayoutWidth = -1.0F;
        this.cachedLayoutScale = -1.0F;
        this.sessionStartTime = System.currentTimeMillis();
        Setting[] asetting = new Setting[]{
            this.glow,
            this.hudStyle,
            this.showAlbumArt,
            this.islandStates,
            this.hoverExpand,
            this.expandWidgets,
            this.expandOrder,
            this.toggleSource,
            this.expandScale
        };
        this.addSettings(asetting);
        Setting.dependAny(this.islandStates, this.hudStyle, new String[]{"Dynamic Island"});
        Setting.dependAny(this.expandWidgets, this.hudStyle, new String[]{"Dynamic Island"});
        Setting.dependAny(this.expandOrder, this.hudStyle, new String[]{"Dynamic Island"});
        Setting.dependAny(this.toggleSource, this.hudStyle, new String[]{"Dynamic Island"});
        Setting.dependAny(this.expandScale, this.hudStyle, new String[]{"Dynamic Island"});
        booleansetting = this.showAlbumArt;
        BooleanSupplier booleansupplier = () -> this.hudStyle.is("Dynamic Island");
        booleansetting.setVisibleWhen(booleansupplier);
        booleansetting = this.hoverExpand;
        BooleanSupplier booleansupplier1 = () -> this.hudStyle.is("Dynamic Island");
        booleansetting.setVisibleWhen(booleansupplier1);
    }

    private void updateExpandState(long i, MediaController mediacontroller) {
        boolean flag;
        boolean flag1;
        flag = this.hoverExpand.isEnabled();
        flag1 = this.screenOpen && this.islandWidth > 0 && (flag || this.islandStates.isSelected("Music") && !mediacontroller.isIdle());
        label119:
        if (flag1) {
            if (!this.seeking) {
                float f1 = this.islandX - 2.0F;
                float f = this.islandY - 2.0F;
                if (!this.isHovered(f1, f, this.islandWidth + 4.0F, this.islandHeight + 4.0F)) {
                    break label119;
                }
            }

            this.hoverStartTime = i;
        }

        boolean flag2 = flag1 && this.hoverStartTime > 0L && i - this.hoverStartTime <= HOVER_GRACE_MS;
        if (flag2 && !this.hovered && !this.expanded) {
            this.lastIslandWidth = this.islandWidth;
            this.lastIslandHeight = this.islandHeight;
            if (flag) {
                this.openPanel();
            }
        }

        if (!flag2) {
            this.seeking = false;
            this.pendingConfig = null;
        }

        this.hovered = flag2 && !flag;
        this.expanded = flag2 && flag;
        if (this.hovered) {
            this.stateChangeTime = 0L;
            this.islandState = 1;
        } else if (this.islandState != 3 || i - this.stateChangeTime >= STATE_TIMEOUT_MS) {
            this.stateChangeTime = 0L;
            boolean flag3 = this.islandStates.isSelected("Fighting")
                && this.targetPlayer != null
                && this.targetPlayer.isAlive()
                && i - this.lastAttackTime < FIGHT_TIMEOUT_MS;
            boolean flag4 = this.islandStates.isSelected("Queue") && this.lastQueueUpdate > 0L && i - this.lastQueueUpdate < STATE_TIMEOUT_MS;
            boolean flag5 = this.islandStates.isSelected("Music") && !mediacontroller.isIdle() && mediacontroller.isPlaying();
            if (flag3) {
                this.islandState = 2;
            } else if (flag4) {
                this.islandState = 3;
            } else if (flag5) {
                this.islandState = 1;
            } else {
                this.islandState = 0;
            }
        }
    }

    private String getTargetText() {
        if (this.targetPlayer == null) {
            return "?  0.0hp";
        }

        float f = this.targetPlayer.getHealth() + this.targetPlayer.getAbsorptionAmount();
        String s3 = this.targetPlayer.getName().getString();
        String s2 = String.format("%.1f", f);
        String s = s3;
        return s + "  " + s2 + "hp";
    }

    private long getPing() {
        long i = this.pingMs;
        long j;
        if (i >= 0L) {
            j = i;
        } else {
            ClientPlayerEntity clientplayerentity = mc.player;
            j = EntityUtil.getPing(clientplayerentity);
        }

        return j;
    }

    private void drawControlButton(int i, String s, float f, float f1, float f2, float f3, int j) {
        float f4 = f2 + f3;
        float f5 = f - f4 / 2.0F;
        float f6 = f1 - f4 / 2.0F;
        boolean flag = (this.hovered || this.expanded) && j > 100;
        boolean flag1 = flag && !this.seeking && this.isHovered(f5, f6, f4, f4);
        if (flag) {
            this.registerClickArea(f5, f6, f4, f4, () -> this.handleControlClick(i));
        }

        UiAnimation uianimation = this.expandAnimations[i];
        double d0 = flag1 ? 1.0 : 0.0;
        uianimation.setTarget(d0);
        uianimation.update();
        float f7 = clamp01(uianimation.getFloat());
        if (f7 > 0.01F) {
            float f11 = f4 / 2.0F;
            int k = scaleAlpha(j, (int)(40.0F * f7));
            Render2DUtil.drawRoundedRect(f5, f6, f4, f4, f11, 1.0F, whiteWithAlpha(k));
        }

        int l = scaleAlpha(j, 170 + (int)(85.0F * f7));
        int i1 = alphaToArgb(l);
        float f8 = f2;
        float f9 = f1;
        float f10 = f;
        String s1 = s;
        this.drawCenteredText(s1, f10, f9, f8, i1);
    }

    private float getIslandWidth() {
        return Math.max(this.scale(264.0F), this.lastIslandWidth);
    }

    private void openPanel() {
        Object object = null;
        java.util.ArrayList<Module> arraylist = new java.util.ArrayList<>();
        String s = this.toggleSource.getMode();
        if (s.equals("Recent")) {
            arraylist.addAll(this.recentModules);
            ModuleManager modulemanager = ModuleManager.INSTANCE;

            for (Module module : modulemanager.getModules()) {
                if (arraylist.size() >= 12) {
                    break;
                }

                if (module != this && module.isEnabled() && !arraylist.contains(module)) {
                    arraylist.add(module);
                }
            }
        } else {
            ModuleManager modulemanager1 = ModuleManager.INSTANCE;

            for (Module module1 : modulemanager1.getModules()) {
                if (module1 != this && module1.getCategory() != null && module1.getCategory().name().equals(s)) {
                    arraylist.add(module1);
                }
            }
        }

        this.toggleModules = arraylist;
        this.toggleLabels = arraylist.stream().map(module2 -> String.valueOf(module2.getName())).toList();
        this.refreshConfigs();
        this.pendingConfig = null;
    }

    private void addWidgetSlot(String s, float f, float f1, float f2, float f3) {
        int j = this.segmentCount;
        int i = j * 4;
        this.segmentNames[this.segmentCount] = s;
        this.segmentValues[i] = f;
        this.segmentValues[i + 1] = f1;
        this.segmentValues[i + 2] = f2;
        this.segmentValues[i + 3] = f3;
        j = this.segmentCount++;
    }

    private float[] computeWidgetLayout(List<String> list, float f, int i) {
        MSDFFontAtlas msdffontatlas = VoilClient.fonts.getSelectedFont();
        float f1 = this.scale(9.0F);
        float f2 = this.scale(6.0F);
        float f3 = this.scale(4.0F);
        float f4 = this.getRowHeight();
        float f5 = f4 + f3;
        float f6 = msdffontatlas.getWidth("+99", f1) + f2 * 2.0F;
        int j2 = list.size() * 3;
        float[] afloat = new float[5 + j2];
        int j = 0;
        int k = 0;
        int l = 0;
        int i1 = 0;
        int j1 = -1;
        float f7 = 0.0F;
        float f8 = 0.0F;

        for (int k1 = 0; k1 < list.size(); k1++) {
            float f9 = Math.min(f, msdffontatlas.getWidth((String)list.get(k1), f1) + f2 * 2.0F);
            if (f7 > 0.0F && f7 + f9 > f) {
                l++;
                f7 = 0.0F;
            }

            boolean flag1;
            label42: {
                if (l == i - 1) {
                    int i2 = list.size();
                    if (k1 < i2 - 1 && f7 + f9 + f3 + f6 > f) {
                        flag1 = true;
                        break label42;
                    }
                }

                flag1 = false;
            }

            boolean flag = flag1;
            if (l >= i || flag) {
                int l2 = list.size();
                k = l2 - k1;
                if (l < i && f7 + f6 <= f) {
                    f8 = f7;
                    i1 = l;
                }

                j1 = Math.max(j1, i1);
                break;
            }

            j2 = j * 3;
            int l1 = 5 + j2;
            afloat[l1] = f7;
            afloat[l1 + 1] = l * f5;
            afloat[l1 + 2] = f9;
            j++;
            f7 += f9 + f3;
            f8 = f7;
            i1 = l;
            j1 = l;
        }

        int k2 = j1 + 1;
        afloat[0] = j;
        afloat[1] = k;
        afloat[2] = f8;
        afloat[3] = i1 * f5;
        afloat[4] = k2 == 0 ? 0.0F : k2 * f4 + (k2 - 1) * f3;
        return afloat;
    }

    @Override
    public void onTick() {
        if (!Module.isNotInGame()) {
            long i = System.currentTimeMillis();
            ClientPlayNetworkHandler clientplaynetworkhandler = mc.getNetworkHandler();
            if (clientplaynetworkhandler != this.networkHandler) {
                this.networkHandler = clientplaynetworkhandler;
                this.sessionStartTime = i;
                this.kills = 0;
                this.deaths = 0;
                this.wasDead = false;
                this.targetPlayer = null;
                this.targetTracked = false;
                this.lastQueueUpdate = -1L;
            }

            boolean flag = mc.player.isDead();
            if (flag && !this.wasDead) {
                int j = this.deaths++;
            }

            this.wasDead = flag;
            if (this.targetTracked) {
                if (i - this.lastAttackTime > 5000L) {
                    this.targetTracked = false;
                } else if (this.targetPlayer.isDead()) {
                    int k = this.kills++;
                    this.targetTracked = false;
                }
            }

            if (!SystemUtil.isLinux() && this.hudStyle.is("Dynamic Island")) {
                VoilClient.getMediaController().update();
            }

            if (i - this.lastPingSentAt > 1000L && mc.getNetworkHandler() != null) {
                this.lastPingSentAt = i;
                mc.getNetworkHandler().sendPacket(new QueryPingC2SPacket(i));
            }
        }
    }

    @Override
    public void onRender2D() {
        if (mc.player != null && mc.world != null) {
            int i = VoilClient.getHudLayout().getX(this);
            int j = VoilClient.getHudLayout().getY(this);
            switch (this.hudStyle.getMode()) {
                case "Voil":
                    this.renderVoilStyle(i, j);
                    break;
                case "Voil 2":
                    this.renderVoil2Style(i, j);
                    break;
                case "Gamesense":
                    this.renderGamesenseStyle(i, j);
                    break;
                case "Glass":
                    this.renderGlassStyle(i, j);
                    break;
                case "Dynamic Island":
                    this.renderDynamicIsland(i, j);
            }
        }
    }

    private void drawCenteredText(String s, float f, float f1, float f2, int i) {
        MSDFFontAtlas msdffontatlas = VoilClient.fonts.getIcons();
        float f3 = msdffontatlas.getWidth(s, f2);
        float f4 = msdffontatlas.getLineHeight(f2);
        msdffontatlas.render(s, f - f3 / 2.0F, f1 - f4 / 2.0F, f2, i);
    }

    private void drawExpandedWidgets(float f, float f1, int i, MediaController mediacontroller, long j) {
        MSDFFontAtlas msdffontatlas = VoilClient.fonts.getSelectedFont();
        boolean flag = this.expanded && i > 0;
        this.usedChipKeys.clear();

        for (int k = 0; k < this.segmentCount; k++) {
            int l = k * 4;
            float f2 = f + this.segmentValues[l];
            float f3 = f1 + this.segmentValues[l + 1];
            float f4 = this.segmentValues[l + 2];
            float f5 = this.segmentValues[l + 3];
            float f6 = f2 + this.scale(8.0F);
            float f7 = f3 + this.scale(18.0F);
            float f12 = this.scale(10.0F);
            int i1 = scaleAlpha(i, 10);
            Render2DUtil.drawRoundedRect(f2, f3, f4, f5, f12, 1.0F, whiteWithAlpha(i1));
            switch (this.segmentNames[k]) {
                case "Media":
                    this.drawMediaWidget(f2, f3, f4, f5, i, flag, mediacontroller, j);
                    break;
                case "Performance":
                    this.drawInfoCard("PERFORMANCE", null, f2, f3, f4, i);
                    float f10 = f6 + this.drawLabeledValue(f6, f7, String.valueOf(mc.getCurrentFps()), "fps", i) + this.scale(10.0F);
                    this.drawLabeledValue(f10, f7, String.valueOf(this.getPing()), "ms", i);
                    break;
                case "Server":
                    this.drawInfoCard("SERVER", null, f2, f3, f4, i);
                    ServerInfo serverinfo = mc.getCurrentServerEntry();
                    String s1 = serverinfo != null && serverinfo.address != null ? serverinfo.address : "Singleplayer";
                    String s = this.truncateText(s1, f4 - this.scale(8.0F) * 2.0F, this.scale(11.0F));
                    float f11 = f3 + this.scale(19.0F);
                    f12 = this.scale(11.0F);
                    int j1 = scaleAlpha(i, 235);
                    msdffontatlas.render(s, f6, f11, f12, alphaToArgb(j1));
                    break;
                case "Session":
                    this.drawInfoCard("SESSION", formatDuration(j - this.sessionStartTime), f2, f3, f4, i);
                    float f8 = this.deaths == 0 ? this.kills : (float)this.kills / this.deaths;
                    float f9 = f6 + (this.drawLabeledValue(f6, f7, String.valueOf(this.kills), "K", i) + this.scale(8.0F));
                    f9 += this.drawLabeledValue(f9, f7, String.valueOf(this.deaths), "D", i) + this.scale(8.0F);
                    this.drawLabeledValue(f9, f7, String.format("%.1f", f8), "KD", i);
                    break;
                case "Toggles":
                    this.drawChipTile(f2, f3, f4, i, flag, false);
                    break;
                case "Configs":
                    this.drawChipTile(f2, f3, f4, i, flag, true);
            }
        }

        this.chipAnimations.keySet().retainAll(this.usedChipKeys);
    }

    private void parseQueueMessage(String s) {
        Matcher matcher = QUEUE_PATTERN.matcher(FORMAT_CODE_PATTERN.matcher(s).replaceAll(""));
        if (matcher.find()) {
            long i = System.currentTimeMillis();
            if (this.lastQueueUpdate < 0L || i - this.lastQueueUpdate >= 2500L) {
                this.queueStartTime = i;
            }

            this.queueName = TRAILING_PUNCT_PATTERN.matcher(matcher.group(1).replace("…", "")).replaceAll("");
            this.queueDetail = matcher.group(2);
            this.lastQueueUpdate = i;
        }
    }

    private void renderGlassStyle(int i, int j) {
        MSDFFontAtlas msdffontatlas = VoilClient.fonts.getInterBold();
        MSDFFontAtlas msdffontatlas1 = VoilClient.fonts.getSelectedFont();
        MSDFFontAtlas msdffontatlas2 = VoilClient.fonts.getMedia();
        Color color = GuiTheme.accentColor1;
        String s = String.valueOf(ClientSettings.userRank.getValue());
        String s1 = String.valueOf(mc.getCurrentFps());
        String s10 = String.valueOf(ClientSettings.username.getValue());
        String s8 = String.valueOf(ClientSettings.userId.getValue());
        String s4 = s10;
        String s2 = s4 + " · uid " + s8;
        long i1 = this.getPing();
        String s9 = LocalTime.now().format(CLOCK_FORMAT);
        long k = i1;
        String s3 = k + " ms · " + s9;
        float f = msdffontatlas.getWidth("Voil ", 10.0F);
        float f1 = msdffontatlas.getWidth(s1, 10.0F);
        float f2 = msdffontatlas.getWidth(s1 + " ", 10.0F) + msdffontatlas1.getWidth("fps", 10.0F);
        float f3 = msdffontatlas1.getWidth(s2, 9.0F);
        float f4 = msdffontatlas1.getWidth(s3, 9.0F);
        float f5 = Math.max(f + msdffontatlas1.getWidth(s, 9.0F) + f2 + 30.0F, f3 + f4 + 20.0F);
        float f6 = 20.0F + 8.0F + f5 + 10.0F * 2.0F;
        float f20 = i;
        float f22 = j;
        float f15 = 34;
        float f16 = f6;
        float f17 = f22;
        float f18 = f20;
        Render2DUtil.drawBlurSimple(f18, f17, f16, f15, 10.0F, 40.0F);
        float f21 = i;
        float f19 = j;
        Render2DUtil.drawOutline(f21, f19, f6, 34, 10.0F, 1.0F, 0.5F, whiteWithAlpha(25));
        float f7 = i + 10.0F;
        float f8 = j + (34 - 20.0F) / 2.0F;
        Render2DUtil.drawRoundedRect(f7, f8, 20.0F, 20.0F, 6.0F, 1.0F, withAlpha(color, 38));
        Render2DUtil.drawOutline(f7, f8, 20.0F, 20.0F, 6.0F, 1.0F, 0.5F, withAlpha(color, 50));
        float f9 = f7 + (20.0F - msdffontatlas2.getWidth(IconGlyphs.iconLetterE(), 26.0F)) / 2.0F;
        float f10 = f8 + (20.0F - msdffontatlas.getLineHeight(26.0F)) / 2.0F + 3.0F;
        msdffontatlas2.render(IconGlyphs.iconLetterE(), f9, f10, 26.0F, color.getRGB());
        float f11 = f7 + 20.0F + 8.0F;
        float f12 = j + 5.0F;
        float f13 = i + f6 - 10.0F;
        float f14 = f12 + msdffontatlas.getLineHeight(10.0F) + 1.0F;
        msdffontatlas.render("Voil", f11, f12, 10.0F, -1);
        msdffontatlas1.render(s, f11 + f, f12 + 1.0F, 9.0F, alphaToArgb(76));
        msdffontatlas.render(s1, f13 - f2, f12, 10.0F, color.getRGB());
        msdffontatlas1.render(" fps", f13 - f2 + f1, f12, 10.0F, alphaToArgb(76));
        msdffontatlas1.render(s2, f11, f14, 9.0F, alphaToArgb(102));
        msdffontatlas1.render(s3, f13 - f4, f14, 9.0F, alphaToArgb(64));
        HudLayout hudlayout = VoilClient.getHudLayout();
        int l = (int)f6;
        HUD hud1 = this;
        hudlayout.setSize(hud1, l, 34);
    }

    private void drawChip(String s, String s1, float f, float f1, float f2, int i, boolean flag, boolean flag1, boolean flag2) {
        MSDFFontAtlas msdffontatlas = VoilClient.fonts.getSelectedFont();
        this.usedChipKeys.add(s);
        UiAnimation uianimation = this.chipAnimations.computeIfAbsent(s, s3 -> new UiAnimation(EasingType.FLUID_RESPONSIVE));
        double d0 = flag ? 1.0 : 0.0;
        uianimation.setTarget(d0);
        uianimation.update();
        float f8 = clamp01(uianimation.getFloat());
        float f9 = this.getRowHeight();
        float f10 = this.scale(9.0F);
        float f11 = this.scale(6.0F);
        boolean flag3 = flag2 && !this.seeking && this.isHovered(f, f1, f2, f9);
        float f17 = f9 / 2.0F;
        int l = scaleAlpha(i, flag3 ? 34 : 18);
        Color color = whiteWithAlpha(l);
        float f3 = 1.0F;
        float f4 = f17;
        float f5 = f9;
        float f6 = f2;
        float f7 = f1;
        Render2DUtil.drawRoundedRect(f, f7, f6, f5, f4, f3, color);
        if (f8 > 0.01F) {
            int j = (int)(i * 0.55F * f8);
            float f15 = Math.round(f2);
            float f16 = (int)f9;
            f17 = f9 / 2.0F;
            Color color2 = withAlpha(GuiTheme.accentColor1, j);
            Color color1 = withAlpha(GuiTheme.accentColor2, j);
            Render2DUtil.drawRoundedRectGradientVertical(f, f1, f15, f16, f17, 1.0F, color2, color1, 25);
        }

        if (flag1) {
            Render2DUtil.drawOutline(f, f1, f2, f9, f9 / 2.0F, 1.0F, 0.5F, withAlpha(GuiTheme.accentColor1, i));
        }

        float f13 = Math.max(f8, flag3 ? 0.6F : 0.0F);
        String s2 = this.truncateText(s1, f2 - f11 * 2.0F + 1.0F, f10);
        float f12 = f1 + (f9 - msdffontatlas.getLineHeight(f10)) / 2.0F;
        float f14 = f + f11;
        int k = (int)(105.0F * f13);
        int i1 = scaleAlpha(i, 150 + k);
        msdffontatlas.render(s2, f14, f12, f10, alphaToArgb(i1));
    }

    private static String formatTrackTime(long millis) {
        long seconds = Math.max(0L, millis / 1000L);
        return String.format("%d:%02d", seconds / 60L, seconds % 60L);
    }

    private void renderGamesenseStyle(int i, int j) {
        MSDFFontAtlas msdffontatlas = VoilClient.fonts.getSelectedFont();
        String[] astring1 = new String[]{"Voil", "|", LocalTime.now().format(CLOCK_FORMAT), "|", null, null, null};
        int j2 = mc.getCurrentFps();
        astring1[4] = j2 + "FPS";
        astring1[5] = "|";
        String s2 = String.valueOf(ClientSettings.userId.getValue());
        astring1[6] = "UID " + s2;
        String[] astring = astring1;
        float[] afloat = new float[astring.length];
        float f = 0.0F;

        for (int k = 0; k < astring.length; k++) {
            afloat[k] = msdffontatlas.getWidth(astring[k]);
            f += afloat[k];
        }

        float f9 = Math.max(3.0F, (125.0F - f) / 6.0F);
        float f1 = Math.max(140.0F, f + f9 * 6.0F + 15.0F);
        float f2 = f1 - 5.0F;
        int l = (int)(i + 2.5F);
        int i1 = j + 2;
        float f5 = j;
        Render2DUtil.drawRoundedRect(i, f5, f1, 25.0F, 0.0F, 1.0F, PANEL_COLOR_DARK);
        float f6 = j;
        Render2DUtil.drawOutline(i, f6, f1, 25.0F, 1.0F, 1.0F, 0.3F, PANEL_COLOR);
        float f7 = i1;
        Render2DUtil.drawRoundedRect(l, f7, f2, 20.0F, 0.0F, 1.0F, PANEL_COLOR);
        float f8 = i1;
        Render2DUtil.drawOutline(l, f8, f2, 20.0F, 1.0F, 1.0F, 0.3F, PANEL_BORDER);
        Render2DUtil.drawRectGradient(
            l + 1, i1 + 1, f2 - 2.0F, 1.0F, 0.0F, 1.0F, GuiTheme.accentColor1, GuiTheme.accentColor1, GuiTheme.accentColor2, GuiTheme.accentColor2
        );
        float f3 = l + 5.0F;
        int i2 = (int)(i1 + (20.0F - msdffontatlas.getLineHeight()) / 2.0F);
        int j1 = i2 + 2;
        int k1 = GuiTheme.accentColor1.brighter().getRGB();

        for (int l1 = 0; l1 < astring.length; l1++) {
            float f4 = astring[l1].equals("|") ? j1 - 1 : j1;
            msdffontatlas.render(astring[l1], f3, f4, 10.0F, l1 == 0 ? k1 : -1);
            f3 += afloat[l1] + f9;
        }

        HudLayout hudlayout = VoilClient.getHudLayout();
        int k2 = (int)f1;
        HUD hud1 = this;
        hudlayout.setSize(hud1, k2, 25);
    }

    private static int scaleAlpha(int i, int j) {
        return Math.max(0, Math.min(j, (int)(j * i / 255.0F)));
    }

    private void renderDynamicIsland(int i, int j) {
        long k = System.currentTimeMillis();
        MediaController mediacontroller = VoilClient.getMediaController();
        MSDFFontAtlas msdffontatlas = VoilClient.fonts.getSelectedFont();
        Window window = mc.getWindow();
        this.screenOpen = mc.currentScreen instanceof ChatScreen || mc.currentScreen instanceof HandledScreen;
        this.mouseX = (float)(mc.mouse.getX() / window.getScaleFactor());
        this.mouseY = (float)(mc.mouse.getY() / window.getScaleFactor());
        if (this.seeking && GLFW.glfwGetMouseButton(window.getHandle(), 0) != 1) {
            this.seeking = false;
        }

        this.seekBarValid = false;
        this.clickRegions.clear();
        this.updateExpandState(k, mediacontroller);
        this.uiScale = Integer.parseInt(this.expandScale.getMode().replace("%", "")) / 100.0F;

        for (int l = 0; l < this.stateAnimations.length; l++) {
            UiAnimation uianimation = this.stateAnimations[l];
            double d0 = l == this.islandState && !this.expanded ? 255.0 : 0.0;
            uianimation.setTarget(d0);
            this.stateAnimations[l].update();
        }

        double d1 = this.hovered ? 1.0 : 0.0;
        this.hoverAnim.setTarget(d1);
        this.hoverAnim.update();
        double d2 = this.expanded ? 1.0 : 0.0;
        this.expandAnim.setTarget(d2);
        this.expandAnim.update();
        float f24 = clamp01(this.hoverAnim.getFloat());
        float f = clamp01(this.expandAnim.getFloat());
        boolean flag = this.islandState == 4;
        boolean flag1 = this.islandState == 1 && this.showAlbumArt.isEnabled() && !mediacontroller.isIdle() && mediacontroller.getCover() != null;
        boolean flag2 = flag || flag1;
        float f1 = flag2 ? 28.0F : msdffontatlas.getWidth("Voil", 11.0F);
        float f2 = this.islandState == 1 ? 30.0F : 24.0F;
        float f3 = (flag2 ? f2 + 2.0F : 12.0F + f1 + 16.0F) + this.getStateWidth(mediacontroller, k) + 12.0F;
        if (this.hovered) {
            f3 = Math.max(f3 + 50.0F, this.lastIslandWidth);
            f2 = Math.max(f2, this.lastIslandHeight);
        }

        boolean flag3 = this.expanded || f > 0.01F;
        if (flag3) {
            this.measureExpandedSize(mediacontroller);
        }

        double d3 = this.expanded ? this.getIslandWidth() : f3;
        this.islandWidthAnim.setTarget(d3);
        double d4 = this.expanded ? Math.max(this.expandedHeight, this.lastIslandHeight) : f2;
        this.islandHeightAnim.setTarget(d4);
        double d5 = f3;
        this.islandRadiusAnim.setTarget(d5);
        this.islandWidthAnim.update();
        this.islandHeightAnim.update();
        this.islandRadiusAnim.update();
        int i1 = this.islandWidthAnim.getRounded();
        int j1 = this.islandHeightAnim.getRounded();
        float f4 = j1 / 2.0F + (Math.min(j1 / 2.0F, this.scale(22.0F)) - j1 / 2.0F) * f;
        float f26 = i;
        int j2 = this.islandRadiusAnim.getRounded();
        float f5 = f26 + (j2 - i1) / 2.0F;
        j2 = window.getScaledWidth();
        float f6 = Math.max(2.0F, Math.min(f5, j2 - i1 - 2.0F));
        float f7 = Math.max(2.0F, Math.min(j, window.getScaledHeight() - j1 - 2.0F));
        int l4 = Math.round(f5 + (f6 - f5) * f);
        int k4 = Math.round(j + (f7 - j) * f);
        f26 = l4;
        float f30 = k4;
        float f15 = i1;
        Render2DUtil.drawBlurRoundedTinted(f26, f30, f15, j1, f4, 24.0F, 1.3F, Color.black);
        boolean flag4 = f < 0.12F;
        float f8 = k4 + j1 / 2.0F;
        float f9 = f8 - msdffontatlas.getLineHeight() / 2.0F;
        float f10;
        if (flag2) {
            if (flag4 && flag) {
                int k1 = this.stateAnimations[4].getRounded();
                float f11 = j1 - 2.0F;
                float f12 = 0.5F + 0.5F * (float)Math.sin(k % 2000L / 2000.0 * Math.PI * 2.0);
                f26 = l4 + 1;
                float f16 = k4 + 1;
                Render2DUtil.drawRoundedRect(f26, f16, f11, f11, f11 / 2.0F, 8.0F, withAlpha(ACCENT, (int)(k1 * (0.2F + 0.25F * f12))));
                f26 = l4 + 1;
                f30 = k4 + 1;
                float f31 = (int)f11;
                float f32 = (int)f11;
                float f33 = f11 / 2.0F;
                Color color1 = withAlpha(ACCENT_LIGHT, k1);
                Color color = withAlpha(ACCENT_DARK, k1);
                Render2DUtil.drawRoundedRectGradientVertical(f26, f30, f31, f32, f33, 1.0F, color1, color, 25);
                String s3 = IconGlyphs.iconE1E0();
                f31 = l4 + 1 + f11 / 2.0F;
                f32 = k4 + 1 + f11 / 2.0F;
                f33 = f11 * 0.55F;
                int i3 = alphaToArgb(k1);
                float f17 = f33;
                float f18 = f32;
                float f19 = f31;
                String s2 = s3;
                this.drawCenteredText(s2, f19, f18, f17, i3);
            } else if (flag4) {
                GlTexture gltexture = mediacontroller.getCover();
                f30 = l4 + 1;
                float f20 = k4 + 1;
                Render2DUtil.drawGlTexture(gltexture, f30, f20, j1 + -3 + 1, j1 + -3 + 1, (j1 - 2) / 2.0F, 0.7F, Color.WHITE);
            }

            f10 = l4 + j1 + 2.0F;
        } else {
            f10 = l4 + 12.0F;
            if (flag4) {
                GradientFontEffect gradientfonteffect = new GradientFontEffect(GuiTheme.accentColor1, GuiTheme.accentColor2, 2.0F, new float[]{f1});
                IndexedFontEffect indexedfonteffect = new IndexedFontEffect(gradientfonteffect);
                indexedfonteffect.resetIndex();
                msdffontatlas.renderWithEffect("Voil", f10, f9, indexedfonteffect);
                f26 = f10 + f1 + 7.0F;
                float f21 = f8 - 5.0F;
                Render2DUtil.drawRoundedRect(f26, f21, 1.0F, 10.0F, 0.5F, 1.0F, whiteWithAlpha(20));
            }

            f10 += f1 + 16.0F;
        }

        int i5 = this.stateAnimations[0].getRounded();
        int j5 = this.stateAnimations[1].getRounded();
        int k5 = this.stateAnimations[2].getRounded();
        int l1 = this.stateAnimations[3].getRounded();
        int i2 = this.stateAnimations[4].getRounded();
        if (i5 > 2) {
            String s4 = this.getFpsPingText();
            int j3 = scaleAlpha(i5, 165);
            msdffontatlas.render(s4, f10, f9, 10.0F, alphaToArgb(j3));
        }

        if (j5 > 2) {
            this.drawTrackInfo(mediacontroller, f10, f8, l4, k4, i1, j1, j5, f24, k);
        }

        if (k5 > 2 && this.targetPlayer != null && this.targetPlayer.isAlive()) {
            String s5 = this.getTargetText();
            int k3 = scaleAlpha(k5, 210);
            msdffontatlas.render(s5, f10, f9, 10.0F, alphaToArgb(k3));
        }

        if (l1 > 2) {
            float f22 = f10 + 2.0F;
            this.drawToggleIndicator(f22, f8, f9, l1, k);
        }

        if (i2 > 2) {
            float f13 = msdffontatlas.getWidth("Queued  ", 10.0F);
            String s = this.queueName;
            float f14 = msdffontatlas.getWidth(s + "  ", 10.0F);
            int l3 = scaleAlpha(i2, 230);
            msdffontatlas.render("Queued", f10, f9, 10.0F, alphaToArgb(l3));
            String s6 = this.queueName;
            float f27 = f10 + f13;
            int i4 = scaleAlpha(i2, 120);
            msdffontatlas.render(s6, f27, f9, 10.0F, alphaToArgb(i4));
            msdffontatlas.render(this.getQueueText(k), f10 + f13 + f14, f9, 10.0F, withAlpha(ACCENT_LIGHT, i2).getRGB());
        }

        if (flag3) {
            f26 = l4;
            float f23 = k4;
            float f28 = i1;
            float f29 = j1;
            int j4 = (int)(22.0F * f);
            Render2DUtil.drawOutline(f26, f23, f28, f29, f4, 1.0F, 0.5F, whiteWithAlpha(j4));
            float f25 = clamp01((Math.min(i1 / this.getIslandWidth(), j1 / this.expandedHeight) - 0.8F) / 0.2F);
            int l5 = (int)(255.0F * f25 * f);
            if (l5 > 2) {
                this.drawExpandedWidgets(l4 + (i1 - this.getIslandWidth()) / 2.0F, k4, l5, mediacontroller, k);
            }
        }

        if (f > 0.01F) {
            HudLayout hudlayout = VoilClient.getHudLayout();
            int i6 = (int)f3;
            int k2 = (int)f2;
            int l2 = i6;
            HUD hud1 = this;
            hudlayout.setSize(hud1, l2, k2);
        } else {
            VoilClient.getHudLayout().setSize(this, i1, j1);
        }

        this.islandX = l4;
        this.islandY = k4;
        this.islandWidth = i1;
        this.islandHeight = j1;
    }

    private static String defaultIfEmpty(String s, String s1) {
        return s != null && !s.isEmpty() ? s : s1;
    }

    private static boolean isInside(float f, float f1, float f2, float f3, float f4, float f5) {
        return f >= f2 && f <= f2 + f4 && f1 >= f3 && f1 <= f3 + f5;
    }

    private float scale(float f) {
        return f * this.uiScale;
    }

    private void loadConfig(CloudConfig cloudconfig) {
        long i = System.currentTimeMillis();
        String s = cloudconfig.getFileName();
        if (s.equals(this.pendingConfig) && i - this.pendingConfigTime <= CONFIRM_WINDOW_MS) {
            ApiClient apiclient = ApiClient.INSTANCE;
            String s1 = s.replace(".json", "");
            apiclient.getConfig(s1);
            this.loadedConfig = s;
            this.loadedConfigTime = i;
            this.pendingConfig = null;
        } else {
            this.pendingConfig = s;
            this.pendingConfigTime = i;
        }
    }

    private void drawToggleIndicator(float f, float f1, float f2, int i, long j) {
        if (j - this.stateChangeTime < 200L) {
            this.toggleSpringAnim.setTarget(1.0);
        }

        this.toggleSpringAnim.update();
        double d0 = this.lastToggledState ? 1.0 : 0.0;
        this.toggleStateAnim.setTarget(d0);
        this.toggleStateAnim.update();
        float f3 = this.toggleSpringAnim.getFloat();
        float f4 = 16.0F * f3;
        float f5 = 8.0F * f3;
        float f6 = 3.0F * f3;
        float f7 = f - f4 / 2.0F + 2.0F;
        float f8 = f1 - f5 + 4.0F;
        Color color = this.lastToggledState ? new Color(110, 231, 183, scaleAlpha(i, 180)) : new Color(248, 113, 113, scaleAlpha(i, 180));
        Render2DUtil.drawRoundedRect(f7, f8, f4, f5, f5 / 2.0F, 1.0F, color);
        float f9 = f7 + f6 - 2.0F;
        float f10 = f7 + f4 - f6 - 4.0F;
        float f11 = f9 + (f10 - f9) * this.toggleStateAnim.getFloat();
        float f12 = f8 + f5 / 2.0F - 3.0F;
        int k = scaleAlpha(i, 240);
        Render2DUtil.drawCircle(f11, f12, f6, 1.0F, whiteWithAlpha(k));
        MSDFFontAtlas msdffontatlas = VoilClient.fonts.getSelectedFont();
        String s = this.lastToggledName;
        float f13 = f + 12.0F;
        int l = scaleAlpha(i, 230);
        msdffontatlas.render(s, f13, f2, 10.0F, alphaToArgb(l));
    }

    private static String formatDuration(long i) {
        long j = Math.max(0L, i / 1000L);
        long k = j / 3600L;
        long l = j % 3600L / 60L;
        if (k > 0L) {
            return k + "h " + l + "m";
        } else {
            long j1 = j % 60L;
            long i1 = l;
            return i1 + "m " + j1 + "s";
        }
    }

    private boolean isHovered(float f, float f1, float f2, float f3) {
        if (this.screenOpen) {
            float f4 = this.mouseY;
            if (isInside(this.mouseX, f4, f, f1, f2, f3)) {
                return true;
            }
        }

        return false;
    }

    private void registerClickArea(float f, float f1, float f2, float f3, Runnable runnable) {
        int j = this.clickRegions.size();
        int i = j * 4;
        if (i + 4 > this.clickBounds.length) {
            j = this.clickBounds.length;
            this.clickBounds = Arrays.copyOf(this.clickBounds, j + j);
        }

        this.clickBounds[i] = f;
        this.clickBounds[i + 1] = f1;
        this.clickBounds[i + 2] = f2;
        this.clickBounds[i + 3] = f3;
        this.clickRegions.add(runnable);
    }

    private String truncateText(String s, float f, float f1) {
        MSDFFontAtlas msdffontatlas = VoilClient.fonts.getSelectedFont();
        if (msdffontatlas.getWidth(s, f1) <= f) {
            return s;
        }

        int i;
        for (i = s.length(); i > 1; i--) {
            String s1 = s.substring(0, i);
            if (!(msdffontatlas.getWidth(s1 + "..", f1) > f)) {
                break;
            }
        }

        String s3 = s.substring(0, i);
        return s3 + "..";
    }

    private void measureExpandedSize(MediaController mediacontroller) {
        float f = this.scale(8.0F);
        float f1 = this.scale(6.0F);
        float f2 = this.scale(38.0F);
        float f3 = this.getIslandWidth() - f * 2.0F;
        float f4 = (f3 - f1) / 2.0F;
        if (this.configs.isEmpty() && this.expanded) {
            this.refreshConfigs();
        }

        float f5 = f3 - this.scale(8.0F) * 2.0F;
        if (f5 != this.cachedLayoutWidth
            || this.uiScale != this.cachedLayoutScale
            || this.toggleLabels != this.cachedToggleLabels
            || this.configNames != this.cachedConfigNames) {
            this.toggleLayout = this.computeWidgetLayout(this.toggleLabels, f5, 3);
            this.configLayout = this.computeWidgetLayout(this.configNames, f5, 2);
            this.cachedLayoutWidth = f5;
            this.cachedLayoutScale = this.uiScale;
            this.cachedToggleLabels = this.toggleLabels;
            this.cachedConfigNames = this.configNames;
        }

        java.util.ArrayList<String> arraylist = new java.util.ArrayList<>(WIDGET_NAMES.size());

        for (String s : this.expandOrder.getItems()) {
            if (WIDGET_NAMES.contains(s) && !arraylist.contains(s) && this.expandWidgets.isSelected(s)) {
                arraylist.add(s);
            }
        }

        for (String s2 : WIDGET_NAMES) {
            if (!arraylist.contains(s2) && this.expandWidgets.isSelected(s2)) {
                arraylist.add(s2);
            }
        }

        this.segmentCount = 0;
        float f7 = f;
        String s3 = null;

        for (String s1 : arraylist) {
            boolean flag = switch (s1) {
                case "Media", "Toggles", "Configs" -> true;
                default -> false;
            };
            if (!flag) {
                if (s3 == null) {
                    s3 = s1;
                } else {
                    this.addWidgetSlot(s3, f, f7, f4, f2);
                    this.addWidgetSlot(s1, f + f4 + f1, f7, f4, f2);
                    f7 += f2 + f1;
                    s3 = null;
                }
            } else {
                if (s3 != null) {
                    this.addWidgetSlot(s3, f, f7, f3, f2);
                    f7 += f2 + f1;
                    s3 = null;
                }

                float f6 = switch (s1) {
                    case "Media" -> this.scale(mediacontroller.isIdle() ? 26.0F : 100.0F);
                    case "Toggles" -> this.getPanelHeight(this.toggleLayout);
                    default -> this.getPanelHeight(this.configLayout);
                };
                this.addWidgetSlot(s1, f, f7, f3, f6);
                f7 += f6 + f1;
            }
        }

        if (s3 != null) {
            this.addWidgetSlot(s3, f, f7, f3, f2);
            f7 += f2 + f1;
        }

        this.expandedHeight = f7 - f1 + f;
    }

    private void drawEqualizer(float f, float f1, float f2, float f3, float f4, float f5, Color color) {
        float f6 = f - (5 * f2 + 4 * f3) / 2.0F;
        float f7 = (float)(System.currentTimeMillis() % 850L) / 850.0F;

        for (int i = 0; i < 5; i++) {
            float f8 = (float)Math.sin((f7 + i * 0.23F) * Math.PI * 2.0) * 0.5F + 0.5F;
            float f9 = f4 + f8 * f5;
            float f11 = f6 + i * (f2 + f3);
            float f10 = f1 - f9 / 2.0F;
            Render2DUtil.drawRoundedRect(f11, f10, f2, f9, f2 / 2.0F, 1.0F, color);
        }
    }

    private boolean isMediaPlaying(MediaController mediacontroller, long i) {
        boolean flag = mediacontroller.isPlaying();
        if (i >= this.playingDebounceUntil) {
            return flag;
        }

        if (flag == this.mediaPlaying) {
            this.playingDebounceUntil = 0L;
        }

        return this.mediaPlaying;
    }

    private void registerSeekBar(float f, float f1, float f2, float f3, long i, float f4) {
        this.seekBarValid = true;
        this.seekBarX = f;
        this.seekBarWidth = f2;
        this.seekBarY = f1 - f4;
        this.seekBarHeight = f3 + f4 * 2.0F;
        this.trackDurationMs = i;
        if (this.seeking) {
            float f5 = (this.mouseX - f) / f2;
            this.seekProgress = clamp01(f5);
        }
    }

    private void refreshConfigs() {
        String s = String.valueOf(ClientSettings.username.getValue());
        Object object = null;
        java.util.ArrayList<CloudConfig> arraylist = new java.util.ArrayList<>();

        try {
            for (CloudConfig cloudconfig : new java.util.ArrayList<>(ClientSettings.cloudConfigs.values())) {
                if (s.equalsIgnoreCase(cloudconfig.getName())) {
                    arraylist.add(cloudconfig);
                }
            }
        } catch (ConcurrentModificationException concurrentmodificationexception) {
            return;
        }

        long now = System.currentTimeMillis();
        if (arraylist.isEmpty() && now - this.lastConfigRefreshTime > STATE_TIMEOUT_MS) {
            this.lastConfigRefreshTime = now;
            ApiClient apiclient = ApiClient.INSTANCE;
            apiclient.getAllConfigs(true);
        }

        arraylist.sort(Comparator.comparing(cloudconfig1 -> String.valueOf(cloudconfig1.getAuthor()), String.CASE_INSENSITIVE_ORDER));
        this.configs = arraylist;
        this.configNames = arraylist.stream().map(cloudconfig1 -> String.valueOf(cloudconfig1.getAuthor())).toList();
    }

    private String getFpsPingText() {
        int k = mc.getCurrentFps();
        long j = this.getPing();
        int i = k;
        return i + "fps  " + j + "ms";
    }

    private void handleControlClick(int i) {
        MediaController mediacontroller = VoilClient.getMediaController();
        if (!mediacontroller.isIdle()) {
            long j = System.currentTimeMillis();
            if (i != 0) {
                if (i != 1) {
                    mediacontroller.next();
                    this.seekHoldUntil = -1L;
                } else {
                    this.mediaPlaying = !this.isMediaPlaying(mediacontroller, j);
                    this.playingDebounceUntil = j + 1500L;
                    mediacontroller.playPause();
                }
            } else {
                mediacontroller.previous();
                this.seekHoldUntil = -1L;
            }
        }
    }

    private void drawMediaControls(MediaController mediacontroller, float f, float f1, float f2, float f3, float f4, int i, long j) {
        String s = this.isMediaPlaying(mediacontroller, j) ? IconGlyphs.iconE132() : IconGlyphs.iconE140();
        this.drawControlButton(0, IconGlyphs.iconE163(), f - f2, f1, f3, f4, i);
        this.drawControlButton(1, s, f, f1, f3 * 1.18F, f4, i);
        this.drawControlButton(2, IconGlyphs.iconE164(), f + f2, f1, f3, f4, i);
    }

    private void drawMediaWidget(float f, float f1, float f2, float f3, int i, boolean flag, MediaController mediacontroller, long now) {
        MSDFFontAtlas msdffontatlas = VoilClient.fonts.getSelectedFont();
        float f9 = this.scale(8.0F);
        if (mediacontroller.isIdle()) {
            float f35 = msdffontatlas.getLineHeight(this.scale(9.0F));
            float f37 = f + f9;
            float f39 = f1 + (f3 - f35) / 2.0F;
            float f40 = this.scale(9.0F);
            int j1 = scaleAlpha(i, 110);
            msdffontatlas.render("Nothing playing", f37, f39, f40, alphaToArgb(j1));
        } else {
            float f10 = this.scale(40.0F);
            float f11 = f + f9;
            float f12 = f + f2 - f9;
            float f13 = Math.max(3.0F, this.scale(5.0F));
            if (mediacontroller.getCover() != null) {
                TextureRectangleRenderer texturerectanglerenderer = Render2DUtil.textureRenderer;
                GlTexture gltexture1 = mediacontroller.getCover();
                float f43 = f1 + f9;
                float f46 = this.scale(8.0F);
                Color color1 = whiteWithAlpha(i);
                float f28 = 0.7F;
                float f29 = f46;
                float f30 = f10;
                float f31 = f10;
                float f32 = f43;
                float f33 = f11;
                GlTexture gltexture = gltexture1;
                texturerectanglerenderer.render(gltexture, f33, f32, f31, f30, f29, f28, color1);
            }

            float f14 = f11 + f10 + this.scale(8.0F);
            float f15 = f12 - this.scale(28.0F) - f14;
            String s6 = mediacontroller.getTitle();
            String s7 = defaultIfEmpty(s6, "Unknown Track");
            String s = this.truncateText(s7, f15, this.scale(11.0F));
            String s8 = mediacontroller.getArtist();
            String s9 = defaultIfEmpty(s8, "Unknown Artist");
            String s1 = this.truncateText(s9, f15, this.scale(9.0F));
            msdffontatlas.render(s, f14, f1 + f9 + this.scale(6.0F), this.scale(11.0F), alphaToArgb(i));
            float f38 = f1 + f9 + this.scale(20.0F);
            float f44 = this.scale(9.0F);
            int k1 = scaleAlpha(i, 140);
            msdffontatlas.render(s1, f14, f38, f44, alphaToArgb(k1));
            boolean flag1 = this.isMediaPlaying(mediacontroller, now);
            float f16 = Math.max(1.0F, this.scale(2.0F));
            float f36 = f12 - this.scale(12.0F);
            float f42 = f1 + f9 + f10 / 2.0F;
            float f45 = flag1 ? this.scale(5.0F) : this.scale(4.0F);
            float f41 = flag1 ? this.scale(11.0F) : 0.0F;
            Color color = withAlpha(GuiTheme.accentColor1, i);
            float f4 = f41;
            float f5 = f45;
            float f6 = f16;
            float f7 = f16;
            float f8 = f42;
            this.drawEqualizer(f36, f8, f7, f6, f5, f4, color);
            float f17 = f1 + f9 + f10 + this.scale(11.0F);
            long j = mediacontroller.getDuration();
            if (j > 0L) {
                long k = mediacontroller.getPosition();
                float f18 = clamp01((float)k / (float)j);
                float f19 = this.getSeekProgress(f18, now);
                long l = f19 == f18 ? k : (long)(f19 * (float)j);
                double d0 = f19;
                this.mediaAnim.setTarget(d0);
                this.mediaAnim.update();
                float f20 = this.scale(8.0F);
                float f21 = msdffontatlas.getLineHeight(f20);
                String s2 = formatTrackTime(l);
                String s4 = formatTrackTime(Math.max(0L, j - l));
                String s3 = "-" + s4;
                float f22 = msdffontatlas.getWidth(s2, f20);
                float f23 = msdffontatlas.getWidth(s3, f20);
                f38 = f17 - f21 / 2.0F;
                int l1 = scaleAlpha(i, 140);
                msdffontatlas.render(s2, f11, f38, f20, alphaToArgb(l1));
                f42 = f12 - f23;
                f38 = f17 - f21 / 2.0F;
                int i2 = scaleAlpha(i, 140);
                msdffontatlas.render(s3, f42, f38, f20, alphaToArgb(i2));
                int i1 = Math.max(2, Math.round(this.scale(3.0F)));
                float f24 = f11 + f22 + this.scale(7.0F);
                float f25 = f12 - f23 - this.scale(7.0F) - f24;
                float f26 = f17 - i1 / 2.0F;
                if (f25 > 0.0F) {
                    f38 = i1;
                    f44 = i1 / 2.0F;
                    int j2 = scaleAlpha(i, 30);
                    Render2DUtil.drawRoundedRect(f24, f26, f25, f38, f44, 1.0F, whiteWithAlpha(j2));
                    float f27 = f25 * clamp01(this.mediaAnim.getFloat());
                    if (f27 >= 1.0F) {
                        f42 = (int)f27;
                        f38 = i1;
                        f44 = i1 / 2.0F;
                        Color color3 = withAlpha(GuiTheme.accentColor1, i);
                        Color color2 = withAlpha(GuiTheme.accentColor2, i);
                        Render2DUtil.drawRoundedRectGradientVertical(f24, f26, f42, f38, f44, 1.0F, color3, color2, 25);
                    }

                    f36 = f24 + f27;
                    float f34 = f26 + i1 / 2.0F;
                    this.drawDot(f36, f34, Math.max(2.0F, this.scale(3.0F)), i);
                    if (flag) {
                        this.registerSeekBar(f24, f26, f25, i1, j, f13);
                    }
                }
            }

            this.drawMediaControls(mediacontroller, f + f2 / 2.0F, f17 + this.scale(23.0F), this.scale(40.0F), this.scale(16.0F), f13, i, now);
        }
    }

    private float getSeekProgress(float f, long i) {
        if (this.seeking) {
            return this.seekProgress;
        } else if (i >= this.seekHoldUntil) {
            return f;
        } else if (Math.abs(f - this.seekProgress) < 0.02F) {
            this.seekHoldUntil = -1L;
            return f;
        } else {
            return this.seekProgress;
        }
    }

    private String getQueueText(long i) {
        String s = this.queueDetail;
        if (s != null && !s.isEmpty()) {
            return s;
        } else {
            long j = Math.max(0L, (i - this.queueStartTime) / 1000L);
            if (j >= 60L) {
                long l = j % 60L;
                long k = j / 60L;
                return k + "m " + l + "s";
            } else {
                return j + "s";
            }
        }
    }

    private void onModuleToggled(Module module, boolean flag) {
        if (this.isEnabled() && module != this) {
            this.recentModules.remove(module);
            this.recentModules.add(0, module);
            if (this.recentModules.size() > 12) {
                List list = this.recentModules;
                int i = this.recentModules.size();
                list.remove(i - 1);
            }

            if (!this.expanded && this.islandStates.isSelected("Notifications")) {
                this.lastToggledName = String.valueOf(module.getName());
                this.lastToggledState = flag;
                this.stateChangeTime = System.currentTimeMillis();
                this.islandState = 3;
                this.toggleSpringAnim.setTarget(0.0);
            }
        }
    }

    private static Color withAlpha(Color color, int i) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), scaleAlpha(i, color.getAlpha()));
    }

    private static int alphaToArgb(int i) {
        int j = Math.max(0, Math.min(255, i)) << 24;
        return j | 16777215;
    }

    private void drawChipTile(float f, float f1, float f2, int i, boolean flag, boolean flag1) {
        MSDFFontAtlas msdffontatlas = VoilClient.fonts.getSelectedFont();
        float[] afloat = flag1 ? this.configLayout : this.toggleLayout;
        List<String> list = flag1 ? this.configNames : this.toggleLabels;
        long now = System.currentTimeMillis();
        if (this.pendingConfig != null && now - this.pendingConfigTime > CONFIRM_WINDOW_MS) {
            this.pendingConfig = null;
        }

        boolean flag4 = this.loadedConfig != null && now - this.loadedConfigTime < LOADED_HINT_MS;
        String s3;
        if (!flag1) {
            s3 = this.toggleSource.getMode().toLowerCase();
        } else if (this.pendingConfig != null) {
            s3 = "click again to load";
        } else {
            s3 = flag4 ? "loaded" : null;
        }

        String s13;
        if (flag1) {
            s13 = "CONFIGS";
        } else {
            s13 = "TOGGLES";
        }

        String s = s13;
        this.drawInfoCard(s, s3, f, f1, f2, i);
        float f5 = f + this.scale(8.0F);
        float f6 = f1 + this.scale(20.0F);
        int k = (int)afloat[0];
        if (k == 0) {
            String s12;
            if (flag1) {
                s12 = "No configs of your own";
            } else {
                s12 = "No shared configs yet";
            }

            String s10 = s12;
            float f16 = this.scale(9.0F);
            int k1 = scaleAlpha(i, 140);
            msdffontatlas.render(s10, f5, f6, f16, alphaToArgb(k1));
        } else {
            float f7 = this.getRowHeight();

            for (int l = 0; l < k; l++) {
                int j1 = l * 3;
                int i1 = j1 + 5;
                float f8 = f5 + afloat[i1];
                float f9 = f6 + afloat[i1 + 1];
                float f10 = afloat[i1 + 2];
                String s4 = (String)list.get(l);
                if (flag1) {
                    CloudConfig cloudconfig = (CloudConfig)this.configs.get(l);
                    String s5 = cloudconfig.getFileName();
                    s13 = "c:" + s5;
                    boolean flag5 = flag4 && s5.equals(this.loadedConfig);
                    boolean flag2 = s5.equals(this.pendingConfig);
                    boolean flag3 = flag5;
                    int j = i;
                    float f3 = f10;
                    float f11 = f9;
                    float f4 = f8;
                    String s1 = s4;
                    String s2 = s13;
                    this.drawChip(s2, s1, f4, f11, f3, j, flag3, flag2, flag);
                    if (flag) {
                        this.registerClickArea(f8, f9, f10, f7, () -> this.loadConfig(cloudconfig));
                    }
                } else {
                    Module module = (Module)this.toggleModules.get(l);
                    String s9 = "m:" + s4;
                    this.drawChip(s9, s4, f8, f9, f10, i, module.isEnabled(), false, flag);
                    if (flag) {
                        this.registerClickArea(f8, f9, f10, f7, module::toggle);
                    }
                }
            }

            int j2 = (int)afloat[1];
            if (j2 > 0) {
                String s11 = "+" + j2;
                float f12 = this.scale(9.0F);
                float f13 = this.scale(6.0F);
                float f14 = f5 + afloat[2];
                float f15 = f6 + afloat[3];
                float f17 = msdffontatlas.getWidth(s11, f12) + f13 * 2.0F;
                float f19 = f7 / 2.0F;
                int l1 = scaleAlpha(i, 10);
                Render2DUtil.drawRoundedRect(f14, f15, f17, f7, f19, 1.0F, whiteWithAlpha(l1));
                f17 = f14 + f13;
                float f18 = f15 + (f7 - msdffontatlas.getLineHeight(f12)) / 2.0F;
                int i2 = scaleAlpha(i, 220);
                msdffontatlas.render(s11, f17, f18, f12, alphaToArgb(i2));
            }
        }
    }

    private void renderVoilStyle(int i, int j) {
        MSDFFontAtlas msdffontatlas = VoilClient.fonts.getInterBold();
        int k = MUTED_TEXT.getRGB();
        String[] astring = new String[]{String.valueOf(ClientSettings.userId.getValue()), String.valueOf(mc.getCurrentFps()), String.valueOf(this.getPing())};
        String[] astring1 = new String[]{"UID", "FPS", "ms"};
        float f = msdffontatlas.getWidth(" | ", 11.0F) + 4.0F;
        float f1 = msdffontatlas.getWidth("Voil", 11.0F);

        for (int l = 0; l < astring.length; l++) {
            String s = astring[l];
            f1 += f + msdffontatlas.getWidth(s + " ", 11.0F) + msdffontatlas.getWidth(astring1[l], 11.0F);
        }

        int k1 = (int)f1;
        int l1 = k1 + 10;
        int i1 = (int)(msdffontatlas.getLineHeight(11.0F) + 6.0F);
        float f5 = i;
        float f4 = j;
        Render2DUtil.drawRoundedRect(f5, f4, l1, i1, 5.0F, 1.0F, ISLAND_BACKGROUND);
        float f2 = i + 5.0F;
        float f3 = j + 4.0F;
        msdffontatlas.render("V", f2, f3, 11.0F, k);
        f2 += msdffontatlas.getWidth("V", 11.0F);
        msdffontatlas.render("oil", f2, f3, 11.0F, -1);
        f2 += msdffontatlas.getWidth("oil", 11.0F);

        for (int j1 = 0; j1 < astring.length; j1++) {
            msdffontatlas.render(" | ", f2, f3, 11.0F, k);
            f2 += f;
            msdffontatlas.render(astring[j1], f2, f3, 11.0F, -1);
            String s2 = astring[j1];
            f2 += msdffontatlas.getWidth(s2 + " ", 11.0F);
            msdffontatlas.render(astring1[j1], f2, f3, 11.0F, k);
            f2 += msdffontatlas.getWidth(astring1[j1], 11.0F);
        }

        VoilClient.getHudLayout().setSize(this, l1, i1);
    }

    private void drawInfoCard(String s, String s1, float f, float f1, float f2, int i) {
        MSDFFontAtlas msdffontatlas = VoilClient.fonts.getSelectedFont();
        float f3 = this.scale(8.0F);
        float f5 = f + this.scale(8.0F);
        float f6 = f1 + this.scale(6.0F);
        int j = scaleAlpha(i, 110);
        msdffontatlas.render(s, f5, f6, f3, alphaToArgb(j));
        if (s1 != null && !s1.isEmpty()) {
            float f4 = msdffontatlas.getWidth(s1, f3);
            f5 = f + f2 - this.scale(8.0F) - f4;
            f6 = f1 + this.scale(6.0F);
            int k = scaleAlpha(i, 80);
            msdffontatlas.render(s1, f5, f6, f3, alphaToArgb(k));
        }
    }

    private static float clamp01(float f) {
        return Math.max(0.0F, Math.min(1.0F, f));
    }

    private static Color whiteWithAlpha(int i) {
        return new Color(255, 255, 255, Math.max(0, Math.min(255, i)));
    }

    private void drawDot(float f, float f1, float f2, int i) {
        if (f2 >= 0.3F) {
            float f8 = f - f2;
            float f9 = f1 - f2;
            float f10 = f2 * 2.0F;
            float f11 = f2 * 2.0F;
            Color color = whiteWithAlpha(i);
            float f3 = 1.0F;
            float f4 = f2;
            float f5 = f11;
            float f6 = f10;
            float f7 = f9;
            Render2DUtil.drawRoundedRect(f8, f7, f6, f5, f4, f3, color);
        }
    }

    private void drawTrackInfo(MediaController mediacontroller, float f, float f1, int i, int j, int k, int l, int i1, float f2, long now) {
        if (!mediacontroller.isIdle()) {
            MSDFFontAtlas msdffontatlas = VoilClient.fonts.getSelectedFont();
            float f3 = i + k - 10.0F;
            String s3 = mediacontroller.getTitle();
            String s7 = defaultIfEmpty(s3, "Unknown");
            float f16 = f1 - 12.0F;
            int l2 = scaleAlpha(i1, 225);
            msdffontatlas.render(s7, f, f16, 9.0F, alphaToArgb(l2));
            String s4 = mediacontroller.getArtist();
            s7 = defaultIfEmpty(s4, "Unknown");
            f16 = f1 - 3.0F;
            int i3 = scaleAlpha(i1, 65);
            msdffontatlas.render(s7, f, f16, 8.0F, alphaToArgb(i3));
            int j1 = (int)(i1 * (1.0F - f2));
            if (j1 > 2) {
                float f4 = 8.5F;
                float f12 = f3 - f4 / 2.0F;
                float f9 = f1 - 2.0F;
                this.drawEqualizer(f12, f9, 0.5F, 1.5F, 3.0F, 6.0F, withAlpha(SPOTIFY_GREEN, j1));
            }

            long k3 = mediacontroller.getDuration();
            if (k3 > 0L) {
                long k1 = mediacontroller.getPosition();
                float f5 = clamp01((float)k1 / (float)k3);
                float f6 = this.getSeekProgress(f5, now);
                long l1 = f6 == f5 ? k1 : (long)(f6 * (float)k3);
                double d0 = f6;
                this.trackAnim.setTarget(d0);
                this.trackAnim.update();
                String s6 = formatTrackTime(l1);
                String s5 = formatTrackTime(k3);
                String s1 = s6;
                String s = s1 + " / " + s5;
                int k2 = j + l;
                k2 = k2 + -3 + 1;
                int i2 = k2 + -4 + 1;
                f16 = i2 - 4.0F;
                int j3 = scaleAlpha(i1, 140);
                msdffontatlas.render(s, f, f16, 7.0F, alphaToArgb(j3));
                float f7 = f + msdffontatlas.getWidth(s, 7.0F) + 4.0F;
                float f8 = f3 - f7;
                if (f8 > 0.0F) {
                    float f10 = i2;
                    Render2DUtil.drawRoundedRect(f7, f10, f8, 2, 1.0F, 1.0F, new Color(40, 40, 40, scaleAlpha(i1, 150)));
                    int j2 = (int)(f8 * this.trackAnim.getFloat());
                    if (j2 > 0) {
                        float f13 = i2;
                        float f15 = j2;
                        f16 = 2;
                        Color color1 = withAlpha(GuiTheme.accentColor1, i1);
                        Color color = withAlpha(GuiTheme.accentColor2, i1);
                        Render2DUtil.drawRoundedRectGradientVertical(f7, f13, f15, f16, 1.0F, 1.0F, color1, color, 25);
                    }

                    float f14 = f7 + j2;
                    float f11 = i2 + 2 / 2.0F;
                    this.drawDot(f14, f11, 2.5F * f2, i1);
                    if (this.hovered) {
                        this.registerSeekBar(f7, i2, f8, 2, k3, 5.0F);
                    }
                }
            }

            int l3 = (int)(i1 * clamp01((f2 - 0.4F) / 0.6F));
            if (l3 > 2) {
                this.drawMediaControls(mediacontroller, i + k - 37.0F, f1 - 3.0F, 17.0F, 11.0F, 5.0F, l3, now);
            }
        }
    }

    private float getStateWidth(MediaController mediacontroller, long i) {
        MSDFFontAtlas msdffontatlas = VoilClient.fonts.getSelectedFont();
        int j = this.islandState;
        float f;
        if (j != 1) {
            if (j != 2) {
                if (j != 3) {
                    if (j != 4) {
                        f = msdffontatlas.getWidth(this.getFpsPingText(), 10.0F);
                    } else {
                        String s9 = this.queueName;
                        String s5 = this.getQueueText(i);
                        String s6 = s9;
                        f = msdffontatlas.getWidth("Queued  " + s6 + "  " + s5, 10.0F);
                    }
                } else {
                    f = msdffontatlas.getWidth(this.lastToggledName, 10.0F) + 5.0F;
                }
            } else {
                f = msdffontatlas.getWidth(this.getTargetText(), 10.0F);
            }
        } else {
            String s7;
            if (mediacontroller.isIdle()) {
                s7 = "Unknown";
            } else {
                String s3 = mediacontroller.getTitle();
                s7 = defaultIfEmpty(s3, "Unknown");
            }

            String s = s7;
            String s8;
            if (mediacontroller.isIdle()) {
                s8 = "Unknown";
            } else {
                String s4 = mediacontroller.getArtist();
                s8 = defaultIfEmpty(s4, "Unknown");
            }

            String s1 = s8;
            f = 16.0F + Math.max(msdffontatlas.getWidth(s, 9.0F), msdffontatlas.getWidth(s1, 8.0F));
        }

        return f;
    }

    private float getRowHeight() {
        return Math.max(6.0F, Math.round(this.scale(15.0F)));
    }

    private void renderVoil2Style(int i, int j) {
        MSDFFontAtlas msdffontatlas = VoilClient.fonts.getPoppins();
        ServerInfo serverinfo = mc.getCurrentServerEntry();
        String[] astring1 = new String[]{
            String.valueOf(ClientSettings.username.getValue()),
            String.valueOf(ClientSettings.userRank.getValue()),
            serverinfo != null && serverinfo.address != null ? serverinfo.address : "localhost",
            null,
            null
        };
        int l1 = mc.getCurrentFps();
        astring1[3] = l1 + "FPS";
        astring1[4] = LocalTime.now().format(CLOCK_FORMAT);
        String[] astring = astring1;
        float[] afloat = new float[astring.length];
        float f = 0.0F;

        for (int k = 0; k < astring.length; k++) {
            afloat[k] = msdffontatlas.getWidth(astring[k]);
            f += afloat[k];
        }

        int k1 = astring.length;
        int l = k1 - 1;
        int i1 = Math.max(200, (int)(f + 6 * 2 + 12.0F * l));
        float f1 = Math.max(12.0F, (i1 - f - 12) / l);
        float f7 = i;
        float f9 = j;
        float f3 = i1;
        float f4 = f9;
        float f5 = f7;
        Render2DUtil.drawBlurSimple(f5, f4, f3, 18.0F, 6.0F, 24.0F);
        float f8 = i;
        float f6 = j;
        Render2DUtil.drawOutline(f8, f6, i1, 18.0F, 6.0F, 0.5F, 0.5F, Color.white);
        float f2 = i + 6;

        for (int j1 = 0; j1 < astring.length; j1++) {
            msdffontatlas.render(astring[j1], f2, j + 1, Color.white);
            if (j1 < l) {
                Render2DUtil.drawCircle(f2 + afloat[j1] + f1 / 2.0F - 1.0F, j + 8, 1.0F, 1.0F, Color.white);
            }

            f2 += afloat[j1] + f1;
        }

        if (this.glow.isEnabled()) {
            Render2DUtil.drawGlowGradient(i, j, i1, 18.0F, 6.0F, 1.0F, 25.0F, 25.0F, Color.pink, Color.pink, Color.cyan, Color.cyan);
        }

        VoilClient.getHudLayout().setSize(this, i1, 18);
    }

    @EventHandler
    public void onPacket(PacketReceiveEvent packetreceiveevent) {
        Packet packet = packetreceiveevent.getPacket();
        if (packet instanceof PingResultS2CPacket pingresults2cpacket) {
            long i = this.lastPingSentAt;
            if (pingresults2cpacket.startTime() == i) {
                this.pingMs = System.currentTimeMillis() - i;
            }
        } else if (packet instanceof OverlayMessageS2CPacket overlaymessages2cpacket && overlaymessages2cpacket.text() != null) {
            this.parseQueueMessage(overlaymessages2cpacket.text().getString());
        } else if (packet instanceof GameMessageS2CPacket gamemessages2cpacket && gamemessages2cpacket.overlay() && gamemessages2cpacket.content() != null) {
            this.parseQueueMessage(gamemessages2cpacket.content().getString());
        }
    }

    private float getPanelHeight(float[] afloat) {
        return this.scale(20.0F) + Math.max(afloat[4], this.scale(10.0F)) + this.scale(8.0F);
    }

    private float drawLabeledValue(float f, float f1, String s, String s1, int i) {
        float f2 = this.scale(12.0F);
        float f3 = this.scale(9.0F);
        float f4 = VoilClient.fonts.getInterBold().getWidth(s, f2);
        MSDFFontAtlas msdffontatlas = VoilClient.fonts.getInterBold();
        int j = scaleAlpha(i, 235);
        msdffontatlas.render(s, f, f1, f2, alphaToArgb(j));
        msdffontatlas = VoilClient.fonts.getSelectedFont();
        float f5 = f + f4 + this.scale(2.0F);
        float f6 = f1 + this.scale(2.0F);
        int k = scaleAlpha(i, 110);
        msdffontatlas.render(s1, f5, f6, f3, alphaToArgb(k));
        return f4 + this.scale(2.0F) + VoilClient.fonts.getSelectedFont().getWidth(s1, f3);
    }

    @EventHandler
    public void onAttackEntity(AttackEntityPostEvent attackentitypostevent) {
        if (this.isEnabled() && mc.player != null && attackentitypostevent.getTarget() instanceof AbstractClientPlayerEntity abstractclientplayerentity) {
            this.targetPlayer = abstractclientplayerentity;
            this.lastAttackTime = System.currentTimeMillis();
            this.targetTracked = true;
            if (this.islandStates.isSelected("Fighting")) {
                this.islandState = 2;
            }
        }
    }

    @EventHandler
    public void onModuleDisable(ModuleDisableEvent moduledisableevent) {
        this.onModuleToggled(moduledisableevent.getModule(), false);
    }

    @EventHandler
    public void onModuleEnable(ModuleEnableEvent moduleenableevent) {
        this.onModuleToggled(moduleenableevent.getModule(), true);
    }

    @EventHandler
    public void onMousePress(MousePressEvent mousepressevent) {
        if (this.isEnabled() && mousepressevent.getButton() == 0) {
            if (mousepressevent.isRelease()) {
                if (this.seeking) {
                    this.seeking = false;
                    MediaController mediacontroller = VoilClient.getMediaController();
                    if (!mediacontroller.isIdle() && this.trackDurationMs > 0L) {
                        mediacontroller.seek((long)(this.seekProgress * (float)this.trackDurationMs));
                        this.seekHoldUntil = System.currentTimeMillis() + 1200L;
                    }
                }

                if (this.mouseHeld) {
                    this.mouseHeld = false;
                    mousepressevent.cancel();
                }
            } else if (mousepressevent.isPress() && this.screenOpen && (this.hovered || this.expanded) && this.hudStyle.is("Dynamic Island")) {
                float f = (float)mousepressevent.getY();
                float f1 = (float)mousepressevent.getX();
                if (isInside(f, f1, this.islandX, this.islandY, this.islandWidth, this.islandHeight)) {
                    this.mouseHeld = true;
                    mousepressevent.cancel();
                    if (this.seekBarValid && isInside(f, f1, this.seekBarX, this.seekBarY, this.seekBarWidth, this.seekBarHeight)) {
                        this.seeking = true;
                        this.seekProgress = clamp01((f - this.seekBarX) / this.seekBarWidth);
                    } else {
                        for (int i = 0; i < this.clickRegions.size(); i++) {
                            int j = i * 4;
                            if (isInside(f, f1, this.clickBounds[j], this.clickBounds[j + 1], this.clickBounds[j + 2], this.clickBounds[j + 3])) {
                                ((Runnable)this.clickRegions.get(i)).run();
                                return;
                            }
                        }
                    }
                }
            }
        }
    }
}
