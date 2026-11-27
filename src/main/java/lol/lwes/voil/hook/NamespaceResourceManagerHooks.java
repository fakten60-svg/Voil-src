package lol.lwes.voil.hook;

import java.util.Optional;
import lol.lwes.voil.util.render.TulipResourcePack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class NamespaceResourceManagerHooks {
    public static void onGetResource(Identifier identifier, CallbackInfoReturnable<Optional<Resource>> callbackinforeturnable) {
        if (!identifier.getNamespace().equals("tulip")) {
            return;
        }

        String location = "assets/tulip/" + identifier.getPath();
        ClassLoader loader = MinecraftClient.class.getClassLoader();
        if (loader.getResource(location) == null) {
            callbackinforeturnable.setReturnValue(Optional.empty());
            return;
        }

        callbackinforeturnable.setReturnValue(Optional.of(new Resource(new TulipResourcePack(), () -> loader.getResourceAsStream(location))));
    }
}
