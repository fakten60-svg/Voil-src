package lol.lwes.voil.gui.clickgui.page;

import dev.prestigeclient.renderlib.api.font.MSDFFontAtlas;
import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.api.ApiClient;
import lol.lwes.voil.config.ClientSettings;
import lol.lwes.voil.config.GuiTheme;
import lol.lwes.voil.config.ThemePreset;
import lol.lwes.voil.config.ThemeProperty;
import lol.lwes.voil.gui.clickgui.ClickGuiComponent;
import lol.lwes.voil.gui.clickgui.ClickGuiPanel;
import lol.lwes.voil.gui.clickgui.ClickGuiScreen;
import lol.lwes.voil.gui.clickgui.ClickRegion;
import lol.lwes.voil.gui.clickgui.dialog.ColorPickerDialog;
import lol.lwes.voil.gui.clickgui.theme.ColorBinding;
import lol.lwes.voil.gui.clickgui.theme.ThemeCard;
import lol.lwes.voil.util.input.KeyUtil;
import lol.lwes.voil.util.render.Render2DUtil;
import lol.lwes.voil.util.render.RenderManager;
import net.minecraft.client.util.math.MatrixStack;

public class GuiSettingsPage extends ClickGuiPanel {
    public final ClickGuiScreen gui;
    private final List themes = new ArrayList();
    private String currentTab = "Settings";
    private int scrollOffset = 0;
    private String themeFilter = "all";
    private boolean listeningForKey = false;
    private float scrollTarget;
    private float scrollCurrent;
    private float scrollMax;
    private ColorBinding selectedTheme;
    private Rectangle settingsTabBounds;
    private Rectangle themeTabBounds;
    private int mouseX = 0;
    private int mouseY = 0;
    private ClickRegion activeControl = null;
    private ColorPickerDialog colorPicker = null;
    private ThemeBrowserPage themeBrowser = null;
    private final List controls = new ArrayList();

    public GuiSettingsPage(ClickGuiScreen clickguiscreen) {
        super(clickguiscreen);
        this.gui = clickguiscreen;
        ClientSettings.themePresets.clear();
        ApiClient.INSTANCE
            .getSharedThemes()
            .thenAccept(list -> list.forEach(themepreset -> ClientSettings.themePresets.put(themepreset.getName(), themepreset)));
    }

    public void closeColorPicker() {
        this.colorPicker = null;
    }

    private void drawPillRight(String s, int i, int j, String s1) {
        float f2 = i + 150;
        float f = j;
        Render2DUtil.drawRoundedRect(f2, f, 100.0F, 15.0F, 6.0F, 1.0F, GuiTheme.modeSettingBg);
        f2 = i + 150;
        float f1 = j;
        Render2DUtil.drawOutline(f2, f1, 100.0F, 15.0F, 6.0F, 1.0F, 0.1F, GuiTheme.modeSettingFill);
        VoilClient.fonts.getSelectedFont().render(s, i + -101 + 1, j + 2, -1);
        VoilClient.fonts.getSelectedFont().render(s1, i + 150 + (100.0F - VoilClient.fonts.getSelectedFont().getWidth(s1)) / 2.0F, j, -1);
    }

    private void drawPillLeft(String s, int i, int j, String s1) {
        float f2 = i + 150;
        float f = j;
        Render2DUtil.drawRoundedRect(f2, f, 100.0F, 15.0F, 6.0F, 1.0F, GuiTheme.modeSettingBg);
        f2 = i + 150;
        float f1 = j;
        Render2DUtil.drawOutline(f2, f1, 100.0F, 15.0F, 6.0F, 1.0F, 0.1F, GuiTheme.modeSettingFill);
        VoilClient.fonts.getSelectedFont().render(s, i - 100, j + 2, -1);
        VoilClient.fonts.getSelectedFont().render(s1, i + 150 + (100.0F - VoilClient.fonts.getSelectedFont().getWidth(s1)) / 2.0F, j, -1);
    }

