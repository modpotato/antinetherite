package top.modpotato.listeners;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.ItemStack;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import top.modpotato.config.Config;
import top.modpotato.util.NetheriteDetector;

/**
 * Prevents equipping Netherite armor
 */
public class EquipListener implements Listener {
    private final NetheriteDetector netheriteDetector;
    private final Config config;
    
    /**
     * Creates a new EquipListener
     * @param netheriteDetector The Netherite detector
     * @param config The configuration
     */
    public EquipListener(NetheriteDetector netheriteDetector, Config config) {
        this.netheriteDetector = netheriteDetector;
        this.config = config;
    }
    
    @EventHandler
    public void onEquip(InventoryClickEvent event) {
        if (event.getSlotType() != SlotType.ARMOR) {
            return;
        }
        
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        
        // Skip players in creative or spectator mode if configured to do so
        if (config.isIgnoreCreativeSpectator() && 
            (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR)) {
            return;
        }

        ItemStack item = event.getCursor();
        if (item != null && netheriteDetector.isNetheriteItem(item)) {
            event.setCancelled(true);
            player.sendMessage(Component.text("Equipping Netherite armor is not allowed!").color(NamedTextColor.RED));
        }
    }
}