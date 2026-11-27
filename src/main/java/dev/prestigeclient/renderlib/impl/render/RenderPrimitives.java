package dev.prestigeclient.renderlib.impl.render;

import dev.prestigeclient.renderlib.impl.api.RenderAPI;
import dev.prestigeclient.renderlib.impl.render.layer.type.BufferCollector;
import dev.prestigeclient.renderlib.impl.render.layer.type.RenderLayer;
import dev.prestigeclient.renderlib.impl.render.mesh.GlBufferMeshBuilder;
import dev.prestigeclient.renderlib.impl.render.state.StateApplier;
import dev.prestigeclient.renderlib.impl.render.vertex.VertexFormats;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.lwjgl.opengl.GL11;

public final class RenderPrimitives {
    private static final int CIRCLE_SEGMENTS = 72;
    private static final int MAX_CACHED_LAYERS = 16;
    private static final Int2ObjectOpenHashMap<RenderLayer> WORLD_COLOR_LAYERS = new Int2ObjectOpenHashMap<>();

    private RenderPrimitives() {
    }

    public static void circleOutline3D(
        RenderAPI.RenderContext context, BufferCollector collector, Matrix4f matrix, float x, float y, float z, float radius, float thickness, int color
    ) {
        Vector3d center = context.camera() != null ? RenderAPI.getRelativePos(context, x, y, z) : new Vector3d(x, y, z);
        float cx = (float)center.x;
        float cy = (float)center.y;
        float cz = (float)center.z;
        float halfThickness = Math.max(thickness, 0.0F) * 0.5F;
        float inner = Math.max(radius - halfThickness, 0.0F);
        float outer = radius + halfThickness;
        int vertexColor = Util.toVertexColor(color);

        GlBufferMeshBuilder builder = collector.getBuffer(worldColorLayer());
        builder.start();

        for (int i = 0; i <= CIRCLE_SEGMENTS; i++) {
            double angle = Math.PI * 2.0 * i / CIRCLE_SEGMENTS;
            float cos = (float)Math.cos(angle);
            float sin = (float)Math.sin(angle);
            builder.pos(matrix, cx + cos * inner, cy, cz + sin * inner).color(vertexColor);
            builder.pos(matrix, cx + cos * outer, cy, cz + sin * outer).color(vertexColor);
        }

        for (int i = 0; i < CIRCLE_SEGMENTS; i++) {
            int base = i * 2;
            builder.putIndices(new int[]{base, base + 1, base + 3, base + 3, base + 2, base});
        }

        builder.end();
    }

    private static RenderLayer worldColorLayer() {
        int framebuffer = RenderAPI.getMinecraftFramebuffer();
        RenderLayer layer = WORLD_COLOR_LAYERS.get(framebuffer);
        if (layer == null) {
            if (WORLD_COLOR_LAYERS.size() >= MAX_CACHED_LAYERS) {
                WORLD_COLOR_LAYERS.clear();
            }

            layer = new RenderLayer(
                VertexFormats.POSITION_COLOR,
                GL11.GL_TRIANGLES,
                false,
                new StateApplier[]{
                    StateApplier.framebufferState(framebuffer),
                    StateApplier.shaderState(InternalShaders.POS_COLOR),
                    StateApplier.flagState(GL11.GL_CULL_FACE, false, true),
                    StateApplier.flagState(GL11.GL_DEPTH_TEST, true, true),
                    StateApplier.blendState(true, true),
                    StateApplier.depthFuncState(GL11.GL_LEQUAL, GL11.GL_LEQUAL)
                }
            );
            WORLD_COLOR_LAYERS.put(framebuffer, layer);
        }

        return layer;
    }
}
