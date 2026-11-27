package lol.lwes.voil.module.client;

import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.ModeSetting;
import lol.lwes.voil.setting.Setting;

public class MoveFix extends Module {
    public static ModeSetting mode = new ModeSetting("MoveFix", "Silent", "Fixes the movement of the player", new String[]{"None", "Silent", "Strict"});

    public MoveFix() {
        super("MoveFix", "Fixes the movement of the player", Category.Client);
        Setting[] asetting = new Setting[]{mode};
        this.addSettings(asetting);
    }

    @Override
    public void setEnabled(boolean flag) {
        super.setEnabled(false);
    }
}
