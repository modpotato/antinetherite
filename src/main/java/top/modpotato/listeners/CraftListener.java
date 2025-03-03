package top.modpotato.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import top.modpotato.util.NetheriteDetector;

/**
 * Prevents crafting of Netherite items
 */
public class CraftListener implements Listener {
    private final NetheriteDetector netheriteDetector;
    
    /**
     * Creates a new CraftListener
     * @param netheriteDetector The Netherite detector
     */
    public CraftListener(NetheriteDetector netheriteDetector) {
        this.netheriteDetector = netheriteDetector;
    }
    
    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        ItemStack result = event.getRecipe().getResult();
        if (netheriteDetector.isNetheriteItem(result)) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            player.sendMessage(Component.text("Crafting Netherite items is not allowed!").color(NamedTextColor.RED));
        }
    }
}
