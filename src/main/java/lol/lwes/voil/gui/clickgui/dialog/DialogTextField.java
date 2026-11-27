package lol.lwes.voil.gui.clickgui.dialog;

import dev.prestigeclient.renderlib.api.font.MSDFFontAtlas;
import java.awt.Color;
import java.util.function.Predicate;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.config.GuiTheme;
import lol.lwes.voil.util.render.ColorUtil;
import lol.lwes.voil.util.render.Render2DUtil;
import org.lwjgl.glfw.GLFW;

public class DialogTextField {
    private static final float TEXT_SIZE = 8.5F;
    private static final float PADDING = 5.0F;
    private static final Color BACKGROUND = new Color(30, 30, 38);
    private static final Color PLACEHOLDER = new Color(122, 124, 134);

    private final String placeholder;
    private final int maxLength;
    private final boolean multiline;
    private final StringBuilder text = new StringBuilder();
    private Predicate<Character> filter = c -> true;
    private int cursor;
    private boolean focused;
    private float x;
    private float y;
    private float width;
    private float height;

    public DialogTextField(String placeholder, int maxLength, boolean multiline) {
        this.placeholder = placeholder;
        this.maxLength = maxLength;
        this.multiline = multiline;
    }

    public DialogTextField withFilter(Predicate<Character> filter) {
        this.filter = filter;
        return this;
    }

    public String getText() {
        return this.text.toString();
    }

    public void setText(String value) {
        this.text.setLength(0);
        if (value != null) {
            this.text.append(value, 0, Math.min(value.length(), this.maxLength));
        }

        this.cursor = this.text.length();
    }

    public boolean isFocused() {
        return this.focused;
    }

    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    public boolean isMultiline() {
        return this.multiline;
    }

    public void render(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        Color accent = GuiTheme.accentColor1 != null ? GuiTheme.accentColor1 : new Color(167, 139, 250);
        Render2DUtil.drawRoundedRect(x, y, width, height, 4.0F, 1.0F, BACKGROUND);
        Render2DUtil.drawOutline(x, y, width, height, 4.0F, 0.4F, 0.6F, this.focused ? accent : ColorUtil.withAlpha(Color.WHITE, 25));
        MSDFFontAtlas font = VoilClient.fonts.getSelectedFont();
        float lineHeight = font.getLineHeight(TEXT_SIZE);
        float innerWidth = width - PADDING * 2.0F;
        boolean caretVisible = this.focused && System.currentTimeMillis() / 500L % 2L == 0L;

        if (this.text.length() == 0 && !this.focused) {
            float textY = this.multiline ? y + PADDING : y + (height - lineHeight) / 2.0F;
            font.render(this.placeholder, x + PADDING, textY, TEXT_SIZE, PLACEHOLDER.getRGB());
            return;
        }

        if (!this.multiline) {
            String value = this.text.toString();
            int start = this.visibleStart(font, value, this.cursor, innerWidth);
            String visible = this.fitToWidth(font, value.substring(start), innerWidth);
            float textY = y + (height - lineHeight) / 2.0F;
            font.render(visible, x + PADDING, textY, TEXT_SIZE, -1);
            if (caretVisible) {
                float caretX = x + PADDING + font.getWidth(value.substring(start, this.cursor), TEXT_SIZE);
                Render2DUtil.drawRoundedRect(caretX, textY, 0.8F, lineHeight, 0.0F, 1.0F, Color.WHITE);
            }

            return;
        }

        String[] lines = this.text.toString().split("\n", -1);
        int cursorLine = this.cursorLine();
        int cursorColumn = this.cursor - this.lineStart(cursorLine);
        int visibleLines = Math.max(1, (int)((height - PADDING * 2.0F) / lineHeight));
        int firstLine = Math.max(0, Math.min(cursorLine - visibleLines + 1, lines.length - visibleLines));
        firstLine = Math.max(0, Math.min(firstLine, cursorLine));

        for (int i = firstLine; i < lines.length && i < firstLine + visibleLines; i++) {
            String line = lines[i];
            float lineY = y + PADDING + (i - firstLine) * lineHeight;
            int start = i == cursorLine ? this.visibleStart(font, line, cursorColumn, innerWidth) : 0;
            font.render(this.fitToWidth(font, line.substring(start), innerWidth), x + PADDING, lineY, TEXT_SIZE, -1);
            if (caretVisible && i == cursorLine) {
                float caretX = x + PADDING + font.getWidth(line.substring(start, cursorColumn), TEXT_SIZE);
                Render2DUtil.drawRoundedRect(caretX, lineY, 0.8F, lineHeight, 0.0F, 1.0F, Color.WHITE);
            }
        }
    }

    public boolean contains(double mouseX, double mouseY) {
        return mouseX >= this.x && mouseX <= this.x + this.width && mouseY >= this.y && mouseY <= this.y + this.height;
    }

    public boolean mouseClicked(double mouseX, double mouseY) {
        this.focused = this.contains(mouseX, mouseY);
        if (this.focused) {
            this.cursor = this.text.length();
        }

        return this.focused;
    }

    public boolean charTyped(char c) {
        if (!this.focused || c < 32 || c == 127 || !this.filter.test(c)) {
            return false;
        }

        this.insert(String.valueOf(c));
        return true;
    }

