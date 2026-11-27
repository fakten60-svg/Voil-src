package lol.lwes.voil.util.render;

import dev.prestigeclient.renderlib.impl.render.mesh.GlBufferMeshBuilder;
import java.awt.Color;
import lol.lwes.voil.setting.GradientSetting;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class GradientBoxEmitter {
    private final Matrix4f matrix;
    private final GlBufferMeshBuilder fillBuffer;
    private final GlBufferMeshBuilder lineBuffer;
    private final GradientSetting fillGradient;
    private final GradientSetting outlineGradient;
    private final float alpha;
    private int fillVertices;
    private int lineVertices;

    public GradientBoxEmitter(
        Matrix4f matrix,
        GlBufferMeshBuilder fillBuffer,
        GlBufferMeshBuilder lineBuffer,
        GradientSetting fillGradient,
        GradientSetting outlineGradient,
        float alpha
    ) {
        this.matrix = matrix;
        this.fillBuffer = fillBuffer;
        this.lineBuffer = lineBuffer;
        this.fillGradient = fillGradient;
        this.outlineGradient = outlineGradient;
        this.alpha = Math.max(0.0F, Math.min(1.0F, alpha));
    }

    public void emitFace(Vector3f origin, Vector3f uEdge, Vector3f vEdge, int uSegments, int vSegments, boolean outline) {
        int columns = Math.max(1, uSegments);
        int rows = Math.max(1, vSegments);
        this.emitFill(origin, uEdge, vEdge, columns, rows);
        if (outline && this.lineBuffer != null && this.outlineGradient != null) {
            this.emitOutline(origin, uEdge, vEdge, columns, rows);
        }
    }

    private void emitFill(Vector3f origin, Vector3f uEdge, Vector3f vEdge, int columns, int rows) {
        if (this.fillBuffer == null || this.fillGradient == null) {
            return;
        }

        float width = uEdge.length();
        float height = vEdge.length();
        int base = this.fillVertices;

        for (int row = 0; row <= rows; row++) {
            float t = (float) row / rows;
            for (int column = 0; column <= columns; column++) {
                float s = (float) column / columns;
                this.fillBuffer.pos(this.matrix, origin.x + uEdge.x * s + vEdge.x * t, origin.y + uEdge.y * s + vEdge.y * t, origin.z + uEdge.z * s + vEdge.z * t)
                    .color(this.colorAt(this.fillGradient, s * width, t * height, width, height));
            }
        }

        int stride = columns + 1;
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                int topLeft = base + row * stride + column;
                int topRight = topLeft + 1;
                int bottomLeft = topLeft + stride;
                int bottomRight = bottomLeft + 1;
                this.fillBuffer.putIndices(new int[]{topLeft, bottomLeft, bottomRight, topLeft, bottomRight, topRight});
            }
        }

        this.fillVertices += stride * (rows + 1);
    }

    private void emitOutline(Vector3f origin, Vector3f uEdge, Vector3f vEdge, int columns, int rows) {
        float width = uEdge.length();
        float height = vEdge.length();
        this.emitEdge(origin, uEdge, vEdge, 0.0F, 0.0F, 1.0F, 0.0F, columns, width, height);
        this.emitEdge(origin, uEdge, vEdge, 0.0F, 1.0F, 1.0F, 1.0F, columns, width, height);
        this.emitEdge(origin, uEdge, vEdge, 0.0F, 0.0F, 0.0F, 1.0F, rows, width, height);
        this.emitEdge(origin, uEdge, vEdge, 1.0F, 0.0F, 1.0F, 1.0F, rows, width, height);
    }

    private void emitEdge(
        Vector3f origin, Vector3f uEdge, Vector3f vEdge, float s0, float t0, float s1, float t1, int segments, float width, float height
    ) {
        for (int i = 0; i < segments; i++) {
            float a = (float) i / segments;
            float b = (float) (i + 1) / segments;
            this.lineVertex(origin, uEdge, vEdge, s0 + (s1 - s0) * a, t0 + (t1 - t0) * a, width, height);
            this.lineVertex(origin, uEdge, vEdge, s0 + (s1 - s0) * b, t0 + (t1 - t0) * b, width, height);
            this.lineBuffer.putIndices(new int[]{this.lineVertices, this.lineVertices + 1});
            this.lineVertices += 2;
        }
    }

    private void lineVertex(Vector3f origin, Vector3f uEdge, Vector3f vEdge, float s, float t, float width, float height) {
        this.lineBuffer.pos(this.matrix, origin.x + uEdge.x * s + vEdge.x * t, origin.y + uEdge.y * s + vEdge.y * t, origin.z + uEdge.z * s + vEdge.z * t)
            .color(this.colorAt(this.outlineGradient, s * width, t * height, width, height));
    }

    private int colorAt(GradientSetting gradient, float x, float y, float width, float height) {
        Color color = gradient.getColorAtPoint(x, y, Math.max(width, 1.0E-4F), Math.max(height, 1.0E-4F));
        int alpha = Math.round(color.getAlpha() * this.alpha);
        return ColorUtil.toArgb(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
    }
}
