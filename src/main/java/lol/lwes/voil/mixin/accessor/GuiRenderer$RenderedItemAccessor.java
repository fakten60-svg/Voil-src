package lol.lwes.voil.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.client.gui.render.GuiRenderer$RenderedItem")
public interface GuiRenderer$RenderedItemAccessor {
    @Accessor("y")
    int getY();

    @Accessor("x")
    int getX();

    @Accessor("u")
    float getU();

    @Accessor("v")
    float getV();
}
