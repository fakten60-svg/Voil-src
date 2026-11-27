package lol.lwes.voil.module.render;

import java.awt.Color;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.config.GuiTheme;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.ColorSetting;
import lol.lwes.voil.setting.NumberSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.render.Render2DUtil;
import net.minecraft.util.math.MathHelper;

public class SpeedHUD extends Module {
    private final NumberSetting thickness;
    private final NumberSetting smoothness;
    private final NumberSetting radius;
    private final ColorSetting lineStart;
    private final ColorSetting lineEnd;
    private final ColorSetting bgTint;
    private final BooleanSetting showValue;
    private int historySize;
    private final float[] speedHistory;
    private int historyIndex;
    private boolean historyFilled;
    private float scale;
    private double prevX;
    private double prevZ;
    private boolean initialized;
    private float lastSpeed;

    public SpeedHUD() {
        super("SpeedHUD", "Displays your current speed on the screen", Category.Render);
        this.thickness = new NumberSetting("Thickness", 1.0, 6.0, 2.5, 0.1, "Line thickness");
        this.smoothness = new NumberSetting("Smoothness", 0.2, 3.0, 1.2, 0.1, "Anti-aliasing");
        this.radius = new NumberSetting("Radius", 0.0, 16.0, 8.0, 0.5, "Corner radius");
        Color color3 = new Color(8454016);
        String s2 = "Sets the start color of the speed graph gradient";
        Color color = color3;
        this.lineStart = new ColorSetting("Line Start", color, s2);
        color3 = new Color(16728128);
        String s3 = "Sets the end color of the speed graph gradient";
        Color color1 = color3;
        this.lineEnd = new ColorSetting("Line End", color1, s3);
        color3 = new Color(0, 0, 0, 80);
        String s4 = "Sets the tint color of the background blur";
        Color color2 = color3;
        this.bgTint = new ColorSetting("BG Tint", color2, s4);
        this.showValue = new BooleanSetting("Show Value", true, "Shows the current speed as text");
        this.historySize = 64;
        this.speedHistory = new float[this.historySize];
        this.historyIndex = 0;
        this.historyFilled = false;
        this.scale = 1.0F;
        this.initialized = false;
        this.lastSpeed = 0.0F;
        Setting[] asetting = new Setting[]{this.thickness, this.smoothness, this.radius, this.lineStart, this.lineEnd, this.bgTint, this.showValue};
        this.addSettings(asetting);
    }

    @Override
    public void onTick() {
        if (!Module.isNotInGame()) {
            double d0 = mc.player.getX();
            double d1 = mc.player.getZ();
            if (this.initialized) {
                double d2 = d0 - this.prevX;
                double d3 = d1 - this.prevZ;
                this.lastSpeed = (float)Math.sqrt(d2 * d2 + d3 * d3) * 20.0F;
            }

            this.prevX = d0;
            this.prevZ = d1;
            this.initialized = true;
            this.speedHistory[this.historyIndex] = this.lastSpeed;
            int i = this.historyIndex;
            this.historyIndex = (i + 1) % this.historySize;
            if (this.historyIndex == 0) {
                this.historyFilled = true;
            }
        }
    }

    @Override
    public void onRender2D() {
        if (!Module.isNotInGame()) {
            int i = this.historyFilled ? this.historySize : this.historyIndex;
            if (i >= 2) {
                float f = 0.0F;

                for (int j = 0; j < i; j++) {
                    f = Math.max(f, this.speedHistory[j]);
                }

                this.scale = Math.max(MathHelper.lerp(0.02F, this.scale, f), 0.5F);
                if (f > this.scale) {
                    this.scale = f;
                }

                float[] afloat = new float[i];

                for (int k = 0; k < i; k++) {
                    int l1;
                    if (this.historyFilled) {
                        int i1 = this.historyIndex;
                        l1 = (i1 + k) % this.historySize;
                    } else {
                        l1 = k;
                    }

                    int l = l1;
                    afloat[k] = MathHelper.clamp(this.speedHistory[l] / this.scale, 0.0F, 1.0F) * 20.0F;
                }

                int j1 = VoilClient.getHudLayout().getX(this);
                int k1 = VoilClient.getHudLayout().getY(this);
                float f1 = this.radius.getFloat();
                Color color = this.bgTint.getColor();
                float f4 = j1;
                float f5 = k1;
                float f3 = 100.0F + 8.0F * 2.0F;
                Render2DUtil.drawBlurRoundedTinted(f4, f5, f3, 40.0F + 8.0F * 2.0F, f1, 20.0F, 1.8F, color);
                Render2DUtil.drawBezierGradient(
                    afloat,
                    j1 + 8.0F,
                    k1 + 8.0F,
                    100.0F,
                    40.0F,
                    this.thickness.getFloat(),
                    this.smoothness.getFloat(),
                    this.lineStart.getColor(),
                    this.lineEnd.getColor()
                );
                if (this.showValue.isEnabled()) {
                    String s = String.format("%.1f BPS", this.lastSpeed);
                    float f2 = VoilClient.fonts.getSelectedFont().getWidth("Speed ");
                    VoilClient.fonts.getGreyscale().render("Speed ", j1 + 8.0F, k1 + 8.0F - 6.0F, GuiTheme.accentColor1);
                    VoilClient.fonts.getSelectedFont().render(s, j1 + 8.0F + f2, k1 + 8.0F - 6.0F, new Color(255, 255, 255, 180).getRGB());
                }

                VoilClient.getHudLayout().setSize(this, 116, 56);
            }
        }
    }

    @Override
    public void onEnable() {
        this.initialized = false;
        this.historyIndex = 0;
        this.historyFilled = false;
        this.scale = 1.0F;
    }
}
