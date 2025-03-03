package top.modpotato.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import top.modpotato.util.NetheriteDetector;

/**
 * Prevents attacking with Netherite weapons
 */
public class AttackListener implements Listener {
    private final NetheriteDetector netheriteDetector;
    
    /**
     * Creates a new AttackListener
     * @param netheriteDetector The Netherite detector
     */
    public AttackListener(NetheriteDetector netheriteDetector) {
        this.netheriteDetector = netheriteDetector;
    }
    
    @EventHandler
    public void onAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getDamager();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item != null && netheriteDetector.isNetheriteItem(item)) {
            event.setCancelled(true);
            player.sendMessage(Component.text("Attacking with Netherite items is not allowed!").color(NamedTextColor.RED));
        }
    }
}