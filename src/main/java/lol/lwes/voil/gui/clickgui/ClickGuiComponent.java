package lol.lwes.voil.gui.clickgui;

import dev.prestigeclient.renderlib.api.font.MSDFFontAtlas;
import java.util.ArrayList;
import java.util.EnumSet;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.config.GuiTheme;
import lol.lwes.voil.event.InputEventType;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.render.GuiScale;
import lol.lwes.voil.util.render.RectBounds;
import lol.lwes.voil.util.render.Render2DUtil;
import net.minecraft.client.util.math.MatrixStack;

public abstract class ClickGuiComponent {
    private static final EnumSet activeInputs = EnumSet.noneOf(InputEventType.class);
    private float height2;
    private float width2;
    protected Setting setting;
    private static String tooltipText;
    private static int tooltipX;
    private static int tooltipY;
    protected float height;
    protected float width;

    public static boolean isPointInRect(float f, float f1, float f2, float f3, double d0, double d1) {
        return d0 >= f && d0 <= f2 && d1 >= f1 && d1 <= f3;
    }

    public void mouseClicked() {
    }

    public double getX() {
        return this.height;
    }

    public static void renderTooltip() {
        if (tooltipText != null) {
            String s = tooltipText;
            int i = tooltipX;
            int j = tooltipY;
            tooltipText = null;
            ArrayList arraylist = new ArrayList();
            StringBuilder stringbuilder = new StringBuilder();

            for (String s1 : s.split(" ")) {
                MSDFFontAtlas msdffontatlas1 = VoilClient.fonts.getSelectedFont();
                String s2 = String.valueOf(stringbuilder);
                if (msdffontatlas1.getWidth(s2 + s1 + " ") > 150) {
                    arraylist.add(stringbuilder.toString());
                    stringbuilder = new StringBuilder();
                }

                stringbuilder.append(s1).append(" ");
            }

            if (!stringbuilder.isEmpty()) {
                arraylist.add(stringbuilder.toString().trim());
            }

            int l = arraylist.size() * 12;
            int j1 = l + 8;
            l = i + 10;
            int k1 = l - 150;
            l = j - j1;
            int l1 = l + -4 + 1;
            l = (int)GuiScale.getScaledWidth();
            l -= 136;
            k1 = Math.min(k1, l + -6 + 1);
            k1 = Math.max(k1, 5);
            l1 = Math.max(l1, 5);
            float f1 = k1;
            float f = l1;
            Render2DUtil.drawRoundedRect(f1, f, 136, j1, 2.0F, 1.0F, GuiTheme.settingsHeader.brighter());

            for (int k = 0; k < arraylist.size(); k++) {
                MSDFFontAtlas msdffontatlas = VoilClient.fonts.getSelectedFont();
                String s4 = (String)arraylist.get(k);
                float f2 = k1 + 4;
                int i2 = l1 + 4;
                int i1 = k * 12;
                l = i2;
                msdffontatlas.render(s4, f2, l + i1, 10.0F, GuiTheme.normalTextColor.getRGB());
            }
        }
    }

    void init() {
    }

    public float getHeight() {
        return this.height2;
    }

    public void render() {
    }

    public void setPosition(float f, float f1) {
        this.height = f;
        this.width = f1;
    }

    public void mouseDragged() {
    }

    public void setY(float f) {
        this.width = f;
    }

    public float getY() {
        return this.width;
    }

    public void mouseScrolled() {
    }

    public void setHeight(float f) {
        this.height2 = f;
    }

    public Setting getSetting() {
        return this.setting;
    }

    public void charTyped(char chr, int modifiers) {
    }

    public static void clearActiveInputs() {
        activeInputs.clear();
    }

    public void mouseReleased() {
    }

    public final void addActiveInput(InputEventType inputeventtype) {
        activeInputs.add(inputeventtype);
    }

    public void keyReleased() {
    }

    public RectBounds getBounds() {
        return new RectBounds(this.height, this.width, this.width2, this.height2);
    }

    public static boolean isInputActive(InputEventType inputeventtype) {
        return activeInputs.contains(inputeventtype);
    }

    public void setTooltip(String s, int i, int j) {
        tooltipText = s;
        tooltipX = i;
        tooltipY = j;
    }

    public void mouseClicked2(double d0, double d1, int i) {
        this.mouseClicked();
    }

    public void render2(MatrixStack matrixstack, int i, int j, float f) {
        this.render();
    }

    public void mouseDragged2(double d0, double d1, int i, double d2, double d3) {
        this.mouseDragged();
    }

    public void mouseScrolled2(double d0, double d1, double d2, double d3) {
        this.mouseScrolled();
    }

    public void mouseReleased2(double d0, double d1, int i) {
        this.mouseReleased();
    }

    public void keyReleased2(int i, int j, int k) {
        this.keyReleased();
    }
}
