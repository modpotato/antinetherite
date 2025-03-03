package top.modpotato.config;

import org.bukkit.configuration.file.FileConfiguration;
import top.modpotato.Main;

import java.util.List;

/**
 * Configuration manager for AntiNetherite plugin
 */
public class Config {
    private final Main plugin;
    
    // Configuration values
    private int delay;
    private int multiplier;
    private boolean clearNetherite;
    private boolean cancelCraft;
    private boolean cancelEquip;
    private boolean cancelAttack;
    private boolean cancelPickup;
    private boolean removeDropped;
    private boolean cancelInventoryMove;
    
    // Netherite detection settings
    private boolean useNameMatching;
    private List<String> netheriteItemsList;
    
    // Ancient Debris settings
    private boolean replaceAncientDebris;
    private boolean replaceOnChunkLoad;
    private boolean onlyReplaceGeneratedChunks;
    private boolean ensureChunksLoaded;
    
    // Performance settings
    private boolean restoreDebrisOnDisable;
    private boolean restoreDebrisOnConfigChange;
    private int maxReplacementsPerChunk;
    
    /**
     * Creates a new Config instance
     * @param plugin The plugin instance
     */
    public Config(Main plugin) {
        this.plugin = plugin;
        reload();
    }
    
    /**
     * Reloads the configuration from disk
     */
    public void reload() {
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();
        
        // Load timing settings
        delay = config.getInt("anti-netherite.timing.delay", 1);
        multiplier = config.getInt("anti-netherite.timing.multiplier", 20);
        
        // Load inventory protection settings
        clearNetherite = config.getBoolean("anti-netherite.inventory.clear", false);
        cancelInventoryMove = config.getBoolean("anti-netherite.inventory.cancel-move", true);
        
        // Load item interaction settings
        cancelCraft = config.getBoolean("anti-netherite.interaction.cancel-craft", true);
        cancelEquip = config.getBoolean("anti-netherite.interaction.cancel-equip", true);
        cancelAttack = config.getBoolean("anti-netherite.interaction.cancel-attack", true);
        
        // Load item handling settings
        cancelPickup = config.getBoolean("anti-netherite.item-handling.cancel-pickup", true);
        removeDropped = config.getBoolean("anti-netherite.item-handling.remove-dropped", true);
        
        // Load Ancient Debris settings
        replaceAncientDebris = config.getBoolean("anti-netherite.ancient-debris.replace-when-mined", true);
        replaceOnChunkLoad = config.getBoolean("anti-netherite.ancient-debris.replace-on-chunk-load", true);
        onlyReplaceGeneratedChunks = config.getBoolean("anti-netherite.ancient-debris.only-replace-generated-chunks", true);
        ensureChunksLoaded = config.getBoolean("anti-netherite.ancient-debris.ensure-chunks-loaded", true);
        
        // Load performance settings
        restoreDebrisOnDisable = config.getBoolean("anti-netherite.performance.restore-debris-on-disable", false);
        restoreDebrisOnConfigChange = config.getBoolean("anti-netherite.performance.restore-debris-on-config-change", false);
        maxReplacementsPerChunk = config.getInt("anti-netherite.performance.max-replacements-per-chunk", 50);
        
        // Load Netherite detection settings
        useNameMatching = config.getBoolean("anti-netherite.detection.use-name-matching", true);
        netheriteItemsList = config.getStringList("anti-netherite.detection.items");
        
        // If the list is empty, add default Netherite items
        if (netheriteItemsList.isEmpty()) {
            netheriteItemsList.add("NETHERITE_SWORD");
            netheriteItemsList.add("NETHERITE_PICKAXE");
            netheriteItemsList.add("NETHERITE_AXE");
            netheriteItemsList.add("NETHERITE_SHOVEL");
            netheriteItemsList.add("NETHERITE_HOE");
            netheriteItemsList.add("NETHERITE_HELMET");
            netheriteItemsList.add("NETHERITE_CHESTPLATE");
            netheriteItemsList.add("NETHERITE_LEGGINGS");
            netheriteItemsList.add("NETHERITE_BOOTS");
            netheriteItemsList.add("NETHERITE_BLOCK");
            netheriteItemsList.add("NETHERITE_INGOT");
            netheriteItemsList.add("NETHERITE_SCRAP");
            
            // Save the default list to config
            config.set("anti-netherite.detection.items", netheriteItemsList);
            plugin.saveConfig();
        }
    }
    
