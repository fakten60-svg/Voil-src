package lol.lwes.voil.util.render;

import java.util.LinkedHashMap;
import java.util.Map;

public class UiEasings {
    private static final Map<String, EasingFunction> EASINGS = new LinkedHashMap<>();

    static {
        EASINGS.put("Ease Out Expo", UiEasings::easeOutExpo);
        EASINGS.put("Ease In Out Quint", UiEasings::easeInOutQuint);
        EASINGS.put("Ease Out Circ", UiEasings::easeOutCirc);
        EASINGS.put("Ease In Out Bezier", UiEasings::bezierEase);
        EASINGS.put("Ease Out Smooth", UiEasings::expDecay);
        EASINGS.put("Linear", UiEasings::linear);
        EASINGS.put("Overshoot", UiEasings::easeOutBack);
        EASINGS.put("Ease In Sine", UiEasings::easeInSine);
        EASINGS.put("Ease Out Sine", UiEasings::easeOutSine);
        EASINGS.put("Ease In Out Sine", UiEasings::easeInOutSine);
        EASINGS.put("Ease In Quad", UiEasings::easeInQuad);
        EASINGS.put("Ease Out Quad", UiEasings::easeOutQuad);
        EASINGS.put("Ease In Out Quad", UiEasings::easeInOutQuad);
        EASINGS.put("Ease In Cubic", UiEasings::easeInCubic);
        EASINGS.put("Ease Out Cubic", UiEasings::easeOutCubic);
        EASINGS.put("Ease In Out Cubic", UiEasings::easeInOutCubic);
    }

    public static float expDecay(float f) {
        return 1.0F - (float)Math.pow(Math.E, -6.5 * f);
    }

    public static float easeOutSine(float f) {
        return (float)Math.sin(f * Math.PI / 2.0);
    }

    public static float easeInOutQuint(float f) {
        return f < 0.5F ? 16.0F * f * f * f * f * f : 1.0F - (float)Math.pow(-2.0F * f + 2.0F, 5.0) / 2.0F;
    }

    public static EasingFunction getEasing(String s) {
        return EASINGS.getOrDefault(s, UiEasings::linear);
    }

    public static float easeOutCirc(float f) {
        return (float)Math.sqrt(1.0 - Math.pow(f - 1.0F, 2.0));
    }

    public static float easeInCubic(float f) {
        return f * f * f;
    }

    public static float easeInSine(float f) {
        return 1.0F - (float)Math.cos(f * Math.PI / 2.0);
    }

    public static float linear(float f) {
        return f;
    }

    public static float easeOutQuad(float f) {
        return 1.0F - (1.0F - f) * (1.0F - f);
    }

    public static float bezierEase(float f) {
        if (f <= 0.0F) {
            return 0.0F;
        }

        if (f >= 1.0F) {
            return 1.0F;
        }

        float f1 = f * f;
        float f2 = f1 * f;
        float f3 = 1.0F - f;
        float f4 = f3 * f3;
        return 3.0F * f4 * f * 0.46F + 3.0F * f3 * f1 * 0.45F + f2;
    }

    public static float easeOutExpo(float f) {
        return f == 1.0F ? 1.0F : 1.0F - (float)Math.pow(2.0, -10.0F * f);
    }

    public static Map<String, EasingFunction> getEasings() {
        return Map.copyOf(EASINGS);
    }

    public static float easeInOutQuad(float f) {
        return f < 0.5F ? 2.0F * f * f : 1.0F - (float)Math.pow(-2.0F * f + 2.0F, 2.0) / 2.0F;
    }

    public static float easeOutCubic(float f) {
        return 1.0F - (float)Math.pow(1.0F - f, 3.0);
    }

    public static float easeInOutCubic(float f) {
        return f < 0.5F ? 4.0F * f * f * f : 1.0F - (float)Math.pow(-2.0F * f + 2.0F, 3.0) / 2.0F;
    }

    public static float easeInQuad(float f) {
        return f * f;
    }

    public static float easeOutBack(float f) {
        return (float)Math.max(0.0, 1.0 + 3.0 * Math.pow(f - 1.0F, 3.0) + 2.0 * Math.pow(f - 1.0F, 2.0));
    }

    public static float easeInOutSine(float f) {
        return -((float)(Math.cos(Math.PI * f) - 1.0)) / 2.0F;
    }
}
