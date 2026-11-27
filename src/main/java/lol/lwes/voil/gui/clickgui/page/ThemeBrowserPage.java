package lol.lwes.voil.gui.clickgui.page;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.prestigeclient.renderlib.api.font.MSDFFontAtlas;
import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.api.ApiClient;
import lol.lwes.voil.api.ApiResponse;
import lol.lwes.voil.config.ClientSettings;
import lol.lwes.voil.config.GuiTheme;
import lol.lwes.voil.config.ThemeInfo;
import lol.lwes.voil.config.ThemePreset;
import lol.lwes.voil.gui.clickgui.ClickGuiComponent;
import lol.lwes.voil.gui.clickgui.ClickGuiPanel;
import lol.lwes.voil.gui.clickgui.ClickGuiScreen;
import lol.lwes.voil.gui.clickgui.page.ClickArea;
import lol.lwes.voil.gui.clickgui.page.ColorPickerDragMode;
import lol.lwes.voil.gui.clickgui.page.ColorTheme;
import lol.lwes.voil.gui.clickgui.theme.ThemeDraft;
import lol.lwes.voil.util.font.IconGlyphs;
import lol.lwes.voil.util.render.ColorUtil;
import lol.lwes.voil.util.render.Render2DUtil;
import lol.lwes.voil.util.render.RenderManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.glfw.GLFW;

public class ThemeBrowserPage extends ClickGuiPanel {
    public final ClickGuiScreen gui;
    private final Runnable onClose;
    private final List<ClickArea> entries = new ArrayList<>();
    private final Set<String> favorites = new HashSet<>();
    private final List<ThemeInfo> visibleEntries = new ArrayList<>();
    private final List<ThemeInfo> customThemes = new ArrayList<>();
    private String[] tabs = new String[]{"All", "Built-In", "Public", "Private"};
    private String activeTab = "Built-In";
    private String searchQuery = "";
    private boolean loading;
    private boolean editing;
    private String[] sortModes = new String[]{"A-Z", "Top", "Favourites", "Light", "Dark"};
    private String sortMode = "A-Z";
    private int scrollOffset;
    private int scrollMax;
    private String statusMessage = "";
    private boolean searchFocused;
    private ThemeDraft editingTheme;
    private Rectangle panelBounds;
    private boolean dragging;
    private String focusedField = "";
    private boolean colorPickerOpen;
    private boolean confirmDelete;
    private Consumer<Color> colorCallback;
    private float pickerHue;
    private float pickerSaturation;
    private float pickerBrightness;
    private String hexInput = "";
    private int pickerX;
    private int pickerY;
    private ColorPickerDragMode colorPicker = ColorPickerDragMode.NONE;

    public ThemeBrowserPage(ClickGuiScreen clickguiscreen) {
        this(clickguiscreen, null);
    }

    public ThemeBrowserPage(ClickGuiScreen clickguiscreen, Runnable runnable) {
        super(clickguiscreen);
        this.gui = clickguiscreen;
        this.onClose = runnable;
        ClientSettings.themePresets.clear();
        ApiClient.INSTANCE
            .getSharedThemes()
            .thenAccept(list -> list.forEach(themepreset -> ClientSettings.themePresets.put(themepreset.getName(), themepreset)));
        this.close();
    }


    private void close() {
        ApiClient.INSTANCE.getCustomThemes(false).thenAccept(list -> {
            this.visibleEntries.clear();
            this.visibleEntries.addAll(list);
            ApiClient.INSTANCE.getCustomThemes(true).thenAccept(favourites -> {
                this.customThemes.clear();
                this.customThemes.addAll(favourites);
            });
        });
    }

    private void drawBrowser(int i, int j, int k, int l) {
        boolean flag = this.searchFocused && this.editingTheme != null;
        if (flag) {
            int i3 = i + k;
            int j1 = i3 - 52;
            i3 = j1 - 52;
            int k1 = i3 + -7 + 1;
            String s6;
            if (this.editingTheme.isPublic) {
                s6 = "Public";
            } else {
                s6 = "Private";
            }

            String s = s6;
            int l1 = (int)(VoilClient.fonts.getSelectedFont().getWidth(s, 8.0F) + 22.0F);
            i3 = k1 - l1;
            int i2 = i3 + -7 + 1;
            float f6 = i2;
            float f14 = j;
            float f15 = l1;
            float f16 = 18;
            Color color = GuiTheme.settingsBg != null ? GuiTheme.settingsBg : new Color(24, 24, 30);
            float f = 1.0F;
            float f1 = 6.0F;
            float f2 = f16;
            float f3 = f15;
            float f4 = f14;
            Render2DUtil.drawRoundedRect(f6, f4, f3, f2, f1, f, color);
            float f7 = i2;
            f14 = j;
            f15 = l1;
            f16 = 18;
            Color color5;
            if (this.editingTheme.isPublic) {
                color5 = GuiTheme.accentColor1 != null ? GuiTheme.accentColor1 : new Color(167, 139, 250);
            } else {
                Color color1 = GuiTheme.accentColor1 != null ? GuiTheme.accentColor1 : new Color(167, 139, 250);
                color5 = ColorUtil.withAlpha(color1, 55);
            }

            color = color5;
            f = 0.4F;
            f1 = 0.3F;
            f2 = 6.0F;
            f3 = f16;
            f4 = f15;
            float f5 = f14;
            Render2DUtil.drawOutline(f7, f5, f4, f3, f2, f1, f, color);
            VoilClient.fonts
                .getIcons()
                .render(
                    this.editingTheme.isPublic ? IconGlyphs.iconE0BE() : IconGlyphs.iconE0BF(),
                    i2 + 4,
                    j + 4,
                    11.0F,
                    this.editingTheme.isPublic
                        ? (GuiTheme.accentColor1 != null ? GuiTheme.accentColor1 : new Color(167, 139, 250)).getRGB()
                        : new Color(122, 124, 134).getRGB()
                );
            VoilClient.fonts.getSelectedFont().render(s, i2 + 18, j + 5, 8.0F, this.editingTheme.isPublic ? -1 : new Color(122, 124, 134).getRGB());
            this.entries.add(new ClickArea(new Rectangle(i2, j, l1, 18), () -> this.editingTheme.isPublic = !this.editingTheme.isPublic));
            float f8 = j1;
            f14 = j;
            f15 = 52;
            f16 = 18;
            Color color2 = GuiTheme.accentColor1 != null ? GuiTheme.accentColor1 : new Color(167, 139, 250);
            color = ColorUtil.withAlpha(color2, 80);
            f = 1.0F;
            f1 = 6.0F;
            f2 = f16;
            f3 = f15;
            f4 = f14;
            Render2DUtil.drawRoundedRect(f8, f4, f3, f2, f1, f, color);
            float f9 = j1;
            f14 = j;
            f15 = 52;
            f16 = 18;
            color = GuiTheme.accentColor1 != null ? GuiTheme.accentColor1 : new Color(167, 139, 250);
            f = 0.4F;
            f1 = 0.3F;
            f2 = 6.0F;
            f3 = f16;
            f4 = f15;
            f5 = f14;
            Render2DUtil.drawOutline(f9, f5, f4, f3, f2, f1, f, color);
            VoilClient.fonts.getIcons().render("", j1 + 6, j + 4, 11.0F, -1);
            VoilClient.fonts.getSelectedFont().render("Save", j1 + 19, j + 5, 8.0F, -1);
            this.entries.add(new ClickArea(new Rectangle(j1, j, 52, 18), this::applyTheme));
            float f10 = k1;
            f14 = j;
            f15 = 52;
            f16 = 18;
            color = GuiTheme.settingsBg != null ? GuiTheme.settingsBg : new Color(24, 24, 30);
            f = 1.0F;
            f1 = 6.0F;
            f2 = f16;
            f3 = f15;
            f4 = f14;
            Render2DUtil.drawRoundedRect(f10, f4, f3, f2, f1, f, color);
            float f11 = k1;
            f14 = j;
            f15 = 52;
            f16 = 18;
            Color color3 = GuiTheme.accentColor1 != null ? GuiTheme.accentColor1 : new Color(167, 139, 250);
            color = ColorUtil.withAlpha(color3, 55);
            f = 0.4F;
            f1 = 0.3F;
            f2 = 6.0F;
            f3 = f16;
            f4 = f15;
            f5 = f14;
            Render2DUtil.drawOutline(f11, f5, f4, f3, f2, f1, f, color);
            VoilClient.fonts
                .getSelectedFont()
                .render(
                    "Cancel", k1 + (52 - VoilClient.fonts.getSelectedFont().getWidth("Cancel", 8.0F)) / 2.0F, j + 5, 8.0F, new Color(122, 124, 134).getRGB()
                );
            this.entries.add(new ClickArea(new Rectangle(k1, j, 52, 18), this::closeEditor));
            int j2 = i + 2;
            i3 = j + 18;
            int k2 = i3 + 6;
            int l2 = k + -5 + 1;
            float f12 = j2;
            f14 = k2;
            f15 = l2;
            f16 = 18;
            color = GuiTheme.settingsBg != null ? GuiTheme.settingsBg : new Color(24, 24, 30);
            f = 1.0F;
            f1 = GuiTheme.radiusBottomRight;
            f2 = f16;
            f3 = f15;
            f4 = f14;
            Render2DUtil.drawRoundedRect(f12, f4, f3, f2, f1, f, color);
            float f13 = j2;
            f14 = k2;
            f15 = l2;
            f16 = 18;
            if (this.editing) {
                color5 = GuiTheme.accentColor1 != null ? GuiTheme.accentColor1 : new Color(167, 139, 250);
            } else {
                Color color4 = GuiTheme.accentColor1 != null ? GuiTheme.accentColor1 : new Color(167, 139, 250);
                color5 = ColorUtil.withAlpha(color4, 55);
            }

            color = color5;
            f = 0.1F;
            f1 = 1.0F;
            f2 = GuiTheme.radiusBottomRight;
            f3 = f16;
            f4 = f15;
            f5 = f14;
            Render2DUtil.drawOutline(f13, f5, f4, f3, f2, f1, f, color);
            String s3 = this.editing ? "|" : "";
            String s2 = this.editingTheme.name;
            String s5 = s2 + s3;
            int j3 = l2 + -9 + 1;
            String s1 = this.truncate(s5, j3);
            VoilClient.fonts
                .getSelectedFont()
                .render(s1, j2 + 6, k2 + 2, this.editingTheme.name.isEmpty() && !this.editing ? new Color(122, 124, 134).getRGB() : -1);
            this.entries.add(new ClickArea(new Rectangle(j2, k2, l2, 18), () -> {
                this.editing = true;
                this.loading = false;
            }));
        } else {
            VoilClient.fonts.getSelectedFont().render(this.generateName(), i + 4, j + 3, -1);
        }

        int k5;
        if (flag) {
            int i5 = j + 18 + 6;
            i5 += 18;
            k5 = i5 + 8;
        } else {
            k5 = j + 22;
        }

        int i1 = k5;
        int j5 = i1 + 84;
        int k4 = j5 + 6;
        j5 = k4 + 88;
        int l4 = j5 + 6;
        this.drawCard1(i, i1 + -5 + 1, k, 86, flag);
        this.drawEditor(i, k4 + -5 + 1, k, 88, flag);
        this.drawCard2(i, l4 + -7 + 1, k, 54, flag);
        if (!flag) {
            int l5 = i1 + -5 + 1;
            int k3 = l - i1 - 16;
            int l3 = k;
            int i4 = l5;
            int j4 = i;
            this.radiusField(j4, i4, l3, k3);
        }
    }

