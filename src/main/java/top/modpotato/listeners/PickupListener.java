package top.modpotato.listeners;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import top.modpotato.config.Config;
import top.modpotato.util.NetheriteDetector;

/**
 * Prevents players from picking up Netherite items
 */
public class PickupListener implements Listener {
    private final NetheriteDetector netheriteDetector;
    private final Config config;
    
    /**
     * Creates a new PickupListener
     * @param netheriteDetector The Netherite detector
     * @param config The configuration
     */
    public PickupListener(NetheriteDetector netheriteDetector, Config config) {
        this.netheriteDetector = netheriteDetector;
        this.config = config;
    }
    
    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getEntity();
        
        // Skip players in creative or spectator mode if configured to do so
        if (config.isIgnoreCreativeSpectator() && 
            (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR)) {
            return;
        }
        
        ItemStack item = event.getItem().getItemStack();
        if (item != null && netheriteDetector.isNetheriteItem(item)) {
            event.setCancelled(true);
            
            // Only notify the player if configured to do so
            if (config.isNotifyPlayers()) {
                player.sendMessage(Component.text("Picking up Netherite items is not allowed!").color(NamedTextColor.RED));
            }
        }
    }
} 