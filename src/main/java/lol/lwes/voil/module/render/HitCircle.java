package lol.lwes.voil.module.render;

import dev.prestigeclient.renderlib.impl.api.RenderAPI;
import dev.prestigeclient.renderlib.impl.render.mesh.GlBufferMeshBuilder;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import lol.lwes.voil.config.ClientSettings;
import lol.lwes.voil.config.GuiTheme;
import lol.lwes.voil.event.AttackEntityPostEvent;
import lol.lwes.voil.event.EventHandler;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.ColorSetting;
import lol.lwes.voil.setting.ModeSetting;
import lol.lwes.voil.setting.NumberSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.SystemUtil;
import lol.lwes.voil.util.render.ColorUtil;
import lol.lwes.voil.util.render.Render3DUtil;
import lol.lwes.voil.util.render.RenderManager;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3d;

public class HitCircle extends Module {
    private PlayerEntity target;
    private long lastHitTime;
    ModeSetting mode;
    NumberSetting radius;
    NumberSetting speed;
    NumberSetting segments;
    BooleanSetting multiTarget;
    ModeSetting colorMode;
    ColorSetting colorOne;
    ColorSetting colorTwo;
    ColorSetting colorThree;
    ColorSetting colorFour;
    private final List<PlayerEntity> hits;
    private float fade;

    public HitCircle() {
        super("HitCircle", "Highlights your hit circle", Category.Render);
        this.mode = new ModeSetting("Mode", "Circle", "What shapes to use when rendering", new String[]{"Squares", "Circle"});
        this.radius = new NumberSetting("Radius", 0.5, 2.0, 1.0, 0.1, "Radius of the circle");
        this.speed = new NumberSetting("Speed", 0.5, 2.0, 0.5, 0.1, "Oscillation / spin speed");
        this.segments = new NumberSetting("Segments", 3.0, 128.0, 48.0, 1.0, "Segments for the circle");
        this.multiTarget = new BooleanSetting("MultiTarget", false, "Allow selecting multiple targets");
        this.colorMode = new ModeSetting(
            "Color Mode",
            "Single",
            "How to color the circle/squares",
            new String[]{"Single", "Vertical", "FourColorVertical", "Around", "FourCorner", "Rainbow"}
        );
        Color color4 = new Color(255, 64, 64, 180);
        String s2 = "Base Color";
        Color color = color4;
        this.colorOne = new ColorSetting("Color One", color, s2);
        color4 = new Color(64, 255, 64, 180);
        String s3 = "Second color";
        Color color1 = color4;
        this.colorTwo = new ColorSetting("Color Two", color1, s3);
        color4 = new Color(64, 64, 255, 180);
        String s4 = "Third color";
        Color color2 = color4;
        this.colorThree = new ColorSetting("Color Three", color2, s4);
        color4 = new Color(255, 200, 64, 180);
        String s5 = "Fourth color";
        Color color3 = color4;
        this.colorFour = new ColorSetting("Color Four", color3, s5);
        this.hits = new ArrayList<>();
        this.fade = 1.0F;
        Setting[] asetting = new Setting[]{
            this.mode, this.radius, this.speed, this.segments, this.colorMode, this.colorOne, this.colorTwo, this.colorThree, this.colorFour, this.multiTarget
        };
        this.addSettings(asetting);
        ModeSetting modesetting = this.mode;
        Setting.dependSetting(this.segments, "Circle", modesetting);
        ColorSetting colorsetting = this.colorOne;
        BooleanSupplier booleansupplier = () -> (Boolean)ClientSettings.syncVisuals.getValue();
        Setting.dependSettingFalse(colorsetting, booleansupplier);
        colorsetting = this.colorTwo;
        BooleanSupplier booleansupplier1 = () -> (Boolean)ClientSettings.syncVisuals.getValue();
        Setting.dependSettingFalse(colorsetting, booleansupplier1);
        colorsetting = this.colorThree;
        BooleanSupplier booleansupplier2 = () -> (Boolean)ClientSettings.syncVisuals.getValue();
        Setting.dependSettingFalse(colorsetting, booleansupplier2);
        colorsetting = this.colorFour;
        BooleanSupplier booleansupplier3 = () -> (Boolean)ClientSettings.syncVisuals.getValue();
        Setting.dependSettingFalse(colorsetting, booleansupplier3);
    }

