package lol.lwes.voil.util.render;

import dev.prestigeclient.renderlib.impl.api.RenderAPI;
import dev.prestigeclient.renderlib.impl.core.GlStateGuard;
import dev.prestigeclient.renderlib.impl.render.vertex.VertexFormats;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.util.render.shader.ShaderRenderer;
import net.minecraft.client.render.Camera;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL33;
import org.lwjgl.system.MemoryUtil;

public class SkyRenderer extends ShaderRenderer {
    public static SkyRenderer instance;
    private int vao = -1;
    private int vbo = -1;
    private int noiseTexture = -1;
    private final Map<String, Integer> uniformLocations = new HashMap<>();
    private boolean dirty = true;
    private float[] horizonColor = new float[]{0.01F, 0.015F, 0.03F};
    private float[] zenithColor = new float[]{0.03F, 0.07F, 0.16F};
    private float[] skyMidColor = new float[]{0.02F, 0.04F, 0.08F};
    private float starDensity = 0.6F;
    private float[] starColor = new float[]{1.0F, 1.0F, 0.95F};
    private boolean enableShootingStars = true;
    private float[] shootingStarColor = new float[]{1.0F, 0.9F, 0.7F};
    private float auroraIntensity = 0.8F;
    private float[] auroraColor1 = new float[]{0.1F, 0.9F, 0.6F};
    private float[] auroraColor2 = new float[]{0.4F, 0.2F, 0.9F};
    private float[] auroraColor3 = new float[]{0.9F, 0.1F, 0.6F};
    private float auroraSpeed = 1.0F;
    private float nebulaIntensity = 0.5F;
    private boolean enableNebula = true;
    private float[] nebulaColor1 = new float[]{0.1F, 0.05F, 0.15F};
    private float[] nebulaColor2 = new float[]{0.05F, 0.1F, 0.15F};
    private boolean enableSun = true;
    private float[] sunColor = new float[]{1.0F, 0.94F, 0.78F};
    private float sunIntensity = 1.0F;
    private float sunSize = 0.05F;
    private boolean enableMoon = true;
    private float[] moonColor = new float[]{0.78F, 0.86F, 1.0F};
    private float moonIntensity = 0.5F;
    private float moonSize = 0.04F;
    private CloudSettings cloudSettings = CloudSettings.createDefault();
    private float vignetteIntensity = 0.1F;
    private float timeSpeed = 0.9F;
    private double time = 0.0;
    private long lastTimeNanos = 0L;
    private final Matrix4f invProjection = new Matrix4f();
    private final Matrix4f invView = new Matrix4f();
    private final Quaternionf viewRotation = new Quaternionf();
    private final float[] matrixBuffer = new float[16];

    public SkyRenderer() {
        try {
            this.shader = ShaderRenderer.loadShader("shaders/core/sky.fsh", "shaders/core/sky.vsh");
            this.vertexFormat = VertexFormats.POSITION_COLOR;
            this.createQuad();
            this.noiseTexture = createNoiseTexture();
            this.useProgram();
            GL30.glUniform1i(this.getUniformLocation("uNoise"), 0);
            this.unuseProgram();
        } catch (Exception exception) {
            PrintStream printstream = System.err;
            String s1 = exception.getMessage();
            printstream.println("Failed to load sky shader: " + s1);
            exception.printStackTrace();
        }
    }

    @Override
    public void setUniformFloat(String s, float f) {
        GL30.glUniform1f(this.getUniformLocation(s), f);
    }

    private void setUniformBool(String s, boolean flag) {
        GL30.glUniform1f(this.getUniformLocation(s), flag ? 1.0F : 0.0F);
    }

    private void setUniformVec3(String s, float[] afloat) {
        GL30.glUniform3f(this.getUniformLocation(s), afloat[0], afloat[1], afloat[2]);
    }

