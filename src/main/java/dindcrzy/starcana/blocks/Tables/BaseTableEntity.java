package dindcrzy.starcana.blocks.Tables;

import dindcrzy.starcana.blocks.ImplementedInventory;
import dindcrzy.starcana.blocks.StarlightInventory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;

public abstract class BaseTableEntity extends StarlightInventory implements NamedScreenHandlerFactory, ImplementedInventory {
    protected ScreenHandler handler = null;
    protected final DefaultedList<ItemStack> items;
    public final PropertyDelegate propertyDelegate = new PropertyDelegate() {
        @Override
        public int get(int index) {
            switch (index) {
                case 0 -> {
                    return BaseTableEntity.this.getStarlight();
                }
                case 1 -> {
                    return BaseTableEntity.this.capacity;
                }
            }
            return 0;
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> {
                    BaseTableEntity.this.setStarlight(value);
                }
            }
        }

        @Override
        public int size() {
            return 2;
        }
    };
    public BaseTableEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int slots, int capacity) {
        super(type, pos, state, capacity);
        items = DefaultedList.ofSize(slots, ItemStack.EMPTY);
    }

    public void setHandler(ScreenHandler handler) {
        this.handler = handler;
    }
    public void clearHandler() {
        this.handler = null;
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        Inventories.readNbt(nbt, items);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        Inventories.writeNbt(nbt, items);
        super.writeNbt(nbt);
    }
    @Override
    public void markDirty() {
        super.markDirty();
        if (this.handler != null) {
            handler.onContentChanged(this);
        }
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return items;
    }
}
