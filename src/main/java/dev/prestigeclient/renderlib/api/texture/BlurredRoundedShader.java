package dev.prestigeclient.renderlib.api.texture;

import dev.prestigeclient.renderlib.api.path.RLPath;
import dev.prestigeclient.renderlib.api.shader.RLShader;
import dev.prestigeclient.renderlib.api.uniform.RLUniforms;
import dev.prestigeclient.renderlib.impl.api.RenderAPI;
import dev.prestigeclient.renderlib.impl.core.GlStateGuard;
import dev.prestigeclient.renderlib.impl.render.blur.BlurState;
import dev.prestigeclient.renderlib.impl.render.mgr.RenderPass;
import java.awt.Color;
import java.util.function.Consumer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

public class BlurredRoundedShader extends RLShader implements AutoCloseable {
    private final BlurState blurState;
    private float blurRadius = 10.0F;
    private float saturation = 1.0F;
    private float tintStrength = 0.0F;
    private Color tintColor = Color.BLACK;
    private float opacity = 1.0F;
    private float smoothness = 1.0F;

    public BlurredRoundedShader() {
        this(new BlurState());
    }

    public BlurredRoundedShader(BlurState blurState) {
        super(RLPath.of("shaders/post.vert"), RLPath.of("assets/tulip", "shaders/post/blur_composite.fsh"));
        this.blurState = blurState;
    }

    public BlurredRoundedShader setBlurRadius(float blurRadius) {
        this.blurRadius = Math.max(blurRadius, 0.0F);
        return this;
    }

    public BlurredRoundedShader setSaturation(float saturation) {
        this.saturation = saturation;
        return this;
    }

    public BlurredRoundedShader setTint(Color color, float strength) {
        this.tintColor = color;
        this.tintStrength = Math.max(0.0F, Math.min(strength, 1.0F));
        return this;
    }

    public BlurredRoundedShader setOpacity(float opacity) {
        this.opacity = Math.max(0.0F, Math.min(opacity, 1.0F));
        return this;
    }

    public BlurredRoundedShader setSmoothness(float smoothness) {
        this.smoothness = Math.max(smoothness, 0.0001F);
        return this;
    }

    public float getBlurRadius() {
        return this.blurRadius;
    }

    public BlurState getBlurState() {
        return this.blurState;
    }

    public void draw(float x, float y, float width, float height, float radius) {
        this.draw(x, y, width, height, radius, radius, radius, radius);
    }

    public void draw(float x, float y, float width, float height, float topLeft, float topRight, float bottomLeft, float bottomRight) {
        if (width <= 0.0F || height <= 0.0F || this.opacity <= 0.0F) {
            return;
        }

        Window window = MinecraftClient.getInstance().getWindow();
        int framebufferWidth = window.getFramebufferWidth();
        int framebufferHeight = window.getFramebufferHeight();
        if (framebufferWidth <= 0 || framebufferHeight <= 0) {
            return;
        }

        float scale = RenderAPI.getMinecraftScale();
        float pixelX = x * scale;
        float pixelY = framebufferHeight - (y + height) * scale;
        float pixelWidth = width * scale;
        float pixelHeight = height * scale;
        float maxRadius = Math.min(pixelWidth, pixelHeight) * 0.5F;

        try (GlStateGuard guard = GlStateGuard.capture()) {
            int blurred = this.blurState.blur(RenderAPI.getMinecraftColorTexture(), framebufferWidth, framebufferHeight, this.blurRadius * scale);

            Consumer<RLUniforms> uniforms = u -> u.set1i("BlurSampler", 0)
                .set2f("InputResolution", framebufferWidth, framebufferHeight)
                .set2f("uLocation", pixelX, pixelY)
                .set2f("uSize", pixelWidth, pixelHeight)
                .set4f(
                    "cornerRadii",
                    Math.min(bottomLeft * scale, maxRadius),
                    Math.min(bottomRight * scale, maxRadius),
                    Math.min(topLeft * scale, maxRadius),
                    Math.min(topRight * scale, maxRadius)
                )
                .set1f("Saturation", this.saturation)
                .set1f("BackgroundTint", this.tintStrength)
                .set3f("TintColor", this.tintColor.getRed() / 255.0F, this.tintColor.getGreen() / 255.0F, this.tintColor.getBlue() / 255.0F)
                .set1f("Opacity", this.opacity)
                .set1f("smoothness", this.smoothness)
                .set1i("useVariableWidth", 0)
                .set1i("numSegments", 0)
                .set1i("alignRight", 0);

            RenderPass pass = RenderPass.builder()
                .target(RenderAPI.getMinecraftFramebuffer(), framebufferWidth, framebufferHeight)
                .shaderProgram(this.program)
                .source(0, blurred)
                .uniforms(uniforms)
                .build();
            GL11.glEnable(GL11.GL_BLEND);
            GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
            pass.dispatchFullscreen();
        }
    }

    @Override
    public void close() {
        this.blurState.close();
    }
}
