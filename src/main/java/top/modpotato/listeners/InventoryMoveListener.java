package top.modpotato.listeners;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import top.modpotato.config.Config;
import top.modpotato.util.NetheriteDetector;

/**
 * Prevents players from moving Netherite items in inventories
 */
public class InventoryMoveListener implements Listener {
    private final NetheriteDetector netheriteDetector;
    private final Config config;
    
    /**
     * Creates a new InventoryMoveListener
     * @param netheriteDetector The Netherite detector
     * @param config The configuration
     */
    public InventoryMoveListener(NetheriteDetector netheriteDetector, Config config) {
        this.netheriteDetector = netheriteDetector;
        this.config = config;
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        
        // Skip players in creative or spectator mode if configured to do so
        if (config.isIgnoreCreativeSpectator() && 
            (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR)) {
            return;
        }
        
        // Check current item
        ItemStack currentItem = event.getCurrentItem();
        if (currentItem != null && netheriteDetector.isNetheriteItem(currentItem)) {
            event.setCancelled(true);
            
            // Only notify the player if configured to do so
            if (config.isNotifyPlayers()) {
                player.sendMessage(Component.text("Moving Netherite items is not allowed!").color(NamedTextColor.RED));
            }
            return;
        }
        
        // Check cursor item
        ItemStack cursorItem = event.getCursor();
        if (cursorItem != null && netheriteDetector.isNetheriteItem(cursorItem)) {
            event.setCancelled(true);
            
            // Only notify the player if configured to do so
            if (config.isNotifyPlayers()) {
                player.sendMessage(Component.text("Moving Netherite items is not allowed!").color(NamedTextColor.RED));
            }
        }
    }
    
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        
        // Skip players in creative or spectator mode if configured to do so
        if (config.isIgnoreCreativeSpectator() && 
            (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR)) {
            return;
        }
        
        ItemStack draggedItem = event.getOldCursor();
        if (draggedItem != null && netheriteDetector.isNetheriteItem(draggedItem)) {
            event.setCancelled(true);
            
            // Only notify the player if configured to do so
            if (config.isNotifyPlayers()) {
                player.sendMessage(Component.text("Moving Netherite items is not allowed!").color(NamedTextColor.RED));
            }
        }
    }
} 