package lol.lwes.voil.util.render;

import dev.prestigeclient.renderlib.api.path.RLPath;
import dev.prestigeclient.renderlib.api.uniform.RLUniforms;
import dev.prestigeclient.renderlib.impl.core.GlStateGuard;
import dev.prestigeclient.renderlib.impl.render.mgr.RenderPass;
import dev.prestigeclient.renderlib.impl.render.mgr.ShaderManager;
import dev.prestigeclient.renderlib.impl.render.resource.ShaderProgram;
import java.awt.Color;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.util.render.shader.ShaderRenderer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;

public class KawaseGlowRenderer {
    private final KawaseBlur kawaseBlur = new KawaseBlur();
    private static RLPath vertexPath = RLPath.of("assets/tulip", "shaders/jfa/jfa.vert");
    private final LinkedHashMap targetCache = new GlowCacheMap(this, 16, 0.75F, true);
    private ShaderProgram solidShader;
    private ShaderProgram glowShader;
    private int levelCount = 1;
    private float lastScale = 0.0F;
    private boolean shadersLoaded = false;
    private static final long LOW_32_BITS_MASK = 4294967295L;

    public void render(
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
        boolean flag,
        Color color,
        Color color1,
        Color color2,
        Color color3
    ) {
        if (afloat != null && afloat1 != null && afloat.length == afloat1.length) {
            float f8 = 0.0F;

            for (float f10 : afloat1) {
                f8 = Math.max(f8, f10);
            }

            float f9 = 0.0F;

            for (float f11 : afloat) {
                f9 += f11;
            }

            this.renderCore(f, f1, f8, f9, f2, f3, f4, f5, f6, f7, 1, afloat, afloat1, flag, color, color1, color2, color3);
        }
    }

    public void render(float f, float f1, float[] afloat, float[] afloat1, float f2, float f3, float f4, Color color) {
        this.render(f, f1, afloat, afloat1, f2, f2, f2, f2, f3, f4, false, color, color, color, color);
    }

    public void render(float f, float f1, float f2, float f3, float f4, float f5, float f6, Color color) {
        this.renderCore(f, f1, f2, f3, f4, f4, f4, f4, f5, f6, 0, null, null, false, color, color, color, color);
    }

    public void render(float f, float f1, float f2, float f3, float f4, float f5, float f6, Color color, Color color1, Color color2, Color color3) {
        this.renderCore(f, f1, f2, f3, f4, f4, f4, f4, f5, f6, 0, null, null, false, color, color1, color2, color3);
    }

    private static int[] createTargets(int i, int j) {
        int k = GL11.glGenTextures();
        GL11.glBindTexture(3553, k);
        GL11.glTexImage2D(3553, 0, 32856, i, j, 0, 6408, 5121, (ByteBuffer)null);
        GL11.glTexParameteri(3553, 10241, 9729);
        GL11.glTexParameteri(3553, 10240, 9729);
        GL11.glTexParameteri(3553, 10242, 33071);
        GL11.glTexParameteri(3553, 10243, 33071);
        int l = GL30.glGenFramebuffers();
        GL30.glBindFramebuffer(36160, l);
        GL30.glFramebufferTexture2D(36160, 36064, 3553, k, 0);
        int i1 = GL30.glCheckFramebufferStatus(36160);
        GL30.glBindFramebuffer(36160, 0);
        if (i1 != 36053) {
            PrintStream printstream = System.err;
            String s1 = Integer.toHexString(i1);
            printstream.println("KawaseGlow source FBO incomplete: 0x" + s1);
        }

        return new int[]{l, k};
    }

    public void render(float f, float f1, float[] afloat, float[] afloat1, float f2, float f3, float f4, Color color, Color color1, Color color2, Color color3) {
        this.render(f, f1, afloat, afloat1, f2, f2, f2, f2, f3, f4, false, color, color1, color2, color3);
    }

