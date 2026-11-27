package dev.prestigeclient.renderlib.impl.render.ui.elements;

import dev.prestigeclient.renderlib.api.uniform.RLUniforms;
import dev.prestigeclient.renderlib.impl.render.layer.type.RenderLayer;
import dev.prestigeclient.renderlib.impl.render.state.StateApplier;
import dev.prestigeclient.renderlib.impl.render.ui.RenderState;
import dev.prestigeclient.renderlib.impl.render.ui.built.DefaultPreBuiltVertices;
import dev.prestigeclient.renderlib.impl.render.ui.built.PreBuiltElement;
import dev.prestigeclient.renderlib.impl.render.ui.built.PreBuiltVertices;
import java.awt.Color;

public class PreBuiltRectangleElement implements PreBuiltElement {
    protected RenderState ourState;
    protected StateApplier[] initStates;
    protected float left;
    protected float top;
    protected float width;
    protected float height;
    protected float currZ;
    protected int color;

    public PreBuiltRectangleElement(RenderLayer renderlayer, float f, float f1, float f2, float f3, int i) {
        this.ourState = new RenderState(null, renderlayer.copy());
        this.initStates = this.ourState.layer().getStates();
        this.left = f;
        this.top = f1;
        this.width = f2;
        this.height = f3;
        this.color = i;
    }

    @Override
    public RenderState state() {
        return this.ourState;
    }

    @Override
    public void preDraw(float f) {
        this.currZ = f;
        int i = this.initStates.length;
        StateApplier[] astateapplier = new StateApplier[i + 1];
        System.arraycopy(this.initStates, 0, astateapplier, 1, this.initStates.length);
        astateapplier[0] = this;
        this.ourState.layer().setStates(astateapplier);
    }

    @Override
    public void setUniforms(RLUniforms rluniforms) {
        Color colorx = new Color(this.color, true);
        rluniforms.set4f("colorVal", colorx.getRed() / 255.0F, colorx.getGreen() / 255.0F, colorx.getBlue() / 255.0F, colorx.getAlpha() / 255.0F);
        rluniforms.set3f("pos_offsetVec", this.left, this.top, this.currZ);
        rluniforms.set3f("pos_scaleVec", this.width, this.height, 1.0F);
    }

    @Override
    public PreBuiltVertices preBuiltVerticesType() {
        return DefaultPreBuiltVertices.SQUARE;
    }
}
