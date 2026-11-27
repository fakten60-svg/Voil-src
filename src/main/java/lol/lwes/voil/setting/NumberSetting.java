package lol.lwes.voil.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

public class NumberSetting extends Setting {
    private double min;
    private double max;
    private final double increment;
    private double value;
    private Runnable onChange;
    private final int decimals;
    private static final String FORMAT_PREFIX = "%.";
    private static final int DEFAULT_DECIMALS = 15;

    public NumberSetting(String s, double d0, double d1, double d2, double d3, String s1) {
        super(s, s1);
        this.min = d0;
        this.max = d1;
        this.increment = d3;
        this.decimals = d3 > 0.0 ? Math.max(0, new BigDecimal(String.valueOf(d3)).stripTrailingZeros().scale()) : DEFAULT_DECIMALS;
        this.value = this.normalize(d2);
    }

    public void setMax(double d0) {
        this.max = d0;
        double d1 = this.value;
        this.setValue(d1);
    }

    public float getFloat() {
        return (float)this.value;
    }

    public void step(boolean flag) {
        if (flag) {
            this.setValue(this.getValue() + this.getIncrement());
        } else {
            this.setValue(this.getFloat() - this.getIncrement());
        }
    }

    public double getMax() {
        return this.max;
    }

    public int getDecimals() {
        return this.decimals;
    }

    public String format(double d0) {
        String s1 = FORMAT_PREFIX;
        int i = this.decimals;
        return String.format(Locale.ROOT, s1 + i + "f", d0);
    }

    public long getLong() {
        return (long)this.value;
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(this.value);
    }

    public double clamp(double d0, double d2, double d3) {
        double d1 = Math.max(d2, d0);
        return Math.min(d3, d1);
    }

    public void setValue(double d0) {
        double d1 = this.value;
        this.value = this.normalize(d0);
        if (this.value != d1 && this.onChange != null) {
            this.onChange.run();
        }
    }

    public double getValue() {
        return this.value;
    }

    public void fromJson(JsonElement jsonelement) {
        this.value = this.normalize(jsonelement.getAsDouble());
    }

    public double normalize(double d0) {
        if (this.increment > 0.0) {
            d0 = Math.round(d0 / this.increment) * this.increment;
        }

        double d1 = this.clamp(d0, this.min, this.max);
        return BigDecimal.valueOf(d1).setScale(this.decimals, RoundingMode.HALF_UP).doubleValue();
    }

    public double getMin() {
        return this.min;
    }

    public void setMin(double d0) {
        this.min = d0;
        double d1 = this.value;
        this.setValue(d1);
    }

    public NumberSetting setOnChange(Runnable runnable) {
        this.onChange = runnable;
        return this;
    }

    public int getInt() {
        return (int)this.value;
    }

    public double getIncrement() {
        return this.increment;
    }

    @Override
    public void fromJson2(JsonElement jsonelement) {
        this.fromJson(jsonelement);
    }
}
