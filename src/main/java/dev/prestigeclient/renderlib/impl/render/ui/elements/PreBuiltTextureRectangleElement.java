package dev.prestigeclient.renderlib.impl.render.ui.elements;

import dev.prestigeclient.renderlib.api.uniform.RLUniforms;
import dev.prestigeclient.renderlib.impl.render.layer.type.RenderLayer;
import dev.prestigeclient.renderlib.impl.render.ui.built.DefaultPreBuiltVertices;
import dev.prestigeclient.renderlib.impl.render.ui.built.PreBuiltVertices;

public class PreBuiltTextureRectangleElement extends PreBuiltRectangleElement {
    protected float uvx;
    protected float uvy;
    protected float uvw;
    protected float uvh;

    public PreBuiltTextureRectangleElement(RenderLayer renderlayer, float f, float f1, float f2, float f3, int i, float f4, float f5, float f6, float f7) {
        super(renderlayer, f, f1, f2, f3, i);
        this.uvx = f4;
        this.uvy = f5;
        this.uvw = f6;
        this.uvh = f7;
    }

    @Override
    public void setUniforms(RLUniforms rluniforms) {
        super.setUniforms(rluniforms);
        rluniforms.set2f("uv_offsetVec", this.uvx, this.uvy);
        rluniforms.set2f("uv_scaleVec", this.uvw, this.uvh);
    }

    @Override
    public PreBuiltVertices preBuiltVerticesType() {
        return DefaultPreBuiltVertices.SQUARE_TEX;
    }
}
