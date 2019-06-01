package net.sradonia.bukkit.alphachest.listeners;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitScheduler;

import net.sradonia.bukkit.alphachest.AlphaChestPlugin;
import net.sradonia.bukkit.alphachest.VirtualChestManager;

public class JoinLeaveListener implements Listener {
    
    private VirtualChestManager chestManager;
    private BukkitScheduler scheduler;
    private AlphaChestPlugin plugin;
    
    public JoinLeaveListener(AlphaChestPlugin plugin) {
        this.chestManager = plugin.getChestManager();
        this.scheduler = Bukkit.getScheduler();
        this.plugin = plugin;
    }
    
    /**
     * Asynchronously load player chest on join
     * 
     * @param e
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        scheduler.runTaskAsynchronously(plugin, () -> { 
            this.chestManager.loadPlayerChest(e.getPlayer().getUniqueId());
        });
    }
    
    /**
     * Asynchronously save & unload player chest on quit
     * 
     * @param e
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        scheduler.runTaskAsynchronously(plugin, () -> { 
            this.chestManager.unloadPlayerChest(e.getPlayer().getUniqueId());
        });
    }
}
