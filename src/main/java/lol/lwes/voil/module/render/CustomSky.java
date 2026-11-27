package lol.lwes.voil.module.render;

import java.awt.Color;
import java.util.Random;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.module.ModuleTag;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.ButtonSetting;
import lol.lwes.voil.setting.ColorSetting;
import lol.lwes.voil.setting.NumberSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.render.CloudSettings;
import lol.lwes.voil.util.render.SkyRenderer;
import net.minecraft.client.MinecraftClient;

public class CustomSky extends Module {
    private final BooleanSetting enableStars;
    private final NumberSetting starDensity;
    private final ColorSetting starColor;
    private final BooleanSetting shootingStars;
    private final ColorSetting shootingStarColor;
    private final BooleanSetting enableAurora;
    private final NumberSetting auroraIntensity;
    private final ColorSetting auroraColor1;
    private final ColorSetting auroraColor2;
    private final ColorSetting auroraColor3;
    private final NumberSetting auroraSpeed;
    private final BooleanSetting enableNebula;
    private final NumberSetting nebulaIntensity;
    private final ColorSetting nebulaColor1;
    private final ColorSetting nebulaColor2;
    private final BooleanSetting enableClouds;
    private final NumberSetting cloudCoverage;
    private final NumberSetting cloudDensity;
    private final NumberSetting cloudScale;
    private final NumberSetting cloudSpeed;
    private final NumberSetting cloudHeight;
    private final NumberSetting cloudThickness;
    private final NumberSetting cloudAbsorption;
    private final NumberSetting cloudAmbient;
    private final NumberSetting cloudQuality;
    private final ColorSetting cloudColor;
    private final ColorSetting cloudShadowColor;
    private final ColorSetting horizonColor;
    private final ColorSetting midSkyColor;
    private final ColorSetting zenithColor;
    private final BooleanSetting enableSun;
    private final ColorSetting sunColor;
    private final NumberSetting sunIntensity;
    private final NumberSetting sunSize;
    private final BooleanSetting enableMoon;
    private final ColorSetting moonColor;
    private final NumberSetting moonIntensity;
    private final NumberSetting moonSize;
    private final NumberSetting vignette;
    private final NumberSetting animationSpeed;
    public final ColorSetting fogColor;
    private final ButtonSetting randomize;

