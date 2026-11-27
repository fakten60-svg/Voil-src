package lol.lwes.voil.mixin;

import lol.lwes.voil.util.MovementInputHook;
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.Vec2f;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin extends Input {
    @Inject(method = "tick", at = @At("RETURN"))
    public void input(CallbackInfo callbackinfo) {
        Pair<Vec2f, PlayerInput> pair = MovementInputHook.getMovementInput(this);
        this.movementVector = pair.getKey();
        this.playerInput = pair.getValue();
    }
}
