package lol.lwes.voil.gui;

import lol.lwes.voil.util.ScreenCorner;

public class Notification {
    CharSequence title;
    CharSequence message;
    ScreenCorner anchor;
    float posX;
    float posY;
    float targetY;
    long createdAt;

    public Notification(CharSequence charsequence, CharSequence charsequence1, ScreenCorner screencorner) {
        this.title = charsequence;
        this.message = charsequence1;
        this.anchor = screencorner;
        this.createdAt = System.currentTimeMillis();
    }
}
