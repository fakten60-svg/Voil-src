package dev.prestigeclient.renderlib.example.shader;

import dev.prestigeclient.renderlib.api.path.RLPath;
import dev.prestigeclient.renderlib.api.shader.RLShader;
import dev.prestigeclient.renderlib.impl.api.RenderAPI;
import dev.prestigeclient.renderlib.impl.render.Util;
import dev.prestigeclient.renderlib.impl.render.layer.type.BufferCollector;
import dev.prestigeclient.renderlib.impl.render.layer.type.RenderLayer;
import dev.prestigeclient.renderlib.impl.render.mesh.GlBufferMeshBuilder;
import dev.prestigeclient.renderlib.impl.render.state.StateApplier;
import dev.prestigeclient.renderlib.impl.render.vertex.VertexFormats;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.lwjgl.opengl.GL11;

public class RoundedRectShader extends RLShader {
    private static final float SOLID_RADIUS = 1.0E6F;
    private static final int MAX_CACHED_LAYERS = 16;
    private final Int2ObjectOpenHashMap<RenderLayer> layers = new Int2ObjectOpenHashMap<>();

    public RoundedRectShader() {
        super(RLPath.of("shaders/rounded_pos_color.vert"), RLPath.of("shaders/rounded_pos_color.frag"));
    }

    public RenderLayer getLayer() {
        int framebuffer = RenderAPI.getMinecraftFramebuffer();
        RenderLayer layer = this.layers.get(framebuffer);
        if (layer == null) {
            if (this.layers.size() >= MAX_CACHED_LAYERS) {
                this.layers.clear();
            }

            layer = new RenderLayer(
                VertexFormats.POSITION_COLOR_UV,
                GL11.GL_TRIANGLES,
                false,
                new StateApplier[]{
                    StateApplier.framebufferState(framebuffer),
                    StateApplier.shaderState(this.program),
                    StateApplier.flagState(GL11.GL_DEPTH_TEST, false, true),
                    StateApplier.flagState(GL11.GL_CULL_FACE, false, true),
                    StateApplier.blendState(true, true)
                }
            );
            this.layers.put(framebuffer, layer);
        }

        return layer;
    }

    public void draw(BufferCollector collector, float x, float y, float width, float height, float radius, int color) {
        if (width <= 0.0F || height <= 0.0F) {
            return;
        }

        float r = Math.max(0.0F, Math.min(radius, Math.min(width, height) * 0.5F));
        int vertexColor = Util.toVertexColor(color);
        GlBufferMeshBuilder builder = collector.getBuffer(this.getLayer());
        float right = x + width;
        float bottom = y + height;
        if (r <= 0.0F) {
            solid(builder, x, y, right, bottom, vertexColor);
            return;
        }

        solid(builder, x + r, y, right - r, bottom, vertexColor);
        solid(builder, x, y + r, x + r, bottom - r, vertexColor);
        solid(builder, right - r, y + r, right, bottom - r, vertexColor);
        corner(builder, x, y, x + r, y + r, x + r, y + r, r, vertexColor);
        corner(builder, right - r, y, right, y + r, right - r, y + r, r, vertexColor);
        corner(builder, x, bottom - r, x + r, bottom, x + r, bottom - r, r, vertexColor);
        corner(builder, right - r, bottom - r, right, bottom, right - r, bottom - r, r, vertexColor);
    }

    private static void solid(GlBufferMeshBuilder builder, float x1, float y1, float x2, float y2, int color) {
        if (x2 <= x1 || y2 <= y1) {
            return;
        }

        quad(builder, x1, y1, x2, y2, (x1 + x2) * 0.5F, (y1 + y2) * 0.5F, SOLID_RADIUS, color);
    }

    private static void corner(GlBufferMeshBuilder builder, float x1, float y1, float x2, float y2, float centerX, float centerY, float radius, int color) {
        quad(builder, x1, y1, x2, y2, centerX, centerY, radius, color);
    }

    private static void quad(GlBufferMeshBuilder builder, float x1, float y1, float x2, float y2, float centerX, float centerY, float radius, int color) {
        builder.start();
        builder.pos(x1, y2, radius).color(color).floats(centerX, centerY);
        builder.pos(x1, y1, radius).color(color).floats(centerX, centerY);
        builder.pos(x2, y1, radius).color(color).floats(centerX, centerY);
        builder.pos(x2, y2, radius).color(color).floats(centerX, centerY);
        builder.putIndices(new int[]{0, 1, 2, 2, 3, 0});
        builder.end();
    }
}
