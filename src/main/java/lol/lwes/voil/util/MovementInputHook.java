package lol.lwes.voil.util;

import lol.lwes.voil.VoilClient;
import lol.lwes.voil.event.InputEvent;
import lol.lwes.voil.mixin.accessor.InputAccessor;
import net.minecraft.client.input.Input;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.Vec2f;
import org.apache.commons.lang3.tuple.Pair;

public class MovementInputHook extends Input {
    public static Pair<Vec2f, PlayerInput> getMovementInput(Input input) {
        InputEvent inputevent = new InputEvent(input);
        VoilClient.eventBus.post(inputevent);
        Input input1 = inputevent.input.toInput();
        return Pair.of(((InputAccessor)input1).getMovementVector(), input1.playerInput);
    }
}
