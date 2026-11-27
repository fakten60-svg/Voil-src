package lol.lwes.voil.gui.clickgui.dialog;

import dev.prestigeclient.renderlib.api.font.MSDFFontAtlas;
import java.awt.Color;
import java.util.List;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.gui.clickgui.ConfigEntryInfo;
import lol.lwes.voil.gui.clickgui.ConfigScriptsPanel;
import lol.lwes.voil.script.ScriptManager;
import lol.lwes.voil.util.render.ColorUtil;
import lol.lwes.voil.util.render.Render2DUtil;
import org.lwjgl.glfw.GLFW;

public class ScriptDetailsDialog extends GuiDialog {
    private static final float CODE_SIZE = 8.0F;
    private static final Color CODE_BACKGROUND = new Color(12, 12, 16);
    private static final Color LINE_NUMBER = new Color(80, 82, 92);
    private static final Color ENABLED = new Color(12, 155, 68);

    private final ConfigScriptsPanel panel;
    private final ConfigEntryInfo entry;
    private final String[] sourceLines;
    private int scrollLine;
    private float codeX;
    private float codeY;
    private float codeWidth;
    private float codeHeight;
    private String status = "";

    public ScriptDetailsDialog(ConfigScriptsPanel panel, ConfigEntryInfo entry) {
        super(panel.screen);
        this.panel = panel;
        this.entry = entry;
        String source = entry.getId() == null ? "" : entry.getId().replace("\r", "");
        this.sourceLines = source.split("\n", -1);
    }

    @Override
    protected float getDialogWidth() {
        return 330.0F;
    }

    @Override
    protected float getDialogHeight() {
        return 250.0F;
    }

    @Override
    protected void drawContent(int mouseX, int mouseY, float delta) {
        ScriptManager scripts = VoilClient.getScriptManager();
        String name = this.entry.getName();
        boolean enabled = scripts.isScriptEnabled(name);
        String byline = "by " + this.entry.getAuthor() + (this.entry.getDate() == null || this.entry.getDate().isEmpty() ? "" : "  ·  " + this.entry.getDate());
        this.drawTitle(ellipsize(name, this.width - 90.0F, 11.0F), byline);

        String state = enabled ? "Enabled" : "Disabled";
        Color stateColor = enabled ? ENABLED : MUTED_TEXT;
        float stateWidth = font().getWidth(state, 8.0F) + 10.0F;
        float stateX = this.x + this.width - stateWidth - 10.0F;
        Render2DUtil.drawRoundedRect(stateX, this.y + 9.0F, stateWidth, 12.0F, 4.0F, 1.0F, ColorUtil.withAlpha(stateColor, 70));
        font().render(state, stateX + 5.0F, this.y + 9.0F + (12.0F - font().getLineHeight(8.0F)) / 2.0F, 8.0F, -1);

        float left = this.x + 10.0F;
        float contentWidth = this.width - 20.0F;
        float rowY = this.y + 36.0F;
        List<String> description = wrap(this.entry.getDescription(), contentWidth, 8.5F);
        float descriptionLineHeight = font().getLineHeight(8.5F);
        for (int i = 0; i < description.size() && i < 2; i++) {
            String line = description.get(i);
            if (i == 1 && description.size() > 2) {
                line = ellipsize(line + "...", contentWidth, 8.5F);
            }

            font().render(line, left, rowY + i * descriptionLineHeight, 8.5F, -1);
        }

        rowY += Math.max(1, Math.min(2, description.size())) * descriptionLineHeight + 6.0F;
        this.codeX = left;
        this.codeY = rowY;
        this.codeWidth = contentWidth;
        this.codeHeight = this.y + this.height - 44.0F - rowY;
        this.drawSource();

        if (!this.status.isEmpty()) {
            font().render(this.status, left, this.y + this.height - 40.0F, 8.0F, MUTED_TEXT.getRGB());
        }

        float buttonY = this.y + this.height - 24.0F;
        float buttonX = this.x + this.width - 10.0F;
        buttonX -= 60.0F;
        this.drawButton(enabled ? "Disable" : "Enable", buttonX, buttonY, 60.0F, 16.0F, mouseX, mouseY, true, this::toggle);
        buttonX -= 56.0F;
        this.drawButton("Reload", buttonX, buttonY, 52.0F, 16.0F, mouseX, mouseY, false, this::reload);
        buttonX -= 56.0F;
        this.drawButton("Copy", buttonX, buttonY, 52.0F, 16.0F, mouseX, mouseY, false, this::copySource);
        buttonX -= 56.0F;
        this.drawButton("Close", buttonX, buttonY, 52.0F, 16.0F, mouseX, mouseY, false, this::close);
    }

