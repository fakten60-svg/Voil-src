package lol.lwes.voil.util;

import lol.lwes.voil.setting.MultiSelectSetting;

public class NamedSetEntry {
    private final String name;
    final MultiSelectSetting owner;

    public NamedSetEntry(MultiSelectSetting multiselectsetting, String s) {
        this.owner = multiselectsetting;
        this.name = s;
    }

    public void add() {
        String s = this.name;
        this.owner.select(s);
    }

    public void remove() {
        String s = this.name;
        this.owner.deselect(s);
    }

    public String getName() {
        return this.name;
    }

    public boolean isPresent() {
        return this.owner.selected.contains(this.name);
    }
}
