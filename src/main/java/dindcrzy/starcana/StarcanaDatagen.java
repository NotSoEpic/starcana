package dindcrzy.starcana;

import dindcrzy.starcana.advancements.Advancements;
import dindcrzy.starcana.blocks.Tables.ArcaneTable.ArcaneTable;
import dindcrzy.starcana.blocks.ModBlocks;
import dindcrzy.starcana.items.ConstellationNotes;
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
            energies(builder);
            constellations(builder);

            // because java edition is well-made, color is reset when formatting is applied
            builder.add("starcana.debug.full_constellation", """
                    
                    §e§lConstellation:§r %s
                    
                    Moon Position:§r %s
                    Phase:§r (full) - %s§r (waning) - %s§r %s§r %s§r (new) - %s§r (waxing) - %s§r %s§r %s§r
                    Quadrant:§r %s %s %s
                    Visible:§r %s
                    
                    §lEnergies:§r %s""");
            builder.add(ConstellationNotes.MOON_KEY, "Moon");
        }

        private void items(TranslationBuilder builder) {
            builder.add(ModItems.LUX_WAND, "Lux Wand");
            builder.add(ModItems.CONSTELLATION_NOTES.getTranslationKey() + ".moon", "Lunar Notes");
            builder.add(ModItems.CONSTELLATION_NOTES.getTranslationKey() + ".stars", "Constellation Notes");
            builder.add(ModItems.CONSTELLATION_NOTES, "Celestial Paper");
        }

        private void blocks(TranslationBuilder builder) {
            builder.add(ModBlocks.ARCANE_TABLE, "Arcane Table");
            builder.add(ArcaneTable.TITLE.getString(), "Arcane Table");
        }

        private void energies(TranslationBuilder builder) {
            add(builder, StarEnergies.POWERFUL, "Powerful");
            add(builder, StarEnergies.DESTRUCTIVE, "Destructive");
            add(builder, StarEnergies.NATURAL, "Natural");
        }

        private void constellations(TranslationBuilder builder) {
            builder.add("constellation.starcana.0", "Test Constellation 0");
            builder.add("constellation.starcana.1", "Test Constellation 1");
            builder.add("constellation.starcana.2", "Test Constellation 2");
            add(builder, Constellations.PLUNGER, "Plunger");
        }

        private void add(TranslationBuilder builder, StarEnergy energy, String s) {
            builder.add(energy.getTranslationKey(), s);
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
            blockModelGen.registerParentedItemModel(ModBlocks.ARCANE_TABLE, Starcana.id("block/arcane_table"));
        }

        @Override
        public void generateItemModels(ItemModelGenerator itemModelGen) {
            itemModelGen.register(ModItems.LUX_WAND, Models.GENERATED);
            // itemModelGen.register(ModItems.CONSTELLATION_NOTES, Models.GENERATED); // uses model with predicate
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
