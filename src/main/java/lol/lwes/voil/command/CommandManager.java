package lol.lwes.voil.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.module.ModuleManager;
import lol.lwes.voil.util.PlayerTargetUtil;
import net.minecraft.client.network.PlayerListEntry;

public final class CommandManager {
    public static final CommandManager INSTANCE = new CommandManager();
    private final List<Command> commands = new ArrayList<>();
    private final List<Command> view = Collections.unmodifiableList(this.commands);

    private CommandManager() {
        this.register(new Command("help", "Lists all commands", "commands") {
            @Override
            protected void execute(String message, String[] args) {
                for (Command command : CommandManager.this.commands) {
                    reply("§f" + VoilClient.getCommandPrefix() + command.getName() + " §7- " + command.getDescription());
                }
            }
        });
        this.register(new Command("toggle", "Toggles a module", "t") {
            @Override
            protected void execute(String message, String[] args) {
                if (args.length < 2) {
                    usage("toggle <module>");
                    return;
                }

                Module module = ModuleManager.INSTANCE.getModuleByName(args[1]);
                if (module == null) {
                    reply("§cUnknown module: " + args[1]);
                    return;
                }

                module.toggle();
                reply(module.getName() + (module.isEnabled() ? " §aenabled" : " §cdisabled"));
            }
        });
        this.register(new Command("friend", "Adds or removes a friend", "f") {
            @Override
            protected void execute(String message, String[] args) {
                if (args.length < 3 || !(args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("remove"))) {
                    usage("friend <add|remove> <player>");
                    return;
                }

                UUID uuid = findPlayer(args[2]);
                if (uuid == null) {
                    reply("§cPlayer not found: " + args[2]);
                    return;
                }

                if (args[1].equalsIgnoreCase("add")) {
                    PlayerTargetUtil.addFriend(uuid);
                    reply("Added §f" + args[2] + "§r as a friend");
                } else {
                    PlayerTargetUtil.removeFriend(uuid);
                    reply("Removed §f" + args[2] + "§r from your friends");
                }
            }
        });
    }

    public void register(Command command) {
        this.commands.removeIf(existing -> existing.getName().equalsIgnoreCase(command.getName()));
        this.commands.add(command);
    }

    public Command getCommand(String name) {
        for (Command command : this.commands) {
            if (command.matches(name)) {
                return command;
            }
        }
        return null;
    }

    public List<Command> getCommands() {
        return this.view;
    }

    private static UUID findPlayer(String name) {
        if (VoilClient.mc.getNetworkHandler() == null) {
            return null;
        }

        PlayerListEntry entry = VoilClient.mc.getNetworkHandler().getPlayerListEntry(name);
        return entry != null ? entry.getProfile().id() : null;
    }
}