    private void drawSource() {
        Render2DUtil.drawRoundedRect(this.codeX, this.codeY, this.codeWidth, this.codeHeight, 4.0F, 1.0F, CODE_BACKGROUND);
        MSDFFontAtlas font = font();
        float lineHeight = font.getLineHeight(CODE_SIZE);
        int visible = this.visibleLines();
        this.scrollLine = Math.max(0, Math.min(this.scrollLine, Math.max(0, this.sourceLines.length - visible)));
        float gutter = font.getWidth(String.valueOf(this.sourceLines.length), CODE_SIZE) + 8.0F;
        float textWidth = this.codeWidth - gutter - 8.0F;

        for (int i = 0; i < visible && this.scrollLine + i < this.sourceLines.length; i++) {
            int index = this.scrollLine + i;
            float lineY = this.codeY + 4.0F + i * lineHeight;
            String number = String.valueOf(index + 1);
            font.render(number, this.codeX + gutter - 4.0F - font.getWidth(number, CODE_SIZE), lineY, CODE_SIZE, LINE_NUMBER.getRGB());
            String line = this.sourceLines[index].replace("\t", "    ");
            font.render(ellipsize(line, textWidth, CODE_SIZE), this.codeX + gutter + 4.0F, lineY, CODE_SIZE, -1);
        }

        if (this.sourceLines.length > visible) {
            float trackHeight = this.codeHeight - 8.0F;
            float thumbHeight = Math.max(12.0F, trackHeight * visible / this.sourceLines.length);
            float progress = (float)this.scrollLine / (this.sourceLines.length - visible);
            float thumbY = this.codeY + 4.0F + (trackHeight - thumbHeight) * progress;
            Render2DUtil.drawRoundedRect(this.codeX + this.codeWidth - 5.0F, thumbY, 2.5F, thumbHeight, 1.25F, 1.0F, ColorUtil.withAlpha(accent(), 160));
        }
    }

    private int visibleLines() {
        return Math.max(1, (int)((this.codeHeight - 8.0F) / font().getLineHeight(CODE_SIZE)));
    }

    @Override
    protected void onMouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        if (vertical == 0.0) {
            return;
        }

        int step = vertical > 0.0 ? -3 : 3;
        int maxScroll = Math.max(0, this.sourceLines.length - this.visibleLines());
        this.scrollLine = Math.max(0, Math.min(maxScroll, this.scrollLine + step));
    }

    @Override
    protected void onKeyPressed(int keyCode, int scanCode, int modifiers) {
        boolean control = (modifiers & (GLFW.GLFW_MOD_CONTROL | GLFW.GLFW_MOD_SUPER)) != 0;
        int maxScroll = Math.max(0, this.sourceLines.length - this.visibleLines());
        switch (keyCode) {
            case GLFW.GLFW_KEY_C -> {
                if (control) {
                    this.copySource();
                }
            }
            case GLFW.GLFW_KEY_UP -> this.scrollLine = Math.max(0, this.scrollLine - 1);
            case GLFW.GLFW_KEY_DOWN -> this.scrollLine = Math.min(maxScroll, this.scrollLine + 1);
            case GLFW.GLFW_KEY_PAGE_UP -> this.scrollLine = Math.max(0, this.scrollLine - this.visibleLines());
            case GLFW.GLFW_KEY_PAGE_DOWN -> this.scrollLine = Math.min(maxScroll, this.scrollLine + this.visibleLines());
            case GLFW.GLFW_KEY_HOME -> this.scrollLine = 0;
            case GLFW.GLFW_KEY_END -> this.scrollLine = maxScroll;
            case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER -> this.toggle();
            default -> {
            }
        }
    }

    @Override
    protected void close() {
        this.panel.clearSelection();
    }

    public ConfigEntryInfo getEntry() {
        return this.entry;
    }

    private void toggle() {
        ScriptManager scripts = VoilClient.getScriptManager();
        String name = this.entry.getName();
        if (!scripts.getScripts().containsKey(name)) {
            scripts.addScript(name, this.entry.getId());
        }

        if (scripts.isScriptEnabled(name)) {
            scripts.disableScript(name);
            this.status = name + " disabled.";
        } else {
            scripts.enableScript(name);
            scripts.runScript(name);
            this.status = name + " enabled.";
        }
    }

    private void reload() {
        ScriptManager scripts = VoilClient.getScriptManager();
        String name = this.entry.getName();
        if (scripts.getScripts().containsKey(name)) {
            scripts.reloadScript(name, this.entry.getId());
        } else {
            scripts.addScript(name, this.entry.getId());
        }

        this.status = name + " reloaded.";
    }

    private void copySource() {
        VoilClient.mc.keyboard.setClipboard(String.join("\n", this.sourceLines));
        this.status = "Source copied to clipboard.";
    }
}
