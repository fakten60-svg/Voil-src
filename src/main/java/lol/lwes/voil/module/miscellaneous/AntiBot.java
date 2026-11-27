package lol.lwes.voil.module.miscellaneous;

import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.Setting;

public class AntiBot extends Module {
    public static BooleanSetting ignoreNpcs = new BooleanSetting("Remove", false, "Kills bots so you wont see them");

    public AntiBot() {
        super("AntiBot", "Ignores NPCS", Category.Miscellaneous);
        Setting[] asetting = new Setting[]{ignoreNpcs};
        this.addSettings(asetting);
    }
}
