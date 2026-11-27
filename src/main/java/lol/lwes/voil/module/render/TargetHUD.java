package lol.lwes.voil.module.render;

import dev.prestigeclient.renderlib.api.font.MSDFFontAtlas;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.config.GuiTheme;
import lol.lwes.voil.event.AttackEntityPostEvent;
import lol.lwes.voil.event.EventHandler;
import lol.lwes.voil.event.PacketReceiveEvent;
import lol.lwes.voil.gui.hudeditor.HudEditorScreen;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.module.render.HudParticle;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.ModeSetting;
import lol.lwes.voil.setting.NumberSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.SystemUtil;
import lol.lwes.voil.util.render.ColorUtil;
import lol.lwes.voil.util.render.KawaseGlowRenderer;
import lol.lwes.voil.util.render.Render2DUtil;
import lol.lwes.voil.util.render.UiEasings;
import lol.lwes.voil.util.world.EntityUtil;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.util.math.Vec3d;

public class TargetHUD extends Module {
    ModeSetting mode = new ModeSetting(
        "Modes",
        "Voil default",
        "Mode for the hud design",
        new String[]{"Voil small", "Voil default", "Voil two", "Rise", "Skeet", "Opal", "Neon", "Gradient", "Achilles"}
    );
    BooleanSetting disappearAfterTime = new BooleanSetting("Disappear After Time", true, "Disappears after a certain time");
    private static BooleanSetting particle = new BooleanSetting("Particle", false, "Adds a particle effect");
    NumberSetting disappearTime = new NumberSetting("Disappear Time", 0.1, 10.0, 5.0, 1.0, "Time to disappear after seconds");
    private static final ModeSetting particleMode = new ModeSetting("Particle Mode", "Hit", "Condition for particle to spawn", new String[]{"Hit", "Totem Pop"});
    public static final NumberSetting particleSpeed = new NumberSetting("Particle Speed", 0.1, 1.0, 0.5, 0.1, "Speed of the particles");
    private static final ModeSetting render = new ModeSetting("Render", "Screen", "Where to render the hud", new String[]{"World", "Screen"});
    private static final BooleanSetting enableAnimation = new BooleanSetting("Enable Animation", true, "Enable smooth fade in/out");
    private static final NumberSetting animationSpeed = new NumberSetting("Animation Speed", 50.0, 1000.0, 300.0, 50.0, "Fade duration in milliseconds");
    private static final ModeSetting animationEasing = new ModeSetting(
        "Animation Easing", "Smooth", "Type of fade easing", new String[]{"Linear", "Bezier", "Expo", "Smooth", "Quintic", "Circle"}
    );
    private static final BooleanSetting healthAnimation = new BooleanSetting("Health Animation", true, "Enable smooth health bar animation");
    private static final NumberSetting healthAnimationSpeed = new NumberSetting(
        "Health Animation Speed", 100.0, 2000.0, 500.0, 50.0, "Health bar animation duration in milliseconds"
    );
    private static final ModeSetting healthAnimationEasing = new ModeSetting(
        "Health Animation Easing", "Smooth", "Type of health animation easing", new String[]{"Linear", "Smooth", "Bezier", "Expo", "Bounce"}
    );
    private BooleanSetting glow = new BooleanSetting("Glow", false, "Adds a glow effect to the hud");
    private static AbstractClientPlayerEntity target;
    private AbstractClientPlayerEntity lastTarget;
    private float lastHealth = -1.0F;
    private float displayHealth = -1.0F;
    private long lastTargetTime = 0L;
    private final List<HudParticle> particles = new ArrayList<>();
    private boolean fadeActive = false;
    private long fadeStartTime = 0L;
    private float fadeProgress = 0.0F;
    private boolean fadeOut = false;
    private boolean hasTarget = false;
    private long healthAnimStart = 0L;
    private float healthFrom = 0.0F;
    private float healthTo = 0.0F;
    private float barOffset = 0.5F;
    private boolean particleSpawned = false;
    private static final long MILLIS_PER_SECOND = 1000L;

