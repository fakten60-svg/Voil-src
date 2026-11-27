package lol.lwes.voil.config;

public class ThemeProperty {
    private boolean modified = false;
    private final String name;
    private Object value;

    public ThemeProperty(String s, Object object) {
        this.name = s;
        this.value = object;
    }

    public void setModified(boolean flag) {
        this.modified = flag;
    }

    public Object getValue() {
        return this.value;
    }

    public void setValue(Object object) {
        this.value = object;
    }

    public boolean isModified() {
        return this.modified;
    }

    public String getName() {
        return this.name;
    }
}
