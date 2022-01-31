package io.github.darkknight8034.factions.Commands;

import org.bukkit.ChatColor;
// Commands
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import io.github.darkknight8034.factions.Main;
import io.github.darkknight8034.factions.Commands.FactionTabComp;

// Handles faction commands
public class FactionCommand implements CommandExecutor
{

    private Main plugin;

    public FactionCommand(Main plugin)
    {

        this.plugin = plugin;
        // Sets up command execution and tab completion
        plugin.getCommand("faction").setExecutor(this);
        plugin.getCommand("faction").setTabCompleter(new FactionTabComp());

        // Command aliases
        List<String> aliases = new ArrayList<String>();
        aliases.add("f");
        plugin.getCommand("faction").setAliases(aliases);

    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    { 

        if (args.length >= 1)
        {

            // Handles creating factions
            if (args[0].equalsIgnoreCase("create"))
            {

                return createFaction(sender, cmd, label, args);

            }
            else if (args[0].equalsIgnoreCase("list"))
            {

                return listFactions(sender, cmd, label, args);

            }

        }

        return true;

    }

    private boolean listFactions(CommandSender sender, Command cmd, String label, String[] args)
    {

        Player player = (Player) sender;

        // Creates section in config
        ConfigurationSection root = plugin.dataFile.getConfigurationSection("factions");

        // Gets keys in section, aka. the faction names
        Set<String> factions = root.getKeys(false);
        String display = "";
        int i = 0;
        for (String f : factions)
        {

            if (plugin.dataFile.getList("factions." + f + ".members").contains(player.getName())) 
            {

                display += ChatColor.GREEN + f;

            }

            if (i != factions.size() - 1)
            {

                display += ", ";
                i ++;

            }

        }

        // Sends the list of factions
        player.sendMessage(display);

        return true;

    }

    // Handles creating a faction
    private boolean createFaction(CommandSender sender, Command cmd, String label, String[] args)
    {

        plugin.getLogger().info("Faction Create!");

        Player player = (Player) sender;
        // Costs 30 levels to create a faction
        if (player.getLevel() >= 30)
        {

            // Error handling for no faction name
            if (args.length >= 2)
            {
            
                // Faction name
                String name = args[1];

                // Removes levels from player
                player.setLevel(player.getLevel() - 30);

                // Default values
                List<String> members = new ArrayList<String>();
                members.add(player.getName());

                List<String> coleaders = new ArrayList<String>();

                // Setting values in persistent data
                File f = new File(plugin.getDataFolder() + File.separator + "data.yml");
                plugin.dataFile = YamlConfiguration.loadConfiguration(f);

                plugin.dataFile.set("factions." + name + ".members", members);
                plugin.dataFile.set("factions." + name + ".coleaders", coleaders);
                plugin.dataFile.set("factions." + name + ".leader", player.getName());

                // In game feedback
                player.sendMessage(ChatColor.GREEN + "You have created the faction: " + name + "!");

                // Removes player from any previous factions
                String previous = plugin.dataFile.getString("players." + player.getName());
                if (previous != null)
                {

                    // Removes player from member list
                    List<?> Members = plugin.dataFile.getList("factions." + previous + ".members");
                    Members.remove(player.getName());

                    plugin.dataFile.set("factions." + previous + ".members", Members);

                    // Determines new faction leader if they owned the previous faction
                    if (plugin.dataFile.getString("factions." + previous + ".leader").equalsIgnoreCase(player.getName()))
                    {

                        // Sets first coleader to leader
                        List<?> Coleaders = plugin.dataFile.getList("factions." + previous + ".coleaders");
                        if (Coleaders.size() >= 1)
                        {

                            // Sets leader and removes leader from list of coleaders
                            plugin.dataFile.set("factions." + previous + ".leader", Coleaders.get(0));

                            Coleaders.remove(Coleaders.get(0));
                            plugin.dataFile.set("factions." + previous + ".coleaders", Coleaders);

                        }
                        else if (Members.size() >= 1)
                        {

                            // Sets leader and removes leader from list of members
                            plugin.dataFile.set("factions." + previous + ".leader", Members.get(0));

                            Members.remove(Members.get(0));
                            plugin.dataFile.set("factions." + previous + ".members", Members);

                        }
                        else 
                        {

                            // Disbands faction if there is no one left
                            plugin.dataFile.set("factions." + previous, null);

                            player.sendMessage(ChatColor.RED + "You disbanded the faction: " + previous);

                        }

                    }

                }

                // Updates players current faction
                plugin.dataFile.set("players." + player.getName(), name);

                // Saves file
                try { plugin.dataFile.save(f); }
                catch (IOException e) {}

                return true;

            }
            else
            {

                player.sendMessage("You need to include a faction name.");
                return false;

            }

        }
        else
        {

            player.sendMessage("You need 30 levels to create a faction!");
            return false;

        }

    }

}