package lol.lwes.voil.util.render;

import java.util.ArrayDeque;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.config.ClientSettings;
import lol.lwes.voil.config.ThemeProperty;
import org.lwjgl.glfw.GLFW;

public class GuiScale {
    private static final ArrayDeque scaleStack = new ArrayDeque();
    private static float overrideScale = -1.0F;
    private static float lockedScale = -1.0F;

    private GuiScale() {
    }

    public static float getScaledWidth() {
        return isScaleOverridden() ? VoilClient.mc.getWindow().getFramebufferWidth() / overrideScale : VoilClient.mc.getWindow().getScaledWidth();
    }

    private static float getScaleSetting(ThemeProperty themeproperty) {
        Object object = themeproperty.getValue();
        float f = object instanceof Number ? ((Number)object).floatValue() : 100.0F;
        return Math.max(0.25F, f / 100.0F);
    }

    public static void popScale() {
        overrideScale = scaleStack.isEmpty() ? -1.0F : (Float)scaleStack.pop();
    }

    public static boolean isScaleOverridden() {
        return overrideScale > 0.0F;
    }

    public static float getGuiHeight() {
        return VoilClient.mc.getWindow().getFramebufferHeight() / getGuiScale();
    }

    public static boolean isVanillaScale() {
        Object object = ClientSettings.followMcScale.getValue();
        return object instanceof Boolean && (Boolean)object;
    }

    public static float getScaledHeight() {
        return isScaleOverridden() ? VoilClient.mc.getWindow().getFramebufferHeight() / overrideScale : VoilClient.mc.getWindow().getScaledHeight();
    }

    public static float getGuiScale() {
        if (isVanillaScale()) {
            return getWindowScaleFactor();
        }

        ThemeProperty themeproperty = ClientSettings.hudScale;
        return 2.0F * getScaleSetting(themeproperty);
    }

    public static float getEffectiveScale() {
        return isScaleOverridden() ? overrideScale : getWindowScaleFactor();
    }

    public static void pushScale(float f) {
        scaleStack.push(overrideScale);
        overrideScale = f;
    }

    public static float getGuiWidth() {
        return VoilClient.mc.getWindow().getFramebufferWidth() / getGuiScale();
    }

    public static double toGuiUnits(double d0, float f) {
        return d0 * getWindowScaleFactor() / f;
    }

    public static int opaqueZero() {
        return 0;
    }

    private static float getLockedScale() {
        if (GLFW.glfwGetMouseButton(VoilClient.mc.getWindow().getHandle(), 0) != 1 || lockedScale < 0.0F) {
            ThemeProperty themeproperty = ClientSettings.guiScale;
            lockedScale = getScaleSetting(themeproperty);
        }

        return lockedScale;
    }

    public static float getWindowScaleFactor() {
        return VoilClient.mc.getWindow().getScaleFactor();
    }

    public static float getAutoScale(float f, float f1) {
        float f2 = isVanillaScale() ? getWindowScaleFactor() : 2.0F * getLockedScale();
        float f3 = VoilClient.mc.getWindow().getFramebufferWidth() / f;
        float f4 = VoilClient.mc.getWindow().getFramebufferHeight() / f1;
        return Math.min(f2, Math.min(f3, f4));
    }
}
