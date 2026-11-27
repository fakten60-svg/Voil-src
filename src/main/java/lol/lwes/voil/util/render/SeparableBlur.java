package lol.lwes.voil.util.render;

import dev.prestigeclient.renderlib.api.uniform.RLUniforms;
import dev.prestigeclient.renderlib.impl.render.mgr.RenderPass;
import dev.prestigeclient.renderlib.impl.render.resource.ShaderProgram;
import java.nio.ByteBuffer;
import java.util.function.Consumer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

public final class SeparableBlur {
    public static final int GAUSSIAN = 0;
    public static final int BOX = 1;
    private static final int MAX_TAPS = 32;
    private static final String VERTEX_SOURCE = """
        #version 330 core
        out vec2 texCoord;

        void main() {
            vec2 pos = vec2(float(gl_VertexID & 1), float(gl_VertexID >> 1));
            texCoord = pos;
            gl_Position = vec4(pos * 2.0 - 1.0, 0.0, 1.0);
        }
        """;
    private static final String FRAGMENT_SOURCE = """
        #version 330 core
        uniform sampler2D texSampler;
        uniform vec2 direction;
        uniform float radius;
        uniform float stepSize;
        uniform int boxFilter;
        in vec2 texCoord;
        out vec4 fragColor;

        void main() {
            vec2 texel = direction / vec2(textureSize(texSampler, 0));
            float sigma = max(radius * 0.5, 0.0001);
            vec4 sum = vec4(0.0);
            float weightSum = 0.0;
            for (float offset = -radius; offset <= radius; offset += stepSize) {
                float weight = boxFilter == 1 ? 1.0 : exp(-(offset * offset) / (2.0 * sigma * sigma));
                sum += texture(texSampler, texCoord + texel * offset) * weight;
                weightSum += weight;
            }
            fragColor = sum / max(weightSum, 0.0001);
        }
        """;
    private static SeparableBlur instance;
    private ShaderProgram program;
    private int width;
    private int height;
    private final int[] textures = new int[2];
    private final int[] framebuffers = new int[2];

    private SeparableBlur() {
    }

    public static SeparableBlur getInstance() {
        if (instance == null) {
            instance = new SeparableBlur();
        }
        return instance;
    }

    public int blur(int sourceTexture, int width, int height, float radius, int mode) {
        if (sourceTexture == 0 || width <= 0 || height <= 0) {
            return 0;
        }

        if (radius <= 0.0F) {
            return sourceTexture;
        }

        this.ensureProgram();
        this.ensureTargets(width, height);
        float step = Math.max(1.0F, (float) Math.ceil(radius / MAX_TAPS));
        int boxFilter = mode == BOX ? 1 : 0;
        RenderPass.setupPostState();
        this.pass(sourceTexture, 0, 1.0F, 0.0F, radius, step, boxFilter);
        this.pass(this.textures[0], 1, 0.0F, 1.0F, radius, step, boxFilter);
        return this.textures[1];
    }

    private void pass(int source, int target, float dirX, float dirY, float radius, float step, int boxFilter) {
        Consumer<RLUniforms> setup = uniforms -> uniforms.set1i("texSampler", 0)
            .set2f("direction", dirX, dirY)
            .set1f("radius", radius)
            .set1f("stepSize", step)
            .set1i("boxFilter", boxFilter);
        RenderPass.builder()
            .target(this.framebuffers[target], this.width, this.height)
            .source(0, source)
            .shaderProgram(this.program)
            .clear()
            .uniforms(setup)
            .build()
            .dispatchFullscreen();
    }

    private void ensureProgram() {
        if (this.program != null) {
            return;
        }

        int vertex = compile(GL20.GL_VERTEX_SHADER, VERTEX_SOURCE);
        int fragment = compile(GL20.GL_FRAGMENT_SHADER, FRAGMENT_SOURCE);
        int id = GL20.glCreateProgram();
        GL20.glAttachShader(id, vertex);
        GL20.glAttachShader(id, fragment);
        GL20.glLinkProgram(id);
        GL20.glDeleteShader(vertex);
        GL20.glDeleteShader(fragment);
        if (GL20.glGetProgrami(id, GL20.GL_LINK_STATUS) == 0) {
            String log = GL20.glGetProgramInfoLog(id);
            GL20.glDeleteProgram(id);
            throw new IllegalStateException("Failed to link blur program: " + log);
        }

        this.program = new ShaderProgram(id, "separable_blur");
    }

    private static int compile(int type, String source) {
        int shader = GL20.glCreateShader(type);
        GL20.glShaderSource(shader, source);
        GL20.glCompileShader(shader);
        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == 0) {
            String log = GL20.glGetShaderInfoLog(shader);
            GL20.glDeleteShader(shader);
            throw new IllegalStateException("Failed to compile blur shader: " + log);
        }
        return shader;
    }

    private void ensureTargets(int width, int height) {
        if (this.width == width && this.height == height && this.textures[0] != 0) {
            return;
        }

        this.release();
        this.width = width;
        this.height = height;
        int previousFramebuffer = GL11.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING);
        int previousTexture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);

        for (int i = 0; i < 2; i++) {
            int texture = GL11.glGenTextures();
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL30.GL_RGBA16F, width, height, 0, GL11.GL_RGBA, GL11.GL_FLOAT, (ByteBuffer) null);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL30.GL_CLAMP_TO_EDGE);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL30.GL_CLAMP_TO_EDGE);
            this.textures[i] = texture;

            int framebuffer = GL30.glGenFramebuffers();
            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebuffer);
            GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, texture, 0);
            this.framebuffers[i] = framebuffer;
        }

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, previousFramebuffer);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, previousTexture);
    }

    public void release() {
        for (int i = 0; i < 2; i++) {
            if (this.framebuffers[i] != 0) {
                GL30.glDeleteFramebuffers(this.framebuffers[i]);
                this.framebuffers[i] = 0;
            }
            if (this.textures[i] != 0) {
                GL11.glDeleteTextures(this.textures[i]);
                this.textures[i] = 0;
            }
        }
        this.width = 0;
        this.height = 0;
    }
}
