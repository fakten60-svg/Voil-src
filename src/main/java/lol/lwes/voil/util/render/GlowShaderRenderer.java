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
import org.joml.Vector2f;
import org.joml.Vector4f;

public class GlowShaderRenderer extends ShaderRenderer {
    static RectInstancer rectInstancer = new RectInstancer(null, DefaultPreBuiltVertices.SQUARE.getVAOIndexMesh(), (object, list) -> {
        ShadowRectData shadowrectdata = (ShadowRectData)object;
        list.get(0).pad(shadowrectdata.radius());
        list.get(1).pad(shadowrectdata.softness());
        list.get(2).pad(shadowrectdata.thickness());
        list.get(3).pad(shadowrectdata.position());
        list.get(4).pad(shadowrectdata.size());
        list.get(5).feed(shadowrectdata.colorTopLeft());
        list.get(6).feed(shadowrectdata.colorTopRight());
        list.get(7).feed(shadowrectdata.colorBottomRight());
        list.get(8).feed(shadowrectdata.colorBottomLeft());
    }, new String[]{"Radius", "Softness", "Spread", "ULocation", "USize", "Color1", "Color2", "Color3", "Color4"});

    public GlowShaderRenderer() {
        try {
            VertexFormat vertexformat = RenderManager.posFormat;
            GlowShaderRenderer glowshaderrenderer1 = this;
            glowshaderrenderer1.vertexFormat = vertexformat;
            VertexFormat vertexformat1 = vertexformat;
            this.shader = ShaderRenderer.loadShaderForFormat("shaders/core/glow.fsh", vertexformat1);
        } catch (Exception exception) {
            PrintStream printstream = System.err;
            String s1 = exception.getMessage();
            printstream.println("Failed to load glow shader: " + s1);
        }
    }

    public void render(float f, float f1, float f2, float f3, float f4, float f5, float f6, Color color) {
        this.render(f, f1, f2, f3, f4, f5, f6, color, color, color, color);
    }

    public void render(float f, float f1, float f2, float f3, float f4, float f5, float f6, Color color, Color color1, Color color2, Color color3) {
        if (this.shader != null) {
            float f7 = ShaderRenderer.getScale();
            float f8 = f4 + f5 * f6 * 3.0F + 10.0F;
            float f9 = f - f8;
            float f10 = f1 - f8;
            float f11 = f2 + f8 * 2.0F;
            float f12 = f3 + f8 * 2.0F;
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
                new ShadowRectData(
                    f4 * f7,
                    f5 * f7,
                    f6,
                    new Vector2f(f * f7, -f1 * f7 + GuiScale.getScaledHeight() * f7 - f3 * f7),
                    new Vector2f(f2 * f7, f3 * f7),
                    new Vector4f(color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, color.getAlpha() / 255.0F),
                    new Vector4f(color1.getRed() / 255.0F, color1.getGreen() / 255.0F, color1.getBlue() / 255.0F, color1.getAlpha() / 255.0F),
                    new Vector4f(color2.getRed() / 255.0F, color2.getGreen() / 255.0F, color2.getBlue() / 255.0F, color2.getAlpha() / 255.0F),
                    new Vector4f(color3.getRed() / 255.0F, color3.getGreen() / 255.0F, color3.getBlue() / 255.0F, color3.getAlpha() / 255.0F)
                )
            );
        }
    }
}
