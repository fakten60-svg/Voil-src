package lol.lwes.voil.gui.clickgui;

import com.google.gson.JsonObject;
import dev.prestigeclient.renderlib.api.font.MSDFFontAtlas;
import dev.prestigeclient.renderlib.impl.render.resource.GlTexture;
import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.api.ApiClient;
import lol.lwes.voil.config.ClientSettings;
import lol.lwes.voil.config.ConfigManager;
import lol.lwes.voil.config.GuiTheme;
import lol.lwes.voil.duck.MatrixStackProviderAccessor;
import lol.lwes.voil.event.InputEventType;
import lol.lwes.voil.gui.BaseScreen;
import lol.lwes.voil.gui.clickgui.ChangelogEntry;
import lol.lwes.voil.gui.clickgui.page.GuiSettingsPage;
import lol.lwes.voil.gui.clickgui.page.IrcPage;
import lol.lwes.voil.gui.clickgui.page.SocialPage;
import lol.lwes.voil.gui.hudeditor.HudEditorScreen;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.module.ModuleManager;
import lol.lwes.voil.module.client.Vlippy;
import lol.lwes.voil.module.miscellaneous.SelfDestruct;
import lol.lwes.voil.setting.KeybindSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.PlayerTargetUtil;
import lol.lwes.voil.util.ReleaseEntry;
import lol.lwes.voil.util.ReleaseNotes;
import lol.lwes.voil.util.font.IconGlyphs;
import lol.lwes.voil.util.render.ColorUtil;
import lol.lwes.voil.util.render.EasingType;
import lol.lwes.voil.util.render.GradientFontEffect;
import lol.lwes.voil.util.render.GuiScale;
import lol.lwes.voil.util.render.Render2DUtil;
import lol.lwes.voil.util.render.RenderManager;
import lol.lwes.voil.util.render.TextureRectangleRenderer;
import lol.lwes.voil.util.render.UiAnimation;
import lol.lwes.voil.util.render.UiEasings;
import lol.lwes.voil.util.render.UiRect;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

public class ClickGuiScreen extends BaseScreen {
    private static final ClickGuiScreen instance = new ClickGuiScreen();
    public int windowX;
    public int windowY;
    public int windowWidth;
    public int windowHeight;
    public int sidebarWidth;
    public int dragOffsetX;
    public int dragOffsetY;
    private String searchText;
    private boolean draggingWindow;
    private boolean searchFocused;
    private boolean searchActive;
    public Category selectedCategory = Category.Combat;
    private float closeProgress = 0.0F;
    private boolean opening = true;
    private boolean closing = false;
    private ClickGuiTab currentPage;
    public final List<ModuleButton> moduleButtons = new ArrayList<>();
    public Module selectedModule;
    public int cardWidth;
    public int cardHeight;
    private double displayedScroll;
    private double targetScroll;
    private double savedScroll;
    public int categorySlideOffset;
    private long openTimestamp;
    private boolean caretVisible;
    private long lastOpenTime;
    public final GlTexture logoTexture;
    public final GlTexture vlippyTexture;
    public final GlTexture coffeeTexture;
    private final UiAnimation highlightX = new UiAnimation(EasingType.FLUID_SMOOTH).setFluidParams(0.6, 0.15);
    private final UiAnimation highlightWidth = new UiAnimation(EasingType.FLUID_SMOOTH).setFluidParams(0.6, 0.15);
    private final UiAnimation highlightY = new UiAnimation(EasingType.FLUID_SMOOTH).setFluidParams(0.6, 0.15);
    private final UiAnimation scrollAnimation = new UiAnimation(EasingType.FLUID_SMOOTH).setFluidParams(0.78, 0.15);
    private final UiAnimation searchFocusAnimation = new UiAnimation(EasingType.FLUID_SMOOTH).setFluidParams(0.7, 0.15);
    private final UiAnimation caretAnimation = new UiAnimation(EasingType.FLUID_RESPONSIVE);
    private final UiAnimation[] categoryAnimations = new UiAnimation[Category.values().length];
    private final UiAnimation[] iconHoverAnimations = new UiAnimation[5];
    private final UiAnimation listFadeAnimation = new UiAnimation(EasingType.FLUID_SMOOTH).setFluidParams(0.75, 0.15);
    private final UiAnimation[] sortButtonAnimations = new UiAnimation[3];
    private final UiAnimation enabledToggleAnimation = new UiAnimation(EasingType.FLUID_SPRING).setFluidParams(0.7, 0.22);
    private final UiAnimation changelogAnimation = new UiAnimation(EasingType.FLUID_SPRING).setFluidParams(0.72, 0.18);
    private final UiAnimation changelogScrollAnimation = new UiAnimation(EasingType.FLUID_SMOOTH).setFluidParams(0.75, 0.15);
    private final UiAnimation changelogCloseHoverAnimation = new UiAnimation(EasingType.FLUID_SMOOTH).setFluidParams(0.7, 0.15);
    private final UiAnimation showModuleHoverAnimation = new UiAnimation(EasingType.FLUID_SMOOTH).setFluidParams(0.7, 0.15);
    private final UiAnimation dismissHoverAnimation = new UiAnimation(EasingType.FLUID_SMOOTH).setFluidParams(0.7, 0.15);
    private long lastFrameTime;
    private final UiAnimation pageTransitionAnimation = new UiAnimation(EasingType.FLUID_SMOOTH).setFluidParams(0.68, 0.15);
    private final UiAnimation scrollbarAnimation = new UiAnimation(EasingType.FLUID_SMOOTH).setFluidParams(0.75, 0.15);
    private long lastScrollTime;
    private SelfDestruct selfDestruct;
    private final Map<ClickGuiTab, ClickGuiPanel> pages = new EnumMap<>(ClickGuiTab.class);
    private ClickGuiPanel activePanel;
    private boolean showHints = false;
    private boolean changelogVisible = false;
    private Module featuredModule;
    private final List<ChangelogEntry> suggestions = new ArrayList<>();
    private double changelogScroll = 0.0;
    private double changelogScrollTarget = 0.0;
    private Module openedModule;
    private final VlippyMascot mascot = new VlippyMascot(this);
    private final List<Module> searchSuggestions = new ArrayList<>();
    private final List<SuggestionRect> suggestionRects = new ArrayList<>();
    private final Map<Module, UiAnimation> suggestionHoverAnimations = new HashMap<>();
    private final UiAnimation suggestionsAnimation = new UiAnimation(EasingType.FLUID_SMOOTH).setFluidParams(0.7, 0.15);
    private String suggestionCacheKey = "";
    private long suggestionsUpdatedAt;
    private int selectedSuggestionIndex = -1;
    private int hiddenSuggestionCount;
    private boolean hasSearchResults = true;
    private float lastScale = -1.0F;

    protected ClickGuiScreen() {
        super(Text.of("ClickGUI"));
        GlTexture gltexture = null;

        try {
            gltexture = PlayerTargetUtil.createTexture(
                ClickGuiScreen.class.getClassLoader().getResourceAsStream("assets/tulip/icons/voillogo.png").readAllBytes()
            );
        } catch (IOException | NullPointerException ioexception2) {
            ioexception2.printStackTrace(System.err);
        } finally {
            this.logoTexture = gltexture;
        }

        GlTexture gltexture1 = null;

        try {
            gltexture1 = PlayerTargetUtil.createTexture(
                ClickGuiScreen.class.getClassLoader().getResourceAsStream("assets/tulip/icons/vlippy.png").readAllBytes()
            );
        } catch (IOException | NullPointerException ioexception1) {
            ioexception1.printStackTrace(System.err);
        } finally {
            this.vlippyTexture = gltexture1;
        }

        GlTexture gltexture2 = null;

        try {
            gltexture2 = PlayerTargetUtil.createTexture(
                ClickGuiScreen.class.getClassLoader().getResourceAsStream("assets/tulip/icons/coffee.png").readAllBytes()
            );
        } catch (IOException | NullPointerException ioexception) {
            ioexception.printStackTrace(System.err);
        } finally {
            this.coffeeTexture = gltexture2;
        }

        for (int i = 0; i < this.categoryAnimations.length; i++) {
            this.categoryAnimations[i] = new UiAnimation(EasingType.FLUID_SMOOTH).setFluidParams(0.72, 0.15);
        }

        for (int j = 0; j < this.iconHoverAnimations.length; j++) {
            this.iconHoverAnimations[j] = new UiAnimation(EasingType.FLUID_SPRING).setFluidParams(0.68, 0.3);
        }

        for (int k = 0; k < this.sortButtonAnimations.length; k++) {
            this.sortButtonAnimations[k] = new UiAnimation(EasingType.FLUID_SMOOTH).setFluidParams(0.68, 0.15);
        }
    }

    private float getSortBarX() {
        int j = this.sidebarWidth;
        int i = this.windowX;
        i += j;
        float f = i + 26;
        float f1 = VoilClient.fonts.getInterBold().getWidth(this.selectedCategory.name);
        return f + f1 + 16.0F;
    }

    private int isSuggestionListEmpty() {
        if (this.searchSuggestions.isEmpty() || this.searchText.isEmpty()) {
            return -1;
        } else if (this.selectedSuggestionIndex >= 0) {
            int j = this.selectedSuggestionIndex;
            int i = this.searchSuggestions.size();
            return Math.min(j, i - 1);
        } else {
            return this.hasSearchResults ? -1 : 0;
        }
    }

    private double toGuiUnits(double d0) {
        float f = this.getScale();
        return GuiScale.toGuiUnits(d0, f);
    }

    private void buildChangelog() {
        ReleaseNotes releasenotes = VoilClient.getReleaseNotes();
        if (releasenotes != null && releasenotes.hasEntries()) {
            Color color = new Color(175, 169, 236);
            Color color1 = new Color(239, 159, 39);
            Color color2 = new Color(240, 149, 149);

            for (ReleaseEntry releaseentry : releasenotes.getEntries()) {
                for (String s : releaseentry.added()) {
                    this.suggestions.add(this.createChangelogEntry("New", color, ColorUtil.withAlpha(color, 45), s));
                }

                for (String s1 : releaseentry.modified()) {
                    this.suggestions.add(this.createChangelogEntry("Updated", color1, ColorUtil.withAlpha(color1, 45), s1));
                }

                for (String s2 : releaseentry.removed()) {
                    this.suggestions.add(this.createChangelogEntry("Removed", color2, ColorUtil.withAlpha(color2, 45), s2));
                }
            }

            for (ChangelogEntry changelogentry : this.suggestions) {
                if (changelogentry.description() != null) {
                    ModuleManager modulemanager = ModuleManager.INSTANCE;
                    Module module = modulemanager.getModules()
                        .stream()
                        .filter(module1 -> module1.getName().equalsIgnoreCase(changelogentry.description()))
                        .findFirst()
                        .orElse(null);
                    if (module != null) {
                        this.featuredModule = module;
                        break;
                    }
                }
            }

            this.changelogVisible = !this.suggestions.isEmpty();
        }
    }

