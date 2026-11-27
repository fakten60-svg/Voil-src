package lol.lwes.voil.module.miscellaneous;

import java.util.List;
import lol.lwes.voil.mixin.accessor.HandledScreenAccessor;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.ItemListSetting;
import lol.lwes.voil.setting.ModeSetting;
import lol.lwes.voil.setting.MultiSelectSetting;
import lol.lwes.voil.setting.NumberSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.inventory.InventoryUtil;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.TextContent;
import net.minecraft.text.TranslatableTextContent;

public class ChestStealer extends Module {
    private final ModeSetting mode = new ModeSetting("Mode", "Hover", "How to steal items", new String[]{"Auto", "Hover"});
    private final NumberSetting delay = new NumberSetting("Delay", 0.0, 10.0, 3.0, 1.0, "Ticks between stealing items");
    private final ModeSetting listMode = new ModeSetting(
        "List Mode", "Blacklist", "Whether listed items are ignored or the only ones stolen", new String[]{"Blacklist", "Whitelist"}
    );
    private final ItemListSetting items = new ItemListSetting("Items", "Items filtered by List Mode");
    private final MultiSelectSetting containers;
    private final BooleanSetting autoClose;
    private int delayTicks;
    private int lastSlot;

    public ChestStealer() {
        super("ChestStealer", "Automatically steals stuff from chests", Category.Miscellaneous);
        List list = List.of("Chests", "Shulker Boxes", "Ender Chests");
        this.containers = MultiSelectSetting.create(
            "Containers", "Which containers to steal from", list, new String[]{"Chests", "Shulker Boxes", "Ender Chests"}
        );
        this.autoClose = new BooleanSetting("Auto Close", true, "Close the container once there is nothing left to steal");
        this.lastSlot = -1;
        Setting[] asetting = new Setting[]{this.mode, this.delay, this.listMode, this.items, this.containers, this.autoClose};
        this.addSettings(asetting);
    }

    private void stealHoveredSlot(HandledScreen handledscreen) {
        Slot slot = ((HandledScreenAccessor)handledscreen).getFocusedSlot();
        if (slot != null && !(slot.inventory instanceof PlayerInventory)) {
            ItemStack itemstack = slot.getStack();
            if (this.shouldSteal(itemstack)) {
                int i = slot.id;
                InventoryUtil.clickSlot(i, 0, SlotActionType.QUICK_MOVE);
                this.delayTicks = this.delay.getInt();
            }
        }
    }

    private boolean hasInventorySpace(ItemStack itemstack) {
        PlayerInventory playerinventory = mc.player.getInventory();

        for (int i = 0; i <= 35; i++) {
            ItemStack itemstack1 = playerinventory.getStack(i);
            if (itemstack1.isEmpty()) {
                return true;
            }

            if (ItemStack.areItemsAndComponentsEqual(itemstack1, itemstack) && itemstack1.getCount() < itemstack1.getMaxCount()) {
                return true;
            }
        }

        return false;
    }

    private boolean isShulkerBoxEnabled(HandledScreen handledscreen) {
        return switch (handledscreen) {
            case ShulkerBoxScreen shulkerBoxScreen -> this.containers.isSelected("Shulker Boxes");
            case GenericContainerScreen containerScreen -> this.isEnderChest(containerScreen)
                ? this.containers.isSelected("Ender Chests")
                : this.containers.isSelected("Chests");
            default -> false;
        };
    }

    @Override
    public void onTick() {
        if (mc.player != null && mc.world != null) {
            if (mc.currentScreen instanceof HandledScreen handledscreen && this.isShulkerBoxEnabled(handledscreen)) {
                ScreenHandler screenhandler = handledscreen.getScreenHandler();
                if (screenhandler.syncId != this.lastSlot) {
                    this.lastSlot = screenhandler.syncId;
                    this.delayTicks = this.delay.getInt();
                }

                if (this.delayTicks > 0) {
                    int i = this.delayTicks--;
                } else if (screenhandler.getCursorStack().isEmpty()) {
                    if (this.autoClose.isEnabled() && !this.hasStealableItems(screenhandler)) {
                        mc.player.closeHandledScreen();
                        this.lastSlot = -1;
                    } else {
                        if (this.mode.is("Hover")) {
                            this.stealHoveredSlot(handledscreen);
                        } else {
                            this.stealItems(screenhandler);
                        }
                    }
                }
            } else {
                this.lastSlot = -1;
            }
        }
    }

    @Override
    public void onEnable() {
        this.delayTicks = 0;
        this.lastSlot = -1;
    }

    private boolean hasStealableItems(ScreenHandler screenhandler) {
        int k = screenhandler.slots.size();
        int i = k + -37 + 1;

        for (int j = 0; j < i; j++) {
            ItemStack itemstack = screenhandler.getSlot(j).getStack();
            if (this.shouldSteal(itemstack)) {
                return true;
            }
        }

        return false;
    }

    private boolean shouldSteal(ItemStack itemstack) {
        if (!itemstack.isEmpty() && this.hasInventorySpace(itemstack)) {
            ItemListSetting itemlistsetting = this.items;
            Item item1 = itemstack.getItem();
            boolean flag = itemlistsetting.matches(item1);
            return this.listMode.is("Whitelist") == flag;
        } else {
            return false;
        }
    }

    private boolean isEnderChest(GenericContainerScreen genericcontainerscreen) {
        TextContent textcontent = genericcontainerscreen.getTitle().getContent();
        return textcontent instanceof TranslatableTextContent && ((TranslatableTextContent)textcontent).getKey().equals("container.enderchest");
    }

    private void stealItems(ScreenHandler screenhandler) {
        int k = screenhandler.slots.size();
        int i = k + -37 + 1;

        for (int j = 0; j < i; j++) {
            ItemStack itemstack = screenhandler.getSlot(j).getStack();
            if (this.shouldSteal(itemstack)) {
                InventoryUtil.clickSlot(j, 0, SlotActionType.QUICK_MOVE);
                if (this.delay.getInt() > 0) {
                    this.delayTicks = this.delay.getInt();
                    return;
                }
            }
        }
    }
}
