package lol.lwes.voil.util.rotation;

public class SmoothRotationState {
    boolean ended;
    boolean onTarget;
    double progress;
    boolean done;

    public static SmoothRotationState createEnded() {
        SmoothRotationState smoothrotationstate = new SmoothRotationState();
        smoothrotationstate.ended = true;
        smoothrotationstate.done = true;
        return smoothrotationstate;
    }

    public double getProgress() {
        return this.progress;
    }

    public boolean hasEnded() {
        return this.ended;
    }

    public void markDone() {
        this.done = true;
    }

    public boolean isOnTarget() {
        return this.onTarget;
    }
}
