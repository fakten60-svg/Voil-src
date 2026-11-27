package dev.prestigeclient.renderlib.impl.render.resource;

import dev.prestigeclient.renderlib.impl.api.RenderAPI;
import java.lang.ref.Cleaner.Cleanable;
import org.lwjgl.opengl.GL30;

public class GlFramebuffer implements AutoCloseable {
    public final int fbId;
    private final Cleanable cleanable;

    public GlFramebuffer(int i) {
        this.fbId = i;
        this.cleanable = RenderAPI.GL_CLEANER.register(this, () -> RenderAPI.runNowOrPostpone(() -> GL30.glDeleteFramebuffers(i)));
    }

    @Override
    public void close() throws Exception {
        this.cleanable.clean();
    }

    public static GlFramebuffer create() {
        return new GlFramebuffer(GL30.glGenFramebuffers());
    }

    public void blit(int i, int j, int k, int l, int i1, int j1, int k1, int l1, int i2, int j2, int k2) {
        GL30.glBindFramebuffer(36008, this.fbId);
        GL30.glBindFramebuffer(36009, i);
        GL30.glBlitFramebuffer(j, k, j + l, k + i1, j1, k1, j1 + l1, k1 + i2, j2, k2);
        GL30.glBindFramebuffer(36008, 0);
        GL30.glBindFramebuffer(36009, 0);
    }

    public void clearColor(float f, float f1, float f2, float f3) {
        GL30.glBindFramebuffer(36009, this.fbId);
        GL30.glClearBufferfv(6144, 0, new float[]{f, f1, f2, f3});
        GL30.glBindFramebuffer(36009, 0);
    }

    public void clearDepth(float f, int i) {
        GL30.glBindFramebuffer(36009, this.fbId);
        GL30.glClearBufferfi(34041, 0, f, i);
        GL30.glBindFramebuffer(36009, 0);
    }

    public void assertComplete() {
        GL30.glBindFramebuffer(36160, this.fbId);
        int i = GL30.glCheckFramebufferStatus(36160);
        GL30.glBindFramebuffer(36160, 0);
        if (i != 36053) {
            throw new IllegalStateException("Framebuffer isn't complete, is: " + i);
        }
    }

    public void bindTexture(int i, GlTexture gltexture) {
        GL30.glBindFramebuffer(36160, this.fbId);
        GL30.glFramebufferTexture2D(36160, i, 3553, gltexture.tex, 0);
        GL30.glBindFramebuffer(36160, 0);
    }

    public void bindTexture(int i, int j) {
        GL30.glBindFramebuffer(36160, this.fbId);
        GL30.glFramebufferTexture2D(36160, i, 3553, j, 0);
        GL30.glBindFramebuffer(36160, 0);
    }
}
