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
        
        delay = config.getInt("anti-netherite.delay", 1);
        multiplier = config.getInt("anti-netherite.multiplier", 20);
        clearNetherite = config.getBoolean("anti-netherite.clear", false);
        cancelCraft = config.getBoolean("anti-netherite.cancel-craft", true);
        cancelEquip = config.getBoolean("anti-netherite.cancel-equip", true);
        cancelAttack = config.getBoolean("anti-netherite.cancel-attack", true);
        cancelPickup = config.getBoolean("anti-netherite.cancel-pickup", true);
        removeDropped = config.getBoolean("anti-netherite.remove-dropped", true);
        cancelInventoryMove = config.getBoolean("anti-netherite.cancel-inventory-move", true);
        
        // Load Ancient Debris settings
        replaceAncientDebris = config.getBoolean("anti-netherite.replace-ancient-debris", true);
        replaceOnChunkLoad = config.getBoolean("anti-netherite.replace-on-chunk-load", true);
        
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
} 