    private void drawCard1(int i, int j, int k, int l, boolean flag) {
        float f9 = i;
        float f10 = j;
        float f11 = k;
        float f12 = l;
        Color color1 = GuiTheme.modeSettingBg != null ? GuiTheme.modeSettingBg : new Color(30, 30, 36);
        Color color = ColorUtil.withAlpha(color1, 180);
        float f = 1.0F;
        float f1 = 10.0F;
        float f2 = f12;
        float f3 = f11;
        float f4 = f10;
        Render2DUtil.drawRoundedRect(f9, f4, f3, f2, f1, f, color);
        f9 = i;
        f10 = j;
        f11 = k;
        f12 = l;
        Color color2 = GuiTheme.accentColor1 != null ? GuiTheme.accentColor1 : new Color(167, 139, 250);
        color = ColorUtil.withAlpha(color2, 55);
        f = 0.5F;
        f1 = 0.5F;
        f2 = 10.0F;
        f3 = f12;
        f4 = f11;
        float f5 = f10;
        Render2DUtil.drawOutline(f9, f5, f4, f3, f2, f1, f, color);
        int i1 = k + -21 + 1;
        int j1 = (int)(i1 * 0.52F);
        int j4 = i + 10;
        int k1 = j4 + j1 + 8;
        int l1 = i1 - (j1 + 8);
        VoilClient.fonts.getSelectedFont().render("Core", i + 10, j + 7, 9.0F, new Color(122, 124, 134).getRGB());
        int i2 = (j1 + -9 + 1) / 2;
        int j2 = i + 10;
        j4 = j2 + i2;
        int k2 = j4 + 8;
        int l2 = j + 20;
        int i3 = l2 + 34;
        Supplier<Color> supplier4 = () -> this.editingTheme.windowColor;
        Consumer<Color> consumer4 = value -> this.editingTheme.windowColor = value;
        color = GuiTheme.windowColor != null ? GuiTheme.windowColor : (GuiTheme.settingsBg != null ? GuiTheme.settingsBg : new Color(24, 24, 30));
        Consumer<Color> consumer = consumer4;
        Supplier<Color> supplier = supplier4;
        this.drawField(j2, l2, i2, "Window", flag, supplier, consumer, color);
        supplier4 = () -> this.editingTheme.sidebarColor;
        consumer4 = value -> this.editingTheme.sidebarColor = value;
        color = GuiTheme.leftPanel != null ? GuiTheme.leftPanel : (GuiTheme.modeSettingBg != null ? GuiTheme.modeSettingBg : new Color(30, 30, 36));
        Consumer<Color> consumer1 = consumer4;
        Supplier<Color> supplier1 = supplier4;
        this.drawField(k2, l2, i2, "Sidebar", flag, supplier1, consumer1, color);
        supplier4 = () -> this.editingTheme.accentColor1;
        consumer4 = value -> this.editingTheme.accentColor1 = value;
        color = GuiTheme.accentColor1 != null ? GuiTheme.accentColor1 : new Color(167, 139, 250);
        Consumer<Color> consumer2 = consumer4;
        Supplier<Color> supplier2 = supplier4;
        this.drawField(j2, i3, i2, "Accent 1", flag, supplier2, consumer2, color);
        supplier4 = () -> this.editingTheme.accentColor2;
        consumer4 = value -> this.editingTheme.accentColor2 = value;
        color = GuiTheme.accentColor2 != null ? GuiTheme.accentColor2 : new Color(196, 181, 253);
        Consumer<Color> consumer3 = consumer4;
        Supplier<Color> supplier3 = supplier4;
        this.drawField(k2, i3, i2, "Accent 2", flag, supplier3, consumer3, color);
        VoilClient.fonts.getSelectedFont().render("Radius", k1, j + 7, 9.0F, new Color(122, 124, 134).getRGB());
        String[] astring = new String[]{"Win", "Btn", "Slot", "Mod"};
        int j3 = (l1 - 12) / 4;
        int k3 = Math.min(j3, 26);

        for (int l3 = 0; l3 < 4; l3++) {
            int k4 = j3 + 4;
            j4 = l3;
            k4 = j4 * k4;
            j4 = k1;
            int i4 = j4 + k4 + (j3 - k3) / 2;
            int i5 = j + 22;
            String s2 = astring[l3];
            float f13 = flag ? this.editingTheme.radii[l3] : new float[]{6.0F, 6.0F, 4.0F, 6.0F}[l3];
            byte b0 = (byte)(flag ? l3 : -1);
            f = f13;
            String s = s2;
            int l4 = k3;
            this.drawSliderField(i4, i5, l4, s, f, b0);
        }

        float f7 = flag ? this.editingTheme.borderThickness : 1.5F;
        VoilClient.fonts.getSelectedFont().render("Border Thickness", k1, j + 60, 9.0F, new Color(122, 124, 134).getRGB());
        if (flag) {
            String s1 = String.format("%.1f", f7);
            VoilClient.fonts.getSelectedFont().render(s1, k1 + l1 - VoilClient.fonts.getSelectedFont().getWidth(s1, 8.0F) + 11.0F, j + 71, 6.5F, -1);
        }

        this.panelBounds = new Rectangle(k1, j + 73, l1, 8);
        float f6 = f7 / 4.0F;
        float f8 = lerp(f6, 0.0F, 1.0F);
        f9 = k1;
        f10 = j + 74;
        f11 = l1;
        color = GuiTheme.settingsBg != null ? GuiTheme.settingsBg : new Color(24, 24, 30);
        f = 1.0F;
        f1 = 2.0F;
        f2 = 4.0F;
        f3 = f11;
        f4 = f10;
        Render2DUtil.drawRoundedRect(f9, f4, f3, f2, f1, f, color);
        f9 = k1;
        f10 = j + 74;
        f11 = l1 * f8;
        color = GuiTheme.accentColor1 != null ? GuiTheme.accentColor1 : new Color(167, 139, 250);
        f = 1.0F;
        f1 = 2.0F;
        f2 = 4.0F;
        f3 = f11;
        f4 = f10;
        Render2DUtil.drawRoundedRect(f9, f4, f3, f2, f1, f, color);
        Render2DUtil.drawCircle(k1 + l1 * f8 - 3.0F, j + 73, 3.0F, 1.0F, Color.WHITE);
    }

    private void drawCard2(int i, int j, int k, int l, boolean flag) {
        float f6 = i;
        float f7 = j;
        float f8 = k;
        float f9 = l;
        Color color1 = GuiTheme.modeSettingBg != null ? GuiTheme.modeSettingBg : new Color(30, 30, 36);
        Color color = ColorUtil.withAlpha(color1, 180);
        float f = 1.0F;
        float f1 = 10.0F;
        float f2 = f9;
        float f3 = f8;
        float f4 = f7;
        Render2DUtil.drawRoundedRect(f6, f4, f3, f2, f1, f, color);
        f6 = i;
        f7 = j;
        f8 = k;
        f9 = l;
        Color color2 = GuiTheme.accentColor1 != null ? GuiTheme.accentColor1 : new Color(167, 139, 250);
        color = ColorUtil.withAlpha(color2, 55);
        f = 0.5F;
        f1 = 0.5F;
        f2 = 10.0F;
        f3 = f9;
        f4 = f8;
        float f5 = f7;
        Render2DUtil.drawOutline(f6, f5, f4, f3, f2, f1, f, color);
        int i1 = k + -21 + 1;
        int i2 = 8 * 3;
        int l1 = i1;
        int j1 = (l1 - i2) / 4;
        VoilClient.fonts.getSelectedFont().render("Module", i + 10, j + 6, 9.0F, new Color(122, 124, 134).getRGB());
        int k1 = j + 20;
        int j2 = i + 10;
        Supplier<Color> supplier4 = () -> this.editingTheme.moduleText;
        Consumer<Color> consumer4 = value -> this.editingTheme.moduleText = value;
        color = GuiTheme.moduleText != null ? GuiTheme.moduleText : Color.WHITE;
        Consumer<Color> consumer = consumer4;
        Supplier<Color> supplier = supplier4;
        this.drawField(j2, k1, j1, "Text", flag, supplier, consumer, color);
        l1 = i + 10;
        l1 += j1;
        int k2 = l1 + 8;
        supplier4 = () -> this.editingTheme.moduleBackground;
        consumer4 = value -> this.editingTheme.moduleBackground = value;
        color = GuiTheme.modeSettingBg != null ? GuiTheme.modeSettingBg : new Color(30, 30, 36);
        Consumer<Color> consumer1 = consumer4;
        Supplier<Color> supplier1 = supplier4;
        this.drawField(k2, k1, j1, "Background", flag, supplier1, consumer1, color);
        int l2 = i + 10;
        i2 = j1 + 8;
        i2 += i2;
        l1 = l2;
        int i3 = l1 + i2;
        supplier4 = () -> this.editingTheme.outlineColor;
        consumer4 = value -> this.editingTheme.outlineColor = value;
        color = GuiTheme.outlineColor != null ? GuiTheme.outlineColor : (GuiTheme.accentColor1 != null ? GuiTheme.accentColor1 : new Color(167, 139, 250));
        Consumer<Color> consumer2 = consumer4;
        Supplier<Color> supplier2 = supplier4;
        this.drawField(i3, k1, j1, "Outline", flag, supplier2, consumer2, color);
        int j3 = i + 10;
        i2 = j1 + 8;
        i2 *= 3;
        l1 = j3;
        int k3 = l1 + i2;
        supplier4 = () -> this.editingTheme.enabledColor;
        consumer4 = value -> this.editingTheme.enabledColor = value;
        color = GuiTheme.accentColor2 != null ? GuiTheme.accentColor2 : new Color(196, 181, 253);
        Consumer<Color> consumer3 = consumer4;
        Supplier<Color> supplier3 = supplier4;
        this.drawField(k3, k1, j1, "Enabled", flag, supplier3, consumer3, color);
    }

