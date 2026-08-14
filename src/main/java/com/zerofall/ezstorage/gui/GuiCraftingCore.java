package com.zerofall.ezstorage.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;

import com.zerofall.ezstorage.EZStorage;
import com.zerofall.ezstorage.Reference;
import com.zerofall.ezstorage.container.ContainerStorageCoreCrafting;
import com.zerofall.ezstorage.enums.SortMode;
import com.zerofall.ezstorage.network.client.MsgClearCraftingGrid;
import com.zerofall.ezstorage.network.client.MsgCraftStoredRecipe;
import com.zerofall.ezstorage.network.client.MsgDeleteRecipe;
import com.zerofall.ezstorage.network.client.MsgLoadRecipe;
import com.zerofall.ezstorage.network.client.MsgSaveRecipe;
import com.zerofall.ezstorage.util.EZInventory;
import com.zerofall.ezstorage.util.StoredRecipe;

import cpw.mods.fml.client.config.GuiButtonExt;

public class GuiCraftingCore extends GuiStorageCore {

    private static final String CRAFT_LABEL = "Craft";
    private static final int CRAFT_LABEL_COLOR = 0xFFAA00;

    protected GuiButtonExt btnClearCraftingPanel;
    private GuiButtonExt btnSaveRecipe;

    private final List<StoredRecipe> displayedRecipes = new ArrayList<StoredRecipe>();

    public GuiCraftingCore(EntityPlayer player, World world, int x, int y, int z) {
        super(new ContainerStorageCoreCrafting(player, world), world, x, y, z);
        this.xSize = 195;
        this.ySize = 256;
    }

    @Override
    public void initGui() {
        super.initGui();
        btnClearCraftingPanel = new GuiButtonExt(10, guiLeft + 99, guiTop + 114, 8, 8, "");
        buttonList.add(btnClearCraftingPanel);

        btnSaveRecipe = new GuiButtonExt(11, guiLeft + 99, guiTop + 126, 8, 8, "+");
        buttonList.add(btnSaveRecipe);
    }

    @Override
    public int rowsVisible() {
        return 5;
    }

