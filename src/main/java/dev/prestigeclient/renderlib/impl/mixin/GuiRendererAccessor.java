package dev.prestigeclient.renderlib.impl.mixin;

import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.render.ProjectionMatrix2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GuiRenderer.class)
public interface GuiRendererAccessor {
    @Accessor("guiProjectionMatrix")
    ProjectionMatrix2 getGuiProjectionMatrix();
}
