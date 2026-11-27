package lol.lwes.voil.module.player;

import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.KeybindSetting;
import lol.lwes.voil.setting.NumberSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.TickFastForward;
import lol.lwes.voil.util.TickRequest;

public class TickBase extends Module {
    private final KeybindSetting key = new KeybindSetting("Key", 0, false, "Key to activate tickbase");
    private final NumberSetting chargeTicks = new NumberSetting("Charge Ticks", 1.0, 20.0, 5.0, 1.0, "Ticks to charge");
    private final NumberSetting releaseTicks = new NumberSetting("Release Ticks", 1.0, 20.0, 5.0, 1.0, "Minimum ticks to release");
    private final BooleanSetting packetFix = new BooleanSetting("Packet Fix", true, "Prevent packets from exposing that you're tick basing and not frozen");

    public TickBase() {
        super("TickBase", "Holds ticks", Category.Player);
        Setting[] asetting = new Setting[]{this.key, this.chargeTicks, this.releaseTicks, this.packetFix};
        this.addSettings(asetting);
    }

    @Override
    public void onRender2D() {
        if (this.key.isPressed()) {
            int i = this.releaseTicks.getInt();
            TickFastForward.requestTicks(new TickRequest(i, this.chargeTicks.getInt(), this.packetFix.isEnabled()));
        }
    }

    @Override
    public void onDisable() {
        TickFastForward.requestTicks(null);
    }
}
