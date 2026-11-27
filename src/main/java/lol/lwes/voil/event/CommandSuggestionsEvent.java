package lol.lwes.voil.event;

import net.minecraft.network.packet.s2c.play.CommandSuggestionsS2CPacket;

public class CommandSuggestionsEvent extends Event {
    private final CommandSuggestionsS2CPacket packet;

    public CommandSuggestionsEvent(CommandSuggestionsS2CPacket commandsuggestionss2cpacket) {
        this.packet = commandsuggestionss2cpacket;
    }

    public CommandSuggestionsS2CPacket getPacket() {
        return this.packet;
    }

    public static CommandSuggestionsEvent create(CommandSuggestionsS2CPacket commandsuggestionss2cpacket) {
        return new CommandSuggestionsEvent(commandsuggestionss2cpacket);
    }
}
