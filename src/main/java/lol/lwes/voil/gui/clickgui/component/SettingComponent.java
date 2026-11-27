package lol.lwes.voil.gui.clickgui.component;

import lol.lwes.voil.gui.clickgui.ClickGuiComponent;
import lol.lwes.voil.gui.clickgui.ClickGuiScreen;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.render.RectBounds;

public abstract class SettingComponent extends ClickGuiComponent {
    protected final Setting setting;
    protected final ClickGuiScreen gui;

    protected SettingComponent(Setting setting, ClickGuiScreen clickguiscreen) {
        this.setting = setting;
        this.gui = clickguiscreen;
    }

    protected float getRowHeight() {
        return 22.0F;
    }

    protected float getControlHeight() {
        return 14.0F;
    }

    protected final float getTextY() {
        return this.width + (this.getRowHeight() - 9.0F) / 2.0F;
    }

    protected final float getCenterY() {
        return this.width + this.getRowHeight() / 2.0F;
    }

    protected float getExtraHeight() {
        return 0.0F;
    }

    public float getTotalHeight() {
        float f = this.getExtraHeight();
        return f > 0.0F ? this.getRowHeight() + 4.0F + f : this.getRowHeight();
    }

    protected final float getRight() {
        return this.getLeft() + 325.0F;
    }

    @Override
    public Setting getSetting() {
        return this.setting;
    }

    @Override
    public RectBounds getBounds() {
        return new RectBounds(this.getLeft(), this.width, 325.0F, this.getTotalHeight());
    }

    protected final float getInnerRight() {
        return this.getRight() - 5.0F;
    }

    protected final float getLeft() {
        return this.height + this.gui.windowX + 445.0F;
    }

    protected final float getInnerLeft() {
        return this.getLeft() + 5.0F;
    }

    protected final float getControlY() {
        return this.width + (this.getRowHeight() - this.getControlHeight()) / 2.0F;
    }

    protected final float getInnerWidth() {
        return 315.0F;
    }

    public static int opaqueZero() {
        return 0;
    }

    protected final float getBottom() {
        return this.width + this.getRowHeight() + 4.0F;
    }

    @Override
    public void setY(float f) {
        this.width = f;
    }

    protected final float fromInnerRight(float f) {
        return this.getInnerRight() - f;
    }
}