    public TargetHUD() {
        super("TargetHUD", "Shows info on players", Category.Render);
        Setting[] asetting = new Setting[]{
            this.mode,
            this.glow,
            this.disappearAfterTime,
            render,
            particle,
            this.disappearTime,
            particleMode,
            particleSpeed,
            enableAnimation,
            animationSpeed,
            animationEasing,
            healthAnimation,
            healthAnimationSpeed,
            healthAnimationEasing
        };
        this.addSettings(asetting);
        Setting.dependSetting(this.disappearTime, this.disappearAfterTime);
        Setting.dependSetting(particleSpeed, particle);
        Setting.dependSetting(particleMode, particle);
        Setting.dependSetting(animationSpeed, enableAnimation);
        Setting.dependSetting(animationEasing, enableAnimation);
        Setting.dependSetting(healthAnimationSpeed, healthAnimation);
        Setting.dependSetting(healthAnimationEasing, healthAnimation);
        ModeSetting modesetting = this.mode;
        Setting.dependSetting(this.glow, "Achilles", modesetting);
    }

    private void setTargetHealth(float f, float f1) {
        if (!healthAnimation.isEnabled()) {
            this.displayHealth = f1;
        } else {
            this.hasTarget = true;
            this.healthAnimStart = System.currentTimeMillis();
            this.healthFrom = f;
            this.healthTo = f1;
        }
    }

    private float bounceEase(float f) {
        if (f < 0.36363637F) {
            return 7.5625F * f * f;
        } else if (f < 0.72727275F) {
            float f3;
            return 7.5625F * (f3 = f - 0.54545456F) * f3 + 0.75F;
        } else {
            float f1;
            float f2;
            return f < 0.90909094F ? 7.5625F * (f1 = f - 0.8181818F) * f1 + 0.9375F : 7.5625F * (f2 = f - 0.95454544F) * f2 + 0.984375F;
        }
    }

    private float getFadeAlpha() {
        return enableAnimation.isEnabled() ? this.fadeProgress : 1.0F;
    }

    private void drawStyle1(int i, int j) {
        Color color = new Color(44, 44, 44, 200);
        Render2DUtil.drawBlurRounded(i, j, 180.0F, 55.0F, 20.0F, 32.0F, this.getFadeAlpha());
        Render2DUtil.drawPlayerHead(target, i + 5, j + 5, 40, 20, this.applyAlpha(Color.WHITE));
        int i1 = i + 50;
        int l = j + 40;
        this.drawHealthBar(
            i1,
            l,
            120,
            5,
            this.displayHealth,
            target.getMaxHealth(),
            this.applyAlpha(color),
            this.applyAlpha(GuiTheme.accentColor1),
            this.applyAlpha(GuiTheme.accentColor2),
            false
        );
        MSDFFontAtlas msdffontatlas = VoilClient.fonts.getSelectedFont();
        int k = (int)this.displayHealth;
        msdffontatlas.render(k + "hp", i + 50, j + 20, this.applyAlpha(new Color(104, 105, 107).getRGB()));
        VoilClient.fonts.getSelectedFont().render(target.getName().getString(), i + 50, j + 4, 10.0F, this.applyAlpha(-1));
        VoilClient.getHudLayout().setSize(this, 180, 55);
    }

    private float approachHealth(float f, float f1) {
        if (f < f1) {
            f += this.barOffset;
            if (f > f1) {
                f = f1;
            }
        } else if (f > f1) {
            f -= this.barOffset;
            if (f < f1) {
                f = f1;
            }
        }

        return f;
    }

    private void drawHealthBar(int i, int j, int k, int l, float f, float f1, Color color, Color color1, Color color2, boolean flag) {
        int i1 = l / 2;
        if (f <= 0.0F) {
            f = 0.0F;
        }

        if (f1 <= 0.0F) {
            f1 = 1.0F;
        }

        int j1 = Math.max(1, (int)(f * k / f1));
        float f5 = i;
        float f3 = j;
        Render2DUtil.drawRoundedRect(f5, f3, k, l, i1, 1.0F, color);
        if (flag) {
            float f2 = (float)((Math.sin(System.currentTimeMillis() / 500.0) + 1.0) / 2.0);
            int k1 = (int)(100.0F * this.getFadeAlpha());
            Color color7 = color1;
            Color color3 = ColorUtil.withAlpha(color7, k1);
            int l1 = (int)(100.0F * this.getFadeAlpha());
            Color color8 = color2;
            Color color4 = ColorUtil.withAlpha(color8, l1);
            Color color5 = ColorUtil.lerpColor(color3, color4, f2);
            Color color6 = ColorUtil.lerpColor(color4, color3, f2);
            Render2DUtil.drawRectGradient(i, j + 1.5F, j1, 1.0F, i1, 10.0F, color5, color5, color6, color6);
        }

        f5 = i;
        float f6 = j;
        float f7 = j1;
        float f4 = l;
        Render2DUtil.drawRectGradient(f5, f6, f7, f4, i1, 1.0F, color1, color1, color2, color2);
    }

