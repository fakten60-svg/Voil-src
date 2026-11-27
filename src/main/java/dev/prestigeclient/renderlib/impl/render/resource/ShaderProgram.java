package dev.prestigeclient.renderlib.impl.render.resource;

import dev.prestigeclient.renderlib.api.uniform.RLUniforms;
import dev.prestigeclient.renderlib.impl.api.RenderAPI;
import java.lang.ref.Cleaner.Cleanable;
import org.lwjgl.opengl.GL20;

public final class ShaderProgram implements AutoCloseable {
    private final int glId;
    public final String name;
    private final Cleanable cleanable;

    public ShaderProgram(int i, String s) {
        this.glId = i;
        this.name = s;
        this.cleanable = RenderAPI.GL_CLEANER.register(this, () -> RenderAPI.runNowOrPostpone(() -> {
            RLUniforms.forget(i);
            GL20.glDeleteProgram(i);
        }));
    }

    @Override
    public void close() {
        this.cleanable.clean();
    }

    public int glId() {
        return this.glId;
    }
}
