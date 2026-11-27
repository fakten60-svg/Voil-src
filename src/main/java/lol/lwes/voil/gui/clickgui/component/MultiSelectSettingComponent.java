package lol.lwes.voil.gui.clickgui.component;

import dev.prestigeclient.renderlib.api.font.MSDFFontAtlas;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.config.GuiTheme;
import lol.lwes.voil.gui.clickgui.ClickGuiComponent;
import lol.lwes.voil.gui.clickgui.ClickGuiScreen;
import lol.lwes.voil.gui.clickgui.component.ButtonAnimations;
import lol.lwes.voil.setting.MultiSelectSetting;
import lol.lwes.voil.util.render.ColorUtil;
import lol.lwes.voil.util.render.EasingType;
import lol.lwes.voil.util.render.Render2DUtil;
import lol.lwes.voil.util.render.UiAnimation;
import net.minecraft.client.util.math.MatrixStack;

public class MultiSelectSettingComponent extends SettingComponent {
    private final float dropdownWidth = 100.0F;
    private final float buttonHeight = 14.0F;
    private final float optionHeight = 18.0F;
    private final float headerHeight = 16.0F;
    private final float cornerRadius = 4.0F;
    private final float animationSpeed = 0.1F;
    private final Color textColor = new Color(220, 220, 220);
    private final Color selectedColor = new Color(200, 200, 200);
    private final MultiSelectSetting setting;
    private final Map<String, ButtonAnimations> optionStates = new HashMap<>();
    private boolean expanded;
    private final UiAnimation expandAnimation = new UiAnimation(EasingType.FLUID_SMOOTH).setFluidParams(0.72, 0.22);
    private final UiAnimation arrowAnimation = new UiAnimation(EasingType.FLUID_SMOOTH).setFluidParams(0.7, 0.15);
    private final UiAnimation hoverAnimation = new UiAnimation(EasingType.FLUID_SMOOTH).setFluidParams(0.6, 0.15);
    private final UiAnimation scrollAnimation = new UiAnimation(EasingType.FLUID_RESPONSIVE);

    public MultiSelectSettingComponent(MultiSelectSetting multiselectsetting, ClickGuiScreen clickguiscreen, int i, int j) {
        super(multiselectsetting, clickguiscreen);
        this.setting = multiselectsetting;
        this.setPosition(i, j);
        this.arrowAnimation.snapTo(270.0);

        for (String s : multiselectsetting.getOptions()) {
            this.getOptionState(s).scale().snapTo(this.isOptionSelected(s) ? 1.0 : 0.0);
        }
    }

    @Override
    protected float getExtraHeight() {
        float f = 8.0F + this.setting.getOptions().size() * 18.0F;
        return Math.max(0.0F, f * this.expandAnimation.getFloat());
    }

    public void mouseClicked(double d0, double d1) {
        float f = this.fromInnerRight(100.0F);
        float f1 = this.getControlY();
        if (ClickGuiComponent.isPointInRect(f, f1, f + 100.0F, f1 + 14.0F, d0, d1)) {
            this.expanded = !this.expanded;
            this.scrollAnimation.snapTo(1.0);
            this.scrollAnimation.setTarget(0.0);
        } else if (!(this.expandAnimation.getValue() < 0.6)) {
            String s = this.getHoveredOption(d0, d1);
            if (s != null) {
                if (this.isOptionSelected(s)) {
                    this.setting.deselect(s);
                } else {
                    this.setting.select(s);
                }

                UiAnimation uianimation = this.getOptionState(s).fade();
                uianimation.snapTo(1.0);
                uianimation.setTarget(0.0);
            }
        }
    }