    @Override
    public void onRender2D() {
        if (mc.player != null && mc.world != null) {
            if ((this.isChatOpen() || this.fadeActive && this.fadeOut) && target != null) {
                this.updateFade();
                this.updateHealthAnimation();
                if (this.disappearAfterTime.isEnabled() && !this.fadeOut) {
                    long i = (System.currentTimeMillis() - this.lastTargetTime) / MILLIS_PER_SECOND;
                    if (i >= this.disappearTime.getValue()) {
                        this.fadeOut = true;
                        this.fadeActive = true;
                        this.fadeStartTime = System.currentTimeMillis();
                    }
                }

                int j;
                int l;
                if (render.is("World")) {
                    Vec3d vec3d = target.getLerpedPos(SystemUtil.getTickDelta());
                    Vec3d vec3d1 = EntityUtil.worldToScreen(vec3d);
                    if (vec3d1.z < 0.0 || vec3d1.z >= 1.0) {
                        return;
                    }

                    int k = (int)vec3d1.x;
                    l = k + -201 + 1;
                    k = (int)vec3d1.y;
                    j = k + -91 + 1;
                } else {
                    l = VoilClient.getHudLayout().getX(this);
                    j = VoilClient.getHudLayout().getY(this);
                }

                float f1 = this.displayHealth;
                float f = target.getHealth();
                this.displayHealth = this.approachHealth(f1, f);
                switch (this.mode.getMode()) {
                    case "Voil default":
                        this.drawStyle7(l, j);
                        break;
                    case "Voil small":
                        this.drawStyle6(l, j);
                        break;
                    case "Voil two":
                        this.drawStyle2(l, j);
                        break;
                    case "Rise":
                        this.drawStyle1(l, j);
                        break;
                    case "Skeet":
                        this.drawStyle9(l, j);
                        break;
                    case "Opal":
                        this.drawStyle5(l, j);
                        break;
                    case "Neon":
                        this.drawStyle8(l, j);
                        break;
                    case "Gradient":
                        this.drawStyle3(l, j);
                        break;
                    case "Achilles":
                        this.drawStyle4(l, j);
                }

                if (particle.isEnabled()) {
                    this.drawParticles(l, j);
                }

                if (this.fadeOut && !this.fadeActive) {
                    target = null;
                    this.fadeOut = false;
                }
            }
        }
    }

    private void drawStyle2(int i, int j) {
        Color color = new Color(44, 44, 44);
        Render2DUtil.drawBlurRounded(i, j, 160.0F, 55.0F, 6.0F, 24.0F, this.getFadeAlpha());
        Render2DUtil.drawPlayerHead(target, i + 5, j + 5, 40, 0, this.applyAlpha(Color.WHITE));
        int i1 = i + 50;
        int l = j + 40;
        this.drawHealthBar(
            i1,
            l,
            100,
            5,
            this.displayHealth,
            target.getMaxHealth(),
            this.applyAlpha(color),
            this.applyAlpha(GuiTheme.accentColor1),
            this.applyAlpha(GuiTheme.accentColor2),
            false
        );
        MSDFFontAtlas msdffontatlas = VoilClient.fonts.getSelectedFont();
        int k = (int)this.displayHealth;
        msdffontatlas.render(k + "hp", i + 50, j + 20, this.applyAlpha(new Color(104, 105, 107).getRGB()));
        VoilClient.fonts.getSelectedFont().render(target.getName().getString(), i + 50, j + 4, 10.0F, this.applyAlpha(-1));
        VoilClient.getHudLayout().setSize(this, 160, 55);
    }

    private void startFade() {
        this.fadeActive = true;
        this.fadeOut = false;
        this.fadeStartTime = System.currentTimeMillis();
        this.fadeProgress = 0.0F;
    }

