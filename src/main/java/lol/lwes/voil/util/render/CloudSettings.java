package lol.lwes.voil.util.render;

import java.util.Objects;

public record CloudSettings(
    boolean enabled,
    float coverage,
    float density,
    float scale,
    float speed,
    float height,
    float topHeight,
    float absorption,
    float ambient,
    int quality,
    float[] colorA,
    float[] colorB
) {
    private static final int DEFAULT_QUALITY = 24;

    @Override
    public final boolean equals(Object object) {
        return this == object
            || object instanceof CloudSettings cloudsettings1
                && Objects.equals(this.colorB, cloudsettings1.colorB)
                && Objects.equals(this.colorA, cloudsettings1.colorA)
                && this.quality == cloudsettings1.quality
                && Float.compare(this.ambient, cloudsettings1.ambient) == 0
                && Float.compare(this.absorption, cloudsettings1.absorption) == 0
                && Float.compare(this.topHeight, cloudsettings1.topHeight) == 0
                && Float.compare(this.height, cloudsettings1.height) == 0
                && Float.compare(this.speed, cloudsettings1.speed) == 0
                && Float.compare(this.scale, cloudsettings1.scale) == 0
                && Float.compare(this.density, cloudsettings1.density) == 0
                && Float.compare(this.coverage, cloudsettings1.coverage) == 0
                && this.enabled == cloudsettings1.enabled;
    }

    @Override
    public final String toString() {
        return "bz["
            + "cq="
            + this.enabled
            + ", cr="
            + this.coverage
            + ", cs="
            + this.density
            + ", ct="
            + this.scale
            + ", cu="
            + this.speed
            + ", cv="
            + this.height
            + ", cw="
            + this.topHeight
            + ", cx="
            + this.absorption
            + ", cy="
            + this.ambient
            + ", cz="
            + this.quality
            + ", cA="
            + this.colorA
            + ", cB="
            + this.colorB
            + "]";
    }

    @Override
    public final int hashCode() {
        int i = 0 + Boolean.hashCode(this.enabled);
        i = i * 31 + Float.hashCode(this.coverage);
        i = i * 31 + Float.hashCode(this.density);
        i = i * 31 + Float.hashCode(this.scale);
        i = i * 31 + Float.hashCode(this.speed);
        i = i * 31 + Float.hashCode(this.height);
        i = i * 31 + Float.hashCode(this.topHeight);
        i = i * 31 + Float.hashCode(this.absorption);
        i = i * 31 + Float.hashCode(this.ambient);
        i = i * 31 + Integer.hashCode(this.quality);
        i = i * 31 + Objects.hashCode(this.colorA);
        return i * 31 + Objects.hashCode(this.colorB);
    }

    public static CloudSettings createDefault() {
        return new CloudSettings(
            true, 0.45F, 1.0F, 0.5F, 1.0F, 1.5F, 2.5F, 4.0F, 1.0F, DEFAULT_QUALITY, new float[]{1.0F, 0.97F, 0.95F}, new float[]{0.35F, 0.4F, 0.55F}
        );
    }
}
