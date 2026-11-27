package lol.lwes.voil.gui.clickgui.component;

import dev.prestigeclient.renderlib.api.font.MSDFFontAtlas;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.config.GuiTheme;
import lol.lwes.voil.gui.clickgui.ClickGuiScreen;
import lol.lwes.voil.setting.GradientSetting;
import lol.lwes.voil.setting.GradientStop;
import lol.lwes.voil.util.render.ColorUtil;
import lol.lwes.voil.util.render.EasingType;
import lol.lwes.voil.util.render.GradientType;
import lol.lwes.voil.util.render.Render2DUtil;
import lol.lwes.voil.util.render.UiAnimation;
import lol.lwes.voil.util.render.UiRect;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Vector2d;
import org.lwjgl.glfw.GLFW;

public class GradientSettingComponent extends SettingComponent {
    private static final GradientType[] GRADIENT_MODES = GradientType.values();
    private static final float[] HUE_STOPS = new float[]{0.0F, 0.16666667F, 0.33333334F, 0.5F, 0.6666667F, 0.8333333F, 1.0F};
    private static final Color[] HUE_COLORS = new Color[]{Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA, Color.RED};
    private static final String[] CHANNEL_LABELS = new String[]{"R", "G", "B", "A"};
    private static final Color LABEL_COLOR = new Color(140, 140, 150);
    private static final Color VALUE_COLOR = new Color(160, 160, 170);
    private final GradientSetting setting;
    private final Map<GradientStop, UiAnimation> hsbCache = new HashMap<>();
    private final Map<GradientStop, UiAnimation> hoverAnimations = new HashMap<>();
    private final Map<GradientStop, float[]> selectAnimations = new HashMap<>();
    private final UiAnimation[] stopAnimations = new UiAnimation[GRADIENT_MODES.length];
    private final UiAnimation openAnimation = createAnimation(0.72, 0.22);
    private final UiAnimation expandAnimation = createAnimation(0.7, 0.15);
    private final UiAnimation previewAnimation = createAnimation(0.72, 0.22);
    private final UiAnimation angleAnimation = createAnimation(0.7, 0.15);
    private final UiAnimation modeAnimation = createAnimation(0.6, 0.15);
    private boolean open;
    private boolean dropdownOpen;
    private GradientStop draggedStop;
    private int dragTarget = 0;
    private float dragOffsetX = 1.0F;
    private float dragOffsetY;
    private int editingIndex = -1;
    private GradientStop selectedStop;
    private String hexInput = "";
    private boolean editingHex;
    private Color editStartColor;
    private int editStartAngle;
    private int hoverIndex;
    private GradientStop hoveredStop;
    private float previewX;
    private float previewRight;
    private float previewWidth;
    private float barTop;
    private float pickerLabelY;
    private float angleDialSize;
    private float barWidth;
    private float barHeight;
    private float listTop;
    private UiRect barRect;
    private UiRect modeRect;
    private UiRect modeDropdownRect;
    private UiRect angleDialRect;
    private UiRect addButtonRect;
    private UiRect pickerRect;
    private UiRect hueRect;
    private UiRect alphaRect;
    private UiRect previewRect;
    private final UiRect[] allRects = new UiRect[4];

    private static boolean isHexDigit(int i) {
        return isDecimalDigit(i) || i >= 97 && i <= 102 || i >= 65 && i <= 70;
    }

    public GradientSettingComponent(GradientSetting gradientsetting, ClickGuiScreen clickguiscreen, float f, float f1) {
        super(gradientsetting, clickguiscreen);
        this.setting = gradientsetting;
        this.draggedStop = (GradientStop)gradientsetting.getStops().getFirst();
        for (int i = 0; i < this.stopAnimations.length; i++) {
            this.stopAnimations[i] = createAnimation(0.6, 0.15);
        }

        this.setPosition(f, f1);

        this.expandAnimation.snapTo(this.isRadial() ? 1.0 : 0.0);
        this.angleAnimation.snapTo(270.0);
    }

    @Override
    protected float getExtraHeight() {
        return Math.max(0.0F, this.getPanelHeight() * this.openAnimation.getFloat());
    }

    private void removeStop(GradientStop gradientstop) {
        if (this.setting.removeStop(gradientstop) && this.draggedStop == gradientstop) {
            this.draggedStop = (GradientStop)this.setting.getStops().getFirst();
        }
    }

    private void commitHexEdit() {
        if (this.editingIndex != -1) {
            int i = this.editingIndex;
            this.editingIndex = -1;
            this.applyHexText(i, this.selectedStop, this.hexInput, true);
        }
    }

    private UiAnimation getHoverAnimation(GradientStop gradientstop) {
        return this.hsbCache.computeIfAbsent(gradientstop, gradientstop1 -> createAnimation(0.65, 0.15));
    }

    public void mouseClicked(double d0, double d1, int i) {
        if (i == 0) {
            this.layout();
            if (contains(this.getHexFieldRect(), d0, d1)) {
                this.commitHexEdit();
                this.open = !this.open;
            } else if (this.open && !(this.openAnimation.getValue() < 0.9)) {
                if (this.dropdownOpen) {
                    this.selectModeAt(d0, d1);
                } else {
                    boolean flag;
                    label35: {
                        flag = this.hitTestStops(d0, d1);
                        if (flag) {
                            GradientStop gradientstop = this.hoveredStop;
                            if (this.isEditing(this.hoverIndex, gradientstop)) {
                                break label35;
                            }
                        }

                        this.commitHexEdit();
                    }

                    if (!flag) {
                        this.handleRelease(d0, d1);
                    } else {
                        this.draggedStop = this.hoveredStop;
                        if (this.editingIndex == -1) {
                            GradientStop gradientstop1 = this.hoveredStop;
                            int j = this.hoverIndex;
                            this.beginEdit(j, gradientstop1);
                        }
                    }
                }
            }
        }
    }

    private void pasteHex() {
        String s = GLFW.glfwGetClipboardString(VoilClient.mc.getWindow().getHandle());
        if (s != null) {
            int i = this.editingIndex;
            boolean flag = isHexField(i);
            StringBuilder stringbuilder = new StringBuilder(this.editingHex ? "" : this.hexInput);

            for (char c0 : s.toCharArray()) {
                if (flag ? isHexDigit(c0) : isDecimalDigit(c0)) {
                    stringbuilder.append(c0);
                }
            }

            String s1 = stringbuilder.toString();
            int k = s1.length();
            int j = this.editingIndex;
            this.hexInput = s1.substring(0, Math.min(k, this.getHexOffset(j, s1)));
            this.editingHex = false;
            this.applyHexText(this.editingIndex, this.selectedStop, this.hexInput, false);
        }
    }

