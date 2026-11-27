package lol.lwes.voil.module.client;

import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.Setting;

public class Vlippy extends Module {
    public static BooleanSetting showMascot = new BooleanSetting("TTS", false, "Makes vlippy talk");

    public Vlippy() {
        super("Vlippy", "Our cute mascot for voil", Category.Client);
        Setting[] asetting = new Setting[]{showMascot};
        this.addSettings(asetting);
    }
}
