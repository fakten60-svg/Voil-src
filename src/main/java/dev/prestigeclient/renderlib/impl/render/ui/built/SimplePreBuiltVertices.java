package dev.prestigeclient.renderlib.impl.render.ui.built;

import dev.prestigeclient.renderlib.impl.api.RenderAPI;
import dev.prestigeclient.renderlib.impl.render.mesh.GlBufferMeshBuilder;
import dev.prestigeclient.renderlib.impl.render.mesh.MeshMetadata;
import dev.prestigeclient.renderlib.impl.render.resource.GlBuffer;
import dev.prestigeclient.renderlib.impl.render.resource.VertexArrayBuffer;
import dev.prestigeclient.renderlib.impl.render.vertex.VertexFormat;

public abstract class SimplePreBuiltVertices implements PreBuiltVertices {
    public boolean autoClean;
    public VertexArrayBuffer vaoBuffer;
    public GlBuffer indexBuffer;
    public MeshMetadata ourMeshData;
    public GlBuffer vertexBuffer;

    public SimplePreBuiltVertices(boolean flag) {
        this.autoClean = flag;
        RenderAPI.runNowOrPostpone(this::initializeData);
    }

    protected SimplePreBuiltVertices() {
        this(true);
    }

    @Override
    public void initializeData() {
        VertexFormat vertexformat = this.getFormat();
        this.vaoBuffer = VertexArrayBuffer.create(null, this.autoClean);
        GlBuffer glbuffer = GlBuffer.create(this.autoClean);
        GlBuffer glbuffer1 = GlBuffer.create(this.autoClean);
        glbuffer.setBufferData(35044, 256L);
        glbuffer1.setBufferData(35044, 32L);

        try (GlBufferMeshBuilder glbuffermeshbuilder = new GlBufferMeshBuilder(vertexformat, glbuffer, glbuffer1)) {
            this.writeVAO(glbuffermeshbuilder, 0.0F);
            this.ourMeshData = new MeshMetadata(vertexformat, glbuffermeshbuilder.countCompleteVertices(), glbuffermeshbuilder.countCompleteIndices());
        }

        this.vertexBuffer = GlBuffer.create(this.autoClean);
        this.indexBuffer = GlBuffer.create(this.autoClean);
        this.vertexBuffer.setBufferData(35044, (long)this.ourMeshData.countVertices() * vertexformat.size);
        this.indexBuffer.setBufferData(35044, this.ourMeshData.countIndices() * 4L);
        this.vaoBuffer.rebind(VertexArrayBuffer.getBindings(this.vertexBuffer, vertexformat));

        try (GlBufferMeshBuilder glbuffermeshbuilder1 = new GlBufferMeshBuilder(vertexformat, this.vertexBuffer, this.indexBuffer)) {
            this.writeVAO(glbuffermeshbuilder1, 0.0F);
            this.ourMeshData = new MeshMetadata(vertexformat, glbuffermeshbuilder1.countCompleteVertices(), glbuffermeshbuilder1.countCompleteIndices());
        }
    }

    public abstract VertexFormat getFormat();

    @Override
    public VAOIndexMesh getVAOIndexMesh() {
        return new VAOIndexMesh(this.vaoBuffer, this.indexBuffer, this.ourMeshData);
    }
}