    public CustomSky() {
        super("CustomSky", "Renders sky with custom effects", Category.Render);
        this.enableStars = new BooleanSetting("Enable Stars", true, "Enables star rendering");
        this.starDensity = new NumberSetting("Star Density", 0.0, 10.0, 0.6, 0.1, "Controls the density of stars");
        Color color14 = new Color(255, 255, 240);
        String s2 = "Controls star color";
        Color color = color14;
        this.starColor = new ColorSetting("Star Color", color, s2);
        this.shootingStars = new BooleanSetting("Shooting Stars", true, "Enables shooting stars");
        color14 = new Color(255, 230, 180);
        String s3 = "Color of shooting stars";
        Color color1 = color14;
        this.shootingStarColor = new ColorSetting("Shooting Star Color", color1, s3);
        this.enableAurora = new BooleanSetting("Enable Aurora", true, "Enables the aurora effect");
        this.auroraIntensity = new NumberSetting("Aurora Intensity", 0.0, 2.0, 0.8, 0.1, "Controls aurora brightness");
        color14 = new Color(26, 230, 153);
        String s4 = "First aurora color";
        Color color2 = color14;
        this.auroraColor1 = new ColorSetting("Aurora Color 1", color2, s4);
        color14 = new Color(102, 51, 230);
        String s5 = "Second aurora color";
        Color color3 = color14;
        this.auroraColor2 = new ColorSetting("Aurora Color 2", color3, s5);
        color14 = new Color(230, 26, 153);
        String s6 = "Third aurora color";
        Color color4 = color14;
        this.auroraColor3 = new ColorSetting("Aurora Color 3", color4, s6);
        this.auroraSpeed = new NumberSetting("Aurora Speed", 0.1, 3.0, 1.0, 0.1, "Aurora animation speed");
        this.enableNebula = new BooleanSetting("Enable Nebula", true, "Enables nebula effect");
        this.nebulaIntensity = new NumberSetting("Nebula Intensity", 0.0, 2.0, 0.5, 0.1, "Nebula density");
        color14 = new Color(26, 13, 38);
        String s7 = "Primary nebula color";
        Color color5 = color14;
        this.nebulaColor1 = new ColorSetting("Nebula Color 1", color5, s7);
        color14 = new Color(13, 26, 38);
        String s8 = "Secondary nebula color";
        Color color6 = color14;
        this.nebulaColor2 = new ColorSetting("Nebula Color 2", color6, s8);
        this.enableClouds = new BooleanSetting("Enable Clouds", true, "Volumetric clouds");
        this.cloudCoverage = new NumberSetting("Cloud Coverage", 0.0, 1.0, 0.45, 0.05, "How much of the sky is covered");
        this.cloudDensity = new NumberSetting("Cloud Density", 0.1, 3.0, 1.0, 0.1, "Cloud thickness");
        this.cloudScale = new NumberSetting("Cloud Scale", 0.1, 2.0, 0.5, 0.05, "Noise frequency");
        this.cloudSpeed = new NumberSetting("Cloud Speed", 0.0, 3.0, 1.0, 0.1, "Drift speed");
        this.cloudHeight = new NumberSetting("Cloud Height", 0.5, 4.0, 1.5, 0.1, "Height of the cloud layer");
        this.cloudThickness = new NumberSetting("Cloud Thickness", 0.2, 3.0, 1.0, 0.1, "Vertical extent of the cloud layer");
        this.cloudAbsorption = new NumberSetting("Cloud Absorption", 0.5, 10.0, 4.0, 0.5, "Light extinction");
        this.cloudAmbient = new NumberSetting("Cloud Ambient", 0.0, 3.0, 1.0, 0.1, "Sky light on clouds");
        this.cloudQuality = new NumberSetting("Cloud Quality", 8.0, 48.0, 24.0, 4.0, "Quality of steps");
        Color color15 = new Color(255, 247, 242);
        String s9 = "Lit cloud color";
        Color color7 = color15;
        this.cloudColor = new ColorSetting("Cloud Color", color7, s9);
        color15 = new Color(89, 102, 140);
        String s10 = "Ambient cloud color";
        Color color8 = color15;
        this.cloudShadowColor = new ColorSetting("Cloud Shadow Color", color8, s10);
        color15 = new Color(3, 4, 8);
        String s11 = "Horizon sky color";
        Color color9 = color15;
        this.horizonColor = new ColorSetting("Horizon Color", color9, s11);
        color15 = new Color(5, 10, 20);
        String s12 = "Middle sky color";
        Color color10 = color15;
        this.midSkyColor = new ColorSetting("Mid Sky Color", color10, s12);
        color15 = new Color(8, 18, 41);
        String s13 = "Zenith (top) sky color";
        Color color11 = color15;
        this.zenithColor = new ColorSetting("Zenith Color", color11, s13);
        this.enableSun = new BooleanSetting("Enable Sun", true, "Renders the sun");
        color15 = new Color(255, 240, 200);
        String s14 = "Sun color";
        Color color12 = color15;
        this.sunColor = new ColorSetting("Sun Color", color12, s14);
        this.sunIntensity = new NumberSetting("Sun Intensity", 0.1, 5.0, 1.0, 0.1, "Sun brightness");
        this.sunSize = new NumberSetting("Sun Size", 0.01, 0.2, 0.05, 0.01, "Sun glow size");
        this.enableMoon = new BooleanSetting("Enable Moon", true, "Renders the moon");
        color15 = new Color(200, 220, 255);
        String s15 = "Moon color";
        Color color13 = color15;
        this.moonColor = new ColorSetting("Moon Color", color13, s15);
        this.moonIntensity = new NumberSetting("Moon Intensity", 0.1, 5.0, 0.5, 0.1, "Moon brightness");
        this.moonSize = new NumberSetting("Moon Size", 0.01, 0.2, 0.04, 0.01, "Moon glow size");
        this.vignette = new NumberSetting("Vignette", 0.0, 1.0, 0.1, 0.05, "Edge darkening intensity");
        this.animationSpeed = new NumberSetting("Animation Speed", 0.1, 3.0, 0.9, 0.1, "Global animation speed");
        color15 = new Color(239, 30, 143);
        this.fogColor = new ColorSetting("Fog Color", color15, "Color of the fog", true);
        this.randomize = new ButtonSetting("Randomize", this::randomizeSettings, "Randomize all settings");
        Setting[] asetting = new Setting[]{
            this.randomize,
            this.enableStars,
            this.starDensity,
            this.starColor,
            this.shootingStars,
            this.shootingStarColor,
            this.enableAurora,
            this.auroraIntensity,
            this.auroraColor1,
            this.auroraColor2,
            this.auroraColor3,
            this.auroraSpeed,
            this.enableNebula,
            this.nebulaIntensity,
            this.nebulaColor1,
            this.nebulaColor2,
            this.enableClouds,
            this.cloudCoverage,
            this.cloudDensity,
            this.cloudScale,
            this.cloudSpeed,
            this.cloudHeight,
            this.cloudThickness,
            this.cloudAbsorption,
            this.cloudAmbient,
            this.cloudQuality,
            this.cloudColor,
            this.cloudShadowColor,
            this.horizonColor,
            this.midSkyColor,
            this.zenithColor,
            this.fogColor,
            this.enableSun,
            this.sunColor,
            this.sunIntensity,
            this.sunSize,
            this.enableMoon,
            this.moonColor,
            this.moonIntensity,
            this.moonSize,
            this.vignette,
            this.animationSpeed
        };
        this.addSettings(asetting);
        Setting.dependSetting(this.starDensity, this.enableStars);
        Setting.dependSetting(this.starColor, this.enableStars);
        Setting.dependSetting(this.shootingStars, this.enableStars);
        Setting.dependSetting(this.shootingStarColor, this.shootingStars);
        Setting.dependSetting(this.auroraIntensity, this.enableAurora);
        Setting.dependSetting(this.auroraColor1, this.enableAurora);
        Setting.dependSetting(this.auroraColor2, this.enableAurora);
        Setting.dependSetting(this.auroraColor3, this.enableAurora);
        Setting.dependSetting(this.auroraSpeed, this.enableAurora);
        Setting.dependSetting(this.nebulaIntensity, this.enableNebula);
        Setting.dependSetting(this.nebulaColor1, this.enableNebula);
        Setting.dependSetting(this.nebulaColor2, this.enableNebula);
        Setting.dependSetting(this.cloudCoverage, this.enableClouds);
        Setting.dependSetting(this.cloudDensity, this.enableClouds);
        Setting.dependSetting(this.cloudScale, this.enableClouds);
        Setting.dependSetting(this.cloudSpeed, this.enableClouds);
        Setting.dependSetting(this.cloudHeight, this.enableClouds);
        Setting.dependSetting(this.cloudThickness, this.enableClouds);
        Setting.dependSetting(this.cloudAbsorption, this.enableClouds);
        Setting.dependSetting(this.cloudAmbient, this.enableClouds);
        Setting.dependSetting(this.cloudQuality, this.enableClouds);
        Setting.dependSetting(this.cloudColor, this.enableClouds);
        Setting.dependSetting(this.cloudShadowColor, this.enableClouds);
        Setting.dependSetting(this.sunColor, this.enableSun);
        Setting.dependSetting(this.sunIntensity, this.enableSun);
        Setting.dependSetting(this.sunSize, this.enableSun);
        Setting.dependSetting(this.moonColor, this.enableMoon);
        Setting.dependSetting(this.moonIntensity, this.enableMoon);
        Setting.dependSetting(this.moonSize, this.enableMoon);
        ModuleTag[] amoduletag = new ModuleTag[]{ModuleTag.NOTOBSPROOF};
        this.addTags(amoduletag);
    }

