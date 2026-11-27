package lol.lwes.voil.hook;

import lol.lwes.voil.VoilClient;
import lol.lwes.voil.event.CameraPosEvent;
import lol.lwes.voil.event.CameraRotationEvent;
import lol.lwes.voil.event.CameraUpdateEvent;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

public class ArgsEventHook {
    public static void onRotationArgs(Args args) {
        if (!VoilClient.getInstance().disabled) {
            float f1 = args.<Float>get(0);
            float f = args.<Float>get(1);
            CameraRotationEvent camerarotationevent = new CameraRotationEvent(f1, f);
            VoilClient.eventBus.post(camerarotationevent);
            args.set(0, camerarotationevent.yaw);
            args.set(1, camerarotationevent.pitch);
        }
    }

    public static void onVelocityArgs(Args args) {
        if (!VoilClient.getInstance().disabled) {
            double d0 = args.<Double>get(0);
            CameraPosEvent cameraposevent = new CameraPosEvent(d0, args.<Double>get(1), args.<Double>get(2));
            VoilClient.eventBus.post(cameraposevent);
            args.set(0, cameraposevent.getX());
            args.set(1, cameraposevent.getY());
            args.set(2, cameraposevent.getZ());
        }
    }

    public static void onEvent(CallbackInfo callbackinfo) {
        if (!VoilClient.getInstance().disabled) {
            CameraUpdateEvent cameraupdateevent = new CameraUpdateEvent();
            VoilClient.eventBus.post(cameraupdateevent);
            if (cameraupdateevent.isCancelled()) {
                callbackinfo.cancel();
            }
        }
    }
}
