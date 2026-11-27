package lol.lwes.voil.mixin.accessor;

import com.mojang.blaze3d.textures.GpuTexture;
import java.util.Map;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.render.item.KeyedItemRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GuiRenderer.class)
public interface GuiRendererAccessor {
    @Invoker("calcItemAtlasSideLength")
    int tulip_calcItemAtlasSideLength(int i);

    @Accessor("renderedItems")
    Map getRenderedItems();

    @Invoker("prepareItemInitially")
    void tulip_prepareItemInitially(KeyedItemRenderState keyeditemrenderstate, MatrixStack matrixstack, int i, int j, int k);

    @Invoker("createItemAtlas")
    void tulip_createItemAtlas(int i);

    @Accessor("itemAtlasX")
    int tulip_getItemAtlasX();

    @Accessor("itemAtlasY")
    int tulip_getItemAtlasY();

    @Accessor("itemAtlasTexture")
    GpuTexture getItemAtlasTexture();

    @Accessor("itemAtlasY")
    void tulip_setItemAtlasY(int i);

    @Accessor("itemAtlasX")
    void tulip_setItemAtlasX(int i);

    @Invoker("getWindowScaleFactor")
    int tulip_getWindowScaleFactor();
}
