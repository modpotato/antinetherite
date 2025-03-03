package top.modpotato.commands;

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

/**
 * Command handler for the AntiNetherite plugin
 */
public class AntiNetheriteCommand implements CommandExecutor, TabCompleter {
    private final Main plugin;
    private final List<String> SETTINGS = Arrays.asList(
        "clear", "cancel-craft", "cancel-equip", "cancel-attack", 
        "cancel-pickup", "remove-dropped", "cancel-inventory-move", 
        "replace-ancient-debris", "replace-on-chunk-load",
        "detection.use-name-matching", "delay", "multiplier"
    );
    private final List<String> BOOLEAN_VALUES = Arrays.asList("true", "false");

    public AntiNetheriteCommand(Main plugin) {
        this.plugin = plugin;
        plugin.getCommand("antinetherite").setTabCompleter(this);
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
                plugin.getDebrisStorage().restoreAllDebris();
                sender.sendMessage(Component.text("All replaced Ancient Debris has been restored to its original state.").color(NamedTextColor.GREEN));
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
                
                String path = "anti-netherite." + args[1];
                Object value = plugin.getConfigValue(path);
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
                
                String settingPath = "anti-netherite." + args[1];
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
        return setting.equals("clear") || 
               setting.equals("cancel-craft") || 
               setting.equals("cancel-equip") || 
               setting.equals("cancel-attack") ||
               setting.equals("cancel-pickup") ||
               setting.equals("remove-dropped") ||
               setting.equals("cancel-inventory-move") ||
               setting.equals("replace-ancient-debris") ||
               setting.equals("replace-on-chunk-load") ||
               setting.equals("detection.use-name-matching");
    }

    private void showHelp(CommandSender sender) {
        sender.sendMessage(Component.text("AntiNetherite Commands:").color(NamedTextColor.GOLD));
        sender.sendMessage(Component.text("/antinetherite reload - Reload the configuration").color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/antinetherite restore-debris - Restore all replaced Ancient Debris").color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/antinetherite get <setting> - Get a configuration value").color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/antinetherite set <setting> <value> - Set a configuration value").color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("Available settings:").color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("clear, cancel-craft, cancel-equip, cancel-attack").color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("cancel-pickup, remove-dropped, cancel-inventory-move").color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("replace-ancient-debris, replace-on-chunk-load").color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("detection.use-name-matching, delay, multiplier").color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("Special commands:").color(NamedTextColor.YELLOW));
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
            List<String> commands = Arrays.asList("reload", "restore-debris", "get", "set");
            StringUtil.copyPartialMatches(args[0], commands, completions);
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("get")) {
                List<String> allSettings = new ArrayList<>(SETTINGS);
                allSettings.add("detection.items");
                StringUtil.copyPartialMatches(args[1], allSettings, completions);
            } else if (args[0].equalsIgnoreCase("set")) {
                List<String> allSettings = new ArrayList<>(SETTINGS);
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