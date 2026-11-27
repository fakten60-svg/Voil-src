package dev.prestigeclient.renderlib.api.shader;

import dev.prestigeclient.renderlib.api.path.RLPath;
import dev.prestigeclient.renderlib.api.uniform.RLUniforms;
import dev.prestigeclient.renderlib.impl.api.DrawType;
import dev.prestigeclient.renderlib.impl.api.RenderAPI;
import dev.prestigeclient.renderlib.impl.render.PostPipeline;
import dev.prestigeclient.renderlib.impl.render.layer.type.BufferCollector;
import dev.prestigeclient.renderlib.impl.render.layer.type.RenderLayer;
import dev.prestigeclient.renderlib.impl.render.mesh.GlBufferMeshBuilder;
import dev.prestigeclient.renderlib.impl.render.resource.GlFramebuffer;
import dev.prestigeclient.renderlib.impl.render.resource.GlTexture;
import it.unimi.dsi.fastutil.Pair;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL33C;

public abstract class RLEntityOutlineShader extends RLShader {
    protected static PostPipeline pipeline;
    protected static int fbWidth = -1;
    protected static int fbHeight = -1;
    protected static boolean initialized = false;
    public GlFramebuffer theFb = GlFramebuffer.create();
    public GlTexture colTex = GlTexture.create(3553);
    protected static final Map<Integer, RenderLayer> outlineCache = new HashMap<>();
    private Consumer<RLUniforms> overrideUniforms;
    private static int maskSampler = 0;

    public RLEntityOutlineShader(RLPath rlpath, RLPath rlpath1) {
        super(rlpath, rlpath1, false);
    }

    public void post(BufferCollector buffercollector, RenderAPI.RenderContext rendercontext) {
        if (initialized && pipeline != null) {
            boolean flag = GL11.glGetBoolean(2884);
            boolean flag1 = GL11.glGetBoolean(3042);
            boolean flag2 = GL11.glGetBoolean(2929);
            GL11.glDisable(2884);
            GL11.glEnable(3042);
            GL11.glBlendFunc(770, 771);
            GL11.glDisable(2929);
            pipeline.draw();
            if (flag2) {
                GL11.glEnable(2929);
            }

            if (flag) {
                GL11.glEnable(2884);
            }

            if (!flag1) {
                GL11.glDisable(3042);
            }
        }
    }

    public abstract GlBufferMeshBuilder getBuffer(BufferCollector buffercollector, int i);

    public static void genIndices(BufferCollector buffercollector) {
        buffercollector.getAllActiveBuffers()
            .filter(pair -> pair.key().isOutline())
            .forEach(pair -> pair.value().generateIndices(DrawType.QUADS));
    }

    public void initializeIfNeeded() {
        if (!initialized) {
            MinecraftClient minecraftclient = MinecraftClient.getInstance();
            fbWidth = minecraftclient.getWindow().getFramebufferWidth();
            fbHeight = minecraftclient.getWindow().getFramebufferHeight();
            this.colTex.init(32856, fbWidth, fbHeight);
            this.colTex.setFilter(9729);
            this.colTex.setEdgeMode(33071);
            this.theFb.bindTexture(36064, this.colTex);
            pipeline = this.getPipeline();
            initialized = true;
        }
    }

    protected static void bindMaskSampler() {
        if (maskSampler == 0) {
            maskSampler = GL33C.glGenSamplers();
            GL33C.glSamplerParameteri(maskSampler, 10241, 9728);
            GL33C.glSamplerParameteri(maskSampler, 10240, 9728);
            GL33C.glSamplerParameteri(maskSampler, 10242, 33071);
            GL33C.glSamplerParameteri(maskSampler, 10243, 33071);
        }

        GL33C.glBindSampler(0, maskSampler);
    }

    public void resizeIfNeeded() {
        MinecraftClient minecraftclient = MinecraftClient.getInstance();
        if (minecraftclient.getWindow() != null) {
            int i = minecraftclient.getWindow().getFramebufferWidth();
            int j = minecraftclient.getWindow().getFramebufferHeight();
            if (i > 0 && j > 0) {
                if (fbWidth != i || fbHeight != j) {
                    fbWidth = i;
                    fbHeight = j;
                    this.colTex = this.colTex.closeAndReinit(fbWidth, fbHeight);
                    this.colTex.setFilter(9729);
                    this.colTex.setEdgeMode(33071);
                    this.theFb.bindTexture(36064, this.colTex);
                }
            }
        }
    }

    public void resizeOrInit() {
        this.initializeIfNeeded();
        this.resizeIfNeeded();
    }

    protected final void applyUniforms() {
        RLUniforms rluniforms = RLUniforms.get(this.getProgram().glId());
        if (this.overrideUniforms != null) {
            this.overrideUniforms.accept(rluniforms);
        } else {
            this.applyUniforms(rluniforms);
        }
    }

    protected abstract void applyUniforms(RLUniforms rluniforms);

    public abstract PostPipeline getPipeline();

    public final void setUniformOverride(Consumer<RLUniforms> consumer) {
        this.overrideUniforms = consumer;
    }
}
