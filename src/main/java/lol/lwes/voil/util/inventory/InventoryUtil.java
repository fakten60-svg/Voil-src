package lol.lwes.voil.util.inventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.mixin.accessor.ClientPlayerInteractionManagerAccessor;
import lol.lwes.voil.setting.ItemListSetting;
import lol.lwes.voil.util.ItemKind;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent.Entry;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SplashPotionItem;
import net.minecraft.item.consume.UseAction;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry.Reference;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.BeaconScreenHandler;
import net.minecraft.screen.BlastFurnaceScreenHandler;
import net.minecraft.screen.BrewingStandScreenHandler;
import net.minecraft.screen.CartographyTableScreenHandler;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.EnchantmentScreenHandler;
import net.minecraft.screen.FurnaceScreenHandler;
import net.minecraft.screen.Generic3x3ContainerScreenHandler;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.GrindstoneScreenHandler;
import net.minecraft.screen.HopperScreenHandler;
import net.minecraft.screen.LecternScreenHandler;
import net.minecraft.screen.LoomScreenHandler;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.screen.SmokerScreenHandler;
import net.minecraft.screen.StonecutterScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;

public abstract class InventoryUtil {
    private static final SlotClicker clickAction = new SlotClicker();
    public static Map armorSlotIndices = new HashMap();

    static {
        armorSlotIndices.put(EquipmentSlot.HEAD, 3);
        armorSlotIndices.put(EquipmentSlot.CHEST, 2);
        armorSlotIndices.put(EquipmentSlot.LEGS, 1);
        armorSlotIndices.put(EquipmentSlot.FEET, 0);
    }

    public static FoundSlot findHotbarStack(Predicate<ItemStack> predicate) {
        return findStackInRange(predicate, 8);
    }

    public static FoundSlot findStackInRange(Predicate predicate, int i) {
        int j = -1;
        int k = 0;

        for (int l = 0; l <= i; l++) {
            ItemStack itemstack = VoilClient.mc.player.getInventory().getStack(l);
            if (predicate.test(itemstack)) {
                if (j == -1) {
                    j = l;
                }

                int j1 = itemstack.getCount();
                int i1 = k;
                k = i1 + j1;
            }
        }

        return new FoundSlot(j, k);
    }

    public static void selectSlot(int i) {
        VoilClient.mc.player.getInventory().setSelectedSlot(i);
        ((ClientPlayerInteractionManagerAccessor)VoilClient.mc.interactionManager).syncSlot();
    }

    public static FoundSlot findBestToolInHotbar(BlockState blockstate) {
        float f = -1.0F;
        int i = -1;
        Registry registry = VoilClient.mc.player.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);

        for (int j = 0; j < 9; j++) {
            ItemStack itemstack = VoilClient.mc.player.getInventory().getStack(j);
            if (!itemstack.isEmpty() && itemstack.isSuitableFor(blockstate)) {
                float f1 = itemstack.getMiningSpeedMultiplier(blockstate);
                RegistryKey registrykey = Enchantments.EFFICIENCY;
                ItemStack itemstack1 = itemstack;
                int k = getEnchantmentLevel(itemstack1, registrykey, registry);
                if (k > 0) {
                    f1 += k * k + 1;
                }

                if (f1 > f) {
                    f = f1;
                    i = j;
                }
            }
        }

