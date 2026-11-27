package lol.lwes.voil.module.player;

import java.util.function.Predicate;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.event.EventHandler;
import lol.lwes.voil.event.InputEvent;
import lol.lwes.voil.mixin.accessor.MinecraftClientAccessor;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.KeybindSetting;
import lol.lwes.voil.setting.NumberSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.MovementUtil;
import lol.lwes.voil.util.inventory.FoundSlot;
import lol.lwes.voil.util.inventory.InventoryUtil;
import lol.lwes.voil.util.rotation.Rotation;
import lol.lwes.voil.util.rotation.RotationPriority;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry.Reference;
import net.minecraft.util.Hand;

public class LungeSwap extends Module {
    private final KeybindSetting keybind = new KeybindSetting("Keybind", -1, false, "The keybind to activate the swap");
    private final NumberSetting switchDelay = new NumberSetting("Switch Delay", 0.0, 8.0, 1.0, 1.0, "Ticks before swapping back to the filler slot");
    private final NumberSetting fillerSlot = new NumberSetting("Filler Slot", 1.0, 9.0, 1.0, 1.0, "The hotbar slot to swap back to after attacking");
    private final BooleanSetting keepDirection = new BooleanSetting("Keep Direction", true, "Rotates to the direction you are pressing before lunging");
    private final BooleanSetting autoChestplate = new BooleanSetting(
        "Auto Chestplate", false, "While gliding, swaps to a hotbar chestplate lunges, then equips the elytra and resumes gliding"
    );
    private int switchTimer = -1;
    private int pendingSlot = -1;
    private boolean lunging;
    private int savedSlot = -1;
    private int spearSlot = -1;
    private LungeSwapState state = LungeSwapState.IDLE;
    private int chestplateSlot = -1;
    private int elytraSlot = -1;
    private int chestplateTimer = 0;
    private int elytraTimer = 0;
    private int jumpReleaseTicks = 0;
    private int jumpPressTicks = 0;

    public LungeSwap() {
        super("LungeSwap", "Does attribute swap between spear and a slot", Category.Player);
        Setting[] asetting = new Setting[]{this.keybind, this.keepDirection, this.switchDelay, this.fillerSlot, this.autoChestplate};
        this.addSettings(asetting);
    }

    private void cleanup() {
        this.resetState();
        VoilClient.getRotationManager().reset();
    }

    private boolean isElytraEquipped() {
        return mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem() == Items.ELYTRA;
    }

