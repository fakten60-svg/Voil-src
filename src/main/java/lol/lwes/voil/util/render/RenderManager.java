package lol.lwes.voil.util.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.prestigeclient.renderlib.impl.api.RenderAPI;
import dev.prestigeclient.renderlib.impl.core.GlStateGuard;
import dev.prestigeclient.renderlib.impl.mixin.GuiRendererAccessor;
import dev.prestigeclient.renderlib.impl.mixin.ProjectionMatrix2Accessor;
import dev.prestigeclient.renderlib.impl.render.InternalShaders;
import dev.prestigeclient.renderlib.impl.render.layer.type.BufferCollector;
import dev.prestigeclient.renderlib.impl.render.layer.type.RenderLayer;
import dev.prestigeclient.renderlib.impl.render.state.StateApplier;
import dev.prestigeclient.renderlib.impl.render.ui.UiRenderer;
import dev.prestigeclient.renderlib.impl.render.ui.built.DefaultPreBuiltVertices;
import dev.prestigeclient.renderlib.impl.render.vertex.VertexElement;
import dev.prestigeclient.renderlib.impl.render.vertex.VertexFormat;
import dev.prestigeclient.renderlib.impl.render.vertex.VertexFormats;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.lang.StackWalker.Option;
import java.nio.ByteBuffer;
import java.util.Collection;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.mixin.accessor.GameRendererAccessor;
import lol.lwes.voil.module.ModuleManager;
import lol.lwes.voil.module.client.OBSBypass;
import lol.lwes.voil.util.render.shader.FramebufferBlitRenderer;
import lol.lwes.voil.util.window.OverlayWindow;
import lol.lwes.voil.util.world.EntityUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.Window;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL32;
import org.lwjgl.system.MemoryStack;

public class RenderManager {
    public static DrawContext drawContext;
    private static final ObsBypassFramebuffer obsFramebuffer = new ObsBypassFramebuffer();
    private static Collection<FramebufferBlitRenderer> framebuffers;
    public static final UiRenderer uiRenderer = new UiRenderer();
    public static final BufferCollector bufferCollector = new TrackingBufferCollector();
    public static RenderAPI.RenderContext renderContext;
    public static boolean guiRendered = false;
    private static GlStateGuard stateGuard;
    public static boolean scissorEnabled = false;
    public static float scissorScale = 1.0F;
    public static int[] scissorBox = new int[4];
    public static int[] scissorRect = new int[4];
    public static final VertexFormat posFormat = new VertexFormat(new VertexElement[]{new VertexElement(3, 5126, 4, false)});
    private static final Int2ObjectOpenHashMap triangleNoCullLayers = new Int2ObjectOpenHashMap();
    private static final Int2ObjectOpenHashMap triangleLayers = new Int2ObjectOpenHashMap();
    private static final Int2ObjectOpenHashMap lineLayers = new Int2ObjectOpenHashMap();

