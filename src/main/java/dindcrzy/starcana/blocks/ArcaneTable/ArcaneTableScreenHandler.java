package dindcrzy.starcana.blocks.ArcaneTable;

import dindcrzy.starcana.Helper;
import dindcrzy.starcana.blocks.ModBlocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class ArcaneTableScreenHandler extends ScreenHandler {
    private final Inventory inventory;

    // clientside constructor
    // screenHandler will automatically populate the empty inventory
    public ArcaneTableScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(9));
    }

    public ArcaneTableScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory) {
        super(ModBlocks.ARCANE_TABLE_SCREEN_HANDLER, syncId);
        this.inventory = inventory;
        inventory.onOpen(playerInventory.player);

        int m;
        int l;
        // the tables inventory
        for (m = 0; m < 3; ++m) {
            for (l = 0; l < 3; ++l) {
                addSlot(new Slot(inventory, l + m * 3, 62 + l * 18, 17 + m * 18));
            }
        }
        Helper.createPlayerInventory(this, playerInventory, 0, 0);
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
    public boolean canUse(PlayerEntity player) {
        return inventory.canPlayerUse(player);
    }
}
