package io.github.darkknight8034.factions;

// Plugin
import org.bukkit.plugin.java.JavaPlugin;

// Bukkit
import org.bukkit.WorldBorder;

// IO Imports
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;

// Files
import io.github.darkknight8034.factions.EventListener;
import io.github.darkknight8034.factions.FactionManager;
import io.github.darkknight8034.factions.Commands.FactionCommand;

public class Main extends JavaPlugin
{
    
    public static Main plugin;

    // Commands
    public FactionCommand factionCommand;

    // Event listener
    public EventListener listener;

    // Managers
    public FactionManager factionManager;

    // Base data file for plugin, factions and members, territories, etc.
    public FileConfiguration dataFile;
    public FileConfiguration configFile;

    @Override
    public void onEnable() {
        plugin = this;
        getLogger().info("Factions plugin enabled!");
        loadConfig();

        // Creates commands
        factionCommand = new FactionCommand();

        // Creates event listener
        listener = new EventListener();
        
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

        // 0 gives default border size
        if (configFile.getInt("world.border") != 0)
        {

            // Sets world border
            WorldBorder border = getServer().getWorlds().get(0).getWorldBorder();
            border.setCenter(0, 0);
            border.setSize(configFile.getInt("world.border") * 2);

        }

    }

    @Override
    public void onDisable()
    {

        saveDefaultConfig();
        getLogger().info("Factions plugin disabled!");

    }

    private void loadConfig()
    {

        File f = new File(getDataFolder() + File.separator + "config.yml");
        if (!f.exists())
        {

            getConfig().options().copyDefaults(true);
            saveConfig();

        }

        this.configFile = getConfig();

    }


}