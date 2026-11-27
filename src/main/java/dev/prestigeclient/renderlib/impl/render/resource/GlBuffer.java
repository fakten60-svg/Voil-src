package dev.prestigeclient.renderlib.impl.render.resource;

import dev.prestigeclient.renderlib.impl.api.RenderAPI;
import java.lang.ref.Cleaner.Cleanable;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class GlBuffer implements AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(GlBuffer.class);
    private static final int SCRATCH_TARGET = 36663;
    private final int bufferId;
    private final AtomicLong size;
    private final Cleanable cleanable;

    public GlBuffer(int i, AtomicLong atomiclong) {
        this(i, atomiclong, true);
    }

    public GlBuffer(int i, AtomicLong atomiclong, boolean flag) {
        this.bufferId = i;
        this.size = atomiclong;
        if (flag) {
            this.cleanable = RenderAPI.GL_CLEANER.register(this, () -> RenderAPI.runNowOrPostpone(() -> GL15.glDeleteBuffers(i)));
        } else {
            this.cleanable = RenderAPI.dummyCleanable;
        }
    }

    public AtomicLong size() {
        return this.size;
    }

    public int getId() {
        return this.bufferId;
    }

    @Override
    public void close() {
        this.cleanable.clean();
    }

    public static GlBuffer create(boolean flag) {
        return new GlBuffer(GL15.glGenBuffers(), new AtomicLong(0L), flag);
    }

    public static GlBuffer create() {
        return create(true);
    }

    public void unmapBuffer() {
        GL15.glBindBuffer(36663, this.bufferId);
        GL15.glUnmapBuffer(36663);
        GL15.glBindBuffer(36663, 0);
    }

    public void setBufferSubData(long i, ByteBuffer bytebuffer) {
        GL15.glBindBuffer(36663, this.bufferId);
        GL15.glBufferSubData(36663, i, bytebuffer);
        GL15.glBindBuffer(36663, 0);
    }

    public void copyBufferSubData(int i, long j, long k, long l) {
        GL15.glBindBuffer(36662, this.bufferId);
        GL15.glBindBuffer(36663, i);
        GL31.glCopyBufferSubData(36662, 36663, j, k, l);
        GL15.glBindBuffer(36662, 0);
        GL15.glBindBuffer(36663, 0);
    }

    public ByteBuffer mapBuffer(long i, long j, int k) {
        GL15.glBindBuffer(36663, this.bufferId);
        ByteBuffer bytebuffer = GL30.glMapBufferRange(36663, i, j, k);
        GL15.glBindBuffer(36663, 0);
        return bytebuffer;
    }

    public void setBufferData(int i, long j) {
        GL15.glBindBuffer(36663, this.bufferId);
        GL15.glBufferData(36663, j, i);
        GL15.glBindBuffer(36663, 0);
        this.size.set(j);
    }

    public void setBufferData(int i, ByteBuffer bytebuffer) {
        GL15.glBindBuffer(36663, this.bufferId);
        GL15.glBufferData(36663, bytebuffer, i);
        GL15.glBindBuffer(36663, 0);
        this.size.set(bytebuffer.remaining());
    }

    public void flushMappedBufferChanges(long i, long j) {
        GL15.glBindBuffer(36663, this.bufferId);
        GL30.glFlushMappedBufferRange(36663, i, j);
        GL15.glBindBuffer(36663, 0);
    }

    public void setDataGrowIfNeeded(int i, ByteBuffer bytebuffer) {
        if (bytebuffer.remaining() > this.size.get()) {
            this.setBufferData(i, bytebuffer);
        } else {
            this.setBufferSubData(0L, bytebuffer);
        }
    }
}
