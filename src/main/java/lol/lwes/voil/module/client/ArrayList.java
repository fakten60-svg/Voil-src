package lol.lwes.voil.module.client;

import dev.prestigeclient.renderlib.api.font.MSDFFontAtlas;
import dev.prestigeclient.renderlib.impl.render.layer.type.BufferCollector;
import java.awt.Color;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.BooleanSupplier;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.config.HudLayout;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.module.ModuleManager;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.ColorSetting;
import lol.lwes.voil.setting.GradientSetting;
import lol.lwes.voil.setting.ModeSetting;
import lol.lwes.voil.setting.MultiSelectSetting;
import lol.lwes.voil.setting.NumberSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.GradientFontEffect;
import lol.lwes.voil.util.render.EasingType;
import lol.lwes.voil.util.render.GradientUtil;
import lol.lwes.voil.util.render.IndexedFontEffect;
import lol.lwes.voil.util.render.Render2DUtil;
import lol.lwes.voil.util.render.RenderManager;
import lol.lwes.voil.util.render.UiAnimation;
import lol.lwes.voil.util.render.shader.BlurShaderRenderer;

public class ArrayList extends Module {
    private final ModeSetting font;
    private final BooleanSetting lowercase;
    private final MultiSelectSetting categoryExclusion;
    private final MultiSelectSetting elements;
    private final NumberSetting horizontalPadding;
    private final NumberSetting verticalPadding;
    private final NumberSetting roundness;
    private final ModeSetting backgroundStyle;
    private final ColorSetting backgroundColor;
    private final NumberSetting blurStrength;
    private final NumberSetting blurSaturation;
    private final ModeSetting sidebarStyle;
    private final NumberSetting sidebarWidth;
    private final NumberSetting bloomStrength;
    private final NumberSetting bloomRadius;
    private final GradientSetting bloomGradient;
    private final NumberSetting outlineThickness;
    private final GradientSetting outlineGradient;
    private final GradientSetting textGradient;
    private final NumberSetting gradientSpeed;
    private final Map moduleAnimations;
    private final Map<String, Float> textWidthCache;
    private final Map<String, float[]> charCenterCache;
    private final java.util.ArrayList visibleModules;
    private final GradientFontEffect effectBatch;
    private final IndexedFontEffect effectRenderer;
    private MSDFFontAtlas fontAtlas;
    private static final String[] CATEGORY_NAMES = Arrays.stream(Category.values()).map(Enum::name).toArray(String[]::new);

