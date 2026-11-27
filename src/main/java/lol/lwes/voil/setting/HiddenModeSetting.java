package lol.lwes.voil.setting;

import com.google.gson.JsonElement;

public class HiddenModeSetting extends TransientModeSetting {
    public HiddenModeSetting(String s, String s1, String s2, String[] astring) {
        super(s, s1, s2, astring);
    }

    @Override
    public JsonElement toJson() {
        return null;
    }
}