    private boolean isChatOpen() {
        if (mc.currentScreen instanceof HudEditorScreen) {
            target = mc.player;
            return true;
        } else if (target == mc.player) {
            target = null;
            return false;
        } else if (target != null && target.isDead()) {
            target = null;
            return false;
        } else {
            return target != null && target.isPartOfGame() && target.getHealth() > 0.0F;
        }
    }

    private void drawParticles(int i, int j) {
        if (particleMode.is("Hit") && target.hurtTime == target.maxHurtTime - 1) {
            float f3 = i + 20;
            float f = j + 30;
            this.particles.add(new HudParticle(f3, f, GuiTheme.accentColor1));
        }

        if (particleMode.is("Totem Pop") && this.particleSpawned) {
            for (int k = 0; k < 5; k++) {
                float f2 = i + 20;
                float f1 = j + 30;
                this.particles.add(new HudParticle(f2, f1, GuiTheme.accentColor1));
            }

            this.particleSpawned = false;
        }

        this.particles.removeIf(hudparticle1 -> !hudparticle1.isDead());

        for (HudParticle hudparticle : this.particles) {
            hudparticle.update();
            Render2DUtil.drawCircle((int)hudparticle.x, (int)hudparticle.y, hudparticle.size, 1.0F, this.applyAlpha(hudparticle.getColor()));
        }
    }

    private int applyAlpha(int i) {
        float f = this.getFadeAlpha();
        if (f >= 0.999F) {
            return i;
        }

        int j = i >>> 24;
        int l = Math.max(0, Math.min(255, (int)((j & 0xFF) * f))) << 24;
        int k = i & 0xFFFFFF;
        j = l;
        return j | k;
    }

    private void drawStyle3(int i, int j) {
        Color color2 = new Color(224, 161, 119);
        Color color = ColorUtil.withAlpha(color2, 128);
        Color color3 = GuiTheme.accentColor2;
        Color color1 = ColorUtil.withAlpha(color3, 128);
        float f = target.getMaxHealth();
        Render2DUtil.drawBlurRounded(i, j, 150.0F, 50.0F, 6.0F, 14.0F, this.getFadeAlpha());
        Render2DUtil.drawRoundedRectGradientHorizontal(i, j, 150.0F, 50.0F, 6.0F, 8.0F, this.applyAlpha(color), this.applyAlpha(color1), 10);
        float f2 = i;
        float f1 = j;
        Render2DUtil.drawOutline(f2, f1, 150.0F, 50.0F, 6.0F, 1.0F, 0.3F, this.applyAlpha(Color.white));
        int i2 = i + 5;
        int l1 = j + 40;
        float f3 = this.displayHealth;
        Color color4 = new Color(44, 44, 44);
        this.drawHealthBar(
            i2,
            l1,
            140,
            5,
            f3,
            f,
            this.applyAlpha(ColorUtil.withAlpha(color4, 128)),
            this.applyAlpha(new Color(224, 161, 119)),
            this.applyAlpha(GuiTheme.accentColor2),
            false
        );
        Render2DUtil.drawPlayerHead(target, i + 5, j + 5, 28, 6, this.applyAlpha(Color.WHITE));
        VoilClient.fonts.getSelectedFont().render(target.getName().getString(), i + 40, j + 3, this.applyAlpha(Color.white));
        int k = i + 38;
        int l = j + 18;
        int i1 = 0;
        ItemStack itemstack = target.getMainHandStack();
        if (!itemstack.isEmpty()) {
            Render2DUtil.drawItem(itemstack, k | 0, l, 1.0F, false);
            i1++;
        }

        for (EquipmentSlot equipmentslot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack itemstack1 = target.getEquippedStack(equipmentslot);
            if (!itemstack1.isEmpty()) {
                int k1 = i1 * 18;
                int j1 = k;
                Render2DUtil.drawItem(itemstack1, j1 + k1, l, 1.0F, false);
                i1++;
            }
        }

        VoilClient.fonts.getSelectedFont().render(String.valueOf((int)this.displayHealth), i + 133, j + 25, this.applyAlpha(Color.white));
        VoilClient.getHudLayout().setSize(this, 150, 50);
    }

