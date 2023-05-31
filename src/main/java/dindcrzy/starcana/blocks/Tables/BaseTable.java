package dindcrzy.starcana.blocks.Tables;

import dindcrzy.starcana.blocks.ImplementedInventory;
import dindcrzy.starcana.blocks.StarlightInventory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class BaseTable extends StarlightInventory implements NamedScreenHandlerFactory, ImplementedInventory {
    private final DefaultedList<ItemStack> items;
    public BaseTable(BlockEntityType<?> type, BlockPos pos, BlockState state, int slots, int capacity) {
        super(type, pos, state, capacity);
        items = DefaultedList.ofSize(slots);
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return items;
    }

    @Override
    public Text getDisplayName() {
        return null;
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return null;
    }
}
