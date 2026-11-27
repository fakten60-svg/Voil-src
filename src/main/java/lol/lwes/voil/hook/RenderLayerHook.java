package lol.lwes.voil.hook;

import lol.lwes.voil.VoilClient;
import lol.lwes.voil.module.ModuleManager;
import lol.lwes.voil.module.render.CustomSky;
import lol.lwes.voil.util.render.SkyRenderer;
import net.minecraft.client.render.BlockRenderLayerGroup;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class RenderLayerHook {
    public static void cancelCelestialBodies(CallbackInfo callbackinfo) {
        if (isCustomSkyActive()) {
            callbackinfo.cancel();
        }
    }

    public static void cancelSkyDark(CallbackInfo callbackinfo) {
        if (isCustomSkyActive()) {
            callbackinfo.cancel();
        }
    }

    public static void cancelTopSky(CallbackInfo callbackinfo) {
        if (isCustomSkyActive()) {
            callbackinfo.cancel();
        }
    }

    public static void renderCustomSky(BlockRenderLayerGroup blockrenderlayergroup) {
        if (blockrenderlayergroup == BlockRenderLayerGroup.OPAQUE) {
            if (isCustomSkyActive()) {
                VoilClient.mc.getRenderTickCounter().getTickProgress(true);
                Camera camera = VoilClient.mc.gameRenderer.getCamera();
                SkyRenderer.instance.render(camera);
            }
        }
    }

    public static void cancelGlowingSky(CallbackInfo callbackinfo) {
        if (isCustomSkyActive()) {
            callbackinfo.cancel();
        }
    }

    private static boolean isCustomSkyActive() {
        return SkyRenderer.instance != null && SkyRenderer.instance.getShader() != null && ModuleManager.INSTANCE.isEnabled(CustomSky.class);
    }
}