    private void uploadSkySettings() {
        this.setUniformVec3("uHorizonColor", this.horizonColor);
        this.setUniformVec3("uZenithColor", this.zenithColor);
        this.setUniformVec3("uSkyMidColor", this.skyMidColor);
        this.setUniformFloat("uStarDensity", this.starDensity);
        this.setUniformVec3("uStarColor", this.starColor);
        this.setUniformBool("uEnableShootingStars", this.enableShootingStars);
        this.setUniformVec3("uShootingStarColor", this.shootingStarColor);
        this.setUniformFloat("uAuroraIntensity", this.auroraIntensity);
        this.setUniformVec3("uAuroraColor1", this.auroraColor1);
        this.setUniformVec3("uAuroraColor2", this.auroraColor2);
        this.setUniformVec3("uAuroraColor3", this.auroraColor3);
        this.setUniformFloat("uAuroraSpeed", this.auroraSpeed);
        this.setUniformFloat("uNebulaIntensity", this.nebulaIntensity);
        this.setUniformBool("uEnableNebula", this.enableNebula);
        this.setUniformVec3("uNebulaColor1", this.nebulaColor1);
        this.setUniformVec3("uNebulaColor2", this.nebulaColor2);
        this.setUniformBool("uEnableSun", this.enableSun);
        this.setUniformVec3("uSunColor", this.sunColor);
        this.setUniformFloat("uSunIntensity", this.sunIntensity);
        this.setUniformFloat("uSunSize", this.sunSize);
        this.setUniformBool("uEnableMoon", this.enableMoon);
        this.setUniformVec3("uMoonColor", this.moonColor);
        this.setUniformFloat("uMoonIntensity", this.moonIntensity);
        this.setUniformFloat("uMoonSize", this.moonSize);
        CloudSettings cloudsettings = this.cloudSettings;
        float f = Math.max(cloudsettings.topHeight(), cloudsettings.height() + 0.1F);
        this.setUniformBool("uEnableClouds", cloudsettings.enabled());
        this.setUniformFloat("uCloudCoverage", cloudsettings.coverage());
        this.setUniformFloat("uCloudDensity", cloudsettings.density());
        this.setUniformFloat("uCloudScale", cloudsettings.scale());
        this.setUniformFloat("uCloudSpeed", cloudsettings.speed());
        this.setUniformFloat("uCloudBottom", cloudsettings.height());
        this.setUniformFloat("uCloudTop", f);
        this.setUniformFloat("uCloudAbsorption", cloudsettings.absorption());
        this.setUniformFloat("uCloudAmbient", cloudsettings.ambient());
        GL30.glUniform1i(this.getUniformLocation("uCloudSteps"), Math.max(4, cloudsettings.quality()));
        this.setUniformVec3("uCloudColor", cloudsettings.colorA());
        this.setUniformVec3("uCloudShadowColor", cloudsettings.colorB());
        this.setUniformFloat("uVignetteIntensity", this.vignetteIntensity);
    }

    public static void initialize() {
        instance = new SkyRenderer();
    }

    public void applySettings(
        float[] afloat,
        float[] afloat1,
        float[] afloat2,
        float f,
        float[] afloat3,
        float f1,
        float[] afloat4,
        float[] afloat5,
        float[] afloat6,
        float f2,
        float f3,
        boolean flag,
        float[] afloat7,
        boolean flag1,
        float f4,
        float f5,
        float[] afloat8,
        float[] afloat9,
        boolean flag2,
        boolean flag3,
        float[] afloat10,
        float[] afloat11,
        float f6,
        float f7,
        float f8,
        float f9
    ) {
        this.horizonColor = afloat;
        this.zenithColor = afloat1;
        this.skyMidColor = afloat2;
        this.starDensity = f;
        this.starColor = afloat3;
        this.enableShootingStars = flag;
        this.shootingStarColor = afloat7;
        this.auroraIntensity = f1;
        this.auroraColor1 = afloat4;
        this.auroraColor2 = afloat5;
        this.auroraColor3 = afloat6;
        this.auroraSpeed = f4;
        this.nebulaIntensity = f5;
        this.enableNebula = flag1;
        this.nebulaColor1 = afloat8;
        this.nebulaColor2 = afloat9;
        this.enableSun = flag2;
        this.sunColor = afloat10;
        this.sunIntensity = f6;
        this.sunSize = f8;
        this.enableMoon = flag3;
        this.moonColor = afloat11;
        this.moonIntensity = f7;
        this.moonSize = f9;
        this.vignetteIntensity = f2;
        this.timeSpeed = f3;
        this.dirty = true;
    }

    private void createQuad() {
        this.vao = GL30.glGenVertexArrays();
        this.vbo = GL30.glGenBuffers();
        GL30.glBindVertexArray(this.vao);
        GL30.glBindBuffer(34962, this.vbo);
        float[] afloat = new float[]{-1.0F, -1.0F, 3.0F, -1.0F, -1.0F, 3.0F};
        GL30.glBufferData(34962, afloat, 35044);
        GL30.glEnableVertexAttribArray(0);
        GL30.glVertexAttribPointer(0, 2, 5126, false, 0, 0L);
        GL30.glBindVertexArray(0);
    }

