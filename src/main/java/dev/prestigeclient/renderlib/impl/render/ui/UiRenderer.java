package dev.prestigeclient.renderlib.impl.render.ui;

import dev.prestigeclient.renderlib.api.uniform.RLUniforms;
import dev.prestigeclient.renderlib.impl.api.RenderAPI;
import dev.prestigeclient.renderlib.impl.render.layer.type.RenderLayer;
import dev.prestigeclient.renderlib.impl.render.mesh.GlBufferMeshBuilder;
import dev.prestigeclient.renderlib.impl.render.mesh.MeshMetadata;
import dev.prestigeclient.renderlib.impl.render.resource.GlBuffer;
import dev.prestigeclient.renderlib.impl.render.resource.VertexArrayBuffer;
import dev.prestigeclient.renderlib.impl.render.state.StateApplier;
import dev.prestigeclient.renderlib.impl.render.ui.built.DefaultPreBuiltVertices;
import dev.prestigeclient.renderlib.impl.render.ui.built.PreBuiltVertices;
import dev.prestigeclient.renderlib.impl.render.ui.built.VAOIndexMesh;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import net.minecraft.client.MinecraftClient;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL33;

public class UiRenderer {
    protected final GlBuffer vertexBuffer = GlBuffer.create();
    protected final GlBuffer indexBuffer = GlBuffer.create();
    protected final VertexArrayBuffer vab = VertexArrayBuffer.create(null);
    protected final List<UiRenderer.DrawTask> tasks = new ArrayList<>();
    protected final Set<RectInstancer> seenInstancers = new HashSet<>();
    protected final Set<RectInstancer> appliedThisFrame = new HashSet<>();
    public float currentZ = 0.5F;
    public static final boolean debuggingZ = false;

    public UiRenderer() {
        this.vertexBuffer.setBufferData(35048, 1024L);
        this.indexBuffer.setBufferData(35048, 128L);
    }

    public void clear() {
        this.currentZ = 0.5F;
        this.tasks.clear();

        for (RectInstancer rectinstancer : this.seenInstancers) {
            rectinstancer.rectCount.set(0);
        }

        this.seenInstancers.clear();
        this.appliedThisFrame.clear();
    }

    public void addElement(RenderElement renderelement) {
        RenderState renderstate = renderelement.state();
        UiRenderer.InternalState uirenderer$internalstate = new UiRenderer.InternalState(renderelement, this.currentZ);
        UiRenderer.ElementTask uirenderer$elementtask = this.lastElementTask();
        if (uirenderer$elementtask != null && Objects.equals(uirenderer$elementtask.state, renderstate)) {
            uirenderer$elementtask.items.add(uirenderer$internalstate);
        } else {
            UiRenderer.ElementTask uirenderer$elementtask1 = new UiRenderer.ElementTask(renderstate);
            uirenderer$elementtask1.items.add(uirenderer$internalstate);
            this.tasks.add(uirenderer$elementtask1);
        }

        this.nextZ();
    }

    public void nextZ() {
        this.currentZ -= 0.001F;
    }

    public void draw(RenderAPI.RenderContext renderapi$rendercontext) {
        if (!MinecraftClient.getInstance().isFinishedLoading()) {
            this.clear();
        } else {
            for (UiRenderer.DrawTask uirenderer$drawtask : this.tasks) {
                if (uirenderer$drawtask instanceof UiRenderer.ElementTask uirenderer$elementtask) {
                    this.drawElementTask(renderapi$rendercontext, uirenderer$elementtask);
                } else if (uirenderer$drawtask instanceof UiRenderer.InstanceTask uirenderer$instancetask) {
                    this.drawInstanceTask(renderapi$rendercontext, uirenderer$instancetask);
                }
            }

            this.clear();
        }
    }

    public void addRectangle(RectInstancer rectinstancer, float f, float f1, float f2, float f3, int i, Object object) {
        this.addRectangle(rectinstancer, f, f1, f2, f3, i, null, object);
    }

    public void addRectangle(RectInstancer rectinstancer, float f, float f1, float f2, float f3, int i, Vector4f vector4f, Object object) {
        if (rectinstancer.rectCount.get() < 1024) {
            float f4 = (i >>> 24 & 0xFF) / 255.0F;
            int k = i >>> 16;
            float f5 = (k & 0xFF) / 255.0F;
            k = i >>> 8;
            float f6 = (k & 0xFF) / 255.0F;
            float f7 = (i & 0xFF) / 255.0F;
            float f8;
            float f9;
            float f10;
            float f11;
            if (vector4f == null) {
                f9 = Float.NEGATIVE_INFINITY;
                f8 = Float.NEGATIVE_INFINITY;
                f11 = Float.POSITIVE_INFINITY;
                f10 = Float.POSITIVE_INFINITY;
            } else {
                int j = RenderAPI.getMinecraftWindowFbHeight();
                float f12 = RenderAPI.getMinecraftScale();
                f8 = vector4f.x * f12;
                f9 = j - vector4f.w * f12;
                f10 = vector4f.z * f12;
                f11 = j - vector4f.y * f12;
            }

            int i1 = rectinstancer.rectCount.get();
            rectinstancer.addRect(f, f1, this.currentZ, f2, f3, f5, f6, f7, f4, f8, f9, f10, f11, object);
            this.nextZ();
            this.seenInstancers.add(rectinstancer);
            UiRenderer.InstanceTask uirenderer$instancetask = this.lastInstanceTask();
            if (uirenderer$instancetask != null && uirenderer$instancetask.instancer == rectinstancer) {
                int l = uirenderer$instancetask.count;
                k = uirenderer$instancetask.startInstance;
                if (k + l == i1) {
                    uirenderer$instancetask.count++;
                    return;
                }
            }

            this.tasks.add(new UiRenderer.InstanceTask(rectinstancer, i1, 1));
        }
    }

