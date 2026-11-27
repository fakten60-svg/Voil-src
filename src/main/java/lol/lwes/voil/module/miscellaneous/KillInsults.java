package lol.lwes.voil.module.miscellaneous;

import lol.lwes.voil.event.AttackEntityPreEvent;
import lol.lwes.voil.event.EventHandler;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.ModeSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.setting.StringSetting;
import lol.lwes.voil.util.ChatUtil;
import net.minecraft.entity.player.PlayerEntity;

public class KillInsults extends Module {
    StringSetting message = new StringSetting("Message", "Ez", "Message to send when you are kill them");
    ModeSetting mode = new ModeSetting("Mode", "Msg", "Mode to use", new String[]{"Public Chat", "Msg"});
    PlayerEntity lastTarget;
    boolean messageSent;

    public KillInsults() {
        super("KillInsults", "Sends messages when you kill them", Category.Miscellaneous);
        Setting[] asetting = new Setting[]{this.message, this.mode};
        this.addSettings(asetting);
    }

    @Override
    public void onTick() {
        if (mc.world != null && this.lastTarget != null) {
            if (this.lastTarget.isDead() && !this.messageSent) {
                switch (this.mode.getMode()) {
                    case "Public Chat" -> {
                        String s9 = this.lastTarget.getName().getString();
                        String s2 = this.message.getValue();
                        ChatUtil.send(s9 + " " + s2);
                    }
                    case "Msg" -> {
                        String s6 = this.lastTarget.getName().getString();
                        String s10 = this.message.getValue();
                        ChatUtil.whisper(s6, s10);
                    }
                    default -> {
                    }
                }

                this.messageSent = true;
            }
        }
    }

    @EventHandler
    public void onAttack(AttackEntityPreEvent attackentitypreevent) {
        if (mc.world != null && attackentitypreevent.getTarget() != null) {
            if (attackentitypreevent.getTarget() instanceof PlayerEntity) {
                this.lastTarget = (PlayerEntity)attackentitypreevent.getTarget();
                this.messageSent = false;
            }
        }
    }
}
