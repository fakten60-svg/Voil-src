package lol.lwes.voil.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ModeSetting extends Setting {
    private List<String> modes;
    private String mode;
    private int index;
    private Runnable beforeSwitch;
    private Runnable afterSwitch;

    public ModeSetting(String s, String s1, String s2, String[] astring) {
        super(s, s2);
        this.mode = s1;
        this.modes = Arrays.asList(astring);
        this.index = this.modes.indexOf(s1);
    }

    public void next() {
        int k = this.index;
        int i = this.modes.size();
        if (k < i - 1) {
            i = this.index;
            int j = i + 1;
            this.setIndex(j);
        } else {
            k = this.index;
            i = this.modes.size();
            if (k >= i - 1) {
                this.setIndex(0);
            }
        }
    }

    public void cycle() {
        this.index++;
        if (this.index >= this.modes.size()) {
            this.index = 0;
        }

        this.setIndex(this.index);
    }

    public void previous() {
        if (this.index > 0 && this.index <= this.modes.size()) {
            int i = this.index--;
            int j = this.index;
            this.setIndex(j);
        } else if (this.index == 0) {
            this.index = 0;
        }
    }

    public void setAfterSwitch(Runnable runnable) {
        this.afterSwitch = runnable;
    }

    private void runBeforeSwitch() {
        if (this.beforeSwitch != null) {
            this.beforeSwitch.run();
        }
    }

    private void runAfterSwitch() {
        if (this.afterSwitch != null) {
            this.afterSwitch.run();
        }
    }

    public boolean is(String s) {
        return Objects.equals(this.mode, s)
            || Objects.equals(this.mode.replaceAll("[&@#$%!\\^\\*\\(\\)\\-_+=]", ""), s.replaceAll("[&@#$%!\\^\\*\\(\\)\\-_+=]", ""));
    }

    public void setModes(List<String> list, int i) {
        this.modes = list;
        this.index = i;
        this.mode = list.get(i);
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(this.mode);
    }

    public String getMode() {
        return this.mode.replaceAll("[&@#$%!\\^\\*\\(\\)\\-_+=]", "");
    }

    public int getIndex() {
        return this.index;
    }

    public void setMode(String s) {
        this.runBeforeSwitch();
        this.mode = s;
        this.index = this.modes.indexOf(s);
        this.runAfterSwitch();
    }

    public void fromJson(JsonElement jsonelement) {
        String s = jsonelement.getAsString();
        this.setMode(s);
    }

    public List<String> getModes() {
        return this.modes.stream().map(s -> s.replaceAll("[&@#$%!\\^\\*\\(\\)\\-_+=]", "")).collect(Collectors.toList());
    }

    public Runnable getAfterSwitch() {
        return this.afterSwitch;
    }

    public void setIndex(int i) {
        this.runBeforeSwitch();
        this.index = i;
        this.mode = (String)this.modes.get(i);
        this.runAfterSwitch();
    }

    public void setBeforeSwitch(Runnable runnable) {
        this.beforeSwitch = runnable;
    }

    public Runnable getBeforeSwitch() {
        return this.beforeSwitch;
    }

    @Override
    public void fromJson2(JsonElement jsonelement) {
        this.fromJson(jsonelement);
    }
}
