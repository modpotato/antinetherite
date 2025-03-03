package top.modpotato.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import top.modpotato.util.NetheriteDetector;

/**
 * Prevents players from picking up Netherite items
 */
public class PickupListener implements Listener {
    private final NetheriteDetector netheriteDetector;
    
    /**
     * Creates a new PickupListener
     * @param netheriteDetector The Netherite detector
     */
    public PickupListener(NetheriteDetector netheriteDetector) {
        this.netheriteDetector = netheriteDetector;
    }
    
    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        ItemStack item = event.getItem().getItemStack();
        if (item != null && netheriteDetector.isNetheriteItem(item)) {
            event.setCancelled(true);
            Player player = (Player) event.getEntity();
            player.sendMessage(Component.text("Picking up Netherite items is not allowed!").color(NamedTextColor.RED));
        }
    }
} 