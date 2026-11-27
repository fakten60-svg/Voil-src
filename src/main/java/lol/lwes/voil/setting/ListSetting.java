package lol.lwes.voil.setting;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Map;
import java.util.Set;

public class ListSetting extends Setting {
    private final List<String> allItems;
    private final List<String> items;
    private final Set<String> enabledItems = new LinkedHashSet<>();
    private final Map<String, Double> minThresholds = new HashMap<>();
    private final Map<String, Double> defaultMinThresholds = new HashMap<>();
    private final Set<String> maxEnabledItems = new LinkedHashSet<>();
    private final Map<String, Double> maxThresholds = new HashMap<>();
    private final Map<String, Double> defaultMaxThresholds;
    private final List<String> pendingOrder;
    private final Map<String, Double> pendingMin;
    private final Map<String, Double> pendingMax;

    public ListSetting(String s, String s1, String[] astring) {
        super(s, s1);
        this.defaultMaxThresholds = new HashMap<>();
        this.pendingOrder = new ArrayList<>();
        this.pendingMin = new HashMap<>();
        this.pendingMax = new HashMap<>();
        this.allItems = new ArrayList<>(Arrays.asList(astring));
        this.items = new ArrayList(this.allItems);
    }

    public boolean isMaxEnabled(String s) {
        return this.maxEnabledItems.contains(s);
    }

    public double getMinThreshold(String s) {
        return this.minThresholds.getOrDefault(s, 0.0);
    }

    public double getMaxThreshold(String s) {
        return this.maxThresholds.getOrDefault(s, 20.0);
    }

    public void setMaxThreshold(String s, double d0) {
        if (this.maxEnabledItems.contains(s)) {
            this.maxThresholds.put(s, clampThreshold(d0));
        }
    }

    public void moveItem(int i, int j) {
        if (i >= 0 && i < this.items.size()) {
            int k = Math.max(0, Math.min(this.items.size() - 1, j));
            if (i != k) {
                this.items.add(k, (String)this.items.remove(i));
            }
        }
    }

    public List<String> getActiveItems(double d0) {
        ArrayList<String> arraylist = new ArrayList<>();

        for (String s : this.items) {
            if (!this.enabledItems.contains(s) || d0 >= this.getMinThreshold(s)) {
                arraylist.add(s);
            }
        }

        return arraylist;
    }

    public void enableMax(String s, double d0) {
        if (this.items.contains(s)) {
            this.maxEnabledItems.add(s);
            Double d1 = (Double)this.pendingMax.remove(s);
            this.maxThresholds.putIfAbsent(s, clampThreshold(d1 != null ? d1 : d0));
            this.defaultMaxThresholds.putIfAbsent(s, clampThreshold(d0));
        }
    }

    @Override
    public boolean isPersistent() {
        return true;
    }

    @Override
    public JsonElement toJson() {
        JsonObject jsonobject = new JsonObject();
        JsonArray jsonarray = new JsonArray();

        for (String s : this.items) {
            jsonarray.add(s);
        }

        jsonobject.add("order", jsonarray);
        JsonObject jsonobject1 = new JsonObject();

        for (String s1 : this.enabledItems) {
            jsonobject1.addProperty(s1, this.getMinThreshold(s1));
        }

        jsonobject.add("bands", jsonobject1);
        JsonObject jsonobject2 = new JsonObject();

        for (String s2 : this.maxEnabledItems) {
            jsonobject2.addProperty(s2, this.getMaxThreshold(s2));
        }

        jsonobject.add("healthBands", jsonobject2);
        return jsonobject;
    }

    public void setItems(List<String> list) {
        this.items.retainAll(list);

        for (String s : list) {
            if (!this.items.contains(s)) {
                this.items.add(s);
            }
        }

        if (!this.pendingOrder.isEmpty()) {
            ArrayList<String> arraylist = new ArrayList<>();

            for (String s1 : this.pendingOrder) {
                if (this.items.contains(s1) && !arraylist.contains(s1)) {
                    arraylist.add(s1);
                }
            }

            for (String s2 : this.items) {
                if (!arraylist.contains(s2)) {
                    arraylist.add(s2);
                }
            }

            this.items.clear();
            this.items.addAll(arraylist);
            this.pendingOrder.clear();
        }

        this.enabledItems.retainAll(list);
        this.maxEnabledItems.retainAll(list);
        this.minThresholds.keySet().retainAll(list);
        this.maxThresholds.keySet().retainAll(list);
        this.defaultMinThresholds.keySet().retainAll(list);
        this.defaultMaxThresholds.keySet().retainAll(list);
    }

