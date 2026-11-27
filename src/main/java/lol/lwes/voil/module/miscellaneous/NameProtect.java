package lol.lwes.voil.module.miscellaneous;

import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.ModeSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.setting.StringSetting;
import net.minecraft.entity.player.PlayerEntity;

public class NameProtect extends Module {
    public static BooleanSetting protectName = new BooleanSetting("Obfuscate Enemy Name", false, "Obfuscate enemy name in distance");
    public static StringSetting replacementName = new StringSetting("Obfuscated Enemy Name", "Voil_Victim", "Obfuscated enemy name");
    public static BooleanSetting protectSkin = new BooleanSetting("Obfuscate Skin", false, "Obfuscate skin into steve");
    public static StringSetting skinName = new StringSetting("Skin Name", "Steve", "Which minecraft user to get skin from");
    public static ModeSetting skinMode = new ModeSetting("Obfuscate Skin for", "Self", "Whos skin to obfuscate", new String[]{"All", "Enemy", "Self"});
    public static StringSetting fallbackName = new StringSetting("Name", "VoilUser", "Your name");
    public static boolean active = false;

    public NameProtect() {
        super("NameProtect", "Protects your name", Category.Miscellaneous);
        Setting[] asetting = new Setting[]{protectName, replacementName, protectSkin, skinName, skinMode, fallbackName};
        this.addSettings(asetting);
        Setting.dependSetting(skinMode, protectSkin);
        Setting.dependSetting(replacementName, protectName);
        Setting.dependSetting(skinName, protectSkin);
    }

    @Override
    public void onDisable() {
        active = false;
        super.onDisable();
    }

    @Override
    public void onEnable() {
        active = true;
        super.onEnable();
    }

    public static String protectText(String s) {
        if (s != null && active && mc.player != null && mc.world != null) {
            String s1 = mc.getSession().getUsername();
            if (s1 != null && !s1.isEmpty() && s.contains(s1)) {
                s = s.replace(s1, fallbackName.getValue());
            }

            if (protectName.isEnabled()) {
                double d0 = mc.options.getViewDistance().getValue().intValue() * 16.0;
                double d1 = d0 * d0;

                for (PlayerEntity playerentity : mc.world.getPlayers()) {
                    String s2 = playerentity.getGameProfile().name();
                    if (s2 != null && !s2.equals(s1) && s.contains(s2) && mc.player.squaredDistanceTo(playerentity) <= d1) {
                        s = s.replace(s2, replacementName.getValue());
                    }
                }
            }

            return s;
        } else {
            return s;
        }
    }
}