    public void render(int i, int j) {
        this.drawHeader(i, j);
        MSDFFontAtlas msdffontatlas = VoilClient.fonts.getSelectedFont();
        float f = this.fromInnerRight(100.0F);
        float f1 = this.getControlY();
        float f2 = this.hoverAnimation.getFloat();
        float f3 = this.expandAnimation.getFloat();
        Color color = GuiTheme.modeSettingFill;
        Color color1 = color.brighter();
        Render2DUtil.drawRoundedRect(f, f1, 100.0F, 14.0F, 4.0F, 1.0F, ColorUtil.blendColors(color, color1, f2 * 0.6F + this.scrollAnimation.getFloat() * 0.4F));
        Render2DUtil.drawOutline(f, f1, 100.0F, 14.0F, 4.0F, 1.0F, 0.2F + f2 * 0.4F, ColorUtil.blendColors(color1, GuiTheme.accentColor1, (f2 + f3) * 0.5F));
        String s = this.getSummary();
        msdffontatlas.render(
            s, f + (100.0F - msdffontatlas.getWidth(s)) / 2.0F, this.getTextY() - 2.0F, 10.0F, ColorUtil.blendColors(this.textColor, Color.WHITE, f2).getRGB()
        );
        msdffontatlas.render(this.setting.getName(), this.getInnerLeft(), this.getTextY(), 10.0F, -1);
        float f10 = f + 100.0F - 13.0F;
        float f11 = f1 + 1.0F;
        float f12 = this.arrowAnimation.getFloat();
        float f8 = 0.6F + f2 * 0.4F;
        Color color2 = Color.WHITE;
        Render2DUtil.drawArrow(f10, f11, 12.0F, 12.0F, 5.0F, 0.6F, 0.5F, 50.0F, f12, 0.8F, ColorUtil.withAlpha(color2, f8));
        float f4 = this.getExtraHeight();
        if (!(f4 <= 0.5F)) {
            float f5 = this.getBottom();
            float f6 = ColorUtil.clamp01(f3 * 1.6F);
            Render2DUtil.drawRoundedRect(f, f5, 100.0F, f4, 4.0F, 1.0F, ColorUtil.withAlpha(color, f6));
            float f9 = f6 * 0.6F;
            Color color3 = color1;
            Render2DUtil.drawOutline(f, f5, 100.0F, f4, 4.0F, 1.0F, 0.2F, ColorUtil.withAlpha(color3, f9));
            float f7 = f5 + 4.0F;

            for (String s1 : this.setting.getOptions()) {
                if (f7 + 16.0F > f5 + f4 + 2.0F) {
                    break;
                }

                ButtonAnimations buttonanimations = this.getOptionState(s1);
                if (buttonanimations.hover().getFloat() > 0.01F) {
                    this.drawOption(s1, buttonanimations, f, f7, color, color1);
                }

                f7 += 18.0F;
            }
        }
    }

    private void drawHeader(int i, int j) {
        float f = this.fromInnerRight(100.0F);
        float f1 = this.getControlY();
        double d1 = this.expanded ? 1.0 : 0.0;
        this.expandAnimation.setTarget(d1);
        double d2 = this.expanded ? 90.0 : 270.0;
        this.arrowAnimation.setTarget(d2);
        UiAnimation uianimation = this.hoverAnimation;
        float f2 = f + 100.0F;
        float f3 = f1 + 14.0F;
        double d8 = i;
        double d3 = j;
        double d4 = ClickGuiComponent.isPointInRect(f, f1, f2, f3, d8, d3) ? 1.0 : 0.0;
        uianimation.setTarget(d4);
        this.expandAnimation.update();
        this.arrowAnimation.update();
        this.hoverAnimation.update();
        this.scrollAnimation.update();
        double d0 = this.expandAnimation.getValue();
        String s = this.getHoveredOption(i, j);
        int k = 0;

        for (String s1 : this.setting.getOptions()) {
            ButtonAnimations buttonanimations = this.getOptionState(s1);
            uianimation = buttonanimations.hover();
            double d5 = this.expanded && d0 > k * 0.1F ? 1.0 : 0.0;
            uianimation.setTarget(d5);
            uianimation = buttonanimations.active();
            double d6 = s1.equals(s) ? 1.0 : 0.0;
            uianimation.setTarget(d6);
            uianimation = buttonanimations.scale();
            double d7 = this.isOptionSelected(s1) ? 1.0 : 0.0;
            uianimation.setTarget(d7);
            buttonanimations.hover().update();
            buttonanimations.active().update();
            buttonanimations.scale().update();
            buttonanimations.fade().update();
            k++;
        }
    }

