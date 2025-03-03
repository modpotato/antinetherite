package top.modpotato;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import top.modpotato.listeners.AttackListener;
import top.modpotato.listeners.CraftListener;
import top.modpotato.listeners.EquipListener;
import top.modpotato.commands.AntiNetheriteCommand;

/**
 * Main plugin class for AntiNetherite
 */
public class Main extends JavaPlugin {
    private int delay;
    private int multiplier;
    private boolean clearNetherite = false;
    private boolean cancelCraft = false;
    private boolean cancelEquip = false;
    private boolean cancelAttack = false;

    private BukkitRunnable removeNetheriteTask;
    private CraftListener craftListener;
    private EquipListener equipListener;
    private AttackListener attackListener;
    private boolean isFolia;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfig();
        
        // Check if running on Folia
        isFolia = checkFolia();
        
        if (clearNetherite) {
            if (isFolia) {
                getLogger().warning("The clear netherite feature is not fully compatible with Folia. Using region-aware scheduler instead.");
                scheduleRemoveNetheriteForFolia();
            } else {
                scheduleRemoveNetherite();
            }
        }

        registerListeners();
        
        // Register command
        getCommand("antinetherite").setExecutor(new AntiNetheriteCommand(this));
        
        getLogger().info("AntiNetherite has been enabled!");
    }

    @Override
    public void onDisable() {
        if (removeNetheriteTask != null) {
            removeNetheriteTask.cancel();
        }
        
        unregisterListeners();
        
        getLogger().info("AntiNetherite has been disabled!");
    }
    
    /**
     * Loads configuration values from config.yml
     */
    public void loadConfig() {
        reloadConfig();
        delay = getConfig().getInt("anti-netherite.delay", 1);
        multiplier = getConfig().getInt("anti-netherite.multiplier", 20);
        clearNetherite = getConfig().getBoolean("anti-netherite.clear", false);
        cancelCraft = getConfig().getBoolean("anti-netherite.cancel-craft", true);
        cancelEquip = getConfig().getBoolean("anti-netherite.cancel-equip", true);
        cancelAttack = getConfig().getBoolean("anti-netherite.cancel-attack", true);
    }
    
    /**
     * Registers event listeners based on configuration
     */
    private void registerListeners() {
        if (cancelCraft) {
            craftListener = new CraftListener();
            getServer().getPluginManager().registerEvents(craftListener, this);
        }

        if (cancelEquip) {
            equipListener = new EquipListener();
            getServer().getPluginManager().registerEvents(equipListener, this);
        }

        if (cancelAttack) {
            attackListener = new AttackListener();
            getServer().getPluginManager().registerEvents(attackListener, this);
        }
    }
    
    /**
     * Unregisters all event listeners
     */
    private void unregisterListeners() {
        if (craftListener != null) {
            HandlerList.unregisterAll(craftListener);
            craftListener = null;
        }

        if (equipListener != null) {
            HandlerList.unregisterAll(equipListener);
            equipListener = null;
        }

        if (attackListener != null) {
            HandlerList.unregisterAll(attackListener);
            attackListener = null;
        }
    }
    
    /**
     * Checks if the server is running on Folia
     * @return true if running on Folia, false otherwise
     */
    private boolean checkFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Schedules the task to remove Netherite items from player inventories (Bukkit scheduler)
     */
    private void scheduleRemoveNetherite() {
        removeNetheriteTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                    removeNetheriteFromPlayer(player);
                }
            }
        };
        removeNetheriteTask.runTaskTimer(this, 0L, delay * multiplier);
    }
    
    /**
     * Schedules the task to remove Netherite items from player inventories (Folia-compatible)
     */
    private void scheduleRemoveNetheriteForFolia() {
        // For Folia, we need to use the region-aware scheduler
        Bukkit.getGlobalRegionScheduler().runAtFixedRate(this, task -> {
            for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                // Schedule task in the player's region
                player.getScheduler().run(this, scheduledTask -> removeNetheriteFromPlayer(player), null);
            }
        }, 0L, delay * multiplier);
    }
    
    /**
     * Removes Netherite items from a player's inventory
     * @param player The player to check
     */
    private void removeNetheriteFromPlayer(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType().name().contains("NETHERITE")) {
                player.getInventory().remove(item);
            }
        }
    }
    
    /**
     * Updates a specific configuration value
     * @param path The configuration path
     * @param value The new value
     */
    public void updateConfig(String path, Object value) {
        getConfig().set(path, value);
        saveConfig();
        loadConfig();
        
        // Re-register listeners and tasks based on new config
        unregisterListeners();
        
        if (removeNetheriteTask != null) {
            removeNetheriteTask.cancel();
            removeNetheriteTask = null;
        }
        
        if (clearNetherite) {
            if (isFolia) {
                scheduleRemoveNetheriteForFolia();
            } else {
                scheduleRemoveNetherite();
            }
        }
        
        registerListeners();
    }
    
    /**
     * Gets the current configuration value
     * @param path The configuration path
     * @return The configuration value
     */
    public Object getConfigValue(String path) {
        return getConfig().get(path);
    }
}