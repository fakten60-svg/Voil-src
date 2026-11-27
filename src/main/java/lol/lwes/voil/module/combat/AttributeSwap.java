package lol.lwes.voil.module.combat;

import lol.lwes.voil.event.AttackEntityPreEvent;
import lol.lwes.voil.event.EventHandler;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.ItemListSetting;
import lol.lwes.voil.setting.ModeSetting;
import lol.lwes.voil.setting.MultiSelectSetting;
import lol.lwes.voil.setting.NumberSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.CombatUtil;
import lol.lwes.voil.util.inventory.InventoryUtil;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;

public class AttributeSwap extends Module {
    private final ModeSetting type;
    private final MultiSelectSetting whenToActivate;
    private final NumberSetting switchDelay;
    private final NumberSetting slot1;
    private final NumberSetting slot2;
    private final BooleanSetting useDistanceCheck;
    private final BooleanSetting onlyWorkIfUsingShield;
    private final NumberSetting minDistance;
    private final BooleanSetting breachOnly;
    private final ItemListSetting weaponItems;
    private int tickCounter;
    private boolean swapping;
    private int swapSlot;
    private int originalSlot;

    public AttributeSwap() {
        super("AttributeSwap", "Combines mace and sword damage on hit", Category.Combat);
        this.type = new ModeSetting("Type", "Mace", "What type of swap to do", new String[]{"Custom", "Mace"});
        String[] astring = new String[]{"On Attack", "On interact block"};
        this.whenToActivate = new MultiSelectSetting("When to Activate", "When to use the mace", astring);
        this.switchDelay = new NumberSetting("Switch Delay", 0.0, 8.0, 1.0, 1.0, "The delay between switching to the mace and back to the sword");
        this.slot1 = new NumberSetting("Slot 1", 1.0, 9.0, 1.0, 1.0, "The hotbar slot to switch from");
        this.slot2 = new NumberSetting("Slot 2", 1.0, 9.0, 1.0, 1.0, "The hotbar slot to switch to");
        this.useDistanceCheck = new BooleanSetting("Use Distance Check", false, "Only use mace if falling less than the min distance");
        this.onlyWorkIfUsingShield = new BooleanSetting("Only Work If Using Shield", false, "Only works if your target is using a shield");
        this.minDistance = new NumberSetting("Min Distance", 0.0, 3.0, 1.0, 1.0, "Minimum distance to use mace");
        this.breachOnly = new BooleanSetting("Breach Only", false, "Only uses mace if has breach enchant");
        Item[] aitem1 = new Item[]{Items.NETHERITE_SWORD, Items.NETHERITE_AXE};
        ItemListSetting itemlistsetting = new ItemListSetting("Prefered Weapons", "Select the weapons you want to use with mace stun", aitem1);
        Item[] aitem = new Item[]{Items.NETHERITE_SWORD, Items.NETHERITE_AXE};
        this.weaponItems = itemlistsetting.setRecommendedItems(aitem).allowCategoryMatch();
        this.tickCounter = 0;
        this.swapping = false;
        this.swapSlot = -1;
        this.originalSlot = -1;
        Setting[] asetting = new Setting[]{
            this.type,
            this.switchDelay,
            this.slot1,
            this.slot2,
            this.useDistanceCheck,
            this.minDistance,
            this.breachOnly,
            this.onlyWorkIfUsingShield,
            this.weaponItems,
            this.whenToActivate
        };
        this.addSettings(asetting);
        ModeSetting modesetting = this.type;
        Setting.dependSetting(this.breachOnly, "Mace", modesetting);
        Setting.dependSetting(this.minDistance, this.useDistanceCheck);
        ModeSetting modesetting1 = this.type;
        Setting.dependSetting(this.weaponItems, "Mace", modesetting1);
        ModeSetting modesetting2 = this.type;
        Setting.dependSetting(this.slot1, "Custom", modesetting2);
        ModeSetting modesetting3 = this.type;
        Setting.dependSetting(this.slot2, "Custom", modesetting3);
    }

