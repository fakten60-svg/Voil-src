package lol.lwes.voil.util.inventory;

import java.util.function.Predicate;
import lol.lwes.voil.VoilClient;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public final class SwapBuilder {
    private int slot = -1;
    private Predicate itemFilter;
    private SwapMode mode = SwapMode.SWAP_THEN_ACTION;
    private Entity attackTarget;
    private Runnable onAction;
    private int swapBackDelay = 1;
    private Runnable onSwapBack;
    private int returnSlot = -1;

    SwapBuilder() {
    }

    public SwapBuilder returnSlot(int i) {
        this.returnSlot = i;
        return this;
    }

    public SwapBuilder noSwapBack() {
        this.swapBackDelay = -1;
        return this;
    }

    public SwapBuilder filter(Predicate<ItemStack> predicate) {
        this.itemFilter = predicate;
        return this;
    }

    public SwapBuilder toItem(Item item) {
        return this.filter(itemstack -> itemstack.isOf(item));
    }

    private void performAction() {
        if (this.attackTarget != null) {
            VoilClient.mc.interactionManager.attackEntity(VoilClient.mc.player, this.attackTarget);
            VoilClient.mc.player.swingHand(Hand.MAIN_HAND);
        }

        if (this.onAction != null) {
            this.onAction.run();
        }
    }

    public SwapBuilder swapBackAfter(int i) {
        this.swapBackDelay = i;
        return this;
    }

    public SwapBuilder attack(Entity entity) {
        this.attackTarget = entity;
        return this;
    }

    private void swapBack(int i) {
        if (this.swapBackDelay == 0) {
            InventoryUtil.selectSlot(i);
            if (this.onSwapBack != null) {
                this.onSwapBack.run();
            }
        } else if (this.swapBackDelay > 0) {
            SwapScheduler.PENDING_SWAPS.add(new PendingSwap(i, this.swapBackDelay, this.onSwapBack));
        } else if (this.onSwapBack != null) {
            this.onSwapBack.run();
        }
    }

    public SwapBuilder mode(SwapMode swapmode) {
        this.mode = swapmode;
        return this;
    }

    public SwapBuilder slot(int i) {
        this.slot = i;
        return this;
    }

    private int findSlot() {
        if (this.slot >= 0) {
            return this.slot;
        }

        if (this.itemFilter != null) {
            for (int i = 0; i < 9; i++) {
                if (this.itemFilter.test(VoilClient.mc.player.getInventory().getStack(i))) {
                    return i;
                }
            }
        }

        return -1;
    }

    public boolean execute() {
        if (VoilClient.mc.player != null && VoilClient.mc.interactionManager != null) {
            int i = this.findSlot();
            if (i >= 0 && i <= 8) {
                int j = VoilClient.mc.player.getInventory().getSelectedSlot();
                int k = this.returnSlot >= 0 ? this.returnSlot : j;
                if (i == j) {
                    this.performAction();
                    if (i != k) {
                        this.swapBack(k);
                    } else if (this.onSwapBack != null) {
                        this.onSwapBack.run();
                    }

                    return true;
                } else {
                    int l = this.mode.ordinal();
                    if (l != 0) {
                        if (l == 1) {
                            this.performAction();
                            InventoryUtil.selectSlot(i);
                        }
                    } else {
                        InventoryUtil.selectSlot(i);
                        this.performAction();
                    }

                    this.swapBack(k);
                    return true;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public SwapBuilder onAction(Runnable runnable) {
        this.onAction = runnable;
        return this;
    }

    public SwapBuilder onSwapBack(Runnable runnable) {
        this.onSwapBack = runnable;
        return this;
    }
}
