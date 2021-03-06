package io.github.darkknight8034.factions;

// io stream
import java.io.File;
import java.io.IOException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.configuration.ConfigurationSection;

// utils
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

// bukkit
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;

// Boss bar
import org.bukkit.boss.KeyedBossBar;
import org.bukkit.boss.BarColor;
import org.bukkit.NamespacedKey;
import org.bukkit.boss.BarStyle;

import io.github.darkknight8034.factions.Main;

public class FactionManager {

    // Handles creating factions
    public void createFaction(String faction, String player)
    {

        // Default values
        List<String> all = new ArrayList<String>();
        all.add(player);

        List<String> members = new ArrayList<String>();

        List<String> coleaders = new ArrayList<String>();

        List<Chunk> territory = new ArrayList<Chunk>();

        List<String> enemies = new ArrayList<String>();

        List<String> invited = new ArrayList<String>();

        // Setting values in persistent data
        File f = new File(Main.plugin.getDataFolder() + File.separator + "data.yml");
        Main.plugin.dataFile = YamlConfiguration.loadConfiguration(f);

        Main.plugin.dataFile.set("factions." + faction + ".all", all);
        Main.plugin.dataFile.set("factions." + faction + ".members", members);
        Main.plugin.dataFile.set("factions." + faction + ".coleaders", coleaders);
        Main.plugin.dataFile.set("factions." + faction + ".leader", player);
        Main.plugin.dataFile.set("factions." + faction + ".territory", territory);
        Main.plugin.dataFile.set("factions." + faction + ".enemies", enemies);
        Main.plugin.dataFile.set("factions." + faction + ".invited", invited);

        // In game feedback
        Main.plugin.getServer().getPlayer(player).sendMessage(ChatColor.GREEN + "You have created the faction: " + faction + "!");

        // Removes player from any previous factions
        String previous = Main.plugin.dataFile.getString("players." + player);
        leaveFaction(previous, player);

        // Updates players current faction
        Main.plugin.dataFile.set("players." + player, faction);

        // Saves file
        try { Main.plugin.dataFile.save(f); }
        catch (IOException e) {}


    }

    // Handles leaving factions
    public void leaveFaction(String faction, String player)
    {

        if (faction != null)
        {
            // Setting values in persistent data
            File f = new File(Main.plugin.getDataFolder() + File.separator + "data.yml");
            Main.plugin.dataFile = YamlConfiguration.loadConfiguration(f);

            // Removes player from member list
            List<?> members = Main.plugin.dataFile.getList("factions." + faction + ".members");
            members.remove(player);

            Main.plugin.dataFile.set("factions." + faction + ".members", members);

            // Determines new faction leader if they owned the previous faction
            if (Main.plugin.dataFile.getString("factions." + faction + ".leader").equalsIgnoreCase(player))
            {

                // Sets first coleader to leader
                List<?> coleaders = Main.plugin.dataFile.getList("factions." + faction + ".coleaders");
                if (coleaders.size() >= 1)
                {

                    // Sets leader and removes leader from list of coleaders
                    Main.plugin.dataFile.set("factions." + faction + ".leader", coleaders.get(0));

                    coleaders.remove(coleaders.get(0));
                    Main.plugin.dataFile.set("factions." + faction + ".coleaders", coleaders);

                }
                else if (members.size() >= 1)
                {

                    // Sets leader and removes leader from list of members
                    Main.plugin.dataFile.set("factions." + faction + ".leader", members.get(0));

                    members.remove(members.get(0));
                    Main.plugin.dataFile.set("factions." + faction + ".members", members);

                }
                else 
                {

                    // Disbands faction if there is no one left
                    Main.plugin.dataFile.set("factions." + faction, null);

                    Main.plugin.getServer().getPlayer(player).sendMessage(ChatColor.RED + "You disbanded the faction: " + faction);

                }

            }

            // Notifies faction members
            for (String p : Main.plugin.dataFile.getStringList("factions." + faction + ".all"))
            {

                Main.plugin.getServer().getPlayer(p).sendMessage(ChatColor.RED + player + " has left the faction!");

            }
            
            // Saves file
            try { Main.plugin.dataFile.save(f); }
            catch (IOException e) {}
        
        }

    }

