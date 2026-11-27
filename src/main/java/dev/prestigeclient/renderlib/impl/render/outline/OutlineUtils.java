package dev.prestigeclient.renderlib.impl.render.outline;

import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import dev.prestigeclient.renderlib.api.entity.EntityOutlineHandler;
import dev.prestigeclient.renderlib.api.shader.RLEntityOutlineShader;
import dev.prestigeclient.renderlib.impl.mixin.RenderLayerMixin;
import dev.prestigeclient.renderlib.impl.render.mesh.GlBufferMeshBuilder;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderSetup;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.GlTexture;
import net.minecraft.client.texture.Sprite;

public final class OutlineUtils {
    private static final String PRIMARY_SAMPLER = "Sampler0";
    private static final int NO_TEXTURE = -1;
    private static final Map<RenderLayer, Integer> textureIds = new IdentityHashMap<>();

    private OutlineUtils() {
    }

    public static void clearFrameCache() {
        textureIds.clear();
    }

    public static int textureIdOf(RenderLayer layer) {
        if (layer == null) {
            return NO_TEXTURE;
        }

        Integer cached = textureIds.get(layer);
        if (cached != null) {
            return cached;
        }

        int id = resolveTextureId(layer);
        textureIds.put(layer, id);
        return id;
    }

    public static VertexConsumer outlineTee(int textureId, Sprite sprite) {
        VertexConsumer tee = new OutlineVertexConsumer(textureId, OutlineSeed.vertexColor());
        return sprite != null ? sprite.getTextureSpecificVertexConsumer(tee) : tee;
    }

    private static int resolveTextureId(RenderLayer layer) {
        RenderSetup setup = ((RenderLayerMixin)(Object)layer).getRenderSetup();
        if (setup == null) {
            return NO_TEXTURE;
        }

        Map<String, RenderSetup.Texture> textures = setup.resolveTextures();
        if (textures.isEmpty()) {
            return NO_TEXTURE;
        }

        RenderSetup.Texture texture = textures.get(PRIMARY_SAMPLER);
        if (texture == null) {
            texture = textures.values().iterator().next();
        }

        GpuTextureView view = texture.textureView();
        if (view == null || view.isClosed()) {
            return NO_TEXTURE;
        }

        GpuTexture gpuTexture = view.texture();
        return gpuTexture instanceof GlTexture glTexture ? glTexture.getGlId() : NO_TEXTURE;
    }

    private static final class OutlineVertexConsumer implements VertexConsumer {
        private final int textureId;
        private final int color;
        private boolean pending;
        private float x;
        private float y;
        private float z;

        private OutlineVertexConsumer(int textureId, int color) {
            this.textureId = textureId;
            this.color = color;
        }

        @Override
        public VertexConsumer vertex(float x, float y, float z) {
            if (this.pending) {
                this.emit(0.0F, 0.0F);
            }

            this.x = x;
            this.y = y;
            this.z = z;
            this.pending = true;
            return this;
        }

        @Override
        public VertexConsumer texture(float u, float v) {
            if (this.pending) {
                this.emit(u, v);
            }

            return this;
        }

        @Override
        public VertexConsumer color(int red, int green, int blue, int alpha) {
            return this;
        }

        @Override
        public VertexConsumer color(int argb) {
            return this;
        }

        @Override
        public VertexConsumer overlay(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer light(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer normal(float x, float y, float z) {
            return this;
        }

        @Override
        public VertexConsumer lineWidth(float width) {
            return this;
        }

        private void emit(float u, float v) {
            this.pending = false;
            RLEntityOutlineShader shader = EntityOutlineHandler.getActiveShader();
            if (shader == null) {
                return;
            }

            GlBufferMeshBuilder builder = shader.getBuffer(EntityOutlineHandler.outlineBufferCollector, this.textureId);
            builder.pos(this.x, this.y, this.z).color(this.color).floats(u, v);
        }
    }
}
