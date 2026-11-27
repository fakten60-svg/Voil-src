package lol.lwes.voil.gui.clickgui.dialog;

import com.google.gson.JsonObject;
import java.util.List;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.api.ApiClient;
import lol.lwes.voil.config.ConfigManager;
import lol.lwes.voil.gui.clickgui.ConfigScriptsPanel;
import org.lwjgl.glfw.GLFW;

public class CreateConfigDialog extends GuiDialog {
    private static final String[] CATEGORIES = new String[]{"Legit", "Closet", "Blatant", "HvH"};

    private final ConfigScriptsPanel panel;
    private final DialogTextField nameField = new DialogTextField("Config name", 32, false);
    private final DialogTextField descriptionField = new DialogTextField("Short description", 120, false);
    private final DialogTextField anticheatField = new DialogTextField("Anticheat (e.g. Grim, Vulcan)", 32, false);
    private final List<DialogTextField> fields = List.of(this.nameField, this.descriptionField, this.anticheatField);
    private int categoryIndex;
    private boolean isPublic = true;
    private boolean uploading;
    private String status = "";

    public CreateConfigDialog(ConfigScriptsPanel panel) {
        super(panel.screen);
        this.panel = panel;
        this.nameField.setFocused(true);
    }

    @Override
    protected float getDialogWidth() {
        return 260.0F;
    }

    @Override
    protected float getDialogHeight() {
        return 214.0F;
    }

    @Override
    protected void drawContent(int mouseX, int mouseY, float delta) {
        this.drawTitle("Create Config", "Upload your current module setup");
        float left = this.x + 10.0F;
        float fieldWidth = this.width - 20.0F;
        float rowY = this.y + 38.0F;

        this.drawLabel("Name", left, rowY);
        this.nameField.render(left, rowY + 10.0F, fieldWidth, 16.0F);
        rowY += 32.0F;
        this.drawLabel("Description", left, rowY);
        this.descriptionField.render(left, rowY + 10.0F, fieldWidth, 16.0F);
        rowY += 32.0F;
        this.drawLabel("Anticheat", left, rowY);
        this.anticheatField.render(left, rowY + 10.0F, fieldWidth, 16.0F);
        rowY += 32.0F;

        this.drawLabel("Category", left, rowY);
        this.drawButton(CATEGORIES[this.categoryIndex], left, rowY + 10.0F, 80.0F, 14.0F, mouseX, mouseY, false, () -> this.categoryIndex = (this.categoryIndex + 1) % CATEGORIES.length);
        this.drawToggle(this.isPublic ? "Public" : "Private", this.isPublic, left + 100.0F, rowY + 12.0F, mouseX, mouseY, () -> this.isPublic = !this.isPublic);

        if (!this.status.isEmpty()) {
            font().render(this.status, left, this.y + this.height - 36.0F, 8.0F, (this.uploading ? MUTED_TEXT : ERROR_TEXT).getRGB());
        }

        float buttonY = this.y + this.height - 24.0F;
        this.drawButton("Cancel", this.x + this.width - 140.0F, buttonY, 60.0F, 16.0F, mouseX, mouseY, false, this::close);
        this.drawButton(this.uploading ? "Uploading..." : "Upload", this.x + this.width - 72.0F, buttonY, 62.0F, 16.0F, mouseX, mouseY, true, this::submit);
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
        if (keyCode == GLFW.GLFW_KEY_TAB) {
            this.cycleFocus((modifiers & GLFW.GLFW_MOD_SHIFT) != 0 ? -1 : 1);
            return;
        }

        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            if (focused == this.anticheatField) {
                this.submit();
            } else {
                this.cycleFocus(1);
            }
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

    private void submit() {
        if (this.uploading) {
            return;
        }

        String name = this.nameField.getText().trim();
        if (name.isEmpty()) {
            this.status = "Please enter a name.";
            this.focus(this.nameField);
            return;
        }

        String description = this.descriptionField.getText().trim();
        String anticheat = this.anticheatField.getText().trim();
        JsonObject data = ConfigManager.serializeModules();
        this.uploading = true;
        this.status = "Uploading config...";
        ApiClient.INSTANCE
            .uploadConfig(data, name, description, this.isPublic, CATEGORIES[this.categoryIndex], anticheat.isEmpty() ? "Any" : anticheat, "Config")
            .whenComplete((response, throwable) -> VoilClient.mc.execute(() -> {
                this.uploading = false;
                if (throwable != null) {
                    this.status = "Upload failed, try again.";
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

    private void cycleFocus(int direction) {
        DialogTextField focused = this.focusedField();
        int index = focused == null ? -1 : this.fields.indexOf(focused);
        int next = Math.floorMod(index + direction, this.fields.size());
        this.focus(this.fields.get(next));
    }

    private void focus(DialogTextField target) {
        for (DialogTextField field : this.fields) {
            field.setFocused(field == target);
        }
    }
}
