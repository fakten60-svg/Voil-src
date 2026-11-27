package dev.prestigeclient.renderlib.impl.render.layer.type;

import dev.prestigeclient.renderlib.impl.api.RenderAPI;
import dev.prestigeclient.renderlib.impl.render.mesh.MeshMetadata;
import dev.prestigeclient.renderlib.impl.render.mgr.RenderPass;
import dev.prestigeclient.renderlib.impl.render.resource.GlBuffer;
import dev.prestigeclient.renderlib.impl.render.resource.ShaderProgram;
import dev.prestigeclient.renderlib.impl.render.resource.VertexArrayBuffer;
import dev.prestigeclient.renderlib.impl.render.state.StateApplier;
import dev.prestigeclient.renderlib.impl.render.vertex.VertexFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class RenderLayer {
    protected VertexFormat format;
    protected int drawType;
    protected boolean outline;
    protected StateApplier[] states;
    private RenderPass cachedPass;
    private boolean passCacheable;
    private static final ClassValue<Boolean> OVERRIDES_SETUP = new ClassValue<>() {
        @Override
        protected Boolean computeValue(Class<?> oclass) {
            try {
                return oclass.getMethod("setup", RenderPass.RenderPassBuilder.class).getDeclaringClass() != StateApplier.class;
            } catch (NoSuchMethodException nosuchmethodexception) {
                return Boolean.TRUE;
            }
        }
    };

    public RenderLayer(VertexFormat vertexformat, int i, boolean flag, StateApplier[] astateapplier) {
        this.format = vertexformat;
        this.drawType = i;
        this.outline = flag;
        this.setStates(astateapplier);
    }

    public RenderLayer copy() {
        return new RenderLayer(this.getFormat(), this.getDrawType(), this.isOutline(), this.getStates());
    }

    public VertexFormat getFormat() {
        return this.format;
    }

    public void addStates(StateApplier[] astateapplier) {
        StateApplier[] astateapplier1 = this.getStates();
        StateApplier[] astateapplier2 = new StateApplier[astateapplier1.length + astateapplier.length];
        System.arraycopy(astateapplier1, 0, astateapplier2, 0, astateapplier1.length);
        System.arraycopy(astateapplier, 0, astateapplier2, astateapplier1.length, astateapplier.length);
        this.states = astateapplier2;
        this.invalidatePassCache();
    }

    public void setStates(StateApplier[] astateapplier) {
        assert verifyNoDuplicates(astateapplier).isEmpty() : "duplicate states that shouldnt be duplicated when initializing: "
            + verifyNoDuplicates(astateapplier);
        this.states = astateapplier;
        this.invalidatePassCache();
    }

    public StateApplier[] getStates() {
        StateApplier[] astateapplier = new StateApplier[this.states.length];
        System.arraycopy(this.states, 0, astateapplier, 0, astateapplier.length);
        return astateapplier;
    }

    public boolean isOutline() {
        return this.outline;
    }

    public void draw(RenderAPI.RenderContext renderapi$rendercontext, VertexArrayBuffer vertexarraybuffer, GlBuffer glbuffer, MeshMetadata meshmetadata, int i) {
        assert meshmetadata.format().equals(this.format) : "format mismatch with "
            + this.extractShader().name
            + " instances: "
            + i
            + " our: "
            + Arrays.toString(this.format.elements)
            + " theirs: "
            + Arrays.toString(meshmetadata.format().elements);
        RenderPass renderpass = this.cachedPass;
        if (renderpass == null) {
            RenderPass.RenderPassBuilder renderpass$renderpassbuilder = RenderPass.builder();

            for (StateApplier stateapplier : this.states) {
                stateapplier.setup(renderpass$renderpassbuilder);
            }

            renderpass = renderpass$renderpassbuilder.build();
            if (this.passCacheable) {
                this.cachedPass = renderpass;
            }
        }

        renderpass.begin();
        renderpass.setDefaultUniforms(renderapi$rendercontext);

        for (StateApplier stateapplier1 : this.states) {
            stateapplier1.apply(renderpass);
        }

        if (i > 1) {
            renderpass.drawInstanced(vertexarraybuffer, glbuffer, this.drawType, meshmetadata.countIndices(), i);
        } else {
            renderpass.draw(vertexarraybuffer, glbuffer, this.drawType, meshmetadata.countIndices());
        }

        for (StateApplier stateapplier2 : this.states) {
            stateapplier2.revert();
        }
    }

    public void draw(RenderAPI.RenderContext renderapi$rendercontext, VertexArrayBuffer vertexarraybuffer, GlBuffer glbuffer, MeshMetadata meshmetadata) {
        this.draw(renderapi$rendercontext, vertexarraybuffer, glbuffer, meshmetadata, 1);
    }

    private void invalidatePassCache() {
        this.cachedPass = null;
        boolean flag = true;
        if (this.states != null) {
            for (StateApplier stateapplier : this.states) {
                if (!(stateapplier instanceof StateApplier.TargetFramebufferState)
                    && !(stateapplier instanceof StateApplier.ShaderState)
                    && OVERRIDES_SETUP.get(stateapplier.getClass())) {
                    flag = false;
                    break;
                }
            }
        }

        this.passCacheable = flag;
    }

    protected static Set verifyNoDuplicates(StateApplier[] astateapplier) {
        HashSet hashset = new HashSet(astateapplier.length);
        HashSet hashset1 = new HashSet();

        for (StateApplier stateapplier : astateapplier) {
            if (!stateapplier.canBeRepeated() && !hashset.add(stateapplier.getClass())) {
                hashset1.add(stateapplier.getClass());
            }
        }

        return hashset1;
    }

    public int getDrawType() {
        return this.drawType;
    }

    public ShaderProgram extractShader() {
        for (StateApplier stateapplier : this.states) {
            if (stateapplier instanceof StateApplier.ShaderState(ShaderProgram shaderprogram)) {
                return shaderprogram;
            }
        }

        return null;
    }
}