    static {
        obsFramebuffer.drawn = true;
        DefaultPreBuiltVertices.SQUARE.getVAOIndexMesh();
        StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE);
        new VertexFormat(new VertexElement[]{new VertexElement(3, 5126, 4, false), new VertexElement(2, 5126, 4, false), new VertexElement(4, 5121, 1, true)});
    }

    public static RenderLayer getLineLayer() {
        return getOrCreateLayer(lineLayers, 1, true);
    }

    public static void beginContext(Matrix4f matrix4f, Matrix4f matrix4f1, Camera camera) {
        if (stateGuard == null) {
            stateGuard = GlStateGuard.capture();
        }

        framebuffers = VoilClient.getHudLayout().getAll();
        RenderAPI.runDelayedActions();
        renderContext = new RenderAPI.RenderContext(matrix4f, matrix4f1, camera);
    }

    public static int getCurrentFramebuffer() {
        for (FramebufferBlitRenderer framebufferblitrenderer : framebuffers) {
            if (framebufferblitrenderer.drawn) {
                return framebufferblitrenderer.getHeight();
            }
        }

        if (Render2DUtil.framebufferRenderer != null && Render2DUtil.framebufferRenderer.drawn) {
            return Render2DUtil.framebufferRenderer.getHeight();
        } else {
            return obsFramebuffer.drawn ? obsFramebuffer.getHeight() : RenderAPI.getMinecraftFramebuffer();
        }
    }

    public static void setDrawContext(DrawContext drawcontext) {
        drawContext = drawcontext;
    }

    public static void setDepthFunc(int i) {
        GL32.glDepthFunc(i);
    }

    public static void disableBlend() {
        GL32.glDisable(3042);
    }

    public static void beginHud() {
        Window window = VoilClient.mc.getWindow();
        GuiRenderer guirenderer = ((GameRendererAccessor)VoilClient.mc.gameRenderer).getGuiRenderer();
        Matrix4f matrix4f2 = ((ProjectionMatrix2Accessor)((GuiRendererAccessor)guirenderer).getGuiProjectionMatrix())
            .callGetMatrix(window.getFramebufferWidth() / GuiScale.getEffectiveScale(), window.getFramebufferHeight() / GuiScale.getEffectiveScale());
        Matrix4f matrix4f3 = new Matrix4f().setTranslation(0.0F, 0.0F, -10000.0F);
        Camera camera = VoilClient.mc.gameRenderer.getCamera();
        Matrix4f matrix4f = matrix4f3;
        Matrix4f matrix4f1 = matrix4f2;
        beginContext(matrix4f1, matrix4f, camera);
    }

    public static void enableScissor() {
        setScissorEnabled(true);
    }

    public static void endFrame(RenderSpace renderspace) {
        drawFrame(renderspace);
        disableScissor();
        stateGuard.restore();
        stateGuard = null;
    }

    public static void drawFrame(RenderSpace renderspace) {
        if (renderspace == RenderSpace.HUD) {
            uiRenderer.draw(renderContext);
        }

        bufferCollector.draw(renderContext);
        if (Render2DUtil.framebufferRenderer != null) {
            GL32.glDisable(2929);
            RenderAPI.RenderContext renderapi$rendercontext = renderContext;
            Render2DUtil.framebufferRenderer.drawBuffer(renderapi$rendercontext);
            Render2DUtil.framebufferRenderer.modelMatrix = new Matrix4f().normal();
            GL32.glEnable(2929);
        }

        obsFramebuffer.drawn = false;
        ModuleManager modulemanager = ModuleManager.INSTANCE;
        OBSBypass obsbypass = (OBSBypass)modulemanager.getModule(OBSBypass.class);
        boolean flag = obsbypass != null && obsbypass.isEnabled();
        obsFramebuffer.useDefaultFramebuffer = flag;
        if (flag) {
            if (renderspace == RenderSpace.HUD) {
                OverlayWindow overlaywindow = obsbypass.capture;
                Window window = VoilClient.mc.getWindow();
                synchronized (obsbypass.lock) {
                    if (overlaywindow != null && window != null) {
                        boolean flag1 = GLFW.glfwGetWindowAttrib(window.getHandle(), 131073) == 1
                            || GLFW.glfwGetWindowAttrib(overlaywindow.handle, 131073) == 1;
                        overlaywindow.setHidden(window.isMinimized() || !flag1);
                        long j = window.getHandle();
                        Runnable runnable = () -> {
                            RenderAPI.RenderContext renderapi$rendercontext2 = renderContext;
                            obsFramebuffer.drawBuffer(renderapi$rendercontext2);
                        };
                        long i = j;
                        overlaywindow.syncAndRender(i, runnable);
                    }
                }
            }
        } else {
            RenderAPI.RenderContext renderapi$rendercontext1 = renderContext;
            obsFramebuffer.drawBuffer(renderapi$rendercontext1);
        }

        obsFramebuffer.drawn = true;
    }

    private static void setScissorEnabled(boolean flag) {
        scissorEnabled = flag;
    }

    public static void beginWorld(Matrix4f matrix4f) {
        Matrix4fStack matrix4fstack = RenderSystem.getModelViewStack();
        Matrix4f matrix4f1 = new Matrix4f(matrix4fstack);
        Camera camera = VoilClient.mc.gameRenderer.getCamera();
        EntityUtil.viewMatrix.set(matrix4f1);
        EntityUtil.projectionMatrix.set(matrix4f);
        EntityUtil.viewport = new int[]{0, 0, VoilClient.mc.getWindow().getFramebufferWidth(), VoilClient.mc.getWindow().getFramebufferHeight()};
        beginContext(matrix4f, matrix4f1, camera);
    }

    public static DrawContext getDrawContext() {
        return drawContext;
    }

    public static void setScissor(int i, int j, int k, int l) {
        float f = GuiScale.getEffectiveScale();
        setScissorEnabled(true);
        scissorBox[0] = Math.round(i * f);
        scissorBox[1] = Math.round((GuiScale.getScaledHeight() - l) * f);
        scissorBox[2] = Math.max(Math.round((k - i) * f), 0);
        scissorBox[3] = Math.max(Math.round((l - j) * f), 0);
        scissorRect[0] = i;
        scissorRect[1] = j;
        scissorRect[2] = k;
        scissorRect[3] = l;
        scissorScale = f / VoilClient.mc.getWindow().getScaleFactor();
    }

    public static void disableScissor() {
        setScissorEnabled(false);
    }

    public static void setDefaultBlendFunc() {
        GL32.glBlendFuncSeparate(770, 771, 1, 0);
    }

    public static boolean isScissorEnabled() {
        return scissorEnabled;
    }

    public static void enableCull() {
        GL32.glEnable(2884);
    }

    public static void flush() {
        boolean[] aboolean = new boolean[4];

        try (MemoryStack memorystack = MemoryStack.stackPush()) {
            ByteBuffer bytebuffer = memorystack.malloc(4);
            GL32.glGetBooleanv(3107, bytebuffer);

            for (int i = 0; i < 4; i++) {
                aboolean[i] = bytebuffer.get(i) != 0;
            }
        }

        boolean flag = GL32.glIsEnabled(2929);
        boolean flag1 = GL32.glIsEnabled(3089);
        int[] aint = new int[4];
        GL32.glGetIntegerv(3088, aint);
        GL32.glColorMask(true, true, true, true);
        GL32.glBlendEquation(32774);
        GL32.glDisable(2929);
        GL32.glScissor(0, 0, VoilClient.mc.getWindow().getFramebufferWidth(), VoilClient.mc.getWindow().getFramebufferHeight());
        GL32.glDisable(3089);
        uiRenderer.draw(renderContext);
        bufferCollector.draw(renderContext);
        GL32.glColorMask(aboolean[0], aboolean[1], aboolean[2], aboolean[3]);
        if (flag) {
            GL32.glEnable(2929);
        }

        GL32.glScissor(aint[0], aint[1], aint[2], aint[3]);
        if (flag1) {
            GL32.glEnable(3089);
        } else {
            GL32.glDisable(3089);
        }

        disableScissor();
        if (stateGuard != null) {
            stateGuard.restore();
            stateGuard = null;
        }
    }

    public static void enableDepthTest() {
        GL32.glEnable(2929);
    }

    public static Vector4f getScissorRect() {
        if (!scissorEnabled) {
            return null;
        }

        float f = scissorScale;
        return new Vector4f(scissorRect[0] * f, scissorRect[1] * f, scissorRect[2] * f, scissorRect[3] * f);
    }

    public static void setBlendFunc(int i, int j, int k, int l) {
        GL32.glBlendFuncSeparate(i, j, k, l);
    }

    public static RenderLayer getTriangleNoCullLayer() {
        return getOrCreateLayer(triangleNoCullLayers, 4, false);
    }

    public static void disableDepthTest() {
        GL32.glDisable(2929);
    }

    public static void disableCull() {
        GL32.glDisable(2884);
    }

    public static void enableBlend() {
        GL32.glEnable(3042);
    }

    private static RenderLayer getOrCreateLayer(Int2ObjectOpenHashMap int2objectopenhashmap, int i, boolean flag) {
        int j = getCurrentFramebuffer();
        RenderLayer renderlayer = (RenderLayer)int2objectopenhashmap.get(j);
        if (renderlayer == null) {
            if (int2objectopenhashmap.size() >= 16) {
                int2objectopenhashmap.clear();
            }

            renderlayer = new RenderLayer(
                VertexFormats.POSITION_COLOR,
                i,
                false,
                new StateApplier[]{
                    new StateApplier.TargetFramebufferState(j),
                    new StateApplier.ShaderState(InternalShaders.POS_COLOR),
                    new StateApplier.GlFlagState(2884, flag),
                    new StateApplier.GlFlagState(3089, false, false),
                    new StateApplier.SimpleBlendState(true, true),
                    new StateApplier.DepthFuncState(515, 515)
                }
            );
            int2objectopenhashmap.put(j, renderlayer);
        }

        return renderlayer;
    }

    public static RenderLayer getTriangleLayer() {
        return getOrCreateLayer(triangleLayers, 4, true);
    }
}
