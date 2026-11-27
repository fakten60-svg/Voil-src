package lol.lwes.voil.util;

public enum ScreenCorner {
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_RIGHT;

    private static ScreenCorner[] $values() {
        return new ScreenCorner[]{TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT};
    }
}