    private void drawTheme(int i, int j) {
        int k3 = i + 40;
        int j3 = this.mouseX;
        int i3 = k3;
        int k = i3 - j3;
        int l = i + 40;
        i3 = this.gui.windowHeight;
        i3 -= l;
        int i1 = i3 + 100;
        int j1 = l + i1;
        this.controls.clear();
        ArrayList<ThemePreset> arraylist = new ArrayList<>(ClientSettings.themePresets.values());
        arraylist.sort((themepreset, themepreset1) -> {
            if (themepreset.getName().equalsIgnoreCase("Default")) {
                return -1;
            } else {
                return themepreset1.getName().equalsIgnoreCase("Default") ? 1 : themepreset.getName().compareToIgnoreCase(themepreset1.getName());
            }
        });
        List<ThemePreset> list = arraylist.stream()
            .filter(themepreset -> this.themeFilter.equals("all") || themepreset.getCategory().equalsIgnoreCase(this.themeFilter))
            .toList();
        i3 = (int)Math.ceil((float)list.size() / 2);
        int k1 = i3 * 136;
        this.mouseY = Math.max(0, k1 - i1);
        int l1 = 0;

        for (ThemePreset themepreset : list) {
            int i2 = l1 / 2;
            int j2 = l1 % 2;
            k3 = j + -176 + 1;
            j3 = j2 * 180;
            i3 = k3;
            int k2 = i3 + j3;
            int l2 = k + i2 * 136;
            if (l2 + 126 < l) {
                l1++;
            } else {
                if (l2 > j1) {
                    break;
                }

                float f10 = k2;
                float f5 = l2;
                Render2DUtil.drawRoundedRect(f10, f5, 170, 126, 6.0F, 1.0F, themepreset.getBackgroundColor());
                Render2DUtil.drawRectGradient(
                    k2 + 10,
                    l2 + 40,
                    155.0F,
                    2.0F,
                    1.0F,
                    1.0F,
                    themepreset.getStartColor(),
                    themepreset.getStartColor(),
                    themepreset.getEndColor(),
                    themepreset.getEndColor()
                );
                Render2DUtil.drawOutlineGradient(
                    k2,
                    l2,
                    170,
                    126,
                    6.0F,
                    0.5F,
                    0.8F,
                    themepreset.getStartColor(),
                    themepreset.getEndColor(),
                    themepreset.getStartColor(),
                    themepreset.getEndColor()
                );
                float f11 = k2 + 10;
                float f6 = l2 + 50;
                Render2DUtil.drawRoundedRect(f11, f6, 55.0F, 15.0F, 3.0F, 1.0F, themepreset.getStartColor());
                float f12 = k2 + 75;
                float f7 = l2 + 50;
                Render2DUtil.drawRoundedRect(f12, f7, 55.0F, 15.0F, 3.0F, 1.0F, themepreset.getEndColor());
                float f13 = k2 + 10;
                float f8 = l2 + 70;
                Render2DUtil.drawRoundedRect(f13, f8, 155.0F, 20.0F, 2.0F, 1.0F, new Color(37, 42, 55, 128));
                float f14 = k2 + 15;
                i3 = l2 + 70;
                i3 += 10;
                Render2DUtil.drawCircle(f14, i3 + -7 + 1, 6.0F, 1.0F, themepreset.getAccentColor());
                float f15 = k2 + 35;
                float f9 = l2 + 78;
                Render2DUtil.drawRoundedRect(f15, f9, 50.0F, 3.0F, 1.0F, 1.0F, new Color(91, 95, 104, 255));
                VoilClient.fonts.getSelectedFont().render("Secondary", k2 + 80, l2 + 51, 9.0F, -1);
                VoilClient.fonts.getSelectedFont().render("Primary", k2 + 20, l2 + 51, 9.0F, -1);
                float f = VoilClient.fonts.getSelectedFont().getWidth("Select");
                float f1 = k2 + 10;
                float f2 = l2 + 95;
                Render2DUtil.drawRectGradient(
                    k2 + 10,
                    f2,
                    155.0F,
                    20.0F,
                    2.0F,
                    1.0F,
                    themepreset.getStartColor(),
                    themepreset.getStartColor(),
                    themepreset.getEndColor(),
                    themepreset.getEndColor()
                );
                float f3 = f1 + (155.0F - f) / 2.0F;
                float f4 = f2 + (20.0F - VoilClient.fonts.getSelectedFont().getLineHeight()) / 2.0F;
                VoilClient.fonts.getSelectedFont().render("Select", f3, f4, 11.0F, -1);
                VoilClient.fonts.getSelectedFont().render(themepreset.getName(), k2 + 10, l2 + 3, -1);
                String s = themepreset.getDescription();

                for (byte b0 = 0; b0 < s.length(); b0 += 26) {
                    String s1 = s.substring(b0, Math.min(b0 + 26, s.length()));
                    MSDFFontAtlas msdffontatlas = VoilClient.fonts.getSelectedFont();
                    float f16 = k2 + 10;
                    int l3 = l2 + 15;
                    i3 = b0 / 26;
                    j3 = i3 * 10;
                    i3 = l3;
                    msdffontatlas.render(s1, f16, i3 + j3, 10.0F, Color.gray.getRGB());
                }

                Rectangle rectangle = new Rectangle((int)f1, (int)f2, 155, 20);
                this.themes.add(new ClickRegion(rectangle, () -> {
                    ApiClient apiclient = ApiClient.INSTANCE;
                    String sx = themepreset.getAuthor();
                    apiclient.getSharedThemeData(sx).thenAccept(ApiClient.INSTANCE::saveTheme);
                    ClientSettings.activeTheme.setValue(themepreset.getAuthor());
                }));
                this.controls.add(new ThemeCard(themepreset, k2, l2, 170, 126));
                l1++;
            }
        }
    }

