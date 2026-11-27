package dev.prestigeclient.renderlib.impl.render;

import dev.prestigeclient.renderlib.impl.render.mgr.RenderPass;
import dev.prestigeclient.renderlib.impl.render.resource.ShaderProgram;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;

public class PostPipeline {
    private final List<Pass> passes = new ArrayList<>();

    public PostPipeline addPass(ShaderProgram program, Consumer<RenderPass> setup, int targetFramebuffer) {
        this.passes.add(new Pass(Objects.requireNonNull(program, "program"), setup, targetFramebuffer));
        return this;
    }

    public int passCount() {
        return this.passes.size();
    }

    public void draw() {
        if (this.passes.isEmpty()) {
            return;
        }

        Window window = MinecraftClient.getInstance().getWindow();
        int width = window.getFramebufferWidth();
        int height = window.getFramebufferHeight();
        if (width <= 0 || height <= 0) {
            return;
        }

        for (Pass pass : this.passes) {
            RenderPass renderPass = pass.renderPass(width, height);
            renderPass.begin();
            if (pass.setup != null) {
                pass.setup.accept(renderPass);
            }

            renderPass.drawFullscreen();
        }
    }

    private static final class Pass {
        private final ShaderProgram program;
        private final Consumer<RenderPass> setup;
        private final int targetFramebuffer;
        private RenderPass cached;
        private int cachedWidth = -1;
        private int cachedHeight = -1;

        private Pass(ShaderProgram program, Consumer<RenderPass> setup, int targetFramebuffer) {
            this.program = program;
            this.setup = setup;
            this.targetFramebuffer = targetFramebuffer;
        }

        private RenderPass renderPass(int width, int height) {
            if (this.cached == null || this.cachedWidth != width || this.cachedHeight != height) {
                this.cached = RenderPass.builder().target(this.targetFramebuffer, width, height).shaderProgram(this.program).build();
                this.cachedWidth = width;
                this.cachedHeight = height;
            }

            return this.cached;
        }
    }
}