    private int findChestplateSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack itemstack = mc.player.getInventory().getStack(i);
            if (!itemstack.isEmpty() && itemstack.getItem() != Items.ELYTRA && itemstack.contains(DataComponentTypes.EQUIPPABLE)) {
                EquippableComponent equippablecomponent = itemstack.get(DataComponentTypes.EQUIPPABLE);
                if (equippablecomponent != null && equippablecomponent.slot() == EquipmentSlot.CHEST) {
                    return i;
                }
            }
        }

        return -1;
    }

    @Override
    public void onTick() {
        if (mc.player != null && mc.world != null && mc.interactionManager != null) {
            if (this.switchTimer > 0) {
                int k = this.switchTimer--;
            }

            if (this.switchTimer == 0 && this.pendingSlot != -1) {
                mc.player.getInventory().setSelectedSlot(this.pendingSlot);
                this.pendingSlot = -1;
            }

            if (this.state != LungeSwapState.IDLE) {
                int j2 = this.chestplateTimer;
                int i1 = j2 - 1;
                LungeSwap lungeswap1 = this;
                lungeswap1.chestplateTimer = i1;
                if (i1 <= 0) {
                    this.cleanup();
                } else {
                    int l = this.state.ordinal();
                    if (l != 1) {
                        if (l != 2) {
                            if (l != 3) {
                                if (l != 4) {
                                    this.resetState();
                                } else {
                                    if (mc.player.isGliding()) {
                                        this.pendingSlot = this.elytraSlot;
                                        this.switchTimer = this.switchDelay.getInt();
                                        this.resetState();
                                        return;
                                    }

                                    if (mc.player.isOnGround() || !this.isElytraEquipped()) {
                                        this.cleanup();
                                        return;
                                    }

                                    if (this.jumpReleaseTicks > 0) {
                                        j2 = this.jumpReleaseTicks--;
                                    } else if (this.jumpPressTicks > 0) {
                                        this.jumpPressTicks--;
                                    } else {
                                        this.jumpReleaseTicks = 1;
                                        this.jumpPressTicks = 3;
                                    }
                                }
                            } else {
                                if (this.elytraTimer > 0) {
                                    j2 = this.elytraTimer--;
                                    return;
                                }

                                int k1 = InventoryUtil.findHotbarSlot(Items.ELYTRA);
                                if (k1 == -1) {
                                    this.cleanup();
                                    return;
                                }

                                InventoryUtil.selectSlot(k1);
                                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                                this.jumpReleaseTicks = 1;
                                this.jumpPressTicks = 3;
                                this.state = LungeSwapState.GLIDE;
                            }
                        } else {
                            if (mc.player.getAttackCooldownProgress(0.0F) < 1.0F) {
                                return;
                            }

                            int j1 = this.chestplateSlot;
                            InventoryUtil.selectSlot(j1);
                            ((MinecraftClientAccessor)mc).invokeDoAttack();
                            VoilClient.getRotationManager().reset();
                            this.elytraTimer = Math.max(1, this.switchDelay.getInt());
                            this.state = LungeSwapState.WEAR_ELYTRA;
                        }
                    } else if (!this.isElytraEquipped()) {
                        this.state = LungeSwapState.LUNGE;
                    }
                }
            } else if (this.lunging) {
                this.lunging = false;
                this.attackWithSlot(this.savedSlot, this.spearSlot);
                this.savedSlot = -1;
                this.spearSlot = -1;
                VoilClient.getRotationManager().reset();
            } else if (this.keybind.isPressed()) {
                if (!mc.player.isTouchingWater()) {
                    if (!(mc.player.getAttackCooldownProgress(0.0F) < 1.0F)) {
                        FoundSlot foundslot = this.findSpear();
                        if (foundslot.isFound()) {
                            int i = foundslot.getSlot();
                            int i2 = this.fillerSlot.getInt();
                            int j = Math.max(0, Math.min(8, i2 - 1));
                            if (i != j) {
                                if (mc.player.isGliding()) {
                                    if (this.autoChestplate.isEnabled()) {
                                        int l1 = this.findChestplateSlot();
                                        if (l1 != -1 && l1 != i) {
                                            this.startLunge(i, j, l1);
                                        }
                                    }
                                } else if (this.keepDirection.isEnabled() && MovementUtil.hasMovementInput()) {
                                    Rotation rotation = new Rotation(MovementUtil.getMovementYaw(), mc.player.getPitch());
                                    VoilClient.getRotationManager().requestFromCaller(rotation, RotationPriority.LOW);
                                    this.savedSlot = i;
                                    this.spearSlot = j;
                                    this.lunging = true;
                                } else {
                                    this.attackWithSlot(i, j);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onDisable() {
        this.resetState();
        VoilClient.getRotationManager().reset();
    }

    @Override
    public void onEnable() {
        this.switchTimer = -1;
        this.pendingSlot = -1;
        this.lunging = false;
        this.savedSlot = -1;
        this.spearSlot = -1;
        this.resetState();
        VoilClient.getRotationManager().reset();
    }

    private void startLunge(int i, int j, int k) {
        this.chestplateSlot = i;
        this.elytraSlot = j;
        this.chestplateTimer = 60;
        this.elytraTimer = 0;
        this.jumpReleaseTicks = 0;
        this.jumpPressTicks = 0;
        if (this.keepDirection.isEnabled() && MovementUtil.hasMovementInput()) {
            Rotation rotation = new Rotation(MovementUtil.getMovementYaw(), mc.player.getPitch());
            VoilClient.getRotationManager().requestFromCaller(rotation, RotationPriority.LOW);
        }

        InventoryUtil.selectSlot(k);
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        this.state = LungeSwapState.WEAR_CHEST;
    }

    private void attackWithSlot(int i, int j) {
        this.pendingSlot = j;
        mc.player.getInventory().setSelectedSlot(i);
        ((MinecraftClientAccessor)mc).invokeDoAttack();
        this.switchTimer = this.switchDelay.getInt();
    }

    private FoundSlot findSpear() {
        if (mc.player == null) {
            return new FoundSlot(-1, 0);
        }

        Reference reference = mc.player.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(Enchantments.LUNGE);
        Predicate<ItemStack> predicate = itemstack -> !itemstack.isEmpty() && EnchantmentHelper.getLevel(reference, itemstack) > 0;
        return InventoryUtil.findHotbarStack(predicate);
    }

    @EventHandler
    private void onInput(InputEvent inputevent) {
        if (this.state == LungeSwapState.GLIDE) {
            if (this.jumpReleaseTicks > 0) {
                inputevent.input.playerInput.jump = false;
            } else if (this.jumpPressTicks > 0) {
                inputevent.input.playerInput.jump = true;
            }
        }
    }

    private void resetState() {
        this.state = LungeSwapState.IDLE;
        this.chestplateSlot = -1;
        this.elytraSlot = -1;
        this.chestplateTimer = 0;
        this.elytraTimer = 0;
        this.jumpReleaseTicks = 0;
        this.jumpPressTicks = 0;
    }
}
