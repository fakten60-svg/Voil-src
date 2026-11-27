package lol.lwes.voil.gui.clickgui.component;

import java.awt.Color;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.config.GuiTheme;
import lol.lwes.voil.gui.clickgui.ClickGuiComponent;
import lol.lwes.voil.gui.clickgui.ClickGuiScreen;
import lol.lwes.voil.setting.NumberSetting;
import lol.lwes.voil.util.render.Render2DUtil;
import net.minecraft.client.util.math.MatrixStack;

public class NumberSettingComponent extends SettingComponent {
    private final NumberSetting setting;
    private float animatedValue;
    private boolean focused;
    private boolean valueBoxHovered;
    private String inputText = "";

    public NumberSettingComponent(NumberSetting numbersetting, ClickGuiScreen clickguiscreen) {
        super(numbersetting, clickguiscreen);
        this.setting = numbersetting;
        this.setPosition(0.0F, 0.0F);
    }

    private float getTrackWidth() {
        return Math.max(20.0F, this.getTrackEnd() - this.getTrackStart());
    }

    public void mouseClicked(double d0, double d1, int i) {
        float f = (float)this.setting.getMin();
        float f1 = (float)this.setting.getMax();
        float f2 = Double.valueOf(this.setting.getValue()).floatValue();
        f2 = Math.max(f, Math.min(f1, f2));
        String s = this.formatValue(f2);
        float f3 = VoilClient.fonts.getSelectedFont().getWidth(s, 10.0F) + 2.0F;
        float f4 = this.getInnerRight() - f3;
        float f5 = this.getInnerLeft() + this.getLabelWidth() + 8.0F;
        float f6 = f4 - 8.0F;
        float f7 = this.getTrackY();
        float f8 = this.getTrackHeight();
        if (ClickGuiComponent.isPointInRect(f5 - 3.0F, f7 - 5.0F, f6 + 3.0F, f7 + f8 + 5.0F, d0, d1)) {
            this.focused = true;
        }

        if (i == 2) {
            float f9 = VoilClient.fonts.getSelectedFont().getLineHeight();
            float f14 = f4 - 3.0F;
            float f15 = this.getTextY() - 1.0F;
            float f16 = this.getInnerRight() + 2.0F;
            float f10 = this.getTextY() + f9 + 1.0F;
            float f11 = f16;
            float f12 = f15;
            float f13 = f14;
            if (ClickGuiComponent.isPointInRect(f13, f12, f11, f10, d0, d1)) {
                this.valueBoxHovered = true;
                this.inputText = "";
            }
        }
    }

    public void render(MatrixStack matrixstack, int i, int j) {
        float f6;
        float f7;
        int k;
        float f9;
        float f10;
        float f11;
        float f12;
        float f13;
        float f14;
        float f15;
        float f16;
        float f29;
        boolean flag2;
        label67: {
            f6 = (float)this.setting.getMin();
            f7 = (float)this.setting.getMax();
            float f8 = Double.valueOf(this.setting.getValue()).floatValue();
            f29 = Math.max(f6, Math.min(f7, f8));
            k = !this.focused && !this.valueBoxHovered ? GuiTheme.moduleText.getRGB() : -1;
            VoilClient.fonts.getSelectedFont().render(this.setting.getName(), this.getInnerLeft(), this.getTextY(), 10.0F, k);
            f9 = VoilClient.fonts.getSelectedFont().getLineHeight();
            f10 = this.getInnerRight() - 24.0F;
            f11 = this.getTextY() + f9 / 2.0F - 14.0F / 2.0F;
            f12 = this.getInnerLeft() + this.getLabelWidth() + 8.0F;
            f13 = f10 - 8.0F;
            f14 = Math.max(20.0F, f13 - f12);
            f15 = this.getTrackHeight();
            f16 = this.getTrackY();
            float f17 = (f29 - f6) / (f7 - f6) * f14;
            this.animatedValue = this.animatedValue + (f17 - this.animatedValue) * 0.05F;
            Render2DUtil.drawRoundedRect(f12, f16, f14, f15, 2.0F, 1.0F, GuiTheme.sliderSettingBg);
            if (!this.focused && !this.valueBoxHovered) {
                float f41 = f12 - 3.0F;
                float f42 = f16 - 5.0F;
                float f43 = f13 + 3.0F;
                float f44 = f16 + f15 + 5.0F;
                double d6 = i;
                double d3 = j;
                if (ClickGuiComponent.isPointInRect(f41, f42, f43, f44, d6, d3)) {
                    flag2 = true;
                    break label67;
                }
            }

            flag2 = false;
        }

        boolean flag = flag2;
        float f18 = 0.0F;
        float f19 = 0.0F;
        float f20 = 0.0F;
        if (flag) {
            float f21 = Math.max(f12, Math.min(f13, i));
            float f22 = (f21 - f12) / f14;
            float f23 = f6 + (f7 - f6) * f22;
            float f24 = (float)this.setting.getIncrement();
            float f30 = f24 > 0.0F ? Math.round(f23 / f24) * f24 : f23;
            f18 = Math.max(f6, Math.min(f7, f30));
            f19 = (f18 - f6) / (f7 - f6);
            f20 = f12 + f19 * f14;
        }

        Render2DUtil.drawRectGradient(
            f12, f16, this.animatedValue, f15, 2.0F, 1.0F, GuiTheme.accentColor2, GuiTheme.accentColor2, GuiTheme.accentColor1, GuiTheme.accentColor1
        );
        if (flag) {
            float f31 = f19 * f14;
            float f33 = Math.min(this.animatedValue, f31);
            float f35 = Math.abs(f31 - this.animatedValue);
            if (f35 >= 1.0F) {
                Render2DUtil.drawRoundedRect(f12 + f33, f16, f35, f15, 0.0F, 1.0F, Color.darkGray.darker());
            }
        }

        float f32 = f16 + f15 / 2.0F - 3.5F;
        float f34 = f12 + this.animatedValue;
        boolean flag1 = flag && Math.abs(f20 - f34) >= 1.5F;
        if (flag1) {
            Render2DUtil.drawCircle(f20 - 2.0F, f32, 3.5F, 1.0F, new Color(60, 60, 60, 230));
        }

        Render2DUtil.drawCircle(f34 - 2.0F, f32, 3.5F, 1.0F, Color.white);
        String s3;
        if (this.valueBoxHovered) {
            String s = this.inputText;
            s3 = s + "|";
            k = -1;
        } else if (flag1) {
            s3 = this.formatValue(f18);
            k = new Color(170, 170, 170, 220).getRGB();
        } else {
            s3 = this.formatValue(f29);
        }

        float f25 = VoilClient.fonts.getSelectedFont().getWidth(s3, 10.0F);
        float f26 = f10 + (24.0F - f25) / 2.0F;
        float f27 = f11 + (14.0F - f9) / 2.0F;
        Render2DUtil.drawRoundedRect(f10, f11, 24.0F, 14.0F, GuiTheme.radiusBottomRight, 1.0F, GuiTheme.modeSettingFill);
        Color color = this.valueBoxHovered ? GuiTheme.accentColor1 : GuiTheme.modeSettingBg;
        float f = GuiTheme.borderThickness;
        float f1 = 0.4F;
        float f2 = GuiTheme.radiusBottomRight;
        float f3 = 14.0F;
        float f4 = 24.0F;
        float f5 = f11;
        Render2DUtil.drawOutline(f10, f5, f4, f3, f2, f1, f, color);
        VoilClient.fonts.getSelectedFont().render(s3, f26, f27, 10.0F, k);
        if (this.focused) {
            double d0 = i - f12;
            d0 = Math.min(Math.max(0.0, d0), f14);
            double d1 = d0 / f14;
            double d2 = f6 + (f7 - f6) * d1;
            this.setting.setValue(d2);
        }

        float f36 = this.getLabelWidth();
        float f28 = VoilClient.fonts.getSelectedFont().getLineHeight();
        float f37 = this.getInnerLeft();
        float f38 = this.getTextY();
        float f39 = this.getInnerLeft() + f36;
        float f40 = this.getTextY() + f28;
        double d5 = i;
        double d4 = j;
        if (ClickGuiComponent.isPointInRect(f37, f38, f39, f40, d5, d4)) {
            String s2 = this.setting.getDescription().toString();
            NumberSettingComponent numbersettingcomponent1 = this;
            numbersettingcomponent1.setTooltip(s2, i, j);
        }
    }