    @Override
    public void onTick() {
        if (mc.player != null && mc.world != null) {
            if (this.swapping) {
                int i1 = this.tickCounter;
                int j = i1 + 1;
                AttributeSwap attributeswap1 = this;
                attributeswap1.tickCounter = j;
                if (j >= this.switchDelay.getInt()) {
                    int l = Math.max(0, Math.min(8, this.originalSlot));
                    if (mc.player.getInventory().getSelectedSlot() != l) {
                        InventoryUtil.selectSlot(l);
                    }

                    this.swapping = false;
                    this.tickCounter = 0;
                }
            } else if (mc.crosshairTarget instanceof BlockHitResult blockhitresult) {
                if (!mc.world.getBlockState(blockhitresult.getBlockPos()).isAir()) {
                    if (this.whenToActivate.isSelected("On interact block")) {
                        if (this.type.is("Custom")) {
                            if (mc.options.attackKey.isPressed()) {
                                this.originalSlot = Math.max(0, Math.min(8, this.slot1.getInt() - 1));
                                int i = this.slot2.getInt();
                                this.swapSlot = Math.max(0, Math.min(8, i - 1));
                                if (this.swapSlot != this.originalSlot) {
                                    ItemStack itemstack = mc.player.getInventory().getStack(this.swapSlot);
                                    if (!itemstack.isEmpty()) {
                                        if (mc.player.getInventory().getSelectedSlot() != this.swapSlot) {
                                            int k = this.swapSlot;
                                            InventoryUtil.selectSlot(k);
                                        }

                                        this.swapping = true;
                                        this.tickCounter = 0;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private int findEnchantedSlot() {
        if (mc.player == null) {
            return -1;
        }

        Registry registry = mc.player.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);

        for (int i = 0; i < 9; i++) {
            ItemStack itemstack = mc.player.getInventory().getStack(i);
            if (itemstack.isOf(Items.MACE)) {
                if (!this.breachOnly.isEnabled()) {
                    return i;
                }

                RegistryKey registrykey = Enchantments.BREACH;
                ItemStack itemstack1 = itemstack;
                if (InventoryUtil.getEnchantmentLevel(itemstack1, registrykey, registry) > 0) {
                    return i;
                }
            }
        }

        return -1;
    }

    @EventHandler
    public void onAttack(AttackEntityPreEvent attackentitypreevent) {
        if (mc.player != null) {
            if (this.whenToActivate.isSelected("On Attack") || !this.type.is("Custom")) {
                if (!(mc.player.fallDistance > this.minDistance.getFloat()) || !this.useDistanceCheck.isEnabled()) {
                    if (mc.crosshairTarget instanceof EntityHitResult entityhitresult) {
                        if (!(entityhitresult.getEntity() instanceof LivingEntity)) {
                            return;
                        }

                        ItemStack itemstack2 = mc.player.getMainHandStack();
                        if (this.onlyWorkIfUsingShield.isEnabled()
                            && !CombatUtil.isShieldBlockingFrom(mc.player, (LivingEntity)entityhitresult.getEntity(), false)) {
                            return;
                        }

                        ItemListSetting itemlistsetting = this.weaponItems;
                        Item item = itemstack2.getItem();
                        if (!itemlistsetting.matches(item)) {
                            return;
                        }

                        if (this.type.is("Custom")) {
                            int i = this.slot1.getInt();
                            this.originalSlot = i - 1;
                            this.swapSlot = this.slot2.getInt() - 1;
                            if (this.swapSlot == -1 || this.swapSlot == this.originalSlot) {
                                return;
                            }

                            int j = this.swapSlot;
                            InventoryUtil.selectSlot(j);
                            this.swapping = true;
                            this.tickCounter = 0;
                        } else {
                            if (this.swapSlot == -1) {
                                this.swapSlot = this.findEnchantedSlot();
                            }

                            this.originalSlot = mc.player.getInventory().getSelectedSlot();
                            if (this.swapSlot == -1 || this.swapSlot == this.originalSlot) {
                                return;
                            }

                            ItemStack itemstack = mc.player.getInventory().getStack(this.swapSlot);
                            if (itemstack == null) {
                                return;
                            }

                            if (!itemstack.isOf(Items.MACE)) {
                                this.swapSlot = this.findEnchantedSlot();
                            }

                            boolean flag = true;
                            if (this.breachOnly.isEnabled()) {
                                Registry registry = mc.player.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
                                RegistryKey registrykey = Enchantments.BREACH;
                                ItemStack itemstack1 = itemstack;
                                flag = InventoryUtil.getEnchantmentLevel(itemstack1, registrykey, registry) > 0;
                            }

                            if (!flag) {
                                return;
                            }

                            int k = this.swapSlot;
                            InventoryUtil.selectSlot(k);
                            this.swapping = true;
                            this.tickCounter = 0;
                        }
                    }
                }
            }
        }
    }
}
