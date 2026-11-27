package dev.prestigeclient.renderlib.example;

import dev.prestigeclient.renderlib.example.shader.RoundedRectShader;
import dev.prestigeclient.renderlib.impl.api.RenderAPI;
import dev.prestigeclient.renderlib.impl.render.InternalShaders;
import dev.prestigeclient.renderlib.impl.render.Util;
import dev.prestigeclient.renderlib.impl.render.layer.type.BufferCollector;
import dev.prestigeclient.renderlib.impl.render.layer.type.RenderLayer;
import dev.prestigeclient.renderlib.impl.render.mesh.MeshDataConsumer;
import dev.prestigeclient.renderlib.impl.render.state.StateApplier;
import dev.prestigeclient.renderlib.impl.render.ui.RenderElement;
import dev.prestigeclient.renderlib.impl.render.ui.RenderState;
import dev.prestigeclient.renderlib.impl.render.ui.UiRenderer;
import dev.prestigeclient.renderlib.impl.render.vertex.VertexFormats;
import org.lwjgl.opengl.GL11;

public final class UIExamples {
    private static final int PANEL_COLOR = 0xD0141420;
    private static final int ACCENT_COLOR = 0xFF7F5AF0;
    private static final int ACCENT_END_COLOR = 0xFF2CB67D;
    private static final int TRACK_COLOR = 0x40FFFFFF;
    private static final BufferCollector collector = new BufferCollector();
    private static UiRenderer renderer;
    private static RoundedRectShader roundedRects;
    private static RenderState colorState;
    private static int colorStateFramebuffer = -1;

    private UIExamples() {
    }

    public static UiRenderer getRenderer() {
        if (renderer == null) {
            renderer = new UiRenderer();
        }

        return renderer;
    }

    public static void draw(RenderAPI.RenderContext context) {
        if (roundedRects == null) {
            roundedRects = new RoundedRectShader();
        }

        float x = 10.0F;
        float y = 10.0F;
        float width = 160.0F;
        float height = 64.0F;
        roundedRects.draw(collector, x, y, width, height, 8.0F, PANEL_COLOR);
        collector.draw(context);

        RenderState state = colorState();
        UiRenderer ui = getRenderer();
        ui.addElement(new GradientRect(state, x + 10.0F, y + 12.0F, width - 20.0F, 2.0F, ACCENT_COLOR, ACCENT_END_COLOR, false));

        float progress = (float)((Math.sin(System.currentTimeMillis() / 600.0) + 1.0) * 0.5);
        float trackWidth = width - 20.0F;
        float trackY = y + height - 18.0F;
        ui.addElement(new GradientRect(state, x + 10.0F, trackY, trackWidth, 6.0F, TRACK_COLOR, TRACK_COLOR, false));
        ui.addElement(new GradientRect(state, x + 10.0F, trackY, trackWidth * progress, 6.0F, ACCENT_COLOR, ACCENT_END_COLOR, false));

        for (int i = 0; i < 4; i++) {
            float swatchX = x + 10.0F + i * 22.0F;
            int top = Util.pack(255 - i * 50, 80 + i * 40, 200, 255);
            ui.addElement(new GradientRect(state, swatchX, y + 22.0F, 16.0F, 16.0F, top, 0xFF101010, true));
        }

        ui.draw(context);
    }

    private static RenderState colorState() {
        int framebuffer = RenderAPI.getMinecraftFramebuffer();
        if (colorState == null || colorStateFramebuffer != framebuffer) {
            RenderLayer layer = new RenderLayer(
                VertexFormats.POSITION_COLOR,
                GL11.GL_TRIANGLES,
                false,
                new StateApplier[]{
                    StateApplier.framebufferState(framebuffer),
                    StateApplier.shaderState(InternalShaders.POS_COLOR),
                    StateApplier.flagState(GL11.GL_DEPTH_TEST, false, true),
                    StateApplier.flagState(GL11.GL_CULL_FACE, false, true),
                    StateApplier.blendState(true, true)
                }
            );
            colorState = new RenderState(null, layer);
            colorStateFramebuffer = framebuffer;
        }

        return colorState;
    }

    private record GradientRect(RenderState state, float x, float y, float width, float height, int startColor, int endColor, boolean vertical)
        implements RenderElement {
        @Override
        public void draw(MeshDataConsumer consumer, float z) {
            if (this.width <= 0.0F || this.height <= 0.0F) {
                return;
            }

            int start = Util.toVertexColor(this.startColor);
            int end = Util.toVertexColor(this.endColor);
            int topLeft = start;
            int bottomLeft = this.vertical ? end : start;
            int topRight = this.vertical ? start : end;
            int bottomRight = end;
            float right = this.x + this.width;
            float bottom = this.y + this.height;
            consumer.start()
                .pos(this.x, bottom, z)
                .color(bottomLeft)
                .pos(this.x, this.y, z)
                .color(topLeft)
                .pos(right, this.y, z)
                .color(topRight)
                .pos(right, bottom, z)
                .color(bottomRight)
                .putIndices(new int[]{0, 1, 2, 2, 3, 0})
                .end();
        }
    }
}