    private void drawSliderField(int i, int j, int k, String s, float f, int l) {
        VoilClient.fonts
            .getSelectedFont()
            .render(s, i + (k - VoilClient.fonts.getSelectedFont().getWidth(s, 8.0F)) / 2.0F, j, 8.0F, new Color(122, 124, 134).getRGB());
        int i1 = j + 11;
        float f9 = k / 2.0F;
        float f10 = 0.0F;
        float f11 = f;
        float f7 = lerp(f11, f10, f9);
        float f13;
        if (this.editingTheme != null) {
            float f12 = this.editingTheme.borderThickness;
            f13 = lerp(f12, 0.5F, 4.0F);
        } else {
            f13 = 1.0F;
        }

        float f8 = f13;
        f13 = i;
        float f14 = i1;
        float f15 = k;
        float f16 = k;
        Color color = GuiTheme.settingsBg != null ? GuiTheme.settingsBg : new Color(24, 24, 30);
        float f1 = 1.0F;
        float f2 = 2.0F;
        float f3 = f16;
        float f4 = f15;
        float f5 = f14;
        Render2DUtil.drawRoundedRect(f13, f5, f4, f3, f2, f1, color);
        f13 = i + k - 12.0F - 2.0F;
        f14 = i1 + 2;
        color = GuiTheme.accentColor1 != null ? GuiTheme.accentColor1 : new Color(167, 139, 250);
        f1 = 1.0F;
        f2 = -45.0F;
        f3 = -45.0F;
        f4 = f7;
        f5 = f8;
        float f6 = 12.0F;
        Render2DUtil.drawArrow(f13, f14, 12.0F, 12.0F, f6, f5, f4, f3, f2, f1, color);
        String s2 = f == Math.floor(f) ? String.valueOf((int)f) : String.valueOf(f);
        String s1 = s2 + "px";
        MSDFFontAtlas msdffontatlas = VoilClient.fonts.getSelectedFont();
        f15 = i + (k - VoilClient.fonts.getSelectedFont().getWidth(s1, 8.0F)) / 2.0F;
        int j1 = i1 + k;
        msdffontatlas.render(s1, f15, j1 + 2, 8.0F, new Color(122, 124, 134).getRGB());
        if (l >= 0) {
            this.entries.add(new ClickArea(new Rectangle(i, i1, k, k), () -> this.scrollTo(l)));
        }
    }

    private void drawColorField(Color color, Consumer<Color> consumer, int i, int j) {
        this.colorCallback = consumer;
        float[] afloat = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        this.pickerHue = afloat[0];
        this.pickerSaturation = afloat[1];
        this.pickerBrightness = afloat[2];
        this.hexInput = this.colorToHex(color);
        this.confirmDelete = false;
        float f = i;
        float f8 = this.gui.windowX;
        float f1 = this.gui.windowX + this.gui.windowWidth - 116;
        float f2 = f8;
        float f3 = f;
        this.pickerX = (int)lerp(f3, f2, f1);
        float f4 = j;
        f8 = this.gui.windowY;
        int l = this.gui.windowHeight;
        int k = this.gui.windowY;
        k += l;
        float f5 = k + -131 + 1;
        float f6 = f8;
        float f7 = f4;
        this.pickerY = (int)lerp(f7, f6, f5);
        this.colorPickerOpen = true;
    }

    private void vote(ColorTheme colortheme) {
        this.statusMessage = colortheme.name();
        CompletableFuture<String> completablefuture;
        if (colortheme.featured()) {
            String s = colortheme.name();
            ApiClient apiclient = ApiClient.INSTANCE;
            completablefuture = apiclient.getCustomThemeData(s);
        } else {
            String s1 = colortheme.name();
            ApiClient apiclient1 = ApiClient.INSTANCE;
            completablefuture = apiclient1.getSharedThemeData(s1);
        }

        completablefuture.thenAccept(ApiClient.INSTANCE::saveTheme);
        ClientSettings.activeTheme.setValue(colortheme.name());
    }

    private void refresh() {
        if (this.isEditing()) {
            this.openEditor(this.parseTheme("New Theme"));
        }
    }

    private ThemeDraft parseTheme(String s) {
        String s1 = "";
        ThemeDraft themedraft = new ThemeDraft();
        themedraft.name = s;
        themedraft.windowColor = GuiTheme.windowColor != null
            ? GuiTheme.windowColor
            : (GuiTheme.settingsBg != null ? GuiTheme.settingsBg : new Color(24, 24, 30));
        themedraft.sidebarColor = GuiTheme.leftPanel != null
            ? GuiTheme.leftPanel
            : (GuiTheme.modeSettingBg != null ? GuiTheme.modeSettingBg : new Color(30, 30, 36));
        themedraft.settingsBackground = GuiTheme.settingsBg != null
            ? GuiTheme.settingsBg
            : (GuiTheme.settingsBg != null ? GuiTheme.settingsBg : new Color(24, 24, 30));
        themedraft.settingsHeader = GuiTheme.settingsHeader != null
            ? GuiTheme.settingsHeader
            : (GuiTheme.accentColor1 != null ? GuiTheme.accentColor1 : new Color(167, 139, 250));
        themedraft.moduleDisabledBackground = GuiTheme.moduleDisabledBg != null
            ? GuiTheme.moduleDisabledBg
            : (GuiTheme.modeSettingBg != null ? GuiTheme.modeSettingBg : new Color(30, 30, 36));
        themedraft.moduleBackground = GuiTheme.moduleColor != null
            ? GuiTheme.moduleColor
            : (GuiTheme.modeSettingBg != null ? GuiTheme.modeSettingBg : new Color(30, 30, 36));
        themedraft.moduleText = GuiTheme.moduleText != null ? GuiTheme.moduleText : Color.WHITE;
        themedraft.secondaryText = GuiTheme.unfocusedTextColor != null ? GuiTheme.unfocusedTextColor : new Color(122, 124, 134);
        themedraft.normalText = GuiTheme.normalTextColor != null ? GuiTheme.normalTextColor : Color.WHITE;
        themedraft.modeBackground = GuiTheme.modeSettingBg != null
            ? GuiTheme.modeSettingBg
            : (GuiTheme.modeSettingBg != null ? GuiTheme.modeSettingBg : new Color(30, 30, 36));
        themedraft.modeFill = GuiTheme.modeSettingFill != null
            ? GuiTheme.modeSettingFill
            : (GuiTheme.accentColor1 != null ? GuiTheme.accentColor1 : new Color(167, 139, 250));
        themedraft.sliderBackground = GuiTheme.sliderSettingBg != null
            ? GuiTheme.sliderSettingBg
            : (GuiTheme.settingsBg != null ? GuiTheme.settingsBg : new Color(24, 24, 30));
        themedraft.outlineColor = GuiTheme.outlineColor != null
            ? GuiTheme.outlineColor
            : (GuiTheme.accentColor1 != null ? GuiTheme.accentColor1 : new Color(167, 139, 250));
        themedraft.accentColor1 = GuiTheme.accentColor1 != null ? GuiTheme.accentColor1 : new Color(167, 139, 250);
        themedraft.accentColor2 = GuiTheme.accentColor2 != null ? GuiTheme.accentColor2 : new Color(196, 181, 253);
        themedraft.enabledColor = GuiTheme.accentColor2 != null ? GuiTheme.accentColor2 : new Color(196, 181, 253);
        themedraft.glow = GuiTheme.glowEnabled;
        themedraft.radii = new float[]{6.0F, 6.0F, 4.0F, 6.0F};
        themedraft.borderThickness = 1.5F;
        return themedraft;
    }

    private void fork(ColorTheme colortheme) {
        if (this.isEditing()) {
            if (colortheme.featured()) {
                ApiClient apiclient1 = ApiClient.INSTANCE;
                String s5 = colortheme.name();
                String s3 = colortheme.author();
                String s1 = s3 + " fork";
                String s = s5;
                ApiClient apiclient = apiclient1;
                apiclient.updateThemeInfo(s, s1, false).thenAccept(apiresponse -> {
                    this.close();
                    this.activeTab = "Private";
                });
            } else {
                String s2 = colortheme.name();
                ApiClient.INSTANCE.getSharedThemeData(s2).thenAccept(themeJson -> {
                    String forkName = colortheme.author() + " fork";
                    this.openEditor(this.createDraft(themeJson, forkName));
                });
            }
        }
    }

