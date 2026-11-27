package lol.lwes.voil.setting;

import com.google.gson.JsonElement;
import lol.lwes.voil.module.miscellaneous.Proxy;

public class ProxyTextSetting extends StringSetting {
    final Proxy proxy;
    private static final int EMPTY_MASK_LENGTH = 8;

    public ProxyTextSetting(Proxy proxy, String s, String s1, String s2) {
        super(s, s1, s2);
        this.proxy = proxy;
    }

    @Override
    public String getMask() {
        return this.proxy.hideInfo.isEnabled() && this != this.proxy.port
            ? ".".repeat(this.getValue().isBlank() ? EMPTY_MASK_LENGTH : this.getValue().length())
            : null;
    }

    @Override
    public JsonElement toJson() {
        return null;
    }

    public void setRaw(String s) {
        super.setValue(s);
    }

    @Override
    public void setValue(String s) {
        super.setValue(s);
        this.proxy.saveCurrentProxy();
    }
}