    private void drawOption(String s, ButtonAnimations buttonanimations, float f, float f1, Color color, Color color1) {
        MSDFFontAtlas msdffontatlas = VoilClient.fonts.getSelectedFont();
        float f2 = buttonanimations.hover().getFloat();
        float f3 = buttonanimations.active().getFloat();
        float f4 = buttonanimations.scale().getFloat();
        float f5 = (f2 - 1.0F) * 6.0F;
        float f6 = f + 1.0F + f5;
        float f7 = ColorUtil.clamp01(f4 * 0.5F + f3 * 0.35F + buttonanimations.fade().getFloat() * 0.3F);
        if (f7 > 0.01F) {
            Color color2 = ColorUtil.blendColors(color, color1, 0.6F + f3 * 0.4F);
            float f12 = f7 * f2;
            Color color3 = color2;
            Render2DUtil.drawRoundedRect(f6, f1, 98.0F, 16.0F, 4.0F, 1.0F, ColorUtil.withAlpha(color3, f12));
        }

        if (f4 > 0.01F) {
            float f17 = 10.0F * ColorUtil.clamp01(f4);
            float f18 = f6 + 2.0F;
            float f13 = f1 + (16.0F - f17) / 2.0F;
            Color color4 = GuiTheme.accentColor1;
            Render2DUtil.drawRoundedRect(f18, f13, 2.0F, f17, 1.0F, 1.0F, ColorUtil.withAlpha(color4, f2));
        }

        Color color11 = ColorUtil.blendColors(this.selectedColor, Color.WHITE, f4 + f3);
        float f8 = f + (100.0F - msdffontatlas.getWidth(s)) / 2.0F + f5 + f3 * 1.5F;
        msdffontatlas.render(s, f8, f1 + 3.0F, 10.0F, ColorUtil.withAlpha(color11, f2).getRGB());
        float f9 = f6 + 98.0F - 9.0F;
        float f10 = f1 + 8.0F;
        float f14 = f2 * (0.5F + f3 * 0.5F);
        Color color5 = color1;
        Color color6 = ColorUtil.withAlpha(color5, f14);
        drawCheckmark(f9, f10, 3.5F, color6);
        if (!(f4 <= 0.01F)) {
            float f19 = 3.5F * Math.min(1.15F, f4);
            Color color12 = GuiTheme.accentColor1;
            float f15 = f2 * ColorUtil.clamp01(f4);
            Color color7 = color12;
            Color color8 = ColorUtil.withAlpha(color7, f15);
            drawCheckmark(f9, f10, f19, color8);
            if (f4 > 1.0F) {
                float f11 = (f4 - 1.0F) / 0.15F;
                f19 = 3.5F + f11 * 3.0F;
                color12 = GuiTheme.accentColor1;
                float f16 = f2 * ColorUtil.clamp01(1.0F - f11) * 0.35F;
                Color color9 = color12;
                Color color10 = ColorUtil.withAlpha(color9, f16);
                drawCheckmark(f9, f10, f19, color10);
            }
        }
    }

    private String getHoveredOption(double d0, double d1) {
        if (!this.expanded) {
            return null;
        }

        float f = this.fromInnerRight(100.0F);
        float f1 = this.getBottom() + 4.0F;

        for (String s : this.setting.getOptions()) {
            if (this.getOptionState(s).hover().getValue() > 0.5 && ClickGuiComponent.isPointInRect(f, f1, f + 100.0F, f1 + 16.0F, d0, d1)) {
                return s;
            }

            f1 += 18.0F;
        }

        return null;
    }

    private boolean isOptionSelected(String s) {
        return this.setting.getSelectedOptions().contains(s);
    }

    private static void drawCheckmark(float f, float f1, float f2, Color color) {
        Render2DUtil.drawCircle(f - f2, f1 - f2, f2, 1.0F, color);
    }

    private String getSummary() {
        Set set = this.setting.getSelectedOptions();
        int i = set.size();
        if (i == 0) {
            return "None";
        }

        if (i == 1) {
            return (String)set.iterator().next();
        }

        if (i == this.setting.getOptions().size()) {
            return "All";
        }

        return i + " selected";
    }

    private ButtonAnimations getOptionState(String s) {
        return this.optionStates.computeIfAbsent(s, s1 -> new ButtonAnimations());
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
