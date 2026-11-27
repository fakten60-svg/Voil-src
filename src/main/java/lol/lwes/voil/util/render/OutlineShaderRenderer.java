package lol.lwes.voil.util.render;

import dev.prestigeclient.renderlib.impl.render.state.StateApplier;
import dev.prestigeclient.renderlib.impl.render.ui.RectInstancer;
import dev.prestigeclient.renderlib.impl.render.ui.UiRenderer;
import dev.prestigeclient.renderlib.impl.render.ui.built.DefaultPreBuiltVertices;
import dev.prestigeclient.renderlib.impl.render.vertex.VertexFormat;
import java.awt.Color;
import java.io.PrintStream;
import java.util.List;
import lol.lwes.voil.util.render.shader.ShaderRenderer;
import org.joml.Vector4f;

public class OutlineShaderRenderer extends ShaderRenderer {
    private static int maxColorCount = 32 / 4;
    private static Vector4f[] colorCache = new Vector4f[]{
        new Vector4f(), new Vector4f(), new Vector4f(), new Vector4f(), new Vector4f(), new Vector4f(), new Vector4f(), new Vector4f()
    };
    static RectInstancer rectInstancer = new RectInstancer(null, DefaultPreBuiltVertices.SQUARE.getVAOIndexMesh(), (object, list) -> {
        BlurRectData blurrectdata = (BlurRectData)object;
        list.get(0).feed(blurrectdata.bounds());
        list.get(0).feed(blurrectdata.cornerRadii());
        list.get(0).feed(blurrectdata.tint());
        list.get(1).feed(blurrectdata.params());
        list.get(1).feed(blurrectdata.colorStart());
        list.get(1).feed(blurrectdata.colorEnd());
        list.get(2).feed(blurrectdata.stopsStart()[0]);
        list.get(2).feed(blurrectdata.stopsStart()[1]);
        list.get(3).feed(blurrectdata.stopsStart()[2]);
        list.get(3).feed(blurrectdata.stopsStart()[3]);
        list.get(4).feed(blurrectdata.stopsStart()[4]);
        list.get(4).feed(blurrectdata.stopsStart()[5]);
        list.get(5).feed(blurrectdata.stopsStart()[6]);
        list.get(5).feed(blurrectdata.stopsStart()[7]);
        list.get(6).feed(blurrectdata.stopsEnd()[0]);
        list.get(6).feed(blurrectdata.stopsEnd()[1]);
        list.get(7).feed(blurrectdata.stopsEnd()[2]);
        list.get(7).feed(blurrectdata.stopsEnd()[3]);
        list.get(8).feed(blurrectdata.stopsEnd()[4]);
        list.get(8).feed(blurrectdata.stopsEnd()[5]);
        list.get(9).feed(blurrectdata.stopsEnd()[6]);
        list.get(9).feed(blurrectdata.stopsEnd()[7]);
    }, new String[]{"Base", "Extra", "SegH0", "SegH1", "SegH2", "SegH3", "SegW0", "SegW1", "SegW2", "SegW3"});

    public OutlineShaderRenderer() {
        try {
            VertexFormat vertexformat = RenderManager.posFormat;
            OutlineShaderRenderer outlineshaderrenderer1 = this;
            outlineshaderrenderer1.vertexFormat = vertexformat;
            VertexFormat vertexformat1 = vertexformat;
            this.shader = ShaderRenderer.loadShaderForFormat("shaders/core/outline.fsh", vertexformat1);
        } catch (Exception exception) {
            PrintStream printstream = System.err;
            String s1 = exception.getMessage();
            printstream.println("Failed to load outline shader: " + s1);
        }
    }

    public void render(float f, float f1, float[] afloat, float[] afloat1, float f2, float f3, Color color, boolean flag) {
        this.render(f, f1, afloat, afloat1, f2, f3, -1.0F, color, color, color, color, flag);
    }

    public void render(float f, float f1, float f2, float f3, float f4, float f5, float f6, Color color) {
        this.render(f, f1, f2, f3, f4, f5, f6, color, color, color, color);
    }

    public void render(float f, float f1, float[] afloat, float[] afloat1, float f2, float f3, float f4, Color color, Color color1, Color color2, Color color3) {
        this.render(f, f1, afloat, afloat1, f2, f3, f4, color, color1, color2, color3, false);
    }

