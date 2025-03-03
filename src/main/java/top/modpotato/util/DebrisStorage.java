package top.modpotato.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import top.modpotato.Main;
import top.modpotato.config.Config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Manages storage of replaced Ancient Debris locations
 */
public class DebrisStorage {
    private final Main plugin;
    private final File storageFile;
    private FileConfiguration storage;
    private final Config config;
    
    // Use ConcurrentHashMap for thread safety
    private final Map<UUID, List<String>> replacedLocations = new ConcurrentHashMap<>();
    
    // Track if storage is currently being saved to prevent concurrent modifications
    private boolean isSaving = false;
    
    /**
     * Creates a new DebrisStorage instance
     * @param plugin The plugin instance
     * @param config The configuration
     */
    public DebrisStorage(Main plugin, Config config) {
        this.plugin = plugin;
        this.config = config;
        this.storageFile = new File(plugin.getDataFolder(), "debris_storage.yml");
        loadStorage();
    }
    
    /**
     * Loads the storage file
     */
    public synchronized void loadStorage() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        
        if (!storageFile.exists()) {
            try {
                storageFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create debris_storage.yml", e);
                return;
            }
        }
        
        storage = YamlConfiguration.loadConfiguration(storageFile);
        
        // Load replaced locations from storage
        replacedLocations.clear();
        try {
            for (String worldUUID : storage.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(worldUUID);
                    List<String> locations = storage.getStringList(worldUUID);
                    
                    // Limit the number of locations to prevent memory issues
                    int maxLocations = config.getMaxLocationsPerWorld();
                    if (locations.size() > maxLocations) {
                        plugin.getLogger().warning("Too many Ancient Debris locations stored for world " + worldUUID + 
                                                  ". Limiting to " + maxLocations);
                        locations = locations.subList(0, maxLocations);
                    }
                    
                    replacedLocations.put(uuid, new ArrayList<>(locations));
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid UUID in debris_storage.yml: " + worldUUID);
                }
            }
            plugin.getLogger().info("Loaded " + getTotalLocationsCount() + " Ancient Debris locations from storage");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error loading debris_storage.yml", e);
        }
    }
    
    /**
     * Saves the storage file asynchronously
     */
    public void saveStorageAsync() {
        if (isSaving) {
            return;
        }
        
        isSaving = true;
        
        // Use the configurable async task delay
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                saveStorage();
            } finally {
                isSaving = false;
            }
        });
    }
    
    /**
     * Saves the storage file
     */
    public synchronized void saveStorage() {
        if (plugin.isShuttingDown()) {
            return;
        }
        
        try {
            // Clear the storage file
            for (String key : storage.getKeys(false)) {
                storage.set(key, null);
            }
            
            // Save all replaced locations
            for (Map.Entry<UUID, List<String>> entry : replacedLocations.entrySet()) {
                storage.set(entry.getKey().toString(), entry.getValue());
            }
            
            // Save the file
            storage.save(storageFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save debris_storage.yml", e);
        }
    }
    
    /**
     * Adds a location to the storage
     * @param location The location to add
     * @return true if the location was added, false otherwise
     */
    public boolean addLocation(Location location) {
        if (location == null) {
            return false;
        }
        
        UUID worldUUID = location.getWorld().getUID();
        String locationString = serializeLocation(location);
        
        // Get or create the list for this world
        List<String> locations = replacedLocations.computeIfAbsent(worldUUID, k -> new ArrayList<>());
        
        // Check if we've reached the maximum number of locations for this world
        if (locations.size() >= config.getMaxLocationsPerWorld()) {
            plugin.getLogger().warning("Maximum number of Ancient Debris locations reached for world " + 
                                      location.getWorld().getName() + ". Not adding more locations.");
            return false;
        }
        
        // Add the location if it doesn't already exist
        if (!locations.contains(locationString)) {
            locations.add(locationString);
            
            // Save the storage periodically
            if (locations.size() % 100 == 0) {
                saveStorageAsync();
            }
            
            return true;
        }
        
        return false;
    }
    
    /**
     * Checks if a location is in the storage
     * @param location The location to check
     * @return true if the location is in the storage, false otherwise
     */
    public boolean containsLocation(Location location) {
        if (location == null || location.getWorld() == null) {
            return false;
        }
        
        UUID worldUUID = location.getWorld().getUID();
        String locString = serializeLocation(location);
        
        return replacedLocations.containsKey(worldUUID) && 
               replacedLocations.get(worldUUID).contains(locString);
    }
    
    /**
     * Removes a location from the storage
     * @param location The location to remove
     * @return true if the location was removed, false otherwise
     */
    public boolean removeLocation(Location location) {
        if (location == null || location.getWorld() == null) {
            return false;
        }
        
        UUID worldUUID = location.getWorld().getUID();
        String locString = serializeLocation(location);
        
        if (replacedLocations.containsKey(worldUUID)) {
            boolean removed = replacedLocations.get(worldUUID).remove(locString);
            if (removed) {
                // Schedule async save to prevent lag
                Bukkit.getScheduler().runTaskAsynchronously(plugin, this::saveStorage);
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Restores all Ancient Debris in the world
     * @return The number of blocks restored
     */
    public int restoreAllDebris() {
        int restoredCount = 0;
        
        for (Map.Entry<UUID, List<String>> entry : replacedLocations.entrySet()) {
            UUID worldUUID = entry.getKey();
            World world = Bukkit.getWorld(worldUUID);
            
            if (world != null) {
                List<String> toRemove = new ArrayList<>();
                
                for (String locString : entry.getValue()) {
                    try {
                        Location location = deserializeLocation(world, locString);
                        
                        // Check if the chunk is loaded or should be loaded
                        if (!isChunkLoaded(location) && !loadChunkIfNeeded(location)) {
                            plugin.getLogger().fine("Skipping restoration at " + locString + " because chunk is not loaded");
                            continue;
                        }
                        
                        Block block = location.getBlock();
                        
                        // Only restore if the block is still Netherrack
                        if (block.getType() == Material.NETHERRACK) {
                            block.setType(Material.ANCIENT_DEBRIS);
                            restoredCount++;
                        }
                        
                        toRemove.add(locString);
                    } catch (Exception e) {
                        plugin.getLogger().warning("Error restoring Ancient Debris at " + locString + ": " + e.getMessage());
                        toRemove.add(locString); // Remove invalid locations
                    }
                }
                
                // Remove restored locations
                entry.getValue().removeAll(toRemove);
            }
        }
        
        // Save changes
        saveStorage();
        
        plugin.getLogger().info("Restored " + restoredCount + " Ancient Debris blocks");
        return restoredCount;
    }
    
    /**
     * Restores Ancient Debris in a specific world
     * @param world The world to restore Ancient Debris in
     * @return The number of blocks restored
     */
    public int restoreDebrisInWorld(World world) {
        if (world == null) {
            return 0;
        }
        
        int restoredCount = 0;
        UUID worldUUID = world.getUID();
        
        if (replacedLocations.containsKey(worldUUID)) {
            List<String> toRemove = new ArrayList<>();
            
            for (String locString : replacedLocations.get(worldUUID)) {
                try {
                    Location location = deserializeLocation(world, locString);
                    
                    // Check if the chunk is loaded or should be loaded
                    if (!isChunkLoaded(location) && !loadChunkIfNeeded(location)) {
                        plugin.getLogger().fine("Skipping restoration at " + locString + " because chunk is not loaded");
                        continue;
                    }
                    
                    Block block = location.getBlock();
                    
                    // Only restore if the block is still Netherrack
                    if (block.getType() == Material.NETHERRACK) {
                        block.setType(Material.ANCIENT_DEBRIS);
                        restoredCount++;
                    }
                    
                    toRemove.add(locString);
                } catch (Exception e) {
                    plugin.getLogger().warning("Error restoring Ancient Debris at " + locString + ": " + e.getMessage());
                    toRemove.add(locString); // Remove invalid locations
                }
            }
            
            // Remove restored locations
            replacedLocations.get(worldUUID).removeAll(toRemove);
            
            // Save changes
            saveStorage();
        }
        
        plugin.getLogger().info("Restored " + restoredCount + " Ancient Debris blocks in world " + world.getName());
        return restoredCount;
    }
    
    /**
     * Gets the total number of stored locations
     * @return The total number of stored locations
     */
    public int getTotalLocationsCount() {
        int count = 0;
        for (List<String> locations : replacedLocations.values()) {
            count += locations.size();
        }
        return count;
    }
    
    /**
     * Gets the number of stored locations in a specific world
     * @param world The world to get the count for
     * @return The number of stored locations in the world
     */
    public int getWorldLocationsCount(World world) {
        if (world == null) {
            return 0;
        }
        
        UUID worldUUID = world.getUID();
        if (replacedLocations.containsKey(worldUUID)) {
            return replacedLocations.get(worldUUID).size();
        }
        
        return 0;
    }
    
    /**
     * Clears all stored locations
     */
    public void clearAllLocations() {
        replacedLocations.clear();
        saveStorage();
        plugin.getLogger().info("Cleared all stored Ancient Debris locations");
    }
    
    /**
     * Checks if a chunk is loaded
     * @param location The location to check
     * @return true if the chunk is loaded, false otherwise
     */
    public boolean isChunkLoaded(Location location) {
        if (location == null || location.getWorld() == null) {
            return false;
        }
        
        return location.getWorld().isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4);
    }
    
    /**
     * Checks if a chunk is generated
     * @param location The location to check
     * @return true if the chunk is generated, false otherwise
     */
    public boolean isChunkGenerated(Location location) {
        if (location == null || location.getWorld() == null) {
            return false;
        }
        
        return location.getWorld().isChunkGenerated(location.getBlockX() >> 4, location.getBlockZ() >> 4);
    }
    
    /**
     * Loads a chunk if needed
     * @param location The location to load the chunk for
     * @return true if the chunk was loaded or was already loaded, false otherwise
     */
    public boolean loadChunkIfNeeded(Location location) {
        if (location == null || location.getWorld() == null) {
            return false;
        }
        
        // Check if we should ensure chunks are loaded
        if (!config.isEnsureChunksLoaded()) {
            return false;
        }
        
        // Check if the chunk is already loaded
        if (isChunkLoaded(location)) {
            return true;
        }
        
        // Check if the chunk is generated if we only want to load generated chunks
        if (config.isOnlyReplaceGeneratedChunks() && !isChunkGenerated(location)) {
            return false;
        }
        
        try {
            // Load the chunk
            return location.getWorld().loadChunk(location.getBlockX() >> 4, location.getBlockZ() >> 4, true);
        } catch (Exception e) {
            plugin.getLogger().warning("Error loading chunk at " + location + ": " + e.getMessage());
            return false;
        }
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
     * @throws IllegalArgumentException If the location string is invalid
     */
    private Location deserializeLocation(World world, String locString) {
        try {
            String[] parts = locString.split(",");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid location format: " + locString);
            }
            
            int x = Integer.parseInt(parts[0]);
            int y = Integer.parseInt(parts[1]);
            int z = Integer.parseInt(parts[2]);
            return new Location(world, x, y, z);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid location format: " + locString, e);
        }
    }
} 