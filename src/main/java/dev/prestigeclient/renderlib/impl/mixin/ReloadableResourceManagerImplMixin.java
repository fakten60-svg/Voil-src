package dev.prestigeclient.renderlib.impl.mixin;

import dev.prestigeclient.renderlib.impl.core.BlossomResourceLookup;
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
        if (identifier.getNamespace().equals("blossom")) {
            callbackinforeturnable.setReturnValue(BlossomResourceLookup.resolve(identifier));
        }
    }
}
