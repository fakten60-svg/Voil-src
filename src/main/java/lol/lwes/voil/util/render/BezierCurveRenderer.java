package lol.lwes.voil.util.render;

import dev.prestigeclient.renderlib.impl.render.vertex.VertexFormat;
import dev.prestigeclient.renderlib.impl.render.vertex.VertexFormats;
import java.awt.Color;
import java.io.PrintStream;
import lol.lwes.voil.util.render.shader.ShaderRenderer;

public class BezierCurveRenderer extends ShaderRenderer {
    public BezierCurveRenderer() {
        try {
            VertexFormat vertexformat = VertexFormats.POSITION;
            BezierCurveRenderer beziercurverenderer1 = this;
            beziercurverenderer1.vertexFormat = vertexformat;
            VertexFormat vertexformat1 = vertexformat;
            this.shader = ShaderRenderer.loadShaderForFormat("shaders/core/bezier_curve.fsh", vertexformat1);
        } catch (Exception exception) {
            PrintStream printstream = System.err;
            String s1 = exception.getMessage();
            printstream.println("Failed to load bezier curve shader: " + s1);
        }
    }

    public void render(float f, float f1, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9, Color color, Color color1) {
        if (this.shader != null) {
            float f10 = ShaderRenderer.getScale();
            float f11 = Math.min(Math.min(f, f2), Math.min(f4, f6));
            float f12 = Math.min(Math.min(f1, f3), Math.min(f5, f7));
            float f13 = Math.max(Math.max(f, f2), Math.max(f4, f6));
            float f14 = Math.max(Math.max(f1, f3), Math.max(f5, f7));
            float f15 = f8 + f9 + 4.0F;
            float f16 = f11 - f15;
            float f17 = f12 - f15;
            float f18 = f13 - f11 + f15 * 2.0F;
            float f19 = f14 - f12 + f15 * 2.0F;
            QuadOutlineUniforms quadoutlineuniforms = new QuadOutlineUniforms(f16, f17, f18, f19, f, f1, f2, f3, f4, f5, f6, f7, f8, f9, color, color1, f10);
            float f20 = f19;
            float f21 = f18;
            float f22 = f17;
            float f23 = f16;
            this.drawRect(f23, f22, f21, f20, quadoutlineuniforms);
        }
    }

    public void renderPath(float[] afloat, float f, float f1, float f2, float f3, float f4, float f5, Color color, Color color1) {
        if (afloat != null && afloat.length >= 2) {
            int i = afloat.length;
            float f6 = f2 / (i - 1);

            for (int j = 0; j < i - 1; j++) {
                float f7 = f + j * f6;
                float f8 = f1 + f3 - afloat[j];
                float f9 = f + (j + 1) * f6;
                float f10 = f1 + f3 - afloat[j + 1];
                float f11 = f6 / 3.0F;
                float f12;
                if (j > 0) {
                    f12 = (afloat[j + 1] - afloat[j - 1]) / 2.0F;
                } else {
                    f12 = afloat[j + 1] - afloat[j];
                }

                float f13;
                if (j < i + -3 + 1) {
                    f13 = (afloat[j + 2] - afloat[j]) / 2.0F;
                } else {
                    f13 = afloat[j + 1] - afloat[j];
                }

                float f14 = f7 + f11;
                float f15 = f8 - f12 / 3.0F;
                float f16 = f9 - f11;
                float f17 = f10 + f13 / 3.0F;
                float f18 = (float)j / (i - 1);
                float f19 = (float)(j + 1) / (i - 1);
                Color color2 = ColorUtil.lerpColor(color, color1, f18);
                Color color3 = ColorUtil.lerpColor(color, color1, f19);
                this.render(f7, f8, f14, f15, f16, f17, f9, f10, f4, f5, color2, color3);
            }
        }
    }

    public void renderPath(float[] afloat, float f, float f1, float f2, float f3, float f4, float f5, Color color) {
        this.renderPath(afloat, f, f1, f2, f3, f4, f5, color, color);
    }

    public void render(float f, float f1, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9, Color color) {
        this.render(f, f1, f2, f3, f4, f5, f6, f7, f8, f9, color, color);
    }
}
