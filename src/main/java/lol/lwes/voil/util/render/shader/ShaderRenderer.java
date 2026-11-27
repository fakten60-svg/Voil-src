package lol.lwes.voil.util.render.shader;

import com.mojang.blaze3d.opengl.GlStateManager;
import dev.prestigeclient.renderlib.api.path.RLPath;
import dev.prestigeclient.renderlib.api.uniform.RLUniforms;
import dev.prestigeclient.renderlib.impl.render.layer.type.RenderLayer;
import dev.prestigeclient.renderlib.impl.render.mgr.ShaderManager;
import dev.prestigeclient.renderlib.impl.render.resource.ShaderProgram;
import dev.prestigeclient.renderlib.impl.render.state.StateApplier;
import dev.prestigeclient.renderlib.impl.render.ui.RectInstancer;
import dev.prestigeclient.renderlib.impl.render.ui.elements.PreBuiltRectangleElement;
import dev.prestigeclient.renderlib.impl.render.vertex.VertexFormat;
import dev.prestigeclient.renderlib.impl.render.vertex.VertexFormats;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.util.render.GuiScale;
import lol.lwes.voil.util.render.RenderManager;
import lol.lwes.voil.util.render.UniformStateApplier;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.opengl.GL32;

public class ShaderRenderer {
    public ShaderProgram shader;
    public VertexFormat vertexFormat;
    protected final Map<Integer, RenderLayer> layerCache = new HashMap<>();
    protected final Map<Integer, RectInstancer> instancerCache = new HashMap<>();

    public void drawRect(float f, float f1, float f2, float f3, Consumer<RLUniforms> consumer) {
        this.drawRectWithPadding(f, f1, f2, f3, -1, consumer);
    }

    protected RectInstancer getInstancerCached(RectInstancer rectinstancer, StateApplier[] astateapplier) {
        int i = RenderManager.getCurrentFramebuffer();
        int j = Objects.hash(i, this.shader.glId());
        return this.instancerCache
            .computeIfAbsent(
                j,
                integer -> {
                    StateApplier[] astateapplier2 = new StateApplier[]{
                        StateApplier.framebufferState(i),
                        StateApplier.shaderState(this.shader),
                        StateApplier.scissorState(false, false, 0, 0, 0, 0),
                        StateApplier.flagState(2884, false),
                        StateApplier.blendState(true, true),
                        StateApplier.depthFuncState(515, 515)
                    };
                    StateApplier[] astateapplier3 = Arrays.copyOf(astateapplier2, astateapplier2.length + astateapplier.length);
                    System.arraycopy(astateapplier, 0, astateapplier3, astateapplier2.length, astateapplier.length);
                    return rectinstancer.copyWithLayer(new RenderLayer(this.vertexFormat, 4, false, astateapplier3));
                }
            );
    }

    public void useProgram() {
        GlStateManager._glUseProgram(this.shader.glId());
    }

    public static ShaderProgram loadShader(String s, String s1) {
        if (MinecraftClient.getInstance() == null) {
            throw new IllegalStateException("MinecraftClient not initialized");
        }

        RLPath rlpath = RLPath.of("assets/tulip", s1);
        RLPath rlpath1 = RLPath.of("assets/tulip", s);
        return ShaderManager.loadShader(rlpath, rlpath1, false);
    }

    public void unuseProgram() {
        GlStateManager._glUseProgram(0);
    }

    public void setUniform3f(String s, float f, float f1, float f2) {
        int i;
        if (this.shader != null && (i = this.getUniformLocation(s)) != -1) {
            GL32.glUniform3f(i, f, f1, f2);
        }
    }

    protected RenderLayer getRenderLayer(Consumer<RLUniforms> consumer) {
        int i = RenderManager.getCurrentFramebuffer();
        int j = Objects.hash(
            consumer,
            i,
            StateApplier.scissorState(
                RenderManager.scissorEnabled,
                !RenderManager.scissorEnabled,
                RenderManager.scissorBox[0],
                RenderManager.scissorBox[1],
                RenderManager.scissorBox[2],
                RenderManager.scissorBox[3]
            )
        );
        Function<Integer, RenderLayer> function = integer -> new RenderLayer(
            this.vertexFormat,
            4,
            false,
            new StateApplier[]{
                StateApplier.framebufferState(i),
                StateApplier.shaderState(this.shader),
                StateApplier.flagState(2884, false),
                StateApplier.blendState(true, true),
                StateApplier.depthFuncState(515, 515),
                StateApplier.scissorState(
                    RenderManager.scissorEnabled,
                    !RenderManager.scissorEnabled,
                    RenderManager.scissorBox[0],
                    RenderManager.scissorBox[1],
                    RenderManager.scissorBox[2],
                    RenderManager.scissorBox[3]
                ),
                new UniformStateApplier(consumer)
            }
        );
        return this.layerCache.computeIfAbsent(j, function);
    }

