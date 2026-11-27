package lol.lwes.voil.gui.clickgui.component;

import java.awt.Color;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.config.GuiTheme;
import lol.lwes.voil.gui.clickgui.ClickGuiComponent;
import lol.lwes.voil.gui.clickgui.ClickGuiScreen;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.ColorSetting;
import lol.lwes.voil.util.render.ColorUtil;
import lol.lwes.voil.util.render.Render2DUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.glfw.GLFW;

public class ColorSettingComponent extends SettingComponent {
    private static final float[] HUE_STOPS = new float[]{0.0F, 0.16666667F, 0.33333334F, 0.5F, 0.6666667F, 0.8333333F, 1.0F};
    private static final Color[] HUE_COLORS = new Color[]{Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA, Color.RED};
    private static final float[] ALPHA_STOPS = new float[]{0.0F, 1.0F};
    private final ColorSetting setting;
    private final Module module;
    private boolean pickerOpen;
    private float hue = 0.0F;
    private int saturationX = 0;
    private int brightnessY = 0;
    private int hueIndex = 0;
    private int alphaPercent = 0;
    private float alpha = 1.0F;
    private boolean draggingBox;
    private boolean draggingHue;
    private boolean draggingAlpha;
    private String hexInput = "";
    private boolean editingHex;

    public ColorSettingComponent(ColorSetting colorsetting, ClickGuiScreen clickguiscreen, float f, float f1, Module module) {
        super(colorsetting, clickguiscreen);
        this.setting = colorsetting;
        this.module = module;
        this.setPosition(f, f1);
    }

    @Override
    protected float getExtraHeight() {
        if (!this.pickerOpen) {
            return 0.0F;
        } else {
            return this.setting.hasAlpha() ? 136.0F : 122.0F;
        }
    }

    private void drawHueSlider(float f, float f1) {
        Render2DUtil.drawGradientStops(f, f1, 100.0F, 6.0F, 3.0F, 1.0F, HUE_STOPS, HUE_COLORS, false, 90.0F, 0.0F, 1.0F);
        Color color = Color.getHSBColor(this.hueIndex / 100.0F, 1.0F, 1.0F);
        float f2 = f + 5.0F + this.hueIndex / 100.0F * 90.0F;
        Render2DUtil.drawCircle(f2 - 5.0F, f1 - 1.0F, 5.0F, 1.0F, Color.WHITE);
        Render2DUtil.drawCircle(f2 - 4.0F, f1, 4.0F, 1.0F, color);
    }

    private void commitColor() {
        float f = this.saturationX / 100.0F;
        float f1 = 1.0F - this.brightnessY / 80.0F;
        f1 = Math.max(0.0F, f1);
        int k;
        if (this.setting.hasAlpha()) {
            int j = Math.round(this.alpha * 255.0F);
            k = clamp255(j);
        } else {
            k = this.setting.getColor().getAlpha();
        }

        int i = k;
        Color color = f1 <= 0.0F ? new Color(0, 0, 0) : Color.getHSBColor(this.hue, f, f1);
        Color color1 = new Color(color.getRed(), color.getGreen(), color.getBlue(), i);
        this.setting.setColor(color1);
        this.hexInput = this.setting.hasAlpha()
            ? String.format("#%02X%02X%02X%02X", color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha())
            : String.format("#%02X%02X%02X", color1.getRed(), color1.getGreen(), color1.getBlue());
    }

