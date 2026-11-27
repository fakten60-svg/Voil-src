package lol.lwes.voil.module.render;

import java.awt.Color;
import java.util.ArrayList;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.config.GuiTheme;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.ModeSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.render.Render2DUtil;
import lol.lwes.voil.util.target.TargetManager;
import lol.lwes.voil.util.world.EntityUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector4d;

public class NameTags extends Module {
    public static BooleanSetting renderItems = new BooleanSetting("RenderItems", true, "Renders nametags with items");
    private static BooleanSetting renderPotionEffects = new BooleanSetting("Render Potion Effects", false, "Renders active potion effects");
    private static ModeSetting style = new ModeSetting("Style", "Blur", "Style for nametags", new String[]{"Solid", "Blur"});
    private static BooleanSetting itemBackground = new BooleanSetting("Item Background", true, "Renders a background for items");
    public static BooleanSetting renderName = new BooleanSetting("RenderName", true, "Renders base nametags");
    private static BooleanSetting border = new BooleanSetting("Border", true, "Renders a border around the nametag");

    public NameTags() {
        super("NameTags", "Renders nametags", Category.Render);
        Setting[] asetting = new Setting[]{renderItems, renderPotionEffects, style, itemBackground, renderName, border};
        this.addSettings(asetting);
        Setting.dependSetting(border, itemBackground);
        Setting.dependSetting(border, "Solid", style);
    }

