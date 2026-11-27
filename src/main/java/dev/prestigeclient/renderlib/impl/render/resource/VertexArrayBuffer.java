package dev.prestigeclient.renderlib.impl.render.resource;

import dev.prestigeclient.renderlib.impl.api.RenderAPI;
import dev.prestigeclient.renderlib.impl.render.vertex.VertexElement;
import dev.prestigeclient.renderlib.impl.render.vertex.VertexFormat;
import java.lang.ref.Cleaner.Cleanable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.lwjgl.opengl.GL30;

public final class VertexArrayBuffer implements AutoCloseable {
    private final int vao;
    private final VertexArrayBuffer.VertexBinding[] recordedBuffers;
    private final Cleanable cleanable;
    private int countBuffers;

    private VertexArrayBuffer(int i, VertexArrayBuffer.VertexBinding[] avertexarraybuffer$vertexbinding) {
        this(i, avertexarraybuffer$vertexbinding, true);
    }

    private VertexArrayBuffer(int i, VertexArrayBuffer.VertexBinding[] avertexarraybuffer$vertexbinding, boolean flag) {
        this.vao = i;
        this.recordedBuffers = new VertexArrayBuffer.VertexBinding[16];
        if (avertexarraybuffer$vertexbinding != null) {
            System.arraycopy(avertexarraybuffer$vertexbinding, 0, avertexarraybuffer$vertexbinding, 0, avertexarraybuffer$vertexbinding.length);
        }

        this.countBuffers = avertexarraybuffer$vertexbinding == null ? 0 : avertexarraybuffer$vertexbinding.length;
        if (flag) {
            this.cleanable = RenderAPI.GL_CLEANER.register(this, () -> RenderAPI.runNowOrPostpone(() -> GL30.glDeleteVertexArrays(i)));
        } else {
            this.cleanable = RenderAPI.dummyCleanable;
        }
    }

    @Override
    public void close() {
        this.cleanable.clean();
    }

    public static VertexArrayBuffer create(List<VertexArrayBuffer.VertexBinding> list) {
        return create(list, true);
    }

    public static VertexArrayBuffer create(List<VertexArrayBuffer.VertexBinding> list, boolean flag) {
        int i = GL30.glGenVertexArrays();
        VertexArrayBuffer vertexarraybuffer = new VertexArrayBuffer(i, null, flag);
        if (list != null) {
            vertexarraybuffer.rebind(list);
        }

        return vertexarraybuffer;
    }

    public void rebind(List<VertexArrayBuffer.VertexBinding> list) {
        int i = list.size();
        assert i <= 16 : "too many bindings (" + i + ")";
        VertexArrayBuffer.VertexBinding[] avertexarraybuffer$vertexbinding = list.toArray(VertexArrayBuffer.VertexBinding[]::new);
        if (!Arrays.equals(this.recordedBuffers, avertexarraybuffer$vertexbinding)) {
            int j = Math.max(this.countBuffers, i);
            int k = Math.min(this.countBuffers, i);
            GL30.glBindVertexArray(this.vao);

            for (; k < j; k++) {
                if (k >= this.countBuffers) {
                    GL30.glEnableVertexAttribArray(k);
                } else {
                    GL30.glDisableVertexAttribArray(k);
                }
            }

            for (int l = 0; l < i; l++) {
                VertexArrayBuffer.VertexBinding vertexarraybuffer$vertexbinding = (VertexArrayBuffer.VertexBinding)list.get(l);
                GL30.glBindBuffer(34962, vertexarraybuffer$vertexbinding.relevantBuffer.getId());
                VertexElement vertexelement = vertexarraybuffer$vertexbinding.relevantElement;
                GL30.glVertexAttribPointer(
                    l,
                    vertexelement.count(),
                    vertexelement.type(),
                    vertexelement.normalized(),
                    vertexarraybuffer$vertexbinding.stride,
                    vertexarraybuffer$vertexbinding.offset
                );
            }

            GL30.glBindVertexArray(0);
            Arrays.fill(this.recordedBuffers, null);
            System.arraycopy(avertexarraybuffer$vertexbinding, 0, this.recordedBuffers, 0, i);
            this.countBuffers = i;
        }
    }

    public int vao() {
        return this.vao;
    }

    public static VertexArrayBuffer fromVertexFormat(GlBuffer glbuffer, VertexFormat vertexformat) {
        return create(getBindings(glbuffer, vertexformat));
    }

    public static List<VertexArrayBuffer.VertexBinding> getBindings(GlBuffer glbuffer, VertexFormat vertexformat) {
        ArrayList<VertexArrayBuffer.VertexBinding> arraylist = new ArrayList<>(vertexformat.elements.length);

        for (int i = 0; i < vertexformat.elements.length; i++) {
            arraylist.add(vertexformat.createVertexBinding(glbuffer, i));
        }

        return arraylist;
    }

    public int getIndexCount(int i, int j) {
        GL30.glBindBuffer(34963, i);
        int k = GL30.glGetBufferParameteri(34963, 34660);
        GL30.glBindBuffer(34963, 0);
        return k / j;
    }

    public record VertexBinding(GlBuffer relevantBuffer, VertexElement relevantElement, int stride, long offset) {
    }
}