    public void mouseClicked(double d0, double d1, int i) {
        if (i == 0) {
            float f = this.getBoxX();
            float f1 = this.getControlY();
            if (ClickGuiComponent.isPointInRect(f, f1, f + 24.0F, f1 + 14.0F, d0, d1)) {
                this.pickerOpen = !this.pickerOpen;
                if (this.pickerOpen) {
                    this.refreshFromSetting();
                }
            } else if (this.pickerOpen) {
                float f2 = this.getContentX();
                float f3 = this.getBoxY();
                float f11 = f2 + 100.0F;
                float f6 = f3 + 80.0F;
                float f7 = f11;
                float f8 = f3;
                float f9 = f2;
                if (ClickGuiComponent.isPointInRect(f9, f8, f7, f6, d0, d1)) {
                    this.saturationX = (int)Math.max(0.0, Math.min(d0 - f2, 100.0));
                    this.brightnessY = (int)Math.max(0.0, Math.min(d1 - f3, 80.0));
                    this.draggingBox = true;
                    this.draggingHue = false;
                    this.draggingAlpha = false;
                    this.commitColor();
                    this.editingHex = false;
                } else {
                    float f4 = this.getHueSliderY();
                    if (ClickGuiComponent.isPointInRect(f2, f4, f2 + 100.0F, f4 + 8.0F, d0, d1)) {
                        this.hueIndex = Math.round(ColorUtil.clamp01((float)((d0 - f2 - 5.0) / 90.0)) * 100.0F);
                        this.hue = this.hueIndex / 100.0F;
                        this.draggingHue = true;
                        this.draggingBox = false;
                        this.draggingAlpha = false;
                        this.commitColor();
                        this.editingHex = false;
                    } else {
                        if (this.setting.hasAlpha()) {
                            float f5 = this.getAlphaSliderY();
                            if (ClickGuiComponent.isPointInRect(f2, f5, f2 + 100.0F, f5 + 8.0F, d0, d1)) {
                                this.alphaPercent = Math.round(ColorUtil.clamp01((float)((d0 - f2 - 5.0) / 90.0)) * 100.0F);
                                this.alpha = this.alphaPercent / 100.0F;
                                this.draggingAlpha = true;
                                this.draggingBox = false;
                                this.draggingHue = false;
                                this.commitColor();
                                this.editingHex = false;
                                return;
                            }
                        }

                        float f10 = this.getHexFieldY();
                        this.editingHex = ClickGuiComponent.isPointInRect(f2, f10, f2 + 100.0F, f10 + 14.0F, d0, d1);
                        this.draggingBox = false;
                        this.draggingHue = false;
                        this.draggingAlpha = false;
                    }
                }
            }
        }
    }

    public void render(int i, int j) {
        Color color = this.setting.getColor();
        if (color == null) {
            color = Color.WHITE;
        }

        VoilClient.fonts.getSelectedFont().render(this.setting.getName(), this.getInnerLeft(), this.getTextY(), 10.0F, -1);
        float f2 = this.getBoxX();
        float f3 = this.getControlY();
        float[] afloat1 = ALPHA_STOPS;
        Color[] acolor1 = new Color[]{color, color};
        float f = this.setting.hasAlpha() ? 2.5F : 0.0F;
        float f1 = 90.0F;
        Color[] acolor = acolor1;
        float[] afloat = afloat1;
        Render2DUtil.drawGradientStops(f2, f3, 24.0F, 14.0F, 7.0F, 1.0F, afloat, acolor, false, f1, f, 1.0F);
        float f11 = f2 - 1.0F;
        float f8 = f3 - 1.0F;
        Render2DUtil.drawOutline(f11, f8, 26.0F, 16.0F, 7.0F, 0.4F, 0.6F, GuiTheme.modeSettingBg);
        if (this.pickerOpen) {
            float f4 = this.getContentX();
            float f5 = this.getBoxY();
            this.drawSaturationBrightnessBox(f4, f5);
            float f6 = this.getHueSliderY();
            this.drawHueSlider(f4, f6);
            if (this.setting.hasAlpha()) {
                float f9 = this.getAlphaSliderY();
                float f10 = f4;
                this.drawAlphaSlider(f10, f9);
            }

            float f7 = this.getHexFieldY();
            Render2DUtil.drawRoundedRect(f4, f7, 100.0F, 14.0F, 4.0F, 1.0F, ColorUtil.scaleBrightness(GuiTheme.settingsBg, 2.5F));
            String s = this.hexInput.isEmpty() ? (this.setting.hasAlpha() ? "HEX (#RRGGBBAA)" : "HEX (#RRGGBB)") : this.hexInput;
            VoilClient.fonts.getSelectedFont().render(s, f4 + 4.0F, f7 + 2.0F, 10.0F, -1);
            if (this.editingHex) {
                Render2DUtil.drawOutline(f4, f7, 100.0F, 14.0F, 4.0F, 0.4F, 0.5F, Color.WHITE);
            }
        }

        float f12 = f2 + 24.0F;
        float f13 = f3 + 14.0F;
        double d1 = i;
        double d0 = j;
        if (ClickGuiComponent.isPointInRect(f2, f3, f12, f13, d1, d0)) {
            String s1 = this.setting.getDescription();
            this.setTooltip(s1, i, j);
        }
    }

