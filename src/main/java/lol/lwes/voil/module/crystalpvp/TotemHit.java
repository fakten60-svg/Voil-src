package lol.lwes.voil.module.crystalpvp;

import java.util.List;
import java.util.function.Function;
import lol.lwes.voil.event.AttackEntityPreEvent;
import lol.lwes.voil.event.AttackKeyEvent;
import lol.lwes.voil.event.EventHandler;
import lol.lwes.voil.event.HotbarKeyEvent;
import lol.lwes.voil.event.InputEvent;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.module.crystalpvp.TotemHitState;
import lol.lwes.voil.setting.ItemListSetting;
import lol.lwes.voil.setting.ModeSetting;
import lol.lwes.voil.setting.NumberSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.inventory.InventoryUtil;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

public class TotemHit extends Module {
    private final ModeSetting hitMode;
    private final NumberSetting switchDelay;
    private final ItemListSetting items;
    private int swordSlot;
    private int swapWaitTicks;
    private int switchDelayTicks;
    private int unusedCounter;
    private boolean attackPending;
    private TotemHitState currentState;
    private final TotemHitState restoreSlotState;
    private final TotemHitState waitState;
    private final TotemHitState attackState;
    private final TotemHitState selectSwordState;
    private TotemHitState idleState;

    public TotemHit() {
        super("TotemHit", "Still deals KB while holding a whitelisted item, modernized", Category.CrystalPvP);
        this.hitMode = new ModeSetting("Hit Mode", "Sprint", "How to deal KB", new String[]{"Sword swap", "Sprint"});
        this.switchDelay = new NumberSetting("Switch Delay", 1.0, 10.0, 3.0, 1.0, "Delay in ticks before switching back to the totem after hitting");
        Item[] aitem1 = new Item[]{Items.TOTEM_OF_UNDYING};
        ItemListSetting itemlistsetting = new ItemListSetting("Items", "Held items that activate the module", aitem1);
        Item[] aitem = new Item[]{Items.TOTEM_OF_UNDYING, Items.END_CRYSTAL, Items.OBSIDIAN, Items.RESPAWN_ANCHOR, Items.GLOWSTONE};
        this.items = itemlistsetting.setRecommendedItems(aitem);
        this.swordSlot = -1;
        this.swapWaitTicks = 0;
        this.switchDelayTicks = 0;
        this.unusedCounter = 0;
        this.attackPending = false;
        this.restoreSlotState = new TotemHitState(null, hotbarkeyevent -> {
            if (this.swordSlot == -1) {
                this.restoreSlot();
                return this.idleState;
            } else if (hotbarkeyevent.getSlot() == this.swordSlot) {
                hotbarkeyevent.addTimesPressed(1);
                this.restoreSlot();
                return this.idleState;
            } else {
                return null;
            }
        });
        Function<AttackKeyEvent, TotemHitState> function = attackkeyevent -> {
            int i = this.switchDelayTicks;
            int j = i + 1;
            TotemHit totemhit2 = this;
            totemhit2.switchDelayTicks = j;
            return j < (int)this.switchDelay.getValue() ? null : this.restoreSlotState;
        };
        this.waitState = new TotemHitState(function, null);
        Function<AttackKeyEvent, TotemHitState> function1 = attackkeyevent -> {
            if (!mc.player.getMainHandStack().isIn(ItemTags.SWORDS)) {
                int i = this.swapWaitTicks + 1;
                TotemHit totemhit2 = this;
                totemhit2.swapWaitTicks = i;
                return i > 5 ? this.restoreSlotState : null;
            } else {
                attackkeyevent.addTimesPressed(1);
                this.attackPending = false;
                this.switchDelayTicks = 0;
                return this.waitState;
            }
        };
        this.attackState = new TotemHitState(function1, null);
        this.selectSwordState = new TotemHitState(null, hotbarkeyevent -> {
            if (!InventoryUtil.getStack(hotbarkeyevent.getSlot()).isIn(ItemTags.SWORDS)) {
                return null;
            }

            this.swordSlot = InventoryUtil.getSelectedSlot();
            hotbarkeyevent.addTimesPressed(1);
            this.swapWaitTicks = 0;
            return this.attackState;
        });
        Function<AttackKeyEvent, TotemHitState> function2 = attackkeyevent -> {
            if (attackkeyevent.getTimesPressed() <= 0) {
                return null;
            }

            ItemStack itemstack = mc.player.getMainHandStack();
            if (!this.isWhitelisted(itemstack)) {
                return null;
            }

            HitResult hitresult = mc.crosshairTarget;
            if (!(hitresult instanceof EntityHitResult && ((EntityHitResult)hitresult).getEntity() instanceof PlayerEntity playerentity)) {
                return null;
            }

            if (mc.player.distanceTo(playerentity) > 3.0F) {
                return null;
            }

            this.attackPending = true;
            return this.selectSwordState;
        };
        this.idleState = new TotemHitState(function2, null);
        Setting[] asetting = new Setting[]{this.hitMode, this.switchDelay, this.items};
        this.addSettings(asetting);
        this.onDisable();
    }

