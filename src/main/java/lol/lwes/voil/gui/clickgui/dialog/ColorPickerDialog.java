package lol.lwes.voil.gui.clickgui.dialog;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.Locale;
import lol.lwes.voil.gui.clickgui.page.GuiSettingsPage;
import lol.lwes.voil.util.render.ColorUtil;
import lol.lwes.voil.util.render.Render2DUtil;
import org.lwjgl.glfw.GLFW;

public class ColorPickerDialog extends GuiDialog {
    private final GuiSettingsPage page;
    private final lol.lwes.voil.gui.clickgui.theme.ColorBinding binding;
    private final Color original;
    private final DialogTextField hexField = new DialogTextField("#RRGGBB", 7, false)
        .withFilter(c -> c == '#' || Character.digit(c, 16) >= 0);
    private float hue;
    private float saturation;
    private float brightness;
    private DragTarget dragTarget = DragTarget.NONE;

    public ColorPickerDialog(GuiSettingsPage page, lol.lwes.voil.gui.clickgui.theme.ColorBinding binding) {
        super(page.gui);
        this.page = page;
        this.binding = binding;
        Color current = binding.getter.get();
        this.original = current == null ? Color.WHITE : current;
        float[] hsb = Color.RGBtoHSB(this.original.getRed(), this.original.getGreen(), this.original.getBlue(), null);
        this.hue = hsb[0];
        this.saturation = hsb[1];
        this.brightness = hsb[2];
        this.hexField.setText(toHex(this.original));
    }

    @Override
    protected float getDialogWidth() {
        return 160.0F;
    }

    @Override
    protected float getDialogHeight() {
        return 196.0F;
    }

    @Override
    protected void drawContent(int mouseX, int mouseY, float delta) {
        this.drawTitle(this.binding.label == null ? "Color" : this.binding.label, null);

        Rectangle area = this.colorArea();
        Render2DUtil.drawRectGradient(area.x, area.y, area.width, area.height, 3.0F, 1.0F, Color.BLACK, Color.WHITE, Color.BLACK, Color.getHSBColor(this.hue, 1.0F, 1.0F));
        float markerX = area.x + this.saturation * area.width;
        float markerY = area.y + (1.0F - this.brightness) * area.height;
        Render2DUtil.drawCircle(markerX - 3.5F, markerY - 3.5F, 3.5F, 1.0F, Color.WHITE);
        Render2DUtil.drawCircle(markerX - 2.5F, markerY - 2.5F, 2.5F, 1.0F, this.currentColor());

        Rectangle hueBar = this.hueBar();
        for (int i = 0; i < hueBar.width; i++) {
            Render2DUtil.drawRoundedRect(hueBar.x + i, hueBar.y, 1.5F, hueBar.height, 0.0F, 1.0F, Color.getHSBColor((float)i / hueBar.width, 1.0F, 1.0F));
        }

        float hueX = hueBar.x + this.hue * hueBar.width;
        Render2DUtil.drawCircle(hueX - 3.5F, hueBar.y + hueBar.height / 2.0F - 3.5F, 3.5F, 1.0F, Color.WHITE);
        Render2DUtil.drawCircle(hueX - 2.5F, hueBar.y + hueBar.height / 2.0F - 2.5F, 2.5F, 1.0F, Color.getHSBColor(this.hue, 1.0F, 1.0F));

        float rowY = hueBar.y + hueBar.height + 8.0F;
        Render2DUtil.drawRoundedRect(area.x, rowY, 16.0F, 16.0F, 4.0F, 1.0F, this.original);
        Render2DUtil.drawRoundedRect(area.x + 18.0F, rowY, 16.0F, 16.0F, 4.0F, 1.0F, this.currentColor());
        Render2DUtil.drawOutline(area.x, rowY, 34.0F, 16.0F, 4.0F, 0.4F, 0.6F, ColorUtil.withAlpha(Color.WHITE, 30));
        this.hexField.render(area.x + 40.0F, rowY, area.width - 40.0F, 16.0F);

        float buttonY = this.y + this.height - 24.0F;
        this.drawButton("Cancel", this.x + 10.0F, buttonY, 66.0F, 16.0F, mouseX, mouseY, false, this::cancel);
        this.drawButton("Apply", this.x + this.width - 76.0F, buttonY, 66.0F, 16.0F, mouseX, mouseY, true, this::close);
    }

