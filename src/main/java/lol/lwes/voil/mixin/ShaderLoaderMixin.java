package lol.lwes.voil.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import java.util.Map;
import lol.lwes.voil.hook.ShaderResourceHook;
import net.minecraft.client.gl.ShaderLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShaderLoader.class)
public abstract class ShaderLoaderMixin {
    @Inject(
        method = "prepare(Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/profiler/Profiler;)Lnet/minecraft/client/gl/ShaderLoader$Definitions;",
        at = @At(value = "INVOKE", target = "Ljava/util/Map;entrySet()Ljava/util/Set;", ordinal = 0)
    )
    private void onPrepare(ResourceManager resourcemanager, Profiler profiler, CallbackInfoReturnable callbackinforeturnable, @Local Map map) {
        ShaderResourceHook.onFindResources(resourcemanager, map);
    }
}
