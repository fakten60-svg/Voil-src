package dev.prestigeclient.renderlib.impl.render.mesh;

import dev.prestigeclient.renderlib.impl.render.vertex.VertexFormat;

public record MeshMetadata(VertexFormat format, int countVertices, int countIndices) {
}