    public void mouseDragged(double d0, double d1, int i) {
        if (i == 0 && this.pickerOpen) {
            if (this.draggingBox || this.draggingHue || this.draggingAlpha) {
                float f = this.getContentX();
                if (this.draggingBox) {
                    this.saturationX = (int)Math.max(0.0, Math.min(d0 - f, 100.0));
                    this.brightnessY = (int)Math.max(0.0, Math.min(d1 - this.getBoxY(), 80.0));
                    this.commitColor();
                } else if (this.draggingHue) {
                    this.hueIndex = Math.round(ColorUtil.clamp01((float)((d0 - f - 5.0) / 90.0)) * 100.0F);
                    this.hue = this.hueIndex / 100.0F;
                    this.commitColor();
                } else {
                    if (this.draggingAlpha && this.setting.hasAlpha()) {
                        this.alphaPercent = Math.round(ColorUtil.clamp01((float)((d0 - f - 5.0) / 90.0)) * 100.0F);
                        this.alpha = this.alphaPercent / 100.0F;
                        this.commitColor();
                    }
                }
            }
        }
    }

    @Override
    public void charTyped(char chr, int modifiers) {
        if (this.editingHex) {
            if (Character.isDigit(chr) || chr >= 'a' && chr <= 'f' || chr >= 'A' && chr <= 'F') {
                int i = this.setting.hasAlpha() ? 8 : 6;
                int l = this.hexInput.startsWith("#") ? 1 : 0;
                int k = i;
                int j = k + l;
                if (this.hexInput.length() < j) {
                    String s1 = this.hexInput;
                    this.hexInput = s1 + chr;
                    String s = this.hexInput.trim();
                    if (s.startsWith("#")) {
                        s = s.substring(1);
                    }

                    if (s.length() == i) {
                        Color color = this.parseHex(s);
                        if (color != null) {
                            this.setting.setColor(color);
                            this.refreshFromSetting();
                        }
                    }
                }
            }
        }
    }

    public void mouseReleased(int i) {
        if (i == 0) {
            this.draggingBox = false;
            this.draggingHue = false;
            this.draggingAlpha = false;
        }
    }

    public void keyPressed(int i, int j) {
        if (this.editingHex) {
            if (i == 257) {
                String s3 = this.hexInput.trim();
                if (s3.startsWith("#")) {
                    s3 = s3.substring(1);
                }

                Color color = this.parseHex(s3);
                if (color != null) {
                    this.setting.setColor(color);
                    this.refreshFromSetting();
                }

                this.editingHex = false;
                return;
            }

            if (i == 259) {
                if (!this.hexInput.isEmpty()) {
                    String s4 = this.hexInput;
                    int k = this.hexInput.length();
                    this.hexInput = s4.substring(0, k - 1);
                }

                return;
            }

            if (i == 86 && (j & 2) != 0) {
                String s = GLFW.glfwGetClipboardString(MinecraftClient.getInstance().getWindow().getHandle());
                if (s != null) {
                    s = s.trim();
                    if (s.startsWith("#")) {
                        s = s.substring(1);
                    }

                    s = s.replaceAll("[^0-9A-Fa-f]", "");
                    String s1 = this.hexInput;
                    this.hexInput = s1 + s;
                }

                return;
            }
        }

        super.keyReleased();
    }

