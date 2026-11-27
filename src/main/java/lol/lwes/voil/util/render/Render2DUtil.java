package lol.lwes.voil.util.render;

import com.mojang.blaze3d.textures.GpuTexture;
import dev.prestigeclient.renderlib.api.path.RLPath;
import dev.prestigeclient.renderlib.api.texture.RLTextureLoader;
import dev.prestigeclient.renderlib.api.uniform.RLUniforms;
import dev.prestigeclient.renderlib.impl.api.DrawType;
import dev.prestigeclient.renderlib.impl.render.layer.type.BufferCollector;
import dev.prestigeclient.renderlib.impl.render.layer.type.RenderLayer;
import dev.prestigeclient.renderlib.impl.render.mesh.GlBufferMeshBuilder;
import dev.prestigeclient.renderlib.impl.render.mgr.RenderPass;
import dev.prestigeclient.renderlib.impl.render.mgr.ShaderManager;
import dev.prestigeclient.renderlib.impl.render.resource.GlTexture;
import dev.prestigeclient.renderlib.impl.render.resource.ShaderProgram;
import dev.prestigeclient.renderlib.impl.render.state.StateApplier;
import dev.prestigeclient.renderlib.impl.render.ui.elements.PreBuiltTextureRectangleElement;
import dev.prestigeclient.renderlib.impl.render.vertex.VertexFormats;
import it.unimi.dsi.fastutil.Pair;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.config.GuiTheme;
import lol.lwes.voil.mixin.accessor.GameRendererAccessor;
import lol.lwes.voil.mixin.accessor.GuiRendererAccessor;
import lol.lwes.voil.setting.GradientSetting;
import lol.lwes.voil.util.PlayerTargetUtil;
import lol.lwes.voil.util.render.shader.ArrowShaderRenderer;
import lol.lwes.voil.util.render.shader.BallsShaderRenderer;
import lol.lwes.voil.util.render.shader.BlurShaderRenderer;
import lol.lwes.voil.util.render.shader.FramebufferBlitRenderer;
import lol.lwes.voil.util.render.shader.GradientBackgroundShaderRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.item.KeyedItemRenderState;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

public class Render2DUtil {
    public static BlurShaderRenderer blurRenderer;
    public static RectangleShaderRenderer rectRenderer;
    public static GradientBackgroundShaderRenderer backgroundRenderer;
    public static GlowShaderRenderer glowRenderer;
    public static OutlineShaderRenderer outlineRenderer;
    public static TextureRectangleRenderer textureRenderer;
    public static VariableGlowRenderer variableGlowRenderer;
    public static FadeLineRenderer fadeLineRenderer;
    public static FramebufferBlitRenderer framebufferRenderer;
    public static BezierCurveRenderer bezierRenderer;
    public static BallsShaderRenderer ballsRenderer;
    public static ArrowShaderRenderer arrowRenderer;
    public static StarShaderRenderer starRenderer;
    public static KawaseGlowRenderer kawaseGlowRenderer;
    public static GradientShaderRenderer gradientRenderer;
    public static ModuleGlowRenderer moduleGlowRenderer;
    private static final Map<Identifier, GlTexture> glTextureCache = new HashMap<>();
    private static final StateApplier.ShaderState texShaderState = StateApplier.shaderState(
        ShaderManager.loadShader(RLPath.of("shaders/built_pos_tex.vert"), RLPath.of("shaders/pos_uv.frag"), false)
    );
    private static final boolean[] atlasReady = new boolean[]{false};
    public static final Map<Object, float[]> itemAtlasUvs = new HashMap<>();
    public static final ArrayList<ItemAtlasEntry> pendingItems = new ArrayList<>();
    private static final ShaderProgram texProgram = ShaderManager.loadShader(RLPath.of("shaders/built_pos_tex.vert"), RLPath.of("shaders/pos_uv.frag"), false);
    private static final HashMap layerCache = new HashMap();
    private static final long FLOW_KEY = 360L;

    public static void drawBlurVariable(
        float f, float f1, float[] afloat, float[] afloat1, float f2, float f3, float f4, float f5, float f6, float f8, float f7
    ) {
        if (blurRenderer != null) {
            BufferCollector buffercollector = RenderManager.bufferCollector;
            blurRenderer.renderRoundedRectArrays(buffercollector, f, f1, afloat, afloat1, f2, f3, f4, f5, f6, f7, 0.2F, 0.0F, 0.0F, 0.0F);
        }
    }

    public static void drawBlurVariableTinted(
        float f, float f1, float[] afloat, float[] afloat1, float f2, float f3, float f4, float f5, float f6, float f21, float f7, Color color
    ) {
        if (blurRenderer != null) {
            BlurShaderRenderer blurshaderrenderer = blurRenderer;
            BufferCollector buffercollector1 = RenderManager.bufferCollector;
            float f20 = color.getRed() / 255.0F;
            float f8 = color.getBlue() / 255.0F;
            float f9 = color.getGreen() / 255.0F;
            float f10 = f20;
            float f11 = 0.5F;
            float f12 = f7;
            float f13 = f6;
            float f14 = f5;
            float f15 = f4;
            float f16 = f3;
            float f17 = f2;
            float[] afloat2 = afloat1;
            float[] afloat3 = afloat;
            float f18 = f1;
            float f19 = f;
            BufferCollector buffercollector = buffercollector1;
            blurshaderrenderer.renderRoundedRectArrays(buffercollector, f19, f18, afloat3, afloat2, f17, f16, f15, f14, f13, f12, f11, f10, f9, f8);
        }
    }

    public static void drawBlurCorners(float f, float f1, float[] afloat, float[] afloat1, float f2, float f3) {
        if (blurRenderer != null) {
            BufferCollector buffercollector = RenderManager.bufferCollector;
            blurRenderer.drawRoundedRectArrays(buffercollector, f, f1, afloat, afloat1, f2, f3);
        }
    }

    public static void drawBlurRounded(float f, float f1, float f2, float f3, float f4, float f5, float f6) {
        if (blurRenderer != null) {
            BufferCollector buffercollector = RenderManager.bufferCollector;
            blurRenderer.drawGlassRect(buffercollector, f, f1, f2, f3, f4, f5, 1.0F, f6);
        }
    }

