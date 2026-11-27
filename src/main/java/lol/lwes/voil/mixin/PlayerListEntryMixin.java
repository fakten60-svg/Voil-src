package lol.lwes.voil.mixin;

import com.mojang.authlib.GameProfile;
import lol.lwes.voil.hook.PlayerListEntryHooks;
import net.minecraft.client.network.PlayerListEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerListEntry.class)
public abstract class PlayerListEntryMixin {
    @Inject(
        method = "getSkinTextures",
        at = @At("HEAD"),
        cancellable = true
    )
    private void obfuscate(CallbackInfoReturnable callbackinforeturnable) {
        PlayerListEntryHooks.obfuscate(this.getProfile(), callbackinforeturnable);
    }

    @Shadow
    public abstract GameProfile getProfile();
}
