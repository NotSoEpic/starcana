package dindcrzy.starcana.blocks;

import dindcrzy.starcana.Starcana;
import dindcrzy.starcana.blocks.ArcaneTable.ArcaneTable;
import dindcrzy.starcana.blocks.ArcaneTable.ArcaneTableEntity;
import dindcrzy.starcana.blocks.ArcaneTable.ArcaneTableScreenHandler;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;

public class ModBlocks {
    public static final ArcaneTable ARCANE_TABLE = new ArcaneTable(FabricBlockSettings.of(Material.STONE));
    public static final BlockEntityType<ArcaneTableEntity> ARCANE_TABLE_ENTITY = Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            Starcana.id("arcane_table_entity"),
            FabricBlockEntityTypeBuilder.create(ArcaneTableEntity::new, ARCANE_TABLE).build()
    );
    public static final ScreenHandlerType<ArcaneTableScreenHandler> ARCANE_TABLE_SCREEN_HANDLER =
            Registry.register(Registries.SCREEN_HANDLER, Starcana.id("arcane_table_screen"),
                    new ScreenHandlerType<>(ArcaneTableScreenHandler::new, FeatureFlags.VANILLA_FEATURES)
            );


    public static void register() {
        register("arcane_table", ARCANE_TABLE);
    }

    private static void register(String id, Block block) {
        Registry.register(Registries.BLOCK, Starcana.id(id), block);
        Registry.register(Registries.ITEM, Starcana.id(id), new BlockItem(block, new FabricItemSettings()));
    }
}