    private void drawStyle4(int i, int j) {
        float f = this.displayHealth;
        float f1 = mc.player.getHealth();
        Color color;
        String s;
        if (f < f1) {
            s = "W";
            color = new Color(8454016);
        } else if (f > f1) {
            s = "L";
            color = new Color(16751001);
        } else {
            s = "?";
            color = new Color(190, 190, 190);
        }

        if (this.glow.isEnabled()) {
            KawaseGlowRenderer kawaseglowrenderer = Render2DUtil.kawaseGlowRenderer;
            float f10 = i;
            float f11 = j;
            Color color5 = this.applyAlpha(GuiTheme.accentColor1);
            Color color6 = this.applyAlpha(GuiTheme.accentColor2);
            Color color7 = this.applyAlpha(GuiTheme.accentColor1);
            Color color1 = this.applyAlpha(GuiTheme.accentColor2);
            Color color2 = color7;
            Color color3 = color6;
            Color color4 = color5;
            float f3 = 2.3F;
            float f4 = 28.0F;
            float f5 = 6.0F;
            float f6 = 50.0F;
            float f7 = 150.0F;
            float f8 = f11;
            float f9 = f10;
            kawaseglowrenderer.render(f9, f8, f7, f6, f5, f4, f3, color4, color3, color2, color1);
        }

        Render2DUtil.drawBlurRounded(i, j, 150.0F, 50.0F, 6.0F, 32.0F, this.getFadeAlpha());
        Render2DUtil.drawPlayerHead(target, i + 5, j + 5, 40, 3, this.applyAlpha(Color.WHITE));
        VoilClient.fonts.getGreyscale().render(target.getName().getString(), i + 50, j + 6, this.applyAlpha(Color.white));
        int i1 = i + 50;
        int k = j + 34;
        this.drawHealthBar(
            i1,
            k,
            90,
            5,
            this.displayHealth,
            target.getMaxHealth(),
            this.applyAlpha(new Color(44, 44, 44)),
            this.applyAlpha(GuiTheme.accentColor1),
            this.applyAlpha(GuiTheme.accentColor2),
            false
        );
        VoilClient.fonts.getGreyscale().render(s, i + 50, j + 18, this.applyAlpha(color));
        float f2 = VoilClient.fonts.getGreyscale().getWidth(s);
        MSDFFontAtlas msdffontatlas = VoilClient.fonts.getGreyscale();
        int l = (int)this.displayHealth;
        msdffontatlas.render("HP: " + l, i + 50 + f2 + 4.0F, j + 18, this.applyAlpha(Color.white));
        VoilClient.getHudLayout().setSize(this, 150, 50);
    }

    private void drawStyle5(int i, int j) {
        Render2DUtil.drawBlurRounded(i, j, 150.0F, 50.0F, 6.0F, 32.0F, this.getFadeAlpha());
        int k = i + 5;
        int l = j + 5;
        Render2DUtil.drawPlayerHead(target, k, l, 32, 8, this.applyAlpha(Color.WHITE));
        int l2 = i + 5;
        int k2 = j + 40;
        this.drawHealthBar(
            l2,
            k2,
            125,
            6,
            this.displayHealth,
            target.getMaxHealth(),
            this.applyAlpha(new Color(44, 44, 44)),
            this.applyAlpha(GuiTheme.accentColor1),
            this.applyAlpha(GuiTheme.accentColor2),
            false
        );
        VoilClient.fonts.getPoppins().render(target.getName().getString(), i + 40, j + 2, this.applyAlpha(-1));
        VoilClient.fonts.getInterBold().render(String.valueOf((int)this.displayHealth), i + 134, j + 36, 10.0F, this.applyAlpha(-1));
        int i1 = i + 40;
        int j1 = j + 16;
        ItemStack itemstack = target.getMainHandStack();
        Render2DUtil.drawBlurRounded(i1, j1, 18, 18, 6.0F, 5.0F, this.getFadeAlpha());
        if (!itemstack.isEmpty()) {
            Render2DUtil.drawItem(itemstack, i1 + 1, j1 + 1, 1.0F, false);
        }

        EquipmentSlot[] aequipmentslot = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

        for (int k1 = 0; k1 < aequipmentslot.length; k1++) {
            int i2 = k1 + 1;
            int j2 = i2 * 22;
            i2 = i1;
            int l1 = i2 + j2;
            Render2DUtil.drawBlurRounded(l1, j1, 18, 18, 6.0F, 5.0F, this.getFadeAlpha());
            ItemStack itemstack1 = target.getEquippedStack(aequipmentslot[k1]);
            if (!itemstack1.isEmpty()) {
                Render2DUtil.drawItem(itemstack1, l1 + 1, j1 + 1, 1.0F, false);
            }
        }

        VoilClient.getHudLayout().setSize(this, 150, 50);
    }