    public static Color randomColor() {
        Random random = new Random();
        return new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
    }

    @Override
    public void onTick() {
        if (SkyRenderer.instance != null) {
            float[] afloat = this.colorToFloats(this.horizonColor.getColor());
            float[] afloat1 = this.colorToFloats(this.midSkyColor.getColor());
            float[] afloat2 = this.colorToFloats(this.zenithColor.getColor());
            float[] afloat3 = this.colorToFloats(this.auroraColor1.getColor());
            float[] afloat4 = this.colorToFloats(this.auroraColor2.getColor());
            float[] afloat5 = this.colorToFloats(this.auroraColor3.getColor());
            float[] afloat6 = this.colorToFloats(this.nebulaColor1.getColor());
            float[] afloat7 = this.colorToFloats(this.nebulaColor2.getColor());
            float[] afloat8 = this.colorToFloats(this.sunColor.getColor());
            float[] afloat9 = this.colorToFloats(this.moonColor.getColor());
            float[] afloat10 = this.colorToFloats(this.starColor.getColor());
            float[] afloat11 = this.colorToFloats(this.shootingStarColor.getColor());
            float f = this.enableStars.isEnabled() ? this.starDensity.getFloat() : 0.0F;
            float f1 = this.enableAurora.isEnabled() ? this.auroraIntensity.getFloat() : 0.0F;
            float f2 = this.enableNebula.isEnabled() ? this.nebulaIntensity.getFloat() : 0.0F;
            SkyRenderer skyrenderer = SkyRenderer.instance;
            float f14 = this.vignette.getFloat();
            float f15 = this.animationSpeed.getFloat();
            boolean flag4 = this.shootingStars.isEnabled();
            boolean flag5 = this.enableNebula.isEnabled();
            float f16 = this.auroraSpeed.getFloat();
            boolean flag6 = this.enableSun.isEnabled();
            boolean flag7 = this.enableMoon.isEnabled();
            float f17 = this.sunIntensity.getFloat();
            float f18 = this.moonIntensity.getFloat();
            float f19 = this.sunSize.getFloat();
            float f4 = this.moonSize.getFloat();
            float f5 = f19;
            float f6 = f18;
            float f7 = f17;
            float[] afloat12 = afloat9;
            float[] afloat13 = afloat8;
            boolean flag = flag7;
            boolean flag1 = flag6;
            float[] afloat14 = afloat7;
            float[] afloat15 = afloat6;
            float f8 = f2;
            float f9 = f16;
            boolean flag2 = flag5;
            float[] afloat16 = afloat11;
            boolean flag3 = flag4;
            float f10 = f15;
            float f11 = f14;
            float[] afloat17 = afloat5;
            float[] afloat18 = afloat4;
            float[] afloat19 = afloat3;
            float f12 = f1;
            float[] afloat20 = afloat10;
            float f13 = f;
            float[] afloat21 = afloat1;
            float[] afloat22 = afloat2;
            float[] afloat23 = afloat;
            skyrenderer.applySettings(
                afloat23,
                afloat22,
                afloat21,
                f13,
                afloat20,
                f12,
                afloat19,
                afloat18,
                afloat17,
                f11,
                f10,
                flag3,
                afloat16,
                flag2,
                f9,
                f8,
                afloat15,
                afloat14,
                flag1,
                flag,
                afloat13,
                afloat12,
                f7,
                f6,
                f5,
                f4
            );
            float f3 = this.cloudHeight.getFloat();
            skyrenderer = SkyRenderer.instance;
            CloudSettings cloudsettings = new CloudSettings(
                this.enableClouds.isEnabled(),
                this.cloudCoverage.getFloat(),
                this.cloudDensity.getFloat(),
                this.cloudScale.getFloat(),
                this.cloudSpeed.getFloat(),
                f3,
                f3 + this.cloudThickness.getFloat(),
                this.cloudAbsorption.getFloat(),
                this.cloudAmbient.getFloat(),
                Math.round(this.cloudQuality.getFloat()),
                this.colorToFloats(this.cloudColor.getColor()),
                this.colorToFloats(this.cloudShadowColor.getColor())
            );
            skyrenderer.setCloudSettings(cloudsettings);
        }
    }

