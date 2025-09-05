# AntiNetherite Plugin

AntiNetherite is a highly configurable Minecraft plugin designed to remove Netherite items from player inventories and prevent their crafting, equipping, and attacking with Netherite items. Every aspect of the plugin can be enabled, disabled, or fine-tuned to suit your server's needs.

The plugin offers both destructive and non-destructive modes. In destructive mode (default), it will remove items from inventories and delete dropped items. In non-destructive mode, it will only prevent the use of Netherite items without destroying them, making it more player-friendly while still enforcing the server rules.

## Features

- **Fully Configurable Protection System** - Every feature can be individually enabled or disabled
- **Non-Destructive Mode Available** - Choose between removing items or just preventing their use
- Periodically removes Netherite items from player inventories
- Cancels the crafting of Netherite items
- Prevents equipping Netherite armor
- Prevents attacking with Netherite weapons
- Prevents picking up Netherite items
- Removes dropped Netherite items
- Prevents moving Netherite items in inventories
- Blocks automated container (hopper) transfers of Netherite items (configurable, disabled by default due to performance impact)
- Blocks automated container (hopper) transfers of Netherite items (configurable, disabled by default due to performance impact)
- Replaces Ancient Debris with Netherrack when mined or generated
- **Performance-Optimized** - Control whether replaced Ancient Debris is restored on plugin disable
- **Fine-Grained Control** - Advanced settings for memory usage, logging, and more
- Customizable Netherite item detection
- **Creative/Spectator Mode Exclusion** - Optionally ignore players in creative or spectator mode
- **Configurable Player Notifications** - Control whether players are notified of blocked actions
- Fully compatible with Folia 1.21.4 using region-aware schedulers
- In-game commands to manage all settings
- Hot reload support for configuration changes without server restart

## Configuration

Every aspect of the plugin's behavior can be customized in the `config.yml` file. All features can be individually enabled or disabled, allowing you to create the exact protection system your server needs:

