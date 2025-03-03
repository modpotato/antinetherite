# AntiNetherite Plugin

AntiNetherite is a Minecraft plugin designed to remove Netherite items from player inventories and prevent their crafting, equipping, and attacking with Netherite items.

## Features

- Periodically removes Netherite items from player inventories
- Cancels the crafting of Netherite items
- Prevents equipping Netherite armor
- Prevents attacking with Netherite weapons
- Prevents picking up Netherite items
- Removes dropped Netherite items
- Prevents moving Netherite items in inventories
- Replaces Ancient Debris with Netherrack when mined or generated
- Restores replaced Ancient Debris when needed
- Customizable Netherite item detection
- Fully compatible with Folia 1.21.4 using region-aware schedulers
- In-game commands to manage settings
- Hot reload support

## Configuration

The plugin's behavior can be customized in the `config.yml` file:
```yaml
# AntiNetherite Configuration
# 
# This plugin prevents the use of Netherite items in your server
# It is fully compatible with both regular Bukkit/Spigot/Paper servers and Folia

# Netherite removal settings
anti-netherite:
  # CHECKS

  # Should we clear netherite in the inventory of players?
  # This feature is compatible with Folia using region-aware schedulers
  clear: false

  # Should we cancel crafting of netherite items?
  # This prevents players from crafting any items containing netherite
  cancel-craft: true

  # Should we cancel equipping of netherite items?
  # This prevents players from equipping netherite armor
  cancel-equip: true
  
  # Should we cancel attacking with netherite items?
  # This prevents players from attacking with netherite weapons
  cancel-attack: true

  # Should we cancel picking up netherite items?
  # This prevents players from picking up netherite items from the ground
  cancel-pickup: true
  
  # Should we remove netherite items when dropped?
  # This removes netherite items when players drop them
  remove-dropped: true
  
  # Should we cancel moving netherite items in inventories?
  # This prevents players from moving netherite items in their inventory
  cancel-inventory-move: true
  
  # ANCIENT DEBRIS SETTINGS
  
  # Should we replace Ancient Debris with Netherrack when mined?
  # This prevents players from obtaining Ancient Debris
  replace-ancient-debris: true
  
  # Should we replace Ancient Debris with Netherrack when chunks are loaded?
  # This prevents Ancient Debris from generating in the world
  replace-on-chunk-load: true

  # DETECTION SETTINGS
  
  # Settings for detecting Netherite items
  detection:
    # Should we use name matching to detect Netherite items?
    # If true, any item with a name containing "NETHERITE" will be considered a Netherite item
    # If false, only items in the list below will be considered Netherite items
    use-name-matching: true
    
    # List of items to consider as Netherite items
    # These are Bukkit Material enum names
    # You can add custom items here if needed
    items:
      - NETHERITE_SWORD
      - NETHERITE_PICKAXE
      - NETHERITE_AXE
      - NETHERITE_SHOVEL
      - NETHERITE_HOE
      - NETHERITE_HELMET
      - NETHERITE_CHESTPLATE
      - NETHERITE_LEGGINGS
      - NETHERITE_BOOTS
      - NETHERITE_BLOCK
      - NETHERITE_INGOT
      - NETHERITE_SCRAP

  # TIMINGS
  
  # Delay in seconds between each inventory check
  # Higher values reduce server load but may allow players to keep netherite items longer
  delay: 1
  
  # Multiply the delay by this number to get the actual delay in ticks
  # 20 ticks = 1 second, so the default is 1 second between checks
  # This is used for both Bukkit and Folia schedulers
  multiplier: 20
```

## Commands

The plugin provides the following commands:

- `/antinetherite reload` - Reload the configuration
- `/antinetherite restore-debris [world]` - Restore all replaced Ancient Debris (optionally in a specific world)
- `/antinetherite debris-info` - Show information about stored Ancient Debris locations
- `/antinetherite get <setting>` - Get a configuration value
- `/antinetherite set <setting> <value>` - Set a configuration value