    public void mouseClicked(double d0, double d1) {
        if (this.colorPickerOpen) {
            if (this.getEditorBounds().contains(d0, d1)) {
                this.colorPicker = ColorPickerDragMode.SAT_BRI;
                this.dragPicker(d0, d1);
            } else if (this.getPanelBounds().contains(d0, d1)) {
                this.colorPicker = ColorPickerDragMode.HUE;
                this.scrollBy(d0);
            } else if (this.getListBounds().contains(d0, d1)) {
                this.confirmDelete = true;
            } else if (!new Rectangle(this.pickerX, this.pickerY, 116, 130).contains(d0, d1)) {
                this.colorPickerOpen = false;
            }
        } else if (this.panelBounds != null && this.searchFocused && this.editingTheme != null && this.panelBounds.contains(d0, d1)) {
            this.colorPicker = ColorPickerDragMode.BORDER;
            this.setHue(d0);
        } else {
            this.loading = false;
            this.editing = false;
            int j = this.entries.size();

            for (int i = this.entries.size() - 1; i >= 0; i--) {
                ClickArea clickarea = this.entries.get(i);
                if (clickarea.bounds().contains(d0, d1)) {
                    clickarea.action().run();
                    return;
                }
            }
        }
    }

    private Color readColor(JsonObject jsonobject, String s, Color color) {
        try {
            if (jsonobject.has(s) && jsonobject.get(s).isJsonArray()) {
                JsonArray jsonarray = jsonobject.getAsJsonArray(s);
                return new Color(
                    Math.max(0, Math.min(255, jsonarray.get(0).getAsInt())),
                    Math.max(0, Math.min(255, jsonarray.get(1).getAsInt())),
                    Math.max(0, Math.min(255, jsonarray.get(2).getAsInt()))
                );
            }
        } catch (Exception exception) {
        }

        return color;
    }

    private void writeColor(JsonObject jsonobject, String s, Color color) {
        JsonArray jsonarray = new JsonArray();
        jsonarray.add(color.getRed());
        jsonarray.add(color.getGreen());
        jsonarray.add(color.getBlue());
        jsonobject.add(s, jsonarray);
    }

    public void render(int i, int j) {
        this.entries.clear();
        int k = this.gui.windowX;
        int l = this.gui.windowY;
        int i1 = this.gui.windowWidth;
        int j1 = this.gui.sidebarWidth;
        int k1 = k + j1;
        int l1 = k + i1;
        int i2 = l1 - k1;
        int j2 = k1 + 8;
        int k2 = (int)(i2 * 0.4F);
        int l3 = j2 + k2;
        int l2 = l3 + 14;
        l3 = l1 - l2;
        int i3 = l3 + -11 + 1;
        int j3 = l + 14;
        int i4 = this.gui.windowHeight;
        l3 = l;
        l3 += i4;
        int k3 = l3 + -13 + 1;
        this.drawCard3(j2, j3, k2, k3, i, j);
        this.drawBrowser(l2, j3, i3, k3);
        if (this.colorPickerOpen) {
            this.loadCustom();
        }
    }

    public void mouseDragged(double d0, double d1) {
        int i = this.colorPicker.ordinal();
        if (i != 1) {
            if (i != 2) {
                if (i == 3) {
                    this.scrollBy(d0);
                }
            } else {
                this.dragPicker(d0, d1);
            }
        } else {
            this.setHue(d0);
        }
    }

    public void mouseScrolled(double d0) {
        int j = (int)(d0 * 20.0);
        int i = this.scrollOffset;
        this.scrollOffset = i - j;
        this.scrollOffset = Math.max(0, Math.min(this.scrollOffset, this.scrollMax));
        super.mouseScrolled();
    }

    @Override
    public void charTyped(char chr, int modifiers) {
        if (chr < ' ' || chr == 127) {
            return;
        }

        if (this.editing) {
            if (this.editingTheme != null && this.editingTheme.name.length() < 32) {
                ThemeDraft themedraft = this.editingTheme;
                themedraft.name = themedraft.name + chr;
            }
        } else if (this.colorPickerOpen && this.confirmDelete) {
            char c = Character.toLowerCase(chr);
            if (c == '#') {
                this.hexInput = this.hexInput + "#";
                return;
            }

            boolean flag = c >= '0' && c <= '9' || c >= 'a' && c <= 'f';
            if (flag && this.hexInput.replace("#", "").length() < 6) {
                this.hexInput = this.hexInput + c;
                if (this.hexInput.replace("#", "").length() == 6) {
                    this.resetEditor();
                }
            }
        } else if (this.loading) {
            this.searchQuery = this.searchQuery + chr;
            this.scrollOffset = 0;
        }
    }

    @Override
    public void mouseReleased() {
        this.colorPicker = ColorPickerDragMode.NONE;
    }

    public void keyPressed(int i, int j) {
        if (this.editing) {
            if (i == 259) {
                if (this.editingTheme != null && !this.editingTheme.name.isEmpty()) {
                    ThemeDraft themedraft = this.editingTheme;
                    String s1 = this.editingTheme.name;
                    int l = this.editingTheme.name.length();
                    themedraft.name = s1.substring(0, l - 1);
                }
            } else if (i == 257 || i == 256) {
                this.editing = false;
            }
        } else if (this.colorPickerOpen && this.confirmDelete) {
            if (i == 259) {
                if (!this.hexInput.isEmpty()) {
                    this.hexInput = this.hexInput.substring(0, this.hexInput.length() - 1);
                }
            } else if (i == 257) {
                this.resetEditor();
                this.confirmDelete = false;
            } else if (i == 256) {
                this.confirmDelete = false;
            } else if (i == 86 && (j & 2) != 0) {
                String s = GLFW.glfwGetClipboardString(MinecraftClient.getInstance().getWindow().getHandle());
                if (s != null) {
                    this.hexInput = s.trim();
                    this.resetEditor();
                }
            }
        } else {
            if (this.loading) {
                if (i == 259) {
                    if (!this.searchQuery.isEmpty()) {
                        String s2 = this.searchQuery;
                        int k = this.searchQuery.length();
                        this.searchQuery = s2.substring(0, k - 1);
                    }

                    this.scrollOffset = 0;
                } else if (i == 256 || i == 257) {
                    this.loading = false;
                }
            }
        }
    }

    private void applyTheme() {
        if (this.editingTheme != null) {
            ThemeDraft themedraft = this.editingTheme;
            String s = this.serializeTheme(themedraft);
            ThemeDraft themedraft1 = this.editingTheme;
            String s1 = this.getThemeId(themedraft1);
            CompletableFuture<?> completablefuture;
            if (this.editingTheme.id == null) {
                boolean flag = this.editingTheme.isPublic;
                String s3 = this.editingTheme.description;
                String s2 = this.editingTheme.name;
                ApiClient apiclient = ApiClient.INSTANCE;
                completablefuture = apiclient.createTheme(s2, s3, flag, s1, s);
            } else {
                boolean flag1 = this.editingTheme.isPublic;
                String s6 = this.editingTheme.description;
                String s5 = this.editingTheme.name;
                String s4 = this.editingTheme.id;
                ApiClient apiclient1 = ApiClient.INSTANCE;
                completablefuture = apiclient1.updateTheme(s4, s5, s6, flag1, s1, s);
            }

            completablefuture.thenAccept(apiresponse -> {
                this.close();
                this.activeTab = "Private";
            });
            this.closeEditor();
        }
    }

    private void setFavorite(ColorTheme colortheme, boolean flag) {
        if (colortheme.featured()) {
            String s = colortheme.name();
            ApiClient apiclient = ApiClient.INSTANCE;
            apiclient.voteTheme(s, flag).thenAccept(apiresponse -> this.close());
        }
    }

    private Color parseColor(String s) {
        try {
            if (s.startsWith("#")) {
                s = s.substring(1);
            }

            if (s.length() != 6) {
                return null;
            }

            int i = Integer.parseInt(s, 16);
            int j = i >> 16;
            int k = j & 0xFF;
            j = i >> 8;
            return new Color(k, j & 0xFF, i & 0xFF);
        } catch (NumberFormatException numberformatexception) {
            return null;
        }
    }

