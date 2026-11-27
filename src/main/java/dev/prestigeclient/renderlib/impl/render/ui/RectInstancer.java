package dev.prestigeclient.renderlib.impl.render.ui;

import dev.prestigeclient.renderlib.api.uniform.RLUniforms;
import dev.prestigeclient.renderlib.impl.render.layer.type.RenderLayer;
import dev.prestigeclient.renderlib.impl.render.ui.built.VAOIndexMesh;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import org.joml.Vector4f;

public class RectInstancer {
    public static final int maxRectangleLimit = 1024;
    public final RenderLayer renderLayer;
    protected final List<BufferHelper> buffers = new ArrayList<>();
    protected final List<BufferHelper> extraBuffers = new ArrayList<>();
    public final BiConsumer<Object, List<BufferHelper>> extraDataConsumer;
    public final AtomicInteger rectCount = new AtomicInteger();
    public final List<String> initialBufferNames;
    public final VAOIndexMesh renderingVertices;
    public final boolean usable;
    public float lastZ;

    public RectInstancer(RenderLayer renderlayer, VAOIndexMesh vaoindexmesh, BiConsumer<Object, List<BufferHelper>> biconsumer, String[] astring) {
        this.initialBufferNames = List.of(astring);
        this.renderingVertices = vaoindexmesh;
        this.usable = renderlayer != null;
        this.renderLayer = renderlayer;
        this.extraDataConsumer = biconsumer;
        if (this.usable) {
            this.buffers.add(new BufferHelper("PosOffset"));
            this.buffers.add(new BufferHelper("PosScale"));
            this.buffers.add(new BufferHelper("Color"));
            this.buffers.add(new BufferHelper("Scissor"));

            for (String s : astring) {
                this.extraBuffers.add(new BufferHelper(s));
            }
        }
    }

    public void apply(RLUniforms rluniforms) {
        this.applyNoReset(rluniforms);
        if (this.usable) {
            this.rectCount.set(0);
        }
    }

    public void bindOnly(RLUniforms rluniforms) {
        if (this.usable) {
            int i1 = this.buffers.size();
            int l = this.extraBuffers.size();
            int k = i1;
            int i = k + l;

            for (int j = 0; j < i; j++) {
                BufferHelper bufferhelper1;
                if (j < this.buffers.size()) {
                    bufferhelper1 = this.buffers.get(j);
                } else {
                    List<BufferHelper> list = this.extraBuffers;
                    l = this.buffers.size();
                    k = j;
                    bufferhelper1 = list.get(k - l);
                }

                BufferHelper bufferhelper = bufferhelper1;
                bufferhelper.bind(rluniforms.programId(), j);
            }
        }
    }

    public void addRect(
        float f, float f1, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9, float f10, float f11, float f12, Object object
    ) {
        if (this.usable) {
            this.lastZ = f2;
            this.buffers.get(0).feed4(f, f1, f2, 0.0F);
            this.buffers.get(1).feed4(f3, f4, 1.0F, 1.0F);
            this.buffers.get(2).feed4(f5, f6, f7, f8);
            this.buffers.get(3).feed4(f9, f10, f11, f12);
            this.extraDataConsumer.accept(object, this.extraBuffers);
            this.rectCount.incrementAndGet();
        }
    }

    public void addRect(Vector4f vector4f, Vector4f vector4f1, Vector4f vector4f2, Vector4f vector4f3, Object object) {
        if (this.usable) {
            this.lastZ = vector4f.z;
            this.buffers.get(0).feed(vector4f);
            this.buffers.get(1).feed(vector4f1);
            this.buffers.get(2).feed(vector4f2);
            this.buffers.get(3).feed(vector4f3);
            this.extraDataConsumer.accept(object, this.extraBuffers);
            this.rectCount.incrementAndGet();
        }
    }

    public RectInstancer copyWithLayer(RenderLayer renderlayer) {
        return new RectInstancer(renderlayer, this.renderingVertices, this.extraDataConsumer, this.initialBufferNames.toArray(new String[0]));
    }

    public void applyNoReset(RLUniforms rluniforms) {
        if (this.usable) {
            int i1 = this.buffers.size();
            int l = this.extraBuffers.size();
            int k = i1;
            int i = k + l;

            for (int j = 0; j < i; j++) {
                BufferHelper bufferhelper1;
                if (j < this.buffers.size()) {
                    bufferhelper1 = this.buffers.get(j);
                } else {
                    List<BufferHelper> list = this.extraBuffers;
                    l = this.buffers.size();
                    k = j;
                    bufferhelper1 = list.get(k - l);
                }

                BufferHelper bufferhelper = bufferhelper1;
                bufferhelper.bind(rluniforms.programId(), j);
                bufferhelper.apply();
            }
        }
    }
}
