package lol.lwes.voil.util;

import com.mojang.blaze3d.opengl.GlStateManager;
import dev.prestigeclient.renderlib.api.path.RLPath;
import dev.prestigeclient.renderlib.api.shader.RLEntityOutlineShader;
import dev.prestigeclient.renderlib.api.uniform.RLUniforms;
import dev.prestigeclient.renderlib.impl.api.RenderAPI;
import dev.prestigeclient.renderlib.impl.render.InternalShaders;
import dev.prestigeclient.renderlib.impl.render.PostPipeline;
import dev.prestigeclient.renderlib.impl.render.layer.type.BufferCollector;
import dev.prestigeclient.renderlib.impl.render.layer.type.RenderLayer;
import dev.prestigeclient.renderlib.impl.render.mesh.GlBufferMeshBuilder;
import dev.prestigeclient.renderlib.impl.render.mgr.RenderPass;
import dev.prestigeclient.renderlib.impl.render.mgr.ShaderManager;
import dev.prestigeclient.renderlib.impl.render.resource.ShaderProgram;
import dev.prestigeclient.renderlib.impl.render.state.StateApplier;
import dev.prestigeclient.renderlib.impl.render.vertex.VertexFormats;
import java.awt.Color;
import java.nio.ByteBuffer;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.module.render.ESP;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

public class JfaWireframeShader extends RLEntityOutlineShader {
    private int depthRenderbuffer = -1;
    private int whiteTexture = -1;
    private RenderLayer wireLayer;
    private ShaderProgram seedProgram;
    private ShaderProgram stepProgram;
    private int jfaFboA = -1;
    private int jfaFboB = -1;
    private int jfaTexA = -1;
    private int jfaTexB = -1;
    private int quadVao = -1;
    private int jfaResultTex = -1;
    private static final RLPath JFA_VERT_PATH = RLPath.of("assets/tulip", "shaders/jfa/jfa.vert");
    private static final long TIME_WRAP_MS = 100000L;

    public JfaWireframeShader() {
        super(RLPath.of("shaders/post.vert"), RLPath.of("assets/tulip", "shaders/post/wireframe.fsh"));
    }

    private void createTargets() {
        this.jfaTexA = createTexture(fbWidth, fbHeight);
        this.jfaTexB = createTexture(fbWidth, fbHeight);
        this.jfaFboA = GL30.glGenFramebuffers();
        GL30.glBindFramebuffer(36160, this.jfaFboA);
        GL30.glFramebufferTexture2D(36160, 36064, 3553, this.jfaTexA, 0);
        this.jfaFboB = GL30.glGenFramebuffers();
        GL30.glBindFramebuffer(36160, this.jfaFboB);
        GL30.glFramebufferTexture2D(36160, 36064, 3553, this.jfaTexB, 0);
        GL30.glBindFramebuffer(36160, 0);
    }

    private static int createTexture(int i, int j) {
        int k = GL11.glGenTextures();
        GL11.glBindTexture(3553, k);
        GL11.glTexImage2D(3553, 0, 33327, i, j, 0, 33319, 5126, (ByteBuffer)null);
        GL11.glTexParameteri(3553, 10241, 9728);
        GL11.glTexParameteri(3553, 10240, 9728);
        GL11.glTexParameteri(3553, 10242, 33071);
        GL11.glTexParameteri(3553, 10243, 33071);
        return k;
    }

    private static void resizeTexture(int i, int j, int k) {
        GL11.glBindTexture(3553, i);
        GL11.glTexImage2D(3553, 0, 33327, j, k, 0, 33319, 5126, (ByteBuffer)null);
    }

