package dindcrzy.starcana.blocks.Tables.ArcaneTable;

import dindcrzy.starcana.blocks.ModBlocks;
import dindcrzy.starcana.blocks.Tables.BaseTableEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ArcaneTableEntity extends BaseTableEntity implements BlockEntityTicker<ArcaneTableEntity> {
    public ArcaneTableEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.ARCANE_TABLE_ENTITY, pos, state, 9, 200);
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
    public void tick(World world, BlockPos pos, BlockState state, ArcaneTableEntity blockEntity) {
        changeStarlight(1);
        markDirty();
    }
}