```yaml
# Main plugin settings
anti-netherite:
  # ==============================
  # GLOBAL SETTINGS
  # ==============================
  
  global:
    # Should we enable destructive actions?
    # If true, the plugin will delete/remove items when appropriate
    # If false, the plugin will only cancel events without destroying items
    # This affects item drops, inventory clearing, and other destructive operations
    enable-destructive-actions: false
  
  # ==============================
  # INVENTORY PROTECTION SETTINGS
  # ==============================
  
  inventory:
    # Should we clear netherite in the inventory of players?
    # This feature is compatible with Folia using region-aware schedulers
    clear: false
    
    # Should we cancel moving netherite items in inventories?
    # This prevents players from moving netherite items in their inventory
    cancel-move: true
    
    # Should we cancel automated container transfers (hoppers, hopper minecarts, etc.)
    # of netherite items between inventories?
    # This blocks bypasses where players pipe netherite into containers
    # WARNING: This can impact server performance with many hoppers/containers
    # Only enable if you need this additional protection layer
    cancel-container-transfer: false
  
  # ==============================
  # ITEM INTERACTION SETTINGS
  # ==============================
  
  interaction:
    # Should we cancel crafting of netherite items?
    # This prevents players from crafting any items containing netherite
    cancel-craft: true
    
    # Should we cancel equipping of netherite items?
    # This prevents players from equipping netherite armor
    cancel-equip: true
    
    # Should we cancel attacking with netherite items?
    # This prevents players from attacking with netherite weapons
    cancel-attack: true
  
  # ==============================
  # ITEM PICKUP/DROP SETTINGS
  # ==============================
  
  item-handling:
    # Should we cancel picking up netherite items?
    # This prevents players from picking up netherite items from the ground
    cancel-pickup: true
    
    # Should we remove netherite items when dropped?
    # This removes netherite items when players drop them
    remove-dropped: false
  
  # ==============================
  # ANCIENT DEBRIS SETTINGS
  # ==============================
  
  ancient-debris:
    # Should we replace Ancient Debris with Netherrack when mined?
    # This prevents players from obtaining Ancient Debris
    replace-when-mined: true
    
    # Should we replace Ancient Debris with Netherrack when chunks are loaded?
    # This prevents Ancient Debris from generating in the world
    # THIS MAY CAUSE LAG ON FIRST LOAD
    replace-on-chunk-load: false
    
    # Should we only replace Ancient Debris in chunks that are already generated?
    # If true, only chunks that have been generated will be processed
    # If false, all chunks will be processed regardless of generation status
    only-replace-generated-chunks: true
    
    # Should we ensure chunks are loaded when trying to replace Ancient Debris?
    # If true, chunks will be loaded if they are not already loaded
    # If false, only already loaded chunks will be processed
    ensure-chunks-loaded: false
    
    # Should we save the locations of replaced Ancient Debris?
    # If true, the plugin will track and save replaced Ancient Debris locations
    # If false, Ancient Debris will be replaced but not tracked or saved
    # Setting this to false will prevent restoration of Ancient Debris
    save-replaced-locations: false
  
  # ==============================
  # PERFORMANCE SETTINGS
  # ==============================
  
  performance:
    # Should we restore Ancient Debris when the plugin is disabled?
    # WARNING: This can cause significant lag if there are many replaced blocks
    # If false, Ancient Debris will remain as Netherrack when the plugin is disabled
    restore-debris-on-disable: false
    
    # Should we restore Ancient Debris when the configuration changes?
    # WARNING: This can cause significant lag if there are many replaced blocks
    # If false, Ancient Debris will remain as Netherrack when the configuration changes
    restore-debris-on-config-change: false
    
    # Maximum number of Ancient Debris to replace per chunk to prevent lag
    # Higher values may cause more lag but will replace more Ancient Debris
    # Lower values will reduce lag but may leave some Ancient Debris unreplaced
    # Set to -1 to remove the limit entirely (not recommended for performance)
    max-replacements-per-chunk: 50
  
  # ==============================
  # ADVANCED SETTINGS
  # ==============================
  
  advanced:
    # Maximum number of Ancient Debris locations to store per world
    # Higher values use more memory but allow tracking more replaced blocks
    # Lower values use less memory but may limit the number of blocks that can be restored
    # Set to -1 to remove the limit entirely (not recommended for large worlds)
    max-locations-per-world: 10000
    
    # Cooldown in seconds between command executions
    # This prevents command spam and potential performance issues
    command-cooldown-seconds: 1
    
    # Should we log Ancient Debris replacements to the console?
    # If true, a message will be logged each time Ancient Debris is replaced
    # If false, only summary messages will be logged
    log-debris-replacements: true
    
    # Should we log Netherite item removals from inventories?
    # If true, a message will be logged each time Netherite items are removed
    # If false, only summary messages will be logged
    log-inventory-removals: true
    
    # Should we ignore players in creative or spectator mode?
    # If true, players in creative or spectator mode will not be affected by the plugin
    # If false, all players will be affected regardless of game mode
    ignore-creative-spectator: true
    
    # Should we send notification messages to players?
    # If true, players will be notified when the plugin blocks their actions
    # If false, players will only be notified for destructive actions (item removal, etc.)
    notify-players: true
  
  # ==============================
  # DETECTION SETTINGS
  # ==============================
  
  detection:
    # Should we use name matching to detect Netherite items?
    # If true, any item with a name containing "NETHERITE" will be considered a Netherite item
    # If false, only items in the list below will be considered Netherite items
    use-name-matching: true
    
    # List of items to consider as Netherite items
    # These are Bukkit Material enum names
    # You can add custom items here if needed
    # The item names follow the format of the Bukkit Material enum
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
      - ANCIENT_DEBRIS
  
  # ==============================
  # TIMING SETTINGS
  # ==============================
  
  timing:
    # Delay in seconds between each inventory check
    # Higher values reduce server load but may allow players to keep netherite items longer
    delay: 1
    
    # Multiply the delay by this number to get the actual delay in ticks
    # 20 ticks = 1 second, so the default is 1 second between checks
    # This is used for both Bukkit and Folia schedulers
    multiplier: 20
```

## Commands

The plugin provides comprehensive commands to manage all settings in-game without editing configuration files:

- `/antinetherite reload` - Reload the configuration
- `/antinetherite restore-debris [world]` - Restore all replaced Ancient Debris (optionally in a specific world)
- `/antinetherite debris-info` - Show information about stored Ancient Debris locations
- `/antinetherite get <setting>` - Get a configuration value
- `/antinetherite set <setting> <value>` - Set a configuration value

