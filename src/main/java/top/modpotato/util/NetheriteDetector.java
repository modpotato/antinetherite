package top.modpotato.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import net.kyori.adventure.text.Component;
import top.modpotato.config.Config;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility class for detecting Netherite items
 * Acts as a centralized source of truth for what constitutes a Netherite item
 */
public class NetheriteDetector {
    private final Config config;
    private final Set<String> netheriteItemNames = new HashSet<>();
    private final Set<Material> netheriteItemMaterials = new HashSet<>();
    
    /**
     * Creates a new NetheriteDetector
     * @param config The plugin configuration
     */
    public NetheriteDetector(Config config) {
        this.config = config;
        reloadNetheriteItems();
    }
    
    /**
     * Reloads the list of Netherite items from the configuration
     */
    public void reloadNetheriteItems() {
        netheriteItemNames.clear();
        netheriteItemMaterials.clear();
        
        // Add custom item names from config
        List<String> customItems = config.getNetheriteItemsList();
        if (customItems != null) {
            netheriteItemNames.addAll(customItems);
        }
        
        // Add material enums if they exist
        for (String itemName : netheriteItemNames) {
            try {
                Material material = Material.valueOf(itemName.toUpperCase());
                netheriteItemMaterials.add(material);
            } catch (IllegalArgumentException ignored) {
                // Material doesn't exist, that's fine
            }
        }
        
        // Add default Netherite materials if using name matching
        if (config.isUseNameMatching()) {
            for (Material material : Material.values()) {
                if (material.name().contains("NETHERITE")) {
                    netheriteItemMaterials.add(material);
                    netheriteItemNames.add(material.name());
                }
            }
        }
    }
    
    /**
     * Checks if an item is a Netherite item
     * @param item The item to check
     * @return true if the item is a Netherite item, false otherwise
     */
    public boolean isNetheriteItem(ItemStack item) {
        if (item == null) {
            return false;
        }
        
        // Check by material (most efficient)
        if (netheriteItemMaterials.contains(item.getType())) {
            return true;
        }
        
        // If using name matching, check by name
        if (config.isUseNameMatching()) {
            String itemTypeName = item.getType().name();
            
            // Check if the item type name contains any of the custom names
            for (String netheriteItemName : netheriteItemNames) {
                if (itemTypeName.contains(netheriteItemName)) {
                    return true;
                }
            }
            
            // Check if the item has a display name that contains any of the custom names
            if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                Component displayName = item.getItemMeta().displayName();
                for (String netheriteItemName : netheriteItemNames) {
                    if (displayName.toString().contains(netheriteItemName)) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    /**
     * Gets the set of Netherite item names
     * @return The set of Netherite item names
     */
    public Set<String> getNetheriteItemNames() {
        return new HashSet<>(netheriteItemNames);
    }
    
    /**
     * Gets the set of Netherite item materials
     * @return The set of Netherite item materials
     */
    public Set<Material> getNetheriteItemMaterials() {
        return new HashSet<>(netheriteItemMaterials);
    }
} 