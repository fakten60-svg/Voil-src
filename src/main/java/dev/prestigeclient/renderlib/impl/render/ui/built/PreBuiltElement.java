package dev.prestigeclient.renderlib.impl.render.ui.built;

import dev.prestigeclient.renderlib.api.uniform.RLUniforms;
import dev.prestigeclient.renderlib.impl.render.mesh.MeshDataConsumer;
import dev.prestigeclient.renderlib.impl.render.mgr.RenderPass;
import dev.prestigeclient.renderlib.impl.render.state.StateApplier;
import dev.prestigeclient.renderlib.impl.render.ui.RenderElement;

public interface PreBuiltElement extends RenderElement, StateApplier {
    @Override
    default void apply(RenderPass renderpass) {
        this.setUniforms(RLUniforms.get(renderpass.shaderProgram.glId()));
    }

    void setUniforms(RLUniforms rluniforms);

    @Override
    default void draw(MeshDataConsumer consumer, float z) {
    }
}
