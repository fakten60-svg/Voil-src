package lol.lwes.voil.gui.clickgui;

import dev.prestigeclient.renderlib.api.font.MSDFFontAtlas;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.config.GuiTheme;
import lol.lwes.voil.gui.clickgui.component.BooleanSettingComponent;
import lol.lwes.voil.gui.clickgui.component.ButtonSettingComponent;
import lol.lwes.voil.gui.clickgui.component.ColorSettingComponent;
import lol.lwes.voil.gui.clickgui.component.CurveSettingComponent;
import lol.lwes.voil.gui.clickgui.component.GradientSettingComponent;
import lol.lwes.voil.gui.clickgui.component.ItemListSettingComponent;
import lol.lwes.voil.gui.clickgui.component.ItemSlotsSettingComponent;
import lol.lwes.voil.gui.clickgui.component.KeybindSettingComponent;
import lol.lwes.voil.gui.clickgui.component.ListSettingComponent;
import lol.lwes.voil.gui.clickgui.component.ModeSettingComponent;
import lol.lwes.voil.gui.clickgui.component.MultiSelectSettingComponent;
import lol.lwes.voil.gui.clickgui.component.NumberSettingComponent;
import lol.lwes.voil.gui.clickgui.component.RangeSettingComponent;
import lol.lwes.voil.gui.clickgui.component.RegistrySettingComponent;
import lol.lwes.voil.gui.clickgui.component.StringSettingComponent;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.module.ModuleManager;
import lol.lwes.voil.module.ModuleTag;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.ButtonSetting;
import lol.lwes.voil.setting.ColorSetting;
import lol.lwes.voil.setting.CurveSetting;
import lol.lwes.voil.setting.GradientSetting;
import lol.lwes.voil.setting.ItemListSetting;
import lol.lwes.voil.setting.ItemSlotsSetting;
import lol.lwes.voil.setting.KeybindSetting;
import lol.lwes.voil.setting.ListSetting;
import lol.lwes.voil.setting.ModeSetting;
import lol.lwes.voil.setting.MultiSelectSetting;
import lol.lwes.voil.setting.NumberSetting;
import lol.lwes.voil.setting.RangeSetting;
import lol.lwes.voil.setting.RegistrySetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.setting.StringSetting;
import lol.lwes.voil.util.font.IconGlyphs;
import lol.lwes.voil.util.input.KeyUtil;
import lol.lwes.voil.util.render.ColorUtil;
import lol.lwes.voil.util.render.EasingType;
import lol.lwes.voil.util.render.ModuleGlowRenderer;
import lol.lwes.voil.util.render.RectBounds;
import lol.lwes.voil.util.render.Render2DUtil;
import lol.lwes.voil.util.render.RenderManager;
import lol.lwes.voil.util.render.UiAnimation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;

public class ModuleButton {
    private final Module module;
    private final ClickGuiScreen gui;
    private final MinecraftClient mc = MinecraftClient.getInstance();
    private List<ClickGuiComponent> settingComponents = new ArrayList<>();
    public double x;
    public double y;
    private static final Map<Module, List<ClickGuiComponent>> COMPONENT_CACHE = new HashMap<>();
    private static final Map<Module, UiAnimation> HOVER_ANIMATIONS = new HashMap<>();
    private static final Map<Module, UiAnimation> ENABLED_ANIMATIONS = new HashMap<>();
    private static final Map<Module, UiAnimation> EXPAND_ANIMATIONS = new HashMap<>();
    private static final Map<Module, UiAnimation> BIND_ANIMATIONS = new HashMap<>();
    private static final Map<Module, UiAnimation> HEIGHT_ANIMATIONS = new HashMap<>();
    private final UiAnimation hoverAnimation;
    private final UiAnimation enabledAnimation;
    private final UiAnimation expandAnimation;
    private final UiAnimation bindAnimation;
    private final UiAnimation heightAnimation;
    private double animatedY;
    private double animatedX;
    private final Set<Setting> expandedSettings = new HashSet<>();
    private float bindX;
    private float bindY;
    private float bindWidth;
    private float bindHeight;
    private boolean bindHovered;
    private float lastMouseX;
    private float lastMouseY;
    private float bindTextWidth;
    private float bindAlpha;
    private float arrowRotation;
    private float scrollOffset;
    private boolean noSettings;

