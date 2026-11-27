package lol.lwes.voil.util.render;

import dev.prestigeclient.renderlib.api.uniform.RLUniforms;
import dev.prestigeclient.renderlib.impl.render.vertex.VertexFormat;
import dev.prestigeclient.renderlib.impl.render.vertex.VertexFormats;
import java.awt.Color;
import java.io.PrintStream;
import java.util.function.Consumer;
import lol.lwes.voil.util.render.shader.ShaderRenderer;

public class StarShaderRenderer extends ShaderRenderer {
    public StarShaderRenderer() {
        try {
            VertexFormat vertexformat = VertexFormats.POSITION;
            StarShaderRenderer starshaderrenderer1 = this;
            starshaderrenderer1.vertexFormat = vertexformat;
            VertexFormat vertexformat1 = vertexformat;
            this.shader = ShaderRenderer.loadShaderForFormat("shaders/core/star.fsh", vertexformat1);
        } catch (Exception exception) {
            PrintStream printstream = System.err;
            String s1 = exception.getMessage();
            printstream.println("Failed to load star shader: " + s1);
        }
    }

    public void render(float f, float f1, float f2, Color color, Color color1, float f3) {
        this.render(f, f1, f2, color, color1, f3, 0.0F, 0.45F, 0.0F);
    }

    public void render(float f, float f1, float f2, Color color, Color color1, float f3, float f4, float f5) {
        this.render(f, f1, f2, color, color1, f3, f4, f5, 0.0F);
    }

    public void render(float f, float f1, float f2, Color color, Color color1, float f3, float f4, float f5, float f6) {
        if (this.shader != null) {
            float f7 = ShaderRenderer.getScale();
            float f8 = f3 + 6.0F;
            boolean flag = color != null;
            boolean flag1 = color1 != null && f3 > 0.0F;
            Consumer<RLUniforms> consumer = rluniforms -> {
                float f8x = f * f7;
                float f9x = -f1 * f7 + GuiScale.getScaledHeight() * f7 - f2 * f7;
                rluniforms.set2f("uLocation", f8x, f9x);
                rluniforms.set2f("uSize", f2 * f7, f2 * f7);
                rluniforms.set1f("starRadius", f2 * 0.5F * f7);
                rluniforms.set1f("outlineThickness", f3 * f7);
                rluniforms.set1f("rotation", f4);
                rluniforms.set1f("pointiness", f5);
                rluniforms.set1f("cornerRadius", f6 * f7);
                rluniforms.set1f("filled", flag ? 1.0F : 0.0F);
                rluniforms.set1f("outlined", flag1 ? 1.0F : 0.0F);
                rluniforms.set1f("smoothness", 1.0F);
                if (flag) {
                    rluniforms.set4f("fillColor", color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, color.getAlpha() / 255.0F);
                } else {
                    rluniforms.set4f("fillColor", 0.0F, 0.0F, 0.0F, 0.0F);
                }

                if (flag1) {
                    rluniforms.set4f(
                        "outlineColor", color1.getRed() / 255.0F, color1.getGreen() / 255.0F, color1.getBlue() / 255.0F, color1.getAlpha() / 255.0F
                    );
                } else {
                    rluniforms.set4f("outlineColor", 0.0F, 0.0F, 0.0F, 0.0F);
                }
            };
            float f9 = f8;
            float f10 = f2;
            float f11 = f2;
            float f12 = f1;
            float f13 = f;
            this.drawRectExpanded(f13, f12, f11, f10, -1, f9, consumer);
        }
    }

    public void render(float f, float f1, float f2, Color color, Color color1, float f3, float f4) {
        this.render(f, f1, f2, color, color1, f3, f4, 0.45F, 0.0F);
    }
}