    private static Color stripAlpha(Color color) {
        int i = color.getRGB();
        return new Color(i & 16777215);
    }

    private void applyHexText(int i, GradientStop gradientstop, String s, boolean flag) {
        if (!s.isEmpty()) {
            label52: {
                if (i != 1) {
                    if (i == 6) {
                        break label52;
                    }

                    if (i != 7) {
                        if (i != 8) {
                            this.setStopComponent(i, gradientstop, Integer.parseInt(s));
                            return;
                        }
                        break label52;
                    }
                }

                int j = gradientstop.getColor().getAlpha();
                Color color = GradientSetting.parseHex(s, j);
                boolean flag1 = s.replace("#", "").length() == this.getComponentCount();
                if (color != null && (flag1 || flag)) {
                    this.setting.setStopColor(gradientstop, color);
                }

                return;
            }

            if (flag) {
                this.setStopComponent(i, gradientstop, Integer.parseInt(s));
            }
        }
    }

    private static boolean isHexField(int i) {
        return i == 1 || i == 7;
    }

    public void render(MatrixStack matrixstack, int i, int j) {
        this.layout();
        this.updateAnimations(i, j);
        VoilClient.fonts.getSelectedFont().render(this.setting.getName(), this.previewX, this.getTextY(), 10.0F, -1);
        this.dragOffsetX = 1.0F;
        UiRect uirect = this.getHexFieldRect();
        Render2DUtil.drawGradient(uirect.x, uirect.y, uirect.width, uirect.height, 7.0F, 1.0F, this.setting, this.getAlphaBarHeight(), 1.0F);
        float f = this.getExtraHeight();
        if (f > 0.5F) {
            this.dragOffsetX = ColorUtil.clamp01(this.openAnimation.getFloat() * 1.6F);
            this.dragOffsetY = this.getBottom() + f + 2.0F;
            this.drawEditor(i, j);
        }

        if (uirect.contains(i, j)) {
            String s = this.setting.getDescription();
            this.setTooltip(s, i, j);
        }
    }

    public void mouseDragged(double d0, double d1, int i) {
        if (i == 0 && this.dragTarget != 0) {
            this.layout();
            this.dragStop(d0, d1);
        }
    }

    private int getComponentCount() {
        return this.setting.isAllowAlpha() ? 8 : 6;
    }

    private float getAlphaBarHeight() {
        return this.setting.isAllowAlpha() ? 2.5F : 0.0F;
    }

    @Override
    public void charTyped(char chr, int modifiers) {
        if (this.editingIndex != -1) {
            int i = this.editingIndex;
            if (isHexField(i) ? isHexDigit(chr) : isDecimalDigit(chr)) {
                if (this.editingHex) {
                    this.hexInput = "";
                    this.editingHex = false;
                }

                int l = this.hexInput.length();
                int j = this.editingIndex;
                if (l < this.getHexOffset(j, this.hexInput)) {
                    String s = this.hexInput;
                    this.hexInput = s + chr;
                    String s2 = this.hexInput;
                    GradientStop gradientstop = this.selectedStop;
                    int k = this.editingIndex;
                    this.applyHexText(k, gradientstop, s2, false);
                }
            }
        }
    }

    public void mouseReleased(int i) {
        if (i == 0) {
            this.dragTarget = 0;
        }
    }

    public void keyPressed(int i, int j) {
        if (this.editingIndex == -1) {
            if (this.dropdownOpen && i == 256) {
                this.dropdownOpen = false;
            } else if (this.open && i == 261) {
                GradientStop gradientstop = this.draggedStop;
                this.removeStop(gradientstop);
            } else {
                super.keyReleased();
            }
        } else {
            boolean flag = (j & 2) != 0;
            boolean flag1 = (j & 1) != 0;
            switch (i) {
                case 65:
                    boolean flag2 = this.editingHex;
                    this.editingHex = flag2 | flag;
                    break;
                case 86:
                    if (flag) {
                        this.pasteHex();
                    }
                    break;
                case 256:
                    this.restoreColor();
                    break;
                case 257:
                case 335:
                    this.commitHexEdit();
                    break;
                case 258:
                    this.cycleEditField(flag1 ? -1 : 1);
                    break;
                case 259:
                    this.backspace();
            }
        }
    }

    private void setStopFromHsb(GradientStop gradientstop, float[] afloat) {
        int j = Color.HSBtoRGB(afloat[0], afloat[1], afloat[2]);
        int i = j & 16777215;
        GradientSetting gradientsetting = this.setting;
        Color color = new Color(gradientstop.getColor().getAlpha() << 24 | i, true);
        GradientStop gradientstop1 = gradientstop;
        gradientsetting.setStopColor(gradientstop1, color);
    }

    private void setStopComponent(int i, GradientStop gradientstop, int j) {
        switch (i) {
            case 0:
                GradientSetting gradientsetting = this.setting;
                int k = Math.min(j, 359);
                gradientsetting.setAngle(k);
            case 1:
            case 7:
            default:
                break;
            case 2:
            case 3:
            case 4:
            case 5:
                GradientSetting gradientsetting1 = this.setting;
                Color color2 = gradientstop.getColor();
                int l = i + -3 + 1;
                Color color = color2;
                Color color1 = withChannel(color, l, j);
                GradientStop gradientstop1 = gradientstop;
                gradientsetting1.setStopColor(gradientstop1, color1);
                break;
            case 6:
            case 8:
                this.setting.setStopPosition(gradientstop, j);
        }
    }

    private void drawModeButton() {
        Color color = GuiTheme.modeSettingFill;
        Color color1 = color.brighter();
        float f = this.modeAnimation.getFloat();
        float f1 = ColorUtil.clamp01(this.previewAnimation.getFloat());
        float f7 = this.modeRect.x;
        float f2 = this.modeRect.y;
        Render2DUtil.drawRoundedRect(
            f7, f2, this.modeRect.width, this.modeRect.height, 4.0F, 1.0F, this.withOpacity(ColorUtil.blendColors(color, color1, f * 0.6F), 1.0F)
        );
        f7 = this.modeRect.x;
        float f3 = this.modeRect.y;
        float f9 = this.modeRect.width;
        float f10 = this.modeRect.height;
        Color color2 = ColorUtil.blendColors(color1, GuiTheme.accentColor1, f * 0.5F + f1 * 0.5F);
        float f4 = 0.6F + f * 0.3F;
        Render2DUtil.drawOutline(f7, f3, f9, f10, 4.0F, 0.4F, 0.6F, this.withOpacity(color2, f4));
        this.drawLabel(formatMode(this.setting.getMode()), this.modeRect, 8.0F, Color.WHITE, 1.0F);
        f7 = right(this.modeRect) - 13.0F;
        float f8 = this.modeRect.y + 1.0F;
        float f6 = this.angleAnimation.getFloat();
        float f5 = 0.6F + f * 0.4F;
        Render2DUtil.drawArrow(f7, f8, 12.0F, 12.0F, 5.0F, 0.3F, 0.5F, 40.0F, f6, 0.8F, this.withOpacity(Color.WHITE, f5));
    }

