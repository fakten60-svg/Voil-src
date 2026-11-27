package lol.lwes.voil.gui.clickgui.component;

import dev.prestigeclient.renderlib.api.font.MSDFFontAtlas;
import java.awt.Color;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.config.GuiTheme;
import lol.lwes.voil.gui.clickgui.ClickGuiComponent;
import lol.lwes.voil.gui.clickgui.ClickGuiScreen;
import lol.lwes.voil.setting.RangeSetting;
import lol.lwes.voil.util.render.Render2DUtil;
import net.minecraft.client.util.math.MatrixStack;

public class RangeSettingComponent extends SettingComponent {
    private final RangeSetting setting;
    private float animatedMin;
    private float animatedMax;
    private boolean draggingMin;
    private boolean draggingMax;
    private boolean editingMin;
    private boolean editingMax;
    private String inputText = "";

    public RangeSettingComponent(RangeSetting rangesetting, ClickGuiScreen clickguiscreen) {
        super(rangesetting, clickguiscreen);
        this.setting = rangesetting;
        this.setPosition(0.0F, 0.0F);
    }

    public void mouseClicked(double d0, double d1, int i) {
        String s = this.formatValue(this.setting.getMin());
        String s1 = this.formatValue(this.setting.getMax());
        float f = Math.max(VoilClient.fonts.getSelectedFont().getWidth(s, 10.0F), VoilClient.fonts.getSelectedFont().getWidth(s1, 10.0F));
        float f1 = VoilClient.fonts.getSelectedFont().getWidth(" to ", 10.0F);
        float f2 = f * 2.0F + f1 + 8.0F;
        float f3 = VoilClient.fonts.getSelectedFont().getLineHeight();
        float f4 = this.getInnerRight() - f2;
        float f5 = this.getTextY() + f3 / 2.0F - 14.0F / 2.0F;
        float f6 = this.getInnerLeft() + this.getLabelWidth() + 8.0F;
        float f7 = f4 - 8.0F;
        float f8 = Math.max(20.0F, f7 - f6);
        float f9 = this.getTrackY();
        float f10 = f6 + this.valueToX(this.setting.getValueMin(), f8);
        float f11 = f6 + this.valueToX(this.setting.getValueMax(), f8);
        boolean flag = ClickGuiComponent.isPointInRect(f10 - 6.0F, f9 - 6.0F, f10 + 6.0F, f9 + 6.0F, d0, d1);
        boolean flag1 = ClickGuiComponent.isPointInRect(f11 - 6.0F, f9 - 6.0F, f11 + 6.0F, f9 + 6.0F, d0, d1);
        boolean flag2 = this.setting.getValueMax() == this.setting.getValueMin() && this.setting.getValueMin() != this.setting.getMin();
        if (!this.draggingMin && !this.draggingMax) {
            if (flag1 && !flag2) {
                this.draggingMax = true;
            } else if (flag) {
                this.draggingMin = true;
            }
        }

        if (i == 2 && ClickGuiComponent.isPointInRect(f4, f5, f4 + f2, f5 + 14.0F, d0, d1)) {
            String s2 = this.formatValue(this.setting.getValueMin());
            String s7 = this.formatValue(this.setting.getValueMax());
            String s4 = s2;
            String s3 = s4 + " to " + s7;
            float f12 = VoilClient.fonts.getSelectedFont().getWidth(s3, 10.0F);
            float f13 = f4 + (f2 - f12) / 2.0F;
            MSDFFontAtlas msdffontatlas = VoilClient.fonts.getSelectedFont();
            float f14 = msdffontatlas.getWidth(s2 + " to ", 10.0F);
            if (d0 < f13 + f14) {
                this.editingMin = true;
                this.editingMax = false;
            } else {
                this.editingMax = true;
                this.editingMin = false;
            }

            this.inputText = "";
        }
    }

