package lol.lwes.voil.module.combat;

import dev.prestigeclient.renderlib.impl.api.RenderAPI;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Optional;
import lol.lwes.voil.event.AttackEntityPreEvent;
import lol.lwes.voil.event.EventHandler;
import lol.lwes.voil.event.PacketBacktrackEvent;
import lol.lwes.voil.mixin.accessor.EntityS2CPacketMixin;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.ColorSetting;
import lol.lwes.voil.setting.ModeSetting;
import lol.lwes.voil.setting.MultiSelectSetting;
import lol.lwes.voil.setting.NumberSetting;
import lol.lwes.voil.setting.RangeSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.CrashHandler;
import lol.lwes.voil.util.render.ColorUtil;
import lol.lwes.voil.util.render.Render3DUtil;
import lol.lwes.voil.util.target.TargetManager;
import net.minecraft.client.gui.screen.world.LevelLoadingScreen;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import net.minecraft.network.packet.s2c.common.KeepAliveS2CPacket;
import net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityAnimationS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.HealthUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.TeamS2CPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class Backtrack extends Module {
    RangeSetting delay;
    ColorSetting boxColor;
    NumberSetting range;
    ModeSetting boxType;
    BooleanSetting render;
    MultiSelectSetting options;
    ModeSetting delayType;
    private Entity target;
    private Vec3d realPosition;
    private Vec3d lastRealPosition;
    private Vec3d renderPosition;
    private long startTime;
    private int currentDelay;
    private double distanceDelta;
    private boolean pendingFlush;
    private float lastPitch;
    private float lastYaw;
    private long lastPacketTime;
    private static final long INFINITE_DELAY_MS = 4611686018427387903L;

    public Backtrack() {
        super("Backtrack", "Lags enemies into previous positions", Category.Combat);
        this.delay = new RangeSetting("Delay", 1.0, 10000.0, 500.0, 1200.0, 1.0, "Delay for the backtrack");
        Color color1 = new Color(130, 120, 255, 145);
        String s2 = "Color of the real position box";
        Color color = color1;
        this.boxColor = new ColorSetting("Box Color", color, s2);
        this.range = new NumberSetting("Range", 1.0, 6.0, 4.5, 0.1, "Range for the backtrack to disable");
        this.boxType = new ModeSetting("Box Type", "Box", "Type of box to render", new String[]{"Player Model", "Box"});
        this.render = new BooleanSetting("Render", true, "Render the real position");
        String[] astring = new String[]{"UpdateHealth", "UpdateShield", "KeepSound", "StopRange", "CancelKeepalive"};
        this.options = new MultiSelectSetting("Options", "Stuff to flush packets on", astring);
        this.delayType = new ModeSetting("Delay Type", "Static", "Mode to use when flushing", new String[]{"Dynamic", "Distance", "Burst", "Static"});
        this.currentDelay = 0;
        this.pendingFlush = false;
        Setting[] asetting = new Setting[]{this.render, this.range, this.delay, this.delayType, this.boxType, this.options, this.boxColor};
        this.addSettings(asetting);
    }

    private boolean isStopRangeEnabled() {
        return !this.options.getOption("StopRange").isPresent()
            ? false
            : mc.player.squaredDistanceTo(this.realPosition) > this.range.getValue() * this.range.getValue();
    }

    @Override
    public void onTick() {
        if (this.delayType.is("Burst")) {
            if (this.target != null && this.target.isAlive()) {
                if (this.lastPacketTime != 0L && System.currentTimeMillis() - this.lastPacketTime >= this.currentDelay) {
                    this.startBacktrack();
                    this.currentDelay = (int)this.delay.getRandomValue();
                }
            } else {
                this.onEnable();
            }
        } else if (!this.delayType.is("Distance")) {
            if (mc.currentScreen instanceof LevelLoadingScreen) {
                this.startBacktrack();
            } else {
                if (this.delayType.is("Dynamic") && this.target != null && this.realPosition != null) {
                    if (this.lastRealPosition != null) {
                        double d1 = mc.player.getEntityPos().distanceTo(this.lastRealPosition);
                        double d0 = mc.player.getEntityPos().distanceTo(this.realPosition);
                        this.distanceDelta = d0 - d1;
                    }

                    this.lastRealPosition = this.realPosition;
                    if (this.distanceDelta < -0.01) {
                        this.startBacktrack();
                    } else if (this.distanceDelta > 0.01) {
                        int i = this.currentDelay;
                        this.currentDelay = i + 50;
                    }
                }
            }
        } else {
            ArrayList<AbstractClientPlayerEntity> arraylist = TargetManager.getMappedTargets(
                false,
                entity1 -> entity1.isAlive() && mc.player.distanceTo(entity1) <= this.range.getValue() && entity1 instanceof AbstractClientPlayerEntity
                    ? Optional.of((AbstractClientPlayerEntity)entity1)
                    : Optional.empty()
            );
            if (!arraylist.isEmpty()) {
                Entity entity = (Entity)arraylist.get(0);
                if (this.target != null && this.target.isAlive() && this.target.equals(entity)) {
                    if (mc.player.squaredDistanceTo(this.realPosition) < mc.player.squaredDistanceTo(this.target)) {
                        this.startBacktrack();
                    }
                } else {
                    this.setTarget(entity);
                    this.currentDelay = (int)this.delay.getRandomValue();
                }
            } else {
                this.onEnable();
            }
        }
    }

    @Override
    public String getSuffix() {
        if (this.delayType.is("Distance") && this.target != null) {
            int j = this.currentDelay;
            return "Auto " + j + "ms";
        } else if (this.delayType.is("Burst") && this.lastPacketTime != 0L) {
            long k = Math.max(0L, this.currentDelay - (System.currentTimeMillis() - this.lastPacketTime));
            return "Burst " + k + "ms";
        } else {
            int i = this.currentDelay;
            return i + "ms";
        }
    }

    @Override
    public void onEnable() {
        this.target = null;
        this.realPosition = null;
        this.lastRealPosition = null;
        this.renderPosition = null;
        this.startTime = 0L;
        this.distanceDelta = 0.0;
        this.lastPacketTime = 0L;
    }

    private void setTarget(Entity entity) {
        this.target = entity;
        this.realPosition = entity.getTrackedPosition().getPos();
        this.renderPosition = null;
        this.startTime = 0L;
    }

    private void startBacktrack() {
        this.pendingFlush = true;
        this.onEnable();
    }

    @EventHandler
    public void onAttack(AttackEntityPreEvent attackentitypreevent) {
        if (this.isValidTarget(attackentitypreevent.target)) {
            if (this.target != attackentitypreevent.target) {
                this.setTarget(attackentitypreevent.target);
                if (this.delayType.is("Static") || this.delayType.is("Burst")) {
                    this.currentDelay = (int)this.delay.getRandomValue();
                    this.lastPacketTime = System.currentTimeMillis();
                } else if (this.delayType.is("Dynamic")) {
                    float f = mc.player.distanceTo(attackentitypreevent.target);
                    float f1 = (float)this.delay.getValueMin();
                    float f2 = (float)this.delay.getValueMax();
                    this.currentDelay = (int)MathHelper.clamp(f * 200.0F, f1, f2);
                }
            }
        }
    }

    private boolean isValidTarget(Entity entity) {
        return entity != null && entity.isAlive();
    }

    @EventHandler(priority = 200)
    public void onPacketReceive(PacketBacktrackEvent packetbacktrackevent) {
        if (mc.world != null && mc.player != null) {
            if (this.isValidTarget(this.target)) {
                Packet packet = packetbacktrackevent.packet;

                try {
                    if (this.pendingFlush) {
                        this.pendingFlush = false;
                        return;
                    }

                    if (packet instanceof PlayerPositionLookS2CPacket || packet instanceof DisconnectS2CPacket) {
                        this.onEnable();
                        return;
                    }

                    packetbacktrackevent.flag2 = true;
                    packetbacktrackevent.flag3 = true;
                    packetbacktrackevent.delayMs = (long)this.currentDelay;
                    if (this.delayType.is("Burst")) {
                        if (this.lastPacketTime == 0L) {
                            this.lastPacketTime = System.currentTimeMillis();
                        }

                        packetbacktrackevent.delayMs = INFINITE_DELAY_MS;
                    }

                    if (packet instanceof KeepAliveS2CPacket && this.options.getOption("CancelKeepalive").isPresent()) {
                        packetbacktrackevent.flag4 = true;
                        return;
                    }

                    if (packet instanceof PlaySoundS2CPacket && this.options.getOption("KeepSound").isPresent()) {
                        packetbacktrackevent.flag4 = true;
                        return;
                    }

                    if (packet instanceof HealthUpdateS2CPacket && this.options.getOption("UpdateHealth").isPresent()) {
                        packetbacktrackevent.flag4 = true;
                        return;
                    }

                    if (packet instanceof EntityTrackerUpdateS2CPacket entitytrackerupdates2cpacket
                        && entitytrackerupdates2cpacket.id() != mc.player.getId()
                        && this.options.getOption("UpdateShield").isPresent()) {
                        packetbacktrackevent.flag4 = true;
                        return;
                    }

                    if (packet instanceof EntityStatusS2CPacket
                        || packet instanceof ChatMessageS2CPacket
                        || packet instanceof EntityAnimationS2CPacket
                        || packet instanceof TeamS2CPacket) {
                        packetbacktrackevent.flag4 = true;
                        return;
                    }

                    if (packet instanceof EntityPositionS2CPacket entitypositions2cpacket && entitypositions2cpacket.entityId() == this.target.getId()) {
                        this.realPosition = entitypositions2cpacket.change().position();
                        this.lastPitch = entitypositions2cpacket.change().pitch();
                        this.lastYaw = entitypositions2cpacket.change().yaw();
                        if (this.isStopRangeEnabled()) {
                            this.pendingFlush = true;
                            return;
                        }
                    }

                    if (packet instanceof EntityS2CPacket entitys2cpacket && ((EntityS2CPacketMixin)entitys2cpacket).getId() == this.target.getId()) {
                        if (this.realPosition == null) {
                            this.realPosition = this.target.getTrackedPosition().getPos();
                        }

                        this.lastPitch = entitys2cpacket.getPitch();
                        this.lastYaw = entitys2cpacket.getYaw();
                        this.realPosition = new Vec3d(
                            this.realPosition.x + entitys2cpacket.getDeltaX() / 4096.0,
                            this.realPosition.y + entitys2cpacket.getDeltaY() / 4096.0,
                            this.realPosition.z + entitys2cpacket.getDeltaZ() / 4096.0
                        );
                        if (this.isStopRangeEnabled()) {
                            this.pendingFlush = true;
                            return;
                        }
                    }
                } catch (Exception exception) {
                    CrashHandler.handleModuleCrash(this, exception);
                }
            }
        }
    }

    @Override
    public void onWorldRender(RenderAPI.RenderContext rendercontext) {
        if (this.render.isEnabled() && this.target != null && this.realPosition != null) {
            long i = System.nanoTime();
            float f;
            if (this.startTime == 0L) {
                f = 0.0F;
            } else {
                f = (float)(i - this.startTime) / 1.0E9F;
            }

            this.startTime = i;
            if (this.renderPosition == null) {
                this.renderPosition = this.realPosition;
            } else {
                double d0 = 1.0 - Math.exp(-10.0F * f);
                this.renderPosition = this.renderPosition.lerp(this.realPosition, d0);
            }

            Color color = this.boxColor.getColor();
            Color color1 = ColorUtil.withAlpha(color, 145);
            if (this.boxType.is("Box")) {
                Matrix4f matrix4f1 = new Matrix4f();
                Vec3d vec3d = this.renderPosition;
                Matrix4f matrix4f = matrix4f1;
                Render3DUtil.drawMarker(matrix4f, vec3d, color1);
            } else {
                if (this.boxType.is("Player Model")) {
                    Entity entity = this.target;
                    if (entity instanceof PlayerEntity) {
                        ;
                    }
                }
            }
        }
    }
}