    private void drawAlphaSlider() {
        Color color = this.draggedStop.getColor();
        Color color1 = new Color(color.getRed(), color.getGreen(), color.getBlue(), 0);
        float f3 = this.alphaRect.x;
        float f4 = this.alphaRect.y;
        float f5 = this.alphaRect.width;
        float f6 = this.alphaRect.height;
        float f7 = this.alphaRect.height / 2.0F;
        float[] afloat1 = new float[]{0.0F, 1.0F};
        Color[] acolor1 = new Color[]{color1, stripAlpha(color)};
        float f = this.dragOffsetX;
        float f1 = 2.5F;
        float f2 = 90.0F;
        Color[] acolor = acolor1;
        float[] afloat = afloat1;
        Render2DUtil.drawGradientStops(f3, f4, f5, f6, f7, 1.0F, afloat, acolor, false, f2, f1, f);
        this.drawKnob(getKnobRect(this.alphaRect, color.getAlpha() / 255.0F), 0.0F, color);
    }

    private GradientStop getStopAt(double d0, double d1) {
        for (GradientStop gradientstop : this.getStopsReversed()) {
            if (contains(insetRect(this.getStopBarRect(gradientstop), 2), d0, d1)) {
                return gradientstop;
            }
        }

        return null;
    }

    private void drawStopList(int i, int j) {
        UiRect uirect3 = this.getStopRow(0);
        if (this.isHovered(uirect3)) {
            float f2 = this.previewX;
            float f3 = this.barHeight;
            float f4 = f2;
            String s = "STOPS";
            this.drawSectionLabel(s, f4, f3);
            List<GradientStop> list = this.setting.getStops();
            Color color = GuiTheme.modeSettingFill.brighter();
            boolean flag = list.size() > 2;

            for (int k = 0; k < list.size(); k++) {
                UiRect uirect = this.getStopRow(k);
                if (!this.isHovered(uirect)) {
                    break;
                }

                GradientStop gradientstop = (GradientStop)list.get(k);
                float f = this.getHoverAnimation(gradientstop).getFloat();
                float f1 = ColorUtil.clamp01(f * 0.7F + this.getSelectAnimation(gradientstop).getFloat() * 0.3F);
                if (f1 > 0.01F) {
                    float f10 = uirect.x;
                    float f5 = uirect.y;
                    float f11 = uirect.width;
                    float f12 = uirect.height;
                    float f6 = f1 * 0.55F;
                    Render2DUtil.drawRoundedRect(f10, f5, f11, f12, 4.0F, 1.0F, this.withOpacity(color, f6));
                }

                UiRect uirect1 = getRemoveButtonRect(uirect);
                this.fillRoundedRect(uirect1.x, uirect1.y, uirect1.width, uirect1.height, 3.0F, gradientstop.getColor());
                if (f > 0.01F) {
                    float f9 = uirect1.x - 1.2F;
                    float f7 = uirect1.y - 1.2F;
                    Render2DUtil.drawOutline(f9, f7, uirect1.width + 2.4F, uirect1.height + 2.4F, 3.5F, 0.5F, 0.6F, this.withOpacity(Color.WHITE, f));
                }

                this.drawValueField(7, gradientstop, getColorButtonRect(uirect), 8.0F, false, 1.0F);
                this.drawValueField(8, gradientstop, getAlphaFieldRect(uirect), 8.0F, false, 1.0F);
                UiRect uirect2 = getCloseButtonRect(uirect);
                Color color1 = flag && uirect2.contains(i, j) ? Color.WHITE : VALUE_COLOR;
                float f8 = flag ? 0.9F : 0.25F;
                Color color2 = this.withOpacity(color1, f8);
                double d0 = uirect2.x + 2.0F;
                Render2DUtil.drawPlus(color2, d0, uirect2.y + 2.0F, uirect2.width - 4.0F, 0.9F, 45.0F);
            }
        }
    }

    private void drawHueSlider() {
        float f = this.getHsb(this.draggedStop)[0];
        Render2DUtil.drawGradientStops(
            this.hueRect.x,
            this.hueRect.y,
            this.hueRect.width,
            this.hueRect.height,
            this.hueRect.height / 2.0F,
            1.0F,
            HUE_STOPS,
            HUE_COLORS,
            false,
            90.0F,
            0.0F,
            this.dragOffsetX
        );
        UiRect uirect = getKnobRect(this.hueRect, f);
        Color color = Color.getHSBColor(f, 1.0F, 1.0F);
        this.drawKnob(uirect, 0.0F, color);
    }

    private void drawLabel(String s, UiRect uirect, float f, Color color, float f1) {
        MSDFFontAtlas msdffontatlas = VoilClient.fonts.getSelectedFont();
        float f2 = centerX(uirect) - msdffontatlas.getWidth(s, f) / 2.0F;
        float f3 = centerY(uirect) - msdffontatlas.getLineHeight(f) / 2.0F;
        msdffontatlas.render(s, f2, f3, f, this.withOpacity(color, f1).getRGB());
    }

    private void drawKnob(UiRect uirect, float f, Color color) {
        float f1 = uirect.x - f;
        float f2 = uirect.y - f;
        float f3 = uirect.width + f * 2.0F;
        float f4 = uirect.height + f * 2.0F;
        float f5 = Math.min(f3, f4) / 2.0F;
        float f7 = f1 - 0.8F;
        float f6 = f2 - 0.8F;
        Render2DUtil.drawRoundedRect(f7, f6, f3 + 1.6F, f4 + 1.6F, f5 + 0.8F, 1.0F, this.withOpacity(Color.WHITE, 1.0F));
        this.fillRoundedRect(f1, f2, f3, f4, f5, color);
    }

    private void fillRoundedRect(float f, float f1, float f2, float f3, float f4, Color color) {
        if (color.getAlpha() < 255) {
            float[] afloat1 = new float[]{0.0F, 1.0F};
            Color[] acolor1 = new Color[]{color, color};
            float f5 = this.dragOffsetX;
            float f6 = 2.0F;
            float f7 = 90.0F;
            Color[] acolor = acolor1;
            float[] afloat = afloat1;
            Render2DUtil.drawGradientStops(f, f1, f2, f3, f4, 1.0F, afloat, acolor, false, f7, f6, f5);
        } else {
            Render2DUtil.drawRoundedRect(f, f1, f2, f3, f4, 1.0F, this.withOpacity(color, 1.0F));
        }
    }

