package lol.lwes.voil.api;

import java.util.List;
import java.util.Map;

public class ApiResponse {
    private final Map fields;

    public ApiResponse(Map map) {
        this.fields = map == null ? Map.of() : map;
    }

    public boolean getBoolean(String s) {
        return Boolean.TRUE.equals(this.fields.get(s));
    }

    public List getList(String s) {
        Object object = this.fields.get(s);
        return object instanceof List ? (List)object : List.of();
    }

    public String getString(String s) {
        Object object = this.fields.get(s);
        return object instanceof String ? (String)object : "";
    }

    public int getInt(String s) {
        Object object = this.fields.get(s);
        return object instanceof Number number ? number.intValue() : 0;
    }

    public Object get(String s) {
        return this.fields.get(s);
    }

    public double getDouble(String s) {
        Object object = this.fields.get(s);
        return object instanceof Double ? (Double)object : 0.0;
    }

    public Map getFields() {
        return this.fields;
    }

    public long getLong(String s) {
        Object object = this.fields.get(s);
        return object instanceof Number number ? number.longValue() : 0L;
    }

    public boolean has(String s) {
        return this.fields.containsKey(s);
    }

    public String getMessage() {
        return this.getString("message");
    }

    public byte[] getBytes(String s) {
        Object object = this.fields.get(s);
        return object instanceof byte[] ? (byte[])object : null;
    }

    public boolean isSuccess() {
        return this.getBoolean("ok") || this.getBoolean("success") || this.getBoolean("accepted");
    }
}
