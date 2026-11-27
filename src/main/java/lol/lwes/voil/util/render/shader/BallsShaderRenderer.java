package lol.lwes.voil.util.render.shader;

import dev.prestigeclient.renderlib.api.uniform.RLUniforms;
import dev.prestigeclient.renderlib.impl.render.vertex.VertexFormat;
import dev.prestigeclient.renderlib.impl.render.vertex.VertexFormats;
import java.io.PrintStream;
import java.util.Random;
import java.util.function.Consumer;
import lol.lwes.voil.util.render.GuiScale;

public class BallsShaderRenderer extends ShaderRenderer {
    private static final int BALL_COUNT = 8;
    private static final long RANDOM_SEED = 6767L;

    public BallsShaderRenderer() {
        try {
            VertexFormat vertexformat = VertexFormats.POSITION;
            BallsShaderRenderer ballsshaderrenderer1 = this;
            ballsshaderrenderer1.vertexFormat = vertexformat;
            VertexFormat vertexformat1 = vertexformat;
            this.shader = ShaderRenderer.loadShaderForFormat("shaders/core/balls.fsh", vertexformat1);
        } catch (Exception exception) {
            PrintStream printstream = System.err;
            String s1 = exception.getMessage();
            printstream.println("Failed to load balls shader: " + s1);
        }
    }

    public void render(float f, float f1, float f2, float f3, float f4, float f5, float f6) {
        if (this.shader != null) {
            float f7 = ShaderRenderer.getScale();
            float f8 = f4 + 10.0F;
            float f9 = f2 / f3;
            int j = (int)f8;
            Consumer<RLUniforms> consumer = rluniforms -> {
                rluniforms.set1f("radius", f4 * f7);
                rluniforms.set1f("smoothness", f5 * f7);
                rluniforms.set2f("uLocation", f * f7, -f1 * f7 + GuiScale.getScaledHeight() * f7 - f3 * f7);
                rluniforms.set2f("uSize", f2 * f7, f3 * f7);
                Random random = new Random(RANDOM_SEED);

                for (int ix = 0; ix < BALL_COUNT; ix++) {
                    float f9x = random.nextFloat() * f9;
                    float f10x = random.nextFloat();
                    float f11x = 0.18F + random.nextFloat() * 0.1F;
                    float f12x = random.nextFloat();
                    float f13x = f6 * 0.08F + random.nextFloat() * (float) (Math.PI * 2);
                    rluniforms.set4f("uBalls[" + ix + "]", f9x, f10x, f11x, f12x);
                    rluniforms.set2f("uPhase[" + ix + "]", (float)Math.sin(f13x), (float)Math.cos(f13x * 1.3F));
                }
            };
            int i = j;
            float f10 = f3;
            float f11 = f2;
            float f12 = f1;
            float f13 = f;
            this.drawRectWithPadding(f13, f12, f11, f10, i, consumer);
        }
    }
}
