package top.modpotato.commands;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import top.modpotato.Main;
import top.modpotato.util.NetheriteDetector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;

/**
 * Command handler for the AntiNetherite plugin
 */
public class AntiNetheriteCommand implements CommandExecutor, TabCompleter {
    private final Main plugin;
    
    // Map of user-friendly setting names to actual config paths
    private final Map<String, String> SETTINGS_MAP = new HashMap<>();
    
    private final List<String> BOOLEAN_VALUES = Arrays.asList("true", "false");
    
    // Track when the last restore command was used to prevent spam
    private long lastRestoreTime = 0;
    private static final long RESTORE_COOLDOWN = 10000; // 10 seconds cooldown

    public AntiNetheriteCommand(Main plugin) {
        this.plugin = plugin;
        plugin.getCommand("antinetherite").setTabCompleter(this);
        
        // Initialize settings map with user-friendly names and actual config paths
        // Inventory settings
        SETTINGS_MAP.put("clear", "anti-netherite.inventory.clear");
        SETTINGS_MAP.put("cancel-inventory-move", "anti-netherite.inventory.cancel-move");
        
        // Interaction settings
        SETTINGS_MAP.put("cancel-craft", "anti-netherite.interaction.cancel-craft");
        SETTINGS_MAP.put("cancel-equip", "anti-netherite.interaction.cancel-equip");
        SETTINGS_MAP.put("cancel-attack", "anti-netherite.interaction.cancel-attack");
        
        // Item handling settings
        SETTINGS_MAP.put("cancel-pickup", "anti-netherite.item-handling.cancel-pickup");
        SETTINGS_MAP.put("remove-dropped", "anti-netherite.item-handling.remove-dropped");
        
        // Ancient debris settings
        SETTINGS_MAP.put("replace-ancient-debris", "anti-netherite.ancient-debris.replace-when-mined");
        SETTINGS_MAP.put("replace-on-chunk-load", "anti-netherite.ancient-debris.replace-on-chunk-load");
        SETTINGS_MAP.put("only-replace-generated-chunks", "anti-netherite.ancient-debris.only-replace-generated-chunks");
        SETTINGS_MAP.put("ensure-chunks-loaded", "anti-netherite.ancient-debris.ensure-chunks-loaded");
        
        // Detection settings
        SETTINGS_MAP.put("detection.use-name-matching", "anti-netherite.detection.use-name-matching");
        
        // Timing settings
        SETTINGS_MAP.put("delay", "anti-netherite.timing.delay");
        SETTINGS_MAP.put("multiplier", "anti-netherite.timing.multiplier");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("antinetherite.manage")) {
            sender.sendMessage(Component.text("You don't have permission to use this command.").color(NamedTextColor.RED));
            return true;
        }

        if (args.length == 0) {
            showHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                plugin.reloadPluginConfig();
                sender.sendMessage(Component.text("AntiNetherite configuration reloaded.").color(NamedTextColor.GREEN));
                return true;
            case "restore-debris":
                // Check for cooldown to prevent command spam
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastRestoreTime < RESTORE_COOLDOWN) {
                    sender.sendMessage(Component.text("Please wait before using this command again.").color(NamedTextColor.RED));
                    return true;
                }
                
                lastRestoreTime = currentTime;
                
                // Check if replacement features are enabled in config
                boolean replaceAncientDebris = (boolean) plugin.getConfigValue("anti-netherite.ancient-debris.replace-when-mined");
                boolean replaceOnChunkLoad = (boolean) plugin.getConfigValue("anti-netherite.ancient-debris.replace-on-chunk-load");
                
                // If both replacement features are disabled, check if there's anything to restore
                if (!replaceAncientDebris && !replaceOnChunkLoad) {
                    int totalLocations = plugin.getDebrisStorage().getTotalLocationsCount();
                    if (totalLocations == 0) {
                        sender.sendMessage(Component.text("Ancient Debris replacement is disabled and there are no stored locations to restore.").color(NamedTextColor.YELLOW));
                        return true;
                    } else {
                        sender.sendMessage(Component.text("Ancient Debris replacement is disabled but there are still " + totalLocations + " stored locations. Proceeding with restoration...").color(NamedTextColor.YELLOW));
                    }
                }
                
