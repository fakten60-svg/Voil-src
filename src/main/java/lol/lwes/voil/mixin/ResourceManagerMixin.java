package lol.lwes.voil.mixin;

import java.util.Map;
import java.util.function.Predicate;
import lol.lwes.voil.hook.ResourceManagerHooks;
import net.minecraft.resource.NamespaceResourceManager;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NamespaceResourceManager.class)
public abstract class ResourceManagerMixin {
    @Inject(method = "findResources", at = @At("RETURN"), cancellable = true)
    private void onFindResources(String s, Predicate<Identifier> predicate, CallbackInfoReturnable<Map<Identifier, Resource>> callbackinforeturnable) {
        ResourceManagerHooks.onFindResources(s, predicate, callbackinforeturnable);
    }
}
