package dindcrzy.starcana.blocks.Tables.ArcaneTable;

import dindcrzy.starcana.blocks.ImplementedInventory;
import dindcrzy.starcana.blocks.ModBlocks;
import dindcrzy.starcana.items.ModItems;
import dindcrzy.starcana.recipes.ArcaneTableRecipe;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("deprecation")
public class ArcaneTable extends BlockWithEntity {
    public static final Text TITLE = Text.translatable("container.starcana.arcane_table");

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

    private static final VoxelShape topShape = Block.createCuboidShape(0, 10, 0, 16, 16, 16);
    private static final VoxelShape stemShape = Block.createCuboidShape(5, 3, 5, 11, 10,  11);
    private static final VoxelShape baseShape = Block.createCuboidShape(1, 0, 1, 15, 3, 15);
    private static final VoxelShape shape = VoxelShapes.union(topShape, stemShape, baseShape);
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return shape;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return shape;
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
                    ArcaneTableRecipe recipe = match.get();
                    int i = 0;
                    AtomicBoolean hasItems = new AtomicBoolean(true);
                    while (hasItems.get() && recipe.canCraft(tileEntity.getStarlight())) {
                        tileEntity.changeStarlight(-recipe.getStarlight());
                        items.forEach(item -> {
                            item.decrement(1);
                            hasItems.set(hasItems.get() & !item.isEmpty());
                        });
                        i++;
                    }
                    if (i > 0) {
                        ItemStack result = match.get().getOutput().copyWithCount(i);
                        ItemEntity e = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5,
                                result);
                        world.spawnEntity(e);
                    }
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

    @Nullable
    @Override
    public NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof ArcaneTableEntity entity) {
            return new SimpleNamedScreenHandlerFactory((
                    (syncId, playerInventory, player) -> {
                        ScreenHandler handler = new ArcaneTableScreenHandler(syncId, playerInventory, entity,
                                entity.propertyDelegate, ScreenHandlerContext.create(world, pos));
                        entity.setHandler(handler);
                        return handler;
                    }),
                    TITLE);
        }
        return null;
    }

    // drops items when broken
    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            if (world.getBlockEntity(pos) instanceof Inventory invEntity) {
                ItemScatterer.spawn(world, pos, invEntity);
                world.updateComparators(pos, this);
            }
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    @Override
    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return ScreenHandler.calculateComparatorOutput(world.getBlockEntity(pos));
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, ModBlocks.ARCANE_TABLE_ENTITY, (world1, pos, state1, be) -> be.tick(world1, pos, state1, be));
    }
}
