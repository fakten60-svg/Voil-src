package lol.lwes.voil.mixin;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemCooldownManager.class)
public abstract class ItemCooldownManagerMixin {
    private Map startTicks;
    private Map totalDurations;
    @Shadow
    private int tick;

    @Inject(method = "remove", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/ItemCooldownManager;entries:Ljava/util/Map;"))
    private void onRemoveCooldown(Identifier identifier, CallbackInfo callbackinfo) {
        this.getStartTicks().remove(identifier);
        this.getTotalDurations().remove(identifier);
    }

    private Map getTotalDurations() {
        if (this.totalDurations == null) {
            this.totalDurations = new HashMap();
        }

        return this.totalDurations;
    }

    public int getRemainingTicks(ItemStack itemstack) {
        if (itemstack == null || itemstack.isEmpty()) {
            return 0;
        } else if (!this.isCoolingDown(itemstack)) {
            return 0;
        } else {
            Identifier identifier = this.getGroup(itemstack);
            Integer integer = (Integer)this.getStartTicks().get(identifier);
            Integer integer1 = (Integer)this.getTotalDurations().get(identifier);
            if (integer != null && integer1 != null) {
                int i = this.tick - integer;
                int k = integer1;
                int j = k - i;
                return Math.max(0, j);
            } else {
                return 0;
            }
        }
    }

    @Inject(
        method = "set(Lnet/minecraft/util/Identifier;I)V",
        at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/ItemCooldownManager;entries:Ljava/util/Map;")
    )
    private void onSetCooldown(Identifier identifier, int i, CallbackInfo callbackinfo) {
        this.getStartTicks().put(identifier, this.tick);
        this.getTotalDurations().put(identifier, i);
    }

    private Map getStartTicks() {
        if (this.startTicks == null) {
            this.startTicks = new HashMap();
        }

        return this.startTicks;
    }

    public int getTotalTicks(ItemStack itemstack) {
        if (itemstack != null && !itemstack.isEmpty()) {
            Identifier identifier = this.getGroup(itemstack);
            Integer integer = (Integer)this.getTotalDurations().get(identifier);
            return integer != null ? integer : 0;
        } else {
            return 0;
        }
    }

    @Shadow
    public abstract boolean isCoolingDown(ItemStack itemstack);

    @Shadow
    public abstract Identifier getGroup(ItemStack itemstack);
}