    private void selectTheme(ColorBinding colorbinding) {
        this.selectedTheme = colorbinding;
        Color color = (Color)colorbinding.getter.get();
        float[] afloat = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        this.scrollTarget = afloat[0];
        this.scrollCurrent = afloat[1];
        this.scrollMax = afloat[2];
        this.listeningForKey = true;
        int i = this.gui.windowX + 340;
        int k = this.gui.windowY;
        int j = k + 80;
        this.settingsTabBounds = new Rectangle(i, j, 100, 100);
        k = i + 100;
        this.themeTabBounds = new Rectangle(k + 10, j, 10, 100);
    }

    private void reloadThemes() {
        if (this.selectedTheme != null) {
            Color color = Color.getHSBColor(this.scrollTarget, this.scrollCurrent, this.scrollMax);
            this.selectedTheme.setter.accept(color);
        }

        this.listeningForKey = false;
    }

    public void mouseClicked(double d0, double d1, int i) {
        if (this.themeBrowser != null) {
            this.themeBrowser.mouseClicked2(d0, d1, i);
        } else if (this.colorPicker != null) {
            ColorPickerDialog colorpickerdialog = this.colorPicker;
            colorpickerdialog.mouseClicked(d0, d1, i);
        } else {
            if (this.listeningForKey) {
                if (this.settingsTabBounds.contains(d0, d1)) {
                    this.updateSliderDrag(d0, d1);
                    this.reloadThemes();
                    return;
                }

                if (this.themeTabBounds.contains(d0, d1)) {
                    this.scrollBy(d1);
                    this.reloadThemes();
                    return;
                }
            }

            int k = this.themes.size();

            for (int j = k - 1; j >= 0; j--) {
                ClickRegion clickregion = (ClickRegion)this.themes.get(j);
                if (clickregion.bounds.contains(d0, d1)) {
                    if (clickregion.type != null) {
                        this.activeControl = clickregion;
                        float f = (float)((d0 - clickregion.bounds.x) / clickregion.bounds.width);
                        float f1 = clickregion.minValue + f * (clickregion.maxValue - clickregion.minValue);
                        f1 = Math.round(f1 / clickregion.step) * clickregion.step;
                        clickregion.type.setValue(Math.max(clickregion.minValue, Math.min(clickregion.maxValue, f1)));
                    } else {
                        clickregion.action.run();
                    }
                    break;
                }
            }
        }
    }

