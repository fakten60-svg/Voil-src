package lol.lwes.voil.gui.clickgui.component;

import java.awt.Color;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.config.GuiTheme;
import lol.lwes.voil.gui.clickgui.ClickGuiComponent;
import lol.lwes.voil.gui.clickgui.ClickGuiScreen;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.util.render.Render2DUtil;
import net.minecraft.client.util.math.MatrixStack;

public class BooleanSettingComponent extends SettingComponent {
    private final BooleanSetting setting;
    private float knobX;
    private boolean enabled;
    private boolean animating;

    public BooleanSettingComponent(BooleanSetting booleansetting, ClickGuiScreen clickguiscreen) {
        super(booleansetting, clickguiscreen);
        this.setting = booleansetting;
        this.setPosition(0.0F, 0.0F);
        this.enabled = booleansetting.isEnabled();
        this.knobX = this.getKnobTargetX();
    }

    public void mouseClicked(double d0, double d1) {
        float f = this.getSwitchX();
        float f1 = this.getSwitchY();
        if (ClickGuiComponent.isPointInRect(f, f1, f + 22.0F, f1 + 10.0F, d0, d1)) {
            this.setting.toggle();
        }
    }

    public void render(int i, int j) {
        float f = this.getSwitchX();
        float f1 = this.getSwitchY();
        float f2 = f1 + 1.0F;
        float f3 = this.getKnobTargetX();
        if (this.setting.isEnabled() != this.enabled) {
            this.animating = true;
            this.enabled = this.setting.isEnabled();
        }

        if (this.animating) {
            this.knobX = this.knobX + (f3 - this.knobX) * 0.2F;
            if (Math.abs(this.knobX - f3) < 0.1F) {
                this.knobX = f3;
                this.animating = false;
            }
        } else {
            this.knobX = f3;
        }

        Color color = this.setting.isEnabled() ? GuiTheme.accentColor1 : GuiTheme.sliderSettingBg;
        int k = this.setting.isEnabled() ? -1 : GuiTheme.sliderSettingBg.getRGB();
        VoilClient.fonts.getSelectedFont().render(this.setting.getName(), this.getInnerLeft(), this.getTextY(), 10.0F, k);
        Render2DUtil.drawRoundedRect(f, f1, 24.0F, 12.0F, 6.0F, 1.0F, color);
        Render2DUtil.drawCircle(this.knobX, f2, 5.0F, 1.0F, Color.WHITE);
        float f4 = this.getLeft();
        float f5 = this.width;
        float f6 = this.getRight();
        float f7 = this.width + 22.0F;
        double d1 = i;
        double d0 = j;
        if (ClickGuiComponent.isPointInRect(f4, f5, f6, f7, d1, d0)) {
            String s = this.setting.getDescription().toString();
            this.setTooltip(s, i, j);
        }
    }

    private float getSwitchX() {
        return this.fromInnerRight(24.0F);
    }

    private float getSwitchY() {
        return this.width + 6.0F;
    }

    private float getKnobTargetX() {
        float f = this.getSwitchX();
        return this.setting.isEnabled() ? f + 13.0F : f + 1.0F;
    }

    @Override
    public void mouseClicked2(double d0, double d1, int i) {
        this.mouseClicked(d0, d1);
    }

    @Override
    public void render2(MatrixStack matrixstack, int i, int j, float f) {
        this.render(i, j);
    }
}
