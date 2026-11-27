package lol.lwes.voil.module.combat;

import java.util.function.Function;
import lol.lwes.voil.event.AttackKeyEvent;
import lol.lwes.voil.util.handler.AttackHotbarEventHandler;

public class MaceSwapState extends AttackHotbarEventHandler {
    public MaceSwapState(Function<AttackKeyEvent, MaceSwapState> function) {
        super(function, null);
    }
}
