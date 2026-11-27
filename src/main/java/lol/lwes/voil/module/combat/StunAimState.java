package lol.lwes.voil.module.combat;

import java.util.function.Function;
import lol.lwes.voil.event.UseKeyEvent;
import lol.lwes.voil.util.RaycastUtil;
import lol.lwes.voil.util.handler.UseEventHandler;
import net.minecraft.world.RaycastContext.FluidHandling;

public class StunAimState extends StunState {
    final AutoStun autoStun;

    public StunAimState(AutoStun autostun, Function function, Function function1) {
        super(function, function1);
        this.autoStun = autostun;
    }

    @Override
    public boolean hasUseHandler() {
        return true;
    }

    public StunState handleUse(UseKeyEvent usekeyevent) {
        if (!this.autoStun.rotationHandle.isOnTarget()) {
            return null;
        }

        AutoStun.getMc22().crosshairTarget = RaycastUtil.raycastBlock(AutoStun.getMc2().player.getBlockInteractionRange(), FluidHandling.NONE);
        usekeyevent.addTimesPressed(1);
        return this.autoStun.cooldownState;
    }

    public UseEventHandler getUseHandler2(UseKeyEvent usekeyevent) {
        return this.handleUse(usekeyevent);
    }

    @Override
    public UseEventHandler getUseHandler(UseKeyEvent usekeyevent) {
        return this.getUseHandler2(usekeyevent);
    }
}
