package lol.lwes.voil.util.render;

import dev.prestigeclient.renderlib.api.uniform.RLUniforms;
import dev.prestigeclient.renderlib.impl.render.layer.type.RenderLayer;
import dev.prestigeclient.renderlib.impl.render.mgr.RenderPass;
import dev.prestigeclient.renderlib.impl.render.resource.GlTexture;
import dev.prestigeclient.renderlib.impl.render.state.StateApplier;
import dev.prestigeclient.renderlib.impl.render.ui.elements.PreBuiltTextureRectangleElement;
import dev.prestigeclient.renderlib.impl.render.vertex.VertexFormat;
import dev.prestigeclient.renderlib.impl.render.vertex.VertexFormats;
import java.awt.Color;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;
import lol.lwes.voil.util.render.shader.ShaderRenderer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

public class TextureRectangleRenderer extends ShaderRenderer {
    private final Map<GlTexture, Map<ScissorRectData, RenderLayer>> layerCache = new WeakHashMap<>();

    public TextureRectangleRenderer() {
        try {
            VertexFormat vertexformat = VertexFormats.POSITION_UV;
            TextureRectangleRenderer texturerectanglerenderer1 = this;
            texturerectanglerenderer1.vertexFormat = vertexformat;
            VertexFormat vertexformat1 = vertexformat;
            this.shader = ShaderRenderer.loadShaderForFormat("shaders/core/texture_rectangle.fsh", vertexformat1);
        } catch (Exception exception) {
            PrintStream printstream = System.err;
            String s1 = exception.getMessage();
            printstream.println("Failed to load texture rectangle shader: " + s1);
        }
    }

    public void renderAlt(GlTexture gltexture, float f, float f1, float f2, float f3, float f4, float f5, Color color) {
        if (this.shader != null) {
            if (gltexture != null) {
                float f6 = ShaderRenderer.getScale();
                float f7 = Math.max(1.0F, f4);
                float[] afloat = new float[]{f4 * f6, f4 * f6, f4 * f6, f4 * f6};
                float f8 = f5 * f6;
                float[] afloat1 = afloat;
                float f9 = f3;
                float f10 = f2;
                float f11 = f1;
                float f12 = f;
                GlTexture gltexture1 = gltexture;
                RenderManager.uiRenderer
                    .addElement(
                        new PreBuiltTextureRectangleElement(
                            this.getBuffer(gltexture1, f12, f11, f10, f9, afloat1, f8),
                            f - f7,
                            f1 - f7,
                            f2 + f7 * 2.0F,
                            f3 + f7 * 2.0F,
                            color.getRGB(),
                            0.0F,
                            0.0F,
                            1.0F,
                            1.0F
                        )
                    );
            }
        }
    }

    public void render(GlTexture gltexture, float f, float f1, float f2, float f3, float f4, float f5) {
        float[] afloat1 = new float[]{f4, f4, f4, f4};
        Color color = Color.WHITE;
        float f6 = f5;
        float[] afloat = afloat1;
        float f7 = f3;
        float f8 = f2;
        float f9 = f1;
        float f10 = f;
        GlTexture gltexture1 = gltexture;
        this.render(gltexture1, f10, f9, f8, f7, afloat, f6, color);
    }

    public void render(GlTexture gltexture, float f, float f1, float f2, float f3, float[] afloat, float f4) {
        Color color = Color.WHITE;
        float f5 = f4;
        float[] afloat1 = afloat;
        float f6 = f3;
        float f7 = f2;
        float f8 = f1;
        float f9 = f;
        GlTexture gltexture1 = gltexture;
        this.render(gltexture1, f9, f8, f7, f6, afloat1, f5, color);
    }

