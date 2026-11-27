package lol.lwes.voil.module.player;

import java.util.function.BooleanSupplier;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.event.AttackEntityPostEvent;
import lol.lwes.voil.event.EventHandler;
import lol.lwes.voil.event.PacketBlinkEvent;
import lol.lwes.voil.event.PacketReceiveEvent;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.ModeSetting;
import lol.lwes.voil.setting.NumberSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.MovementUtil;
import lol.lwes.voil.util.TickFastForward;
import lol.lwes.voil.util.TickRequest;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityDamageS2CPacket;
import net.minecraft.util.math.MathHelper;

public class AntiCombo extends Module {
    private final ModeSetting lagMode = new ModeSetting("Lag Mode", "Blink", "How to lag out of the combo", new String[]{"Blink", "Tick Base"});
    private final NumberSetting hits = new NumberSetting("Hits", 1.0, 10.0, 3.0, 1.0, "Hits taken without attacking before lagging");
    private final NumberSetting blinkTime = new NumberSetting("Blink Time", 50.0, 2000.0, 500.0, 50.0, "How long to blink for (ms)");
    private final NumberSetting freezeTicks = new NumberSetting("Freeze Ticks", 1.0, 40.0, 10.0, 1.0, "Ticks to freeze for");
    private final BooleanSetting packetFix = new BooleanSetting("Packet Fix", true, "Hold packets while charging tick base");
    private final BooleanSetting onlyMoving = new BooleanSetting("Only Moving", false, "Only lag out of combos while moving");
    private int hitCount;
    private long lagTimestamp;
    private static final long TICK_MILLIS = 50L;

    public AntiCombo() {
        super("AntiCombo", "Lags out of a combo after taking too many hits", Category.Player);
        Setting[] asetting = new Setting[]{this.lagMode, this.onlyMoving, this.hits, this.blinkTime, this.freezeTicks, this.packetFix};
        this.addSettings(asetting);
        NumberSetting numbersetting = this.blinkTime;
        BooleanSupplier booleansupplier = () -> this.lagMode.is("Blink");
        numbersetting.setVisibleWhen(booleansupplier);
        numbersetting = this.freezeTicks;
        BooleanSupplier booleansupplier1 = () -> this.lagMode.is("Tick Base");
        numbersetting.setVisibleWhen(booleansupplier1);
        BooleanSetting booleansetting = this.packetFix;
        BooleanSupplier booleansupplier2 = () -> this.lagMode.is("Tick Base");
        booleansetting.setVisibleWhen(booleansupplier2);
    }

    @Override
    public void onRender2D() {
        if (this.isLagging() && mc.player != null) {
            int i = mc.getWindow().getScaledWidth();
            int j = mc.getWindow().getScaledHeight();
            long k = this.lagTimestamp - System.currentTimeMillis();
            String s3;
            if (this.lagMode.is("Blink")) {
                s3 = "Blinking " + k + "ms";
            } else {
                int i1 = MathHelper.ceil((float)k / 50.0F);
                s3 = "Frozen " + i1 + "t";
            }

            String s = s3;
            float f = VoilClient.fonts.getSelectedFont().getWidth(s);
            float f1 = i / 2.0F - f / 2.0F;
            float f2 = j / 2.0F + 20.0F;
            int l = MathHelper.clamp((int)(170.0 + 85.0 * Math.sin(System.currentTimeMillis() / 120.0)), 0, 255) << 24 | 16733525;
            VoilClient.fonts.getSelectedFont().render(s, f1, f2, l);
        }
    }

    @Override
    public String getSuffix() {
        return this.lagMode.getMode();
    }

    @Override
    public void onDisable() {
        if (this.lagMode.is("Tick Base") && this.isLagging()) {
            TickFastForward.requestTicks(null);
        }

        this.lagTimestamp = 0L;
    }

    @Override
    public void onEnable() {
        this.hitCount = 0;
        this.lagTimestamp = 0L;
    }

    private boolean isLagging() {
        return System.currentTimeMillis() < this.lagTimestamp;
    }

    @EventHandler
    private void onBlink(PacketBlinkEvent packetblinkevent) {
        if (this.lagMode.is("Blink") && this.isLagging()) {
            packetblinkevent.held = true;
        }
    }

    @EventHandler
    private void onPacketReceive(PacketReceiveEvent packetreceiveevent) {
        if (mc.player != null && mc.world != null) {
            if (packetreceiveevent.getPacket() instanceof EntityDamageS2CPacket entitydamages2cpacket) {
                if (entitydamages2cpacket.entityId() == mc.player.getId()) {
                    if (!this.isLagging()) {
                        if (!this.onlyMoving.isEnabled() || MovementUtil.hasMovementInput()) {
                            Entity entity = entitydamages2cpacket.createDamageSource(mc.world).getAttacker();
                            if (entity != null && entity != mc.player) {
                                this.hitCount++;
                                if (this.hitCount >= this.hits.getInt()) {
                                    this.hitCount = 0;
                                    if (this.lagMode.is("Blink")) {
                                        this.lagTimestamp = System.currentTimeMillis() + this.blinkTime.getInt();
                                    } else {
                                        int i = this.freezeTicks.getInt();
                                        if (TickFastForward.requestTicks(new TickRequest(i, i, this.packetFix.isEnabled()))) {
                                            this.lagTimestamp = System.currentTimeMillis() + i * TICK_MILLIS;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    private void onAttackEntity(AttackEntityPostEvent attackentitypostevent) {
        if (attackentitypostevent.getTarget() instanceof PlayerEntity) {
            this.hitCount = 0;
        }
    }
}
