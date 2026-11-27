package lol.lwes.voil.module.render;

import java.awt.Color;

public class HudParticle {
    public float x;
    public float y;
    float velocityX;
    float velocityY;
    float alpha;
    public int size;
    long spawnTime;
    final int radius;
    final Color color;
    private static final int DEFAULT_RADIUS = 1200;
    private static final long PARTICLE_LIFETIME_MS = 1200L;

    public HudParticle(float f, float f1, Color color) {
        this.radius = DEFAULT_RADIUS;
        this.x = f;
        this.y = f1;
        int i = (int)(Math.random() * 4.0);
        this.size = 1 + i;
        double d0 = Math.random() * 2.0 * Math.PI;
        float f2 = TargetHUD.particleSpeed.getFloat() + (float)(Math.random() * 0.1F);
        this.velocityX = (float)Math.cos(d0) * f2;
        this.velocityY = (float)Math.sin(d0) * f2;
        this.alpha = 255.0F;
        this.spawnTime = System.currentTimeMillis();
        this.color = color;
    }

    public void update() {
        float f = (float)(System.currentTimeMillis() - this.spawnTime) / 1200.0F;
        this.x = this.x + this.velocityX;
        this.y = this.y + (this.velocityY + 0.1F);
        this.alpha = 255.0F * (1.0F - f);
    }

    public Color getColor() {
        return new Color(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), Math.max(0, (int)this.alpha));
    }

    public boolean isDead() {
        return System.currentTimeMillis() - this.spawnTime < PARTICLE_LIFETIME_MS;
    }
}
