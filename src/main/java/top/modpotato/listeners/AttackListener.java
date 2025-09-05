package top.modpotato.listeners;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import top.modpotato.config.Config;
import top.modpotato.util.NetheriteDetector;

/**
 * Prevents attacking with Netherite weapons
 */
public class AttackListener implements Listener {
    private final NetheriteDetector netheriteDetector;
    private final Config config;
    
    /**
     * Creates a new AttackListener
     * @param netheriteDetector The Netherite detector
     * @param config The configuration
     */
    public AttackListener(NetheriteDetector netheriteDetector, Config config) {
        this.netheriteDetector = netheriteDetector;
        this.config = config;
    }
    
    @EventHandler
    public void onAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getDamager();
        
        // Skip players in creative or spectator mode if configured to do so
        if (config.isIgnoreCreativeSpectator() && 
            (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR)) {
            return;
        }
        
        // Skip if attack cancellation is disabled
        if (!config.isCancelAttack()) {
            return;
        }
        
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item != null && netheriteDetector.isNetheriteItem(item)) {
            event.setCancelled(true);
            
            // Only notify the player if configured to do so
            if (config.isNotifyPlayers()) {
                player.sendMessage(Component.text("Attacking with Netherite items is not allowed!").color(NamedTextColor.RED));
            }
        }
    }
}