    private void drawBar(int i, int j, int k, int l, float f, float f1, Color color, Color color1, boolean flag) {
        int i1 = l / 2;
        if (f <= 0.0F) {
            f = 0.0F;
        }

        if (f1 <= 0.0F) {
            f1 = 1.0F;
        }

        int j1 = Math.max(1, (int)(f * k / f1));
        float f5 = i;
        float f3 = j;
        Render2DUtil.drawRoundedRect(f5, f3, k, l, i1, 1.0F, color);
        if (flag) {
            float f2 = (float)((Math.sin(System.currentTimeMillis() / 500.0) + 1.0) / 2.0);
            int k1 = (int)(60.0F * this.getFadeAlpha());
            Color color5 = color1;
            Color color2 = ColorUtil.withAlpha(color5, k1);
            int l1 = (int)(120.0F * this.getFadeAlpha());
            Color color6 = color1;
            Color color3 = ColorUtil.withAlpha(color6, l1);
            Color color4 = ColorUtil.lerpColor(color2, color3, f2);
            Render2DUtil.drawRectGradient(i, j + 1.5F, j1, 1.0F, i1, 10.0F, color4, color4, color4, color4);
        }

        f5 = i;
        float f4 = j;
        Render2DUtil.drawRoundedRect(f5, f4, j1, l, i1, 1.0F, color1);
    }

    private void updateHealthAnimation() {
        if (target != null) {
            float f = target.getHealth();
            if (Math.abs(f - this.lastHealth) > 0.1F) {
                float f3 = this.displayHealth;
                this.setTargetHealth(f3, f);
                this.lastHealth = f;
            }

            if (this.hasTarget && healthAnimation.isEnabled()) {
                float f1 = (float)(System.currentTimeMillis() - this.healthAnimStart) / (float)healthAnimationSpeed.getValue();
                if (f1 >= 1.0F) {
                    this.displayHealth = this.healthTo;
                    this.hasTarget = false;
                } else {
                    float f2 = switch (healthAnimationEasing.getMode()) {
                        case "Linear" -> f1;
                        case "Smooth" -> UiEasings.expDecay(f1);
                        case "Bezier" -> UiEasings.bezierEase(f1);
                        case "Expo" -> UiEasings.easeOutExpo(f1);
                        case "Bounce" -> this.bounceEase(f1);
                        default -> f1;
                    };
                    this.displayHealth = this.healthFrom + (this.healthTo - this.healthFrom) * f2;
                }
            } else {
                this.displayHealth = f;
            }
        }
    }

    private void drawStyle6(int i, int j) {
        Color color = new Color(35, 36, 35);
        Color color1 = new Color(79, 79, 79);
        Color color2 = new Color(44, 44, 44);
        float f = this.displayHealth;
        float f1 = mc.player.getHealth();
        Color color3 = f < f1 ? new Color(8454016) : (f > f1 ? new Color(16751001) : new Color(16764006));
        float f4 = i;
        float f2 = j;
        Render2DUtil.drawRoundedRect(f4, f2, 140.0F, 55.0F, 6.0F, 1.0F, this.applyAlpha(color));
        float f5 = i;
        float f3 = j;
        Render2DUtil.drawOutline(f5, f3, 140.0F, 55.0F, 6.0F, 1.0F, 0.5F, this.applyAlpha(color1));
        Render2DUtil.drawPlayerHead(target, i + 5, j + 5, 40, 0, this.applyAlpha(Color.WHITE));
        int i1 = i + 50;
        int l = j + 40;
        this.drawHealthBar(
            i1,
            l,
            85,
            10,
            f,
            target.getMaxHealth(),
            this.applyAlpha(color2),
            this.applyAlpha(GuiTheme.accentColor1),
            this.applyAlpha(GuiTheme.accentColor2),
            false
        );
        float f6 = i;
        float f7 = j;
        Color color4 = this.applyAlpha(new Color(0, 0, 0, 140));
        Render2DUtil.drawGlow(f6, f7, 140.0F, 55.0F, 6.0F, 1.0F, 6.0F, color4);
        Render2DUtil.drawCircle(i + 130, j + 30, 3.0F, 1.0F, this.applyAlpha(color3));
        MSDFFontAtlas msdffontatlas = VoilClient.fonts.getSelectedFont();
        int k = (int)f;
        msdffontatlas.render(k + "hp", i + 50, j + 20, this.applyAlpha(new Color(104, 105, 107).getRGB()));
        VoilClient.fonts.getSelectedFont().render(target.getName().getString(), i + 50, j + 4, 10.0F, this.applyAlpha(-1));
        VoilClient.getHudLayout().setSize(this, 140, 55);
    }

