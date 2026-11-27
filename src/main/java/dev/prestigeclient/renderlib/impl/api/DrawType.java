package dev.prestigeclient.renderlib.impl.api;

public enum DrawType {
    QUADS,
    TRIANGLES,
    CUBE,
    TRIANGLES_FAN,
    DEBUG_LINE_STRIP,
    QUAD_STRIP,
    LINES;

    private static DrawType[] $values() {
        return new DrawType[]{QUADS, TRIANGLES, CUBE, TRIANGLES_FAN, DEBUG_LINE_STRIP, QUAD_STRIP, LINES};
    }
}