    public static void drawRectGradient(float f, float f1, float f2, float f3, float f4, float f5, Color color, Color color1, Color color2, Color color3) {
        if (rectRenderer != null) {
            rectRenderer.render(f, f1, f2, f3, f4, f5, color, color1, color2, color3);
        }
    }

    public static void drawRoundedRect(float f, float f1, float f2, float f3, float f4, float f5, Color color) {
        if (rectRenderer != null) {
            rectRenderer.render(f, f1, f2, f3, f4, f5, color);
        }
    }

    public static void drawBlurCircle(float f, float f1, float f2) {
        float f3;
        float f4 = f3 = f2 * 2.0F;
        float f5 = f1;
        float f6 = f;
        drawBlurRect(f6, f5, f4, f3, f2, 14.0F);
    }

    public static void drawOutline(float f, float f1, float f2, float f3, float f4, float f5, float f6, Color color) {
        if (outlineRenderer != null) {
            outlineRenderer.render(f, f1, f2, f3, f4, f5, f6, color);
        }
    }

    public static void drawGlowGradient(
        float f, float f1, float f2, float f3, float f4, float f5, float f6, float f7, Color color, Color color1, Color color2, Color color3
    ) {
        if (glowRenderer != null) {
            int i = (int)f7;
            Color color4 = interpolateColor(color, color3, i, (int)f1);
            int j = (int)f7;
            Color color5 = interpolateColor(color1, color2, j, (int)(f1 + f3));
            glowRenderer.render(f, f1, f2, f3, f4, f5, f6, color5, color5, color4, color4);
        }
    }

    public static void drawLine(double d0, double d1, double d2, double d3, Color color) {
        BufferCollector buffercollector = RenderManager.bufferCollector;
        int i = color.getRGB();
        double d4 = d2 - d0;
        double d5 = d3 - d1;
        double d6 = Math.sqrt(d4 * d4 + d5 * d5);
        if (d6 != 0.0) {
            GlBufferMeshBuilder glbuffermeshbuilder = buffercollector.getBuffer(RenderManager.getLineLayer());
            glbuffermeshbuilder.start();
            glbuffermeshbuilder.pos((float)d0, (float)d1, 0.0F).color(i).pos((float)d2, (float)d3, 0.0F).color(i).putIndices(new int[]{0, 1});
            glbuffermeshbuilder.end();
        }
    }

    public static void drawAxes(Color color, double d0, double d1, float f, float f1, float f2, float f3) {
        GlBufferMeshBuilder glbuffermeshbuilder = RenderManager.bufferCollector.getBuffer(RenderManager.getTriangleLayer());
        int i = color.getRGB();
        Matrix4f matrix4f = new Matrix4f().translate((float)d0, (float)d1, 0.0F).rotateZ((float)Math.toRadians(f3)).translate(-f1 / 2.0F, f / 2.0F, 0.0F);
        glbuffermeshbuilder.start();
        addQuad(glbuffermeshbuilder, matrix4f, 0.0F, -f, f2, 0.0F, i);
        addQuad(glbuffermeshbuilder, matrix4f, 0.0F, -f2, f1, 0.0F, i);
        glbuffermeshbuilder.end();
    }

    public static Color lerpColor(Color color, Color color1, float f) {
        float f1 = color.getRed();
        int k2 = color1.getRed();
        int j1 = color.getRed();
        int i1 = k2;
        int k1 = (int)(f1 + (i1 - j1) * f);
        int i = clampChannel(k1);
        f1 = color.getGreen();
        k2 = color1.getGreen();
        j1 = color.getGreen();
        i1 = k2;
        int l1 = (int)(f1 + (i1 - j1) * f);
        int j = clampChannel(l1);
        f1 = color.getBlue();
        k2 = color1.getBlue();
        j1 = color.getBlue();
        i1 = k2;
        int i2 = (int)(f1 + (i1 - j1) * f);
        int k = clampChannel(i2);
        f1 = color.getAlpha();
        k2 = color1.getAlpha();
        j1 = color.getAlpha();
        i1 = k2;
        int j2 = (int)(f1 + (i1 - j1) * f);
        int l = clampChannel(j2);
        return new Color(i, j, k, l);
    }

    public static void drawGlTexture(GlTexture gltexture, float f, float f1, float f2, float f3, float f4, float f5, Color color) {
        if (textureRenderer != null) {
            textureRenderer.render(gltexture, f, f1, f2, f3, f4, f5, color);
        }
    }

    public static void drawBlurFull(float f, float f1, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f10, float f9) {
        if (blurRenderer != null) {
            BlurShaderRenderer blurshaderrenderer = blurRenderer;
            new Matrix4f();
            blurshaderrenderer.renderRoundedRect(f, f1, f2, f3, f4, f5, f6, f7, f8, f9, 0.2F, 0.0F, 0.0F, 0.0F);
        }
    }

    public static void drawTexture(AbstractTexture abstracttexture, float f, float f1, float f2, float f3, float f4, float f5, Color color) {
        if (textureRenderer != null) {
            try {
                TextureRectangleRenderer texturerectanglerenderer = textureRenderer;
                GlTexture gltexture = RLTextureLoader.loadTexture(getTextureIdentifier(abstracttexture).getPath());
                texturerectanglerenderer.render(gltexture, f, f1, f2, f3, f4, f5, color);
            } catch (IOException ioexception) {
                throw new RuntimeException(ioexception);
            }
        }
    }

