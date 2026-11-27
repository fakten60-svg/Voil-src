package lol.lwes.voil.setting;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class ItemSlotsSetting extends Setting {
    private final Item[] items = new Item[9];
    private final Item[] defaultItems = new Item[9];
    private final List<Item> recommendedItems = new ArrayList<>();
    private boolean categoryMatch = false;

    public ItemSlotsSetting(String s, String s1) {
        super(s, s1);
    }

    public ItemSlotsSetting(String s, String s1, Item[] aitem) {
        super(s, s1);
        if (aitem.length != 9) {
            throw new IllegalArgumentException("defaults must have length " + 9);
        }

        for (int i = 0; i < 9; i++) {
            this.defaultItems[i] = aitem[i] == Items.AIR ? null : aitem[i];
            this.items[i] = this.defaultItems[i];
        }
    }

    public void clearSlot(int i) {
        checkSlot(i);
        this.items[i] = null;
    }

    public boolean isSlotEmpty(int i) {
        checkSlot(i);
        return this.items[i] == null || this.items[i] == Items.AIR;
    }

    public Item[] getItems() {
        Item[] aitem = new Item[9];
        System.arraycopy(this.items, 0, aitem, 0, 9);
        return aitem;
    }

    public void clearAll() {
        for (int i = 0; i < 9; i++) {
            this.items[i] = null;
        }
    }

    public void resetToDefaults() {
        System.arraycopy(this.defaultItems, 0, this.items, 0, 9);
    }

    private static void checkSlot(int i) {
        if (i < 0 || i >= 9) {
            throw new IndexOutOfBoundsException("slot " + i + " out of range [0, " + 9 + ")");
        }
    }

    public ItemSlotsSetting allowCategoryMatch() {
        this.categoryMatch = true;
        return this;
    }

    @Override
    public JsonElement toJson() {
        JsonArray jsonarray = new JsonArray();

        for (int i = 0; i < 9; i++) {
            Identifier identifier = this.getItemId(i);
            if (identifier != null && !this.isSlotEmpty(i)) {
                jsonarray.add(identifier.toString());
            } else {
                jsonarray.add(JsonNull.INSTANCE);
            }
        }

        return jsonarray;
    }

    public ItemSlotsSetting setRecommendedItems(Item[] aitem) {
        this.recommendedItems.clear();
        Collections.addAll(this.recommendedItems, aitem);
        return this;
    }

    public Item getItem(int i) {
        checkSlot(i);
        return this.items[i];
    }

    public List getRecommendedItems() {
        return Collections.unmodifiableList(this.recommendedItems);
    }

    public void fromJson(JsonElement jsonelement) {
        JsonArray jsonarray = jsonelement.getAsJsonArray();
        if (jsonarray.size() == 9) {
            for (int i = 0; i < 9; i++) {
                JsonElement jsonelement1 = jsonarray.get(i);
                if (jsonelement1 != null && !jsonelement1.isJsonNull()) {
                    String s = jsonelement1.getAsString();
                    if (s.isEmpty()) {
                        this.clearSlot(i);
                    } else {
                        Identifier identifier = Identifier.tryParse(s);
                        int j = i;
                        this.setItemById(j, identifier);
                    }
                } else {
                    this.clearSlot(i);
                }
            }
        }
    }

    public boolean isCategoryMatch() {
        return this.categoryMatch;
    }

    public void setItems(Item[] aitem) {
        if (aitem.length != 9) {
            throw new IllegalArgumentException("items must have length " + 9);
        }

        System.arraycopy(aitem, 0, this.items, 0, 9);
    }

    public void setItem(int i, Item item) {
        checkSlot(i);
        this.items[i] = item;
    }

    public void setItemById(int i, Identifier identifier) {
        if (identifier == null) {
            this.setItem(i, null);
        } else {
            this.setItem(i, Registries.ITEM.get(identifier));
        }
    }

    public Identifier getItemId(int i) {
        Item item = this.getItem(i);
        return item == null ? null : Registries.ITEM.getId(item);
    }

    @Override
    public void fromJson2(JsonElement jsonelement) {
        this.fromJson(jsonelement);
    }
}
