package top.modpotato.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import top.modpotato.Main;
import top.modpotato.util.NetheriteDetector;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Handles the scheduling of netherite removal tasks
 * with compatibility for both Bukkit and Folia
 */
public class NetheriteRemover {
    private final Main plugin;
    private BukkitTask bukkitTask;
    private Object foliaTask;
    private final boolean isFolia;
    private final NetheriteDetector netheriteDetector;
    
    /**
     * Creates a new NetheriteRemover instance
     * @param plugin The plugin instance
     * @param isFolia Whether the server is running Folia
     * @param netheriteDetector The Netherite detector
     */
    public NetheriteRemover(Main plugin, boolean isFolia, NetheriteDetector netheriteDetector) {
        this.plugin = plugin;
        this.isFolia = isFolia;
        this.netheriteDetector = netheriteDetector;
    }
    
    /**
     * Starts the netherite removal task
     * @param delayTicks The delay between checks in ticks
     */
    public void start(int delayTicks) {
        stop(); // Stop any existing task
        
        if (isFolia) {
            startFoliaTask(delayTicks);
        } else {
            startBukkitTask(delayTicks);
        }
    }
    
    /**
     * Stops the netherite removal task
     */
    public void stop() {
        if (bukkitTask != null) {
            bukkitTask.cancel();
            bukkitTask = null;
        }
        
        if (foliaTask != null) {
            try {
                // Use reflection to call cancel() on the Folia task
                foliaTask.getClass().getMethod("cancel").invoke(foliaTask);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to cancel Folia task: " + e.getMessage());
            }
            foliaTask = null;
        }
    }
    
    /**
     * Starts a Bukkit-based task
     * @param delayTicks The delay between checks in ticks
     */
    private void startBukkitTask(int delayTicks) {
        bukkitTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                    removeNetheriteFromPlayer(player);
                }
            }
        }.runTaskTimer(plugin, 0L, delayTicks);
    }
    
    /**
     * Starts a Folia-compatible task using region-aware schedulers
     * @param delayTicks The delay between checks in ticks
     */
    private void startFoliaTask(int delayTicks) {
        // Use Folia's global region scheduler to periodically check all players
        try {
            foliaTask = Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, scheduledTask -> {
                for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                    // Schedule task in the player's region to avoid cross-region operations
                    player.getScheduler().run(plugin, playerTask -> 
                        removeNetheriteFromPlayer(player), null);
                }
            }, 0L, delayTicks);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to start Folia task: " + e.getMessage());
            plugin.getLogger().severe("This may indicate an incompatibility with the current Folia version.");
        }
    }
    
    /**
     * Removes Netherite items from a player's inventory
     * @param player The player to check
     */
    private void removeNetheriteFromPlayer(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && netheriteDetector.isNetheriteItem(item)) {
                player.getInventory().remove(item);
            }
        }
    }
} 