package lol.lwes.voil.hook;

import java.awt.Color;
import java.util.Objects;
import lol.lwes.voil.module.ModuleManager;
import lol.lwes.voil.module.render.CustomSky;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class FogAlphaHooks {
    public static void onGetFogColor(CallbackInfoReturnable callbackinforeturnable) {
        Class<CustomSky> oclass = CustomSky.class;
        ModuleManager modulemanager = ModuleManager.INSTANCE;
        if (modulemanager.isEnabled(oclass)) {
            oclass = CustomSky.class;
            modulemanager = ModuleManager.INSTANCE;
            Color color = Objects.requireNonNull((CustomSky)modulemanager.getModule(oclass)).fogColor.getColor();
            callbackinforeturnable.setReturnValue(
                new Vector4f(color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, color.getAlpha() / 255.0F)
            );
        }
    }
}
