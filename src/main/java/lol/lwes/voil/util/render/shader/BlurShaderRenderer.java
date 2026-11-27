package lol.lwes.voil.util.render.shader;

import dev.prestigeclient.renderlib.api.uniform.RLUniforms;
import dev.prestigeclient.renderlib.impl.api.RenderAPI;
import dev.prestigeclient.renderlib.impl.core.GlStateGuard;
import dev.prestigeclient.renderlib.impl.render.layer.type.BufferCollector;
import dev.prestigeclient.renderlib.impl.render.vertex.VertexFormat;
import dev.prestigeclient.renderlib.impl.render.vertex.VertexFormats;
import java.awt.Color;
import java.io.PrintStream;
import java.util.function.Consumer;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.module.ModuleManager;
import lol.lwes.voil.module.render.BlurOverride;
import lol.lwes.voil.util.render.BlurCompositeRenderer;
import lol.lwes.voil.util.render.ColorUtil;
import lol.lwes.voil.util.render.GuiScale;
import lol.lwes.voil.util.render.KawaseBlur;
import lol.lwes.voil.util.render.Render2DUtil;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL33;
import org.lwjgl.opengl.GL41;

public class BlurShaderRenderer extends FramebufferShaderRenderer {
    private final KawaseBlur blurCache = new KawaseBlur();
    private final BlurCompositeRenderer rectRenderer = new BlurCompositeRenderer(() -> this.framebufferWidth);
    private long lastUsed;
    private int framebufferWidth;
    private final int framebufferHeight;
    private int boundTextureId;
    private int blurSamplerLocation;
    private static final long NO_FRAME = -1L;

    public BlurShaderRenderer() {
        this.lastUsed = NO_FRAME;
        this.framebufferWidth = -1;
        this.framebufferHeight = 4;
        this.boundTextureId = -1;
        this.blurSamplerLocation = -1;

        try {
            VertexFormat vertexformat = VertexFormats.POSITION;
            BlurShaderRenderer blurshaderrenderer1 = this;
            blurshaderrenderer1.vertexFormat = vertexformat;
            VertexFormat vertexformat1 = vertexformat;
            this.shader = ShaderRenderer.loadShaderForFormat("shaders/post/blur_composite.fsh", vertexformat1);
        } catch (Exception exception) {
            PrintStream printstream = System.err;
            String s1 = exception.getMessage();
            printstream.println("Failed to load blur composite shader: " + s1);
        }
    }

    private void unbindBlurTexture() {
        GL13.glActiveTexture(33988);
        GL11.glBindTexture(3553, 0);
        GL13.glActiveTexture(33984);
    }

    public void drawBlurRectBuffered(BufferCollector buffercollector, float f, float f1, float f2, float f3, float f4, float f5, float f6) {
        new Matrix4f();
        this.renderRoundedRect(f, f1, f2, f3, f4, f4, f4, f4, f5, f6, 0.4F, 0.0F, 0.0F, 0.0F);
    }

    public void drawColoredRect(BufferCollector buffercollector, float f, float f1, float f2, float f3, float f4, float f5, float f6, Color color) {
        new Matrix4f();
        float f21 = color.getRed() / 255.0F;
        float f7 = color.getBlue() / 255.0F;
        float f8 = color.getGreen() / 255.0F;
        float f9 = f21;
        float f10 = 0.5F;
        float f11 = f6;
        float f12 = f5;
        float f13 = f4;
        float f14 = f4;
        float f15 = f4;
        float f16 = f4;
        float f17 = f3;
        float f18 = f2;
        float f19 = f1;
        float f20 = f;
        this.renderRoundedRect(f20, f19, f18, f17, f16, f15, f14, f13, f12, f11, f10, f9, f8, f7);
    }

