package lol.lwes.voil.util;

import java.util.function.Supplier;
import lol.lwes.voil.setting.NumberSetting;
import lol.lwes.voil.setting.RangeSetting;

public class DelayTimer {
    private final Supplier delaySupplier;
    private boolean running;
    private int delay;
    private long startTime;

    public DelayTimer(NumberSetting numbersetting) {
        this(numbersetting::getInt);
    }

    public DelayTimer(RangeSetting rangesetting) {
        this(rangesetting::getRandomInt);
    }

    public DelayTimer(Supplier<Integer> supplier) {
        this.delaySupplier = supplier;
    }

    public boolean stop() {
        if (!this.running) {
            return false;
        }

        this.running = false;
        return true;
    }

    public boolean start() {
        if (this.running) {
            return false;
        }

        this.delay = (Integer)this.delaySupplier.get();
        this.startTime = System.currentTimeMillis();
        this.running = true;
        return true;
    }

    public double getProgress() {
        if (!this.running) {
            return 0.0;
        }

        double d0 = System.currentTimeMillis() - this.startTime;
        return Math.max(0.0, Math.min(1.0, d0 / this.delay));
    }

    public boolean hasFinished() {
        if (!this.running) {
            this.start();
        }

        if (System.currentTimeMillis() - this.delay > this.startTime) {
            this.running = false;
            return true;
        } else {
            return false;
        }
    }
}
