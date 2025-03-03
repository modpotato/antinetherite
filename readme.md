# AntiNetherite Plugin

AntiNetherite is a Minecraft plugin designed to remove Netherite items from player inventories and prevent their crafting, equipping, and attacking with Netherite items.

## Features

- Periodically removes Netherite items from player inventories
- Cancels the crafting of Netherite items
- Prevents equipping Netherite armor
- Prevents attacking with Netherite weapons
- Fully compatible with Folia 1.21.4
- In-game commands to manage settings

## Configuration

The plugin's behavior can be customized in the `config.yml` file:
```yaml
# Netherite removal settings
anti-netherite:
  # CHECKS

  # Should we clear netherite in the inventory of players?
  # For Folia servers, this uses region-aware schedulers
  clear: false

  # Should we cancel crafting of netherite items?
  cancel-craft: true

  # Should we cancel equipping of netherite items?
  cancel-equip: true
  
  # Should we cancel attacking with netherite items?
  cancel-attack: true

  # TIMINGS
  
  # Delay in seconds between each check (assuming you leave multiplier at 20)
  delay: 1
  # Multiply the delay by this number to get the actual delay
  # 20 = 20 ticks = 1 second
  multiplier: 20
```

## Commands

The plugin provides the following commands:

- `/antinetherite reload` - Reload the configuration
- `/antinetherite get <setting>` - Get a configuration value
- `/antinetherite set <setting> <value>` - Set a configuration value

Available settings:
- `clear` - Enable/disable clearing Netherite from inventories (true/false)
- `cancel-craft` - Enable/disable canceling Netherite crafting (true/false)
- `cancel-equip` - Enable/disable preventing Netherite armor equipping (true/false)
- `cancel-attack` - Enable/disable preventing attacks with Netherite weapons (true/false)
- `delay` - Set the delay between inventory checks (in seconds)
- `multiplier` - Set the tick multiplier (20 = 1 second)

## Permissions

- `antinetherite.manage` - Allows using the `/antinetherite` command (default: op)

## Usage

1. Place the built JAR file in your server's `plugins` folder.
2. Start or restart your Minecraft server.
3. Configure the plugin settings in `plugins/AntiNetherite/config.yml` if needed.
4. Use in-game commands to adjust settings as needed.

## Compatibility

- Works with Bukkit/Spigot/Paper servers
- Fully compatible with Folia 1.21.4
- Supports hot reloads

## Problems

Open an issue on the GitHub repository if you have any problems or suggestions.