    public ArrayList() {
        super("ArrayList", "Enabled modules", Category.Client);
        NumberSetting numbersetting = null;
        this.font = new ModeSetting(
            "Font",
            "Client",
            "Font of the arraylist",
            new String[]{"Client", "InterBold", "Inter", "Verdana", "Poppins", "Quicksand", "SfRegular", "Greycliff"}
        );
        this.lowercase = new BooleanSetting("Lowercase", false, "Lowercase module names");
        this.categoryExclusion = new MultiSelectSetting("Category Exclusion", CATEGORY_NAMES, new String[0], "Exclude categories from the arraylist");
        String[] astring2 = new String[]{"Background", "Bloom", "Outline", "Sidebar"};
        String[] astring3 = new String[]{"Background"};
        String s2 = "Visual elements to render";
        String[] astring = astring3;
        String[] astring1 = astring2;
        this.elements = new MultiSelectSetting("Elements", astring1, astring, s2);
        this.horizontalPadding = new NumberSetting("Horizontal Padding", 0.0, 10.0, 4.0, 0.5, "Horizontal padding around module names");
        this.verticalPadding = new NumberSetting("Vertical Padding", 0.0, 6.0, 2.0, 0.5, "Vertical padding around module names");
        this.roundness = new NumberSetting("Roundness", 0.0, 10.0, 3.0, 1.0, "Roundness of the background");
        this.backgroundStyle = new ModeSetting("Background Style", "Normal", "Style of the background", new String[]{"Normal", "Blur"});
        Color color = new Color(0, 0, 0, 140);
        this.backgroundColor = new ColorSetting("Background Color", color, "Color of the background", true);
        this.blurStrength = new NumberSetting("Blur Strength", 1.0, 20.0, 8.0, 1.0, "Strength of the background blur");
        this.blurSaturation = new NumberSetting("Blur Saturation", 0.0, 2.0, 1.0, 0.1, "Saturation of the blurred background");
        this.sidebarStyle = new ModeSetting("Sidebar Style", "Full", "Style of sidebar", new String[]{"Dashed", "Full"});
        this.sidebarWidth = new NumberSetting("Sidebar Width", 0.0, 10.0, 2.0, 0.5, "Width of the sidebar");
        this.bloomStrength = new NumberSetting("Bloom Strength", 0.0, 10.0, 5.0, 1.0, "Strength of the bloom effect");
        this.bloomRadius = new NumberSetting("Bloom Radius", 0.0, 10.0, 5.0, 1.0, "Radius of the bloom effect");
        this.bloomGradient = new GradientSetting(
            "Bloom Gradient", "Gradient of the bloom effect", new Color[]{new Color(255, 153, 81), new Color(105, 105, 255)}
        );
        this.outlineThickness = new NumberSetting("Outline Thickness", 0.0, 10.0, 1.0, 0.5, "Thickness of the outline");
        this.outlineGradient = new GradientSetting(
            "Outline Gradient", "Gradient of the outline", new Color[]{new Color(255, 153, 81), new Color(105, 105, 255)}
        );
        this.textGradient = new GradientSetting("Text Gradient", "Gradient of the arraylist", new Color[]{new Color(255, 153, 81), new Color(105, 105, 255)});
        this.gradientSpeed = new NumberSetting("Gradient Speed", 0.0, 5.0, 1.0, 0.1, "Scroll speed of all gradients");
        this.moduleAnimations = new IdentityHashMap();
        this.textWidthCache = new HashMap<>();
        this.charCenterCache = new HashMap<>();
        this.visibleModules = new java.util.ArrayList();
        this.effectBatch = new GradientFontEffect();
        this.effectRenderer = new IndexedFontEffect(this.effectBatch);
        Setting[] asetting = new Setting[]{
            this.font,
            this.lowercase,
            this.categoryExclusion,
            this.elements,
            this.horizontalPadding,
            this.verticalPadding,
            this.roundness,
            this.backgroundStyle,
            this.backgroundColor,
            this.blurStrength,
            this.blurSaturation,
            this.sidebarStyle,
            this.sidebarWidth,
            this.bloomStrength,
            this.bloomRadius,
            this.bloomGradient,
            this.outlineThickness,
            this.outlineGradient,
            this.textGradient,
            this.gradientSpeed
        };
        this.addSettings(asetting);
        numbersetting = this.roundness;
        BooleanSupplier booleansupplier = () -> this.elements.isSelected("Background")
            || this.elements.isSelected("Bloom")
            || this.elements.isSelected("Outline");
        numbersetting.setVisibleWhen(booleansupplier);
        ModeSetting modesetting = this.backgroundStyle;
        BooleanSupplier booleansupplier1 = () -> this.elements.isSelected("Background");
        modesetting.setVisibleWhen(booleansupplier1);
        ColorSetting colorsetting = this.backgroundColor;
        BooleanSupplier booleansupplier2 = () -> this.elements.isSelected("Background");
        colorsetting.setVisibleWhen(booleansupplier2);
        numbersetting = this.blurStrength;
        BooleanSupplier booleansupplier3 = () -> this.elements.isSelected("Background") && this.backgroundStyle.is("Blur");
        numbersetting.setVisibleWhen(booleansupplier3);
        numbersetting = this.blurSaturation;
        BooleanSupplier booleansupplier4 = () -> this.elements.isSelected("Background") && this.backgroundStyle.is("Blur");
        numbersetting.setVisibleWhen(booleansupplier4);
        ModeSetting modesetting1 = this.sidebarStyle;
        BooleanSupplier booleansupplier5 = () -> this.elements.isSelected("Sidebar");
        modesetting1.setVisibleWhen(booleansupplier5);
        numbersetting = this.sidebarWidth;
        BooleanSupplier booleansupplier6 = () -> this.elements.isSelected("Sidebar");
        numbersetting.setVisibleWhen(booleansupplier6);
        numbersetting = this.bloomStrength;
        BooleanSupplier booleansupplier7 = () -> this.elements.isSelected("Bloom");
        numbersetting.setVisibleWhen(booleansupplier7);
        numbersetting = this.bloomRadius;
        BooleanSupplier booleansupplier8 = () -> this.elements.isSelected("Bloom");
        numbersetting.setVisibleWhen(booleansupplier8);
        GradientSetting gradientsetting = this.bloomGradient;
        BooleanSupplier booleansupplier9 = () -> this.elements.isSelected("Bloom");
        gradientsetting.setVisibleWhen(booleansupplier9);
        numbersetting = this.outlineThickness;
        BooleanSupplier booleansupplier10 = () -> this.elements.isSelected("Outline");
        numbersetting.setVisibleWhen(booleansupplier10);
        GradientSetting gradientsetting1 = this.outlineGradient;
        BooleanSupplier booleansupplier11 = () -> this.elements.isSelected("Outline");
        gradientsetting1.setVisibleWhen(booleansupplier11);
    }

