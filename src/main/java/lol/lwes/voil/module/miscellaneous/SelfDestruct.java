package lol.lwes.voil.module.miscellaneous;

import java.util.Iterator;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.module.ModuleManager;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.ModUnloader;

public class SelfDestruct extends Module {
    BooleanSetting clearChat = new BooleanSetting("Clear Chat", true, "Clears the chat after self-destructing");

    public SelfDestruct() {
        super("SelfDestruct", "Removes voil from your game", Category.Miscellaneous);
        Setting[] asetting = new Setting[]{this.clearChat};
        this.addSettings(asetting);
    }

    private void clearChatHistory() {
        mc.inGameHud.getChatHud().clear(true);
    }

    private void unloadClasses() throws InterruptedException {
        ModUnloader.unload("tulip");

        System.gc();
    }

    @Override
    public void onEnable() {
        VoilClient voilclient = VoilClient.getInstance();
        voilclient.disabled = true;
        if (mc.currentScreen != null) {
            mc.currentScreen.close();
        }

        this.resetAllModules();

        try {
            this.unloadClasses();
        } catch (InterruptedException interruptedexception) {
            throw new RuntimeException(interruptedexception);
        }
    }

    private void resetAllModules() {
        for (Module module : ModuleManager.INSTANCE.getModules()) {
            module.setEnabled(false);
            module.setDescription("");
            module.setName("");
            module.setCategory(null);
            module.wipeStrings();
            Iterator iterator = module.getSettings().iterator();

            while (iterator.hasNext()) {
                ((Setting)iterator.next()).a();
            }
        }
    }
}