Available settings:
- `clear` - Enable/disable clearing Netherite from inventories (true/false)
- `cancel-craft` - Enable/disable canceling Netherite crafting (true/false)
- `cancel-equip` - Enable/disable preventing Netherite armor equipping (true/false)
- `cancel-attack` - Enable/disable preventing attacks with Netherite weapons (true/false)
- `cancel-pickup` - Enable/disable preventing picking up Netherite items (true/false)
- `remove-dropped` - Enable/disable removing dropped Netherite items (true/false)
- `cancel-inventory-move` - Enable/disable preventing inventory movement of Netherite items (true/false)
- `replace-ancient-debris` - Enable/disable replacing Ancient Debris with Netherrack when mined (true/false)
- `replace-on-chunk-load` - Enable/disable replacing Ancient Debris with Netherrack when chunks are loaded (true/false)
- `detection.use-name-matching` - Enable/disable name-based detection of Netherite items (true/false)
- `delay` - Set the delay between inventory checks (in seconds)
- `multiplier` - Set the tick multiplier (20 = 1 second)

Special commands for managing the Netherite items list:
- `/antinetherite get detection.items` - List all items considered as Netherite items
- `/antinetherite set detection.items add <item>` - Add an item to the Netherite items list
- `/antinetherite set detection.items remove <item>` - Remove an item from the Netherite items list

## Permissions

- `antinetherite.manage` - Allows using the `/antinetherite` command (default: op)

## Usage

1. Place the built JAR file in your server's `plugins` folder.
2. Start or restart your Minecraft server.
3. Configure the plugin settings in `plugins/AntiNetherite/config.yml` if needed.
4. Use in-game commands to adjust settings as needed.

## Netherite Item Detection

The plugin provides two ways to detect Netherite items:

1. **Name Matching**: Any item with a type name containing "NETHERITE" will be considered a Netherite item. This is the default behavior.
2. **Custom List**: You can define a custom list of items to be considered as Netherite items. This is useful if you want to add custom items or only block specific Netherite items.

You can toggle between these methods using the `detection.use-name-matching` setting. If set to `false`, only items in the custom list will be considered Netherite items.

## Ancient Debris Replacement

The plugin can prevent players from obtaining Ancient Debris in two ways:

1. **Mining Prevention**: When a player starts to mine Ancient Debris, it is immediately replaced with Netherrack and no drops are given.
2. **Generation Prevention**: When new chunks are generated, any Ancient Debris in those chunks is automatically replaced with Netherrack.

These features can be toggled independently using the `replace-ancient-debris` and `replace-on-chunk-load` settings.

### Reversibility

The plugin keeps track of all Ancient Debris that has been replaced with Netherrack. When either:

- The plugin is disabled
- The configuration options are turned off
- The `/antinetherite restore-debris` command is used

All replaced Ancient Debris will be restored to its original state, but only if the block is still Netherrack. This ensures that player-built structures are not affected.

### Safeguards

The Ancient Debris replacement system includes several safeguards:

- **Thread Safety**: Uses thread-safe collections to prevent concurrent modification issues
- **Error Handling**: Catches and logs exceptions without crashing the plugin
- **Performance Optimization**: Limits the number of replacements per chunk to prevent lag
- **Async Processing**: Performs restoration operations asynchronously to prevent server lag
- **Cooldown System**: Prevents command spam with a cooldown period
- **World-Specific Restoration**: Allows restoring Ancient Debris in specific worlds
- **Storage Limits**: Prevents excessive memory usage by limiting the number of stored locations
- **Nether-Only Processing**: Only processes chunks in the Nether dimension where Ancient Debris naturally generates
- **Persistent Storage**: Saves replaced locations to disk for recovery after server restarts

## Compatibility

- Works with Bukkit/Spigot/Paper servers
- Fully compatible with Folia 1.21.4 using region-aware schedulers
- Supports hot reloads

## Technical Details

The plugin uses different scheduling mechanisms depending on the server type:
- On regular Bukkit/Spigot/Paper servers, it uses the standard BukkitScheduler
- On Folia servers, it uses the region-aware schedulers to ensure thread safety

The plugin provides comprehensive protection against Netherite items:
- Periodic inventory scanning removes existing items
- Event listeners prevent various ways of obtaining/using Netherite
- Ancient Debris is replaced with Netherrack when mined or generated
- All protection mechanisms can be individually configured
- Centralized Netherite item detection for consistent behavior

## Problems

Open an issue on the GitHub repository if you have any problems or suggestions.