    public void renderRoundedRect(
        float f, float f1, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9, float f10, float f11, float f12, float f13
    ) {
        if (this.shader != null && VoilClient.mc.getWindow() != null) {
            Class<BlurOverride> oclass = BlurOverride.class;
            ModuleManager modulemanager = ModuleManager.INSTANCE;
            BlurOverride bluroverride = (BlurOverride)modulemanager.getModule(oclass);
            if (bluroverride != null) {
                oclass = BlurOverride.class;
                modulemanager = ModuleManager.INSTANCE;
                if (modulemanager.isEnabled(oclass)) {
                    f8 = (float)bluroverride.strength.getValue();
                    f9 = (float)bluroverride.saturation.getValue();
                }
            }

            ShaderRenderer.getScale();
            GuiScale.getScaledHeight();
            Math.max(Math.max(f4, f5), Math.max(f6, f7));
            if (bluroverride != null && bluroverride.shadow.isEnabled() && bluroverride.isEnabled()) {
                Color color = Color.black;
                Color color1 = ColorUtil.withAlpha(color, 130);
                Render2DUtil.drawGlow(f, f1, f2, f3, f6, 1.0F, 22.0F, color1);
            }

            this.setBlurRadius(f8);
            this.rectRenderer.render(f, f1, f2, f3, f4, f5, f6, f7, f9, f10, 1.0F, f11, f12, f13);
            if (bluroverride != null && bluroverride.outline.isEnabled() && bluroverride.isEnabled()) {
                Render2DUtil.drawOutline(f, f1, f2, f3, f6, 0.5F, bluroverride.outlineThickness.getFloat(), bluroverride.outlineColor.getColor());
            }
        }
    }

    public void drawRoundedRectArraysA(
        BufferCollector buffercollector,
        float f,
        float f1,
        float[] afloat,
        float[] afloat1,
        float f2,
        float f3,
        float f4,
        float f5,
        float f6,
        float f7,
        float f8,
        float f9,
        float f10,
        float f11,
        boolean flag
    ) {
        new Matrix4f();
        this.renderVariableWidth(f, f1, afloat, afloat1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, flag);
    }

    public void drawRoundedRectArraysMatrix(
        BufferCollector buffercollector,
        Matrix4f matrix4f,
        float f,
        float f1,
        float[] afloat,
        float[] afloat1,
        float f2,
        float f3,
        float f4,
        float f5,
        float f6,
        float f12,
        float f7,
        float f8,
        float f9,
        float f10,
        float f11
    ) {
        this.renderVariableWidth(f, f1, afloat, afloat1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, false);
    }

    @Override
    protected boolean usesFramebuffer() {
        return false;
    }

    private void setBlurRadius(float f) {
        long i = RenderAPI.getFrameCounter();
        if (i != this.lastUsed || this.framebufferWidth <= 0) {
            int j = RenderAPI.getMinecraftColorTexture();
            int k = VoilClient.mc.getWindow().getFramebufferWidth();
            int l = VoilClient.mc.getWindow().getFramebufferHeight();
            if (j != 0 && k > 0 && l > 0) {
                int i1 = this.getBlurIterations(f);

                int j1;
                try (GlStateGuard glstateguard = GlStateGuard.capture(1)) {
                    j1 = this.blurCache.blur(j, k, l, i1);
                }

                if (j1 != 0) {
                    this.lastUsed = i;
                    this.framebufferWidth = j1;
                }
            }
        }
    }

    private int getBlurIterations(float f) {
        if (f <= 10.0F) {
            return 2;
        } else {
            return f <= 30.0F ? 4 : 5;
        }
    }

    public void drawRectBuffered(
        BufferCollector buffercollector,
        float f,
        float f1,
        float f2,
        float f3,
        float f4,
        float f5,
        float f6,
        float f7,
        float f8,
        float f11,
        float f9,
        float f10
    ) {
        new Matrix4f();
        this.renderRoundedRect(f, f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, 0.0F, 0.0F, 0.0F);
    }

    private void bindBlurTexture(int i, int j) {
        if (i > 0) {
            GL13.glActiveTexture(33988);
            GL11.glBindTexture(3553, i);
            GL33.glBindSampler(4, 0);
            if (j != this.boundTextureId) {
                this.boundTextureId = j;
                this.blurSamplerLocation = GL20.glGetUniformLocation(j, "BlurSampler");
                GL41.glProgramUniform1i(j, this.blurSamplerLocation, 4);
            }

            GL13.glActiveTexture(33984);
        }
    }

