package lol.lwes.voil.util.render;

import dev.prestigeclient.renderlib.api.uniform.RLUniforms;
import dev.prestigeclient.renderlib.impl.render.mgr.RenderPass;
import dev.prestigeclient.renderlib.impl.render.state.StateApplier;
import java.util.Objects;
import java.util.function.Consumer;

public record UniformStateApplier(Consumer<RLUniforms> uniformSetter) implements StateApplier {
    @Override
    public final boolean equals(Object object) {
        return this == object
            || object instanceof UniformStateApplier uniformstateapplier1 && Objects.equals(this.uniformSetter, uniformstateapplier1.uniformSetter);
    }

    @Override
    public final String toString() {
        return "bt[" + "bG=" + this.uniformSetter + "]";
    }

    @Override
    public final int hashCode() {
        return 0 + Objects.hashCode(this.uniformSetter);
    }

    @Override
    public void apply(RenderPass renderpass) {
        if (this.uniformSetter != null) {
            this.uniformSetter.accept(RLUniforms.get(renderpass.shaderProgram.glId()));
        }
    }
}