Every setting in the configuration file can be adjusted through these commands. Available settings are organized by category:

**Global settings:**
- `global.enable-destructive-actions` - Enable/disable destructive actions like removing items (true/false)

**Inventory settings:**
- `inventory.clear` - Enable/disable clearing Netherite from inventories (true/false)
- `inventory.cancel-move` - Enable/disable preventing inventory movement of Netherite items (true/false)
- `inventory.cancel-container-transfer` - Enable/disable blocking automated hopper/container transfers of Netherite items (true/false, disabled by default due to performance impact)
 - `inventory.cancel-container-transfer` - Enable/disable blocking automated hopper/container transfers of Netherite items (true/false)

**Interaction settings:**
- `interaction.cancel-craft` - Enable/disable canceling Netherite crafting (true/false)
- `interaction.cancel-equip` - Enable/disable preventing Netherite armor equipping (true/false)
- `interaction.cancel-attack` - Enable/disable preventing attacks with Netherite weapons (true/false)

**Item handling settings:**
- `item-handling.cancel-pickup` - Enable/disable preventing picking up Netherite items (true/false)
- `item-handling.remove-dropped` - Enable/disable removing dropped Netherite items (true/false)

**Ancient debris settings:**
- `ancient-debris.replace-when-mined` - Enable/disable replacing Ancient Debris with Netherrack when mined (true/false)
- `ancient-debris.replace-on-chunk-load` - Enable/disable replacing Ancient Debris with Netherrack when chunks are loaded (true/false)
- `ancient-debris.only-replace-generated-chunks` - Enable/disable only replacing Ancient Debris in generated chunks (true/false)
- `ancient-debris.ensure-chunks-loaded` - Enable/disable ensuring chunks are loaded when replacing Ancient Debris (true/false)
- `ancient-debris.save-replaced-locations` - Enable/disable saving locations of replaced Ancient Debris (true/false)

**Performance settings:**
- `performance.restore-debris-on-disable` - Enable/disable restoring Ancient Debris when the plugin is disabled (true/false)
- `performance.restore-debris-on-config-change` - Enable/disable restoring Ancient Debris when config changes (true/false)
- `performance.max-replacements-per-chunk` - Set the maximum number of Ancient Debris replacements per chunk (integer, -1 for unlimited)

**Advanced settings:**
- `advanced.max-locations-per-world` - Set the maximum number of Ancient Debris locations to store per world (integer, -1 for unlimited)
- `advanced.command-cooldown-seconds` - Set the cooldown between command executions (integer)
- `advanced.log-debris-replacements` - Enable/disable logging Ancient Debris replacements (true/false)
- `advanced.log-inventory-removals` - Enable/disable logging Netherite item removals (true/false)
- `advanced.ignore-creative-spectator` - Enable/disable ignoring players in creative or spectator mode (true/false)
- `advanced.notify-players` - Enable/disable notifying players when their actions are blocked (true/false)

**Detection settings:**
- `detection.use-name-matching` - Enable/disable name-based detection of Netherite items (true/false)

**Timing settings:**
- `timing.delay` - Set the delay between inventory checks (in seconds)
- `timing.multiplier` - Set the tick multiplier (20 = 1 second)

Special commands for managing the Netherite items list:
- `/antinetherite get detection.items` - List all items considered as Netherite items
- `/antinetherite set detection.items add <item>` - Add an item to the Netherite items list
- `/antinetherite set detection.items remove <item>` - Remove an item from the Netherite items list

## Permissions

- `antinetherite.manage` - Allows using the `/antinetherite` command (default: op)
- `antinetherite.bypass` - Allows players to bypass Netherite item removal (default: op)

## Usage

1. Place the built JAR file in your server's `plugins` folder.
2. Start or restart your Minecraft server.
3. Configure the plugin settings in `plugins/AntiNetherite/config.yml` if needed.
4. Use in-game commands to adjust settings as needed.

## Netherite Item Detection

The plugin provides two fully configurable ways to detect Netherite items:

1. **Name Matching**: Any item with a type name containing "NETHERITE" will be considered a Netherite item. This is the default behavior.
2. **Custom List**: You can define a custom list of items to be considered as Netherite items. This is useful if you want to add custom items or only block specific Netherite items.

