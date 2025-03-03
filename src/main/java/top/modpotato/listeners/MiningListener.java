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

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import top.modpotato.util.DebrisStorage;

import java.util.logging.Logger;

/**
 * Handles mining of Ancient Debris and converts it to Netherrack
 */
public class MiningListener implements Listener {
    private final DebrisStorage debrisStorage;
    private final boolean replaceAncientDebris;
    private final boolean replaceOnChunkLoad;
    private final Logger logger;
    
    // Maximum number of Ancient Debris to replace per chunk to prevent lag
    private static final int MAX_REPLACEMENTS_PER_CHUNK = 50;
    
    /**
     * Creates a new MiningListener
     * @param debrisStorage The debris storage
     * @param replaceAncientDebris Whether to replace Ancient Debris when mined
     * @param replaceOnChunkLoad Whether to replace Ancient Debris when chunks are loaded
     */
    public MiningListener(DebrisStorage debrisStorage, 
                          boolean replaceAncientDebris, boolean replaceOnChunkLoad) {
        this.debrisStorage = debrisStorage;
        this.replaceAncientDebris = replaceAncientDebris;
        this.replaceOnChunkLoad = replaceOnChunkLoad;
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
        
        // Skip if we've already processed this location
        if (debrisStorage.containsLocation(block.getLocation())) {
            return;
        }
        
        try {
            // Store the location before replacing
            if (debrisStorage.addLocation(block.getLocation())) {
                // Replace the block with Netherrack
                block.setType(Material.NETHERRACK);
                
                // Notify the player
                Player player = event.getPlayer();
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
        
        // Only process newly generated chunks
        if (!event.isNewChunk()) {
            return;
        }
        
        // Only process nether chunks since Ancient Debris only generates in the Nether
        World world = event.getWorld();
        if (world.getEnvironment() != Environment.NETHER) {
            return;
        }
        
        try {
            int replacementCount = 0;
            
            // Get all blocks in the chunk
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    // Ancient Debris only generates in the Nether between Y=8 and Y=119
                    for (int y = 8; y < 120; y++) {
                        // Limit the number of replacements per chunk to prevent lag
                        if (replacementCount >= MAX_REPLACEMENTS_PER_CHUNK) {
                            logger.warning("Too many Ancient Debris found in chunk at " + 
                                          event.getChunk().getX() + "," + event.getChunk().getZ() + 
                                          " in world " + world.getName() + 
                                          ". Limiting replacements to " + MAX_REPLACEMENTS_PER_CHUNK);
                            return;
                        }
                        
                        Block block = event.getChunk().getBlock(x, y, z);
                        if (block.getType() == Material.ANCIENT_DEBRIS) {
                            // Skip if we've already processed this location
                            if (debrisStorage.containsLocation(block.getLocation())) {
                                continue;
                            }
                            
                            // Store the location before replacing
                            if (debrisStorage.addLocation(block.getLocation())) {
                                // Replace the block with Netherrack
                                block.setType(Material.NETHERRACK);
                                replacementCount++;
                            }
                        }
                    }
                }
            }
            
            if (replacementCount > 0) {
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