    private void drawStyle7(int i, int j) {
        Color color = new Color(35, 36, 35);
        Color color1 = new Color(79, 79, 79);
        Color color2 = new Color(44, 44, 44);
        float f3 = i;
        float f = j;
        Render2DUtil.drawRoundedRect(f3, f, 180.0F, 55.0F, 6.0F, 1.0F, this.applyAlpha(color));
        f3 = i;
        float f1 = j;
        Render2DUtil.drawOutline(f3, f1, 180.0F, 55.0F, 6.0F, 1.0F, 0.5F, this.applyAlpha(color1));
        Render2DUtil.drawPlayerHead(target, i + 5, j + 5, 40, 0, this.applyAlpha(Color.WHITE));
        int i1 = i + 50;
        int l = j + 40;
        this.drawHealthBar(
            i1,
            l,
            120,
            5,
            this.displayHealth,
            target.getMaxHealth(),
            this.applyAlpha(color2),
            this.applyAlpha(GuiTheme.accentColor1),
            this.applyAlpha(GuiTheme.accentColor2),
            false
        );
        f3 = i;
        float f2 = j;
        Color color3 = this.applyAlpha(new Color(0, 0, 0, 140));
        Render2DUtil.drawGlow(f3, f2, 180.0F, 55.0F, 6.0F, 1.0F, 6.0F, color3);
        MSDFFontAtlas msdffontatlas = VoilClient.fonts.getSelectedFont();
        int k = (int)this.displayHealth;
        msdffontatlas.renderWithShadow(k + "hp", i + 50, j + 20, 11.0F, this.applyAlpha(new Color(104, 105, 107).getRGB()));
        VoilClient.fonts.getSelectedFont().renderWithShadow(target.getName().getString(), i + 50, j + 4, 10.0F, this.applyAlpha(-1));
        VoilClient.getHudLayout().setSize(this, 180, 55);
    }

    private void drawStyle8(int i, int j) {
        Render2DUtil.drawBlurRounded(i, j, 150.0F, 50.0F, 8.0F, 32.0F, this.getFadeAlpha());
        int k = i + 5;
        int l = j + 5;
        Render2DUtil.drawPlayerHead(target, k, l, 40, 3, this.applyAlpha(Color.WHITE));
        VoilClient.fonts.getSelectedFont().render(target.getName().getString(), k + 44, l + -3 + 1, this.applyAlpha(Color.white));
        VoilClient.fonts.getSelectedFont().render("Health: ", k + 44, l + 10, this.applyAlpha(Color.white));
        float f = this.displayHealth;
        float f1 = target.getMaxHealth();
        float f2 = f / f1;
        Color color = new Color(255, 105, 97);
        Color color1 = new Color(253, 253, 150);
        Color color2 = new Color(119, 221, 119);
        Color color3 = f2 < 0.5F ? ColorUtil.lerpColor(color, color1, f2 / 0.5F) : ColorUtil.lerpColor(color1, color2, (f2 - 0.5F) / 0.5F);
        int i1 = Math.round(f);
        VoilClient.fonts.getSelectedFont().render(String.valueOf(i1), k + 86, l + 10, this.applyAlpha(color3));
        int k1 = i + 50;
        int j1 = j + 30;
        this.drawHealthBar(k1, j1, 95, 4, i1, f1, this.applyAlpha(new Color(44, 44, 44)), this.applyAlpha(color3), this.applyAlpha(color3), false);
        k1 = i + 50;
        int l1 = j + 40;
        float f3 = target.getArmor();
        this.drawBar(k1, l1, 95, 4, f3, 20.0F, this.applyAlpha(new Color(40, 40, 40, 180)), this.applyAlpha(new Color(100, 149, 237)), false);
        VoilClient.getHudLayout().setSize(this, 150, 50);
    }