    /**
     * Updates a configuration value
     * @param path The configuration path
     * @param value The new value
     */
    public void update(String path, Object value) {
        plugin.getConfig().set(path, value);
        plugin.saveConfig();
        reload();
    }
    
    /**
     * Gets a configuration value
     * @param path The configuration path
     * @return The configuration value
     */
    public Object getValue(String path) {
        return plugin.getConfig().get(path);
    }
    
    /**
     * Gets the delay between checks
     * @return The delay in ticks
     */
    public int getDelayTicks() {
        return delay * multiplier;
    }
    
    // Getters
    public int getDelay() {
        return delay;
    }
    
    public int getMultiplier() {
        return multiplier;
    }
    
    public boolean isClearNetherite() {
        return clearNetherite;
    }
    
    public boolean isCancelCraft() {
        return cancelCraft;
    }
    
    public boolean isCancelEquip() {
        return cancelEquip;
    }
    
    public boolean isCancelAttack() {
        return cancelAttack;
    }
    
    public boolean isCancelPickup() {
        return cancelPickup;
    }
    
    public boolean isRemoveDropped() {
        return removeDropped;
    }
    
    public boolean isCancelInventoryMove() {
        return cancelInventoryMove;
    }
    
    /**
     * Gets whether to use name matching for Netherite item detection
     * @return true if using name matching, false otherwise
     */
    public boolean isUseNameMatching() {
        return useNameMatching;
    }
    
    /**
     * Gets the list of Netherite item names
     * @return The list of Netherite item names
     */
    public List<String> getNetheriteItemsList() {
        return netheriteItemsList;
    }
    
    /**
     * Gets whether to replace Ancient Debris when mined
     * @return true if replacing Ancient Debris, false otherwise
     */
    public boolean isReplaceAncientDebris() {
        return replaceAncientDebris;
    }
    
    /**
     * Gets whether to replace Ancient Debris when chunks are loaded
     * @return true if replacing Ancient Debris on chunk load, false otherwise
     */
    public boolean isReplaceOnChunkLoad() {
        return replaceOnChunkLoad;
    }
    
    /**
     * Gets whether to only replace Ancient Debris in generated chunks
     * @return true if only replacing in generated chunks, false otherwise
     */
    public boolean isOnlyReplaceGeneratedChunks() {
        return onlyReplaceGeneratedChunks;
    }
    
    /**
     * Gets whether to restore Ancient Debris when the plugin is disabled
     * @return true if restoring Ancient Debris on disable, false otherwise
     */
    public boolean isRestoreDebrisOnDisable() {
        return restoreDebrisOnDisable;
    }
    
    /**
     * Gets whether to restore Ancient Debris when the configuration changes
     * @return true if restoring Ancient Debris on config change, false otherwise
     */
    public boolean isRestoreDebrisOnConfigChange() {
        return restoreDebrisOnConfigChange;
    }
    
    /**
     * Gets the maximum number of Ancient Debris replacements per chunk
     * @return The maximum number of replacements per chunk
     */
    public int getMaxReplacementsPerChunk() {
        return maxReplacementsPerChunk;
    }
    
    /**
     * Gets whether to ensure chunks are loaded when replacing Ancient Debris
     * @return true if ensuring chunks are loaded, false otherwise
     */
    public boolean isEnsureChunksLoaded() {
        return ensureChunksLoaded;
    }
} 