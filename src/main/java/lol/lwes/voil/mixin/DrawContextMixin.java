package lol.lwes.voil.mixin;

import lol.lwes.voil.duck.MatrixStackProviderAccessor;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(DrawContext.class)
public abstract class DrawContextMixin implements MatrixStackProviderAccessor {
    @Unique
    private final MatrixStack matrixStack = new MatrixStack();

    @Override
    public MatrixStack tulip$getMatrices() {
        return this.matrixStack;
    }
}