    private static float noise3D(float f, float f1, float f2, int i, int j) {
        int k = i - 1;
        int l = (int)Math.floor(f);
        int i1 = (int)Math.floor(f1);
        int j1 = (int)Math.floor(f2);
        float f3 = f - l;
        float f4 = f1 - i1;
        float f5 = f2 - j1;
        f3 = f3 * f3 * (3.0F - 2.0F * f3);
        f4 = f4 * f4 * (3.0F - 2.0F * f4);
        f5 = f5 * f5 * (3.0F - 2.0F * f5);
        float f6 = hashNoise(l, i1, j1, k, j);
        float f7 = hashNoise(l + 1, i1, j1, k, j);
        float f8 = hashNoise(l, i1 + 1, j1, k, j);
        float f9 = hashNoise(l + 1, i1 + 1, j1, k, j);
        float f10 = hashNoise(l, i1, j1 + 1, k, j);
        float f11 = hashNoise(l + 1, i1, j1 + 1, k, j);
        float f12 = hashNoise(l, i1 + 1, j1 + 1, k, j);
        float f13 = hashNoise(l + 1, i1 + 1, j1 + 1, k, j);
        float f14 = f6 + (f7 - f6) * f3;
        float f15 = f8 + (f9 - f8) * f3;
        float f16 = f10 + (f11 - f10) * f3;
        float f17 = f12 + (f13 - f12) * f3;
        float f18 = f14 + (f15 - f14) * f4;
        float f19 = f16 + (f17 - f16) * f4;
        return f18 + (f19 - f18) * f5;
    }

    public void render(Camera camera) {
        if (this.shader != null && VoilClient.mc.world != null && this.vao != -1 && this.noiseTexture != -1) {
            long i = System.nanoTime();
            double d0 = this.lastTimeNanos == 0L ? 0.0 : Math.min((i - this.lastTimeNanos) * 1.0E-9, 0.1);
            this.lastTimeNanos = i;
            this.time = this.time + d0 * 15.0 * this.timeSpeed * this.timeSpeed;
            if (this.time >= 62831.85307179586) {
                this.time -= 62831.85307179586;
            }

            int j = VoilClient.mc.getWindow().getFramebufferWidth();
            int k = VoilClient.mc.getWindow().getFramebufferHeight();
            GlStateGuard glstateguard = GlStateGuard.capture();
            int l = RenderAPI.getMinecraftFramebuffer();
            GL30.glBindFramebuffer(36160, l);
            GL30.glViewport(0, 0, j, k);
            GL30.glEnable(2929);
            GL30.glDepthFunc(515);
            GL30.glDepthMask(false);
            GL30.glDisable(3042);
            GL30.glDisable(2884);
            this.useProgram();
            if (this.dirty) {
                this.uploadSkySettings();
                this.dirty = false;
            }

            this.uploadCameraUniforms(camera, j, k);
            GL30.glActiveTexture(33984);
            GL33.glBindSampler(0, 0);
            GL30.glBindTexture(32879, this.noiseTexture);
            GL30.glBindVertexArray(this.vao);
            GL30.glDrawArrays(4, 0, 3);
            GL30.glBindVertexArray(0);
            GL30.glBindTexture(32879, 0);
            this.unuseProgram();
            GL30.glDepthMask(true);
            GL30.glDepthFunc(515);
            glstateguard.restore();
        }
    }

