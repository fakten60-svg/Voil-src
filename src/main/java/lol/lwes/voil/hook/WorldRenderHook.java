package lol.lwes.voil.hook;

import dev.prestigeclient.renderlib.impl.api.RenderAPI;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.module.ModuleManager;
import lol.lwes.voil.util.CrashHandler;
import lol.lwes.voil.util.render.RenderManager;
import lol.lwes.voil.util.render.RenderSpace;
import lol.lwes.voil.util.rotation.RotationManager;
import org.joml.Matrix4f;

public abstract class WorldRenderHook {
    public static void onRenderWorld(Matrix4f matrix4f) {
        RenderManager.beginWorld(matrix4f);
        RotationManager rotationmanager = VoilClient.getRotationManager();
        RenderAPI.RenderContext renderapi$rendercontext = RenderManager.renderContext;
        rotationmanager.updateCameraAngles(renderapi$rendercontext);
        VoilClient.getMouseSimulator().tick();

        for (Module module : ModuleManager.INSTANCE.getEnabledModules()) {
            try {
                module.onWorldRender(RenderManager.renderContext);
            } catch (Throwable throwable) {
                CrashHandler.handleModuleCrash(module, throwable);
            }
        }

        RenderManager.endFrame(RenderSpace.WORLD);
    }
}
