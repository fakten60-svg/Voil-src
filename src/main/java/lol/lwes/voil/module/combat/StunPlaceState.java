package lol.lwes.voil.module.combat;

import java.util.function.Function;
import lol.lwes.voil.event.UseKeyEvent;
import lol.lwes.voil.util.handler.UseEventHandler;
import lol.lwes.voil.util.inventory.InventoryUtil;
import lol.lwes.voil.util.world.BlockUtil;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

public class StunPlaceState extends StunState {
    final AutoStun autoStun;
    private static final int MAX_ATTEMPTS = 10;

    public StunPlaceState(AutoStun autostun, Function function, Function function1) {
        super(function, function1);
        this.autoStun = autostun;
    }

    @Override
    public boolean hasUseHandler() {
        return true;
    }

    public StunState getNextState() {
        if (this.autoStun.webPos != null && AutoStun.getMc222().world.getBlockState(this.autoStun.webPos).isReplaceable()) {
            BlockPos blockpos = this.autoStun.webPos;
            if (!this.autoStun.isAimedAtPos(blockpos)) {
                int i = this.autoStun.stunTicks;
                int j = i + 1;
                AutoStun autostun = this.autoStun;
                autostun.stunTicks = j;
                if (j > MAX_ATTEMPTS) {
                    this.autoStun.webPos = null;
                    this.autoStun.releaseRotation();
                    return this.autoStun.cooldownState;
                } else {
                    return null;
                }
            } else {
                this.autoStun.stunTicks = 0;
                BlockUtil.placeBlock(this.autoStun.webPos, Hand.MAIN_HAND, InventoryUtil.getSelectedSlot(), true, false);
                this.autoStun.webPos = null;
                return this.autoStun.finishState;
            }
        } else {
            this.autoStun.webPos = null;
            return this.autoStun.cooldownState;
        }
    }

    public UseEventHandler j() {
        return this.getNextState();
    }

    @Override
    public UseEventHandler getUseHandler(UseKeyEvent usekeyevent) {
        return this.j();
    }
}
