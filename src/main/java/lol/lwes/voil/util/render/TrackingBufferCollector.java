package lol.lwes.voil.util.render;

import dev.prestigeclient.renderlib.impl.api.RenderAPI;
import dev.prestigeclient.renderlib.impl.render.layer.type.BufferCollector;
import dev.prestigeclient.renderlib.impl.render.layer.type.RenderLayer;
import dev.prestigeclient.renderlib.impl.render.mesh.GlBufferMeshBuilder;
import it.unimi.dsi.fastutil.Pair;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Stream;

public class TrackingBufferCollector extends BufferCollector {
    protected final Set<RenderLayer> usedLayers = new LinkedHashSet<>();

    @Override
    public GlBufferMeshBuilder getBuffer(RenderLayer renderlayer, int i, int j) {
        this.usedLayers.add(renderlayer);
        return super.getBuffer(renderlayer, i, j);
    }

    @Override
    public void draw(RenderAPI.RenderContext renderapi$rendercontext) {
        for (RenderLayer renderlayer : this.usedLayers) {
            super.draw(renderapi$rendercontext, renderlayer);
        }

        this.usedLayers.clear();
        this.buildingBuffers.clear();
        this.buffers.size();
    }

    @Override
    public Stream getAllActiveBuffers() {
        return this.usedLayers
            .stream()
            .map(renderlayer -> Pair.of(renderlayer, (GlBufferMeshBuilder)((BufferCollector.BuildingBuffer)this.buffers.get(renderlayer)).meshBuilder().get()));
    }
}
