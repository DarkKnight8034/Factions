package io.github.darkknight8034.factions;

// Listener stuff
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

// Events
import org.bukkit.event.block.BlockBreakEvent;

// Bukkit
import org.bukkit.entity.Player;
import org.bukkit.Chunk;

// Java
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

// IO/Configuration
import org.bukkit.configuration.ConfigurationSection;

// Files
import io.github.darkknight8034.factions.Main;

public class EventListener implements Listener
{

    private Main plugin;

    public EventListener (Main plugin)
    {

        // Registers listener
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.plugin = plugin;

    }


    // Handles block breaking restrictions if in enemy faction
    @EventHandler
    public void blockBreakEvent(BlockBreakEvent event)
    {

        plugin.getLogger().info(event.getPlayer().getDisplayName() + " is breaking " + event.getBlock().getType().toString());

        // Gets information about the event
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        Chunk chunk = player.getWorld().getChunkAt(player.getLocation());

        // Gets faction the player is in
        Set<String> factions = plugin.dataFile.getConfigurationSection("factions").getKeys(false);
        String faction = "";
        for (String f : factions)
        {

            Set<String> members = plugin.dataFile.getConfigurationSection("factions." + f).getKeys(false);
            if (members.contains(uuid))
            {

                faction = f;
                break;

            }

        }

        // Makes sure player is not a free agent
        if (faction != "") {
            // Gets chunks in player's faction's territory
            ArrayList<Chunk> chunks = (ArrayList<Chunk>) plugin.dataFile.getList("factions." + faction + ".territories");
            if (!chunks.contains(chunk))
            {

                // Cancels event if player is 
                event.setCancelled(true);

            }
            plugin.getLogger().info(faction);
        }

    }

}
