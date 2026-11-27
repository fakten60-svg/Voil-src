package lol.lwes.voil.util.render;

import dev.prestigeclient.renderlib.api.uniform.RLUniforms;
import dev.prestigeclient.renderlib.impl.render.layer.type.RenderLayer;
import dev.prestigeclient.renderlib.impl.render.ui.elements.PreBuiltTextureRectangleElement;

public class ItemAtlasElement extends PreBuiltTextureRectangleElement {
    final Object itemKey;

    ItemAtlasElement(RenderLayer renderlayer, float f, float f1, float f2, float f3, int i, float f4, float f5, float f6, float f7, Object object) {
        super(renderlayer, f, f1, f2, f3, i, f4, f5, f6, f7);
        this.itemKey = object;
    }

    @Override
    public void setUniforms(RLUniforms rluniforms) {
        float[] afloat = (float[])Render2DUtil.itemAtlasUvs.get(this.itemKey);
        if (afloat != null) {
            this.uvx = afloat[0];
            this.uvy = afloat[1];
            this.currZ = 10.0F;
            super.setUniforms(rluniforms);
        }
    }
}
