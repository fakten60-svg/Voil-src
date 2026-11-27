package lol.lwes.voil.module.render;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.config.GuiTheme;
import lol.lwes.voil.config.HudLayout;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.module.ModuleManager;
import lol.lwes.voil.setting.ModeSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.font.IconGlyphs;
import lol.lwes.voil.util.input.KeyUtil;
import lol.lwes.voil.util.render.Render2DUtil;

public class HotkeyHUD extends Module {
    ModeSetting mode = new ModeSetting("Mode", "Separated", "How to render keybinds", new String[]{"Merged", "Separated"});

    public HotkeyHUD() {
        super("HotkeyHUD", "Shows the hotkey of the module", Category.Render);
        Setting[] asetting = new Setting[]{this.mode};
        this.addSettings(asetting);
    }

    @Override
    public void onRender2D() {
        if (mc.world != null && this.isEnabled()) {
            int i = VoilClient.getHudLayout().getX(this);
            int j = VoilClient.getHudLayout().getY(this);
            String s = IconGlyphs.iconE284();
            float f = VoilClient.fonts.getIcons().getWidth(s);
            float f1 = VoilClient.fonts.getSelectedFont().getWidth("Keybinds");
            float f2 = f + f1 + 20.0F;
            float f28 = i;
            float f13 = j;
            float f14 = f28;
            Render2DUtil.drawBlurRect(f14, f13, f2, 20.0F, 6.0F, 14.0F);
            float f3 = f + f1 + 4.0F;
            float f4 = i + (f2 - f3) / 2.0F;
            VoilClient.fonts.getIcons().render(s, f4, j + 3, 13.0F, -1);
            VoilClient.fonts.getSelectedFont().render("Keybinds", f4 + f + 4.0F, j + 1.5F, -1);
            int k = j + 23;
            List<Module> list = ModuleManager.INSTANCE.getModules();
            if (this.mode.is("Separated")) {
                float f5 = 0.0F;

                for (Module module : list) {
                    if (module.getKeyCode() != 0) {
                        String s1 = module.getName();
                        int l1 = module.getKeyCode();
                        String s2 = KeyUtil.getKeyName(l1);
                        Category category = module.getCategory();
                        String s3 = IconGlyphs.getCategoryIcon(category);
                        float f6 = VoilClient.fonts.getIcons().getWidth(s3) + 4.0F;
                        float f7 = VoilClient.fonts.getSelectedFont().getWidth(s1);
                        float f8 = VoilClient.fonts.getSelectedFont().getWidth(s2);
                        float f9 = f6 + f7 + f8 + 15.0F;
                        float f10 = 15.0F;
                        Color color = module.isEnabled() ? GuiTheme.accentColor2 : Color.white;
                        if (f9 > f5) {
                            f5 = f9;
                        }

                        f28 = i;
                        float f15 = k;
                        float f16 = f28;
                        Render2DUtil.drawBlurRect(f16, f15, f9, f10, 6.0F, 14.0F);
                        VoilClient.fonts.getIcons().render(s3, i + 4, k + 2, color);
                        VoilClient.fonts.getSelectedFont().render(s1, i + f6 + 4.0F, k, color);
                        VoilClient.fonts.getGreyscale().render(s2, i + f9 - f8 - 6.0F, k, color);
                        k = (int)(k + (f10 + 3));
                    }
                }

                HudLayout hudlayout = VoilClient.getHudLayout();
                int l2 = (int)f5;
                int l = k - j;
                int i1 = l2;
                HotkeyHUD hotkeyhud1 = this;
                hudlayout.setSize(hotkeyhud1, i1, l);
            } else if (this.mode.is("Merged")) {
                LinkedHashMap<Category, List<Module>> linkedhashmap = new LinkedHashMap<>();

                for (Module module1 : list) {
                    if (module1.getKeyCode() != 0) {
                        linkedhashmap.computeIfAbsent(module1.getCategory(), category3 -> new ArrayList<>()).add(module1);
                    }
                }

                float f19 = 0.0F;

                for (Entry<Category, List<Module>> entry : linkedhashmap.entrySet()) {
                    Category category1 = entry.getKey();
                    List<Module> list1 = entry.getValue();
                    String s7 = IconGlyphs.getCategoryIcon(category1);
                    float f20 = VoilClient.fonts.getIcons().getWidth(s7);
                    float f22 = 0.0F;

                    for (Module module2 : list1) {
                        String s9 = module2.getName();
                        int i2 = module2.getKeyCode();
                        String s4 = KeyUtil.getKeyName(i2);
                        float f11 = VoilClient.fonts.getSelectedFont().getWidth(s9) + VoilClient.fonts.getGreyscale().getWidth(s4) + 10.0F;
                        f22 = Math.max(f22, f11);
                    }

                    float f24 = f20 + f22 + 18.0F;
                    f19 = Math.max(f19, f24);
                }

                for (Entry<Category, List<Module>> entry1 : linkedhashmap.entrySet()) {
                    Category category2 = entry1.getKey();
                    List<Module> list2 = entry1.getValue();
                    String s8 = IconGlyphs.getCategoryIcon(category2);
                    float f21 = VoilClient.fonts.getIcons().getWidth(s8);
                    float f23 = 15.0F;
                    float f25 = list2.size() * (f23 + 3) - 3 + 6.0F;
                    f28 = i;
                    float f17 = k;
                    float f18 = f28;
                    Render2DUtil.drawBlurRect(f18, f17, f19, f25, 6.0F, 14.0F);
                    float f26 = k + (f25 - 13.0F) / 2.0F;
                    VoilClient.fonts.getIcons().render(s8, i + 4, f26, 13.0F, -1);
                    float f27 = k + 3;

                    for (Module module3 : list2) {
                        String s5 = module3.getName().toString();
                        int j2 = module3.getKeyCode();
                        String s6 = KeyUtil.getKeyName(j2);
                        float f12 = VoilClient.fonts.getGreyscale().getWidth(s6);
                        Color color1 = module3.isEnabled() ? GuiTheme.accentColor2 : Color.white;
                        VoilClient.fonts.getSelectedFont().render(s5, i + f21 + 8.0F, f27, color1);
                        VoilClient.fonts.getGreyscale().render(s6, i + f19 - f12 - 6.0F, f27, color1);
                        f27 += f23 + 3;
                    }

                    k = (int)(k + (f25 + 3 + 3.0F));
                }

                HudLayout hudlayout1 = VoilClient.getHudLayout();
                int k2 = (int)f19;
                int j1 = k - j;
                int k1 = k2;
                HotkeyHUD hotkeyhud2 = this;
                hudlayout1.setSize(hotkeyhud2, k1, j1);
            }
        }
    }
}
