package lol.lwes.voil.util.render;

import dev.prestigeclient.renderlib.api.uniform.RLUniforms;
import dev.prestigeclient.renderlib.impl.render.vertex.VertexFormat;
import dev.prestigeclient.renderlib.impl.render.vertex.VertexFormats;
import java.awt.Color;
import java.io.PrintStream;
import java.util.function.Consumer;
import lol.lwes.voil.util.render.shader.ShaderRenderer;
import org.lwjgl.opengl.GL20;

public class VariableGlowRenderer extends ShaderRenderer {
    private float pixelScale = 0.0F;

    public VariableGlowRenderer() {
        try {
            VertexFormat vertexformat = VertexFormats.POSITION;
            VariableGlowRenderer variableglowrenderer1 = this;
            variableglowrenderer1.vertexFormat = vertexformat;
            VertexFormat vertexformat1 = vertexformat;
            this.shader = ShaderRenderer.loadShaderForFormat("shaders/core/variableglow.fsh", vertexformat1);
        } catch (Exception exception) {
            PrintStream printstream = System.err;
            String s1 = exception.getMessage();
            printstream.println("Failed to load variable glow shader: " + s1);
        }
    }

    public void render(float f, float f1, float f2, float f3, float f4, float f5, float f6, Color color) {
        this.render(f, f1, f2, f3, f4, f4, f4, f4, f5, f6, color, color, color, color);
    }

    public void render(
        float f,
        float f1,
        float f2,
        float f3,
        float f4,
        float f5,
        float f6,
        float f7,
        float f8,
        float f9,
        Color color,
        Color color1,
        Color color2,
        Color color3
    ) {
        if (this.shader != null) {
            float f10 = ShaderRenderer.getScale();
            float f11 = Math.max(Math.max(f4, f5), Math.max(f6, f7)) + f8 + 30.0F;
            this.pixelScale += 0.005F;
            Consumer<RLUniforms> consumer = rluniforms -> {
                rluniforms.set1i("useVariableWidth", 0);
                rluniforms.set4f("cornerRadii", f4 * f10, f5 * f10, f6 * f10, f7 * f10);
                rluniforms.set1f("smoothness", 2.0F);
                rluniforms.set2f("uLocation", f * f10, -f1 * f10 + GuiScale.getScaledHeight() * f10 - f3 * f10);
                rluniforms.set2f("uSize", f2 * f10, f3 * f10);
                rluniforms.set2f("InputResolution", GuiScale.getScaledWidth() * f10, GuiScale.getScaledHeight() * f10);
                rluniforms.set1f("GlowWidth", f8 * f10);
                rluniforms.set1f("GlowIntensity", f9);
                rluniforms.set4f("GlowColor1", color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, color.getAlpha() / 255.0F);
                rluniforms.set4f("GlowColor2", color1.getRed() / 255.0F, color1.getGreen() / 255.0F, color1.getBlue() / 255.0F, color1.getAlpha() / 255.0F);
                rluniforms.set4f("GlowColor3", color2.getRed() / 255.0F, color2.getGreen() / 255.0F, color2.getBlue() / 255.0F, color2.getAlpha() / 255.0F);
                rluniforms.set4f("GlowColor4", color3.getRed() / 255.0F, color3.getGreen() / 255.0F, color3.getBlue() / 255.0F, color3.getAlpha() / 255.0F);
                rluniforms.set1f("Time", this.pixelScale);
            };
            float f12 = f11;
            float f13 = f3;
            float f14 = f2;
            float f15 = f1;
            float f16 = f;
            this.drawRectExpanded(f16, f15, f14, f13, 0, f12, consumer);
        }
    }

    public void renderVariableWidth(
        float f,
        float f1,
        float[] afloat,
        float[] afloat1,
        float f2,
        float f3,
        float f4,
        float f5,
        float f6,
        float f7,
        Color color,
        Color color1,
        Color color2,
        Color color3
    ) {
        if (this.shader != null) {
            if (afloat != null && afloat1 != null && afloat.length == afloat1.length) {
                float f8 = ShaderRenderer.getScale();
                float f9 = 0.0F;

                for (float f11 : afloat) {
                    f9 += f11;
                }

                float f10 = Math.max(Math.max(f2, f3), Math.max(f4, f5));
                float f19 = f10 + f6 + 30.0F;
                float[] afloat2 = new float[32];
                float[] afloat3 = new float[32];
                int i = Math.min(afloat.length, 32);

                for (int j = 0; j < i; j++) {
                    afloat2[j] = afloat[j] * f8;
                    afloat3[j] = afloat1[j] * f8;
                }

                this.pixelScale += 0.005F;
                float f12 = 0.0F;

                for (float f13 : afloat1) {
                    f12 = Math.max(f12, f13);
                }

                float f20 = (int)f19;
                float locationX = f * f8;
                float locationY = -f1 * f8 + GuiScale.getScaledHeight() * f8 - f9 * f8;
                float width = afloat1[0] * f8;
                float height = f9 * f8;
                Consumer<RLUniforms> consumer = rluniforms -> {
                    rluniforms.set1i("useVariableWidth", 1);
                    rluniforms.set1i("numSegments", i);
                    GL20.glUniform1fv(this.getUniformLocation("segmentHeights"), afloat2);
                    GL20.glUniform1fv(this.getUniformLocation("segmentWidths"), afloat3);
                    rluniforms.set4f("cornerRadii", f2 * f8, f3 * f8, f4 * f8, f5 * f8);
                    rluniforms.set1f("smoothness", 2.0F);
                    rluniforms.set2f("uLocation", locationX, locationY);
                    rluniforms.set2f("uSize", width, height);
                    rluniforms.set2f("InputResolution", GuiScale.getScaledWidth() * f8, GuiScale.getScaledHeight() * f8);
                    rluniforms.set1f("GlowWidth", f6 * f8);
                    rluniforms.set1f("GlowIntensity", f7);
                    rluniforms.set4f("GlowColor1", color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, color.getAlpha() / 255.0F);
                    rluniforms.set4f("GlowColor2", color1.getRed() / 255.0F, color1.getGreen() / 255.0F, color1.getBlue() / 255.0F, color1.getAlpha() / 255.0F);
                    rluniforms.set4f("GlowColor3", color2.getRed() / 255.0F, color2.getGreen() / 255.0F, color2.getBlue() / 255.0F, color2.getAlpha() / 255.0F);
                    rluniforms.set4f("GlowColor4", color3.getRed() / 255.0F, color3.getGreen() / 255.0F, color3.getBlue() / 255.0F, color3.getAlpha() / 255.0F);
                    rluniforms.set1f("Time", this.pixelScale);
                };
                float f14 = f20;
                float f15 = f9;
                float f16 = f12;
                float f17 = f1;
                float f18 = f;
                this.drawRectExpanded(f18, f17, f16, f15, 0, f14, consumer);
            }
        }
    }

    public void render(float f, float f1, float f2, float f3, float f4, float f5, float f6, Color color, Color color1, Color color2, Color color3) {
        this.render(f, f1, f2, f3, f4, f4, f4, f4, f5, f6, color, color1, color2, color3);
    }
}
