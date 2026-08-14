package com.zerofall.ezstorage.network.client;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

public class MsgCraftStoredRecipe implements IMessage {

    public int index;
    public int count;

    public MsgCraftStoredRecipe() {}

    public MsgCraftStoredRecipe(int index, int count) {
        this.index = index;
        this.count = count;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        index = ByteBufUtils.readVarInt(buf, 5);
        count = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeVarInt(buf, index, 5);
        buf.writeInt(count);
    }
}
