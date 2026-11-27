package lol.lwes.voil.event;

import lol.lwes.voil.mixin.accessor.InputAccessor;
import net.minecraft.client.input.Input;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.Vec2f;

public class MovementInput {
    public float forward;
    public float sideways;
    public PlayerInputState playerInput = PlayerInputState.fromPlayerInput(PlayerInput.DEFAULT);

    public Input toInput() {
        Input input = new Input();
        ((InputAccessor)input).setMovementVector(new Vec2f(this.sideways, this.forward).normalize());
        input.playerInput = this.playerInput.toPlayerInput();
        return input;
    }

    public static MovementInput fromInput(Input input) {
        MovementInput movementinput = new MovementInput();
        Vec2f vec2f = input.getMovementInput();
        movementinput.sideways = vec2f.x;
        movementinput.forward = vec2f.y;
        movementinput.playerInput = PlayerInputState.fromPlayerInput(input.playerInput);
        return movementinput;
    }
}
