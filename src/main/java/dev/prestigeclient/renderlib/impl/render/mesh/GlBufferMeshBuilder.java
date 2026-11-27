package dev.prestigeclient.renderlib.impl.render.mesh;

import dev.prestigeclient.renderlib.impl.api.RenderAPI;
import dev.prestigeclient.renderlib.impl.render.resource.GlBuffer;
import dev.prestigeclient.renderlib.impl.render.vertex.VertexFormat;
import java.lang.ref.Cleaner.Cleanable;
import java.nio.ByteBuffer;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

public class GlBufferMeshBuilder implements MeshDataConsumer, AutoCloseable {
    public final VertexFormat format;
    private final GlBuffer vertexBuffer;
    private final GlBuffer indexBuffer;
    private final Cleanable cleanable;
    private ByteBuffer vb_mapped;
    private ByteBuffer ib_mapped;
    private boolean closed = false;
    private int indexOffset = 0;
    private volatile boolean unmapped = false;
    private static final int MAP_FLAGS = 10;

    public GlBufferMeshBuilder(VertexFormat vertexformat, GlBuffer glbuffer, GlBuffer glbuffer1) {
        this.format = vertexformat;
        this.vertexBuffer = glbuffer;
        this.indexBuffer = glbuffer1;
        long i = glbuffer.size().get();
        long j = glbuffer1.size().get();
        if (i != 0L && j != 0L) {
            this.vb_mapped = glbuffer.mapBuffer(0L, i, 10);
            this.ib_mapped = glbuffer1.mapBuffer(0L, j, 10);
            if (this.vb_mapped != null && this.ib_mapped != null) {
                this.cleanable = RenderAPI.GL_CLEANER.register(this, () -> {
                    if (!this.unmapped) {
                        RenderAPI.runNowOrPostpone(() -> {
                            glbuffer.unmapBuffer();
                            glbuffer1.unmapBuffer();
                        });
                    }
                });
            } else {
                throw new IllegalStateException("glMapBufferRange returned null (error: " + GL30.glGetError() + ")");
            }
        } else {
            throw new IllegalStateException("cannot map zero buffer (vb=" + i + ", ib=" + j + ")");
        }
    }

    @Override
    public GlBufferMeshBuilder putFloat(float f) {
        this.vertexCheckAvail(4);
        this.vb_mapped.putFloat(f);
        return this;
    }

    @Override
    public GlBufferMeshBuilder end() {
        this.indexOffset = 0;
        return this;
    }

    @Override
    public void close() {
        if (!this.closed) {
            this.closed = true;
            this.unmapped = true;
            this.cleanable.clean();
            this.vertexBuffer.unmapBuffer();
            this.indexBuffer.unmapBuffer();
            this.vb_mapped = null;
            this.ib_mapped = null;
        }
    }

    @Override
    public GlBufferMeshBuilder pos(Matrix4f matrix4f, float f, float f1, float f2) {
        return this.floats(
            matrix4f.m00() * f + matrix4f.m10() * f1 + matrix4f.m20() * f2 + matrix4f.m30(),
            matrix4f.m01() * f + matrix4f.m11() * f1 + matrix4f.m21() * f2 + matrix4f.m31(),
            matrix4f.m02() * f + matrix4f.m12() * f1 + matrix4f.m22() * f2 + matrix4f.m32()
        );
    }

    @Override
    public GlBufferMeshBuilder pos(float f, float f1, float f2) {
        return this.floats(f, f1, f2);
    }

    private static ByteBuffer grow(GlBuffer glbuffer, ByteBuffer bytebuffer, int i) {
        int j = bytebuffer.limit();
        int l1 = bytebuffer.remaining();
        int k1 = i;
        int k = k1 - l1;
        int l = Math.max(j << 1, j + k);
        int i1 = bytebuffer.position();
        int j1 = getBufferUsage(glbuffer.getId());
        glbuffer.unmapBuffer();
        ByteBuffer bytebuffer1 = MemoryUtil.memAlloc(Math.max(i1, 1));
        bytebuffer1.limit(i1);
        if (i1 > 0) {
            GL15.glBindBuffer(36662, glbuffer.getId());
            GL15.glGetBufferSubData(36662, 0L, bytebuffer1);
            GL15.glBindBuffer(36662, 0);
        }

        glbuffer.setBufferData(j1, l);
        ByteBuffer bytebuffer2 = glbuffer.mapBuffer(0L, l, 10);
        if (bytebuffer2 == null) {
            MemoryUtil.memFree(bytebuffer1);
            throw new IllegalStateException("glMapBufferRange returned null on grow (error: " + GL30.glGetError() + ")");
        } else {
            MemoryUtil.memCopy(bytebuffer1, bytebuffer2);
            MemoryUtil.memFree(bytebuffer1);
            bytebuffer2.position(i1);
            return bytebuffer2;
        }
    }

    @Override
    public GlBufferMeshBuilder color(int i) {
        this.vertexCheckAvail(4);
        this.vb_mapped.putInt(i);
        return this;
    }

    private static int getBufferUsage(int i) {
        GL15.glBindBuffer(36663, i);
        int j = GL15.glGetBufferParameteri(36663, 34661);
        GL15.glBindBuffer(36663, 0);
        return j;
    }

    @Override
    public GlBufferMeshBuilder putIndices(int[] aint) {
        int k = aint.length;
        this.indexCheckAvail(4 * k);

        for (int i : aint) {
            int j = this.indexOffset;
            this.ib_mapped.putInt(j + i);
        }

        return this;
    }

    @Override
    public GlBufferMeshBuilder putIndex(int i) {
        this.indexCheckAvail(4);
        int j = this.indexOffset;
        this.ib_mapped.putInt(j + i);
        return this;
    }

    @Override
    public int countCompleteVertices() {
        return this.vb_mapped.position() / this.format.size;
    }

    @Override
    public int countCompleteIndices() {
        return this.ib_mapped.position() / 4;
    }

    @Override
    public GlBufferMeshBuilder floats(float[] afloat) {
        int i = afloat.length;
        this.vertexCheckAvail(4 * i);

        for (float f : afloat) {
            this.vb_mapped.putFloat(f);
        }

        return this;
    }

    @Override
    public GlBufferMeshBuilder floats(float f, float f1, float f2) {
        this.vertexCheckAvail(12);
        this.vb_mapped.putFloat(f).putFloat(f1).putFloat(f2);
        return this;
    }

    @Override
    public GlBufferMeshBuilder floats(float f, float f1) {
        this.vertexCheckAvail(8);
        this.vb_mapped.putFloat(f).putFloat(f1);
        return this;
    }

    @Override
    public void indexCheckAvail(int i) {
        if (this.ib_mapped.remaining() < i) {
            this.ib_mapped = grow(this.indexBuffer, this.ib_mapped, i);
        }
    }

    @Override
    public int getIndexOffset() {
        return this.indexOffset;
    }

    @Override
    public void vertexCheckAvail(int i) {
        if (this.vb_mapped.remaining() < i) {
            this.vb_mapped = grow(this.vertexBuffer, this.vb_mapped, i);
        }
    }

    @Override
    public GlBufferMeshBuilder setIndexOffset(int i) {
        this.indexOffset = i;
        return this;
    }
}
