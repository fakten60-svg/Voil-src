package lol.lwes.voil.module.render;

import dev.prestigeclient.renderlib.api.entity.EntityOutlineHandler;
import dev.prestigeclient.renderlib.impl.api.RenderAPI;
import java.awt.Color;
import java.util.List;
import java.util.function.BooleanSupplier;
import lol.lwes.voil.config.ClientSettings;
import lol.lwes.voil.config.GuiTheme;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.ColorSetting;
import lol.lwes.voil.setting.GradientSetting;
import lol.lwes.voil.setting.GradientStop;
import lol.lwes.voil.setting.ModeSetting;
import lol.lwes.voil.setting.NumberSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.JfaOutlineShader;
import lol.lwes.voil.util.JfaWireframeShader;
import lol.lwes.voil.util.SystemUtil;
import lol.lwes.voil.util.render.Render2DUtil;
import lol.lwes.voil.util.render.Render3DUtil;
import lol.lwes.voil.util.target.TargetManager;
import lol.lwes.voil.util.world.EntityUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class ESP extends Module {
    public static ModeSetting mode = new ModeSetting("modes", "2D", "Mode for the esp", new String[]{"Box", "2D", "2D Corners", "Shader"});
    public static ModeSetting shaderType = new ModeSetting("Shader Type", "Glow", "Type of shader to use for shader esp", new String[]{"Glow", "Wireframe"});
    public static ModeSetting glowType = new ModeSetting(
        "Glow Type", "Sharp", "Sharp: more cheaper glow, Bloom more spread but more expensive", new String[]{"Bloom", "Sharp"}
    );
    public static ModeSetting blurType = new ModeSetting("Blur Type", "Kawase", "Blur used for the bloom halo", new String[]{"Kawase", "Gaussian", "Box"});
    public NumberSetting padding;
    public ModeSetting tracerY;
    public BooleanSetting healthbar;
    public static BooleanSetting fill = new BooleanSetting("Fill", true, "Draws a filled area");
    public static BooleanSetting outline = new BooleanSetting("Outline", true, "Draws an outline around the entity alongside glow");
    public BooleanSetting tracers;
    public BooleanSetting flashOnDamage;
    public ModeSetting healthbarPosition;
    public NumberSetting healthbarPadding;
    public NumberSetting thickness;
    public static GradientSetting fillGradient = new GradientSetting(
        "Fill Gradient", "Fill colors for ESP", true, new Color[]{new Color(187, 98, 239, 110), new Color(98, 160, 239, 110)}
    );
    public static GradientSetting outlineGradient = new GradientSetting(
        "Outline Gradient", "Line, outline and tracer colors for ESP", true, new Color[]{new Color(187, 98, 239), new Color(98, 160, 239)}
    );
    public static NumberSetting glowRadius = new NumberSetting("Glow Radius", 0.1, 5.0, 2.0, 0.1, "How far the glow extends (thickness of outline)");
    public static NumberSetting glowIntensity = new NumberSetting("Glow Intensity", 0.1, 1.0, 0.3, 0.1, "How soft or hard the glow's falloff is");
    public static NumberSetting speed = new NumberSetting("Speed", 0.1, 10.0, 1.0, 0.1, "Animation speed");
    public static NumberSetting colorFrequency = new NumberSetting("Color Frequency", 0.1, 10.0, 1.0, 0.1, "How many times the color pattern repeats");
    public static NumberSetting outlineThickness = new NumberSetting("Outline Thickness", 0.1, 5.0, 0.3, 0.1, "Thickness of the outline");
    public static ColorSetting damagedColor = new ColorSetting("Damaged Color", new Color(255, 40, 40, 255), "Color while the entity is hurt");
    public static BooleanSetting shine = new BooleanSetting("Shine", false, "Sweeps a glossy highlight across the fill and outline");
    public static NumberSetting shineIntensity = new NumberSetting("Shine Intensity", 0.1, 2.0, 0.9, 0.1, "Brightness of the shine highlight");
    public static NumberSetting shineSpeed = new NumberSetting("Shine Speed", 0.1, 2.0, 0.4, 0.1, "Shine sweeps per second");
    public static NumberSetting shineWidth = new NumberSetting("Shine Width", 4.0, 60.0, 18.0, 1.0, "Width of the shine band in pixels");
    public static NumberSetting shineAngle = new NumberSetting("Shine Angle", 0.0, 360.0, 30.0, 5.0, "Direction the shine sweeps in degrees");
    public static BooleanSetting self = new BooleanSetting("Self", false, "Highlights self");
    public static NumberSetting alpha = new NumberSetting("Alpha", 1.0, 255.0, 255.0, 1.0, "Alpha value for ESP");
    public static ModeSetting colorMode = new ModeSetting(
        "Color Mode", "Gradient", "Color mode for the shader esp", new String[]{"Gradient", "Health Based", "Damaged"}
    );
    private static GradientSetting themeGradient = new GradientSetting(
        "Theme Gradient", "Synced theme colors", true, new Color[]{GuiTheme.accentColor1, GuiTheme.accentColor2}
    );
    private String wireframeShaderId;
    private String glowShaderId;

    public ESP() {
        super("ESP", "Highlights players", Category.Render);
        this.padding = new NumberSetting("Padding", 0.0, 0.4, 0.2, 0.1, "Overall padding for 2D esp");
        this.tracerY = new ModeSetting("Tracer Y", "Feet", "From where line to go to enemy", new String[]{"Feet", "Crosshair"});
        this.healthbar = new BooleanSetting("Healthbar", true, "Show healthbar");
        this.tracers = new BooleanSetting("Tracers", false, "Draws a line to enemy");
        this.flashOnDamage = new BooleanSetting("Flash on damage", false, "Changes tracers line color to red when enemy damaged");
        this.healthbarPosition = new ModeSetting("Healthbar Position", "Left", "Position of the healthbar", new String[]{"Right", "Left", "Top", "Under"});
        this.healthbarPadding = new NumberSetting("Healthbar Padding", 0.0, 3.0, 2.0, 1.0, "Padding between healthbar and box");
        this.thickness = new NumberSetting("Thickness", 0.5, 4.0, 1.5, 0.5, "Thickness of the box lines in pixels");
        this.wireframeShaderId = SystemUtil.randomString(10);
        this.glowShaderId = SystemUtil.randomString(10);
        Setting[] asetting = new Setting[]{
            mode,
            shaderType,
            glowType,
            blurType,
            this.padding,
            this.healthbar,
            this.healthbarPadding,
            fill,
            this.thickness,
            this.tracers,
            this.flashOnDamage,
            this.tracerY,
            this.healthbarPosition,
            self,
            glowRadius,
            glowIntensity,
            speed,
            colorFrequency,
            alpha,
            colorMode,
            fillGradient,
            outlineGradient,
            damagedColor,
            outline,
            outlineThickness,
            shine,
            shineIntensity,
            shineSpeed,
            shineWidth,
            shineAngle
        };
        this.addSettings(asetting);
        BooleanSupplier booleansupplier = () -> !isThemeGradientEnabled();
        BooleanSupplier booleansupplier1 = () -> mode.is("Shader");
        Setting.dependSetting(this.healthbarPosition, this.healthbar);
        Setting.dependSetting(this.healthbarPadding, this.healthbar);
        Setting.dependSetting(colorFrequency, "Shader", mode);
        GradientSetting gradientsetting = fillGradient;
        BooleanSupplier booleansupplier3 = () -> booleansupplier.getAsBoolean() && (!booleansupplier1.getAsBoolean() || !colorMode.is("Health Based"));
        gradientsetting.setVisibleWhen(booleansupplier3);
        gradientsetting = outlineGradient;
        BooleanSupplier booleansupplier4 = () -> {
            if (!booleansupplier.getAsBoolean()) {
                return false;
            }

            if (!booleansupplier1.getAsBoolean() || this.tracers.isEnabled()) {
                return true;
            }

            return outline.isEnabled() && !colorMode.is("Health Based");
        };
        gradientsetting.setVisibleWhen(booleansupplier4);
        ColorSetting colorsetting = damagedColor;
        BooleanSupplier booleansupplier5 = () -> booleansupplier.getAsBoolean() && booleansupplier1.getAsBoolean() && colorMode.is("Damaged");
        colorsetting.setVisibleWhen(booleansupplier5);
        Setting.dependSetting(outline, "Shader", mode);
        NumberSetting numbersetting5 = outlineThickness;
        BooleanSupplier booleansupplier6 = () -> booleansupplier1.getAsBoolean() && outline.isEnabled();
        numbersetting5.setVisibleWhen(booleansupplier6);
        Setting.dependAny(colorMode, mode, new String[]{"Shader"});
        Setting.dependSetting(self, "Shader", mode);
        Setting.dependSetting(glowRadius, "Shader", mode);
        Setting.dependSetting(glowIntensity, "Shader", mode);
        Setting.dependSetting(speed, "Shader", mode);
        Setting.dependSetting(shaderType, "Shader", mode);
        Setting.dependSetting(glowType, "Shader", mode);
        BooleanSupplier booleansupplier2 = () -> booleansupplier1.getAsBoolean() && shaderType.is("Glow");
        BooleanSetting booleansetting = shine;
        BooleanSupplier booleansupplier7 = booleansupplier2::getAsBoolean;
        booleansetting.setVisibleWhen(booleansupplier7);
        NumberSetting numbersetting = shineIntensity;
        BooleanSupplier booleansupplier8 = () -> booleansupplier2.getAsBoolean() && shine.isEnabled();
        numbersetting.setVisibleWhen(booleansupplier8);
        NumberSetting numbersetting1 = shineSpeed;
        BooleanSupplier booleansupplier9 = () -> booleansupplier2.getAsBoolean() && shine.isEnabled();
        numbersetting1.setVisibleWhen(booleansupplier9);
        NumberSetting numbersetting2 = shineWidth;
        BooleanSupplier booleansupplier10 = () -> booleansupplier2.getAsBoolean() && shine.isEnabled();
        numbersetting2.setVisibleWhen(booleansupplier10);
        NumberSetting numbersetting3 = shineAngle;
        BooleanSupplier booleansupplier11 = () -> booleansupplier2.getAsBoolean() && shine.isEnabled();
        numbersetting3.setVisibleWhen(booleansupplier11);
        ModeSetting modesetting = blurType;
        BooleanSupplier booleansupplier12 = () -> booleansupplier1.getAsBoolean() && glowType.is("Bloom");
        modesetting.setVisibleWhen(booleansupplier12);
        NumberSetting numbersetting4 = alpha;
        BooleanSupplier booleansupplier13 = () -> booleansupplier1.getAsBoolean() && fill.isEnabled();
        numbersetting4.setVisibleWhen(booleansupplier13);
        Setting.dependSetting(this.padding, mode, new String[]{"2D", "2D Corners"});
        Setting.dependSetting(this.thickness, mode, new String[]{"2D", "2D Corners"});
        Setting.dependSetting(this.tracerY, this.tracers);
        Setting.dependSetting(this.flashOnDamage, this.tracers);
        ModeSetting modesetting1 = mode;
        Runnable runnable = this::reload;
        modesetting1.setAfterSwitch(runnable);
        ModeSetting modesetting2 = shaderType;
        Runnable runnable1 = this::reload;
        modesetting2.setAfterSwitch(runnable1);
    }

    static {
        new BooleanSetting("Players", true, "Highlights players");
    }

    public static GradientSetting getOutlineGradient() {
        return isThemeGradientEnabled() ? updateThemeGradient() : outlineGradient;
    }

    private static GradientSetting updateThemeGradient() {
        List list = themeGradient.getStops();
        GradientSetting gradientsetting = themeGradient;
        GradientStop gradientstop2 = (GradientStop)list.getFirst();
        Color color = GuiTheme.accentColor1;
        GradientStop gradientstop = gradientstop2;
        gradientsetting.setStopColor(gradientstop, color);
        gradientsetting = themeGradient;
        gradientstop2 = (GradientStop)list.getLast();
        Color color1 = GuiTheme.accentColor2;
        GradientStop gradientstop1 = gradientstop2;
        gradientsetting.setStopColor(gradientstop1, color1);
        return themeGradient;
    }

    private void drawTracer(LivingEntity livingentity, float f) {
        boolean flag = this.tracerY.is("Crosshair");
        Vec3d vec3d = flag ? mc.player.getCameraPosVec(f).add(mc.player.getRotationVec(f).multiply(0.1)) : mc.player.getLerpedPos(f);
        Vec3d vec3d1 = flag
            ? livingentity.getLerpedPos(f).add(0.0, livingentity.getEyeHeight(livingentity.getPose()) / 2.0F, 0.0)
            : livingentity.getLerpedPos(f);
        if (this.flashOnDamage.isEnabled() && livingentity.hurtTime > 0) {
            Render3DUtil.drawLine(vec3d, vec3d1, Color.RED);
        } else {
            Render3DUtil.drawGradientLine(vec3d, vec3d1, getOutlineGradient());
        }
    }

    private Vec3d[] getBoxCorners(Entity entity) {
        double d0 = entity.lastX + (entity.getX() - entity.lastX) * SystemUtil.getTickDelta();
        double d1 = entity.lastY + (entity.getY() - entity.lastY) * SystemUtil.getTickDelta();
        double d2 = entity.lastZ + (entity.getZ() - entity.lastZ) * SystemUtil.getTickDelta();
        Box box = entity.getBoundingBox().offset(-entity.getX(), -entity.getY(), -entity.getZ()).offset(d0, d1, d2).expand(0.01);
        double d3 = this.padding.getFloat();
        return new Vec3d[]{
            new Vec3d(box.minX - d3, box.minY - d3, box.minZ - d3),
            new Vec3d(box.minX - d3, box.maxY + d3, box.minZ - d3),
            new Vec3d(box.maxX + d3, box.minY - d3, box.minZ - d3),
            new Vec3d(box.maxX + d3, box.maxY + d3, box.minZ - d3),
            new Vec3d(box.minX - d3, box.minY - d3, box.maxZ + d3),
            new Vec3d(box.minX - d3, box.maxY + d3, box.maxZ + d3),
            new Vec3d(box.maxX + d3, box.minY - d3, box.maxZ + d3),
            new Vec3d(box.maxX + d3, box.maxY + d3, box.maxZ + d3)
        };
    }

    @Override
    public void onRender2D() {
        if (mc.world != null && mc.player != null) {
            if (mode.is("2D") || mode.is("2D Corners")) {
                double d0 = mc.getWindow().getScaledWidth();
                double d1 = mc.getWindow().getScaledHeight();

                for (Entity entity : TargetManager.getTargetList(true)) {
                    if (entity instanceof LivingEntity livingentity) {
                        Vec3d[] avec3d = this.getBoxCorners(livingentity);
                        double d2 = Double.MAX_VALUE;
                        double d3 = Double.MAX_VALUE;
                        double d4 = -Double.MAX_VALUE;
                        double d5 = -Double.MAX_VALUE;
                        int i = 0;

                        for (Vec3d vec3d : avec3d) {
                            Vec3d vec3d1 = EntityUtil.worldToScreen(vec3d);
                            if (!(vec3d1.z <= 0.0) && !(vec3d1.z >= 1.0)) {
                                i++;
                                d2 = Math.min(d2, vec3d1.x);
                                d3 = Math.min(d3, vec3d1.y);
                                d4 = Math.max(d4, vec3d1.x);
                                d5 = Math.max(d5, vec3d1.y);
                            }
                        }

                        if (i >= 2) {
                            d2 = Math.max(0.0, Math.min(d0, d2));
                            d4 = Math.max(0.0, Math.min(d0, d4));
                            d3 = Math.max(0.0, Math.min(d1, d3));
                            d5 = Math.max(0.0, Math.min(d1, d5));
                            int j = (int)d2;
                            int k = (int)d3;
                            int l = (int)(d4 - d2);
                            int i1 = (int)(d5 - d3);
                            if (l >= 1 && i1 >= 1) {
                                if (mode.is("2D")) {
                                    this.drawBox2D(j, k, l, i1, this.thickness.getFloat());
                                } else {
                                    this.drawCornerBox(j, k, l, i1, this.thickness.getFloat());
                                }

                                if (this.healthbar.isEnabled()) {
                                    this.drawHealthBar(livingentity, j, k, l, i1);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onDisable() {
        mc.send(() -> EntityOutlineHandler.disable());
    }

    @Override
    public void onEnable() {
        if (mode.is("Shader")) {
            mc.send(() -> {
                if (EntityOutlineHandler.getShader(this.wireframeShaderId) == null || EntityOutlineHandler.getShader(this.glowShaderId) == null) {
                    EntityOutlineHandler.registerShader(this.wireframeShaderId, new JfaOutlineShader());
                    EntityOutlineHandler.registerShader(this.glowShaderId, new JfaWireframeShader());
                }

                if (shaderType.is("Glow")) {
                    EntityOutlineHandler.setShader(this.wireframeShaderId);
                } else {
                    EntityOutlineHandler.setShader(this.glowShaderId);
                }

                EntityOutlineHandler.setEntityFilter(entity -> TargetManager.isTarget(entity, true) || entity == mc.player && self.isEnabled());
                EntityOutlineHandler.enable();
            });
        }
    }

    public static GradientSetting getFillGradient() {
        return isThemeGradientEnabled() ? updateThemeGradient() : fillGradient;
    }

    private void drawCornerBox(int i, int j, int k, int l, float f) {
        GradientSetting gradientsetting = getOutlineGradient();
        int i1 = Math.min(k, l) / 3;

        for (int j1 = 0; j1 < 4; j1++) {
            boolean flag = (j1 & 1) != 0;
            boolean flag1 = (j1 & 2) != 0;
            float f14;
            if (flag) {
                int k1 = i + k;
                f14 = k1 - i1;
            } else {
                f14 = i;
            }

            float f1 = f14;
            float f2 = flag1 ? j + l - f : j;
            float f3 = flag ? i + k - f : i;
            if (flag1) {
                int i2 = j + l;
                f14 = i2 - i1;
            } else {
                f14 = j;
            }

            float f4 = f14;
            f14 = f1 - 1.0F;
            float f5 = f2 - 1.0F;
            Render2DUtil.drawRoundedRect(f14, f5, i1 + 2.0F, f + 2.0F, 0.0F, 0.0F, Color.BLACK);
            f14 = f3 - 1.0F;
            float f6 = f4 - 1.0F;
            Render2DUtil.drawRoundedRect(f14, f6, f + 2.0F, i1 + 2.0F, 0.0F, 0.0F, Color.BLACK);
        }

        for (int l1 = 0; l1 < 4; l1++) {
            boolean flag2 = (l1 & 1) != 0;
            boolean flag3 = (-3 - l1 & 2) != 0;
            float f9 = flag2 ? i + k - i1 : i;
            float f10 = flag3 ? j + l - f : j;
            float f11 = flag2 ? i + k - f : i;
            float f13;
            if (flag3) {
                int j2 = j + l;
                f13 = j2 - i1;
            } else {
                f13 = j;
            }

            float f12 = f13;
            float f15 = i1;
            float f17 = i;
            float f18 = j;
            float f7 = k;
            Render2DUtil.drawGradientRoundedEx(f9, f10, f15, f, 0.0F, 1.0F, f17, f18, f7, l, gradientsetting, 1.0F);
            float f16 = i1;
            f17 = i;
            f18 = j;
            float f8 = k;
            Render2DUtil.drawGradientRoundedEx(f11, f12, f, f16, 0.0F, 1.0F, f17, f18, f8, l, gradientsetting, 1.0F);
        }

        if (fill.isEnabled()) {
            this.drawBoxOutline(i, j, k, l, 2.0F);
        }
    }

    private void drawHealthBar(LivingEntity livingentity, int i, int j, int k, int l) {
        float f = livingentity.getMaxHealth();
        if (!(f <= 0.0F)) {
            float f1 = Math.max(0.0F, Math.min(1.0F, livingentity.getHealth() / f));
            Color color = new Color(0.0F, 0.0F, 0.0F, 0.85F);
            Color color1 = new Color(Math.min(1.0F, 2.0F * (1.0F - f1)), Math.min(1.0F, 2.0F * f1), 0.0F, 1.0F);
            float f2 = this.healthbarPadding.getFloat();
            switch (this.healthbarPosition.getMode()) {
                case "Left", "Right" -> {
                    float f3 = this.healthbarPosition.is("Left") ? i - 2.5F - f2 : i + k + f2;
                    float f4 = Math.max(1.0F, l * f1);
                    float f11 = f3 - 1.0F;
                    float f5 = j - 1;
                    Render2DUtil.drawRoundedRect(f11, f5, 2.5F + 2.0F, l + 2, 0.0F, 0.0F, color);
                    float f6 = j + l - f4;
                    Render2DUtil.drawRoundedRect(f3, f6, 2.5F, f4, 0.0F, 0.0F, color1);
                }
                case "Top", "Under" -> {
                    float f8 = this.healthbarPosition.is("Top") ? j - 2.5F - f2 : j + l + f2;
                    float f9 = Math.max(1.0F, k * f1);
                    float f10 = i - 1;
                    float f7 = f8 - 1.0F;
                    Render2DUtil.drawRoundedRect(f10, f7, k + 2, 2.5F + 2.0F, 0.0F, 0.0F, color);
                    Render2DUtil.drawRoundedRect(i, f8, f9, 2.5F, 0.0F, 0.0F, color1);
                }
                default -> {
                }
            }
        }
    }

    private void drawBoxOutline(int i, int j, int k, int l, float f) {
        float f1 = k - 2.0F * f;
        float f2 = l - 2.0F * f;
        if (!(f1 <= 0.0F) && !(f2 <= 0.0F)) {
            float f4 = i + f;
            float f5 = j + f;
            float f6 = i;
            float f7 = j;
            float f3 = k;
            Render2DUtil.drawGradientRoundedEx(f4, f5, f1, f2, 0.0F, 1.0F, f6, f7, f3, l, getFillGradient(), getAlphaMultiplier());
        }
    }

    private static void drawGradientBox(int i, int j, int k, int l, float f, GradientSetting gradientsetting) {
        float f5 = i;
        float f6 = j;
        float f7 = k;
        float f9 = i;
        float f10 = j;
        float f1 = k;
        Render2DUtil.drawGradientRoundedEx(f5, f6, f7, f, 0.0F, 1.0F, f9, f10, f1, l, gradientsetting, 1.0F);
        f5 = i;
        f6 = j + l - f;
        f7 = k;
        f9 = i;
        f10 = j;
        float f2 = k;
        Render2DUtil.drawGradientRoundedEx(f5, f6, f7, f, 0.0F, 1.0F, f9, f10, f2, l, gradientsetting, 1.0F);
        f5 = i;
        f6 = j + f;
        float f8 = l - 2.0F * f;
        f9 = i;
        f10 = j;
        float f3 = k;
        Render2DUtil.drawGradientRoundedEx(f5, f6, f, f8, 0.0F, 1.0F, f9, f10, f3, l, gradientsetting, 1.0F);
        f5 = i + k - f;
        f6 = j + f;
        f8 = l - 2.0F * f;
        f9 = i;
        f10 = j;
        float f4 = k;
        Render2DUtil.drawGradientRoundedEx(f5, f6, f, f8, 0.0F, 1.0F, f9, f10, f4, l, gradientsetting, 1.0F);
    }

    private static boolean isThemeGradientEnabled() {
        return (Boolean)ClientSettings.syncVisuals.getValue();
    }

    private static void drawLine(float f, float f1, float f2, float f3, float f4, Color color) {
        Render2DUtil.drawRoundedRect(f, f1, f2, f4, 0.0F, 0.0F, color);
        float f5 = f1 + f3 - f4;
        Render2DUtil.drawRoundedRect(f, f5, f2, f4, 0.0F, 0.0F, color);
        float f6 = f1 + f4;
        Render2DUtil.drawRoundedRect(f, f6, f4, f3 - 2.0F * f4, 0.0F, 0.0F, color);
        float f8 = f + f2 - f4;
        float f7 = f1 + f4;
        Render2DUtil.drawRoundedRect(f8, f7, f4, f3 - 2.0F * f4, 0.0F, 0.0F, color);
    }

    public void drawBox2D(int i, int j, int k, int l, float f) {
        Color color = Color.BLACK;
        float f4 = i - 1;
        float f5 = j - 1;
        float f2 = k + 2;
        drawLine(f4, f5, f2, l + 2, 1.0F, color);
        drawGradientBox(i, j, k, l, f, getOutlineGradient());
        float f1 = Math.max(1.0F, f);
        if (k - 2.0F * f1 > 1.0F && l - 2.0F * f1 > 1.0F) {
            f4 = i + f1;
            f5 = j + f1;
            float f3 = k - 2.0F * f1;
            drawLine(f4, f5, f3, l - 2.0F * f1, 1.0F, color);
        }

        if (fill.isEnabled()) {
            this.drawBoxOutline(i, j, k, l, f1 + 1.0F);
        }
    }

    public static float getAlphaMultiplier() {
        return isThemeGradientEnabled() ? 0.5F : 1.0F;
    }

    @Override
    public void onWorldRender(RenderAPI.RenderContext rendercontext) {
        if (this.isEnabled()) {
            float f = SystemUtil.getTickDelta();

            for (Entity entity : TargetManager.getTargetList(true)) {
                if (entity instanceof LivingEntity livingentity) {
                    if (this.tracers.isEnabled()) {
                        this.drawTracer(livingentity, f);
                    }

                    if (mode.is("Box")) {
                        Render3DUtil.drawGradientBox(
                            new Matrix4f(),
                            livingentity.getLerpedPos(f),
                            getFillGradient(),
                            getOutlineGradient(),
                            getAlphaMultiplier(),
                            0.3F,
                            livingentity.getHeight()
                        );
                    }
                }
            }
        }
    }
}
