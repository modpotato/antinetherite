package top.modpotato.listeners;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import top.modpotato.config.Config;
import top.modpotato.util.NetheriteDetector;

/**
 * Prevents crafting of Netherite items
 */
public class CraftListener implements Listener {
    private final NetheriteDetector netheriteDetector;
    private final Config config;
    
    /**
     * Creates a new CraftListener
     * @param netheriteDetector The Netherite detector
     * @param config The configuration
     */
    public CraftListener(NetheriteDetector netheriteDetector, Config config) {
        this.netheriteDetector = netheriteDetector;
        this.config = config;
    }
    
    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        
        // Skip players in creative or spectator mode if configured to do so
        if (config.isIgnoreCreativeSpectator() && 
            (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR)) {
            return;
        }
        
        ItemStack result = event.getRecipe().getResult();
        if (netheriteDetector.isNetheriteItem(result)) {
            event.setCancelled(true);
            player.sendMessage(Component.text("Crafting Netherite items is not allowed!").color(NamedTextColor.RED));
        }
    }
}
