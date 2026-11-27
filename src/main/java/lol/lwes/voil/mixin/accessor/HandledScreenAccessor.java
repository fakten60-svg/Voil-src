package lol.lwes.voil.mixin.accessor;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(HandledScreen.class)
public interface HandledScreenAccessor {
    @Accessor("y")
    int getY();

    @Accessor("x")
    int getX();

    @Accessor("focusedSlot")
    Slot getFocusedSlot();

    @Accessor("focusedSlot")
    void setFocusedSlot(Slot slot);
}
