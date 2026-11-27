package lol.lwes.voil.util;

public enum PlayerActivity {
    MOVED,
    ROTATED,
    TOOK_DAMAGE,
    SWINGED,
    TOUCHED_GROUND,
    IN_AIR,
    PING_ISNT_0;

    private static PlayerActivity[] $values() {
        return new PlayerActivity[]{MOVED, ROTATED, TOOK_DAMAGE, SWINGED, TOUCHED_GROUND, IN_AIR, PING_ISNT_0};
    }
}
