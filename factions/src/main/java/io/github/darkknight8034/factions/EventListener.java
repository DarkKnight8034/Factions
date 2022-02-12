package io.github.darkknight8034.factions;

// Listener stuff
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

// Events
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.entity.EntityType;
// Bukkit
import org.bukkit.entity.Player;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.ChatColor;

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

            // Gets string version of chunk data
            String chunk = event.getBlock().getChunk().getX() + "," + event.getBlock().getChunk().getZ();

            // Not in their own territory
            if (!Main.plugin.dataFile.getList("factions." + faction + ".territory").contains(chunk))
            {

                List<String> factions = (List<String>) Main.plugin.dataFile.getList("factions." + faction + ".enemies");
            
                // Not in any wars
                if (factions.size() == 0)
                {

                    event.setCancelled(true);
                    event.getPlayer().sendMessage(ChatColor.RED + "You aren't allowed to do that here!");
                
                }
                else // checks if enemies have chunk
                {

                    for (String f: factions)
                    {

                        // Found chunk, done search
                        if (Main.plugin.dataFile.getList("factions." + f + ".territory").contains(chunk))
                        {

                            break;
                        
                        }
                    
                    }
                    
                }

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

            // Gets string version of chunk data
            String chunk = event.getBlock().getChunk().getX() + "," + event.getBlock().getChunk().getZ();

            // Not in their own territory
            if (!Main.plugin.dataFile.getList("factions." + faction + ".territory").contains(chunk))
            {

                List<String> factions = (List<String>) Main.plugin.dataFile.getList("factions." + faction + ".enemies");
            
                // Not in any wars
                if (factions.size() == 0)
                {

                    event.setCancelled(true);
                    event.getPlayer().sendMessage(ChatColor.RED + "You aren't allowed to do that here!");
                
                }
                else // checks if enemies have chunk
                {

                    for (String f: factions)
                    {

                        // Found chunk, done search
                        if (Main.plugin.dataFile.getList("factions." + f + ".territory").contains(chunk))
                        {

                            break;
                        
                        }
                    
                    }
                    
                }

            }

        }

    }

    // Handles block interaction events
    @EventHandler
    public void interactEvent(PlayerInteractEvent event)
    {

        Block block = event.getClickedBlock();
        if (block != null) // Error handling
        {

            // Not interacting with air
            if (block.getType() != Material.AIR)
            {

                String faction = Main.plugin.dataFile.getString("players." + event.getPlayer().getName());

                // Players outside of a faction can do whatever they want
                if (faction != null)
                {

                    // Allowed to interact with doors
                    if (!block.getType().toString().contains("DOOR"))
                    {

                        String chunk = block.getChunk().getX() + "," + block.getChunk().getZ();

                        // Not in faction's territory
                        if (!Main.plugin.dataFile.getList("factions." + faction + ".territory").contains(chunk))
                        {

                            List<String> factions = (List<String>) Main.plugin.dataFile.getList("factions." + faction + ".enemies");
                    
                            // Not in any wars
                            if (factions.size() == 0)
                            {

                                event.setCancelled(true);
                                event.getPlayer().sendMessage(ChatColor.RED + "You aren't allowed to do that here!");
                            
                            }
                            else // checks if enemies have chunk
                            {

                                for (String f: factions)
                                {

                                    // Found chunk, done search
                                    if (Main.plugin.dataFile.getList("factions." + f + ".territory").contains(chunk))
                                    {

                                        break;
                                    
                                    }
                                
                                }
                                
                            }

                        }
                    
                    }

                }

            }
        
        }

    }

    // Handles villager damage and killing
    @EventHandler
    public void entityAttackEvent(EntityDamageByEntityEvent event)
    {

        // Only runs if player did damage
        if (event.getDamager().getType() == EntityType.PLAYER)
        {

            String faction = Main.plugin.dataFile.getString("players." + event.getDamager().getName());
            // Those out of factions can do whatever they want
            if (faction != null)
            {

                // Gets string form of chunk location
                String chunk = event.getDamager().getLocation().getChunk().getX() + "," + event.getDamager().getLocation().getChunk().getZ();

                // Not in faction's territory
                if (!Main.plugin.dataFile.getList("factions." + faction + ".territory").contains(chunk))
                {

                    List<String> factions = (List<String>) Main.plugin.dataFile.getList("factions." + faction + ".enemies");
                
                    // Not in any wars
                    if (factions.size() == 0)
                    {

                        event.setCancelled(true);
                        event.getDamager().sendMessage(ChatColor.RED + "You aren't allowed to do that here!");
                    
                    }
                    else // checks if enemies have chunk
                    {

                        for (String f: factions)
                        {

                            // Found chunk, done search
                            if (Main.plugin.dataFile.getList("factions." + f + ".territory").contains(chunk))
                            {

                                break;
                            
                            }
                        
                        }
                        
                    }

                }

            }

        }

    }

}
