package lol.lwes.voil.gui.hudeditor;

import dev.prestigeclient.renderlib.api.font.MSDFFontAtlas;
import java.awt.Color;
import java.util.List;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.api.ApiClient;
import lol.lwes.voil.config.GuiTheme;
import lol.lwes.voil.config.HudLayout;
import lol.lwes.voil.gui.BaseScreen;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.module.ModuleManager;
import lol.lwes.voil.util.render.ColorUtil;
import lol.lwes.voil.util.render.GuiScale;
import lol.lwes.voil.util.render.Render2DUtil;
import lol.lwes.voil.util.render.RenderManager;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class HudEditorScreen extends BaseScreen {
    private Module draggedModule = null;
    private Module scaleTarget = null;
    private int dragStartX;
    private int dragStartY;
    private float startScale;
    private int gridSize = 10;
    private boolean dragging = true;
    private boolean popupOpen = false;
    private boolean popupClosing = false;
    private float popupX;
    private float popupY;
    private long popupOpenTime;
    private long popupCloseTime;
    private float popupWidth = 130.0F;
    private float headerHeight = 14.0F;
    private float rowHeight = 14.0F;
    private float topPadding = 4.0F;
    private float bottomPadding = 4.0F;
    private long popupOpenDuration = 220L;
    private long popupCloseDuration = 130L;

    public HudEditorScreen() {
        super(Text.of("HUD Editor"));
    }

    private List getHudModules() {
        return VoilClient.getHudLayout().getModules();
    }

    private float[] getPopupPosition(int i, int j) {
        float f = this.getPopupHeight();
        float f1 = this.popupX;
        float f2 = this.popupY;
        if (f1 + this.popupWidth > i - 2) {
            f1 = i - this.popupWidth - 2.0F;
        }

        if (f2 + f > j - 2) {
            f2 = j - f - 2.0F;
        }

        if (f1 < 2.0F) {
            f1 = 2.0F;
        }

        if (f2 < 2.0F) {
            f2 = 2.0F;
        }

        return new float[]{f1, f2};
    }

    private static float clamp01(float f) {
        return f < 0.0F ? 0.0F : (f > 1.0F ? 1.0F : f);
    }

    private void updateDraggedModulePosition(Click click) {
        int k1 = (int)getMouseX(click);
        int j1 = this.dragStartX;
        int i1 = k1;
        int i = i1 - j1;
        k1 = (int)getMouseY(click);
        j1 = this.dragStartY;
        i1 = k1;
        int j = i1 - j1;
        i = Math.min(getScreenWidth(), Math.max(0, i));
        j = Math.min(getScreenHeight(), Math.max(0, j));
        Module module1 = this.draggedModule;
        float[] afloat = this.getElementSize(module1);
        int k = (int)afloat[0];
        int l = (int)afloat[1];
        i1 = Math.min(getScreenWidth(), Math.max(0, i + k));
        i = i1 - k;
        i1 = Math.min(getScreenHeight(), Math.max(0, j + l));
        j = i1 - l;
        if (this.dragging) {
            i = this.snapToGrid(i);
            j = this.snapToGrid(j);
        }

        HudLayout hudlayout = VoilClient.getHudLayout();
        Module module = this.draggedModule;
        hudlayout.setX(module, i);
        HudLayout hudlayout1 = VoilClient.getHudLayout();
        Module module2 = this.draggedModule;
        hudlayout1.setY(module2, j);
    }

    private static int getScreenHeight() {
        return (int)GuiScale.getGuiHeight();
    }

    private static float easeOut(float f) {
        if (f <= 0.0F) {
            return 0.0F;
        }

        if (f >= 1.0F) {
            return 1.0F;
        }

        float f1 = 1.70158F + 1.0F;
        float f2 = f - 1.0F;
        return 1.0F + f1 * f2 * f2 * f2 + 1.70158F * f2 * f2;
    }

    private int getPopupRowAt(double d0, double d1, int i, int j) {
        if (this.popupOpen && !this.popupClosing) {
            float[] afloat = this.getPopupPosition(i, j);
            float f = afloat[0];
            float f1 = afloat[1] + this.topPadding + this.headerHeight;
            if (d0 < f + 2.0F || d0 > f + this.popupWidth - 2.0F) {
                return -1;
            } else {
                return !(d1 < f1) && !(d1 > f1 + this.rowHeight * this.getHudModules().size()) ? (int)((d1 - f1) / this.rowHeight) : -1;
            }
        } else {
            return -1;
        }
    }

    private float getPopupAlpha() {
        if (!this.popupOpen) {
            return 0.0F;
        } else {
            long i = System.currentTimeMillis();
            if (this.popupClosing) {
                float f = 1.0F - (float)(i - this.popupCloseTime) / (float)this.popupCloseDuration;
                return clamp01(f);
            } else {
                float f1 = ((float)(i - this.popupOpenTime) / (float)this.popupOpenDuration - 0.35F) / 0.55F;
                return clamp01(f1);
            }
        }
    }

    private float getPopupHeight() {
        return this.topPadding + this.headerHeight + this.rowHeight * this.getHudModules().size() + this.bottomPadding;
    }

    private void updatePopup() {
        if (this.popupOpen && this.popupClosing && System.currentTimeMillis() - this.popupCloseTime >= this.popupCloseDuration) {
            this.popupOpen = false;
            this.popupClosing = false;
        }
    }

    private static float easeInOut(float f) {
        if (f <= 0.0F) {
            return 0.0F;
        } else {
            return f >= 1.0F ? 1.0F : (1.70158F + 1.0F) * f * f * f - 1.70158F * f * f;
        }
    }

    private int snapToGrid(int i) {
        int l = Math.round((float)i / this.gridSize);
        int k = this.gridSize;
        int j = l;
        return j * k;
    }

    private boolean isInsidePopup(double d0, double d1, int i, int j) {
        if (this.popupOpen && !this.popupClosing) {
            float[] afloat = this.getPopupPosition(i, j);
            return d0 >= afloat[0] && d0 <= afloat[0] + this.popupWidth && d1 >= afloat[1] && d1 <= afloat[1] + this.getPopupHeight();
        } else {
            return false;
        }
    }

    private int getHandleSize() {
        return 16;
    }

    private float[] getElementSize(Module module) {
        float f = VoilClient.getHudLayout().getScale(module);
        int i = VoilClient.getHudLayout().getWidth(module);
        int j = VoilClient.getHudLayout().getHeight(module);
        return new float[]{i * f, j * f};
    }

    private void startClosingPopup() {
        if (this.popupOpen && !this.popupClosing) {
            this.popupClosing = true;
            this.popupCloseTime = System.currentTimeMillis();
        }
    }

    private static double getMouseX(Click click) {
        double d0 = click.x();
        float f = GuiScale.getGuiScale();
        return GuiScale.toGuiUnits(d0, f);
    }

    private float getPopupProgress() {
        if (!this.popupOpen) {
            return 0.0F;
        } else {
            long i = System.currentTimeMillis();
            if (this.popupClosing) {
                float f1 = 1.0F - (float)(i - this.popupCloseTime) / (float)this.popupCloseDuration;
                float f = clamp01(f1);
                return easeInOut(f);
            } else {
                float f2 = (float)(i - this.popupOpenTime) / (float)this.popupOpenDuration;
                float f3 = clamp01(f2);
                return easeOut(f3);
            }
        }
    }

    private void renderHudElements(DrawContext drawcontext, int i, int j) {
        if (VoilClient.getHudLayout() != null) {
            int k = getScreenWidth();
            int l = getScreenHeight();
            Render2DUtil.drawRoundedRect(k / 2.0F - 0.5F, 0.0F, 1.0F, l, 0.0F, 0.0F, new Color(255, 255, 255, 100));
            float f6 = l / 2.0F - 0.5F;
            Render2DUtil.drawRoundedRect(0.0F, f6, k, 1.0F, 0.0F, 0.0F, new Color(255, 255, 255, 100));

            for (Module module : VoilClient.getHudLayout().getModules()) {
                int i3 = VoilClient.getHudLayout().getX(module);
                int i1 = i3 + -4 + 1;
                i3 = VoilClient.getHudLayout().getY(module);
                int j1 = i3 + -4 + 1;
                boolean flag = VoilClient.getHudLayout().isVisible(module);
                boolean flag1 = VoilClient.getHudLayout().isRegistered(module);
                if (flag1 && flag) {
                    float[] afloat = this.getElementSize(module);
                    int k1 = (int)(afloat[0] + 6.0F);
                    int l1 = (int)(afloat[1] + 6.0F);
                    int i2 = this.hitTestElement(module, i, j);
                    int j2 = this.getHandleSize();
                    i3 = i1 + k1;
                    int k2 = i3 - j2;
                    i3 = j1 + l1;
                    int l2 = i3 - j2;
                    Color color1 = Color.GREEN;
                    Color color2 = Color.WHITE;
                    float f14 = i1;
                    float f15 = j1;
                    float f16 = k1;
                    float f17 = l1;
                    Color color = i2 == 1 ? color1 : color2;
                    float f = 1.0F;
                    float f1 = 0.0F;
                    float f2 = 0.0F;
                    float f3 = f17;
                    float f4 = f16;
                    float f5 = f15;
                    Render2DUtil.drawOutline(f14, f5, f4, f3, f2, f1, f, color);
                    double d7 = k2;
                    double d9 = l2 + j2 - (double)j2 / 4.0F;
                    double d11 = k2 + j2 - (double)j2 / 4.0F;
                    double d13 = l2;
                    color = i2 == 2 ? color1 : color2;
                    double d1 = d13;
                    double d3 = d11;
                    double d5 = d9;
                    double d0 = d7;
                    Render2DUtil.drawLine(d0, d5, d3, d1, color);
                    double d8 = k2 + j2 / 2.0;
                    double d10 = l2 + j2 - (double)j2 / 4.0F;
                    double d12 = k2 + j2 - (double)j2 / 4.0F;
                    double d14 = l2 + j2 / 2.0;
                    color = i2 == 2 ? color1 : color2;
                    double d2 = d14;
                    double d4 = d12;
                    double d6 = d10;
                    d0 = d8;
                    Render2DUtil.drawLine(d0, d6, d4, d2, color);
                }
            }

            int j4 = k + -71 + 1;
            int j3 = j4 - 6;
            j4 = l + -17 + 1;
            int k3 = j4 - 6;
            boolean flag2 = i >= j3 && i <= j3 + 70 && j >= k3 && j <= k3 + 16;
            Color color3 = new Color(0, 0, 0, flag2 ? 140 : 110);
            Color color4 = this.dragging ? new Color(80, 255, 140, 220) : new Color(255, 255, 255, 140);
            float f13 = j3;
            float f7 = k3;
            Render2DUtil.drawRoundedRect(f13, f7, 70, 16, 3.0F, 1.0F, color3);
            f13 = j3;
            float f8 = k3;
            Render2DUtil.drawOutline(f13, f8, 70, 16, 3.0F, 0.3F, 0.4F, color4);
            int l3 = j3 + 4;
            int i4 = k3 + 4 / 2;
            f13 = l3;
            float f9 = i4;
            Render2DUtil.drawOutline(f13, f9, 12, 12, 12 / 2.0F, 0.3F, 0.4F, color4);
            if (this.dragging) {
                float f10 = 3.0F;
                float f11 = l3 + (12 - f10 * 2.0F) / 2.0F;
                float f12 = i4 + (12 - f10 * 2.0F) / 2.0F;
                Render2DUtil.drawCircle(f11, f12, f10, 1.0F, color4);
            }

            MSDFFontAtlas msdffontatlas = VoilClient.fonts.getSelectedFont();
            j4 = l3 + 12;
            msdffontatlas.render("Snap", j4 + 4, k3 + 2 / 2.0F, new Color(255, 255, 255, 210).getRGB());
            this.renderPopup(i, j, k, l);
            this.updatePopup();
        }
    }

    private void openPopup(float f, float f1) {
        this.popupOpen = true;
        this.popupClosing = false;
        this.popupX = f;
        this.popupY = f1;
        this.popupOpenTime = System.currentTimeMillis();
    }

    private int hitTestElement(Module module, int i, int j) {
        int j2 = VoilClient.getHudLayout().getX(module);
        int k = j2 + -4 + 1;
        j2 = VoilClient.getHudLayout().getY(module);
        int l = j2 + -4 + 1;
        float[] afloat = this.getElementSize(module);
        int i1 = (int)(afloat[0] + 6.0F);
        int j1 = (int)(afloat[1] + 6.0F);
        boolean flag = i >= k && i <= k + i1 && j >= l && j <= l + j1;
        int k1 = this.getHandleSize();
        j2 = k + i1;
        int l1 = j2 - k1;
        j2 = l + j1;
        int i2 = j2 - k1;
        boolean flag1 = i >= l1 && i <= l1 + k1 && j >= i2 && j <= i2 + k1;
        if (this.draggedModule == module) {
            return 1;
        } else if (this.scaleTarget == module) {
            return 2;
        } else if (flag1) {
            return 2;
        } else {
            return flag ? 1 : 0;
        }
    }

    private void renderPopup(int i, int j, int k, int l) {
        if (this.popupOpen) {
            float f = this.getPopupProgress();
            if (!(f <= 0.001F)) {
                float f1 = this.getPopupAlpha();
                float f2 = this.getPopupHeight();
                float[] afloat = this.getPopupPosition(k, l);
                float f3 = afloat[0];
                float f4 = afloat[1];
                float f5 = this.popupWidth * f;
                float f6 = f2 * f;
                float f7 = this.popupX + (f3 - this.popupX) * f;
                float f8 = this.popupY + (f4 - this.popupY) * f;
                Math.min(1.0F, f);
                Color color2 = Color.black;
                Render2DUtil.drawRoundedRect(f7, f8, f5, f6, 4.0F, 1.0F, ColorUtil.withAlpha(color2, 140));
                Render2DUtil.drawBlurRect(f7, f8, f5, f6, 4.0F, 16.0F);
                Render2DUtil.drawOutline(f7, f8, f5, f6, 4.0F, 0.4F, 0.6F, GuiTheme.accentColor1);
                if (!(f1 <= 0.01F)) {
                    VoilClient.fonts.getGreyscale().render("HUD modules", f3 + 6.0F, f4 + this.topPadding, GuiTheme.accentColor1);
                    List list = this.getHudModules();
                    float f9 = f4 + this.topPadding + this.headerHeight;
                    int i1 = -1;
                    if (!this.popupClosing) {
                        i1 = this.getPopupRowAt(i, j, k, l);
                    }

                    for (int j1 = 0; j1 < list.size(); j1++) {
                        Module module = (Module)list.get(j1);
                        boolean flag = VoilClient.getHudLayout().isVisible(module);
                        float f10 = f9 + j1 * this.rowHeight;
                        if (j1 == i1) {
                            Color color = new Color(255, 255, 255, (int)(22.0F * f1));
                            Render2DUtil.drawRoundedRect(f3 + 2.0F, f10, this.popupWidth - 4.0F, this.rowHeight, 3.0F, 1.0F, color);
                        }

                        float f11 = f3 + 6.0F;
                        float f12 = f10 + (this.rowHeight - 8) / 2.0F;
                        Color color1 = flag
                            ? new Color(GuiTheme.accentColor1.getRed(), GuiTheme.accentColor1.getGreen(), GuiTheme.accentColor1.getBlue(), (int)(235.0F * f1))
                            : new Color(255, 255, 255, (int)(155.0F * f1));
                        Render2DUtil.drawOutline(f11, f12, 8, 8, 8 / 2.0F, 0.3F, 0.4F, color1);
                        if (flag) {
                            float f13 = 2.0F;
                            Render2DUtil.drawCircle(f11 + (8 - f13 * 2.0F) / 2.0F, f12 + (8 - f13 * 2.0F) / 2.0F, f13, 1.0F, color1);
                        }

                        int k1 = flag ? new Color(255, 255, 255, (int)(235.0F * f1)).getRGB() : new Color(200, 200, 200, (int)(180.0F * f1)).getRGB();
                        VoilClient.fonts.getSelectedFont().render(module.getName(), f11 + 8 + 5.0F, f10 + (this.rowHeight - 14.0F) / 2.0F, k1);
                    }
                }
            }
        }
    }

    private static double getMouseY(Click click) {
        double d0 = click.y();
        float f = GuiScale.getGuiScale();
        return GuiScale.toGuiUnits(d0, f);
    }

    private static int getScreenWidth() {
        return (int)GuiScale.getGuiWidth();
    }

    private void applyScaleDrag(Click click) {
        float f = (float)(this.startScale + (getMouseX(click) - this.dragStartX + getMouseY(click) - this.dragStartY) / 250.0);
        float[] afloat = VoilClient.getHudLayout().getScaleRange();
        float f1 = Math.max(afloat[0], Math.min(afloat[1], f));
        HudLayout hudlayout = VoilClient.getHudLayout();
        Module module = this.scaleTarget;
        hudlayout.setScale(module, f1);
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (click.button() == 0) {
            if (this.scaleTarget != null) {
                this.applyScaleDrag(click);
                this.scaleTarget = null;
            }

            if (this.draggedModule != null) {
                this.updateDraggedModulePosition(click);
                this.draggedModule = null;
            }

            return true;
        } else {
            return super.mouseReleased(click);
        }
    }

    @Override
    public boolean mouseDragged(Click click, double d0, double d1) {
        if (click.button() == 0) {
            if (this.scaleTarget != null) {
                this.applyScaleDrag(click);
            }

            if (this.draggedModule != null) {
                this.updateDraggedModulePosition(click);
            }

            return true;
        } else {
            return super.mouseDragged(click, d0, d1);
        }
    }

    @Override
    public boolean mouseClicked(Click click, boolean flag) {
        if (VoilClient.getHudLayout() == null) {
            return super.mouseClicked(click, flag);
        }

        int i = getScreenWidth();
        int j = getScreenHeight();
        if (this.popupOpen && !this.popupClosing) {
            double d1 = getMouseX(click);
            double d0 = getMouseY(click);
            if (this.isInsidePopup(d1, d0, i, j)) {
                if (click.button() == 0) {
                    int k = this.getPopupRowAt(getMouseX(click), getMouseY(click), i, j);
                    if (k >= 0) {
                        Module module = (Module)this.getHudModules().get(k);
                        ModuleManager modulemanager = ModuleManager.INSTANCE;
                        Class oclass = module.getClass();
                        modulemanager.getModuleUnchecked(oclass).toggle();
                    }
                }

                return true;
            }

            this.startClosingPopup();
        }

        if (click.button() == 1) {
            this.openPopup((float)getMouseX(click), (float)getMouseY(click));
            return true;
        }

        int i2 = i + -71 + 1;
        int l = i2 - 6;
        int i1 = j + -17 + 1 - 6;
        if (click.button() == 0 && getMouseX(click) >= l && getMouseX(click) <= l + 70 && getMouseY(click) >= i1 && getMouseY(click) <= i1 + 16) {
            this.dragging = !this.dragging;
            return true;
        }

        if (click.button() == 0 || click.button() == 2) {
            for (Module module1 : VoilClient.getHudLayout().getModules()) {
                int j1 = VoilClient.getHudLayout().getX(module1);
                int k1 = VoilClient.getHudLayout().getY(module1);
                boolean flag1 = VoilClient.getHudLayout().isRegistered(module1);
                boolean flag2 = VoilClient.getHudLayout().isVisible(module1);
                if (flag1 && flag2) {
                    int j2 = (int)getMouseY(click);
                    int l1 = this.hitTestElement(module1, (int)getMouseX(click), j2);
                    if (click.button() == 0) {
                        if (l1 == 1) {
                            this.draggedModule = module1;
                            this.dragStartX = (int)(getMouseX(click) - j1);
                            this.dragStartY = (int)(getMouseY(click) - k1);
                        } else if (l1 == 2) {
                            this.startScale = VoilClient.getHudLayout().getScale(module1);
                            this.scaleTarget = module1;
                            this.dragStartX = (int)getMouseX(click);
                            this.dragStartY = (int)getMouseY(click);
                        }
                    } else if (click.button() == 2 && l1 != 0) {
                        VoilClient.getHudLayout().setScale(module1, 1.0F);
                    }
                }
            }
        }

        return super.mouseClicked(click, flag);
    }

    @Override
    public void render(DrawContext drawcontext, int i, int j, float f1) {
        RenderManager.beginHud();
        RenderManager.flush();
        float f = GuiScale.getGuiScale();
        int k = (int)GuiScale.toGuiUnits(i, f);
        int l = (int)GuiScale.toGuiUnits(j, f);
        GuiScale.pushScale(f);

        try {
            RenderManager.beginHud();
            this.renderHudElements(drawcontext, k, l);
            RenderManager.flush();
        } finally {
            GuiScale.popScale();
        }
    }

    @Override
    public void close() {
        ApiClient.INSTANCE.saveHudLayout();
        super.close();
    }
}
