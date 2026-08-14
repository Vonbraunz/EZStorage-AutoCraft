# Simple Storage

Simple storage mod for Minecraft 1.7.10 (Forge).

## Description

Simple Storage (former EZStorage) introduces an early-game storage system that scales and evolves as players progress, while keeping the vanilla flair. Want to put 100k Cobblestone in 1 slot? No problem. The blocks in the mod can add a crafting grid, additional storage, and more. Also includes integration into some mods for easier crafting or additional features!

## Blocks & Items

- **Storage Core**
  - This is the core of your storage system
  - Click on this block to open the GUI (search box included), and add adjacent blocks to expand
  - Each system can only have 1 Storage Core
  - This block can only be broken if it contains no items
- **Storage Box**
  - Tier 1 storage add-on wich increases the storage capacity of the Storage Core by a small amount
- **Condensed Storage Box**
  - Tier 2 storage add-on
- **Hyper Storage Box**
  - Tier 3 storage add-on
- **Proxy Port**
  - Expose the storage inventory to hoppers, conduits, machines and AE2 storage bus
- **Crafting Box**
  - This adds a crafting grid to the GUI of your Storage Core (compatible with NEI + clicking for easy crafting from the internal inventory)
  - Save recipes for later: they show up as extra icons right in the item grid, labeled "Craft", with a red X if you're currently short on materials
  - Click a saved recipe to load it into the grid, shift-click to craft one (ctrl+shift for a full stack), or right-click to delete it
  - A recipe can be saved straight from an NEI overlay even if you don't own the ingredients yet, so it's ready to craft once you gather them
  - Sort mode includes a "Recipe" option that brings your saved recipes to the front of the grid
- **Portable Storage Panel**
  - This adds a small item that features a wood panel with wireless access to your storage core. It's tier can be upgraded and a crafting grid can also be added.
  - Upgrade by putting in crafting grid together with one redstone block and the upgrade item (ender eye, ender pearl, nether star, crafting box)
  - No need for chunkloading the target storage core, it works without!

## Mod Integration

- **Not Enough Items** (GTNH version)
  - Overlay recipes into the crafting grid, pulling ingredients from storage first and your own inventory if needed
  - One-click auto-crafting: crafts as many as your materials allow, sends the results to your inventory (storage only holds the overflow), and counts toward crafting-based quests like GTNH's
  - NEI-like search
- **Waila**
  - Advanced tooltip overlay
  - Show storage content (items/types count) in world tooltip
- **JABBA**
  - Move the storage core from one place to another place using the dolly from Jabba
- **Crafting Tweaks**
  - Show typical crafting tweaks buttons on crafting grid
- **Et Futurum Requiem**
  - Spectator mode
- **Applied Energistics 2**
  - Inventory proxy can be used with AE storage buses

## Remarks

This mod is intented to be a compact storage solution, and not an automated storage network. As of right now, I'm not going to include any features like filtered output, network cables, external monitors, or anything else remeniscent of Applied Energistics. If you have an idea how such features would fit nicely in vanilla worlds, feel free to open an issue for discussion.

As from my side, this mod is feature-completed. I'll try fixing bugs as they were found or make improvements where possible. However, any contribution in form of troubleshooting or pull requests for bugfixes, improvements, mod compat, or new features are welcome at any time.

## Contribution

Feel free to open PRs for features, improvements, or compatibility fixes. I'm maintaining this at minimal effort for use on my server/modpack.

## Changes compared to the original version of EZStorage

This fork becomes some changes to be usable on servers, less-buggy and a lot of feature and code improvements.

- Many bugfixes and some stability and code improvements
- Lots of UI and performance improvements
- Mod compat with NEI, Waila, Jabba, Crafting Tweaks, Et Futurum Requiem, etc.
- Re-made the most textures to look nicer and fit better into vanilla worlds
- Replaced input and output block with a more enhanced proxy block
- Configurable maximum different item types per storage (no limit by default)
- Store storage as separated file in the world's save directory instead directly on the TileEntity (only send to client when needed)
- Saved recipes on the Crafting Box, shown right in the storage grid, with load/craft/delete and a dedicated sort mode
- Fixed NEI auto-crafting silently losing items, not registering GTNH quest crafting progress, and leaving the player-inventory display stale until the GUI was reopened

## Development

With vscode you need to run `gradlew eclipse` for the project to correctly recognize the class paths
