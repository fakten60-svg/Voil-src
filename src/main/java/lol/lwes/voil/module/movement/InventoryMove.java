package lol.lwes.voil.module.movement;

import lol.lwes.voil.event.EventHandler;
import lol.lwes.voil.event.InputEvent;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.Setting;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.ingame.BookScreen;
import net.minecraft.client.gui.screen.ingame.SignEditScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

public class InventoryMove extends Module {
    BooleanSetting shift = new BooleanSetting("Shift", false, "Be able to shift in GUIs");
    BooleanSetting stopSprint = new BooleanSetting("Stop Sprint", true, "Be able to shift in GUIs");
    private boolean moving;

    public InventoryMove() {
        super("InventoryMove", "Lets you move when inventory is open", Category.Movement);
        Setting[] asetting = new Setting[]{this.shift, this.stopSprint};
        this.addSettings(asetting);
    }

    @Override
    public void onTick() {
        boolean flag = this.isChatOpen();
        if (!flag) {
            if (!this.moving) {
                if (this.stopSprint.isEnabled()) {
                    Sprint.setSprintBlocked(false);
                }

                return;
            }

            this.moving = false;
        } else {
            this.moving = true;
        }

        for (KeyBinding keybinding : new KeyBinding[]{
            mc.options.forwardKey, mc.options.backKey, mc.options.leftKey, mc.options.rightKey, mc.options.jumpKey, mc.options.sprintKey
        }) {
            keybinding.setPressed(InputUtil.isKeyPressed(mc.getWindow(), InputUtil.fromTranslationKey(keybinding.getBoundKeyTranslationKey()).getCode()));
        }

        if (this.shift.isEnabled()) {
            mc.options
                .sneakKey
                .setPressed(InputUtil.isKeyPressed(mc.getWindow(), InputUtil.fromTranslationKey(mc.options.sneakKey.getBoundKeyTranslationKey()).getCode()));
        }

        if (this.stopSprint.isEnabled()) {
            mc.player.setSprinting(false);
            Sprint.setSprintBlocked(true);
        }
    }

    @Override
    public void onDisable() {
        Sprint.setSprintBlocked(false);
    }

    @Override
    public void onEnable() {
        this.moving = false;
    }

    private boolean isChatOpen() {
        return mc.currentScreen != null
            && !(mc.currentScreen instanceof ChatScreen)
            && !(mc.currentScreen instanceof SignEditScreen)
            && !(mc.currentScreen instanceof BookScreen);
    }

    @EventHandler
    private void onInput(InputEvent inputevent) {
        if (inputevent.localPlayer) {
            if (this.stopSprint.isEnabled() && this.moving) {
                inputevent.input.playerInput.sprint = false;
            }
        }
    }
}
