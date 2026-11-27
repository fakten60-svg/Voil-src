package lol.lwes.voil.module.client;

import lol.lwes.voil.VoilClient;
import lol.lwes.voil.event.AttackEntityPostEvent;
import lol.lwes.voil.event.EventHandler;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.ModeSetting;
import lol.lwes.voil.setting.Setting;

public class Sounds extends Module {
    public static BooleanSetting onItemUseSound = new BooleanSetting("OnItemUse", true, "Simulates mouse sounds when you use an item");
    public static BooleanSetting onAttackSound = new BooleanSetting("OnAttack", true, "Simulates mouse sounds when you attack");
    BooleanSetting hitSound = new BooleanSetting("HitSound", true, "Simulates hit sounds when you attack");
    ModeSetting soundMode = new ModeSetting("SoundMode", "Skeet", "Mode for sounds", new String[]{"othersoon", "Skeet"});

    public Sounds() {
        super("Sounds", "Makes sounds", Category.Client);
        Setting[] asetting = new Setting[]{onItemUseSound, onAttackSound, this.hitSound, this.soundMode};
        this.addSettings(asetting);
    }

    @EventHandler
    public void onAttack(AttackEntityPostEvent attackentitypostevent) {
        if (this.hitSound.isEnabled()) {
            switch (this.soundMode.getMode()) {
                case "Skeet" -> VoilClient.getSoundPlayer().playSound("skeet", 1);
                default -> {
                }
            }
        }
    }
}
