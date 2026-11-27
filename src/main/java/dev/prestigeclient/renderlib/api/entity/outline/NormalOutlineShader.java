package dev.prestigeclient.renderlib.api.entity.outline;

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
import dev.prestigeclient.renderlib.impl.render.state.StateApplier;
import dev.prestigeclient.renderlib.impl.render.vertex.VertexFormats;
import java.awt.Color;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

public class NormalOutlineShader extends RLEntityOutlineShader {
    private final Color color = Color.WHITE;

    public NormalOutlineShader() {
        super(RLPath.of("shaders/post.vert"), RLPath.of("shaders/outline.frag"));
    }

    @Override
    public GlBufferMeshBuilder getBuffer(BufferCollector buffercollector, int i) {
        return buffercollector.getBuffer(
            outlineCache.computeIfAbsent(
                i,
                integer -> new RenderLayer(
                    VertexFormats.POSITION_COLOR_UV,
                    4,
                    true,
                    new StateApplier[]{
                        StateApplier.framebufferState(this.theFb.fbId),
                        StateApplier.shaderState(InternalShaders.POS_COLOR_UV),
                        StateApplier.flagState(2884, false, true),
                        StateApplier.blendState(true, true),
                        StateApplier.depthFuncState(519, 515),
                        StateApplier.apply(renderpass -> {
                            GL13.glActiveTexture(33984);
                            GL11.glBindTexture(3553, i);
                            bindMaskSampler();
                            RLUniforms.get(InternalShaders.POS_COLOR_UV.glId()).set1i("texSampler", 0);
                        })
                    }
                )
            )
        );
    }

    @Override
    public void applyUniforms(RLUniforms rluniforms) {
        rluniforms.set4f("color", this.color.getRed() / 255.0F, this.color.getGreen() / 255.0F, this.color.getBlue() / 255.0F, this.color.getAlpha() / 255.0F);
    }

    @Override
    public PostPipeline getPipeline() {
        return new PostPipeline().addPass(this.getProgram(), renderpass -> {
            GL13.glActiveTexture(33984);
            GL11.glBindTexture(3553, this.colTex.tex);
            this.applyUniforms();
        }, RenderAPI.getMinecraftFramebuffer());
    }
}
