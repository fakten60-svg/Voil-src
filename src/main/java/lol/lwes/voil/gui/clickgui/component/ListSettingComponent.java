package lol.lwes.voil.gui.clickgui.component;

import dev.prestigeclient.renderlib.api.font.MSDFFontAtlas;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.config.GuiTheme;
import lol.lwes.voil.event.InputEventType;
import lol.lwes.voil.gui.clickgui.ClickGuiComponent;
import lol.lwes.voil.gui.clickgui.ClickGuiScreen;
import lol.lwes.voil.setting.ListSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.render.ColorUtil;
import lol.lwes.voil.util.render.EasingType;
import lol.lwes.voil.util.render.Render2DUtil;
import lol.lwes.voil.util.render.UiAnimation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class ListSettingComponent extends SettingComponent {
    private final ListSetting setting;
    private final Map<String, UiAnimation> yAnimations = new HashMap<>();
    private final Map<String, UiAnimation> liftAnimations = new HashMap<>();
    private final Map<String, UiAnimation> hoverAnimations = new HashMap<>();
    private String draggedItem = null;
    private String hoveredItem = null;
    private ThresholdKind dragState = null;
    private float dragY;
    private float dragX;

    public ListSettingComponent(Setting setting, ClickGuiScreen clickguiscreen) {
        super(setting, clickguiscreen);
        this.setting = (ListSetting)setting;
    }

    @Override
    protected float getExtraHeight() {
        int i = this.setting.getItems().size();
        return i <= 0 ? 25.0F : 3.0F + i * 19.0F + (i - 1) * 3.0F + 3.0F;
    }

    private List<ThresholdTrack> getRankedItems(String s) {
        boolean flag = this.setting.isEnabled(s);
        boolean flag1 = this.setting.isMaxEnabled(s);
        ArrayList<ThresholdTrack> arraylist = new ArrayList<>(2);
        if (!flag && !flag1) {
            return arraylist;
        }

        boolean flag2 = flag && flag1;
        float f = flag2 ? 26.0F : 40.0F;
        float f1 = flag2 ? 20.0F : 24.0F;
        float f2 = this.getInnerLeft() + this.getInnerWidth() - 8.0F;
        if (flag) {
            f2 -= f1 + 6.0F + f;
            arraylist.add(new ThresholdTrack(ThresholdKind.FALL, f2, f, f1));
        }

        if (flag1) {
            f2 -= 6.0F + f1 + 6.0F + f;
            arraylist.add(new ThresholdTrack(ThresholdKind.HEALTH, f2, f, f1));
        }

        return arraylist;
    }

    private void drawGrip(float f, float f1, Color color) {
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 3; j++) {
                float f3 = f + i * 3.2F;
                float f2 = f1 - 3.2F + j * 3.2F;
                Render2DUtil.drawRoundedRect(f3, f2, 1.6F, 1.6F, 1.6F / 2.0F, 1.0F, color);
            }
        }
    }

    private void drawItem(String s, int i, int j, float f, float f1) {
        float f2 = this.getLiftAnimation(s).getFloat();
        float f3 = this.getHoverAnimation(s).getFloat();
        float f4 = Math.max(f3, f2);
        float f5 = this.getContentX() + this.getYAnimation(s).getFloat() + 9.5F;
        float f6 = 19.0F + 1.5F * f2;
        float f7 = 2.5F * f2;
        float f8 = f + f7;
        float f9 = f1 - f7 * 2.0F;
        float f10 = f5 - f6 / 2.0F;
        Color color = GuiTheme.accentColor1;
        if (f2 > 0.02F) {
            float f16 = 0.85F * f2;
            int l = (int)(130.0F * f2);
            Color color4 = color;
            Render2DUtil.drawVariableGlowRect(f8, f10, f9, f6, 5.0F, 8.0F, f16, ColorUtil.withAlpha(color4, l));
        }

        Color color1 = Render2DUtil.lerpColor(new Color(1776930), new Color(2566964), f3);
        color1 = Render2DUtil.lerpColor(color1, new Color(3093824), f2);
        Render2DUtil.drawRoundedRect(f8, f10, f9, f6, 5.0F, 1.0F, color1);
        if (f4 > 0.02F) {
            float f17 = f10 + 3.0F;
            float f20 = f6 - 6.0F;
            int i1 = (int)(220.0F * f4);
            Color color5 = color;
            Render2DUtil.drawRoundedRect(f8, f17, 2.0F, f20, 1.0F, 1.0F, ColorUtil.withAlpha(color5, i1));
        }

        if (f2 > 0.02F) {
            int j1 = (int)(200.0F * f2);
            Color color6 = color;
            Render2DUtil.drawOutline(f8, f10, f9, f6, 5.0F, 0.4F, 0.6F, ColorUtil.withAlpha(color6, j1));
        }

        float f11 = f8 + 6.0F;
        float f12 = f5 - 11.0F / 2.0F;
        float f13 = j <= 1 ? 0.0F : (float)i / (j - 1);
        Color color2 = Render2DUtil.lerpColor(color, new Color(90, 96, 104), f13);
        if (i == 0) {
            int k1 = (int)(90.0F + 70.0F * f4);
            Color color7 = color;
            Color color8 = ColorUtil.withAlpha(color7, k1);
            Render2DUtil.drawGlow(f11, f12, 11.0F, 11.0F, 3.0F, 1.0F, 3.0F, color8);
        }

        Render2DUtil.drawRoundedRect(f11, f12, 11.0F, 11.0F, 3.0F, 1.0F, ColorUtil.withAlpha(color2, 215));
        String s1 = String.valueOf(i + 1);
        VoilClient.fonts.getSelectedFont().render(s1, f11 + (11.0F - VoilClient.fonts.getSelectedFont().getWidth(s1, 7.0F)) / 2.0F, f5 - 3.5F, 7.0F, -1);
        List<ThresholdTrack> list = this.getRankedItems(s);
        float f14 = f11 + 11.0F + 6.0F;
        float f18;
        if (list.isEmpty()) {
            f18 = f8 + f9 - 20.0F;
        } else {
            int k = list.size();
            f18 = ((ThresholdTrack)list.get(k - 1)).startX() - 8.0F;
        }

        float f15 = f18;
        String s3 = formatName(s);
        String s2 = this.truncate(s3, 8.0F, f15 - f14);
        Color color3 = Render2DUtil.lerpColor(GuiTheme.normalTextColor, Color.WHITE, f4);
        VoilClient.fonts.getSelectedFont().render(s2, f14, f5 - 4.0F, 8.0F, color3.getRGB());
        if (list.isEmpty()) {
            float f19 = f8 + f9 - 12.0F;
            int l1 = (int)(55.0F + 130.0F * f4);
            Color color9 = Color.WHITE;
            this.drawGrip(f19, f5, ColorUtil.withAlpha(color9, l1));
        } else {
            for (ThresholdTrack thresholdtrack : list) {
                this.drawRow(s, thresholdtrack, f5, f4, color);
            }
        }
    }

    public void mouseClicked(double d0, double d1, int i) {
        if (i == 0) {
            List<String> list = this.setting.getItems();

            for (int j = 0; j < list.size(); j++) {
                String s = (String)list.get(j);
                ThresholdTrack thresholdtrack = this.findTarget(s, j, d0, d1);
                if (thresholdtrack != null) {
                    this.hoveredItem = s;
                    this.dragState = thresholdtrack.kind();
                    ThresholdKind thresholdkind = thresholdtrack.kind();
                    this.updateDrag(s, thresholdkind, thresholdtrack, d0);
                    this.addActiveInput(InputEventType.Click);
                    return;
                }
            }

            for (int k = 0; k < list.size(); k++) {
                if (this.isOverRow(k, d0, d1)) {
                    this.draggedItem = (String)list.get(k);
                    this.dragY = (float)d1 - (this.getContentX() + this.getRowY(k));
                    this.dragX = this.getRowY(k);
                    this.getYAnimation(this.draggedItem).snapTo(this.dragX);
                    this.addActiveInput(InputEventType.Click);
                    return;
                }
            }
        }
    }

    private ThresholdTrack createDragEntry(String s, ThresholdKind thresholdkind) {
        for (ThresholdTrack thresholdtrack : this.getRankedItems(s)) {
            if (thresholdtrack.kind() == thresholdkind) {
                return thresholdtrack;
            }
        }

        return null;
    }

    public void render(int i, int j) {
        List<String> list = this.setting.getItems();
        int k = list.size();
        float f = this.getInnerLeft();
        float f1 = this.getInnerWidth();
        VoilClient.fonts.getSelectedFont().render(this.setting.getName(), f, this.getTextY(), GuiTheme.normalTextColor.getRGB());
        MSDFFontAtlas msdffontatlas = VoilClient.fonts.getSelectedFont();
        float f4 = this.getInnerRight() - VoilClient.fonts.getSelectedFont().getWidth("drag to rank", 7.0F);
        float f5 = this.getTextY() + 0.5F;
        Color color = Color.WHITE;
        msdffontatlas.render("drag to rank", f4, f5, 7.0F, ColorUtil.withAlpha(color, 70).getRGB());
        float f3 = f - 2.0F;
        float f2 = this.getBottom();
        Render2DUtil.drawRoundedRect(f3, f2, f1 + 4.0F, this.getExtraHeight(), 6.0F, 1.0F, new Color(20, 21, 25, 130));
        if (k == 0) {
            String s1 = "no weapons selected";
            msdffontatlas = VoilClient.fonts.getSelectedFont();
            f4 = f + (f1 - VoilClient.fonts.getSelectedFont().getWidth(s1, 7.0F)) / 2.0F;
            f5 = this.getContentX() + 9.5F - 3.5F;
            Color color1 = Color.WHITE;
            msdffontatlas.render(s1, f4, f5, 7.0F, ColorUtil.withAlpha(color1, 80).getRGB());
        } else {
            for (int l = 0; l < k; l++) {
                String s = list.get(l);
                boolean flag = s.equals(this.draggedItem);
                boolean flag1 = flag || s.equals(this.hoveredItem) || this.isOverRow(l, i, j);
                UiAnimation uianimation = this.getHoverAnimation(s);
                double d1 = flag1 ? 1.0 : 0.0;
                uianimation.setTarget(d1);
                UiAnimation uianimation1 = this.getLiftAnimation(s);
                double d2 = flag ? 1.0 : 0.0;
                uianimation1.setTarget(d2);
                if (flag) {
                    this.getYAnimation(s).snapTo(this.getClampedY(this.dragX));
                } else {
                    UiAnimation uianimation2 = this.getYAnimation(s);
                    double d3 = this.getRowY(l);
                    uianimation2.setTarget(d3);
                }

                this.getHoverAnimation(s).update();
                this.getLiftAnimation(s).update();
                this.getYAnimation(s).update();
            }

            for (int i1 = 0; i1 < 2; i1++) {
                for (int j1 = 0; j1 < k; j1++) {
                    String s2 = (String)list.get(j1);
                    boolean flag2 = s2.equals(this.draggedItem);
                    if (i1 == 0 != flag2) {
                        this.drawItem(s2, j1, k, f, f1);
                    }
                }
            }
        }
    }

    public void mouseDragged(double d0, double d1) {
        if (this.hoveredItem != null && this.dragState != null) {
            String s = this.hoveredItem;
            ThresholdTrack thresholdtrack = this.createDragEntry(s, this.dragState);
            if (thresholdtrack != null) {
                ThresholdKind thresholdkind = this.dragState;
                this.updateDrag(this.hoveredItem, thresholdkind, thresholdtrack, d0);
            }

            this.addActiveInput(InputEventType.Drag);
        } else if (this.draggedItem != null) {
            this.dragX = this.getClampedY((float)d1 - this.dragY - this.getContentX());
            List<String> list = this.setting.getItems();
            int i = list.indexOf(this.draggedItem);
            if (i < 0) {
                this.draggedItem = null;
            } else {
                int j = Math.max(0, Math.min(list.size() - 1, Math.round(this.dragX / 22.0F)));
                if (j != i) {
                    this.setting.moveItem(i, j);
                }

                this.addActiveInput(InputEventType.Drag);
            }
        }
    }

    public void mouseScrolled(double d0, double d1, double d2) {
        List<String> list = this.setting.getItems();

        for (int i = 0; i < list.size(); i++) {
            String s1 = (String)list.get(i);
            if (this.isOverRow(i, d0, d1)) {
                ThresholdTrack thresholdtrack = this.findTarget(s1, i, d0, d1);
                if (thresholdtrack == null) {
                    List<ThresholdTrack> list1 = this.getRankedItems(s1);
                    if (list1.size() == 1) {
                        thresholdtrack = (ThresholdTrack)list1.get(0);
                    }
                }

                if (thresholdtrack != null) {
                    ThresholdKind thresholdkind1 = thresholdtrack.kind();
                    String s2 = s1;
                    double d4 = this.getItemValue(s2, thresholdkind1);
                    ThresholdKind thresholdkind2 = thresholdtrack.kind();
                    double d3 = d4 + (d2 > 0.0 ? 0.5 : -0.5);
                    ThresholdKind thresholdkind = thresholdkind2;
                    String s = s1;
                    this.startDrag(s, thresholdkind, d3);
                    this.addActiveInput(InputEventType.Scroll);
                }

                return;
            }
        }
    }

    public void mouseReleased2() {
        if (this.hoveredItem != null) {
            this.hoveredItem = null;
            this.dragState = null;
        } else if (this.draggedItem != null) {
            String s = this.draggedItem;
            this.draggedItem = null;
            int i = this.setting.getItems().indexOf(s);
            if (i >= 0) {
                UiAnimation uianimation = this.getYAnimation(s);
                double d0 = this.getRowY(i);
                uianimation.setTarget(d0);
            }
        }
    }

    private void startDrag(String s, ThresholdKind thresholdkind, double d0) {
        if (thresholdkind == ThresholdKind.FALL) {
            this.setting.setMinThreshold(s, d0);
        } else {
            this.setting.setMaxThreshold(s, d0);
        }
    }

    private String truncate(String s, float f, float f1) {
        if (f1 <= 0.0F) {
            return "";
        }

        if (VoilClient.fonts.getSelectedFont().getWidth(s, f) <= f1) {
            return s;
        }

        while (s.length() > 1) {
            MSDFFontAtlas msdffontatlas = VoilClient.fonts.getSelectedFont();
            if (!(msdffontatlas.getWidth(s + "..", f) > f1)) {
                break;
            }

            int i = s.length();
            s = s.substring(0, i - 1);
        }

        return s + "..";
    }

    private static String formatValue(float f) {
        return f == Math.floor(f) ? String.valueOf((int)f) : String.valueOf(Math.round(f * 10.0F) / 10.0F);
    }

    private UiAnimation getYAnimation(String s) {
        return this.yAnimations.computeIfAbsent(s, s1 -> new UiAnimation(EasingType.FLUID_SMOOTH).setFluidParams(0.55, 0.15));
    }

    private void updateDrag(String s, ThresholdKind thresholdkind, ThresholdTrack thresholdtrack, double d0) {
        float f = (float)((d0 - thresholdtrack.startX()) / thresholdtrack.width());
        f = Math.max(0.0F, Math.min(1.0F, f));
        double d1 = Math.round(f * 20.0F / 0.5) * 0.5;
        this.startDrag(s, thresholdkind, d1);
    }

    private UiAnimation getLiftAnimation(String s) {
        return this.liftAnimations.computeIfAbsent(s, s1 -> new UiAnimation(EasingType.FLUID_RESPONSIVE));
    }

    private UiAnimation getHoverAnimation(String s) {
        return this.hoverAnimations.computeIfAbsent(s, s1 -> new UiAnimation(EasingType.FLUID_SMOOTH).setFluidParams(0.62, 0.15));
    }

    private float getRowY(int i) {
        return i * 22.0F;
    }

    private void drawRow(String s, ThresholdTrack thresholdtrack, float f, float f1, Color color) {
        boolean flag = s.equals(this.hoveredItem) && thresholdtrack.kind() == this.dragState;
        float f2 = f - 1.0F;
        ThresholdKind thresholdkind = thresholdtrack.kind();
        String s4 = s;
        float f3 = (float)this.getItemValue(s4, thresholdkind);
        float f4 = Math.min(1.0F, f3 / 20.0F);
        Color color1 = thresholdtrack.kind() == ThresholdKind.FALL ? color : new Color(227, 93, 106);
        float f14 = thresholdtrack.startX();
        float f15 = thresholdtrack.width();
        Color color2 = Color.WHITE;
        Render2DUtil.drawRoundedRect(f14, f2, f15, 2.0F, 1.0F, 1.0F, ColorUtil.withAlpha(color2, 45));
        f14 = thresholdtrack.startX();
        f15 = thresholdtrack.width() * f4;
        Color color3 = ColorUtil.withAlpha(color1, 210);
        float f7 = 1.0F;
        float f8 = 1.0F;
        float f9 = 2.0F;
        float f10 = f15;
        float f11 = f2;
        float f12 = f14;
        Render2DUtil.drawRoundedRect(f12, f11, f10, f9, f8, f7, color3);
        float f5 = flag ? 7.0F : 5.0F;
        float f6 = flag ? 3.5F : 3.0F;
        f14 = thresholdtrack.startX() + thresholdtrack.width() * f4 - f6 / 2.0F;
        float f13 = f - f5 / 2.0F;
        Render2DUtil.drawRoundedRect(f14, f13, f6, f5, f6 / 2.0F, 1.0F, color1);
        String s7;
        if (thresholdtrack.kind() == ThresholdKind.FALL) {
            String s5 = formatValue(f3);
            s7 = ">=" + s5;
        } else {
            String s6 = formatValue(f3);
            s7 = "hp<=" + s6;
        }

        String s1 = s7;
        MSDFFontAtlas msdffontatlas = VoilClient.fonts.getSelectedFont();
        f15 = thresholdtrack.startX() + thresholdtrack.width() + 6.0F;
        float f16 = f - 3.5F;
        int i = (int)(150.0F + 90.0F * Math.max(f1, flag ? 1.0F : 0.0F));
        Color color4 = color1;
        msdffontatlas.render(s1, f15, f16, 7.0F, ColorUtil.withAlpha(color4, i).getRGB());
    }

    private double getItemValue(String s, ThresholdKind thresholdkind) {
        return thresholdkind == ThresholdKind.FALL ? this.setting.getMinThreshold(s) : this.setting.getMaxThreshold(s);
    }

    private static String formatName(String s) {
        Identifier identifier = Identifier.tryParse(s);
        if (identifier == null) {
            return s;
        }

        Item item = Registries.ITEM.get(identifier);
        return item == Items.AIR ? s : item.getName().getString();
    }

    private float getContentX() {
        return this.getBottom() + 3.0F;
    }

    private float getClampedY(float f) {
        int i = this.setting.getItems().size();
        return Math.max(0.0F, Math.min(this.getRowY(i - 1), f));
    }

    private boolean isOverRow(int i, double d0, double d1) {
        float f = this.getContentX() + this.getRowY(i);
        return ClickGuiComponent.isPointInRect(this.getInnerLeft(), f, this.getInnerLeft() + this.getInnerWidth(), f + 19.0F, d0, d1);
    }

    private ThresholdTrack findTarget(String s, int i, double d0, double d1) {
        float f = this.getContentX() + this.getRowY(i);
        if (!(d1 < f) && !(d1 > f + 19.0F)) {
            for (ThresholdTrack thresholdtrack : this.getRankedItems(s)) {
                float f1 = thresholdtrack.startX() - 4.0F;
                float f2 = thresholdtrack.startX() + thresholdtrack.width() + 6.0F + thresholdtrack.labelWidth();
                if (d0 >= f1 && d0 <= f2) {
                    return thresholdtrack;
                }
            }

            return null;
        } else {
            return null;
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
    public void mouseDragged2(double d0, double d1, int i, double d2, double d3) {
        this.mouseDragged(d0, d1);
    }

    @Override
    public void mouseScrolled2(double d0, double d1, double d3, double d2) {
        this.mouseScrolled(d0, d1, d2);
    }

    @Override
    public void mouseReleased2(double d0, double d1, int i) {
        this.mouseReleased2();
    }
}
