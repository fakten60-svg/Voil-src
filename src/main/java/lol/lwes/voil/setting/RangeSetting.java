package lol.lwes.voil.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

public class RangeSetting extends Setting {
    private double min;
    private double max;
    private final double increment;
    private double valueMin;
    private double valueMax;
    private final int decimals;
    private static final String FORMAT_PREFIX = "%.";
    private static final int DEFAULT_DECIMALS = 15;

    public RangeSetting(String s, double d0, double d1, double d2, double d3, double d4, String s1) {
        super(s, s1);
        this.min = d0;
        this.max = d1;
        this.increment = d4;
        this.decimals = d4 > 0.0 ? Math.max(0, new BigDecimal(String.valueOf(d4)).stripTrailingZeros().scale()) : DEFAULT_DECIMALS;
        this.valueMin = this.normalize(d2);
        this.valueMax = this.normalize(d3);
    }

    public void setValueMax(double d0) {
        double d1 = this.normalize(d0);
        if (0 == 0) {
            if (d1 < this.valueMin) {
                d1 = this.valueMin;
            }

            this.valueMax = d1;
        }
    }

    public void setMin(double d0) {
        this.min = d0;
        this.setValueMin(this.valueMin);
        double d1 = this.valueMax;
        this.setValueMax(d1);
    }

    public void setMax(double d0) {
        this.max = d0;
        this.setValueMin(this.valueMin);
        double d1 = this.valueMax;
        this.setValueMax(d1);
    }

    public double getMin() {
        return this.min;
    }

    public String format(double d0) {
        String s1 = FORMAT_PREFIX;
        int i = this.decimals;
        return String.format(Locale.ROOT, s1 + i + "f", d0);
    }

    public double getRandomValue() {
        return this.valueMin == this.valueMax ? this.valueMin : ThreadLocalRandom.current().nextDouble(this.valueMin, this.valueMax);
    }

    public double getValueMin() {
        return this.valueMin;
    }

    @Override
    public JsonElement toJson() {
        double d1 = this.valueMax;
        double d0 = this.valueMin;
        return new JsonPrimitive(d0 + ":" + d1);
    }

    private double clamp(double d0, double d2, double d3) {
        double d1 = Math.max(d2, d0);
        return Math.min(d3, d1);
    }

    public double getValueMax() {
        return this.valueMax;
    }

    public double normalize(double d0) {
        if (this.increment > 0.0) {
            d0 = Math.round(d0 / this.increment) * this.increment;
        }

        double d1 = BigDecimal.valueOf(d0).setScale(this.decimals, RoundingMode.HALF_UP).doubleValue();
        return this.clamp(d1, this.min, this.max);
    }

    public void fromJson(JsonElement jsonelement) {
        String[] astring = jsonelement.getAsString().split(":");
        if (astring.length == 2) {
            try {
                double d2 = Double.parseDouble(astring[0]);
                double d0 = this.normalize(d2);
                double d3 = Double.parseDouble(astring[1]);
                double d1 = this.normalize(d3);
                this.valueMin = Math.min(d0, d1);
                this.valueMax = Math.max(d0, d1);
            } catch (NumberFormatException numberformatexception) {
            }
        }
    }

    public void setValueMin(double d0) {
        double d1 = this.normalize(d0);
        if (0 == 0) {
            if (d1 > this.valueMax) {
                d1 = this.valueMax;
            }

            this.valueMin = d1;
        }
    }

    public int getDecimals() {
        return this.decimals;
    }

    public float getValueMaxFloat() {
        return (float)this.valueMax;
    }

    public int getRandomValueInt() {
        return (int)this.getRandomValue();
    }

    public float getRandomValueFloat() {
        return (float)this.getRandomValue();
    }

    public double getIncrement() {
        return this.increment;
    }

    public double getMax() {
        return this.max;
    }

    public int getRandomInt() {
        return ThreadLocalRandom.current().nextInt((int)this.valueMin, (int)(this.valueMax + 1.0));
    }

    public float getMinFloat() {
        return (float)this.min;
    }

    @Override
    public void fromJson2(JsonElement jsonelement) {
        this.fromJson(jsonelement);
    }
}
