package com.zerofall.ezstorage.network.server;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;

import com.zerofall.ezstorage.container.ContainerStorageCoreCrafting;
import com.zerofall.ezstorage.integration.IntegrationUtils;
import com.zerofall.ezstorage.network.client.MsgDeleteRecipe;
import com.zerofall.ezstorage.util.EZInventoryManager;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class HandlerMsgDeleteRecipe implements IMessageHandler<MsgDeleteRecipe, IMessage> {

    @Override
    public IMessage onMessage(MsgDeleteRecipe message, MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().playerEntity;
        if (IntegrationUtils.isSpectatorMode(player)) {
            return null;
        }
        Container container = player.openContainer;
        if (!(container instanceof ContainerStorageCoreCrafting con) || con.inventory == null) {
            return null;
        }

        if (message.index >= 0 && message.index < con.inventory.recipes.size()) {
            con.inventory.recipes.remove(message.index);
            con.inventory.setHasChanges();
            EZInventoryManager.saveInventory(con.inventory);
            EZInventoryManager.sendToClients(con.inventory);
        }
        return null;
    }
}