    public void render(MatrixStack matrixstack, int i, int j, float f) {
        if (this.themeBrowser != null) {
            this.themeBrowser.render2(matrixstack, i, j, f);
        } else if (this.colorPicker != null) {
            ColorPickerDialog colorpickerdialog = this.colorPicker;
            colorpickerdialog.render(i, j, f);
        } else {
            this.themes.clear();
            int k = this.gui.windowX;
            int l = this.gui.windowY;
            int i1 = (int)VoilClient.fonts.getSelectedFont().getWidth("Customize the client to your liking!");
            int i5 = k + 180;
            int l1 = (340 - i1) / 2;
            int k1 = i5;
            int j1 = k1 + l1;
            VoilClient.fonts.getSelectedFont().render("Customize the client to your liking!", j1, l + 3, -1);
            float f6 = k + 138;
            float f10 = l + 24;
            l1 = this.gui.sidebarWidth;
            k1 = this.gui.windowWidth;
            float f5 = k1 - l1;
            Render2DUtil.drawFadeLineHorizontal(f6, f10, f5, 0.8F, GuiTheme.accentColor1);
            float f7 = j1;
            f10 = l + 20;
            float f11 = j1 + VoilClient.fonts.getSelectedFont().getWidth("Settings");
            k1 = l + 20;
            float f12 = k1 + 15;
            double d3 = i;
            double d0 = j;
            if (ClickGuiComponent.isPointInRect(f7, f10, f11, f12, d3, d0)) {
                this.themes
                    .add(
                        new ClickRegion(
                            new Rectangle(j1, l + 24, (int)VoilClient.fonts.getSelectedFont().getWidth("Settings"), 15), () -> this.currentTab = "Settings"
                        )
                    );
            }

            float f8 = j1 + 100;
            f10 = l + 20;
            f11 = j1 + 100 + VoilClient.fonts.getSelectedFont().getWidth("Theme");
            k1 = l + 20;
            f12 = k1 + 15;
            d3 = i;
            double d1 = j;
            if (ClickGuiComponent.isPointInRect(f8, f10, f11, f12, d3, d1)) {
                this.themes
                    .add(
                        new ClickRegion(
                            new Rectangle(j1 + 100, l + 24, (int)VoilClient.fonts.getSelectedFont().getWidth("Theme"), 15),
                            () -> this.themeBrowser = new ThemeBrowserPage(this.gui, () -> this.themeBrowser = null)
                        )
                    );
            }

            float f9 = j1;
            f10 = l + 20;
            f11 = j1 + VoilClient.fonts.getSelectedFont().getWidth("Settings");
            k1 = l + 20;
            f12 = k1 + 15;
            d3 = i;
            double d2 = j;
            if (ClickGuiComponent.isPointInRect(f9, f10, f11, f12, d3, d2)) {
                this.themes.add(new ClickRegion(new Rectangle(j1, l + 24, (int)VoilClient.fonts.getSelectedFont().getWidth("Settings"), 15), () -> {
                    this.currentTab = "Settings";
                    this.mouseX = 0;
                }));
            }

            f10 = j1;
            f11 = l + 28;
            boolean flag = this.currentTab.equals("Settings");
            Render2DUtil.drawText("Settings", f10, f11, flag);
            f10 = j1 + 114;
            f11 = l + 28;
            boolean flag1 = this.currentTab.equals("Theme");
            Render2DUtil.drawText("Theme", f10, f11, flag1);
            if (this.currentTab.equals("Settings")) {
                i5 = k - 1;
                int j4 = l + 50;
                l1 = this.gui.windowWidth;
                k1 = k;
                k1 += l1;
                int k2 = k1 + 1;
                l1 = this.gui.windowHeight;
                k1 = l;
                k1 += l1;
                RenderManager.setScissor(i5, j4, k2, k1 + -7 + 1);
                this.drawSettings(matrixstack);
                RenderManager.disableScissor();
            } else {
                int l4 = j1 + 115;
                int l2 = l + 50;
                int i3 = l4;
                this.drawThemeFilter(i3, l2);
                i5 = k - 1;
                int k4 = l + 76;
                k1 = k + this.gui.windowWidth;
                int j3 = k1 + 1;
                k1 = l + this.gui.windowHeight;
                RenderManager.setScissor(i5, k4, j3, k1 + -7 + 1);
                int k3 = l + 40;
                int l3 = k + 325;
                int i4 = k3;
                this.drawTheme(i4, l3);
                RenderManager.disableScissor();
            }

            if (this.activeControl != null && this.activeControl.type != null) {
                ClickRegion clickregion = this.activeControl;
                l1 = clickregion.bounds.x;
                k1 = i;
                float f1 = (float)(k1 - l1) / clickregion.bounds.width;
                float f2 = clickregion.minValue + f1 * (clickregion.maxValue - clickregion.minValue);
                f2 = Math.round(f2 / clickregion.step) * clickregion.step;
                f2 = Math.max(clickregion.minValue, Math.min(clickregion.maxValue, f2));
                clickregion.type.setValue(f2);
            }
        }
    }

    public void mouseDragged(double d0, double d1, int i, double d2, double d3) {
        if (this.themeBrowser != null) {
            this.themeBrowser.mouseDragged2(d0, d1, i, d2, d3);
        } else if (this.colorPicker != null) {
            ColorPickerDialog colorpickerdialog = this.colorPicker;
            colorpickerdialog.mouseDragged(d0, d1, i, d2, d3);
        } else if (this.listeningForKey) {
            if (this.settingsTabBounds.contains(d0, d1)) {
                this.updateSliderDrag(d0, d1);
            } else if (this.themeTabBounds.contains(d0, d1)) {
                this.scrollBy(d1);
            }
        } else {
            if (this.activeControl != null && this.activeControl.type != null) {
                int k = this.activeControl.bounds.height;
                int l = this.activeControl.bounds.y;
                if (!this.isInContentArea(l, k)) {
                    this.activeControl = null;
                    return;
                }

                ClickRegion clickregion = this.activeControl;
                float f = (float)(d0 - clickregion.bounds.x) / clickregion.bounds.width;
                float f1 = clickregion.minValue + f * (clickregion.maxValue - clickregion.minValue);
                f1 = Math.round(f1 / clickregion.step) * clickregion.step;
                f1 = Math.max(clickregion.minValue, Math.min(clickregion.maxValue, f1));
                clickregion.type.setValue(f1);
            }
        }
    }