    private void beginEdit(int i, GradientStop gradientstop) {
        this.editingIndex = i;
        this.selectedStop = gradientstop;
        this.hexInput = this.getFieldText(i, gradientstop);
        this.editingHex = true;
        this.editStartColor = gradientstop.getColor();
        this.editStartAngle = this.setting.getAngle();
    }

    private boolean isEditing(int i, GradientStop gradientstop) {
        return this.editingIndex == i && this.selectedStop == gradientstop;
    }

    public void mouseScrolled(double d0, double d1, double d2) {
        if (this.open && !this.dropdownOpen && d2 != 0.0) {
            this.layout();
            if (this.hitTestStops(d0, d1)) {
                int i1 = this.hoverIndex;
                if (!isHexField(i1)) {
                    int j2 = d2 > 0.0 ? 1 : -1;
                    int k = isShiftDown() ? 10 : 1;
                    int j = j2;
                    int i = j * k;
                    if (this.hoverIndex == 0) {
                        GradientSetting gradientsetting = this.setting;
                        j = this.setting.getAngle();
                        int l = j + i;
                        gradientsetting.setAngle(l);
                    } else {
                        int j1 = this.hoverIndex;
                        GradientStop gradientstop;
                        GradientStop gradientstop4 = gradientstop = this.hoveredStop;
                        int k1 = Integer.parseInt(this.getFieldText(this.hoverIndex, gradientstop)) + i;
                        GradientStop gradientstop1 = gradientstop4;
                        int l1 = j1;
                        this.setStopComponent(l1, gradientstop1, k1);
                    }

                    GradientStop gradientstop2 = this.hoveredStop;
                    int i2 = this.hoverIndex;
                    if (this.isEditing(i2, gradientstop2)) {
                        GradientStop gradientstop3 = this.hoveredStop;
                        this.hexInput = this.getFieldText(this.hoverIndex, gradientstop3);
                        this.editingHex = true;
                    }

                    return;
                }
            }
        }
    }

    private int getComponentCountForLayout() {
        return this.setting.isAllowAlpha() ? 4 : 3;
    }

    private static boolean isDecimalDigit(int i) {
        return i >= 48 && i <= 57;
    }

    private void drawValueField(int i, GradientStop gradientstop, UiRect uirect, float f, boolean flag, float f1) {
        boolean flag1 = this.isEditing(i, gradientstop);
        Color color = GuiTheme.modeSettingFill;
        Color color1 = flag1 ? GuiTheme.accentColor1 : (flag ? Color.WHITE : color.brighter());
        float f2 = !flag1 && !flag ? 0.5F : 0.85F;
        float f12 = uirect.x;
        float f7 = uirect.y;
        Render2DUtil.drawRoundedRect(f12, f7, uirect.width, uirect.height, 3.0F, 1.0F, this.withOpacity(color, f1));
        f12 = uirect.x;
        float f8 = uirect.y;
        float f13 = uirect.width;
        float f14 = uirect.height;
        float f9 = f2 * f1;
        Render2DUtil.drawOutline(f12, f8, f13, f14, 3.0F, 0.4F, 0.4F, this.withOpacity(color1, f9));
        MSDFFontAtlas msdffontatlas = VoilClient.fonts.getSelectedFont();
        String s = flag1 ? this.hexInput : this.getFieldText(i, gradientstop);
        float f3 = msdffontatlas.getWidth(s, f);
        float f4 = msdffontatlas.getLineHeight(f);
        float f5 = centerX(uirect) - f3 / 2.0F;
        float f6 = centerY(uirect) - f4 / 2.0F;
        if (flag1 && this.editingHex && !s.isEmpty()) {
            f12 = f5 - 1.0F;
            f13 = f3 + 2.0F;
            float f10 = 0.45F * f1;
            Render2DUtil.drawRoundedRect(f12, f6, f13, f4, 1.5F, 1.0F, this.withOpacity(GuiTheme.accentColor1, f10));
        }

        msdffontatlas.render(s, f5, f6, f, this.withOpacity(Color.WHITE, f1).getRGB());
        if (flag1 && !this.editingHex && System.currentTimeMillis() % 1000L < 500L) {
            f12 = f5 + f3 + 0.5F;
            float f11 = 0.8F * f1;
            Render2DUtil.drawRoundedRect(f12, f6, 0.6F, f4, 0.0F, 1.0F, this.withOpacity(Color.WHITE, f11));
        }
    }

    private void drawEditor(int i, int j) {
        float f = this.barTop + 11.0F;
        if (this.isVisibleY(f)) {
            this.drawStopHandles();
        }

        if (this.isHovered(this.modeRect)) {
            this.drawModeButton();
            this.drawAngleDial();
        }

        if (this.isHovered(this.pickerRect)) {
            this.drawPicker();
        }

        if (this.isHovered(this.hueRect)) {
            this.drawHueSlider();
        }

        if (this.alphaRect != null && this.isHovered(this.alphaRect)) {
            this.drawAlphaSlider();
        }

        UiRect uirect = this.previewRect;
        if (this.isHovered(uirect)) {
            float f1 = this.previewRect.x;
            this.drawSectionLabel("HEX", f1, this.angleDialSize);
            GradientStop gradientstop = this.draggedStop;
            this.drawValueField(1, gradientstop, this.previewRect, 8.0F, false, 1.0F);
        }

        if (this.isHovered(this.allRects[0])) {
            this.drawChannelFields();
        }

        this.drawStopList(i, j);
        if (this.isHovered(this.modeRect)) {
            this.drawModeDropdown();
        }
    }

    private List<GradientStop> getStopsReversed() {
        ArrayList<GradientStop> arraylist = new ArrayList<>(this.setting.getStops().reversed());
        if (arraylist.removeIf(gradientstop -> gradientstop == this.draggedStop)) {
            arraylist.addFirst(this.draggedStop);
        }

        return arraylist;
    }

