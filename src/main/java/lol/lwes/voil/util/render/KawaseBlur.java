package lol.lwes.voil.util.render;

import dev.prestigeclient.renderlib.api.path.RLPath;
import dev.prestigeclient.renderlib.api.uniform.RLUniforms;
import dev.prestigeclient.renderlib.impl.api.RenderAPI;
import dev.prestigeclient.renderlib.impl.core.GlStateGuard;
import dev.prestigeclient.renderlib.impl.render.mgr.RenderPass;
import dev.prestigeclient.renderlib.impl.render.mgr.ShaderManager;
import dev.prestigeclient.renderlib.impl.render.resource.ShaderProgram;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

public class KawaseBlur {
    private static final RLPath vertexPath = RLPath.of("assets/tulip", "shaders/jfa/jfa.vert");
    private static KawaseBlur instance;
    public final Map lastUsedFrame = new HashMap();
    private final LinkedHashMap levelCache = new BlurCacheMap(this, 8, 0.75F, true);
    private static ShaderProgram downShader;
    private static ShaderProgram upShader;
    private static boolean shadersLoaded = false;
    private static final long LOW_BITS_MASK = 4294967295L;

    private static void createLevels(int[] aint, int i, int j, int k) {
        for (int l = 0; l < k; l++) {
            if (aint[6 + l] == 0) {
                int k2 = 6 + l;
                int l2 = getMipSize(i, l + 1);
                int i1 = getMipSize(j, l + 1);
                aint[k2] = createTexture(l2, i1);
                k2 = 0 + l;
                int j1 = aint[6 + l];
                aint[k2] = createFramebuffer(j1);
            }

            if (aint[18 + l] == 0) {
                int i2 = l + 18;
                int j2 = getMipSize(i, l);
                int k1 = getMipSize(j, l);
                aint[i2] = createTexture(j2, k1);
                i2 = 12 + l;
                int l1 = aint[l + 18];
                aint[i2] = createFramebuffer(l1);
            }
        }
    }

    private static int createFramebuffer(int i) {
        int j = GL30.glGenFramebuffers();
        GL30.glBindFramebuffer(36160, j);
        GL30.glFramebufferTexture2D(36160, 36064, 3553, i, 0);
        int k = GL30.glCheckFramebufferStatus(36160);
        if (k != 36053) {
            PrintStream printstream = System.err;
            String s1 = Integer.toHexString(k);
            printstream.println("KawaseBlur FBO incomplete: 0x" + s1);
        }

        return j;
    }

    public void loadShaders() {
        if (!shadersLoaded) {
            try (GlStateGuard glstateguard = GlStateGuard.capture(1)) {
                downShader = ShaderManager.loadShader(vertexPath, RLPath.of("assets/tulip", "shaders/post/kawase/kawase_down.fsh"), false);
                upShader = ShaderManager.loadShader(vertexPath, RLPath.of("assets/tulip", "shaders/post/kawase/kawase_up.fsh"), false);
                GL20.glUseProgram(downShader.glId());
                GL20.glUniform1i(GL20.glGetUniformLocation(downShader.glId(), "texSampler"), 0);
                GL20.glUseProgram(upShader.glId());
                GL20.glUniform1i(GL20.glGetUniformLocation(upShader.glId(), "texSampler"), 0);
                shadersLoaded = true;
            }
        }
    }

    public int blur(int i, int j, int k, int l) {
        if (i != 0 && j > 0 && k > 0) {
            if (!shadersLoaded) {
                this.loadShaders();
            }

            int i1 = Math.max(1, Math.min(l, 6));
            int[] aint = this.getLevels(j, k);
            createLevels(aint, j, k, i1);
            RenderPass.setupPostState();
            int j1 = j;
            int k1 = k;

            for (int l1 = 0; l1 < i1; l1++) {
                int i2 = getMipSize(j, l1 + 1);
                int j2 = getMipSize(k, l1 + 1);
                float f = 0.5F / j1;
                float f1 = 0.5F / k1;
                RenderPass.builder()
                    .target(aint[0 | l1], i2, j2)
                    .source(0, i)
                    .shaderProgram(downShader)
                    .uniforms(rluniforms -> rluniforms.set2f("halfPixel", f, f1))
                    .build()
                    .dispatchFullscreen();
                i = aint[6 + l1];
                j1 = i2;
                k1 = j2;
            }

            for (int k2 = i1 - 1; k2 >= 0; k2--) {
                int l2 = getMipSize(j, k2);
                int i3 = getMipSize(k, k2);
                float f2 = 0.5F / j1;
                float f3 = 0.5F / k1;
                RenderPass.builder()
                    .target(aint[k2 + 12], l2, i3)
                    .source(0, i)
                    .shaderProgram(upShader)
                    .uniforms(rluniforms -> rluniforms.set2f("halfPixel", f2, f3))
                    .build()
                    .dispatchFullscreen();
                i = aint[k2 + 18];
                j1 = l2;
                k1 = i3;
            }

            return i;
        } else {
            return 0;
        }
    }

    private static int createTexture(int i, int j) {
        int k = GL11.glGenTextures();
        GL11.glBindTexture(3553, k);
        GL11.glTexImage2D(3553, 0, 34842, i, j, 0, 6408, 5126, (ByteBuffer)null);
        GL11.glTexParameteri(3553, 10241, 9729);
        GL11.glTexParameteri(3553, 10240, 9729);
        GL11.glTexParameteri(3553, 10242, 33071);
        GL11.glTexParameteri(3553, 10243, 33071);
        return k;
    }

    public static void deleteLevels(int[] aint) {
        for (int i = 0; i < 6; i++) {
            if (aint[0 + i] != 0) {
                GL30.glDeleteFramebuffers(aint[i]);
            }

            if (aint[6 + i] != 0) {
                GL11.glDeleteTextures(aint[6 + i]);
            }

            if (aint[i + 12] != 0) {
                GL30.glDeleteFramebuffers(aint[12 + i]);
            }

            if (aint[18 + i] != 0) {
                GL11.glDeleteTextures(aint[i + 18]);
            }
        }
    }

    private int[] getLevels(int i, int j) {
        long k = (long)i << 32 | j & LOW_BITS_MASK;
        int[] aint = (int[])this.levelCache.get(k);
        if (aint == null) {
            aint = new int[24];
            this.levelCache.put(k, aint);
        }

        this.lastUsedFrame.put(k, RenderAPI.getFrameCounter());
        return aint;
    }

    public static KawaseBlur getInstance() {
        if (instance == null) {
            instance = new KawaseBlur();
        }

        return instance;
    }

    private static int getMipSize(int i, int j) {
        return Math.max(i >> j, 1);
    }
}
