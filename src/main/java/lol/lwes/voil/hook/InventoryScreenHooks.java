package lol.lwes.voil.hook;

import lol.lwes.voil.VoilClient;
import lol.lwes.voil.mixin.accessor.HandledScreenAccessor;
import lol.lwes.voil.module.ModuleManager;
import lol.lwes.voil.module.client.Interface;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class InventoryScreenHooks {
    private static final String INVENTORY_OPTION = "Inventory";
    private static final int BACKGROUND_WIDTH = 176;
    private static final int BACKGROUND_HEIGHT = 166;
    private static final int PANEL_COLOR = 0xB0101014;
    private static final int OUTLINE_COLOR = 0x40FFFFFF;
    private static final int SLOT_COLOR = 0x50000000;

    public static void onDrawBackground(Object screen, DrawContext context, float delta, int mouseX, int mouseY, CallbackInfo callbackInfo) {
        if (!(screen instanceof InventoryScreen inventoryScreen) || !isCustomInventoryEnabled()) {
            return;
        }

        callbackInfo.cancel();
        HandledScreenAccessor accessor = (HandledScreenAccessor) inventoryScreen;
        int x = accessor.getX();
        int y = accessor.getY();

        context.fill(x, y, x + BACKGROUND_WIDTH, y + BACKGROUND_HEIGHT, PANEL_COLOR);
        context.drawStrokedRectangle(x, y, BACKGROUND_WIDTH, BACKGROUND_HEIGHT, OUTLINE_COLOR);

        for (Slot slot : inventoryScreen.getScreenHandler().slots) {
            if (slot.isEnabled()) {
                int slotX = x + slot.x;
                int slotY = y + slot.y;
                context.fill(slotX - 1, slotY - 1, slotX + 17, slotY + 17, SLOT_COLOR);
            }
        }

        if (VoilClient.mc.player != null) {
            InventoryScreen.drawEntity(context, x + 26, y + 8, x + 75, y + 78, 30, 0.0625F, mouseX, mouseY, VoilClient.mc.player);
        }
    }

    public static void onDrawForeground(DrawContext context, int mouseX, int mouseY, CallbackInfo callbackInfo) {
        if (isCustomInventoryEnabled()) {
            callbackInfo.cancel();
        }
    }

    private static boolean isCustomInventoryEnabled() {
        Interface iface = (Interface) ModuleManager.INSTANCE.getModule(Interface.class);
        return iface != null && iface.isEnabled() && iface.changes.isSelected(INVENTORY_OPTION);
    }
}