    private void renderGui(int i, int j, float f, long k) {
        VoilClient.mc.getWindow().getFramebufferWidth();
        VoilClient.mc.getWindow().getFramebufferHeight();
        float f1 = Math.min(0.1F, (float)(k - this.lastFrameTime) / 1000.0F);
        this.lastFrameTime = k;
        double d1 = this.targetScroll;
        this.scrollAnimation.setTarget(d1);
        this.scrollAnimation.update();
        this.displayedScroll = this.scrollAnimation.getValue();
        String s = (String)ClientSettings.easingType.getValue();
        if (this.opening) {
            float f2 = ((Number)ClientSettings.openSpeed.getValue()).floatValue();
            float f3 = Math.min((float)(k - this.lastOpenTime) / f2, 1.0F);
            this.closeProgress = UiEasings.getEasing(s).apply(f3);
            if (f3 >= 1.0F) {
                this.closeProgress = 1.0F;
                this.opening = false;
            }
        } else if (this.closing) {
            float f94 = ((Number)ClientSettings.closeSpeed.getValue()).floatValue();
            float f96 = Math.min((float)(k - this.lastOpenTime) / f94, 1.0F);
            float f4 = 1.0F - f96;
            this.closeProgress = UiEasings.getEasing(s).apply(f4);
            if (f96 >= 1.0F) {
                this.closeProgress = 0.0F;
                this.close();
                return;
            }
        }

        if (this.draggingWindow) {
            if (this.dragOffsetX == 0 && this.dragOffsetY == 0) {
                int j7 = this.windowX;
                int l6 = i;
                this.dragOffsetX = l6 - j7;
                j7 = this.windowY;
                l6 = j;
                this.dragOffsetY = l6 - j7;
            } else {
                int i2 = this.dragOffsetX;
                int l1 = i;
                this.windowX = l1 - i2;
                i2 = this.dragOffsetY;
                l1 = j;
                this.windowY = l1 - i2;
            }
        } else if (this.dragOffsetX != 0 || this.dragOffsetY != 0) {
            this.dragOffsetX = 0;
            this.dragOffsetY = 0;
        }

        float f95 = GuiTheme.radiusTopLeft;
        MatrixStack matrixstack = ((MatrixStackProviderAccessor)RenderManager.drawContext).tulip$getMatrices();
        if (ClientSettings.glowEnabled.getValue() instanceof Boolean && (Boolean)ClientSettings.glowEnabled.getValue()) {
            Render2DUtil.drawGlowGradient(
                this.windowX,
                this.windowY,
                this.windowWidth,
                this.windowHeight,
                f95,
                1.0F,
                32.0F,
                10.0F,
                GuiTheme.accentColor1,
                GuiTheme.accentColor2,
                GuiTheme.accentColor1,
                GuiTheme.accentColor2
            );
        } else {
            float f133 = this.windowX;
            float f134 = this.windowY;
            float f135 = this.windowWidth;
            float f136 = this.windowHeight;
            Color color3 = Color.black;
            Color color4 = ColorUtil.withAlpha(color3, 127);
            Render2DUtil.drawGlow(f133, f134, f135, f136, f95, 1.0F, 25.0F, color4);
        }

        int i7 = this.windowX;
        int l7 = i7 - 1;
        i7 = this.windowY;
        int i8 = i7 - 1;
        int k7 = this.windowWidth;
        i7 = this.windowX;
        i7 += k7;
        int j2 = i7 + 1;
        i7 = this.windowY + this.windowHeight;
        RenderManager.setScissor(l7, i8, j2, i7 + 1);
        float f97 = this.windowX + this.windowWidth / 2.0F;
        float f5 = this.windowY + this.windowHeight / 2.0F;
        Matrix4f matrix4f = new Matrix4f();
        if (this.closeProgress > 0.0F) {
            matrix4f.translate(f97, f5, 0.0F);
            matrix4f.scale(1.0F / this.closeProgress, 1.0F / this.closeProgress, 1.0F);
            matrix4f.translate(-f97, -f5, 0.0F);
            matrix4f.normal();
        }

        Render2DUtil.framebufferRenderer.modelMatrix = matrix4f;
        if (GuiTheme.isBlurMode()) {
            float f108 = this.windowX;
            float f122 = this.windowY;
            float f52 = this.windowWidth;
            Render2DUtil.drawBlurRoundedTinted(f108, f122, f52, this.windowHeight, f95, 30.0F, 1.0F, GuiTheme.windowColor);
        } else if (GuiTheme.isGradientMode()) {
            float f6 = (float)(System.currentTimeMillis() % 1000000L) / 1000.0F;
            float f123 = this.windowX;
            float f53 = this.windowY;
            float f129 = this.windowWidth;
            float f137 = this.windowHeight;
            Color color5 = Color.black;
            Color color6 = GuiTheme.accentColor1;
            float f54 = 1.0F;
            float f55 = f95;
            float f56 = f137;
            float f57 = f129;
            float f58 = f53;
            float f59 = f123;
            Render2DUtil.drawBackground(f59, f58, f57, f56, f55, f54, color6, color5, f6);
        } else {
            float f109 = this.windowX;
            float f60 = this.windowY;
            Render2DUtil.drawRoundedRect(f109, f60, this.windowWidth, this.windowHeight, f95, 1.0F, GuiTheme.windowColor);
        }

        float f110 = this.windowX;
        float f61 = this.windowY;
        Render2DUtil.drawOutline(f110, f61, this.windowWidth, this.windowHeight, f95, 1.0F, GuiTheme.borderThickness, GuiTheme.outlineColor);
        k7 = this.sidebarWidth;
        i7 = this.windowX;
        float f111 = i7 + k7;
        i7 = this.windowY;
        float f124 = i7 + 8;
        i7 = this.windowHeight;
        float f62 = i7 + -16 + 1;
        Color color7 = GuiTheme.accentColor1;
        float f63 = 0.8F;
        float f64 = f62;
        float f65 = f124;
        float f66 = f111;
        Render2DUtil.drawFadeLineVertical(f66, f65, f64, f63, color7);
        GradientFontEffect gradientfonteffect = new GradientFontEffect(
            GuiTheme.accentColor1, GuiTheme.accentColor2, 16.0F, new float[]{VoilClient.fonts.getMedia().getWidth(IconGlyphs.iconLetterE(), 90.0F)}
        );
        MSDFFontAtlas msdffontatlas = VoilClient.fonts.getMedia();
        String s13 = IconGlyphs.iconLetterE();
        i7 = this.windowX;
        float f128 = i7 + 26;
        i7 = this.windowY;
        msdffontatlas.renderWithEffect(s13, f128, i7 + -21 + 1, 90.0F, gradientfonteffect);
        if (this.activePanel != null) {
            this.activePanel.render2(matrixstack, i, j, f);
        }

        if (this.currentPage == ClickGuiTab.HOME && (Boolean)ClientSettings.searchBarEnabled.getValue()) {
            i7 = this.windowWidth;
            i7 = this.windowX + (i7 + -141 + 1) / 2;
            int i1 = i7 + -11 + 1;
            i7 = this.windowY;
            i7 += 8;
            int j1 = i7 + 2;
            double d2 = this.searchFocused ? 1.0 : 0.0;
            this.searchFocusAnimation.setTarget(d2);
            this.searchFocusAnimation.update();
            float f7 = this.searchFocusAnimation.getFloat();
            if (GuiTheme.isBlurMode()) {
                float f112 = i1 + 180;
                float f67 = j1 + -3 + 1;
                f128 = 140;
                Color color8 = GuiTheme.moduleColor;
                Render2DUtil.drawRoundedRect(f112, f67, f128, 20.0F, 6.0F, 1.0F, ColorUtil.withAlpha(color8, 128));
            } else if ((Boolean)ClientSettings.translucentSearchBar.getValue()) {
                float f113 = i1 + 180;
                float f68 = j1 + -3 + 1;
                f128 = 140;
                Color color9 = GuiTheme.moduleColor;
                Render2DUtil.drawRoundedRect(f113, f68, f128, 20.0F, 6.0F, 1.0F, ColorUtil.withAlpha(color9, 128));
            } else {
                float f114 = i1 + 180;
                float f69 = j1 + -3 + 1;
                Render2DUtil.drawRoundedRect(f114, f69, 140, 20.0F, 6.0F, 1.0F, GuiTheme.moduleColor);
            }

            float f115 = i1 + 180;
            float f70 = j1 + -3 + 1;
            f128 = 140;
            Color color10 = ColorUtil.scaleBrightness(GuiTheme.outlineColor, 1.1F);
            float f71 = 0.6F;
            float f72 = 0.3F;
            float f73 = 6.0F;
            float f74 = 20.0F;
            float f75 = f128;
            float f76 = f70;
            float f77 = f115;
            Render2DUtil.drawOutline(f77, f76, f75, f74, f73, f72, f71, color10);
            if (f7 > 0.01F) {
                float f116 = i1 + 180;
                float f78 = j1 + -3 + 1;
                f128 = 140;
                float f138 = 0.6F * f7;
                int k2 = (int)(255.0F * f7);
                Color color11 = GuiTheme.accentColor1;
                Render2DUtil.drawOutline(f116, f78, f128, 20.0F, 6.0F, 0.4F, f138, ColorUtil.withAlpha(color11, k2));
            }

            if (this.searchFocused) {
                VoilClient.fonts.getPoppins().render(this.searchText, i1 + 205, j1 + 1, new Color(11184810));
            } else {
                VoilClient.fonts.getPoppins().render("Search", i1 + 205, j1 + 1, new Color(11184810));
            }

            long k1 = System.currentTimeMillis();
            if (k1 - this.openTimestamp >= 800L) {
                this.caretVisible = !this.caretVisible;
                this.openTimestamp = k1;
            }

            double d3 = this.searchFocused && this.caretVisible ? 1.0 : 0.0;
            this.caretAnimation.setTarget(d3);
            this.caretAnimation.update();
            float f9 = this.caretAnimation.getFloat();
            if (this.searchFocused && f9 > 0.02F) {
                float f10 = i1 + 205 + VoilClient.fonts.getPoppins().getWidth(this.searchText) + 1.5F;
                float f79 = j1 + 3;
                int l2 = (int)(220.0F * f9);
                Color color12 = Color.WHITE;
                Render2DUtil.drawRoundedRect(f10, f79, 0.5F, 10.0F, 0.5F, 1.0F, ColorUtil.withAlpha(color12, l2));
            }

            Color color24 = ColorUtil.lerpColor(new Color(10066329), Color.WHITE, f7);
            VoilClient.fonts.getIcons().renderWithShadow(IconGlyphs.iconE155(), i1 + 185.0F, j1 + 1, 14.0F, color24.getRGB());
        }

        k7 = this.sidebarWidth;
        i7 = this.windowX;
        i7 += k7;
        int l = i7 + 34;
        if (this.currentPage == ClickGuiTab.HOME) {
            MSDFFontAtlas msdffontatlas1 = VoilClient.fonts.getIcons();
            Category category1 = this.selectedCategory;
            String s14 = IconGlyphs.getCategoryIcon(category1);
            f128 = l + -17 + 1;
            i7 = this.windowY;
            msdffontatlas1.renderWithShadow(s14, f128, i7 + 12, 14.0F, -1);
            MSDFFontAtlas msdffontatlas2 = VoilClient.fonts.getInterBold();
            f128 = l;
            i7 = this.windowY;
            msdffontatlas2.render(this.selectedCategory.name, f128, i7 + 12, new Color(16777215));
        }

        i7 = this.windowY;
        int l4 = i7 + 232;
        String s8 = (String)ClientSettings.username.getValue();
        String s9 = String.valueOf(ClientSettings.userRank.getValue());
        float f8 = 7.5F;
        float f98 = VoilClient.fonts.getSelectedFont().getWidth(s9, f8);
        float f99 = 2.0F;
        float f11 = f98 + f99 * 2.0F;
        i7 = this.windowX;
        float f12 = i7 + 121 - 4.0F - f11;
        i7 = this.windowX;
        float f13 = f12 - (i7 + 52) - 4.0F;
        float f14 = 10.5F;

        for (float f15 = VoilClient.fonts.getSelectedFont().getWidth(s8, f14);
            f15 > f13 && f14 > 6.0F;
            f15 = VoilClient.fonts.getSelectedFont().getWidth(s8, f14)
        ) {
            f14 -= 0.25F;
        }

        if (ClientSettings.userAvatarTexture == null) {
            if (ClientSettings.userAvatarUrl != null && !ClientSettings.userAvatarUrl.isBlank()) {
                ClientSettings.userAvatarTexture = PlayerTargetUtil.textureFromBase64(ClientSettings.userAvatarUrl);
            } else {
                ClientSettings.userAvatarTexture = (GlTexture)PlayerTargetUtil.defaultSkinTextures.key();
            }
        }

        if (ClientSettings.activeAvatarTexture == null) {
            if (ClientSettings.activeAvatarUrl != null && !ClientSettings.activeAvatarUrl.isBlank()) {
                ClientSettings.activeAvatarTexture = PlayerTargetUtil.textureFromBase64(ClientSettings.activeAvatarUrl);
            } else {
                ClientSettings.activeAvatarTexture = (GlTexture)PlayerTargetUtil.defaultSkinTextures.key();
            }
        }

        i7 = this.windowX;
        float f117 = i7 + 16;
        float f80 = l4 + 35;
        Render2DUtil.drawRectCornerRadii(f117, f80, 105.0F, 20.0F, 0.0F, 0.0F, 10.0F, 10.0F, 1.0F, GuiTheme.leftPanel);
        TextureRectangleRenderer texturerectanglerenderer = Render2DUtil.textureRenderer;
        GlTexture gltexture2 = ClientSettings.activeAvatarTexture;
        i7 = this.windowX;
        f128 = i7 + 16;
        i7 = l4 + 35;
        float f130 = i7 + -36 + 1;
        float[] afloat2 = new float[]{0.0F, 0.0F, 12.0F, 12.0F};
        Color color2 = ColorUtil.scaleBrightness(GuiTheme.moduleColor, 1.1F);
        float f45 = 1.0F;
        float[] afloat1 = afloat2;
        float f46 = 35.0F;
        float f47 = 105.0F;
        float f48 = f130;
        float f49 = f128;
        GlTexture gltexture = gltexture2;
        texturerectanglerenderer.render(gltexture, f49, f48, f47, f46, afloat1, f45, color2);
        i7 = this.windowX;
        float f118 = i7 + 16;
        float f81 = l4;
        Color color13 = Color.black;
        Color color14 = ColorUtil.withAlpha(color13, 60);
        float f82 = 1.0F;
        float f83 = 0.0F;
        float f84 = 0.0F;
        float f85 = 10.0F;
        float f86 = 10.0F;
        float f87 = 35.0F;
        float f88 = 105.0F;
        float f89 = f81;
        float f90 = f118;
        Render2DUtil.drawRectCornerRadii(f90, f89, f88, f87, f86, f85, f84, f83, f82, color14);
        Render2DUtil.drawOutlineGradient(
            this.windowX + 15.5F,
            l4,
            106.0F,
            56.0F,
            10.0F,
            0.5F,
            0.5F,
            GuiTheme.accentColor2,
            GuiTheme.accentColor1,
            GuiTheme.accentColor2,
            GuiTheme.accentColor1
        );
        MSDFFontAtlas msdffontatlas3 = VoilClient.fonts.getSelectedFont();
        i7 = this.windowX;
        f128 = i7 + 52;
        i7 = this.windowY;
        msdffontatlas3.renderWithShadow(s8, f128, i7 + 234, f14, -1);
        float f16 = VoilClient.fonts.getSelectedFont().getLineHeight(f8);
        float f17 = f16 + 2.0F;
        i7 = this.windowY;
        float f18 = i7 + 234 + (VoilClient.fonts.getSelectedFont().getLineHeight(f14) - f17) / 2.0F;
        Color color = GuiTheme.accentColor1;
        Color color1 = ColorUtil.scaleBrightness(GuiTheme.accentColor1, 0.3F);
        Render2DUtil.drawRoundedRect(f12, f18, f11, f17, 3.0F, 1.0F, color);
        VoilClient.fonts.getSelectedFont().render(s9, f12 + f99, f18 + (f17 - f16) / 2.0F, f8, color1.getRGB());
        MSDFFontAtlas msdffontatlas4 = VoilClient.fonts.getGreyscale();
        String s4 = String.valueOf(ClientSettings.userId.getValue());
        String s15 = "UID: " + s4;
        i7 = this.windowX;
        f128 = i7 + 52;
        i7 = this.windowY;
        msdffontatlas4.render(s15, f128, i7 + 246, 9.5F, Color.lightGray.getRGB());
        Color color25 = Color.WHITE;
        Color color26 = GuiTheme.accentColor1;
        Color color27 = this.currentPage == ClickGuiTab.CONFIG ? color25 : color26;
        color = this.currentPage == ClickGuiTab.GUI ? color25 : color26;
        color1 = this.currentPage == ClickGuiTab.IRC ? color25 : color26;
        i7 = this.windowX;
        float f19 = i7 + 16;
        float f20 = (105.0F - 5 * 13.0F) / 6;
        float f21 = f19 + f20;
        i7 = this.windowY;
        float f22 = i7 + 270;
        float f23 = VoilClient.fonts.getIcons().getLineHeight(13.0F);
        float[] afloat = new float[5];

        for (byte b0 = 0; b0 < 5; b0 += 1) {
            afloat[b0] = f21 + b0 * (13.0F + f20);
        }

        Color[] acolor = new Color[]{color27, color, color1, color26, new Color(250, 58, 48)};
        String[] astring = new String[]{IconGlyphs.iconE0D0(), IconGlyphs.iconE158(), IconGlyphs.iconE05D(), IconGlyphs.iconE417(), IconGlyphs.iconE555()};

        for (byte b1 = 0; b1 < 5; b1 += 1) {
            float f24 = afloat[b1];
            float f125 = f24 - 2.0F;
            f128 = f22 - 2.0F;
            f130 = f24 + 13.0F + 2.0F;
            float f131 = f22 + f23 + 2.0F;
            double d4 = i;
            boolean flag = this.isInside(f125, f128, f130, f131, d4, j);
            UiAnimation uianimation = this.iconHoverAnimations[b1];
            double d5 = flag ? 1.0 : 0.0;
            uianimation.setTarget(d5);
            this.iconHoverAnimations[b1].update();
            float f25 = this.iconHoverAnimations[b1].getFloat();
            float f26 = Math.max(0.0F, Math.min(1.0F, f25));
            float f27 = -1.5F * f25;
            float f28 = VoilClient.fonts.getIcons().getWidth(astring[b1], 13.0F);
            float f29 = f24 + (13.0F - f28) / 2.0F;
            VoilClient.fonts.getIcons().render(astring[b1], f29, f22 + f27, 13.0F, acolor[b1].getRGB());
            if (f26 > 0.03F) {
                float f30 = 13.0F * f26;
                float f31 = f24 + (13.0F - f30) / 2.0F;
                float f91 = f22 + f23;
                f130 = 1.5F * f26;
                Color color31 = acolor[b1];
                int i3 = (int)(140.0F * f26);
                Color color15 = color31;
                Render2DUtil.drawRoundedRect(f31, f91, f30, f130, 0.75F, 1.0F, ColorUtil.withAlpha(color15, i3));
            }
        }

        i7 = this.windowX;
        f128 = i7 + 21;
        i7 = this.windowY;
        float f50 = i7 + 237;
        float f51 = f128;
        GlTexture gltexture1 = ClientSettings.userAvatarTexture;
        Render2DUtil.textureRenderer.render(gltexture1, f51, f50, 25.0F, 25.0F, 12.5F, 1.0F);
        i7 = this.windowY;
        float f100 = i7 + 80;
        i7 = this.windowX;
        float f119 = i7 + 21 - 0.4F;
        i7 = this.windowY;
        float f92 = i7 + 237 - 0.4F;
        Render2DUtil.drawOutline(f119, f92, 26.8F, 26.8F, 12.9F, 0.3F, 0.4F, GuiTheme.accentColor1);
        VoilClient.fonts.getInterBold().render("Categories", this.windowX + 44, f100 - 32.0F, GuiTheme.normalTextColor);
        if (this.selectedModule != null) {
            if (this.categorySlideOffset > -80) {
                i7 = this.categorySlideOffset;
                this.categorySlideOffset = i7 + -101 + 1;
            }
        } else if (this.categorySlideOffset < 0) {
            i7 = this.categorySlideOffset;
            this.categorySlideOffset = i7 + 5;
        }

        byte b2 = 0;
        Category[] acategory = Category.values();
        int j5 = acategory.length;

        for (byte b3 = 0; b3 < j5; b3 += 1) {
            Category category2 = acategory[b3];
            switch (category2) {
                case Combat:
                    Identifier.of("tulip", "icons/swords.png");
                    break;
                case Movement:
                    Identifier.of("tulip", "icons/chevrons-right.png");
                    break;
                case Render:
                    Identifier.of("tulip", "icons/sparkles.png");
                    break;
                case Player:
                    Identifier.of("tulip", "icons/person-standing.png");
                    break;
                case Miscellaneous:
                    Identifier.of("tulip", "icons/expand.png");
                    break;
                case Client:
                    Identifier.of("tulip", "icons/user.png");
                    break;
                case CrystalPvP:
                    Identifier.of("tulip", "icons/aperture.png");
                    break;
                default:
                    Identifier.of("tulip", "icons/default.png");
            }

            boolean flag3 = this.currentPage == ClickGuiTab.HOME && category2 == this.selectedCategory;
            if (flag3) {
                float f101 = 9.0F;
                float f102 = VoilClient.fonts.getSelectedFont().getWidth(this.selectedCategory.name);
                float f32 = f102 + f101 * 2.0F;
                float f33 = this.windowX + 25 - f101;
                float f34 = f100 - 8.0F;
                if (this.draggingWindow) {
                    this.highlightX.snapTo(f33);
                    this.highlightWidth.snapTo(f32);
                    this.highlightY.snapTo(f34);
                } else {
                    double d6 = f33;
                    this.highlightX.setTarget(d6);
                    double d7 = f32;
                    this.highlightWidth.setTarget(d7);
                    double d8 = f34;
                    this.highlightY.setTarget(d8);
                    this.highlightX.update();
                    this.highlightWidth.update();
                    this.highlightY.update();
                }

                float f35 = this.highlightX.getFloat();
                float f36 = this.highlightWidth.getFloat();
                float f37 = this.highlightY.getFloat();
                Color color16 = GuiTheme.accentColor1;
                Render2DUtil.drawRoundedRect(f35, f37, f36, 15.0F, 4.0F, 1.0F, ColorUtil.withAlpha(color16, 160));
                Render2DUtil.drawOutline(f35, f37, f36, 15.0F, 4.0F, 0.3F, 0.4F, GuiTheme.accentColor1);
                MSDFFontAtlas msdffontatlas5 = VoilClient.fonts.getSelectedFont();
                i7 = this.windowX;
                msdffontatlas5.render(category2.name, i7 + 32, f100 - 7.0F, 10.0F, -1);
                this.categoryAnimations[b2].snapTo(0.0);
                this.moduleButtons.clear();
                float f38 = 40.0F;
                Category category = this.selectedCategory;

                for (Module module : ModuleManager.INSTANCE.getModulesByCategory(category)) {
                    this.moduleButtons.add(new ModuleButton(module, -20.0, f38, this));
                    f38 += 40.0F;
                }
            } else {
                i7 = this.windowX;
                float f126 = i7 + 14;
                f128 = f100 - 8.0F;
                i7 = this.windowX + 14;
                f130 = i7 + 120;
                float f132 = f100 + 7.0F;
                double d9 = i;
                boolean flag4 = this.isInside(f126, f128, f130, f132, d9, j);
                UiAnimation uianimation1 = this.categoryAnimations[b2];
                double d10 = flag4 ? 1.0 : 0.0;
                uianimation1.setTarget(d10);
                this.categoryAnimations[b2].update();
                float f103 = this.categoryAnimations[b2].getFloat();
                Color color28 = ColorUtil.lerpColor(GuiTheme.unfocusedTextColor, Color.WHITE, f103 * 0.4F);
                MSDFFontAtlas msdffontatlas6 = VoilClient.fonts.getSelectedFont();
                i7 = this.windowX;
                msdffontatlas6.render(category2.name, i7 + 32 + 2.0F * f103, f100 - 7.0F, 10.0F, color28.getRGB());
            }

            MSDFFontAtlas msdffontatlas7 = VoilClient.fonts.getIcons();
            String s16 = IconGlyphs.getCategoryIcon(category2);
            i7 = this.windowX;
            msdffontatlas7.render(s16, i7 + 18, f100 - 7.0F, 12.0F, Color.white.getRGB());
            f100 += 20.0F;
            b2 += 1;
        }

        int i5 = this.windowX;
        i7 = this.windowY;
        j5 = i7 + 30;
        int k5 = this.windowWidth;
        i7 = this.windowHeight;
        int l5 = i7 + -32 + 1;
        UiRect uirect = new UiRect(i5, j5, k5, l5);
        int j3 = uirect.x + uirect.width;
        k7 = uirect.height;
        i7 = uirect.y;
        RenderManager.setScissor(uirect.x, uirect.y, j3, i7 + k7);
        UiRect uirect1 = new UiRect(uirect.x, 0, uirect.width, 0);
        int i6 = 45;
        boolean flag5 = false;

        for (ModuleButton modulebutton : this.moduleButtons) {
            Module module1 = modulebutton.getModule();
            boolean flag8 = module1.getCategory().equals(this.selectedCategory);
            boolean flag9 = module1.getName().toLowerCase().contains(this.searchText.toLowerCase());
            boolean flag10 = false;
            if ((Boolean)ClientSettings.searchSettings.getValue() && !this.searchText.isEmpty()) {
                for (Setting setting1 : module1.getSettings()) {
                    boolean flag1 = setting1.getName().toLowerCase().contains(this.searchText.toLowerCase());
                    modulebutton.setExpanded(setting1, flag1);
                    if (flag1) {
                        flag10 = true;
                    }
                }
            } else {
                for (Setting setting : module1.getSettings()) {
                    modulebutton.setExpanded(setting, false);
                }
            }

            if (flag10) {
                module1.setExpanded(true);
            } else if (this.searchActive && !flag9) {
                module1.setExpanded(false);
            }

            boolean flag11 = flag8 && (flag9 || flag10);
            module1.setHidden(!flag11);
            if (flag11) {
                flag5 = true;
                if (this.currentPage == ClickGuiTab.HOME) {
                    double d21 = i6;
                    double d11 = this.displayedScroll;
                    modulebutton.setPosition(d21, d11);
                    int k6 = (int)(modulebutton.getHeight() + 5.0F);
                    i7 = this.windowY;
                    i7 = (int)(i7 + i6 + this.displayedScroll);
                    uirect1.y = i7 + -11 + 1;
                    uirect1.height = k6 + 35;
                    if (uirect.overlaps(uirect1) || uirect1.overlaps(uirect)) {
                        modulebutton.render(matrixstack, i, j, f);
                    }

                    i6 += k6;
                }
            }
        }

        this.hasSearchResults = flag5;
        if (this.openedModule != null) {
            float f104 = 0.0F;
            boolean flag7 = false;

            for (ModuleButton modulebutton1 : this.moduleButtons) {
                if (!modulebutton1.getModule().isHidden()) {
                    if (modulebutton1.getModule() == this.openedModule) {
                        flag7 = true;
                        break;
                    }

                    f104 += modulebutton1.getHeight() + 5.0F;
                }
            }

            if (flag7) {
                double d15 = 0.0;

                for (ModuleButton modulebutton3 : this.moduleButtons) {
                    if (!modulebutton3.getModule().isHidden()) {
                        d15 += modulebutton3.getHeight() + 5.0F;
                    }
                }

                i7 = this.windowHeight;
                double d17 = i7 + -51 + 1;
                double d19 = Math.min(0.0, d17 - d15);
                this.targetScroll = Math.max(d19, Math.min(-f104 + 20.0F, 0.0F));
                this.lastScrollTime = System.currentTimeMillis();
            }

            this.openedModule = null;
        }

        RenderManager.disableScissor();
        if (this.currentPage == ClickGuiTab.HOME) {
            double d14 = 0.0;

            for (ModuleButton modulebutton2 : this.moduleButtons) {
                if (!modulebutton2.getModule().isHidden()) {
                    d14 += modulebutton2.getHeight() + 5.0F;
                }
            }

            i7 = this.windowHeight;
            double d16 = i7 + -51 + 1;
            double d18 = Math.min(0.0, d16 - d14);
            double d20 = Math.pow(1.0E-4, f1);
            if (this.targetScroll > 0.0) {
                this.targetScroll *= d20;
                if (this.targetScroll < 0.4) {
                    this.targetScroll = 0.0;
                }
            } else if (this.targetScroll < d18) {
                this.targetScroll = d18 + (this.targetScroll - d18) * d20;
                if (this.targetScroll > d18 - 0.4) {
                    this.targetScroll = d18;
                }
            }

            if (d14 > d16) {
                if (!GuiTheme.isBlurMode() && !GuiTheme.isGradientMode()) {
                    k7 = this.sidebarWidth;
                    i7 = this.windowX;
                    i7 += k7;
                    float f120 = i7 + 2;
                    k7 = this.windowHeight;
                    i7 = this.windowY;
                    i7 += k7;
                    float f127 = i7 + -17 + 1;
                    k7 = this.sidebarWidth;
                    i7 = this.windowWidth;
                    i7 -= k7;
                    f128 = i7 + -10 + 1;
                    Color color17 = GuiTheme.windowColor;
                    Color color32 = ColorUtil.withAlpha(color17, 0);
                    Color color18 = GuiTheme.windowColor;
                    Render2DUtil.drawRectGradient(
                        f120, f127, f128, 15.0F, 0.0F, 1.0F, color32, ColorUtil.withAlpha(color18, 0), GuiTheme.windowColor, GuiTheme.windowColor
                    );
                }

                k7 = this.windowWidth;
                i7 = this.windowX;
                i7 += k7;
                float f107 = i7 - 5;
                i7 = this.windowY;
                float f39 = i7 + 38;
                float f40 = this.windowHeight - 46;
                float f41 = (float)Math.max(18.0, f40 * (d16 / d14));
                double d0 = d14 - d16;
                float f42 = (float)(-this.displayedScroll / d0);
                f42 = Math.max(0.0F, Math.min(1.0F, f42));
                float f43 = f39 + (f40 - f41) * f42;
                boolean flag2 = System.currentTimeMillis() - this.lastScrollTime < 900L || Math.abs(this.targetScroll - this.displayedScroll) > 0.5;
                double d12 = flag2 ? 1.0 : 0.0;
                this.scrollbarAnimation.setTarget(d12);
                this.scrollbarAnimation.update();
                float f44 = this.scrollbarAnimation.getFloat();
                if (f44 > 0.02F) {
                    int k3 = (int)(70.0F * f44);
                    Color color19 = Color.black;
                    Render2DUtil.drawRoundedRect(f107, f39, 2.0F, f40, 1.0F, 1.0F, ColorUtil.withAlpha(color19, k3));
                    int l3 = (int)(180.0F * f44);
                    Color color20 = GuiTheme.accentColor1;
                    Render2DUtil.drawRoundedRect(f107, f43, 2.0F, f41, 1.0F, 1.0F, ColorUtil.withAlpha(color20, l3));
                }
            }
        }

        this.updateSuggestions();
        boolean flag6 = !flag5 && this.currentPage == ClickGuiTab.HOME && !this.searchText.isEmpty();
        double d13 = flag6 ? 1.0 : 0.0;
        this.listFadeAnimation.setTarget(d13);
        this.listFadeAnimation.update();
        float f105 = this.listFadeAnimation.getFloat();
        if (f105 > 0.02F) {
            int j6 = this.isSuggestionListEmpty();
            String s12;
            if (j6 >= 0) {
                String s11 = this.selectedCategory.name;
                String s5 = ((Module)this.searchSuggestions.get(j6)).getName();
                String s6 = s11;
                s12 = "Not in " + s6 + " - Enter to jump to " + s5;
            } else {
                String s7 = this.searchText;
                s12 = "No modules found for \"" + s7 + "\"";
            }

            String s10 = s12;
            Color color30 = ColorUtil.scaleBrightness(Color.darkGray, 1.5F);
            int i4 = (int)(255.0F * f105);
            Color color21 = color30;
            Color color29 = ColorUtil.withAlpha(color21, i4);
            VoilClient.fonts
                .getSelectedFont()
                .renderCenteredString(s10, this.windowX + this.windowWidth / 2.0F + 60.0F, this.windowY + this.windowWidth / 6.0F, color29.getRGB());
            MSDFFontAtlas msdffontatlas8 = VoilClient.fonts.getIcons();
            String s17 = IconGlyphs.iconE155();
            f128 = this.windowX + this.windowWidth / 2.0F + 50.0F;
            f130 = this.windowY + this.windowWidth / 10.0F;
            int j4 = (int)(200.0F * f105);
            Color color22 = Color.darkGray;
            msdffontatlas8.render(s17, f128, f130, 32.0F, ColorUtil.withAlpha(color22, j4).getRGB());
        }

        this.pageTransitionAnimation.update();
        float f106 = this.pageTransitionAnimation.getFloat();
        if (f106 > 0.02F) {
            k7 = this.sidebarWidth;
            i7 = this.windowX;
            i7 += k7;
            float f121 = i7 + 1;
            float f93 = this.windowY + 1;
            k7 = this.sidebarWidth;
            i7 = this.windowWidth;
            i7 -= k7;
            f128 = i7 + -3 + 1;
            i7 = this.windowHeight;
            f130 = i7 + -3 + 1;
            int k4 = (int)(255.0F * f106);
            Color color23 = GuiTheme.windowColor;
            Render2DUtil.drawRectCornerRadii(
                f121, f93, f128, f130, 0.0F, GuiTheme.radiusTopLeft, 0.0F, GuiTheme.radiusTopLeft, 1.0F, ColorUtil.withAlpha(color23, k4)
            );
        }

        RenderManager.disableScissor();
        this.drawSortBar(i, j);
        this.drawSuggestions(i, j);
        this.drawChangelog(i, j);
    }