    public void mouseScrolled(double d0, double d1, double d2, double d3) {
        if (this.themeBrowser != null) {
            this.themeBrowser.mouseScrolled2(d0, d1, d2, d3);
        } else if (this.colorPicker != null) {
            ColorPickerDialog colorpickerdialog = this.colorPicker;
            colorpickerdialog.mouseScrolled(d0, d1, d2, d3);
        } else {
            int j = (int)(d3 * 20.0);
            int i = this.mouseX;
            this.mouseX = i - j;
            if (this.mouseX < 0) {
                this.mouseX = 0;
            }

            if (this.mouseX > this.mouseY) {
                this.mouseX = this.mouseY;
            }

            super.mouseScrolled();
        }
    }

    @Override
    public void charTyped(char chr, int modifiers) {
        if (this.themeBrowser != null) {
            this.themeBrowser.charTyped(chr, modifiers);
        } else {
            super.charTyped(chr, modifiers);
        }
    }

    public void mouseReleased(double d0, double d1, int i) {
        if (this.themeBrowser != null) {
            this.themeBrowser.mouseReleased2(d0, d1, i);
        } else if (this.colorPicker != null) {
            ColorPickerDialog colorpickerdialog = this.colorPicker;
            colorpickerdialog.mouseReleased(d0, d1, i);
        } else {
            if (this.listeningForKey) {
                this.selectedTheme.setter.accept(Color.getHSBColor(this.scrollTarget, this.scrollCurrent, this.scrollMax));
                this.listeningForKey = false;
            }

            this.activeControl = null;
        }
    }

    public void keyPressed(int i, int j, int k) {
        if (this.themeBrowser != null) {
            this.themeBrowser.keyReleased2(i, j, k);
        } else if (this.colorPicker != null) {
            ColorPickerDialog colorpickerdialog = this.colorPicker;
            colorpickerdialog.keyPressed(i, j, k);
        } else {
            if (ClientSettings.menuKeybind.isModified()) {
                if (i == 256) {
                    ClientSettings.menuKeybind.setValue(0);
                } else {
                    ClientSettings.menuKeybind.setValue(i);
                }

                ClientSettings.menuKeybind.setModified(false);
            }
        }
    }

    private void drawThemeFilter(int i, int j) {
        String[] astring = new String[]{"All", "Light", "Dark"};

        for (int k = 0; k < astring.length; k++) {
            int j1 = k * 43;
            int i1 = i;
            int l = i1 + j1;
            String s = astring[k];
            Color color = this.themeFilter.equalsIgnoreCase(s.toLowerCase()) ? GuiTheme.accentColor1 : new Color(50, 50, 50);
            float f1 = l;
            float f = j;
            Render2DUtil.drawRoundedRect(f1, f, 35, 15, 4.0F, 1.0F, color);
            VoilClient.fonts.getSelectedFont().render(s, l + (35 - VoilClient.fonts.getSelectedFont().getWidth(s)) / 2.0F, j + 1, -1);
            this.themes.add(new ClickRegion(new Rectangle(l, j, 35, 15), () -> {
                this.themeFilter = s.toLowerCase();
                this.mouseX = 0;
            }));
        }
    }

    private void drawSlider(String s, int i, int j, ThemeProperty themeproperty, float f, float f1, float f2) {
        float f3 = ((Number)themeproperty.getValue()).floatValue();
        float f4 = (f3 - f) / (f1 - f);
        float f10 = i + 150;
        float f8 = j;
        Render2DUtil.drawRoundedRect(f10, f8, 115.0F, 5.0F, 5.0F / 2.0F, 1.0F, GuiTheme.modeSettingBg);
        VoilClient.fonts.getSelectedFont().render(s, i + -101 + 1, j + 2, -1);
        float f5 = f4 * 115.0F;
        f10 = i + 150;
        float f9 = j;
        Render2DUtil.drawRoundedRect(f10, f9, f5, 5.0F, 5.0F / 2.0F, 1.0F, GuiTheme.accentColor1);
        float f6 = i + 150 + f5 - 5.0F;
        float f7 = j - 0.5F;
        Render2DUtil.drawCircle(f6, f7, 3.0F, 1.0F, Color.white);
        String s1 = String.format("%.0f", f3);
        VoilClient.fonts.getSelectedFont().render(s1, i + 195 + (115.0F - VoilClient.fonts.getSelectedFont().getWidth(s1)) / 2.0F, j + -15 + 1, -1);
        if (this.isInContentArea(j, 5)) {
            this.themes.add(new ClickRegion(new Rectangle(i + 150, j, 115, 5), themeproperty, f, f1, f2));
        }
    }

