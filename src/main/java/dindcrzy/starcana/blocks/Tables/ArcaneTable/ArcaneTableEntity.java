package dindcrzy.starcana.blocks.Tables.ArcaneTable;

import dindcrzy.starcana.blocks.ImplementedInventory;
import dindcrzy.starcana.blocks.ModBlocks;
import dindcrzy.starcana.blocks.StarlightInventory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ArcaneTableEntity extends StarlightInventory implements NamedScreenHandlerFactory, ImplementedInventory, BlockEntityTicker<ArcaneTableEntity> {
    private ScreenHandler handler = null;
    private final DefaultedList<ItemStack> items = DefaultedList.ofSize(9, ItemStack.EMPTY);
    protected final PropertyDelegate propertyDelegate = new PropertyDelegate() {
        @Override
        public int get(int index) {
            switch (index) {
                case 0 -> {
                    return ArcaneTableEntity.this.getStarlight();
                }
                case 1 -> {
                    return ArcaneTableEntity.this.capacity;
                }
            }
            return 0;
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> {
                    ArcaneTableEntity.this.setStarlight(value);
                }
            }
        }

        @Override
        public int size() {
            return 2;
        }
    };
    public ArcaneTableEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.ARCANE_TABLE_ENTITY, pos, state, 200);
    }

    void setHandler(ScreenHandler handler) {
        this.handler = handler;
    }
    void clearHandler() {
        this.handler = null;
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (this.handler != null) {
            handler.onContentChanged(this);
        }
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable(getCachedState().getBlock().getTranslationKey());
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        handler = new ArcaneTableScreenHandler(syncId, playerInventory, this, propertyDelegate, ScreenHandlerContext.create(world, pos));
        return handler;
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return items;
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
    public void tick(World world, BlockPos pos, BlockState state, ArcaneTableEntity blockEntity) {
        changeStarlight(1);
        markDirty();
    }
}
