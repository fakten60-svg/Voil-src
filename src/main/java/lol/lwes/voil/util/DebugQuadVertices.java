package lol.lwes.voil.util;

import dev.prestigeclient.renderlib.impl.render.mesh.MeshDataConsumer;
import dev.prestigeclient.renderlib.impl.render.ui.built.SimplePreBuiltVertices;
import dev.prestigeclient.renderlib.impl.render.vertex.VertexFormat;
import java.awt.Color;
import lol.lwes.voil.util.render.shader.FramebufferBlitRenderer;

public class DebugQuadVertices extends SimplePreBuiltVertices {
    final FramebufferBlitRenderer framebufferShader;
    private static final int INDEX_COUNT = 6;

    public DebugQuadVertices(FramebufferBlitRenderer framebufferblitrenderer) {
        super(false);
        this.framebufferShader = framebufferblitrenderer;
    }

    @Override
    public VertexFormat getFormat() {
        return this.framebufferShader.vertexFormat;
    }

    @Override
    public void writeVAO(MeshDataConsumer meshdataconsumer, float f) {
        int i = Color.CYAN.getRGB();
        int j = Color.RED.getRGB();
        int k = Color.MAGENTA.getRGB();
        int l = Color.GREEN.getRGB();
        MeshDataConsumer meshdataconsumer1 = meshdataconsumer.start()
            .floats(-1.0F, 1.0F, f)
            .floats(0.0F, 1.0F)
            .color(i)
            .floats(-1.0F, -1.0F, f)
            .floats(0.0F, 0.0F)
            .color(k)
            .floats(1.0F, -1.0F, f)
            .floats(1.0F, 0.0F)
            .color(l)
            .floats(1.0F, 1.0F, f)
            .floats(1.0F, 1.0F)
            .color(j);
        int[] aint = new int[INDEX_COUNT];
        aint[0] = 0;
        aint[1] = 1;
        aint[2] = 2;
        aint[3] = 2;
        aint[4] = 3;
        aint[5] = 0;
        meshdataconsumer1.putIndices(aint).end();
    }
}
