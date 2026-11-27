package lol.lwes.voil.util.render.shader;

import dev.prestigeclient.renderlib.api.uniform.RLUniforms;
import dev.prestigeclient.renderlib.impl.api.RenderAPI;
import dev.prestigeclient.renderlib.impl.render.layer.type.RenderLayer;
import dev.prestigeclient.renderlib.impl.render.mgr.RenderPass;
import dev.prestigeclient.renderlib.impl.render.resource.GlFramebuffer;
import dev.prestigeclient.renderlib.impl.render.resource.ShaderProgram;
import dev.prestigeclient.renderlib.impl.render.state.StateApplier;
import dev.prestigeclient.renderlib.impl.render.vertex.VertexElement;
import dev.prestigeclient.renderlib.impl.render.vertex.VertexFormat;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.util.DebugQuadVertices;
import lol.lwes.voil.util.render.FramebufferStateApplier;
import lol.lwes.voil.util.render.RenderManager;
import lol.lwes.voil.util.window.OverlayWindow;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL33;

public class FramebufferBlitRenderer extends FramebufferShaderRenderer {
    public boolean flipped = false;
    public Matrix4f modelMatrix = new Matrix4f().normal();
    public boolean useViewProj = false;
    public float[] offset = new float[3];
    public float[] scale = new float[]{1.0F, 1.0F, 1.0F};
    public boolean drawn = false;
    public static final Map<Long, DebugQuadVertices> buffers = new HashMap<>();
    private long lastUsed;
    private static ShaderProgram blitShader;
    private static final long UNSET_WINDOW_HANDLE = -1L;

    public FramebufferBlitRenderer() {
        this.lastUsed = UNSET_WINDOW_HANDLE;

        try {
            this.vertexFormat = new VertexFormat(
                new VertexElement[]{new VertexElement(3, 5126, 4, false), new VertexElement(2, 5126, 4, false), new VertexElement(4, 5121, 1, true)}
            );
            if (blitShader == null) {
                RenderAPI.schedule(() -> {
                    blitShader = ShaderRenderer.loadShader("shaders/core/simple_framebuffer.fsh", "shaders/copied/plain_position_tex_color.vsh");
                    this.shader = blitShader;
                });
            } else {
                this.shader = blitShader;
            }
        } catch (Exception exception) {
            PrintStream printstream = System.err;
            String s1 = exception.getMessage();
            printstream.println("Failed to load simple framebuffer shader: " + s1);
        }
    }

    public int getHeight() {
        int i = this.getWidth();
        return i != -1 && this.framebuffer != null ? this.framebuffer.fbId : 0;
    }

    public static GlFramebuffer getFramebuffer(FramebufferBlitRenderer framebufferblitrenderer) {
        return framebufferblitrenderer.framebuffer;
    }

    public void drawBuffer(RenderAPI.RenderContext renderapi$rendercontext) {
        int i = this.getWidth();
        if (i != -1) {
            RenderLayer renderlayer = new RenderLayer(
                this.vertexFormat,
                4,
                false,
                new StateApplier[]{
                    StateApplier.framebufferState(this.getTextureId()),
                    new FramebufferStateApplier(this),
                    StateApplier.shaderState(this.shader),
                    StateApplier.flagState(2929, false, true),
                    StateApplier.flagState(2884, false, true),
                    StateApplier.flagState(3089, false, true),
                    StateApplier.flagState(3058, false, false),
                    StateApplier.blendFuncSeparate(true, true, 1, 771, 1, 771),
                    StateApplier.apply(renderpass -> {
                        if (this.texture != null) {
                            GL33.glActiveTexture(33984);
                            GL33.glBindTexture(3553, this.texture.tex);
                            RLUniforms rluniforms = new RLUniforms(renderpass.shaderProgram.glId());
                            rluniforms.set1i("frameSampler", 0);
                            rluniforms.setMatrix4fv("modelMat", false, this.modelMatrix.get(new float[16]));
                            rluniforms.set1f("useViewProj", this.useViewProj ? 2.0F : 0.0F);
                            if (this.useViewProj) {
                                rluniforms.set3f("pos_offsetVec", this.offset[0], this.offset[1], this.offset[2]);
                                rluniforms.set3f("pos_scaleVec", this.scale[0], this.scale[1], this.scale[2]);
                            }

                            rluniforms.set1f("testUniform", this.flipped ? 2.0F : 0.0F);
                        }
                    })
                }
            );
            long j = OverlayWindow.getCurrentContextHandle();
            if (j != this.lastUsed) {
                this.dirty = true;
                this.getWidth();
                this.lastUsed = j;
            }

            DebugQuadVertices debugquadvertices = buffers.computeIfAbsent(j, olong -> {
                DebugQuadVertices debugquadvertices1 = new DebugQuadVertices(this);
                if (debugquadvertices1.ourMeshData == null) {
                    debugquadvertices1.initializeData();
                }

                OverlayWindow.queueTask(() -> {
                    GL30.glDeleteVertexArrays(debugquadvertices1.vaoBuffer.vao());
                    GL15.glDeleteBuffers(debugquadvertices1.indexBuffer.getId());
                    GL15.glDeleteBuffers(debugquadvertices1.vertexBuffer.getId());
                });
                return debugquadvertices1;
            });
            renderlayer.draw(renderapi$rendercontext, debugquadvertices.vaoBuffer, debugquadvertices.indexBuffer, debugquadvertices.ourMeshData);
        }
    }

    public int getWidth() {
        if (this.shader == null) {
            return -1;
        }

        if (VoilClient.mc.getWindow() == null) {
            return -1;
        }

        int i = RenderAPI.getMinecraftFramebuffer();
        int j = VoilClient.mc.getWindow().getFramebufferWidth();
        int k = VoilClient.mc.getWindow().getFramebufferHeight();
        this.ensureFramebuffer(j, k);
        return this.framebuffer != null && this.texture != null ? i : -1;
    }

    protected int getTextureId() {
        return RenderManager.getCurrentFramebuffer();
    }
}