    private float width(MSDFFontAtlas msdffontatlas, String s) {
        return this.textWidthCache.computeIfAbsent(s, s1 -> msdffontatlas.getWidth(s1, 10.0F));
    }

    @Override
    public void onRender2D() {
        if (mc.player != null && mc.world != null) {
            MSDFFontAtlas msdffontatlas = switch (this.font.getMode()) {
                case "InterBold" -> VoilClient.fonts.getInterBold();
                case "Inter" -> VoilClient.fonts.getInter();
                case "Verdana" -> VoilClient.fonts.getVerdana();
                case "Poppins" -> VoilClient.fonts.getPoppins();
                case "Quicksand" -> VoilClient.fonts.getQuicksand();
                case "SfRegular" -> VoilClient.fonts.getSfRegular();
                case "Greycliff" -> VoilClient.fonts.getGreyscale();
                default -> VoilClient.fonts.getSelectedFont();
            };
            if (msdffontatlas != null) {
                if (msdffontatlas != this.fontAtlas) {
                    this.textWidthCache.clear();
                    this.charCenterCache.clear();
                    this.fontAtlas = msdffontatlas;
                }

                this.visibleModules.clear();

                for (Module module1 : ModuleManager.INSTANCE.getModules()) {
                    boolean flag4;
                    label224: {
                        if (module1.isEnabled()) {
                            MultiSelectSetting multiselectsetting = this.categoryExclusion;
                            String s = module1.getCategory().name();
                            if (!multiselectsetting.isSelected(s)) {
                                flag4 = true;
                                break label224;
                            }
                        }

                        flag4 = false;
                    }

                    boolean flag = flag4;
                    UiAnimation uianimation = (UiAnimation)this.moduleAnimations.get(module1);
                    if (uianimation == null) {
                        if (!flag) {
                            continue;
                        }

                        uianimation = new UiAnimation(EasingType.EASE_OUT_CUBIC).setDuration(250.0);
                        this.moduleAnimations.put(module1, uianimation);
                    }

                    double d1 = flag ? 1.0 : 0.0;
                    uianimation.setTarget(d1);
                    uianimation.update();
                    if (!flag && !uianimation.isAnimating()) {
                        this.moduleAnimations.remove(module1);
                    } else {
                        this.visibleModules.add(module1);
                    }
                }

                if (!this.visibleModules.isEmpty()) {
                    this.visibleModules.sort(Comparator.<Module>comparingDouble(module2 -> {
                        String s2 = module2.getName();
                        return -this.width(msdffontatlas, this.formatName(s2));
                    }).thenComparing(module2 -> {
                        String s2 = module2.getName();
                        return this.formatName(s2);
                    }));
                    HudLayout hudlayout = VoilClient.getHudLayout();
                    int j2 = hudlayout.getX(this);
                    int k2 = hudlayout.getY(this);
                    int j1 = hudlayout.getWidth(this);
                    int i1 = j2;
                    int l2 = i1 + j1;
                    boolean flag1 = j2 + hudlayout.getWidth(this) / 2.0F > mc.getWindow().getScaledWidth() / 2.0F;
                    boolean flag2 = this.elements.isSelected("Sidebar") && this.sidebarWidth.getFloat() > 0.0F;
                    float f = flag2 ? this.sidebarWidth.getFloat() : 0.0F;
                    float f1 = flag2 ? f + 1.5F : 0.0F;
                    float f2 = msdffontatlas.getLineHeight(10.0F);
                    float f3 = this.horizontalPadding.getFloat();
                    float f4 = f2 + this.verticalPadding.getFloat() * 2.0F;
                    float f5 = this.roundness.getFloat();
                    double d0 = System.nanoTime() / 1.0E9 * this.gradientSpeed.getFloat() * 0.25;
                    float f6 = (float)(d0 - Math.floor(d0));
                    int i = this.visibleModules.size();
                    String[] astring = new String[i];
                    float[] afloat = new float[i];
                    float[] afloat1 = new float[i];
                    float[] afloat2 = new float[i];
                    float[] afloat3 = new float[i];
                    float[] afloat4 = new float[i];
                    float f7 = 0.0F;
                    float f8 = 0.0F;

                    for (int j = 0; j < i; j++) {
                        Module module = (Module)this.visibleModules.get(j);
                        String s1 = module.getName();
                        astring[j] = this.formatName(s1);
                        afloat[j] = ((UiAnimation)this.moduleAnimations.get(module)).getFloat();
                        afloat1[j] = this.width(msdffontatlas, astring[j]) + f3 * 2.0F;
                        afloat2[j] = afloat1[j] * afloat[j];
                        afloat3[j] = f4 * afloat[j];
                        afloat4[j] = k2 + f8;
                        f8 += afloat3[j];
                        f7 = Math.max(f7, afloat2[j]);
                    }

                    if (!(f7 <= 0.0F) && !(f8 <= 0.0F)) {
                        int i3 = (int)Math.ceil(f7 + f1);
                        int j3 = flag1 ? l2 - i3 : j2;
                        hudlayout.setX(this, j3);
                        int k1 = (int)Math.ceil(f8);
                        int l1 = i3;
                        ArrayList arraylist1 = this;
                        hudlayout.setSize(arraylist1, l1, k1);
                        float f9 = flag1 ? l2 - f1 - f7 : j2 + f1;
                        float f10 = f9 + f7;
                        float f11 = flag1 ? l2 - f : j2;
                        float f12 = flag1 ? f9 : j2;
                        float f13 = f7 + f1;
                        int k = Math.min(i, 32);
                        float[] afloat5 = new float[k];
                        float[] afloat6 = new float[k];
                        float f14 = 0.0F;
                        float f15 = 0.0F;

                        for (int l = 0; l < k; l++) {
                            afloat5[k - 1 - l] = afloat3[l];
                            i1 = k - 1;
                            afloat6[i1 - l] = afloat2[l];
                            f14 = Math.max(f14, afloat2[l]);
                            f15 += afloat3[l];
                        }

                        float f50 = flag1 ? f10 - f14 : f9;
                        if (this.elements.isSelected("Bloom") && Render2DUtil.kawaseGlowRenderer != null) {
                            float f16 = this.bloomRadius.getFloat() * 3.0F;
                            float f17 = this.bloomStrength.getFloat() / 5.0F;
                            if (f16 > 0.0F && f17 > 0.0F) {
                                float f48 = k2;
                                Color[] acolor = GradientUtil.getCornerColors(this.bloomGradient, f50, f48, f14, f15, f6);
                                float f61 = k2;
                                Color color16 = acolor[0];
                                Color color17 = acolor[1];
                                Color color18 = acolor[2];
                                Color color3 = acolor[3];
                                Color color4 = color18;
                                Color color5 = color17;
                                Color color6 = color16;
                                boolean flag3 = flag1;
                                float f19 = f17;
                                float f20 = f16;
                                float f21 = f5;
                                float[] afloat7 = afloat6;
                                float[] afloat8 = afloat5;
                                float f22 = f61;
                                float f23 = f50;
                                Render2DUtil.kawaseGlowRenderer.render(f23, f22, afloat8, afloat7, f21, f20, f19, flag3, color6, color5, color4, color3);
                            }
                        }

                        if (this.elements.isSelected("Background")) {
                            Color color12 = this.backgroundColor.getColor();
                            if (this.backgroundStyle.is("Blur")) {
                                if (Render2DUtil.blurRenderer != null) {
                                    BlurShaderRenderer blurshaderrenderer = Render2DUtil.blurRenderer;
                                    BufferCollector buffercollector1 = RenderManager.bufferCollector;
                                    float f62 = k2;
                                    float f59 = this.blurStrength.getFloat();
                                    float f60 = this.blurSaturation.getFloat();
                                    float f63 = color12.getAlpha() / 255.0F;
                                    float f64 = color12.getRed() / 255.0F;
                                    float f24 = color12.getBlue() / 255.0F;
                                    float f25 = color12.getGreen() / 255.0F;
                                    float f26 = f64;
                                    float f27 = f63;
                                    float f28 = f60;
                                    float f29 = f59;
                                    float f30 = f5;
                                    float f31 = f5;
                                    float f32 = f5;
                                    float f33 = f5;
                                    float[] afloat9 = afloat6;
                                    float[] afloat10 = afloat5;
                                    float f34 = f62;
                                    float f35 = f50;
                                    BufferCollector buffercollector = buffercollector1;
                                    blurshaderrenderer.drawRoundedRectArraysA(
                                        buffercollector, f35, f34, afloat10, afloat9, f33, f32, f31, f30, f29, f28, f27, f26, f25, f24, flag1
                                    );
                                }
                            } else if (Render2DUtil.outlineRenderer != null) {
                                float f36 = k2;
                                float f37 = f50;
                                Render2DUtil.outlineRenderer.render(f37, f36, afloat5, afloat6, f5, 1.0F, color12, flag1);
                            }
                        }

                        float f51 = this.outlineThickness.getFloat();
                        if (this.elements.isSelected("Outline") && f51 > 0.0F && Render2DUtil.outlineRenderer != null) {
                            float f49 = k2;
                            Color[] acolor1 = GradientUtil.getCornerColors(this.outlineGradient, f50, f49, f14, f15, f6);
                            float f57 = k2;
                            Color color11 = acolor1[0];
                            Color color14 = acolor1[1];
                            Color color15 = acolor1[2];
                            Color color7 = acolor1[3];
                            Color color8 = color15;
                            Color color9 = color14;
                            Color color10 = color11;
                            float f38 = f51;
                            float f39 = 1.0F;
                            float f40 = f5;
                            float[] afloat11 = afloat6;
                            float[] afloat12 = afloat5;
                            float f41 = f57;
                            float f42 = f50;
                            Render2DUtil.outlineRenderer.render(f42, f41, afloat12, afloat11, f40, f39, f38, color10, color9, color8, color7, flag1);
                        }

                        if (flag2) {
                            float f52 = f11 + f / 2.0F;
                            if (this.sidebarStyle.is("Full")) {
                                Color color13 = GradientUtil.getColorAt(this.textGradient, f12, k2, f13, f8, f52, k2, f6);
                                Color color = GradientUtil.getColorAt(this.textGradient, f12, k2, f13, f8, f52, k2 + f8, f6);
                                Render2DUtil.drawRectGradient(f11, k2, f, f8, Math.min(f5, f / 2.0F), 1.0F, color, color, color13, color13);
                            } else {
                                for (int l3 = 0; l3 < i; l3++) {
                                    float f54 = afloat3[l3] * 0.6F;
                                    if (!(f54 <= 0.01F)) {
                                        float f18 = afloat4[l3] + (afloat3[l3] - f54) / 2.0F;
                                        Color color1 = GradientUtil.getColorAt(this.textGradient, f12, k2, f13, f8, f52, f18 + f54 / 2.0F, f6);
                                        Color color2 = new Color(
                                            color1.getRed(), color1.getGreen(), color1.getBlue(), Math.round(color1.getAlpha() * afloat[l3])
                                        );
                                        Render2DUtil.drawRoundedRect(f11, f18, f, f54, Math.min(f, f54) / 2.0F, 1.0F, color2);
                                    }
                                }
                            }
                        }

                        float f43 = k2;
                        float f44 = f12;
                        GradientSetting gradientsetting = this.textGradient;
                        this.effectBatch.begin(gradientsetting, f44, f43, f13, f8, f6, i);
                        this.effectRenderer.resetIndex();

                        for (int k3 = 0; k3 < i; k3++) {
                            float f53 = flag1 ? f10 - afloat2[k3] : f9 + afloat2[k3] - afloat1[k3];
                            float f55 = f53 + f3;
                            float f56 = afloat4[k3] + (afloat3[k3] - f2) / 2.0F;
                            float[] afloat14 = this.charCenterCache.computeIfAbsent(astring[k3], s2 -> computeCharCenters(msdffontatlas, s2));
                            float f58 = f56 + f2 / 2.0F;
                            float f45 = afloat[k3];
                            float[] afloat13 = afloat14;
                            float f46 = f58;
                            float f47 = f55;
                            int i2 = k3;
                            this.effectBatch.setGlyph(i2, f47, f46, afloat13, f45);
                            if (afloat[k3] > 0.02F) {
                                msdffontatlas.renderWithEffect(astring[k3], f55, f56, 10.0F, this.effectRenderer);
                            }

                            this.effectRenderer.advanceIndex();
                        }
                    }
                }
            }
        }
    }

    private String formatName(String s) {
        return this.lowercase.isEnabled() ? s.toLowerCase(Locale.ROOT) : s;
    }

    private static float[] computeCharCenters(MSDFFontAtlas msdffontatlas, String s) {
        float[] afloat = new float[s.length()];
        float f = 0.0F;

        for (int i = 0; i < s.length(); i++) {
            float f1 = msdffontatlas.getWidth(String.valueOf(s.charAt(i)), 10.0F);
            afloat[i] = f + f1 / 2.0F;
            f += f1;
        }

        return afloat;
    }
}
