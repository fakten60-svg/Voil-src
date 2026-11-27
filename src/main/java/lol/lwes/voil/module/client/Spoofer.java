package lol.lwes.voil.module.client;

import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.setting.StringSetting;

public class Spoofer extends Module {
    public StringSetting brand = new StringSetting("Brand", "Fabric", "What to spoof as");
    public final BooleanSetting resourcePack = new BooleanSetting("Resource Pack", false, "Spoof resource pack");

    public Spoofer() {
        super("Spoofer", "Spoofs your client", Category.Client);
        Setting[] asetting = new Setting[]{this.resourcePack, this.brand};
        this.addSettings(asetting);
    }
}
