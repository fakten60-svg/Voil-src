package lol.lwes.voil.script;

import java.util.List;

public class ScriptValue {
    public static final ScriptValue NULL_VALUE = new ScriptValue(null);
    private final Object value;

    public ScriptValue(Object value) {
        this.value = value;
    }

    public static ScriptValue of(Object value) {
        return value == null ? NULL_VALUE : new ScriptValue(value);
    }

    public static ScriptValue nullValue() {
        return NULL_VALUE;
    }

    public Object getRaw() {
        return this.value;
    }

    public Object getValue() {
        return this.value;
    }

    public boolean isNull() {
        return this.value == null;
    }

    public boolean isNumber() {
        return this.value instanceof Number;
    }

    public boolean isList() {
        return this.value instanceof List;
    }

    @SuppressWarnings("unchecked")
    public List<Object> asList() {
        return (List<Object>)this.value;
    }

    public String asString() {
        return String.valueOf(this.value);
    }

    public int asInt() {
        if (this.value instanceof Number number) {
            return number.intValue();
        }

        return Integer.parseInt(String.valueOf(this.value).trim());
    }

    public float asFloat() {
        return (float)this.asDouble();
    }

    public double asDouble() {
        if (this.value instanceof Number number) {
            return number.doubleValue();
        }

        return Double.parseDouble(String.valueOf(this.value).trim());
    }

    @Override
    public String toString() {
        return String.valueOf(this.value);
    }
}
