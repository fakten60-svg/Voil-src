package lol.lwes.voil.module.combat;

import java.util.concurrent.ThreadLocalRandom;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.mixin.accessor.MinecraftClientAccessor;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.ModeSetting;
import lol.lwes.voil.setting.RangeSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.input.KeyUtil;

public class AutoClicker extends Module {
    private RangeSetting cps = new RangeSetting("CPS", 1.0, 20.0, 7.0, 14.0, 1.0, "Clicks per second");
    private ModeSetting randomness = new ModeSetting("Randomness", "Spike", "Clicking mode", new String[]{"None", "Spike"});
    private ModeSetting clickType = new ModeSetting("ClickType", "Left", "Click type", new String[]{"Left", "Right"});
    private BooleanSetting holdOnly = new BooleanSetting("Hold Only", false, "Only clicks when holding down the button");
    private BooleanSetting butterfly = new BooleanSetting("Butterfly", false, "Uses butterfly clicking");
    private long nextClickTime = 0L;
    private boolean butterflyToggle = false;

    public AutoClicker() {
        super("AutoClicker", "Automatically clicks for you", Category.Combat);
        Setting[] asetting = new Setting[]{this.cps, this.randomness, this.clickType, this.holdOnly, this.butterfly};
        this.addSettings(asetting);
    }

    private long getClickDelay() {
        double d0 = this.cps.getMin();
        double d1 = this.cps.getMax();
        double d2 = this.cps.getRandomValue();
        if (this.randomness.is("Spike")) {
            d2 = ThreadLocalRandom.current().nextDouble(d0, d1);
        }

        return (long)(1000.0 / d2);
    }

    @Override
    public void onTick() {
        if (mc.player != null && mc.world != null) {
            if (mc.currentScreen == null) {
                if (System.currentTimeMillis() >= this.nextClickTime) {
                    if (this.holdOnly.isEnabled()) {
                        if (this.clickType.is("Left") && !KeyUtil.isKeyDown(0)) {
                            return;
                        }

                        if (this.clickType.is("Right") && !KeyUtil.isKeyDown(1)) {
                            return;
                        }
                    }

                    if (this.butterfly.isEnabled()) {
                        if (this.butterflyToggle) {
                            this.click(this.clickType.is("Left") ? 0 : 1);
                        } else {
                            this.click(this.clickType.is("Left") ? 0 : 1);
                        }

                        this.butterflyToggle = !this.butterflyToggle;
                    } else {
                        if (this.clickType.is("Left")) {
                            ((MinecraftClientAccessor)mc).invokeDoAttack();
                        } else if (this.clickType.is("Right")) {
                            ((MinecraftClientAccessor)mc).invokeDoItemUse();
                        }

                        this.click(this.clickType.is("Left") ? 0 : 1);
                    }

                    this.nextClickTime = System.currentTimeMillis() + this.getClickDelay();
                }
            }
        }
    }

    private void click(int i) {
        VoilClient.getMouseSimulator().clickButton(i);
    }
}
