package lol.lwes.voil.util.rotation;

public enum RotationPriority {
    LOW(0),
    MEDIUM(1),
    HIGH(2);

    final int priority;

    RotationPriority(int j) {
        this.priority = j;
    }

    private static RotationPriority[] allValues() {
        return new RotationPriority[]{LOW, MEDIUM, HIGH};
    }

    public int getPriority() {
        return this.priority;
    }
}
