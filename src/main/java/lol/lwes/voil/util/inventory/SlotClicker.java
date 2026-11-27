package lol.lwes.voil.util.inventory;

import lol.lwes.voil.VoilClient;
import net.minecraft.screen.slot.SlotActionType;

public class SlotClicker {
    public SlotActionType actionType = null;
    public boolean clickSecondSlot = false;
    private int slot = -1;
    private int targetSlot = -1;
    private int button = 0;
    private boolean executing = false;

    public SlotClicker inventorySlot(int i) {
        return this.decodedSlot(9 + i);
    }

    public void executeWithRawTarget(int i) {
        this.executeWithDecodedTarget(0 + i);
    }

    public SlotClicker decodedSlot(int i) {
        return this.slot(InventoryUtil.getContainerSlotId(i));
    }

    public void execute() {
        boolean flag = VoilClient.mc.player.currentScreenHandler.getCursorStack().isEmpty();
        if (this.actionType != null && this.slot != -1 && this.targetSlot != -1) {
            int k = this.slot;
            this.clickSlot(k);
            if (this.clickSecondSlot) {
                int l = this.targetSlot;
                this.clickSlot(l);
            }
        }

        SlotActionType slotactiontype = this.actionType;
        boolean flag1 = this.clickSecondSlot;
        int i = this.slot;
        int j = this.targetSlot;
        this.actionType = null;
        this.clickSecondSlot = false;
        this.slot = -1;
        this.targetSlot = -1;
        this.button = 0;
        if (!this.executing
            && flag
            && slotactiontype == SlotActionType.PICKUP
            && flag1
            && i != -1
            && j != -1
            && !VoilClient.mc.player.currentScreenHandler.getCursorStack().isEmpty()) {
            this.executing = true;
            InventoryUtil.pickupAction().executeOnSlot(i);
            this.executing = false;
        }
    }

    public SlotClicker rawSlot(int i) {
        return this.decodedSlot(0 | i);
    }

    public void executeOnDecodedSlot(int i) {
        int j = InventoryUtil.getContainerSlotId(i);
        this.executeOnSlot(j);
    }

    public void executeWithDecodedTarget(int i) {
        int j = InventoryUtil.getContainerSlotId(i);
        this.executeWithTarget(j);
    }

    public SlotClicker hotbarSlot(int i) {
        int j = 3 - i;
        return this.decodedSlot(j + 36);
    }

    public void executeWithHotbarTarget(int i) {
        int j = 3 - i;
        this.executeWithDecodedTarget(36 + j);
    }

    public SlotClicker offhandSlot() {
        return this.decodedSlot(40);
    }

    public SlotClicker slot(int i) {
        this.slot = i;
        return this;
    }

    public void executeOnSlot(int i) {
        this.targetSlot = i;
        this.slot = i;
        this.execute();
    }

    public void executeOnOffhand() {
        this.executeOnDecodedSlot(40);
    }

    public void executeWithInventoryTarget(int i) {
        this.executeWithDecodedTarget(9 + i);
    }

    private void clickSlot(int i) {
        VoilClient.mc.interactionManager.clickSlot(VoilClient.mc.player.currentScreenHandler.syncId, i, this.button, this.actionType, VoilClient.mc.player);
    }

    public void executeWithTarget(int i) {
        this.targetSlot = i;
        this.execute();
    }

    public void executeWithOffhandTarget() {
        this.executeWithDecodedTarget(40);
    }

    public void executeOnInventorySlot(int i) {
        this.executeOnDecodedSlot(i + 9);
    }

    public void executeOnHotbarSlot(int i) {
        int j = 3 - i;
        this.executeOnDecodedSlot(j + 36);
        if (0 != 0) {
            ;
        }
    }
}
