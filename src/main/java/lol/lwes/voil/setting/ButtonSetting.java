package lol.lwes.voil.setting;

public class ButtonSetting extends Setting {
    private String label;
    private Runnable action;
    private static final String SPECIAL_CHARS = "[&@#$%!\\^\\*\\(\\)\\-_+=]";

    public ButtonSetting(String s, Runnable runnable, String s1) {
        super(s, s1);
        this.label = s;
        this.action = runnable;
    }

    public void setLabel(String s) {
        this.label = s;
    }

    @Override
    public boolean isPersistent() {
        return false;
    }

    public void runAction() {
        if (this.action != null) {
            this.action.run();
        }
    }

    public String getLabel() {
        return this.label.replaceAll(SPECIAL_CHARS, "");
    }
}
