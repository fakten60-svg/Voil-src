package dev.prestigeclient.renderlib.impl.render.ui;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.nio.FloatBuffer;
import java.util.concurrent.atomic.AtomicInteger;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.lwjgl.system.MemoryStack;

public record BufferHelper(String blockName, AtomicInteger pointerIndex, LongSet boundCombos, float[] buffer, int ubo) {
    public BufferHelper(String s) {
        this(s, 4096);
    }

    public BufferHelper(String s, int i) {
        this(s, new AtomicInteger(), new LongOpenHashSet(), new float[i], GL15.glGenBuffers());
        GL15.glBindBuffer(35345, this.ubo);
        GL15.glBufferData(35345, i * 4L, 35048);
        GL15.glBindBuffer(35345, 0);
    }

    public void apply() {
        int i = this.pointerIndex.get();
        if (i > 0) {
            GL15.glBindBuffer(35345, this.ubo);

            try (MemoryStack memorystack = MemoryStack.stackPush()) {
                FloatBuffer floatbuffer = memorystack.mallocFloat(i);
                floatbuffer.put(this.buffer, 0, i).flip();
                GL15.glBufferSubData(35345, 0L, floatbuffer);
            }

            GL15.glBindBuffer(35345, 0);
        }

        this.pointerIndex.set(0);
    }

    public void bind(int i, int j) {
        long k = (long)i << 32 | j & 4294967295L;
        if (this.boundCombos.add(k)) {
            GL31.glUniformBlockBinding(i, GL31.glGetUniformBlockIndex(i, this.blockName), j);
        }

        GL30.glBindBufferBase(35345, j, this.ubo);
    }

    public void pad(float f) {
        this.feed4(f, 0.0F, 0.0F, 0.0F);
    }

    public void pad(Vector2f vector2f) {
        this.feed4(vector2f.x, vector2f.y, 0.0F, 0.0F);
    }

    public void pad(Vector3f vector3f) {
        this.feed4(vector3f.x, vector3f.y, vector3f.z, 0.0F);
    }

    public void feed4(float f, float f1, float f2, float f3) {
        int i = this.pointerIndex.get();
        this.buffer[i] = f;
        this.buffer[i + 1] = f1;
        this.buffer[i + 2] = f2;
        this.buffer[i + 3] = f3;
        this.pointerIndex.set(i + 4);
    }

    public void feed(float[] afloat) {
        int i = this.pointerIndex.get();
        System.arraycopy(afloat, 0, this.buffer, i, afloat.length);
        int k = afloat.length;
        int j = i;
        this.pointerIndex.set(j + k);
    }

    public void feed(Vector4f vector4f) {
        this.feed4(vector4f.x, vector4f.y, vector4f.z, vector4f.w);
    }
}
