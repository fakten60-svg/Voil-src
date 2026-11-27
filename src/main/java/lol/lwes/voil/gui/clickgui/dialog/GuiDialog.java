package lol.lwes.voil.gui.clickgui.dialog;

import dev.prestigeclient.renderlib.api.font.MSDFFontAtlas;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.config.GuiTheme;
import lol.lwes.voil.gui.clickgui.ClickGuiScreen;
import lol.lwes.voil.util.render.ColorUtil;
import lol.lwes.voil.util.render.Render2DUtil;
import org.lwjgl.glfw.GLFW;

public abstract class GuiDialog {
    protected static final Color OVERLAY = new Color(0, 0, 0, 110);
    protected static final Color BACKGROUND = new Color(18, 18, 24, 248);
    protected static final Color SURFACE = new Color(30, 30, 38);
    protected static final Color MUTED_TEXT = new Color(122, 124, 134);
    protected static final Color ERROR_TEXT = new Color(225, 90, 90);

    protected final ClickGuiScreen screen;
    protected float x;
    protected float y;
    protected float width;
    protected float height;
    private final List<DialogButton> buttons = new ArrayList<>();

    protected GuiDialog(ClickGuiScreen screen) {
        this.screen = screen;
    }

    protected abstract float getDialogWidth();

    protected abstract float getDialogHeight();

    protected abstract void drawContent(int mouseX, int mouseY, float delta);

    protected abstract void close();

    protected boolean onMouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    protected void onEscape() {
        this.close();
    }

    protected void onKeyPressed(int keyCode, int scanCode, int modifiers) {
    }

    protected void onCharTyped(char character, int modifiers) {
    }

