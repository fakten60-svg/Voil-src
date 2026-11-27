package lol.lwes.voil.util.rotation;

public final class RotationRequest {
    final Rotation rotation;
    final RotationPriority priority;
    final long createdAt;
    final long lifetime;

    RotationRequest(Rotation rotation, RotationPriority rotationpriority) {
        this.rotation = rotation;
        this.priority = rotationpriority;
        this.createdAt = System.currentTimeMillis();
        this.lifetime = 0L;
    }

    boolean isExpired() {
        return this.lifetime > 0L && System.currentTimeMillis() - this.createdAt > this.lifetime;
    }
}
