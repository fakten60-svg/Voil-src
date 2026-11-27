package dev.prestigeclient.renderlib.impl.render.layer.type;

import dev.prestigeclient.renderlib.impl.api.RenderAPI;
import dev.prestigeclient.renderlib.impl.render.mesh.GlBufferMeshBuilder;
import dev.prestigeclient.renderlib.impl.render.mesh.MeshMetadata;
import dev.prestigeclient.renderlib.impl.render.mesh.QuadIndexBuffer;
import dev.prestigeclient.renderlib.impl.render.resource.GlBuffer;
import dev.prestigeclient.renderlib.impl.render.resource.VertexArrayBuffer;
import it.unimi.dsi.fastutil.Pair;
import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

public class BufferCollector {
    protected final WeakHashMap<RenderLayer, BufferCollector.BuildingBuffer> buffers = new WeakHashMap<>();
    protected final Set<RenderLayer> buildingBuffers = new HashSet<>();

    public GlBufferMeshBuilder getBuffer(RenderLayer renderlayer, int i, int j) {
        this.buildingBuffers.add(renderlayer);
        BufferCollector.BuildingBuffer buffercollector$buildingbuffer = (BufferCollector.BuildingBuffer)this.buffers.get(renderlayer);
        if (buffercollector$buildingbuffer != null) {
            if (buffercollector$buildingbuffer.meshBuilder.get() != null) {
                return (GlBufferMeshBuilder)buffercollector$buildingbuffer.meshBuilder.get();
            }

            GlBufferMeshBuilder glbuffermeshbuilder1 = new GlBufferMeshBuilder(
                renderlayer.format, buffercollector$buildingbuffer.vert, buffercollector$buildingbuffer.idx
            );
            buffercollector$buildingbuffer.meshBuilder.set(glbuffermeshbuilder1);
            return glbuffermeshbuilder1;
        } else {
            GlBuffer glbuffer = GlBuffer.create();
            GlBuffer glbuffer1 = GlBuffer.create();
            glbuffer.setBufferData(35048, (long)i * renderlayer.format.size);
            glbuffer1.setBufferData(35048, j * 4L);
            GlBufferMeshBuilder glbuffermeshbuilder = new GlBufferMeshBuilder(renderlayer.format, glbuffer, glbuffer1);
            VertexArrayBuffer vertexarraybuffer = VertexArrayBuffer.fromVertexFormat(glbuffer, renderlayer.format);
            this.buffers
                .put(renderlayer, new BufferCollector.BuildingBuffer(glbuffer, glbuffer1, vertexarraybuffer, new AtomicReference<>(glbuffermeshbuilder)));
            return glbuffermeshbuilder;
        }
    }

    public GlBufferMeshBuilder getBuffer(RenderLayer renderlayer) {
        return this.getBuffer(renderlayer, 128, 128);
    }

    public void draw(RenderAPI.RenderContext renderapi$rendercontext) {
        for (RenderLayer renderlayer : this.buildingBuffers) {
            this.draw(renderapi$rendercontext, renderlayer);
        }

        this.buildingBuffers.clear();
        this.buffers.size();
    }

    public void draw(RenderAPI.RenderContext renderapi$rendercontext, RenderLayer renderlayer) {
        BufferCollector.BuildingBuffer buffercollector$buildingbuffer = (BufferCollector.BuildingBuffer)this.buffers.get(renderlayer);
        assert buffercollector$buildingbuffer != null;
        GlBufferMeshBuilder glbuffermeshbuilder = (GlBufferMeshBuilder)buffercollector$buildingbuffer.meshBuilder.get();
        assert glbuffermeshbuilder != null : "empty buffer but we have a record of it being requested. huh?";
        int i = glbuffermeshbuilder.countCompleteIndices();
        int j = glbuffermeshbuilder.countCompleteVertices();
        glbuffermeshbuilder.close();
        buffercollector$buildingbuffer.meshBuilder.set(null);
        GlBuffer glbuffer = buffercollector$buildingbuffer.idx;
        if (i == 0 && j >= 4 && renderlayer.isOutline()) {
            int k = j / 4;
            glbuffer = QuadIndexBuffer.forQuads(k);
            i = k * 6;
        }

        renderlayer.draw(renderapi$rendercontext, buffercollector$buildingbuffer.vab, glbuffer, new MeshMetadata(renderlayer.format, j, i));
    }

    public Stream<Pair<RenderLayer, GlBufferMeshBuilder>> getAllActiveBuffers() {
        return this.buildingBuffers
            .stream()
            .map(renderlayer -> Pair.of(renderlayer, this.buffers.get(renderlayer).meshBuilder.get()));
    }

    public record BuildingBuffer(GlBuffer vert, GlBuffer idx, VertexArrayBuffer vab, AtomicReference<GlBufferMeshBuilder> meshBuilder) {
    }
}