    private void drawStopHandles() {
        float f5 = this.barRect.x;
        float f6 = this.barRect.y;
        float f7 = this.barRect.width;
        float f8 = this.barRect.height;
        float f9 = this.barRect.height / 2.0F;
        float[] afloat = this.setting.getPositions();
        Render2DUtil.drawGradientStops(f5, f6, f7, f8, f9, 1.0F, afloat, this.setting.getColors(), false, 90.0F, this.getAlphaBarHeight(), this.dragOffsetX);
        List<GradientStop> list = this.getStopsReversed().reversed();

        for (GradientStop gradientstop1 : list) {
            float f1 = this.getHoverAnimation(gradientstop1).getFloat();
            float f2 = this.getSelectAnimation(gradientstop1).getFloat() * 0.5F;
            UiRect uirect1 = this.getStopBarRect(gradientstop1);
            if (f1 > 0.01F) {
                float f3 = f2 + 2.5F * f1;
                f5 = uirect1.x - f3;
                float f4 = uirect1.y - f3;
                Render2DUtil.drawRoundedRect(
                    f5, f4, uirect1.width + f3 * 2.0F, uirect1.height + f3 * 2.0F, uirect1.width / 2.0F + f3, 1.0F, GuiTheme.accentColor1
                );
            }

            Color color = gradientstop1.getColor();
            this.drawKnob(uirect1, f2, color);
        }

        for (GradientStop gradientstop2 : list) {
            UiRect uirect2 = this.getStopHandleRect(gradientstop2);
            boolean flag = gradientstop2 == this.draggedStop;
            float f = 7.0F;
            UiRect uirect = uirect2;
            GradientStop gradientstop = gradientstop2;
            this.drawValueField(6, gradientstop, uirect, f, flag, 1.0F);
        }
    }

    private void backspace() {
        if (this.editingHex) {
            this.hexInput = "";
            this.editingHex = false;
        } else if (!this.hexInput.isEmpty()) {
            String s1 = this.hexInput;
            int i = this.hexInput.length();
            this.hexInput = s1.substring(0, i - 1);
        }

        String s = this.hexInput;
        GradientStop gradientstop = this.selectedStop;
        int j = this.editingIndex;
        this.applyHexText(j, gradientstop, s, false);
    }

    private void updateAnimations(int i, int j) {
        List<GradientStop> list = this.setting.getStops();
        if (!list.contains(this.draggedStop)) {
            this.draggedStop = (GradientStop)list.getFirst();
        }

        if (this.editingIndex != -1 && !list.contains(this.selectedStop)) {
            this.editingIndex = -1;
        }

        if (!this.open) {
            this.dropdownOpen = false;
        }

        this.hsbCache.keySet().retainAll(list);
        this.hoverAnimations.keySet().retainAll(list);
        this.selectAnimations.keySet().retainAll(list);
        animateTo(this.openAnimation, this.open ? 1.0 : 0.0);
        animateTo(this.expandAnimation, this.isRadial() ? 1.0 : 0.0);
        animateTo(this.previewAnimation, this.dropdownOpen ? 1.0 : 0.0);
        animateTo(this.angleAnimation, this.dropdownOpen ? 90.0 : 270.0);
        animateTo(this.modeAnimation, this.open && this.modeRect.contains(i, j) ? 1.0 : 0.0);

        for (int k = 0; k < GRADIENT_MODES.length; k++) {
            animateTo(this.stopAnimations[k], this.dropdownOpen && this.getSwatchRect(k).contains(i, j) ? 1.0 : 0.0);
        }

        boolean flag1 = this.open && !this.dropdownOpen;

        for (int l = 0; l < list.size(); l++) {
            GradientStop gradientstop = (GradientStop)list.get(l);
            boolean flag = flag1 && (insetRect(this.getStopBarRect(gradientstop), 2).contains(i, j) || this.getStopRow(l).contains(i, j));
            animateTo(this.getHoverAnimation(gradientstop), gradientstop == this.draggedStop ? 1.0 : 0.0);
            animateTo(this.getSelectAnimation(gradientstop), flag ? 1.0 : 0.0);
        }
    }

    private void selectModeAt(double d0, double d1) {
        this.dropdownOpen = false;
        if (!(this.previewAnimation.getValue() < 0.6)) {
            for (int i = 0; i < GRADIENT_MODES.length; i++) {
                if (contains(this.getSwatchRect(i), d0, d1)) {
                    GradientType gradienttype = GRADIENT_MODES[i];
                    this.setting.setMode(gradienttype);
                    return;
                }
            }
        }
    }

    private void beginDrag(int i, double d0, double d1) {
        this.dragTarget = i;
        this.dragStop(d0, d1);
    }

    private UiAnimation getSelectAnimation(GradientStop gradientstop) {
        return this.hoverAnimations.computeIfAbsent(gradientstop, gradientstop1 -> createAnimation(0.6, 0.15));
    }

    private float[] getHsb(GradientStop gradientstop) {
        float[] afloat = this.selectAnimations.computeIfAbsent(gradientstop, gradientstop1 -> new float[3]);
        Color color = gradientstop.getColor();
        int i = Color.HSBtoRGB(afloat[0], afloat[1], afloat[2]);
        if ((i & 16777215) == (color.getRGB() & 16777215)) {
            return afloat;
        }

        float[] afloat1 = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        if (afloat1[1] > 0.0F && afloat1[2] > 0.0F) {
            afloat[0] = afloat1[0];
        }

        if (afloat1[2] > 0.0F) {
            afloat[1] = afloat1[1];
        }

        afloat[2] = afloat1[2];
        return afloat;
    }

    private void updateAngleFromMouse(double d0, double d1) {
        Vector2d vector2d = this.angleDialRect.getCenter();
        double d2 = d0 - vector2d.x;
        double d3 = d1 - vector2d.y;
        if (!(d2 * d2 + d3 * d3 < 1.0)) {
            int i = (int)Math.round(Math.toDegrees(Math.atan2(d2, -d3)));
            if (isShiftDown()) {
                int j = Math.round(i / 15.0F);
                i = j * 15;
            }

            this.setting.setAngle(i);
        }
    }

    private boolean isLinear() {
        return this.setting.getMode() == GradientType.LINEAR;
    }

    private boolean hitTestStops(double d0, double d1) {
        for (GradientStop gradientstop : this.getStopsReversed()) {
            if (contains(this.getStopHandleRect(gradientstop), d0, d1)) {
                return this.setHoverStop(6, gradientstop);
            }
        }

        if (this.isLinear() && contains(this.addButtonRect, d0, d1)) {
            return this.setHoverStop(0, this.draggedStop);
        }

        if (contains(this.previewRect, d0, d1)) {
            GradientStop gradientstop1 = this.draggedStop;
            return this.setHoverStop(1, gradientstop1);
        }

        for (int i = 0; i < this.getComponentCountForLayout(); i++) {
            if (contains(this.allRects[i], d0, d1)) {
                return this.setHoverStop(2 + i, this.draggedStop);
            }
        }

        List<GradientStop> list = this.setting.getStops();

        for (int j = 0; j < list.size(); j++) {
            UiRect uirect = this.getStopRow(j);
            if (contains(getColorButtonRect(uirect), d0, d1)) {
                return this.setHoverStop(7, (GradientStop)list.get(j));
            }

            if (contains(getAlphaFieldRect(uirect), d0, d1)) {
                return this.setHoverStop(8, (GradientStop)list.get(j));
            }
        }

        return false;
    }