    public void render(
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
        Color color,
        Color color1,
        Color color2,
        Color color3
    ) {
        this.render(f, f1, afloat, afloat1, f2, f3, f4, f5, f6, f7, false, color, color1, color2, color3);
    }

    private void renderGlowLayer(
        int i,
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
        int j,
        int k,
        float[] afloat,
        float[] afloat1,
        int l,
        float f10
    ) {
        int i1 = VoilClient.mc.getWindow().getFramebufferHeight();
        int j1 = Math.round((f - f4) * f5);
        int k1 = Math.round(i1 - (f1 + f3 + f4) * f5);
        int l1 = Math.max(1, Math.round((f2 + 2.0F * f4) * f5));
        int i2 = Math.max(1, Math.round((f3 + 2.0F * f4) * f5));
        int j2 = RenderManager.getCurrentFramebuffer();
        RenderPass renderpass = RenderPass.builder().targetFramebuffer(j2).viewport(j1, k1, l1, i2).shaderProgram(this.glowShader).source(0, i).build();
        renderpass.begin();
        RLUniforms rluniforms = RLUniforms.get(this.glowShader.glId());
        rluniforms.set1i("GlowSampler", 0);
        rluniforms.set1f("GlowIntensity", f10);
        rluniforms.set1i("useVariableWidth", j);
        rluniforms.set1i("numSegments", k);
        rluniforms.setfv("segmentHeights", afloat);
        rluniforms.setfv("segmentWidths", afloat1);
        rluniforms.set1i("alignRight", l);
        rluniforms.set4f("cornerRadii", f6, f7, f8, f9);
        rluniforms.set2f("uShapeSize", f2 * f5, f3 * f5);
        rluniforms.set1f("uPad", f4 * f5);
        rluniforms.set2f("uQuadOrigin", j1, k1);
        rluniforms.set2f("uQuadSize", l1, i2);
        rluniforms.set1f("uFlipY", 0.0F);
        GL11.glEnable(3042);
        GL11.glBlendFunc(1, 771);
        renderpass.drawFullscreen();
    }

    public void render(
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
        Color color,
        Color color1,
        Color color2,
        Color color3
    ) {
        this.renderCore(f, f1, f2, f3, f4, f5, f6, f7, f8, f9, 0, null, null, false, color, color1, color2, color3);
    }

    private void loadShaders() {
        if (!this.shadersLoaded) {
            this.solidShader = ShaderManager.loadShader(vertexPath, RLPath.of("assets/tulip", "shaders/core/kawaseglow_solid.fsh"), false);
            this.glowShader = ShaderManager.loadShader(vertexPath, RLPath.of("assets/tulip", "shaders/core/kawaseglow.fsh"), false);
            this.shadersLoaded = true;
        }
    }

    private int[] getTargets(int i, int j) {
        long k = (long)i << 32 | j & LOW_32_BITS_MASK;
        int[] aint = (int[])this.targetCache.get(k);
        if (aint == null) {
            aint = createTargets(i, j);
            this.targetCache.put(k, aint);
        }

        return aint;
    }

    public static void deleteTargets(int[] aint) {
        GL13.glActiveTexture(33984);
        GL11.glBindTexture(3553, 0);
        GL30.glBindFramebuffer(36160, 0);
        if (aint[1] != 0) {
            GL11.glDeleteTextures(aint[1]);
        }

        if (aint[0] != 0) {
            GL30.glDeleteFramebuffers(aint[0]);
        }
    }

    private static float[] scaleRadii(float[] afloat, float f) {
        float[] afloat1 = new float[32];
        if (afloat != null) {
            for (int i = 0; i < afloat.length && i < 32; i++) {
                afloat1[i] = afloat[i] * f;
            }
        }

        return afloat1;
    }