    public void drawRoundedRectArrays(BufferCollector buffercollector, float f, float f1, float[] afloat, float[] afloat1, float f2, float f3) {
        this.renderRoundedRectArrays(buffercollector, f, f1, afloat, afloat1, f2, f2, f2, f2, f3, 1.5F, 0.3F, 0.0F, 0.0F, 0.0F);
    }

    public void drawRoundedRectSimple(BufferCollector buffercollector, float f, float f1, float f2, float f3, float f4, float f5) {
        new Matrix4f();
        this.renderRoundedRect(f, f1, f2, f3, f4, f4, f4, f4, f5, 1.5F, 0.3F, 0.0F, 0.0F, 0.0F);
    }

    public void drawRoundedRectCorners(BufferCollector buffercollector, float f, float f1, float f2, float f3, float f4, float f5, float f6, float f7, float f8) {
        new Matrix4f();
        this.renderRoundedRect(f, f1, f2, f3, f4, f5, f6, f7, f8, 1.5F, 0.3F, 0.0F, 0.0F, 0.0F);
    }

    public void drawRectBufferedAlt(
        BufferCollector buffercollector,
        float f,
        float f1,
        float f2,
        float f3,
        float f4,
        float f5,
        float f6,
        float f7,
        float f8,
        float f9,
        float f10,
        Color color
    ) {
        if (color == null) {
            color = new Color(0, 0, 0, 255);
        }

        new Matrix4f();
        float f25 = color.getRed() / 255.0F;
        float f11 = color.getBlue() / 255.0F;
        float f12 = color.getGreen() / 255.0F;
        float f13 = f25;
        float f14 = f10;
        float f15 = f9;
        float f16 = f8;
        float f17 = f7;
        float f18 = f6;
        float f19 = f5;
        float f20 = f4;
        float f21 = f3;
        float f22 = f2;
        float f23 = f1;
        float f24 = f;
        this.renderRoundedRect(f24, f23, f22, f21, f20, f19, f18, f17, f16, f15, f14, f13, f12, f11);
    }

    public void renderRoundedRectArrays(
        BufferCollector buffercollector,
        float f,
        float f1,
        float[] afloat,
        float[] afloat1,
        float f2,
        float f3,
        float f4,
        float f5,
        float f6,
        float f7,
        float f8,
        float f9,
        float f10,
        float f11
    ) {
        new Matrix4f();
        this.renderVariableWidth(f, f1, afloat, afloat1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, false);
    }

    public void drawColoredRectAlt(BufferCollector buffercollector, float f, float f1, float f2, float f3, float f4, float f5, float f6, float f7, Color color) {
        if (color == null) {
            color = new Color(0, 0, 0, 255);
        }

        new Matrix4f();
        float f22 = color.getRed() / 255.0F;
        float f8 = color.getBlue() / 255.0F;
        float f9 = color.getGreen() / 255.0F;
        float f10 = f22;
        float f11 = f7;
        float f12 = f6;
        float f13 = f5;
        float f14 = f4;
        float f15 = f4;
        float f16 = f4;
        float f17 = f4;
        float f18 = f3;
        float f19 = f2;
        float f20 = f1;
        float f21 = f;
        this.renderRoundedRect(f21, f20, f19, f18, f17, f16, f15, f14, f13, f12, f11, f10, f9, f8);
    }

    public void drawGlassRect(BufferCollector buffercollector, float f, float f1, float f2, float f3, float f4, float f5, float f6, float f7) {
        new Matrix4f();
        this.renderGlassRect(f, f1, f2, f3, f4, f4, f4, f4, f5, f7, f6, 0.0F, 0.0F, 0.0F, 0.0F);
    }

