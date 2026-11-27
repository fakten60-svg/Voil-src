package lol.lwes.voil.util.render.shader;

import dev.prestigeclient.renderlib.api.uniform.RLUniforms;
import dev.prestigeclient.renderlib.impl.render.vertex.VertexFormat;
import dev.prestigeclient.renderlib.impl.render.vertex.VertexFormats;
import java.awt.Color;
import java.io.PrintStream;
import java.util.function.Consumer;
import lol.lwes.voil.util.render.GuiScale;

public class ArrowShaderRenderer extends ShaderRenderer {
    public ArrowShaderRenderer() {
        try {
            VertexFormat vertexformat = VertexFormats.POSITION;
            ArrowShaderRenderer arrowshaderrenderer1 = this;
            arrowshaderrenderer1.vertexFormat = vertexformat;
            VertexFormat vertexformat1 = vertexformat;
            this.shader = ShaderRenderer.loadShaderForFormat("shaders/core/arrow.fsh", vertexformat1);
        } catch (Exception exception) {
            PrintStream printstream = System.err;
            String s1 = exception.getMessage();
            printstream.println("Failed to load arrow shader: " + s1);
        }
    }

    public void render(float f, float f1, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9, Color color) {
        if (this.shader != null) {
            float f10 = ShaderRenderer.getScale();
            float f11 = f5 + f6 + 10.0F;
            int j = (int)f11;
            Consumer<RLUniforms> consumer = rluniforms -> {
                rluniforms.set1f("armLength", f4 * f10);
                rluniforms.set1f("radius", f5 * f10);
                rluniforms.set1f("cornerRadius", f6 * f10);
                rluniforms.set1f("angle", f7);
                rluniforms.set1f("rotation", f8);
                rluniforms.set1f("smoothness", f9 * f10);
                rluniforms.set2f("uLocation", f * f10, -f1 * f10 + GuiScale.getScaledHeight() * f10 - f3 * f10);
                rluniforms.set2f("uSize", f2 * f10, f3 * f10);
                rluniforms.set4f("uColor", color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, color.getAlpha() / 255.0F);
            };
            int i = j;
            float f12 = f3;
            float f13 = f2;
            float f14 = f1;
            float f15 = f;
            this.drawRectWithPadding(f15, f14, f13, f12, i, consumer);
        }
    }
}
