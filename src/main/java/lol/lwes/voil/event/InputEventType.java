package lol.lwes.voil.event;

public enum InputEventType {
    Click,
    Scroll,
    Drag,
    Key,
    Char,
    Release;

    private static final int VALUE_COUNT = 6;

    private static InputEventType[] $values() {
        InputEventType[] ainputeventtype = new InputEventType[VALUE_COUNT];
        ainputeventtype[0] = Click;
        ainputeventtype[1] = Scroll;
        ainputeventtype[2] = Drag;
        ainputeventtype[3] = Key;
        ainputeventtype[4] = Char;
        ainputeventtype[5] = Release;
        return ainputeventtype;
    }
}
