package lol.lwes.voil.module.client;

import java.util.logging.Logger;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.util.CrashHandler;
import lol.lwes.voil.util.SystemUtil;
import lol.lwes.voil.util.window.OverlayWindow;
import net.minecraft.client.util.Monitor;
import net.minecraft.client.util.Window;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.glfw.GLFWWindowCloseCallbackI;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class OBSBypass extends Module {
    public final Object lock;
    public OverlayWindow capture;

    public OBSBypass() {
        super("OBS Bypass", SystemUtil.isLinux() ? "ONLY works on X11 NOT Wayland, Use XWayland OR Use X11" : "voil advertisement module", Category.Client);
        this.lock = new Object();
    }

    @Override
    public void onDisable() {
        mc.send(() -> {
            Object object = null;
            CallbackInfo callbackinfo = new CallbackInfo(null, true);
            this.applyBorderlessFullscreen(callbackinfo, true);
            synchronized (this.lock) {
                if (this.capture != null) {
                    try {
                        this.capture.close();
                    } catch (Exception exception) {
                        CrashHandler.handleModuleCrash(this, exception);
                    }

                    this.capture = null;
                }
            }
        });
    }

    @Override
    public void onEnable() {
        this.onDisable();
        mc.send(() -> {
            CallbackInfo callbackinfo = new CallbackInfo(null, true);
            this.applyBorderlessFullscreen(callbackinfo, false);
            synchronized (this.lock) {
                if (this.capture == null) {
                    GLFWWindowCloseCallbackI glfwwindowclosecallbacki = i -> this.setEnabled(false);
                    this.capture = new OverlayWindow(glfwwindowclosecallbacki);
                }
            }
        });
    }

    @Override
    public void setEnabled(boolean flag) {
        super.setEnabled(flag);
    }

    public void applyBorderlessFullscreen(CallbackInfo callbackinfo, boolean flag) {
        Window window = mc.getWindow();
        if (window != null) {
            if (!window.isFullscreen()) {
                GLFW.glfwSetWindowAttrib(window.getHandle(), 131077, 1);
            } else if (flag) {
                GLFW.glfwSetWindowAttrib(window.getHandle(), 131077, 1);
            } else {
                callbackinfo.cancel();
                if (GLFW.glfwGetWindowMonitor(window.getHandle()) != 0L) {
                    GLFW.glfwSetWindowMonitor(window.getHandle(), 0L, window.getX(), window.getY(), window.getWidth(), window.getHeight(), -1);
                }

                Monitor monitor = window.getMonitor();
                if (monitor == null) {
                    Logger.getLogger("CRITICAL ERROR!!! null monitor");
                } else {
                    GLFWVidMode glfwvidmode = GLFW.glfwGetVideoMode(monitor.getHandle());
                    if (glfwvidmode == null) {
                        Logger.getLogger("CRITICAL ERROR!!! null vidmode");
                    } else {
                        int[] aint = new int[1];
                        int[] aint1 = new int[1];
                        GLFW.glfwGetMonitorPos(monitor.getHandle(), aint, aint1);
                        GLFW.glfwSetWindowAttrib(window.getHandle(), 131077, 0);
                        GLFW.glfwSetWindowPos(window.getHandle(), aint[0], aint1[0]);
                        long j = window.getHandle();
                        int i = glfwvidmode.width();
                        GLFW.glfwSetWindowSize(j, i - 1, glfwvidmode.height());
                    }
                }
            }
        }
    }
}
