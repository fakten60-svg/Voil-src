package lol.lwes.voil.mixin.accessor;

import net.minecraft.client.Mouse;
import net.minecraft.client.input.MouseInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Mouse.class)
public interface MouseAccessor {
    @Invoker("onMouseButton")
    void callOnMouseButton(long i, MouseInput mouseinput, int j);

    @Invoker("onCursorPos")
    void invokeOnCursorPos(long i, double d0, double d1);

    @Accessor("cursorDeltaY")
    double getCursorDeltaY();

    @Accessor("cursorDeltaX")
    double getCursorDeltaX();

    @Accessor("cursorDeltaX")
    void setDeltaX(double d0);

    @Accessor("cursorDeltaY")
    void setDeltaY(double d0);
}
