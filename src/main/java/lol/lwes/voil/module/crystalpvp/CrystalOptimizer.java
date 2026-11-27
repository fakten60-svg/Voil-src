package lol.lwes.voil.module.crystalpvp;

import lol.lwes.voil.event.EventHandler;
import lol.lwes.voil.event.PacketSendEvent;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.module.crystalpvp.OptimizerAttackHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;

public class CrystalOptimizer extends Module {
    public CrystalOptimizer() {
        super("CrystalOptimizer", "Breaks crystals client side", Category.CrystalPvP);
    }

    public static MinecraftClient getClient() {
        return mc;
    }

    public static MinecraftClient getClient2() {
        return mc;
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    public boolean isTool(ItemStack itemstack) {
        return itemstack.contains(DataComponentTypes.TOOL) && !(itemstack.getItem() instanceof HoeItem);
    }

    public static MinecraftClient getClient3() {
        return mc;
    }

    public static MinecraftClient getClient4() {
        return mc;
    }

    @EventHandler
    public void onPacket(PacketSendEvent packetsendevent) {
        Packet packet = packetsendevent.getPacket();
        if (packet instanceof PlayerInteractEntityC2SPacket) {
            ((PlayerInteractEntityC2SPacket)packet).handle(new OptimizerAttackHandler(this));
        }
    }
}