    private void handleRelease(double d0, double d1) {
        GradientStop gradientstop = this.getStopAt(d0, d1);
        if (gradientstop != null) {
            this.draggedStop = gradientstop;
            this.dragTarget = 1;
        } else if (contains(this.barRect, d0, d1)) {
            GradientSetting gradientsetting = this.setting;
            int j = this.percentAt(d0);
            GradientStop gradientstop1 = gradientsetting.addStop(j);
            if (gradientstop1 != null) {
                this.draggedStop = gradientstop1;
                this.dragTarget = 1;
            }
        } else if (contains(this.modeRect, d0, d1)) {
            this.dropdownOpen = true;
        } else if (this.isLinear() && contains(this.angleDialRect, d0, d1)) {
            this.beginDrag(2, d0, d1);
        } else if (contains(this.pickerRect, d0, d1)) {
            this.beginDrag(3, d0, d1);
        } else if (contains(insetRect(this.hueRect, 3), d0, d1)) {
            this.beginDrag(4, d0, d1);
        } else if (this.alphaRect != null && contains(insetRect(this.alphaRect, 3), d0, d1)) {
            this.beginDrag(5, d0, d1);
        } else {
            List<GradientStop> list = this.setting.getStops();

            for (int i = 0; i < list.size(); i++) {
                UiRect uirect = this.getStopRow(i);
                if (contains(getCloseButtonRect(uirect), d0, d1)) {
                    Object object = list.get(i);
                    this.removeStop((GradientStop)object);
                    return;
                }

                if (contains(uirect, d0, d1)) {
                    this.draggedStop = (GradientStop)list.get(i);
                    return;
                }
            }
        }
    }

    private void drawAngleDial() {
        Color color = GuiTheme.modeSettingFill;
        float f = 1.0F - this.expandAnimation.getFloat() * 0.65F;
        float f1 = this.angleDialRect.width / 2.0F;
        Render2DUtil.drawCircle(this.angleDialRect.x, this.angleDialRect.y, f1, 1.0F, this.withOpacity(color, f));
        Render2DUtil.drawCircleOutline(this.angleDialRect.x, this.angleDialRect.y, f1, 1.0F, 0.5F, this.withOpacity(color.brighter(), f));
        double d0 = Math.toRadians(this.setting.getAngle());
        float f2 = centerX(this.angleDialRect) + (float)Math.sin(d0) * (f1 - 3.5F);
        float f3 = centerY(this.angleDialRect) - (float)Math.cos(d0) * (f1 - 3.5F);
        Render2DUtil.drawCircle(f2 - 1.6F, f3 - 1.6F, 1.6F, 1.0F, this.withOpacity(Color.WHITE, f));
        GradientStop gradientstop = this.draggedStop;
        this.drawValueField(0, gradientstop, this.addButtonRect, 8.0F, false, f);
    }

    private boolean isHovered(UiRect uirect) {
        float f = bottom(uirect);
        return this.isVisibleY(f);
    }

    private UiRect getSwatchRect(int i) {
        int j = this.modeDropdownRect.x;
        int k = j + 2;
        j = this.modeDropdownRect.y;
        int l = j + 2 + i * 14;
        j = this.modeDropdownRect.width;
        return new UiRect(k, l, j + -5 + 1, 14);
    }

    private static UiRect getRemoveButtonRect(UiRect uirect) {
        int i = uirect.x;
        int j = i + 2;
        i = uirect.y;
        return new UiRect(j, i + 2, 12, 12);
    }

    private void drawChannelFields() {
        for (int i = 0; i < this.getComponentCountForLayout(); i++) {
            String s = CHANNEL_LABELS[i];
            float f = this.allRects[i].x;
            this.drawSectionLabel(s, f, this.barWidth);
            int j = 2 + i;
            GradientStop gradientstop = this.draggedStop;
            this.drawValueField(j, gradientstop, this.allRects[i], 8.0F, false, 1.0F);
        }
    }

    private static UiRect insetRect(UiRect uirect, int i) {
        int j = uirect.x;
        int l = j - i;
        j = uirect.y;
        int i1 = j - i;
        int k = i + i;
        j = uirect.width;
        return new UiRect(l, i1, j + k, uirect.height + i + i);
    }

    private int getPanelWidth() {
        int i = this.setting.isAllowAlpha() ? 16 : 0;
        return 253 + i;
    }

    private UiRect getStopHandleRect(GradientStop gradientstop) {
        return makeRect(Math.max(this.previewX, Math.min(this.getStopX(gradientstop) - 10.0F, this.previewRight - 20.0F)), this.barTop, 20.0F, 11.0F);
    }

    private void drawSectionLabel(String s, float f, float f1) {
        VoilClient.fonts.getSelectedFont().render(s, f, f1, 7.0F, this.withOpacity(LABEL_COLOR, 1.0F).getRGB());
    }

    private static UiRect getAlphaFieldRect(UiRect uirect) {
        int i = right(uirect);
        int j = i + -41 + 1;
        i = uirect.y;
        return new UiRect(j, i + 1, 24, 14);
    }

    private static boolean contains(UiRect uirect, double d0, double d1) {
        int j = (int)Math.floor(d0);
        int i = (int)Math.floor(d1);
        return uirect.contains(j, i);
    }

    private UiRect getStopBarRect(GradientStop gradientstop) {
        return makeRect(this.getStopX(gradientstop) - 3.0F, centerY(this.barRect) - 9.0F, 6.0F, 18.0F);
    }

    private float getStopX(GradientStop gradientstop) {
        float f = this.barRect.x;
        int k = this.barRect.width;
        int j = gradientstop.getPosition();
        int i = k;
        return f + i * j / 100.0F;
    }

    private static float centerY(UiRect uirect) {
        return uirect.y + uirect.height / 2.0F;
    }

    private boolean isRadial() {
        return this.setting.getMode() == GradientType.RADIAL;
    }

    private static boolean isShiftDown() {
        long i = VoilClient.mc.getWindow().getHandle();
        return GLFW.glfwGetKey(i, 340) == 1 || GLFW.glfwGetKey(i, 344) == 1;
    }

    private static float centerX(UiRect uirect) {
        return uirect.x + uirect.width / 2.0F;
    }

