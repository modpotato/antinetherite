package top.modpotato.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import top.modpotato.util.NetheriteDetector;

/**
 * Handles dropped Netherite items
 * This listener doesn't prevent dropping, but can be used to remove dropped Netherite items
 */
public class DropListener implements Listener {
    
    private final boolean removeDropped;
    private final NetheriteDetector netheriteDetector;
    
    /**
     * Creates a new DropListener
     * @param removeDropped Whether to remove dropped Netherite items
     * @param netheriteDetector The Netherite detector
     */
    public DropListener(boolean removeDropped, NetheriteDetector netheriteDetector) {
        this.removeDropped = removeDropped;
        this.netheriteDetector = netheriteDetector;
    }
    
    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (!removeDropped) {
            return;
        }
        
        ItemStack item = event.getItemDrop().getItemStack();
        if (item != null && netheriteDetector.isNetheriteItem(item)) {
            // Remove the dropped item entity
            event.getItemDrop().remove();
        }
    }
} 