    public ModuleButton(Module module, double d0, double d1, ClickGuiScreen clickguiscreen) {
        this.module = module;
        this.y = d1;
        this.x = d0;
        this.gui = clickguiscreen;
        this.animatedY = d1;
        this.animatedX = 0.0;
        this.loadSettingComponents();
        this.hoverAnimation = HOVER_ANIMATIONS.computeIfAbsent(module, module1 -> new UiAnimation(EasingType.FLUID_RESPONSIVE).setFluidParams(0.5, 0.18));
        this.enabledAnimation = ENABLED_ANIMATIONS.computeIfAbsent(module, module2 -> {
            UiAnimation uianimation = new UiAnimation(EasingType.FLUID_SMOOTH).setFluidParams(0.6, 0.14);
            uianimation.snapTo(module.isEnabled() ? 1.0 : 0.0);
            return uianimation;
        });
        this.expandAnimation = EXPAND_ANIMATIONS.computeIfAbsent(module, module2 -> {
            UiAnimation uianimation = new UiAnimation(EasingType.FLUID_SMOOTH).setFluidParams(0.55, 0.15);
            uianimation.snapTo(module.isExpanded() ? 1.0 : 0.0);
            return uianimation;
        });
        this.bindAnimation = BIND_ANIMATIONS.computeIfAbsent(module, module2 -> {
            UiAnimation uianimation = new UiAnimation(EasingType.FLUID_SMOOTH).setFluidParams(0.5, 0.18);
            uianimation.snapTo(module.getKeyCode() != 0 ? 1.0 : 0.0);
            return uianimation;
        });
        this.heightAnimation = HEIGHT_ANIMATIONS.computeIfAbsent(module, module1 -> {
            UiAnimation uianimation = new UiAnimation(EasingType.FLUID_SMOOTH).setFluidParams(0.62, 0.15);
            uianimation.snapTo(d1);
            return uianimation;
        });
    }

    public boolean handleClick(double d0, double d1, int i) {
        if (this.module.isBinding()) {
            this.module.setKeyCode(0);
            this.module.setBinding(false);
            return true;
        }

        float f3 = this.gui.windowX + (float)this.x + 100.0F + this.gui.cardWidth;
        float f4 = this.gui.windowY + (float)this.y - 10.0F;
        float f5 = this.gui.windowX + (float)this.x + 425.0F + this.gui.cardWidth + 40.0F;
        float f = this.gui.windowY + (float)this.y + 24.0F;
        boolean flag = this.isInBounds(f3, f4, f5, f, d0, d1);
        if (i == 0) {
            f5 = this.lastMouseX + 18.0F;
            float f1 = this.lastMouseY + 18.0F;
            if (this.isInBounds(this.lastMouseX, this.lastMouseY, f5, f1, d0, d1)) {
                Module modulex = this.module;
                ModuleManager modulemanager3 = ModuleManager.INSTANCE;
                modulemanager3.toggle(modulex);
                return true;
            }
        }

        if (this.bindHovered && i == 0) {
            f5 = this.bindX + this.bindWidth;
            float f2 = this.bindY + this.bindHeight;
            if (this.isInBounds(this.bindX, this.bindY, f5, f2, d0, d1)) {
                this.module.setBinding(true);
                return true;
            }
        }

        if (flag) {
            if (i == 0) {
                ModuleManager.INSTANCE.toggle(this.module);
            } else if (i == 1) {
                if (this.gui.selectedModule != null && this.gui.selectedModule != this.module) {
                    this.gui.selectedModule = null;
                    Category category = this.gui.selectedCategory;
                    ModuleManager modulemanager = ModuleManager.INSTANCE;
                    modulemanager.getModulesByCategory(category).forEach(module1 -> module1.setExpanded(false));
                }

                if (this.gui.selectedModule != this.module && !this.module.getSettings().isEmpty()) {
                    this.gui.categorySlideOffset = 0;
                    this.gui.selectedModule = this.module;
                    Category category2 = this.gui.selectedCategory;
                    ModuleManager modulemanager2 = ModuleManager.INSTANCE;
                    modulemanager2.getModulesByCategory(category2).forEach(module1 -> {
                        if (module1 != this.module) {
                            module1.setExpanded(false);
                        }
                    });
                    this.module.setExpanded(true);
                } else {
                    this.gui.selectedModule = null;
                    Category category1 = this.gui.selectedCategory;
                    ModuleManager modulemanager1 = ModuleManager.INSTANCE;
                    modulemanager1.getModulesByCategory(category1).forEach(module1 -> module1.setExpanded(false));
                }
            }
        }

        if (this.module.isExpanded()) {
            this.settingComponents.forEach(clickguicomponent -> clickguicomponent.mouseClicked2(d0, d1, i));
        }

        return true;
    }

