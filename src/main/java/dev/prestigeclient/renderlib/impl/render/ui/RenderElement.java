package dev.prestigeclient.renderlib.impl.render.ui;

import dev.prestigeclient.renderlib.impl.render.mesh.MeshDataConsumer;
import dev.prestigeclient.renderlib.impl.render.ui.built.DefaultPreBuiltVertices;
import dev.prestigeclient.renderlib.impl.render.ui.built.PreBuiltVertices;

public interface RenderElement {
    RenderState state();

    default void preDraw(float f) {
    }

    void draw(MeshDataConsumer consumer, float z);

    default PreBuiltVertices preBuiltVerticesType() {
        return DefaultPreBuiltVertices.NONE;
    }
}