    private void refreshFromSetting() {
        Color color = this.setting.getColor();
        float[] afloat = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        this.hue = afloat[0];
        this.saturationX = (int)(afloat[1] * 100.0F);
        this.brightnessY = (int)((1.0F - afloat[2]) * 80.0F);
        this.hueIndex = (int)(this.hue * 100.0F);
        this.alpha = color.getAlpha() / 255.0F;
        this.alphaPercent = (int)(this.alpha * 100.0F);
        this.hexInput = this.setting.hasAlpha()
            ? String.format("#%02X%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha())
            : String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
    }

    private static int clamp255(int i) {
        return Math.max(0, Math.min(255, i));
    }

    private void drawSaturationBrightnessBox(float f, float f1) {
        Render2DUtil.drawRectGradient(f, f1, 100.0F, 80.0F, 2.0F, 1.0F, Color.BLACK, Color.WHITE, Color.BLACK, Color.getHSBColor(this.hue, 1.0F, 1.0F));
        Render2DUtil.drawCircle(f + this.saturationX, f1 + this.brightnessY, 4.0F, 1.0F, Color.WHITE);
    }

    private float getBoxX() {
        return this.fromInnerRight(24.0F);
    }

    private float getContentX() {
        return this.getInnerLeft();
    }

    private float getHueSliderY() {
        return this.getBoxY() + 80.0F + 6.0F;
    }

    private Color parseHex(String s) {
        try {
            if (s.length() == 6) {
                int l1 = Integer.parseInt(s, 16);
                int i3 = l1 >> 16;
                int i2 = i3 & 0xFF;
                i3 = l1 >> 8;
                int j2 = i3 & 0xFF;
                int k2 = l1 & 0xFF;
                int j3;
                if (this.setting.hasAlpha()) {
                    int k1 = Math.round(this.alpha * 255.0F);
                    j3 = clamp255(k1);
                } else {
                    j3 = 255;
                }

                int l2 = j3;
                return new Color(i2, j2, k2, l2);
            }

            if (s.length() == 8) {
                int i = (int)Long.parseLong(s, 16);
                int j1 = i >> 24;
                int j = j1 & 0xFF;
                j1 = i >> 16;
                int k = j1 & 0xFF;
                int l = i >> 8 & 0xFF;
                int i1 = i & 0xFF;
                if (!this.setting.hasAlpha()) {
                    i1 = 255;
                }

                this.alpha = i1 / 255.0F;
                this.alphaPercent = (int)(this.alpha * 100.0F);
                return new Color(j, k, l, i1);
            }
        } catch (NumberFormatException numberformatexception) {
        }

        return null;
    }

    private void drawAlphaSlider(float f, float f1) {
        float f2 = this.saturationX / 100.0F;
        float f3 = 1.0F - this.brightnessY / 80.0F;
        Color color = Color.getHSBColor(this.hue, f2, Math.max(0.0F, f3));
        Color color1 = new Color(color.getRed(), color.getGreen(), color.getBlue(), 0);
        Render2DUtil.drawGradientStops(f, f1, 100.0F, 6.0F, 3.0F, 1.0F, ALPHA_STOPS, new Color[]{color1, color}, false, 90.0F, 2.5F, 1.0F);
        float f4 = f + 5.0F + this.alphaPercent / 100.0F * 90.0F;
        Render2DUtil.drawCircle(f4 - 5.0F, f1 - 1.0F, 5.0F, 1.0F, Color.WHITE);
        int j = color.getRed();
        int k = color.getGreen();
        int l = color.getBlue();
        int i = Math.round(this.alpha * 255.0F);
        Color color2 = new Color(j, k, l, clamp255(i));
        Render2DUtil.drawGradientStops(f4 - 4.0F, f1, 8.0F, 8.0F, 4.0F, 1.0F, ALPHA_STOPS, new Color[]{color2, color2}, false, 90.0F, 2.0F, 1.0F);
    }

    private float getHexFieldY() {
        return (this.setting.hasAlpha() ? this.getAlphaSliderY() + 8.0F : this.getHueSliderY() + 8.0F) + 6.0F;
    }

    private float getBoxY() {
        return this.getBottom() + 4.0F;
    }

    private float getAlphaSliderY() {
        return this.getHueSliderY() + 8.0F + 6.0F;
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
    public void mouseDragged2(double d0, double d1, int i, double d2, double d3) {
        this.mouseDragged(d0, d1, i);
    }

    @Override
    public void mouseReleased2(double d0, double d1, int i) {
        this.mouseReleased(i);
    }

    @Override
    public void keyReleased2(int i, int k, int j) {
        this.keyPressed(i, j);
    }
}
