# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Minecraft Spigot/PaperMC plugin (Java 21, Spigot API 1.21.5) that implements a waystone teleportation system. Players can craft special banners, place them as waystones with custom names, and teleport between them via an interactive GUI. The plugin uses SQLite for persistence and ArmorStands for hologram displays.

## Build and Development Commands

### Building the plugin
```bash
mvn clean package
```
The compiled JAR will be in `target/waystone-1.0.0.jar` (shaded with maven-shade-plugin).

### Testing locally
1. Copy the JAR to your Spigot/Paper server's `plugins/` folder
2. Restart the server
3. The plugin will create `plugins/waystone/waystones.db` on first run

## Architecture

### Core Components

**WaystonePlugin (main class)**
- Initializes SQLite database on startup (src/main/java/fr/cyberdodo/waystone/config/SQLiteDatabase.java)
- Registers crafting recipe for waystone banners
- Registers all event listeners (10+ listeners for different interactions)
- Starts particle effect task for active waystones

**Data Layer**
- `WaystoneDAO`: Database access with CRUD operations for waystones. Uses Base64 serialization for ItemStack storage (preserves banner patterns)
- `WaystoneData`: Data model containing id, name, location, bannerItem (full ItemStack), and hologramId
- `SQLiteDatabase`: Connection wrapper, creates table with schema: (id, name, world, x, y, z, item_base64, hologram_id)

**Manager Layer**
- `WaystoneManager`: Orchestrates waystone lifecycle (create/delete/rename). Coordinates between DAO and HologramManager
- `HologramManager`: Manages invisible ArmorStands as floating text displays. Maintains in-memory map for quick lookups. Handles server reload scenarios by searching world entities
- `WaystoneTeleportManager`: 5-second teleport animation with particles, sounds, and potion effects (blindness/nausea). Costs 1 XP level. Cancellable on damage
- `WaystoneParticleManager`: Async task spawning portal particles around all active waystones

**UI Layer**
- `WaystoneInventory`: Builds 54-slot GUI showing all waystones as their original banners with coordinates in lore. Slot 53 contains anvil for rename function

**Event Listeners**
Critical listeners to understand:
- `WaystoneCraftListener`: Detects waystone recipe crafts, preserves banner colors/patterns in result
- `WaystonePlaceListener`: Intercepts banner placement, triggers name input via chat listener
- `WaystoneChatListener`: Captures next chat message as waystone name after placement
- `WaystoneInteractListener`: Right-click on placed waystone opens teleport GUI, shift+right-click breaks it
- `WaystoneInventoryListener`: Handles clicks in teleport GUI (click banner to teleport, click anvil to rename)
- `WaystoneBreakListener`, `WaystoneExplosionListener`, `WaystonePistonListener`, `WaystonePhysicsListener`: Protection against unintended waystone destruction
- `WaystoneTeleportListener`: Cancels teleport on player damage during channel

### Key Design Patterns

**Hologram lifecycle**: When creating a waystone, the flow is:
1. Insert DB record with hologram_id=0
2. Retrieve inserted record to get auto-generated waystone ID
3. Spawn ArmorStand hologram, get its entity ID
4. Update DB record with the hologram's entity ID

This two-step process ensures both IDs are properly linked for deletion/reload scenarios.

**ItemStack serialization**: Banner ItemStacks are serialized to Base64 using BukkitObjectOutputStream. This preserves all NBT data including banner patterns, which is critical since waystones can be any colored banner with custom patterns.

**Teleport system**: Uses Bukkit scheduler's repeating task pattern. The TeleportTicker runs every tick for 100 ticks (5 seconds), spawning portal particles and playing sounds. Movement or damage triggers cancellation via the damage listener.

## Dependencies

- Spigot API 1.21.5 (provided scope)
- Lombok 1.18.36 (code generation for data classes)

## Important Notes

- The plugin has no commands defined in plugin.yml - all interaction is through crafting, placing, and right-clicking waystones
- Waystone naming happens through chat interception, not commands
- The hologram system maintains both in-memory cache and persistent entity IDs to survive server reloads
- XP cost is hardcoded to 1 level per teleport in WaystoneTeleportManager:42-47