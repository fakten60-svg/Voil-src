package lol.lwes.voil.mixin.accessor;

import net.minecraft.client.input.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(KeyboardInput.class)
public interface KeyboardInputAccessor {
    @Invoker("getMovementMultiplier")
    static float getMovementMultiplier(boolean flag, boolean flag1) {
        throw new AssertionError();
    }
}
