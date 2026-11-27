package lol.lwes.voil.util.input;

import java.util.HashMap;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.event.BlockBreakingEvent;
import lol.lwes.voil.event.DoAttackEvent;
import lol.lwes.voil.event.EventHandler;
import lol.lwes.voil.event.ItemUseEvent;
import lol.lwes.voil.mixin.accessor.MouseAccessor;
import net.minecraft.client.input.MouseInput;

public class MouseSimulator {
    private final HashMap heldButtons = new HashMap();
    private boolean leftClickActive = false;
    private boolean rightClickActive = false;

    public void releaseButton(int i) {
        if (this.isPressed(i)) {
            MouseInput mouseinput = new MouseInput(i, 0);
            this.getMouseAccessor().callOnMouseButton(VoilClient.mc.getWindow().getHandle(), mouseinput, 0);
            this.heldButtons.remove(i);
        }
    }

    public MouseAccessor getMouseAccessor() {
        return (MouseAccessor)VoilClient.mc.mouse;
    }

    public void pressButton(int i, int j) {
        if (!this.isPressed(i)) {
            if (!this.rightClickActive) {
                this.rightClickActive = i == 1;
            }

            if (!this.leftClickActive) {
                this.leftClickActive = i == 0;
            }

            this.heldButtons.put(i, j);
            MouseInput mouseinput = new MouseInput(i, 0);
            this.getMouseAccessor().callOnMouseButton(VoilClient.mc.getWindow().getHandle(), mouseinput, 1);
        }
    }

    private void tickButton(int i) {
        if (this.isPressed(i)) {
            int j = (Integer)this.heldButtons.get(i);
            if (j > 0) {
                this.heldButtons.replace(i, j - 1);
            } else {
                this.releaseButton(i);
            }
        }
    }

    public void clickButton(int i) {
        this.pressButton(i, 1);
    }

    public boolean isPressed(int i) {
        return this.heldButtons.containsKey(i);
    }

    public void tick() {
        this.tickButton(0);
        this.tickButton(1);
    }

    @EventHandler(priority = 200)
    private void onItemUse(ItemUseEvent itemuseevent) {
        if (this.rightClickActive) {
            itemuseevent.cancel();
            this.rightClickActive = VoilClient.mc.options.useKey.isPressed() && !KeyUtil.isKeyDown(1);
        }
    }

    @EventHandler(priority = 200)
    private void onAttack(DoAttackEvent doattackevent) {
        if (this.leftClickActive) {
            doattackevent.cancel();
            this.leftClickActive = VoilClient.mc.options.attackKey.isPressed() && !KeyUtil.isKeyDown(0);
        }
    }

    @EventHandler(priority = 200)
    private void onBlockBreak(BlockBreakingEvent blockbreakingevent) {
        if (this.leftClickActive) {
            blockbreakingevent.cancel();
            this.leftClickActive = VoilClient.mc.options.attackKey.isPressed() && !KeyUtil.isKeyDown(0);
        }
    }
}
