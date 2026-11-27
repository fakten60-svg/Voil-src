package lol.lwes.voil.setting;

import com.google.gson.JsonElement;

public class TransientModeSetting extends ModeSetting {
    public TransientModeSetting(String s, String s1, String s2, String[] astring) {
        super(s, s1, s2, astring);
    }

    @Override
    public JsonElement toJson() {
        return null;
    }

    public void fromJson() {
    }

    public void setModeSilently(String s) {
        Runnable runnable = this.getAfterSwitch();
        this.setAfterSwitch(null);
        super.setMode(s);
        this.setAfterSwitch(runnable);
    }

    @Override
    public void fromJson2(JsonElement jsonelement) {
        this.fromJson();
    }
}
