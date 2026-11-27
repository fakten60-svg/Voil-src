package lol.lwes.voil.mixin.accessor;

import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.memory.ObjectPool;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GameRenderer.class)
public interface GameRendererAccessor {
    @Accessor("pool")
    ObjectPool getPool();

    @Accessor("guiRenderer")
    GuiRenderer getGuiRenderer();

    @Accessor("guiState")
    GuiRenderState getGuiState();
}
