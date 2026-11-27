package lol.lwes.voil.module.combat;

import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.MultiSelectSetting;
import lol.lwes.voil.setting.NumberSetting;
import lol.lwes.voil.setting.Setting;

public class Reach extends Module {
    public final NumberSetting entityReach = new NumberSetting("Entity Reach", 1.0, 6.0, 4.0, 0.1, "Entity reach distance");
    public final NumberSetting blockReach = new NumberSetting("Block Reach", 1.0, 6.0, 4.0, 0.1, "Block reach distance");
    public final MultiSelectSetting mode;

    public Reach() {
        super("Reach", "Extends your reach", Category.Combat);
        String[] astring = new String[]{"Entity Interactions", "Block Interactions"};
        this.mode = new MultiSelectSetting("Mode", "What interactions to extend", astring);
        Setting[] asetting = new Setting[]{this.mode, this.entityReach, this.blockReach};
        this.addSettings(asetting);
        Setting.dependSetting(this.entityReach, "Entity Interactions", this.mode);
        Setting.dependSetting(this.blockReach, "Block Interactions", this.mode);
    }
}