                if (args.length > 1) {
                    // World-specific restore
                    String worldName = args[1];
                    World world = Bukkit.getWorld(worldName);
                    
                    if (world == null) {
                        sender.sendMessage(Component.text("World not found: " + worldName).color(NamedTextColor.RED));
                        return true;
                    }
                    
                    // Check if there are any locations to restore in this world
                    int worldLocations = plugin.getDebrisStorage().getWorldLocationsCount(world);
                    if (worldLocations == 0) {
                        sender.sendMessage(Component.text("No Ancient Debris locations to restore in world " + worldName + ".").color(NamedTextColor.YELLOW));
                        return true;
                    }
                    
                    sender.sendMessage(Component.text("Restoring " + worldLocations + " Ancient Debris in world " + worldName + "...").color(NamedTextColor.YELLOW));
                    
                    // Run the restoration asynchronously to prevent lag
                    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                        try {
                            int count = plugin.getDebrisStorage().restoreDebrisInWorld(world);
                            
                            // Send message on the main thread
                            Bukkit.getScheduler().runTask(plugin, () -> {
                                sender.sendMessage(Component.text("Restored " + count + " Ancient Debris blocks in world " + worldName + ".").color(NamedTextColor.GREEN));
                            });
                        } catch (Exception e) {
                            // Send error message on the main thread
                            Bukkit.getScheduler().runTask(plugin, () -> {
                                sender.sendMessage(Component.text("Error restoring Ancient Debris: " + e.getMessage()).color(NamedTextColor.RED));
                            });
                            plugin.getLogger().severe("Error restoring Ancient Debris: " + e.getMessage());
                            e.printStackTrace();
                        }
                    });
                } else {
                    // Global restore
                    // Check if there are any locations to restore
                    int totalLocations = plugin.getDebrisStorage().getTotalLocationsCount();
                    if (totalLocations == 0) {
                        sender.sendMessage(Component.text("No Ancient Debris locations to restore.").color(NamedTextColor.YELLOW));
                        return true;
                    }
                    
                    sender.sendMessage(Component.text("Restoring " + totalLocations + " replaced Ancient Debris...").color(NamedTextColor.YELLOW));
                    
                    // Run the restoration asynchronously to prevent lag
                    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                        try {
                            int count = plugin.getDebrisStorage().restoreAllDebris();
                            
                            // Send message on the main thread
                            Bukkit.getScheduler().runTask(plugin, () -> {
                                sender.sendMessage(Component.text("Restored " + count + " Ancient Debris blocks.").color(NamedTextColor.GREEN));
                            });
                        } catch (Exception e) {
                            // Send error message on the main thread
                            Bukkit.getScheduler().runTask(plugin, () -> {
                                sender.sendMessage(Component.text("Error restoring Ancient Debris: " + e.getMessage()).color(NamedTextColor.RED));
                            });
                            plugin.getLogger().severe("Error restoring Ancient Debris: " + e.getMessage());
                            e.printStackTrace();
                        }
                    });
                }
                return true;
            case "debris-info":
                // Show information about stored Ancient Debris locations
                int totalLocations = plugin.getDebrisStorage().getTotalLocationsCount();
                
                if (totalLocations == 0) {
                    sender.sendMessage(Component.text("No Ancient Debris locations are currently stored.").color(NamedTextColor.YELLOW));
                    return true;
                }
                
                sender.sendMessage(Component.text("Ancient Debris Information:").color(NamedTextColor.GREEN));
                sender.sendMessage(Component.text("Total stored locations: " + totalLocations).color(NamedTextColor.WHITE));
                
                // Show per-world counts
                for (World world : Bukkit.getWorlds()) {
                    int worldCount = plugin.getDebrisStorage().getWorldLocationsCount(world);
                    if (worldCount > 0) {
                        sender.sendMessage(Component.text("- " + world.getName() + ": " + worldCount + " locations").color(NamedTextColor.WHITE));
                    }
                }
                
                // Show config status
                boolean isReplaceAncientDebris = (boolean) plugin.getConfigValue("anti-netherite.ancient-debris.replace-when-mined");
                boolean isReplaceOnChunkLoad = (boolean) plugin.getConfigValue("anti-netherite.ancient-debris.replace-on-chunk-load");
                
                sender.sendMessage(Component.text("Current config:").color(NamedTextColor.GREEN));
                sender.sendMessage(Component.text("- Replace when mined: " + (isReplaceAncientDebris ? "Enabled" : "Disabled")).color(
                    isReplaceAncientDebris ? NamedTextColor.RED : NamedTextColor.GREEN));
                sender.sendMessage(Component.text("- Replace on chunk load: " + (isReplaceOnChunkLoad ? "Enabled" : "Disabled")).color(
                    isReplaceOnChunkLoad ? NamedTextColor.RED : NamedTextColor.GREEN));
                
                return true;
            case "get":
                if (args.length < 2) {
                    sender.sendMessage(Component.text("Usage: /antinetherite get <setting>").color(NamedTextColor.RED));
                    return true;
                }
                
                if (args[1].equals("detection.items")) {
                    // Special handling for the items list
                    NetheriteDetector detector = plugin.getNetheriteDetector();
                    Set<String> items = detector.getNetheriteItemNames();
                    sender.sendMessage(Component.text("Netherite items: " + String.join(", ", items)).color(NamedTextColor.GREEN));
                    return true;
                }
                
                // Get the actual config path from the user-friendly name
                String configPath = SETTINGS_MAP.get(args[1]);
                if (configPath == null) {
                    sender.sendMessage(Component.text("Setting not found: " + args[1]).color(NamedTextColor.RED));
                    return true;
                }
                
                Object value = plugin.getConfigValue(configPath);
                if (value == null) {
                    sender.sendMessage(Component.text("Setting not found: " + args[1]).color(NamedTextColor.RED));
                } else {
                    sender.sendMessage(Component.text(args[1] + " = " + value).color(NamedTextColor.GREEN));
                }
                return true;
            case "set":
                if (args.length < 3) {
                    sender.sendMessage(Component.text("Usage: /antinetherite set <setting> <value>").color(NamedTextColor.RED));
                    return true;
                }
                
                // Get the actual config path from the user-friendly name
                String settingPath = SETTINGS_MAP.get(args[1]);
                if (settingPath == null && !args[1].equals("detection.items")) {
                    sender.sendMessage(Component.text("Unknown setting: " + args[1]).color(NamedTextColor.RED));
                    return true;
                }
                
                String settingValue = args[2];
                
                // Handle different setting types
                if (args[1].equals("delay") || args[1].equals("multiplier")) {
                    try {
                        int intValue = Integer.parseInt(settingValue);
                        if (intValue < 1) {
                            sender.sendMessage(Component.text("Value must be at least 1.").color(NamedTextColor.RED));
                            return true;
                        }
                        plugin.updateConfig(settingPath, intValue);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(Component.text("Invalid number: " + settingValue).color(NamedTextColor.RED));
                        return true;
                    }
                } else if (isBooleanSetting(args[1])) {
                    if (!settingValue.equalsIgnoreCase("true") && !settingValue.equalsIgnoreCase("false")) {
                        sender.sendMessage(Component.text("Value must be true or false.").color(NamedTextColor.RED));
                        return true;
                    }
                    plugin.updateConfig(settingPath, Boolean.parseBoolean(settingValue));
                } else if (args[1].equals("detection.items")) {
                    // Special handling for adding/removing items
                    if (args.length < 4) {
                        sender.sendMessage(Component.text("Usage: /antinetherite set detection.items <add|remove> <item>").color(NamedTextColor.RED));
                        return true;
                    }
                    
                    String action = args[2].toLowerCase();
                    String item = args[3].toUpperCase();
                    
                    @SuppressWarnings("unchecked")
                    List<String> items = (List<String>) plugin.getConfigValue("anti-netherite.detection.items");
                    
                    if (action.equals("add")) {
                        if (!items.contains(item)) {
                            items.add(item);
                            plugin.updateConfig("anti-netherite.detection.items", items);
                            sender.sendMessage(Component.text("Added " + item + " to the Netherite items list.").color(NamedTextColor.GREEN));
                        } else {
                            sender.sendMessage(Component.text(item + " is already in the Netherite items list.").color(NamedTextColor.YELLOW));
                        }
                    } else if (action.equals("remove")) {
                        if (items.contains(item)) {
                            items.remove(item);
                            plugin.updateConfig("anti-netherite.detection.items", items);
                            sender.sendMessage(Component.text("Removed " + item + " from the Netherite items list.").color(NamedTextColor.GREEN));
                        } else {
                            sender.sendMessage(Component.text(item + " is not in the Netherite items list.").color(NamedTextColor.YELLOW));
                        }
                    } else {
                        sender.sendMessage(Component.text("Invalid action: " + action + ". Use 'add' or 'remove'.").color(NamedTextColor.RED));
                    }
                    return true;
                } else {
                    sender.sendMessage(Component.text("Unknown setting: " + args[1]).color(NamedTextColor.RED));
                    return true;
                }
                
                sender.sendMessage(Component.text("Set " + args[1] + " to " + settingValue).color(NamedTextColor.GREEN));
                return true;
            default:
                showHelp(sender);
                return true;
        }
    }

    private boolean isBooleanSetting(String setting) {
        String configPath = SETTINGS_MAP.get(setting);
        if (configPath == null) {
            return false;
        }
        
        // Check if the setting is a boolean type
        return configPath.contains("clear") || 
               configPath.contains("cancel") || 
               configPath.contains("remove") || 
               configPath.contains("replace") || 
               configPath.contains("use-name-matching") ||
               configPath.contains("only-replace-generated-chunks") ||
               configPath.contains("ensure-chunks-loaded");
    }

    private void showHelp(CommandSender sender) {
        sender.sendMessage(Component.text("AntiNetherite Commands:").color(NamedTextColor.GOLD));
        sender.sendMessage(Component.text("/antinetherite reload - Reload the configuration").color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/antinetherite restore-debris [world] - Restore all replaced Ancient Debris").color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("  - Optional world parameter to restore only in a specific world").color(NamedTextColor.GRAY));
        sender.sendMessage(Component.text("  - Only restores blocks that are still Netherrack").color(NamedTextColor.GRAY));
        sender.sendMessage(Component.text("/antinetherite debris-info - Show information about stored Ancient Debris locations").color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("  - Displays counts per world and current config status").color(NamedTextColor.GRAY));
        sender.sendMessage(Component.text("/antinetherite get <setting> - Get a configuration value").color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/antinetherite set <setting> <value> - Set a configuration value").color(NamedTextColor.YELLOW));
        
        sender.sendMessage(Component.text("Available settings:").color(NamedTextColor.GOLD));
        
        sender.sendMessage(Component.text("Inventory settings:").color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("  clear, cancel-inventory-move").color(NamedTextColor.GRAY));
        
        sender.sendMessage(Component.text("Interaction settings:").color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("  cancel-craft, cancel-equip, cancel-attack").color(NamedTextColor.GRAY));
        
        sender.sendMessage(Component.text("Item handling settings:").color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("  cancel-pickup, remove-dropped").color(NamedTextColor.GRAY));
        
        sender.sendMessage(Component.text("Ancient debris settings:").color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("  replace-ancient-debris, replace-on-chunk-load").color(NamedTextColor.GRAY));
        sender.sendMessage(Component.text("  only-replace-generated-chunks, ensure-chunks-loaded").color(NamedTextColor.GRAY));
        
        sender.sendMessage(Component.text("Detection settings:").color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("  detection.use-name-matching").color(NamedTextColor.GRAY));
        
        sender.sendMessage(Component.text("Timing settings:").color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("  delay, multiplier").color(NamedTextColor.GRAY));
        
        sender.sendMessage(Component.text("Special commands:").color(NamedTextColor.GOLD));
        sender.sendMessage(Component.text("/antinetherite get detection.items - List all Netherite items").color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/antinetherite set detection.items add <item> - Add an item to the list").color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/antinetherite set detection.items remove <item> - Remove an item from the list").color(NamedTextColor.YELLOW));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (!sender.hasPermission("antinetherite.manage")) {
            return completions;
        }
        
        if (args.length == 1) {
            List<String> commands = Arrays.asList("reload", "restore-debris", "debris-info", "get", "set");
            StringUtil.copyPartialMatches(args[0], commands, completions);
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("restore-debris")) {
                // Tab complete for world names
                List<String> worldNames = new ArrayList<>();
                for (World world : Bukkit.getWorlds()) {
                    // Only include worlds that have stored locations
                    if (plugin.getDebrisStorage().getWorldLocationsCount(world) > 0) {
                        worldNames.add(world.getName());
                    }
                }
                StringUtil.copyPartialMatches(args[1], worldNames, completions);
            } else if (args[0].equalsIgnoreCase("get") || args[0].equalsIgnoreCase("set")) {
                List<String> allSettings = new ArrayList<>(SETTINGS_MAP.keySet());
                allSettings.add("detection.items");
                StringUtil.copyPartialMatches(args[1], allSettings, completions);
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("set")) {
                if (isBooleanSetting(args[1])) {
                    StringUtil.copyPartialMatches(args[2], BOOLEAN_VALUES, completions);
                } else if (args[1].equals("detection.items")) {
                    StringUtil.copyPartialMatches(args[2], Arrays.asList("add", "remove"), completions);
                }
            }
        } else if (args.length == 4) {
            if (args[0].equalsIgnoreCase("set") && args[1].equals("detection.items") && args[2].equals("remove")) {
                // Tab complete for removing items - show current items
                NetheriteDetector detector = plugin.getNetheriteDetector();
                Set<String> items = detector.getNetheriteItemNames();
                StringUtil.copyPartialMatches(args[3], items, completions);
            }
        }
        
        Collections.sort(completions);
        return completions;
    }
} 