package lol.lwes.voil.gui.clickgui.component;

public record ThresholdTrack(lol.lwes.voil.gui.clickgui.component.ThresholdKind kind, float startX, float width, float labelWidth) {
    public float endX() {
        return this.startX + this.width;
    }

    public float valueToX(float fraction) {
        return this.startX + this.width * Math.max(0.0F, Math.min(1.0F, fraction));
    }

    public float xToValue(double mouseX) {
        if (this.width <= 0.0F) {
            return 0.0F;
        }

        return (float)Math.max(0.0, Math.min(1.0, (mouseX - this.startX) / this.width));
    }
}
