package dindcrzy.starcana;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;

public class StarcanaDatagen implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(LanguageProviderEnglish::new);
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
}
