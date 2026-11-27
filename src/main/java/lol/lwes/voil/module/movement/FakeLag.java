package lol.lwes.voil.module.movement;

import dev.prestigeclient.renderlib.impl.api.RenderAPI;
import java.awt.Color;
import lol.lwes.voil.event.EventHandler;
import lol.lwes.voil.event.PacketBlinkEvent;
import lol.lwes.voil.event.PacketReceiveEvent;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.ColorSetting;
import lol.lwes.voil.setting.ModeSetting;
import lol.lwes.voil.setting.NumberSetting;
import lol.lwes.voil.setting.RangeSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.SystemUtil;
import lol.lwes.voil.util.render.ColorUtil;
import lol.lwes.voil.util.render.Render3DUtil;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.option.Perspective;
import net.minecraft.item.AxeItem;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class FakeLag extends Module {
    private Vec3d lastPacketPos;
    private long lagStartTime;
    private boolean lagging;
    private boolean flushing;
    private Vec3d renderPos;
    private long lastRenderTime;
    private long lagDuration;
    private final ModeSetting mode;
    private final RangeSetting delay;
    private final NumberSetting range;
    private final BooleanSetting weaponOnly;
    private final BooleanSetting serverPos;
    private final ColorSetting color;

    public FakeLag() {
        super("FakeLag", "Holds packets and simulates lag", Category.Movement);
        this.lastPacketPos = Vec3d.ZERO;
        this.lagStartTime = 0L;
        this.lagging = false;
        this.flushing = false;
        this.renderPos = Vec3d.ZERO;
        this.lastRenderTime = 0L;
        this.lagDuration = 0L;
        this.mode = new ModeSetting("Mode", "Fakelag", "Mode of the fakelag", new String[]{"Fakelag", "Lag range"});
        this.delay = new RangeSetting("Delay", 50.0, 3000.0, 400.0, 600.0, 1.0, "Delay of holding packets");
        this.range = new NumberSetting("Range", 1.0, 12.0, 6.0, 1.0, "Range for the lag range to enable");
        this.weaponOnly = new BooleanSetting("Weapon Only", false, "Only lag while holding a weapon");
        this.serverPos = new BooleanSetting("Server Pos", true, "Shows server position of player");
        String s2 = "Color of the server pos";
        Color colorx = Color.RED;
        this.color = new ColorSetting("Color", colorx, s2);
        Setting[] asetting = new Setting[]{this.mode, this.delay, this.range, this.weaponOnly, this.serverPos, this.color};
        this.addSettings(asetting);
        ModeSetting modesetting = this.mode;
        Setting.dependSetting(this.range, "Lag range", modesetting);
    }

    private boolean isAttackPacket(Packet packet) {
        return packet instanceof PlayerInteractEntityC2SPacket
            || packet instanceof PlayerInteractBlockC2SPacket
            || packet instanceof PlayerInteractItemC2SPacket
            || packet instanceof ClickSlotC2SPacket
            || packet instanceof ClientCommandC2SPacket
            || packet instanceof UpdateSelectedSlotC2SPacket;
    }

    private boolean isHoldingSword() {
        return mc.player.getMainHandStack().isIn(ItemTags.SWORDS) || mc.player.getMainHandStack().getItem() instanceof AxeItem;
    }

    private void drawRangeCircle() {
        Vec3d vec3d = mc.player.getLerpedPos(SystemUtil.getTickDelta());
        double d0 = this.range.getFloat();
        Vec3d vec3d1 = null;

        for (int i = 0; i <= 64; i++) {
            double d1 = (Math.PI * 2) * i / 64;
            Vec3d vec3d2 = new Vec3d(vec3d.x + d0 * Math.cos(d1), vec3d.y + 0.5, vec3d.z + d0 * Math.sin(d1));
            if (vec3d1 != null) {
                Color colorx = this.color.getColor();
                Render3DUtil.drawLine(vec3d1, vec3d2, colorx);
            }

            vec3d1 = vec3d2;
        }
    }

    @Override
    public void onTick() {
        if (this.isPlayerNull()) {
            this.onDisable();
        } else if (this.lagging) {
            boolean flag = false;
            if (this.mode.is("Fakelag")) {
                flag = System.currentTimeMillis() - this.lagStartTime >= this.lagDuration;
            } else if (this.mode.is("Lag range")) {
                if (this.hasEnemyInRange() && (!this.weaponOnly.isEnabled() || this.isHoldingSword())) {
                    flag = System.currentTimeMillis() - this.lagStartTime >= this.lagDuration;
                } else {
                    flag = true;
                }
            }

            if (flag) {
                this.onDisable();
            }
        }
    }

    @Override
    public String getSuffix() {
        int j = (int)this.delay.getValueMax();
        int i = (int)this.delay.getValueMin();
        return i + "-" + j;
    }

    private boolean shouldHoldPackets() {
        if (this.weaponOnly.isEnabled() && !this.isHoldingSword()) {
            return false;
        } else if (this.mode.is("Fakelag")) {
            return true;
        } else {
            return this.mode.is("Lag range") ? this.hasEnemyInRange() : false;
        }
    }

    private boolean isPlayerNull() {
        return mc.world == null || mc.player == null;
    }

    private boolean hasEnemyInRange() {
        if (mc.world == null) {
            return false;
        }

        int j;
        int i = j = this.range.getInt();
        double d0 = i * j;
        return mc.world.getPlayers().stream().anyMatch(abstractclientplayerentity -> {
            if (abstractclientplayerentity == mc.player || abstractclientplayerentity.isSpectator()) {
                return false;
            }

            return abstractclientplayerentity.squaredDistanceTo(mc.player) - d0 <= 0.0;
        });
    }

    @Override
    public void onEnable() {
        this.reset();
    }

    public void reset() {
        this.lagStartTime = System.currentTimeMillis();
        this.lagging = false;
        this.lagDuration = 0L;
        if (!this.isPlayerNull()) {
            this.lastPacketPos = mc.player.getEntityPos();
        }

        this.renderPos = this.lastPacketPos;
        this.lastRenderTime = 0L;
        this.flushing = false;
    }

    @Override
    public void onDisable() {
        this.flushing = true;
        this.lagStartTime = System.currentTimeMillis();
        this.lagging = false;
        this.lagDuration = 0L;
    }

    private void recordPosition(Packet packet) {
        if (packet instanceof PlayerMoveC2SPacket playermovec2spacket) {
            this.lastPacketPos = new Vec3d(
                playermovec2spacket.getX(mc.player.getX()), playermovec2spacket.getY(mc.player.getY()), playermovec2spacket.getZ(mc.player.getZ())
            );
        }
    }

    @EventHandler
    public void onPacketReceive(PacketReceiveEvent packetreceiveevent) {
        if (packetreceiveevent.getPacket() instanceof PlayerPositionLookS2CPacket) {
            this.onDisable();
        }
    }

    @EventHandler(priority = 200)
    public void onPacketSend(PacketBlinkEvent packetblinkevent) {
        if (!this.isPlayerNull()) {
            Packet packet = packetblinkevent.packet;
            packetblinkevent.callback = this::recordPosition;
            if (this.flushing) {
                this.flushing = false;
            } else if (!this.isAttackPacket(packet)) {
                if (this.shouldHoldPackets()) {
                    packetblinkevent.held = true;
                    if (!this.lagging) {
                        this.lagDuration = (long)this.delay.getRandomValue();
                    }

                    this.lagging = true;
                }
            }
        }
    }

    @Override
    public void onWorldRender(RenderAPI.RenderContext rendercontext) {
        if (this.serverPos.isEnabled() && !this.isPlayerNull()) {
            Color color2 = this.color.getColor();
            Color color1 = new Color(ColorUtil.toArgb(color2));
            long i = System.nanoTime();
            if (this.lastRenderTime == 0L) {
                this.lastRenderTime = i;
                this.renderPos = this.lastPacketPos;
            } else {
                double d0 = Math.min((i - this.lastRenderTime) / 1.0E9, 0.1);
                this.lastRenderTime = i;
                if (this.renderPos.squaredDistanceTo(this.lastPacketPos) > 1024.0) {
                    this.renderPos = this.lastPacketPos;
                } else {
                    double d1 = 1.0 - Math.exp(-6.0 * d0);
                    this.renderPos = new Vec3d(
                        this.renderPos.x + (this.lastPacketPos.x - this.renderPos.x) * d1,
                        this.renderPos.y + (this.lastPacketPos.y - this.renderPos.y) * d1,
                        this.renderPos.z + (this.lastPacketPos.z - this.renderPos.z) * d1
                    );
                    if (mc.options.getPerspective() != Perspective.FIRST_PERSON && this.renderPos.squaredDistanceTo(mc.player.getEntityPos()) > 0.02) {
                        Matrix4f matrix4f1 = new Matrix4f();
                        Vec3d vec3d1 = this.renderPos;
                        Color colorx = ColorUtil.withAlpha(color1, 145);
                        Vec3d vec3d = vec3d1;
                        Matrix4f matrix4f = matrix4f1;
                        Render3DUtil.drawMarker(matrix4f, vec3d, colorx);
                    }

                    if (this.mode.is("Lag range")) {
                        this.drawRangeCircle();
                    }
                }
            }
        }
    }
}
