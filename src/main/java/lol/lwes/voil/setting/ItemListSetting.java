package lol.lwes.voil.setting;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lol.lwes.voil.util.ItemKind;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ShieldItem;
import net.minecraft.item.TridentItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Identifier;

public class ItemListSetting extends Setting {
    private final List<Item> items = new ArrayList<>();
    private final List<Item> defaultItems = new ArrayList<>();
    private final List<Item> recommendedItems = new ArrayList<>();
    private final Set<Block> blocks = new LinkedHashSet<>();
    private boolean categoryMatch = false;
    private Runnable onChange;

    public ItemListSetting(String s, String s1) {
        super(s, s1);
    }

    public ItemListSetting(String s, String s1, Item[] aitem) {
        super(s, s1);

        for (Item item : aitem) {
            if (item != null && item != Items.AIR && !this.defaultItems.contains(item)) {
                this.defaultItems.add(item);
                this.items.add(item);
            }
        }
    }

    public Set<Block> getBlocks() {
        return Collections.unmodifiableSet(this.blocks);
    }

    public int getItemCount() {
        return this.items.size();
    }

    public boolean isEmpty() {
        return this.items.isEmpty();
    }

    public Item getItem(int i) {
        return (Item)this.items.get(i);
    }

    public void removeAt(int i) {
        if (i >= 0 && i < this.items.size()) {
            this.items.remove(i);
        }
    }

    public void toggleItem(Item item) {
        if (item != null && item != Items.AIR) {
            if (!this.items.remove(item)) {
                this.items.add(item);
            }
        }
    }

    public ItemListSetting allowCategoryMatch() {
        this.categoryMatch = true;
        return this;
    }

    @Override
    public JsonElement toJson() {
        JsonArray jsonarray = new JsonArray();

        for (Item item : this.items) {
            Identifier identifier = Registries.ITEM.getId(item);
            if (identifier != null) {
                jsonarray.add(identifier.toString());
            }
        }

        return jsonarray;
    }

    public ItemListSetting setRecommendedItems(Item[] aitem) {
        this.recommendedItems.clear();
        Collections.addAll(this.recommendedItems, aitem);
        return this;
    }

    public List<Item> getItems() {
        return Collections.unmodifiableList(this.items);
    }

    public List<Item> getRecommendedItems() {
        return Collections.unmodifiableList(this.recommendedItems);
    }

    public void fromJson(JsonElement jsonelement) {
        this.items.clear();
        if (jsonelement != null && !jsonelement.isJsonNull() && jsonelement.isJsonArray()) {
            for (JsonElement jsonelement1 : jsonelement.getAsJsonArray()) {
                if (jsonelement1 != null && !jsonelement1.isJsonNull()) {
                    String s = jsonelement1.getAsString();
                    if (!s.isEmpty()) {
                        Identifier identifier = Identifier.tryParse(s);
                        if (identifier != null) {
                            Item item = Registries.ITEM.get(identifier);
                            if (item != null && item != Items.AIR && !this.items.contains(item)) {
                                this.items.add(item);
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean isCategoryMatch() {
        return this.categoryMatch;
    }

    public boolean containsItem(Item item) {
        return item != null && this.items.contains(item);
    }

    public ItemListSetting addBlocks(Block[] ablock) {
        for (Block block : ablock) {
            if (block != null && block != Blocks.AIR) {
                this.blocks.add(block);
            }
        }

        return this;
    }

    public void resetToDefaults() {
        this.items.clear();
        this.items.addAll(this.defaultItems);
    }

    public ItemListSetting setOnChange(Runnable runnable) {
        this.onChange = runnable;
        return this;
    }

    public void notifyChange() {
        if (this.onChange != null) {
            this.onChange.run();
        }
    }

    public static boolean isCategoryComparable(ItemKind itemkind) {
        return itemkind != ItemKind.OTHER && itemkind != ItemKind.BLOCK && itemkind != ItemKind.POTION;
    }

    public void removeItem(Item item) {
        this.items.remove(item);
    }

    public boolean containsBlock(Block block) {
        if (block == null || block == Blocks.AIR) {
            return false;
        }

        if (this.blocks.contains(block)) {
            return true;
        }

        Item item = block.asItem();
        if (item == Items.AIR) {
            return false;
        }

        return this.matches(item);
    }

    public static ItemKind getCategory(Item item) {
        ItemStack itemstack = item.getDefaultStack();
        if (item instanceof BlockItem) {
            return ItemKind.BLOCK;
        } else if (itemstack.isIn(ItemTags.HEAD_ARMOR)
            || itemstack.isIn(ItemTags.CHEST_ARMOR)
            || itemstack.isIn(ItemTags.LEG_ARMOR)
            || itemstack.isIn(ItemTags.FOOT_ARMOR)) {
            return ItemKind.ARMOR;
        } else if (item instanceof ShieldItem) {
            return ItemKind.ARMOR;
        } else if (itemstack.isIn(ItemTags.SWORDS)) {
            return ItemKind.SWORD;
        } else if (itemstack.isIn(ItemTags.AXES)) {
            return ItemKind.AXE;
        } else if (itemstack.isIn(ItemTags.PICKAXES)) {
            return ItemKind.PICKAXE;
        } else if (itemstack.isIn(ItemTags.SHOVELS)) {
            return ItemKind.SHOVEL;
        } else if (itemstack.isIn(ItemTags.HOES)) {
            return ItemKind.HOE;
        } else if (item instanceof BowItem || item instanceof CrossbowItem || item instanceof TridentItem) {
            return ItemKind.BOW;
        } else if (itemstack.get(DataComponentTypes.FOOD) != null) {
            return ItemKind.FOOD;
        } else {
            return itemstack.get(DataComponentTypes.POTION_CONTENTS) != null ? ItemKind.POTION : ItemKind.OTHER;
        }
    }

    public boolean matches(Item item) {
        if (item == null || item == Items.AIR) {
            return false;
        }

        if (this.items.contains(item)) {
            return true;
        }

        if (!this.categoryMatch) {
            return false;
        }

        ItemKind itemkind = getCategory(item);
        if (!isCategoryComparable(itemkind)) {
            return false;
        }

        Iterator iterator = this.items.iterator();

        while (iterator.hasNext()) {
            if (getCategory((Item)iterator.next()) == itemkind) {
                return true;
            }
        }

        return false;
    }

    public void clear() {
        this.items.clear();
    }

    public void addItem(Item item) {
        if (item != null && item != Items.AIR) {
            if (!this.items.contains(item)) {
                this.items.add(item);
            }
        }
    }

    @Override
    public void fromJson2(JsonElement jsonelement) {
        this.fromJson(jsonelement);
    }
}