    private String getReleaseVersion() {
        ReleaseNotes releasenotes = VoilClient.getReleaseNotes();
        return releasenotes != null && !releasenotes.getEntries().isEmpty() ? ((ReleaseEntry)releasenotes.getEntries().getLast()).version() : "";
    }

    private float getSearchBoxX() {
        int i = this.windowWidth;
        int j = (i + -141 + 1) / 2;
        i = this.windowX;
        i = i + j - 10;
        return i + 180;
    }

    private void drawSortBar(int i, int j) {
        if (this.currentPage == ClickGuiTab.HOME) {
            String s = String.valueOf(ClientSettings.moduleSortMode.getValue());
            boolean flag = "Favourite".equals(s);
            boolean flag1 = "Alphabetical".equals(s);
            boolean flag2 = "Enabled".equals(s);
            float f = this.getSortBarX();
            float f1 = this.windowY + 8.0F;
            Render2DUtil.drawRoundedRect(f, f1, 98.0F, 20.0F, 7.0F, 1.0F, GuiTheme.moduleColor);
            Color color = new Color(7237240);
            Color color1 = GuiTheme.accentColor1;
            Color color2 = GuiTheme.modeSettingBg;
            float f2 = f1 + 2.5F;
            float f3 = f1 + 6.0F;
            float f20 = f + 26.0F;
            float f21 = f1 + 20.0F;
            double d0 = i;
            boolean flag3 = this.isInside(f, f1, f20, f21, d0, j);
            UiAnimation uianimation = this.sortButtonAnimations[0];
            double d1 = flag3 ? 1.0 : 0.0;
            uianimation.setTarget(d1);
            this.sortButtonAnimations[0].update();
            float f4 = this.sortButtonAnimations[0].getFloat();
            if (flag) {
                Render2DUtil.drawRoundedRect(f + 4.0F, f2, 18.0F, 15.0F, 5.0F, 1.0F, ColorUtil.withAlpha(color1, 45));
            }

            Color color3 = flag ? color1 : ColorUtil.lerpColor(color, Color.WHITE, f4);
            float f5 = f + (26.0F - 11.0F) / 2.0F;
            float f6 = f1 + (20.0F - 11.0F) / 2.0F;
            if (flag) {
                Render2DUtil.drawStarFilledRotated(f5, f6, 11.0F, color3, null, 0.0F, 0.8F);
            } else {
                Render2DUtil.drawStarFilledRotated(f5, f6, 11.0F, null, color3, 0.5F, 0.8F);
            }

            float f18 = f + 26.0F;
            Render2DUtil.drawRoundedRect(f18, f3, 0.5F, 8.0F, 0.25F, 1.0F, color2);
            float f7 = f + 26.0F;
            f20 = f7 + 38.0F;
            f21 = f1 + 20.0F;
            double d2 = i;
            boolean flag4 = this.isInside(f7, f1, f20, f21, d2, j);
            uianimation = this.sortButtonAnimations[1];
            double d3 = flag4 ? 1.0 : 0.0;
            uianimation.setTarget(d3);
            this.sortButtonAnimations[1].update();
            float f8 = this.sortButtonAnimations[1].getFloat();
            if (flag1) {
                Render2DUtil.drawRoundedRect(f7 + 5.0F, f2, 28.0F, 15.0F, 5.0F, 1.0F, ColorUtil.withAlpha(color1, 45));
            }

            Color color4 = flag1 ? color1 : ColorUtil.lerpColor(color, Color.WHITE, f8);
            float f9 = VoilClient.fonts.getQuicksand().getWidth("A-Z", 9.0F);
            float f10 = VoilClient.fonts.getQuicksand().getLineHeight(9.0F);
            VoilClient.fonts.getQuicksand().render("A-Z", f7 + (38.0F - f9) / 2.0F, f1 + (20.0F - f10) / 2.0F, 9.0F, color4.getRGB());
            Render2DUtil.drawRoundedRect(f7 + 38.0F, f3, 0.5F, 8.0F, 0.25F, 1.0F, color2);
            float f11 = f7 + 38.0F;
            f20 = f11 + 34.0F;
            f21 = f1 + 20.0F;
            double d4 = i;
            boolean flag5 = this.isInside(f11, f1, f20, f21, d4, j);
            uianimation = this.sortButtonAnimations[2];
            double d5 = flag5 ? 1.0 : 0.0;
            uianimation.setTarget(d5);
            this.sortButtonAnimations[2].update();
            float f12 = this.sortButtonAnimations[2].getFloat();
            double d6 = flag2 ? 1.0 : 0.0;
            this.enabledToggleAnimation.setTarget(d6);
            this.enabledToggleAnimation.update();
            float f13 = Math.max(0.0F, Math.min(1.0F, this.enabledToggleAnimation.getFloat()));
            float f14 = f11 + (34.0F - 18.0F) / 2.0F;
            float f15 = f1 + (20.0F - 9.0F) / 2.0F;
            Color color5 = ColorUtil.lerpColor(ColorUtil.lerpColor(new Color(3947589), new Color(4868693), f12), color1, f13);
            Render2DUtil.drawRoundedRect(f14, f15, 18.0F, 9.0F, 9.0F / 2.0F, 1.0F, color5);
            float f16 = 9.0F - 3.0F;
            float f17 = f14 + 1.5F + (18.0F - f16 - 3.0F) * f13;
            Color color6 = ColorUtil.lerpColor(new Color(5921378), Color.WHITE, Math.max(f13, f12));
            float f19 = f15 + 1.5F;
            Render2DUtil.drawRoundedRect(f17, f19, f16, f16, f16 / 2.0F, 1.0F, color6);
        }
    }

