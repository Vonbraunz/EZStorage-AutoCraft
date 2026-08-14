package com.zerofall.ezstorage.network.server;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;

import com.zerofall.ezstorage.container.ContainerStorageCoreCrafting;
import com.zerofall.ezstorage.integration.IntegrationUtils;
import com.zerofall.ezstorage.network.client.MsgLoadRecipe;
import com.zerofall.ezstorage.util.EZInventoryManager;
import com.zerofall.ezstorage.util.StoredRecipe;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class HandlerMsgLoadRecipe implements IMessageHandler<MsgLoadRecipe, IMessage> {

    @Override
    public IMessage onMessage(MsgLoadRecipe message, MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().playerEntity;
        if (IntegrationUtils.isSpectatorMode(player)) {
            return null;
        }
        Container container = player.openContainer;
        if (!(container instanceof ContainerStorageCoreCrafting con) || con.inventory == null) {
            return null;
        }

        if (message.index < 0 || message.index >= con.inventory.recipes.size()) {
            return null;
        }
        StoredRecipe stored = con.inventory.recipes.get(message.index);

        ItemStack[][] recipe = new ItemStack[9][];
        for (int i = 0; i < 9; i++) {
            if (stored.matrix[i] != null) {
                recipe[i] = new ItemStack[] { stored.matrix[i] };
            }
        }

        if (con.tryToPopulateCraftingGrid(recipe, player, true)) {
            EZInventoryManager.sendToClients(con.inventory);
            con.detectAndSendChanges();
        }
        return null;
    }
}
