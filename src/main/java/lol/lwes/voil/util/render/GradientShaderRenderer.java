package lol.lwes.voil.util.render;

import dev.prestigeclient.renderlib.api.uniform.RLUniforms;
import dev.prestigeclient.renderlib.impl.render.vertex.VertexFormat;
import dev.prestigeclient.renderlib.impl.render.vertex.VertexFormats;
import java.awt.Color;
import java.io.PrintStream;
import java.util.function.Consumer;
import lol.lwes.voil.util.render.shader.ShaderRenderer;

public class GradientShaderRenderer extends ShaderRenderer {
    private final String[] colorUniformNames = new String[]{"uColor0", "uColor1", "uColor2", "uColor3", "uColor4", "uColor5", "uColor6", "uColor7"};

    public GradientShaderRenderer() {
        try {
            VertexFormat vertexformat = VertexFormats.POSITION;
            GradientShaderRenderer gradientshaderrenderer1 = this;
            gradientshaderrenderer1.vertexFormat = vertexformat;
            VertexFormat vertexformat1 = vertexformat;
            this.shader = ShaderRenderer.loadShaderForFormat("shaders/core/gradient.fsh", vertexformat1);
        } catch (Exception exception) {
            PrintStream printstream = System.err;
            String s1 = exception.getMessage();
            printstream.println("Failed to load gradient shader: " + s1);
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
        float f7,
        float f8,
        float f9,
        float f10,
        float[] afloat,
        Color[] acolor,
        boolean flag,
        float f11,
        float f12,
        float f13
    ) {
        int i = Math.min(Math.min(afloat.length, acolor.length), this.colorUniformNames.length);
        if (this.shader != null && i != 0) {
            float f14 = ShaderRenderer.getScale();
            float f15 = GuiScale.getScaledHeight() * f14;
            float[] afloat1 = new float[this.colorUniformNames.length];

            for (int j = 0; j < afloat1.length; j++) {
                afloat1[j] = j < i ? afloat[j] : 1.0F;
            }

            int k = (int)(f4 + f5);
            int i1 = k + 2;
            Consumer<RLUniforms> consumer = rluniforms -> {
                rluniforms.set1f("radius", f4 * f14);
                rluniforms.set1f("smoothness", f5 * f14);
                rluniforms.set1f("uThickness", f6 * f14);
                rluniforms.set2f("uLocation", f * f14, f15 - (f1 + f3) * f14);
                rluniforms.set2f("uSize", f2 * f14, f3 * f14);
                rluniforms.set2f("uFrameLocation", f7 * f14, f15 - (f8 + f10) * f14);
                rluniforms.set2f("uFrameSize", f9 * f14, f10 * f14);
                rluniforms.set1f("uType", flag ? 1.0F : 0.0F);
                rluniforms.set1f("uAngle", f11);
                rluniforms.set1f("uCount", i);
                rluniforms.set1f("uChecker", f12 * f14);
                rluniforms.set1f("uOpacity", f13);
                rluniforms.set4f("uStops0", afloat1[0], afloat1[1], afloat1[2], afloat1[3]);
                rluniforms.set4f("uStops1", afloat1[4], afloat1[5], afloat1[6], afloat1[7]);

                for (int jx = 0; jx < this.colorUniformNames.length; jx++) {
                    Color color = acolor[Math.min(jx, i - 1)];
                    rluniforms.set4f(
                        this.colorUniformNames[jx], color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, color.getAlpha() / 255.0F
                    );
                }
            };
            int l = i1;
            float f16 = f3;
            float f17 = f2;
            float f18 = f1;
            float f19 = f;
            this.drawRectWithPadding(f19, f18, f17, f16, l, consumer);
        }
    }
}
