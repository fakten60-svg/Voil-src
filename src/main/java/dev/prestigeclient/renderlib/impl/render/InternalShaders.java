package dev.prestigeclient.renderlib.impl.render;

import dev.prestigeclient.renderlib.api.path.RLPath;
import dev.prestigeclient.renderlib.impl.render.mgr.ShaderManager;
import dev.prestigeclient.renderlib.impl.render.resource.ShaderProgram;

public class InternalShaders {
    public static final ShaderProgram POS_COLOR = ShaderManager.loadShader(RLPath.of("shaders/pos_col.vert"), RLPath.of("shaders/pos_col.frag"), false);
    public static final ShaderProgram POS_COLOR_UV = ShaderManager.loadShader(RLPath.of("shaders/pos_col_uv.vert"), RLPath.of("shaders/pos_col_uv.frag"), false);
    public static final ShaderProgram POS_COLOR_R = ShaderManager.loadShader(
        RLPath.of("shaders/rounded_pos_color.vert"), RLPath.of("shaders/rounded_pos_color.frag"), false
    );
    public static final ShaderProgram RENDERTYPE_TEXT = ShaderManager.loadShader(RLPath.of("shaders/pos_col_uv.vert"), RLPath.of("shaders/text.frag"), false);
    public static final ShaderProgram POST_BLUR = ShaderManager.loadShader(RLPath.of("shaders/post.vert"), RLPath.of("shaders/gaussian.frag"), false);
    public static final ShaderProgram MSDF = ShaderManager.loadShader(
        RLPath.of("shaders/instance_built_pos_tex.vert"), RLPath.of("shaders/msdf_text.frag"), false
    );
    public static final ShaderProgram OUTLINE_SEED = ShaderManager.loadShader(
        RLPath.of("shaders/pos_col_uv.vert"), RLPath.of("shaders/outline_seed.frag"), false
    );
}
