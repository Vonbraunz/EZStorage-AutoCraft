package com.zerofall.ezstorage.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import org.lwjgl.input.Keyboard;
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
import cpw.mods.fml.common.registry.GameData;

public class GuiCraftingCore extends GuiStorageCore {

    private static final String CRAFT_LABEL = "Craft";
    private static final int CRAFT_LABEL_COLOR = 0xFFAA00;
    private static final int CHAIN_LABEL_COLOR = 0x55CFFF;
    private static final int MAX_CHAIN_DEPTH = 8;
    private static final int MAX_PROMPTED_COUNT = 10000;
    private static final int PROMPT_BOX_WIDTH = 120;
    private static final int PROMPT_BOX_HEIGHT = 44;

    protected GuiButtonExt btnClearCraftingPanel;
    private GuiButtonExt btnSaveRecipe;

    private final List<StoredRecipe> displayedRecipes = new ArrayList<StoredRecipe>();

    private GuiTextField craftCountField;
    private int promptRecipeIndex = -1;

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

        craftCountField = new GuiTextField(
            fontRendererObj,
            promptBoxX() + 8,
            promptBoxY() + 18,
            PROMPT_BOX_WIDTH - 16,
            fontRendererObj.FONT_HEIGHT + 2);
        craftCountField.setMaxStringLength(6);
    }

    private int promptBoxX() {
        return guiLeft + (xSize - PROMPT_BOX_WIDTH) / 2;
    }

    private int promptBoxY() {
        return guiTop + (ySize - PROMPT_BOX_HEIGHT) / 2;
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
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (promptRecipeIndex >= 0) {
            if (isOverCraftCountField(mouseX, mouseY)) {
                craftCountField.mouseClicked(mouseX, mouseY, mouseButton);
            } else {
                closeCraftCountPrompt();
            }
            return;
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (promptRecipeIndex >= 0) {
            if (keyCode == Keyboard.KEY_ESCAPE) {
                closeCraftCountPrompt();
            } else if (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER) {
                submitCraftCountPrompt();
            } else if (Character.isDigit(typedChar) || keyCode == Keyboard.KEY_BACK
                || keyCode == Keyboard.KEY_DELETE
                || keyCode == Keyboard.KEY_LEFT
                || keyCode == Keyboard.KEY_RIGHT
                || keyCode == Keyboard.KEY_HOME
                || keyCode == Keyboard.KEY_END) {
                    craftCountField.textboxKeyTyped(typedChar, keyCode);
                }
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Runs after everything else this GUI (and its buttons/tooltips) draws, so the prompt
        // reliably paints on top instead of getting covered by button hover tooltips underneath it.
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (promptRecipeIndex >= 0) {
            drawCraftCountPrompt();
        }
    }

    @Override
    public void handleMouseInput() {
        // Block scroll-wheel from reaching the storage grid underneath while the prompt is open.
        if (promptRecipeIndex < 0) {
            super.handleMouseInput();
        }
    }

    private boolean isOverCraftCountField(int mouseX, int mouseY) {
        return mouseX >= craftCountField.xPosition && mouseX < craftCountField.xPosition + craftCountField.width
            && mouseY >= craftCountField.yPosition
            && mouseY < craftCountField.yPosition + craftCountField.height;
    }

    private void openCraftCountPrompt(int realIndex) {
        promptRecipeIndex = realIndex;
        craftCountField.setText("1");
        craftCountField.setFocused(true);
        craftCountField.setCursorPositionEnd();
    }

    private void closeCraftCountPrompt() {
        promptRecipeIndex = -1;
        craftCountField.setFocused(false);
    }

    private void submitCraftCountPrompt() {
        int count;
        try {
            count = Integer.parseInt(
                craftCountField.getText()
                    .trim());
        } catch (NumberFormatException ignored) {
            count = 0;
        }
        if (count > 0 && promptRecipeIndex >= 0) {
            count = Math.min(count, MAX_PROMPTED_COUNT);
            EZStorage.instance.network.sendToServer(new MsgCraftStoredRecipe(promptRecipeIndex, count));
        }
        closeCraftCountPrompt();
    }

    private void drawCraftCountPrompt() {
        int boxX = promptBoxX();
        int boxY = promptBoxY();

        drawRect(guiLeft, guiTop, guiLeft + xSize, guiTop + ySize, 0xCC000000);
        drawRect(boxX, boxY, boxX + PROMPT_BOX_WIDTH, boxY + PROMPT_BOX_HEIGHT, 0xFF333333);
        fontRendererObj.drawStringWithShadow("Craft how many?", boxX + 8, boxY + 6, 0xFFFFFF);
        craftCountField.drawTextBox();
        fontRendererObj.drawStringWithShadow("Enter to craft, Esc to cancel", boxX + 8, boxY + 32, 0xAAAAAA);
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

        boolean ready = canCraft(recipe);
        boolean chainable = !ready && canCraftRecursively(recipe);
        drawCraftLabel(x, y, chainable ? CHAIN_LABEL_COLOR : CRAFT_LABEL_COLOR);
        if (chainable) {
            drawChainableMark(x, y);
        } else if (!ready) {
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
        } else if (mouseButton == 2) {
            openCraftCountPrompt(realIndex);
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
        boolean ready = canCraft(recipe);
        if (!ready) {
            LinkedHashMap<String, ChainStep> steps = new LinkedHashMap<String, ChainStep>();
            boolean chainable = canSatisfy(recipe.matrix, 1, buildVirtualStock(), new HashSet<String>(), 0, steps);
            if (chainable) {
                tip.add(EnumChatFormatting.AQUA + "Craftable via other saved recipes:");
                for (ChainStep step : steps.values()) {
                    tip.add(EnumChatFormatting.GRAY + "  " + step.quantity + "x " + step.displayName);
                }
            } else {
                tip.add(EnumChatFormatting.RED + "Not enough materials");
            }
        }
        tip.add(EnumChatFormatting.GRAY + "Click: load into grid");
        tip.add(EnumChatFormatting.GRAY + "Shift+Click: craft one");
        tip.add(EnumChatFormatting.GRAY + "Ctrl+Shift+Click: craft a stack");
        tip.add(EnumChatFormatting.GRAY + "Middle-Click: craft a custom amount");
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

    /**
     * Whether this recipe could be crafted after auto-crafting any missing ingredients from the player's
     * other saved recipes first (mirrors the server-side RecipeAutoCrafter, but as a pure feasibility
     * check -- nothing here is actually crafted). Simulates consumption against a shared virtual stock
     * pool so ingredients competing for the same underlying raw materials are accounted for correctly.
     */
    private boolean canCraftRecursively(StoredRecipe recipe) {
        return canSatisfy(recipe.matrix, 1, buildVirtualStock(), new HashSet<String>(), 0, null);
    }

    /**
     * @param steps if non-null, records each sub-recipe used to resolve a shortfall (keyed by item, so
     *              the same ingredient needed via multiple branches is reported once with a combined
     *              quantity) -- used to preview a chain-craft before committing to it.
     */
    private boolean canSatisfy(ItemStack[] matrix, int count, Map<String, Integer> stock, Set<String> inProgress,
        int depth, Map<String, ChainStep> steps) {
        if (depth >= MAX_CHAIN_DEPTH || count <= 0) {
            return false;
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
            String key = itemKey(template);
            int have = stock.containsKey(key) ? stock.get(key) : 0;

            if (have >= requiredTotal) {
                stock.put(key, have - requiredTotal);
                continue;
            }

            int shortfall = requiredTotal - have;
            stock.put(key, 0);

            if (inProgress.contains(key)) {
                return false;
            }

            StoredRecipe subRecipe = findRecipeProducing(template);
            if (subRecipe == null) {
                return false;
            }

            ItemStack subResult = getRecipeIcon(subRecipe);
            if (subResult == null) {
                return false;
            }
            int perBatch = Math.max(1, subResult.stackSize);
            int batches = (shortfall + perBatch - 1) / perBatch;

            inProgress.add(key);
            boolean subOk = canSatisfy(subRecipe.matrix, batches, stock, inProgress, depth + 1, steps);
            inProgress.remove(key);

            if (!subOk) {
                return false;
            }
            stock.put(key, (perBatch * batches) - shortfall);

            if (steps != null) {
                int totalCrafted = perBatch * batches;
                ChainStep step = steps.get(key);
                if (step == null) {
                    step = new ChainStep();
                    step.displayName = subResult.getDisplayName();
                    steps.put(key, step);
                }
                step.quantity += totalCrafted;
            }
        }
        return true;
    }

    private StoredRecipe findRecipeProducing(ItemStack template) {
        for (StoredRecipe recipe : getInventory().recipes) {
            ItemStack result = getRecipeIcon(recipe);
            if (result != null && ContainerStorageCoreCrafting.isRecipeItemValid(template, result)) {
                return recipe;
            }
        }
        return null;
    }

    private Map<String, Integer> buildVirtualStock() {
        Map<String, Integer> stock = new HashMap<String, Integer>();
        for (ItemStack group : getInventory().inventory) {
            addStock(stock, group);
        }
        for (ItemStack stack : this.mc.thePlayer.inventory.mainInventory) {
            if (stack != null) {
                addStock(stock, stack);
            }
        }
        return stock;
    }

    private void addStock(Map<String, Integer> stock, ItemStack stack) {
        String key = itemKey(stack);
        Integer existing = stock.get(key);
        stock.put(key, (existing == null ? 0 : existing) + stack.stackSize);
    }

    private static String itemKey(ItemStack stack) {
        String name = GameData.getItemRegistry()
            .getNameForObject(stack.getItem());
        return name + "@" + stack.getItemDamage();
    }

    private void drawCraftLabel(int x, int y, int color) {
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
        fontRendererObj.drawString(CRAFT_LABEL, drawX, drawY, color);
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

    /**
     * Small diamond badge in the top-left corner -- deliberately a different shape from the full
     * insufficient-materials X, so "not enough materials, but auto-craftable via another saved recipe"
     * doesn't read as "blocked" at a glance.
     */
    private void drawChainableMark(int x, int y) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);

        Tessellator tessellator = Tessellator.instance;
        int cx = x + 4;
        int cy = y + 4;

        drawDiamond(tessellator, cx, cy, 4, 0.0f, 0.0f, 0.0f, 0.95f);
        drawDiamond(tessellator, cx, cy, 3, 0.33f, 0.81f, 1.0f, 0.95f);

        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }

    private void drawDiamond(Tessellator tessellator, int cx, int cy, int r, float red, float green, float blue,
        float alpha) {
        tessellator.startDrawingQuads();
        tessellator.setColorRGBA_F(red, green, blue, alpha);
        tessellator.addVertex(cx, cy - r, 0);
        tessellator.addVertex(cx - r, cy, 0);
        tessellator.addVertex(cx, cy + r, 0);
        tessellator.addVertex(cx + r, cy, 0);
        tessellator.draw();
    }

    /** One entry in a chain-craft preview: how much of an item will get auto-crafted along the way. */
    private static final class ChainStep {

        String displayName;
        int quantity;
    }
}