    private void openModule(Module module) {
        this.selectedCategory = module.getCategory();
        this.setPage(ClickGuiTab.HOME);
        ModuleManager.INSTANCE.init();
        module.setExpanded(true);
        this.selectedModule = null;
        this.searchText = "";
        this.searchFocused = false;
        this.displayedScroll = 0.0;
        this.targetScroll = 0.0;
        this.scrollAnimation.snapTo(0.0);
        this.suggestionRects.clear();
        this.selectedSuggestionIndex = -1;
        this.openedModule = module;
    }

    public boolean isInside(float f, float f1, float f2, float f3, double d0, double d1) {
        return d0 >= f && d0 <= f2 && d1 >= f1 && d1 <= f3;
    }

    private float getScale() {
        float f1 = this.windowWidth + 20.0F;
        float f = this.windowHeight + 20.0F;
        return GuiScale.getAutoScale(f1, f);
    }

    private void drawChangelog(int i, int j) {
        double d0 = this.changelogVisible ? 1.0 : 0.0;
        this.changelogAnimation.setTarget(d0);
        this.changelogAnimation.update();
        float f = this.changelogAnimation.getFloat();
        float f1 = Math.max(0.0F, Math.min(1.0F, f));
        if (!(f1 < 0.02F)) {
            int k = (int)(255.0F * f1);
            float f50 = this.windowX;
            float f19 = this.windowY;
            float f51 = this.windowWidth;
            float f52 = this.windowHeight;
            int l = (int)(150.0F * f1);
            Color color1 = Color.black;
            Render2DUtil.drawRoundedRect(f50, f19, f51, f52, GuiTheme.radiusTopLeft, 1.0F, ColorUtil.withAlpha(color1, l));
            float f2 = 240.0F;
            float f3 = this.suggestions.size() * 22.0F;
            float f4 = this.windowHeight - 90 - 46.0F - 30.0F;
            float f5 = Math.min(f3, f4);
            float f6 = 46.0F + f5 + 30.0F;
            float f7 = this.windowX + (this.windowWidth - f2) / 2.0F;
            float f8 = this.windowY + (this.windowHeight - f6) / 2.0F;
            float f9 = 0.92F + 0.08F * f;
            float f10 = f7 + f2 / 2.0F;
            float f11 = f8 + f6 / 2.0F;
            f2 *= f9;
            f6 *= f9;
            f7 = f10 - f2 / 2.0F;
            f8 = f11 - f6 / 2.0F;
            double d1 = this.changelogScrollTarget;
            this.changelogScrollAnimation.setTarget(d1);
            this.changelogScrollAnimation.update();
            this.changelogScroll = this.changelogScrollAnimation.getValue();
            if (GuiTheme.isBlurMode()) {
                Render2DUtil.drawBlurRect(f7, f8, f2, f6, 10.0F, 14.0F);
            } else {
                Color color2 = GuiTheme.moduleColor;
                Color color3 = ColorUtil.withAlpha(color2, k);
                float f20 = 1.0F;
                float f21 = 10.0F;
                float f22 = f6;
                float f23 = f2;
                float f24 = f8;
                float f25 = f7;
                Render2DUtil.drawRoundedRect(f25, f24, f23, f22, f21, f20, color3);
            }

            Color color4 = GuiTheme.accentColor1;
            Color color5 = ColorUtil.withAlpha(color4, k);
            float f26 = 0.6F;
            float f27 = 0.4F;
            float f28 = 10.0F;
            float f29 = f6;
            float f30 = f2;
            float f31 = f8;
            float f32 = f7;
            Render2DUtil.drawOutline(f32, f31, f30, f29, f28, f27, f26, color5);
            MSDFFontAtlas msdffontatlas = VoilClient.fonts.getIcons();
            String s8 = IconGlyphs.iconE417();
            f51 = f7 + 12.0F;
            f52 = f8 + 12.0F;
            Color color6 = GuiTheme.accentColor1;
            msdffontatlas.render(s8, f51, f52, 12.0F, ColorUtil.withAlpha(color6, k).getRGB());
            MSDFFontAtlas msdffontatlas1 = VoilClient.fonts.getInterBold();
            String s3 = this.getReleaseVersion();
            s8 = "Voil " + s3;
            f51 = f7 + 28.0F;
            f52 = f8 + 12.0F;
            Color color7 = Color.WHITE;
            msdffontatlas1.render(s8, f51, f52, 12.0F, ColorUtil.withAlpha(color7, k).getRGB());
            MSDFFontAtlas msdffontatlas2 = VoilClient.fonts.getSelectedFont();
            f51 = f7 + 12.0F;
            f52 = f8 + 28.0F;
            Color color8 = new Color(9145238);
            msdffontatlas2.render("since your last session", f51, f52, 9.0F, ColorUtil.withAlpha(color8, k).getRGB());
            float f48 = f7 + f2 - 22.0F;
            f51 = f8 + 10.0F;
            f52 = f7 + f2 - 8.0F;
            float f53 = f8 + 24.0F;
            double d2 = i;
            boolean flag = this.isInside(f48, f51, f52, f53, d2, j);
            double d3 = flag ? 1.0 : 0.0;
            this.changelogCloseHoverAnimation.setTarget(d3);
            this.changelogCloseHoverAnimation.update();
            Color color9 = ColorUtil.lerpColor(new Color(6052966), Color.WHITE, this.changelogCloseHoverAnimation.getFloat());
            Color color19 = ColorUtil.withAlpha(color9, k);
            double d4 = f7 + f2 - 21.0F;
            Render2DUtil.drawPlus(color19, d4, f8 + 12.0F, 9.0F, 1.4F, 45.0F);
            float f12 = f8 + 46.0F;
            int k1 = (int)f7;
            int l1 = (int)f12;
            int i1 = (int)(f7 + f2);
            RenderManager.setScissor(k1, l1, i1, (int)(f12 + f5));
            float f13 = (float)(f12 + this.changelogScroll);

            for (ChangelogEntry changelogentry : this.suggestions) {
                if (f13 + 22.0F >= f12 && f13 <= f12 + f5) {
                    float f14 = VoilClient.fonts.getSelectedFont().getWidth(changelogentry.title(), 8.0F);
                    float f15 = f14 + 10.0F;
                    float f16 = 11.0F;
                    f50 = f7 + 12.0F;
                    Color color10 = changelogentry.endColor();
                    Render2DUtil.drawRoundedRect(f50, f13, f15, f16, 3.0F, 1.0F, ColorUtil.withAlpha(color10, k));
                    MSDFFontAtlas msdffontatlas3 = VoilClient.fonts.getSelectedFont();
                    s8 = changelogentry.title();
                    f51 = f7 + 12.0F + (f15 - f14) / 2.0F;
                    f52 = f13 + (f16 - VoilClient.fonts.getSelectedFont().getLineHeight(8.0F)) / 2.0F;
                    Color color11 = Color.WHITE;
                    msdffontatlas3.render(s8, f51, f52, 8.0F, ColorUtil.withAlpha(color11, k).getRGB());
                    float f17 = f7 + 12.0F + f15 + 6.0F;
                    MSDFFontAtlas msdffontatlas4 = VoilClient.fonts.getInterBold();
                    s8 = changelogentry.description();
                    f52 = f13 + 1.0F;
                    Color color12 = Color.WHITE;
                    msdffontatlas4.render(s8, f17, f52, 9.5F, ColorUtil.withAlpha(color12, k).getRGB());
                    if (!changelogentry.module().isEmpty()) {
                        float f18 = VoilClient.fonts.getInterBold().getWidth(changelogentry.description(), 9.5F);
                        MSDFFontAtlas msdffontatlas5 = VoilClient.fonts.getSelectedFont();
                        String s4 = changelogentry.module();
                        s8 = "- " + s4;
                        f51 = f17 + f18 + 4.0F;
                        f52 = f13 + 1.0F;
                        Color color13 = new Color(11974336);
                        msdffontatlas5.render(s8, f51, f52, 9.5F, ColorUtil.withAlpha(color13, k).getRGB());
                    }
                }

                f13 += 22.0F;
            }

            RenderManager.disableScissor();
            if (f3 > f5) {
                float f35 = f5 - 8.0F;
                float f37 = Math.max(16.0F, f35 * (f5 / f3));
                float f40 = f3 - f5;
                float f42 = f40 <= 0.0F ? 0.0F : (float)(-this.changelogScroll / f40);
                float f44 = f12 + 4.0F + (f35 - f37) * f42;
                f50 = f7 + f2 - 6.0F;
                float f33 = f12 + 4.0F;
                int j1 = (int)(80.0F * f1);
                Color color14 = Color.black;
                Render2DUtil.drawRoundedRect(f50, f33, 2.0F, f35, 1.0F, 1.0F, ColorUtil.withAlpha(color14, j1));
                f50 = f7 + f2 - 6.0F;
                Color color15 = GuiTheme.accentColor1;
                Render2DUtil.drawRoundedRect(f50, f44, 2.0F, f37, 1.0F, 1.0F, ColorUtil.withAlpha(color15, k));
            }

            float f36 = f8 + f6 - 24.0F;
            if (this.featuredModule != null) {
                float f38 = 16.0F;
                String s5 = this.featuredModule.getName();
                String s6 = "Show me " + s5;
                float f43 = VoilClient.fonts.getSelectedFont().getWidth(s6, 9.0F);
                float f45 = f43 + 18.0F;
                float f49 = f7 + 12.0F;
                f52 = f7 + 12.0F + f45;
                f53 = f36 + f38;
                double d5 = i;
                boolean flag1 = this.isInside(f49, f36, f52, f53, d5, j);
                double d6 = flag1 ? 1.0 : 0.0;
                this.showModuleHoverAnimation.setTarget(d6);
                this.showModuleHoverAnimation.update();
                f50 = f7 + 12.0F;
                Color color20 = GuiTheme.accentColor1;
                float f34 = 1.0F + 0.25F * this.showModuleHoverAnimation.getFloat();
                Color color16 = ColorUtil.scaleBrightness(color20, f34);
                Render2DUtil.drawRoundedRect(f50, f36, f45, f38, 5.0F, 1.0F, ColorUtil.withAlpha(color16, k));
                MSDFFontAtlas msdffontatlas6 = VoilClient.fonts.getSelectedFont();
                f51 = f7 + 12.0F + (f45 - f43) / 2.0F;
                f52 = f36 + (f38 - VoilClient.fonts.getSelectedFont().getLineHeight(9.0F)) / 2.0F;
                Color color17 = Color.WHITE;
                msdffontatlas6.render(s6, f51, f52, 9.0F, ColorUtil.withAlpha(color17, k).getRGB());
            }

            float f39 = 52.0F;
            float f41 = 16.0F;
            String s7 = "Dismiss";
            float f46 = VoilClient.fonts.getSelectedFont().getWidth(s7, 9.0F);
            float f47 = f7 + f2 - 12.0F - f39;
            f52 = f47 + f39;
            f53 = f36 + f41;
            double d7 = i;
            boolean flag2 = this.isInside(f47, f36, f52, f53, d7, j);
            double d8 = flag2 ? 1.0 : 0.0;
            this.dismissHoverAnimation.setTarget(d8);
            this.dismissHoverAnimation.update();
            Color color18 = new Color(3816005);
            Render2DUtil.drawOutline(f47, f36, f39, f41, 5.0F, 0.3F, 0.5F, ColorUtil.withAlpha(color18, k));
            Color color = ColorUtil.lerpColor(new Color(9145238), Color.WHITE, this.dismissHoverAnimation.getFloat());
            VoilClient.fonts
                .getSelectedFont()
                .render(
                    s7,
                    f47 + (f39 - f46) / 2.0F,
                    f36 + (f41 - VoilClient.fonts.getSelectedFont().getLineHeight(9.0F)) / 2.0F,
                    9.0F,
                    ColorUtil.withAlpha(color, k).getRGB()
                );
        }
    }

