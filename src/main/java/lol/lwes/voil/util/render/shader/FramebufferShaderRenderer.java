package lol.lwes.voil.util.render.shader;

import dev.prestigeclient.renderlib.api.uniform.RLUniforms;
import dev.prestigeclient.renderlib.impl.api.RenderAPI;
import dev.prestigeclient.renderlib.impl.render.resource.GlFramebuffer;
import dev.prestigeclient.renderlib.impl.render.resource.GlTexture;
import java.util.function.Consumer;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.opengl.ARBDirectStateAccess;
import org.lwjgl.opengl.GL33;

public abstract class FramebufferShaderRenderer extends ShaderRenderer {
    protected GlFramebuffer framebuffer;
    protected GlTexture texture;
    protected int width;
    protected int height;
    protected boolean dirty = false;
    private long lastUsed;
    private static final long NEVER_USED = -1L;

    public FramebufferShaderRenderer() {
        this.lastUsed = NEVER_USED;
    }

    protected boolean usesFramebuffer() {
        return true;
    }

    protected void ensureFramebuffer(int i, int j) {
        if (i > 0 && j > 0) {
            if (this.framebuffer == null || this.width != i || this.height != j || this.dirty) {
                this.dirty = false;
                this.releaseFramebuffer();
                this.width = i;
                this.height = j;

                try {
                    this.texture = GlTexture.create(3553);
                    this.texture.init(32856, i, j);
                    this.texture.setFilter(9729);
                    this.texture.setEdgeMode(33071);
                    this.framebuffer = GlFramebuffer.create();
                    this.framebuffer.bindTexture(36064, this.texture);
                    this.framebuffer.assertComplete();
                } catch (Exception exception) {
                    this.releaseFramebuffer();
                }
            }
        } else {
            this.dirty = true;
        }
    }

    public void releaseFramebuffer() {
        if (this.framebuffer != null) {
            try {
                this.framebuffer.close();
            } catch (Exception exception1) {
            }

            this.framebuffer = null;
        }

        if (this.texture != null) {
            try {
                this.texture.close();
            } catch (Exception exception) {
            }

            this.texture = null;
        }
    }

    public void renderFramebuffer(float f, float f1, float f2, float f3, int i, Consumer consumer) {
        if (this.shader != null) {
            MinecraftClient minecraftclient = MinecraftClient.getInstance();
            if (minecraftclient.getWindow() != null) {
                int j = RenderAPI.getMinecraftFramebuffer();
                int k = minecraftclient.getWindow().getFramebufferWidth();
                int l = minecraftclient.getWindow().getFramebufferHeight();
                boolean flag = this.usesFramebuffer();
                if (flag) {
                    this.ensureFramebuffer(k, l);
                    if (this.framebuffer == null || this.texture == null) {
                        return;
                    }

                    long i1 = RenderAPI.getFrameCounter();
                    if (i1 != this.lastUsed) {
                        this.lastUsed = i1;
                        ARBDirectStateAccess.glBlitNamedFramebuffer(j, this.framebuffer.fbId, 0, 0, k, l, 0, 0, this.width, this.height, 16384, 9729);
                    }

                    GL33.glBindFramebuffer(36160, j);
                }

                float f4 = f - i;
                super.drawRect(f4, f1 - i, f2 + (i + i), f3 + i * 2, rluniforms -> {
                    if (flag) {
                        if (this.texture == null) {
                            return;
                        }

                        GL33.glActiveTexture(33984);
                        GL33.glBindTexture(3553, this.texture.tex);
                        rluniforms.set1i("InputSampler", 0);
                    }

                    rluniforms.set2f("InputResolution", k, l);
                    if (consumer != null) {
                        consumer.accept(rluniforms);
                    }
                });
            }
        }
    }
}