    private static void drawAtlasItem(int i, int j, Object object, int k, int l, float f, GpuTexture gputexture) {
        int i1 = getGlId(gputexture);
        float f1 = (float)k / l;
        float f2 = (float)(-k) / l;
        int j1 = (int)(16.0F * f);
        int k1 = (int)(16.0F * f);
        int l1 = RenderManager.getCurrentFramebuffer();
        TextureLayerKey texturelayerkey = new TextureLayerKey(
            l1,
            i1,
            RenderManager.scissorEnabled,
            RenderManager.scissorBox[0],
            RenderManager.scissorBox[1],
            RenderManager.scissorBox[2],
            RenderManager.scissorBox[3],
            texProgram
        );
        RenderLayer renderlayer = (RenderLayer)layerCache.get(texturelayerkey);
        if (renderlayer == null) {
            if (layerCache.size() >= 256) {
                layerCache.clear();
            }

            renderlayer = new RenderLayer(
                VertexFormats.POSITION_UV,
                4,
                false,
                new StateApplier[]{
                    StateApplier.framebufferState(l1),
                    StateApplier.shaderState(texProgram),
                    StateApplier.scissorState(
                        RenderManager.scissorEnabled,
                        !RenderManager.scissorEnabled,
                        RenderManager.scissorBox[0],
                        RenderManager.scissorBox[1],
                        RenderManager.scissorBox[2],
                        RenderManager.scissorBox[3]
                    ),
                    StateApplier.flagState(2929, false, true),
                    StateApplier.flagState(2884, false),
                    StateApplier.blendState(true, true),
                    StateApplier.depthFuncState(515, 515),
                    StateApplier.apply(renderpass -> {
                        GL13.glActiveTexture(33984);
                        GL11.glBindTexture(3553, i1);
                        RLUniforms.get(renderpass.shaderProgram.glId()).set1i("texSampler", 0);
                    })
                }
            );
            layerCache.put(texturelayerkey, renderlayer);
        }

        RenderManager.uiRenderer.addElement(new ItemAtlasElement(renderlayer, i, j, j1, k1, Color.WHITE.getRGB(), 0.0F, 0.0F, f1, f2, object));
    }

    private static int clampChannel(int i) {
        return Math.max(0, Math.min(255, i));
    }

    public static void fillBox(Matrix4f matrix4f, double d0, double d1, double d2, double d3, double d4, double d5, int i) {
        GlBufferMeshBuilder glbuffermeshbuilder = RenderManager.bufferCollector.getBuffer(RenderManager.getTriangleNoCullLayer());
        glbuffermeshbuilder.start();
        double d6 = d0 + d4;
        double d7 = d1 + d4;
        double d8 = d2 - d4;
        double d9 = d3 - d4;
        double[][] adouble = new double[][]{{d8, d9}, {d8, d7}, {d6, d7}, {d6, d9}};

        for (int j = 0; j < 4; j++) {
            double d10 = adouble[j][0];
            double d11 = adouble[j][1];
            double d12 = j * 90;
            double d13 = d12 + 90.0;

            for (double d14 = 90.0 / d5; d12 <= d13; d12 += d14) {
                double d15 = Math.toRadians(d12);
                float f = (float)(d10 + Math.sin(d15) * d4);
                float f1 = (float)(d11 + Math.cos(d15) * d4);
                glbuffermeshbuilder.pos(matrix4f, f, f1, 0.0F).color(i);
            }
        }

        ((GlBufferMeshBuilder)glbuffermeshbuilder.generateIndices(DrawType.TRIANGLES_FAN)).end();
    }

    public static void drawPlayerHead(AbstractClientPlayerEntity abstractclientplayerentity, float f, float f1, int i, int j, Color color) {
        if (VoilClient.mc.player != null && abstractclientplayerentity != null) {
            Pair pair = (Pair)PlayerTargetUtil.skinColors.get(abstractclientplayerentity.getSkin().body().id());
            if (pair == null) {
                pair = (Pair)PlayerTargetUtil.skinColors.get(VoilClient.mc.player.getSkin().body().id());
            }

            if (pair == null) {
                pair = PlayerTargetUtil.defaultSkinTextures;
            }

            if (pair != null) {
                TextureRectangleRenderer texturerectanglerenderer1 = textureRenderer;
                GlTexture gltexture2 = (GlTexture)pair.left();
                float f11 = i;
                float f12 = i;
                float f13 = j;
                float f2 = 1.0F;
                float f3 = f13;
                float f4 = f12;
                float f8 = f11;
                float f9 = f1;
                float f10 = f;
                GlTexture gltexture1 = gltexture2;
                TextureRectangleRenderer texturerectanglerenderer = texturerectanglerenderer1;
                texturerectanglerenderer.render(gltexture1, f10, f9, f8, f4, f3, f2, color);
                if (abstractclientplayerentity.isModelPartVisible(PlayerModelPart.HAT)) {
                    texturerectanglerenderer1 = textureRenderer;
                    gltexture2 = (GlTexture)pair.right();
                    f11 = i;
                    f12 = i;
                    f13 = j;
                    f2 = 1.0F;
                    f3 = f13;
                    f4 = f12;
                    float f5 = f11;
                    float f6 = f1;
                    float f7 = f;
                    GlTexture gltexture = gltexture2;
                    texturerectanglerenderer1.render(gltexture, f7, f6, f5, f4, f3, f2, color);
                }
            }
        }
    }

    public static void drawThickLine(double d0, double d1, double d2, double d3, float f, Color color) {
        BufferCollector buffercollector = RenderManager.bufferCollector;
        int i = color.getRGB();
        double d4 = d2 - d0;
        double d5 = d3 - d1;
        double d6 = Math.sqrt(d4 * d4 + d5 * d5);
        if (d6 != 0.0) {
            d4 /= d6;
            d5 /= d6;
            double d7 = (d0 + d2) / 2.0 + d5 * f;
            double d8 = (d1 + d3) / 2.0 - d4 * f;
            GlBufferMeshBuilder glbuffermeshbuilder = buffercollector.getBuffer(RenderManager.getTriangleLayer());
            glbuffermeshbuilder.start();

            for (int j = 0; j < 10; j++) {
                double d9 = (double)j / 10;
                double d10 = (double)(j + 1) / 10;
                double d11 = (1.0 - d9) * (1.0 - d9) * d0 + 2.0 * (1.0 - d9) * d9 * d7 + d9 * d9 * d2;
                double d12 = (1.0 - d9) * (1.0 - d9) * d1 + 2.0 * (1.0 - d9) * d9 * d8 + d9 * d9 * d3;
                double d13 = (1.0 - d10) * (1.0 - d10) * d0 + 2.0 * (1.0 - d10) * d10 * d7 + d10 * d10 * d2;
                double d14 = (1.0 - d10) * (1.0 - d10) * d1 + 2.0 * (1.0 - d10) * d10 * d8 + d10 * d10 * d3;
                double d15 = d13 - d11;
                double d16 = d14 - d12;
                double d17 = Math.sqrt(d15 * d15 + d16 * d16);
                if (d17 != 0.0) {
                    d15 /= d17;
                    d16 /= d17;
                    double d18 = -d16 * (f / 2.0);
                    double d19 = d15 * (f / 2.0);
                    float f1 = (float)(d11 + d18);
                    float f2 = (float)(d12 + d19);
                    float f3 = (float)(d11 - d18);
                    float f4 = (float)(d12 - d19);
                    float f5 = (float)(d13 + d18);
                    float f6 = (float)(d14 + d19);
                    float f7 = (float)(d13 - d18);
                    float f8 = (float)(d14 - d19);
                    int k = glbuffermeshbuilder.countCompleteVertices();
                    glbuffermeshbuilder.pos(f1, f2, 0.0F)
                        .color(i)
                        .pos(f5, f6, 0.0F)
                        .color(i)
                        .pos(f7, f8, 0.0F)
                        .color(i)
                        .pos(f3, f4, 0.0F)
                        .color(i)
                        .putIndices(new int[]{k, k + 1, k + 2, k, k + 2, k + 3});
                }
            }

            glbuffermeshbuilder.end();
        }
    }

