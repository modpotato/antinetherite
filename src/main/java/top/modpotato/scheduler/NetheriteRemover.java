package top.modpotato.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import top.modpotato.Main;
import top.modpotato.config.Config;
import top.modpotato.util.NetheriteDetector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;

/**
 * Scheduler for removing Netherite items from player inventories
 */
public class NetheriteRemover {
    private final Main plugin;
    private final boolean isFolia;
    private final NetheriteDetector netheriteDetector;
    private final Logger logger;
    private final Config config;
    
    // Task references for both Bukkit and Folia
    private BukkitTask bukkitTask;
    private List<ScheduledTask> foliaTasks;
    
    /**
     * Creates a new NetheriteRemover
     * @param plugin The plugin instance
     * @param isFolia Whether the server is running on Folia
     * @param netheriteDetector The Netherite detector
     * @param config The plugin configuration
     */
    public NetheriteRemover(Main plugin, boolean isFolia, NetheriteDetector netheriteDetector, Config config) {
        this.plugin = plugin;
        this.isFolia = isFolia;
        this.netheriteDetector = netheriteDetector;
        this.logger = plugin.getLogger();
        this.config = config;
        this.foliaTasks = new ArrayList<>();
    }
    
    /**
     * Starts the Netherite removal task
     * @param delay The delay between checks in ticks
     */
    public void start(int delay) {
        stop(); // Stop any existing tasks
        
        if (isFolia) {
            startFoliaTask(delay);
        } else {
            startBukkitTask(delay);
        }
    }
    
    /**
     * Stops the Netherite removal task
     */
    public void stop() {
        if (isFolia) {
            stopFoliaTasks();
        } else {
            stopBukkitTask();
        }
    }
    
