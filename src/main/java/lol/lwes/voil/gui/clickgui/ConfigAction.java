package lol.lwes.voil.gui.clickgui;

public enum ConfigAction {
    UPVOTE,
    DOWNVOTE,
    DOWNLOAD,
    UPDATE,
    DELETE,
    CANCEL;

    private static final int VALUE_COUNT = 6;

    private static ConfigAction[] $values() {
        ConfigAction[] aconfigaction = new ConfigAction[VALUE_COUNT];
        aconfigaction[0] = UPVOTE;
        aconfigaction[1] = DOWNVOTE;
        aconfigaction[2] = DOWNLOAD;
        aconfigaction[3] = UPDATE;
        aconfigaction[4] = DELETE;
        aconfigaction[5] = CANCEL;
        return aconfigaction;
    }
}
