package top.modpotato.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import top.modpotato.util.NetheriteDetector;

/**
 * Prevents players from moving Netherite items in inventories
 */
public class InventoryMoveListener implements Listener {
    private final NetheriteDetector netheriteDetector;
    
    /**
     * Creates a new InventoryMoveListener
     * @param netheriteDetector The Netherite detector
     */
    public InventoryMoveListener(NetheriteDetector netheriteDetector) {
        this.netheriteDetector = netheriteDetector;
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        // Check current item
        ItemStack currentItem = event.getCurrentItem();
        if (currentItem != null && netheriteDetector.isNetheriteItem(currentItem)) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            player.sendMessage(Component.text("Moving Netherite items is not allowed!").color(NamedTextColor.RED));
            return;
        }
        
        // Check cursor item
        ItemStack cursorItem = event.getCursor();
        if (cursorItem != null && netheriteDetector.isNetheriteItem(cursorItem)) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            player.sendMessage(Component.text("Moving Netherite items is not allowed!").color(NamedTextColor.RED));
        }
    }
    
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        ItemStack draggedItem = event.getOldCursor();
        if (draggedItem != null && netheriteDetector.isNetheriteItem(draggedItem)) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            player.sendMessage(Component.text("Moving Netherite items is not allowed!").color(NamedTextColor.RED));
        }
    }
} 