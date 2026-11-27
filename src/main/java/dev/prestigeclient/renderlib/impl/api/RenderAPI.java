package dev.prestigeclient.renderlib.impl.api;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.prestigeclient.renderlib.impl.render.events.RenderEvents;
import dev.prestigeclient.renderlib.impl.render.layer.type.BufferCollector;
import java.lang.ref.Cleaner.Cleanable;
import java.lang.ref.Cleaner;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.GlBackend;
import net.minecraft.client.render.Camera;
import net.minecraft.client.texture.GlTexture;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3d;

public class RenderAPI {
    public static final Cleaner GL_CLEANER = Cleaner.create();
    public static final BufferCollector bufferCollector = new BufferCollector();
    private static final List<Runnable> delayedActions = new ArrayList<>();
    public static RenderAPI.RenderContext worldRenderContext = null;
    private static Thread renderThread;
    private static long frameCounter = -9223372036854775807L;
    public static final Cleanable dummyCleanable = () -> {};

    public static void schedule(Runnable runnable) {
        synchronized (delayedActions) {
            delayedActions.add(runnable);
        }
    }

    public static void drawHud(RenderAPI.RenderContext renderapi$rendercontext) {
        RenderEvents.HUD_RENDER.invoker().draw(bufferCollector, renderapi$rendercontext);
        bufferCollector.draw(renderapi$rendercontext);
        RenderEvents.RENDER_POST.invoker().draw(bufferCollector, renderapi$rendercontext);
        bufferCollector.draw(renderapi$rendercontext);
        frameCounter++;
    }

    public static void drawWorld(RenderAPI.RenderContext renderapi$rendercontext) {
        worldRenderContext = renderapi$rendercontext;
        RenderEvents.WORLD_RENDER.invoker().draw(bufferCollector, renderapi$rendercontext);
        bufferCollector.draw(renderapi$rendercontext);
    }

    public static int getMinecraftWindowFbHeight() {
        return MinecraftClient.getInstance().getWindow().getFramebufferHeight();
    }

    public static int getMinecraftFramebuffer() {
        return ((GlTexture)Objects.requireNonNull(MinecraftClient.getInstance().getFramebuffer().getColorAttachment()))
            .getOrCreateFramebuffer(
                ((GlBackend)RenderSystem.getDevice()).getBufferManager(), MinecraftClient.getInstance().getFramebuffer().getDepthAttachment()
            );
    }

    public static int getMinecraftColorTexture() {
        return ((GlTexture)Objects.requireNonNull(MinecraftClient.getInstance().getFramebuffer().getColorAttachment())).getGlId();
    }

    public static Vector3d getRelativePos(RenderAPI.RenderContext renderapi$rendercontext, double d0, double d1, double d2) {
        Vector3d vector3d = new Vector3d(d0, d1, d2);
        Camera camera = renderapi$rendercontext.camera;
        Vec3d vec3d = camera.getCameraPos();
        Vector3d vector3d1 = new Vector3d(vec3d.x, vec3d.y, vec3d.z);
        Vector3d vector3d2 = new Vector3d();
        vector3d.sub(vector3d1, vector3d2);
        return vector3d2;
    }

    public static void runNowOrPostpone(Runnable runnable) {
        if (Thread.currentThread() == renderThread) {
            runnable.run();
        } else {
            schedule(runnable);
        }
    }

    public static float getMinecraftScale() {
        return MinecraftClient.getInstance().getWindow().getScaleFactor();
    }

    public static void setRenderThread(Thread thread) {
        renderThread = thread;
    }

    public static void runDelayedActions() {
        ArrayList<Runnable> arraylist;
        synchronized (delayedActions) {
            arraylist = new ArrayList<>(delayedActions);
            delayedActions.clear();
        }

        for (Runnable runnable : arraylist) {
            runnable.run();
        }
    }

    public static long getFrameCounter() {
        return frameCounter;
    }

    public record RenderContext(Matrix4f projMatrix, Matrix4f modelView, Camera camera) {
    }
}
