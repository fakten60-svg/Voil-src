package lol.lwes.voil.util.render;

import dev.prestigeclient.renderlib.impl.render.mgr.RenderPass;
import dev.prestigeclient.renderlib.impl.render.state.StateApplier;
import dev.prestigeclient.renderlib.impl.render.ui.RectInstancer;
import dev.prestigeclient.renderlib.impl.render.ui.UiRenderer;
import dev.prestigeclient.renderlib.impl.render.ui.built.DefaultPreBuiltVertices;
import dev.prestigeclient.renderlib.impl.render.vertex.VertexFormat;
import java.io.PrintStream;
import java.util.List;
import java.util.function.IntSupplier;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.util.render.shader.ShaderRenderer;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL41;

public class BlurCompositeRenderer extends ShaderRenderer {
    private static final RectInstancer rectInstancer = new RectInstancer(null, DefaultPreBuiltVertices.SQUARE.getVAOIndexMesh(), (object, list) -> {
        RectInstanceData rectinstancedata = (RectInstanceData)object;
        list.get(0).feed(rectinstancedata.cornerRadii());
        list.get(1).feed(rectinstancedata.bounds());
        list.get(2).feed(rectinstancedata.colorStart());
        list.get(3).feed(rectinstancedata.colorEnd());
    }, new String[]{"CornerRadii", "Rect", "Params", "Tint"});
    private final IntSupplier blurTextureSupplier;
    private final StateApplier stateApplier;
    private int programId = -1;
    private int blurSamplerLocation = -1;
    private int inputResolutionLocation = -1;

    public BlurCompositeRenderer(IntSupplier intsupplier) {
        this.blurTextureSupplier = intsupplier;
        this.stateApplier = StateApplier.apply(this::bindBlurTexture);

        try {
            VertexFormat vertexformat = RenderManager.posFormat;
            BlurCompositeRenderer blurcompositerenderer1 = this;
            blurcompositerenderer1.vertexFormat = vertexformat;
            VertexFormat vertexformat1 = vertexformat;
            this.shader = ShaderRenderer.loadShaderForFormat("shaders/post/blur_composite_instanced.fsh", vertexformat1);
        } catch (Exception exception) {
            PrintStream printstream = System.err;
            String s1 = exception.getMessage();
            printstream.println("Failed to load instanced blur shader: " + s1);
        }
    }

    private void bindBlurTexture(RenderPass renderpass) {
        int i = this.blurTextureSupplier.getAsInt();
        if (i > 0) {
            int j = renderpass.shaderProgram.glId();
            GL41.glActiveTexture(33988);
            GL41.glBindTexture(3553, i);
            GL41.glBindSampler(4, 0);
            GL41.glActiveTexture(33984);
            if (j != this.programId) {
                this.programId = j;
                this.blurSamplerLocation = GL41.glGetUniformLocation(j, "BlurSampler");
                this.inputResolutionLocation = GL41.glGetUniformLocation(j, "InputResolution");
                GL41.glProgramUniform1i(j, this.blurSamplerLocation, 4);
            }

            GL41.glProgramUniform2f(
                j, this.inputResolutionLocation, VoilClient.mc.getWindow().getFramebufferWidth(), VoilClient.mc.getWindow().getFramebufferHeight()
            );
        }
    }

    public void render(
        float f, float f1, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9, float f10, float f11, float f12, float f13
    ) {
        if (this.shader != null) {
            float f14 = ShaderRenderer.getScale();
            float f15 = GuiScale.getScaledHeight();
            UiRenderer uirenderer = RenderManager.uiRenderer;
            RectInstancer rectinstancer1 = rectInstancer;
            StateApplier[] astateapplier = new StateApplier[]{this.stateApplier};
            RectInstancer rectinstancer = rectinstancer1;
            uirenderer.addRectangle(
                this.getInstancerCached(rectinstancer, astateapplier),
                f - 10.0F,
                f1 - 10.0F,
                f2 + 20.0F,
                f3 + 20.0F,
                -1,
                RenderManager.getScissorRect(),
                new RectInstanceData(
                    new Vector4f(f4 * f14, f5 * f14, f6 * f14, f7 * f14),
                    new Vector4f(f * f14, (f15 - f1 - f3) * f14, f2 * f14, f3 * f14),
                    new Vector4f(f8, f9, f10, 2.0F),
                    new Vector4f(f11, f12, f13, 0.0F)
                )
            );
        }
    }
}