    protected void onMouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
    }

    protected void onMouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
    }

    protected void onMouseReleased(double mouseX, double mouseY, int button) {
    }

    public void render(int mouseX, int mouseY, float delta) {
        this.layout();
        this.buttons.clear();
        Render2DUtil.drawRoundedRect(this.screen.windowX, this.screen.windowY, this.screen.windowWidth, this.screen.windowHeight, 8.0F, 1.0F, OVERLAY);
        Render2DUtil.drawRoundedRect(this.x, this.y, this.width, this.height, 8.0F, 1.0F, BACKGROUND);
        Render2DUtil.drawOutline(this.x, this.y, this.width, this.height, 8.0F, 0.5F, 0.4F, ColorUtil.withAlpha(accent(), 150));
        this.drawContent(mouseX, mouseY, delta);
    }

    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            for (DialogButton dialogButton : List.copyOf(this.buttons)) {
                if (dialogButton.contains(mouseX, mouseY)) {
                    dialogButton.action.run();
                    return;
                }
            }
        }

        if (!this.isInside(mouseX, mouseY)) {
            this.close();
            return;
        }

        this.onMouseClicked(mouseX, mouseY, button);
    }

    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.onEscape();
            return;
        }

        this.onKeyPressed(keyCode, scanCode, modifiers);
    }

    public void charTyped(char character, int modifiers) {
        this.onCharTyped(character, modifiers);
    }

    public void mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        this.onMouseScrolled(mouseX, mouseY, horizontal, vertical);
    }

    public void mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        this.onMouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        this.onMouseReleased(mouseX, mouseY, button);
    }

    protected void layout() {
        this.width = this.getDialogWidth();
        this.height = this.getDialogHeight();
        this.x = this.screen.windowX + (this.screen.windowWidth - this.width) / 2.0F;
        this.y = this.screen.windowY + (this.screen.windowHeight - this.height) / 2.0F;
    }

    protected boolean isInside(double mouseX, double mouseY) {
        return mouseX >= this.x && mouseX <= this.x + this.width && mouseY >= this.y && mouseY <= this.y + this.height;
    }

    protected void drawTitle(String title, String subtitle) {
        font().render(title, this.x + 10.0F, this.y + 8.0F, 11.0F, -1);
        if (subtitle != null && !subtitle.isEmpty()) {
            font().render(subtitle, this.x + 10.0F, this.y + 22.0F, 8.0F, MUTED_TEXT.getRGB());
        }
    }

    protected void drawLabel(String label, float labelX, float labelY) {
        font().render(label, labelX, labelY, 8.0F, MUTED_TEXT.getRGB());
    }

    protected void drawButton(String label, float buttonX, float buttonY, float buttonWidth, float buttonHeight, int mouseX, int mouseY, boolean primary, Runnable action) {
        boolean hovered = mouseX >= buttonX && mouseX <= buttonX + buttonWidth && mouseY >= buttonY && mouseY <= buttonY + buttonHeight;
        Color base = primary ? accent() : SURFACE;
        Color fill = hovered ? ColorUtil.scaleBrightness(base, 1.2F) : base;
        Render2DUtil.drawRoundedRect(buttonX, buttonY, buttonWidth, buttonHeight, 4.0F, 1.0F, fill);
        if (!primary) {
            Render2DUtil.drawOutline(buttonX, buttonY, buttonWidth, buttonHeight, 4.0F, 0.4F, 0.6F, ColorUtil.withAlpha(Color.WHITE, hovered ? 60 : 25));
        }

        MSDFFontAtlas font = font();
        float textWidth = font.getWidth(label, 8.5F);
        float textY = buttonY + (buttonHeight - font.getLineHeight(8.5F)) / 2.0F;
        font.render(label, buttonX + (buttonWidth - textWidth) / 2.0F, textY, 8.5F, -1);
        this.buttons.add(new DialogButton(buttonX, buttonY, buttonWidth, buttonHeight, action));
    }

    protected void drawToggle(String label, boolean value, float toggleX, float toggleY, int mouseX, int mouseY, Runnable action) {
        float trackWidth = 20.0F;
        float trackHeight = 10.0F;
        Color track = value ? accent() : SURFACE;
        Render2DUtil.drawRoundedRect(toggleX, toggleY, trackWidth, trackHeight, trackHeight / 2.0F, 1.0F, track);
        float knobX = value ? toggleX + trackWidth - trackHeight + 1.5F : toggleX + 1.5F;
        Render2DUtil.drawCircle(knobX, toggleY + 1.5F, (trackHeight - 3.0F) / 2.0F, 1.0F, Color.WHITE);
        font().render(label, toggleX + trackWidth + 6.0F, toggleY + (trackHeight - font().getLineHeight(8.5F)) / 2.0F, 8.5F, -1);
        float hitWidth = trackWidth + 6.0F + font().getWidth(label, 8.5F);
        this.buttons.add(new DialogButton(toggleX, toggleY - 2.0F, hitWidth, trackHeight + 4.0F, action));
    }

    protected static List<String> wrap(String text, float maxWidth, float size) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return lines;
        }

        MSDFFontAtlas font = font();
        for (String paragraph : text.split("\n", -1)) {
            StringBuilder line = new StringBuilder();
            for (String word : paragraph.split(" ")) {
                String candidate = line.length() == 0 ? word : line + " " + word;
                if (line.length() > 0 && font.getWidth(candidate, size) > maxWidth) {
                    lines.add(line.toString());
                    line = new StringBuilder(word);
                } else {
                    line = new StringBuilder(candidate);
                }
            }

            lines.add(line.toString());
        }

        return lines;
    }

    protected static String ellipsize(String text, float maxWidth, float size) {
        MSDFFontAtlas font = font();
        if (font.getWidth(text, size) <= maxWidth) {
            return text;
        }

        int end = text.length();
        while (end > 0 && font.getWidth(text.substring(0, end) + "...", size) > maxWidth) {
            end--;
        }

        return text.substring(0, end) + "...";
    }

    protected static MSDFFontAtlas font() {
        return VoilClient.fonts.getSelectedFont();
    }

    protected static Color accent() {
        return GuiTheme.accentColor1 != null ? GuiTheme.accentColor1 : new Color(167, 139, 250);
    }

    private record DialogButton(float x, float y, float width, float height, Runnable action) {
        boolean contains(double mouseX, double mouseY) {
            return mouseX >= this.x && mouseX <= this.x + this.width && mouseY >= this.y && mouseY <= this.y + this.height;
        }
    }
}