    public void prepareOpen() {
        ApiClient.INSTANCE.ping();
    }

    public void markOpened() {
        if (!this.closing) {
            this.closing = true;
            this.opening = false;
            this.lastOpenTime = System.currentTimeMillis();
        }
    }

    private void drawSuggestions(int i, int j) {
        boolean flag = this.currentPage == ClickGuiTab.HOME
            && (Boolean)ClientSettings.searchBarEnabled.getValue()
            && !this.searchText.isEmpty()
            && !this.searchSuggestions.isEmpty()
            && !this.changelogVisible;
        double d0 = flag ? 1.0 : 0.0;
        this.suggestionsAnimation.setTarget(d0);
        this.suggestionsAnimation.update();
        float f = Math.max(0.0F, Math.min(1.0F, this.suggestionsAnimation.getFloat()));
        this.suggestionRects.clear();
        if (!(f < 0.02F) && !this.searchSuggestions.isEmpty() && this.currentPage == ClickGuiTab.HOME) {
            MSDFFontAtlas msdffontatlas = VoilClient.fonts.getSelectedFont();
            float f1 = this.getSearchBoxX() + 140.0F;
            float f2 = this.windowY + 8.0F + 20.0F + 5.0F;
            long k = System.currentTimeMillis();
            int l = this.isSuggestionListEmpty();

            for (int i1 = 0; i1 < this.searchSuggestions.size(); i1++) {
                Module module = (Module)this.searchSuggestions.get(i1);
                float f3 = Math.max(0.0F, Math.min(1.0F, ((float)(k - this.suggestionsUpdatedAt) - i1 * 35.0F) / 180.0F));
                float f4 = 1.0F - (float)Math.pow(1.0F - f3, 3.0);
                float f5 = f4 * f;
                if (f5 < 0.02F) {
                    f2 += 17.0F + 3.0F;
                } else {
                    String s = module.getName();
                    String s1 = module.getCategory().name;
                    float f6 = msdffontatlas.getWidth(s, 9.0F);
                    float f7 = msdffontatlas.getWidth(s1, 7.5F);
                    float f8 = 6.0F + 10.0F + 5.0F + f6 + 7.0F + f7 + 7.0F;
                    float f9 = f1 - f8;
                    float f10 = f2 - 4.0F * (1.0F - f4);
                    float f17 = f9 + f8;
                    float f18 = f10 + 17.0F;
                    double d1 = i;
                    boolean flag1 = this.isInside(f9, f10, f17, f18, d1, j);
                    UiAnimation uianimation = this.suggestionHoverAnimations
                        .computeIfAbsent(module, module1 -> new UiAnimation(EasingType.FLUID_SMOOTH).setFluidParams(0.68, 0.15));
                    double d2 = !flag1 && i1 != l ? 0.0 : 1.0;
                    uianimation.setTarget(d2);
                    uianimation.update();
                    float f11 = Math.max(0.0F, Math.min(1.0F, uianimation.getFloat()));
                    int j1 = (int)(255.0F * f5);
                    int k1 = (int)(110.0F * f5);
                    Color color = Color.black;
                    Color color1 = ColorUtil.withAlpha(color, k1);
                    Render2DUtil.drawGlow(f9, f10, f8, 17.0F, 6.0F, 1.0F, 12.0F, color1);
                    Color color2 = ColorUtil.lerpColor(GuiTheme.moduleColor, ColorUtil.scaleBrightness(GuiTheme.moduleColor, 1.35F), f11);
                    Render2DUtil.drawRoundedRect(f9, f10, f8, 17.0F, 6.0F, 1.0F, ColorUtil.withAlpha(color2, j1));
                    Color color3 = ColorUtil.lerpColor(GuiTheme.outlineColor, GuiTheme.accentColor1, f11);
                    Render2DUtil.drawOutline(f9, f10, f8, 17.0F, 6.0F, 0.3F, 0.5F, ColorUtil.withAlpha(color3, j1));
                    float f12 = VoilClient.fonts.getIcons().getLineHeight(10.0F);
                    MSDFFontAtlas msdffontatlas1 = VoilClient.fonts.getIcons();
                    Category category = module.getCategory();
                    String s4 = IconGlyphs.getCategoryIcon(category);
                    float f16 = f9 + 6.0F;
                    f17 = f10 + (17.0F - f12) / 2.0F;
                    Color color4 = GuiTheme.accentColor1;
                    msdffontatlas1.render(s4, f16, f17, 10.0F, ColorUtil.withAlpha(color4, j1).getRGB());
                    float f13 = f9 + 6.0F + 10.0F + 5.0F;
                    f17 = f10 + (17.0F - msdffontatlas.getLineHeight(9.0F)) / 2.0F;
                    Color color5 = Color.WHITE;
                    msdffontatlas.render(s, f13, f17, 9.0F, ColorUtil.withAlpha(color5, j1).getRGB());
                    f16 = f13 + f6 + 7.0F;
                    f17 = f10 + (17.0F - msdffontatlas.getLineHeight(7.5F)) / 2.0F;
                    Color color6 = new Color(9145238);
                    msdffontatlas.render(s1, f16, f17, 7.5F, ColorUtil.withAlpha(color6, j1).getRGB());
                    if (f5 > 0.5F) {
                        this.suggestionRects.add(new SuggestionRect(module, f9, f10, f8, 17.0F));
                    }

                    f2 += 17.0F + 3.0F;
                }
            }

            if (this.hiddenSuggestionCount > 0) {
                int l1 = this.hiddenSuggestionCount;
                String s3 = "+" + l1 + " more";
                float f14 = msdffontatlas.getWidth(s3, 7.5F);
                float f15 = f1 - f14 - 4.0F;
                Color color8 = new Color(7237240);
                int i2 = (int)(255.0F * f);
                Color color7 = color8;
                msdffontatlas.render(s3, f15, f2, 7.5F, ColorUtil.withAlpha(color7, i2).getRGB());
            }
        }
    }