    private void drawCard3(int i, int j, int k, int l, int i1, int j1) {
        VoilClient.fonts.getIcons().render(IconGlyphs.iconE04C(), i + 2, j + 4, 14.0F, new Color(122, 124, 134).getRGB());
        this.entries.add(new ClickArea(new Rectangle(i, j, 22, 20), () -> {
            if (this.onClose != null) {
                this.onClose.run();
            }
        }));
        VoilClient.fonts.getSelectedFont().render("Themes", i + 22, j + 4, -1);
        int k1 = j + 26;
        float f8 = i;
        float f9 = k1;
        float f10 = k;
        float f11 = 24;
        Color color = GuiTheme.settingsBg != null ? GuiTheme.settingsBg : new Color(24, 24, 30);
        float f = 1.0F;
        float f1 = 8.0F;
        float f2 = f11;
        float f3 = f10;
        float f4 = f9;
        Render2DUtil.drawRoundedRect(f8, f4, f3, f2, f1, f, color);
        f8 = i;
        f9 = k1;
        f10 = k;
        f11 = 24;
        Color color6;
        if (this.loading) {
            color6 = GuiTheme.accentColor1 != null ? GuiTheme.accentColor1 : new Color(167, 139, 250);
        } else {
            Color color1 = GuiTheme.accentColor1 != null ? GuiTheme.accentColor1 : new Color(167, 139, 250);
            color6 = ColorUtil.withAlpha(color1, 55);
        }

        color = color6;
        f = 0.1F;
        f1 = 1.0F;
        f2 = 8.0F;
        f3 = f11;
        f4 = f10;
        float f5 = f9;
        Render2DUtil.drawOutline(f8, f5, f4, f3, f2, f1, f, color);
        VoilClient.fonts.getIcons().render(IconGlyphs.iconE155(), i + 7, k1 + 5, 13.0F, new Color(122, 124, 134).getRGB());
        String s6;
        if (this.searchQuery.isEmpty()) {
            s6 = "Search...";
        } else if (this.searchQuery.length() >= 15) {
            String s3 = this.searchQuery.substring(0, 15);
            s6 = s3 + "...";
        } else {
            s6 = this.searchQuery;
        }

        String s = s6;
        VoilClient.fonts.getSelectedFont().render(s, i + 24, k1 + 4, this.searchQuery.isEmpty() ? new Color(122, 124, 134).getRGB() : -1);
        this.entries.add(new ClickArea(new Rectangle(i, k1, k, 24), () -> this.loading = true));
        k1 += 34;
        f8 = i;
        f9 = k1;
        f10 = k;
        f11 = 20;
        Color color2 = GuiTheme.settingsBg != null ? GuiTheme.settingsBg : new Color(24, 24, 30);
        color = ColorUtil.withAlpha(color2, 150);
        f = 1.0F;
        f1 = 8.0F;
        f2 = f11;
        f3 = f10;
        f4 = f9;
        Render2DUtil.drawRoundedRect(f8, f4, f3, f2, f1, f, color);
        float f6 = (float)k / this.tabs.length;

        for (int l1 = 0; l1 < this.tabs.length; l1++) {
            String s1 = this.tabs[l1];
            boolean flag = this.activeTab.equals(s1);
            int i2 = (int)(i + l1 * f6);
            if (flag) {
                f8 = i2 + 3;
                f9 = k1 + 3;
                int j3 = (int)f6;
                f10 = j3 + -7 + 1;
                f11 = 14;
                Color color3 = GuiTheme.accentColor1 != null ? GuiTheme.accentColor1 : new Color(167, 139, 250);
                color = ColorUtil.withAlpha(color3, 90);
                f = 1.0F;
                f1 = 6.0F;
                f2 = f11;
                f3 = f10;
                f4 = f9;
                Render2DUtil.drawRoundedRect(f8, f4, f3, f2, f1, f, color);
                f8 = i2 + 3;
                f9 = k1 + 3;
                j3 = (int)f6;
                f10 = j3 + -7 + 1;
                f11 = 14;
                color = GuiTheme.accentColor1 != null ? GuiTheme.accentColor1 : new Color(167, 139, 250);
                f = 0.4F;
                f1 = 0.3F;
                f2 = 6.0F;
                f3 = f11;
                f4 = f10;
                f5 = f9;
                Render2DUtil.drawOutline(f8, f5, f4, f3, f2, f1, f, color);
            }

            VoilClient.fonts
                .getSelectedFont()
                .render(
                    s1, i2 + (f6 - VoilClient.fonts.getSelectedFont().getWidth(s1, 8.0F)) / 2.0F, k1 + 5, 8.0F, flag ? -1 : new Color(122, 124, 134).getRGB()
                );
            int tab = l1;
            this.entries.add(new ClickArea(new Rectangle(i2, k1, (int)f6, 20), () -> {
                this.activeTab = this.tabs[tab];
                this.scrollOffset = 0;
            }));
        }

        k1 += 30;
        float f7 = (float)k / this.sortModes.length;

        for (int j4 = 0; j4 < this.sortModes.length; j4++) {
            String s5 = this.sortModes[j4];
            boolean flag1 = this.sortMode.equals(s5);
            int j2 = (int)(i + j4 * f7);
            if (flag1) {
                f8 = j2 + 2;
                f9 = k1;
                int l4 = (int)f7;
                f10 = l4 + -5 + 1;
                f11 = 18;
                Color color4 = GuiTheme.accentColor1 != null ? GuiTheme.accentColor1 : new Color(167, 139, 250);
                color = ColorUtil.withAlpha(color4, 70);
                f = 1.0F;
                f1 = 6.0F;
                f2 = f11;
                f3 = f10;
                f4 = f9;
                Render2DUtil.drawRoundedRect(f8, f4, f3, f2, f1, f, color);
                f8 = j2 + 2;
                f9 = k1;
                l4 = (int)f7;
                f10 = l4 + -5 + 1;
                f11 = 18;
                color = GuiTheme.accentColor1 != null ? GuiTheme.accentColor1 : new Color(167, 139, 250);
                f = 0.4F;
                f1 = 0.3F;
                f2 = 6.0F;
                f3 = f11;
                f4 = f10;
                f5 = f9;
                Render2DUtil.drawOutline(f8, f5, f4, f3, f2, f1, f, color);
            }

            int i5 = (int)f7;
            int l3 = i5 + -7 + 1;
            String s2 = this.truncate(s5, l3);
            VoilClient.fonts
                .getSelectedFont()
                .render(
                    s2, j2 + (f7 - VoilClient.fonts.getSelectedFont().getWidth(s2, 8.0F)) / 2.0F, k1 + 5, 8.0F, flag1 ? -1 : new Color(122, 124, 134).getRGB()
                );
            int mode = j4;
            this.entries.add(new ClickArea(new Rectangle(j2, k1, (int)f7, 18), () -> {
                this.sortMode = this.sortModes[mode];
                this.scrollOffset = 0;
            }));
        }

        k1 += 28;
        int k4 = l + -39 + 1;
        List<ColorTheme> list = this.buildEntries();
        int j5 = list.size();
        int l5 = j5 * 51;
        int k3 = k4 - k1;
        j5 = l5;
        this.scrollMax = Math.max(0, j5 - k3);
        int k5 = i - 1;
        j5 = i + k;
        int i4 = j5 + 1;
        RenderManager.setScissor(k5, k1, i4, k4);
        k3 = this.scrollOffset;
        j5 = k1;
        int k2 = j5 - k3;

        for (ColorTheme colortheme : list) {
            if (k2 + 46 >= k1 && k2 <= k4) {
                this.drawRow(i, k2, k, 46, colortheme, i1, j1);
            }

            k2 += 43 + (43 & 4 * 2);
        }

        RenderManager.disableScissor();
        k3 = (k - 116) / 2;
        j5 = i;
        int l2 = j5 + k3;
        j5 = l - 22;
        int i3 = j5 + -3 + 1;
        f8 = l2;
        f9 = i3;
        f10 = 116;
        f11 = 22;
        Color color5 = GuiTheme.accentColor1 != null ? GuiTheme.accentColor1 : new Color(167, 139, 250);
        color = ColorUtil.withAlpha(color5, 70);
        f = 1.0F;
        f1 = 8.0F;
        f2 = f11;
        f3 = f10;
        f4 = f9;
        Render2DUtil.drawRoundedRect(f8, f4, f3, f2, f1, f, color);
        f8 = l2;
        f9 = i3;
        f10 = 116;
        f11 = 22;
        color = GuiTheme.accentColor1 != null ? GuiTheme.accentColor1 : new Color(167, 139, 250);
        f = 0.4F;
        f1 = 0.3F;
        f2 = 8.0F;
        f3 = f11;
        f4 = f10;
        f5 = f9;
        Render2DUtil.drawOutline(f8, f5, f4, f3, f2, f1, f, color);
        VoilClient.fonts.getSelectedFont().render("Create New", l2 + 32, i3 + 4, -1);
        this.entries.add(new ClickArea(new Rectangle(l2, i3, 116, 22), this::refresh));
    }

    private void resetEditor() {
        Color color = this.parseColor(this.hexInput.trim());
        if (color != null) {
            float[] afloat = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
            this.pickerHue = afloat[0];
            this.pickerSaturation = afloat[1];
            this.pickerBrightness = afloat[2];
            if (this.colorCallback != null) {
                this.colorCallback.accept(color);
            }

            this.hexInput = this.colorToHex(color);
        }
    }

    private void drawEditor(int i, int j, int k, int l, boolean flag) {
        float f6 = i;
        float f7 = j;
        float f8 = k;
        float f9 = l;
        Color color1 = GuiTheme.modeSettingBg != null ? GuiTheme.modeSettingBg : new Color(30, 30, 36);
        Color color = ColorUtil.withAlpha(color1, 180);
        float f = 1.0F;
        float f1 = 10.0F;
        float f2 = f9;
        float f3 = f8;
        float f4 = f7;
        Render2DUtil.drawRoundedRect(f6, f4, f3, f2, f1, f, color);
        f6 = i;
        f7 = j;
        f8 = k;
        f9 = l;
        Color color2 = GuiTheme.accentColor1 != null ? GuiTheme.accentColor1 : new Color(167, 139, 250);
        color = ColorUtil.withAlpha(color2, 55);
        f = 0.5F;
        f1 = 0.5F;
        f2 = 10.0F;
        f3 = f9;
        f4 = f8;
        float f5 = f7;
        Render2DUtil.drawOutline(f6, f5, f4, f3, f2, f1, f, color);
        int k2 = k + -21 + 1;
        int i1 = (k2 + -17 + 1) / 3;
        int j1 = i + 10;
        k2 = j1 + i1;
        int k1 = k2 + 8;
        k2 = k1 + i1;
        int l1 = k2 + 8;
        int i2 = j + 20;
        int j2 = i2 + 34;
        VoilClient.fonts.getSelectedFont().render("Settings", j1, j + 6, 9.0F, new Color(122, 124, 134).getRGB());
        Supplier<Color> supplier5 = () -> this.editingTheme.settingsHeader;
        Consumer<Color> consumer5 = value -> this.editingTheme.settingsHeader = value;
        color = GuiTheme.settingsHeader != null ? GuiTheme.settingsHeader : (GuiTheme.accentColor1 != null ? GuiTheme.accentColor1 : new Color(167, 139, 250));
        Consumer<Color> consumer = consumer5;
        Supplier<Color> supplier = supplier5;
        this.drawField(j1, i2, i1, "Header", flag, supplier, consumer, color);
        supplier5 = () -> this.editingTheme.settingsBackground;
        consumer5 = value -> this.editingTheme.settingsBackground = value;
        color = GuiTheme.settingsBg != null ? GuiTheme.settingsBg : new Color(24, 24, 30);
        Consumer<Color> consumer1 = consumer5;
        Supplier<Color> supplier1 = supplier5;
        this.drawField(j1, j2, i1, "Background", flag, supplier1, consumer1, color);
        VoilClient.fonts.getSelectedFont().render("Background", k1, j + 6, 9.0F, new Color(122, 124, 134).getRGB());
        supplier5 = () -> this.editingTheme.modeBackground;
        consumer5 = value -> this.editingTheme.modeBackground = value;
        color = GuiTheme.modeSettingBg != null ? GuiTheme.modeSettingBg : new Color(30, 30, 36);
        Consumer<Color> consumer2 = consumer5;
        Supplier<Color> supplier2 = supplier5;
        this.drawField(k1, i2, i1, "Background", flag, supplier2, consumer2, color);
        VoilClient.fonts.getSelectedFont().render("Text", l1, j + 6, 9.0F, new Color(122, 124, 134).getRGB());
        supplier5 = () -> this.editingTheme.normalText;
        consumer5 = value -> this.editingTheme.normalText = value;
        color = GuiTheme.normalTextColor != null ? GuiTheme.normalTextColor : Color.WHITE;
        Consumer<Color> consumer3 = consumer5;
        Supplier<Color> supplier3 = supplier5;
        this.drawField(l1, i2, i1, "Normal", flag, supplier3, consumer3, color);
        supplier5 = () -> this.editingTheme.secondaryText;
        consumer5 = value -> this.editingTheme.secondaryText = value;
        color = GuiTheme.unfocusedTextColor != null ? GuiTheme.unfocusedTextColor : new Color(122, 124, 134);
        Consumer<Color> consumer4 = consumer5;
        Supplier<Color> supplier4 = supplier5;
        this.drawField(l1, j2, i1, "Secondary", flag, supplier4, consumer4, color);
    }

