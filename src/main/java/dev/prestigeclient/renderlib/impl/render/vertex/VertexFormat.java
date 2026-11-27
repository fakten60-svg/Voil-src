package dev.prestigeclient.renderlib.impl.render.vertex;

import dev.prestigeclient.renderlib.impl.render.resource.GlBuffer;
import dev.prestigeclient.renderlib.impl.render.resource.VertexArrayBuffer;
import java.util.Arrays;
import java.util.Objects;

public class VertexFormat {
    public final VertexElement[] elements;
    public final int size;
    private final int[] elementOffsets;

    public VertexFormat(VertexElement[] avertexelement) {
        this.elements = avertexelement;
        int i = 0;
        this.elementOffsets = new int[avertexelement.length];

        for (int j = 0; j < avertexelement.length; j++) {
            VertexElement vertexelement = avertexelement[j];
            this.elementOffsets[j] = i;
            int l = vertexelement.typeSize();
            int k = vertexelement.count();
            l = k * l;
            k = i;
            i = k + l;
        }

        this.size = i;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else if (object != null && this.getClass() == object.getClass()) {
            VertexFormat vertexformat1 = (VertexFormat)object;
            return this.size == vertexformat1.size
                && Arrays.equals(this.elements, vertexformat1.elements)
                && Arrays.equals(this.elementOffsets, vertexformat1.elementOffsets);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int i = Objects.hash(this.size);
        int l = 31 * i;
        int k = Arrays.hashCode(this.elements);
        int j = l;
        i = j + k;
        l = 31 * i;
        k = Arrays.hashCode(this.elementOffsets);
        j = l;
        return j + k;
    }

    public VertexArrayBuffer.VertexBinding createVertexBinding(GlBuffer glbuffer, int i) {
        if (i >= 0 && i < this.elements.length) {
            VertexElement vertexelement = this.elements[i];
            int j = this.elementOffsets[i];
            return new VertexArrayBuffer.VertexBinding(glbuffer, vertexelement, this.size, j);
        } else {
            throw new IllegalArgumentException("Invalid element index: " + i);
        }
    }
}
