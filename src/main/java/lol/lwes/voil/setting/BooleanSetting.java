package lol.lwes.voil.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class BooleanSetting extends Setting {
    private boolean value;
    private Runnable onChange;

    public BooleanSetting(String s, boolean flag, String s1) {
        super(s, s1);
        this.value = flag;
    }

    public void setOnChange(Runnable runnable) {
        this.onChange = runnable;
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(this.value);
    }

    public void toggle() {
        this.value = !this.value;
        if (this.onChange != null) {
            this.onChange.run();
        }
    }

    public void setEnabled(boolean flag) {
        this.value = flag;
    }

    public boolean isEnabled() {
        return this.value;
    }

    public void fromJson(JsonElement jsonelement) {
        this.value = jsonelement.getAsBoolean();
    }

    @Override
    public void fromJson2(JsonElement jsonelement) {
        this.fromJson(jsonelement);
    }
}
