package lol.lwes.voil.module.player;

import lol.lwes.voil.event.EventHandler;
import lol.lwes.voil.event.InputEvent;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.module.crystalpvp.AutoCrystal;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.KeybindSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.inventory.InventoryUtil;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public class ElytraSwap extends Module {
    KeybindSetting elytraSwap = new KeybindSetting("ElytraSwap", 0, false, "Bind to swap to elytra");
    BooleanSetting rocketSwap = new BooleanSetting("RocketSwap", true, "Swaps to rocket when elytra is on");
    BooleanSetting autoFly = new BooleanSetting("AutoFly", true, "Automatically uses the elytra after swapping");
    private long lastSwapTime = 0L;
    private long lastActionTime = 0L;
    private boolean autoFlying = false;
    private boolean rocketPending = false;
    private int savedSlot = 0;
    private int jumpTimer = 0;
    private int gliderDelay = 0;
    private int rocketTimer = 0;
    private int postFlyTimer = 0;
    private ElytraSwapState state = ElytraSwapState.IDLE;

    public ElytraSwap() {
        super("ElytraSwap", "Swaps to your elytra and flies", Category.Player);
        Setting[] asetting = new Setting[]{this.elytraSwap, this.rocketSwap, this.autoFly};
        this.addSettings(asetting);
    }

    private boolean isElytraEquipped() {
        return mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem() == Items.ELYTRA;
    }

    @Override
    public void onTick() {
        if (mc.player != null && mc.interactionManager != null) {
            if (this.elytraSwap.isPressed() && System.currentTimeMillis() - this.lastSwapTime > 300L) {
                if (this.isElytraEquipped()) {
                    int i = this.findChestplateSlot();
                    if (i != -1) {
                        InventoryUtil.selectSlot(i);
                        this.autoFlying = false;
                        this.rocketPending = false;
                        this.resetTimers();
                        this.state = ElytraSwapState.IDLE;
                    }
                } else {
                    InventoryUtil.hasItemInHotbar(Items.ELYTRA);
                    this.autoFlying = true;
                    this.rocketPending = false;
                    if (this.autoFly.isEnabled()) {
                        this.state = ElytraSwapState.SWAPPING;
                        this.jumpTimer = 2;
                        this.gliderDelay = 2;
                        this.rocketTimer = 2;
                    }
                }

                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                this.lastSwapTime = System.currentTimeMillis();
            }

            if (mc.player.isOnGround()) {
                this.savedSlot = 0;
            } else if (this.savedSlot < Integer.MAX_VALUE) {
                this.savedSlot++;
            }

            if (this.jumpTimer > 0) {
                int j = this.jumpTimer--;
            } else if (this.gliderDelay > 0) {
                this.gliderDelay--;
            } else if (this.rocketTimer > 0) {
                int l = this.rocketTimer--;
            }

            if (this.postFlyTimer > 0) {
                int i1 = this.postFlyTimer--;
            }

            int k = this.state.ordinal();
            if (k != 1) {
                if (k != 2) {
                    if (k != 3) {
                        if (k != 4) {
                            if (mc.player.isGliding()
                                && this.rocketSwap.isEnabled()
                                && !this.rocketPending
                                && this.isElytraEquipped()
                                && System.currentTimeMillis() - this.lastActionTime > 600L) {
                                InventoryUtil.hasItemInHotbar(Items.FIREWORK_ROCKET);
                                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                                this.rocketPending = true;
                                this.lastActionTime = System.currentTimeMillis();
                            }

                            if (!mc.player.isGliding() && mc.player.isOnGround()) {
                                this.rocketPending = false;
                            }
                        } else {
                            if (mc.player.isGliding()
                                && this.rocketSwap.isEnabled()
                                && !this.rocketPending
                                && System.currentTimeMillis() - this.lastActionTime > 600L) {
                                InventoryUtil.hasItemInHotbar(Items.FIREWORK_ROCKET);
                                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                                this.rocketPending = true;
                                this.lastActionTime = System.currentTimeMillis();
                            }

                            if (mc.player.isOnGround() || !this.isElytraEquipped()) {
                                this.state = ElytraSwapState.IDLE;
                                this.resetTimers();
                            }
                        }
                    } else if (mc.player.isGliding()) {
                        this.state = ElytraSwapState.BOOSTING;
                    } else if (this.savedSlot > 12 && this.postFlyTimer == 0) {
                        this.postFlyTimer = 2;
                    }
                } else if (this.savedSlot >= 2) {
                    this.state = ElytraSwapState.TRIGGER_GLIDE;
                    this.postFlyTimer = 3;
                }
            } else if (!mc.player.isOnGround()) {
                this.state = ElytraSwapState.WAIT_AIRBORNE;
            }
        }
    }

    private void resetTimers() {
        this.jumpTimer = 0;
        this.gliderDelay = 0;
        this.rocketTimer = 0;
        this.postFlyTimer = 0;
    }

    @EventHandler
    private void onInput(InputEvent inputevent) {
        if (AutoCrystal.isInGame()) {
            if (this.autoFly.isEnabled() && this.autoFlying) {
                boolean flag = this.jumpTimer > 0 || this.gliderDelay == 0 && this.rocketTimer > 0 || this.postFlyTimer > 0;
                int i = this.state.ordinal();
                if (i == 1 || i == 2 || i == 3 || i == 4) {
                    inputevent.input.forward = 1.0F;
                    inputevent.input.playerInput.forward = true;
                    inputevent.input.playerInput.jump = flag;
                    inputevent.input.playerInput.sprint = true;
                }
            }
        }
    }

    private int findChestplateSlot() {
        PlayerInventory playerinventory = mc.player.getInventory();

        for (int i = 0; i < playerinventory.size(); i++) {
            ItemStack itemstack = playerinventory.getStack(i);
            if (itemstack.contains(DataComponentTypes.EQUIPPABLE)) {
                EquippableComponent equippablecomponent = itemstack.getItem().getDefaultStack().get(DataComponentTypes.EQUIPPABLE);
                if (equippablecomponent != null && equippablecomponent.slot() == EquipmentSlot.CHEST && itemstack.getItem() != Items.ELYTRA) {
                    return i;
                }
            }
        }

        return -1;
    }
}