    @Override
    protected ResourceLocation getBackground() {
        return new ResourceLocation(Reference.MOD_ID, "textures/gui/storageCraftingGui.png");
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button == btnClearCraftingPanel) {
            EZStorage.instance.network.sendToServer(new MsgClearCraftingGrid());
        } else if (button == btnSaveRecipe) {
            EZStorage.instance.network.sendToServer(new MsgSaveRecipe());
        }
    }

    @Override
    protected int extraEntryCount() {
        refreshDisplayedRecipes();
        return displayedRecipes.size();
    }

    @Override
    protected boolean extraEntriesFirst() {
        return currentSortMode == SortMode.RECIPE;
    }

    @Override
    protected ItemStack getExtraEntryIcon(int extraIndex) {
        if (extraIndex < 0 || extraIndex >= displayedRecipes.size()) {
            return null;
        }
        return getRecipeIcon(displayedRecipes.get(extraIndex));
    }

    @Override
    protected void drawExtraEntryBadge(int extraIndex, int x, int y) {
        if (extraIndex < 0 || extraIndex >= displayedRecipes.size()) {
            return;
        }
        StoredRecipe recipe = displayedRecipes.get(extraIndex);

        drawCraftLabel(x, y);
        if (!canCraft(recipe)) {
            drawInsufficientMaterialsMark(x, y);
        }
    }

    @Override
    protected void handleExtraEntryClick(int extraIndex, int mouseButton, int mode) {
        if (extraIndex < 0 || extraIndex >= displayedRecipes.size()) {
            return;
        }
        StoredRecipe recipe = displayedRecipes.get(extraIndex);
        int realIndex = getInventory().recipes.indexOf(recipe);
        if (realIndex < 0) {
            return;
        }

        if (mouseButton == 1) {
            EZStorage.instance.network.sendToServer(new MsgDeleteRecipe(realIndex));
        } else if (mouseButton == 0) {
            if (mode == 1) {
                int count = GuiScreen.isCtrlKeyDown() ? 64 : 1;
                EZStorage.instance.network.sendToServer(new MsgCraftStoredRecipe(realIndex, count));
            } else {
                EZStorage.instance.network.sendToServer(new MsgLoadRecipe(realIndex));
            }
        }
    }

    @Override
    protected void drawExtraEntryTooltip(int mouseX, int mouseY) {
        int extraIndex = mouseOverExtraIndex;
        if (extraIndex < 0 || extraIndex >= displayedRecipes.size()) {
            return;
        }
        StoredRecipe recipe = displayedRecipes.get(extraIndex);
        List<String> tip = new ArrayList<String>();
        tip.add(recipe.name != null && !recipe.name.isEmpty() ? recipe.name : "Recipe");
        if (!canCraft(recipe)) {
            tip.add(EnumChatFormatting.RED + "Not enough materials");
        }
        tip.add(EnumChatFormatting.GRAY + "Click: load into grid");
        tip.add(EnumChatFormatting.GRAY + "Shift+Click: craft one");
        tip.add(EnumChatFormatting.GRAY + "Ctrl+Shift+Click: craft a stack");
        tip.add(EnumChatFormatting.GRAY + "Right-Click: delete");
        func_146283_a(tip, mouseX, mouseY);
    }

    private void refreshDisplayedRecipes() {
        displayedRecipes.clear();
        String text = searchText == null ? ""
            : searchText.trim()
                .toLowerCase();
        for (StoredRecipe recipe : getInventory().recipes) {
            String name = recipe.name == null ? "" : recipe.name;
            if (text.isEmpty() || name.toLowerCase()
                .contains(text)) {
                displayedRecipes.add(recipe);
            }
        }
        Collections.sort(displayedRecipes, (a, b) -> {
            String an = a.name == null ? "" : a.name;
            String bn = b.name == null ? "" : b.name;
            return an.compareToIgnoreCase(bn);
        });
    }

    private ItemStack getRecipeIcon(StoredRecipe recipe) {
        InventoryCrafting temp = new InventoryCrafting(new Container() {

            @Override
            public boolean canInteractWith(EntityPlayer playerIn) {
                return false;
            }
        }, 3, 3);
        for (int i = 0; i < 9; i++) {
            if (recipe.matrix[i] != null) {
                temp.setInventorySlotContents(i, recipe.matrix[i]);
            }
        }
        return CraftingManager.getInstance()
            .findMatchingRecipe(temp, this.mc.theWorld);
    }

    /**
     * Whether the combined storage + player inventory currently holds enough of every ingredient to
     * craft this recipe at least once.
     */
    private boolean canCraft(StoredRecipe recipe) {
        List<ItemStack> templates = new ArrayList<ItemStack>();
        List<Integer> needed = new ArrayList<Integer>();

        for (ItemStack slotStack : recipe.matrix) {
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
                needed.set(templateIndex, needed.get(templateIndex) + 1);
            } else {
                templates.add(slotStack);
                needed.add(1);
            }
        }

        for (int i = 0; i < templates.size(); i++) {
            if (countAvailable(templates.get(i)) < needed.get(i)) {
                return false;
            }
        }
        return true;
    }

    private int countAvailable(ItemStack template) {
        int count = 0;
        for (ItemStack group : getInventory().inventory) {
            if (ContainerStorageCoreCrafting.isRecipeItemValid(template, group)) {
                count += group.stackSize;
            }
        }
        for (ItemStack stack : this.mc.thePlayer.inventory.mainInventory) {
            if (stack != null && ContainerStorageCoreCrafting.isRecipeItemValid(template, stack)) {
                count += stack.stackSize;
            }
        }
        return count;
    }

    private void drawCraftLabel(int x, int y) {
        boolean unicodeFlag = fontRendererObj.getUnicodeFlag();
        fontRendererObj.setUnicodeFlag(false);

        int fullWidth = fontRendererObj.getStringWidth(CRAFT_LABEL);
        // Cap the on-screen width at 16px (the icon's own width) so the label can never overflow the
        // 18px cell, regardless of the font's actual metrics.
        float scale = fullWidth > 0 ? Math.min(0.5f, 16.0f / fullWidth) : 0.5f;
        float invScale = 1.0f / scale;

        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glPushMatrix();
        GL11.glScaled(scale, scale, scale);
        int drawX = (int) (((x + 9.0f) - fullWidth * scale / 2.0f) * invScale);
        int drawY = (int) ((y + 14.0f - 8.0f * scale) * invScale);
        // Hand-rolled outline (black in all four directions) instead of drawStringWithShadow's single
        // bottom-right shadow -- the gold-on-gray text was hard to read against the icon.
        fontRendererObj.drawString(CRAFT_LABEL, drawX - 1, drawY, 0x000000);
        fontRendererObj.drawString(CRAFT_LABEL, drawX + 1, drawY, 0x000000);
        fontRendererObj.drawString(CRAFT_LABEL, drawX, drawY - 1, 0x000000);
        fontRendererObj.drawString(CRAFT_LABEL, drawX, drawY + 1, 0x000000);
        fontRendererObj.drawString(CRAFT_LABEL, drawX, drawY, CRAFT_LABEL_COLOR);
        GL11.glPopMatrix();
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_DEPTH_TEST);

        fontRendererObj.setUnicodeFlag(unicodeFlag);
    }

    private void drawInsufficientMaterialsMark(int x, int y) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);

        Tessellator tessellator = Tessellator.instance;

        // Black outline: same X, drawn wider and underneath the red one.
        GL11.glLineWidth(10.0f);
        GL11.glColor4f(0.0f, 0.0f, 0.0f, 0.95f);
        tessellator.startDrawing(GL11.GL_LINES);
        tessellator.addVertex(x + 5, y + 5, 0);
        tessellator.addVertex(x + 13, y + 13, 0);
        tessellator.addVertex(x + 13, y + 5, 0);
        tessellator.addVertex(x + 5, y + 13, 0);
        tessellator.draw();

        GL11.glLineWidth(4.5f);
        GL11.glColor4f(1.0f, 0.15f, 0.15f, 0.95f);
        tessellator.startDrawing(GL11.GL_LINES);
        tessellator.addVertex(x + 5, y + 5, 0);
        tessellator.addVertex(x + 13, y + 13, 0);
        tessellator.addVertex(x + 13, y + 5, 0);
        tessellator.addVertex(x + 5, y + 13, 0);
        tessellator.draw();

        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }
}