    private static void animateTo(UiAnimation uianimation, double d0) {
        uianimation.setTarget(d0);
        uianimation.update();
    }

    private static UiAnimation createAnimation(double d0, double d1) {
        return new UiAnimation(EasingType.FLUID_SMOOTH).setFluidParams(d0, d1);
    }

    private static String formatMode(GradientType gradienttype) {
        String s = gradienttype.name();
        char c1 = s.charAt(0);
        String s1 = s.substring(1).toLowerCase(Locale.ROOT);
        char c0 = c1;
        return c0 + s1;
    }

    private static float normalize(double d0, float f, float f1) {
        return ColorUtil.clamp01((float)((d0 - f) / f1));
    }

    private boolean setHoverStop(int i, GradientStop gradientstop) {
        this.hoverIndex = i;
        this.hoveredStop = gradientstop;
        return true;
    }

    private static UiRect getKnobRect(UiRect uirect, float f) {
        return makeRect(uirect.x + f * uirect.width - 2.5F, centerY(uirect) - 5.5F, 5.0F, 11.0F);
    }

    private static Color withChannel(Color color, int i, int j) {
        int[] aint = new int[]{color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()};
        aint[i] = i == 3 ? Math.round(Math.clamp(j, 0, 100) * 255.0F / 100.0F) : Math.clamp(j, 0, 255);
        return new Color(aint[0], aint[1], aint[2], aint[3]);
    }

    private void drawModeDropdown() {
        float f = this.previewAnimation.getFloat();
        if (!(f <= 0.01F)) {
            Color color = GuiTheme.modeSettingFill;
            Color color1 = color.brighter();
            float f1 = this.modeDropdownRect.y + Math.max(0.0F, this.modeDropdownRect.height * f) + 1.0F;
            float f2 = ColorUtil.clamp01(f * 1.6F);
            float f9 = this.modeDropdownRect.x;
            float f5 = this.modeDropdownRect.y;
            Render2DUtil.drawRoundedRect(f9, f5, this.modeDropdownRect.width, this.modeDropdownRect.height, 4.0F, 1.0F, this.withOpacity(color, f2));

            for (int i = 0; i < GRADIENT_MODES.length; i++) {
                UiRect uirect = this.getSwatchRect(i);
                if (bottom(uirect) > f1) {
                    break;
                }

                boolean flag = this.setting.getMode() == GRADIENT_MODES[i];
                float f3 = this.stopAnimations[i].getFloat();
                float f4 = ColorUtil.clamp01((flag ? 0.5F : 0.0F) + f3 * 0.35F);
                if (f4 > 0.01F) {
                    f9 = uirect.x;
                    float f6 = uirect.y;
                    float f10 = uirect.width;
                    float f11 = uirect.height;
                    float f7 = f4 * f2;
                    Render2DUtil.drawRoundedRect(f9, f6, f10, f11, 3.0F, 1.0F, this.withOpacity(color1, f7));
                }

                if (flag) {
                    f9 = uirect.x + 2.0F;
                    float f8 = uirect.y + 3.0F;
                    Render2DUtil.drawRoundedRect(f9, f8, 2.0F, uirect.height - 6.0F, 1.0F, 1.0F, this.withOpacity(GuiTheme.accentColor1, f2));
                }

                this.drawLabel(formatMode(GRADIENT_MODES[i]), uirect, 8.0F, ColorUtil.blendColors(VALUE_COLOR, Color.WHITE, flag ? 1.0F : f3), f2);
            }
        }
    }

    private static UiRect makeRect(float f, float f1, float f2, float f3) {
        int i = Math.round(f);
        int j = Math.round(f1);
        int k = Math.round(f + f2);
        int l = k - i;
        k = Math.round(f1 + f3);
        return new UiRect(i, j, l, k - j);
    }

    private UiRect getHexFieldRect() {
        return makeRect(this.fromInnerRight(40.0F), this.getControlY(), 40.0F, 14.0F);
    }

    private static int right(UiRect uirect) {
        int j = uirect.width;
        int i = uirect.x;
        return i + j;
    }

    private String getFieldText(int i, GradientStop gradientstop) {
        Color color = gradientstop.getColor();

        return switch (i) {
            case 0 -> String.valueOf(this.setting.getAngle());
            case 1, 7 -> {
                boolean flag = this.setting.isAllowAlpha();
                yield GradientSetting.toHex(color, flag);
            }
            case 2 -> String.valueOf(color.getRed());
            case 3 -> String.valueOf(color.getGreen());
            case 4 -> String.valueOf(color.getBlue());
            case 5 -> String.valueOf(Math.round(color.getAlpha() * 100.0F / 255.0F));
            case 6, 8 -> String.valueOf(gradientstop.getPosition());
            default -> "";
        };
    }

    private float getPanelHeight() {
        return this.getPanelWidth() + this.setting.getStops().size() * 18 + 2.0F;
    }

    private static UiRect getCloseButtonRect(UiRect uirect) {
        int i = right(uirect);
        int j = i + -13 + 1;
        i = uirect.y;
        return new UiRect(j, i + 3, 10, 10);
    }

    private UiRect getStopRow(int i) {
        return makeRect(this.previewX, this.listTop + i * 18, this.previewWidth, 16.0F);
    }

    private static UiRect getColorButtonRect(UiRect uirect) {
        int i = uirect.x;
        int j = i + 18;
        i = uirect.y;
        int k = i + 1;
        i = uirect.width;
        return new UiRect(j, k, i + -63 + 1, 14);
    }

    private static int bottom(UiRect uirect) {
        int j = uirect.height;
        int i = uirect.y;
        return i + j;
    }

