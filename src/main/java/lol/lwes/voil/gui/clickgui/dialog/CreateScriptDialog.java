package lol.lwes.voil.gui.clickgui.dialog;

import com.google.gson.JsonObject;
import java.time.LocalDate;
import java.util.List;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.api.ApiClient;
import lol.lwes.voil.config.ClientSettings;
import lol.lwes.voil.gui.clickgui.ConfigEntryInfo;
import lol.lwes.voil.gui.clickgui.ConfigScriptsPanel;
import org.lwjgl.glfw.GLFW;

public class CreateScriptDialog extends GuiDialog {
    private static final String TEMPLATE = "on onTick {\n    \n}\n";

    private final ConfigScriptsPanel panel;
    private final DialogTextField nameField = new DialogTextField("Script name", 32, false)
        .withFilter(c -> Character.isLetterOrDigit(c) || c == '_' || c == '-' || c == ' ');
    private final DialogTextField descriptionField = new DialogTextField("What does it do?", 120, false);
    private final DialogTextField sourceField = new DialogTextField("Script source", 20000, true);
    private final List<DialogTextField> fields = List.of(this.nameField, this.descriptionField, this.sourceField);
    private boolean isPublic;
    private boolean uploading;
    private String status = "";

    public CreateScriptDialog(ConfigScriptsPanel panel) {
        super(panel.screen);
        this.panel = panel;
        this.sourceField.setText(TEMPLATE);
        this.nameField.setFocused(true);
    }

    @Override
    protected float getDialogWidth() {
        return 320.0F;
    }

    @Override
    protected float getDialogHeight() {
        return 262.0F;
    }

    @Override
    protected void drawContent(int mouseX, int mouseY, float delta) {
        this.drawTitle("Create Script", "Write a script and share it with others");
        float left = this.x + 10.0F;
        float fieldWidth = this.width - 20.0F;
        float halfWidth = (fieldWidth - 8.0F) / 2.0F;
        float rowY = this.y + 38.0F;

        this.drawLabel("Name", left, rowY);
        this.nameField.render(left, rowY + 10.0F, halfWidth, 16.0F);
        this.drawLabel("Description", left + halfWidth + 8.0F, rowY);
        this.descriptionField.render(left + halfWidth + 8.0F, rowY + 10.0F, halfWidth, 16.0F);
        rowY += 32.0F;

        this.drawLabel("Source", left, rowY);
        this.sourceField.render(left, rowY + 10.0F, fieldWidth, 140.0F);
        rowY += 156.0F;

        this.drawToggle(this.isPublic ? "Public" : "Private", this.isPublic, left, rowY + 4.0F, mouseX, mouseY, () -> this.isPublic = !this.isPublic);
        if (!this.status.isEmpty()) {
            font().render(this.status, left + 80.0F, rowY + 4.0F, 8.0F, (this.uploading ? MUTED_TEXT : ERROR_TEXT).getRGB());
        }

        float buttonY = this.y + this.height - 24.0F;
        this.drawButton("Cancel", this.x + this.width - 140.0F, buttonY, 60.0F, 16.0F, mouseX, mouseY, false, this::close);
        this.drawButton(this.uploading ? "Saving..." : "Create", this.x + this.width - 72.0F, buttonY, 62.0F, 16.0F, mouseX, mouseY, true, this::submit);
    }

    @Override
    protected boolean onMouseClicked(double mouseX, double mouseY, int button) {
        boolean handled = false;
        for (DialogTextField field : this.fields) {
            handled |= field.mouseClicked(mouseX, mouseY);
        }

        return handled;
    }

    @Override
    protected void onKeyPressed(int keyCode, int scanCode, int modifiers) {
        DialogTextField focused = this.focusedField();
        if (focused == this.sourceField) {
            if ((keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) && (modifiers & GLFW.GLFW_MOD_CONTROL) != 0) {
                this.submit();
                return;
            }

            focused.keyPressed(keyCode, modifiers);
            return;
        }

        if (keyCode == GLFW.GLFW_KEY_TAB || keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            int index = focused == null ? -1 : this.fields.indexOf(focused);
            int direction = keyCode == GLFW.GLFW_KEY_TAB && (modifiers & GLFW.GLFW_MOD_SHIFT) != 0 ? -1 : 1;
            this.focus(this.fields.get(Math.floorMod(index + direction, this.fields.size())));
            return;
        }

        if (focused != null) {
            focused.keyPressed(keyCode, modifiers);
        }
    }

    @Override
    protected void onCharTyped(char character, int modifiers) {
        DialogTextField focused = this.focusedField();
        if (focused != null) {
            focused.charTyped(character);
        }
    }

    @Override
    protected void close() {
        this.panel.clearSelection();
    }

    @SuppressWarnings("unchecked")
    private void submit() {
        if (this.uploading) {
            return;
        }

        String name = this.nameField.getText().trim();
        String source = this.sourceField.getText();
        if (name.isEmpty()) {
            this.status = "Please enter a name.";
            this.focus(this.nameField);
            return;
        }

        if (source.isBlank()) {
            this.status = "The script is empty.";
            this.focus(this.sourceField);
            return;
        }

        String description = this.descriptionField.getText().trim();
        String author = String.valueOf(ClientSettings.username.getValue());
        ConfigEntryInfo entry = new ConfigEntryInfo(name, author, description, LocalDate.now().toString(), source);
        ClientSettings.cloudScripts.put(name, entry);

        JsonObject data = new JsonObject();
        data.addProperty("source", source);
        this.uploading = true;
        this.status = "Saving script...";
        ApiClient.INSTANCE
            .uploadConfig(data, name, description, this.isPublic, "Script", "Any", "Script")
            .whenComplete((response, throwable) -> VoilClient.mc.execute(() -> {
                this.uploading = false;
                if (throwable != null) {
                    this.status = "Saved locally, upload failed.";
                } else {
                    this.close();
                }
            }));
    }

    private DialogTextField focusedField() {
        for (DialogTextField field : this.fields) {
            if (field.isFocused()) {
                return field;
            }
        }

        return null;
    }

    private void focus(DialogTextField target) {
        for (DialogTextField field : this.fields) {
            field.setFocused(field == target);
        }
    }
}
