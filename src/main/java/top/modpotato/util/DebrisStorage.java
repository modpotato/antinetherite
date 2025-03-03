package top.modpotato.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import top.modpotato.Main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Manages storage of replaced Ancient Debris locations
 */
public class DebrisStorage {
    private final Main plugin;
    private final File storageFile;
    private FileConfiguration storage;
    
    // Map of world UUID to list of locations where Ancient Debris was replaced
    private final Map<UUID, List<String>> replacedLocations = new HashMap<>();
    
    /**
     * Creates a new DebrisStorage instance
     * @param plugin The plugin instance
     */
    public DebrisStorage(Main plugin) {
        this.plugin = plugin;
        this.storageFile = new File(plugin.getDataFolder(), "debris_storage.yml");
        loadStorage();
    }
    
    /**
     * Loads the storage file
     */
    public void loadStorage() {
        if (!storageFile.exists()) {
            try {
                storageFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create debris_storage.yml", e);
            }
        }
        
        storage = YamlConfiguration.loadConfiguration(storageFile);
        
        // Load replaced locations from storage
        replacedLocations.clear();
        for (String worldUUID : storage.getKeys(false)) {
            UUID uuid = UUID.fromString(worldUUID);
            List<String> locations = storage.getStringList(worldUUID);
            replacedLocations.put(uuid, locations);
        }
    }
    
    /**
     * Saves the storage file
     */
    public void saveStorage() {
        // Save replaced locations to storage
        for (Map.Entry<UUID, List<String>> entry : replacedLocations.entrySet()) {
            storage.set(entry.getKey().toString(), entry.getValue());
        }
        
        try {
            storage.save(storageFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save debris_storage.yml", e);
        }
    }
    
    /**
     * Adds a location to the storage
     * @param location The location to add
     */
    public void addLocation(Location location) {
        UUID worldUUID = location.getWorld().getUID();
        String locString = serializeLocation(location);
        
        replacedLocations.computeIfAbsent(worldUUID, k -> new ArrayList<>()).add(locString);
        saveStorage();
    }
    
    /**
     * Checks if a location is in the storage
     * @param location The location to check
     * @return true if the location is in the storage, false otherwise
     */
    public boolean containsLocation(Location location) {
        UUID worldUUID = location.getWorld().getUID();
        String locString = serializeLocation(location);
        
        return replacedLocations.containsKey(worldUUID) && 
               replacedLocations.get(worldUUID).contains(locString);
    }
    
    /**
     * Removes a location from the storage
     * @param location The location to remove
     */
    public void removeLocation(Location location) {
        UUID worldUUID = location.getWorld().getUID();
        String locString = serializeLocation(location);
        
        if (replacedLocations.containsKey(worldUUID)) {
            replacedLocations.get(worldUUID).remove(locString);
            saveStorage();
        }
    }
    
    /**
     * Restores all Ancient Debris in the world
     */
    public void restoreAllDebris() {
        for (Map.Entry<UUID, List<String>> entry : replacedLocations.entrySet()) {
            UUID worldUUID = entry.getKey();
            World world = Bukkit.getWorld(worldUUID);
            
            if (world != null) {
                List<String> toRemove = new ArrayList<>();
                
                for (String locString : entry.getValue()) {
                    Location location = deserializeLocation(world, locString);
                    Block block = location.getBlock();
                    
                    // Only restore if the block is still Netherrack
                    if (block.getType() == Material.NETHERRACK) {
                        block.setType(Material.ANCIENT_DEBRIS);
                    }
                    
                    toRemove.add(locString);
                }
                
                // Remove restored locations
                entry.getValue().removeAll(toRemove);
            }
        }
        
        saveStorage();
    }
    
    /**
     * Serializes a location to a string
     * @param location The location to serialize
     * @return The serialized location
     */
    private String serializeLocation(Location location) {
        return location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
    }
    
    /**
     * Deserializes a location from a string
     * @param world The world the location is in
     * @param locString The serialized location
     * @return The deserialized location
     */
    private Location deserializeLocation(World world, String locString) {
        String[] parts = locString.split(",");
        int x = Integer.parseInt(parts[0]);
        int y = Integer.parseInt(parts[1]);
        int z = Integer.parseInt(parts[2]);
        return new Location(world, x, y, z);
    }
} 