    public static void drawIdentifierTexture(DrawContext drawcontext, float f, float f1, int i, int j, Identifier identifier) {
        drawcontext.drawTexture(RenderPipelines.GUI_TEXTURED, identifier, (int)f, (int)f1, 0.0F, 0.0F, i, j, i, j);
    }

    public static void drawRectArgb(float f, float f1, float f2, float f3, int i) {
        float f4;
        if (f > f2) {
            f4 = f2;
            f2 = f;
        } else {
            f4 = f;
        }

        float f5;
        if (f1 > f3) {
            f5 = f3;
            f3 = f1;
        } else {
            f5 = f1;
        }

        RectangleShaderRenderer rectangleshaderrenderer = rectRenderer;
        float f12 = f2 - f4;
        float f13 = f3 - f5;
        Color color = new Color(i, true);
        float f6 = 0.0F;
        float f7 = 0.0F;
        float f8 = f13;
        float f9 = f12;
        float f10 = f5;
        float f11 = f4;
        rectangleshaderrenderer.render(f11, f10, f9, f8, f7, f6, color);
    }

    public static Color interpolateColor(Color color, Color color1, int i, int j) {
        int k = (int)((System.currentTimeMillis() / i + j) % FLOW_KEY);
        int l = k >= 180 ? 360 - k : k;
        k = l + l;
        float f = k / 360.0F;
        return lerpColor(color, color1, f);
    }

    public static void drawTriangle(double d0, double d1, double d2, double d3, double d4, double d5, Color color) {
        GlBufferMeshBuilder glbuffermeshbuilder = RenderManager.bufferCollector.getBuffer(RenderManager.getTriangleNoCullLayer());
        glbuffermeshbuilder.start();
        double d6 = d0 + d4;
        double d7 = d1 + d4;
        double d8 = d2 - d4;
        double d9 = d3 - d4;
        double[][] adouble = new double[][]{{d8, d9}, {d8, d7}, {d6, d7}, {d6, d9}};

        for (int i = 0; i < 4; i++) {
            double d10 = adouble[i][0];
            double d11 = adouble[i][1];
            double d12 = i * 90;
            double d13 = d12 + 90.0;

            for (double d14 = 90.0 / d5; d12 <= d13; d12 += d14) {
                Math.toRadians(d12);
                float f = (float)(d10 + Math.sin(d4) * d4);
                float f1 = (float)(d11 + Math.cos(d4) * d4);
                glbuffermeshbuilder.pos(f, f1, 0.0F).color(color.getRGB());
            }
        }

        ((GlBufferMeshBuilder)glbuffermeshbuilder.generateIndices(DrawType.TRIANGLES_FAN)).end();
    }

    public static void drawItem(ItemStack itemstack, float f, float f1, float f2, boolean flag) {
        if (!itemstack.isEmpty()) {
            KeyedItemRenderState keyeditemrenderstate = new KeyedItemRenderState();
            VoilClient.mc
                .getItemModelManager()
                .clearAndUpdate(keyeditemrenderstate, itemstack, ItemDisplayContext.GUI, VoilClient.mc.world, VoilClient.mc.player, 0);
            Object object = keyeditemrenderstate.getModelKey();
            pendingItems.add(new ItemAtlasEntry(object, keyeditemrenderstate, itemstack.getName().getString()));
            GuiRendererAccessor guirendereraccessor = (GuiRendererAccessor)((GameRendererAccessor)VoilClient.mc.gameRenderer).getGuiRenderer();
            int i = guirendereraccessor.tulip_getWindowScaleFactor();
            int j = 16 * i;
            int k = guirendereraccessor.tulip_calcItemAtlasSideLength(j);
            int i1 = (int)f;
            int l = (int)f1;
            GpuTexture gputexture = guirendereraccessor.getItemAtlasTexture();
            drawAtlasItem(i1, l, object, j, k, f2, gputexture);
            if (flag) {
                if (itemstack.isItemBarVisible()) {
                    i = (int)(f + 2.0F);
                    j = (int)(f1 + 13.0F);
                    float f9 = i;
                    float f3 = j;
                    drawRoundedRect(f9, f3, 13.0F, 2.0F, 0.0F, 0.0F, Color.BLACK);
                    float f10 = i;
                    float f4 = j;
                    drawRoundedRect(f10, f4, itemstack.getItemBarStep(), 1.0F, 0.0F, 0.0F, new Color(ColorHelper.fullAlpha(itemstack.getItemBarColor())));
                }

                float f7 = VoilClient.mc.player == null
                    ? 0.0F
                    : VoilClient.mc.player.getItemCooldownManager().getCooldownProgress(itemstack, VoilClient.mc.getRenderTickCounter().getTickProgress(true));
                if (f7 > 0.0F) {
                    j = (int)(f1 + MathHelper.floor(16.0F * (1.0F - f7)));
                    k = MathHelper.ceil(16.0F * f7);
                    float f5 = j;
                    drawRoundedRect(f, f5, 16.0F, k, 0.0F, 0.0F, new Color(255, 255, 255, 127));
                }

                if (itemstack.getCount() != 1) {
                    String s = String.valueOf(itemstack.getCount());
                    float f8 = 9.5F;
                    VoilClient.fonts
                        .getSelectedFont()
                        .render(s, f + 17.0F - VoilClient.fonts.getSelectedFont().getWidth(s, f8), f1 + 6.0F, f8, Color.WHITE.getRGB());
                }
            }

            if (atlasReady[0]) {
                atlasReady[0] = false;
                float f6;
                drawRoundedRect(f6 = 20, f6, 200, 200, 2.0F, 2.0F, Color.DARK_GRAY);
                RenderManager.uiRenderer
                    .addElement(
                        new PreBuiltTextureRectangleElement(
                            new RenderLayer(
                                VertexFormats.POSITION_UV,
                                4,
                                false,
                                new StateApplier[]{
                                    StateApplier.framebufferState(RenderManager.getCurrentFramebuffer()),
                                    texShaderState,
                                    StateApplier.flagState(2884, false),
                                    StateApplier.blendState(true, true),
                                    StateApplier.depthFuncState(515, 515),
                                    StateApplier.uniformState(rluniforms -> {
                                        GL13.glActiveTexture(33984);
                                        GL11.glBindTexture(3553, getGlId(guirendereraccessor.getItemAtlasTexture()));
                                        atlasReady[0] = true;
                                        rluniforms.set1i("texSampler", 0);
                                    })
                                }
                            ),
                            20,
                            20,
                            200,
                            200,
                            Color.WHITE.getRGB(),
                            0.0F,
                            0.0F,
                            1.0F,
                            1.0F
                        )
                    );
            }
        }
    }

