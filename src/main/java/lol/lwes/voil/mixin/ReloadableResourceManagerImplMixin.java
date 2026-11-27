package lol.lwes.voil.mixin;

import lol.lwes.voil.hook.ReloadableResourceManagerImplHooks;
import net.minecraft.resource.ReloadableResourceManagerImpl;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ReloadableResourceManagerImpl.class)
public abstract class ReloadableResourceManagerImplMixin {
    @Inject(method = "getResource", at = @At("HEAD"), cancellable = true)
    private void onGetResource(Identifier identifier, CallbackInfoReturnable callbackinforeturnable) {
        ReloadableResourceManagerImplHooks.onGetResource(identifier, callbackinforeturnable);
    }
}