    public void render(GlTexture gltexture, float f, float f1, float f2, float f3, float[] afloat, float f4, Color color) {
        float f14 = f4;
        f4 = f3;
        if (this.shader != null) {
            if (gltexture != null) {
                if (afloat != null && afloat.length == 4) {
                    float f5 = ShaderRenderer.getScale();
                    float f6 = Math.max(Math.max(afloat[0], afloat[1]), Math.max(afloat[2], afloat[3]));
                    float f7 = Math.max(1.0F, f6);
                    float f8 = Math.round(f);
                    float f9 = Math.round(f1);
                    float f10 = f8 - f7;
                    float f11 = f9 - f7;
                    float f12 = f8 + f2 + f7;
                    float f13 = f9 + f4 + f7;
                    float[] afloat1 = new float[]{afloat[0] * f5, afloat[1] * f5, afloat[2] * f5, afloat[3] * f5};
                    f3 = f14 * f5;
                    int i = color.getRGB();
                    float uvScaleX = (f12 - f10) / f2;
                    float uvScaleY = (f13 - f11) / f4;
                    RenderManager.uiRenderer
                        .addElement(
                            new PreBuiltTextureRectangleElement(
                                this.getBuffer(gltexture, f8, f9, f2, f4, afloat1, f3),
                                f10,
                                f11,
                                f12 - f10,
                                f13 - f11,
                                i,
                                -f7 / f2,
                                -f7 / f4,
                                uvScaleX,
                                uvScaleY
                            )
                        );
                } else {
                    throw new IllegalArgumentException("radii must be a float[4] of {topLeft, topRight, bottomLeft, bottomRight}");
                }
            }
        }
    }

    public void render(GlTexture gltexture, float f, float f1, float f2, float f3, float f4, float f5, Color color) {
        float[] afloat = new float[]{f4, f4, f4, f4};
        float f6 = f3;
        float f7 = f2;
        float f8 = f1;
        float f9 = f;
        GlTexture gltexture1 = gltexture;
        this.render(gltexture1, f9, f8, f7, f6, afloat, f5, color);
    }

    protected RenderLayer getBuffer(GlTexture gltexture, float f, float f1, float f2, float f3, float[] afloat, float f4) {
        ScissorRectData scissorrectdata = new ScissorRectData(
            afloat,
            f4,
            f * ShaderRenderer.getScale(),
            -f1 * ShaderRenderer.getScale() + GuiScale.getScaledHeight() * ShaderRenderer.getScale() - f3 * ShaderRenderer.getScale(),
            f2 * ShaderRenderer.getScale(),
            f3 * ShaderRenderer.getScale(),
            StateApplier.scissorState(
                RenderManager.scissorEnabled,
                !RenderManager.scissorEnabled,
                RenderManager.scissorBox[0],
                RenderManager.scissorBox[1],
                RenderManager.scissorBox[2],
                RenderManager.scissorBox[3]
            )
        );
        Map<ScissorRectData, RenderLayer> map = this.layerCache.computeIfAbsent(gltexture, gltexture1 -> new HashMap<>());
        Function<ScissorRectData, RenderLayer> function = scissorrectdata1 -> new RenderLayer(
            this.vertexFormat,
            4,
            false,
            new StateApplier[]{
                StateApplier.framebufferState(RenderManager.getCurrentFramebuffer()),
                StateApplier.shaderState(this.shader),
                StateApplier.flagState(2884, false),
                StateApplier.blendState(true, true),
                StateApplier.depthFuncState(515, 515),
                scissorrectdata.scissor(),
                StateApplier.apply(
                    renderpass -> {
                        GL13.glActiveTexture(33984);
                        GL11.glBindTexture(3553, gltexture.tex);
                        float[] afloatx = scissorrectdata.cornerRadii();
                        RLUniforms.get(renderpass.shaderProgram.glId())
                            .set1i("texSampler", 0)
                            .set4f("radius", afloatx[0], afloatx[1], afloatx[2], afloatx[3])
                            .set1f("smoothness", scissorrectdata.x())
                            .set2f("uLocation", scissorrectdata.y(), scissorrectdata.width())
                            .set2f("uSize", scissorrectdata.height(), scissorrectdata.radius())
                            .set4f("ColorModulator", 1.0F, 1.0F, 1.0F, 1.0F);
                    }
                )
            }
        );
        return map.computeIfAbsent(scissorrectdata, function);
    }
}
