package dev.prestigeclient.renderlib.impl.render.ui.built;

import dev.prestigeclient.renderlib.impl.render.mesh.MeshMetadata;
import dev.prestigeclient.renderlib.impl.render.resource.GlBuffer;
import dev.prestigeclient.renderlib.impl.render.resource.VertexArrayBuffer;

public record VAOIndexMesh(boolean anyNull, VertexArrayBuffer vaoBuffer, GlBuffer indexBuffer, MeshMetadata ourMeshData) {
    public VAOIndexMesh(VertexArrayBuffer vertexarraybuffer, GlBuffer glbuffer, MeshMetadata meshmetadata) {
        this(vertexarraybuffer == null || glbuffer == null || meshmetadata == null, vertexarraybuffer, glbuffer, meshmetadata);
    }
}
