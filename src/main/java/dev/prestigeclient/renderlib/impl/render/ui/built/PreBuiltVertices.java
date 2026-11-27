package dev.prestigeclient.renderlib.impl.render.ui.built;

import dev.prestigeclient.renderlib.impl.render.mesh.MeshDataConsumer;

public interface PreBuiltVertices {
    void initializeData();

    void writeVAO(MeshDataConsumer meshdataconsumer, float f);

    VAOIndexMesh getVAOIndexMesh();
}
