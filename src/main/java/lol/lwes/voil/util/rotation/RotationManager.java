package lol.lwes.voil.util.rotation;

import dev.prestigeclient.renderlib.impl.api.RenderAPI;
import java.lang.StackWalker.Option;
import java.util.Map.Entry;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.event.EventHandler;
import lol.lwes.voil.event.HookPostEvent;
import lol.lwes.voil.event.HookPreEvent;
import lol.lwes.voil.event.InputEvent;
import lol.lwes.voil.event.TickEvent;
import net.minecraft.client.option.Perspective;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class RotationManager {
    private Rotation currentRotation = new Rotation(0.0, 0.0);
    private Rotation targetRotation = new Rotation(0.0, 0.0);
    private float cameraYaw;
    private float cameraPitch;
    private float savedYaw;
    private float savedPitch;
    private float previousYaw;
    private float previousPitch;
    private final Map<Object, RotationRequest> requests = new ConcurrentHashMap<>();
    private volatile boolean active = false;
    private static final StackWalker callerWalker = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE);

    public void removeRequest(Object object) {
        if (object != null) {
            this.requests.remove(object);
            this.active = !this.requests.isEmpty();
            this.selectTopRequest();
        }
    }

    public void clearRequests() {
        this.requests.clear();
        this.active = false;
    }

    public boolean isActive() {
        return this.active;
    }

    public int getRequestCount() {
        return this.requests.size();
    }

    private void selectTopRequest() {
        if (!this.requests.isEmpty()) {
            RotationRequest rotationrequest = null;

            for (RotationRequest rotationrequest1 : this.requests.values()) {
                if (rotationrequest == null) {
                    rotationrequest = rotationrequest1;
                } else if (rotationrequest1.priority.getPriority() > rotationrequest.priority.getPriority()
                    || rotationrequest1.priority.getPriority() == rotationrequest.priority.getPriority()
                        && rotationrequest1.createdAt > rotationrequest.createdAt) {
                    rotationrequest = rotationrequest1;
                }
            }

            if (rotationrequest != null) {
                this.targetRotation = rotationrequest.rotation;
            }
        }
    }

    public void requestAnglesPriority(Object object, float f, float f1, RotationPriority rotationpriority) {
        Rotation rotation = new Rotation(f, f1);
        Object object1 = object;
        this.submitRequest(object1, rotation, rotationpriority);
    }

    public Rotation getTargetRotation() {
        return this.targetRotation;
    }

    public void lookAtPositionDefault(Vec3d vec3d) {
        this.lookAtPosition(vec3d, RotationPriority.MEDIUM);
    }

    public void lookAtBlockDefault(BlockPos blockpos) {
        this.lookAtBlock(blockpos, RotationPriority.MEDIUM);
    }

    public void requestAngles(Object object, float f, float f1) {
        Rotation rotation = new Rotation(f, f1);
        Object object1 = object;
        this.requestRotationEx(object1, rotation);
    }

    public void requestFromCaller(Rotation rotation, RotationPriority rotationpriority) {
        Class oclass = callerWalker.getCallerClass();
        if (rotation == null) {
            this.removeRequest(oclass);
        } else {
            this.submitRequest(oclass, rotation, rotationpriority);
        }
    }

    public void requestRotation(Object object, Rotation rotation) {
        this.submitRequest(object, rotation, RotationPriority.MEDIUM);
    }

    public void requestAnglesPriorityEx(Object object, float f, float f1, RotationPriority rotationpriority) {
        Rotation rotation = new Rotation(f, f1);
        Object object1 = object;
        this.submitRequest(object1, rotation, rotationpriority);
    }

    public void requestFromCallerDefault(Rotation rotation) {
        this.requestFromCaller(rotation, RotationPriority.MEDIUM);
    }

    public void requestAnglesEx(Object object, float f, float f1) {
        Rotation rotation = new Rotation(f, f1);
        Object object1 = object;
        this.submitRequest(object1, rotation, RotationPriority.MEDIUM);
    }

    public Rotation getCurrentRotation() {
        return this.currentRotation;
    }

    public void requestFromCallerAngles(float f, float f1) {
        Rotation rotation = new Rotation(f, f1);
        this.requestFromCaller(rotation, RotationPriority.MEDIUM);
    }

    public void requestFromCallerAnglesEx(float f, float f1) {
        Rotation rotation = new Rotation(f, f1);
        this.requestFromCaller(rotation, RotationPriority.MEDIUM);
    }

    public void requestFromCallerRotation(Rotation rotation) {
        this.requestFromCaller(rotation, RotationPriority.MEDIUM);
    }

    public void lookAtBlockKeyed(BlockPos blockpos) {
        this.lookAtBlock(blockpos, RotationPriority.MEDIUM);
    }

    public void requestRotationEx(Object object, Rotation rotation) {
        this.submitRequest(object, rotation, RotationPriority.MEDIUM);
    }

    public void lookAtBlock(BlockPos blockpos, RotationPriority rotationpriority) {
        if (VoilClient.mc.player != null && blockpos != null) {
            Vec3d vec3d = new Vec3d(blockpos.getX() + 0.5, blockpos.getY() + 0.5, blockpos.getZ() + 0.5);
            this.lookAtPosition(vec3d, rotationpriority);
        }
    }

    public void submitRequest(Object object, Rotation rotation, RotationPriority rotationpriority) {
        if (VoilClient.mc.player != null && object != null && rotation != null) {
            Rotation rotation1 = new Rotation(rotation.getYawFloat(), rotation.getPitchFloat());
            Map<Object, RotationRequest> map = this.requests;
            float f = rotation1.getYawFloat();
            map.put(
                object,
                new RotationRequest(
                    Rotation.fromMouseDelta(f, rotation1.getPitchFloat()), rotationpriority == null ? RotationPriority.MEDIUM : rotationpriority
                )
            );
            this.selectTopRequest();
            this.active = !this.requests.isEmpty();
        }
    }

    public void requestFromCallerAnglesPriority(float f, float f1, RotationPriority rotationpriority) {
        Rotation rotation = new Rotation(f, f1);
        this.requestFromCaller(rotation, rotationpriority);
    }

    public void lookAtPosition(Vec3d vec3d, RotationPriority rotationpriority) {
        if (VoilClient.mc.player != null && vec3d != null) {
            Rotation rotation = Rotation.fromPositions(VoilClient.mc.player.getEyePos(), vec3d);
            float f2 = rotation.getYawFloat();
            float f = rotation.getPitchFloat();
            float f1 = f2;
            this.requestFromCallerAnglesPriorityEx(f1, f, rotationpriority);
        }
    }

    public void lookAtPositionSimple(Vec3d vec3d) {
        this.lookAtPosition(vec3d, RotationPriority.MEDIUM);
    }

    public void requestFromCallerAnglesPriorityEx(float f, float f1, RotationPriority rotationpriority) {
        Rotation rotation = new Rotation(f, f1);
        this.requestFromCaller(rotation, rotationpriority);
    }

    public boolean hasRequest(Object object) {
        return object != null && this.requests.containsKey(object);
    }

    public void reset() {
        if (VoilClient.mc.player != null) {
            this.currentRotation = new Rotation(0.0F, 0.0F);
            this.targetRotation = new Rotation(0.0F, 0.0F);
            this.requests.clear();
            this.active = false;
        }
    }

    public void updateCameraAngles(RenderAPI.RenderContext renderapi$rendercontext) {
        this.cameraYaw = renderapi$rendercontext.camera().getYaw();
        this.cameraPitch = renderapi$rendercontext.camera().getPitch();
        if (VoilClient.mc.options.getPerspective() == Perspective.THIRD_PERSON_FRONT) {
            this.cameraYaw = MathHelper.wrapDegrees(this.cameraYaw + 180.0F);
        }
    }

    @EventHandler(priority = 200)
    private void onTick(TickEvent tickevent) {
        if (VoilClient.mc.player != null && VoilClient.mc.world != null) {
            if (this.currentRotation.getYawFloat() == 0.0F
                && this.currentRotation.getPitchFloat() == 0.0F
                && this.targetRotation.getYawFloat() == 0.0F
                && this.targetRotation.getPitchFloat() == 0.0F) {
                this.currentRotation = new Rotation(0.0F, 0.0F);
                this.targetRotation = new Rotation(0.0F, 0.0F);
            } else {
                this.requests.entrySet().removeIf(entry -> entry.getValue().isExpired());
                this.selectTopRequest();
                this.currentRotation = this.targetRotation;
                this.active = !this.requests.isEmpty();
            }
        }
    }

    @EventHandler(priority = 9999)
    void onInput(InputEvent inputevent) {
        if (inputevent.localPlayer && this.isActive() && (inputevent.input.forward != 0.0F || inputevent.input.sideways != 0.0F)) {
            float f = this.targetRotation.getYawFloat();
            double d0 = inputevent.input.sideways * Math.cos(Math.toRadians(f)) - inputevent.input.forward * Math.sin(Math.toRadians(f));
            double d1 = inputevent.input.forward * Math.cos(Math.toRadians(f)) + inputevent.input.sideways * Math.sin(Math.toRadians(f));
            double d2 = Double.MAX_VALUE;
            double d3 = 0.0;
            double d4 = 0.0;

            for (double d5 = -1.0; d5 <= 1.0; d5++) {
                for (double d6 = -1.0; d6 <= 1.0; d6++) {
                    double d7 = d6 * Math.cos(Math.toRadians(f)) - d5 * Math.sin(Math.toRadians(f));
                    double d8 = d5 * Math.cos(Math.toRadians(f)) + d6 * Math.sin(Math.toRadians(f));
                    double d9 = d7 - d0;
                    double d10 = d8 - d1;
                    double d11 = Math.sqrt(d9 * d9 + d10 * d10);
                    if (d2 > d11) {
                        d2 = d11;
                        d3 = d5;
                        d4 = d6;
                    }
                }
            }

            inputevent.input.forward = (float)d3;
            inputevent.input.sideways = (float)d4;
        }
    }

    public Map getRequests() {
        return this.requests;
    }

    @EventHandler
    void postMovementPacket(HookPreEvent hookpreevent) {
        if (this.savedYaw != 0.0F && this.savedPitch != 0.0F) {
            if (this.active) {
                if (this.targetRotation.getYaw() - this.savedYaw > 360.0) {
                    VoilClient.mc.player.renderYaw += 360.0F;
                    VoilClient.mc.player.lastRenderYaw += 360.0F;
                    VoilClient.mc.player.lastYaw += 360.0F;
                    this.savedYaw += 360.0F;
                } else if (this.targetRotation.getYaw() - this.savedYaw < -360.0) {
                    VoilClient.mc.player.renderYaw -= 360.0F;
                    VoilClient.mc.player.lastRenderYaw -= 360.0F;
                    VoilClient.mc.player.lastYaw -= 360.0F;
                    this.savedYaw -= 360.0F;
                }
            }

            VoilClient.mc.player.setYaw(this.savedYaw);
            VoilClient.mc.player.setPitch(this.savedPitch);
            this.savedYaw = 0.0F;
            this.savedPitch = 0.0F;
        }
    }

    @EventHandler
    void preMovementPacket(HookPostEvent hookpostevent) {
        float f = VoilClient.mc.player.getYaw();
        RotationManager rotationmanager1 = this;
        rotationmanager1.previousYaw = f;
        this.savedYaw = f;
        float f1 = VoilClient.mc.player.getPitch();
        RotationManager rotationmanager2 = this;
        rotationmanager2.previousPitch = f1;
        this.savedPitch = f1;
        if (this.active) {
            VoilClient.mc.player.setYaw(this.targetRotation.getYawFloat());
            VoilClient.mc.player.setPitch(this.targetRotation.getPitchFloat());
        }
    }

    public void requestRotationPriority(Object object, Rotation rotation, RotationPriority rotationpriority) {
        this.submitRequest(object, rotation, rotationpriority);
    }
}