    private void randomizeSettings() {
        Random random = new Random();
        this.enableStars.setEnabled(random.nextBoolean());
        NumberSetting numbersetting = this.starDensity;
        double d0 = (float)(random.nextDouble() * 10.0);
        numbersetting.setValue(d0);
        ColorSetting colorsetting = this.starColor;
        Color color = randomColor();
        colorsetting.setColor(color);
        this.shootingStars.setEnabled(random.nextBoolean());
        ColorSetting colorsetting1 = this.shootingStarColor;
        Color color1 = randomColor();
        colorsetting1.setColor(color1);
        this.enableAurora.setEnabled(random.nextBoolean());
        numbersetting = this.auroraIntensity;
        double d1 = (float)(random.nextDouble() * 2.0);
        numbersetting.setValue(d1);
        ColorSetting colorsetting2 = this.auroraColor1;
        Color color2 = randomColor();
        colorsetting2.setColor(color2);
        ColorSetting colorsetting3 = this.auroraColor2;
        Color color3 = randomColor();
        colorsetting3.setColor(color3);
        ColorSetting colorsetting4 = this.auroraColor3;
        Color color4 = randomColor();
        colorsetting4.setColor(color4);
        numbersetting = this.auroraSpeed;
        double d2 = (float)(0.1 + random.nextDouble() * 2.9);
        numbersetting.setValue(d2);
        this.enableNebula.setEnabled(random.nextBoolean());
        numbersetting = this.nebulaIntensity;
        double d3 = (float)(random.nextDouble() * 2.0);
        numbersetting.setValue(d3);
        ColorSetting colorsetting5 = this.nebulaColor1;
        Color color5 = randomColor();
        colorsetting5.setColor(color5);
        ColorSetting colorsetting6 = this.nebulaColor2;
        Color color6 = randomColor();
        colorsetting6.setColor(color6);
        this.enableClouds.setEnabled(random.nextBoolean());
        numbersetting = this.cloudCoverage;
        double d4 = (float)(0.2 + random.nextDouble() * 0.6);
        numbersetting.setValue(d4);
        numbersetting = this.cloudDensity;
        double d5 = (float)(0.5 + random.nextDouble() * 1.5);
        numbersetting.setValue(d5);
        numbersetting = this.cloudScale;
        double d6 = (float)(0.25 + random.nextDouble() * 0.75);
        numbersetting.setValue(d6);
        numbersetting = this.cloudSpeed;
        double d7 = (float)(random.nextDouble() * 2.0);
        numbersetting.setValue(d7);
        numbersetting = this.cloudHeight;
        double d8 = (float)(0.8 + random.nextDouble() * 2.0);
        numbersetting.setValue(d8);
        numbersetting = this.cloudThickness;
        double d9 = (float)(0.5 + random.nextDouble() * 1.5);
        numbersetting.setValue(d9);
        numbersetting = this.cloudAbsorption;
        double d10 = (float)(2.0 + random.nextDouble() * 4.0);
        numbersetting.setValue(d10);
        numbersetting = this.cloudAmbient;
        double d11 = (float)(0.5 + random.nextDouble() * 1.5);
        numbersetting.setValue(d11);
        ColorSetting colorsetting7 = this.cloudColor;
        Color color7 = randomColor();
        colorsetting7.setColor(color7);
        ColorSetting colorsetting8 = this.cloudShadowColor;
        Color color8 = randomColor();
        colorsetting8.setColor(color8);
        ColorSetting colorsetting9 = this.horizonColor;
        Color color9 = randomColor();
        colorsetting9.setColor(color9);
        ColorSetting colorsetting10 = this.midSkyColor;
        Color color10 = randomColor();
        colorsetting10.setColor(color10);
        ColorSetting colorsetting11 = this.zenithColor;
        Color color11 = randomColor();
        colorsetting11.setColor(color11);
        ColorSetting colorsetting12 = this.fogColor;
        Color color12 = randomColor();
        colorsetting12.setColor(color12);
        this.enableSun.setEnabled(random.nextBoolean());
        ColorSetting colorsetting13 = this.sunColor;
        Color color13 = randomColor();
        colorsetting13.setColor(color13);
        numbersetting = this.sunIntensity;
        double d12 = (float)(0.1 + random.nextDouble() * 4.9);
        numbersetting.setValue(d12);
        numbersetting = this.sunSize;
        double d13 = (float)(0.01 + random.nextDouble() * 0.19);
        numbersetting.setValue(d13);
        this.enableMoon.setEnabled(random.nextBoolean());
        ColorSetting colorsetting14 = this.moonColor;
        Color color14 = randomColor();
        colorsetting14.setColor(color14);
        numbersetting = this.moonIntensity;
        double d14 = (float)(0.1 + random.nextDouble() * 4.9);
        numbersetting.setValue(d14);
        numbersetting = this.moonSize;
        double d15 = (float)(0.01 + random.nextDouble() * 0.19);
        numbersetting.setValue(d15);
        numbersetting = this.vignette;
        double d16 = (float)(random.nextDouble() * 1.0);
        numbersetting.setValue(d16);
        numbersetting = this.animationSpeed;
        double d17 = (float)(0.1 + random.nextDouble() * 0.4);
        numbersetting.setValue(d17);
    }

    @Override
    public void onEnable() {
        if (SkyRenderer.instance == null) {
            MinecraftClient.getInstance().submit(SkyRenderer::initialize);
        }
    }

    private float[] colorToFloats(Color color) {
        return new float[]{color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F};
    }
}
