package lol.lwes.voil.util.window;

import dev.prestigeclient.renderlib.impl.api.RenderAPI;
import java.lang.ref.Cleaner.Cleanable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lol.lwes.voil.VoilClient;
import net.minecraft.client.util.Window;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWWindowCloseCallbackI;
import org.lwjgl.opengl.GL30;

public class OverlayWindow implements AutoCloseable {
    private static final Map<Long, List<Runnable>> pendingTasks = Collections.synchronizedMap(new HashMap<>());
    private static final Map<Thread, Long> contextByThread = Collections.synchronizedMap(new HashMap<>());
    private final Cleanable cleanable;
    public final long handle;
    private boolean hidden;
    private int previousFramebuffer;

    public OverlayWindow() {
        this(null);
    }

    public OverlayWindow(GLFWWindowCloseCallbackI glfwwindowclosecallbacki) {
        Window window = VoilClient.mc.getWindow();
        if (window == null) {
            throw new NullPointerException("Minecraft window has not been initialized");
        }

        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(131076, 0);
        GLFW.glfwWindowHint(131075, 0);
        GLFW.glfwWindowHint(131085, 1);
        GLFW.glfwWindowHint(131082, 1);
        GLFW.glfwWindowHint(131079, 1);
        GLFW.glfwWindowHint(131077, 0);
        this.handle = GLFW.glfwCreateWindow(300, 300, "...", 0L, window.getHandle());

        long k = this.handle;
        if (this.handle == 0L) {
            throw new NullPointerException("Failed to create the GLFW window");
        }

        this.makeCurrent();
        k = this.handle;

        GLFW.glfwShowWindow(k);
        if (glfwwindowclosecallbacki != null) {
            GLFW.glfwSetWindowCloseCallback(this.handle, glfwwindowclosecallbacki);
        }

        GLFW.glfwSetWindowOpacity(this.handle, 1.0F);
        long l = window.getHandle();
        Runnable runnable = () -> {};
        long j = l;
        this.syncAndRender(j, runnable);
        pendingTasks.put(this.handle, new ArrayList());

        long i = this.handle;
        this.cleanable = RenderAPI.GL_CLEANER.register(this, () -> RenderAPI.runNowOrPostpone(() -> {
            long k1 = contextByThread.get(Thread.currentThread());
            GLFW.glfwMakeContextCurrent(i);
            List<Runnable> list = pendingTasks.remove(i);
            if (list != null) {
                list.forEach(Runnable::run);
            }

            GLFW.glfwMakeContextCurrent(k1);
            Callbacks.glfwFreeCallbacks(i);
            GLFW.glfwDestroyWindow(i);
        }));
    }

    static {
        Window window = VoilClient.mc.getWindow();
        long i = window != null ? window.getHandle() : -1L;
        contextByThread.put(Thread.currentThread(), i);
    }

    public void syncAndRender(long i, Runnable runnable) {
        int[] aint = new int[]{0};
        int[] aint1 = new int[]{0};
        GLFW.glfwGetWindowPos(i, aint, aint1);
        int[] aint2 = new int[]{0};
        int[] aint3 = new int[]{0};
        GLFW.glfwGetWindowSize(i, aint2, aint3);
        this.makeCurrent();
        GLFW.glfwSetWindowPos(this.handle, aint[0], aint1[0]);
        GLFW.glfwSetWindowSize(this.handle, aint2[0], aint3[0]);
        GL30.glViewport(0, 0, aint2[0], aint3[0]);
        this.clear();
        runnable.run();
        GLFW.glfwSwapBuffers(this.handle);
        this.restoreMainContext();
    }

    public static synchronized boolean queueTask(Runnable runnable) {
        List<Runnable> list = pendingTasks.get(0L);
        if (list == null) {
            return false;
        }

        list.add(runnable);
        return true;
    }

    public void makeCurrent() {
        if (this.previousFramebuffer == -1) {
            this.previousFramebuffer = GL30.glGetInteger(36006);
        }

        GLFW.glfwMakeContextCurrent(this.handle);
        GLFW.glfwSwapInterval(0);
        contextByThread.put(Thread.currentThread(), this.handle);
        GL30.glBindFramebuffer(36160, 0);
        GLFW.glfwSetWindowAttrib(this.handle, 131079, 1);
    }

    private void clear() {
        boolean flag = GL30.glGetBoolean(3089);
        GL30.glDisable(3089);
        GL30.glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        GL30.glClear(16384);
        if (flag) {
            GL30.glEnable(3089);
        }
    }

    public void setHidden(boolean flag) {
        if (flag != this.hidden) {
            this.hidden = flag;
            if (flag) {
                GLFW.glfwHideWindow(this.handle);
            } else {
                GLFW.glfwShowWindow(this.handle);
            }
        }
    }

    public static long getCurrentContextHandle() {
        Window window = VoilClient.mc.getWindow();
        Thread thread = Thread.currentThread();
        if (window == null) {
            return contextByThread.getOrDefault(thread, 0L);
        }

        return contextByThread.getOrDefault(thread, window.getHandle());
    }

    public boolean restoreMainContext() {
        Window window = VoilClient.mc.getWindow();
        if (window == null) {
            return false;
        }

        GLFW.glfwMakeContextCurrent(window.getHandle());
        contextByThread.put(Thread.currentThread(), window.getHandle());
        if (this.previousFramebuffer != -1) {
            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, this.previousFramebuffer);
            this.previousFramebuffer = -1;
        }

        return true;
    }

    @Override
    public void close() throws Exception {
        this.cleanable.clean();
    }
}
