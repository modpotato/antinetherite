package top.modpotato.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import top.modpotato.Main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Command handler for the AntiNetherite plugin
 */
public class AntiNetheriteCommand implements CommandExecutor, TabCompleter {
    private final Main plugin;
    private final List<String> SETTINGS = Arrays.asList("clear", "cancel-craft", "cancel-equip", "cancel-attack", "delay", "multiplier");
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
                plugin.loadConfig();
                sender.sendMessage(Component.text("AntiNetherite configuration reloaded.").color(NamedTextColor.GREEN));
                return true;
            case "get":
                if (args.length < 2) {
                    sender.sendMessage(Component.text("Usage: /antinetherite get <setting>").color(NamedTextColor.RED));
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
                } else if (args[1].equals("clear") || args[1].equals("cancel-craft") || 
                           args[1].equals("cancel-equip") || args[1].equals("cancel-attack")) {
                    if (!settingValue.equalsIgnoreCase("true") && !settingValue.equalsIgnoreCase("false")) {
                        sender.sendMessage(Component.text("Value must be true or false.").color(NamedTextColor.RED));
                        return true;
                    }
                    plugin.updateConfig(settingPath, Boolean.parseBoolean(settingValue));
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

    private void showHelp(CommandSender sender) {
        sender.sendMessage(Component.text("AntiNetherite Commands:").color(NamedTextColor.GOLD));
        sender.sendMessage(Component.text("/antinetherite reload - Reload the configuration").color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/antinetherite get <setting> - Get a configuration value").color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/antinetherite set <setting> <value> - Set a configuration value").color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("Available settings: clear, cancel-craft, cancel-equip, cancel-attack, delay, multiplier").color(NamedTextColor.YELLOW));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (!sender.hasPermission("antinetherite.manage")) {
            return completions;
        }
        
        if (args.length == 1) {
            List<String> commands = Arrays.asList("reload", "get", "set");
            StringUtil.copyPartialMatches(args[0], commands, completions);
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("get") || args[0].equalsIgnoreCase("set")) {
                StringUtil.copyPartialMatches(args[1], SETTINGS, completions);
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("set")) {
                if (args[1].equals("clear") || args[1].equals("cancel-craft") || 
                    args[1].equals("cancel-equip") || args[1].equals("cancel-attack")) {
                    StringUtil.copyPartialMatches(args[2], BOOLEAN_VALUES, completions);
                }
            }
        }
        
        Collections.sort(completions);
        return completions;
    }
} 