    public boolean keyPressed(int keyCode, int modifiers) {
        if (!this.focused) {
            return false;
        }

        boolean control = (modifiers & (GLFW.GLFW_MOD_CONTROL | GLFW.GLFW_MOD_SUPER)) != 0;
        switch (keyCode) {
            case GLFW.GLFW_KEY_BACKSPACE:
                if (this.cursor > 0) {
                    int from = control ? this.previousWordBoundary() : this.cursor - 1;
                    this.text.delete(from, this.cursor);
                    this.cursor = from;
                }
                return true;
            case GLFW.GLFW_KEY_DELETE:
                if (this.cursor < this.text.length()) {
                    this.text.deleteCharAt(this.cursor);
                }
                return true;
            case GLFW.GLFW_KEY_LEFT:
                this.cursor = Math.max(0, this.cursor - 1);
                return true;
            case GLFW.GLFW_KEY_RIGHT:
                this.cursor = Math.min(this.text.length(), this.cursor + 1);
                return true;
            case GLFW.GLFW_KEY_HOME:
                this.cursor = this.multiline ? this.lineStart(this.cursorLine()) : 0;
                return true;
            case GLFW.GLFW_KEY_END:
                this.cursor = this.multiline ? this.lineEnd(this.cursorLine()) : this.text.length();
                return true;
            case GLFW.GLFW_KEY_UP:
            case GLFW.GLFW_KEY_DOWN:
                if (!this.multiline) {
                    return false;
                }

                this.moveVertically(keyCode == GLFW.GLFW_KEY_UP ? -1 : 1);
                return true;
            case GLFW.GLFW_KEY_ENTER:
            case GLFW.GLFW_KEY_KP_ENTER:
                if (!this.multiline) {
                    return false;
                }

                String line = this.text.substring(this.lineStart(this.cursorLine()), this.cursor);
                int indent = 0;
                while (indent < line.length() && (line.charAt(indent) == ' ' || line.charAt(indent) == '\t')) {
                    indent++;
                }

                this.insert("\n" + line.substring(0, indent));
                return true;
            case GLFW.GLFW_KEY_TAB:
                if (!this.multiline) {
                    return false;
                }

                this.insert("    ");
                return true;
            case GLFW.GLFW_KEY_V:
                if (control) {
                    String clipboard = VoilClient.mc.keyboard.getClipboard();
                    if (clipboard != null) {
                        String cleaned = clipboard.replace("\r", "");
                        if (!this.multiline) {
                            cleaned = cleaned.replace('\n', ' ');
                        }

                        this.insert(cleaned);
                    }
                    return true;
                }
                return false;
            case GLFW.GLFW_KEY_C:
                if (control) {
                    VoilClient.mc.keyboard.setClipboard(this.text.toString());
                    return true;
                }
                return false;
            default:
                return false;
        }
    }

    private void insert(String value) {
        int room = this.maxLength - this.text.length();
        if (room <= 0) {
            return;
        }

        String inserted = value.length() > room ? value.substring(0, room) : value;
        this.text.insert(this.cursor, inserted);
        this.cursor += inserted.length();
    }

    private int previousWordBoundary() {
        int index = this.cursor;
        while (index > 0 && Character.isWhitespace(this.text.charAt(index - 1))) {
            index--;
        }

        while (index > 0 && !Character.isWhitespace(this.text.charAt(index - 1))) {
            index--;
        }

        return index;
    }

    private void moveVertically(int direction) {
        int line = this.cursorLine();
        int column = this.cursor - this.lineStart(line);
        int target = line + direction;
        int lineCount = this.lineCount();
        if (target < 0) {
            this.cursor = 0;
        } else if (target >= lineCount) {
            this.cursor = this.text.length();
        } else {
            int start = this.lineStart(target);
            this.cursor = Math.min(start + column, this.lineEnd(target));
        }
    }

    private int lineCount() {
        int count = 1;
        for (int i = 0; i < this.text.length(); i++) {
            if (this.text.charAt(i) == '\n') {
                count++;
            }
        }

        return count;
    }

    private int cursorLine() {
        int line = 0;
        for (int i = 0; i < this.cursor; i++) {
            if (this.text.charAt(i) == '\n') {
                line++;
            }
        }

        return line;
    }

    private int lineStart(int line) {
        int index = 0;
        for (int current = 0; current < line; current++) {
            int next = this.text.indexOf("\n", index);
            if (next < 0) {
                return this.text.length();
            }

            index = next + 1;
        }

        return index;
    }

    private int lineEnd(int line) {
        int start = this.lineStart(line);
        int end = this.text.indexOf("\n", start);
        return end < 0 ? this.text.length() : end;
    }

    private int visibleStart(MSDFFontAtlas font, String value, int caret, float maxWidth) {
        int start = 0;
        while (start < caret && font.getWidth(value.substring(start, caret), TEXT_SIZE) > maxWidth - 2.0F) {
            start++;
        }

        return start;
    }

    private String fitToWidth(MSDFFontAtlas font, String value, float maxWidth) {
        int end = value.length();
        while (end > 0 && font.getWidth(value.substring(0, end), TEXT_SIZE) > maxWidth) {
            end--;
        }

        return value.substring(0, end);
    }
}
