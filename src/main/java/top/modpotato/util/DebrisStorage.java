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
import top.modpotato.restoration.RestorationSession;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import org.bukkit.command.CommandSender;

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
                    if (maxLocations != -1 && locations.size() > maxLocations) {
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
        
        // Skip if we're not saving replaced locations
        if (!config.isSaveReplacedLocations()) {
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
        
        // Skip if we're not saving replaced locations
        if (!config.isSaveReplacedLocations()) {
            return true;
        }
        
        World world = location.getWorld();
        if (world == null) {
            return false;
        }
        
        UUID worldUUID = world.getUID();
        String locString = serializeLocation(location);
        
        // Initialize the list if it doesn't exist
        replacedLocations.computeIfAbsent(worldUUID, k -> new ArrayList<>());
        
        // Check if we've reached the maximum number of locations for this world
        List<String> worldLocations = replacedLocations.get(worldUUID);
        int maxLocations = config.getMaxLocationsPerWorld();
        if (maxLocations != -1 && worldLocations.size() >= maxLocations) {
            plugin.getLogger().warning("Maximum number of Ancient Debris locations reached for world " + 
                                      world.getName() + ". Skipping location: " + locString);
            return false;
        }
        
        // Add the location if it doesn't already exist
        if (!worldLocations.contains(locString)) {
            worldLocations.add(locString);
            
            // Save the storage asynchronously
            saveStorageAsync();
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
        AtomicInteger restoredCount = new AtomicInteger(0);
        boolean isFolia = checkFolia();
        
        for (Map.Entry<UUID, List<String>> entry : replacedLocations.entrySet()) {
            UUID worldUUID = entry.getKey();
            World world = Bukkit.getWorld(worldUUID);
            
            if (world != null) {
                if (isFolia) {
                    // On Folia, use thread-safe collection
                    ConcurrentLinkedQueue<String> toRemove = new ConcurrentLinkedQueue<>();
                    
                    // On Folia, schedule each block change on the region scheduler
                    CountDownLatch latch = new CountDownLatch(entry.getValue().size());
                    
                    for (String locString : entry.getValue()) {
                        try {
                            Location location = deserializeLocation(world, locString);
                            
                            // Check if the chunk is loaded or should be loaded
                            if (!isChunkLoaded(location) && !loadChunkIfNeeded(location)) {
                                plugin.getLogger().fine("Skipping restoration at " + locString + " because chunk is not loaded");
                                toRemove.add(locString);
                                latch.countDown();
                                continue;
                            }
                            
                            // Schedule block change on the region scheduler
                            Bukkit.getRegionScheduler().execute(plugin, location, () -> {
                                try {
                                    Block block = location.getBlock();
                                    
                                    // Only restore if the block is still Netherrack
                                    if (block.getType() == Material.NETHERRACK) {
                                        block.setType(Material.ANCIENT_DEBRIS);
                                        restoredCount.incrementAndGet();
                                    }
                                    
                                    toRemove.add(locString);
                                } finally {
                                    latch.countDown();
                                }
                            });
                        } catch (Exception e) {
                            plugin.getLogger().warning("Error restoring Ancient Debris at " + locString + ": " + e.getMessage());
                            toRemove.add(locString); // Remove invalid locations
                            latch.countDown();
                        }
                    }
                    
                    // Wait for all tasks to complete (with timeout to prevent indefinite blocking)
                    try {
                        if (!latch.await(60, TimeUnit.SECONDS)) {
                            plugin.getLogger().warning("Timeout waiting for debris restoration in world " + world.getName() + 
                                                      ". Some blocks may not have been restored.");
                        }
                    } catch (InterruptedException e) {
                        plugin.getLogger().warning("Interrupted while waiting for debris restoration: " + e.getMessage());
                        Thread.currentThread().interrupt();
                    }
                    
                    // Thread-safe removal from the original list
                    synchronized (entry.getValue()) {
                        entry.getValue().removeAll(toRemove);
                    }
                } else {
                    // On Paper/Spigot, use simple ArrayList (single-threaded)
                    List<String> toRemove = new ArrayList<>();
                    
                    for (String locString : entry.getValue()) {
                        try {
                            Location location = deserializeLocation(world, locString);
                            
                            // Check if the chunk is loaded or should be loaded
                            if (!isChunkLoaded(location) && !loadChunkIfNeeded(location)) {
                                plugin.getLogger().fine("Skipping restoration at " + locString + " because chunk is not loaded");
                                toRemove.add(locString); // Add to toRemove for consistency
                                continue;
                            }
                            
                            Block block = location.getBlock();
                            
                            // Only restore if the block is still Netherrack
                            if (block.getType() == Material.NETHERRACK) {
                                block.setType(Material.ANCIENT_DEBRIS);
                                restoredCount.incrementAndGet();
                            }
                            
                            toRemove.add(locString);
                        } catch (Exception e) {
                            plugin.getLogger().warning("Error restoring Ancient Debris at " + locString + ": " + e.getMessage());
                            toRemove.add(locString); // Remove invalid locations
                        }
                    }
                    
                    // Remove restored locations (no sync needed - single thread)
                    entry.getValue().removeAll(toRemove);
                }
            }
        }
        
        // Save changes asynchronously
        saveStorageAsync();
        
        plugin.getLogger().info("Restored " + restoredCount.get() + " Ancient Debris blocks");
        return restoredCount.get();
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
        
        AtomicInteger restoredCount = new AtomicInteger(0);
        UUID worldUUID = world.getUID();
        boolean isFolia = checkFolia();
        
        if (replacedLocations.containsKey(worldUUID)) {
            List<String> locations = new ArrayList<>(replacedLocations.get(worldUUID));
            
            if (isFolia) {
                // On Folia, use thread-safe collection
                ConcurrentLinkedQueue<String> toRemove = new ConcurrentLinkedQueue<>();
                
                // On Folia, schedule each block change on the region scheduler
                CountDownLatch latch = new CountDownLatch(locations.size());
                
                for (String locString : locations) {
                    try {
                        Location location = deserializeLocation(world, locString);
                        
                        // Check if the chunk is loaded or should be loaded
                        if (!isChunkLoaded(location) && !loadChunkIfNeeded(location)) {
                            plugin.getLogger().fine("Skipping restoration at " + locString + " because chunk is not loaded");
                            toRemove.add(locString);
                            latch.countDown();
                            continue;
                        }
                        
                        // Schedule block change on the region scheduler
                        Bukkit.getRegionScheduler().execute(plugin, location, () -> {
                            try {
                                Block block = location.getBlock();
                                
                                // Only restore if the block is still Netherrack
                                if (block.getType() == Material.NETHERRACK) {
                                    block.setType(Material.ANCIENT_DEBRIS);
                                    restoredCount.incrementAndGet();
                                }
                                
                                toRemove.add(locString);
                            } finally {
                                latch.countDown();
                            }
                        });
                    } catch (Exception e) {
                        plugin.getLogger().warning("Error restoring Ancient Debris at " + locString + ": " + e.getMessage());
                        toRemove.add(locString); // Remove invalid locations
                        latch.countDown();
                    }
                }
                
                // Wait for all tasks to complete (with timeout to prevent indefinite blocking)
                try {
                    if (!latch.await(60, TimeUnit.SECONDS)) {
                        plugin.getLogger().warning("Timeout waiting for debris restoration in world " + world.getName() + 
                                                  ". Some blocks may not have been restored.");
                    }
                } catch (InterruptedException e) {
                    plugin.getLogger().warning("Interrupted while waiting for debris restoration: " + e.getMessage());
                    Thread.currentThread().interrupt();
                }
                
                // Thread-safe removal from the original list
                List<String> worldLocations = replacedLocations.get(worldUUID);
                synchronized (worldLocations) {
                    worldLocations.removeAll(toRemove);
                }
            } else {
                // On Paper/Spigot, use simple ArrayList (single-threaded)
                List<String> toRemove = new ArrayList<>();
                
                for (String locString : locations) {
                    try {
                        Location location = deserializeLocation(world, locString);
                        
                        // Check if the chunk is loaded or should be loaded
                        if (!isChunkLoaded(location) && !loadChunkIfNeeded(location)) {
                            plugin.getLogger().fine("Skipping restoration at " + locString + " because chunk is not loaded");
                            toRemove.add(locString); // Add to toRemove for consistency
                            continue;
                        }
                        
                        Block block = location.getBlock();
                        
                        // Only restore if the block is still Netherrack
                        if (block.getType() == Material.NETHERRACK) {
                            block.setType(Material.ANCIENT_DEBRIS);
                            restoredCount.incrementAndGet();
                        }
                        
                        toRemove.add(locString);
                    } catch (Exception e) {
                        plugin.getLogger().warning("Error restoring Ancient Debris at " + locString + ": " + e.getMessage());
                        toRemove.add(locString); // Remove invalid locations
                    }
                }
                
                // Remove restored locations (no sync needed - single thread)
                replacedLocations.get(worldUUID).removeAll(toRemove);
            }
            
            // Save changes asynchronously
            saveStorageAsync();
        }
        
        plugin.getLogger().info("Restored " + restoredCount.get() + " Ancient Debris blocks in world " + world.getName());
        return restoredCount.get();
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
     * Schedules restoration of all Ancient Debris and returns a session for progress tracking
     * @param initiator The command sender who initiated the restoration
     * @return The restoration session, or null if nothing to restore
     */
    public RestorationSession scheduleRestoreAll(CommandSender initiator) {
        // Calculate total locations and unique chunks
        int totalLocations = getTotalLocationsCount();
        if (totalLocations == 0) {
            return null;
        }
        
        Set<String> uniqueChunks = new HashSet<>();
        for (Map.Entry<UUID, List<String>> entry : replacedLocations.entrySet()) {
            World world = Bukkit.getWorld(entry.getKey());
            if (world != null) {
                for (String locString : entry.getValue()) {
                    try {
                        Location loc = deserializeLocation(world, locString);
                        uniqueChunks.add(world.getUID() + ":" + (loc.getBlockX() >> 4) + "," + (loc.getBlockZ() >> 4));
                    } catch (Exception e) {
                        // Ignore invalid locations
                    }
                }
            }
        }
        
        RestorationSession session = new RestorationSession(initiator, null, totalLocations, uniqueChunks.size());
        
        // Schedule the actual restoration work
        scheduleRestorationWork(session, null);
        
        return session;
    }
    
    /**
     * Schedules restoration of Ancient Debris in a specific world and returns a session for progress tracking
     * @param initiator The command sender who initiated the restoration
     * @param world The world to restore debris in
     * @return The restoration session, or null if nothing to restore
     */
    public RestorationSession scheduleRestoreInWorld(CommandSender initiator, World world) {
        if (world == null) {
            return null;
        }
        
        UUID worldUUID = world.getUID();
        if (!replacedLocations.containsKey(worldUUID)) {
            return null;
        }
        
        List<String> locations = replacedLocations.get(worldUUID);
        if (locations.isEmpty()) {
            return null;
        }
        
        // Calculate unique chunks
        Set<String> uniqueChunks = new HashSet<>();
        for (String locString : locations) {
            try {
                Location loc = deserializeLocation(world, locString);
                uniqueChunks.add((loc.getBlockX() >> 4) + "," + (loc.getBlockZ() >> 4));
            } catch (Exception e) {
                // Ignore invalid locations
            }
        }
        
        RestorationSession session = new RestorationSession(initiator, world, locations.size(), uniqueChunks.size());
        
        // Schedule the actual restoration work
        scheduleRestorationWork(session, world);
        
        return session;
    }
    
    /**
     * Schedules the actual restoration work for a session
     * @param session The restoration session
     * @param worldFilter The world to restore in, or null for all worlds
     */
    private void scheduleRestorationWork(RestorationSession session, World worldFilter) {
        boolean isFolia = checkFolia();
        AtomicInteger restoredCount = new AtomicInteger(0);
        
        if (isFolia) {
            scheduleFoliaRestoration(session, worldFilter, restoredCount);
        } else {
            schedulePaperRestoration(session, worldFilter, restoredCount);
        }
    }
    
    /**
     * Schedules restoration work on Paper (main-thread batching)
     * @param session The restoration session
     * @param worldFilter The world to restore in, or null for all worlds
     * @param restoredCount Counter for restored blocks
     */
    private void schedulePaperRestoration(RestorationSession session, World worldFilter, AtomicInteger restoredCount) {
        // Collect all locations to process
        List<LocationRestore> toRestore = new ArrayList<>();
        
        if (worldFilter == null) {
            // Restore all worlds
            for (Map.Entry<UUID, List<String>> entry : replacedLocations.entrySet()) {
                World world = Bukkit.getWorld(entry.getKey());
                if (world != null) {
                    for (String locString : entry.getValue()) {
                        toRestore.add(new LocationRestore(world, locString, entry.getKey()));
                    }
                }
            }
        } else {
            // Restore specific world
            UUID worldUUID = worldFilter.getUID();
            if (replacedLocations.containsKey(worldUUID)) {
                for (String locString : replacedLocations.get(worldUUID)) {
                    toRestore.add(new LocationRestore(worldFilter, locString, worldUUID));
                }
            }
        }
        
        // Process in batches on the main thread to avoid lag
        final int BATCH_SIZE = 50; // Process 50 blocks per tick
        final int totalBatches = (int) Math.ceil((double) toRestore.size() / BATCH_SIZE);
        
        Bukkit.getScheduler().runTask(plugin, new Runnable() {
            int currentBatch = 0;
            
            @Override
            public void run() {
                int start = currentBatch * BATCH_SIZE;
                int end = Math.min(start + BATCH_SIZE, toRestore.size());
                
                for (int i = start; i < end; i++) {
                    LocationRestore lr = toRestore.get(i);
                    try {
                        Location location = deserializeLocation(lr.world, lr.locString);
                        
                        // Skip if chunk not loaded
                        if (!isChunkLoaded(location) && !loadChunkIfNeeded(location)) {
                            session.incrementCompleted();
                            continue;
                        }
                        
                        Block block = location.getBlock();
                        
                        // Only restore if the block is still Netherrack
                        if (block.getType() == Material.NETHERRACK) {
                            block.setType(Material.ANCIENT_DEBRIS);
                            restoredCount.incrementAndGet();
                        }
                        
                        session.incrementCompleted();
                    } catch (Exception e) {
                        plugin.getLogger().warning("Error restoring Ancient Debris at " + lr.locString + ": " + e.getMessage());
                        session.incrementCompleted();
                    }
                }
                
                currentBatch++;
                
                if (currentBatch < totalBatches) {
                    // Schedule next batch
                    Bukkit.getScheduler().runTask(plugin, this);
                } else {
                    // All done - clean up
                    cleanupAfterRestore(worldFilter);
                    
                    // Notify progress tracker
                    if (plugin.getRestorationProgressTracker() != null) {
                        plugin.getRestorationProgressTracker().completeSession(session.getSessionId(), restoredCount.get());
                    }
                }
            }
        });
    }
    
    /**
     * Schedules restoration work on Folia (region scheduler per location)
     * @param session The restoration session
     * @param worldFilter The world to restore in, or null for all worlds
     * @param restoredCount Counter for restored blocks
     */
    private void scheduleFoliaRestoration(RestorationSession session, World worldFilter, AtomicInteger restoredCount) {
        // Collect all locations to process
        List<LocationRestore> toRestore = new ArrayList<>();
        
        if (worldFilter == null) {
            // Restore all worlds
            for (Map.Entry<UUID, List<String>> entry : replacedLocations.entrySet()) {
                World world = Bukkit.getWorld(entry.getKey());
                if (world != null) {
                    for (String locString : entry.getValue()) {
                        toRestore.add(new LocationRestore(world, locString, entry.getKey()));
                    }
                }
            }
        } else {
            // Restore specific world
            UUID worldUUID = worldFilter.getUID();
            if (replacedLocations.containsKey(worldUUID)) {
                for (String locString : replacedLocations.get(worldUUID)) {
                    toRestore.add(new LocationRestore(worldFilter, locString, worldUUID));
                }
            }
        }
        
        // Count down latch to know when all are done
        CountDownLatch latch = new CountDownLatch(toRestore.size());
        
        // Schedule each location on its region
        for (LocationRestore lr : toRestore) {
            try {
                Location location = deserializeLocation(lr.world, lr.locString);
                
                // Skip if chunk not loaded
                if (!isChunkLoaded(location) && !loadChunkIfNeeded(location)) {
                    session.incrementCompleted();
                    latch.countDown();
                    continue;
                }
                
                // Schedule on region
                Bukkit.getRegionScheduler().execute(plugin, location, () -> {
                    try {
                        Block block = location.getBlock();
                        
                        // Only restore if the block is still Netherrack
                        if (block.getType() == Material.NETHERRACK) {
                            block.setType(Material.ANCIENT_DEBRIS);
                            restoredCount.incrementAndGet();
                        }
                        
                        session.incrementCompleted();
                    } catch (Exception e) {
                        plugin.getLogger().warning("Error restoring Ancient Debris: " + e.getMessage());
                        session.incrementCompleted();
                    } finally {
                        latch.countDown();
                    }
                });
            } catch (Exception e) {
                plugin.getLogger().warning("Error scheduling restoration at " + lr.locString + ": " + e.getMessage());
                session.incrementCompleted();
                latch.countDown();
            }
        }
        
        // Wait for completion in a separate async task
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                latch.await(5, TimeUnit.MINUTES); // Wait up to 5 minutes
            } catch (InterruptedException e) {
                plugin.getLogger().warning("Interrupted while waiting for restoration: " + e.getMessage());
                Thread.currentThread().interrupt();
            }
            
            // Clean up on main thread (for Paper compatibility)
            Bukkit.getScheduler().runTask(plugin, () -> {
                cleanupAfterRestore(worldFilter);
                
                // Notify progress tracker
                if (plugin.getRestorationProgressTracker() != null) {
                    plugin.getRestorationProgressTracker().completeSession(session.getSessionId(), restoredCount.get());
                }
            });
        });
    }
    
    /**
     * Cleans up restored locations from storage
     * @param worldFilter The world filter, or null for all worlds
     */
    private void cleanupAfterRestore(World worldFilter) {
        if (worldFilter == null) {
            replacedLocations.clear();
        } else {
            replacedLocations.remove(worldFilter.getUID());
        }
        saveStorageAsync();
    }
    
    /**
     * Helper class to hold location restoration data
     */
    private static class LocationRestore {
        final World world;
        final String locString;
        final UUID worldUUID;
        
        LocationRestore(World world, String locString, UUID worldUUID) {
            this.world = world;
            this.locString = locString;
            this.worldUUID = worldUUID;
        }
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
    
    /**
     * Checks if the server is running on Folia
     * @return true if running on Folia, false otherwise
     */
    private boolean checkFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
} 