    public void render(
        float f, float f1, float[] afloat, float[] afloat1, float f2, float f3, float f4, Color color, Color color1, Color color2, Color color3, boolean flag
    ) {
        if (this.shader != null) {
            if (afloat != null && afloat1 != null && afloat.length == afloat1.length) {
                float f5 = ShaderRenderer.getScale();
                int i = Math.min(afloat.length, 32);
                float f6 = 0.0F;
                float f7 = 0.0F;

                for (int j = 0; j < i; j++) {
                    f6 += afloat[j];
                    if (afloat1[j] > f7) {
                        f7 = afloat1[j];
                    }
                }

                if (i != 0 && !(f7 <= 0.0F) && !(f6 <= 0.0F)) {
                    float f12 = f2 + f4 + 10.0F;
                    float f8 = flag ? -i : i;
                    Vector4f[] avector4f = new Vector4f[maxColorCount];
                    Vector4f[] avector4f1 = new Vector4f[maxColorCount];

                    for (int k = 0; k < maxColorCount; k++) {
                        int l = k * 4;
                        avector4f[k] = new Vector4f(
                            l < i ? afloat[l] * f5 : 0.0F,
                            l + 1 < i ? afloat[l + 1] * f5 : 0.0F,
                            l + 2 < i ? afloat[l + 2] * f5 : 0.0F,
                            l + 3 < i ? afloat[l + 3] * f5 : 0.0F
                        );
                        avector4f1[k] = new Vector4f(
                            l < i ? afloat1[l] * f5 : 0.0F,
                            l + 1 < i ? afloat1[l + 1] * f5 : 0.0F,
                            l + 2 < i ? afloat1[l + 2] * f5 : 0.0F,
                            l + 3 < i ? afloat1[l + 3] * f5 : 0.0F
                        );
                    }

                    float f13 = f - f12;
                    float f14 = f1 - f12;
                    float f9 = f7 + f12 * 2.0F;
                    float f10 = f6 + f12 * 2.0F;
                    float f11 = -f1 * f5 + GuiScale.getScaledHeight() * f5 - f6 * f5;
                    UiRenderer uirenderer = RenderManager.uiRenderer;
                    RectInstancer rectinstancer1 = rectInstancer;
                    StateApplier[] astateapplier = new StateApplier[0];
                    RectInstancer rectinstancer = rectinstancer1;
                    uirenderer.addRectangle(
                        this.getInstancerCached(rectinstancer, astateapplier),
                        f13,
                        f14,
                        f9,
                        f10,
                        -1,
                        RenderManager.getScissorRect(),
                        new BlurRectData(
                            new Vector4f(f2 * f5, f3 * f5, f4 * f5, f8),
                            new Vector4f(f * f5, f11, f7 * f5, f6 * f5),
                            toVector4f(color),
                            toVector4f(color1),
                            toVector4f(color2),
                            toVector4f(color3),
                            avector4f,
                            avector4f1
                        )
                    );
                }
            }
        }
    }

    public void render(float f, float f1, float f2, float f3, float f4, float f5, float f6, Color color, Color color1, Color color2, Color color3) {
        if (this.shader != null) {
            float f7 = ShaderRenderer.getScale();
            float f8 = f4 + f6 + 10.0F;
            float f9 = f - f8;
            float f10 = f1 - f8;
            float f11 = f2 + f8 * 2.0F;
            float f12 = f3 + f8 * 2.0F;
            float f13 = -f1 * f7 + GuiScale.getScaledHeight() * f7 - f3 * f7;
            UiRenderer uirenderer = RenderManager.uiRenderer;
            RectInstancer rectinstancer1 = rectInstancer;
            StateApplier[] astateapplier = new StateApplier[0];
            RectInstancer rectinstancer = rectinstancer1;
            uirenderer.addRectangle(
                this.getInstancerCached(rectinstancer, astateapplier),
                f9,
                f10,
                f11,
                f12,
                -1,
                RenderManager.getScissorRect(),
                new BlurRectData(
                    new Vector4f(f4 * f7, f5 * f7, f6 * f7, 0.0F),
                    new Vector4f(f * f7, f13, f2 * f7, f3 * f7),
                    toVector4f(color),
                    toVector4f(color1),
                    toVector4f(color2),
                    toVector4f(color3),
                    colorCache,
                    colorCache
                )
            );
        }
    }

    private static Vector4f toVector4f(Color color) {
        return new Vector4f(color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, color.getAlpha() / 255.0F);
    }
}
