package lol.lwes.voil.module.render;

import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import dev.prestigeclient.renderlib.impl.api.RenderAPI;
import java.awt.Color;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.ColorSetting;
import lol.lwes.voil.setting.ModeSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.SystemUtil;
import lol.lwes.voil.util.render.ColorUtil;
import lol.lwes.voil.util.render.Render2DUtil;
import lol.lwes.voil.util.render.Render3DUtil;
import lol.lwes.voil.util.world.EntityUtil;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector4d;

public class ItemESP extends Module {
    private final ModeSetting highlightMode;
    private final BooleanSetting fill;
    private final ColorSetting highlightColor;

    public ItemESP() {
        super("ItemESP", "Highlights items in the world", Category.Render);
        this.highlightMode = new ModeSetting("Highlight Mode", "Outline", "Mode for highlighting", new String[]{"Outline", "Box"});
        this.fill = new BooleanSetting("Fill", true, "Fills the highlighted area");
        Color color1 = new Color(120, 24, 175);
        String s2 = "Color for highlighting items";
        Color color = color1;
        this.highlightColor = new ColorSetting("Highlight Color", color, s2);
        Setting[] asetting = new Setting[]{this.highlightMode, this.fill, this.highlightColor};
        this.addSettings(asetting);
        ModeSetting modesetting = this.highlightMode;
        Setting.dependSetting(this.fill, "Outline", modesetting);
    }

    @Override
    public void onRender2D() {
        if (mc.world != null && mc.player != null) {
            for (Entity entity : mc.world.getEntities()) {
                if (entity instanceof ItemEntity) {
                    Vec3d[] avec3d = EntityUtil.getBoxCorners(entity);
                    Vector4d vector4d = null;

                    for (Vec3d vec3d : avec3d) {
                        Vec3d vec3d1 = EntityUtil.worldToScreen(vec3d);
                        if (vec3d1.z > 0.0 && vec3d1.z < 1.0) {
                            if (vector4d == null) {
                                vector4d = new Vector4d(vec3d1.x, vec3d1.y, vec3d1.x, vec3d1.y);
                            } else {
                                vector4d.x = Math.min(vector4d.x, vec3d1.x);
                                vector4d.y = Math.min(vector4d.y, vec3d1.y);
                                vector4d.z = Math.max(vector4d.z, vec3d1.x);
                                vector4d.w = Math.max(vector4d.w, vec3d1.y);
                            }
                        }
                    }

                    if (vector4d != null) {
                        float f = (float)vector4d.x;
                        float f1 = (float)vector4d.y;
                        float f2 = (float)vector4d.z;
                        float f3 = (float)vector4d.w;
                        Render2DUtil.setupBlend();
                        BufferBuilder bufferbuilder = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
                        if (this.highlightMode.is("Outline")) {
                            int k1 = (int)f;
                            int l1 = (int)f1;
                            int i2 = (int)(f2 - f);
                            int i = (int)(f3 - f1);
                            int j = this.highlightColor.getColor().getRGB();
                            int k = i;
                            int l = i2;
                            int i1 = l1;
                            int j1 = k1;
                            this.drawEntityBox(j1, i1, l, k, j, 1.2F);
                        }

                        bufferbuilder.endNullable();
                        Render2DUtil.disableBlend();
                    }
                }
            }
        }
    }

    private void drawEntityBox(int i, int j, int k, int l, int i1, float f) {
        new Matrix4f();
        float f1 = i;
        float f7 = j;
        float f8 = i + k;
        int k1 = (int)f;
        int j1 = j;
        Render2DUtil.drawRectArgb(f1, f7, f8, j1 + k1, i1);
        float f2 = i;
        int l1 = j + l;
        k1 = (int)f;
        j1 = l1;
        Render2DUtil.drawRectArgb(f2, j1 - k1, i + k, j + l, i1);
        float f3 = i;
        f7 = j;
        k1 = (int)f;
        j1 = i;
        Render2DUtil.drawRectArgb(f3, f7, j1 + k1, j + l, i1);
        int j2 = i + k;
        k1 = (int)f;
        j1 = j2;
        float f4 = j1 - k1;
        Render2DUtil.drawRectArgb(f4, j, i + k, j + l, i1);
        if (this.fill.isEnabled()) {
            k1 = (int)f;
            j1 = i;
            float f5 = j1 + k1;
            f7 = j + (int)f;
            int i2 = i + k;
            k1 = (int)f;
            j1 = i2;
            f8 = j1 - k1;
            int k2 = j + l;
            k1 = (int)f;
            j1 = k2;
            float f6 = j1 - k1;
            Color color = Color.black;
            Render2DUtil.drawRectArgb(f5, f7, f8, f6, ColorUtil.withAlpha(color, 145).getRGB());
        }
    }

    @Override
    public void onWorldRender(RenderAPI.RenderContext rendercontext) {
        if (mc.world != null && mc.player != null) {
            for (Entity entity : mc.world.getEntities()) {
                if (entity instanceof ItemEntity && this.highlightMode.is("Box")) {
                    Vec3d vec3d = entity.getLerpedPos(SystemUtil.getTickDelta());
                    Matrix4f matrix4f = new Matrix4f();
                    Vec3d vec3d1 = vec3d.add(0.0, 0.1, 0.0);
                    Color color = this.highlightColor.getColor();
                    Render3DUtil.drawPillar(matrix4f, vec3d1, ColorUtil.withAlpha(color, 145), 0.2F, 0.4F);
                }
            }
        }
    }
}
