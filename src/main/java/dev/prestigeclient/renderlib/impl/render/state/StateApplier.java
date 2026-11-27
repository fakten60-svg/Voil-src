package dev.prestigeclient.renderlib.impl.render.state;

import dev.prestigeclient.renderlib.api.uniform.RLUniforms;
import dev.prestigeclient.renderlib.impl.render.mgr.RenderPass;
import dev.prestigeclient.renderlib.impl.render.resource.ShaderProgram;
import java.util.function.Consumer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

public interface StateApplier {
    static StateApplier apply(Consumer<RenderPass> consumer) {
        return new StateApplier() {
            @Override
            public void apply(RenderPass renderpass) {
                consumer.accept(renderpass);
            }

            @Override
            public boolean canBeRepeated() {
                return true;
            }
        };
    }

    default void apply(RenderPass renderpass) {
    }

    static StateApplier setup(Consumer<RenderPass.RenderPassBuilder> consumer) {
        return new StateApplier() {
            @Override
            public void setup(RenderPass.RenderPassBuilder builder) {
                consumer.accept(builder);
            }

            @Override
            public boolean canBeRepeated() {
                return true;
            }
        };
    }

    default void setup(RenderPass.RenderPassBuilder renderpassbuilder) {
    }

    static StateApplier.GlFlagState flagState(int i, boolean flag) {
        return new StateApplier.GlFlagState(i, flag);
    }

    static StateApplier.GlFlagState flagState(int i, boolean flag, boolean flag1) {
        return new StateApplier.GlFlagState(i, flag, flag1);
    }

    static StateApplier.ScissorState scissorState(boolean flag, boolean flag1, int i, int j, int k, int l) {
        return new StateApplier.ScissorState(flag, flag1, i, j, k, l);
    }

    static StateApplier.SimpleBlendState blendState(boolean flag, boolean flag1) {
        return new StateApplier.SimpleBlendState(flag, flag1);
    }

    default void revert() {
    }

    static StateApplier.DepthFuncState depthFuncState(int i, int j) {
        return new StateApplier.DepthFuncState(i, j);
    }

    static StateApplier.ShaderState shaderState(ShaderProgram shaderprogram) {
        return new StateApplier.ShaderState(shaderprogram);
    }

    static StateApplier.TargetFramebufferState framebufferState(int i) {
        return new StateApplier.TargetFramebufferState(i);
    }

    static StateApplier.BlendStateSeparate blendFuncSeparate(boolean flag, boolean flag1, int i, int j, int k, int l) {
        return new StateApplier.BlendStateSeparate(flag, flag1, i, j, k, l);
    }

    default boolean canBeRepeated() {
        return false;
    }

    static StateApplier.UniformState uniformState(Consumer<RLUniforms> consumer) {
        return new StateApplier.UniformState(consumer);
    }

    record BlendStateSeparate(boolean enable, boolean returnTo, int srcFactorRGB, int dstFactorRgb, int srcFactorAlpha, int dstFactorAlpha)
        implements StateApplier {
        @Override
        public void apply(RenderPass renderpass) {
            if (this.enable) {
                GL11.glEnable(3042);
                GL14.glBlendFuncSeparate(this.srcFactorRGB, this.dstFactorRgb, this.srcFactorAlpha, this.dstFactorAlpha);
            } else {
                GL11.glDisable(3042);
            }
        }

        @Override
        public void revert() {
            if (this.returnTo != this.enable) {
                if (this.returnTo) {
                    GL11.glEnable(3042);
                } else {
                    GL11.glDisable(3042);
                }
            }
        }
    }

    record DepthFuncState(int func, int returnTo) implements StateApplier {
        @Override
        public void apply(RenderPass renderpass) {
            GL11.glDepthFunc(this.func);
        }

        @Override
        public void revert() {
            if (this.returnTo != this.func) {
                GL11.glDepthFunc(this.returnTo);
            }
        }
    }

    record GlFlagState(int flag, boolean state, boolean returnTo) implements StateApplier {
        public GlFlagState(int i, boolean flag) {
            this(i, flag, flag);
        }

        @Override
        public void apply(RenderPass renderpass) {
            if (this.state) {
                GL11.glEnable(this.flag);
            } else {
                GL11.glDisable(this.flag);
            }
        }

        @Override
        public void revert() {
            if (this.returnTo != this.state) {
                if (this.returnTo) {
                    GL11.glEnable(this.flag);
                } else {
                    GL11.glDisable(this.flag);
                }
            }
        }

        @Override
        public boolean canBeRepeated() {
            return true;
        }
    }

    record ScissorState(boolean enable, boolean returnTo, int x, int y, int width, int height) implements StateApplier {
        @Override
        public void apply(RenderPass renderpass) {
            if (this.enable) {
                GL11.glEnable(3089);
                GL11.glScissor(this.x, this.y, this.width, this.height);
            } else {
                GL11.glDisable(3089);
            }
        }

        @Override
        public void revert() {
            if (this.returnTo != this.enable) {
                if (this.returnTo) {
                    GL11.glEnable(3089);
                } else {
                    GL11.glDisable(3089);
                }
            }
        }
    }

    record ShaderState(ShaderProgram prog) implements StateApplier {
        @Override
        public void setup(RenderPass.RenderPassBuilder renderpass$renderpassbuilder) {
            renderpass$renderpassbuilder.shaderProgram(this.prog);
        }
    }

    record SimpleBlendState(boolean enable, boolean returnTo) implements StateApplier {
        @Override
        public void apply(RenderPass renderpass) {
            if (this.enable) {
                GL11.glEnable(3042);
                GL14.glBlendFuncSeparate(770, 771, 1, 771);
            } else {
                GL11.glDisable(3042);
            }
        }

        @Override
        public void revert() {
            if (this.returnTo != this.enable) {
                if (this.returnTo) {
                    GL11.glEnable(3042);
                } else {
                    GL11.glDisable(3042);
                }
            }
        }
    }

    record TargetFramebufferState(int fb) implements StateApplier {
        @Override
        public void setup(RenderPass.RenderPassBuilder renderpass$renderpassbuilder) {
            renderpass$renderpassbuilder.targetFramebuffer(this.fb);
        }
    }

    record UniformState(Consumer<RLUniforms> applier) implements StateApplier {
        @Override
        public void apply(RenderPass renderpass) {
            this.applier.accept(RLUniforms.get(renderpass.shaderProgram.glId()));
        }

        @Override
        public boolean canBeRepeated() {
            return true;
        }
    }
}
