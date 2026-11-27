package lol.lwes.voil.mixin;

import lol.lwes.voil.hook.ResourceLoadHook;
import net.minecraft.resource.LifecycledResourceManagerImpl;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LifecycledResourceManagerImpl.class)
public abstract class LifecycledResourceManagerImplMixin {
    @Inject(method = "getResource", at = @At("HEAD"), cancellable = true)
    private void onGetResource(Identifier identifier, CallbackInfoReturnable callbackinforeturnable) {
        ResourceLoadHook.onGetResource(identifier, callbackinforeturnable);
    }
}