    private void runJfa() {
        int i = GL11.glGetInteger(36006);
        int j = GL11.glGetInteger(35725);
        int k = GL11.glGetInteger(34229);
        int l = GL11.glGetInteger(34016);
        GL13.glActiveTexture(33984);
        int i1 = GL11.glGetInteger(32873);
        GL13.glActiveTexture(l);
        int[] aint = new int[4];
        GL11.glGetIntegerv(2978, aint);
        float[] afloat = new float[4];
        GL11.glGetFloatv(3106, afloat);
        GL11.glDisable(2884);
        GL11.glDisable(2929);
        GL11.glDisable(3042);
        GL30.glBindVertexArray(this.quadVao);
        GL11.glViewport(0, 0, fbWidth, fbHeight);
        GL30.glBindFramebuffer(36160, this.jfaFboA);
        GL11.glClearColor(-1.0F, -1.0F, 0.0F, 0.0F);
        GL11.glClear(16384);
        if (this.seedProgram != null) {
            GL20.glUseProgram(this.seedProgram.glId());
            GL13.glActiveTexture(33984);
            GL11.glBindTexture(3553, this.colTex.tex);
            RLUniforms.get(this.seedProgram.glId()).set1i("texSampler", 0);
        }

        GL11.glDrawArrays(5, 0, 4);
        int step = Integer.highestOneBit(Math.max(fbWidth, fbHeight));
        boolean readFromA = true;

        for (int pass = 0; step >= 1 && pass < 2; pass++) {
            GL30.glBindFramebuffer(36160, readFromA ? this.jfaFboB : this.jfaFboA);
            if (this.stepProgram != null) {
                GL20.glUseProgram(this.stepProgram.glId());
                GL13.glActiveTexture(33984);
                GL11.glBindTexture(3553, readFromA ? this.jfaTexA : this.jfaTexB);
                RLUniforms rluniforms = RLUniforms.get(this.stepProgram.glId());
                rluniforms.set1i("jfaTex", 0);
                rluniforms.set1f("uStep", step);
            }

            GL11.glDrawArrays(5, 0, 4);
            readFromA = !readFromA;
            step >>= 1;
        }

        this.jfaResultTex = readFromA ? this.jfaTexA : this.jfaTexB;
        GL30.glBindVertexArray(k);
        GL20.glUseProgram(j);
        GL11.glViewport(aint[0], aint[1], aint[2], aint[3]);
        GL11.glClearColor(afloat[0], afloat[1], afloat[2], afloat[3]);
        GL13.glActiveTexture(33984);
        GL11.glBindTexture(3553, i1);
        GL13.glActiveTexture(l);
        GL30.glBindFramebuffer(36160, i);
        GlStateManager._glBindFramebuffer(36160, i != this.jfaFboA ? this.jfaFboA : this.jfaFboB);
        GlStateManager._glBindFramebuffer(36160, i);
        GlStateManager._activeTexture(33984);
        GlStateManager._bindTexture(i1 != this.jfaTexA ? this.jfaTexA : this.jfaTexB);
        GlStateManager._bindTexture(i1);
        GlStateManager._activeTexture(l);
        GlStateManager._enableCull();
        GlStateManager._disableCull();
        GlStateManager._enableBlend();
        GlStateManager._disableBlend();
        GlStateManager._enableDepthTest();
        GlStateManager._disableDepthTest();
    }

    @Override
    public void post(BufferCollector buffercollector, RenderAPI.RenderContext renderapi$rendercontext) {
        GL11.glPolygonMode(1032, 6914);
        if (initialized) {
            int i = GL11.glGetInteger(36010);
            int j = GL11.glGetInteger(36006);
            int k = RenderAPI.getMinecraftFramebuffer();
            GL30.glBindFramebuffer(36008, k);
            GL30.glBindFramebuffer(36009, this.theFb.fbId);
            GL30.glBlitFramebuffer(0, 0, fbWidth, fbHeight, 0, 0, fbWidth, fbHeight, 256, 9728);
            GL30.glBindFramebuffer(36008, i);
            GL30.glBindFramebuffer(36009, j);
            int l = GL11.glGetInteger(36006);
            GlStateManager._glBindFramebuffer(36160, l != this.jfaFboA ? this.jfaFboA : this.jfaFboB);
            GlStateManager._glBindFramebuffer(36160, l);
            this.runJfa();
            super.post(buffercollector, renderapi$rendercontext);
            GlStateManager._activeTexture(33985);
            GlStateManager._bindTexture(0);
            GlStateManager._activeTexture(33984);
            GlStateManager._glBindFramebuffer(36160, this.theFb.fbId);
            GlStateManager._clear(256);
        }

        GlStateManager._glBindFramebuffer(36160, 0);
    }

    @Override
    public GlBufferMeshBuilder getBuffer(BufferCollector buffercollector, int i) {
        if (this.wireLayer == null) {
            this.wireLayer = new RenderLayer(
                VertexFormats.POSITION_COLOR_UV,
                4,
                true,
                new StateApplier[]{
                    StateApplier.framebufferState(this.theFb.fbId),
                    StateApplier.shaderState(InternalShaders.POS_COLOR_UV),
                    StateApplier.flagState(2884, true, true),
                    StateApplier.flagState(2929, true, true),
                    StateApplier.blendState(true, true),
                    StateApplier.depthFuncState(515, 515),
                    StateApplier.apply(renderpass -> GL11.glPolygonMode(1032, 6913)),
                    StateApplier.apply(renderpass -> {
                        GL13.glActiveTexture(33984);
                        GL11.glBindTexture(3553, this.whiteTexture);
                        RLUniforms.get(InternalShaders.POS_COLOR_UV.glId()).set1i("texSampler", 0);
                    })
                }
            );
        }

        return buffercollector.getBuffer(this.wireLayer);
    }