    public List<String> getItems() {
        return this.items;
    }

    public void enable(String s, double d0) {
        if (this.items.contains(s)) {
            this.enabledItems.add(s);
            Double d1 = (Double)this.pendingMin.remove(s);
            this.minThresholds.putIfAbsent(s, clampThreshold(d1 != null ? d1 : d0));
            this.defaultMinThresholds.putIfAbsent(s, clampThreshold(d0));
        }
    }

    public void fromJson(JsonElement jsonelement) {
        if (jsonelement != null) {
            if (jsonelement.isJsonArray()) {
                this.loadOrder(jsonelement.getAsJsonArray());
            } else if (jsonelement.isJsonObject()) {
                JsonObject jsonobject = jsonelement.getAsJsonObject();
                if (jsonobject.has("order") && jsonobject.get("order").isJsonArray()) {
                    this.loadOrder(jsonobject.getAsJsonArray("order"));
                }

                if (jsonobject.has("bands") && jsonobject.get("bands").isJsonObject()) {
                    for (Entry entry : jsonobject.getAsJsonObject("bands").entrySet()) {
                        double d0 = clampThreshold(((JsonElement)entry.getValue()).getAsDouble());
                        if (this.enabledItems.contains(entry.getKey())) {
                            this.minThresholds.put((String)entry.getKey(), d0);
                        } else {
                            this.pendingMin.put((String)entry.getKey(), d0);
                        }
                    }
                }

                if (jsonobject.has("healthBands") && jsonobject.get("healthBands").isJsonObject()) {
                    for (Entry entry1 : jsonobject.getAsJsonObject("healthBands").entrySet()) {
                        double d1 = clampThreshold(((JsonElement)entry1.getValue()).getAsDouble());
                        if (this.maxEnabledItems.contains(entry1.getKey())) {
                            this.maxThresholds.put((String)entry1.getKey(), d1);
                        } else {
                            this.pendingMax.put((String)entry1.getKey(), d1);
                        }
                    }
                }
            }
        }
    }

    public ListSetting addDefaultEnabled(String s, double d0) {
        double d1 = clampThreshold(d0);
        this.enabledItems.add(s);
        this.minThresholds.put(s, d1);
        this.defaultMinThresholds.put(s, d1);
        return this;
    }

    public void setMinThreshold(String s, double d0) {
        if (this.enabledItems.contains(s)) {
            this.minThresholds.put(s, clampThreshold(d0));
        }
    }

    public boolean isEnabled(String s) {
        return this.enabledItems.contains(s);
    }

    public String getFirst() {
        return this.items.isEmpty() ? null : (String)this.items.get(0);
    }

    private static double clampThreshold(double d0) {
        return Math.max(0.0, Math.min(20.0, d0));
    }
    private void loadOrder(JsonArray jsonarray) {
        if (this.allItems.isEmpty()) {
            this.pendingOrder.clear();

            for (JsonElement jsonelement1 : jsonarray) {
                String s2 = jsonelement1.getAsString();
                if (!this.pendingOrder.contains(s2)) {
                    this.pendingOrder.add(s2);
                }
            }
        } else {
            ArrayList<String> arraylist = new ArrayList<>();

            for (JsonElement jsonelement : jsonarray) {
                String s = jsonelement.getAsString();
                if (this.allItems.contains(s) && !arraylist.contains(s)) {
                    arraylist.add(s);
                }
            }

            for (String s1 : this.allItems) {
                if (!arraylist.contains(s1)) {
                    arraylist.add(s1);
                }
            }

            if (!arraylist.isEmpty()) {
                this.items.clear();
                this.items.addAll(arraylist);
            }
        }
    }

    public String findMatchingItem(double d0, double d1) {
        for (String s : this.items) {
            if (this.matches(s, d0, d1)) {
                return s;
            }
        }

        return null;
    }

    public int indexOf(String s) {
        return this.items.indexOf(s);
    }

    public boolean matches(String s, double d0, double d1) {
        return this.enabledItems.contains(s) && d0 < this.getMinThreshold(s) ? false : !this.maxEnabledItems.contains(s) || !(d1 > this.getMaxThreshold(s));
    }

    @Override
    public void fromJson2(JsonElement jsonelement) {
        this.fromJson(jsonelement);
    }
}
