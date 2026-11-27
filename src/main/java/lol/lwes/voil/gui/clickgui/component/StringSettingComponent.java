package lol.lwes.voil.gui.clickgui.component;

import lol.lwes.voil.VoilClient;
import lol.lwes.voil.config.GuiTheme;
import lol.lwes.voil.gui.clickgui.ClickGuiComponent;
import lol.lwes.voil.gui.clickgui.ClickGuiScreen;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.StringSetting;
import lol.lwes.voil.util.render.Render2DUtil;
import net.minecraft.client.util.math.MatrixStack;

public class StringSettingComponent extends SettingComponent {
    private final float boxHeight = 16.0F;
    private final float boxWidth = 60.0F;
    private final float animationSpeed = 0.2F;
    private final StringSetting setting;
    private String text;
    private boolean focused;
    private float cursorBlink;
    private static StringSettingComponent activeField;

    public StringSettingComponent(ClickGuiScreen clickguiscreen, StringSetting stringsetting, float f, float f1, Module module) {
        super(stringsetting, clickguiscreen);
        this.setting = stringsetting;
        this.text = stringsetting.getValue();
        this.setPosition(0.0F, 0.0F);
    }

    public String getValue() {
        return this.text;
    }

    public void mouseClicked(double d0, double d1) {
        float f = this.calculateBoxWidth(this.getDisplayText());
        float f1 = this.fromInnerRight(f);
        float f2 = this.getControlY();
        if (ClickGuiComponent.isPointInRect(f1, f2, f1 + f, f2 + 16.0F, d0, d1)) {
            if (activeField != null && activeField != this && activeField.focused) {
                activeField.submit();
            }

            activeField = this;
            this.focused = true;
        } else if (this.focused) {
            this.submit();
        }
    }

    public void render(int i, int j) {
        VoilClient.fonts.getSelectedFont().render(this.setting.getName(), this.getInnerLeft(), this.getTextY(), 10.0F, -1);
        if (!this.focused) {
            this.text = this.setting.getValue();
        }

        String s = this.getDisplayText();
        int k = this.text.isEmpty() && !this.focused ? GuiTheme.unfocusedTextColor.getRGB() : GuiTheme.normalTextColor.getRGB();
        float f = this.calculateBoxWidth(s);
        if (this.cursorBlink == 0.0F) {
            this.cursorBlink = f;
        } else {
            this.cursorBlink = this.cursorBlink + (f - this.cursorBlink) * 0.2F;
        }

        float f1 = this.cursorBlink;
        float f2 = this.fromInnerRight(f1);
        float f3 = this.getControlY();
        float f4 = f2 + (f1 - VoilClient.fonts.getSelectedFont().getWidth(s)) / 2.0F;
        float f7 = f2 + 1.0F;
        float f6 = f3 + 1.0F;
        Render2DUtil.drawRoundedRect(f7, f6, f1 - 2.0F, 14.0F, 6.0F, 1.0F, GuiTheme.modeSettingFill);
        Render2DUtil.drawOutline(f2, f3, f1, 16.0F, 6.0F, 0.3F, 0.6F, GuiTheme.modeSettingFill.brighter());
        float f5 = f3 + (16.0F - VoilClient.fonts.getSelectedFont().getLineHeight()) / 2.0F;
        VoilClient.fonts.getSelectedFont().render(s, f4, f5, 10.0F, k);
        float f8 = f2 + f1;
        float f9 = f3 + 16.0F;
        double d1 = i;
        double d0 = j;
        if (ClickGuiComponent.isPointInRect(f2, f3, f8, f9, d1, d0)) {
            String s1 = this.setting.getDescription().toString();
            this.setTooltip(s1, i, j);
        }
    }

    @Override
    public void charTyped(char chr, int modifiers) {
        if (this.focused) {
            if (this.isAllowedChar(chr)) {
                String s = this.text;
                this.text = s + chr;
            }
        }
    }

    public void keyPressed(int i, int j) {
        if (this.focused) {
            boolean flag = (j & 2) != 0;
            if (flag && i == 67) {
                VoilClient.mc.keyboard.setClipboard(this.text);
            } else if (flag && i == 88) {
                VoilClient.mc.keyboard.setClipboard(this.text);
                this.unfocus();
            } else if (i == 257 || i == 335) {
                this.focused = false;
                String s3 = this.text;
                this.setting.setValue(s3);
            } else if (flag && i == 86) {
                String s4 = this.text;
                String s1 = VoilClient.mc.keyboard.getClipboard();
                String s = s4;
                this.text = s + s1;
            } else {
                if (i == 259 && !this.text.isEmpty()) {
                    this.text = this.text.substring(0, this.text.length() - 1);
                }
            }
        }
    }

    private float calculateBoxWidth(String s) {
        return Math.max(60.0F, VoilClient.fonts.getSelectedFont().getWidth(s) + 8.0F);
    }

    private String getDisplayText() {
        String s = this.setting.getMask();
        if (s != null) {
            return s;
        }

        if (this.text.isEmpty() && !this.focused) {
            return "Enter text...";
        }

        String s3;
        if (this.focused) {
            String s1 = this.text;
            s3 = s1 + "|";
        } else {
            s3 = this.text;
        }

        return s3;
    }

    private void submit() {
        this.focused = false;
        String s = this.text;
        this.setting.setValue(s);
    }

    private boolean isAllowedChar(int i) {
        return "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890!\"$%&/()=? _-:.,;+*/#'".indexOf(i) >= 0;
    }

    public void setFocused(boolean flag) {
        this.focused = flag;
    }

    public void unfocus() {
        this.text = "";
    }

    public boolean isFocused() {
        return this.focused;
    }

    @Override
    public void mouseClicked2(double d0, double d1, int i) {
        this.mouseClicked(d0, d1);
    }

    @Override
    public void render2(MatrixStack matrixstack, int i, int j, float f) {
        this.render(i, j);
    }

    @Override
    public void keyReleased2(int i, int k, int j) {
        this.keyPressed(i, j);
    }
}