    private void drawRow(int i, int j, int k, int l, ColorTheme colortheme, int i1, int j1) {
        boolean flag = colortheme.name().equals(this.statusMessage);
        float f13 = i;
        float f14 = j;
        float f15 = i + k;
        float f16 = j + l;
        double d1 = i1;
        double d0 = j1;
        if (ClickGuiComponent.isPointInRect(f13, f14, f15, f16, d1, d0) || flag) {
            f13 = i;
            f14 = j;
            f15 = k;
            f16 = l;
            Color color6 = GuiTheme.modeSettingBg != null ? GuiTheme.modeSettingBg : new Color(30, 30, 36);
            int i3 = flag ? 220 : 130;
            Color color4 = color6;
            Color color = ColorUtil.withAlpha(color4, i3);
            float f = 1.0F;
            float f1 = 8.0F;
            float f2 = f16;
            float f3 = f15;
            float f4 = f14;
            Render2DUtil.drawRoundedRect(f13, f4, f3, f2, f1, f, color);
            if (flag) {
                f13 = i;
                f14 = j;
                f15 = k;
                f16 = l;
                color = GuiTheme.accentColor1 != null ? GuiTheme.accentColor1 : new Color(167, 139, 250);
                f = 0.5F;
                f1 = 0.4F;
                f2 = 8.0F;
                f3 = f16;
                f4 = f15;
                float f5 = f14;
                Render2DUtil.drawOutline(f13, f5, f4, f3, f2, f1, f, color);
            }
        }

        int k1 = j + (l - 36) / 2;
        int l1 = 34 / 2;
        f13 = i + 8;
        float f7 = k1;
        Render2DUtil.drawRoundedRect(f13, f7, l1, l1, 4.0F, 1.0F, colortheme.primary());
        int l2 = i + 8;
        l2 += l1;
        f13 = l2 + 2;
        float f8 = k1;
        Render2DUtil.drawRoundedRect(f13, f8, l1, l1, 4.0F, 1.0F, colortheme.secondary());
        f13 = i + 8;
        l2 = k1 + l1;
        float f9 = l2 + 2;
        Render2DUtil.drawRoundedRect(f13, f9, l1, l1, 4.0F, 1.0F, colortheme.background());
        l2 = i + 8;
        l2 += l1;
        f13 = l2 + 2;
        l2 = k1 + l1;
        float f10 = l2 + 2;
        Render2DUtil.drawRoundedRect(f13, f10, l1, l1, 4.0F, 1.0F, colortheme.accent());
        l2 = i + 8;
        l2 += 36;
        int i2 = l2 + 10;
        VoilClient.fonts.getSelectedFont().render(colortheme.author(), i2, j + 11, -1);
        VoilClient.fonts.getSelectedFont().render(colortheme.description(), i2, j + 24, 8.0F, new Color(122, 124, 134).getRGB());
        l2 = i + k;
        int j2 = l2 + -11 + 1;
        int k2 = j2 + -13 + 1;
        if (colortheme.featured()) {
            boolean flag1 = this.dragging && colortheme.name().equals(this.focusedField);
            VoilClient.fonts
                .getIcons()
                .render(IconGlyphs.iconE18E(), k2, j + 9, 11.0F, flag1 ? new Color(220, 80, 90).getRGB() : new Color(122, 124, 134).getRGB());
            this.entries.add(new ClickArea(new Rectangle(k2 - 1, j + 8, 14, 14), () -> this.toggleFav(colortheme)));
            k2 -= 14;
        }

        if (colortheme.dark()) {
            f13 = k2;
            f14 = j + 9;
            Color color5 = GuiTheme.accentColor1 != null ? GuiTheme.accentColor1 : new Color(167, 139, 250);
            float f11 = 11.0F;
            Render2DUtil.drawStar(f13, f14, f11, color5);
        } else {
            float f12 = k2;
            Render2DUtil.drawStarAt(f12, j + 9, 11.0F, 0.5F, new Color(122, 124, 134));
        }

        this.entries.add(new ClickArea(new Rectangle(k2, j + 8, 14, 14), () -> this.delete(colortheme)));
        k2 -= 14;
        VoilClient.fonts.getIcons().render(IconGlyphs.iconE28D(), k2, j + 9, 11.0F, new Color(122, 124, 134).getRGB());
        this.entries.add(new ClickArea(new Rectangle(k2 - 1, j + 8, 14, 14), () -> this.fork(colortheme)));
        int j3 = j + 28;
        boolean flag2 = colortheme.version() > 0;
        boolean flag3 = colortheme.version() < 0;
        Color color1 = flag2 ? new Color(80, 200, 130) : new Color(122, 124, 134);
        Color color2 = flag3 ? new Color(220, 90, 100) : new Color(122, 124, 134);
        Color color3 = flag2 ? color1 : (flag3 ? color2 : new Color(122, 124, 134));
        String s = String.valueOf(colortheme.version());
        float f6 = VoilClient.fonts.getSelectedFont().getWidth(s, 8.0F);
        VoilClient.fonts.getSelectedFont().render(s, j2 - f6, j3, 8.0F, color3.getRGB());
        VoilClient.fonts.getIcons().render(IconGlyphs.iconE1E4(), j2 - f6 - 11.0F, j3 - 1, 10.0F, color1.getRGB());
        VoilClient.fonts.getIcons().render(IconGlyphs.iconE1E1(), j2 - f6 - 22.0F, j3 - 1, 10.0F, color2.getRGB());
        this.entries.add(new ClickArea(new Rectangle((int)(j2 - f6 - 12.0F), j3 + -3 + 1, 11, 12), () -> this.setFavorite(colortheme, true)));
        this.entries.add(new ClickArea(new Rectangle((int)(j2 - f6 - 23.0F), j3 + -3 + 1, 11, 12), () -> this.setFavorite(colortheme, false)));
        this.entries.add(new ClickArea(new Rectangle(i, j, k + -35 + 1, l), () -> this.vote(colortheme)));
    }

    private void toggleFav(ColorTheme colortheme) {
        if (colortheme.featured()) {
            if (!colortheme.name().equals(this.focusedField)) {
                this.focusedField = colortheme.name();
                this.dragging = true;
            } else {
                String s = colortheme.name();
                ApiClient.INSTANCE.deleteCustomTheme(s).thenAccept(apiresponse -> {
                    if (colortheme.name().equals(this.statusMessage)) {
                        this.statusMessage = "";
                    }

                    this.dragging = false;
                    this.focusedField = "";
                    this.close();
                });
            }
        }
    }

    private void scrollTo(int i) {
        String s = "";
        float[] afloat = new float[]{0.0F, 2.0F, 4.0F, 6.0F, 8.0F, 10.0F, 12.0F, 14.0F, 16.0F, 18.0F, 20.0F};
        float f = this.editingTheme.radii[i];
        float f1 = afloat[0];

        for (float f2 : afloat) {
            if (f2 > f) {
                f1 = f2;
                break;
            }
        }

        this.editingTheme.radii[i] = f1;
    }

    private Rectangle getPanelBounds() {
        int i = this.pickerX;
        int j = i + 8;
        i = this.pickerY;
        i += 8;
        i += 80;
        return new Rectangle(j, i + 6, 100, 8);
    }

    private void scrollBy(double d0) {
        Rectangle rectangle = this.getPanelBounds();
        float f = (float)((d0 - rectangle.x) / rectangle.width);
        this.pickerHue = lerp(f, 0.0F, 1.0F);
        this.saveDraft();
    }

    private void dragPicker(double d0, double d1) {
        Rectangle rectangle = this.getEditorBounds();
        float f = (float)((d0 - rectangle.x) / rectangle.width);
        this.pickerSaturation = lerp(f, 0.0F, 1.0F);
        float f1 = 1.0F - (float)((d1 - rectangle.y) / rectangle.height);
        this.pickerBrightness = lerp(f1, 0.0F, 1.0F);
        this.saveDraft();
    }

    private void setHue(double d0) {
        float f = (float)(d0 - this.panelBounds.x) / this.panelBounds.width;
        this.editingTheme.borderThickness = lerp(f, 0.0F, 1.0F) * 4.0F;
    }

