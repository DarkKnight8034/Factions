package io.github.darkknight8034.factions.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.Collection;

import io.github.darkknight8034.factions.Main;

public class FactionTabComp implements TabCompleter
{
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args)
    {

        List<String> vals = new ArrayList<String> ();

        if (args.length == 1)
        {

            vals.add("create");
            vals.add("list");
            vals.add("promote");
            vals.add("demote");
            vals.add("claim");
            vals.add("war");
            vals.add("invite");

            return vals;

        }
        else if (args.length == 2)
        {

            // Shows what members the player has permission to promote
            if (args[0].equalsIgnoreCase("promote") || args[0].equalsIgnoreCase("demote"))
            {

                // Gets info
                Player player = (Player) sender;
                String faction = Main.plugin.dataFile.getString("players." + player.getName());

                // Player can promote anyone they want
                if (Main.plugin.dataFile.getString("factions." + faction + ".leader").equalsIgnoreCase(player.getName()))
                {

                    // List of everyone in the faction, - the leader
                    List<String> all = (List<String>) Main.plugin.dataFile.getList("factions." + faction + ".all");
                    all.remove(player.getName());

                    return all;

                }
                else if (Main.plugin.dataFile.getList("factions." + faction + ".coleaders").contains(player.getName()))
                {

                    return (List<String>) Main.plugin.dataFile.getList("factions." + faction + ".members");
                
                }
                else 
                {

                    // Player is member and cannot promote anyone
                    return new ArrayList<String>();

                }

            }
            else if (args[0].equalsIgnoreCase("war"))
            {

                // All factions minus the one player is currently in
                Set<String> factions = Main.plugin.factionManager.factions();

                if (factions.size() > 0)
                {

                    String faction = Main.plugin.dataFile.getString("players." + sender.getName());
                    factions.remove(faction);
                    return new ArrayList<String> (factions);        

                }

                return new ArrayList<String>();
            

            }
            else if (args[0].equalsIgnoreCase("invite"))
            {

                // Gets online players
                List<String> players = new ArrayList<String>();
                for (Player p : Main.plugin.getServer().getOnlinePlayers())
                {

                    players.add(p.getName());

                }

                return players;

            }
            else if (args[0].equalsIgnoreCase("kick"))
            {

                String faction = Main.plugin.getFaction(sender.getName());
                if (faction == null)
                {

                    return null;
                    
                }

                
            }

        }

        return null;

    }

}
