package lol.lwes.voil.module.combat;

import java.util.function.Function;
import lol.lwes.voil.event.AttackKeyEvent;
import lol.lwes.voil.event.HotbarKeyEvent;
import lol.lwes.voil.util.handler.AttackHotbarEventHandler;

public class TriggerbotState extends AttackHotbarEventHandler {
    public TriggerbotState(Function<AttackKeyEvent, TriggerbotState> function, Function<HotbarKeyEvent, TriggerbotState> function1) {
        super(function, function1);
    }
}