    private String getThemeId(ThemeDraft themedraft) {
        Color color = themedraft.windowColor;
        String s5 = this.colorToHex(color);
        Color color1 = themedraft.accentColor1;
        String s6 = this.colorToHex(color1);
        Color color2 = themedraft.enabledColor;
        String s7 = this.colorToHex(color2);
        Color color3 = themedraft.accentColor2;
        String s3 = this.colorToHex(color3);
        String s2 = s7;
        String s1 = s6;
        String s = s5;
        return s + "," + s1 + "," + s2 + "," + s3;
    }

    private Rectangle getListBounds() {
        int j = this.pickerX + 8;
        int i = this.pickerY;
        i += 8;
        i += 80;
        i = i + 6 + 8;
        return new Rectangle(j, i + 6, 100, 14);
    }

    private void loadCustom() {
        float f11 = this.pickerX;
        float f7 = this.pickerY;
        Render2DUtil.drawRoundedRect(f11, f7, 116, 130, 8.0F, 1.0F, new Color(18, 18, 24, 248));
        f11 = this.pickerX;
        float f12 = this.pickerY;
        float f13 = 116;
        float f14 = 130;
        Color color = GuiTheme.accentColor1 != null ? GuiTheme.accentColor1 : new Color(167, 139, 250);
        float f = 0.5F;
        float f1 = 0.4F;
        float f2 = 8.0F;
        float f3 = f14;
        float f4 = f13;
        float f5 = f12;
        Render2DUtil.drawOutline(f11, f5, f4, f3, f2, f1, f, color);
        Rectangle rectangle = this.getEditorBounds();
        f11 = rectangle.x;
        f12 = rectangle.y;
        f13 = rectangle.width;
        float f8 = rectangle.height;
        Render2DUtil.drawRectGradient(f11, f12, f13, f8, 2.0F, 1.0F, Color.BLACK, Color.WHITE, Color.BLACK, Color.getHSBColor(this.pickerHue, 1.0F, 1.0F));
        Render2DUtil.drawCircle(
            rectangle.x + this.pickerSaturation * rectangle.width - 3.0F,
            rectangle.y + (1.0F - this.pickerBrightness) * rectangle.height - 3.0F,
            3.0F,
            1.0F,
            Color.WHITE
        );
        Rectangle rectangle1 = this.getPanelBounds();

        for (int i = 0; i < rectangle1.width; i++) {
            float f6 = (float)i / rectangle1.width;
            int j = rectangle1.x;
            f11 = j + i;
            f12 = rectangle1.y;
            f14 = rectangle1.height;
            color = Color.getHSBColor(f6, 1.0F, 1.0F);
            f = 1.0F;
            f1 = 0.0F;
            f2 = f14;
            f3 = 2.0F;
            f4 = f12;
            Render2DUtil.drawRoundedRect(f11, f4, f3, f2, f1, f, color);
        }

        float f10 = rectangle1.x + this.pickerHue * rectangle1.width;
        Render2DUtil.drawCircle(f10 - 3.0F, rectangle1.y + rectangle1.height / 2.0F - 3.0F, 3.0F, 1.0F, Color.WHITE);
        Render2DUtil.drawCircle(f10 - 2.0F, rectangle1.y + rectangle1.height / 2.0F - 2.0F, 2.0F, 1.0F, Color.getHSBColor(this.pickerHue, 1.0F, 1.0F));
        Rectangle rectangle2 = this.getListBounds();
        f11 = rectangle2.x;
        f12 = rectangle2.y;
        f13 = rectangle2.width;
        f14 = rectangle2.height;
        color = ColorUtil.scaleBrightness(GuiTheme.settingsBg != null ? GuiTheme.settingsBg : new Color(24, 24, 30), 1.4F);
        f = 1.0F;
        f1 = 4.0F;
        f2 = f14;
        f3 = f13;
        f4 = f12;
        Render2DUtil.drawRoundedRect(f11, f4, f3, f2, f1, f, color);
        MSDFFontAtlas msdffontatlas = VoilClient.fonts.getSelectedFont();
        String s = this.hexInput.isEmpty() ? "#RRGGBB" : this.hexInput;
        int k = rectangle2.x;
        msdffontatlas.render(s, k + 4, rectangle2.y, this.hexInput.isEmpty() ? new Color(122, 124, 134).getRGB() : -1);
        if (this.confirmDelete) {
            f11 = rectangle2.x;
            float f9 = rectangle2.y;
            Render2DUtil.drawOutline(f11, f9, rectangle2.width, rectangle2.height, 4.0F, 0.4F, 0.5F, Color.WHITE);
        }
    }

    private List<ColorTheme> buildEntries() {
        ArrayList<ColorTheme> arraylist = new ArrayList<>();
        if (this.activeTab.equals("All") || this.activeTab.equals("Built-In")) {
            for (ThemePreset themepreset : ClientSettings.themePresets.values()) {
                arraylist.add(
                    new ColorTheme(
                        themepreset.getAuthor(),
                        themepreset.getName(),
                        "Official",
                        false,
                        themepreset.getCategory(),
                        themepreset.getBackgroundColor(),
                        themepreset.getStartColor(),
                        themepreset.getAccentColor(),
                        themepreset.getEndColor(),
                        this.favorites.contains(themepreset.getAuthor()),
                        0
                    )
                );
            }
        }

        List<ThemeInfo> list = this.activeTab.equals("Private")
            ? this.customThemes
            : (!this.activeTab.equals("Public") && !this.activeTab.equals("All") ? null : this.visibleEntries);
        if (list != null) {
            for (ThemeInfo themeinfo : list) {
                arraylist.add(this.toEntry(themeinfo));
            }
        }

        if (!this.searchQuery.isEmpty()) {
            String s = this.searchQuery.toLowerCase();
            arraylist.removeIf(colortheme -> !colortheme.author().toLowerCase().contains(s) && !colortheme.description().toLowerCase().contains(s));
        }

        switch (this.sortMode) {
            case "Favourites" -> arraylist.removeIf(colortheme -> !colortheme.dark());
            case "Light" -> arraylist.removeIf(colortheme -> !"light".equalsIgnoreCase(colortheme.id()));
            case "Dark" -> arraylist.removeIf(colortheme -> !"dark".equalsIgnoreCase(colortheme.id()));
            default -> {
            }
        }

        arraylist.sort(
            (colortheme, colortheme1) -> {
                boolean flag = colortheme.author().equalsIgnoreCase("Default");
                boolean flag1 = colortheme1.author().equalsIgnoreCase("Default");
                if (flag != flag1) {
                    return flag ? -1 : 1;
                } else if (colortheme.dark() != colortheme1.dark()) {
                    return colortheme.dark() ? -1 : 1;
                } else {
                    return this.sortMode.equals("Top") && colortheme.version() != colortheme1.version()
                        ? Integer.compare(colortheme1.version(), colortheme.version())
                        : colortheme.author().compareToIgnoreCase(colortheme1.author());
                }
            }
        );
        return arraylist;
    }

    private void openEditor(ThemeDraft themedraft) {
        this.editingTheme = themedraft;
        this.searchFocused = true;
    }

    private void drawField(int i, int j, int k, String s, boolean flag, Supplier<Color> supplier, Consumer<Color> consumer, Color color) {
        Color color2 = flag ? (Color)supplier.get() : color;
        VoilClient.fonts.getSelectedFont().render(this.truncate(s, k), i, j, 8.0F, new Color(122, 124, 134).getRGB());
        int l = j + 12;
        int i1 = i + 16 + 4;
        float f9 = i;
        float f8 = l;
        Render2DUtil.drawRoundedRect(f9, f8, 16, 16, GuiTheme.radiusBottomRight, 1.0F, color2);
        f9 = i;
        float f10 = l;
        float f11 = 16;
        float f12 = 16;
        float f13 = GuiTheme.radiusBottomRight;
        Color color3 = GuiTheme.accentColor1 != null ? GuiTheme.accentColor1 : new Color(167, 139, 250);
        Color color1 = ColorUtil.withAlpha(color3, 55);
        float f = 0.1F;
        float f1 = 1.0F;
        float f2 = f13;
        float f3 = f12;
        float f4 = f11;
        float f5 = f10;
        Render2DUtil.drawOutline(f9, f5, f4, f3, f2, f1, f, color1);
        f9 = i1;
        f10 = l;
        f11 = 16;
        f12 = 16;
        color1 = GuiTheme.settingsBg != null ? GuiTheme.settingsBg : new Color(24, 24, 30);
        f = 1.0F;
        f1 = GuiTheme.radiusBottomRight;
        f2 = f12;
        f3 = f11;
        f4 = f10;
        Render2DUtil.drawRoundedRect(f9, f4, f3, f2, f1, f, color1);
        f9 = i1;
        f10 = l;
        f11 = 16;
        f12 = 16;
        f13 = GuiTheme.radiusBottomRight;
        Color color4 = GuiTheme.accentColor1 != null ? GuiTheme.accentColor1 : new Color(167, 139, 250);
        color1 = ColorUtil.withAlpha(color4, 55);
        f = 0.1F;
        f1 = 1.0F;
        f2 = f13;
        f3 = f12;
        f4 = f11;
        f5 = f10;
        Render2DUtil.drawOutline(f9, f5, f4, f3, f2, f1, f, color1);
        float f6 = VoilClient.fonts.getSelectedFont().getLineHeight() * (12.0F / VoilClient.fonts.getSelectedFont().getSize());
        float f7 = VoilClient.fonts.getSelectedFont().getWidth("A", 12.0F);
        VoilClient.fonts.getSelectedFont().render("A", i1 + (16 - f7) / 2.0F, l + (16 - f6) / 2.0F, 12.0F, new Color(122, 124, 134).getRGB());
        if (flag) {
            this.entries.add(new ClickArea(new Rectangle(i, l, 36, 16), () -> {
                this.drawColorField(supplier.get(), consumer, i, l + 16 + 4);
            }));
        }
    }

    private void delete(ColorTheme colortheme) {
        if (colortheme.featured()) {
            String s = colortheme.name();
            ApiClient.INSTANCE.favoriteCustomTheme(s).thenAccept(obool -> this.close());
        } else if (!this.favorites.remove(colortheme.name())) {
            this.favorites.add(colortheme.name());
        }
    }

