package com.zerofall.ezstorage.network.server;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;

import com.zerofall.ezstorage.container.ContainerStorageCoreCrafting;
import com.zerofall.ezstorage.integration.IntegrationUtils;
import com.zerofall.ezstorage.network.client.MsgSaveRecipe;
import com.zerofall.ezstorage.util.EZInventory;
import com.zerofall.ezstorage.util.EZInventoryManager;
import com.zerofall.ezstorage.util.StoredRecipe;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class HandlerMsgSaveRecipe implements IMessageHandler<MsgSaveRecipe, IMessage> {

    @Override
    public IMessage onMessage(MsgSaveRecipe message, MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().playerEntity;
        if (IntegrationUtils.isSpectatorMode(player)) {
            return null;
        }
        Container container = player.openContainer;
        if (!(container instanceof ContainerStorageCoreCrafting con) || con.inventory == null) {
            return null;
        }

        ItemStack[] matrix = new ItemStack[9];
        boolean empty = true;
        for (int i = 0; i < 9; i++) {
            // Prefer whatever's physically in the grid; fall back to the last NEI-requested recipe so a
            // recipe can still be saved even when none of its ingredients are currently owned.
            ItemStack stack = con.craftMatrix.getStackInSlot(i);
            if (stack == null && con.lastRequestedRecipe != null) {
                stack = con.lastRequestedRecipe[i];
            }
            if (stack != null) {
                matrix[i] = stack.copy();
                empty = false;
            }
        }
        if (empty || matrixAlreadySaved(con, matrix)) {
            return null;
        }

        InventoryCrafting nameLookup = new InventoryCrafting(new Container() {

            @Override
            public boolean canInteractWith(EntityPlayer playerIn) {
                return false;
            }
        }, 3, 3);
        for (int i = 0; i < 9; i++) {
            if (matrix[i] != null) {
                nameLookup.setInventorySlotContents(i, matrix[i]);
            }
        }
        ItemStack result = CraftingManager.getInstance()
            .findMatchingRecipe(nameLookup, player.worldObj);
        String baseName = result != null ? result.getDisplayName() : "Recipe";

        String name = baseName;
        int suffix = 2;
        while (nameInUse(con, name)) {
            name = baseName + " (" + suffix + ")";
            suffix++;
        }

        con.inventory.recipes.add(new StoredRecipe(name, matrix));
        con.inventory.setHasChanges();
        EZInventoryManager.saveInventory(con.inventory);
        EZInventoryManager.sendToClients(con.inventory);
        return null;
    }

    private static boolean nameInUse(ContainerStorageCoreCrafting con, String name) {
        for (StoredRecipe recipe : con.inventory.recipes) {
            if (name.equals(recipe.name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * True if a recipe with this exact ingredient arrangement is already saved, so repeat clicks on an
     * unchanged grid don't pile up numbered duplicates ("Torch", "Torch (2)", ...).
     */
    private static boolean matrixAlreadySaved(ContainerStorageCoreCrafting con, ItemStack[] matrix) {
        for (StoredRecipe recipe : con.inventory.recipes) {
            boolean same = true;
            for (int i = 0; i < 9; i++) {
                if (!EZInventory.stacksEqual(matrix[i], recipe.matrix[i])) {
                    same = false;
                    break;
                }
            }
            if (same) {
                return true;
            }
        }
        return false;
    }
}