You can toggle between these methods using the `detection.use-name-matching` setting. If set to `false`, only items in the custom list will be considered Netherite items.

## Netherite Item Protection

The plugin can prevent players from using Netherite items in several ways:

1. **Destructive vs. Non-Destructive Mode**: The global setting `enable-destructive-actions` controls how the plugin handles Netherite items:
   - In destructive mode (default), items are removed from inventories and deleted when dropped
   - In non-destructive mode, events are cancelled but items are not destroyed, making it more player-friendly

2. **Inventory Protection**: The plugin can prevent players from having Netherite items in their inventory:
   - Clear items from player inventories periodically
   - Prevent moving Netherite items in inventories
   - Block automated container (hopper) transfers of Netherite items (disabled by default due to performance impact)
   - Prevent picking up Netherite items from the ground
   - Remove or prevent dropping Netherite items

3. **Usage Prevention**: The plugin can prevent players from using Netherite items:
   - Cancel crafting of Netherite items
   - Prevent equipping Netherite armor
   - Prevent attacking with Netherite weapons

All of these features can be individually configured to create the exact protection system your server needs.

## Ancient Debris Replacement

The plugin can prevent players from obtaining Ancient Debris in two ways, both independently configurable:

1. **Mining Prevention**: When a player starts to mine Ancient Debris, it is immediately replaced with Netherrack and no drops are given.
2. **Generation Prevention**: When new chunks are generated, any Ancient Debris in those chunks is automatically replaced with Netherrack.

These features can be toggled independently using the `ancient-debris.replace-when-mined` and `ancient-debris.replace-on-chunk-load` settings.

### Reversibility

The plugin keeps track of all Ancient Debris that has been replaced with Netherrack. By default, replaced Ancient Debris will NOT be automatically restored when the plugin is disabled or when configuration changes. This behavior can be controlled through the performance settings:

- `performance.restore-debris-on-disable`: Controls whether Ancient Debris is restored when the plugin is disabled
- `performance.restore-debris-on-config-change`: Controls whether Ancient Debris is restored when configuration changes

You can also manually restore all replaced Ancient Debris using the `/antinetherite restore-debris` command.

### Performance Considerations

The Ancient Debris replacement system includes several performance optimizations:

- **Configurable Restoration**: By default, Ancient Debris is NOT restored when the plugin is disabled or when configuration changes, preventing potential lag spikes
- **Replacement Limits**: The `performance.max-replacements-per-chunk` setting limits how many blocks can be replaced per chunk to prevent lag
- **Memory Management**: The `advanced.max-locations-per-world` setting controls how many replaced blocks are tracked per world
- **Logging Control**: The `advanced.log-debris-replacements` and `advanced.log-inventory-removals` settings allow you to reduce console spam
- **Selective Processing**: Only processes chunks in the Nether dimension where Ancient Debris naturally generates
- **Chunk Generation Checking**: Can be configured to only process chunks that have already been generated
- **Chunk Loading Control**: Can be configured to ensure chunks are loaded when replacing or restoring Ancient Debris

**Container Transfer Protection**: The automated container transfer blocking feature (`inventory.cancel-container-transfer`) is disabled by default due to potential performance impact. This feature monitors all hopper and container item movements, which can add significant overhead on servers with many automated systems. Only enable this feature if you specifically need protection against players using hoppers to bypass inventory restrictions.

### Safeguards

The Ancient Debris replacement system includes several safeguards:

- **Thread Safety**: Uses thread-safe collections to prevent concurrent modification issues
- **Error Handling**: Catches and logs exceptions without crashing the plugin
- **Async Processing**: Performs restoration operations asynchronously to prevent server lag
- **Cooldown System**: Prevents command spam with a configurable cooldown period
- **World-Specific Restoration**: Allows restoring Ancient Debris in specific worlds
- **Storage Limits**: Prevents excessive memory usage by limiting the number of stored locations
- **Persistent Storage**: Saves replaced locations to disk for recovery after server restarts

## Compatibility

- Works with Bukkit/Spigot/Paper servers
- Fully compatible with Folia 1.21.4 using region-aware schedulers
- Supports hot reloads

## Contributing

For information on building the plugin and contributing to development, please see [CONTRIBUTING.md](CONTRIBUTING.md).
