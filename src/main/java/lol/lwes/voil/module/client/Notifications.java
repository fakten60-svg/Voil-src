package lol.lwes.voil.module.client;

import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.ModeSetting;
import lol.lwes.voil.setting.Setting;

public class Notifications extends Module {
    public static ModeSetting position = new ModeSetting(
        "Position", "TOP_LEFT", "Position of the notification", new String[]{"TOP_LEFT", "TOP_RIGHT", "BOTTOM_LEFT", "BOTTOM_RIGHT"}
    );
    public static ModeSetting style = new ModeSetting("Style", "Skeet", "Style of the notification", new String[]{"Solid", "Skeet", "Voil"});

    public Notifications() {
        super("Notifications", "Sends notification when you turn on and off a module", Category.Client);
        Setting[] asetting = new Setting[]{style, position};
        this.addSettings(asetting);
    }
}
