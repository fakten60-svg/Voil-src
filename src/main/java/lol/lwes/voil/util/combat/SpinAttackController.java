package lol.lwes.voil.util.combat;

import java.util.function.Consumer;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.event.AttackEntityPreEvent;
import lol.lwes.voil.event.DisplacementEvent;
import lol.lwes.voil.event.EventHandler;
import lol.lwes.voil.event.HandSwingEvent;
import lol.lwes.voil.event.InputEvent;
import lol.lwes.voil.event.PacketSendPostEvent;
import lol.lwes.voil.event.TickEvent;
import lol.lwes.voil.util.AimMathUtil;
import lol.lwes.voil.util.rotation.Rotation;
import lol.lwes.voil.util.rotation.RotationManager;
import lol.lwes.voil.util.world.EntityUtil;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

public class SpinAttackController {
    private int state;
    private Entity target;
    private boolean rotating;
    private boolean attacked;
    private float targetYaw;
    private Consumer onRotate;
    private Runnable onFinish;

    public boolean start(Entity entity, float f, Consumer consumer, Runnable runnable) {
        if (this.state <= 0 && VoilClient.mc.player != null && entity != null) {
            this.targetYaw = VoilClient.mc.player.getYaw() + f;
            this.target = entity;
            this.onRotate = consumer;
            this.onFinish = runnable;
            this.state = 1;
            return true;
        } else {
            return false;
        }
    }

    private void finish() {
        VoilClient.getRotationManager().reset();
        this.state = 0;
        this.target = null;
        this.onRotate = null;
        Runnable runnable = this.onFinish;
        this.onFinish = null;
        if (runnable != null) {
            runnable.run();
        }
    }

    public void cancel() {
        if (this.state > 0) {
            this.onFinish = null;
            this.finish();
        }
    }

    public boolean isActive() {
        return this.state > 0;
    }

    @EventHandler
    private void onTick(TickEvent tickevent) {
        if (VoilClient.mc.player != null && VoilClient.mc.world != null && this.target != null) {
            int i = this.state;
            if (i != 1) {
                if (i != 3) {
                    if (i == 4) {
                        this.finish();
                    }
                } else {
                    if (VoilClient.mc.targetedEntity == this.target) {
                        this.rotating = true;
                        VoilClient.mc.interactionManager.attackEntity(VoilClient.mc.player, this.target);
                        VoilClient.mc.player.swingHand(Hand.MAIN_HAND);
                        this.rotating = false;
                    }

                    this.state = 4;
                }
            } else {
                RotationManager rotationmanager = VoilClient.getRotationManager();
                float f = this.targetYaw;
                rotationmanager.requestFromCallerAngles(f, VoilClient.mc.player.getPitch());
                if (this.onRotate != null) {
                    this.onRotate.accept(this.targetYaw);
                }

                this.state = 2;
            }
        }
    }

    @EventHandler(priority = -200)
    private void onAttackEntity(AttackEntityPreEvent attackentitypreevent) {
        if (!this.rotating) {
            if (this.state <= 0 && VoilClient.mc.player != null) {
                Entity entity1 = attackentitypreevent.getTarget();
                DisplacementEvent displacementevent = DisplacementEvent.create(entity1);
                VoilClient.eventBus.postObject(displacementevent);
                if (displacementevent.handled) {
                    this.attacked = true;
                    attackentitypreevent.setCancelled2(true);
                    Entity entity2 = attackentitypreevent.getTarget();
                    Consumer consumer1 = displacementevent.callback;
                    Runnable runnable = displacementevent.onFinish;
                    Consumer consumer = consumer1;
                    float f = displacementevent.yawOffset;
                    Entity entity = entity2;
                    this.start(entity, f, consumer, runnable);
                }
            }
        }
    }

    @EventHandler
    private void onInputEvent(InputEvent inputevent) {
        if (this.state > 0) {
            inputevent.input.playerInput.sprint = true;
            inputevent.input.playerInput.forward = true;
            inputevent.input.forward = 1.0F;
            inputevent.input.playerInput.backward = false;
        }
    }

    @EventHandler
    private void onHandSwing(HandSwingEvent handswingevent) {
        if (this.attacked) {
            this.attacked = false;
            handswingevent.cancel();
        }
    }

    @EventHandler
    private void onPacketPost(PacketSendPostEvent packetsendpostevent) {
        if (this.state == 2 && this.target != null) {
            if (packetsendpostevent.getPacket() instanceof PlayerMoveC2SPacket playermovec2spacket && playermovec2spacket.changesLook()) {
                this.state = 3;
                Vec3d vec3d1 = VoilClient.mc.player.getEyePos();
                Vec3d vec3d = AimMathUtil.closestPointOnBox(vec3d1, this.target.getBoundingBox().shrink(0.1, 0.1, 0.1));
                RotationManager rotationmanager = VoilClient.getRotationManager();
                Rotation rotation = Rotation.fromPositions(vec3d1, vec3d);
                rotationmanager.requestFromCallerRotation(rotation);
            }
        }
    }
}
