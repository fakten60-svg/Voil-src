package lol.lwes.voil.util.render;

import dev.prestigeclient.renderlib.api.uniform.RLUniforms;
import dev.prestigeclient.renderlib.impl.render.vertex.VertexFormat;
import dev.prestigeclient.renderlib.impl.render.vertex.VertexFormats;
import java.awt.Color;
import java.io.PrintStream;
import java.util.function.Consumer;
import lol.lwes.voil.util.render.shader.ShaderRenderer;

public class ModuleGlowRenderer extends ShaderRenderer {
    public ModuleGlowRenderer() {
        try {
            VertexFormat vertexformat = VertexFormats.POSITION;
            ModuleGlowRenderer moduleglowrenderer1 = this;
            moduleglowrenderer1.vertexFormat = vertexformat;
            VertexFormat vertexformat1 = vertexformat;
            this.shader = ShaderRenderer.loadShaderForFormat("shaders/core/module_glow.fsh", vertexformat1);
        } catch (Exception exception) {
            PrintStream printstream = System.err;
            String s1 = exception.getMessage();
            printstream.println("Failed to load module glow shader: " + s1);
        }
    }

    public static float half(float f) {
        return f * 0.5F;
    }

    public void renderVariant(
        float f,
        float f1,
        float f2,
        float f3,
        float f4,
        float f5,
        float f6,
        Color color,
        Color color1,
        Color color2,
        float f7,
        float f8,
        float f9,
        float f10,
        float f11,
        int i,
        float f12
    ) {
        if (this.shader != null) {
            float f13 = ShaderRenderer.getScale();
            float f14 = GuiScale.getScaledHeight() * f13;
            float f15 = f5 + 2.0F;
            Consumer<RLUniforms> consumer = rluniforms -> {
                rluniforms.set2f("uLocation", f * f13, f14 - (f1 + f3) * f13);
                rluniforms.set2f("uSize", f2 * f13, f3 * f13);
                rluniforms.set1f("radius", f4 * f13);
                rluniforms.set1f("smoothness", f5 * f13);
                rluniforms.set1f("uThickness", f6 * f13);
                setColorUniform(rluniforms, "uFillColor", color);
                setColorUniform(rluniforms, "uStrokeColor", color1);
                setColorUniform(rluniforms, "uGlowColor", color2);
                rluniforms.set2f("uGlowCenter", f7 * f13, (f3 - f8) * f13);
                rluniforms.set2f("uGlowHalf", f9 * f13, f10 * f13);
                rluniforms.set1f("uGlowSigma", f11 * f13);
                rluniforms.set1i("uSamples", i);
                rluniforms.set1f("uOpacity", f12);
            };
            float f16 = f15;
            float f17 = f3;
            float f18 = f2;
            float f19 = f1;
            float f20 = f;
            this.drawRectExpanded(f20, f19, f18, f17, -1, f16, consumer);
        }
    }

    private static void setColorUniform(RLUniforms rluniforms, String s, Color color) {
        if (color == null) {
            rluniforms.set4f(s, 0.0F, 0.0F, 0.0F, 0.0F);
        } else {
            rluniforms.set4f(s, color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, color.getAlpha() / 255.0F);
        }
    }

    public void render(
        float f,
        float f1,
        float f2,
        float f3,
        float f4,
        float f5,
        float f6,
        Color color,
        Color color1,
        Color color2,
        float f7,
        float f8,
        float f9,
        float f10,
        float f11,
        int i,
        float f12
    ) {
        float f25 = f7 * f2;
        float f26 = f8 * f3;
        float f27 = f9 * f2;
        float f28 = f10 * f3;
        float f13 = f11 * f3;
        float f14 = f28;
        float f15 = f27;
        float f16 = f26;
        float f17 = f25;
        Color color3 = color2;
        Color color4 = color1;
        Color color5 = color;
        float f18 = f6;
        float f19 = f5;
        float f20 = f4;
        float f21 = f3;
        float f22 = f2;
        float f23 = f1;
        float f24 = f;
        this.renderVariant(f24, f23, f22, f21, f20, f19, f18, color5, color4, color3, f17, f16, f15, f14, f13, i, f12);
    }
}
