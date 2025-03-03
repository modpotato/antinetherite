package top.modpotato.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import top.modpotato.config.Config;
import top.modpotato.util.NetheriteDetector;

/**
 * Handles dropped Netherite items
 * This listener can either cancel drop events or remove dropped Netherite items
 * based on the configuration
 */
public class DropListener implements Listener {
    
    private final boolean removeDropped;
    private final NetheriteDetector netheriteDetector;
    private final Config config;
    
    /**
     * Creates a new DropListener
     * @param removeDropped Whether to handle dropped Netherite items
     * @param netheriteDetector The Netherite detector
     * @param config The plugin configuration
     */
    public DropListener(boolean removeDropped, NetheriteDetector netheriteDetector, Config config) {
        this.removeDropped = removeDropped;
        this.netheriteDetector = netheriteDetector;
        this.config = config;
    }
    
    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (!removeDropped) {
            return;
        }
        
        ItemStack item = event.getItemDrop().getItemStack();
        if (item != null && netheriteDetector.isNetheriteItem(item)) {
            if (config.isEnableDestructiveActions()) {
                // Remove the dropped item entity (destructive)
                event.getItemDrop().remove();
            } else {
                // Cancel the drop event (non-destructive)
                event.setCancelled(true);
            }
        }
    }
} 