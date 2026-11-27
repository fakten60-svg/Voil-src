package lol.lwes.voil.module.player;

import java.util.HashSet;
import java.util.Set;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.ItemSlotsSetting;
import lol.lwes.voil.setting.KeybindSetting;
import lol.lwes.voil.setting.RangeSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.ItemCategory;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ShieldItem;
import net.minecraft.item.TridentItem;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.screen.slot.SlotActionType;

public class HotbarManager extends Module {
    private final BooleanSetting onKey = new BooleanSetting("On Key", false, "Only sort when the keybind is held");
    private final KeybindSetting keybind = new KeybindSetting("Keybind", 0, false, "Key to trigger hotbar sort");
    private final BooleanSetting throwJunk = new BooleanSetting("Throw Junk", false, "Throw items not in your hotbar layout");
    private final ItemSlotsSetting hotbar;
    private final ItemSlotsSetting junkWhitelist;
    private final RangeSetting delay;
    private int tickCounter;

    public HotbarManager() {
        super("HotbarManager", "Auto sorts your hotbar", Category.Player);
        Item[] aitem1 = new Item[]{Items.NETHERITE_SWORD, Items.ENDER_PEARL, Items.TOTEM_OF_UNDYING, null, null, null, null, null, null};
        String s = "Define your desired hotbar layout";
        ItemSlotsSetting itemslotssetting = new ItemSlotsSetting("Hotbar", s, aitem1);
        Item[] aitem = new Item[]{
            Items.NETHERITE_SWORD,
            Items.NETHERITE_AXE,
            Items.MACE,
            Items.BOW,
            Items.CROSSBOW,
            Items.SHIELD,
            Items.TOTEM_OF_UNDYING,
            Items.ENDER_PEARL,
            Items.END_CRYSTAL,
            Items.ENCHANTED_GOLDEN_APPLE,
            Items.GOLDEN_APPLE,
            Items.SPLASH_POTION,
            Items.OBSIDIAN,
            Items.FLINT_AND_STEEL,
            Items.NETHERITE_PICKAXE,
            Items.GLOWSTONE,
            Items.RESPAWN_ANCHOR,
            Items.TNT_MINECART
        };
        this.hotbar = itemslotssetting.setRecommendedItems(aitem).allowCategoryMatch();
        this.junkWhitelist = new ItemSlotsSetting("Junk Whitelist", "Items that will never be thrown as junk");
        this.delay = new RangeSetting("Delay", 0.0, 20.0, 1.0, 3.0, 1.0, "Delay between each item move in ticks");
        this.tickCounter = 0;
        Setting[] asetting = new Setting[]{this.hotbar, this.delay, this.onKey, this.keybind, this.throwJunk, this.junkWhitelist};
        this.addSettings(asetting);
        Setting.dependSetting(this.keybind, this.onKey);
        Setting.dependSetting(this.junkWhitelist, this.throwJunk);
    }

    private ItemCategory getItemKind(Item item) {
        ItemStack itemstack = item.getDefaultStack();
        if (item instanceof BlockItem) {
            return ItemCategory.BLOCK;
        } else if (itemstack.isIn(ItemTags.HEAD_ARMOR)
            || itemstack.isIn(ItemTags.CHEST_ARMOR)
            || itemstack.isIn(ItemTags.LEG_ARMOR)
            || itemstack.isIn(ItemTags.FOOT_ARMOR)) {
            return ItemCategory.ARMOR;
        } else if (item instanceof ShieldItem) {
            return ItemCategory.ARMOR;
        } else if (itemstack.isIn(ItemTags.SWORDS)) {
            return ItemCategory.SWORD;
        } else if (itemstack.isIn(ItemTags.AXES)) {
            return ItemCategory.AXE;
        } else if (itemstack.isIn(ItemTags.PICKAXES)) {
            return ItemCategory.PICKAXE;
        } else if (itemstack.isIn(ItemTags.SHOVELS)) {
            return ItemCategory.SHOVEL;
        } else if (itemstack.isIn(ItemTags.HOES)) {
            return ItemCategory.HOE;
        } else if (item instanceof BowItem || item instanceof CrossbowItem || item instanceof TridentItem) {
            return ItemCategory.BOW;
        } else if (itemstack.get(DataComponentTypes.FOOD) != null) {
            return ItemCategory.FOOD;
        } else {
            return itemstack.get(DataComponentTypes.POTION_CONTENTS) != null ? ItemCategory.POTION : ItemCategory.OTHER;
        }
    }