    public void renderVariableWidth(
        float f,
        float f1,
        float[] afloat,
        float[] afloat1,
        float f2,
        float f3,
        float f4,
        float f5,
        float f6,
        float f7,
        float f8,
        float f9,
        float f10,
        float f11,
        boolean flag
    ) {
        if (this.shader != null && VoilClient.mc.getWindow() != null) {
            if (afloat != null && afloat1 != null && afloat.length == afloat1.length) {
                Class<BlurOverride> oclass = BlurOverride.class;
                ModuleManager modulemanager = ModuleManager.INSTANCE;
                BlurOverride bluroverride = (BlurOverride)modulemanager.getModule(oclass);
                if (bluroverride != null) {
                    oclass = BlurOverride.class;
                    modulemanager = ModuleManager.INSTANCE;
                    if (modulemanager.isEnabled(oclass)) {
                        f6 = (float)bluroverride.strength.getValue();
                        f7 = (float)bluroverride.saturation.getValue();
                    }
                }

                float f12 = ShaderRenderer.getScale();
                float f13 = GuiScale.getScaledHeight();
                int i = Math.min(afloat.length, 32);
                if (i != 0) {
                    float f14 = 0.0F;
                    float f15 = 0.0F;
                    float[] afloat2 = new float[32];
                    float[] afloat3 = new float[32];

                    for (int j = 0; j < i; j++) {
                        f14 += afloat[j];
                        f15 = Math.max(f15, afloat1[j]);
                        afloat2[j] = afloat[j] * f12;
                        afloat3[j] = afloat1[j] * f12;
                    }

                    if (!(f15 <= 0.0F) && !(f14 <= 0.0F)) {
                        int k = flag ? 1 : 0;
                        float f16 = Math.max(Math.max(f2, f3), Math.max(f4, f5)) + 10.0F;
                        this.setBlurRadius(f6);
                        int i1 = (int)f16;
                        float locationX = f * f12;
                        float locationY = -f1 * f12 + f13 * f12 - f14 * f12;
                        float width = f15 * f12;
                        float height = f14 * f12;
                        float saturation = f7;
                        Consumer<RLUniforms> consumer = rluniforms -> {
                            int kx = this.framebufferWidth;
                            int lx = this.shader.glId();
                            this.bindBlurTexture(kx, lx);
                            rluniforms.set1i("useVariableWidth", 1).set1i("numSegments", i).set1i("alignRight", k);
                            rluniforms.setfv("segmentHeights", afloat2);
                            rluniforms.setfv("segmentWidths", afloat3);
                            rluniforms.set4f("cornerRadii", f2 * f12, f3 * f12, f4 * f12, f5 * f12)
                                .set1f("smoothness", 2.0F)
                                .set2f("uLocation", locationX, locationY)
                                .set2f("uSize", width, height)
                                .set1f("Saturation", saturation)
                                .set1f("BackgroundTint", f8)
                                .set3f("TintColor", f9, f10, f11)
                                .set1f("Opacity", 1.0F);
                        };
                        int l = i1;
                        float f17 = f14;
                        float f18 = f15;
                        float f19 = f1;
                        float f20 = f;
                        this.renderFramebuffer(f20, f19, f18, f17, l, consumer);
                        this.unbindBlurTexture();
                    }
                }
            }
        }
    }

    public void renderGlassRect(
        float f,
        float f1,
        float f2,
        float f3,
        float f4,
        float f5,
        float f6,
        float f7,
        float f8,
        float f9,
        float f10,
        float f11,
        float f12,
        float f13,
        float f14
    ) {
        if (this.shader != null && VoilClient.mc.getWindow() != null) {
            Class<BlurOverride> oclass = BlurOverride.class;
            ModuleManager modulemanager = ModuleManager.INSTANCE;
            BlurOverride bluroverride = (BlurOverride)modulemanager.getModule(oclass);
            if (bluroverride != null) {
                oclass = BlurOverride.class;
                modulemanager = ModuleManager.INSTANCE;
                if (modulemanager.isEnabled(oclass)) {
                    f8 = (float)bluroverride.strength.getValue();
                    f10 = (float)bluroverride.saturation.getValue();
                }
            }

            ShaderRenderer.getScale();
            GuiScale.getScaledHeight();
            Math.max(Math.max(f4, f5), Math.max(f6, f7));
            if (bluroverride != null && bluroverride.shadow.isEnabled()) {
                Color color = Color.black;
                Color color1 = ColorUtil.withAlpha(color, 130);
                Render2DUtil.drawGlow(f, f1, f2, f3, f6, 1.0F, 22.0F, color1);
            }

            this.setBlurRadius(f8);
            this.rectRenderer.render(f, f1, f2, f3, f4, f5, f6, f7, f10, f11, f9, f12, f13, f14);
        }
    }
}
