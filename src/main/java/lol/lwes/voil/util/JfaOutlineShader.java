package lol.lwes.voil.util;

import dev.prestigeclient.renderlib.api.entity.EntityOutlineHandler;
import dev.prestigeclient.renderlib.api.path.RLPath;
import dev.prestigeclient.renderlib.api.shader.RLSimpleEntityOutlineShader;
import dev.prestigeclient.renderlib.api.uniform.RLUniforms;
import dev.prestigeclient.renderlib.impl.api.RenderAPI;
import dev.prestigeclient.renderlib.impl.core.GlStateGuard;
import dev.prestigeclient.renderlib.impl.render.PostPipeline;
import dev.prestigeclient.renderlib.impl.render.layer.type.BufferCollector;
import dev.prestigeclient.renderlib.impl.render.mgr.RenderPass;
import dev.prestigeclient.renderlib.impl.render.mgr.ShaderManager;
import dev.prestigeclient.renderlib.impl.render.resource.ShaderProgram;
import java.awt.Color;
import java.nio.ByteBuffer;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.module.render.ESP;
import lol.lwes.voil.util.render.GradientUniformBinder;
import lol.lwes.voil.util.render.KawaseBlur;
import lol.lwes.voil.util.render.SeparableBlur;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL33;

public class JfaOutlineShader extends RLSimpleEntityOutlineShader {
    private final KawaseBlur shaderState = new KawaseBlur();
    private final RLPath jfaVertPath = RLPath.of("assets/tulip", "shaders/jfa/jfa.vert");
    private final int maxLevel = 6;
    private final GradientUniformBinder fillColorSource = new GradientUniformBinder("fill");
    private final GradientUniformBinder outlineColorSource = new GradientUniformBinder("outline");
    private ShaderProgram seedProgram;
    private ShaderProgram stepProgram;
    private ShaderProgram glowExtractProgram;
    private int jfaFboA = -1;
    private int jfaFboB = -1;
    private int jfaTexA = -1;
    private int jfaTexB = -1;
    private int glowFbo = -1;
    private int glowTex = -1;
    private int jfaResultTex = -1;
    private int extraTex = -1;
    private double shinePhase;
    private long lastShineTime;
    private static final long TIME_WRAP_MS = 100000L;

    public JfaOutlineShader() {
        super(RLPath.of("assets/tulip", "shaders/post/outline.fsh"));
    }

    private static int getGlowLevel() {
        return Math.max(2, Math.min(6, Math.round(ESP.glowRadius.getFloat() + 1.0F)));
    }

    private void createResources() {
        this.jfaTexA = createTexture(33328, 33319, 5126, 9728, fbWidth, fbHeight);
        this.jfaTexB = createTexture(33328, 33319, 5126, 9728, fbWidth, fbHeight);
        int i = this.jfaTexA;
        this.jfaFboA = createFramebuffer(i);
        int j = this.jfaTexB;
        this.jfaFboB = createFramebuffer(j);
        int k = this.getGlowWidth();
        this.glowTex = createTexture(34842, 6408, 5126, 9729, k, this.getGlowHeight());
        int l = this.glowTex;
        this.glowFbo = createFramebuffer(l);
        GL30.glBindFramebuffer(36160, 0);
    }

    private boolean jfaStep(boolean flag, int i) {
        float f = i;
        RenderPass.builder()
            .target(flag ? this.jfaFboB : this.jfaFboA, fbWidth, fbHeight)
            .source(0, flag ? this.jfaTexA : this.jfaTexB)
            .shaderProgram(this.stepProgram)
            .uniforms(rluniforms -> rluniforms.set1i("jfaTex", 0).set1f("uStep", f))
            .build()
            .dispatchFullscreen();
        return !flag;
    }

    private void runGlowExtract() {
        RenderPass.setupPostState();
        RenderPass.builder()
            .target(this.glowFbo, this.getGlowWidth(), this.getGlowHeight())
            .source(0, this.colTex.tex)
            .source(1, this.jfaResultTex)
            .shaderProgram(this.glowExtractProgram)
            .clear()
            .uniforms(rluniforms -> {
                rluniforms.set1i("jfaTex", 1);
                this.applyColorUniforms(rluniforms);
            })
            .build()
            .dispatchFullscreen();
    }

    private static float getTimeSeconds() {
        return (float)(System.currentTimeMillis() % TIME_WRAP_MS) / 1000.0F;
    }

    private float updateShinePhase() {
        long i = System.nanoTime();
        if (this.lastShineTime != 0L) {
            double d0 = (i - this.lastShineTime) / 1.0E9;
            this.shinePhase = this.shinePhase + d0 * ESP.shineSpeed.getFloat();
            this.shinePhase = this.shinePhase - Math.floor(this.shinePhase);
        }

        this.lastShineTime = i;
        return (float)this.shinePhase;
    }

