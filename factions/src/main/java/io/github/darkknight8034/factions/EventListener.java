package io.github.darkknight8034.factions;

import org.bukkit.event.Event;
// Listener stuff
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

// Events
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.entity.EntityType;

// Bukkit
import org.bukkit.entity.Player;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.KeyedBossBar;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

// Java
import java.util.List;
import java.util.Set;
import java.util.UUID;

// Files
import io.github.darkknight8034.factions.Main;

public class EventListener implements Listener
{

    private Map<String, Chunk> location = new HashMap<String, Chunk>();

    public EventListener()
    {

        // Registers listener
        Main.plugin.getServer().getPluginManager().registerEvents(this, Main.plugin);

    }

    // Handles block breaking restrictions if in enemy faction
    @EventHandler
    public void blockBreakEvent(BlockBreakEvent event)
    {

        String faction = Main.plugin.dataFile.getString("players." + event.getPlayer().getName());

        // Player is in a faction, territories must be checked
        if (faction != null)
        {

            // Gets chunk
            String chunk = event.getBlock().getChunk().getX() + "," + event.getBlock().getChunk().getZ();

            // Checks if event can continue
            if (!checkTerritories(event, faction, chunk))
            {

                Main.plugin.getLogger().info("Block break event canceled!");
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.RED + "You aren't allowed to do that here!");

            }

        }

    }

    // Handles placing blocks
    @EventHandler
    public void blockPlaceEvent(BlockPlaceEvent event)
    {

        String faction = Main.plugin.dataFile.getString("players." + event.getPlayer().getName());

        // Player is in a faction, territories must be checked
        if (faction != null)
        {

            // Gets chunk
            String chunk = event.getBlock().getChunk().getX() + "," + event.getBlock().getChunk().getZ();

            // Checks if event can continue
            if (!checkTerritories(event, faction, chunk))
            {

                Main.plugin.getLogger().info("Block place event canceled!");
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.RED + "You aren't allowed to do that here!");

            }
            
        }

    }

    // Checks for territory boundaries
    private boolean checkTerritories(Event event, String faction, String chunk)
    {

        // Chunk is within faction's territory, good to go
        if (Main.plugin.dataFile.getList("factions." + faction + ".territory").contains(chunk))
        {

            return true;

        }

        Set<String> factions = Main.plugin.factionManager.factions();
        factions.remove(faction);

        List<String> enemies = (List<String>) Main.plugin.dataFile.getList("factions." + faction + ".enemies");

        // Allowed to do stuff in enemy territory
        for (String e : enemies)
        {

            factions.remove(e);

        }

        for (String f : factions)
        {

            // Chunk in non waring factions territory
            if (Main.plugin.dataFile.getList("factions." + f +  ".territory").contains(chunk))
            {

                Main.plugin.getLogger().warning(f);
                return false;

            }

        }

        return true;

    }

    // Handles block interaction events
    @EventHandler
    public void interactEvent(InventoryOpenEvent event)
    {

        // Allowed inventories to open at all times
        List<InventoryType> allowed = new ArrayList<InventoryType>();
        allowed.add(InventoryType.PLAYER);
        allowed.add(InventoryType.MERCHANT);

        // Not always allowed, must
        if (!allowed.contains(event.getView().getType()))
        {

            String faction = Main.plugin.dataFile.getString("players." + event.getPlayer().getName());

            // None faction members can do what they want
            if (faction != null)
            {  

                // Gets chunk
                String chunk = event.getInventory().getLocation().getChunk().getX() + "," + event.getInventory().getLocation().getChunk().getZ();

                // Checks if event can continue
                if (!checkTerritories(event, faction, chunk))
                {
    
                    Main.plugin.getLogger().info("Block place event canceled!");
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(ChatColor.RED + "You aren't allowed to do that here!");
    
                }
            
            }

        }

    }

    // Handles villager damage and killing
    @EventHandler
    public void entityAttackEvent(EntityDamageByEntityEvent event)
    {

        // Only runs if player did damage to another player
        if (event.getDamager().getType() == EntityType.PLAYER && event.getEntity().getType() != EntityType.PLAYER)
        {

            String faction = Main.plugin.dataFile.getString("players." + event.getDamager().getName());
            // Those out of factions can do whatever they want
            if (faction != null)
            {

                // Gets string form of chunk location
                String chunk = event.getEntity().getLocation().getChunk().getX() + "," + event.getEntity().getLocation().getChunk().getZ();

                // Checks if event can continue
                if (!checkTerritories(event, faction, chunk))
                {
    
                    Main.plugin.getLogger().info("Block place event canceled!");
                    event.setCancelled(true);
                    event.getDamager().sendMessage(ChatColor.RED + "You aren't allowed to do that here!");
    
                }

            }

        }

    }


    // Boss bar for faction player is in
    @EventHandler
    public void playerMoveEvent(PlayerMoveEvent event)
    {

        Chunk current = event.getPlayer().getLocation().getChunk();
        // Player moved chunks
        if (location.get(event.getPlayer().getName()) != current)
        {

            String chunk = current.getX() + "," + current.getZ();
            String faction = Main.plugin.factionManager.getFactionFromChunk(chunk);

            BarColor color = BarColor.RED;

            // Player is in their own faction's territory
            if (Main.plugin.dataFile.getString("players." + event.getPlayer().getName()).equalsIgnoreCase(faction))
            {

                color = BarColor.GREEN;

            }

            // In wilderness
            if (faction == null)
            {

                color = BarColor.PURPLE;
                faction = "Wilderness";

            }

            Iterator<KeyedBossBar> bars = Main.plugin.getServer().getBossBars();
            // Checks all bars
            while (bars.hasNext())
            {

                // Gets bar
                KeyedBossBar b = bars.next();

                // Bar is location bar and contains event player
                if (b.getKey().getNamespace().equalsIgnoreCase("factions") && b.getKey().getKey().equalsIgnoreCase("location_bar") && b.getPlayers().contains(event.getPlayer()))
                {

                    // Removes bar from player
                    b.removePlayer(event.getPlayer());

                } 


            }

            // Creates boss bar
            KeyedBossBar bar = Main.plugin.getServer().createBossBar(new NamespacedKey(Main.plugin, "location_bar"), faction, color, BarStyle.SOLID);
            bar.addPlayer(event.getPlayer());
            bar.setVisible(true);
        
            // Updates player location in map
            location.put(event.getPlayer().getName(), current);

        }

    }

}
