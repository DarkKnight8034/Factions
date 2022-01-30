package io.github.darkknight8034.factions;

// Plugin
import org.bukkit.plugin.java.JavaPlugin;

// IO Imports
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;

// Files
import io.github.darkknight8034.factions.EventListener;
import io.github.darkknight8034.factions.Commands.FactionCommand;

public class Main extends JavaPlugin
{
    
    // Commands
    public FactionCommand fm;

    // Event listener
    public EventListener listener;

    // Base data file for plugin, factions and members, territories, etc.
    public FileConfiguration dataFile;

    @Override
    public void onEnable() {
        getLogger().info("Factions plugin enabled!");

        // Creates commands
        fm = new FactionCommand(this);

        // Creates event listener
        listener = new EventListener(this);
        
        // Gets or creates data.yml file if not there
        File f = new File(getDataFolder() + File.separator + "data.yml");
        if (!f.exists())
        {
            try { f.createNewFile(); }
            catch (IOException e) {}

        }

        // Tries to set dataFile to the yml file 
        dataFile = YamlConfiguration.loadConfiguration(f);
        try { dataFile.save(f); }
        catch (IOException e) {}

    }


}