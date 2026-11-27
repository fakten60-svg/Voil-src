package lol.lwes.voil.command;

import java.util.List;
import lol.lwes.voil.VoilClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public abstract class Command {
    private final String name;
    private final String description;
    private final List<String> aliases;

    protected Command(String name, String description, String... aliases) {
        this.name = name;
        this.description = description;
        this.aliases = List.of(aliases);
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public List<String> getAliases() {
        return this.aliases;
    }

    public boolean matches(String input) {
        if (this.name.equalsIgnoreCase(input)) {
            return true;
        }

        for (String alias : this.aliases) {
            if (alias.equalsIgnoreCase(input)) {
                return true;
            }
        }
        return false;
    }

    public void run(String message, String[] arguments) {
        try {
            this.execute(message, arguments);
        } catch (RuntimeException exception) {
            reply("§c" + this.name + " failed: " + exception.getMessage());
        }
    }

    protected abstract void execute(String message, String[] args);

    protected static void reply(String message) {
        MinecraftClient mc = VoilClient.mc;
        if (mc != null && mc.inGameHud != null) {
            mc.inGameHud.getChatHud().addMessage(Text.literal("§d§l[Voil] §r" + message));
        }
    }

    protected static void usage(String usage) {
        reply("§7Usage: " + VoilClient.getCommandPrefix() + usage);
    }
}