    private void drawStyle9(int i, int j) {
        Color color = new Color(20, 20, 20);
        Color color1 = new Color(47, 46, 46);
        Color color2 = new Color(255, 0, 150);
        Color color3 = new Color(160, 10, 255);
        Color color4 = new Color(44, 44, 44);
        float f2 = i;
        float f = j;
        Render2DUtil.drawRoundedRect(f2, f, 180.0F, 53.0F, 0.0F, 1.0F, this.applyAlpha(color));
        f2 = i;
        float f1 = j;
        Render2DUtil.drawOutline(f2, f1, 180.0F, 53.0F, 0.0F, 1.0F, 1.0F, this.applyAlpha(color1));
        Render2DUtil.drawPlayerHead(target, i + 5, j + 6, 40, 4, this.applyAlpha(Color.WHITE));
        VoilClient.fonts.getSelectedFont().render(target.getName().getString(), i + 50, j + 5, this.applyAlpha(-1));
        int l = i + 50;
        int k = j + 40;
        this.drawHealthBar(
            l, k, 125, 5, this.displayHealth, target.getMaxHealth(), this.applyAlpha(color4), this.applyAlpha(color2), this.applyAlpha(color3), true
        );
        VoilClient.getHudLayout().setSize(this, 180, 53);
    }

    private Color applyAlpha(Color color) {
        if (color == null) {
            return null;
        }

        float f = this.getFadeAlpha();
        if (f >= 0.999F) {
            return color;
        }

        int i = Math.max(0, Math.min(255, (int)(color.getAlpha() * f)));
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), i);
    }

    private void updateFade() {
        if (!enableAnimation.isEnabled()) {
            this.fadeProgress = this.isChatOpen() ? 1.0F : 0.0F;
            this.fadeActive = false;
        } else {
            float f = (float)(System.currentTimeMillis() - this.fadeStartTime) / (float)animationSpeed.getValue();
            if (f >= 1.0F) {
                this.fadeProgress = this.fadeOut ? 0.0F : 1.0F;
                this.fadeActive = false;
            } else {
                float f1 = switch (animationEasing.getMode()) {
                    case "Linear" -> f;
                    case "Bezier" -> UiEasings.bezierEase(f);
                    case "Expo" -> UiEasings.easeOutExpo(f);
                    case "Smooth" -> UiEasings.expDecay(f);
                    case "Quintic" -> UiEasings.easeInOutQuint(f);
                    case "Circle" -> UiEasings.easeOutCirc(f);
                    default -> f;
                };
                this.fadeProgress = this.fadeOut ? 1.0F - f1 : f1;
            }
        }
    }

    @EventHandler
    public void onPacket(PacketReceiveEvent packetreceiveevent) {
        if (mc.world != null && mc.player != null) {
            Packet packet = packetreceiveevent.getPacket();
            if (packet instanceof EntityStatusS2CPacket) {
                Entity entity = ((EntityStatusS2CPacket)packet).getEntity(mc.world);
                if (entity instanceof PlayerEntity playerentity && entity != mc.player) {
                    UUID uuid = playerentity.getUuid();
                    if (target != null && target.getUuid() == uuid) {
                        this.particleSpawned = true;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onAttackEntity(AttackEntityPostEvent attackentitypostevent) {
        if (attackentitypostevent.getTarget() instanceof AbstractClientPlayerEntity abstractclientplayerentity) {
            if (target == abstractclientplayerentity && !this.fadeOut) {
                this.lastTargetTime = System.currentTimeMillis();
                return;
            }

            target = abstractclientplayerentity;
            this.lastTarget = abstractclientplayerentity;
            this.lastTargetTime = System.currentTimeMillis();
            this.lastHealth = abstractclientplayerentity.getHealth();
            this.displayHealth = abstractclientplayerentity.getHealth();
            this.startFade();
        }
    }
}
