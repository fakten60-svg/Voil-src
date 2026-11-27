package lol.lwes.voil.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class StringSetting extends Setting {
    private String value;
    private static final String SPECIAL_CHARS = "[&@#$%!\\^\\*\\(\\)\\-_+=]";

    public StringSetting(String s, String s1, String s2) {
        super(s, s2);
        this.value = s1;
    }

    public String getMask() {
        return null;
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(this.value);
    }

    public String getValue() {
        return this.value.replaceAll(SPECIAL_CHARS, "");
    }

    public void setValue(String s) {
        this.value = s;
    }

    public void fromJson(JsonElement jsonelement) {
        if (this.isPersistent()) {
            this.value = jsonelement.getAsString();
        }
    }

    @Override
    public boolean isPersistent() {
        return true;
    }

    @Override
    public void fromJson2(JsonElement jsonelement) {
        this.fromJson(jsonelement);
    }
}
