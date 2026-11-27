package lol.lwes.voil.module.render;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.ColorSetting;
import lol.lwes.voil.setting.ModeSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.render.GuiScale;
import lol.lwes.voil.util.render.Render2DUtil;
import lol.lwes.voil.util.target.TargetManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class Arrows extends Module {
    ColorSetting color;
    ModeSetting mode;
    private final HashMap<Integer, Float> arrowStates;
    private static final String DYNAMIC_COLOR_MODE = "Dynamic Color";

    public Arrows() {
        super("Arrows", "Draws arrows where players are", Category.Render);
        Color color1 = new Color(125, 83, 213);
        String s2 = "Color of the arrows";
        Color colorx = color1;
        this.color = new ColorSetting("Color", colorx, s2);
        this.mode = new ModeSetting("Mode", "Static Color", "Changes color of arrow", new String[]{"Dynamic Color", "Static Color"});
        this.arrowStates = new HashMap<>();
        Setting[] asetting = new Setting[]{this.color, this.mode};
        this.addSettings(asetting);
    }

    @Override
    public void onRender2D() {
        if (mc.player != null && mc.world != null) {
            float f = GuiScale.getScaledWidth() / 2.0F;
            float f1 = GuiScale.getScaledHeight() / 2.0F;
            Vec3d vec3d = mc.player.getEntityPos();
            float f2 = mc.player.getYaw();
            ArrayList<Entity> arraylist = TargetManager.getTargetList(true);
            HashSet<Integer> hashset = new HashSet<>();

            for (Entity entity : arraylist) {
                if (entity != mc.player && entity.isAlive() && entity instanceof PlayerEntity) {
                    Vec3d vec3d1 = entity.getEntityPos();
                    double d0 = vec3d1.x - vec3d.x;
                    double d1 = vec3d1.z - vec3d.z;
                    if (d0 != 0.0 || d1 != 0.0) {
                        double d2 = MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(-d0, d1)) - f2);
                        double d3 = Math.toRadians(d2);
                        int i = entity.getId();
                        hashset.add(i);
                        float f3 = Math.min(1.0F, this.arrowStates.getOrDefault(i, 0.0F) + 0.12F);
                        this.arrowStates.put(i, f3);
                        float f4 = f3 * f3 * (3.0F - 2.0F * f3);
                        float f5 = (float)MathHelper.clamp((Math.abs(d2) - 6.0) / 14.0, 0.0, 1.0);
                        float f6 = f4 * f5;
                        if (!(f6 <= 0.01F)) {
                            float f7 = f + (float)(36.0 * Math.sin(d3));
                            float f8 = f1 - (float)(36.0 * Math.cos(d3));
                            Color colorx = this.color.getColor();
                            if (this.mode.is(DYNAMIC_COLOR_MODE)) {
                                double d4 = mc.player.distanceTo(entity);
                                colorx = this.getGradientColor(d4, 3.0, 50.0);
                            }

                            int j = Math.round(colorx.getAlpha() * f6);
                            Color color1 = new Color(colorx.getRed(), colorx.getGreen(), colorx.getBlue(), j);
                            float f9 = (float)d2 - 90.0F;
                            Render2DUtil.drawArrow(f7 - 11.0F, f8 - 11.0F, 22.0F, 22.0F, 9.0F, 0.8F, 1.0F, 38.0F, f9, 0.6F, color1);
                        }
                    }
                }
            }

            this.arrowStates.keySet().retainAll(hashset);
        }
    }

    private Color getGradientColor(double d0, double d1, double d2) {
        float f = (float)MathHelper.clamp((d0 - d1) / (d2 - d1), 0.0, 1.0);
        int i = MathHelper.lerp(f, 0, 255);
        int j = MathHelper.lerp(1.0F - f, 0, 255);
        return new Color(i, j, 0, 255);
    }
}
