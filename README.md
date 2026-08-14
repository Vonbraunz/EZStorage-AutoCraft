# Simple Storage

A no-frills item storage mod for Minecraft 1.7.10 / Forge. One block holds everything: no cables, no network to manage, no energy to feed. Right-click, search, done.

## Why this exists

Chests fill up. Sorting them by hand doesn't scale. Simple Storage collapses that problem into a single **Storage Core**: place it, add tiers of storage blocks next to it as your stockpile grows, and every item in the system lives in one searchable GUI. It's deliberately not a full automation network. If you want conveyor belts and remote terminals, this isn't that mod. If you want a chest room that doesn't need forty chests, it is.

## Getting started

1. Craft and place a **Storage Core**.
2. Right-click to open it (there's a search bar built in from the start).
3. Place **Storage Box** / **Condensed Storage Box** / **Hyper Storage Box** tiers directly against the Core to raise its capacity.
4. Add a **Crafting Box** for a 3x3 grid wired straight into your stockpile, or a **Proxy Port** to expose the inventory to hoppers, pipes, and machines.

Only one Core per system, and it won't break while it's still holding items, so you can't lose a stash to a stray explosion.

Away from base? Craft a **Portable Storage Panel** for wireless access to a Core, no chunkloading required. It starts basic but can be upgraded (crafting grid + redstone block + an upgrade item: ender eye, ender pearl, nether star, or crafting box) to raise its tier and even add its own crafting grid.

## Crafting without leaving the grid

The Crafting Box pulls ingredients directly from storage as you place them, and returns anything you take back out. On top of that:

- **Recipes stick around.** Build something once and save it: it appears as its own icon inside the storage grid, so you're not hunting through a mental list of "things I usually make."
- **A red X means you're short something.** The saved-recipe icon checks your storage *and* your own inventory in real time, so you know before you click whether you can actually make it.
- **A blue diamond means you're short something, but it can fix that itself.** If another saved recipe produces the missing ingredient, the Crafting Box will auto-craft that first, then continue, recursively, as many levels deep as it needs to. Hover the icon to see exactly what it'll craft along the way before you commit to it.
- **One click reloads it, one modifier crafts it.** Click to drop a saved recipe back into the grid, shift-click to craft a single one straight to your inventory, ctrl+shift-click to knock out a full stack, right-click to remove it from the list.
- **You don't need the ingredients to save the recipe.** Pull a recipe up in NEI and save it on the spot; it'll sit there with a red X as a reminder of what to go collect.
- **Sort by "Recipe"** to pull all your saved recipes to the front of the grid instead of hunting for them among your items.

## Talks to your other mods

| Mod | What you get |
|---|---|
| Not Enough Items (GTNH) | Drop a recipe into the grid straight from the NEI overlay, or one-click auto-craft it: results go to your inventory first, storage only catches the overflow, and it counts toward crafting-based quest lines |
| Waila | Tooltip showing item/type counts when you look at a Storage Core |
| JABBA | Move a fully-stocked Storage Core with a dolly instead of breaking it down |
| Crafting Tweaks | The usual grid-manipulation buttons, right on the Crafting Box |
| Et Futurum Requiem | Respects spectator mode, no reading other players' storage by flying through walls |
| Applied Energistics 2 | Proxy Port doubles as an AE2 storage bus target |

## Building

Standard RetroFuturaGradle project. `./gradlew build` for a jar, `./gradlew runClient` for a dev client. VS Code users should run `gradlew eclipse` once so the editor picks up the class paths correctly.
