package dev.prestigeclient.renderlib.impl.mixin;

import dev.prestigeclient.renderlib.impl.core.BlossomResources;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Map;
import java.util.function.Predicate;
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
        Map<Identifier, Resource> map = BlossomResources.getCache();
        if (!map.isEmpty()) {
            Map<Identifier, Resource> returnValue = callbackinforeturnable.getReturnValue();
            HashMap<Identifier, Resource> hashmap = returnValue == null ? new HashMap<>() : new HashMap<>(returnValue);

            String s1 = s + "/";

            for (Entry<Identifier, Resource> entry : map.entrySet()) {
                if (entry.getKey().getPath().startsWith(s1) && predicate.test(entry.getKey())) {
                    hashmap.put(entry.getKey(), entry.getValue());
                }
            }

            callbackinforeturnable.setReturnValue(hashmap);
        }
    }
}
