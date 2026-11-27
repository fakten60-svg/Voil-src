package lol.lwes.voil.module.miscellaneous;

import com.mojang.brigadier.suggestion.Suggestion;
import java.util.ArrayList;
import java.util.Random;
import lol.lwes.voil.event.CommandSuggestionsEvent;
import lol.lwes.voil.event.EventHandler;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.util.packet.PacketUtil;
import net.minecraft.network.packet.c2s.play.RequestCommandCompletionsC2SPacket;
import net.minecraft.network.packet.s2c.play.CommandSuggestionsS2CPacket;

public class Plugins extends Module {
    public Plugins() {
        super("Plugins", "Gets the plugins", Category.Miscellaneous);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (mc.player != null) {
            RequestCommandCompletionsC2SPacket requestcommandcompletionsc2spacket = new RequestCommandCompletionsC2SPacket(new Random().nextInt(200), "/");
            PacketUtil.sendPacket(requestcommandcompletionsc2spacket);
        }
    }

    @EventHandler
    public void onCmdSuggest(CommandSuggestionsEvent commandsuggestionsevent) {
        if (this.isEnabled()) {
            CommandSuggestionsS2CPacket commandsuggestionss2cpacket = commandsuggestionsevent.getPacket();
            ArrayList<String> arraylist = new ArrayList<>();

            for (Suggestion suggestion : commandsuggestionss2cpacket.getSuggestions().getList()) {
                String[] astring = suggestion.getText().split(":");
                if (astring.length > 1) {
                    String s = astring[0].replace("/", "");
                    if (!arraylist.contains(s)) {
                        arraylist.add(s);
                    }
                }
            }

            if (!arraylist.isEmpty()) {
                StringBuilder stringbuilder = new StringBuilder();

                for (String s4 : arraylist) {
                    if (s4.equalsIgnoreCase("itemeditor") || s4.equalsIgnoreCase("bettershulkerboxes")) {
                        stringbuilder.append("§c").append(s4).append(", §a");
                    } else if (!s4.equalsIgnoreCase("vulcan") && !s4.equalsIgnoreCase("ncp") && !s4.equalsIgnoreCase("grim") && !s4.equalsIgnoreCase("spartan")
                        )
                     {
                        stringbuilder.append(s4).append(", ");
                    } else {
                        stringbuilder.append("§b").append(s4).append(", §a");
                    }
                }

                String s3 = String.valueOf(stringbuilder);
                String s2 = "&7Plugins: &a" + s3;
                this.sendMessage(s2);
            } else {
                this.sendMessage("&cNo plugins found!");
            }

            this.toggle();
        }
    }
}
