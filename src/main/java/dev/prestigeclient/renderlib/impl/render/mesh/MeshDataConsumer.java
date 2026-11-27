package dev.prestigeclient.renderlib.impl.render.mesh;

import dev.prestigeclient.renderlib.impl.api.DrawType;
import org.joml.Matrix4f;

public interface MeshDataConsumer {
    MeshDataConsumer putFloat(float f);

    MeshDataConsumer end();

    default MeshDataConsumer start() {
        return this.setIndexOffset(this.countCompleteVertices());
    }

    MeshDataConsumer pos(Matrix4f matrix4f, float f, float f1, float f2);

    MeshDataConsumer pos(float f, float f1, float f2);

    MeshDataConsumer color(int i);

    MeshDataConsumer putIndices(int[] aint);

    MeshDataConsumer putIndex(int i);

    default MeshDataConsumer generateIndices(DrawType drawtype) {
        assert this.getIndexOffset() != 0 || this.countCompleteIndices() == 0 : "generateIndices called with no offset being present and indices in this buffer : don't know where to begin generating..?";
        int i1 = this.getIndexOffset();
        int l = this.countCompleteVertices();
        int i = l - i1;
        switch (drawtype) {
            case TRIANGLES:
                l = i / 3;
                l *= 3;
                this.indexCheckAvail(l * 4);

                for (byte b3 = 0; b3 < i; b3 += 3) {
                    this.putIndices(new int[]{b3, b3 + 1, b3 + 2});
                }

                break;
            case QUADS:
                l = i / 4;
                l *= 6;
                this.indexCheckAvail(l * 4);

                for (byte b2 = 0; b2 < i; b2 += 4) {
                    this.putIndices(new int[]{b2, b2 + 1, b2 + 2, b2 + 2, b2 + 3, b2});
                }

                break;
            case CUBE:
                l = i / 8;
                l *= 36;
                this.indexCheckAvail(l * 4);

                for (byte b1 = 0; b1 < i; b1 += 8) {
                    this.setIndexOffset(b1);
                    this.putIndices(new int[]{0, 3, 7, 7, 4, 0, 1, 0, 4, 4, 5, 1, 2, 1, 5, 5, 6, 2, 3, 2, 6, 6, 7, 3, 1, 2, 3, 3, 0, 1, 4, 7, 6, 6, 5, 4});
                }

                break;
            case TRIANGLES_FAN:
                l = (i - 2) * 3;
                this.indexCheckAvail(l * 4);

                for (int l1 = 1; l1 < i - 1; l1++) {
                    this.putIndices(new int[]{0, l1, l1 + 1});
                }

                break;
            case DEBUG_LINE_STRIP:
                l = i - 1;
                l *= 2;
                this.indexCheckAvail(l * 4);

                for (int k1 = 0; k1 < i - 1; k1++) {
                    this.putIndices(new int[]{k1, k1 + 1});
                }

                break;
            case QUAD_STRIP:
                l = i / 2;
                int j1 = l - 1;
                l = j1 * 6;
                this.indexCheckAvail(l * 4);

                for (int j = 0; j < j1; j++) {
                    int k = j * 2;
                    this.putIndices(new int[]{k, k + 1, k + 3, k + 3, k + 2, k});
                }

                break;
            case LINES:
                l = i / 2;
                l *= 2;
                this.indexCheckAvail(l * 4);

                for (byte b0 = 0; b0 < i; b0 += 2) {
                    this.putIndices(new int[]{b0, b0 + 1});
                }

                break;
            default:
                throw new MatchException(null, null);
        }

        return this;
    }

    int countCompleteVertices();

    int countCompleteIndices();

    MeshDataConsumer floats(float[] afloat);

    default MeshDataConsumer floats(float f, float f1, float f2) {
        this.putFloat(f);
        this.putFloat(f1);
        return this.putFloat(f2);
    }

    default MeshDataConsumer floats(float f, float f1) {
        this.putFloat(f);
        return this.putFloat(f1);
    }

    void indexCheckAvail(int i);

    int getIndexOffset();

    void vertexCheckAvail(int i);

    MeshDataConsumer setIndexOffset(int i);
}
