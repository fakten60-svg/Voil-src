package lol.lwes.voil.gui.clickgui.component;

import java.awt.Color;
import java.util.List;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.config.GuiTheme;
import lol.lwes.voil.gui.clickgui.ClickGuiComponent;
import lol.lwes.voil.gui.clickgui.ClickGuiScreen;
import lol.lwes.voil.setting.ModeSetting;
import lol.lwes.voil.util.render.Render2DUtil;
import net.minecraft.client.util.math.MatrixStack;

public class ModeSettingComponent extends SettingComponent {
    private float optionHeight = 17.0F;
    private float animationSpeed = 0.2F;
    private float padding = 3.0F;
    private float cornerRadius = 6.0F;
    private float spacing = 6.0F;
    private float arrowSize = 4.0F;
    private final ModeSetting setting;
    private float selectorX;
    private float selectorWidth;
    private float selectorAnim;
    private boolean animating;
    private static final int INACTIVE_TEXT_COLOR = 0xFF888888;

    public ModeSettingComponent(ModeSetting modesetting, ClickGuiScreen clickguiscreen) {
        super(modesetting, clickguiscreen);
        this.setting = modesetting;
        this.setPosition(0.0F, 0.0F);
    }

    public void mouseClicked(double d0, double d1, int i) {
        List list = this.setting.getModes();
        if (!list.isEmpty()) {
            int j = Math.max(0, list.indexOf(this.setting.getMode()));
            float[] afloat = this.getOptionWidths();
            float f = afloat[j];
            float f1 = this.getTotalWidth();
            float f2 = this.getNextX(f, f1);
            float f3 = this.animating ? this.getSelectorOffset(afloat) : f2;
            float f4 = this.fromInnerRight(f3);
            float f5 = this.getControlY();
            if (ClickGuiComponent.isPointInRect(f4, f5, f4 + f3, f5 + this.optionHeight, d0, d1)) {
                if (i == 1) {
                    this.animating = !this.animating;
                } else {
                    if (i == 0) {
                        if (!this.animating) {
                            this.setting.next();
                            return;
                        }

                        float f6 = f4 + 2.0F;

                        for (int k = 0; k < list.size(); k++) {
                            if (d0 >= f6 && d0 < f6 + afloat[k]) {
                                String s = (String)list.get(k);
                                int l = list.size();

                                while (!this.setting.getMode().equals(s) && l-- > 0) {
                                    this.setting.next();
                                }

                                return;
                            }

                            f6 += afloat[k];
                        }
                    }
                }
            }
        }
    }

