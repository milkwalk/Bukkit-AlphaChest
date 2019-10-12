package net.sradonia.bukkit.alphachest;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.inventory.Inventory;

public class VirtualChestManager {

    private static final String YAML_CHEST_EXTENSION = ".chest.yml";
    private static final Map<UUID, Inventory> chests = new ConcurrentHashMap<>();
    
    private final File dataFolder;
    private final Logger logger;

    public VirtualChestManager(File dataFolder, Logger logger) {
        this.logger = logger;
        this.dataFolder = dataFolder;
    }
    
    /**
     * Lazy chest load
     */
    public void loadPlayerChest(UUID uuid) {
        File chestFile = new File(dataFolder, uuid.toString() + YAML_CHEST_EXTENSION);
        if(chestFile != null && chestFile.exists() && chestFile.isFile()) {
            try {
                chests.put(uuid, InventoryIO.loadFromYaml(chestFile));
            } catch (IllegalArgumentException | IOException | InvalidConfigurationException e) {
                logger.log(Level.WARNING, "Couldn't load chest file: " + chestFile.getName());
                chestFile.delete();
            }
        }
    }
    
    public void unloadPlayerChest(UUID uuid) {
        this.saveChest(uuid);
        chests.remove(uuid);
    }
    
    /**
     * Saves all existing chests to the data folder.
     *
     * @return the number of successfully written chests
     */
    public int save() {
        int savedChests = 0;
        dataFolder.mkdirs();
        Iterator<Entry<UUID, Inventory>> chestIterator = chests.entrySet().iterator();

        while (chestIterator.hasNext()) {
            final Entry<UUID, Inventory> entry = chestIterator.next();
            final UUID playerUUID = entry.getKey();
            final Inventory chest = entry.getValue();

            final File chestFile = new File(dataFolder, playerUUID.toString() + YAML_CHEST_EXTENSION);

            if (chest == null) {
                // Chest got removed, so we have to delete the file.
                chestFile.delete();
                chestIterator.remove();
            } else {
                try {
                    // Write the chest file in YAML format
                    InventoryIO.saveToYaml(chest, chestFile);

                    savedChests++;
                } catch (IOException e) {
                    logger.log(Level.WARNING, "Couldn't save chest file: " + chestFile.getName(), e);
                }
            }
        }
        
        if(savedChests > 0) {
            logger.info("Auto-saved " + savedChests + " chests");
        }
        
        return savedChests;
    }

    /**
     * Saves a specified player's chest to the data folder.
     *
     * @param playerUUID the UUID of the player to save the chest of
     */
    public void saveChest(UUID playerUUID) {
        if(chests.containsKey(playerUUID)) {
            dataFolder.mkdirs();
    
            String uuidString = playerUUID.toString();
            Inventory chest = chests.get(playerUUID);
            File chestFile = new File(dataFolder, uuidString + YAML_CHEST_EXTENSION);
    
            try {
                // Write the chest file in YAML format
                InventoryIO.saveToYaml(chest, chestFile);
            } catch (IOException e) {
                logger.log(Level.WARNING, "Couldn't save chest file: " + chestFile.getName(), e);
            }
            logger.info("Saved chest for UUID " + playerUUID);
        }
    }

    /**
     * Gets a player's virtual chest.
     *
     * @param playerUUID the UUID of the player
     * @return the player's virtual chest.
     */
    public Inventory getChest(UUID playerUUID) {
        Inventory chest = chests.get(playerUUID);
        
        if (chest == null) {
            this.loadPlayerChest(playerUUID);
            chest = chests.get(playerUUID);
            if(chest == null) {
                chest = Bukkit.getServer().createInventory(null, 6 * 9);
                chests.put(playerUUID, chest);
            }
        }

        return chest;
    }

    /**
     * Clears a player's virtual chest.
     *
     * @param playerUUID the UUID of the player
     */
    public void removeChest(UUID playerUUID) {
        // Put a null to the map so we remember to delete the file when saving!
        chests.put(playerUUID, null);
    }

    /**
     * Gets the number of virtual chests.
     *
     * @return the number of virtual chests
     */
    public int getChestCount() {
        return chests.size();
    }
}
