package dindcrzy.starcana;

import dindcrzy.starcana.networking.ConKnowledgeHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.DimensionRenderingRegistry;

public class StarcanaClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // called too early, @see WorldRendererMixin
        // ConstellationVisuals.init();
        ConKnowledgeHandler.register();
    }
}