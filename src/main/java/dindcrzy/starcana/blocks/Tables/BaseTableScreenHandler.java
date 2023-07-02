package dindcrzy.starcana.blocks.Tables;

import dindcrzy.starcana.blocks.ImplementedInventory;
import dindcrzy.starcana.recipes.BaseTableRecipe;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

public abstract class BaseTableScreenHandler extends ScreenHandler {
    protected final ImplementedInventory inventory;
    private final PropertyDelegate propertyDelegate;
    private final ScreenHandlerContext context;
    protected final CraftingResultInventory result = new CraftingResultInventory();
    protected final PlayerEntity player;

    public BaseTableScreenHandler(ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, ImplementedInventory inventory, PropertyDelegate propertyDelegate, ScreenHandlerContext context) {
        super(type, syncId);
        this.inventory = inventory;
        this.propertyDelegate = propertyDelegate;
        this.context = context;
        this.player = playerInventory.player;
        ScreenHandler.checkDataCount(propertyDelegate, 2);
        inventory.onOpen(player);
        addSlots();
        addProperties(propertyDelegate);
        context.run((world, pos) -> updateResult(getRecipeType(), this, world, player, this.inventory, result));
    }

    protected abstract void addSlots();

    @Override
    public void onContentChanged(Inventory inventory) {
        context.run((world, pos) -> updateResult(getRecipeType(), this, world, player, this.inventory, result));
    }

    public abstract RecipeType<? extends BaseTableRecipe> getRecipeType();

    private static void updateResult(RecipeType<? extends BaseTableRecipe> recipeType, ScreenHandler handler, World world, PlayerEntity playerEntity, ImplementedInventory inventory, CraftingResultInventory resultInventory) {
        BaseTableRecipe recipe = world.getRecipeManager().getFirstMatch(recipeType, inventory, world).orElse(null);
        ItemStack out = ItemStack.EMPTY;
        if (recipe != null) {
            out = recipe.getOutput();
        }
        resultInventory.setStack(0, out);
        handler.setPreviousTrackedSlot(0, out);
        ((ServerPlayerEntity)playerEntity).networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(handler.syncId, handler.nextRevision(), 0, out));
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slotId) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = slots.get(slotId);
        if (slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            stack = originalStack.copy();
            if (slotId < inventory.size()) {
                if (!insertItem(originalStack, inventory.size(), slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!insertItem(originalStack, 0, inventory.size(), false)) {
                return ItemStack.EMPTY;
            }

            if (originalStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }

        return stack;
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        if (inventory instanceof BaseTableEntity baseTable) {
            baseTable.clearHandler();
        }
    }

    public ImplementedInventory getInventory() {
        return inventory;
    }

    public int getStarlight() {
        return propertyDelegate.get(0);
    }

    public float getStarlightFract() {
        return getStarlightFract(propertyDelegate.get(0));
    }
    public float getStarlightFract(int s) {
        return (float)s / propertyDelegate.get(1);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return inventory.canPlayerUse(player);
    }
}
