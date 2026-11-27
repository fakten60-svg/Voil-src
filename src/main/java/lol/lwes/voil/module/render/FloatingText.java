package lol.lwes.voil.module.render;

import lol.lwes.voil.setting.NumberSetting;
import net.minecraft.util.math.Vec3d;

public class FloatingText {
    public Vec3d pos;
    long spawnTime;
    public String text;
    public float alpha;

    public FloatingText(Vec3d vec3d, String s) {
        this.pos = vec3d;
        this.spawnTime = System.currentTimeMillis();
        this.text = s;
        this.alpha = 1.0F;
    }

    public void updateAlpha(NumberSetting numbersetting) {
        long i = System.currentTimeMillis() - this.spawnTime;
        int k = numbersetting.getInt();
        long j = k * 900;
        if (i > j) {
            float f1 = (float)(i - j);
            k = numbersetting.getInt();
            float f = f1 / (k * 100);
            this.alpha = Math.max(0.0F, 1.0F - f);
        }
    }

    public boolean isExpired(NumberSetting numbersetting) {
        int j = numbersetting.getInt();
        long i = j * 1000;
        return System.currentTimeMillis() - this.spawnTime > i;
    }
}