    public void drawRectWithPadding(float f, float f1, float f2, float f3, int i, Consumer<RLUniforms> consumer) {
        if (this.shader != null) {
            float f4 = f - 10.0F;
            float f5 = f1 - 10.0F;
            float f6 = f2 + 20.0F;
            float f7 = f3 + 20.0F;
            RenderManager.uiRenderer.addElement(new PreBuiltRectangleElement(this.getRenderLayer(consumer), f4, f5, f6, f7, i));
        }
    }

    public void setUniformFloats(String s, float[] afloat) {
        int i;
        if (this.shader != null && (i = this.getUniformLocation(s)) != -1) {
            GL32.glUniform1fv(i, afloat);
        }
    }

    public static float getScale() {
        return GuiScale.isScaleOverridden() ? GuiScale.getEffectiveScale() : Math.max(1, VoilClient.mc.getWindow().getScaleFactor());
    }

    public ShaderProgram getShader() {
        return this.shader;
    }

    public void setUniform2f(String s, float f, float f1) {
        int i;
        if (this.shader != null && (i = this.getUniformLocation(s)) != -1) {
            GL32.glUniform2f(i, f, f1);
        }
    }

    public void setUniform4f(String s, float f, float f1, float f2, float f3) {
        int i;
        if (this.shader != null && (i = this.getUniformLocation(s)) != -1) {
            GL32.glUniform4f(i, f, f1, f2, f3);
        }
    }

    public int getUniformLocation(String s) {
        return this.shader == null ? -1 : GlStateManager._glGetUniformLocation(this.shader.glId(), s);
    }

    public void setUniformInt(String s, int i) {
        int j;
        if (this.shader != null && (j = this.getUniformLocation(s)) != -1) {
            GL32.glUniform1i(j, i);
        }
    }

    public void drawRectExpanded(float f, float f1, float f2, float f3, int i, float f4, Consumer<RLUniforms> consumer) {
        if (this.shader != null) {
            float f5 = f - f4;
            float f6 = f1 - f4;
            float f7 = f2 + f4 * 2.0F;
            float f8 = f3 + f4 * 2.0F;
            RenderManager.uiRenderer.addElement(new PreBuiltRectangleElement(this.getRenderLayer(consumer), f5, f6, f7, f8, i));
        }
    }

    public void setUniformFloat(String s, float f) {
        int i;
        if (this.shader != null && (i = this.getUniformLocation(s)) != -1) {
            GL32.glUniform1f(i, f);
        }
    }

    public static ShaderRenderer create(ShaderProgram shaderprogram, VertexFormat vertexformat) {
        ShaderRenderer shaderrenderer = new ShaderRenderer();
        shaderrenderer.shader = shaderprogram;
        shaderrenderer.vertexFormat = vertexformat;
        return shaderrenderer;
    }

    public static ShaderProgram loadShaderForFormat(String s, VertexFormat vertexformat) {
        MinecraftClient minecraftclient = MinecraftClient.getInstance();
        if (minecraftclient == null) {
            throw new IllegalStateException("MinecraftClient not initialized");
        }

        RLPath rlpath;
        if (vertexformat == VertexFormats.POSITION) {
            rlpath = RLPath.of("assets/tulip", "shaders/built/built_position.vsh");
        } else if (vertexformat == VertexFormats.POSITION_UV) {
            rlpath = RLPath.of("assets/tulip", "shaders/built/built_position_tex.vsh");
        } else {
            if (vertexformat != RenderManager.posFormat) {
                throw new IllegalStateException("Unsupported VertexFormat");
            }

            rlpath = RLPath.of("shaders/instance_built_pos.vert");
        }

        RLPath rlpath1 = RLPath.of("assets/tulip", s);
        return ShaderManager.loadShader(rlpath, rlpath1, false);
    }
}