    private ChangelogEntry createChangelogEntry(String s, Color color, Color color1, String s1) {
        int i = s1.indexOf(": ");
        if (i < 0) {
            i = s1.indexOf(" - ");
            if (i >= 0) {
                String s4 = s1.substring(0, i).trim();
                String s5 = s1.substring(i + 3).trim();
                return new ChangelogEntry(s, color, color1, s4, s5);
            } else {
                return new ChangelogEntry(s, color, color1, s1.trim(), "");
            }
        } else {
            String s2 = s1.substring(0, i).trim();
            String s3 = s1.substring(i + 2).trim();
            return new ChangelogEntry(s, color, color1, s2, s3);
        }
    }

    private static String normalizeSearch(String s) {
        return s.toLowerCase(Locale.ROOT).replace(" ", "");
    }

    public ClickGuiTab getPage() {
        return this.currentPage;
    }

    private void updateSuggestions() {
        String s = normalizeSearch(this.searchText);
        boolean flag = this.currentPage == ClickGuiTab.HOME && (Boolean)ClientSettings.searchBarEnabled.getValue() && !s.isEmpty();
        if (!flag) {
            this.suggestionCacheKey = "";
        } else {
            int j = this.selectedCategory.ordinal();
            String s1 = j + "\u0000" + s;
            if (!s1.equals(this.suggestionCacheKey)) {
                this.suggestionCacheKey = s1;
                ArrayList arraylist = new ArrayList();
                ModuleManager modulemanager = ModuleManager.INSTANCE;

                for (Module module : modulemanager.getModules()) {
                    if (module.getCategory() != this.selectedCategory && normalizeSearch(module.getName()).contains(s)) {
                        arraylist.add(module);
                    }
                }

                arraylist.sort(
                    Comparator.<Module>comparingInt(module1 -> normalizeSearch(module1.getName()).startsWith(s) ? 0 : 1)
                        .thenComparing(module1 -> module1.getName().toLowerCase(Locale.ROOT))
                );
                int i = arraylist.size();
                this.hiddenSuggestionCount = Math.max(0, i + -9 + 1);
                this.searchSuggestions.clear();
                this.searchSuggestions.addAll(arraylist.subList(0, Math.min(8, arraylist.size())));
                this.suggestionHoverAnimations.keySet().retainAll(this.searchSuggestions);
                this.suggestionsUpdatedAt = System.currentTimeMillis();
                this.selectedSuggestionIndex = -1;
            }
        }
    }

