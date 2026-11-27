package lol.lwes.voil.module.crystalpvp;

import java.util.function.Function;
import lol.lwes.voil.event.HotbarKeyEvent;
import lol.lwes.voil.event.UseKeyEvent;
import lol.lwes.voil.util.handler.UseHotbarEventHandler;

public class AutoAnchorState extends UseHotbarEventHandler {
    public AutoAnchorState(Function<UseKeyEvent, AutoAnchorState> function, Function<HotbarKeyEvent, AutoAnchorState> function1) {
        super(function, function1);
    }
}
