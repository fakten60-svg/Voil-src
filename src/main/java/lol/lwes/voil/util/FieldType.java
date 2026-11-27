package lol.lwes.voil.util;

public enum FieldType {
    STRING(0),
    INT(1),
    LONG(2),
    BOOL(3),
    DOUBLE(4),
    BYTES(5),
    LIST(6);

    private final int typeId;

    FieldType(int j) {
        this.typeId = j;
    }

    public static FieldType fromId(int i) {
        for (FieldType fieldtype : values()) {
            if (fieldtype.typeId == i) {
                return fieldtype;
            }
        }

        throw new IllegalArgumentException("unknown field type: " + i);
    }

    private static FieldType[] $values() {
        return new FieldType[]{STRING, INT, LONG, BOOL, DOUBLE, BYTES, LIST};
    }

    public int getId() {
        return this.typeId;
    }
}