    private boolean isActive() {
        List list = List.of(this.selectSwordState, this.attackState);
        boolean flag = list.contains(this.currentState);
        boolean flag1 = !this.hitMode.is("Sword swap")
            || mc.currentScreen instanceof HandledScreen
            || InventoryUtil.findHotbarSlotByTag(ItemTags.SWORDS) == -1;
        if (flag1) {
            return false;
        }

        if (!flag) {
            return true;
        }

        HitResult hitresult = mc.crosshairTarget;
        return hitresult instanceof EntityHitResult && ((EntityHitResult)hitresult).getEntity() instanceof PlayerEntity playerentity
            ? mc.player.distanceTo(playerentity) <= 3.0F
            : false;
    }

    private boolean isWhitelisted(ItemStack itemstack) {
        if (!itemstack.isEmpty()) {
            ItemListSetting itemlistsetting = this.items;
            Item item1 = itemstack.getItem();
            if (itemlistsetting.matches(item1)) {
                return !this.hitMode.is("Sword swap") || !itemstack.isIn(ItemTags.SWORDS);
            }
        }

        return false;
    }

    @Override
    public void onDisable() {
        this.restoreSlot();
        this.currentState = this.idleState;
    }

    private void restoreSlot() {
        this.swordSlot = -1;
        this.swapWaitTicks = 0;
        this.switchDelayTicks = 0;
        this.unusedCounter = 0;
        this.attackPending = false;
    }

    @EventHandler
    public void onSwitchSlot(HotbarKeyEvent hotbarkeyevent) {
        if (!Module.isNotInGame()) {
            if (!this.isActive()) {
                this.currentState = this.restoreSlotState;
            }

            this.currentState = (TotemHitState)this.currentState.applyHotbar(hotbarkeyevent);
        }
    }

    @EventHandler
    public void onAttackKey(AttackKeyEvent attackkeyevent) {
        if (!Module.isNotInGame()) {
            if (attackkeyevent.getTimesPressed() > 0) {
                this.unusedCounter = 6;
            }

            if (!this.isActive()) {
                this.currentState = this.restoreSlotState;
            }

            this.currentState = (TotemHitState)this.currentState.applyAttack(attackkeyevent);
        }
    }

    @EventHandler
    public void onInputEvent(InputEvent inputevent) {
        if (inputevent.localPlayer) {
            boolean flag = this.unusedCounter > 0;
            if (this.unusedCounter > 0) {
                this.unusedCounter--;
            }

            if (this.hitMode.is("Sprint")) {
                if (!Module.isNotInGame()) {
                    if (flag) {
                        ItemStack itemstack = mc.player.getMainHandStack();
                        if (this.isWhitelisted(itemstack)) {
                            HitResult hitresult = mc.crosshairTarget;
                            if (hitresult instanceof EntityHitResult) {
                                Entity entity = ((EntityHitResult)hitresult).getEntity();
                                if (entity instanceof PlayerEntity) {
                                    inputevent.input.playerInput.sprint = true;
                                    inputevent.input.playerInput.sneak = false;
                                    inputevent.input.playerInput.backward = false;
                                    if (!inputevent.input.playerInput.forward) {
                                        inputevent.input.playerInput.forward = true;
                                        inputevent.input.forward = 1.0F;
                                    }

                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onAttackPre(AttackEntityPreEvent attackentitypreevent) {
        if (!Module.isNotInGame()) {
            if (attackentitypreevent.getPlayer() == mc.player) {
                ItemStack itemstack = mc.player.getMainHandStack();
                if (this.isWhitelisted(itemstack)) {
                    if (this.hitMode.is("Sword swap")) {
                        if (this.attackPending) {
                            attackentitypreevent.setCancelled2(true);
                        }
                    } else if (this.hitMode.is("Sprint")) {
                        if (attackentitypreevent.getTarget() instanceof PlayerEntity playerentity) {
                            if (!(mc.player.distanceTo(playerentity) > 3.0F)) {
                                ;
                            }
                        }
                    }
                }
            }
        }
    }
}
