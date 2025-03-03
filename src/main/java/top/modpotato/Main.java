package top.modpotato;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.HandlerList;

import top.modpotato.listeners.AttackListener;
import top.modpotato.listeners.CraftListener;
import top.modpotato.listeners.DropListener;
import top.modpotato.listeners.EquipListener;
import top.modpotato.listeners.InventoryMoveListener;
import top.modpotato.listeners.MiningListener;
import top.modpotato.listeners.PickupListener;
import top.modpotato.commands.AntiNetheriteCommand;
import top.modpotato.config.Config;
import top.modpotato.scheduler.NetheriteRemover;
import top.modpotato.util.DebrisStorage;
import top.modpotato.util.NetheriteDetector;

/**
 * Main plugin class for AntiNetherite
 */
public class Main extends JavaPlugin {
    private Config config;
    private NetheriteRemover netheriteRemover;
    private NetheriteDetector netheriteDetector;
    private DebrisStorage debrisStorage;
    
    private CraftListener craftListener;
    private EquipListener equipListener;
    private AttackListener attackListener;
    private PickupListener pickupListener;
    private DropListener dropListener;
    private InventoryMoveListener inventoryMoveListener;
    private MiningListener miningListener;
    private boolean isFolia;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        
        // Initialize config
        config = new Config(this);
        
        // Initialize Netherite detector
        netheriteDetector = new NetheriteDetector(config);
        
        // Initialize debris storage
        debrisStorage = new DebrisStorage(this);
        
        // Check if running on Folia
        isFolia = checkFolia();
        getLogger().info("Running on " + (isFolia ? "Folia" : "Bukkit") + " server");
        
        // Initialize netherite remover
        netheriteRemover = new NetheriteRemover(this, isFolia, netheriteDetector);
        
        // Start tasks based on config
        if (config.isClearNetherite()) {
            netheriteRemover.start(config.getDelayTicks());
        }

        // Register listeners
        registerListeners();
        
        // Register command
        getCommand("antinetherite").setExecutor(new AntiNetheriteCommand(this));
        
        getLogger().info("AntiNetherite has been enabled!");
    }

    @Override
    public void onDisable() {
        // Stop tasks
        if (netheriteRemover != null) {
            netheriteRemover.stop();
        }
        
        // Restore Ancient Debris if needed
        if (miningListener != null && (!config.isReplaceAncientDebris() && !config.isReplaceOnChunkLoad())) {
            miningListener.restoreAllDebris();
        }
        
        // Unregister listeners
        unregisterListeners();
        
        getLogger().info("AntiNetherite has been disabled!");
    }
    
    /**
     * Registers event listeners based on configuration
     */
    private void registerListeners() {
        if (config.isCancelCraft()) {
            craftListener = new CraftListener(netheriteDetector);
            getServer().getPluginManager().registerEvents(craftListener, this);
        }

        if (config.isCancelEquip()) {
            equipListener = new EquipListener(netheriteDetector);
            getServer().getPluginManager().registerEvents(equipListener, this);
        }

        if (config.isCancelAttack()) {
            attackListener = new AttackListener(netheriteDetector);
            getServer().getPluginManager().registerEvents(attackListener, this);
        }
        
        if (config.isCancelPickup()) {
            pickupListener = new PickupListener(netheriteDetector);
            getServer().getPluginManager().registerEvents(pickupListener, this);
        }
        
        dropListener = new DropListener(config.isRemoveDropped(), netheriteDetector);
        getServer().getPluginManager().registerEvents(dropListener, this);
        
        if (config.isCancelInventoryMove()) {
            inventoryMoveListener = new InventoryMoveListener(netheriteDetector);
            getServer().getPluginManager().registerEvents(inventoryMoveListener, this);
        }
        
        // Register mining listener if either ancient debris replacement option is enabled
        if (config.isReplaceAncientDebris() || config.isReplaceOnChunkLoad()) {
            miningListener = new MiningListener(netheriteDetector, debrisStorage, 
                                               config.isReplaceAncientDebris(), config.isReplaceOnChunkLoad());
            getServer().getPluginManager().registerEvents(miningListener, this);
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
        
        if (pickupListener != null) {
            HandlerList.unregisterAll(pickupListener);
            pickupListener = null;
        }
        
        if (dropListener != null) {
            HandlerList.unregisterAll(dropListener);
            dropListener = null;
        }
        
        if (inventoryMoveListener != null) {
            HandlerList.unregisterAll(inventoryMoveListener);
            inventoryMoveListener = null;
        }
        
        if (miningListener != null) {
            HandlerList.unregisterAll(miningListener);
            miningListener = null;
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
     * Updates a specific configuration value and applies changes
     * @param path The configuration path
     * @param value The new value
     */
    public void updateConfig(String path, Object value) {
        config.update(path, value);
        
        // Reload Netherite detector
        if (netheriteDetector != null) {
            netheriteDetector.reloadNetheriteItems();
        }
        
        // Re-register listeners and tasks based on new config
        unregisterListeners();
        
        if (netheriteRemover != null) {
            netheriteRemover.stop();
        }
        
        if (config.isClearNetherite()) {
            netheriteRemover.start(config.getDelayTicks());
        }
        
        registerListeners();
    }
    
    /**
     * Gets the current configuration value
     * @param path The configuration path
     * @return The configuration value
     */
    public Object getConfigValue(String path) {
        return config.getValue(path);
    }
    
    /**
     * Reloads the plugin configuration
     */
    public void reloadPluginConfig() {
        boolean oldReplaceAncientDebris = config.isReplaceAncientDebris();
        boolean oldReplaceOnChunkLoad = config.isReplaceOnChunkLoad();
        
        // Reload config
        config.reload();
        
        // Reload Netherite detector
        netheriteDetector.reloadNetheriteItems();
        
        // Stop tasks
        if (netheriteRemover != null) {
            netheriteRemover.stop();
        }
        
        // Restore Ancient Debris if needed
        if (miningListener != null && 
            (oldReplaceAncientDebris || oldReplaceOnChunkLoad) && 
            (!config.isReplaceAncientDebris() && !config.isReplaceOnChunkLoad())) {
            miningListener.restoreAllDebris();
        }
        
        // Unregister listeners
        unregisterListeners();
        
        // Register listeners again
        registerListeners();
        
        // Start tasks based on config
        if (config.isClearNetherite()) {
            netheriteRemover.start(config.getDelayTicks());
        }
        
        getLogger().info("AntiNetherite configuration reloaded.");
    }
    
    /**
     * Gets the Netherite detector
     * @return The Netherite detector
     */
    public NetheriteDetector getNetheriteDetector() {
        return netheriteDetector;
    }
    
    /**
     * Gets the debris storage
     * @return The debris storage
     */
    public DebrisStorage getDebrisStorage() {
        return debrisStorage;
    }
}