    public void render(int i, int j) {
        VoilClient.fonts.getSelectedFont().render(this.setting.getName(), this.getInnerLeft(), this.getTextY(), 10.0F, -1);
        List list = this.setting.getModes();
        if (!list.isEmpty()) {
            int k = Math.max(0, list.indexOf(this.setting.getMode()));
            float[] afloat = this.getOptionWidths();
            float f5 = this.getSelectorOffset(afloat);
            float f6 = afloat[k];
            float f7 = this.getTotalWidth();
            float f8 = this.getNextX(f6, f7);
            float f9 = this.animating ? f5 : f8;
            if (this.selectorAnim == 0.0F) {
                this.selectorAnim = f9;
            } else {
                this.selectorAnim = this.selectorAnim + (f9 - this.selectorAnim) * this.animationSpeed;
            }

            float f10 = this.selectorAnim;
            float f11 = this.fromInnerRight(f10);
            float f12 = this.getControlY();
            float f13 = f11 + f10;
            Render2DUtil.drawRoundedRect(f11 + 2.0F, f12, f10 - 2.0F, this.optionHeight, 6.0F, 1.0F, GuiTheme.modeSettingFill);
            Render2DUtil.drawOutline(f11 + 2.0F, f12, f10 - 2.0F, this.optionHeight, 6.0F, 0.3F, 0.6F, GuiTheme.modeSettingFill.brighter());
            float f14 = f13 - f5 + 2.0F;
            float f15;
            if (this.animating) {
                f15 = f14;

                for (int l = 0; l < k; l++) {
                    f15 += afloat[l];
                }
            } else {
                f15 = f13 - f6 - 2.0F;
            }

            if (this.selectorWidth == 0.0F) {
                this.selectorX = f15;
                this.selectorWidth = f6;
            } else {
                this.selectorX = this.selectorX + (f15 - this.selectorX) * this.animationSpeed;
                this.selectorWidth = this.selectorWidth + (f6 - this.selectorWidth) * this.animationSpeed;
            }

            if (this.animating) {
                float f27 = this.selectorX + 2.0F;
                float f21 = f12 + 2.0F;
                Render2DUtil.drawRoundedRect(f27, f21, this.selectorWidth - 2.0F, this.optionHeight - 4.0F, 5.0F, 1.0F, GuiTheme.accentColor1);
            }

            if (f7 > 0.0F) {
                float f22 = f5 == f8 ? 1.0F : 1.0F - Math.max(0.0F, Math.min(1.0F, (this.selectorAnim - f8) / (f5 - f8)));
                if (f22 > 0.01F) {
                    float f16 = f11 + 2.0F + this.spacing;
                    float f17 = f12 + (this.optionHeight - this.padding) / 2.0F;

                    for (int i1 = 0; i1 < list.size(); i1++) {
                        float f18 = f16 + i1 * this.cornerRadius;
                        float f30 = this.padding / 2.0F;
                        Color color = i1 == k ? GuiTheme.accentColor1 : GuiTheme.modeSettingFill.brighter().brighter();
                        float f = f22;
                        float f1 = f30;
                        float f2 = this.padding;
                        float f3 = this.padding;
                        float f4 = f17;
                        Render2DUtil.drawRoundedRect(f18, f4, f3, f2, f1, f, color);
                    }
                }
            }

            float f23 = VoilClient.fonts.getSelectedFont().getLineHeight(9.0F);
            float f24 = f12 + (this.optionHeight - f23) / 2.0F;
            if (this.animating) {
                for (int j1 = 0; j1 < list.size(); j1++) {
                    if (j1 != k && f14 + afloat[j1] / 2.0F > f11) {
                        String s2 = (String)list.get(j1);
                        float f19 = VoilClient.fonts.getSelectedFont().getWidth(s2, 9.0F);
                        float f20 = f14 + (afloat[j1] - f19) / 2.0F;
                        VoilClient.fonts.getSelectedFont().render(s2, f20, f24, 9.0F, INACTIVE_TEXT_COLOR);
                    }

                    f14 += afloat[j1];
                }
            }

            String s1 = (String)list.get(k);
            float f25 = VoilClient.fonts.getSelectedFont().getWidth(s1, 9.0F);
            float f26 = this.selectorX + (f6 - f25) / 2.0F;
            VoilClient.fonts.getSelectedFont().render(s1, f26, f24, 9.0F, -1);
            float f28 = f11 + f10;
            float f29 = f12 + this.optionHeight;
            double d1 = i;
            double d0 = j;
            if (ClickGuiComponent.isPointInRect(f11, f12, f28, f29, d1, d0)) {
                String s = this.setting.getDescription().toString();
                this.setTooltip(s, i, j);
            }
        }
    }

    private float getSelectorOffset(float[] afloat) {
        float f = 4.0F;

        for (float f1 : afloat) {
            f += f1;
        }

        return f;
    }

    private float[] getOptionWidths() {
        List list = this.setting.getModes();
        float[] afloat = new float[list.size()];

        for (int i = 0; i < list.size(); i++) {
            afloat[i] = VoilClient.fonts.getSelectedFont().getWidth((String)list.get(i), 9.0F) + 12.0F;
        }

        return afloat;
    }

    private float getTotalWidth() {
        int i = this.setting.getModes().size();
        return i <= 1 ? 0.0F : (i - 1) * this.cornerRadius + this.padding;
    }

    private float getNextX(float f, float f1) {
        float f2 = f1 > 0.0F ? this.spacing + f1 + this.arrowSize : 4.0F;
        return f + f2;
    }

    @Override
    public void mouseClicked2(double d0, double d1, int i) {
        this.mouseClicked(d0, d1, i);
    }

    @Override
    public void render2(MatrixStack matrixstack, int i, int j, float f) {
        this.render(i, j);
    }
}
