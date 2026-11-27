package lol.lwes.voil.gui.clickgui;

public enum ClickGuiTab {
    HOME,
    CONFIG,
    GUI,
    PROFILE,
    IRC,
    SOCIAL;

    private static final int VALUE_COUNT = 6;

    private static ClickGuiTab[] $values() {
        ClickGuiTab[] aclickguitab = new ClickGuiTab[VALUE_COUNT];
        aclickguitab[0] = HOME;
        aclickguitab[1] = CONFIG;
        aclickguitab[2] = GUI;
        aclickguitab[3] = PROFILE;
        aclickguitab[4] = IRC;
        aclickguitab[5] = SOCIAL;
        return aclickguitab;
    }
}
