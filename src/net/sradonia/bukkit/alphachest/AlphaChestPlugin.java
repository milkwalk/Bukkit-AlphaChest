package net.sradonia.bukkit.alphachest;

import java.io.File;
import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;

import net.sradonia.bukkit.alphachest.commands.ChestCommand;
import net.sradonia.bukkit.alphachest.commands.ClearChestCommand;
import net.sradonia.bukkit.alphachest.commands.DisposalCommand;
import net.sradonia.bukkit.alphachest.commands.SaveChestsCommand;
import net.sradonia.bukkit.alphachest.commands.WorkbenchCommand;
import net.sradonia.bukkit.alphachest.listeners.JoinLeaveListener;
import net.sradonia.bukkit.alphachest.listeners.PlayerListener;

public class AlphaChestPlugin extends JavaPlugin {

    private Logger logger;
    private VirtualChestManager chestManager;
    
    @Override
    public void onEnable() {
        // Save a copy of the default config.yml if one doesn't already exist
        saveDefaultConfig();

        // Initialize some classes and objects
        logger = getLogger();

        File chestFolder = new File(getDataFolder(), "chests");
        chestManager = new VirtualChestManager(chestFolder, logger);
        new Teller(this);

        // Set the plugin's command executors
        getCommand("chest").setExecutor(new ChestCommand(chestManager));
        getCommand("clearchest").setExecutor(new ClearChestCommand(chestManager));
        getCommand("disposal").setExecutor(new DisposalCommand());
        getCommand("savechests").setExecutor(new SaveChestsCommand(chestManager));
        getCommand("workbench").setExecutor(new WorkbenchCommand());

        // Register the plugin's events
        getServer().getPluginManager().registerEvents(new PlayerListener(this, chestManager), this);
        getServer().getPluginManager().registerEvents(new JoinLeaveListener(this), this);

        // Schedule an auto-save task
        int autosaveInterval = getConfig().getInt("autosave") * 1200;

        if (autosaveInterval > 0) {
            getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
                chestManager.save();
            }, autosaveInterval, autosaveInterval);
        }
    }

    @Override
    public void onDisable() {
        chestManager.save();
    }
    
    public VirtualChestManager getChestManager() {
        return this.chestManager;
    }
}
