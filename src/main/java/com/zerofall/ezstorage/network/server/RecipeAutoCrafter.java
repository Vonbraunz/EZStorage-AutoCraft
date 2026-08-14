package com.zerofall.ezstorage.network.server;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;

import com.zerofall.ezstorage.container.ContainerStorageCoreCrafting;
import com.zerofall.ezstorage.util.EZInventory;
import com.zerofall.ezstorage.util.StoredRecipe;

import cpw.mods.fml.common.registry.GameData;

/**
 * Before a craft request runs, recursively tops up any ingredient shortfall by auto-crafting from the
 * player's other saved recipes -- e.g. if you're out of sticks but have a saved "sticks" recipe and
 * enough planks, that gets crafted automatically on the way to the recipe that actually needs sticks.
 * <p>
 * Deliberately scoped to the player's <em>saved</em> recipes only (not every registered recipe in the
 * game) so the chain stays predictable and bounded. Best-effort: if it can't fully resolve a shortage,
 * the normal craft extraction that runs afterwards just runs out and stops, same as it always has.
 */
public class RecipeAutoCrafter {

    private static final int MAX_DEPTH = 8;

    public static void resolve(ItemStack[] matrix, int count, EntityPlayerMP player, ContainerStorageCoreCrafting con) {
        resolve(matrix, count, player, con, new HashSet<String>(), 0);
    }

    private static void resolve(ItemStack[] matrix, int count, EntityPlayerMP player, ContainerStorageCoreCrafting con,
        Set<String> inProgress, int depth) {
        if (depth >= MAX_DEPTH || count <= 0) {
            return;
        }

        List<ItemStack> templates = new ArrayList<ItemStack>();
        List<Integer> needed = new ArrayList<Integer>();
        for (ItemStack slotStack : matrix) {
            if (slotStack == null) {
                continue;
            }
            int templateIndex = -1;
            for (int i = 0; i < templates.size(); i++) {
                if (EZInventory.stacksEqual(templates.get(i), slotStack)) {
                    templateIndex = i;
                    break;
                }
            }
            if (templateIndex >= 0) {
                needed.set(templateIndex, needed.get(templateIndex) + count);
            } else {
                templates.add(slotStack);
                needed.add(count);
            }
        }

        for (int i = 0; i < templates.size(); i++) {
            ItemStack template = templates.get(i);
            int requiredTotal = needed.get(i);
            int available = countAvailable(con.inventory, player, template);
            if (available >= requiredTotal) {
                continue;
            }

            String key = itemKey(template);
            if (inProgress.contains(key)) {
                // A recipe further up this chain already produces this item -- bail rather than loop.
                continue;
            }

            StoredRecipe subRecipe = findRecipeProducing(con.inventory.recipes, template, player);
            if (subRecipe == null) {
                continue;
            }

            ItemStack subResult = CraftingManager.getInstance()
                .findMatchingRecipe(toInventoryCrafting(subRecipe.matrix), player.worldObj);
            if (subResult == null) {
                continue;
            }

            int perBatch = Math.max(1, subResult.stackSize);
            int shortfall = requiredTotal - available;
            int batches = (shortfall + perBatch - 1) / perBatch;

            inProgress.add(key);
            resolve(subRecipe.matrix, batches, player, con, inProgress, depth + 1);

            ItemStack[][] subRecipeSlots = new ItemStack[9][];
            for (int s = 0; s < 9; s++) {
                if (subRecipe.matrix[s] != null) {
                    subRecipeSlots[s] = new ItemStack[] { subRecipe.matrix[s] };
                }
            }
            HandlerMsgAutoCraft.craft(subRecipeSlots, batches, player, con);
            inProgress.remove(key);
        }
    }

    private static StoredRecipe findRecipeProducing(List<StoredRecipe> recipes, ItemStack template,
        EntityPlayerMP player) {
        for (StoredRecipe recipe : recipes) {
            ItemStack result = CraftingManager.getInstance()
                .findMatchingRecipe(toInventoryCrafting(recipe.matrix), player.worldObj);
            if (result != null && ContainerStorageCoreCrafting.isRecipeItemValid(template, result)) {
                return recipe;
            }
        }
        return null;
    }

    private static InventoryCrafting toInventoryCrafting(ItemStack[] matrix) {
        InventoryCrafting temp = new InventoryCrafting(new Container() {

            @Override
            public boolean canInteractWith(EntityPlayer playerIn) {
                return false;
            }
        }, 3, 3);
        for (int i = 0; i < 9; i++) {
            if (matrix[i] != null) {
                temp.setInventorySlotContents(i, matrix[i]);
            }
        }
        return temp;
    }

    private static int countAvailable(EZInventory inventory, EntityPlayerMP player, ItemStack template) {
        int count = 0;
        for (ItemStack group : inventory.inventory) {
            if (ContainerStorageCoreCrafting.isRecipeItemValid(template, group)) {
                count += group.stackSize;
            }
        }
        for (ItemStack stack : player.inventory.mainInventory) {
            if (stack != null && ContainerStorageCoreCrafting.isRecipeItemValid(template, stack)) {
                count += stack.stackSize;
            }
        }
        return count;
    }

    private static String itemKey(ItemStack stack) {
        String name = GameData.getItemRegistry()
            .getNameForObject(stack.getItem());
        return name + "@" + stack.getItemDamage();
    }
}
