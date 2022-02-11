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

        

    }

}
