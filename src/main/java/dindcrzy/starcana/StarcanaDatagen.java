package dindcrzy.starcana;

import dindcrzy.starcana.advancements.Advancements;
import dindcrzy.starcana.blocks.Tables.ArcaneTable.ArcaneTable;
import dindcrzy.starcana.blocks.ModBlocks;
import dindcrzy.starcana.items.ModItems;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.advancement.Advancement;
import net.minecraft.data.client.BlockStateModelGenerator;
import net.minecraft.data.client.ItemModelGenerator;
import net.minecraft.data.client.Models;

import java.util.function.Consumer;

public class StarcanaDatagen implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(LanguageProviderEnglish::new);
        pack.addProvider(ModelProvider::new);
        pack.addProvider(AdvancementProvider::new);
    }

    private static class LanguageProviderEnglish extends FabricLanguageProvider {

        protected LanguageProviderEnglish(FabricDataOutput dataOutput) {
            super(dataOutput);
        }

        @Override
        public void generateTranslations(TranslationBuilder builder) {
            items(builder);
            blocks(builder);
            constellations(builder);
        }

        private void items(TranslationBuilder builder) {
            builder.add(ModItems.LUX_WAND, "Lux Wand");
        }

        private void blocks(TranslationBuilder builder) {
            builder.add(ModBlocks.ARCANE_TABLE, "Arcane Table");
            builder.add(ArcaneTable.TITLE.getString(), "Arcane Table");
        }

        private void constellations(TranslationBuilder builder) {
            add(builder, Constellations.PLUNGER, "Plunger");
            add(builder, Constellations.BOX, "Box");
        }

        private void add(TranslationBuilder builder, Constellation constellation, String s) {
            builder.add(constellation.getTranslationKey(), s);
        }
    }

    private static class ModelProvider extends FabricModelProvider {

        public ModelProvider(FabricDataOutput output) {
            super(output);
        }

        @Override
        public void generateBlockStateModels(BlockStateModelGenerator blockModelGen) {
            blockModelGen.registerSimpleState(ModBlocks.ARCANE_TABLE);
            // surely there's a better option?
            blockModelGen.registerParentedItemModel(ModBlocks.ARCANE_TABLE, Starcana.id("block/" + "arcane_table"));
        }

        @Override
        public void generateItemModels(ItemModelGenerator itemModelGen) {
            itemModelGen.register(ModItems.LUX_WAND, Models.GENERATED);
        }
    }

    private static class AdvancementProvider extends FabricAdvancementProvider {

        protected AdvancementProvider(FabricDataOutput output) {
            super(output);
        }

        @Override
        public void generateAdvancement(Consumer<Advancement> consumer) {
            new Advancements().accept(consumer);
        }
    }
}
