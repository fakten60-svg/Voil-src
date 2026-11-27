package dev.prestigeclient.renderlib.impl.render.mgr;

import dev.prestigeclient.renderlib.api.uniform.RLUniforms;
import dev.prestigeclient.renderlib.impl.api.RenderAPI;
import dev.prestigeclient.renderlib.impl.render.Util;
import dev.prestigeclient.renderlib.impl.render.resource.GlBuffer;
import dev.prestigeclient.renderlib.impl.render.resource.ShaderProgram;
import dev.prestigeclient.renderlib.impl.render.resource.VertexArrayBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.function.Consumer;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL33;

public class RenderPass {
    public final Integer targetFramebuffer;
    public final OptionalInt clearColor;
    public final OptionalDouble clearDepth;
    public final ShaderProgram shaderProgram;
    private final int[] viewport;
    private final float[] clearColorF;
    private final List<int[]> sources;
    private final Consumer uniforms;
    private static int fullscreenVao = 0;
    private static final float[] MATRIX_SCRATCH = new float[16];

    private RenderPass(RenderPass.RenderPassBuilder renderpass$renderpassbuilder) {
        this.targetFramebuffer = Objects.requireNonNull(renderpass$renderpassbuilder.targetFramebuffer, "targetFramebuffer cannot be null");
        this.clearColor = Objects.requireNonNull(renderpass$renderpassbuilder.clearColor, "clearColor cannot be null");
        this.clearDepth = Objects.requireNonNull(renderpass$renderpassbuilder.clearDepth, "clearDepth cannot be null");
        this.shaderProgram = Objects.requireNonNull(renderpass$renderpassbuilder.shaderProgram, "shaderProgram cannot be null");
        this.viewport = renderpass$renderpassbuilder.viewport;
        this.clearColorF = renderpass$renderpassbuilder.clearColorF;
        this.sources = renderpass$renderpassbuilder.sources;
        this.uniforms = renderpass$renderpassbuilder.uniforms;
    }

    public void begin() {
        GL30.glBindFramebuffer(36160, this.targetFramebuffer);
        if (this.viewport != null) {
            GL30.glViewport(this.viewport[0], this.viewport[1], this.viewport[2], this.viewport[3]);
        }

        this.clearBuffersIfNeeded();
        GL30.glUseProgram(this.shaderProgram.glId());

        for (int[] aint : this.sources) {
            int i = aint[0];
            GL30.glActiveTexture(33984 + i);
            GL33.glBindSampler(aint[0], 0);
            GL30.glBindTexture(3553, aint[1]);
        }

        if (!this.sources.isEmpty()) {
            GL30.glActiveTexture(33984);
        }

        if (this.uniforms != null) {
            this.uniforms.accept(RLUniforms.get(this.shaderProgram.glId()));
        }
    }

    public static RenderPass.RenderPassBuilder builder() {
        return new RenderPass.RenderPassBuilder();
    }

    public void draw(VertexArrayBuffer vertexarraybuffer, GlBuffer glbuffer, int i, int j) {
        GL30.glBindVertexArray(vertexarraybuffer.vao());
        GL30.glBindBuffer(34963, glbuffer.getId());
        GL30.glDrawElements(i, j, 5125, 0L);
        GL30.glBindVertexArray(0);
        GL30.glBindBuffer(34963, 0);
    }

    private void clearBuffersIfNeeded() {
        short short1 = 0;
        if (this.clearColorF != null) {
            GL30.glClearColor(this.clearColorF[0], this.clearColorF[1], this.clearColorF[2], this.clearColorF[3]);
            short1 = 16384;
        } else if (this.clearColor.isPresent()) {
            int i = this.clearColor.getAsInt();
            GL30.glClearColor(Util.getRedFloat(i), Util.getGreenFloat(i), Util.getBlueFloat(i), Util.getAlphaFloat(i));
            short1 = 16384;
        }

        if (this.clearDepth.isPresent()) {
            GL30.glClearDepth(this.clearDepth.getAsDouble());
            short1 |= 256;
        }

        if (short1 != 0) {
            GL30.glDisable(3089);
            GL30.glDepthMask(true);
            GL30.glColorMask(true, true, true, true);
            GL30.glClear(short1);
        }
    }