    public static void prepareItemAtlas() {
        GuiRenderer guirenderer = ((GameRendererAccessor)VoilClient.mc.gameRenderer).getGuiRenderer();
        GuiRendererAccessor guirendereraccessor = (GuiRendererAccessor)guirenderer;
        int j = VoilClient.mc.getWindow().getScaleFactor();
        int i = j * 16;
        float f = guirendereraccessor.tulip_calcItemAtlasSideLength(i);
        if (guirendereraccessor.getItemAtlasTexture() == null) {
            guirendereraccessor.tulip_createItemAtlas((int)f);
        }

        for (ItemAtlasEntry itematlasentry : pendingItems) {
            if (!itemAtlasUvs.containsKey(itematlasentry.modelKey())) {
                j = guirendereraccessor.tulip_getItemAtlasX();
                if (j + i > f) {
                    guirendereraccessor.tulip_setItemAtlasX(0);
                    j = guirendereraccessor.tulip_getItemAtlasY();
                    guirendereraccessor.tulip_setItemAtlasY(j + i);
                }

                guirendereraccessor.tulip_prepareItemInitially(
                    itematlasentry.renderState(), new MatrixStack(), guirendereraccessor.tulip_getItemAtlasX(), guirendereraccessor.tulip_getItemAtlasY(), i
                );
                float[] afloat = new float[]{guirendereraccessor.tulip_getItemAtlasX() / f, (f - guirendereraccessor.tulip_getItemAtlasY()) / f};
                itemAtlasUvs.put(itematlasentry.modelKey(), afloat);
                guirendereraccessor.getRenderedItems().put(new Object(), new Object());
                j = guirendereraccessor.tulip_getItemAtlasX();
                guirendereraccessor.tulip_setItemAtlasX(j + i);
            }
        }

        pendingItems.clear();
    }

    public static void drawPlus(Color color, double d0, double d1, float f, float f1, float f2) {
        GlBufferMeshBuilder glbuffermeshbuilder = RenderManager.bufferCollector.getBuffer(RenderManager.getTriangleLayer());
        float f3 = f / 2.0F;
        float f4 = f1 / 2.0F;
        Matrix4f matrix4f = new Matrix4f().translate((float)(d0 + f3), (float)(d1 + f3), 0.0F).rotateZ((float)Math.toRadians(f2)).translate(-f3, -f3, 0.0F);
        glbuffermeshbuilder.start();
        addQuad(glbuffermeshbuilder, matrix4f, f3 - f4, 0.0F, f3 + f4, f, color.getRGB());
        Matrix4f matrix4f1 = new Matrix4f(matrix4f).translate(f3, f3, 0.0F).rotateZ((float)Math.toRadians(90.0)).translate(-f3, -f3, 0.0F);
        addQuad(glbuffermeshbuilder, matrix4f1, f3 - f4, 0.0F, f3 + f4, f, color.getRGB());
        glbuffermeshbuilder.end();
    }

    public static void drawBlurRect(float f, float f1, float f2, float f3, float f4, float f5) {
        if (blurRenderer != null) {
            BufferCollector buffercollector = RenderManager.bufferCollector;
            blurRenderer.drawBlurRectBuffered(buffercollector, f, f1, f2, f3, f4, f5, 2.0F);
        }
    }

    public static AbstractTexture convertImage(BufferedImage bufferedimage, int i) {
        int j = bufferedimage.getWidth();
        int k = bufferedimage.getHeight();
        NativeImage nativeimage = new NativeImage(j, k, false);
        for (int l = 0; l < k; l++) {
            for (int i1 = 0; i1 < j; i1++) {
                nativeimage.setColorArgb(i1, l, bufferedimage.getRGB(i1, l));
            }
        }

        String s2 = new String("converted_" + nativeimage.hashCode());
        NativeImageBackedTexture nativeimagebackedtexture = new NativeImageBackedTexture(s2, j, k, false);
        nativeimagebackedtexture.setImage(nativeimage);
        nativeimagebackedtexture.upload();
        return nativeimagebackedtexture;
    }

    public static void drawBlurRoundedTinted(float f, float f1, float f2, float f3, float f4, float f5, float f6, Color color) {
        if (blurRenderer != null) {
            BufferCollector buffercollector = RenderManager.bufferCollector;
            blurRenderer.drawColoredRect(buffercollector, f, f1, f2, f3, f4, f5, f6, color);
        }
    }

    public static void addQuad(GlBufferMeshBuilder glbuffermeshbuilder, Matrix4f matrix4f, float f, float f1, float f2, float f3, int i) {
        glbuffermeshbuilder.pos(matrix4f, f, f3, 0.0F)
            .color(i)
            .pos(matrix4f, f2, f3, 0.0F)
            .color(i)
            .pos(matrix4f, f2, f1, 0.0F)
            .color(i)
            .pos(matrix4f, f, f1, 0.0F)
            .color(i);
    }

    public static void clearItemCaches() {
        itemAtlasUvs.clear();
        pendingItems.clear();
    }

