package com.zerofall.ezstorage.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class StoredRecipe {

    public String name;
    public ItemStack[] matrix;

    public StoredRecipe() {
        this.matrix = new ItemStack[9];
    }

    public StoredRecipe(String name, ItemStack[] matrix) {
        this.name = name;
        this.matrix = matrix;
    }

    public boolean isEmpty() {
        for (ItemStack stack : matrix) {
            if (stack != null) {
                return false;
            }
        }
        return true;
    }

    public void writeToNBT(NBTTagCompound tag) {
        tag.setString("Name", this.name == null ? "" : this.name);
        NBTTagList gridList = new NBTTagList();
        for (int i = 0; i < 9; i++) {
            NBTTagCompound slotTag = new NBTTagCompound();
            slotTag.setByte("Slot", (byte) i);
            if (this.matrix[i] != null) {
                this.matrix[i].writeToNBT(slotTag);
            }
            gridList.appendTag(slotTag);
        }
        tag.setTag("Matrix", gridList);
    }

    public static StoredRecipe readFromNBT(NBTTagCompound tag) {
        StoredRecipe recipe = new StoredRecipe();
        recipe.name = tag.getString("Name");
        NBTTagList gridList = tag.getTagList("Matrix", 10);
        for (int i = 0; i < gridList.tagCount(); i++) {
            NBTTagCompound slotTag = gridList.getCompoundTagAt(i);
            byte slotIndex = slotTag.getByte("Slot");
            if (slotIndex >= 0 && slotIndex < 9) {
                recipe.matrix[slotIndex] = ItemStack.loadItemStackFromNBT(slotTag);
            }
        }
        return recipe;
    }
}
