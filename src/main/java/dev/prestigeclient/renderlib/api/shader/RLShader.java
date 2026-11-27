package dev.prestigeclient.renderlib.api.shader;

import dev.prestigeclient.renderlib.api.path.RLPath;
import dev.prestigeclient.renderlib.impl.render.mgr.ShaderManager;
import dev.prestigeclient.renderlib.impl.render.resource.ShaderProgram;

public abstract class RLShader {
    protected static final long time = System.currentTimeMillis();
    protected final ShaderProgram program;

    protected RLShader(RLPath rlpath, RLPath rlpath1, boolean flag) {
        this.program = ShaderManager.loadShader(rlpath, rlpath1, flag);
    }

    protected RLShader(RLPath rlpath, RLPath rlpath1) {
        this(rlpath, rlpath1, false);
    }

    public ShaderProgram getProgram() {
        return this.program;
    }
}