    public static void drawGlow(float f, float f1, float f2, float f3, float f4, float f5, float f6, Color color) {
        if (glowRenderer != null) {
            glowRenderer.render(f, f1, f2, f3, f4, f5, f6, color);
        }
    }

    public static void drawCurve(float f, float f1, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9, Color color) {
        if (bezierRenderer != null) {
            bezierRenderer.render(f, f1, f2, f3, f4, f5, f6, f7, f8, f9, color);
        }
    }

    public static void drawStar(float f, float f1, float f2, Color color) {
        if (starRenderer != null) {
            starRenderer.render(f, f1, f2, color, null, 0.0F);
        }
    }

    public static void drawBlurVariableBasic(float f, float f1, float[] afloat, float[] afloat1, float f2, float f3, float f5, float f4) {
        if (blurRenderer != null) {
            BufferCollector buffercollector = RenderManager.bufferCollector;
            blurRenderer.renderRoundedRectArrays(buffercollector, f, f1, afloat, afloat1, f2, f2, f2, f2, f3, f4, 0.2F, 0.0F, 0.0F, 0.0F);
        }
    }

    public static void drawText(String s, float f, float f1, boolean flag) {
        float f2 = VoilClient.fonts.getSelectedFont().getWidth(s);
        float f3 = VoilClient.fonts.getSelectedFont().getLineHeight();
        float f4 = f2 + 6.0F;
        float f5 = f3 + 2.0F;
        if (flag) {
            Color color = GuiTheme.accentColor1;
            drawRoundedRect(f, f1, f4, f5, 4.0F, 1.0F, ColorUtil.withAlpha(color, 128));
            drawOutline(f, f1, f4, f5, 4.0F, 0.3F, 0.4F, GuiTheme.accentColor1);
        }

        float f6 = f + (f4 - f2) / 2.0F;
        float f7 = f1 + (f5 - f3) / 2.0F;
        VoilClient.fonts.getSelectedFont().render(s, f6, f7, GuiTheme.normalTextColor);
    }

    public static void drawStarOutline(float f, float f1, float f2, Color color, Color color1, float f3) {
        if (starRenderer != null) {
            starRenderer.render(f, f1, f2, color, color1, f3);
        }
    }

    public static void drawGradient(float f, float f1, float f2, float f3, float f4, float f5, GradientSetting gradientsetting, float f6, float f7) {
        float[] afloat1 = gradientsetting.getPositions();
        Color[] acolor1 = gradientsetting.getColors();
        boolean flag1 = gradientsetting.getMode() == GradientType.RADIAL;
        float f8 = gradientsetting.getAngle();
        boolean flag = flag1;
        Color[] acolor = acolor1;
        float[] afloat = afloat1;
        drawGradientStops(f, f1, f2, f3, f4, f5, afloat, acolor, flag, f8, f6, f7);
    }

    public static void drawBlurFullTinted(
        float f, float f1, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f25, float f9, Color color
    ) {
        if (blurRenderer != null) {
            BlurShaderRenderer blurshaderrenderer = blurRenderer;
            new Matrix4f();
            float f24 = color.getRed() / 255.0F;
            float f10 = color.getBlue() / 255.0F;
            float f11 = color.getGreen() / 255.0F;
            float f12 = f24;
            float f13 = 0.5F;
            float f14 = f9;
            float f15 = f8;
            float f16 = f7;
            float f17 = f6;
            float f18 = f5;
            float f19 = f4;
            float f20 = f3;
            float f21 = f2;
            float f22 = f1;
            float f23 = f;
            blurshaderrenderer.renderRoundedRect(f23, f22, f21, f20, f19, f18, f17, f16, f15, f14, f13, f12, f11, f10);
        }
    }

    public static void drawBlurRectEx(float f, float f1, float f2, float f3, float f4, float f5, float f6) {
        if (blurRenderer != null) {
            BufferCollector buffercollector = RenderManager.bufferCollector;
            blurRenderer.drawBlurRectBuffered(buffercollector, f, f1, f2, f3, f4, f5, f6);
        }
    }

    public static int getGlId(GpuTexture gputexture) {
        if (gputexture instanceof net.minecraft.client.texture.GlTexture) {
            int i = ((net.minecraft.client.texture.GlTexture)gputexture).getGlId();
            return i == -1 ? -1 : i;
        } else {
            return -1;
        }
    }

    public static GlTexture getGlTexture(Identifier identifier) throws IOException {
        return glTextureCache.computeIfAbsent(identifier, key -> {
            try {
                Optional optional = MinecraftClient.getInstance().getResourceManager().getResource(key);
                return optional.isEmpty() ? null : RLTextureLoader.loadTexture(key.toString(), 0, 0, true);
            } catch (IOException ioexception) {
                throw new RuntimeException(ioexception);
            }
        });
    }

    public static void drawBlurSimple(float f, float f1, float f2, float f3, float f4, float f5) {
        if (blurRenderer != null) {
            BufferCollector buffercollector = RenderManager.bufferCollector;
            blurRenderer.drawRoundedRectSimple(buffercollector, f, f1, f2, f3, f4, f5);
        }
    }

    public static void drawModuleGlow(
        float f,
        float f1,
        float f2,
        float f3,
        float f4,
        float f5,
        float f6,
        Color color,
        Color color1,
        Color color2,
        float f7,
        float f8,
        float f9,
        float f10,
        float f11,
        int i,
        float f12
    ) {
        if (moduleGlowRenderer != null) {
            moduleGlowRenderer.render(f, f1, f2, f3, f4, f5, f6, color, color1, color2, f7, f8, f9, f10, f11, i, f12);
        }
    }

    public static void drawBlurTinted(float f, float f1, float f2, float f3, float f4, float f5, float f6, float f7, Color color) {
        if (blurRenderer != null) {
            BufferCollector buffercollector = RenderManager.bufferCollector;
            blurRenderer.drawColoredRectAlt(buffercollector, f, f1, f2, f3, f4, f5, f6, f7, color);
        }
    }