    public void keyPressed(int i, int j, int k) {
        if (this.module.isBinding()) {
            this.module.setKeyCode(i == 256 ? 0 : i);
            this.module.setBinding(false);
        } else {
            this.settingComponents.forEach(clickguicomponent -> clickguicomponent.keyReleased2(i, j, k));
        }
    }

    public float getContentHeight() {
        float f = 54.0F;

        for (ClickGuiComponent clickguicomponent : this.settingComponents) {
            Setting setting = clickguicomponent.getSetting();
            if (setting == null || setting.isVisible()) {
                RectBounds rectbounds = clickguicomponent.getBounds();
                float f1 = 25.0F;
                if (rectbounds != null) {
                    f1 = Math.max(f1, rectbounds.height);
                }

                f += f1;
            }
        }

        return f;
    }

    public boolean isExpanded(Setting setting) {
        return this.expandedSettings.contains(setting);
    }

    public int getRowCount() {
        if (!this.module.isExpanded()) {
            return 34;
        }

        int i = 0;
        Iterator<ClickGuiComponent> iterator = this.settingComponents.iterator();

        while (iterator.hasNext()) {
            Setting setting = iterator.next().getSetting();
            if (setting == null || setting.isVisible()) {
                i++;
            }
        }

        Math.ceil(i / 6.0F);
        return 1000;
    }

    public void setX(double d0) {
        this.x = d0;
    }

    public void mouseReleased(double d0, double d1, int i) {
        this.settingComponents.forEach(clickguicomponent -> clickguicomponent.mouseReleased2(d0, d1, i));
    }

    private void loadSettingComponents() {
        if (COMPONENT_CACHE.containsKey(this.module)) {
            this.settingComponents = COMPONENT_CACHE.get(this.module);
        } else {
            float f = (float)(this.gui.windowX + this.x + 100.0 + this.gui.cardWidth) + 10.0F - this.gui.windowX - 445.0F;
            float f1 = (float)(this.gui.windowY + this.y + 29.0);
            int i = 0;
            ArrayList<ClickGuiComponent> arraylist = new ArrayList<>();

            for (Setting setting : this.module.getSettings()) {
                if (setting instanceof BooleanSetting booleansetting) {
                    ClickGuiScreen clickguiscreen = this.gui;
                    arraylist.add(new BooleanSettingComponent(booleansetting, clickguiscreen));
                } else if (setting instanceof NumberSetting numbersetting) {
                    arraylist.add(new NumberSettingComponent(numbersetting, this.gui));
                } else if (setting instanceof ModeSetting modesetting) {
                    arraylist.add(new ModeSettingComponent(modesetting, this.gui));
                } else if (setting instanceof KeybindSetting keybindsetting) {
                    arraylist.add(new KeybindSettingComponent(keybindsetting, this.gui, this.module));
                } else if (setting instanceof StringSetting stringsetting) {
                    arraylist.add(new StringSettingComponent(this.gui, stringsetting, f, (int)f1, this.module));
                } else if (setting instanceof ColorSetting colorsetting) {
                    arraylist.add(new ColorSettingComponent(colorsetting, this.gui, f, (int)f1, this.module));
                } else if (setting instanceof ButtonSetting buttonsetting) {
                    arraylist.add(new ButtonSettingComponent(buttonsetting, this.gui, this.module));
                } else if (setting instanceof RangeSetting rangesetting) {
                    arraylist.add(new RangeSettingComponent(rangesetting, this.gui));
                } else if (setting instanceof MultiSelectSetting multiselectsetting) {
                    ClickGuiScreen clickguiscreen1 = this.gui;
                    arraylist.add(new MultiSelectSettingComponent(multiselectsetting, clickguiscreen1, (int)f, (int)f1));
                } else if (setting instanceof CurveSetting curvesetting) {
                    arraylist.add(new CurveSettingComponent(curvesetting, this.gui, (int)f, (int)f1));
                } else if (setting instanceof RegistrySetting registrysetting) {
                    arraylist.add(new RegistrySettingComponent(registrysetting, this.gui, f, f1));
                } else if (setting instanceof ItemSlotsSetting itemslotssetting) {
                    arraylist.add(new ItemSlotsSettingComponent(itemslotssetting, this.gui));
                } else if (setting instanceof ItemListSetting itemlistsetting) {
                    ClickGuiScreen clickguiscreen2 = this.gui;
                    arraylist.add(new ItemListSettingComponent(itemlistsetting, clickguiscreen2));
                } else if (setting instanceof ListSetting listsetting) {
                    arraylist.add(new ListSettingComponent(listsetting, this.gui));
                } else if (setting instanceof GradientSetting gradientsetting) {
                    arraylist.add(new GradientSettingComponent(gradientsetting, this.gui, f, f1));
                }

                f1 += 25.0F;
                if (++i >= 6) {
                    f += 130.0F;
                    f1 = (float)(this.gui.windowY + this.y + 29.0);
                    i = 0;
                }
            }

            COMPONENT_CACHE.put(this.module, arraylist);
            this.settingComponents = arraylist;
        }
    }