    private void drawSettings(MatrixStack matrixstack) {
        int i = this.gui.windowX - 15;
        int j2 = this.gui.windowY;
        int j = j2 + 15;
        int k = i + 255;
        int l = j + 40 - this.mouseX;
        ThemeProperty themeproperty = ClientSettings.menuKeybind;
        int l2 = l;
        int i3 = k;
        String s4 = "Keybind";
        this.drawKeybind(s4, i3, l2, themeproperty);
        if (this.isInContentArea(l, 15)) {
            this.themes.add(new ClickRegion(new Rectangle(k + 150, l, 100, 15), () -> ClientSettings.menuKeybind.setModified(true)));
        }

        l += 20;
        boolean flag = (Boolean)ClientSettings.searchBarEnabled.getValue();
        int j3 = l;
        int k3 = k;
        String s5 = "Search Bar";
        this.drawToggle(s5, k3, j3, flag);
        if (this.isInContentArea(l, 15)) {
            this.themes
                .add(
                    new ClickRegion(
                        new Rectangle(k + 150, l, 15, 15), () -> ClientSettings.searchBarEnabled.setValue(!(Boolean)ClientSettings.searchBarEnabled.getValue())
                    )
                );
        }

        l += 20;
        boolean flag1 = (Boolean)ClientSettings.glowEnabled.getValue();
        int l3 = l;
        int i4 = k;
        String s6 = "Glow";
        this.drawToggle(s6, i4, l3, flag1);
        if (this.isInContentArea(l, 15)) {
            this.themes
                .add(
                    new ClickRegion(
                        new Rectangle(k + 150, l, 15, 15), () -> ClientSettings.glowEnabled.setValue(!(Boolean)ClientSettings.glowEnabled.getValue())
                    )
                );
        }

        l += 20;
        String[] astring = new String[]{"Verdana", "Inter", "Poppins", "InterBold", "Quicksand", "SfRegular"};
        String s = (String)ClientSettings.fontName.getValue();
        int i1 = IntStream.range(0, astring.length).filter(idx -> astring[idx].equalsIgnoreCase(s)).findFirst().orElse(0);
        String s7 = astring[i1];
        int j4 = l;
        int k4 = k;
        String s8 = "Font";
        this.drawPillRight(s8, k4, j4, s7);
        if (this.isInContentArea(l, 15)) {
            this.themes.add(new ClickRegion(new Rectangle(k + 150, l, 100, 15), () -> ClientSettings.fontName.setValue(astring[(i1 + 1) % astring.length])));
        }

        l += 20;
        boolean flag2 = (Boolean)ClientSettings.customBackground.getValue();
        int l4 = l;
        int i5 = k;
        String s9 = "Custom Background";
        this.drawToggle(s9, i5, l4, flag2);
        if (this.isInContentArea(l, 15)) {
            this.themes
                .add(
                    new ClickRegion(
                        new Rectangle(k + 150, l, 15, 15), () -> ClientSettings.customBackground.setValue(!(Boolean)ClientSettings.customBackground.getValue())
                    )
                );
        }

        l += 20;
        String[] astring1 = new String[]{"Blur", "Gradient"};
        String s1 = (String)ClientSettings.backgroundType.getValue();
        int j1 = IntStream.range(0, astring1.length).filter(idx -> astring1[idx].equalsIgnoreCase(s1)).findFirst().orElse(0);
        String s10 = astring1[j1];
        int j5 = l;
        int k5 = k;
        String s11 = "Background Type";
        this.drawPillRight(s11, k5, j5, s10);
        if (this.isInContentArea(l, 15)) {
            this.themes
                .add(new ClickRegion(new Rectangle(k + 150, l, 100, 15), () -> ClientSettings.backgroundType.setValue(astring1[(j1 + 1) % astring1.length])));
        }

        l += 20;
        boolean flag3 = (Boolean)ClientSettings.syncVisuals.getValue();
        int l5 = l;
        int i6 = k;
        String s12 = "Sync Visuals";
        this.drawToggle(s12, i6, l5, flag3);
        if (this.isInContentArea(l, 15)) {
            this.themes
                .add(
                    new ClickRegion(
                        new Rectangle(k + 150, l, 15, 15), () -> ClientSettings.syncVisuals.setValue(!(Boolean)ClientSettings.syncVisuals.getValue())
                    )
                );
        }

        l += 20;
        boolean flag4 = (Boolean)ClientSettings.customMainMenu.getValue();
        int j6 = l;
        int k6 = k;
        String s13 = "Custom Main Menu";
        this.drawToggle(s13, k6, j6, flag4);
        if (this.isInContentArea(l, 15)) {
            this.themes
                .add(
                    new ClickRegion(
                        new Rectangle(k + 150, l, 15, 15), () -> ClientSettings.customMainMenu.setValue(!(Boolean)ClientSettings.customMainMenu.getValue())
                    )
                );
        }

        l += 20;
        boolean flag5 = (Boolean)ClientSettings.searchSettings.getValue();
        int l6 = l;
        int i7 = k;
        String s14 = "Search Settings";
        this.drawToggle(s14, i7, l6, flag5);
        if (this.isInContentArea(l, 15)) {
            this.themes
                .add(
                    new ClickRegion(
                        new Rectangle(k + 150, l, 15, 15), () -> ClientSettings.searchSettings.setValue(!(Boolean)ClientSettings.searchSettings.getValue())
                    )
                );
        }

        l += 20;
        ThemeProperty themeproperty1 = ClientSettings.openSpeed;
        int j7 = l;
        int k7 = k;
        String s15 = "Open Speed";
        this.drawSlider(s15, k7, j7, themeproperty1, 50.0F, 800.0F, 1.0F);
        l += 20;
        ThemeProperty themeproperty2 = ClientSettings.closeSpeed;
        int l7 = l;
        int i8 = k;
        String s16 = "Close Speed";
        this.drawSlider(s16, i8, l7, themeproperty2, 50.0F, 800.0F, 1.0F);
        l += 20;
        ThemeProperty themeproperty3 = ClientSettings.guiScale;
        int j8 = l;
        int k8 = k;
        String s17 = "GUI Scale";
        this.drawSlider(s17, k8, j8, themeproperty3, 50.0F, 200.0F, 5.0F);
        l += 20;
        ThemeProperty themeproperty4 = ClientSettings.hudScale;
        int l8 = l;
        int i9 = k;
        String s18 = "HUD Scale";
        this.drawSlider(s18, i9, l8, themeproperty4, 50.0F, 200.0F, 5.0F);
        l += 20;
        boolean flag6 = (Boolean)ClientSettings.followMcScale.getValue();
        int j9 = l;
        int k9 = k;
        String s19 = "Follow MC Scale";
        this.drawToggle(s19, k9, j9, flag6);
        if (this.isInContentArea(l, 15)) {
            this.themes
                .add(
                    new ClickRegion(
                        new Rectangle(k + 150, l, 15, 15), () -> ClientSettings.followMcScale.setValue(!(Boolean)ClientSettings.followMcScale.getValue())
                    )
                );
        }

        l += 20;
        String[] astring2 = new String[]{
            "Overshoot",
            "Ease Out Smooth",
            "Linear",
            "Ease In Sine",
            "Ease Out Sine",
            "Ease In Out Sine",
            "Ease In Quad",
            "Ease Out Quad",
            "Ease In Out Quad",
            "Ease In Cubic",
            "Ease Out Cubic",
            "Ease In Out Cubic"
        };
        String s2 = (String)ClientSettings.easingType.getValue();
        int k1 = IntStream.range(0, astring2.length).filter(idx -> astring2[idx].equalsIgnoreCase(s2)).findFirst().orElse(0);
        String s20 = astring2[k1];
        int l9 = l;
        int i10 = k;
        String s21 = "Easing Type";
        this.drawPillRight(s21, i10, l9, s20);
        if (this.isInContentArea(l, 15)) {
            this.themes
                .add(new ClickRegion(new Rectangle(k + 150, l, 100, 15), () -> ClientSettings.easingType.setValue(astring2[(k1 + 1) % astring2.length])));
        }

        l += 20;
        String[] astring3 = new String[]{"Gradient", "Balls"};
        String s3 = (String)ClientSettings.mainMenuBackground.getValue();
        int l1 = IntStream.range(0, astring3.length).filter(idx -> astring3[idx].equalsIgnoreCase(s3)).findFirst().orElse(0);
        String s22 = astring3[l1];
        int j10 = l;
        int k10 = k;
        String s23 = "Main Menu Background";
        this.drawPillRight(s23, k10, j10, s22);
        if (this.isInContentArea(l, 15)) {
            this.themes
                .add(
                    new ClickRegion(new Rectangle(k + 150, l, 100, 15), () -> ClientSettings.mainMenuBackground.setValue(astring3[(l1 + 1) % astring3.length]))
                );
        }

        int k2 = this.mouseX;
        j2 = l;
        j2 += k2;
        int i2 = j2 + 15;
        j2 = this.gui.windowY + this.gui.windowHeight;
        k2 = j2 + -7 + 1;
        j2 = i2;
        j2 -= k2;
        this.mouseY = Math.max(0, j2 + 6);
        if (this.mouseX > this.mouseY) {
            this.mouseX = this.mouseY;
        }
    }

