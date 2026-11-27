package lol.lwes.voil.util.render;

import dev.prestigeclient.renderlib.api.uniform.RLUniforms;
import java.awt.Color;
import java.util.List;
import lol.lwes.voil.setting.GradientSetting;
import lol.lwes.voil.setting.GradientStop;

public final class GradientUniformBinder {
    public static final int MAX_STOPS = 8;
    private final String prefix;
    private final String countName;
    private final String typeName;
    private final String angleName;
    private final String[] colorNames = new String[MAX_STOPS];
    private final String[] positionNames = new String[MAX_STOPS];

    public GradientUniformBinder(String prefix) {
        this.prefix = prefix;
        this.countName = prefix + "StopCount";
        this.typeName = prefix + "GradientType";
        this.angleName = prefix + "GradientAngle";

        for (int i = 0; i < MAX_STOPS; i++) {
            this.colorNames[i] = prefix + "Colors[" + i + "]";
            this.positionNames[i] = prefix + "Stops[" + i + "]";
        }
    }

    public String getPrefix() {
        return this.prefix;
    }

    public void apply(RLUniforms uniforms, GradientSetting gradient) {
        if (uniforms == null || gradient == null) {
            return;
        }

        List<?> stops = gradient.getStops();
        int count = Math.min(stops.size(), MAX_STOPS);
        uniforms.set1i(this.countName, count);
        if (count == 0) {
            return;
        }

        uniforms.set1i(this.typeName, gradient.getMode() == GradientType.RADIAL ? 1 : 0);
        uniforms.set1f(this.angleName, (float) Math.toRadians(gradient.getAngle()));

        for (int i = 0; i < MAX_STOPS; i++) {
            GradientStop stop = (GradientStop) stops.get(Math.min(i, count - 1));
            Color color = stop.getColor();
            uniforms.set4f(this.colorNames[i], color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, color.getAlpha() / 255.0F);
            uniforms.set1f(this.positionNames[i], i < count ? stop.getPosition() / 100.0F : 1.0F);
        }
    }
}