        return new FoundSlot(i, 1);
    }

    public static boolean isHolding(Item item, Hand hand) {
        return VoilClient.mc.player.getStackInHand(hand).isOf(item);
    }

    public static boolean isHoldingNamed(String s) {
        return isHoldingNamed(s, Hand.MAIN_HAND);
    }

    public static int findItemSlot(Item item, ItemListSetting itemlistsetting) {
        int i = -1;
        boolean flag = itemlistsetting != null && itemlistsetting.isCategoryMatch();
        ItemKind itemkind = flag ? ItemListSetting.getCategory(item) : null;
        boolean flag1 = flag && ItemListSetting.isCategoryComparable(itemkind);

        for (int j = 0; j < 9; j++) {
            ItemStack itemstack = VoilClient.mc.player.getInventory().getStack(j);
            if (itemstack.getItem() == item) {
                return j;
            }

            if (flag1 && i == -1 && !itemstack.isEmpty() && ItemListSetting.getCategory(itemstack.getItem()) == itemkind) {
                i = j;
            }
        }

        return i;
    }

    private static int toPlayerScreenSlot(int i) {
        if (isHotbarSlot(i)) {
            return i + 36;
        } else {
            return isArmorSlot(i) ? 5 + i + -37 + 1 : i;
        }
    }

    public static void clickSlot(int i, int j, SlotActionType slotactiontype) {
        VoilClient.mc.interactionManager.clickSlot(VoilClient.mc.player.currentScreenHandler.syncId, i, j, slotactiontype, VoilClient.mc.player);
    }

    public static boolean isUsingItem(PlayerEntity playerentity, Item item) {
        ItemStack itemstack = null;
        if (playerentity.getMainHandStack().getItem() == item) {
            itemstack = playerentity.getMainHandStack();
        } else if (playerentity.getMainHandStack().getUseAction() == UseAction.NONE && playerentity.getOffHandStack().getItem() == item) {
            itemstack = playerentity.getOffHandStack();
        }

        return itemstack != null;
    }

    public static int findSplashPotionSlot(StatusEffect statuseffect) {
        PlayerInventory playerinventory = VoilClient.mc.player.getInventory();

        for (int i = 0; i < 9; i++) {
            ItemStack itemstack = playerinventory.getStack(i);
            if (itemstack.getItem() instanceof SplashPotionItem) {
                PotionContentsComponent potioncontentscomponent = itemstack.get(DataComponentTypes.POTION_CONTENTS);
                if (potioncontentscomponent != null) {
                    Iterator iterator = potioncontentscomponent.getEffects().iterator();

                    while (iterator.hasNext()) {
                        if (((StatusEffectInstance)iterator.next()).getEffectType().value() == statuseffect) {
                            return i;
                        }
                    }
                }
            }
        }

        return -1;
    }

    public static int findEmptySlotFiltered(boolean flag) {
        if (VoilClient.mc.player == null) {
            return -1;
        }

        PlayerInventory playerinventory = VoilClient.mc.player.getInventory();
        int i = flag ? 9 : playerinventory.size();

        for (int j = 0; j < i; j++) {
            ItemStack itemstack = playerinventory.getStack(j);
            if (itemstack.contains(DataComponentTypes.TOOL)) {
                return j;
            }
        }

        return -1;
    }

    private static int toDispenserSlot(int i) {
        if (isHotbarSlot(i)) {
            return i + 36;
        } else {
            return isMainInventorySlot(i) ? i : -1;
        }
    }

    public static int getEnchantmentLevel(ItemStack itemstack, RegistryKey registrykey, RegistryWrapper registrywrapper) {
        Reference reference = registrywrapper.getOrThrow(registrykey);
        return reference != null && EnchantmentHelper.hasEnchantments(itemstack) ? EnchantmentHelper.getLevel(reference, itemstack) : 0;
    }

    public static boolean hasEnchantment(ItemStack itemstack, RegistryEntry registryentry) {
        return itemstack != null && !itemstack.isEmpty() && registryentry != null ? EnchantmentHelper.getLevel(registryentry, itemstack) > 0 : false;
    }

    public static boolean hasItemInHotbar(Item item) {
        PlayerInventory playerinventory = VoilClient.mc.player.getInventory();

        for (int i = 0; i <= 8; i++) {
            if (playerinventory.getStack(i).isOf(item)) {
                selectSlot(i);
                return true;
            }
        }

        return false;
    }

    public static FoundSlot findHotbar(Item[] aitem) {
        Predicate<ItemStack> predicate = itemstack -> {
            for (Item item : aitem) {
                if (itemstack.getItem() == item) {
                    return true;
                }
            }

            return false;
        };
        return findInventoryStack(predicate);
    }

    public static int findPotionSlot(StatusEffect statuseffect, int i, int j) {
        PlayerInventory playerinventory = VoilClient.mc.player.getInventory();
        StatusEffectInstance statuseffectinstance = new StatusEffectInstance(Registries.STATUS_EFFECT.getEntry(statuseffect), i, j);

        for (int k = 0; k < 9; k++) {
            ItemStack itemstack = playerinventory.getStack(k);
            if (itemstack.getItem() instanceof SplashPotionItem) {
                String s = itemstack.get(DataComponentTypes.POTION_CONTENTS).getEffects().toString();
                if (s.contains(statuseffectinstance.toString())) {
                    return k;
                }
            }
        }

        return -1;
    }

    public static int getSelectedSlot() {
        return VoilClient.mc.player == null ? -1 : VoilClient.mc.player.getInventory().getSelectedSlot();
    }

    public static boolean hasSplashEffect(ItemStack itemstack, StatusEffects statuseffects) {
        if (itemstack.getItem() instanceof SplashPotionItem) {
            Iterator iterator = itemstack.get(DataComponentTypes.POTION_CONTENTS).getEffects().iterator();

            while (iterator.hasNext()) {
                if (((StatusEffectInstance)iterator.next()).getEffectType() == statuseffects) {
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean containsItemInHotbar(Item item) {
        PlayerInventory playerinventory = VoilClient.mc.player.getInventory();

        for (int i = 0; i < 9; i++) {
            if (playerinventory.getStack(i).isOf(item)) {
                return true;
            }
        }

        return false;
    }

    public static boolean hasItemInInventory(Item item) {
        PlayerInventory playerinventory = VoilClient.mc.player.getInventory();

        for (int i = 0; i <= 8; i++) {
            if (playerinventory.getStack(i).isOf(item)) {
                playerinventory.setSelectedSlot(i);
                return true;
            }
        }

        return false;
    }

    public static ItemStack findStack(Item[] aitem) {
        Predicate<ItemStack> predicate = itemstack -> {
            for (Item item : aitem) {
                if (itemstack.getItem() == item) {
                    return true;
                }
            }

            return false;
        };
        FoundSlot foundslot = findInventoryStack(predicate);
        return foundslot.getSlot() == -1 ? ItemStack.EMPTY : VoilClient.mc.player.getInventory().getStack(foundslot.getSlot());
    }

    public static FoundSlot find(Item[] aitem) {
        Predicate<ItemStack> predicate = itemstack -> {
            for (Item item : aitem) {
                if (itemstack.getItem() == item) {
                    return true;
                }
            }

            return false;
        };
        return findHotbarStack(predicate);
    }

    public static int findHotbarSlotByTag(TagKey tagkey) {
        if (VoilClient.mc.player == null) {
            return -1;
        }

        for (int i = 0; i < 9; i++) {
            ItemStack itemstack = VoilClient.mc.player.getInventory().getStack(i);
            if (!itemstack.isEmpty() && itemstack.isIn(tagkey)) {
                return i;
            }
        }

        return -1;
    }

    public static boolean isSplashPotionMatching(StatusEffect statuseffect, int i, int j, ItemStack itemstack) {
        StatusEffectInstance statuseffectinstance = new StatusEffectInstance(Registries.STATUS_EFFECT.getEntry(statuseffect), i, j);
        return itemstack.getItem() instanceof SplashPotionItem
            && itemstack.get(DataComponentTypes.POTION_CONTENTS).getEffects().toString().contains(statuseffectinstance.toString());
    }

    public static boolean hasEnchantmentLevel(ItemStack itemstack, RegistryEntry registryentry, int i) {
        return itemstack != null && !itemstack.isEmpty() && registryentry != null && i > 0 ? EnchantmentHelper.getLevel(registryentry, itemstack) >= i : false;
    }

    public static boolean hasItemMatching(Predicate predicate) {
        PlayerInventory playerinventory = VoilClient.mc.player.getInventory();

        for (int i = 0; i < 36; i++) {
            ItemStack itemstack = playerinventory.getStack(i);
            if (predicate.test(itemstack.getItem())) {
                return true;
            }
        }

        return false;
    }

    private static float getArmorValue(ItemStack itemstack) {
        AttributeModifiersComponent attributemodifierscomponent = itemstack.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
        return attributemodifierscomponent == null
            ? 0.0F
            : (float)attributemodifierscomponent.modifiers()
                .stream()
                .filter(entry -> entry.attribute().equals(EntityAttributes.ARMOR))
                .mapToDouble(entry -> entry.modifier().value())
                .sum();
    }

    public static boolean isSplashPotionWith(StatusEffect statuseffect, ItemStack itemstack) {
        if (!(itemstack.getItem() instanceof SplashPotionItem)) {
            return false;
        }

        PotionContentsComponent potioncontentscomponent = itemstack.get(DataComponentTypes.POTION_CONTENTS);
        if (potioncontentscomponent == null) {
            return false;
        }

        Iterator iterator = potioncontentscomponent.getEffects().iterator();

        while (iterator.hasNext()) {
            if (((StatusEffectInstance)iterator.next()).getEffectType().value() == statuseffect) {
                return true;
            }
        }

        return false;
    }

    public static boolean isStackInTag(TagKey tagkey, ItemStack itemstack) {
        if (VoilClient.mc.player == null) {
            return false;
        } else {
            return itemstack.isEmpty() ? false : itemstack.isIn(tagkey);
        }
    }

    public static FoundSlot findItemMatching(Predicate predicate, Item[] aitem) {
        for (int i = 0; i < 36; i++) {
            ItemStack itemstack = VoilClient.mc.player.getInventory().getStack(i);
            if (predicate.test(itemstack)) {
                for (Item item : aitem) {
                    if (itemstack.getItem() == item) {
                        return new FoundSlot(i, itemstack.getCount());
                    }
                }
            }
        }

        return new FoundSlot(-1, 0);
    }

    public static int getDefaultSlot() {
        return 0;
    }

    public static boolean isBlockItem(Item item) {
        return item instanceof BlockItem;
    }

    private static int toCartographySlot(int i) {
        if (isHotbarSlot(i)) {
            return i + 30;
        } else if (isMainInventorySlot(i)) {
            int j = i - 9;
            return 3 + j;
        } else {
            return -1;
        }
    }

    private static float getToughnessValue(ItemStack itemstack) {
        AttributeModifiersComponent attributemodifierscomponent = itemstack.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
        return attributemodifierscomponent == null
            ? 0.0F
            : (float)attributemodifierscomponent.modifiers()
                .stream()
                .filter(entry -> entry.attribute().equals(EntityAttributes.ARMOR_TOUGHNESS))
                .mapToDouble(entry -> entry.modifier().value())
                .sum();
    }

    public static boolean isBetterArmor(ItemStack itemstack, ItemStack itemstack1, RegistryWrapper registrywrapper) {
        EquippableComponent equippablecomponent = itemstack.get(DataComponentTypes.EQUIPPABLE);
        EquippableComponent equippablecomponent1 = itemstack1.get(DataComponentTypes.EQUIPPABLE);
        if (equippablecomponent != null && equippablecomponent1 != null) {
            float f = getArmorValue(itemstack) + getToughnessValue(itemstack);
            float f1 = getArmorValue(itemstack1) + getToughnessValue(itemstack1);
            if (f < f1) {
                return true;
            }

            RegistryKey registrykey = Enchantments.PROTECTION;
            ItemStack itemstack2 = itemstack;
            f += getEnchantmentLevel(itemstack2, registrykey, registrywrapper);
            RegistryKey registrykey1 = Enchantments.UNBREAKING;
            ItemStack itemstack3 = itemstack;
            f += getEnchantmentLevel(itemstack3, registrykey1, registrywrapper);
            RegistryKey registrykey2 = Enchantments.MENDING;
            ItemStack itemstack4 = itemstack;
            f += getEnchantmentLevel(itemstack4, registrykey2, registrywrapper) * 2;
            RegistryKey registrykey3 = Enchantments.PROTECTION;
            ItemStack itemstack5 = itemstack1;
            f1 += getEnchantmentLevel(itemstack5, registrykey3, registrywrapper);
            RegistryKey registrykey4 = Enchantments.UNBREAKING;
            ItemStack itemstack6 = itemstack1;
            f1 += getEnchantmentLevel(itemstack6, registrykey4, registrywrapper);
            RegistryKey registrykey5 = Enchantments.MENDING;
            ItemStack itemstack7 = itemstack1;
            int i = getEnchantmentLevel(itemstack7, registrykey5, registrywrapper);
            f1 += i + i;
            if (f == f1) {
                EquipmentSlot equipmentslot = equippablecomponent.slot();
                if (equipmentslot != EquipmentSlot.HEAD) {
                    if (equipmentslot == EquipmentSlot.FEET) {
                        RegistryKey registrykey10 = Enchantments.DEPTH_STRIDER;
                        ItemStack itemstack12 = itemstack;
                        f += getEnchantmentLevel(itemstack12, registrykey10, registrywrapper);
                        RegistryKey registrykey11 = Enchantments.SWIFT_SNEAK;
                        ItemStack itemstack13 = itemstack;
                        f += getEnchantmentLevel(itemstack13, registrykey11, registrywrapper);
                        RegistryKey registrykey12 = Enchantments.SOUL_SPEED;
                        ItemStack itemstack14 = itemstack;
                        f += getEnchantmentLevel(itemstack14, registrykey12, registrywrapper);
                        RegistryKey registrykey13 = Enchantments.FEATHER_FALLING;
                        ItemStack itemstack15 = itemstack;
                        f += getEnchantmentLevel(itemstack15, registrykey13, registrywrapper);
                        RegistryKey registrykey14 = Enchantments.DEPTH_STRIDER;
                        ItemStack itemstack16 = itemstack1;
                        f1 += getEnchantmentLevel(itemstack16, registrykey14, registrywrapper);
                        RegistryKey registrykey15 = Enchantments.SWIFT_SNEAK;
                        ItemStack itemstack17 = itemstack1;
                        f1 += getEnchantmentLevel(itemstack17, registrykey15, registrywrapper);
                        RegistryKey registrykey16 = Enchantments.SOUL_SPEED;
                        ItemStack itemstack18 = itemstack1;
                        f1 += getEnchantmentLevel(itemstack18, registrykey16, registrywrapper);
                        RegistryKey registrykey17 = Enchantments.FEATHER_FALLING;
                        ItemStack itemstack19 = itemstack1;
                        f1 += getEnchantmentLevel(itemstack19, registrykey17, registrywrapper);
                    }
                } else {
                    RegistryKey registrykey6 = Enchantments.AQUA_AFFINITY;
                    ItemStack itemstack8 = itemstack;
                    f += getEnchantmentLevel(itemstack8, registrykey6, registrywrapper);
                    RegistryKey registrykey7 = Enchantments.RESPIRATION;
                    ItemStack itemstack9 = itemstack;
                    f += getEnchantmentLevel(itemstack9, registrykey7, registrywrapper);
                    RegistryKey registrykey8 = Enchantments.AQUA_AFFINITY;
                    ItemStack itemstack10 = itemstack1;
                    f1 += getEnchantmentLevel(itemstack10, registrykey8, registrywrapper);
                    RegistryKey registrykey9 = Enchantments.RESPIRATION;
                    ItemStack itemstack11 = itemstack1;
                    f1 += getEnchantmentLevel(itemstack11, registrykey9, registrywrapper);
                }
            }

            return f < f1;
        } else {
            return false;
        }
    }

    public static int findEmptySlot() {
        if (VoilClient.mc.player == null) {
            return -1;
        }

        for (int i = 0; i < 9; i++) {
            ItemStack itemstack = VoilClient.mc.player.getInventory().getStack(i);
            if (!itemstack.isEmpty() && itemstack.isIn(ItemTags.CHEST_ARMOR)) {
                return i;
            }
        }

        return -1;
    }

    public static List<ItemStack> getHotbarStacks() {
        ArrayList<ItemStack> arraylist = new ArrayList<>();

        for (int i = 0; i < 9; i++) {
            ItemStack itemstack = VoilClient.mc.player.getInventory().getStack(i);
            if (!itemstack.isEmpty()) {
                arraylist.add(itemstack);
            }
        }

        return arraylist;
    }

    public static ItemStack getStack(int i) {
        return VoilClient.mc.player != null && i >= 0 && i <= VoilClient.mc.player.getInventory().size()
            ? VoilClient.mc.player.getInventory().getStack(i)
            : ItemStack.EMPTY;
    }

    public static FoundSlot findInventoryStack(Predicate<ItemStack> predicate) {
        return findStackInRange(predicate, VoilClient.mc.player.getInventory().size());
    }

    public static SlotClicker pickupActionFlagged() {
        clickAction.actionType = SlotActionType.PICKUP;
        clickAction.clickSecondSlot = true;
        return clickAction;
    }

    public static SlotClicker pickupAction() {
        clickAction.actionType = SlotActionType.PICKUP;
        return clickAction;
    }

    public static boolean stackNameContains(String s, ItemStack itemstack) {
        return itemstack.getItem().getTranslationKey().toLowerCase().contains(s.toLowerCase()) && itemstack.getName().getString().contains(s);
    }

    private static int toLecternSlot() {
        return -1;
    }

    private static int toGrindstoneSlot(int i) {
        if (isHotbarSlot(i)) {
            return 30 + i;
        } else if (isMainInventorySlot(i)) {
            int j = i + -10 + 1;
            return j + 3;
        } else {
            return -1;
        }
    }

    private static int toLoomSlot(int i) {
        if (isHotbarSlot(i)) {
            return 31 + i;
        } else if (isMainInventorySlot(i)) {
            int j = i - 9;
            return j + 4;
        } else {
            return -1;
        }
    }

    public static int getVersionCode() {
        return 77;
    }

    public static void forEachNonEmptyStack(Consumer consumer) {
        for (int i = 0; i < 9; i++) {
            ItemStack itemstack = VoilClient.mc.player.getInventory().getStack(i);
            if (itemstack != null && !itemstack.isEmpty()) {
                consumer.accept(itemstack);
            }
        }
    }

    public static ItemStack[] getInventoryStacks() {
        PlayerInventory playerinventory = VoilClient.mc.player.getInventory();
        ItemStack[] aitemstack = new ItemStack[0];

        for (int i = 0; i < 9; i++) {
            aitemstack[i] = playerinventory.getStack(i);
        }

        return aitemstack;
    }

    public static int findBlockItemSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack itemstack = VoilClient.mc.player.getInventory().getStack(i);
            if (itemstack.getItem() instanceof BlockItem) {
                BlockState blockstate = ((BlockItem)itemstack.getItem()).getBlock().getDefaultState();
                if (blockstate.getBlock() instanceof SlabBlock) {
                    return i;
                }
            }
        }

        return -1;
    }

    public static int getContainerSlotId(int i) {
        if (VoilClient.mc.player == null) {
            return -1;
        } else {
            ScreenHandler screenhandler = VoilClient.mc.player.currentScreenHandler;
            if (screenhandler instanceof PlayerScreenHandler) {
                return toPlayerScreenSlot(i);
            } else if (screenhandler instanceof GenericContainerScreenHandler) {
                return toGenericContainerSlot(i, ((GenericContainerScreenHandler)screenhandler).getRows());
            } else if (screenhandler instanceof CraftingScreenHandler) {
                return toCraftingSlot(i);
            } else if (screenhandler instanceof FurnaceScreenHandler) {
                return 0;
            } else if (screenhandler instanceof BlastFurnaceScreenHandler) {
                return 0;
            } else if (screenhandler instanceof SmokerScreenHandler) {
                return 0;
            } else if (screenhandler instanceof Generic3x3ContainerScreenHandler) {
                return toDispenserSlot(i);
            } else if (screenhandler instanceof EnchantmentScreenHandler) {
                return 0;
            } else if (screenhandler instanceof BrewingStandScreenHandler) {
                return 0;
            } else if (screenhandler instanceof MerchantScreenHandler) {
                return toMerchantSlot(i);
            } else if (screenhandler instanceof BeaconScreenHandler) {
                return toBeaconSlot(i);
            } else if (screenhandler instanceof AnvilScreenHandler) {
                return toAnvilSlot(i);
            } else if (screenhandler instanceof HopperScreenHandler) {
                return toHopperSlot(i);
            } else if (screenhandler instanceof ShulkerBoxScreenHandler) {
                return toGenericContainerSlot(i, 3);
            } else if (screenhandler instanceof CartographyTableScreenHandler) {
                return toCartographySlot(i);
            } else if (screenhandler instanceof GrindstoneScreenHandler) {
                return toGrindstoneSlot(i);
            } else if (screenhandler instanceof LecternScreenHandler) {
                return -1;
            } else if (screenhandler instanceof LoomScreenHandler) {
                return toLoomSlot(i);
            } else {
                return screenhandler instanceof StonecutterScreenHandler ? 0 : -1;
            }
        }
    }

    public static boolean isHolding(Item item) {
        return isHolding(item, Hand.MAIN_HAND);
    }

    public static FoundSlot findMainInventoryStack(Predicate<ItemStack> predicate) {
        for (int i = 0; i < 36; i++) {
            ItemStack itemstack = VoilClient.mc.player.getInventory().getStack(i);
            if (predicate.test(itemstack)) {
                return new FoundSlot(i, itemstack.getCount());
            }
        }

        return new FoundSlot(-1, 0);
    }

    public static int findHotbarSlot(Item item) {
        for (int i = 0; i < 9; i++) {
            ItemStack itemstack = VoilClient.mc.player.getInventory().getStack(i);
            if (itemstack.getItem() == item) {
                return i;
            }
        }

        return -1;
    }

    private static int toAnvilSlot(int i) {
        if (isHotbarSlot(i)) {
            return 30 + i;
        } else if (isMainInventorySlot(i)) {
            int j = i + -10 + 1;
            return 3 + j;
        } else {
            return -1;
        }
    }

    public static void switchToItem(Item item, Hand hand) {
        int i = findInventorySlot(item);
        if (i != -1) {
            hasItemInHotbar(Item.byRawId(i));
            VoilClient.mc.player.getInventory().setSelectedSlot(i);
            VoilClient.mc.interactionManager.clickSlot(VoilClient.mc.player.playerScreenHandler.syncId, i + 36, 0, SlotActionType.PICKUP, VoilClient.mc.player);
            VoilClient.mc.player.setStackInHand(hand, VoilClient.mc.player.getInventory().getStack(i));
        }
    }

    public static boolean isHotbarSlot(int i) {
        return i >= 0 && i <= 8;
    }

    private static int toHopperSlot(int i) {
        if (isHotbarSlot(i)) {
            return i + 32;
        } else if (isMainInventorySlot(i)) {
            int j = i - 9;
            return 5 + j;
        } else {
            return -1;
        }
    }

    public static boolean isMainInventorySlot(int i) {
        return i >= 9 && i <= 35;
    }

    private static int toMerchantSlot(int i) {
        if (isHotbarSlot(i)) {
            return i + 30;
        } else if (isMainInventorySlot(i)) {
            int j = i + -10 + 1;
            return j + 3;
        } else {
            return -1;
        }
    }

    public static int findInventorySlot(Item item) {
        for (int i = 0; i < 36; i++) {
            ItemStack itemstack = VoilClient.mc.player.getInventory().getStack(i);
            if (itemstack.getItem() == item) {
                return i;
            }
        }

        return -1;
    }

    public static boolean isHoldingNamed(String s, Hand hand) {
        ItemStack itemstack = VoilClient.mc.player.getStackInHand(hand);
        return itemstack != null && itemstack.getItem().getTranslationKey().toLowerCase().contains(s.toLowerCase());
    }

    public static FoundSlot findBestToolInInventory(BlockState blockstate) {
        float f = -1.0F;
        int i = -1;
        Registry registry = VoilClient.mc.player.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);

        for (int j = 0; j < VoilClient.mc.player.getInventory().size(); j++) {
            ItemStack itemstack = VoilClient.mc.player.getInventory().getStack(j);
            if (!itemstack.isEmpty() && itemstack.isSuitableFor(blockstate)) {
                float f1 = itemstack.getMiningSpeedMultiplier(blockstate);
                RegistryKey registrykey = Enchantments.EFFICIENCY;
                ItemStack itemstack1 = itemstack;
                int k = getEnchantmentLevel(itemstack1, registrykey, registry);
                if (k > 0 && !itemstack.isEmpty()) {
                    int l = k * k;
                    f1 += l + 1;
                }

                if (f1 > f) {
                    f = f1;
                    i = j;
                }
            }
        }

        return new FoundSlot(i, 1);
    }

    private static int toGenericContainerSlot(int i, int j) {
        if (isHotbarSlot(i)) {
            int i1 = j + 3;
            i1 *= 9;
            return i1 + i;
        } else if (isMainInventorySlot(i)) {
            int j1 = j * 9;
            int l = i + -10 + 1;
            int k = j1;
            return k + l;
        } else {
            return -1;
        }
    }

    public static boolean isArmorSlot(int i) {
        return i >= 36 && i <= 39;
    }

    private static int toCraftingSlot(int i) {
        if (isHotbarSlot(i)) {
            return 37 + i;
        } else {
            return isMainInventorySlot(i) ? i + 1 : -1;
        }
    }

    private static int toBeaconSlot(int i) {
        if (isHotbarSlot(i)) {
            return 28 + i;
        } else if (isMainInventorySlot(i)) {
            int j = i + -10 + 1;
            return 1 + j;
        } else {
            return -1;
        }
    }
}
