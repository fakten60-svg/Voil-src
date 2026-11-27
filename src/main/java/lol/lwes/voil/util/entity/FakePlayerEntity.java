package lol.lwes.voil.util.entity;

import com.mojang.authlib.GameProfile;
import java.util.Objects;
import java.util.UUID;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.mixin.accessor.AbstractClientPlayerEntityAccessor;
import lol.lwes.voil.util.inventory.InventoryUtil;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.player.PlayerEntity;

public class FakePlayerEntity extends OtherClientPlayerEntity {
    private PlayerListEntry cachedListEntry;
    public boolean isClone;
    public boolean locked;

    public FakePlayerEntity(PlayerEntity playerentity, String s, float f, boolean flag) {
        super(Objects.requireNonNull(VoilClient.mc.world), new GameProfile(UUID.randomUUID(), s));
        this.copyPositionAndRotation(playerentity);
        this.setYaw(playerentity.getYaw());
        this.setPitch(playerentity.getPitch());
        this.headYaw = playerentity.headYaw;
        this.bodyYaw = playerentity.bodyYaw;
        Byte obyte = playerentity.getDataTracker().get(AbstractClientPlayerEntity.PLAYER_MODE_CUSTOMIZATION_ID);
        this.getDataTracker().set(AbstractClientPlayerEntity.PLAYER_MODE_CUSTOMIZATION_ID, obyte);
        this.getAttributes().setFrom(playerentity.getAttributes());
        this.setPose(playerentity.getPose());
        if (!this.isPushable()) {
            this.pushAwayFrom(playerentity);
        }

        if (f <= 20.0F) {
            this.setHealth(f);
        } else {
            this.setHealth(f);
            this.setAbsorptionAmount(f - 20.0F);
        }

        if (flag) {
            this.getInventory().clone(playerentity.getInventory());
        }
    }

    public void despawn() {
        VoilClient.mc.world.removeEntity(this.getId(), RemovalReason.DISCARDED);
        this.setRemoved(RemovalReason.DISCARDED);
    }

    public void spawn() {
        this.unsetRemoved();
        VoilClient.mc.world.addEntity(this);
    }

    @Override
    protected PlayerListEntry getPlayerListEntry() {
        if (this.cachedListEntry == null) {
            this.cachedListEntry = ((AbstractClientPlayerEntityAccessor)VoilClient.mc.player).invokeGetPlayerListEntry();
        }

        return this.cachedListEntry;
    }
}
