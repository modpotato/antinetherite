package top.modpotato.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.GameMode;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import top.modpotato.util.DebrisStorage;
import top.modpotato.config.Config;

import java.util.logging.Logger;

/**
 * Handles mining of Ancient Debris and converts it to Netherrack
 */
public class MiningListener implements Listener {
    private final DebrisStorage debrisStorage;
    private final boolean replaceAncientDebris;
    private final boolean replaceOnChunkLoad;
    private final boolean onlyReplaceGeneratedChunks;
    private final Logger logger;
    private final Config config;
    
    /**
     * Creates a new MiningListener
     * @param debrisStorage The debris storage
     * @param replaceAncientDebris Whether to replace Ancient Debris when mined
     * @param replaceOnChunkLoad Whether to replace Ancient Debris when chunks are loaded
     * @param onlyReplaceGeneratedChunks Whether to only replace Ancient Debris in generated chunks
     * @param config The plugin configuration
     */
    public MiningListener(DebrisStorage debrisStorage, 
                          boolean replaceAncientDebris, 
                          boolean replaceOnChunkLoad,
                          boolean onlyReplaceGeneratedChunks,
                          Config config) {
        this.debrisStorage = debrisStorage;
        this.replaceAncientDebris = replaceAncientDebris;
        this.replaceOnChunkLoad = replaceOnChunkLoad;
        this.onlyReplaceGeneratedChunks = onlyReplaceGeneratedChunks;
        this.config = config;
        this.logger = Bukkit.getLogger();
    }
    
    /**
     * Handles block damage events to replace Ancient Debris with Netherrack
     * This is triggered when a player starts breaking a block
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockDamage(BlockDamageEvent event) {
        if (!replaceAncientDebris) {
            return;
        }
        
        Block block = event.getBlock();
        
        // Skip if the block is not Ancient Debris
        if (block.getType() != Material.ANCIENT_DEBRIS) {
            return;
        }
        
        // Skip if player is in creative or spectator mode and we're ignoring those modes
        Player player = event.getPlayer();
        if (config.isIgnoreCreativeSpectator() && 
            (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR)) {
            return;
        }
        
        // Skip if we've already processed this location
        if (debrisStorage.containsLocation(block.getLocation())) {
            return;
        }
        
        try {
            // Replace the block with Netherrack
            block.setType(Material.NETHERRACK);
            
            // Store the location if configured to do so
            if (config.isSaveReplacedLocations()) {
                debrisStorage.addLocation(block.getLocation());
            }
            
            // Notify the player if configured to do so
            if (config.isNotifyPlayers()) {
                player.sendMessage(Component.text("Ancient Debris has been converted to Netherrack!").color(NamedTextColor.RED));
            }
            
            // Cancel the event
            event.setCancelled(true);
        } catch (Exception e) {
            // Log the error but don't crash the plugin
            logger.warning("Error replacing Ancient Debris at " + block.getLocation() + ": " + e.getMessage());
        }
    }
    
    /**
     * Handles chunk load events to replace Ancient Debris with Netherrack
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onChunkLoad(ChunkLoadEvent event) {
        if (!replaceOnChunkLoad) {
            return;
        }
        
        // Check if we should only process newly generated chunks
        if (onlyReplaceGeneratedChunks && !event.isNewChunk()) {
            return;
        }
        
        // Only process nether chunks since Ancient Debris only generates in the Nether
        World world = event.getWorld();
        if (world.getEnvironment() != Environment.NETHER) {
            return;
        }
        
        try {
            int replacementCount = 0;
            int maxReplacements = config.getMaxReplacementsPerChunk();
            
            // Get all blocks in the chunk
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    // Ancient Debris only generates in the Nether between Y=8 and Y=119
                    for (int y = 8; y < 120; y++) {
                        // Limit the number of replacements per chunk to prevent lag
                        // Unless maxReplacements is -1, which means no limit
                        if (maxReplacements != -1 && replacementCount >= maxReplacements) {
                            logger.warning("Too many Ancient Debris found in chunk at " + 
                                          event.getChunk().getX() + "," + event.getChunk().getZ() + 
                                          " in world " + world.getName() + 
                                          ". Limiting replacements to " + maxReplacements);
                            return;
                        }
                        
                        Block block = event.getChunk().getBlock(x, y, z);
                        if (block.getType() == Material.ANCIENT_DEBRIS) {
                            // Skip if we've already processed this location
                            if (debrisStorage.containsLocation(block.getLocation())) {
                                continue;
                            }
                            
                            // Replace the block with Netherrack
                            block.setType(Material.NETHERRACK);
                            replacementCount++;
                            
                            // Store the location if configured to do so
                            if (config.isSaveReplacedLocations()) {
                                debrisStorage.addLocation(block.getLocation());
                            }
                        }
                    }
                }
            }
            
            if (replacementCount > 0 && config.isLogDebrisReplacements()) {
                logger.info("Replaced " + replacementCount + " Ancient Debris in chunk at " + 
                           event.getChunk().getX() + "," + event.getChunk().getZ() + 
                           " in world " + world.getName());
            }
        } catch (Exception e) {
            // Log the error but don't crash the plugin
            logger.warning("Error replacing Ancient Debris in chunk at " + 
                          event.getChunk().getX() + "," + event.getChunk().getZ() + 
                          " in world " + world.getName() + ": " + e.getMessage());
        }
    }
    
    /**
     * Restores all replaced Ancient Debris
     * @return The number of blocks restored
     */
    public int restoreAllDebris() {
        return debrisStorage.restoreAllDebris();
    }
    
    /**
     * Restores replaced Ancient Debris in a specific world
     * @param world The world to restore Ancient Debris in
     * @return The number of blocks restored
     */
    public int restoreDebrisInWorld(World world) {
        return debrisStorage.restoreDebrisInWorld(world);
    }
} 