    public static void setupPostState() {
        GL30.glDisable(2884);
        GL30.glDisable(2929);
        GL30.glDisable(3042);
        GL30.glDisable(3089);
    }

    public void drawFullscreen() {
        if (fullscreenVao == 0) {
            fullscreenVao = GL30.glGenVertexArrays();
        }

        GL30.glBindVertexArray(fullscreenVao);
        GL30.glDrawArrays(5, 0, 4);
        GL30.glBindVertexArray(0);
    }

    public void drawInstanced(VertexArrayBuffer vertexarraybuffer, GlBuffer glbuffer, int i, int j, int k) {
        GL30.glBindVertexArray(vertexarraybuffer.vao());
        GL30.glBindBuffer(34963, glbuffer.getId());
        GL31.glDrawElementsInstanced(i, j, 5125, 0L, k);
        GL30.glBindVertexArray(0);
        GL30.glBindBuffer(34963, 0);
    }

    public void setDefaultUniforms(RenderAPI.RenderContext renderapi$rendercontext) {
        RLUniforms rluniforms = RLUniforms.get(this.shaderProgram.glId());
        renderapi$rendercontext.projMatrix().get(MATRIX_SCRATCH);
        rluniforms.setMatrix4fv("projMat", false, MATRIX_SCRATCH);
        renderapi$rendercontext.modelView().get(MATRIX_SCRATCH);
        rluniforms.setMatrix4fv("viewMat", false, MATRIX_SCRATCH);
    }

    public void dispatchFullscreen() {
        this.begin();
        this.drawFullscreen();
    }

    public static class RenderPassBuilder {
        private Integer targetFramebuffer;
        private OptionalInt clearColor = OptionalInt.empty();
        private OptionalDouble clearDepth = OptionalDouble.empty();
        private ShaderProgram shaderProgram;
        private int[] viewport;
        private float[] clearColorF;
        private final List<int[]> sources = new ArrayList<>();
        private Consumer uniforms;

        public RenderPass.RenderPassBuilder target(int i, int j, int k) {
            this.targetFramebuffer = i;
            return this.viewport(0, 0, j, k);
        }

        public RenderPass.RenderPassBuilder clear() {
            return this.clearColor(0.0F, 0.0F, 0.0F, 0.0F);
        }

        public RenderPass.RenderPassBuilder source(int i, int j) {
            this.sources.add(new int[]{i, j});
            return this;
        }

        public RenderPass.RenderPassBuilder source(int i) {
            return this.source(0, i);
        }

        public RenderPass build() {
            return new RenderPass(this);
        }

        public RenderPass.RenderPassBuilder viewport(int i, int j, int k, int l) {
            this.viewport = new int[]{i, j, k, l};
            return this;
        }

        public RenderPass.RenderPassBuilder clearColor(OptionalInt optionalint) {
            this.clearColor = optionalint;
            return this;
        }

        public RenderPass.RenderPassBuilder clearColor(float f, float f1, float f2, float f3) {
            this.clearColorF = new float[]{f, f1, f2, f3};
            return this;
        }

        public RenderPass.RenderPassBuilder clearDepth(OptionalDouble optionaldouble) {
            this.clearDepth = optionaldouble;
            return this;
        }

        public RenderPass.RenderPassBuilder uniforms(Consumer<RLUniforms> consumer) {
            this.uniforms = consumer;
            return this;
        }

        public RenderPass.RenderPassBuilder shaderProgram(ShaderProgram shaderprogram) {
            this.shaderProgram = shaderprogram;
            return this;
        }

        public RenderPass.RenderPassBuilder targetFramebuffer(Integer integer) {
            this.targetFramebuffer = integer;
            return this;
        }
    }
}
