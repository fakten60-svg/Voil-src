package lol.lwes.voil.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lol.lwes.voil.util.NamedSetEntry;

public class MultiSelectSetting extends Setting {
    private List<String> options;
    public Set<String> selected;

    public MultiSelectSetting(String s, String s1, String[] astring) {
        this(s, s1, astring == null ? List.of() : Arrays.asList(astring), astring != null && astring.length != 0 ? List.of(astring[0]) : List.of());
    }

    public MultiSelectSetting(String s, String[] astring, String[] astring1, String s1) {
        this(s, s1, Arrays.asList(astring), astring1 == null ? List.of() : Arrays.asList(astring1));
    }

    public MultiSelectSetting(String s, String s1, List<String> list, Collection<String> collection) {
        super(s, s1);
        this.selected = new HashSet<>();
        this.options = List.copyOf(new LinkedHashSet<>(list));
        if (collection != null) {
            for (String s2 : collection) {
                if (this.options.contains(s2)) {
                    this.selected.add(s2);
                }
            }
        }
    }

    public NamedSetEntry getOption(String s) {
        return this.options.stream().filter(s2 -> s2.equalsIgnoreCase(s)).findFirst().map(s1 -> new NamedSetEntry(this, s1)).orElse(null);
    }

    public Set<String> getSelectedOptions() {
        return this.selected.stream().map(s -> s.replaceAll("[&@#$%!\\^\\*\\(\\)\\-_+=]", "")).collect(Collectors.toSet());
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(String.join(",", this.selected));
    }

    public static MultiSelectSetting create(String s, String s1, Collection<String> collection, String[] astring) {
        return new MultiSelectSetting(s, s1, Arrays.asList(astring), collection);
    }

    public void deselect(String s) {
        this.selected.remove(s);
    }

    public List<String> getOptions() {
        return this.options.stream().map(s -> s.replaceAll("[&@#$%!\\^\\*\\(\\)\\-_+=]", "")).toList();
    }

    public void fromJson(JsonElement jsonelement) {
        this.selected.clear();
        String s = jsonelement.getAsString();
        if (!s.isEmpty()) {
            for (String s1 : s.split(",")) {
                this.select(s1.trim());
            }
        }
    }

    public void select(String s) {
        if (this.options.contains(s)) {
            this.selected.add(s);
        }
    }

    public boolean isSelected(String s) {
        return this.selected.contains(s);
    }

    @Override
    public void fromJson2(JsonElement jsonelement) {
        this.fromJson(jsonelement);
    }
}
