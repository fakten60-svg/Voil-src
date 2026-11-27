package lol.lwes.voil.event;

public class SwingProgressEvent extends Event {
    private float progress;

    public SwingProgressEvent(float f) {
        this.progress = f;
    }

    public float getProgress() {
        return this.progress;
    }

    public void setProgress(float f) {
        this.progress = f;
    }
}
