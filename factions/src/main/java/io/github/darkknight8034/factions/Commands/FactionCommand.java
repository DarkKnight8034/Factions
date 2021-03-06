package io.github.darkknight8034.factions.Commands;

// Bukkit
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Sound;
import org.bukkit.Location;

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
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

// Handles faction commands
public class FactionCommand implements CommandExecutor
{

    public FactionCommand()
    {

        // Sets up command execution and tab completion
        Main.plugin.getCommand("faction").setExecutor(this);
        Main.plugin.getCommand("faction").setTabCompleter(new FactionTabComp());

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
            else if (args[0].equalsIgnoreCase("join"))
            {

                return join(sender, cmd, label, args);

            }
            else if (args[0].equalsIgnoreCase("invite"))
            {

                return invite(sender, cmd, label, args);

            }
            else if (args[0].equalsIgnoreCase("kick"))
            {

                return kick(sender, cmd, label, args);
            
            }
            else if (args[0].equalsIgnoreCase("leave"))
            {

                String faction = Main.plugin.getFaction(sender.getName());
                if (faction != null)
                {

                    Main.plugin.factionManager.leaveFaction(faction, sender.getName());

                }

            }

        }

        return true;

    }

    // Lists factions, green for if the player is in them
    private boolean listFactions(CommandSender sender, Command cmd, String label, String[] args)
    {

        Player player = (Player) sender;

        // Gets keys in section, aka. the faction names
        Set<String> factions = Main.plugin.factionManager.factions();
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
                if (Main.plugin.factionManager.factions().contains(name))
                {

                    player.sendMessage(ChatColor.RED + "A faction with that name already exists!");
                    return false;

                }

                // Removes levels from player
                player.setLevel(player.getLevel() - Main.plugin.configFile.getInt("factions.createCost"));

                Main.plugin.factionManager.createFaction(name, player.getName());

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

            player.sendMessage("You need " + (Main.plugin.configFile.getInt("factions.createCost") - player.getLevel()) + " more levels to create a faction!");
            return false;

        }

    }

    // Handles promoting members
    private boolean promote(CommandSender sender, Command cmd, String label, String[] args)
    {

        Player player = (Player) sender;

        // Error handling
        if (args.length == 1)
        {

            player.sendMessage("You need to give a player name!");
            return false;
        
        }

        // Gets player to be promoted
        Player target = Main.plugin.getServer().getPlayer(args[1]);
        // Error handling
        if (target == null)
        {

            player.sendMessage("Player: " + args[1] + ", could not be found.");
            return false;

        }

        // Gets factions of involved players
        String pFaction = Main.plugin.getFaction(player.getName());
        String tFaction = Main.plugin.getFaction(target.getName());

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

        // Error handling
        if (args.length == 1)
        {

            player.sendMessage("You need to give a player name!");
            return false;
        
        }

        // Gets player to be promoted
        Player target = Main.plugin.getServer().getPlayer(args[1]);
        // Error hanlding
        if (target == null)
        {

            player.sendMessage("Player: " + args[1] + ", could not be found.");
            return false;

        }

        // Gets factions of involved players
        String pFaction = Main.plugin.getFaction(player.getName());
        String tFaction = Main.plugin.getFaction(target.getName());

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

        // Gets variables
        Player player = (Player) sender;
        Location location = player.getLocation();
        String faction = Main.plugin.getFaction(sender.getName());

        // Must be in a faction
        if (faction != null)
        {

            // Checking if player has enough xp
            if (player.getLevel() <= Main.plugin.configFile.getInt("factions.chunkCost"))
            {

                sender.sendMessage("You need " + (Main.plugin.configFile.getInt("factions.chunkCost") - player.getLevel()) + " more levels to calim territory!");
                return false;

            }

            // String version of chunk
            String chunk = location.getChunk().getX() + "," + location.getChunk().getZ();
            // Gets world
            String world = (location.getWorld().getName().endsWith("_nether")) ? "nether" : "overworld";

            if (Main.plugin.getTerritory(faction, world).contains(chunk))
            {

                sender.sendMessage("Your faction already has this chunk!");
                return false;

            }

            // Makes sure other faction doesn't own chunk
            for (String f : Main.plugin.factionManager.factions())
            {

                if (Main.plugin.getTerritory(f, world).contains(chunk))
                {

                    sender.sendMessage(f + " has already claimed this chunk!");
                    return false;

                }

            }

            // Claims territory
            Main.plugin.factionManager.claimChunk(faction, location, player);

            // Feedback
            sender.sendMessage("Chunk claimed!");
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 10);

            // Removes xp from player
            player.setLevel(player.getLevel() - Main.plugin.configFile.getInt("factions.chunkCost"));
            return true;

        }

        // So sad
        sender.sendMessage("You need to be in a faction before you can claim land!");
        return false;

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
        String faction = Main.plugin.getFaction(sender.getName());
        if (faction == null)
        {

            sender.sendMessage(ChatColor.RED + "You need to be in a faction first!");
            return false;

        }

        // Only leaders and coleaders can declare war
        if (Main.plugin.getMembers(faction).contains(sender.getName()))
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
        if (!Main.plugin.factionManager.factions().contains(target))
        {

            sender.sendMessage(ChatColor.RED + "Target faction is invalid!");
            return false;

        }

        if (Main.plugin.getEnemies(faction).contains(target))
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
        if (Main.plugin.getColeaders(faction).contains(sender.getName()) || Main.plugin.getLeader(faction).equalsIgnoreCase(sender.getName()))
        {

            // Editing file
            File f = new File(Main.plugin.getDataFolder() + File.separator + "data.yml");
            Main.plugin.dataFile = YamlConfiguration.loadConfiguration(f);

            // Updates enemies for player faction
            List<String> enemies = Main.plugin.getEnemies(faction);
            enemies.add(target);
            Main.plugin.dataFile.set("factions." + faction + ".enemies", enemies);

            // Updates enemies for target faction
            enemies = Main.plugin.getEnemies(target);
            enemies.add(faction);
            Main.plugin.dataFile.set("factions." + target + ".enemies", enemies);
            
            // Saves file
            try { Main.plugin.dataFile.save(f); }
            catch (IOException e) {}

            for (String p : Main.plugin.getAll(faction))
            {

                Main.plugin.getServer().getPlayer(p).sendMessage(ChatColor.RED + "Your faction has declared war against " + target + "!");

            }

            for (String p : (List<String>) Main.plugin.getAll(target))
            {

                Main.plugin.getServer().getPlayer(p).sendMessage(ChatColor.RED + faction + " has declared war against your faction!");

            }

        }


        return true;

    }

    // Handles inviting players to faction
    private boolean invite(CommandSender sender, Command cmd, String label, String[] args)
    {

        // Gets faction
        String faction = Main.plugin.getFaction(sender.getName());
        if (args.length == 1)
        {

            sender.sendMessage("You need to give a player name!");
            return false;

        }

        Player player = Main.plugin.getServer().getPlayer(args[1]);
        if (player == null)
        {

            sender.sendMessage(args[1] + " is offline. Invite them again when they are online.");
            return false;

        }

        // Can't invite player if they aren't in faction
        if (faction != null)
        {

            // Creates and sends invite message to player
            TextComponent msg = new TextComponent(sender.getName() + " has invited you to their faction, " + faction +  ".");
            msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Accept invite")));
            msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/faction join " + faction));
            player.spigot().sendMessage(msg);

        }

        return true;

    }

    // Handles joining factions
    private boolean join(CommandSender sender, Command cmd, String label, String[] args)
    {

        // Error handling
        if (args.length == 1)
        {

            sender.sendMessage("You need to include a faction");
            return false;
        
        }

        String faction = args[1];

        // Player joins faction if invited
        if (Main.plugin.getInvited(faction).contains(sender.getName()))
        {

            Main.plugin.factionManager.joinFaction(faction, sender.getName());

        }

        return true;
    
    }

    // Handles kicking people from factions
    private boolean kick(CommandSender sender, Command cmd, String label, String[] args)
    {

        String faction = Main.plugin.getFaction(sender.getName());
        if (faction == null) // Not in a faction
        {

            return false;
            
        }

        if (args.length == 1)
        {

            sender.sendMessage("You need to give a player name!");
            return false;

        }

        // Gets player being kicked
        String player = args[1];

        // Player not in faction
        if (!Main.plugin.getAll(faction).contains(player))
        {

            sender.sendMessage("Player not in faction.");
            return false;

        }

        // Leaving
        if (sender.getName().equalsIgnoreCase(player))
        {

            Main.plugin.factionManager.leaveFaction(faction, player);

        }

        // Kicker is leader, no checking
        if (Main.plugin.getLeader(faction).equalsIgnoreCase(sender.getName()))
        {

            Main.plugin.factionManager.leaveFaction(faction, player);

        }

        // Coleader kicks member
        if (Main.plugin.getColeaders(faction).contains(sender.getName()) && Main.plugin.getMembers(faction).contains(player))
        {

            Main.plugin.factionManager.leaveFaction(faction, player);

        }

        return true;
            
    }

}