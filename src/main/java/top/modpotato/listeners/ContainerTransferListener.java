package top.modpotato.listeners;

import org.bukkit.GameMode;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.ItemStack;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import top.modpotato.config.Config;
import top.modpotato.util.NetheriteDetector;

/**
 * Prevents automated container (hopper, hopper minecart, etc.) transfers of Netherite items
 * when enabled via configuration. This plugs the bypass where players could insert Netherite
 * into containers via hoppers to avoid direct movement restrictions.
 */
public class ContainerTransferListener implements Listener {
    private final NetheriteDetector detector;
    private final Config config;

    public ContainerTransferListener(NetheriteDetector detector, Config config) {
        this.detector = detector;
        this.config = config;
    }

    @EventHandler
    public void onInventoryMoveItem(InventoryMoveItemEvent event) {
        if (!config.isCancelContainerTransfer()) {
            return; // feature disabled
        }

        ItemStack item = event.getItem();
        if (item == null || !detector.isNetheriteItem(item)) {
            return; // not netherite
        }

        // Cancel the automated move
        event.setCancelled(true);

        // Attempt to notify a nearby player viewing the source or destination (optional UX)
        // Only notify if configured to notify players
        if (!config.isNotifyPlayers()) {
            return;
        }

        // Notify viewers of either inventory (players with it open). Avoid spamming many players.
        // We'll notify the first eligible player.
        HumanEntity notify = null;
        if (!event.getSource().getViewers().isEmpty()) {
            notify = event.getSource().getViewers().get(0);
        } else if (!event.getDestination().getViewers().isEmpty()) {
            notify = event.getDestination().getViewers().get(0);
        }
        if (notify instanceof Player player) {
            if (!(config.isIgnoreCreativeSpectator() && (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR))) {
                player.sendMessage(Component.text("Automated transfer of Netherite items is blocked!").color(NamedTextColor.RED));
            }
        }
    }
}
