package dindcrzy.starcana;

import dindcrzy.starcana.blocks.ModBlocks;
import dindcrzy.starcana.networking.ConKnowledgeHandler;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

public class StarcanaClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // called too early, @see WorldRendererMixin
        // ConstellationVisuals.init();
        ConKnowledgeHandler.register();
        HandledScreens.register(ModBlocks.ARCANE_TABLE_SCREEN_HANDLER, ArcaneTableScreen::new);
    }
}