    public static void drawGradientRounded(float f, float f1, float f2, float f3, float f4, float f5, float f6, GradientSetting gradientsetting, float f7) {
        if (gradientRenderer != null) {
            GradientShaderRenderer gradientshaderrenderer = gradientRenderer;
            float[] afloat1 = gradientsetting.getPositions();
            Color[] acolor1 = gradientsetting.getColors();
            boolean flag1 = gradientsetting.getMode() == GradientType.RADIAL;
            float f8 = gradientsetting.getAngle();
            boolean flag = flag1;
            Color[] acolor = acolor1;
            float[] afloat = afloat1;
            float f9 = f3;
            float f10 = f2;
            float f11 = f1;
            float f12 = f;
            float f13 = f6;
            float f14 = f5;
            float f15 = f4;
            float f16 = f3;
            float f17 = f2;
            float f18 = f1;
            float f19 = f;
            gradientshaderrenderer.render(f19, f18, f17, f16, f15, f14, f13, f12, f11, f10, f9, afloat, acolor, flag, f8, 0.0F, f7);
        }
    }

    public static void drawModuleGlowFilled(
        float f,
        float f1,
        float f2,
        float f3,
        float f4,
        float f5,
        float f6,
        Color color,
        Color color1,
        Color color2,
        float f7,
        float f8,
        float f9,
        float f10,
        float f11,
        int i,
        float f12
    ) {
        if (moduleGlowRenderer != null) {
            moduleGlowRenderer.renderVariant(f, f1, f2, f3, f4, f5, f6, color, color1, color2, f7, f8, f9, f10, f11, i, f12);
        }
    }

    public static void drawBlurVariableColored(float f, float f1, float[] afloat, float[] afloat1, float f2, float f3, float f18, float f4, Color color) {
        if (blurRenderer != null) {
            BlurShaderRenderer blurshaderrenderer = blurRenderer;
            BufferCollector buffercollector1 = RenderManager.bufferCollector;
            float f17 = color.getRed() / 255.0F;
            float f5 = color.getBlue() / 255.0F;
            float f6 = color.getGreen() / 255.0F;
            float f7 = f17;
            float f8 = 0.5F;
            float f9 = f4;
            float f10 = f3;
            float f11 = f2;
            float f12 = f2;
            float f13 = f2;
            float f14 = f2;
            float[] afloat2 = afloat1;
            float[] afloat3 = afloat;
            float f15 = f1;
            float f16 = f;
            BufferCollector buffercollector = buffercollector1;
            blurshaderrenderer.renderRoundedRectArrays(buffercollector, f16, f15, afloat3, afloat2, f14, f13, f12, f11, f10, f9, f8, f7, f6, f5);
        }
    }

    public static void drawVariableGlowGradient(
        float f, float f1, float[] afloat, float[] afloat1, float f2, float f3, float f4, Color color, Color color1, Color color2, Color color3
    ) {
        if (variableGlowRenderer != null) {
            variableGlowRenderer.renderVariableWidth(f, f1, afloat, afloat1, f2, f2, f2, f2, f3, f4, color, color1, color2, color3);
        }
    }

    public static void drawVariableGlow(float f, float f1, float[] afloat, float[] afloat1, float f2, float f3, float f4, Color color) {
        drawVariableGlowGradient(f, f1, afloat, afloat1, f2, f3, f4, color, color, color, color);
    }

    public static void drawFadeLineHorizontal(float f, float f1, float f2, float f3, Color color) {
        if (fadeLineRenderer != null) {
            fadeLineRenderer.render(f, f1, f2, f3, false, f3, color);
        }
    }

    public static void drawGradientRoundedEx(
        float f, float f1, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9, GradientSetting gradientsetting, float f10
    ) {
        if (gradientRenderer != null) {
            GradientShaderRenderer gradientshaderrenderer = gradientRenderer;
            float[] afloat1 = gradientsetting.getPositions();
            Color[] acolor1 = gradientsetting.getColors();
            boolean flag1 = gradientsetting.getMode() == GradientType.RADIAL;
            float f11 = gradientsetting.getAngle();
            boolean flag = flag1;
            Color[] acolor = acolor1;
            float[] afloat = afloat1;
            float f12 = f9;
            float f13 = f8;
            float f14 = f7;
            float f15 = f6;
            float f16 = 0.0F;
            float f17 = f5;
            float f18 = f4;
            float f19 = f3;
            float f20 = f2;
            float f21 = f1;
            float f22 = f;
            gradientshaderrenderer.render(f22, f21, f20, f19, f18, f17, f16, f15, f14, f13, f12, afloat, acolor, flag, f11, 0.0F, f10);
        }
    }

    public static void drawStarRotated(float f, float f1, float f2, Color color, Color color1, float f3, float f4, float f5) {
        if (starRenderer != null) {
            starRenderer.render(f, f1, f2, color, color1, f3, f4, f5);
        }
    }

    public static void drawStarWithOutline(float f, float f1, float f2, Color color, Color color1, float f3, float f4) {
        if (starRenderer != null) {
            starRenderer.render(f, f1, f2, color, color1, f3, f4);
        }
    }

    public static void drawStarAt(float f, float f1, float f2, float f3, Color color) {
        if (starRenderer != null) {
            starRenderer.render(f, f1, f2, null, color, f3);
        }
    }

    public static void drawVariableOutlineGradient(
        float f,
        float f1,
        float f5,
        float f6,
        float f2,
        float f3,
        float f4,
        Color color,
        Color color1,
        Color color2,
        Color color3,
        float[] afloat,
        float[] afloat1
    ) {
        if (outlineRenderer != null) {
            outlineRenderer.render(f, f1, afloat, afloat1, f2, f3, f4, color, color1, color2, color3);
        }
    }

    public static void drawRectCornerRadii(float f, float f1, float f2, float f3, float f4, float f5, float f6, float f7, float f8, Color color) {
        if (rectRenderer != null) {
            rectRenderer.render(f, f1, f2, f3, f6, f7, f4, f5, f8, color);
        }
    }

    public static void drawBackground(float f, float f1, float f2, float f3, float f4, float f5, Color color, Color color1, float f6) {
        if (backgroundRenderer != null) {
            backgroundRenderer.render(f, f1, f2, f3, f4, f5, color, color1, f6);
        }
    }

    public static void drawCircle(float f, float f1, float f2, float f3, Color color) {
        drawRoundedRect(f, f1, f2 * 2.0F, f2 * 2.0F, f2, f3, color);
    }

