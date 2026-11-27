package lol.lwes.voil.hook;

import lol.lwes.voil.module.ModuleManager;
import lol.lwes.voil.module.client.Interface;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class ScreenHooks {
    private static final String INVENTORY_OPTION = "Inventory";

    public static void onRenderInGameBackground(Object object, CallbackInfo callbackinfo) {
        if (object instanceof InventoryScreen) {
            Interface iface = (Interface)ModuleManager.INSTANCE.getModule(Interface.class);
            if (iface != null && iface.isEnabled()) {
                String s = INVENTORY_OPTION;
                if (iface.changes.isSelected(s)) {
                    callbackinfo.cancel();
                }
            }
        }
    }
}