    private static Color scaleAlpha(Color color, float f) {
        int i = Math.max(0, Math.min(255, (int)(color.getAlpha() * f)));
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), i);
    }

    private static Color lerpColor(Color color, Color color1, float f) {
        float f1 = clampUnit(f);
        float f2 = color.getRed();
        int k1 = color1.getRed();
        int j1 = color.getRed();
        int i1 = k1;
        int i = (int)(f2 + (i1 - j1) * f1);
        f2 = color.getGreen();
        k1 = color1.getGreen();
        j1 = color.getGreen();
        i1 = k1;
        int j = (int)(f2 + (i1 - j1) * f1);
        f2 = color.getBlue();
        k1 = color1.getBlue();
        j1 = color.getBlue();
        i1 = k1;
        int k = (int)(f2 + (i1 - j1) * f1);
        f2 = color.getAlpha();
        k1 = color1.getAlpha();
        j1 = color.getAlpha();
        i1 = k1;
        int l = (int)(f2 + (i1 - j1) * f1);
        return new Color(i, j, k, l);
    }

    private static Color bilerpColor(Color color, Color color1, Color color2, Color color3, float f, float f1) {
        return lerpColor(lerpColor(color, color1, clampUnit(f)), lerpColor(color2, color3, clampUnit(f)), clampUnit(f1));
    }

    @Override
    public void onEnable() {
        this.hits.clear();
    }

    private static void addColoredVertex(BufferBuilder bufferbuilder, Matrix4f matrix4f, Vec3d vec3d, Color color) {
        bufferbuilder.vertex(matrix4f, (float)vec3d.x, (float)vec3d.y, (float)vec3d.z)
            .color(color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, color.getAlpha() / 255.0F);
    }

    private static Color applyTheme(Color color) {
        return (Boolean)ClientSettings.syncVisuals.getValue() ? GuiTheme.accentColor1 : color;
    }

    private static Color getFourColorGradient(float f, Color color, Color color1, Color color2, Color color3) {
        Color color4 = color3;
        float f2 = clampUnit(f);
        if (f2 < 0.33333334F) {
            float f4 = f2 * 3.0F;
            return lerpColor(color, color1, f4);
        } else if (f2 < 0.6666667F) {
            float f3 = (f2 - 0.33333334F) * 3.0F;
            return lerpColor(color1, color2, f3);
        } else {
            float f1 = (f2 - 0.6666667F) * 3.0F;
            return lerpColor(color2, color4, f1);
        }
    }

    private static Color shiftHue(Color color, float f) {
        float[] afloat = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        int i = Color.HSBtoRGB(wrapUnit(afloat[0] + f), afloat[1], afloat[2]);
        Color color1 = new Color(i);
        return new Color(color1.getRed(), color1.getGreen(), color1.getBlue(), color.getAlpha());
    }

    private static float wrapUnit(double d0) {
        double d1 = d0 % 1.0;
        return (float)(d1 < 0.0 ? d1 + 1.0 : d1);
    }

    private static Color hsbToColor(float f, float f1, float f2, int i) {
        int j = Color.HSBtoRGB(f, f1, f2);
        int k = j >> 16;
        int l = k & 0xFF;
        k = j >> 8;
        return new Color(l, k & 0xFF, j & 0xFF, i);
    }

    @EventHandler
    public void onAttack(AttackEntityPostEvent attackentitypostevent) {
        if (attackentitypostevent.getTarget() instanceof PlayerEntity playerentity) {
            if (this.multiTarget.isEnabled()) {
                if (!this.hits.contains(playerentity)) {
                    this.hits.add(playerentity);
                } else {
                    this.hits.remove(playerentity);
                }
            } else {
                this.hits.clear();
                this.hits.add(playerentity);
            }

            this.lastHitTime = System.currentTimeMillis();
        }
    }

    private static float clampUnit(float f) {
        return Math.max(0.0F, Math.min(1.0F, f));
    }

    @Override
    public void onWorldRender(RenderAPI.RenderContext renderapi$rendercontext) {
        if (!this.hits.isEmpty()) {
            double d0 = System.currentTimeMillis() / 1000.0;

            for (PlayerEntity playerentity : this.hits) {
                if (playerentity != null && !playerentity.isRemoved() && playerentity.isAlive()) {
                    float f = playerentity.getHeight() / 2.0F;
                    Vec3d vec3d = playerentity.getLerpedPos(SystemUtil.getTickDelta());
                    double d1 = Math.sin(d0 * Math.PI * this.speed.getFloat()) * f;
                    boolean flag = Math.cos(d0 * Math.PI * this.speed.getFloat()) < 0.0;
                    if (this.mode.is("Circle")) {
                        int i = Math.max(3, this.segments.getInt());
                        Vec3d[][] avec3d = new Vec3d[9][i + 1];
                        float f1 = flag ? 1.0F : -1.0F;
                        this.fade = this.fade + (f1 - this.fade) * 0.04F;

                        for (int k1 = 0; k1 <= 8; k1++) {
                            float f22 = k1 / 8.0F;
                            double d20 = this.fade * f22 * 0.3F;
                            double d21 = vec3d.y + 1.0 + d1 + d20;

                            for (int l1 = 0; l1 <= i; l1++) {
                                double d7 = (Math.PI * 2) * l1 / i;
                                double d9 = vec3d.x + this.radius.getFloat() * Math.cos(d7);
                                double d22 = vec3d.z + this.radius.getFloat() * Math.sin(d7);
                                avec3d[k1][l1] = new Vec3d(d9, d21, d22);
                            }
                        }

                        Color color9 = this.colorOne.getColor();
                        Color color11 = applyTheme(color9);
                        Color color12 = (Boolean)ClientSettings.syncVisuals.getValue() ? shiftHue(color11, 0.08F) : this.colorTwo.getColor();
                        Color color13 = (Boolean)ClientSettings.syncVisuals.getValue() ? shiftHue(color11, -0.08F) : this.colorThree.getColor();
                        Color color = (Boolean)ClientSettings.syncVisuals.getValue() ? shiftHue(color11, 0.16F) : this.colorFour.getColor();
                        GlBufferMeshBuilder glbuffermeshbuilder = RenderManager.bufferCollector.getBuffer(RenderManager.getTriangleNoCullLayer());
                        glbuffermeshbuilder.start();
                        int[] aint = new int[i * 8 * 6];
                        int i2 = 0;
                        byte b1 = 0;

                        for (int j2 = 1; j2 <= 8; j2++) {
                            float f23 = (j2 - 1) / 8.0F;
                            float f24 = j2 / 8.0F;
                            float f25 = 1.0F - f23;
                            float f26 = 1.0F - f24;

                            for (int k2 = 1; k2 <= i; k2++) {
                                float f27 = (float)(k2 - 1) / i;
                                float f4 = (float)k2 / i;
                                Color color5;
                                Color color6;
                                Color color14;
                                Color color15;
                                switch (this.colorMode.getMode()) {
                                    case "Single":
                                    default:
                                        color14 = scaleAlpha(color11, f25);
                                        color5 = scaleAlpha(color11, f25);
                                        color15 = scaleAlpha(color11, f26);
                                        color6 = scaleAlpha(color11, f26);
                                        break;
                                    case "Vertical":
                                        Color color18 = lerpColor(color11, color12, f23);
                                        Color color21 = lerpColor(color11, color12, f24);
                                        color14 = scaleAlpha(color18, f25);
                                        color5 = scaleAlpha(color18, f25);
                                        color15 = scaleAlpha(color21, f26);
                                        color6 = scaleAlpha(color21, f26);
                                        break;
                                    case "FourColorVertical":
                                        Color color17 = getFourColorGradient(f23, color11, color12, color13, color);
                                        Color color20 = getFourColorGradient(f24, color11, color12, color13, color);
                                        color14 = scaleAlpha(color17, f25);
                                        color5 = scaleAlpha(color17, f25);
                                        color15 = scaleAlpha(color20, f26);
                                        color6 = scaleAlpha(color20, f26);
                                        break;
                                    case "Around":
                                        float f29 = (float)(d0 * 0.1 % 1.0);
                                        float f30 = wrapUnit(f27 + f29);
                                        float f32 = wrapUnit(f4 + f29);
                                        Color color23 = lerpColor(color11, color12, f30);
                                        Color color24 = lerpColor(color11, color12, f32);
                                        color14 = scaleAlpha(color23, f25);
                                        color5 = scaleAlpha(color24, f25);
                                        color15 = scaleAlpha(color24, f26);
                                        color6 = scaleAlpha(color23, f26);
                                        break;
                                    case "FourCorner":
                                        Color color16 = bilerpColor(color11, color12, color13, color, f27, f23);
                                        Color color19 = bilerpColor(color11, color12, color13, color, f4, f23);
                                        Color color22 = bilerpColor(color11, color12, color13, color, f4, f24);
                                        Color color7 = bilerpColor(color11, color12, color13, color, f27, f24);
                                        color14 = scaleAlpha(color16, f25);
                                        color5 = scaleAlpha(color19, f25);
                                        color15 = scaleAlpha(color22, f26);
                                        color6 = scaleAlpha(color7, f26);
                                        break;
                                    case "Rainbow":
                                        float f28 = 0.15F;
                                        float f5 = wrapUnit(f27 + (float)d0 * f28);
                                        float f31 = wrapUnit(f4 + (float)d0 * f28);
                                        int k = (int)(255.0F * f25);
                                        float f9 = 1.0F;
                                        float f10 = 1.0F;
                                        float f11 = f5;
                                        color14 = hsbToColor(f11, f10, f9, k);
                                        int l = (int)(255.0F * f25);
                                        float f12 = 1.0F;
                                        float f13 = 1.0F;
                                        float f14 = f31;
                                        color5 = hsbToColor(f14, f13, f12, l);
                                        int i1 = (int)(255.0F * f26);
                                        float f15 = 1.0F;
                                        float f16 = 1.0F;
                                        float f17 = f31;
                                        color15 = hsbToColor(f17, f16, f15, i1);
                                        int j1 = (int)(255.0F * f26);
                                        float f18 = 1.0F;
                                        float f19 = 1.0F;
                                        float f20 = f5;
                                        color6 = hsbToColor(f20, f19, f18, j1);
                                }

                                Vec3d vec3d2 = avec3d[j2 - 1][k2 - 1];
                                Vec3d vec3d3 = avec3d[j2 - 1][k2];
                                Vec3d vec3d4 = avec3d[j2][k2];
                                Vec3d vec3d5 = avec3d[j2][k2 - 1];
                                Vector3d vector3d1 = RenderAPI.getRelativePos(renderapi$rendercontext, (float)vec3d2.x, (float)vec3d2.y, (float)vec3d2.z);
                                Vector3d vector3d2 = RenderAPI.getRelativePos(renderapi$rendercontext, (float)vec3d3.x, (float)vec3d3.y, (float)vec3d3.z);
                                Vector3d vector3d3 = RenderAPI.getRelativePos(renderapi$rendercontext, (float)vec3d4.x, (float)vec3d4.y, (float)vec3d4.z);
                                Vector3d vector3d = RenderAPI.getRelativePos(renderapi$rendercontext, (float)vec3d5.x, (float)vec3d5.y, (float)vec3d5.z);
                                glbuffermeshbuilder.pos(new Matrix4f(), (float)vector3d1.x, (float)vector3d1.y, (float)vector3d1.z)
                                    .color(ColorUtil.toArgb(color14));
                                glbuffermeshbuilder.pos(new Matrix4f(), (float)vector3d2.x, (float)vector3d2.y, (float)vector3d2.z)
                                    .color(ColorUtil.toArgb(color5));
                                glbuffermeshbuilder.pos(new Matrix4f(), (float)vector3d3.x, (float)vector3d3.y, (float)vector3d3.z)
                                    .color(ColorUtil.toArgb(color15));
                                glbuffermeshbuilder.pos(new Matrix4f(), (float)vector3d.x, (float)vector3d.y, (float)vector3d.z)
                                    .color(ColorUtil.toArgb(color6));
                                aint[i2++] = b1;
                                aint[i2++] = b1 + 1;
                                aint[i2++] = b1 + 2;
                                aint[i2++] = b1;
                                aint[i2++] = b1 + 2;
                                aint[i2++] = b1 + 3;
                                b1 += 4;
                            }
                        }

                        glbuffermeshbuilder.putIndices(aint).end();
                    } else if (this.mode.is("Squares")) {
                        double d2 = this.speed.getFloat();
                        double d3 = d0 * d2 * 2.0 * Math.PI;
                        float f2 = mc.player.getYaw();
                        float f3 = mc.player.getPitch();
                        double d4 = Math.toRadians(f2);
                        double d5 = Math.toRadians(f3);
                        double d6 = d5 * 1.8;
                        double d8 = Math.toRadians(Math.sin(d0 * 1.5) * 20.0);
                        Color color10 = this.colorOne.getColor();
                        Color color1 = applyTheme(color10);
                        Color color2 = (Boolean)ClientSettings.syncVisuals.getValue() ? shiftHue(color1, 0.08F) : this.colorTwo.getColor();
                        Color color3 = (Boolean)ClientSettings.syncVisuals.getValue() ? shiftHue(color1, -0.08F) : this.colorThree.getColor();
                        Color color4 = (Boolean)ClientSettings.syncVisuals.getValue() ? shiftHue(color1, 0.16F) : this.colorFour.getColor();

                        for (int j = 0; j < 10; j++) {
                            double d10 = (Math.PI * 2) * j / 10 + d3;
                            double d11 = this.radius.getFloat() * Math.cos(d10);
                            double d12 = 0.0;
                            double d13 = this.radius.getFloat() * Math.sin(d10);
                            double d14 = d12 * Math.cos(d6) - d13 * Math.sin(d6);
                            double d15 = d12 * Math.sin(d6) + d13 * Math.cos(d6);
                            double d16 = d11 * Math.cos(d8) - d14 * Math.sin(d8);
                            double d17 = d11 * Math.sin(d8) + d14 * Math.cos(d8);
                            double d18 = d16 * Math.cos(d4) - d15 * Math.sin(d4);
                            double d19 = d16 * Math.sin(d4) + d15 * Math.cos(d4);
                            Vec3d vec3d1 = new Vec3d(vec3d.x + d18, vec3d.y + 1.0 + d1 + d17, vec3d.z + d19);
                            float f6 = (float)((double)j / 10 % 1.0);
                            float f7 = (float)(0.5 + 0.5 * Math.tanh(d17));
                            Color color8;
                            switch (this.colorMode.getMode()) {
                                case "Vertical" -> color8 = lerpColor(color1, color2, clampUnit(f7));
                                case "Around" -> {
                                    float f33 = (float)(d0 * 0.1 % 1.0);
                                    color8 = lerpColor(color1, color2, wrapUnit(f6 + f33));
                                }
                                case "FourCorner" -> color8 = bilerpColor(color1, color2, color3, color4, clampUnit(f6), clampUnit(f7));
                                case "Rainbow" -> {
                                    float f8 = 0.15F;
                                    float f21 = wrapUnit(f6 + (float)d0 * f8);
                                    color8 = hsbToColor(f21, 1.0F, 1.0F, 220);
                                }
                                default -> color8 = color1;
                            }

                            Render3DUtil.drawPillar(new Matrix4f(), vec3d1, color8, 0.05F, 0.1F);
                        }
                    }
                }
            }
        }
    }
}
