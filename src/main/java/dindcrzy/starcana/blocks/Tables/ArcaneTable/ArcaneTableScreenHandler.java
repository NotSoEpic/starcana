package dindcrzy.starcana.blocks.Tables.ArcaneTable;

import dindcrzy.starcana.Helper;
import dindcrzy.starcana.blocks.ImplementedInventory;
import dindcrzy.starcana.blocks.ModBlocks;
import dindcrzy.starcana.blocks.PreviewSlot;
import dindcrzy.starcana.blocks.Tables.BaseTableScreenHandler;
import dindcrzy.starcana.recipes.ArcaneTableRecipe;
import dindcrzy.starcana.recipes.BaseTableRecipe;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;

public class ArcaneTableScreenHandler extends BaseTableScreenHandler {
    public ArcaneTableScreenHandler(int syncId, PlayerInventory playerInventory) {
        super(ModBlocks.ARCANE_TABLE_SCREEN_HANDLER, syncId, playerInventory, ImplementedInventory.ofSize(9), new ArrayPropertyDelegate(2), ScreenHandlerContext.EMPTY);
    }

    public ArcaneTableScreenHandler(int syncId, PlayerInventory playerInventory, ImplementedInventory inventory, PropertyDelegate propertyDelegate, ScreenHandlerContext context) {
        super(ModBlocks.ARCANE_TABLE_SCREEN_HANDLER, syncId, playerInventory, inventory, propertyDelegate, context);
    }

    @Override
    protected void addSlots() {
        addSlot(new PreviewSlot(result, 0, 120, 35));
        int m;
        int l;
        // the tables inventory
        for (m = 0; m < 3; ++m) {
            for (l = 0; l < 3; ++l) {
                addSlot(new Slot(inventory, l + m * 3, 62 + l * 18, 17 + m * 18));
            }
        }
        Helper.createPlayerInventory(this, player.getInventory(), 0, 20);
    }

    @Override
    public RecipeType<? extends BaseTableRecipe> getRecipeType() {
        return ArcaneTableRecipe.Type.INSTANCE;
    }
}
