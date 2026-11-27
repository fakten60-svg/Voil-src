package lol.lwes.voil.setting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.minecraft.util.math.MathHelper;

public class CurveSetting extends Setting {
    private final List<CurvePoint> points;
    private final String label;
    private static final String SANITIZE_REGEX = "[&@#$%!\\^\\*\\(\\)\\-_+=]";

    public CurveSetting(String s, String s1, CurvePoint[] acurvepoint) {
        super(s, s1);
        this.points = new ArrayList<>();
        this.label = s;
        if (acurvepoint != null && acurvepoint.length > 0) {
            this.points.addAll(Arrays.asList(acurvepoint));
        } else {
            this.points.add(new CurvePoint(0.0F, 0.0F));
            this.points.add(new CurvePoint(0.5F, 1.0F));
            this.points.add(new CurvePoint(1.0F, 0.0F));
        }
    }

    @Override
    public String getName() {
        return this.label.replaceAll(SANITIZE_REGEX, "");
    }

    private float lerp(float f, float f1, float f2) {
        return f + (f1 - f) * f2;
    }

    @Override
    public boolean isPersistent() {
        return false;
    }

    public List<CurvePoint> getPoints() {
        return Collections.unmodifiableList(this.points);
    }

    public float evaluate(float f) {
        float f2 = MathHelper.clamp(f, 0.0F, 1.0F);
        if (this.points.size() < 2) {
            return 0.0F;
        }

        for (int i = 0; i < this.points.size() - 1; i++) {
            CurvePoint curvepoint = this.points.get(i);
            CurvePoint curvepoint1 = this.points.get(i + 1);
            if (f2 >= curvepoint.x && f2 <= curvepoint1.x) {
                float f1 = (f2 - curvepoint.x) / (curvepoint1.x - curvepoint.x);
                return this.lerp(curvepoint.y, curvepoint1.y, f1);
            }
        }

        return this.points.get(this.points.size() - 1).y;
    }
}
