package com.zerofall.ezstorage.network.server;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;

import com.zerofall.ezstorage.container.ContainerStorageCoreCrafting;
import com.zerofall.ezstorage.integration.IntegrationUtils;
import com.zerofall.ezstorage.network.client.MsgReqCrafting;
import com.zerofall.ezstorage.util.EZInventoryManager;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class HandlerMsgReqCrafting implements IMessageHandler<MsgReqCrafting, IMessage> {

    ItemStack[][] recipe;

    @Override
    public IMessage onMessage(MsgReqCrafting message, MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().playerEntity;
        if (IntegrationUtils.isSpectatorMode(player)) {
            return null; // no response, we're in read-only mode
        }
        Container container = player.openContainer;
        if (container instanceof ContainerStorageCoreCrafting con) {
            this.recipe = new ItemStack[9][];
            for (int x = 0; x < this.recipe.length; x++) {
                NBTTagList list = message.recipe.getTagList("#" + x, 10);
                if (list.tagCount() > 0) {
                    this.recipe[x] = new ItemStack[list.tagCount()];
                    for (int y = 0; y < list.tagCount(); y++) {
                        this.recipe[x][y] = ItemStack.loadItemStackFromNBT(list.getCompoundTagAt(y));
                    }
                }
            }

            // Remember what NEI actually asked for (one representative item per slot), even for slots
            // tryToPopulateCraftingGrid can't fill below -- lets "Save Recipe" work without the materials.
            ItemStack[] ghost = new ItemStack[9];
            for (int i = 0; i < 9; i++) {
                if (this.recipe[i] != null && this.recipe[i].length > 0 && this.recipe[i][0] != null) {
                    ghost[i] = this.recipe[i][0].copy();
                }
            }
            con.lastRequestedRecipe = ghost;

            if (con.tryToPopulateCraftingGrid(recipe, player, true)) {
                EZInventoryManager.sendToClients(con.inventory);
                con.detectAndSendChanges();
            }
        }

        return null;
    }

}
