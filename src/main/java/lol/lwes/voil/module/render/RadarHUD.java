package lol.lwes.voil.module.render;

import java.awt.Color;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.config.GuiTheme;
import lol.lwes.voil.config.HudLayout;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.ModeSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.render.ColorUtil;
import lol.lwes.voil.util.render.Render2DUtil;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

public class RadarHUD extends Module {
    private ModeSetting mode = new ModeSetting("Mode", "Rectangle", "Radar", new String[]{"Circle", "Rectangle"});
    private final int size = 80;

    public RadarHUD() {
        super("RadarHUD", "shows where other players are", Category.Render);
        Setting[] asetting = new Setting[]{this.mode};
        this.addSettings(asetting);
    }

    @Override
    public void onRender2D() {
        if (mc.player != null && mc.world != null) {
            int i = VoilClient.getHudLayout().getX(this);
            int j = VoilClient.getHudLayout().getY(this);
            if (this.mode.is("Rectangle")) {
                float f9 = i;
                float f1 = j;
                float f2 = f9;
                Render2DUtil.drawBlurRect(f2, f1, 80.0F, 80.0F, 6.0F, 14.0F);
                Render2DUtil.drawCircle((float)(i + 40.0), (float)(j + 40.0), 3.0F, 1.0F, Color.WHITE);
                VoilClient.getHudLayout().setSize(this, 80, 80);
            } else if (this.mode.is("Circle")) {
                int k = (int)VoilClient.fonts.getSelectedFont().getWidth("W ");
                int l = (int)VoilClient.fonts.getSelectedFont().getSize();
                i += k;
                j += l;
                Render2DUtil.drawBlurCircle(i, j, 60);
                int i1 = i - 60;
                float f8 = i1 + 61;
                float f3 = j + 61;
                Render2DUtil.drawRoundedRect(f8, f3, 16, 0.8F, 0.0F, 1.0F, GuiTheme.accentColor1);
                f8 = i + 61;
                i1 = j - 60;
                float f4 = i1 + 61;
                Render2DUtil.drawRoundedRect(f8, f4, 0.8F, 16, 0.0F, 1.0F, GuiTheme.accentColor1);
                i1 = i + 60;
                f8 = i1 + 42;
                float f5 = j + 61;
                Render2DUtil.drawRoundedRect(f8, f5, 16, 0.8F, 0.0F, 1.0F, GuiTheme.accentColor1);
                f8 = i + 61;
                i1 = j + 60;
                float f6 = i1 + 42;
                Render2DUtil.drawRoundedRect(f8, f6, 0.8F, 16, 0.0F, 1.0F, GuiTheme.accentColor1);
                i1 = i + 60;
                float f10 = i1 + -7 + 1;
                i1 = j + 60;
                Render2DUtil.drawPlayerHead(mc.player, f10, i1 + -7 + 1, 12, 60, Color.WHITE);
                i1 = i + 60;
                f8 = i1 + -6 + 1;
                i1 = j + 60;
                float f7 = i1 + -6 + 1;
                Color color = GuiTheme.accentColor2;
                Render2DUtil.drawRoundedRect(f8, f7, 10.0F, 10.0F, 7.0F, 8.0F, ColorUtil.withAlpha(color, 140));
                double d0 = i + 60;
                double d1 = j + 60;
                float f = mc.player.getYaw();
                double d2 = Math.toRadians(f);
                double d3 = d0 + -12.0 * Math.sin(d2);
                double d4 = d1 - -12.0 * Math.cos(d2);
                Render2DUtil.drawAxes(Color.white, d3, d4, 8.0F, 8.0F, 1.5F, f - 45.0F);
                f8 = i;
                f10 = j;
                float f11 = 60;
                Color color1 = GuiTheme.accentColor1;
                Render2DUtil.drawCircleOutline(f8, f10, f11, 10.0F, 1.0F, ColorUtil.withAlpha(color1, 190));
                Render2DUtil.drawCircleOutline(i, j, 60, 0.5F, 0.8F, GuiTheme.accentColor1);
                VoilClient.fonts
                    .getSelectedFont()
                    .render("N", (float)(d0 - VoilClient.fonts.getSelectedFont().getWidth("N") / 2.0) + 1.0F, (float)(d1 - 63 - 11), Color.white);
                VoilClient.fonts
                    .getSelectedFont()
                    .render("S", (float)(d0 - VoilClient.fonts.getSelectedFont().getWidth("S") / 2.0) + 1.0F, (float)(d1 + 63), Color.white);
                VoilClient.fonts.getSelectedFont().render("E", (float)(d0 + 63), (float)(d1 - 11 / 2.0), Color.white);
                VoilClient.fonts
                    .getSelectedFont()
                    .render("W", (float)(d0 - 63 - VoilClient.fonts.getSelectedFont().getWidth("W")), (float)(d1 - 11 / 2.0), Color.white);
                HudLayout hudlayout = VoilClient.getHudLayout();
                int j1 = (int)(k * 0.75);
                i1 = 63 + j1;
                int i2 = i1 + i1;
                int k1 = (63 + l) * 2;
                int l1 = i2;
                RadarHUD radarhud1 = this;
                hudlayout.setSize(radarhud1, l1, k1);
            }

            for (Entity entity : mc.world.getEntities()) {
                if (entity != mc.player && entity instanceof AbstractClientPlayerEntity) {
                    this.drawEntityDot(entity, Color.RED);
                }
            }
        }
    }

    private void drawEntityDot(Entity entity, Color color) {
        if (mc.player != null) {
            int i = VoilClient.getHudLayout().getX(this);
            int j = VoilClient.getHudLayout().getY(this);
            Vec3d vec3d = mc.player.getEntityPos();
            Vec3d vec3d1 = entity.getEntityPos();
            double d0 = this.mode.is("Circle") ? 60.0 : 40.0;
            double d1 = i + d0;
            double d2 = j + d0;
            double d3 = d0 / 60.0;
            double d4 = vec3d1.x - vec3d.x;
            double d5 = vec3d1.z - vec3d.z;
            double d6 = d4 * d3;
            double d7 = d5 * d3;
            double d8 = d1 + d6;
            double d9 = d2 + d7;
            double d10 = Math.sqrt(d6 * d6 + d7 * d7);
            float f = 1.0F;
            if (this.mode.is("Circle")) {
                if (d10 > d0) {
                    double d11 = d0 / d10;
                    d8 = d1 + d6 * d11;
                    d9 = d2 + d7 * d11;
                }

                f = (float)Math.max(0.0, 1.0 - (d10 - d0) / 20.0);
            } else if (this.mode.is("Rectangle")) {
                d8 = Math.max(i, Math.min(i + 80, d8));
                d9 = Math.max(j, Math.min(j + 80, d9));
                double d14 = Math.max(0.0, Math.max(i - d8, d8 - (i + 80)));
                double d12 = Math.max(0.0, Math.max(j - d9, d9 - (j + 80)));
                double d13 = Math.sqrt(d14 * d14 + d12 * d12);
                f = (float)Math.max(0.0, 1.0 - d13 / 20.0);
            }

            int k = Math.min(255, Math.max(0, (int)(color.getAlpha() * f)));
            new Color(color.getRed(), color.getGreen(), color.getBlue(), k);
            Render2DUtil.drawPlayerHead((AbstractClientPlayerEntity)entity, (float)d8, (float)d9, 10, 0, Color.WHITE);
        }
    }
}