    private void scrollBy(double d0) {
        float f = (float)((d0 - this.themeTabBounds.y) / this.themeTabBounds.height);
        this.scrollTarget = this.lerp(f, 0.0F, 1.0F);
    }

    private float lerp(float f, float f1, float f2) {
        return f < f1 ? f1 : (f > f2 ? f2 : f);
    }

    private boolean isInContentArea(int i, int j) {
        int i1 = this.gui.windowY;
        int k = i1 + 30;
        int j1 = this.gui.windowHeight;
        i1 = this.gui.windowY;
        i1 += j1;
        int l = i1 + -7 + 1;
        return i >= k && i + j <= l;
    }

    private void drawToggle(String s, int i, int j, boolean flag) {
        float f2 = i + 150;
        float f = j;
        Render2DUtil.drawRoundedRect(f2, f, 15.0F, 15.0F, 7.5F, 1.0F, GuiTheme.settingsBg);
        f2 = i + 150;
        float f1 = j;
        Render2DUtil.drawOutline(f2, f1, 15.0F, 15.0F, 7.5F, 1.0F, 0.1F, GuiTheme.settingsBg.brighter().brighter());
        VoilClient.fonts.getSelectedFont().render(s, i + -101 + 1, j, -1);
        if (flag) {
            Render2DUtil.drawCircle(i + 150 + 3.0F, j + 3.0F, 4.5F, 1.0F, GuiTheme.accentColor2);
        }
    }

