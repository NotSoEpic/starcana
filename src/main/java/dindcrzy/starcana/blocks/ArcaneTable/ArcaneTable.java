package dindcrzy.starcana.blocks.ArcaneTable;

import dindcrzy.starcana.blocks.ImplementedInventory;
import dindcrzy.starcana.items.ModItems;
import dindcrzy.starcana.recipes.ArcaneTableRecipe;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ArcaneTable extends BlockWithEntity {
    private static final Text TITLE = Text.translatable("container.arcane_table");

    public ArcaneTable(Settings settings) {
        super(settings);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ArcaneTableEntity(pos, state);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    // tries craft / opens gui when interacted with
    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient) {
            if (player.getStackInHand(hand).isOf(ModItems.LUX_WAND) &&
                    world.getBlockEntity(pos) instanceof ArcaneTableEntity tileEntity) {
                DefaultedList<ItemStack> items = tileEntity.getItems();
                Optional<ArcaneTableRecipe> match = world.getRecipeManager()
                        .getFirstMatch(ArcaneTableRecipe.Type.INSTANCE,
                                ImplementedInventory.of(items), world);
                if (match.isPresent()) {
                    items.forEach(i -> i.decrement(1));
                    ItemEntity e = new ItemEntity(world, pos.getX(), pos.getY() + 1, pos.getZ(),
                            match.get().getOutput());
                    world.spawnEntity(e);
                }
            } else {
                NamedScreenHandlerFactory screenHandlerFactory = state.createScreenHandlerFactory(world, pos);
                if (screenHandlerFactory != null) {
                    player.openHandledScreen(screenHandlerFactory);
                }
            }
        }
        return ActionResult.SUCCESS;
    }

    // drops items when broken
    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return ScreenHandler.calculateComparatorOutput(world.getBlockEntity(pos));
    }
}
