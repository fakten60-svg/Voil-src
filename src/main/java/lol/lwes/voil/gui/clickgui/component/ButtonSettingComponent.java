package lol.lwes.voil.gui.clickgui.component;

import java.awt.Color;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.config.GuiTheme;
import lol.lwes.voil.gui.clickgui.ClickGuiComponent;
import lol.lwes.voil.gui.clickgui.ClickGuiScreen;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.ButtonSetting;
import lol.lwes.voil.util.render.Render2DUtil;
import net.minecraft.client.util.math.MatrixStack;

public class ButtonSettingComponent extends SettingComponent {
    private final float buttonHeight = 14.0F;
    private final float padding = 5.0F;
    private final ButtonSetting setting;
    private final Module module;

    public ButtonSettingComponent(ButtonSetting buttonsetting, ClickGuiScreen clickguiscreen, Module module) {
        super(buttonsetting, clickguiscreen);
        this.setting = buttonsetting;
        this.module = module;
        this.setPosition(0.0F, 0.0F);
    }

    public void mouseClicked(double d0, double d1) {
        float f = this.getInnerLeft();
        float f1 = this.getControlY();
        if (ClickGuiComponent.isPointInRect(f, f1, f + this.getButtonWidth(), f1 + 14.0F, d0, d1)) {
            this.setting.runAction();
        }
    }

    public void render(MatrixStack matrixstack, int i, int j) {
        float f5 = this.getInnerLeft();
        float f6 = this.getControlY();
        float f7 = this.getButtonWidth();
        float f12 = f5 + f7;
        float f13 = f6 + 14.0F;
        double d2 = i;
        double d0 = j;
        boolean flag = ClickGuiComponent.isPointInRect(f5, f6, f12, f13, d2, d0);
        Color color = flag ? GuiTheme.modeSettingFill.brighter() : GuiTheme.modeSettingFill;
        float f = 1.0F;
        float f1 = GuiTheme.radiusBottomRight;
        float f2 = 14.0F;
        float f3 = f7;
        float f4 = f6;
        Render2DUtil.drawRoundedRect(f5, f4, f3, f2, f1, f, color);
        float f8 = f5 + (f7 - VoilClient.fonts.getSelectedFont().getWidth(this.setting.getLabel())) / 2.0F;
        float f9 = f6 + (14.0F - VoilClient.fonts.getSelectedFont().getLineHeight()) / 2.0F;
        VoilClient.fonts.getSelectedFont().render(this.setting.getLabel(), f8, f9, 10.0F, GuiTheme.normalTextColor.getRGB());
        float f10 = this.getLeft();
        float f11 = this.width;
        f12 = this.getRight();
        f13 = this.width + 22.0F;
        d2 = i;
        double d1 = j;
        if (ClickGuiComponent.isPointInRect(f10, f11, f12, f13, d2, d1)) {
            String s = this.setting.getDescription().toString();
            ButtonSettingComponent buttonsettingcomponent1 = this;
            buttonsettingcomponent1.setTooltip(s, i, j);
        }
    }

    private float getButtonWidth() {
        return VoilClient.fonts.getSelectedFont().getWidth(this.setting.getLabel()) + 10.0F;
    }

    @Override
    public void mouseClicked2(double d0, double d1, int i) {
        this.mouseClicked(d0, d1);
    }

    @Override
    public void render2(MatrixStack matrixstack, int i, int j, float f) {
        this.render(matrixstack, i, j);
    }
}