    public void setPosition(double d0, double d1) {
        this.animatedY = d0;
        this.animatedX = d1;
        this.heightAnimation.setTarget(d0);
        this.heightAnimation.update();
        this.y = this.heightAnimation.getValue() + d1;
    }

    private void drawBackground(int i, int j) {
        float f = (float)(this.gui.windowX + this.x + 100.0 + this.gui.cardWidth);
        float f1 = (float)(this.gui.windowY + this.y - 10.0);
        float f2 = this.getHeight();
        float f57 = f + 345.0F;
        float f29 = f1 + f2;
        boolean flag = this.isInBounds(f, f1, f57, f29, i, j);
        double d0 = flag ? 1.0 : 0.0;
        this.hoverAnimation.setTarget(d0);
        this.hoverAnimation.update();
        float f3 = this.hoverAnimation.getFloat();
        UiAnimation uianimation = this.enabledAnimation;
        double d1 = this.module.isEnabled() ? 1.0 : 0.0;
        uianimation.setTarget(d1);
        this.enabledAnimation.update();
        float f4 = this.enabledAnimation.getFloat();
        float f5 = this.expandAnimation.getFloat();
        Color color = GuiTheme.moduleColor;
        float f6 = 1.0F + 0.08F * f3;
        Color color1 = ColorUtil.scaleBrightness(color, f6);
        float f7 = GuiTheme.radiusBottomLeft;
        if (GuiTheme.isBlurMode()) {
            Render2DUtil.drawRoundedRect(f, f1, 345.0F, f2, f7, 1.0F, ColorUtil.withAlpha(color1, 50));
        } else if (GuiTheme.isGradientMode()) {
            Render2DUtil.drawRoundedRect(f, f1, 345.0F, f2, f7, 1.0F, ColorUtil.withAlpha(color1, 128));
        } else {
            Render2DUtil.drawRoundedRect(f, f1, 345.0F, f2, f7, 1.0F, color1);
        }

        if (f4 > 0.01F) {
            Color color13 = GuiTheme.accentColor1;
            Color color21 = ColorUtil.withAlpha(color13, 214);
            float f58 = 345.0F * 0.5F;
            float f59 = 345.0F * 0.26449F;
            float f30 = ModuleGlowRenderer.half(45.957462F);
            Render2DUtil.drawModuleGlowFilled(f, f1, 345.0F, f2, f7, 1.0F, 0.0F, null, null, color21, f58, 34.0F, f59, 14.13482F, f30, 1, f4);
        }

        if (f5 > 0.01F) {
            float f8 = f1 + 34.0F + 2.0F;
            float f9 = f2 - 46.0F;
            if (GuiTheme.isBlurMode()) {
                float f53 = f + 5.0F;
                float f56 = 345.0F - 10.0F;
                Color color14 = GuiTheme.settingsBg;
                Render2DUtil.drawRoundedRect(f53, f8, f56, f9, f7, 1.0F, ColorUtil.withAlpha(color14, 128));
            } else if (GuiTheme.isGradientMode()) {
                float f54 = f + 5.0F;
                float f55 = 345.0F - 10.0F;
                Color color15 = GuiTheme.settingsBg;
                Render2DUtil.drawRoundedRect(f54, f8, f55, f9, f7, 1.0F, ColorUtil.withAlpha(color15, 128));
            } else {
                Render2DUtil.drawRoundedRect(f + 5.0F, f8, 345.0F - 10.0F, f9, f7, 1.0F, GuiTheme.settingsBg);
            }
        }

        float f41 = (float)(this.gui.windowY + this.y);
        if (!this.module.getSettings().isEmpty()) {
            float f42 = 20.0F;
            float f10 = 34.0F;
            float f11 = f + 345.0F - f42;
            Color color2 = ColorUtil.scaleBrightness(GuiTheme.moduleColor, 1.25F);
            Color color3 = ColorUtil.scaleBrightness(GuiTheme.moduleColor, 1.55F);
            Color color4 = ColorUtil.lerpColor(color2, color3, f5);
            if (!GuiTheme.isBlurMode() && !GuiTheme.isGradientMode()) {
                Render2DUtil.drawRectCornerRadii(f11, f1, f42, f10, 0.0F, f7, 0.0F, f7, 1.0F, color4);
            } else {
                Color color16 = ColorUtil.withAlpha(color4, 128);
                float f31 = 1.0F;
                float f32 = f7;
                float f33 = 0.0F;
                float f34 = f7;
                float f35 = 0.0F;
                float f36 = f10;
                float f37 = f42;
                float f38 = f1;
                float f39 = f11;
                Render2DUtil.drawRectCornerRadii(f39, f38, f37, f36, f35, f34, f33, f32, f31, color16);
            }

            Color color5 = this.module.isEnabled() ? GuiTheme.accentColor1 : ColorUtil.scaleBrightness(GuiTheme.moduleText, 1.3F);
            Color color6 = ColorUtil.lerpColor(color5, GuiTheme.normalTextColor, f5);
            float f13 = 8.0F;
            float f14 = f11 + (f42 - f13) / 2.0F;
            float f15 = f1 + (f10 - f13) / 2.0F;
            float f16 = -90.0F + 180.0F * f5;
            Render2DUtil.drawArrow(f14, f15, f13, f13, 5.0F, 0.5F, 1.0F, 40.0F, f16, 1.0F, color6);
        }

        Color color17 = ColorUtil.scaleBrightness(GuiTheme.outlineColor, 1.2F);
        Color color18 = GuiTheme.accentColor2;
        Color color19 = ColorUtil.lerpColor(color17, color18, f4);
        Render2DUtil.drawOutline(f, f1, 345.0F, f2, f7, 0.5F, GuiTheme.borderThickness, color19);
        float f12 = f + 8.0F;
        float f43 = VoilClient.fonts.getSelectedFont().getWidth(this.module.getName().toString());
        Module module1 = this.module;
        float f44 = this.drawModuleTags(module1, f12 + f43 + 4.0F, f41);
        boolean flag1 = this.module.getKeyCode() != 0;
        List list = flag1 ? this.getSettingComponents() : List.of();
        this.noSettings = !list.isEmpty();
        boolean flag2 = false;
        if (this.noSettings) {
            float f45 = VoilClient.fonts.getIcons().getWidth(IconGlyphs.iconE07B(), 14.0F);
            float f46 = VoilClient.fonts.getIcons().getLineHeight(14.0F);
            float f47 = f12 + f43 + 4.0F + f44;
            float f49 = f41 - 4.5F + (12.0F - f46) / 2.0F;
            this.bindTextWidth = f47;
            this.bindAlpha = f49;
            this.arrowRotation = f45;
            this.scrollOffset = f46;
            f57 = f47 + f45;
            float f40 = f49 + f46;
            flag2 = this.isInBounds(f47, f49, f57, f40, i, j);
            Color color7 = flag2 ? new Color(255, 120, 70) : new Color(217, 96, 60);
            VoilClient.fonts.getIcons().render(IconGlyphs.iconE07B(), f47, f49, 14.0F, color7.getRGB());
        }

        boolean flag3 = this.module.isBinding();
        boolean flag4 = flag1 || flag3 || flag;
        double d2 = flag4 ? 1.0 : 0.0;
        this.bindAnimation.setTarget(d2);
        this.bindAnimation.update();
        float f48 = this.bindAnimation.getFloat();
        float f50 = this.module.getSettings().isEmpty() ? 0.0F : 20.0F;
        float f51 = f + 345.0F - f50 - 8.0F;
        if (f48 > 0.01F) {
            float f17 = this.drawBindBadge(f51, f1, flag1, flag3, f48);
            f51 -= f17 + 6.0F;
        } else {
            this.bindHovered = false;
        }

        Color color20 = new Color(255, 179, 71);
        Color color8 = new Color(180, 180, 180);
        float f18 = f51 - 18.0F;
        float f19 = f1 + 8.0F;
        this.lastMouseX = f18;
        this.lastMouseY = f19;
        Module modulex = this.module;
        if (ModuleManager.INSTANCE.isEnabled(modulex)) {
            Render2DUtil.drawStarCustom(f18, f19, 18.0F, color20, null, 0.6F, 0.0F, 0.45F, 1.0F);
        } else if (f3 > 0.01F) {
            int k = (int)(220.0F * f3);
            Render2DUtil.drawStarCustom(f18, f19, 18.0F, null, ColorUtil.withAlpha(color8, k), 0.6F, 0.0F, 0.45F, 1.0F);
        }

        float f52 = f1 + 17.0F;
        float f20 = VoilClient.fonts.getSelectedFont().getLineHeight();
        float f21 = VoilClient.fonts.getSelectedFont().getLineHeight(9.0F);
        float f22 = f52 - (f20 + 2.0F + f21) / 2.0F;
        float f23 = f22 + f20 + 2.0F;
        Color color9 = ColorUtil.scaleBrightness(GuiTheme.moduleText, 1.4F);
        MSDFFontAtlas msdffontatlas = VoilClient.fonts.getSelectedFont();
        String s = this.module.getDescription();
        msdffontatlas.render(s + ".", f12, f23, 9.0F, color9.getRGB());
        Color color10 = ColorUtil.scaleBrightness(GuiTheme.moduleText, 1.6F);
        Color color11 = GuiTheme.normalTextColor;
        Color color12 = ColorUtil.lerpColor(color10, color11, f4);
        VoilClient.fonts.getSelectedFont().render(this.module.getName().toString(), f12, f22, color12.getRGB());
        if (flag2) {
            StringBuilder stringbuilder = new StringBuilder();

            for (int l = 0; l < list.size(); l++) {
                if (l > 0) {
                    stringbuilder.append(", ");
                }

                stringbuilder.append(((Module)list.get(l)).getName());
            }

            String s6 = String.valueOf(stringbuilder);
            String s3 = list.size() == 1 ? " Module also has the same bind" : "";
            String s2 = s6;
            String s5 = s2 + s3;
            float f24 = VoilClient.fonts.getGreyscale().getWidth(s5, 8);
            float f25 = VoilClient.fonts.getGreyscale().getLineHeight(8);
            float f26 = f24 + 6.0F;
            float f27 = this.bindTextWidth + this.arrowRotation + 6.0F;
            float f28 = f41 - 4.5F;
            Render2DUtil.drawRoundedRect(f27, f28, f26, 12.0F, 6.0F, 1.0F, ColorUtil.scaleBrightness(GuiTheme.moduleColor, 1.6F));
            Render2DUtil.drawOutline(f27, f28, f26, 12.0F, 6.0F, 0.5F, GuiTheme.borderThickness, new Color(217, 96, 60));
            VoilClient.fonts.getGreyscale().render(s5, f27 + (f26 - f24) / 2.0F, f28 + (12.0F - f25) / 2.0F, 8, GuiTheme.normalTextColor.getRGB());
        }
    }