    public void render(int i, int j) {
        int k = !this.draggingMin && !this.draggingMax && !this.editingMin && !this.editingMax ? GuiTheme.moduleText.getRGB() : -1;
        VoilClient.fonts.getSelectedFont().render(this.setting.getName(), this.getInnerLeft(), this.getTextY(), 10.0F, k);
        String s = this.formatValue(this.setting.getMin());
        String s1 = this.formatValue(this.setting.getMax());
        float f6 = Math.max(VoilClient.fonts.getSelectedFont().getWidth(s, 10.0F), VoilClient.fonts.getSelectedFont().getWidth(s1, 10.0F));
        float f7 = VoilClient.fonts.getSelectedFont().getWidth(" to ", 10.0F);
        float f8 = f6 * 2.0F + f7 + 8.0F;
        float f9 = VoilClient.fonts.getSelectedFont().getLineHeight();
        float f10 = this.getInnerRight() - f8;
        float f11 = this.getTextY() + f9 / 2.0F - 14.0F / 2.0F;
        float f12 = this.getInnerLeft() + this.getLabelWidth() + 8.0F;
        float f13 = f10 - 8.0F;
        float f14 = Math.max(20.0F, f13 - f12);
        float f15 = this.getTrackHeight();
        float f16 = this.getTrackY();
        float f17 = this.valueToX(this.setting.getValueMin(), f14);
        float f18 = this.valueToX(this.setting.getValueMax(), f14);
        this.animatedMin = this.animatedMin + (f17 - this.animatedMin) * 0.05F;
        this.animatedMax = this.animatedMax + (f18 - this.animatedMax) * 0.05F;
        Render2DUtil.drawRoundedRect(f12, f16, f14, f15, 2.0F, 1.0F, GuiTheme.sliderSettingBg);
        float f19 = f12 + this.animatedMin;
        float f20 = f12 + this.animatedMax - f19;
        if (f20 < 0.0F) {
            f19 += f20;
            f20 = -f20;
        }

        Render2DUtil.drawRectGradient(
            f19, f16, f20, f15, 2.0F, 1.0F, GuiTheme.accentColor2, GuiTheme.accentColor2, GuiTheme.accentColor1, GuiTheme.accentColor1
        );
        float f21 = f16 + f15 / 2.0F - 3.5F;
        Render2DUtil.drawCircle(f12 + this.animatedMin - 2.0F, f21, 3.5F, 1.0F, Color.white);
        Render2DUtil.drawCircle(f12 + this.animatedMax - 2.0F, f21, 3.5F, 1.0F, Color.white);
        String s11;
        if (this.editingMin) {
            String s5 = this.inputText;
            s11 = s5 + "|";
        } else {
            double d0 = this.setting.getValueMin();
            s11 = this.formatValue(d0);
        }

        String s2 = s11;
        if (this.editingMax) {
            String s7 = this.inputText;
            s11 = s7 + "|";
        } else {
            double d1 = this.setting.getValueMax();
            s11 = this.formatValue(d1);
        }

        String s3 = s11;
        String s4 = s2 + " to " + s3;
        float f22 = VoilClient.fonts.getSelectedFont().getWidth(s4, 10.0F);
        float f23 = f10 + (f8 - f22) / 2.0F;
        float f24 = f11 + (14.0F - f9) / 2.0F;
        Render2DUtil.drawRoundedRect(f10, f11, f8, 14.0F, GuiTheme.radiusBottomRight, 1.0F, GuiTheme.modeSettingFill);
        Color color = !this.editingMin && !this.editingMax ? GuiTheme.modeSettingBg : GuiTheme.accentColor1;
        float f = GuiTheme.borderThickness;
        float f1 = 0.4F;
        float f2 = GuiTheme.radiusBottomRight;
        float f3 = 14.0F;
        float f4 = f8;
        float f5 = f11;
        Render2DUtil.drawOutline(f10, f5, f4, f3, f2, f1, f, color);
        VoilClient.fonts.getSelectedFont().render(s4, f23, f24, 10.0F, k);
        if (this.draggingMin || this.draggingMax) {
            double d2 = i;
            this.dragHandle(d2, f12, f14);
        }

        float f25 = this.getLabelWidth();
        float f26 = VoilClient.fonts.getSelectedFont().getLineHeight();
        float f27 = this.getInnerLeft();
        float f28 = this.getTextY();
        float f29 = this.getInnerLeft() + f25;
        float f30 = this.getTextY() + f26;
        double d4 = i;
        double d3 = j;
        if (ClickGuiComponent.isPointInRect(f27, f28, f29, f30, d4, d3)) {
            String s10 = this.setting.getDescription().toString();
            this.setTooltip(s10, i, j);
        }
    }

    @Override
    public void charTyped(char chr, int modifiers) {
        if (this.editingMin || this.editingMax) {
            if (Character.isDigit(chr)) {
                String s = this.inputText;
                this.inputText = s + chr;
            }
        }
    }

    @Override
    public void mouseReleased() {
        this.draggingMin = false;
        this.draggingMax = false;
    }

    public void keyPressed(int i) {
        if (this.editingMin || this.editingMax) {
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

                    if (this.editingMin) {
                        if (d0 <= this.setting.getValueMax()) {
                            this.setting.setValueMin(d0);
                        }
                    } else if (d0 >= this.setting.getValueMin()) {
                        this.setting.setValueMax(d0);
                    }
                } catch (NumberFormatException numberformatexception) {
                }

                this.editingMin = false;
                this.editingMax = false;
                this.inputText = "";
            } else if (i == 256) {
                this.editingMin = false;
                this.editingMax = false;
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

    private float valueToX(double d0, float f) {
        return (float)((d0 - this.setting.getMin()) / (this.setting.getMax() - this.setting.getMin()) * f);
    }

    private float getLabelWidth() {
        return VoilClient.fonts.getSelectedFont().getWidth(this.setting.getName(), 10.0F);
    }

    private void dragHandle(double d0, float f, float f1) {
        double d1 = d0 - f;
        double d2 = Math.min(Math.max(0.0, d1 / f1), 1.0);
        double d3 = this.setting.getMin() + (this.setting.getMax() - this.setting.getMin()) * d2;
        if (this.setting.getIncrement() > 0.0) {
            d3 = Math.round(d3 / this.setting.getIncrement()) * this.setting.getIncrement();
        }

        d3 = Math.max(this.setting.getMin(), Math.min(this.setting.getMax(), d3));
        if (this.draggingMin) {
            if (d3 <= this.setting.getValueMax()) {
                this.setting.setValueMin(d3);
            }
        } else if (this.draggingMax && d3 >= this.setting.getValueMin()) {
            this.setting.setValueMax(d3);
        }
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
    public void mouseReleased2(double d0, double d1, int i) {
        this.mouseReleased();
    }

    @Override
    public void keyReleased2(int i, int j, int k) {
        this.keyPressed(i);
    }
}
