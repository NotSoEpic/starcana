package dindcrzy.starcana;

import dindcrzy.starcana.advancements.Advancements;
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
            blockModelGen.registerCubeAllModelTexturePool(ModBlocks.ARCANE_TABLE);
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