    public void render(MatrixStack matrixstack, int i, int j, float f) {
        if (!this.module.isHidden()) {
            UiAnimation uianimation = this.expandAnimation;
            double d0 = this.module.isExpanded() ? 1.0 : 0.0;
            uianimation.setTarget(d0);
            this.expandAnimation.update();
            float f1 = this.expandAnimation.getFloat();
            this.drawBackground(i, j);
            if (f1 > 0.01F) {
                float f2 = (float)(this.gui.windowX + this.x + 100.0 + this.gui.cardWidth);
                float f3 = (float)(this.gui.windowY + this.y - 10.0);
                float f4 = this.getHeight();
                float f5 = f3 + f4 - 10.0F;
                float f6 = f2 + 10.0F - this.gui.windowX - 445.0F;
                float f7 = (float)(this.gui.windowY + this.y + 29.0);
                int k = (int)Math.max(f2, this.gui.windowX);
                float f10 = f3 + 34.0F;
                int k1 = this.gui.windowY;
                int l = (int)Math.max(f10, k1 + 30);
                float f11 = f2 + 345.0F;
                int l1 = this.gui.windowWidth;
                k1 = this.gui.windowX;
                int i1 = (int)Math.min(f11, k1 + l1);
                l1 = this.gui.windowHeight;
                k1 = this.gui.windowY;
                k1 += l1;
                int j1 = (int)Math.min(f5, k1 - 1);
                if (i1 <= k || j1 <= l) {
                    return;
                }

                RenderManager.setScissor(k, l, i1, j1);

                for (ClickGuiComponent clickguicomponent : this.settingComponents) {
                    Setting setting = clickguicomponent.getSetting();
                    if (setting == null || setting.isVisible()) {
                        RectBounds rectbounds = clickguicomponent.getBounds();
                        float f8 = 25.0F;
                        if (rectbounds != null) {
                            f8 = Math.max(f8, rectbounds.height);
                        }

                        if (f7 > f5) {
                            break;
                        }

                        clickguicomponent.setPosition(f6, f7);
                        clickguicomponent.render2(matrixstack, i, j, f);
                        if (setting != null && this.isExpanded(setting)) {
                            RectBounds rectbounds1 = clickguicomponent.getBounds();
                            if (rectbounds1 != null) {
                                float f12 = rectbounds1.posX - 2.0F;
                                float f9 = rectbounds1.posY;
                                Render2DUtil.drawOutline(f12, f9, rectbounds1.width + 4.0F, rectbounds1.height, 6.0F, 0.5F, 0.4F, GuiTheme.accentColor2);
                            }
                        }

                        f7 += f8;
                    }
                }

                RenderManager.disableScissor();
                k1 = this.gui.windowY;
                int j3 = k1 + 30;
                l1 = this.gui.windowWidth;
                k1 = this.gui.windowX;
                int i2 = k1 + l1;
                l1 = this.gui.windowHeight;
                k1 = this.gui.windowY;
                int j2 = k1 + l1 + -1;
                int k2 = i2;
                int l2 = j3;
                int i3 = this.gui.windowX;
                RenderManager.setScissor(i3, l2, k2, j2);
            }
        }
    }

