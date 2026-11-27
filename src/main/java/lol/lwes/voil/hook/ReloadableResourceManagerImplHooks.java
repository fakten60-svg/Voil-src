package lol.lwes.voil.hook;

import java.util.Optional;
import lol.lwes.voil.util.render.TulipResourcePack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class ReloadableResourceManagerImplHooks {
    public static void onGetResource(Identifier identifier, CallbackInfoReturnable callbackinforeturnable) {
        if (identifier.getNamespace().equals("tulip")) {
            callbackinforeturnable.setReturnValue(Optional.of(new Resource(new TulipResourcePack(), () -> {
                String s2 = identifier.getPath();
                String s = "assets/tulip/" + s2;
                return MinecraftClient.class.getClassLoader().getResourceAsStream(s);
            })));
        }
    }
}