    private void layout() {
        this.previewX = this.getInnerLeft();
        this.previewRight = this.fromInnerRight(0.0F);
        this.previewWidth = this.previewRight - this.previewX;
        float f = this.getBottom();
        this.barRect = makeRect(this.previewX + 5.0F, f + 11.0F, this.previewWidth - 10.0F, 12.0F);
        this.barTop = f + 29.0F;
        float f1 = f + 48.0F;
        this.modeRect = makeRect(this.previewX, f1, 72.0F, 14.0F);
        float f3 = f1 + 16.0F;
        int k = GRADIENT_MODES.length;
        this.modeDropdownRect = makeRect(this.previewX, f3, 72.0F, 4.0F + k * 14);
        this.addButtonRect = makeRect(this.previewRight - 28.0F, f1, 28.0F, 14.0F);
        this.angleDialRect = makeRect(this.previewRight - 48.0F, f1, 14.0F, 14.0F);
        f1 += 24.0F;
        this.pickerLabelY = f1;
        f1 += 10.0F;
        this.pickerRect = makeRect(this.previewX, f1, this.previewWidth, 72.0F);
        f1 += 81.0F;
        this.hueRect = makeRect(this.previewX, f1, this.previewWidth, 6.0F);
        f1 += 16.0F;
        this.alphaRect = null;
        if (this.setting.isAllowAlpha()) {
            this.alphaRect = makeRect(this.previewX, f1, this.previewWidth, 6.0F);
            f1 += 16.0F;
        }

        this.angleDialSize = f1;
        f1 += 10.0F;
        this.previewRect = makeRect(this.previewX, f1, this.previewWidth, 14.0F);
        f1 += 20.0F;
        this.barWidth = f1;
        f1 += 10.0F;
        int i = this.getComponentCountForLayout();
        float f2 = (this.previewWidth - (i - 1) * 3.0F) / i;

        for (int j = 0; j < i; j++) {
            this.allRects[j] = makeRect(this.previewX + j * (f2 + 3.0F), f1, f2, 14.0F);
        }

        this.listTop = f + this.getPanelWidth();
        this.barHeight = this.listTop - 10.0F;
    }

    private void dragStop(double d0, double d1) {
        switch (this.dragTarget) {
            case 1:
                GradientSetting gradientsetting = this.setting;
                GradientStop gradientstop4 = this.draggedStop;
                int j = this.percentAt(d0);
                GradientStop gradientstop = gradientstop4;
                gradientsetting.setStopPosition(gradientstop, j);
                break;
            case 2:
                this.updateAngleFromMouse(d0, d1);
                break;
            case 3:
                float[] afloat1 = this.getHsb(this.draggedStop);
                afloat1[1] = normalize(d0, this.pickerRect.x, this.pickerRect.width);
                afloat1[2] = 1.0F - normalize(d1, this.pickerRect.y, this.pickerRect.height);
                GradientStop gradientstop2 = this.draggedStop;
                this.setStopFromHsb(gradientstop2, afloat1);
                break;
            case 4:
                float[] afloat = this.getHsb(this.draggedStop);
                afloat[0] = normalize(d0, this.hueRect.x, this.hueRect.width);
                GradientStop gradientstop3 = this.draggedStop;
                this.setStopFromHsb(gradientstop3, afloat);
                break;
            case 5:
                Color color = this.draggedStop.getColor();
                int i = Math.round(normalize(d0, this.alphaRect.x, this.alphaRect.width) * 255.0F);
                GradientSetting gradientsetting1 = this.setting;
                GradientStop gradientstop5 = this.draggedStop;
                Color color1 = new Color(color.getRed(), color.getGreen(), color.getBlue(), i);
                GradientStop gradientstop1 = gradientstop5;
                gradientsetting1.setStopColor(gradientstop1, color1);
        }
    }

    private void restoreColor() {
        int i = this.editingIndex;
        this.editingIndex = -1;
        if (i == 0) {
            int j = this.editStartAngle;
            this.setting.setAngle(j);
        } else {
            Color color = this.editStartColor;
            GradientStop gradientstop = this.selectedStop;
            this.setting.setStopColor(gradientstop, color);
        }
    }

    private void drawPicker() {
        float[] afloat = this.getHsb(this.draggedStop);
        Color color = Color.getHSBColor(afloat[0], 1.0F, 1.0F);
        float f2 = this.pickerRect.x;
        this.drawSectionLabel("PICKER", f2, this.pickerLabelY);
        float f4 = this.pickerRect.x;
        float f5 = this.pickerRect.y;
        float f6 = this.pickerRect.width;
        float f3 = this.pickerRect.height;
        Render2DUtil.drawRectGradient(
            f4,
            f5,
            f6,
            f3,
            3.0F,
            1.0F,
            this.withOpacity(Color.BLACK, 1.0F),
            this.withOpacity(Color.WHITE, 1.0F),
            this.withOpacity(Color.BLACK, 1.0F),
            this.withOpacity(color, 1.0F)
        );
        float f = this.pickerRect.x + afloat[1] * this.pickerRect.width;
        float f1 = this.pickerRect.y + (1.0F - afloat[2]) * this.pickerRect.height;
        Render2DUtil.drawCircle(f - 4.5F, f1 - 4.5F, 4.5F, 1.0F, this.withOpacity(Color.WHITE, 1.0F));
        f4 = f - 3.0F;
        f5 = f1 - 3.0F;
        Color color1 = this.draggedStop.getColor();
        Render2DUtil.drawCircle(f4, f5, 3.0F, 1.0F, this.withOpacity(stripAlpha(color1), 1.0F));
    }

    private void cycleEditField(int i) {
        int j = this.editingIndex;
        GradientStop gradientstop = this.selectedStop;
        this.commitHexEdit();
        ArrayList arraylist = new ArrayList();
        ArrayList arraylist1 = new ArrayList();
        List<GradientStop> list = this.setting.getStops();

        for (GradientStop gradientstop1 : list) {
            arraylist.add(6);
            arraylist1.add(gradientstop1);
        }

        if (this.isLinear()) {
            arraylist.add(0);
            arraylist1.add(this.draggedStop);
        }

        int l = 1;

        while (true) {
            int k = this.getComponentCountForLayout();
            if (l >= k + 2) {
                for (GradientStop gradientstop2 : list) {
                    arraylist.add(7);
                    arraylist1.add(gradientstop2);
                    arraylist.add(8);
                    arraylist1.add(gradientstop2);
                }

                l = -1;

                for (int i1 = 0; i1 < arraylist.size(); i1++) {
                    if ((Integer)arraylist.get(i1) == j && arraylist1.get(i1) == gradientstop) {
                        l = i1;
                        break;
                    }
                }

                int j1 = Math.floorMod(l + i, arraylist.size());
                this.draggedStop = (GradientStop)arraylist1.get(j1);
                this.beginEdit((Integer)arraylist.get(j1), this.draggedStop);
                return;
            }

            arraylist.add(l);
            arraylist1.add(this.draggedStop);
            l++;
        }
    }

    private int getHexOffset(int i, String s) {
        if (!isHexField(i)) {
            return 3;
        }

        int l = this.getComponentCount();
        int k = s.startsWith("#") ? 1 : 0;
        int j = l;
        return j + k;
    }

    private boolean isVisibleY(float f) {
        return f <= this.dragOffsetY;
    }

    private Color withOpacity(Color color, float f) {
        float f1 = f * this.dragOffsetX;
        Color color1 = color;
        return ColorUtil.withAlpha(color1, f1);
    }

    private int percentAt(double d0) {
        return Math.round(normalize(d0, this.barRect.x, this.barRect.width) * 100.0F);
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