    @Override
    public void initializeIfNeeded() {
        if (initialized) {
            return;
        }

        super.initializeIfNeeded();
        this.depthRenderbuffer = GL30.glGenRenderbuffers();
        GL30.glBindRenderbuffer(36161, this.depthRenderbuffer);
        GL30.glRenderbufferStorage(36161, 33190, fbWidth, fbHeight);
        GL30.glBindFramebuffer(36160, this.theFb.fbId);
        GL30.glFramebufferRenderbuffer(36160, 36096, 36161, this.depthRenderbuffer);
        GL30.glBindFramebuffer(36160, 0);
        this.whiteTexture = GL11.glGenTextures();
        GL11.glBindTexture(3553, this.whiteTexture);
        ByteBuffer bytebuffer = BufferUtils.createByteBuffer(4);
        bytebuffer.put((byte)-1).put((byte)-1).put((byte)-1).put((byte)-1).flip();
        GL11.glTexImage2D(3553, 0, 6408, 1, 1, 0, 6408, 5121, bytebuffer);
        GL11.glTexParameteri(3553, 10241, 9728);
        GL11.glTexParameteri(3553, 10240, 9728);
        VoilClient.mc.send(() -> {
            this.seedProgram = ShaderManager.loadShader(JFA_VERT_PATH, RLPath.of("assets/tulip", "shaders/jfa/jfa_seed.fsh"), false);
            this.stepProgram = ShaderManager.loadShader(JFA_VERT_PATH, RLPath.of("assets/tulip", "shaders/jfa/jfa_step.fsh"), false);
            if (this.seedProgram == null || this.stepProgram == null) {
                return;
            }

            this.quadVao = GL30.glGenVertexArrays();
            this.createTargets();
        });
    }

    @Override
    public void resizeIfNeeded() {
        int i = fbWidth;
        int j = fbHeight;
        super.resizeIfNeeded();
        if (fbWidth == i && fbHeight == j) {
            return;
        }

        if (this.depthRenderbuffer != -1) {
            GL30.glBindRenderbuffer(36161, this.depthRenderbuffer);
            GL30.glRenderbufferStorage(36161, 33190, fbWidth, fbHeight);
        }

        if (this.jfaTexA == -1) {
            return;
        }

        resizeTexture(this.jfaTexA, fbWidth, fbHeight);
        resizeTexture(this.jfaTexB, fbWidth, fbHeight);
    }

    @Override
    protected void applyUniforms(RLUniforms rluniforms) {
        Color color = Color.white;
        Color color1 = Color.white;
        Color color2 = Color.white;
        Color color3 = Color.white;
        rluniforms.set3f("color", color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F);
        rluniforms.set3f("color2", color1.getRed() / 255.0F, color1.getGreen() / 255.0F, color1.getBlue() / 255.0F);
        rluniforms.set3f("color3", color2.getRed() / 255.0F, color2.getGreen() / 255.0F, color2.getBlue() / 255.0F);
        rluniforms.set3f("color4", color3.getRed() / 255.0F, color3.getGreen() / 255.0F, color3.getBlue() / 255.0F);
        rluniforms.set1f("time", (float)(System.currentTimeMillis() % TIME_WRAP_MS) / 1000.0F);
        rluniforms.set1f("speed", ESP.speed.getFloat() * 0.6F);
        rluniforms.set1f("wireAlpha", ESP.alpha.getFloat() / 255.0F);
        rluniforms.set1f("thickness", 0.0F);
        rluniforms.set1f("glowIntensity", ESP.glowIntensity.getFloat());
        rluniforms.set1f("glowRadius", ESP.glowRadius.getFloat() * 3.0F);
        rluniforms.set1i("texSampler", 0);
        rluniforms.set1i("jfaTex", 1);
    }

    @Override
    public PostPipeline getPipeline() {
        return new PostPipeline().addPass(this.getProgram(), renderpass -> {
            GlStateManager._activeTexture(33984);
            GlStateManager._bindTexture(this.colTex.tex);
            GlStateManager._activeTexture(33985);
            GlStateManager._bindTexture(this.jfaResultTex);
            GlStateManager._activeTexture(33984);
            this.applyUniforms();
        }, RenderAPI.getMinecraftFramebuffer());
    }
}