    private static float getBlurScale() {
        return (1 << getGlowLevel()) * 1.5F;
    }

    private static void bindTexture(int i, int j) {
        GL13.glActiveTexture(33984 + i);
        GL33.glBindSampler(i, 0);
        GL11.glBindTexture(3553, j);
    }

    private void applyColorUniforms(RLUniforms rluniforms) {
        rluniforms.set1i("texSampler", 0);
        this.fillColorSource.apply(rluniforms, ESP.getFillGradient());
        rluniforms.set1f("glowRadius", ESP.glowRadius.getFloat() * 10.0F);
        rluniforms.set1f("colorFrequency", ESP.colorFrequency.getFloat());
        rluniforms.set1f("time", getTimeSeconds());
        rluniforms.set1f("speed", ESP.speed.getFloat() * 0.6F);
        rluniforms.set1f("glowIntensity", ESP.glowIntensity.getFloat());
        rluniforms.set1i("colorSource", getColorSource());
        setColorUniform(rluniforms, "damagedColor", ESP.damagedColor.getColor());
    }

    private void runJfa() {
        RenderPass.setupPostState();
        RenderPass.builder()
            .target(this.jfaFboA, fbWidth, fbHeight)
            .source(0, this.colTex.tex)
            .shaderProgram(this.seedProgram)
            .clearColor(-1.0F, -1.0F, 0.0F, 0.0F)
            .uniforms(rluniforms -> rluniforms.set1i("texSampler", 0))
            .build()
            .dispatchFullscreen();
        boolean flag = true;
        int i = this.getJfaStartStep();

        for (int j = i; j >= 1; j >>= 1) {
            flag = this.jfaStep(flag, j);
        }

        if (i >= 16) {
            flag = this.jfaStep(flag, 2);
        }

        flag = this.jfaStep(flag, 1);
        this.jfaResultTex = flag ? this.jfaTexA : this.jfaTexB;
    }

    private static void setColorUniform(RLUniforms rluniforms, String s, Color color) {
        rluniforms.set3f(s, color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F);
    }

    private static int getColorSource() {
        return switch (ESP.colorMode.getMode()) {
            case "Health Based" -> 1;
            case "Damaged" -> 2;
            default -> 0;
        };
    }

    private static int createFramebuffer(int i) {
        int j = GL30.glGenFramebuffers();
        GL30.glBindFramebuffer(36160, j);
        GL30.glFramebufferTexture2D(36160, 36064, 3553, i, 0);
        return j;
    }

    private int getGlowHeight() {
        return Math.max(1, fbHeight / 6);
    }

    private int getJfaStartStep() {
        float f = ESP.glowRadius.getFloat() * 10.0F;
        float f1 = ESP.outlineThickness.getFloat() * 0.5F + 1.0F;
        if (ESP.glowType.is("Bloom")) {
            f1 = Math.max(f1, Math.max(f * 0.25F, 1.0F) + 1.0F);
        } else {
            f1 = Math.max(f1, f + 1.0F);
        }

        return Math.min(Integer.highestOneBit(Math.max(1, (int)Math.ceil(f1))), Integer.highestOneBit(Math.max(fbWidth, fbHeight)));
    }

    private int getGlowWidth() {
        return Math.max(1, fbWidth / 6);
    }

    private static int createTexture(int i, int j, int k, int l, int i1, int j1) {
        int k1 = GL11.glGenTextures();
        GL11.glBindTexture(3553, k1);
        GL11.glTexImage2D(3553, 0, i, i1, j1, 0, j, k, (ByteBuffer)null);
        GL11.glTexParameteri(3553, 10241, l);
        GL11.glTexParameteri(3553, 10240, l);
        GL11.glTexParameteri(3553, 10242, 33071);
        GL11.glTexParameteri(3553, 10243, 33071);
        return k1;
    }

    private static void resizeTexture(int i, int j, int k, int l, int i1, int j1) {
        GL11.glBindTexture(3553, i);
        GL11.glTexImage2D(3553, 0, j, i1, j1, 0, k, l, (ByteBuffer)null);
    }