    public void setY(double d0) {
        this.y = d0;
        this.animatedY = d0;
        this.animatedX = 0.0;
        this.heightAnimation.snapTo(d0);
    }

    public double getX() {
        return this.x;
    }

    public float getHeight() {
        return 34.0F + (this.getContentHeight() - 34.0F) * this.expandAnimation.getFloat();
    }

    public void mouseScrolled(double d0, double d1, double d2, double d3) {
        this.settingComponents.forEach(clickguicomponent -> clickguicomponent.mouseScrolled2(d0, d1, d2, d3));
    }

    public Module getModule() {
        return this.module;
    }

    public void mouseClicked(double d0, double d1, int i, double d2, double d3) {
        Iterator<ClickGuiComponent> iterator = this.settingComponents.iterator();

        while (iterator.hasNext()) {
            iterator.next().mouseDragged2(d0, d1, i, d2, d3);
        }
    }

    private float drawBindBadge(float f, float f1, boolean flag, boolean flag1, float f2) {
        String s;
        if (flag1) {
            s = "...";
        } else {
            int j = this.module.getKeyCode();
            s = KeyUtil.getKeyName(j);
        }

        float f3 = VoilClient.fonts.getGreyscale().getWidth(s, 8);
        float f4 = VoilClient.fonts.getGreyscale().getLineHeight(8);
        float f5 = Math.max(f3 + 6.0F, 14.0F);
        float f6 = (1.0F - f2) * 5.0F;
        float f7 = f - f5 + f6;
        float f8 = f1 + (34.0F - 12.0F) / 2.0F;
        int i = Math.round(255.0F * Math.min(1.0F, Math.max(0.0F, f2)));
        Color color;
        Color color1;
        if (!flag && !flag1) {
            Color color6 = ColorUtil.scaleBrightness(GuiTheme.moduleColor, 1.5F);
            int k = (int)(i * 0.8F);
            Color color4 = color6;
            color = ColorUtil.withAlpha(color4, k);
            Color color5 = ColorUtil.scaleBrightness(GuiTheme.moduleText, 1.5F);
            color1 = ColorUtil.withAlpha(color5, i);
        } else {
            Color color2 = GuiTheme.modeSettingFill.brighter();
            color = ColorUtil.withAlpha(color2, i);
            Color color3 = GuiTheme.accentColor1;
            color1 = ColorUtil.withAlpha(color3, i);
        }

        Render2DUtil.drawRoundedRect(f7, f8, f5, 12.0F, 6.0F, 1.0F, color);
        float f9 = f7 + (f5 - f3) / 2.0F;
        float f10 = f8 + (12.0F - f4) / 2.0F;
        VoilClient.fonts.getGreyscale().render(s, f9, f10, 8, color1.getRGB());
        this.bindX = f7;
        this.bindY = f8;
        this.bindWidth = f5;
        this.bindHeight = 12.0F;
        this.bindHovered = f2 > 0.5F;
        return f5 * f2;
    }

