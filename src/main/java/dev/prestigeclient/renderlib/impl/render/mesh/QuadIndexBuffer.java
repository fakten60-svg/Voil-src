package dev.prestigeclient.renderlib.impl.render.mesh;

import dev.prestigeclient.renderlib.impl.render.resource.GlBuffer;
import java.nio.ByteBuffer;
import org.lwjgl.opengl.GL15;
import org.lwjgl.system.MemoryUtil;

public final class QuadIndexBuffer {
    private static final int MIN_QUADS = 256;
    private static final int INDICES_PER_QUAD = 6;
    private static GlBuffer buffer;
    private static int capacity;

    private QuadIndexBuffer() {
    }

    public static GlBuffer forQuads(int quadCount) {
        if (quadCount < 0) {
            throw new IllegalArgumentException("quadCount must be >= 0, got " + quadCount);
        }

        if (buffer == null) {
            buffer = GlBuffer.create();
        }

        if (quadCount > capacity) {
            grow(quadCount);
        }

        return buffer;
    }

    public static int capacity() {
        return capacity;
    }

    private static void grow(int quadCount) {
        int newCapacity = Math.max(MIN_QUADS, Integer.highestOneBit(quadCount - 1) << 1);
        if (newCapacity < quadCount) {
            newCapacity = quadCount;
        }

        ByteBuffer data = MemoryUtil.memAlloc(newCapacity * INDICES_PER_QUAD * Integer.BYTES);

        try {
            for (int quad = 0; quad < newCapacity; quad++) {
                int base = quad * 4;
                data.putInt(base).putInt(base + 1).putInt(base + 2).putInt(base + 2).putInt(base + 3).putInt(base);
            }

            data.flip();
            buffer.setBufferData(GL15.GL_STATIC_DRAW, data);
        } finally {
            MemoryUtil.memFree(data);
        }

        capacity = newCapacity;
    }
}
