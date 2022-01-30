package io.github.darkknight8034.factions.Commands;

// Commands
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import io.github.darkknight8034.factions.Main;

// Handles faction commands
public class FactionCommand implements CommandExecutor
{

    private Main plugin;

    public FactionCommand(Main plugin)
    {

        this.plugin = plugin;
        plugin.getCommand("faction").setExecutor(this);

    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    { 

        plugin.getLogger().info(cmd.toString() + " | " + label + " | " + args);

        return false;

    }

}