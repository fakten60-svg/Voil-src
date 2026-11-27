package lol.lwes.voil.module.render;

import dev.prestigeclient.renderlib.impl.api.RenderAPI;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import lol.lwes.voil.config.ClientSettings;
import lol.lwes.voil.config.GuiTheme;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.module.render.FloatingText;
import lol.lwes.voil.setting.ColorSetting;
import lol.lwes.voil.setting.ModeSetting;
import lol.lwes.voil.setting.NumberSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.SystemUtil;
import lol.lwes.voil.util.render.ColorUtil;
import lol.lwes.voil.util.render.Render3DUtil;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class Breadcrumbs extends Module {
    ModeSetting mode;
    NumberSetting lifetime;
    ColorSetting color;
    private Vec3d lastPoint;
    private final float lineSize;
    private final List<FloatingText> points;

    public Breadcrumbs() {
        super("Breadcrumbs", "Renders a path where you walk", Category.Render);
        this.mode = new ModeSetting("Mode", "Square", "Mode of the Breadcrumbs", new String[]{"Line", "Square", "Orb"});
        this.lifetime = new NumberSetting("Lifetime", 1.0, 10.0, 3.0, 1.0, "Lifetime of the breadcrumbs in seconds");
        Color color1 = new Color(255, 255, 255, 255);
        String s2 = "Color of the breadcrumbs";
        Color colorx = color1;
        this.color = new ColorSetting("Color", colorx, s2);
        this.lastPoint = null;
        this.lineSize = 0.6F;
        this.points = new ArrayList<>();
        Setting[] asetting = new Setting[]{this.mode, this.lifetime, this.color};
        this.addSettings(asetting);
    }

    @Override
    public void onTick() {
        if (mc.player != null) {
            Vec3d vec3d = mc.player.getLerpedPos(SystemUtil.getTickDelta());
            String s = this.mode.getMode();
            if (!s.equals("Square") && !s.equals("Orb")) {
                this.addPoint(vec3d, s);
            } else if (this.lastPoint == null || vec3d.distanceTo(this.lastPoint) >= 0.6F) {
                this.addPoint(vec3d, s);
                this.lastPoint = vec3d;
            }

            this.points.forEach(floatingtext -> {
                NumberSetting numbersetting = this.lifetime;
                floatingtext.updateAlpha(numbersetting);
            });
            this.points.removeIf(floatingtext -> {
                NumberSetting numbersetting = this.lifetime;
                return floatingtext.isExpired(numbersetting);
            });
        }
    }

    public void addPoint(Vec3d vec3d, String s) {
        this.points.add(new FloatingText(vec3d, s));
    }

    @Override
    public void onWorldRender(RenderAPI.RenderContext rendercontext) {
        for (int i = 0; i < this.points.size(); i++) {
            FloatingText floatingtext = (FloatingText)this.points.get(i);
            Color color1;
            if ((Boolean)ClientSettings.syncVisuals.getValue()) {
                color1 = GuiTheme.accentColor1;
            } else {
                color1 = this.color.getColor();
            }

            int j = color1.getRGB();
            Color color2 = new Color(j & 0xFFFFFF | (int)(floatingtext.alpha * 255.0F) << 24, true);
            switch (floatingtext.text) {
                case "Square" -> Render3DUtil.drawPillar(new Matrix4f(), floatingtext.pos, color2, 0.1F, 0.2F);
                case "Line" -> {
                    if (i > 0) {
                        Render3DUtil.drawLine(((FloatingText)this.points.get(i - 1)).pos, floatingtext.pos, color2);
                    }
                }
                case "Orb" -> {
                    Matrix4f matrix4f1 = new Matrix4f();
                    double d3 = floatingtext.pos.x;
                    double d4 = floatingtext.pos.y;
                    double d5 = floatingtext.pos.z;
                    Color colorx = ColorUtil.withAlpha(color2, 250);
                    float f = 0.1F;
                    double d0 = d5;
                    double d1 = d4;
                    double d2 = d3;
                    Matrix4f matrix4f = matrix4f1;
                    Render3DUtil.drawFilledCube(matrix4f, d2, d1, d0, f, colorx);
                }
                default -> {
                }
            }
        }
    }
}
