package lol.lwes.voil.module.crystalpvp;

import java.util.function.Function;
import lol.lwes.voil.event.HotbarKeyEvent;
import lol.lwes.voil.event.UseKeyEvent;
import lol.lwes.voil.util.handler.UseHotbarEventHandler;

public class AutoHitCrystalState extends UseHotbarEventHandler {
    public AutoHitCrystalState(Function<UseKeyEvent, AutoHitCrystalState> function, Function<HotbarKeyEvent, AutoHitCrystalState> function1) {
        super(function, function1);
    }
}
