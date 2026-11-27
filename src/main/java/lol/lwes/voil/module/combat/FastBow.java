package lol.lwes.voil.module.combat;

import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.NumberSetting;
import lol.lwes.voil.setting.Setting;
import net.minecraft.item.BowItem;
import net.minecraft.util.Hand;

public class FastBow extends Module {
    private final NumberSetting bowDelay = new NumberSetting("Bow Delay", 1.0, 5.0, 3.0, 1.0, "Delay between bow shots in ticks");
    private final NumberSetting minPower = new NumberSetting("Min Power", 0.2F, 1.0, 0.5, 0.1F, "Delay between bow shots in ticks");
    private int tickCounter = 0;

    public FastBow() {
        super("FastBow", "Allows you to shoot arrows faster with bows", Category.Combat);
        Setting[] asetting = new Setting[]{this.bowDelay, this.minPower};
        this.addSettings(asetting);
    }

    @Override
    public void onTick() {
        if (mc.player != null && mc.world != null) {
            if (mc.player.getMainHandStack().getItem() instanceof BowItem) {
                if (mc.options.useKey.isPressed()) {
                    if (!mc.player.isUsingItem()) {
                        mc.player.setCurrentHand(Hand.MAIN_HAND);
                        this.tickCounter = 0;
                    } else {
                        int i = this.tickCounter++;
                        if (mc.player.getItemUseTime() / 20.0F >= this.minPower.getFloat() && this.tickCounter >= this.bowDelay.getInt()) {
                            mc.interactionManager.stopUsingItem(mc.player);
                            this.tickCounter = 0;
                        }
                    }
                } else {
                    this.tickCounter = 0;
                }
            }
        }
    }
}
