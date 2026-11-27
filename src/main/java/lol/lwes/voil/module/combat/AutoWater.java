package lol.lwes.voil.module.combat;

import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.module.player.NoSlow;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.inventory.InventoryUtil;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;

public class AutoWater extends Module {
    private BooleanSetting waterInsideWeb = new BooleanSetting("Water inside web", false, "Automatically places water inside webs");
    private int cooldown = 0;
    private final MinecraftClient mc = MinecraftClient.getInstance();

    public AutoWater() {
        super("AutoWater", "Automatically places water when your on fire", Category.Combat);
        Setting[] asetting = new Setting[]{this.waterInsideWeb};
        this.addSettings(asetting);
    }

    @Override
    public void onTick() {
        if (this.isEnabled()) {
            if (!this.mc.player.hasStatusEffect(StatusEffects.FIRE_RESISTANCE)) {
                if (this.mc.player.isOnFire() || this.waterInsideWeb.isEnabled() && NoSlow.isBlockInBox(this.mc.player.getBoundingBox(), Blocks.COBWEB)) {
                    int i = this.cooldown++;

                    try {
                        this.mc.player.setPitch(90.0F);
                        if (InventoryUtil.hasItemInHotbar(Items.WATER_BUCKET)) {
                            this.mc
                                .getNetworkHandler()
                                .sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0, this.mc.player.getYaw(), this.mc.player.getPitch()));
                        }
                    } catch (Exception exception) {
                        throw new RuntimeException(exception);
                    }
                } else {
                    this.cooldown = 0;
                }
            }
        }
    }
}