    private static int createNoiseTexture() {
        ByteBuffer bytebuffer = MemoryUtil.memAlloc(8388608);
        IntStream.range(0, 128).parallel().forEach(j -> {
            float f = j * 0.125F;

            for (int k = 0; k < 128; k++) {
                float f1 = k * 0.125F;
                int k1 = j * 128;
                k1 += k;
                k1 *= 128;
                int l = k1 * 4;

                for (int i1 = 0; i1 < 128; i1++) {
                    float f2 = i1 * 0.125F;
                    float f3 = noise3D(f2, f1, f, 16, 4660);
                    float f10 = f2 * 2.0F + 3.7F;
                    float f4 = noise3D(f10, f1 * 2.0F + 1.3F, f * 2.0F + 9.1F, 32, 22136);
                    float f11 = f2 * 4.0F + 7.9F;
                    float f5 = noise3D(f11, f1 * 4.0F + 5.1F, f * 4.0F + 2.3F, 64, 39612);
                    float f12 = f2 * 8.0F + 1.1F;
                    float f6 = noise3D(f12, f1 * 8.0F + 8.7F, f * 8.0F + 4.9F, 128, 57072);
                    float f7 = f3 * 0.5F + f4 * 0.25F;
                    float f8 = f7 + f5 * 0.125F;
                    float f9 = f8 + f6 * 0.0625F;
                    int l1 = i1 * 4;
                    k1 = l;
                    int j1 = k1 + l1;
                    bytebuffer.put(j1, (byte)(f9 * 255.0F + 0.5F));
                    bytebuffer.put(j1 + 1, (byte)(f8 * 255.0F + 0.5F));
                    bytebuffer.put(j1 + 2, (byte)(f7 * 255.0F + 0.5F));
                    bytebuffer.put(j1 + 3, (byte)(f3 * 255.0F + 0.5F));
                }
            }
        });
        int i = GL30.glGenTextures();
        GL30.glBindTexture(32879, i);
        GL30.glTexParameteri(32879, 10241, 9729);
        GL30.glTexParameteri(32879, 10240, 9729);
        GL30.glTexParameteri(32879, 10242, 10497);
        GL30.glTexParameteri(32879, 10243, 10497);
        GL30.glTexParameteri(32879, 32882, 10497);
        GL30.glPixelStorei(3317, 4);
        GL30.glPixelStorei(3314, 0);
        GL30.glPixelStorei(32878, 0);
        GL30.glPixelStorei(3316, 0);
        GL30.glPixelStorei(3315, 0);
        GL30.glPixelStorei(32877, 0);
        GL30.glTexImage3D(32879, 0, 32856, 128, 128, 128, 0, 6408, 5121, bytebuffer);
        GL30.glGenerateMipmap(32879);
        GL30.glBindTexture(32879, 0);
        MemoryUtil.memFree(bytebuffer);
        return i;
    }

    private static float hashNoise(int i, int j, int k, int l, int i1) {
        int i2 = k;
        int j2 = l;
        k = i & j2;
        l = j & j2;
        j = i2 & j2;
        int k2 = k * 668265261;
        int l1 = l * 374761393;
        int k1 = k2;
        k2 = k1 ^ l1;
        l1 = j * -1640531535;
        k1 = k2;
        k1 ^= l1;
        int j1 = k1 ^ i1;
        l1 = j1 >>> 15;
        k1 = j1;
        j1 = k1 ^ l1;
        j1 *= 739982445;
        l1 = j1 >>> 12;
        k1 = j1;
        j1 = k1 ^ l1;
        j1 *= 695872825;
        l1 = j1 >>> 15;
        k1 = j1;
        j1 = k1 ^ l1;
        return (j1 & 16777215) * 5.9604645E-8F;
    }

    public void setCloudSettings(CloudSettings cloudsettings) {
        this.cloudSettings = cloudsettings;
        this.dirty = true;
    }

    private void uploadCameraUniforms(Camera camera, int i, int j) {
        this.setUniformFloat("uTime", (float)this.time);
        GL30.glUniform2f(this.getUniformLocation("uResolution"), i, j);
        float f = VoilClient.mc.options.getFov().getValue().floatValue();
        float f1 = (float)i / j;
        this.invProjection.identity().perspective((float)Math.toRadians(f), f1, 0.05F, VoilClient.mc.gameRenderer.getFarPlaneDistance()).invert();
        this.invView.identity().rotation(camera.getRotation().conjugate(this.viewRotation)).invert();
        this.invProjection.get(this.matrixBuffer);
        GL30.glUniformMatrix4fv(this.getUniformLocation("uInvProjection"), false, this.matrixBuffer);
        this.invView.get(this.matrixBuffer);
        GL30.glUniformMatrix4fv(this.getUniformLocation("uInvView"), false, this.matrixBuffer);
    }

    @Override
    public int getUniformLocation(String s) {
        Integer integer = this.uniformLocations.get(s);
        if (integer == null) {
            integer = super.getUniformLocation(s);
            this.uniformLocations.put(s, integer);
        }

        return integer;
    }

    public void cleanup() {
        if (this.vao != -1) {
            GL30.glDeleteVertexArrays(this.vao);
        }

        if (this.vbo != -1) {
            GL30.glDeleteBuffers(this.vbo);
        }

        if (this.noiseTexture != -1) {
            GL30.glDeleteTextures(this.noiseTexture);
        }

        this.noiseTexture = -1;
        SkyRenderer skyrenderer1 = this;
        skyrenderer1.vbo = -1;
        this.vao = -1;
    }
}
