package lol.lwes.voil.util.render;

import dev.prestigeclient.renderlib.api.uniform.RLUniforms;
import dev.prestigeclient.renderlib.impl.render.mgr.RenderPass;
import dev.prestigeclient.renderlib.impl.render.resource.GlTexture;
import dev.prestigeclient.renderlib.impl.render.state.StateApplier;
import java.util.Objects;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL13C;

public record TextureStateApplier(GlTexture texture) implements StateApplier {
    private static final String SAMPLER_UNIFORM = "texSampler";

    @Override
    public final boolean equals(Object object) {
        return this == object || object instanceof TextureStateApplier texturestateapplier1 && Objects.equals(this.texture, texturestateapplier1.texture);
    }

    @Override
    public final String toString() {
        return "bo[" + "bh=" + this.texture + "]";
    }

    @Override
    public final int hashCode() {
        return 0 + Objects.hashCode(this.texture);
    }

    @Override
    public void apply(RenderPass renderpass) {
        GL13C.glActiveTexture(33984);
        GL11C.glBindTexture(3553, this.texture.tex);
        RLUniforms.get(renderpass.shaderProgram.glId()).set1i(SAMPLER_UNIFORM, 0);
    }
}
