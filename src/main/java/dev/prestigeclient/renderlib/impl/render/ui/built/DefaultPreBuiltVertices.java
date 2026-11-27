package dev.prestigeclient.renderlib.impl.render.ui.built;

import dev.prestigeclient.renderlib.impl.render.mesh.MeshDataConsumer;
import dev.prestigeclient.renderlib.impl.render.vertex.VertexFormat;
import dev.prestigeclient.renderlib.impl.render.vertex.VertexFormats;

public final class DefaultPreBuiltVertices {
    public static final SimplePreBuiltVertices NONE = new SimplePreBuiltVertices() {
        @Override
        public void initializeData() {
        }

        @Override
        public VertexFormat getFormat() {
            return VertexFormats.POSITION;
        }

        @Override
        public void writeVAO(MeshDataConsumer meshdataconsumer, float f) {
        }
    };
    public static final SimplePreBuiltVertices SQUARE = new SimplePreBuiltVertices() {
        @Override
        public VertexFormat getFormat() {
            return VertexFormats.POSITION;
        }

        @Override
        public void writeVAO(MeshDataConsumer meshdataconsumer, float f) {
            meshdataconsumer.start()
                .floats(0.0F, 1.0F, f)
                .floats(0.0F, 0.0F, f)
                .floats(1.0F, 0.0F, f)
                .floats(1.0F, 1.0F, f)
                .putIndices(new int[]{0, 1, 2, 2, 3, 0})
                .end();
        }
    };
    public static final SimplePreBuiltVertices SQUARE_TEX = new SimplePreBuiltVertices() {
        @Override
        public VertexFormat getFormat() {
            return VertexFormats.POSITION_UV;
        }

        @Override
        public void writeVAO(MeshDataConsumer meshdataconsumer, float f) {
            meshdataconsumer.start()
                .floats(0.0F, 1.0F, f)
                .floats(0.0F, 1.0F)
                .floats(0.0F, 0.0F, f)
                .floats(0.0F, 0.0F)
                .floats(1.0F, 0.0F, f)
                .floats(1.0F, 0.0F)
                .floats(1.0F, 1.0F, f)
                .floats(1.0F, 1.0F)
                .putIndices(new int[]{0, 1, 2, 2, 3, 0})
                .end();
        }
    };
}
