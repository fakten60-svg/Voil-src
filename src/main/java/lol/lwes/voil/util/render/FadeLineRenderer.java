package lol.lwes.voil.util.render;

import dev.prestigeclient.renderlib.api.uniform.RLUniforms;
import dev.prestigeclient.renderlib.impl.render.vertex.VertexFormat;
import dev.prestigeclient.renderlib.impl.render.vertex.VertexFormats;
import java.awt.Color;
import java.io.PrintStream;
import java.util.function.Consumer;
import lol.lwes.voil.util.render.shader.ShaderRenderer;

public class FadeLineRenderer extends ShaderRenderer {
    public FadeLineRenderer() {
        try {
            VertexFormat vertexformat = VertexFormats.POSITION;
            FadeLineRenderer fadelinerenderer1 = this;
            fadelinerenderer1.vertexFormat = vertexformat;
            VertexFormat vertexformat1 = vertexformat;
            this.shader = ShaderRenderer.loadShaderForFormat("shaders/core/fade_line.fsh", vertexformat1);
        } catch (Exception exception) {
            PrintStream printstream = System.err;
            String s1 = exception.getMessage();
            printstream.println("Failed to load fade shader: " + s1);
        }
    }

    public void render(float f, float f1, float f2, float f3, boolean flag, float f4, Color color) {
        float f5 = ShaderRenderer.getScale();
        Consumer<RLUniforms> consumer = rluniforms -> {
            rluniforms.set2f(
                "uLocation",
                f * ShaderRenderer.getScale(),
                -f1 * ShaderRenderer.getScale() + GuiScale.getScaledHeight() * ShaderRenderer.getScale() - f3 * ShaderRenderer.getScale()
            );
            rluniforms.set2f("uSize", f2 * ShaderRenderer.getScale(), f3 * ShaderRenderer.getScale());
            rluniforms.set1i("uDirection", flag ? 1 : 0);
            rluniforms.set1f("uThickness", f4 * f5);
            rluniforms.set4f("color1", color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, color.getAlpha() / 255.0F);
        };
        float f6 = f3;
        float f7 = f2;
        float f8 = f1;
        float f9 = f;
        this.drawRect(f9, f8, f7, f6, consumer);
    }
}
