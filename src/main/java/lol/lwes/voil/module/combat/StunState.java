package lol.lwes.voil.module.combat;

import java.util.function.Function;
import lol.lwes.voil.event.AttackKeyEvent;
import lol.lwes.voil.event.HotbarKeyEvent;
import lol.lwes.voil.util.handler.AttackHotbarEventHandler;
import lol.lwes.voil.util.handler.UseEventHandler;

public class StunState extends AttackHotbarEventHandler implements UseEventHandler {
    public StunState(Function<AttackKeyEvent, StunState> function, Function<HotbarKeyEvent, StunState> function1) {
        super(function, function1);
    }

    @Override
    public boolean hasUseHandler() {
        return false;
    }
}