    private int findSlot(Item item, int i, Set set) {
        PlayerInventory playerinventory = mc.player.getInventory();

        for (int j = 9; j < 36; j++) {
            if (!set.contains(j) && this.itemsMatch(item, playerinventory.getStack(j).getItem())) {
                return j;
            }
        }

        for (int k = 0; k < 9; k++) {
            if (k != i && !set.contains(k) && this.itemsMatch(item, playerinventory.getStack(k).getItem())) {
                return k;
            }
        }

        return -1;
    }

    private boolean isOtherItem(Item item) {
        return this.getItemKind(item) == ItemCategory.OTHER;
    }

    @Override
    public void onTick() {
        if (mc.player != null) {
            if (!this.onKey.isEnabled() || this.keybind.isPressed(true)) {
                if (mc.currentScreen instanceof InventoryScreen) {
                    this.tickCounter++;
                    if (this.tickCounter >= this.delay.getRandomInt()) {
                        this.tickCounter = 0;
                        if (!this.fillMissingSlot() && this.throwJunk.isEnabled()) {
                            this.throwJunk();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onDisable() {
        this.tickCounter = 0;
    }

    private boolean fillMissingSlot() {
        PlayerInventory playerinventory = mc.player.getInventory();
        Item[] aitem = this.hotbar.getItems();

        for (int i = 0; i < 9; i++) {
            Item item = aitem[i];
            if (item != null && item != Items.AIR) {
                ItemStack itemstack = playerinventory.getStack(i);
                if (itemstack.isEmpty() || !this.itemsMatch(item, itemstack.getItem())) {
                    int j = this.findSlot(item, i, new HashSet());
                    if (j != -1) {
                        this.swapSlots(j, i);
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private void throwSlot(int i) {
        if (mc.interactionManager != null) {
            int j = i < 9 ? i + 36 : i;
            mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, j, 1, SlotActionType.THROW, mc.player);
        }
    }

    private boolean itemsMatch(Item item, Item item1) {
        if (item1 == null || item1 == Items.AIR) {
            return false;
        }

        if (item1 == item) {
            return true;
        }

        if (!this.hotbar.isCategoryMatch()) {
            return false;
        }

        ItemCategory itemcategory = this.getItemKind(item);
        return itemcategory != ItemCategory.OTHER && itemcategory != ItemCategory.BLOCK && itemcategory != ItemCategory.POTION
            ? this.getItemKind(item1) == itemcategory
            : false;
    }

    private void throwJunk() {
        PlayerInventory playerinventory = mc.player.getInventory();
        Item[] aitem = this.hotbar.getItems();
        HashSet hashset = new HashSet();

        for (int i = 0; i < 9; i++) {
            Item item = aitem[i];
            if (item != null && item != Items.AIR) {
                ItemStack itemstack = playerinventory.getStack(i);
                if (!itemstack.isEmpty() && this.itemsMatch(item, itemstack.getItem())) {
                    hashset.add(i);
                } else {
                    int j = this.findSlot(item, i, hashset);
                    if (j != -1) {
                        hashset.add(j);
                    }
                }
            }
        }

        for (int k = 0; k < 36; k++) {
            ItemStack itemstack1 = playerinventory.getStack(k);
            if (!itemstack1.isEmpty() && !hashset.contains(k)) {
                Item item1 = itemstack1.getItem();
                if (!this.isWhitelisted(item1) && this.isOtherItem(item1)) {
                    this.throwSlot(k);
                }
            }
        }
    }

    private boolean isWhitelisted(Item item) {
        for (int i = 0; i < 9; i++) {
            if (!this.junkWhitelist.isSlotEmpty(i) && this.junkWhitelist.getItem(i) == item) {
                return true;
            }
        }

        return false;
    }

    private void swapSlots(int i, int j) {
        if (mc.interactionManager != null) {
            int k = mc.player.playerScreenHandler.syncId;
            if (i >= 9) {
                mc.interactionManager.clickSlot(k, i, j, SlotActionType.SWAP, mc.player);
            } else {
                int l = i + 36;
                int i1 = j + 36;
                mc.interactionManager.clickSlot(k, l, 0, SlotActionType.PICKUP, mc.player);
                mc.interactionManager.clickSlot(k, i1, 0, SlotActionType.PICKUP, mc.player);
                if (!mc.player.playerScreenHandler.getCursorStack().isEmpty()) {
                    mc.interactionManager.clickSlot(k, l, 0, SlotActionType.PICKUP, mc.player);
                }
            }
        }
    }
}
