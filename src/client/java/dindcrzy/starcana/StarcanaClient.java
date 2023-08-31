package dindcrzy.starcana;

import dindcrzy.starcana.blocks.ModBlocks;
import dindcrzy.starcana.items.ConstellationNotes;
import dindcrzy.starcana.items.ModItems;
import dindcrzy.starcana.networking.ConKnowledgeHandler;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.util.Identifier;

public class StarcanaClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // called too early, @see WorldRendererMixin
        // ConstellationVisuals.init();
        ConKnowledgeHandler.register();
        HandledScreens.register(ModBlocks.ARCANE_TABLE_SCREEN_HANDLER, ArcaneTableScreen::new);

        ModelPredicateProviderRegistry.register(ModItems.CONSTELLATION_NOTES, new Identifier("notes"),
                (stack, world, entity, seed) -> ConstellationNotes.getConstellationId(stack) != null ? 1 : 0);
    }
}