    @Override
    public void post(BufferCollector buffercollector, RenderAPI.RenderContext renderapi$rendercontext) {
        if (VoilClient.mc.world == null || !EntityOutlineHandler.hadCapturesThisFrame()) {
            return;
        }

        if (initialized && this.seedProgram != null && this.jfaFboA != -1) {
            try (GlStateGuard glstateguard = GlStateGuard.capture(2)) {
                this.runJfa();
                if (ESP.glowType.is("Bloom") && this.glowExtractProgram != null && this.glowFbo != -1) {
                    this.runGlowExtract();
                    this.extraTex = switch (ESP.blurType.getMode()) {
                        case "Gaussian" -> SeparableBlur.getInstance().blur(this.glowTex, this.getGlowWidth(), this.getGlowHeight(), getBlurScale(), 0);
                        case "Box" -> SeparableBlur.getInstance().blur(this.glowTex, this.getGlowWidth(), this.getGlowHeight(), getBlurScale(), 1);
                        default -> this.shaderState.blur(this.glowTex, this.getGlowWidth(), this.getGlowHeight(), getGlowLevel());
                    };
                } else {
                    this.extraTex = -1;
                }
            }
        }

        try (GlStateGuard glstateguard = GlStateGuard.capture(3)) {
            super.post(buffercollector, renderapi$rendercontext);
        }
    }

    @Override
    public void initializeIfNeeded() {
        if (initialized) {
            return;
        }

        super.initializeIfNeeded();
        VoilClient.mc.send(() -> {
            this.seedProgram = ShaderManager.loadShader(this.jfaVertPath, RLPath.of("assets/tulip", "shaders/jfa/jfa_seed.fsh"), false);
            this.stepProgram = ShaderManager.loadShader(this.jfaVertPath, RLPath.of("assets/tulip", "shaders/jfa/jfa_step.fsh"), false);
            this.glowExtractProgram = ShaderManager.loadShader(this.jfaVertPath, RLPath.of("assets/tulip", "shaders/post/glow_extract.fsh"), false);
            this.createResources();
        });
    }

    @Override
    public void resizeIfNeeded() {
        int i = fbWidth;
        int j = fbHeight;
        super.resizeIfNeeded();
        if (fbWidth == i && fbHeight == j) {
            return;
        }

        if (this.jfaTexA == -1) {
            return;
        }

        resizeTexture(this.jfaTexA, 33328, 33319, 5126, fbWidth, fbHeight);
        resizeTexture(this.jfaTexB, 33328, 33319, 5126, fbWidth, fbHeight);
        if (this.glowTex == -1) {
            return;
        }

        resizeTexture(this.glowTex, 34842, 6408, 5126, this.getGlowWidth(), this.getGlowHeight());
    }

    @Override
    protected void applyUniforms(RLUniforms rluniforms) {
        boolean flag = ESP.glowType.is("Bloom");
        this.fillColorSource.apply(rluniforms, ESP.getFillGradient());
        this.outlineColorSource.apply(rluniforms, ESP.getOutlineGradient());
        rluniforms.set1f("fillAlpha", ESP.fill.isEnabled() ? ESP.alpha.getFloat() / 255.0F : 0.0F);
        rluniforms.set1f("glowInstensity", ESP.glowIntensity.getFloat());
        rluniforms.set1f("coreGlow", flag ? 0.0F : 1.0F);
        rluniforms.set1f("haloIntensity", flag ? ESP.glowIntensity.getFloat() * 2.0F : 0.0F);
        rluniforms.set1f("time", getTimeSeconds());
        rluniforms.set1i("outline", ESP.outline.isEnabled() ? 1 : 0);
        rluniforms.set1f("thickness", ESP.outlineThickness.getFloat());
        rluniforms.set1f("glowRadius", ESP.glowRadius.getFloat() * 10.0F);
        rluniforms.set1f("speed", ESP.speed.getFloat() * 0.6F);
        rluniforms.set1i("texSampler", 0);
        rluniforms.set1i("jfaTex", 1);
        rluniforms.set1i("glowTex", 2);
        rluniforms.set1i("colorSource", getColorSource());
        setColorUniform(rluniforms, "damagedColor", ESP.damagedColor.getColor());
        rluniforms.set1f("colorFrequency", ESP.colorFrequency.getFloat());
        rluniforms.set1i("shine", ESP.shine.isEnabled() ? 1 : 0);
        rluniforms.set1f("shineIntensity", ESP.shineIntensity.getFloat());
        rluniforms.set1f("shinePhase", this.updateShinePhase());
        rluniforms.set1f("shineWidth", ESP.shineWidth.getFloat());
        rluniforms.set1f("shineAngle", ESP.shineAngle.getFloat());
    }

    @Override
    public PostPipeline getPipeline() {
        return new PostPipeline().addPass(this.getProgram(), renderpass -> {
            bindTexture(0, this.colTex.tex);
            bindTexture(1, this.jfaResultTex != -1 ? this.jfaResultTex : 0);
            bindTexture(2, this.extraTex != -1 ? this.extraTex : 0);
            GL13.glActiveTexture(33984);
            GL11.glEnable(3042);
            GL11.glBlendFunc(1, 771);
            this.applyUniforms();
        }, RenderAPI.getMinecraftFramebuffer());
    }
}
