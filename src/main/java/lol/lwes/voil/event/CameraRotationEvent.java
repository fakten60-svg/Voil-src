package lol.lwes.voil.event;

public class CameraRotationEvent extends Event {
    public float yaw;
    public float pitch;

    public CameraRotationEvent(float f, float f1) {
        this.yaw = f;
        this.pitch = f1;
    }
}