    public void joinFaction(String faction, String player)
    {

        // Leaves previous faction
        String previous = Main.plugin.dataFile.getString("players." + player);
        leaveFaction(previous, player);

        // Setting values in persistent data
        File f = new File(Main.plugin.getDataFolder() + File.separator + "data.yml");
        Main.plugin.dataFile = YamlConfiguration.loadConfiguration(f);

        // Updates invited list
        List<String> invited = Main.plugin.dataFile.getStringList("factions." + faction + ".invited");
        invited.remove(player);

        Main.plugin.dataFile.set("factions." + faction + ".invited", invited);

        // Updates member list
        List<String> members = Main.plugin.dataFile.getStringList("factions." + faction + ".members");
        members.add(player);

        Main.plugin.dataFile.set("factions." + faction + ".members", members);

        // Updates total faction member list
        List<String> all = Main.plugin.dataFile.getStringList("factions." + faction + ".all");
        all.add(player);

        Main.plugin.dataFile.set("factions." + faction + ".all", all);

        // Updates player's current faction
        Main.plugin.dataFile.set("players." + player, faction);

        // Alerts members
        for (String p : all)
        {

            Main.plugin.getServer().getPlayer(p).sendMessage(ChatColor.RED + player + " has joined the faction!");

        }

        // Saves file
        try { Main.plugin.dataFile.save(f); }
        catch (IOException e) {}

    }

    public void claimChunk(String faction, Location location, Player player)
    {

        // Gets variables
        Chunk chunk = location.getChunk();
        String c = chunk.getX() + "," + chunk.getZ();

        String world = "overworld";

        if (location.getWorld().getName().endsWith("_nether"))
        {

            world = "nether";

        }

        // Setting values in persistent data
        File f = new File(Main.plugin.getDataFolder() + File.separator + "data.yml");
        Main.plugin.dataFile = YamlConfiguration.loadConfiguration(f);

        // Edits territory
        List<String> territory = Main.plugin.getTerritory(faction, world);
        territory.add(c);

        Main.plugin.dataFile.set("factions." + faction + ".territory." + world, territory);

        // Saves file
        try { Main.plugin.dataFile.save(f); }
        catch (IOException e) {}

        // Updates players boss bar
        setBar(faction, player);

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

    // Gets faction containing chunk, returns null if not claimed
    public String getFactionFromLocation(Location location)
    {

        Chunk chunk = location.getChunk();
        String c = chunk.getX() + "," + chunk.getZ();

        if (location.getWorld().getName().endsWith("_nether")) // Nether chunk
        {

            for (String faction : factions())
            {

                if (Main.plugin.getTerritory(faction, "nether").contains(c))
                {

                    return faction;

                }

            }

        }
        else // Overworld
        {

            for (String faction : factions())
            {
    
                if (Main.plugin.getTerritory(faction, "overworld").contains(c))
                {
    
                    return faction;
    
                }
    
            }

        }

       

        return null;

    }
    
    // Creates boss bar for faction
    public void setBar(String faction, Player player)
    {

        BarColor color = BarColor.RED;

        // In wilderness
        if (faction == null)
        {

            color = BarColor.PURPLE;
            faction = "Wilderness";

        }
        // Player is in their own faction's territory
        else if (Main.plugin.getFaction(player.getName()).equalsIgnoreCase(faction))
        {

            color = BarColor.GREEN;

        }

        Iterator<KeyedBossBar> bars = Main.plugin.getServer().getBossBars();
        // Checks all bars
        while (bars.hasNext())
        {

            // Gets bar
            KeyedBossBar b = bars.next();

            // Bar is location bar and contains event player
            if (b.getKey().getNamespace().equalsIgnoreCase("factions") && b.getKey().getKey().equalsIgnoreCase("location_bar") && b.getPlayers().contains(player))
            {

                // Removes boss bar
                b.removeAll();

            } 


        }

        // Creates boss bar
        KeyedBossBar bar = Main.plugin.getServer().createBossBar(new NamespacedKey(Main.plugin, "location_bar"), faction, color, BarStyle.SOLID);
        bar.addPlayer(player);
        bar.setVisible(true);

    }   
}