package dindcrzy.starcana;

import dindcrzy.starcana.advancements.DiscoverConstellationCriterion;
import dindcrzy.starcana.blocks.ModBlocks;
import dindcrzy.starcana.commands.Commands;
import dindcrzy.starcana.items.ModItems;
import dindcrzy.starcana.recipes.ModRecipes;
import net.fabricmc.api.ModInitializer;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Starcana implements ModInitializer {
    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("starlight-arcana");
    public static Identifier id(String s) {
        return new Identifier("starcana", s);
    }

    public static final DiscoverConstellationCriterion DISCOVER_CONSTELLATION =
            Criteria.register(new DiscoverConstellationCriterion());

    @Override
    public void onInitialize() {
        ModItems.register();
        ModBlocks.register();
        StarEnergies.register();
        Constellations.register();
        Commands.register();
        ModRecipes.register();
    }
}