package top.modpotato.listeners;

import org.bukkit.Material;
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
import top.modpotato.util.NetheriteDetector;

/**
 * Handles mining of Ancient Debris and converts it to Netherrack
 */
public class MiningListener implements Listener {
    private final NetheriteDetector netheriteDetector;
    private final DebrisStorage debrisStorage;
    private final boolean replaceAncientDebris;
    private final boolean replaceOnChunkLoad;
    
    /**
     * Creates a new MiningListener
     * @param netheriteDetector The Netherite detector
     * @param debrisStorage The debris storage
     * @param replaceAncientDebris Whether to replace Ancient Debris when mined
     * @param replaceOnChunkLoad Whether to replace Ancient Debris when chunks are loaded
     */
    public MiningListener(NetheriteDetector netheriteDetector, DebrisStorage debrisStorage, 
                          boolean replaceAncientDebris, boolean replaceOnChunkLoad) {
        this.netheriteDetector = netheriteDetector;
        this.debrisStorage = debrisStorage;
        this.replaceAncientDebris = replaceAncientDebris;
        this.replaceOnChunkLoad = replaceOnChunkLoad;
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
        if (block.getType() == Material.ANCIENT_DEBRIS) {
            // Store the location before replacing
            debrisStorage.addLocation(block.getLocation());
            
            // Replace the block with Netherrack
            block.setType(Material.NETHERRACK);
            
            // Notify the player
            Player player = event.getPlayer();
            player.sendMessage(Component.text("Ancient Debris has been converted to Netherrack!").color(NamedTextColor.RED));
            
            // Cancel the event
            event.setCancelled(true);
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
        
        // Get all blocks in the chunk
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                // Ancient Debris only generates in the Nether between Y=8 and Y=119
                for (int y = 8; y < 120; y++) {
                    Block block = event.getChunk().getBlock(x, y, z);
                    if (block.getType() == Material.ANCIENT_DEBRIS) {
                        // Store the location before replacing
                        debrisStorage.addLocation(block.getLocation());
                        
                        // Replace the block with Netherrack
                        block.setType(Material.NETHERRACK);
                    }
                }
            }
        }
    }
    
    /**
     * Restores all replaced Ancient Debris
     */
    public void restoreAllDebris() {
        debrisStorage.restoreAllDebris();
    }
} 