    @Override
    public void onRender2D() {
        if (mc.world != null && mc.player != null) {
            for (Entity entity : TargetManager.getTargetList(true)) {
                if (entity instanceof LivingEntity livingentity) {
                    Vec3d[] avec3d = EntityUtil.getBoxCorners(livingentity);
                    Vector4d vector4d = null;

                    for (Vec3d vec3d : avec3d) {
                        Vec3d vec3d1 = EntityUtil.worldToScreen(vec3d);
                        if (vec3d1.z > 0.0 && vec3d1.z < 1.0) {
                            if (vector4d == null) {
                                vector4d = new Vector4d(vec3d1.x, vec3d1.y, vec3d1.x, vec3d1.y);
                            } else {
                                vector4d.x = Math.min(vec3d1.x, vector4d.x);
                                vector4d.y = Math.min(vec3d1.y, vector4d.y);
                                vector4d.z = Math.max(vec3d1.x, vector4d.z);
                                vector4d.w = Math.max(vec3d1.y, vector4d.w);
                            }
                        }
                    }

                    if (vector4d != null) {
                        String s = livingentity.getName().getString();
                        double d0 = mc.player.squaredDistanceTo(livingentity);
                        float f32 = (float)Math.sqrt(d0);
                        String s3 = String.format("%.1fm", f32);
                        VoilClient.fonts.getSelectedFont().getWidth(s);
                        VoilClient.fonts.getSelectedFont().getWidth(s3);
                        float f = VoilClient.fonts.getSelectedFont().getLineHeight() + 4.0F;
                        float f1 = (float)((vector4d.x + vector4d.z) / 2.0);
                        float f2 = (float)vector4d.y - 14.0F - 2.0F;
                        float f3 = f2 + (14.0F - f) / 2.0F;
                        if (renderName.isEnabled()) {
                            if (style.is("Blur")) {
                                float f4 = livingentity.getHealth();
                                float f5 = livingentity.getMaxHealth();
                                String s1 = String.format("%.1f", f4);
                                String s2 = String.format("%.1fm", f32);
                                float f6 = VoilClient.fonts.getSelectedFont().getWidth(s, 11.0F);
                                float f7 = VoilClient.fonts.getSelectedFont().getWidth(s1, 9.0F);
                                float f8 = VoilClient.fonts.getSelectedFont().getWidth(s2, 9.0F);
                                float f9 = 5.0F;
                                float f10 = 8.0F;
                                float f11 = f10 + f6 + f9 + f7 + f9 + f8 + f10;
                                float f12 = 18.0F;
                                float f13 = f1 - f11 / 2.0F;
                                float f14 = (float)vector4d.y - f12 - 3.0F;
                                float f15 = Math.max(0.0F, Math.min(f4 / f5, 1.0F));
                                Color color;
                                if (f4 <= 0.0F) {
                                    color = new Color(200, 0, 0);
                                } else if (f4 <= 7.0F) {
                                    color = new Color(255, 85, 85);
                                } else if (f4 <= 15.0F) {
                                    color = new Color(255, 211, 67);
                                } else {
                                    color = new Color(85, 255, 85);
                                }

                                Render2DUtil.drawBlurSimple(f13, f14, f11, f12, 5.0F, 8.0F);
                                float f16 = f14 + f12 - 2.0F - 2.0F;
                                float f17 = f13 + 4.0F;
                                float f18 = f11 - 8.0F;
                                Render2DUtil.drawRoundedRect(f17, f16, f18, 2.0F, 1.0F, 1.0F, new Color(255, 255, 255, 20));
                                if (f15 > 0.0F) {
                                    Render2DUtil.drawRoundedRect(f17, f16, f18 * f15, 2.0F, 1.0F, 1.0F, color);
                                }

                                float f19 = f14 + (f12 - 2.0F - 2.0F - VoilClient.fonts.getSelectedFont().getLineHeight()) / 2.0F;
                                float f20 = f13 + f10;
                                VoilClient.fonts.getSelectedFont().renderWithShadow(s, f20, f19, 11.0F, -1);
                                f20 += f6 + f9;
                                VoilClient.fonts.getSelectedFont().renderWithShadow(s1, f20, f19 + 1.0F, 9.0F, color.getRGB());
                                f20 += f7 + f9;
                                VoilClient.fonts.getSelectedFont().renderWithShadow(s2, f20, f19 + 1.0F, 9.0F, new Color(180, 180, 180, 180).getRGB());
                            } else if (style.is("Solid")) {
                                float f33 = livingentity.getHealth();
                                String s4 = String.format("%.1f", f33);
                                float f36 = VoilClient.fonts.getSelectedFont().getWidth(s);
                                float f37 = VoilClient.fonts.getSelectedFont().getWidth(s4);
                                float f39 = 8.0F;
                                float f41 = 6.0F;
                                float f43 = f36 + f37 + f39 + f41;
                                float f44 = 0.5F;
                                float f45 = 0.65F;
                                float f47 = f1 - f43 / 2.0F;
                                Color color2;
                                if (f33 <= 0.0F) {
                                    color2 = new Color(200, 0, 0);
                                } else if (f33 <= 7.0F) {
                                    color2 = new Color(255, 85, 85);
                                } else if (f33 <= 15.0F) {
                                    color2 = new Color(255, 211, 67);
                                } else {
                                    color2 = new Color(85, 255, 85);
                                }

                                Render2DUtil.drawRoundedRect(f47, f2, f43, 14.0F, 0.0F, 1.0F, new Color(0.0F, 0.0F, 0.0F, f45));
                                Color color1 = GuiTheme.accentColor1;
                                float f21 = f44;
                                float f22 = 0.0F;
                                float f23 = 0.0F;
                                float f24 = 14.0F;
                                float f25 = f43;
                                float f26 = f2;
                                float f27 = f47;
                                Render2DUtil.drawOutline(f27, f26, f25, f24, f23, f22, f21, color1);
                                VoilClient.fonts.getSelectedFont().renderWithShadow(s, f47 + 3.0F, f3 + 2.0F, 11.0F, -1);
                                VoilClient.fonts.getSelectedFont().renderWithShadow(s4, f47 + 3.0F + f36 + f39, f3 + 2.0F, 11.0F, color2.getRGB());
                            }
                        }

                        if (renderItems.isEnabled()) {
                            float f34 = 0.75F;
                            float f35 = f2 - 16.0F;
                            ArrayList<ItemStack> arraylist = new ArrayList<>();

                            for (EquipmentSlot equipmentslot : EquipmentSlot.values()) {
                                ItemStack itemstack = livingentity.getEquippedStack(equipmentslot);
                                if (!itemstack.isEmpty()) {
                                    arraylist.add(itemstack);
                                }
                            }

                            float f38 = 18.0F;
                            float f40 = arraylist.size() * f38;
                            float f42 = f1 - f40 / 2.0F + 3.0F;

                            for (ItemStack itemstack1 : arraylist) {
                                if (itemBackground.isEnabled() && style.is("Blur")) {
                                    float f46 = 16.0F;
                                    float f48 = 16.0F * f34 + 4.0F;
                                    float f49 = f42 - 3.0F;
                                    float f28 = f35 - 2.0F;
                                    float f29 = f49;
                                    Render2DUtil.drawBlurRect(f29, f28, f46, f48, 6.0F, 6.0F);
                                } else if (itemBackground.isEnabled() && style.is("Solid")) {
                                    float f50 = f42 - 3.0F;
                                    float f30 = f35 - 2.0F;
                                    Render2DUtil.drawRoundedRect(f50, f30, 16.0F, 16.0F * f34 + 4.0F, 0.0F, 1.0F, new Color(0.0F, 0.0F, 0.0F, 0.65F));
                                    if (border.isEnabled()) {
                                        f50 = f42 - 3.0F;
                                        float f31 = f35 - 2.0F;
                                        Render2DUtil.drawOutline(f50, f31, 16.0F, 16.0F * f34 + 4.0F, 0.0F, 1.0F, 0.5F, GuiTheme.accentColor1);
                                    }
                                }

                                Render2DUtil.drawItem(itemstack1, f42, f35, f34, true);
                                f42 += f38;
                            }
                        }
                    }
                }
            }
        }
    }
}
