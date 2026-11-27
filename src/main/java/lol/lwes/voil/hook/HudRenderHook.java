package lol.lwes.voil.hook;

import lol.lwes.voil.VoilClient;
import lol.lwes.voil.duck.MatrixStackProviderAccessor;
import lol.lwes.voil.event.EventBus;
import lol.lwes.voil.event.Render2DEvent;
import lol.lwes.voil.module.ModuleManager;
import lol.lwes.voil.module.client.Interface;
import lol.lwes.voil.module.client.OBSBypass;
import lol.lwes.voil.util.render.RenderManager;
import net.minecraft.client.gui.DrawContext;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class HudRenderHook {
    private static boolean hotbarDrawn = false;

    public static void renderHudOverlay(DrawContext drawcontext, CallbackInfo callbackinfo) {
        Interface iface = (Interface)ModuleManager.INSTANCE.getModule(Interface.class);
        if (iface != null && iface.isEnabled() && iface.changes.isSelected("Hotbar")) {
            if (hotbarDrawn) {
                if (!ModuleManager.INSTANCE.isEnabled(OBSBypass.class)) {
                    callbackinfo.cancel();
                }
            } else {
                hotbarDrawn = true;
            }

            iface.renderHotbar();
        }
    }

    public static void onRenderHud(DrawContext drawcontext, CallbackInfo callbackinfo) {
        if (!VoilClient.instance.disabled) {
            RenderManager.setDrawContext(drawcontext);
            VoilClient.getScriptManager().dispatchEvent("onRenderTwoD");
            EventBus eventbus = VoilClient.eventBus;
            Render2DEvent render2devent = new Render2DEvent(drawcontext,((MatrixStackProviderAccessor)drawcontext).tulip$getMatrices(), 0, 0);
            if (((Render2DEvent)eventbus.post(render2devent)).isCancelled()) {
                callbackinfo.cancel();
            }
        }
    }
}
