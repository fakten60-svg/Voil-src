package lol.lwes.voil.util.render;

public class UiAnimation {
    private double value;
    private double velocity;
    private double target;
    private long lastUpdateNanos;
    private final EasingType type;
    private AnimationState state;
    private double smoothing;
    private double stiffness;
    private double durationMs = 250.0;
    private double startValue;
    private long startTimeNanos;

    public UiAnimation(EasingType easingtype) {
        this.value = 0.0;
        this.velocity = 0.0;
        this.target = 0.0;
        this.type = easingtype;
        this.state = AnimationState.IDLE;
        this.lastUpdateNanos = System.nanoTime();
        if (0L >= 0L) {
            label20: {
                int i = easingtype.ordinal();
                if (i != 9) {
                    if (i != 10) {
                        break label20;
                    }

                    this.smoothing = 0.5;
                    this.stiffness = 0.15;
                    if (0L >= 0L) {
                        return;
                    }
                }

                this.smoothing = 0.82;
                this.stiffness = 0.08;
                if (0L >= 0L) {
                    return;
                }
            }

            this.smoothing = 0.7;
        }

        this.stiffness = 0.15;
    }

    public UiAnimation() {
        this(EasingType.FLUID_SMOOTH);
    }

    private void stepFluid(double d0) {
        double d1 = this.target - this.value;
        int i = this.type.ordinal();
        if (i != 7) {
            if (i == 8 || i == 9) {
                this.stepSpring(d0);
                return;
            }

            if (i != 10) {
                return;
            }
        }

        this.value = this.value + d1 * (1.0 - Math.pow(this.smoothing, d0 * 60.0));
    }

    public int getRounded() {
        return (int)Math.round(this.value);
    }

    public UiAnimation setDuration(double d0) {
        this.durationMs = Math.max(1.0, d0);
        return this;
    }

    public UiAnimation setTarget(double d0) {
        if (Math.abs(this.target - d0) > 0.001) {
            this.target = d0;
            if (this.isFluid()) {
                if (this.state != AnimationState.ANIMATING) {
                    this.lastUpdateNanos = System.nanoTime();
                }
            } else {
                this.startValue = this.value;
                this.startTimeNanos = System.nanoTime();
            }

            this.state = AnimationState.ANIMATING;
        }

        return this;
    }

    private boolean isFluid() {
        return this.type == EasingType.FLUID_SMOOTH
            || this.type == EasingType.FLUID_SPRING
            || this.type == EasingType.FLUID_DAMPED
            || this.type == EasingType.FLUID_RESPONSIVE;
    }

    public float getFloat() {
        return (float)this.value;
    }

    private void stepSpring(double d0) {
        double d1 = 60.0 * Math.sqrt(this.stiffness);
        double d2 = -60.0 * Math.log(this.smoothing) / (2.0 * d1);
        double d3 = this.value - this.target;
        double d4 = this.velocity;
        if (d2 < 1.0) {
            double d5 = d1 * Math.sqrt(1.0 - d2 * d2);
            double d6 = Math.exp(-d2 * d1 * d0);
            double d7 = Math.cos(d5 * d0);
            double d8 = Math.sin(d5 * d0);
            double d10 = (d4 + d2 * d1 * d3) / d5;
            double d11 = d6 * (d3 * d7 + d10 * d8);
            double d12 = d6 * ((d10 * d5 - d2 * d1 * d3) * d7 - (d3 * d5 + d2 * d1 * d10) * d8);
            this.value = this.target + d11;
            this.velocity = d12;
        } else {
            double d13 = Math.exp(-d1 * d0);
            double d14 = d4 + d1 * d3;
            double d15 = d13 * (d3 + d14 * d0);
            double d9 = d13 * (d4 - d1 * d14 * d0);
            this.value = this.target + d15;
            this.velocity = d9;
        }
    }

    public double getTarget() {
        return this.target;
    }

    public void update() {
        if (this.state == AnimationState.ANIMATING) {
            long i = System.nanoTime();
            if (this.isFluid()) {
                double d0 = (i - this.lastUpdateNanos) / 1.0E9;
                this.lastUpdateNanos = i;
                if (d0 <= 0.0) {
                    return;
                }

                if (d0 > 0.05) {
                    d0 = 0.05;
                }

                this.stepFluid(d0);
                if (Math.abs(this.value - this.target) < 0.001 && Math.abs(this.velocity) < 0.05) {
                    this.value = this.target;
                    this.velocity = 0.0;
                    this.state = AnimationState.FINISHED;
                }
            } else {
                double d1 = (i - this.startTimeNanos) / 1000000.0 / this.durationMs;
                if (d1 >= 1.0) {
                    this.value = this.target;
                    this.velocity = 0.0;
                    this.state = AnimationState.FINISHED;
                    return;
                }

                this.value = this.startValue + (this.target - this.startValue) * this.applyEasing(d1);
            }
        }
    }

    public UiAnimation setFluidParams(double d0, double d1) {
        this.smoothing = Math.max(1.0E-4, Math.min(0.9999, d0));
        this.stiffness = Math.max(0.01, Math.min(1.0, d1));
        return this;
    }

    public UiAnimation reset() {
        return this.snapTo(0.0);
    }

    private double applyEasing(double d0) {
        switch (this.type) {
            case LINEAR:
                return d0;
            case EASE_OUT_CUBIC:
                return 1.0 - Math.pow(1.0 - d0, 3.0);
            case EASE_IN_OUT_CUBIC:
                return d0 < 0.5 ? 4.0 * d0 * d0 * d0 : 1.0 - Math.pow(-2.0 * d0 + 2.0, 3.0) / 2.0;
            case EASE_OUT_EXPO:
                return d0 == 1.0 ? 1.0 : 1.0 - Math.pow(2.0, -10.0 * d0);
            case EASE_OUT_BACK:
                double d2 = 1.70158 + 1.0;
                return 1.0 + d2 * Math.pow(d0 - 1.0, 3.0) + 1.70158 * Math.pow(d0 - 1.0, 2.0);
            case EASE_OUT_ELASTIC:
                return d0 == 0.0 ? 0.0 : (d0 == 1.0 ? 1.0 : Math.pow(2.0, -10.0 * d0) * Math.sin((d0 * 10.0 - 0.75) * (Math.PI * 2.0 / 3.0)) + 1.0);
            case EASE_OUT_BOUNCE:
                if (d0 < 1.0 / 2.75) {
                    return 7.5625 * d0 * d0;
                } else if (d0 < 2.0 / 2.75) {
                    double d4;
                    return 7.5625 * (d4 = d0 - 1.5 / 2.75) * d4 + 0.75;
                } else {
                    if (d0 < 2.5 / 2.75) {
                        double d3;
                        return 7.5625 * (d3 = d0 - 2.25 / 2.75) * d3 + 0.9375;
                    }

                    double d1;
                    return 7.5625 * (d1 = d0 - 2.625 / 2.75) * d1 + 0.984375;
                }
            default:
                return d0;
        }
    }

    public boolean isAnimating() {
        return this.state == AnimationState.ANIMATING;
    }

    public double getValue() {
        return this.value;
    }

    public UiAnimation snapTo(double d0) {
        this.value = d0;
        this.target = d0;
        this.velocity = 0.0;
        this.state = AnimationState.IDLE;
        this.lastUpdateNanos = System.nanoTime();
        this.startValue = d0;
        return this;
    }
}