    public void charTyped(char chr, int modifiers) {
        this.settingComponents.forEach(clickguicomponent -> clickguicomponent.charTyped(chr, modifiers));
    }

    private boolean isInBounds(float f, float f1, float f2, float f3, double d0, double d1) {
        return d0 >= f && d0 <= f2 && d1 >= f1 && d1 <= f3;
    }

    private List<Module> getSettingComponents() {
        int i = this.module.getKeyCode();
        if (i == 0) {
            return List.of();
        }

        ArrayList<Module> arraylist = new ArrayList<>();

        for (Module modulex : ModuleManager.INSTANCE.getModules()) {
            if (modulex != this.module && modulex.getKeyCode() == i) {
                arraylist.add(modulex);
            }
        }

        return arraylist;
    }

    private float drawModuleTags(Module module, float f, float f1) {
        if (module.getTags().isEmpty()) {
            return 0.0F;
        }

        float f2 = 0.0F;

        for (ModuleTag moduletag : module.getTags()) {
            String s = moduletag.getLabel();
            Color color = switch (moduletag) {
                case NEW -> new Color(72, 217, 126);
                case BLATANT -> new Color(217, 72, 72);
                case NOTOBSPROOF -> new Color(187, 94, 5);
                case DEV -> new Color(114, 137, 218);
                default -> Color.WHITE;
            };

            float f3 = VoilClient.fonts.getSelectedFont().getWidth(s, 8);
            float f4 = VoilClient.fonts.getSelectedFont().getLineHeight(8);
            float f5 = f3 + 5.0F;
            float f6 = f + f2;
            float f7 = f1 - 4.5F;
            Render2DUtil.drawRoundedRect(f6, f7, f5, 12.0F, 12.0F / 2.0F, 1.0F, color);
            float f8 = f6 + (f5 - f3) / 2.0F;
            float f9 = f7 + (12.0F - f4) / 2.0F;
            VoilClient.fonts.getSelectedFont().render(s, f8, f9, 8, color.brighter().getRGB());
            f2 += f5 + 3.0F;
        }

        return f2;
    }

    public void setExpanded(Setting setting, boolean flag) {
        if (flag) {
            this.expandedSettings.add(setting);
        } else {
            this.expandedSettings.remove(setting);
        }
    }
}
