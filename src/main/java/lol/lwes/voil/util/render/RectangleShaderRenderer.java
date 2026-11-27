package lol.lwes.voil.util.render;

import dev.prestigeclient.renderlib.impl.render.state.StateApplier;
import dev.prestigeclient.renderlib.impl.render.ui.BufferHelper;
import dev.prestigeclient.renderlib.impl.render.ui.RectInstancer;
import dev.prestigeclient.renderlib.impl.render.ui.UiRenderer;
import dev.prestigeclient.renderlib.impl.render.ui.built.DefaultPreBuiltVertices;
import dev.prestigeclient.renderlib.impl.render.vertex.VertexFormat;
import java.awt.Color;
import java.io.PrintStream;
import java.util.List;
import java.util.function.BiConsumer;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.util.render.shader.ShaderRenderer;
import org.joml.Vector2f;
import org.joml.Vector4f;

public class RectangleShaderRenderer extends ShaderRenderer {
    private static final BiConsumer<OutlineRectData, List<BufferHelper>> RECT_WRITER = (outlinerectdata, list) -> {
        list.get(0).feed(outlinerectdata.bounds());
        list.get(1).pad(outlinerectdata.thickness());
        list.get(2).pad(outlinerectdata.position());
        list.get(3).pad(outlinerectdata.size());
        list.get(4).feed(outlinerectdata.colorTopLeft());
        list.get(5).feed(outlinerectdata.colorTopRight());
        list.get(6).feed(outlinerectdata.colorBottomRight());
        list.get(7).feed(outlinerectdata.colorBottomLeft());
    };
    static RectInstancer rectInstancer = new RectInstancer(
        null,
        DefaultPreBuiltVertices.SQUARE.getVAOIndexMesh(),
        (data, buffers) -> RECT_WRITER.accept((OutlineRectData) data, buffers),
        new String[]{"CornerRadii", "Smoothness", "ULocation", "USize", "Color1", "Color2", "Color3", "Color4"}
    );

    public RectangleShaderRenderer() {
        try {
            VertexFormat vertexformat = RenderManager.posFormat;
            RectangleShaderRenderer rectangleshaderrenderer1 = this;
            rectangleshaderrenderer1.vertexFormat = vertexformat;
            VertexFormat vertexformat1 = vertexformat;
            this.shader = ShaderRenderer.loadShaderForFormat("shaders/core/rectangle.fsh", vertexformat1);
        } catch (Exception exception) {
            PrintStream printstream = System.err;
            String s1 = exception.getMessage();
            printstream.println("Failed to load rectangle shader: " + s1);
        }
    }

    public void render(float f, float f1, float f2, float f3, float f4, float f5, Color color) {
        this.render(f, f1, f2, f3, f4, f4, f4, f4, f5, color, color, color, color);
    }

    public void render(float f, float f1, float f2, float f3, float f4, float f5, float f6, float f7, float f8, Color color) {
        this.render(f, f1, f2, f3, f4, f5, f6, f7, f8, color, color, color, color);
    }

    public void render(
        float f, float f1, float f2, float f3, float f4, float f5, float f6, float f7, float f8, Color color, Color color1, Color color2, Color color3
    ) {
        if (this.shader != null) {
            float f9 = ShaderRenderer.getScale();
            int i = VoilClient.mc.getWindow().getFramebufferHeight();
            float f10 = f - 10.0F;
            float f11 = f1 - 10.0F;
            float f12 = f2 + 20.0F;
            float f13 = f3 + 20.0F;
            float f14 = f * f9;
            float f15 = i - (f1 + f3) * f9;
            float f16 = f2 * f9;
            float f17 = f3 * f9;
            UiRenderer uirenderer = RenderManager.uiRenderer;
            RectInstancer rectinstancer1 = rectInstancer;
            StateApplier[] astateapplier = new StateApplier[0];
            RectInstancer rectinstancer = rectinstancer1;
            uirenderer.addRectangle(
                this.getInstancerCached(rectinstancer, astateapplier),
                f10,
                f11,
                f12,
                f13,
                -1,
                RenderManager.getScissorRect(),
                new OutlineRectData(
                    new Vector4f(f4 * f9, f5 * f9, f6 * f9, f7 * f9),
                    f8 * f9,
                    new Vector2f(f14, f15),
                    new Vector2f(f16, f17),
                    new Vector4f(color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, color.getAlpha() / 255.0F),
                    new Vector4f(color1.getRed() / 255.0F, color1.getGreen() / 255.0F, color1.getBlue() / 255.0F, color1.getAlpha() / 255.0F),
                    new Vector4f(color2.getRed() / 255.0F, color2.getGreen() / 255.0F, color2.getBlue() / 255.0F, color2.getAlpha() / 255.0F),
                    new Vector4f(color3.getRed() / 255.0F, color3.getGreen() / 255.0F, color3.getBlue() / 255.0F, color3.getAlpha() / 255.0F)
                )
            );
        }
    }

    public void render(float f, float f1, float f2, float f3, float f4, float f5, Color color, Color color1, Color color2, Color color3) {
        this.render(f, f1, f2, f3, f4, f4, f4, f4, f5, color, color1, color2, color3);
    }
}
