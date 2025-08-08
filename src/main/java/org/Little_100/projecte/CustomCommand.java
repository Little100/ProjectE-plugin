package org.Little_100.projecte;

import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;

import java.util.List;

public class CustomCommand extends BukkitCommand {

    private final CommandManager commandManager;

    public CustomCommand(String name, CommandManager commandManager) {
        super(name);
        this.commandManager = commandManager;
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        return commandManager.onCommand(sender, this, commandLabel, args);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        return commandManager.onTabComplete(sender, this, alias, args);
    }
}