    private void renderSolid(
        int i,
        int j,
        int k,
        float f,
        float f1,
        float f2,
        float f3,
        float f4,
        float f5,
        float f6,
        int l,
        int i1,
        float[] afloat,
        float[] afloat1,
        int j1,
        Color color,
        Color color1,
        Color color2,
        Color color3
    ) {
        RenderPass.setupPostState();
        RenderPass.builder().target(i, j, k).shaderProgram(this.solidShader).clear().uniforms(rluniforms -> {
            rluniforms.set2f("uFboSize", j, k);
            rluniforms.set1f("uPad", f);
            rluniforms.set2f("uShapeSize", f1, f2);
            rluniforms.set4f("cornerRadii", f3, f4, f5, f6);
            rluniforms.set1i("useVariableWidth", l);
            rluniforms.set1i("numSegments", i1);
            rluniforms.setfv("segmentHeights", afloat);
            rluniforms.setfv("segmentWidths", afloat1);
            rluniforms.set1i("alignRight", j1);
            setColorUniform(rluniforms, "GlowColor1", color);
            setColorUniform(rluniforms, "GlowColor2", color1);
            setColorUniform(rluniforms, "GlowColor3", color2);
            setColorUniform(rluniforms, "GlowColor4", color3);
            rluniforms.set1f("Time", this.lastScale);
        }).build().dispatchFullscreen();
    }

    public void render(
        float f, float f1, float[] afloat, float[] afloat1, float f2, float f3, float f4, boolean flag, Color color, Color color1, Color color2, Color color3
    ) {
        this.render(f, f1, afloat, afloat1, f2, f2, f2, f2, f3, f4, flag, color, color1, color2, color3);
    }

    private void renderCore(
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
        int i,
        float[] afloat,
        float[] afloat1,
        boolean flag,
        Color color,
        Color color1,
        Color color2,
        Color color3
    ) {
        if (!this.shadersLoaded) {
            this.loadShaders();
        }

        if (this.solidShader != null && this.glowShader != null && VoilClient.mc.getWindow() != null && !(f2 <= 0.0F) && !(f3 <= 0.0F)) {
            float f10 = getScaleFactor();
            float f11 = f10 / this.levelCount;
            float f12 = Math.max(Math.max(f4, f5), Math.max(f6, f7));
            float f13 = f8 + f12 + 16.0F;
            int j = Math.max(2, Math.min(6, Math.round(f8 / 6.0F)));
            int k = i == 1 && afloat != null ? Math.min(afloat.length, 32) : 0;
            int l = flag ? 1 : 0;
            int i1 = Math.max(1, Math.round((f2 + 2.0F * f13) * f11));
            int j1 = Math.max(1, Math.round((f3 + 2.0F * f13) * f11));
            int[] aint = this.getTargets(i1, j1);
            int k1 = aint[0];
            int l1 = aint[1];
            this.lastScale += 0.005F;
            float[] afloat2 = scaleRadii(afloat, f11);
            float[] afloat3 = scaleRadii(afloat1, f11);
            GlStateGuard glstateguard = GlStateGuard.capture(1);

            try {
                this.renderSolid(
                    k1, i1, j1, f13 * f11, f2 * f11, f3 * f11, f4 * f11, f5 * f11, f6 * f11, f7 * f11, i, k, afloat2, afloat3, l, color, color1, color2, color3
                );
                int i2 = this.kawaseBlur.blur(l1, i1, j1, j);
                if (i2 != 0) {
                    this.renderGlowLayer(
                        i2, f, f1, f2, f3, f13, f10, f4 * f10, f5 * f10, f6 * f10, f7 * f10, i, k, scaleRadii(afloat, f10), scaleRadii(afloat1, f10), l, f9
                    );
                    return;
                }
            } finally {
                glstateguard.close();
            }
        }
    }

    private static void setColorUniform(RLUniforms rluniforms, String s, Color color) {
        rluniforms.set4f(s, color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, color.getAlpha() / 255.0F);
    }

    private static float getScaleFactor() {
        return ShaderRenderer.getScale();
    }
}
