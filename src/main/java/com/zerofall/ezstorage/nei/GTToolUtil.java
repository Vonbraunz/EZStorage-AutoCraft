package com.zerofall.ezstorage.nei;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public final class GTToolUtil {

    private GTToolUtil() {}

    public static boolean isGTTool(ItemStack stack) {
        if (stack == null || !stack.hasTagCompound()) return false;
        NBTTagCompound tag = stack.getTagCompound();
        return tag != null && tag.hasKey("GT.ToolStats");
    }

    public static long getGTDurability(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null || !tag.hasKey("GT.ToolStats")) return 0;
        NBTTagCompound stats = tag.getCompoundTag("GT.ToolStats");
        return stats.getLong("MaxDamage") - stats.getLong("Damage");
    }

    public static void incrementGTDamage(ItemStack stack, int amount) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null || !tag.hasKey("GT.ToolStats")) return;
        NBTTagCompound stats = tag.getCompoundTag("GT.ToolStats");
        stats.setLong("Damage", stats.getLong("Damage") + amount);
    }
}
