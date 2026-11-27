package dev.prestigeclient.renderlib.impl.render.blur;

import dev.prestigeclient.renderlib.api.uniform.RLUniforms;
import dev.prestigeclient.renderlib.impl.render.InternalShaders;
import dev.prestigeclient.renderlib.impl.render.mgr.RenderPass;
import dev.prestigeclient.renderlib.impl.render.resource.GlFramebuffer;
import dev.prestigeclient.renderlib.impl.render.resource.GlTexture;
import java.util.function.Consumer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL30;

public class BlurState implements AutoCloseable {
    private final int downscale;
    private final GlFramebuffer horizontalFb = GlFramebuffer.create();
    private final GlFramebuffer verticalFb = GlFramebuffer.create();
    private GlTexture horizontalTex;
    private GlTexture verticalTex;
    private int width = -1;
    private int height = -1;

    public BlurState() {
        this(2);
    }

    public BlurState(int downscale) {
        if (downscale < 1) {
            throw new IllegalArgumentException("downscale must be >= 1, got " + downscale);
        }

        this.downscale = downscale;
    }

    public int blur(int sourceTexture, int sourceWidth, int sourceHeight, float radius) {
        this.ensureSize(Math.max(1, sourceWidth / this.downscale), Math.max(1, sourceHeight / this.downscale));
        float sourceRadius = Math.max(radius, 0.0F);
        float scaledRadius = sourceRadius / this.downscale;

        Consumer<RLUniforms> horizontal = uniforms -> uniforms.set1i("colTex", 0).set2f("direction", 1.0F, 0.0F).set1f("radius", sourceRadius);
        Consumer<RLUniforms> vertical = uniforms -> uniforms.set1i("colTex", 0).set2f("direction", 0.0F, 1.0F).set1f("radius", scaledRadius);

        RenderPass.setupPostState();
        RenderPass.builder()
            .target(this.horizontalFb.fbId, this.width, this.height)
            .clear()
            .shaderProgram(InternalShaders.POST_BLUR)
            .source(0, sourceTexture)
            .uniforms(horizontal)
            .build()
            .dispatchFullscreen();
        RenderPass.builder()
            .target(this.verticalFb.fbId, this.width, this.height)
            .clear()
            .shaderProgram(InternalShaders.POST_BLUR)
            .source(0, this.horizontalTex.tex)
            .uniforms(vertical)
            .build()
            .dispatchFullscreen();
        return this.verticalTex.tex;
    }

    public int getOutputTexture() {
        return this.verticalTex == null ? 0 : this.verticalTex.tex;
    }

    public int getOutputFramebuffer() {
        return this.verticalFb.fbId;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public int getDownscale() {
        return this.downscale;
    }

    private void ensureSize(int newWidth, int newHeight) {
        if (newWidth == this.width && newHeight == this.height) {
            return;
        }

        this.width = newWidth;
        this.height = newHeight;
        this.horizontalTex = this.recreate(this.horizontalTex, this.horizontalFb);
        this.verticalTex = this.recreate(this.verticalTex, this.verticalFb);
    }

    private GlTexture recreate(GlTexture previous, GlFramebuffer framebuffer) {
        GlTexture texture;
        if (previous == null) {
            texture = GlTexture.create(GL11.GL_TEXTURE_2D).withoutMipmaps();
            texture.init(GL11.GL_RGBA8, this.width, this.height);
        } else {
            texture = previous.closeAndReinit(this.width, this.height);
        }

        texture.setFilter(GL11.GL_LINEAR);
        texture.setEdgeMode(GL12.GL_CLAMP_TO_EDGE);
        framebuffer.bindTexture(GL30.GL_COLOR_ATTACHMENT0, texture);
        return texture;
    }

    @Override
    public void close() {
        if (this.horizontalTex != null) {
            this.horizontalTex.close();
            this.horizontalTex = null;
        }

        if (this.verticalTex != null) {
            this.verticalTex.close();
            this.verticalTex = null;
        }

        try {
            this.horizontalFb.close();
            this.verticalFb.close();
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to release blur framebuffers", exception);
        }

        this.width = -1;
        this.height = -1;
    }
}
