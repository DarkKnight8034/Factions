package io.github.darkknight8034.factions.Commands;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;

// Commands
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;

import java.util.HashSet;
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

    public FactionCommand()
    {

        // Sets up command execution and tab completion
        Main.plugin.getCommand("faction").setExecutor(this);
        Main.plugin.getCommand("faction").setTabCompleter(new FactionTabComp(this));

        // Command aliases
        List<String> aliases = new ArrayList<String>();
        aliases.add("f");
        Main.plugin.getCommand("faction").setAliases(aliases);

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
            else if (args[0].equalsIgnoreCase("promote"))
            {

                return promote(sender, cmd, label, args);

            }
            else if (args[0].equalsIgnoreCase("demote"))
            {

                return demote(sender, cmd, label, args);

            }
            else if (args[0].equalsIgnoreCase("claim"))
            {

                return claim(sender, cmd, label, args);

            }
            else if (args[0].equalsIgnoreCase("war"))
            {

                return war(sender, cmd, label, args);

            }

        }

        return true;

    }

    // Gets list of faction names
    public Set<String> factions()
    {

        // Creates config section
        ConfigurationSection root = Main.plugin.dataFile.getConfigurationSection("factions");

        // Error handling
        if (root != null) 
        {

            // Gets keys (aka factions)
            return root.getKeys(false);

        }

        Main.plugin.getLogger().info("No root to get factions");
        return new HashSet<String>();

    }

    // Lists factions, green for if the player is in them
    private boolean listFactions(CommandSender sender, Command cmd, String label, String[] args)
    {

        Player player = (Player) sender;

        // Gets keys in section, aka. the faction names
        Set<String> factions = factions();
        if (factions.size() != 0)
        {

            String display = "Factions: ";
            int i = 0;
            for (String f : factions)
            {

                if (Main.plugin.dataFile.getString("players." + player.getName()).equalsIgnoreCase(f))
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

        player.sendMessage("There are currently no factions!");
        return true;

    }

    // Handles creating a faction
    private boolean createFaction(CommandSender sender, Command cmd, String label, String[] args)
    {

        Main.plugin.getLogger().info("Faction Create!");

        Player player = (Player) sender;

        // Costs levels to create a faction (set in config)
        if (player.getLevel() >= Main.plugin.configFile.getInt("factions.createCost"))
        {

            // Error handling for no faction name
            if (args.length >= 2)
            {
            
                // Faction name
                String name = args[1];

                // No duplicate faction names
                if (factions().contains(name))
                {

                    player.sendMessage(ChatColor.RED + "A faction with that name already exists!");
                    return false;

                }

                // Removes levels from player
                player.setLevel(player.getLevel() - Main.plugin.configFile.getInt("factions.createCost"));

                // Default values
                List<String> all = new ArrayList<String>();
                all.add(player.getName());

                List<String> members = new ArrayList<String>();

                List<String> coleaders = new ArrayList<String>();

                List<Chunk> territory = new ArrayList<Chunk>();

                List<String> enemies = new ArrayList<String>();

                // Setting values in persistent data
                File f = new File(Main.plugin.getDataFolder() + File.separator + "data.yml");
                Main.plugin.dataFile = YamlConfiguration.loadConfiguration(f);

                Main.plugin.dataFile.set("factions." + name + ".all", all);
                Main.plugin.dataFile.set("factions." + name + ".members", members);
                Main.plugin.dataFile.set("factions." + name + ".coleaders", coleaders);
                Main.plugin.dataFile.set("factions." + name + ".leader", player.getName());
                Main.plugin.dataFile.set("factions." + name + ".territory", territory);
                Main.plugin.dataFile.set("factions." + name + ".enemies", enemies);

                // In game feedback
                player.sendMessage(ChatColor.GREEN + "You have created the faction: " + name + "!");

                // Removes player from any previous factions
                String previous = Main.plugin.dataFile.getString("players." + player.getName());
                if (previous != null)
                {

                    // Removes player from member list
                    List<?> Members = Main.plugin.dataFile.getList("factions." + previous + ".members");
                    Members.remove(player.getName());

                    Main.plugin.dataFile.set("factions." + previous + ".members", Members);

                    // Determines new faction leader if they owned the previous faction
                    if (Main.plugin.dataFile.getString("factions." + previous + ".leader").equalsIgnoreCase(player.getName()))
                    {

                        // Sets first coleader to leader
                        List<?> Coleaders = Main.plugin.dataFile.getList("factions." + previous + ".coleaders");
                        if (Coleaders.size() >= 1)
                        {

                            // Sets leader and removes leader from list of coleaders
                            Main.plugin.dataFile.set("factions." + previous + ".leader", Coleaders.get(0));

                            Coleaders.remove(Coleaders.get(0));
                            Main.plugin.dataFile.set("factions." + previous + ".coleaders", Coleaders);

                        }
                        else if (Members.size() >= 1)
                        {

                            // Sets leader and removes leader from list of members
                            Main.plugin.dataFile.set("factions." + previous + ".leader", Members.get(0));

                            Members.remove(Members.get(0));
                            Main.plugin.dataFile.set("factions." + previous + ".members", Members);

                        }
                        else 
                        {

                            // Disbands faction if there is no one left
                            Main.plugin.dataFile.set("factions." + previous, null);

                            player.sendMessage(ChatColor.RED + "You disbanded the faction: " + previous);

                        }

                    }

                }

                // Updates players current faction
                Main.plugin.dataFile.set("players." + player.getName(), name);

                // Saves file
                try { Main.plugin.dataFile.save(f); }
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

    // Handles promoting members
    private boolean promote(CommandSender sender, Command cmd, String label, String[] args)
    {

        Player player = (Player) sender;

        // Gets player to be promoted
        Player target = Main.plugin.getServer().getPlayer(args[1]);

        // Gets factions of involved players
        String pFaction = Main.plugin.dataFile.getString("players." + player.getName());
        String tFaction = Main.plugin.dataFile.getString("players." + target.getName());

        // Have to be in the same faction
        if (!pFaction.equalsIgnoreCase(tFaction))
        {

            return false;

        }

        boolean change = changeRank(player, target, 1);
        if (change)
        {

            player.sendMessage(ChatColor.GREEN + "Promoted " + target.getName() + "!");            

        }

        return true;

    }

    // Handles demoting members
    private boolean demote(CommandSender sender, Command cmd, String label, String[] args)
    {

        Player player = (Player) sender;

        // Gets player to be promoted
        Player target = Main.plugin.getServer().getPlayer(args[1]);

        // Gets factions of involved players
        String pFaction = Main.plugin.dataFile.getString("players." + player.getName());
        String tFaction = Main.plugin.dataFile.getString("players." + target.getName());

        // Have to be in the same faction
        if (!pFaction.equalsIgnoreCase(tFaction))
        {

            return false;

        }

        boolean change = changeRank(player, target, -1);
        if (change)
        {

            player.sendMessage(ChatColor.GREEN + "Demoted " + target.getName() + "!");            

        }

        return true;

    }

    // Handles changing ranks
    private boolean changeRank(Player player, Player target, int change)
    {

        int pLevel = 1;
        int tLevel = 1;

        String faction = Main.plugin.dataFile.getString("players." + player.getName());

        // Gets level of player
        if (Main.plugin.dataFile.getString("factions." + faction + ".leader").equalsIgnoreCase(player.getName()))
        {

            pLevel = 3; // Player is leader

        }
        else if (Main.plugin.dataFile.getList("factions." + faction + ".coleaders").contains(player.getName()))
        {

            pLevel = 2; // Player is coleader

        }

        // Memebers don't have those perms
        if (pLevel == 1)
        {

            player.sendMessage(ChatColor.RED + "You don't have permission to do this!");
            return false;

        }

        // Gets level of target
        if (Main.plugin.dataFile.getString("factions." + faction + ".leader").equalsIgnoreCase(target.getName()))
        {

            tLevel = 3; // Target is leader

        }
        else if (Main.plugin.dataFile.getList("factions." + faction + ".coleaders").contains(target.getName()))
        {

            tLevel = 2; // Target is coleader

        }

        int newL = 0;
        if (pLevel > tLevel)
        {

            newL = tLevel + change;

        }
        else
        {

            // Target is higher rank
            player.sendMessage(ChatColor.RED + "Target is higher rank, you don't have permission to do this!");
            return false;

        }

        // Editing file
        File f = new File(Main.plugin.getDataFolder() + File.separator + "data.yml");
        Main.plugin.dataFile = YamlConfiguration.loadConfiguration(f);

        // Removes player from their current role
        switch(tLevel)
        {

            // Members
            case 1:
                List<String> members = (List<String>) Main.plugin.dataFile.getList("factions." + faction + ".members");
                members.remove(target.getName());
                Main.plugin.dataFile.set("factions." + faction + ".members", members);
                break;

            // Coleaders
            case 2:
                List<String> coleaders = (List<String>) Main.plugin.dataFile.getList("factions." + faction + ".coleaders");
                coleaders.remove(target.getName());
                Main.plugin.dataFile.set("factions." + faction + ".coleaders", coleaders);

        }

        switch(newL)
        {

            // Player being demoted from member
            case 0:
                player.sendMessage(ChatColor.RED + "You can't demote a member.");
                break;


            // Change to coleader
            case 2:
                List<String> coleaders = (List<String>) Main.plugin.dataFile.getList("factions." + faction + ".coleaders");
                coleaders.add(target.getName());
                Main.plugin.dataFile.set("factions." + faction + ".coleaders", coleaders);
                break;

            // Change to leader
            case 3:
                Main.plugin.dataFile.set("factions." + faction + ".leader", target.getName());
                
                // Promoting to leader sets leader to be a coleader
                coleaders = (List<String>) Main.plugin.dataFile.getList("factions." + faction + ".coleaders");
                coleaders.add(player.getName());
                Main.plugin.dataFile.set("factions." + faction + ".coleaders", coleaders);

        }

        // Saves file
        try { Main.plugin.dataFile.save(f); }
        catch (IOException e) {}

        return true;

    }   

    // Handles claiming territory
    private boolean claim(CommandSender sender, Command cmd, String label, String[] args)
    {

        Player player = (Player) sender;
        String faction = Main.plugin.dataFile.getString("players." + player.getName());

        // Cannot claim land when not in a faction
        if (faction == null)
        {

            player.sendMessage(ChatColor.RED + "You need to be in a faction first!");
            return false;

        }
        
        // Gets chunk
        Chunk current = player.getLocation().getChunk();
        // Gets chunks claimed by faction
        List<String> chunks = (List<String>) Main.plugin.dataFile.getList("factions." + faction + ".territory");

        // Already claimed
        String serialized = current.getX() + "," + current.getZ();
        if (chunks.contains(serialized))
        {

            player.sendMessage("Chunk already claimed!");
            return false;

        }

        // Max land claimed
        int max = Main.plugin.configFile.getInt("factions.maxTerritory");
        Main.plugin.getLogger().info("" + max);
        if (chunks.size() >= max && max != 0)
        {

            player.sendMessage(ChatColor.RED + "Max territory reached!");
            return false;

        }

        // Player has enough xp
        if (player.getLevel() >= Main.plugin.configFile.getInt("factions.chunkCost"))
        {

            // Removes xp
            player.setLevel(player.getLevel() - Main.plugin.configFile.getInt("factions.chunkCost"));
            chunks.add(serialized);

            // Editing file
            File f = new File(Main.plugin.getDataFolder() + File.separator + "data.yml");
            Main.plugin.dataFile = YamlConfiguration.loadConfiguration(f);

            // Writes new chunk
            Main.plugin.dataFile.set("factions." + faction + ".territory", chunks);

            // Feedback
            player.sendMessage(ChatColor.GREEN + "Chunk claimed!");

            // Saves file
            try { Main.plugin.dataFile.save(f); }
            catch (IOException e) {}

        }

        return true;

    }

    // Handles declaring war
    private boolean war(CommandSender sender, Command cmd, String label, String[] args)
    {

        // Configurated to not allow wars
        if (!Main.plugin.configFile.getBoolean("factions.war"))
        {

            sender.sendMessage(ChatColor.RED + "Wars are disabled on this server!");
            return false;

        }

        // Getting factions
        String faction = Main.plugin.dataFile.getString("players." + sender.getName());
        if (faction == null)
        {

            sender.sendMessage(ChatColor.RED + "You need to be in a faction first!");
            return false;

        }

        // Only leaders and coleaders can declare war
        if (Main.plugin.dataFile.getList("factions." + faction + ".members").contains(sender.getName()))
        {

            sender.sendMessage("Only leaders and coleaders can declare war!");
            return false;

        }

        if (args.length == 1)
        {

            sender.sendMessage("You need to provide a target faction!");
            return false;

        }
        String target = args[1];
        if (!factions().contains(target))
        {

            sender.sendMessage(ChatColor.RED + "Target faction is invalid!");
            return false;

        }

        if (Main.plugin.dataFile.getList("factions." + faction + ".enemies").contains(target))
        {

            sender.sendMessage("You are already in a war with this faction!");
            return false;

        }

        // lol
        if (faction.equalsIgnoreCase(target))
        {

            sender.sendMessage("You can't declare war against your own faction!");
            return false;

        }

        // Only coleaders and leader are allowed to declare war
        if (Main.plugin.dataFile.getList("factions." + faction + ".coleaders").contains(sender.getName()) || Main.plugin.dataFile.getString("factions." + faction + ".leader").equalsIgnoreCase(sender.getName()))
        {

            // Editing file
            File f = new File(Main.plugin.getDataFolder() + File.separator + "data.yml");
            Main.plugin.dataFile = YamlConfiguration.loadConfiguration(f);

            // Updates enemies for player faction
            List<String> enemies = (List<String>) Main.plugin.dataFile.getList("factions." + faction + ".enemies");
            enemies.add(target);
            Main.plugin.dataFile.set("factions." + faction + ".enemies", enemies);

            // Updates enemies for target faction
            enemies = (List<String>) Main.plugin.dataFile.getList("factions." + target + ".enemies");
            enemies.add(faction);
            Main.plugin.dataFile.set("factions." + target + ".enemies", enemies);
            
            // Saves file
            try { Main.plugin.dataFile.save(f); }
            catch (IOException e) {}

            for (String p : (List<String>) Main.plugin.dataFile.getList("factions." + faction + ".all"))
            {

                Main.plugin.getServer().getPlayer(p).sendMessage(ChatColor.RED + "Your faction has declared war against " + target + "!");

            }

            for (String p : (List<String>) Main.plugin.dataFile.getList("factions." + target + ".all"))
            {

                Main.plugin.getServer().getPlayer(p).sendMessage(ChatColor.RED + faction + " has declared war against your faction!");

            }

        }


        return true;

    }
}