    public static ClickGuiScreen getInstance() {
        return instance;
    }

    public ClickGuiPanel getActivePanel() {
        return this.activePanel;
    }

    private void stopSearching() {
        this.changelogVisible = false;
        VoilClient.getReleaseNotes().markAsSeen();
    }

    public void setPage(ClickGuiTab clickguitab) {
        if (this.currentPage != clickguitab) {
            this.pageTransitionAnimation.snapTo(1.0);
            this.pageTransitionAnimation.setTarget(0.0);
            if (this.activePanel != null) {
                this.activePanel.onClose();
            }

            this.currentPage = clickguitab;
            this.activePanel = (ClickGuiPanel)this.pages.get(clickguitab);
            if (this.activePanel != null) {
                this.activePanel.onOpen();
            }
        }
    }

    @Override
    public boolean mouseReleased(Click click) {
        int i = click.button();
        double d2 = click.x();
        double d0 = this.toGuiUnits(d2);
        double d3 = click.y();
        double d1 = this.toGuiUnits(d3);
        if (i == 0) {
            this.draggingWindow = false;
        }

        if (this.activePanel != null) {
            this.activePanel.mouseReleased2(d0, d1, i);
            return true;
        }

        for (ModuleButton modulebutton : this.moduleButtons) {
            if (modulebutton.getModule().getCategory().equals(this.selectedCategory) && this.currentPage == ClickGuiTab.HOME) {
                modulebutton.mouseReleased(d0, d1, i);
            }
        }

        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseDragged(Click click, double d0, double d1) {
        int i = click.button();
        double d6 = click.x();
        double d2 = this.toGuiUnits(d6);
        double d7 = click.y();
        double d3 = this.toGuiUnits(d7);
        double d4 = this.toGuiUnits(d0);
        double d5 = this.toGuiUnits(d1);
        if (this.activePanel != null) {
            this.activePanel.mouseDragged2(d2, d3, i, d4, d5);
            return true;
        }

        for (ModuleButton modulebutton : this.moduleButtons) {
            modulebutton.mouseClicked(d2, d3, i, d4, d5);
        }

        if (this.draggingWindow) {
            int l = (int)d2;
            int k = this.dragOffsetX;
            int j = l;
            this.windowX = j - k;
            l = (int)d3;
            k = this.dragOffsetY;
            j = l;
            this.windowY = j - k;
        }

        return super.mouseDragged(click, d0, d1);
    }

    @Override
    public boolean charTyped(CharInput charinput) {
        char c0 = (char)charinput.codepoint();
        int j = charinput.modifiers();
        if (this.activePanel != null) {
            this.activePanel.charTyped(c0, j);
            return true;
        }

        for (ModuleButton modulebutton : this.moduleButtons) {
            modulebutton.charTyped(c0, j);
        }

        if (!this.searchFocused) {
            return super.charTyped(charinput);
        }

        if (c0 == '\b' && this.searchText.length() > 0) {
            String s2 = this.searchText;
            int i = this.searchText.length();
            this.searchText = s2.substring(0, i - 1);
        } else if (this.searchText.length() < 16) {
            String s = this.searchText;
            this.searchText = s + c0;
            this.displayedScroll = 0.0;
            this.targetScroll = 0.0;
            this.scrollAnimation.snapTo(0.0);
        }

        return true;
    }

    @Override
    public boolean mouseClicked(Click click, boolean flag) {
        int i = click.button();
        double d2 = click.x();
        double d0 = this.toGuiUnits(d2);
        double d3 = click.y();
        double d1 = this.toGuiUnits(d3);
        if (i == 0 && this.windowX < d0) {
            int l = this.windowWidth;
            int k = this.windowX;
            if (d0 < k + l && this.windowY < d1) {
                k = this.windowY;
                if (d1 < k + 15) {
                    this.draggingWindow = true;
                    this.dragOffsetX = (int)(d0 - this.windowX);
                    this.dragOffsetY = (int)(d1 - this.windowY);
                    return true;
                }
            }
        }

        if (this.changelogVisible && this.changelogAnimation.getValue() > 0.5) {
            float f10 = 240.0F;
            float f13 = 22.0F;
            float f14 = 46.0F;
            float f15 = 30.0F;
            float f16 = this.suggestions.size() * f13;
            float f17 = Math.min(f16, this.windowHeight - 90 - f14 - f15);
            float f18 = f14 + f17 + f15;
            float f19 = this.windowX + (this.windowWidth - f10) / 2.0F;
            float f20 = this.windowY + (this.windowHeight - f18) / 2.0F;
            float f22 = f20 + f18 - 24.0F;
            if (i == 0) {
                if (this.isInside(f19 + f10 - 22.0F, f20 + 10.0F, f19 + f10 - 8.0F, f20 + 24.0F, d0, d1)) {
                    this.stopSearching();
                    return true;
                }

                if (this.featuredModule != null) {
                    MSDFFontAtlas msdffontatlas = VoilClient.fonts.getSelectedFont();
                    String s2 = this.featuredModule.getName();
                    float f23 = msdffontatlas.getWidth("show me " + s2, 9.0F) + 18.0F;
                    if (this.isInside(f19 + 12.0F, f22, f19 + 12.0F + f23, f22 + 16.0F, d0, d1)) {
                        this.selectedCategory = this.featuredModule.getCategory();
                        this.setPage(ClickGuiTab.HOME);
                        ModuleManager modulemanager3 = ModuleManager.INSTANCE;
                        modulemanager3.init();
                        this.featuredModule.setExpanded(true);
                        this.selectedModule = null;
                        this.searchText = "";
                        this.searchFocused = false;
                        this.openedModule = this.featuredModule;
                        this.stopSearching();
                        return true;
                    }
                }

                float f24 = f19 + f10 - 12.0F - 52.0F;
                if (this.isInside(f24, f22, f24 + 52.0F, f22 + 16.0F, d0, d1)) {
                    this.stopSearching();
                    return true;
                }
            }

            return true;
        } else {
            if (this.mascot.mouseClicked(d0, d1, i)) {
                return true;
            }

            if (i == 0) {
                for (SuggestionRect suggestionrect : this.suggestionRects) {
                    if (this.isInside(
                        suggestionrect.x(), suggestionrect.y(), suggestionrect.x() + suggestionrect.width(), suggestionrect.y() + suggestionrect.height(), d0, d1
                    )) {
                        this.openModule(suggestionrect.module());
                        return true;
                    }
                }
            }

            if (i == 0 && this.currentPage == ClickGuiTab.HOME) {
                float f8 = this.getSortBarX();
                float f11 = this.windowY + 8.0F;
                if (this.isInside(f8, f11, f8 + 98.0F, f11 + 20.0F, d0, d1)) {
                    String s = String.valueOf(ClientSettings.moduleSortMode.getValue());
                    String s3;
                    if (d0 < f8 + 26.0F) {
                        s3 = "Favourite".equals(s) ? "Alphabetical" : "Favourite";
                    } else if (d0 < f8 + 26.0F + 38.0F) {
                        s3 = "Alphabetical";
                    } else {
                        s3 = "Enabled".equals(s) ? "Alphabetical" : "Enabled";
                    }

                    ClientSettings.moduleSortMode.setValue(s3);
                    ModuleManager modulemanager2 = ModuleManager.INSTANCE;
                    modulemanager2.clearCategoryCache();
                    return true;
                }
            }

            float f9 = this.windowX + 16;
            float f12 = 105.0F;
            float f = 13.0F;
            float f1 = (f12 - 5 * f) / 6;
            float f2 = f9 + f1;
            int l1 = this.windowY;
            float f3 = l1 + 270;
            float f4 = VoilClient.fonts.getIcons().getLineHeight(f);
            float[] afloat = new float[5];

            for (int j = 0; j < 5; j++) {
                afloat[j] = f2 + j * (f + f1);
            }

            if (i == 0) {
                for (int i1 = 0; i1 < 5; i1++) {
                    if (!(d0 < afloat[i1] - 2.0F) && !(d0 > afloat[i1] + f + 2.0F) && !(d1 < f3 - 2.0F) && !(d1 > f3 + f4 + 2.0F)) {
                        switch (i1) {
                            case 0:
                                this.setPage(ClickGuiTab.CONFIG);
                                break;
                            case 1:
                                this.setPage(ClickGuiTab.GUI);
                                break;
                            case 2:
                                this.setPage(ClickGuiTab.IRC);
                                break;
                            case 3:
                                VoilClient.mc.setScreen(new HudEditorScreen());
                                break;
                            case 4:
                                Class<SelfDestruct> oclass = SelfDestruct.class;
                                ModuleManager modulemanager = ModuleManager.INSTANCE;
                                this.selfDestruct = (SelfDestruct)modulemanager.getModule(oclass);
                                if (this.selfDestruct != null) {
                                    this.selfDestruct.onEnable();
                                }
                        }

                        return true;
                    }
                }
            }

            l1 = this.windowY;
            float f21 = l1 + 80;

            for (Category category : Category.values()) {
                l1 = this.windowX;
                float f5 = l1 + 14;
                float f6 = f21 - 8.0F;
                if (this.isInside(f5, f6, f5 + 120.0F, f6 + 15.0F, d0, d1) && i == 0) {
                    this.selectedCategory = category;
                    this.setPage(ClickGuiTab.HOME);
                    ModuleManager modulemanager1 = ModuleManager.INSTANCE;
                    modulemanager1.init();
                    this.selectedModule = null;
                    this.displayedScroll = 0.0;
                    this.targetScroll = 0.0;
                    this.scrollAnimation.snapTo(0.0);
                    this.moduleButtons.clear();
                    float f7 = (float)(70.0 + this.displayedScroll);
                    Category category1 = this.selectedCategory;
                    modulemanager1 = ModuleManager.INSTANCE;

                    for (Module module : modulemanager1.getModulesByCategory(category1)) {
                        this.moduleButtons.add(new ModuleButton(module, 0.0, f7, this));
                        f7 += 40.0F;
                    }

                    return true;
                }

                f21 += 20.0F;
            }

            if (this.activePanel != null) {
                this.activePanel.mouseClicked2(d0, d1, i);
                return true;
            }

            if (i == 0) {
                l1 = this.windowWidth;
                l1 = this.windowX + (l1 + -161 + 1) / 2;
                int j1 = l1 + 145;
                int k1 = this.windowY + 7;
                if (d0 >= j1 && d0 <= j1 + 160 && d1 >= k1 && d1 <= k1 + 20) {
                    this.searchFocused = true;
                    return true;
                }
            }

            boolean flag2;
            label161: {
                if (d0 >= this.windowX && d0 <= this.windowX + this.windowWidth) {
                    l1 = this.windowY;
                    if (d1 >= l1 + 30) {
                        int i2 = this.windowHeight;
                        l1 = this.windowY;
                        if (d1 <= l1 + i2) {
                            flag2 = true;
                            break label161;
                        }
                    }
                }

                flag2 = false;
            }

            boolean flag1 = flag2;
            if (flag1) {
                for (ModuleButton modulebutton : this.moduleButtons) {
                    if (this.selectedCategory.equals(modulebutton.getModule().getCategory())
                        && !modulebutton.getModule().isHidden()
                        && this.currentPage == ClickGuiTab.HOME) {
                        modulebutton.handleClick(d0, d1, i);
                    }
                }
            }

            return super.mouseClicked(click, flag);
        }
    }

    @Override
    public boolean mouseScrolled(double d0, double d1, double d2, double d3) {
        d0 = this.toGuiUnits(d0);
        d1 = this.toGuiUnits(d1);
        if (this.activePanel != null) {
            this.activePanel.mouseScrolled2(d0, d1, d2, d3);
            return true;
        }

        if (this.changelogVisible && this.changelogAnimation.getValue() > 0.5) {
            float f = 22.0F;
            float f1 = 46.0F;
            float f2 = 30.0F;
            float f3 = this.suggestions.size() * f;
            int k = this.windowHeight;
            float f4 = Math.min(f3, k + -91 + 1 - f1 - f2);
            double d8 = Math.min(0.0F, f4 - f3);
            this.changelogScrollTarget = Math.max(d8, Math.min(this.changelogScrollTarget + d3 * 20.0, 0.0));
            return true;
        }

        int j = this.sidebarWidth;
        int i = this.windowX;
        float f5 = i + j;
        float f6 = this.windowY + 30;
        j = this.windowWidth;
        i = this.windowX;
        float f7 = i + j;
        j = this.windowHeight;
        i = this.windowY;
        boolean flag = this.isInside(f5, f6, f7, i + j, d0, d1);
        if (flag && this.currentPage == ClickGuiTab.HOME) {
            ClickGuiComponent.clearActiveInputs();

            for (ModuleButton modulebutton : this.moduleButtons) {
                if (!modulebutton.getModule().isHidden()) {
                    modulebutton.mouseScrolled(d0, d1, d2, d3);
                    if (ClickGuiComponent.isInputActive(InputEventType.Scroll)) {
                        return true;
                    }
                }
            }

            double d6 = 0.0;

            for (ModuleButton modulebutton1 : this.moduleButtons) {
                if (!modulebutton1.getModule().isHidden()) {
                    d6 += modulebutton1.getHeight() + 5.0F;
                }
            }

            i = this.windowHeight;
            double d7 = i + -51 + 1;
            double d4 = Math.min(0.0, d7 - d6);
            double d5 = this.targetScroll + d3 * 30.0;
            if (d5 > 0.0) {
                this.targetScroll = Math.min(14.0, d5 * 0.35);
            } else if (d5 < d4) {
                this.targetScroll = Math.max(d4 - 14.0, d4 + (d5 - d4) * 0.35);
            } else {
                this.targetScroll = d5;
            }

            this.lastScrollTime = System.currentTimeMillis();
            return true;
        } else {
            return super.mouseScrolled(d0, d1, d2, d3);
        }
    }

    @Override
    public void render(DrawContext drawcontext, int i, int j, float f) {
        if (this.closeProgress < 0.01F && !this.opening) {
            if (this.closing) {
                this.close();
            }
        } else {
            long k = System.currentTimeMillis();
            RenderManager.beginHud();
            RenderManager.flush();
            float f1 = this.getScale();
            if (this.lastScale > 0.0F && f1 != this.lastScale) {
                float f2 = this.lastScale / f1;
                this.windowX = Math.round((this.windowX + this.windowWidth / 2.0F) * f2 - this.windowWidth / 2.0F);
                this.windowY = Math.round((this.windowY + this.windowHeight / 2.0F) * f2 - this.windowHeight / 2.0F);
            }

            this.lastScale = f1;
            double d0 = i;
            int i1 = (int)this.toGuiUnits(d0);
            double d1 = j;
            int l = (int)this.toGuiUnits(d1);
            GuiScale.pushScale(this.getScale());

            try {
                RenderManager.beginHud();
                ModuleManager modulemanager = ModuleManager.INSTANCE;
                if (modulemanager.isEnabled(Vlippy.class)) {
                    this.mascot.render(i1, l, this.closeProgress);
                }

                Render2DUtil.framebufferRenderer.drawn = true;
                this.renderGui(i1, l, f, k);
                ClickGuiComponent.renderTooltip();
                Render2DUtil.framebufferRenderer.drawn = false;
                RenderManager.flush();
            } finally {
                GuiScale.popScale();
            }

            RenderManager.guiRendered = true;
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return KeybindSetting.isAnyListening() ? false : this.moduleButtons.stream().noneMatch(modulebutton -> modulebutton.getModule().isBinding());
    }

    @Override
    public boolean keyPressed(KeyInput keyinput) {
        int i = keyinput.key();
        int j = keyinput.scancode();
        int k = keyinput.modifiers();
        if (this.changelogVisible && this.changelogAnimation.getValue() > 0.5) {
            if (i == 256) {
                this.stopSearching();
            }

            return true;
        } else if (this.activePanel instanceof IrcPage ircpage && ircpage.isConnected() && i == 256) {
            ircpage.scrollToBottom();
            return true;
        } else if (i == 256) {
            if (!KeybindSetting.isAnyListening()) {
                for (ModuleButton modulebutton2 : this.moduleButtons) {
                    if (modulebutton2.getModule().isBinding()) {
                        modulebutton2.keyPressed(i, j, k);
                        return true;
                    }
                }

                if (this.searchFocused) {
                    this.searchFocused = false;
                    this.searchText = "";
                    return true;
                } else {
                    this.markOpened();
                    return true;
                }
            } else {
                for (ModuleButton modulebutton1 : this.moduleButtons) {
                    modulebutton1.keyPressed(i, j, k);
                }

                return true;
            }
        } else {
            if (this.activePanel != null) {
                this.activePanel.keyReleased2(i, j, k);
                return true;
            }

            if (this.searchFocused) {
                label87:
                if (!this.searchText.isEmpty() && !this.searchSuggestions.isEmpty()) {
                    if (i != 257) {
                        if (i == 264) {
                            int k1 = this.searchSuggestions.size();
                            int l1 = k1 - 1;
                            k1 = this.selectedSuggestionIndex;
                            this.selectedSuggestionIndex = Math.min(l1, k1 + 1);
                            return true;
                        }

                        if (i == 265) {
                            int j1 = this.selectedSuggestionIndex;
                            this.selectedSuggestionIndex = Math.max(-1, j1 - 1);
                            return true;
                        }

                        if (i != 335) {
                            break label87;
                        }
                    }

                    int i1 = this.isSuggestionListEmpty();
                    if (i1 >= 0) {
                        this.openModule((Module)this.searchSuggestions.get(i1));
                        return true;
                    }
                }

                if (i == 259 && this.searchText.length() > 0) {
                    String s = this.searchText;
                    int l = this.searchText.length();
                    this.searchText = s.substring(0, l - 1);
                }

                return true;
            } else {
                for (ModuleButton modulebutton : this.moduleButtons) {
                    modulebutton.keyPressed(i, j, k);
                }

                return super.keyPressed(keyinput);
            }
        }
    }

    @Override
    protected void init() {
        this.windowWidth = 510;
        this.windowHeight = 300;
        this.sidebarWidth = 135;
        this.dragOffsetX = 0;
        this.dragOffsetY = 0;
        this.draggingWindow = false;
        this.searchFocused = false;
        this.searchActive = false;
        float f = this.getScale();
        this.lastScale = f;
        this.windowX = (int)((VoilClient.mc.getWindow().getFramebufferWidth() / f - this.windowWidth) / 2.0F);
        this.windowY = (int)((VoilClient.mc.getWindow().getFramebufferHeight() / f - this.windowHeight) / 2.0F);
        this.opening = true;
        this.closing = false;
        this.lastOpenTime = System.currentTimeMillis();
        this.closeProgress = 0.0F;
        this.searchText = "";
        this.cardWidth = 70;
        this.cardHeight = 40;
        this.categorySlideOffset = 0;
        this.selectedModule = null;
        this.moduleButtons.clear();
        this.openTimestamp = System.currentTimeMillis();
        this.caretVisible = true;
        this.displayedScroll = this.savedScroll;
        this.targetScroll = this.savedScroll;
        this.scrollAnimation.snapTo(this.savedScroll);
        this.searchFocusAnimation.reset();
        this.caretAnimation.snapTo(1.0);

        for (UiAnimation uianimation : this.categoryAnimations) {
            uianimation.reset();
        }

        for (UiAnimation uianimation1 : this.iconHoverAnimations) {
            uianimation1.reset();
        }

        this.listFadeAnimation.reset();
        this.enabledToggleAnimation.snapTo("Enabled".equals(String.valueOf(ClientSettings.moduleSortMode.getValue())) ? 1.0 : 0.0);

        for (UiAnimation uianimation2 : this.sortButtonAnimations) {
            uianimation2.reset();
        }

        this.changelogAnimation.reset();
        this.changelogScrollAnimation.reset();
        this.changelogCloseHoverAnimation.reset();
        this.showModuleHoverAnimation.reset();
        this.dismissHoverAnimation.reset();
        this.pageTransitionAnimation.reset();
        this.scrollbarAnimation.reset();
        this.lastScrollTime = 0L;
        this.lastFrameTime = System.currentTimeMillis();
        this.searchSuggestions.clear();
        this.suggestionRects.clear();
        this.suggestionHoverAnimations.clear();
        this.suggestionsAnimation.reset();
        this.suggestionCacheKey = "";
        this.selectedSuggestionIndex = -1;
        this.hiddenSuggestionCount = 0;
        this.hasSearchResults = true;
        this.pages.clear();
        this.pages.put(ClickGuiTab.GUI, new GuiSettingsPage(this));
        this.pages.put(ClickGuiTab.CONFIG, new ConfigScriptsPanel(this));
        this.pages.put(ClickGuiTab.IRC, new IrcPage(this));
        this.pages.put(ClickGuiTab.SOCIAL, new SocialPage(this));
        this.changelogVisible = false;
        this.featuredModule = null;
        this.suggestions.clear();
        this.changelogScroll = 0.0;
        this.changelogScrollTarget = 0.0;
        this.buildChangelog();
        this.setPage(ClickGuiTab.HOME);
        this.mascot.reset();
        super.init();
    }

    @Override
    public void close() {
        ModuleManager modulemanager = ModuleManager.INSTANCE;
        modulemanager.init();
        this.searchText = "";
        this.searchFocused = false;
        this.searchActive = false;
        ApiClient apiclient3 = ApiClient.INSTANCE;
        JsonObject jsonobject = ConfigManager.getRoot();
        ApiClient apiclient = apiclient3;
        apiclient.uploadSettings(jsonobject);
        ApiClient apiclient1 = ApiClient.INSTANCE;
        apiclient1.saveClientSettings();
        ApiClient apiclient2 = ApiClient.INSTANCE;
        apiclient2.saveHudLayout();

        for (ModuleButton modulebutton : this.moduleButtons) {
            modulebutton.getModule().setBinding(false);
        }

        this.savedScroll = this.targetScroll;
        KeybindSetting.stopListening();
        super.close();
    }
}