    private void updateSliderDrag(double d0, double d1) {
        float f = (float)((d0 - this.settingsTabBounds.x) / this.settingsTabBounds.width);
        this.scrollCurrent = this.lerp(f, 0.0F, 1.0F);
        float f1 = 1.0F - (float)((d1 - this.settingsTabBounds.y) / this.settingsTabBounds.height);
        this.scrollMax = this.lerp(f1, 0.0F, 1.0F);
    }

    private void drawKeybind(String s, int i, int j, ThemeProperty themeproperty) {
        float f2 = i + 150;
        float f = j;
        Render2DUtil.drawRoundedRect(f2, f, 100.0F, 15.0F, 6.0F, 1.0F, GuiTheme.settingsBg);
        f2 = i + 150;
        float f1 = j;
        Render2DUtil.drawOutline(f2, f1, 100.0F, 15.0F, 6.0F, 1.0F, 0.1F, GuiTheme.settingsBg.brighter().brighter());
        VoilClient.fonts.getSelectedFont().render(s, i + -101 + 1, j, -1);
        String s4;
        if (themeproperty.isModified()) {
            s4 = "Press any key...";
        } else {
            int k = (Integer)themeproperty.getValue();
            String s3 = KeyUtil.getKeyName(k);
            s4 = "Key: " + s3;
        }

        String s1 = s4;
        VoilClient.fonts.getSelectedFont().render(s1, i + 150 + (100.0F - VoilClient.fonts.getSelectedFont().getWidth(s1)) / 2.0F, j, -1);
    }

    @Override
    public void mouseClicked2(double d0, double d1, int i) {
        this.mouseClicked(d0, d1, i);
    }

    @Override
    public void render2(MatrixStack matrixstack, int i, int j, float f) {
        this.render(matrixstack, i, j, f);
    }

    @Override
    public void mouseDragged2(double d0, double d1, int i, double d2, double d3) {
        this.mouseDragged(d0, d1, i, d2, d3);
    }

    @Override
    public void mouseScrolled2(double d0, double d1, double d2, double d3) {
        this.mouseScrolled(d0, d1, d2, d3);
    }

    @Override
    public void mouseReleased2(double d0, double d1, int i) {
        this.mouseReleased(d0, d1, i);
    }

    @Override
    public void keyReleased2(int i, int j, int k) {
        this.keyPressed(i, j, k);
    }
}
