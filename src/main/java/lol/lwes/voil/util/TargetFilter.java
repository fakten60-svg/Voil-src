package lol.lwes.voil.util;

import lol.lwes.voil.VoilClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;

public enum TargetFilter {
    NOT_SELF {
        @Override
        protected boolean matches(PlayerEntity player) {
            return player != VoilClient.mc.player;
        }
    },
    NOT_FRIEND {
        @Override
        protected boolean matches(PlayerEntity player) {
            return !PlayerTargetUtil.isFriend(player.getUuid());
        }
    },
    NOT_TEAMMATE {
        @Override
        protected boolean matches(PlayerEntity player) {
            return !PlayerTargetUtil.isIgnored(player.getUuid());
        }
    },
    VISIBLE {
        @Override
        protected boolean matches(PlayerEntity player) {
            return !player.isInvisible() && PlayerTargetUtil.isValidTargetEntity(player);
        }
    },
    HITTABLE {
        @Override
        protected boolean matches(PlayerEntity player) {
            return player.isAlive() && !player.isSpectator() && !player.isCreative() && !player.isInvulnerable();
        }
    },
    RANGE_30 {
        @Override
        protected boolean matches(PlayerEntity player) {
            return withinRange(player, 30.0);
        }
    },
    RANGE_20 {
        @Override
        protected boolean matches(PlayerEntity player) {
            return withinRange(player, 20.0);
        }
    },
    RANGE_3 {
        @Override
        protected boolean matches(PlayerEntity player) {
            return withinRange(player, 3.0);
        }
    },
    MOVING {
        @Override
        protected boolean matches(PlayerEntity player) {
            return PlayerTargetUtil.isMoving(player);
        }
    },
    PLAYERSONLY {
        @Override
        protected boolean matches(PlayerEntity player) {
            return PlayerTargetUtil.hasMovedFlag(player);
        }
    },
    ANY {
        @Override
        protected boolean matches(PlayerEntity player) {
            return true;
        }
    };

    protected abstract boolean matches(PlayerEntity player);

    public boolean test(PlayerEntity player) {
        return player != null && this.matches(player);
    }

    private static boolean withinRange(PlayerEntity player, double range) {
        ClientPlayerEntity self = VoilClient.mc.player;
        return self != null && self.squaredDistanceTo(player) <= range * range;
    }
}
