package lol.lwes.voil.module.crystalpvp;

import java.util.function.Function;
import lol.lwes.voil.event.AttackKeyEvent;
import lol.lwes.voil.event.HotbarKeyEvent;
import lol.lwes.voil.util.handler.AttackHotbarEventHandler;

public class TotemHitState extends AttackHotbarEventHandler {
    public TotemHitState(Function<AttackKeyEvent, TotemHitState> function, Function<HotbarKeyEvent, TotemHitState> function1) {
        super(function, function1);
    }
}
