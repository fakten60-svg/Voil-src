package lol.lwes.voil.input;

import java.util.ArrayList;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.config.ClientSettings;
import lol.lwes.voil.config.GuiTheme;
import lol.lwes.voil.event.EventHandler;
import lol.lwes.voil.event.KeyPressEvent;
import lol.lwes.voil.event.MousePressEvent;
import lol.lwes.voil.event.Render2DEvent;
import lol.lwes.voil.event.TickEvent;
import lol.lwes.voil.gui.clickgui.ClickGuiScreen;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.module.ModuleManager;
import lol.lwes.voil.module.miscellaneous.AntiBot;
import lol.lwes.voil.setting.KeybindSetting;
import lol.lwes.voil.util.CrashHandler;
import lol.lwes.voil.util.PlayerActivity;
import lol.lwes.voil.util.PlayerTargetUtil;
import lol.lwes.voil.util.world.EntityUtil;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity.RemovalReason;

public class KeybindHandler {

    @EventHandler
    private void onTick(TickEvent tickevent) {
        KeybindSetting.flushPressedKeys();
        if (VoilClient.mc.player != null) {
            for (Module module : ModuleManager.INSTANCE.getEnabledModules()) {
                try {
                    module.onTick();
                } catch (Throwable throwable) {
                    CrashHandler.handleModuleCrash(module, throwable);
                }
            }
        }
    }

    @EventHandler
    private void onRender(Render2DEvent render2devent) {
        if (VoilClient.mc.world != null && VoilClient.mc.player != null) {
            GuiTheme.reloadColors();
        }
    }

    @EventHandler
    private void onKeyPress(KeyPressEvent keypressevent) {
        if (VoilClient.mc.currentScreen == null && keypressevent.action == 1) {
            if (keypressevent.key == (Integer)ClientSettings.menuKeybind.getValue()) {
                String s = (String)ClientSettings.menuKeyMode.getValue();
                if (s.equalsIgnoreCase("Menu")) {
                    ClickGuiScreen.getInstance().prepareOpen();
                    VoilClient.mc.setScreen(ClickGuiScreen.getInstance());
                } else if (s.equalsIgnoreCase("Dropdown")) {
                    ClickGuiScreen.getInstance().prepareOpen();
                    VoilClient.mc.setScreen(ClickGuiScreen.getInstance());
                }
            }

            for (Module module : ModuleManager.INSTANCE.getModules()) {
                if (module.getKeyCode() == keypressevent.key) {
                    module.toggle();
                }
            }
        }
    }

    @EventHandler
    public void onUpdate(TickEvent tickevent) {
        if (VoilClient.mc.world != null && VoilClient.mc.player != null) {
            if (ModuleManager.INSTANCE.isEnabled(AntiBot.class)) {
                new ArrayList<>(VoilClient.mc.world.getPlayers())
                    .forEach(
                        abstractclientplayerentity -> {
                            if (abstractclientplayerentity != null && abstractclientplayerentity != VoilClient.mc.player) {
                                if (!PlayerTargetUtil.groundedPlayers.contains(abstractclientplayerentity)) {
                                    if (abstractclientplayerentity.hurtTime != 0) {
                                        PlayerTargetUtil.addBotFlag(abstractclientplayerentity, PlayerActivity.TOUCHED_GROUND);
                                    } else {
                                        PlayerTargetUtil.addBotFlag(abstractclientplayerentity, PlayerActivity.IN_AIR);
                                    }

                                    if (abstractclientplayerentity.handSwingTicks > 0) {
                                        PlayerTargetUtil.addBotFlag(abstractclientplayerentity, PlayerActivity.SWINGED);
                                    }

                                    int i;
                                    if (abstractclientplayerentity.getX() == abstractclientplayerentity.lastX
                                        && abstractclientplayerentity.getY() == abstractclientplayerentity.lastY) {
                                        double d0;
                                        i = (d0 = abstractclientplayerentity.getZ() - abstractclientplayerentity.lastZ) == 0.0 ? 0 : (d0 < 0.0 ? -1 : 1);
                                    } else {
                                        PlayerTargetUtil.addBotFlag(abstractclientplayerentity, PlayerActivity.MOVED);
                                        float f;
                                        i = (f = abstractclientplayerentity.getYaw() - abstractclientplayerentity.lastYaw) == 0.0F ? 0 : (f < 0.0F ? -1 : 1);
                                    }

                                    if (i != 0) {
                                        PlayerTargetUtil.addBotFlag(abstractclientplayerentity, PlayerActivity.ROTATED);
                                    }

                                    if (EntityUtil.getPing(abstractclientplayerentity) != 0) {
                                        PlayerTargetUtil.removeBotTracking(abstractclientplayerentity);
                                        PlayerTargetUtil.groundedPlayers.add(abstractclientplayerentity);
                                    } else {
                                        PlayerTargetUtil.trackPlayer(abstractclientplayerentity);
                                        if (AntiBot.ignoreNpcs.isEnabled()) {
                                            VoilClient.mc.world.removeEntity(abstractclientplayerentity.getId(), RemovalReason.KILLED);
                                        }
                                    }
                                }
                            }
                        }
                    );
            }
        }
    }

    @EventHandler
    private void onMousePress(MousePressEvent mousepressevent) {
        if (mousepressevent.isPress()) {
            if (VoilClient.mc.currentScreen == null) {
                if (!KeybindSetting.isAnyListening()) {
                    mousepressevent.getButton();

                    for (Module module : ModuleManager.INSTANCE.getModules()) {
                        if (module.getKeyCode() != 0 && module.getKeyCode() == 0) {
                            module.toggle();
                        }
                    }
                }
            }
        }
    }
}
