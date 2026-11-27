package lol.lwes.voil.gui.clickgui.component;

import java.awt.Color;
import java.util.List;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.config.GuiTheme;
import lol.lwes.voil.gui.clickgui.ClickGuiComponent;
import lol.lwes.voil.gui.clickgui.ClickGuiScreen;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.KeybindSetting;
import lol.lwes.voil.util.font.IconGlyphs;
import lol.lwes.voil.util.input.KeyUtil;
import lol.lwes.voil.util.render.Render2DUtil;
import net.minecraft.client.util.math.MatrixStack;

public class KeybindSettingComponent extends SettingComponent {
    private final KeybindSetting setting;
    private final Module module;
    private static final Color LISTENING_COLOR = new Color(122, 124, 134);

    public KeybindSettingComponent(KeybindSetting keybindsetting, ClickGuiScreen clickguiscreen, Module module) {
        super(keybindsetting, clickguiscreen);
        this.setting = keybindsetting;
        this.module = module;
        this.setPosition(0.0F, 0.0F);
    }

    public void mouseClicked(double d0, double d1, int i) {
        String s = this.getBindText();
        float f = this.getTextWidth(s);
        float f1 = this.fromInnerRight(f);
        float f2 = this.getControlY();
        if (!this.setting.isListening() && i == 0 && ClickGuiComponent.isPointInRect(f1, f2, f1 + 16.0F, f2 + 16.0F, d0, d1)) {
            this.setting.setHold(!this.setting.isHold());
            super.mouseClicked();
        } else {
            if (ClickGuiComponent.isPointInRect(f1, f2, f1 + f, f2 + 14.0F, d0, d1)) {
                if (!this.setting.isListening()) {
                    if (i == 0) {
                        this.setting.setListening(true);
                    }
                } else {
                    this.setting.setKey(0);
                    this.setting.setListening(false);
                    if (this.setting.isToggleable()) {
                        this.module.setKeyCode(0);
                    }
                }
            }

            super.mouseClicked();
        }
    }

    public void render(int i, int j) {
        float f = this.getInnerLeft();
        float f1 = this.getTextY();
        VoilClient.fonts.getSelectedFont().render(this.setting.getName(), f, f1, 10.0F, GuiTheme.normalTextColor.getRGB());
        Module modulex = this.module;
        List list = KeybindSetting.findConflicts(modulex, this.setting);
        boolean flag = false;
        if (!list.isEmpty()) {
            float f2 = VoilClient.fonts.getSelectedFont().getWidth(this.setting.getName(), 10.0F);
            float f3 = VoilClient.fonts.getIcons().getWidth(IconGlyphs.iconE07B(), 12.0F);
            float f4 = VoilClient.fonts.getIcons().getLineHeight(12.0F);
            float f5 = f + f2 + 4.0F;
            float f6 = f1 + (VoilClient.fonts.getSelectedFont().getLineHeight(10.0F) - f4) / 2.0F;
            float f32 = f5 + f3;
            float f33 = f6 + f4;
            double d5 = i;
            double d0 = j;
            flag = ClickGuiComponent.isPointInRect(f5, f6, f32, f33, d5, d0);
            Color color = flag ? new Color(255, 120, 70) : new Color(217, 96, 60);
            VoilClient.fonts.getIcons().render(IconGlyphs.iconE07B(), f5, f6, 12.0F, color.getRGB());
        }

        String s3 = this.getBindText();
        float f25 = this.getTextWidth(s3);
        float f26 = 16.0F;
        float f27 = this.fromInnerRight(f25);
        float f28 = this.getControlY();
        Color color1 = GuiTheme.modeSettingFill;
        float f14 = 1.0F;
        float f15 = GuiTheme.radiusTopRight;
        float f16 = f26;
        float f17 = f25;
        float f18 = f28;
        float f19 = f27;
        Render2DUtil.drawRoundedRect(f19, f18, f17, f16, f15, f14, color1);
        String s4 = this.setting.isHold() ? IconGlyphs.iconE1D7() : IconGlyphs.iconE124();
        float f7 = VoilClient.fonts.getIcons().getWidth(s4, 11.0F);
        float f8 = VoilClient.fonts.getIcons().getLineHeight(11.0F);
        float f9 = f27 + (16.0F - f7) / 2.0F;
        float f10 = f28 + (f26 - f8) / 2.0F;
        VoilClient.fonts.getIcons().render(s4, f9, f10, 11.0F, LISTENING_COLOR.getRGB());
        float f11 = VoilClient.fonts.getSelectedFont().getWidth(s3, 10.0F);
        float f12 = VoilClient.fonts.getSelectedFont().getLineHeight(10.0F);
        float f13 = f27 + 16.0F + (f25 - 16.0F - f11) / 2.0F;
        VoilClient.fonts.getSelectedFont().render(s3, f13, f28 + (f26 - f12) / 2.0F, 10.0F, GuiTheme.normalTextColor.getRGB());
        float f31 = (f9 + f7 + f13) / 2.0F - 1.0F / 2.0F;
        float f20 = f28 + 3.0F;
        Render2DUtil.drawRoundedRect(f31, f20, 1.0F, f26 - 6.0F, 0.5F, 1.0F, GuiTheme.outlineColor);
        float f29 = f27 + 16.0F;
        float f30 = f28 + f26;
        double d4 = i;
        double d1 = j;
        boolean flag1 = ClickGuiComponent.isPointInRect(f27, f28, f29, f30, d4, d1);
        if (flag) {
            String s1 = KeybindSetting.formatConflicts(list);
            this.setTooltip(s1, i, j);
        } else if (flag1) {
            String s = this.setting.isHold() ? "Hold" : "Press";
            this.setTooltip(s, i, j);
        } else {
            f29 = f27 + f25;
            f30 = f28 + f26;
            d4 = i;
            double d2 = j;
            double d3 = d4;
            float f21 = f30;
            float f22 = f29;
            float f23 = f28;
            float f24 = f27;
            if (ClickGuiComponent.isPointInRect(f24, f23, f22, f21, d3, d2)) {
                String s2 = this.setting.getDescription().toString();
                this.setTooltip(s2, i, j);
            }
        }
    }

    public void keyPressed(int i) {
        if (!this.setting.isListening()) {
            super.keyReleased();
        } else {
            if (i == 256) {
                this.setting.setKey(0);
                this.setting.setListening(false);
                if (this.setting.isToggleable()) {
                    this.module.setKeyCode(0);
                }
            } else if (i == 259) {
                this.setting.setKey(this.setting.getDefaultKey());
                this.setting.setListening(false);
                if (this.setting.isToggleable()) {
                    this.module.setKeyCode(this.setting.getDefaultKey());
                }
            } else {
                this.setting.setKey(i);
                this.setting.setListening(false);
                if (this.setting.isToggleable()) {
                    this.module.setKeyCode(i);
                }
            }
        }
    }

    private float getTextWidth(String s) {
        return 16.0F + VoilClient.fonts.getSelectedFont().getWidth(s, 10.0F) + 14.0F;
    }

    private String getBindText() {
        String s;
        if (this.setting.isListening()) {
            s = "Listening...";
        } else {
            int i = this.setting.getKey();
            s = KeyUtil.getKeyName(i);
        }

        return s;
    }

    @Override
    public void mouseClicked2(double d0, double d1, int i) {
        this.mouseClicked(d0, d1, i);
    }

    @Override
    public void render2(MatrixStack matrixstack, int i, int j, float f) {
        this.render(i, j);
    }

    @Override
    public void keyReleased2(int i, int j, int k) {
        this.keyPressed(i);
    }
}
