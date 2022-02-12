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
import org.bukkit.entity.EntityType;
// Bukkit
import org.bukkit.entity.Player;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.ChatColor;

import java.util.ArrayList;
// Java
import java.util.List;
import java.util.Set;
import java.util.UUID;

// Files
import io.github.darkknight8034.factions.Main;

public class EventListener implements Listener
{

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

        Set<String> factions = Main.plugin.fm.factions();
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

}
