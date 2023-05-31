package dindcrzy.starcana.blocks.Tables.ArcaneTable;

import dindcrzy.starcana.Helper;
import dindcrzy.starcana.blocks.ImplementedInventory;
import dindcrzy.starcana.blocks.ModBlocks;
import dindcrzy.starcana.blocks.PreviewSlot;
import dindcrzy.starcana.recipes.ArcaneTableRecipe;
import dindcrzy.starcana.recipes.BaseTableRecipe;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

public class ArcaneTableScreenHandler extends ScreenHandler {
    private final ImplementedInventory inventory;
    private final PropertyDelegate propertyDelegate;
    private final ScreenHandlerContext context;
    private final CraftingResultInventory result = new CraftingResultInventory();
    private final PlayerEntity player;

    // clientside constructor
    // screenHandler will automatically populate the empty inventory
    public ArcaneTableScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, ImplementedInventory.ofSize(9), new ArrayPropertyDelegate(2), ScreenHandlerContext.EMPTY);
    }

    public ArcaneTableScreenHandler(int syncId, PlayerInventory playerInventory, ImplementedInventory inventory, PropertyDelegate propertyDelegate, ScreenHandlerContext context) {
        super(ModBlocks.ARCANE_TABLE_SCREEN_HANDLER, syncId);
        this.inventory = inventory;
        this.propertyDelegate = propertyDelegate;
        this.context = context;
        this.player = playerInventory.player;
        ArcaneTableScreenHandler.checkDataCount(propertyDelegate, 2);
        inventory.onOpen(playerInventory.player);

        addSlot(new PreviewSlot(result, 0, 120, 35));
        int m;
        int l;
        // the tables inventory
        for (m = 0; m < 3; ++m) {
            for (l = 0; l < 3; ++l) {
                addSlot(new Slot(inventory, l + m * 3, 62 + l * 18, 17 + m * 18));
            }
        }
        Helper.createPlayerInventory(this, playerInventory, 0, 20);
        addProperties(propertyDelegate);

        context.run((world, pos) -> {
            updateResult(this, world, player, this.inventory, result);
        });
    }

    @Override
    public void onContentChanged(Inventory inventory) {
        context.run((world, pos) -> updateResult(this, world, player, this.inventory, result));
    }

    private static void updateResult(ScreenHandler handler, World world, PlayerEntity playerEntity, ImplementedInventory inventory, CraftingResultInventory resultInventory) {
        BaseTableRecipe recipe = world.getRecipeManager().getFirstMatch(ArcaneTableRecipe.Type.INSTANCE, inventory, world).orElse(null);
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
        if (inventory instanceof ArcaneTableEntity arcaneTable) {
            arcaneTable.clearHandler();
        }
    }

    public ScreenHandlerContext getContext() {
        return context;
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