    /**
     * Starts the Netherite removal task using Bukkit scheduler
     * @param delay The delay between checks in ticks
     */
    private void startBukkitTask(int delay) {
        bukkitTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            AtomicInteger removedCount = new AtomicInteger(0);
            
            for (Player player : Bukkit.getOnlinePlayers()) {
                checkPlayerInventory(player, removedCount);
            }
            
            if (removedCount.get() > 0 && config.isLogInventoryRemovals()) {
                logger.info("Removed " + removedCount.get() + " Netherite items from player inventories");
            }
        }, delay, delay);
    }
    
    /**
     * Stops the Netherite removal task using Bukkit scheduler
     */
    private void stopBukkitTask() {
        if (bukkitTask != null) {
            bukkitTask.cancel();
            bukkitTask = null;
        }
    }
    
    /**
     * Starts the Netherite removal task using Folia scheduler
     * @param delay The delay between checks in ticks
     */
    private void startFoliaTask(int delay) {
        foliaTasks = new ArrayList<>();
        
        // for Folia, we need to schedule a task for each player
        for (Player player : Bukkit.getOnlinePlayers()) {
            schedulePlayerTask(player, delay);
        }
        
        // schedule a global task to handle new players
        ScheduledTask globalTask = Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, (task) -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                // check if this player already has a task
                for (ScheduledTask playerTask : foliaTasks) {
                    // assume playerTask is cancelled if the player is offline
                    if (playerTask.isCancelled()) {
                        foliaTasks.remove(playerTask);
                    }
                }
                
                // schedule a new task for this player
                schedulePlayerTask(player, delay);
            }
        }, 1, delay);
        
        foliaTasks.add(globalTask);
    }
    
    /**
     * Schedules a task for a specific player using Folia scheduler
     * @param player The player to schedule for
     * @param delay The delay between checks in ticks
     */
    private void schedulePlayerTask(Player player, int delay) {
        ScheduledTask task = player.getScheduler().runAtFixedRate(plugin, (scheduledTask) -> {
            AtomicInteger removedCount = new AtomicInteger(0);
            checkPlayerInventory(player, removedCount);
            
            if (removedCount.get() > 0 && config.isLogInventoryRemovals()) {
                logger.info("Removed " + removedCount.get() + " Netherite items from " + player.getName() + "'s inventory");
            }
            
            // if the player is offline, cancel this task
            if (!player.isOnline()) {
                scheduledTask.cancel();
                foliaTasks.remove(scheduledTask);
            }
        }, null, 1, delay);
        
        foliaTasks.add(task);
    }
    
    /**
     * Stops all Folia tasks
     */
    private void stopFoliaTasks() {
        for (ScheduledTask task : foliaTasks) {
            task.cancel();
        }
        foliaTasks.clear();
    }
    
    /**
     * Checks a player's inventory for Netherite items and removes them
     * @param player The player to check
     * @param removedCount Counter for removed items
     */
    private void checkPlayerInventory(Player player, AtomicInteger removedCount) {
        // Skip players with permission
        if (player.hasPermission("antinetherite.bypass")) {
            return;
        }
        
        // Skip players in creative or spectator mode if configured to do so
        if (config.isIgnoreCreativeSpectator() && 
            (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR)) {
            return;
        }
        
        if (config.isEnableDestructiveActions()) {
            // Destructive mode: Remove items from inventory
            removeNetheriteItems(player, removedCount);
        } else {
            // Non-destructive mode: Notify player but don't remove items
            int count = countNetheriteItems(player);
            if (count > 0) {
                // Always notify for non-destructive actions since we're not actually removing items
                player.sendMessage(Component.text("You have " + count + " Netherite items in your inventory that are not allowed on this server.").color(NamedTextColor.RED));
                removedCount.addAndGet(count);
            }
        }
    }
    
    /**
     * Removes Netherite items from a player's inventory
     * @param player The player to check
     * @param removedCount Counter for removed items
     */
    private void removeNetheriteItems(Player player, AtomicInteger removedCount) {
        int itemsRemoved = 0;
        
        // Check main inventory
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item != null && netheriteDetector.isNetheriteItem(item)) {
                player.getInventory().setItem(i, null);
                removedCount.incrementAndGet();
                itemsRemoved++;
            }
        }
        
        // Check armor slots
        for (ItemStack item : player.getInventory().getArmorContents()) {
            if (item != null && netheriteDetector.isNetheriteItem(item)) {
                // We can't directly modify the armor contents array, so we need to set each slot individually
                if (item.equals(player.getInventory().getHelmet())) {
                    player.getInventory().setHelmet(null);
                    removedCount.incrementAndGet();
                    itemsRemoved++;
                } else if (item.equals(player.getInventory().getChestplate())) {
                    player.getInventory().setChestplate(null);
                    removedCount.incrementAndGet();
                    itemsRemoved++;
                } else if (item.equals(player.getInventory().getLeggings())) {
                    player.getInventory().setLeggings(null);
                    removedCount.incrementAndGet();
                    itemsRemoved++;
                } else if (item.equals(player.getInventory().getBoots())) {
                    player.getInventory().setBoots(null);
                    removedCount.incrementAndGet();
                    itemsRemoved++;
                }
            }
        }
        
        // Check offhand
        ItemStack offhand = player.getInventory().getItemInOffHand();
        if (offhand != null && netheriteDetector.isNetheriteItem(offhand)) {
            player.getInventory().setItemInOffHand(null);
            removedCount.incrementAndGet();
            itemsRemoved++;
        }
        
        // Always notify for destructive actions
        if (itemsRemoved > 0) {
            player.sendMessage(Component.text("Removed " + itemsRemoved + " Netherite items from your inventory.").color(NamedTextColor.RED));
        }
    }
    
    /**
     * Counts Netherite items in a player's inventory
     * @param player The player to check
     * @return The number of Netherite items found
     */
    private int countNetheriteItems(Player player) {
        int count = 0;
        
        // Check main inventory
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && netheriteDetector.isNetheriteItem(item)) {
                count++;
            }
        }
        
        // Check armor slots
        for (ItemStack item : player.getInventory().getArmorContents()) {
            if (item != null && netheriteDetector.isNetheriteItem(item)) {
                count++;
            }
        }
        
        // Check offhand
        ItemStack offhand = player.getInventory().getItemInOffHand();
        if (offhand != null && netheriteDetector.isNetheriteItem(offhand)) {
            count++;
        }
        
        return count;
    }
} 