    private void closeEditor() {
        this.editingTheme = null;
        this.searchFocused = false;
        this.colorPickerOpen = false;
    }

    private boolean isEditing() {
        return "voil+".equalsIgnoreCase(String.valueOf(ClientSettings.userRank.getValue()));
    }

    private String truncate(String s, int i) {
        if (VoilClient.fonts.getSelectedFont().getWidth(s, 8.0F) <= i) {
            return s;
        }

        while (s.length() > 1) {
            MSDFFontAtlas msdffontatlas = VoilClient.fonts.getSelectedFont();
            if (!(msdffontatlas.getWidth(s + "…", 8.0F) > i)) {
                break;
            }

            int j = s.length();
            s = s.substring(0, j - 1);
        }

        return s + "…";
    }

    private void saveDraft() {
        Color color = Color.getHSBColor(this.pickerHue, this.pickerSaturation, this.pickerBrightness);
        if (this.colorCallback != null) {
            this.colorCallback.accept(color);
        }

        this.hexInput = this.colorToHex(color);
    }

    private static float lerp(float f, float f1, float f2) {
        return f < f1 ? f1 : (f > f2 ? f2 : f);
    }

    private String colorToHex(Color color) {
        return String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
    }

    private ColorTheme toEntry(ThemeInfo themeinfo) {
        Color[] acolor = themeinfo.getPreviewColors();
        Color color = acolor.length > 0 && acolor[0] != null ? acolor[0] : new Color(40, 40, 48);
        Color color1 = acolor.length > 1 && acolor[1] != null ? acolor[1] : (GuiTheme.accentColor1 != null ? GuiTheme.accentColor1 : new Color(167, 139, 250));
        Color color2 = acolor.length > 2 && acolor[2] != null ? acolor[2] : new Color(28, 28, 34);
        Color color3 = acolor.length > 3 && acolor[3] != null ? acolor[3] : (GuiTheme.accentColor2 != null ? GuiTheme.accentColor2 : new Color(196, 181, 253));
        return new ColorTheme(
            themeinfo.id,
            themeinfo.name,
            themeinfo.owner.isEmpty() ? "You" : themeinfo.owner,
            true,
            themeinfo.type,
            color,
            color1,
            color2,
            color3,
            themeinfo.favorited,
            themeinfo.getScore()
        );
    }

    private Rectangle getEditorBounds() {
        int i = this.pickerX;
        int j = i + 8;
        i = this.pickerY;
        return new Rectangle(j, i + 8, 100, 80);
    }

    private void radiusField(int i, int j, int k, int l) {
        float f10 = i;
        float f8 = j;
        Render2DUtil.drawRoundedRect(f10, f8, k, l, 10.0F, 1.0F, new Color(8, 8, 11, 170));
        int l1 = (k + -181 + 1) / 2;
        int k1 = i;
        int i1 = k1 + l1;
        l1 = (l - 64) / 2;
        k1 = j;
        int j1 = k1 + l1;
        f10 = i1;
        float f9 = j1;
        Render2DUtil.drawRoundedRect(f10, f9, 180, 64, 10.0F, 1.0F, new Color(18, 18, 24, 235));
        f10 = i1;
        float f11 = j1;
        float f12 = 180;
        float f13 = 64;
        Color color = GuiTheme.accentColor1 != null ? GuiTheme.accentColor1 : new Color(167, 139, 250);
        float f = 0.6F;
        float f1 = 0.4F;
        float f2 = 10.0F;
        float f3 = f13;
        float f4 = f12;
        float f5 = f11;
        Render2DUtil.drawOutline(f10, f5, f4, f3, f2, f1, f, color);
        boolean flag = this.isEditing();
        String[] astring = flag
            ? new String[]{"Click create new or fork", "an existing theme to", "open editor"}
            : new String[]{"Theme editor is", "voil+ only", "upgrade to create themes"};
        float f6 = VoilClient.fonts.getSelectedFont().getLineHeight();
        float f7 = j1 + (64 - astring.length * (f6 + 3.0F)) / 2.0F;

        for (String s : astring) {
            VoilClient.fonts
                .getSelectedFont()
                .render(
                    s,
                    i1 + (180 - VoilClient.fonts.getSelectedFont().getWidth(s)) / 2.0F,
                    f7,
                    !flag && s.contains("voil+") ? (GuiTheme.accentColor1 != null ? GuiTheme.accentColor1 : new Color(167, 139, 250)).getRGB() : -1
                );
            f7 += f6 + 3.0F;
        }
    }

    private ThemeDraft createDraft(String s, String s1) {
        ThemeDraft themedraft = this.parseTheme(s1);

        try {
            JsonObject jsonobject = JsonParser.parseString(s).getAsJsonObject();
            themedraft.windowColor = this.readColor(jsonobject, "windowColor", themedraft.windowColor);
            themedraft.sidebarColor = this.readColor(jsonobject, "leftPanel", themedraft.sidebarColor);
            themedraft.settingsBackground = this.readColor(jsonobject, "settingsBg", themedraft.settingsBackground);
            themedraft.settingsHeader = this.readColor(jsonobject, "settingsHeader", themedraft.settingsHeader);
            themedraft.moduleDisabledBackground = this.readColor(jsonobject, "moduleDisabledBg", themedraft.moduleDisabledBackground);
            themedraft.moduleBackground = this.readColor(jsonobject, "moduleColor", themedraft.moduleBackground);
            themedraft.moduleText = this.readColor(jsonobject, "moduleText", themedraft.moduleText);
            themedraft.secondaryText = this.readColor(jsonobject, "unfocusedTextColor", themedraft.secondaryText);
            themedraft.normalText = this.readColor(jsonobject, "normalTextColor", themedraft.normalText);
            themedraft.modeBackground = this.readColor(jsonobject, "modeSettingBg", themedraft.modeBackground);
            themedraft.modeFill = this.readColor(jsonobject, "modeSettingFill", themedraft.modeFill);
            themedraft.sliderBackground = this.readColor(jsonobject, "sliderSettingBg", themedraft.sliderBackground);
            themedraft.outlineColor = this.readColor(jsonobject, "outlineColor", themedraft.outlineColor);
            themedraft.accentColor1 = this.readColor(jsonobject, "accentColor1", themedraft.accentColor1);
            themedraft.accentColor2 = this.readColor(jsonobject, "accentColor2", themedraft.accentColor2);
            themedraft.enabledColor = this.readColor(jsonobject, "enabled", themedraft.enabledColor);
            if (jsonobject.has("glowEnabled")) {
                themedraft.glow = jsonobject.get("glowEnabled").getAsBoolean();
            }

            if (jsonobject.has("borderThickness")) {
                themedraft.borderThickness = jsonobject.get("borderThickness").getAsFloat();
            }

            if (jsonobject.has("radius") && jsonobject.get("radius").isJsonArray()) {
                JsonArray jsonarray = jsonobject.getAsJsonArray("radius");

                for (int i = 0; i < Math.min(4, jsonarray.size()); i++) {
                    themedraft.radii[i] = jsonarray.get(i).getAsFloat();
                }
            }
        } catch (Exception exception) {
        }

        return themedraft;
    }

    private String serializeTheme(ThemeDraft themedraft) {
        JsonObject jsonobject = new JsonObject();
        this.writeColor(jsonobject, "windowColor", themedraft.windowColor);
        this.writeColor(jsonobject, "leftPanel", themedraft.sidebarColor);
        this.writeColor(jsonobject, "settingsBg", themedraft.settingsBackground);
        this.writeColor(jsonobject, "settingsHeader", themedraft.settingsHeader);
        this.writeColor(jsonobject, "moduleDisabledBg", themedraft.moduleDisabledBackground);
        this.writeColor(jsonobject, "moduleColor", themedraft.moduleBackground);
        this.writeColor(jsonobject, "moduleText", themedraft.moduleText);
        this.writeColor(jsonobject, "unfocusedTextColor", themedraft.secondaryText);
        this.writeColor(jsonobject, "normalTextColor", themedraft.normalText);
        this.writeColor(jsonobject, "modeSettingBg", themedraft.modeBackground);
        this.writeColor(jsonobject, "modeSettingFill", themedraft.modeFill);
        this.writeColor(jsonobject, "sliderSettingBg", themedraft.sliderBackground);
        this.writeColor(jsonobject, "outlineColor", themedraft.outlineColor);
        this.writeColor(jsonobject, "accentColor1", themedraft.accentColor1);
        this.writeColor(jsonobject, "accentColor2", themedraft.accentColor2);
        this.writeColor(jsonobject, "enabled", themedraft.enabledColor);
        jsonobject.addProperty("glowEnabled", themedraft.glow);
        jsonobject.addProperty("borderThickness", themedraft.borderThickness);
        JsonArray jsonarray = new JsonArray();

        for (float f : themedraft.radii) {
            jsonarray.add(f);
        }

        jsonobject.add("radius", jsonarray);
        return jsonobject.toString();
    }

    private String generateName() {
        for (ThemePreset themepreset : ClientSettings.themePresets.values()) {
            if (themepreset.getAuthor().equals(this.statusMessage)) {
                return themepreset.getName();
            }
        }

        return "Voil Classic";
    }

    @Override
    public void mouseClicked2(double d0, double d1, int i) {
        this.mouseClicked(d0, d1);
    }

    @Override
    public void render2(MatrixStack matrixstack, int i, int j, float f) {
        this.render(i, j);
    }

    @Override
    public void mouseDragged2(double d0, double d1, int i, double d2, double d3) {
        this.mouseDragged(d0, d1);
    }

    @Override
    public void mouseScrolled2(double d1, double d2, double d3, double d0) {
        this.mouseScrolled(d0);
    }

    @Override
    public void mouseReleased2(double d0, double d1, int i) {
        this.mouseReleased();
    }

    @Override
    public void keyReleased2(int i, int k, int j) {
        this.keyPressed(i, j);
    }
}
