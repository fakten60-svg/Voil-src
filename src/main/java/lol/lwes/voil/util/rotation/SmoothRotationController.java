package lol.lwes.voil.util.rotation;

import java.util.function.Supplier;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.event.EventHandler;
import lol.lwes.voil.event.PacketSendPostEvent;
import lol.lwes.voil.event.PreRenderEvent;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.util.SystemUtil;
import lol.lwes.voil.util.input.KeyUtil;
import lol.lwes.voil.util.render.EasingFunction;
import lol.lwes.voil.util.render.UiEasings;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class SmoothRotationController {
    private Object owner;
    private Rotation startRotation;
    private Rotation currentRotation;
    private Rotation targetRotation;
    private double baseSpeed;
    private double speed;
    private double progress;
    private Supplier smoothingSupplier;
    private Supplier targetSupplier;
    private Supplier speedSupplier;
    private boolean useServerRotation;
    private boolean finished;
    private RotationAxisMode axisMode;
    private EasingFunction axisFilter;
    private SmoothRotationState state = SmoothRotationState.createEnded();
    private static final long ROTATION_LIFETIME_MS = 75L;

    public void release(Object object) {
        if (this.owner == object) {
            this.state.ended = true;
            this.owner = null;
            VoilClient.getRotationManager().reset();
        }
    }

    public static int getMinPriority() {
        return 0;
    }

    public static Supplier<Double> smoothForTicks(double d0) {
        double d1 = defaultSpeed() + d0;
        return () -> d1 - defaultSpeed();
    }

    public static int getMaxPriority() {
        return 3;
    }

    public SmoothRotationState start(Object object, Supplier<Double> supplier, Supplier<Rotation> supplier1, Supplier<Boolean> supplier2, boolean flag) {
        RotationAxisMode rotationaxismode1 = RotationAxisMode.YawAndPitch;
        EasingFunction easingfunction = UiEasings::linear;
        RotationAxisMode rotationaxismode = rotationaxismode1;
        Object object1 = null;
        boolean flag1 = flag;
        Supplier supplier3 = supplier2;
        Supplier supplier4 = supplier1;
        Supplier supplier5 = supplier;
        Object object2 = object;
        return this.startWith(object2, supplier5, supplier4, supplier3, flag1, false, (SmoothRotationState)object1, rotationaxismode, easingfunction);
    }

    private Rotation getCurrentRotation(boolean flag) {
        Rotation rotation;
        if (flag && VoilClient.getRotationManager().isActive()) {
            rotation = VoilClient.getRotationManager().getTargetRotation();
        } else {
            ClientPlayerEntity clientplayerentity = VoilClient.mc.player;
            rotation = Rotation.fromEntity(clientplayerentity);
        }

        return rotation;
    }

    public SmoothRotationState startWith(
        Object object,
        Supplier<Double> supplier,
        Supplier<Rotation> supplier1,
        Supplier<Boolean> supplier2,
        boolean flag,
        boolean flag1,
        SmoothRotationState smoothrotationstate,
        RotationAxisMode rotationaxismode,
        EasingFunction easingfunction
    ) {
        if (!flag1 && this.owner == object) {
            return this.state;
        } else {
            this.targetRotation = (Rotation)supplier1.get();
            if (this.targetRotation == null) {
                this.release(this.owner);
                return SmoothRotationState.createEnded();
            } else {
                this.owner = object;
                this.useServerRotation = flag;
                this.baseSpeed = defaultSpeed();
                this.speed = this.baseSpeed;
                this.progress = 0.0;
                this.speedSupplier = supplier2;
                boolean flag2 = this.useServerRotation;
                this.startRotation = this.getCurrentRotation(flag2);
                this.currentRotation = this.startRotation;
                this.smoothingSupplier = supplier;
                this.targetSupplier = supplier1;
                this.axisMode = rotationaxismode;
                this.axisFilter = easingfunction;
                this.state = smoothrotationstate == null ? new SmoothRotationState() : smoothrotationstate;
                return this.state;
            }
        }
    }

    private static double defaultSpeed() {
        return (VoilClient.mc.player == null ? 0 : VoilClient.mc.player.age) + SystemUtil.getTickDelta();
    }

    @EventHandler
    public void onPostSend(PacketSendPostEvent packetsendpostevent) {
        Packet packet = packetsendpostevent.getPacket();
        if (packet instanceof PlayerMoveC2SPacket && ((PlayerMoveC2SPacket)packet).changesLook()) {
            KeyUtil.isKeyDown(80);
        }
    }

    @EventHandler
    public void onPreRender(PreRenderEvent prerenderevent) {
        if (this.state.done) {
            this.owner = null;
            this.state.ended = true;
        }

        if (!Module.isNotInGame() && this.owner != null) {
            double d0 = Math.max(0.0, (Double)this.smoothingSupplier.get());
            double d1 = Math.max(0.0, defaultSpeed() - this.baseSpeed);
            double d2 = d1 + d0;
            double d3 = d2 < 1.0E-6 ? 1.0 : d1 / d2;
            d3 = Math.min(1.0, Math.max(d3, this.progress));
            this.progress = d3;
            boolean flag = this.useServerRotation;
            Rotation rotation1 = this.getCurrentRotation(flag);
            Rotation rotation4 = this.currentRotation;
            if (!rotation1.isWithin(rotation4, 0.01)) {
                Rotation rotation5 = this.currentRotation;
                Rotation rotation2 = rotation1.subtract(rotation5);
                this.startRotation = this.startRotation.add(rotation2);
            }

            Rotation rotation6 = (Rotation)this.targetSupplier.get();
            if (rotation6 == null) {
                rotation6 = this.targetRotation;
            } else {
                this.targetRotation = rotation6;
            }

            double d4 = Math.max(1.0E-4, defaultSpeed() - this.speed);
            this.speed = defaultSpeed();
            Rotation rotation3;
            if (d3 >= 0.95) {
                float f = (float)(1.0 - Math.pow(0.55, d4));
                double d5 = f;
                rotation3 = this.currentRotation.lerp(d5, rotation6);
            } else {
                Rotation rotation7 = this.startRotation;
                double d6 = this.axisFilter.apply((float)d3);
                rotation3 = rotation7.lerp(d6, rotation6);
            }

            this.currentRotation = rotation3;
            this.state.progress = d3;
            this.state.onTarget = d3 >= 0.95 || d0 <= 0.15;
            KeyUtil.isKeyDown(79);
            if (this.state.onTarget && (this.speedSupplier == null || (Boolean)this.speedSupplier.get())) {
                this.owner = null;
                this.state.ended = true;
            }

            if (this.useServerRotation) {
                RotationManager rotationmanager = VoilClient.getRotationManager();
                Object object1 = this.owner == null ? this : this.owner;
                RotationPriority rotationpriority = RotationPriority.MEDIUM;
                Rotation rotation = rotation3;
                Object object = object1;
                rotationmanager.submitRequest(object, rotation, rotationpriority);
            } else {
                rotation3 = Rotation.applyMouseSensitivity(new Rotation(VoilClient.mc.player.getYaw(), VoilClient.mc.player.getPitch()), rotation3);
                this.currentRotation = rotation3;
                if (this.axisMode == RotationAxisMode.YawAndPitch || this.axisMode == RotationAxisMode.Yaw) {
                    float f1 = rotation3.getYawFloat() - VoilClient.mc.player.getYaw();
                    VoilClient.mc.player.renderYaw += f1;
                    VoilClient.mc.player.lastRenderYaw += f1;
                    VoilClient.mc.player.lastYaw += f1;
                    VoilClient.mc.player.setYaw(rotation3.getYawFloat());
                }

                if (this.axisMode == RotationAxisMode.YawAndPitch || this.axisMode == RotationAxisMode.Pitch) {
                    VoilClient.mc.player.setPitch(rotation3.getPitchFloat());
                }
            }
        }
    }
}
