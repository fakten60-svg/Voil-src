package lol.lwes.voil.hook;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import lol.lwes.voil.util.render.TulipResources;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class ResourceManagerHooks {
    public static void onFindResources(String prefix, Predicate<Identifier> filter, CallbackInfoReturnable<Map<Identifier, Resource>> callback) {
        Map<Identifier, Resource> cache = TulipResources.getCache();
        if (cache == null || cache.isEmpty()) {
            return;
        }

        Map<Identifier, Resource> result = new HashMap<>(callback.getReturnValue());
        String directory = prefix + "/";
        for (Map.Entry<Identifier, Resource> entry : cache.entrySet()) {
            if (entry.getKey().getPath().startsWith(directory) && filter.test(entry.getKey())) {
                result.put(entry.getKey(), entry.getValue());
            }
        }

        callback.setReturnValue(result);
    }
}