    private UiRenderer.InstanceTask lastInstanceTask() {
        if (this.tasks.isEmpty()) {
            return null;
        }

        UiRenderer.DrawTask uirenderer$drawtask = this.tasks.get(this.tasks.size() - 1);
        return uirenderer$drawtask instanceof UiRenderer.InstanceTask uirenderer$instancetask ? uirenderer$instancetask : null;
    }

    private UiRenderer.ElementTask lastElementTask() {
        if (this.tasks.isEmpty()) {
            return null;
        }

        UiRenderer.DrawTask uirenderer$drawtask = this.tasks.get(this.tasks.size() - 1);
        return uirenderer$drawtask instanceof UiRenderer.ElementTask uirenderer$elementtask ? uirenderer$elementtask : null;
    }

    private void drawElementTask(RenderAPI.RenderContext renderapi$rendercontext, UiRenderer.ElementTask uirenderer$elementtask) {
        RenderState renderstate = uirenderer$elementtask.state;
        Vector4f vector4f = renderstate.scissor();
        if (vector4f != null) {
            GL33.glEnable(3089);
            int i = RenderAPI.getMinecraftWindowFbHeight();
            float f = RenderAPI.getMinecraftScale();
            GL33.glScissor((int)(vector4f.x * f), (int)(i - vector4f.y * f - vector4f.w * f), (int)(vector4f.z * f), (int)(vector4f.w * f));
        }

        RenderLayer renderlayer = renderstate.layer();
        List<UiRenderer.InternalState> list = uirenderer$elementtask.items;
        if (list.size() == 1) {
            UiRenderer.InternalState uirenderer$internalstate = list.get(0);
            PreBuiltVertices prebuiltvertices = uirenderer$internalstate.element().preBuiltVerticesType();
            if (prebuiltvertices != DefaultPreBuiltVertices.NONE) {
                VAOIndexMesh vaoindexmesh = prebuiltvertices.getVAOIndexMesh();
                if (!vaoindexmesh.anyNull()) {
                    uirenderer$internalstate.element().preDraw(uirenderer$internalstate.depth());
                    renderlayer.draw(renderapi$rendercontext, vaoindexmesh.vaoBuffer(), vaoindexmesh.indexBuffer(), vaoindexmesh.ourMeshData());
                    if (vector4f != null) {
                        GL33.glDisable(3089);
                    }

                    return;
                }
            }
        }

        this.vab.rebind(VertexArrayBuffer.getBindings(this.vertexBuffer, renderlayer.getFormat()));
        GlBufferMeshBuilder glbuffermeshbuilder = new GlBufferMeshBuilder(renderlayer.getFormat(), this.vertexBuffer, this.indexBuffer);

        int j;
        int k;
        try {
            for (UiRenderer.InternalState uirenderer$internalstate1 : list) {
                RenderElement renderelement = uirenderer$internalstate1.element();
                renderelement.draw(glbuffermeshbuilder, uirenderer$internalstate1.depth());
            }

            j = glbuffermeshbuilder.countCompleteVertices();
            k = glbuffermeshbuilder.countCompleteIndices();
        } catch (Throwable throwable1) {
            try {
                glbuffermeshbuilder.close();
            } catch (Throwable throwable) {
                throwable1.addSuppressed(throwable);
            }

            throw throwable1;
        }

        glbuffermeshbuilder.close();
        renderlayer.draw(renderapi$rendercontext, this.vab, this.indexBuffer, new MeshMetadata(renderlayer.getFormat(), j, k));
        if (vector4f != null) {
            GL33.glDisable(3089);
        }
    }

    private void drawInstanceTask(RenderAPI.RenderContext renderapi$rendercontext, UiRenderer.InstanceTask uirenderer$instancetask) {
        RectInstancer rectinstancer = uirenderer$instancetask.instancer;
        int i = uirenderer$instancetask.startInstance;
        int j = uirenderer$instancetask.count;
        if (j > 0) {
            boolean flag = this.appliedThisFrame.add(rectinstancer);
            RenderLayer renderlayer = rectinstancer.renderLayer.copy();
            renderlayer.addStates(new StateApplier[]{StateApplier.uniformState(rluniforms -> {
                if (flag) {
                    rectinstancer.applyNoReset(rluniforms);
                } else {
                    rectinstancer.bindOnly(rluniforms);
                }

                rluniforms.set1i("uBaseInstance", i);
            })});
            VAOIndexMesh vaoindexmesh = rectinstancer.renderingVertices;
            renderlayer.draw(renderapi$rendercontext, vaoindexmesh.vaoBuffer(), vaoindexmesh.indexBuffer(), vaoindexmesh.ourMeshData(), j);
        }
    }

    public sealed interface DrawTask permits UiRenderer.ElementTask, UiRenderer.InstanceTask {
    }

    public static final class ElementTask implements UiRenderer.DrawTask {
        public final RenderState state;
        public final List<UiRenderer.InternalState> items = new ArrayList<>();

        public ElementTask(RenderState renderstate) {
            this.state = renderstate;
        }
    }

    public static final class InstanceTask implements UiRenderer.DrawTask {
        public final RectInstancer instancer;
        public final int startInstance;
        public int count;

        public InstanceTask(RectInstancer rectinstancer, int i, int j) {
            this.instancer = rectinstancer;
            this.startInstance = i;
            this.count = j;
        }
    }

    public record InternalState(RenderElement element, float depth) {
    }
}
