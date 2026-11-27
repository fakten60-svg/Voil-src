package lol.lwes.voil.module.client;

import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.ListSetting;
import lol.lwes.voil.setting.Setting;

public class Targets extends Module {
    public final BooleanSetting players = new BooleanSetting("Players", true, "Target other Players");
    public final BooleanSetting mobs = new BooleanSetting("Mobs", false, "Target Monsters");
    public final BooleanSetting animals = new BooleanSetting("Animals", false, "Target Animals");
    public final BooleanSetting crystals = new BooleanSetting("Crystals", false, "Target Ender Crystals");
    public final BooleanSetting friends = new BooleanSetting("Friends", false, "Target your Friends!");
    public final BooleanSetting teammates = new BooleanSetting("Teammates", false, "Target your Teammates!");
    public final BooleanSetting ignoreInvisible = new BooleanSetting(
        "Ignore Invisible", false, "Ignore completely invisible entities (entities with armor/items will be targeted!)"
    );
    public final BooleanSetting ignoreBlocked = new BooleanSetting("Ignore Blocked", false, "Ignore entities behind blocks");
    public final ListSetting targetSort = new ListSetting(
            "Target Sort", "Sorts targets by distance, health, or armor", new String[]{"Distance", "Health", "Armor"}
        )
        .addDefaultEnabled("Distance", 16.0);

    public Targets() {
        super("Targets", "Manages who you want to attack", Category.Client);
        Setting[] asetting = new Setting[]{
            this.targetSort, this.players, this.mobs, this.animals, this.crystals, this.friends, this.teammates, this.ignoreInvisible, this.ignoreBlocked
        };
        this.addSettings(asetting);
    }

    @Override
    public void setEnabled(boolean flag) {
        if (!flag) {
            super.setEnabled(false);
        }
    }
}