    @Override
    public void charTyped(char chr, int modifiers) {
        if (this.valueBoxHovered) {
            if (Character.isDigit(chr) || chr == '.' && !this.inputText.contains(".")) {
                String s = this.inputText;
                this.inputText = s + chr;
            }
        }
    }

    @Override
    public void mouseReleased() {
        this.focused = false;
    }

    public void keyPressed(int i) {
        if (this.valueBoxHovered) {
            if (i == 257) {
                try {
                    double d0 = Double.parseDouble(this.inputText);
                    d0 = Math.max(0.0, Math.min(10000.0, d0));
                    if (d0 < this.setting.getMin()) {
                        this.setting.setMin(d0);
                    }

                    if (d0 > this.setting.getMax()) {
                        this.setting.setMax(d0);
                    }

                    this.setting.setValue(d0);
                } catch (NumberFormatException numberformatexception) {
                }

                this.valueBoxHovered = false;
                this.inputText = "";
            } else if (i == 256) {
                this.valueBoxHovered = false;
                this.inputText = "";
            } else if (i == 259 && !this.inputText.isEmpty()) {
                String s = this.inputText;
                int j = this.inputText.length();
                this.inputText = s.substring(0, j - 1);
            }
        }
    }

    private String formatValue(double d0) {
        return this.setting.format(d0);
    }

    private float getTrackHeight() {
        return 4.0F;
    }

    private float getTrackY() {
        return this.width + this.getRowHeight() / 2.0F - this.getTrackHeight() / 2.0F + 3.0F;
    }

    private float getValueBoxWidth(boolean flag) {
        String s3;
        if (this.valueBoxHovered && flag) {
            String s1 = this.inputText;
            s3 = s1 + "|";
        } else {
            s3 = this.formatValue(Double.valueOf(this.setting.getValue()).doubleValue());
        }

        String s = s3;
        return VoilClient.fonts.getSelectedFont().getWidth(s, 10.0F) + 2.0F;
    }

    private float getTrackEnd() {
        return this.getInnerRight() - this.getValueBoxWidth(true) - 8.0F;
    }

    private float getLabelWidth() {
        return VoilClient.fonts.getSelectedFont().getWidth(this.setting.getName(), 10.0F);
    }

    private float getTrackStart() {
        return this.getInnerLeft() + this.getLabelWidth() + 8.0F;
    }

    @Override
    public void mouseClicked2(double d0, double d1, int i) {
        this.mouseClicked(d0, d1, i);
    }

    @Override
    public void render2(MatrixStack matrixstack, int i, int j, float f) {
        this.render(matrixstack, i, j);
    }

    @Override
    public void mouseReleased2(double d0, double d1, int i) {
        this.mouseReleased();
    }

    @Override
    public void keyReleased2(int i, int j, int k) {
        this.keyPressed(i);
    }
}
