# Contributing to AntiNetherite

This document contains technical information and development guidelines for contributors to the AntiNetherite plugin.

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

## Building

This plugin uses Gradle for building. It supports both Paper and Folia servers.

### Requirements

- Java 21 or higher (required for Paper 1.21.4)
- Gradle 8.0 or higher

### Building the plugin

1. Clone the repository
2. Run `./gradlew build` (Linux/macOS) or `gradlew.bat build` (Windows)
3. The built JAR file will be in `build/libs/antinetherite-INDEV.jar`

### Development

The plugin is set up to support both Paper and Folia servers. It uses:

- Paper API for standard server functionality
- Folia API for compatibility with Folia servers
- Region-aware schedulers for Folia compatibility

When developing, keep these guidelines in mind:

1. Always check if the server is Folia before using Bukkit schedulers
2. Use the appropriate scheduler based on the server type
3. For entity-related operations in Folia, use the entity's scheduler
4. For location-based operations in Folia, use the region scheduler
5. For global operations in Folia, use the global region scheduler

The plugin automatically detects whether it's running on Paper or Folia and uses the appropriate APIs.

## Problems

Open an issue on the GitHub repository if you have any problems or suggestions. 