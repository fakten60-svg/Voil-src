package lol.lwes.voil.util.render.shader;

import dev.prestigeclient.renderlib.api.uniform.RLUniforms;
import dev.prestigeclient.renderlib.impl.render.vertex.VertexFormat;
import dev.prestigeclient.renderlib.impl.render.vertex.VertexFormats;
import java.awt.Color;
import java.io.PrintStream;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import lol.lwes.voil.util.render.GuiScale;

public class GradientBackgroundShaderRenderer extends ShaderRenderer {
    private final float seed = ThreadLocalRandom.current().nextFloat() * 10000.0F;

    public GradientBackgroundShaderRenderer() {
        try {
            VertexFormat vertexformat = VertexFormats.POSITION;
            GradientBackgroundShaderRenderer gradientbackgroundshaderrenderer1 = this;
            gradientbackgroundshaderrenderer1.vertexFormat = vertexformat;
            VertexFormat vertexformat1 = vertexformat;
            this.shader = ShaderRenderer.loadShaderForFormat("shaders/core/background.fsh", vertexformat1);
        } catch (Exception exception) {
            PrintStream printstream = System.err;
            String s1 = exception.getMessage();
            printstream.println("Failed to load background shader: " + s1);
        }
    }

    public void render(float f, float f1, float f2, float f3, float f4, float f5, Color color, Color color1, float f6) {
        if (this.shader != null) {
            float f7 = ShaderRenderer.getScale();
            float f8 = f4 + 10.0F;
            int j = (int)f8;
            Consumer<RLUniforms> consumer = rluniforms -> {
                rluniforms.set1f("radius", f4 * f7);
                rluniforms.set1f("smoothness", f5 * f7);
                rluniforms.set2f("uLocation", f * f7, -f1 * f7 + GuiScale.getScaledHeight() * f7 - f3 * f7);
                rluniforms.set2f("uSize", f2 * f7, f3 * f7);
                rluniforms.set4f("color1", color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, color.getAlpha() / 255.0F);
                rluniforms.set4f("color2", color1.getRed() / 255.0F, color1.getGreen() / 255.0F, color1.getBlue() / 255.0F, color1.getAlpha() / 255.0F);
                rluniforms.set1f("u_time", f6);
                rluniforms.set1f("u_seed", this.seed);
            };
            int i = j;
            float f9 = f3;
            float f10 = f2;
            float f11 = f1;
            float f12 = f;
            this.drawRectWithPadding(f12, f11, f10, f9, i, consumer);
        }
    }
}