    @Override
    protected boolean onMouseClicked(double mouseX, double mouseY, int button) {
        if (this.hexField.mouseClicked(mouseX, mouseY)) {
            return true;
        }

        if (button != 0) {
            return false;
        }

        if (this.colorArea().contains(mouseX, mouseY)) {
            this.dragTarget = DragTarget.SATURATION_BRIGHTNESS;
        } else if (this.hueBar().contains(mouseX, mouseY)) {
            this.dragTarget = DragTarget.HUE;
        } else {
            return false;
        }

        this.updateDrag(mouseX, mouseY);
        return true;
    }

    @Override
    protected void onMouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.dragTarget != DragTarget.NONE) {
            this.updateDrag(mouseX, mouseY);
        }
    }

    @Override
    protected void onMouseReleased(double mouseX, double mouseY, int button) {
        this.dragTarget = DragTarget.NONE;
    }

    @Override
    protected void onMouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        if (vertical == 0.0) {
            return;
        }

        float next = this.hue + (float)vertical * 0.01F;
        this.hue = next - (float)Math.floor(next);
        this.apply();
    }

    @Override
    protected void onKeyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            if (this.hexField.isFocused()) {
                this.applyHex();
                this.hexField.setFocused(false);
            } else {
                this.close();
            }

            return;
        }

        if (this.hexField.keyPressed(keyCode, modifiers)) {
            this.applyHexIfComplete();
        }
    }

    @Override
    protected void onCharTyped(char character, int modifiers) {
        if (this.hexField.charTyped(character)) {
            this.applyHexIfComplete();
        }
    }

    @Override
    protected void onEscape() {
        this.cancel();
    }

    @Override
    protected void close() {
        this.dragTarget = DragTarget.NONE;
        this.page.closeColorPicker();
    }

    private void cancel() {
        this.binding.setter.accept(this.original);
        this.close();
    }

    private void updateDrag(double mouseX, double mouseY) {
        if (this.dragTarget == DragTarget.SATURATION_BRIGHTNESS) {
            Rectangle area = this.colorArea();
            this.saturation = clamp01((float)((mouseX - area.x) / area.width));
            this.brightness = 1.0F - clamp01((float)((mouseY - area.y) / area.height));
        } else if (this.dragTarget == DragTarget.HUE) {
            Rectangle hueBar = this.hueBar();
            this.hue = clamp01((float)((mouseX - hueBar.x) / hueBar.width));
        }

        this.apply();
    }

    private void apply() {
        Color color = this.currentColor();
        this.binding.setter.accept(color);
        this.hexField.setText(toHex(color));
    }

    private void applyHexIfComplete() {
        String value = this.hexField.getText().replace("#", "");
        if (value.length() == 6) {
            this.applyHex();
        }
    }

    private void applyHex() {
        String value = this.hexField.getText().replace("#", "").trim();
        if (value.length() != 6) {
            this.hexField.setText(toHex(this.currentColor()));
            return;
        }

        try {
            Color color = new Color(Integer.parseInt(value, 16));
            float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
            this.hue = hsb[0];
            this.saturation = hsb[1];
            this.brightness = hsb[2];
            this.binding.setter.accept(color);
        } catch (NumberFormatException exception) {
            this.hexField.setText(toHex(this.currentColor()));
        }
    }

    private Color currentColor() {
        Color color = Color.getHSBColor(this.hue, this.saturation, this.brightness);
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), this.original.getAlpha());
    }

    private Rectangle colorArea() {
        return new Rectangle((int)this.x + 10, (int)this.y + 28, (int)this.width - 20, 90);
    }

    private Rectangle hueBar() {
        Rectangle area = this.colorArea();
        return new Rectangle(area.x, area.y + area.height + 8, area.width, 8);
    }

    private static float clamp01(float value) {
        return value < 0.0F ? 0.0F : Math.min(value, 1.0F);
    }

    private static String toHex(Color color) {
        return String.format(Locale.ROOT, "#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
    }

    private enum DragTarget {
        NONE,
        SATURATION_BRIGHTNESS,
        HUE
    }
}
