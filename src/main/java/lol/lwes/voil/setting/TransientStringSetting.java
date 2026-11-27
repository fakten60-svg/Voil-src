package lol.lwes.voil.setting;

public class TransientStringSetting extends StringSetting {
    public TransientStringSetting(String s, String s1, String s2) {
        super(s, s1, s2);
    }

    @Override
    public boolean isPersistent() {
        return false;
    }
}