    public static void drawVariableGlowRect(float f, float f1, float f2, float f3, float f4, float f5, float f6, Color color) {
        if (variableGlowRenderer != null) {
            variableGlowRenderer.render(f, f1, f2, f3, f4, f5, f6, color);
        }
    }

    public static void drawRoundedRectGradientVertical(float f, float f1, float f2, float f3, float f4, float f5, Color color, Color color1, int i) {
        if (rectRenderer != null) {
            Color color2 = interpolateColor(color, color1, i, (int)f1);
            Color color3 = interpolateColor(color, color1, i, (int)(f1 + f3));
            rectRenderer.render(f, f1, f2, f3, f4, f5, color3, color3, color2, color2);
        }
    }

    public static void drawCircleOutline(float f, float f1, float f2, float f3, float f4, Color color) {
        drawOutline(f, f1, f2 * 2.0F, f2 * 2.0F, f2, f3, f4, color);
    }

    public static void drawCircleOutlineAlt(float f, float f1, float f2, float f3, float f4, Color color) {
        drawOutline(f, f1, f2 * 2.0F, f2 * 2.0F, f2, f3, f4, color);
    }

    public static void drawBezierGradient(float[] afloat, float f, float f1, float f2, float f3, float f4, float f5, Color color, Color color1) {
        if (bezierRenderer != null) {
            bezierRenderer.renderPath(afloat, f, f1, f2, f3, f4, f5, color, color1);
        }
    }

    public static void drawBalls(float f, float f1, float f2, float f3, float f4, float f5, float f6) {
        if (ballsRenderer != null) {
            ballsRenderer.render(f, f1, f2, f3, f4, f5, f6);
        }
    }

    public static void drawStarCustom(float f, float f1, float f2, Color color, Color color1, float f3, float f4, float f5, float f6) {
        if (starRenderer != null) {
            starRenderer.render(f, f1, f2, color, color1, f3, f4, f5, f6);
        }
    }

    public static void drawGradientStops(
        float f, float f1, float f2, float f3, float f4, float f5, float[] afloat, Color[] acolor, boolean flag, float f6, float f7, float f8
    ) {
        if (gradientRenderer != null) {
            gradientRenderer.render(f, f1, f2, f3, f4, f5, 0.0F, f, f1, f2, f3, afloat, acolor, flag, f6, f7, f8);
        }
    }

    public static void setupBlend() {
        RenderManager.enableBlend();
        RenderManager.setDefaultBlendFunc();
    }

    public static void drawText(String s, float f, float f1) {
        drawText(s, f, f1, true);
    }

    public static void drawArrow(float f, float f1, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9, Color color) {
        if (arrowRenderer != null) {
            arrowRenderer.render(f, f1, f2, f3, f4, f5, f6, f7, f8, f9, color);
        }
    }

    public static void drawStarFilledRotated(float f, float f1, float f2, Color color, Color color1, float f3, float f4) {
        if (starRenderer != null) {
            starRenderer.render(f, f1, f2, color, color1, f3, 0.0F, 0.45F, f4);
        }
    }

    public static void drawCurveGradient(
        float f, float f1, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9, Color color, Color color1
    ) {
        if (bezierRenderer != null) {
            bezierRenderer.render(f, f1, f2, f3, f4, f5, f6, f7, f8, f9, color, color1);
        }
    }

    public static void disableBlend() {
        RenderManager.disableBlend();
    }

    public static void initRenderers() {
        blurRenderer = new BlurShaderRenderer();
        rectRenderer = new RectangleShaderRenderer();
        glowRenderer = new GlowShaderRenderer();
        outlineRenderer = new OutlineShaderRenderer();
        textureRenderer = new TextureRectangleRenderer();
        backgroundRenderer = new GradientBackgroundShaderRenderer();
        variableGlowRenderer = new VariableGlowRenderer();
        fadeLineRenderer = new FadeLineRenderer();
        framebufferRenderer = new FramebufferBlitRenderer();
        bezierRenderer = new BezierCurveRenderer();
        ballsRenderer = new BallsShaderRenderer();
        arrowRenderer = new ArrowShaderRenderer();
        starRenderer = new StarShaderRenderer();
        kawaseGlowRenderer = new KawaseGlowRenderer();
        gradientRenderer = new GradientShaderRenderer();
        moduleGlowRenderer = new ModuleGlowRenderer();
    }

    public static void drawBlurCircle2(float f, float f1, float f2) {
        float f3;
        float f4 = f3 = f2 * 2.0F;
        float f5 = f1;
        float f6 = f;
        drawBlurRect(f6, f5, f4, f3, f2, 14.0F);
    }

    public static void drawFadeLineVertical(float f, float f1, float f2, float f3, Color color) {
        if (fadeLineRenderer != null) {
            fadeLineRenderer.render(f, f1, f3, f2, true, f3, color);
        }
    }

    public static void drawRoundedRectGradientHorizontal(float f, float f1, float f2, float f3, float f4, float f5, Color color, Color color1, int i) {
        if (rectRenderer != null) {
            Color color2 = interpolateColor(color, color1, i, (int)f1);
            Color color3 = interpolateColor(color, color1, i, (int)(f1 + f3));
            rectRenderer.render(f, f1, f2, f3, f4, f5, color2, color3, color2, color3);
        }
    }

    public static void drawOutlineGradient(
        float f, float f1, float f2, float f3, float f4, float f5, float f6, Color color, Color color1, Color color2, Color color3
    ) {
        if (outlineRenderer != null) {
            outlineRenderer.render(f, f1, f2, f3, f4, f5, f6, color, color1, color2, color3);
        }
    }

    public static Identifier getTextureIdentifier(AbstractTexture abstracttexture) {
        if (abstracttexture == null) {
            return null;
        }

        return TextureIdentifiers.find(abstracttexture);
    }

    public static void drawVariableOutline(float f, float f1, float f5, float f6, float f2, float f3, float f4, Color color, float[] afloat, float[] afloat1) {
        if (outlineRenderer != null) {
            outlineRenderer.render(f, f1, afloat, afloat1, f2, f3, f4, color, color, color, color);
        }
    }

    public static void drawBezier(float[] afloat, float f, float f1, float f2, float f3, float f4, float f5, Color color) {
        if (bezierRenderer != null) {
            bezierRenderer.renderPath(afloat, f, f1, f2, f3, f4, f5, color);
        }
    }
}
