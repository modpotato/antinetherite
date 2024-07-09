# AntiNetherite Plugin

AntiNetherite is a Minecraft plugin designed to remove Netherite items from player inventories and prevent their crafting.

## Features

- Periodically removes Netherite items from player inventories
- Optionally cancels the crafting of Netherite items
- Configurable check intervals and settings

## Configuration

The plugin's behavior can be customized in the `config.yml` file:
```yaml
# Netherite removal settings
anti-netherite:
  # CHECKS

  # Should we clear netherite in the inventory of players?
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

## Usage

1. Place the built JAR file in your server's `plugins` folder.
2. Start or restart your Minecraft server.
3. Configure the plugin settings in `plugins/AntiNetherite/config.yml` if needed.

## Problems

Open an issue on the GitHub repository if you have any problems or suggestions.
